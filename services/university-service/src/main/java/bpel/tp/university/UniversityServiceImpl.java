package bpel.tp.university;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.jws.WebService;

@WebService(
  serviceName   = "UniversityService",
  portName      = "UniversityPort",
  endpointInterface = "bpel.tp.university.UniversityPT",
  targetNamespace   = "http://tp.bpel/university/",
  wsdlLocation  = "classpath:university.wsdl"
)
public class UniversityServiceImpl implements UniversityPT {
    private static final Map<String, String> STATUS = new ConcurrentHashMap<>();
    private static final AtomicInteger NEXT_ID = new AtomicInteger(10000);

    @Override public RegisterResponse register(RegisterRequest req) {
        String studentId = "STU-" + NEXT_ID.incrementAndGet();
        STATUS.put(studentId, "REGISTERED");
        System.out.println("[UNIVERSITY] " + studentId + " REGISTERED: "
                + req.getStudent().getName() + " (" + req.getStudent().getProgram() + ")");
        RegisterResponse r = new RegisterResponse();
        r.setStudentId(studentId);
        return r;
    }

    @Override public void cancel(CancelRequest req) {
        STATUS.put(req.getStudentId(), "CANCELLED");
        System.out.println("[UNIVERSITY] " + req.getStudentId() + " CANCELLED");
    }
}
