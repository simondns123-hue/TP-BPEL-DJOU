package bpel.tp.transport;

import javax.servlet.ServletConfig;
import javax.xml.ws.Endpoint;
import org.apache.cxf.BusFactory;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;

public class TransportServlet extends CXFNonSpringServlet {
    @Override
    protected void loadBus(ServletConfig config) {
        super.loadBus(config);
        BusFactory.setDefaultBus(getBus());
        Endpoint.publish("/TransportService", new TransportServiceImpl());
    }
}
