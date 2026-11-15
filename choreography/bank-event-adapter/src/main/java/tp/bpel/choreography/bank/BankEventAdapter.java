package tp.bpel.choreography.bank;

import bpel.tp.bank.BankServiceImpl;
import bpel.tp.bank.PayRequest;
import bpel.tp.bank.PayResponse;
import java.math.BigDecimal;
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
public class BankEventAdapter {
  public static void main(String[] a) {
    Event.defaultSource = "bank-service";
    SpringApplication.run(BankEventAdapter.class, a);
  }

  @Bean
  BankServiceImpl bankService() { return new BankServiceImpl(); }
}

@Component
class BankListener {
  @Autowired private JmsTemplate jms;
  @Autowired private BankServiceImpl bank;

  @JmsListener(destination = "StudentRegistered")
  public void onStudentRegistered(String json) throws Exception {
    Event e = Event.parse(json);
    PayRequest req = new PayRequest();
    req.setStudentId(e.data.get("studentId").asText());
    req.setAmount(new BigDecimal(e.data.get("amount").asText()));
    PayResponse r = bank.pay(req);

    Event out = Event.of("PaymentDecided", e.correlationId,
        Map.of("studentId", e.data.get("studentId").asText(),
               "approved",  r.isApproved(),
               "receipt",   r.getReceipt()));
    System.out.println("[BANK] " + e.correlationId + " " + req.getStudentId()
        + " amount=" + req.getAmount() + " -> " + (r.isApproved() ? "APPROVED" : "REFUSED"));
    jms.convertAndSend("PaymentDecided", out.toJson(), m -> {
      m.setBooleanProperty("approved", r.isApproved());
      return m;
    });
  }
}
