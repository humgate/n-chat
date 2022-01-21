import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * Зачитывает параметры приложения из файла настроек в блоке статической инициализации
 * и хранит в себе в константах значения параметров приложения. Если зачитывание параметров каким-то образом
 * не прошло, - это критическая проблема и дальше падаем с RuntimeException.
 */
public class Config {
    /*
     * Файл настроек зачитывается из домашней директории пользователя.
     * Это сделано из-за того, что юнит тесты могут запускаться проверяющим из IDEA, из maven и еще каким-либо
     * другим способом. В каждом случае рабочая директория запуска приложения и рабочая директория запуска тестов
     * могут отличаться. Так IDEA по умолчанию считает рабочим фолдером приложения корень проектной папки,
     * а рабочей директорией запуска тестов - корень папки модуля. Из-за этого для того, чтобы тесты работали,
     * приходится выполнять спец. настройки IDEA - указывать рабочую директорию запуска тестов. При этом, если в
     * IDEA это делается очень просто, то для maven после упорных поисков так и не удалось найти как это настроить.
     * А ведь у проверяющего может быть eclipse итд. Поэтому, чтобы исключить данную проблему, файл с настройками
     * зачитывается по относительному к user.home пути (а не к текущей директории). Лог файлы клиента и сервера также
     * создаются по относительному к user.home пути. Таким образом запуская и программу и юнит тесты любым способом,
     * в любом случае поиск файлов идет относительно user.home директории и описанная проблема устраняется.
     *
     * Файл settings.txt в корне проектной папки там просто лежит как пример, его необходимо скопировать перед
     * запуском приложения или тестов в user.home
     *
     */
    static final String SETTINGS_FILE = System.getProperty("user.home")  + System.getProperty("file.separator") +
            "settings.txt";

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

            SERVER_LOG_FILE = System.getProperty("user.home")  + System.getProperty("file.separator")
                    + keyValues.get("server_log_file").replace(":", System.getProperty("file.separator"));

            CLIENT_LOG_FILE = System.getProperty("user.home")  + System.getProperty("file.separator")
                    + keyValues.get("client_log_file").replace(":", System.getProperty("file.separator"));

            CLIENT_CONNECTION_MSG_PFX = keyValues.get("client_connection_msg_pfx");

        } catch (NumberFormatException | IOException | NullPointerException e ) {
            //Если что-то не прочиталось или не распарсилось - выходим
            System.out.println(Thread.currentThread().getName()+
                    ": Ошибка чтения настроек приложения из файла настроек. Завершение работы");
            throw new RuntimeException(e.getMessage());
        }
    }
}
