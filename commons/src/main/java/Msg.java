import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Msg {
    private final String client;
    private final String message;
    private final LocalDateTime stamp;

    public String getClient() {
        return client;
    }

    public String getMessage() {
        return message;
    }

    public Msg(String client, String message, LocalDateTime stamp) {
        this.client = client;
        this.message = message;
        this.stamp = stamp;
    }

    @Override
    public String toString() {
        return stamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)+ ": "+ client + ": "+ message;
    }
}
