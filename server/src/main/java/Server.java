import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.Scanner;


public class Server {
    static final String IP_ADDRESS = "localhost";
    static final short PORT = 23334;
    static final int BUFFER_SIZE = 2 << 20;
    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws IOException {
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
            System.out.println("Введите команду Stop для остановки...");
            String msg = scanner.nextLine();
            if ("Stop".equals(msg)) {
                clientHandlerThread.interrupt();
                break;
            }
        }


    }
}


