package tp.bpel.choreography.client;

import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import tp.bpel.choreography.event.Event;

@SpringBootApplication
@EnableJms
public class EnrollmentClient implements CommandLineRunner {
  @Autowired private JmsTemplate jms;

  public static void main(String[] a) {
    Event.defaultSource = "student-client";
    SpringApplication.run(EnrollmentClient.class, a);
  }

  @Override
  public void run(String... args) throws Exception {
    String amount = args.length > 0 ? args[0] : "350000";
    String correlationId = "enr-" + UUID.randomUUID().toString().substring(0, 8);
    Event request = Event.of("EnrollmentRequested", correlationId,
        Map.of("student", Map.of("name", "Alice", "program", "ISI4"), "amount", amount));
    System.out.println("[CLIENT] " + correlationId + " -> EnrollmentRequested amount=" + amount);
    jms.convertAndSend("EnrollmentRequested", request.toJson());
  }

  @JmsListener(destination = "EnrollmentCompleted")
  public void onEnrollmentCompleted(String json) throws Exception {
    Event e = Event.parse(json);
    System.out.println("[CLIENT] " + e.correlationId + " <- EnrollmentCompleted " + e.data);
  }
}
