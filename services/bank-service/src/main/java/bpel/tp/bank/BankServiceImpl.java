package bpel.tp.bank;

import java.math.BigDecimal;
import java.util.UUID;
import javax.jws.WebService;

@WebService(
  serviceName   = "BankService",
  portName      = "BankPort",
  endpointInterface = "bpel.tp.bank.BankPT",
  targetNamespace   = "http://tp.bpel/bank/",
  wsdlLocation  = "classpath:bank.wsdl"
)
public class BankServiceImpl implements BankPT {
    @Override public PayResponse pay(PayRequest req) {
        PayResponse r = new PayResponse();
        // Rule for the demo: amount > 500_000 FCFA → payment refused
        boolean ok = req.getAmount().compareTo(new BigDecimal("500000")) <= 0;
        r.setApproved(ok);
        r.setReceipt(ok ? "RCT-" + UUID.randomUUID() : "N/A");
        return r;
    }
}
