import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.Scanner;

public class Server {
    static Scanner scanner = new Scanner(System.in);


    public static void main(String[] args) throws IOException {
        Logger.createLogDir(Config.SERVER_LOG_FILE);
        // Занимаем порт, определяя серверный сокет
        final ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(Config.IP_ADDRESS, Config.PORT));
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


