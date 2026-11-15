package bpel.tp.transport;

import java.util.UUID;
import javax.jws.WebService;

@WebService(
  serviceName   = "TransportService",
  portName      = "TransportPort",
  endpointInterface = "bpel.tp.transport.TransportPT",
  targetNamespace   = "http://tp.bpel/transport/",
  wsdlLocation  = "classpath:transport.wsdl"
)
public class TransportServiceImpl implements TransportPT {

    @Override public ActivateResponse activatePass(ActivateRequest req) {
        String passId = "PASS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        System.out.println("[TRANSPORT] " + req.getStudentId() + " -> " + passId);
        ActivateResponse r = new ActivateResponse();
        r.setPassId(passId);
        return r;
    }
}
