import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Писатель логов в текстовый файл
 */
public class Logger {
    /**
     * Записывает сообщение в файл
     * @param logfile - файл
     * @param msg - сообщение
     * @return
     */
    public static boolean writeMsgToFile(String logfile,Msg msg) {
        try {
                 Files.writeString(
                    Paths.get(logfile), msg.toString()+"\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Записывает строку в файл
     * @param logfile - файл
     * @param str - строка
     * @return
     */
    public static boolean writeStringToFile(String logfile, String str) {
        try {
            Files.writeString(Paths.get(logfile), str+"\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Создает директорию если ее еще нет
     * @param logfile - директория
     * @throws IOException
     */
    public static void createLogDir(String logfile) throws IOException {
        Files.createDirectories(Paths.get(logfile).getParent().getFileName());
    }
}
