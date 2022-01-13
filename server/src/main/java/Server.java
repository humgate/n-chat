import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;


public class Server {
    static final String IP_ADDRESS = "localhost";
    static final short PORT = 23334;
    static Scanner scanner = new Scanner(System.in);


    public static void main(String[] args) throws IOException {
        Logger.createLogDir(Logger.SERVER_LOG_FILE);
        // Занимаем порт, определяя серверный сокет
        final ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(IP_ADDRESS, PORT));
        System.out.println("Сервер запущен...");

        MessageBroker msgBroker = new MessageBroker();
        ClientHandler clientHandler = new ClientHandler(serverChannel, msgBroker);
        msgBroker.setClientHandler(clientHandler);

        Thread clientHandlerThread = new Thread(clientHandler, "clientHandlerThread");
        clientHandlerThread.start();

        Thread messageBrokerThread = new Thread(msgBroker, "messageBrokerThread");
        messageBrokerThread.start();

        while (true) {
            System.out.println("Введите команду Stop для остановки сервера...");
            String msg = scanner.nextLine();
            if ("stop".equals(msg)) {
                clientHandlerThread.interrupt();
                messageBrokerThread.interrupt();
                break;
            }
        }
    }
}


