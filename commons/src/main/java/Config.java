import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class Config {
    static final String SETTINGS_FILE = "settings.txt";

    static final String IP_ADDRESS;
    static final int PORT;
    static final int BUFFER_SIZE;

    static {
        final HashMap<String, String> keyValues = new HashMap<>();
        BufferedReader br;
        String line;

        try {
            br = new BufferedReader(new FileReader(SETTINGS_FILE));
            while ((line = br.readLine()) != null) {
                keyValues.put(line.split(" ")[0], line.split(" ")[1]);
            }
            IP_ADDRESS = keyValues.get("IP_address");
            if (IP_ADDRESS == null) throw new RuntimeException();
            PORT = Integer.parseInt(keyValues.get("port"));
            BUFFER_SIZE = Integer.parseInt(keyValues.get("buffer_size"));

        } catch (NumberFormatException | IOException e) {
            System.out.println("Ошибка чтения настроек приложения. Завершение работы");
            throw new RuntimeException(e.getMessage());
        }
    }
}
