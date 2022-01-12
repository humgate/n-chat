import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Msg {
    private String client;
    private String message;
    LocalDateTime stamp;

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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
