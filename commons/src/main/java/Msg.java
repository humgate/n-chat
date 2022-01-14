import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Реализует сообщение клиента
 */
public class Msg {
    //имя в чате
    private final String client;
    //текст сообщения
    private final String message;
    //таймстемп сообщения
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
