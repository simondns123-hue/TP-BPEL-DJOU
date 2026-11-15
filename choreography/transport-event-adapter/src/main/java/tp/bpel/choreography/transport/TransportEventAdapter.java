package tp.bpel.choreography.transport;

import bpel.tp.transport.ActivateRequest;
import bpel.tp.transport.TransportServiceImpl;
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
public class TransportEventAdapter {
  public static void main(String[] a) {
    Event.defaultSource = "transport-service";
    SpringApplication.run(TransportEventAdapter.class, a);
  }

  @Bean
  TransportServiceImpl transportService() { return new TransportServiceImpl(); }
}

@Component
class TransportListener {
  @Autowired private JmsTemplate jms;
  @Autowired private TransportServiceImpl transport;

  @JmsListener(destination = "PaymentDecided", selector = "approved = TRUE")
  public void onPaymentApproved(String json) throws Exception {
    Event e = Event.parse(json);
    String studentId = e.data.get("studentId").asText();
    ActivateRequest req = new ActivateRequest();
    req.setStudentId(studentId);
    String passId = transport.activatePass(req).getPassId();

    Event out = Event.of("PassActivated", e.correlationId,
        Map.of("studentId", studentId, "passId", passId));
    System.out.println("[TRANSPORT] " + e.correlationId + " " + studentId + " -> " + passId);
    jms.convertAndSend("PassActivated", out.toJson());
  }
}
