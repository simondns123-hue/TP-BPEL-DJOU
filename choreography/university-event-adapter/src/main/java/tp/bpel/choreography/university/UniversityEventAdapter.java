package tp.bpel.choreography.university;

import bpel.tp.university.CancelRequest;
import bpel.tp.university.RegisterRequest;
import bpel.tp.university.UniversityServiceImpl;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import tp.bpel.choreography.event.Event;

@SpringBootApplication
@EnableJms
public class UniversityEventAdapter {
  public static void main(String[] a) {
    Event.defaultSource = "university-service";
    SpringApplication.run(UniversityEventAdapter.class, a);
  }

  @Bean
  UniversityServiceImpl universityService() { return new UniversityServiceImpl(); }
}

@Component
class UniversityListener {
  @Autowired private JmsTemplate jms;
  @Autowired private UniversityServiceImpl university;

  private final Map<String, Map<String, Object>> pending = new ConcurrentHashMap<>();

  @JmsListener(destination = "EnrollmentRequested")
  public void onEnrollmentRequested(String json) throws Exception {
    Event e = Event.parse(json);
    RegisterRequest req = new RegisterRequest();
    RegisterRequest.Student student = new RegisterRequest.Student();
    student.setName(e.data.get("student").get("name").asText());
    student.setProgram(e.data.get("student").get("program").asText());
    req.setStudent(student);
    String studentId = university.register(req).getStudentId();

    publish("StudentRegistered", e.correlationId,
        Map.of("studentId", studentId, "amount", e.data.get("amount").asText()));
  }

  @JmsListener(destination = "PaymentDecided", selector = "approved = FALSE")
  public void onPaymentRefused(String json) throws Exception {
    Event e = Event.parse(json);
    String studentId = e.data.get("studentId").asText();
    CancelRequest cancel = new CancelRequest();
    cancel.setStudentId(studentId);
    university.cancel(cancel);

    publish("EnrollmentCompleted", e.correlationId,
        Map.of("status", "REFUSED", "studentId", studentId));
  }

  @JmsListener(destination = "PaymentDecided", selector = "approved = TRUE")
  public void onPaymentApproved(String json) throws Exception {
    Event e = Event.parse(json);
    collect(e.correlationId, Map.of(
        "studentId", e.data.get("studentId").asText(),
        "receipt", e.data.get("receipt").asText()));
  }

  @JmsListener(destination = "RoomAssigned")
  public void onRoomAssigned(String json) throws Exception {
    Event e = Event.parse(json);
    collect(e.correlationId, Map.of("roomNumber", e.data.get("roomNumber").asText()));
  }

  @JmsListener(destination = "PassActivated")
  public void onPassActivated(String json) throws Exception {
    Event e = Event.parse(json);
    collect(e.correlationId, Map.of("passId", e.data.get("passId").asText()));
  }

  private synchronized void collect(String correlationId, Map<String, ?> part) throws Exception {
    Map<String, Object> state = pending.computeIfAbsent(correlationId, k -> new HashMap<>());
    state.putAll(part);
    if (state.containsKey("receipt") && state.containsKey("roomNumber") && state.containsKey("passId")) {
      pending.remove(correlationId);
      state.put("status", "ENROLLED");
      publish("EnrollmentCompleted", correlationId, state);
    }
  }

  private void publish(String type, String correlationId, Map<String, ?> data) throws Exception {
    System.out.println("[UNIVERSITY] " + correlationId + " -> " + type + " " + data);
    jms.convertAndSend(type, Event.of(type, correlationId, data).toJson());
  }
}
