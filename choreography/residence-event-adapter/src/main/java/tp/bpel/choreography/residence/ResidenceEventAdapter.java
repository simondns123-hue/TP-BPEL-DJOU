package tp.bpel.choreography.residence;

import bpel.tp.residence.AssignRequest;
import bpel.tp.residence.ResidenceServiceImpl;
import java.util.Map;
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
public class ResidenceEventAdapter {
  public static void main(String[] a) {
    Event.defaultSource = "residence-service";
    SpringApplication.run(ResidenceEventAdapter.class, a);
  }

  @Bean
  ResidenceServiceImpl residenceService() { return new ResidenceServiceImpl(); }
}

@Component
class ResidenceListener {
  @Autowired private JmsTemplate jms;
  @Autowired private ResidenceServiceImpl residence;

  @JmsListener(destination = "PaymentDecided", selector = "approved = TRUE")
  public void onPaymentApproved(String json) throws Exception {
    Event e = Event.parse(json);
    String studentId = e.data.get("studentId").asText();
    AssignRequest req = new AssignRequest();
    req.setStudentId(studentId);
    String room = residence.assignRoom(req).getRoomNumber();

    Event out = Event.of("RoomAssigned", e.correlationId,
        Map.of("studentId", studentId, "roomNumber", room));
    System.out.println("[RESIDENCE] " + e.correlationId + " " + studentId + " -> " + room);
    jms.convertAndSend("RoomAssigned", out.toJson());
  }
}
