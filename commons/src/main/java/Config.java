import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * Зачитывает параметры приложения из файла настроек в блоке статической инициализации
 * и хранит в себе в константах значения параметров приложения. Если зачитывание параметров каким-то образом
 * не прошло, это критическая проблема и дальше падаем с RuntimeException
 */
public class Config {
    //файл настроек
    static final String SETTINGS_FILE = "settings.txt";

    public static final String IP_ADDRESS;
    public static final int PORT;
    public static final int BUFFER_SIZE;
    public static final String SERVER_LOG_FILE;
    public static final String CLIENT_LOG_FILE;
    public static final String CLIENT_CONNECTION_MSG_PFX;

    //инициализация констант значениями из файла настроек
    static {
        final HashMap<String, String> keyValues = new HashMap<>();
        BufferedReader br;
        String line;

        try {
            br = new BufferedReader(new FileReader(SETTINGS_FILE));
            while ((line = br.readLine()) != null) {
                keyValues.put(line.split(" ")[0], line.split(" ")[1]);
            }

            //Если хотя бы одно значение == null, выходим
            keyValues.forEach((k, v) -> {
                if (v == null) {
                    throw new NullPointerException();
                }
            });

            IP_ADDRESS = keyValues.get("IP_address");
            PORT = Integer.parseInt(keyValues.get("port"));
            BUFFER_SIZE = Integer.parseInt(keyValues.get("buffer_size"));
            SERVER_LOG_FILE = keyValues.get("server_log_file");
            CLIENT_LOG_FILE = keyValues.get("client_log_file");
            CLIENT_CONNECTION_MSG_PFX = keyValues.get("client_connection_msg_pfx");

        } catch (NumberFormatException | IOException | NullPointerException e ) {
            //Если что-то не прочиталось или не распарсилось - выходим
            System.out.println("Ошибка чтения настроек приложения из файла настроек. Завершение работы");
            throw new RuntimeException(e.getMessage());
        }
    }
}
