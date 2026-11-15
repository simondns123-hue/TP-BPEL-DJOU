package bpel.tp.bank;

import javax.servlet.ServletConfig;
import javax.xml.ws.Endpoint;
import org.apache.cxf.BusFactory;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;

public class BankServlet extends CXFNonSpringServlet {
    @Override
    protected void loadBus(ServletConfig config) {
        super.loadBus(config);
        BusFactory.setDefaultBus(getBus());
        Endpoint.publish("/BankService", new BankServiceImpl());
    }
}
