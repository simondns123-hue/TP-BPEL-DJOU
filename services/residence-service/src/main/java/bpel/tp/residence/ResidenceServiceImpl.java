package bpel.tp.residence;

import java.util.concurrent.atomic.AtomicInteger;
import javax.jws.WebService;

@WebService(
  serviceName   = "ResidenceService",
  portName      = "ResidencePort",
  endpointInterface = "bpel.tp.residence.ResidencePT",
  targetNamespace   = "http://tp.bpel/residence/",
  wsdlLocation  = "classpath:residence.wsdl"
)
public class ResidenceServiceImpl implements ResidencePT {
    private static final AtomicInteger NEXT_ROOM = new AtomicInteger(100);

    @Override public AssignResponse assignRoom(AssignRequest req) {
        String room = "ROOM-" + NEXT_ROOM.incrementAndGet();
        System.out.println("[RESIDENCE] " + req.getStudentId() + " -> " + room);
        AssignResponse r = new AssignResponse();
        r.setRoomNumber(room);
        return r;
    }
}
