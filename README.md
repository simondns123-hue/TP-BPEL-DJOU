# TP-BPEL — Service Composition: Orchestration & Choreography

Student enrollment scenario composed of four SOAP services (University, Bank, Residence, Transport):

- **Part A — Orchestration**: a WS-BPEL 2.0 process (`EnrollmentProcess`) executed by Apache ODE.
- **Part B — Choreography**: the same scenario driven by events on ActiveMQ Artemis topics, without a central coordinator.

## Project structure

```
tp-bpel-enrollment/
├── pom.xml                         Maven parent (Java 11)
├── services/                       JAX-WS services (Apache CXF), packaged as WAR
│   ├── university-service/         register / cancel
│   ├── bank-service/               pay (refused when amount > 500 000 FCFA)
│   ├── residence-service/          assignRoom
│   └── transport-service/          activatePass
├── bpel-orchestrator/              EnrollmentProcess.bpel, EnrollmentProcess.wsdl, deploy.xml, partner WSDLs
├── choreography/                   Spring Boot + JMS (Artemis)
│   ├── event-model/                common event envelope (JSON)
│   ├── university-event-adapter/   registration, compensation, completion
│   ├── bank-event-adapter/
│   ├── residence-event-adapter/
│   ├── transport-event-adapter/
│   └── enrollment-client/          publishes EnrollmentRequested, prints EnrollmentCompleted
├── soapui/
│   └── enrollment-tests.xml        SoapUI project (4 test cases)
└── report/
    ├── TP-BPEL_DJOU_Simon.pdf      lab report
    └── Screenshots/                screenshots of the runs (Parts A and B)
```

## Prerequisites

| Tool | Version |
|---|---|
| JDK | 11 |
| Maven | 3.8+ |
| Apache Tomcat | 9.0.x |
| Apache ODE | 1.3.8 (WAR) |
| ActiveMQ Artemis | 2.33 |
| SoapUI | 5.7.x |

## Build

```bash
mvn clean install
```

## Part A — Orchestration

1. Copy the four WAR files to `TOMCAT_HOME/webapps/`:
   `services/*/target/*-service.war`
2. Copy `ode.war` to `TOMCAT_HOME/webapps/` and start Tomcat (`catalina.bat run`).
3. Check the services:
   - http://localhost:8080/university-service/UniversityService?wsdl
   - http://localhost:8080/bank-service/BankService?wsdl
   - http://localhost:8080/residence-service/ResidenceService?wsdl
   - http://localhost:8080/transport-service/TransportService?wsdl
4. Deploy the process: copy every file of `bpel-orchestrator/` into
   `TOMCAT_HOME/webapps/ode/WEB-INF/processes/enrollment/`.
5. Check the process: http://localhost:8080/ode/processes/EnrollmentService?wsdl
6. Import `soapui/enrollment-tests.xml` in SoapUI and run the TestSuite.

| Test case | amount | Expected status |
|---|---|---|
| TC1_Nominal_350000 | 350000 | ENROLLED |
| TC2_Refused_900000 | 900000 | REFUSED (University registration compensated) |
| TC3_Limit_500000 | 500000 | ENROLLED |
| TC4_Limit_500001 | 500001 | REFUSED |

## Part B — Choreography

1. Start the Artemis broker (`artemis run`, user `admin` / `admin`, port 61616).
2. Create the topics:

```bash
for %a in (EnrollmentRequested StudentRegistered PaymentDecided RoomAssigned PassActivated EnrollmentCompleted) do artemis.cmd address create --name %a --multicast --no-anycast --user admin --password admin --silent
```

3. From `choreography/`, start the four adapters (one terminal each):

```bash
java -jar university-event-adapter/target/university-event-adapter-1.0.jar
java -jar bank-event-adapter/target/bank-event-adapter-1.0.jar
java -jar residence-event-adapter/target/residence-event-adapter-1.0.jar
java -jar transport-event-adapter/target/transport-event-adapter-1.0.jar
```

4. Run the client with an amount:

```bash
java -jar enrollment-client/target/enrollment-client-1.0.jar 350000
java -jar enrollment-client/target/enrollment-client-1.0.jar 900000
```

### Event flow

| Event | Publisher | Subscribers |
|---|---|---|
| EnrollmentRequested | client | University |
| StudentRegistered | University | Bank |
| PaymentDecided (`approved` property) | Bank | Residence, Transport (`approved = TRUE`), University |
| RoomAssigned | Residence | University |
| PassActivated | Transport | University |
| EnrollmentCompleted | University | client |

When the payment is refused, the University adapter cancels the registration (compensation) and publishes `EnrollmentCompleted` with status `REFUSED`.
