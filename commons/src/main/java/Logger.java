import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Logger {

    static final String SERVER_LOG_FILE = "./serverlog/file.log";
    static final String CLIENT_LOG_FILE = "./clientlog/file.log";


    /**
     * загрузчик текстового файла
     *
     * @param fileName -имя файла
     * @return зачитанная строка или null в случае ошибки
     */
    public static String readFileBlocking(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            //чтение построчно
            StringBuilder stringBuffer = new StringBuilder();
            String str;
            while ((str = br.readLine()) != null) {
                stringBuffer.append(str);
            }
            return stringBuffer.toString();
        } catch (IOException ex) {
            System.out.println("Ошибка ввода-вывода" + ex.getMessage());
            return null;
        }
    }

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

    public static boolean writeStringToFile(String logfile, String str) {
        try {
            Files.writeString(Paths.get(logfile), str+"\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void createLogDir(String logfile) throws IOException {
        Files.createDirectories(Paths.get(logfile).getParent().getFileName());
    }
}
