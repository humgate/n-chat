import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Реализует сообщение клиента. Необходимо для того чтобы можно было хранить и манипулировать
 * коллекцией сообщений и на сервере и на клиентах.
 * В процессе выполнения задания, оказалось что это не требуется. Поэтому сообщения на сервере и клиентах
 * есть только в их System.out-ах и в их лог файлах, а в каналах передаются просто строки состоящие из
 * таймстемпа, имени клиента и его сообщения (то есть Msg.toString).
 *
 * Сам класс Msg было решено оставить, потому что в нормальном чате наверняка потребуется иметь коллекцию
 * сообщений, и плюс к тому видится более правильным обмениваться с сервером сериализованным представлением
 * этого объекта (а лучше еще и зашифрованным), а не открытой строкой.
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
