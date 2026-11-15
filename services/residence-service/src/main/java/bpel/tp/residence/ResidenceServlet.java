package bpel.tp.residence;

import javax.servlet.ServletConfig;
import javax.xml.ws.Endpoint;
import org.apache.cxf.BusFactory;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;

public class ResidenceServlet extends CXFNonSpringServlet {
    @Override
    protected void loadBus(ServletConfig config) {
        super.loadBus(config);
        BusFactory.setDefaultBus(getBus());
        Endpoint.publish("/ResidenceService", new ResidenceServiceImpl());
    }
}
