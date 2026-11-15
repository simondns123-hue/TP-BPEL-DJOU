package tp.bpel.choreography.event;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class Event {
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static String defaultSource = "unknown";

    public String eventId;
    public String correlationId;
    public String timestamp;
    public String source;
    public String type;
    public JsonNode data;

    public static Event parse(String json) throws Exception {
        return MAPPER.readValue(json, Event.class);
    }

    public static Event of(String type, String correlationId, Map<String, ?> data) {
        Event e = new Event();
        e.eventId = UUID.randomUUID().toString();
        e.correlationId = correlationId;
        e.timestamp = Instant.now().toString();
        e.source = defaultSource;
        e.type = type;
        e.data = MAPPER.valueToTree(data);
        return e;
    }

    public String toJson() throws Exception {
        return MAPPER.writeValueAsString(this);
    }
}
