import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Logger {
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
