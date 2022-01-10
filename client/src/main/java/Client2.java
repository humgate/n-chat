import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Client2 {
    static final String IP_ADDRESS = "localhost";
    static final short PORT = 23334;
    static final int BUFFER_SIZE = 2 << 8;
    static SocketChannel socketChannel = null;
    String name;


    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String message = null;

        Client2 client = new Client2();

        // Определяем сокет сервера
        InetSocketAddress socketAddress = new InetSocketAddress(IP_ADDRESS, PORT);

        try {
            socketChannel = SocketChannel.open();
            // подключаемся к серверу
            socketChannel.connect(socketAddress);

            //устанавливаем blocking IO см. комментарии ниже
            socketChannel.configureBlocking(true);

            // Определяем буфер для получения и отправки данных
            final ByteBuffer inputBuffer = ByteBuffer.allocate(BUFFER_SIZE);
            ByteBuffer outputBuffer;

            /*
             * Зачитывание и отображение ответов сервера сделано в отдельном потоке, который
             * получает команду interrupt из основного потока если пользователь решил закончить работу
             * введя end.
             */
            Thread readerThread = new Thread(() -> {
                String threadName = Thread.currentThread().getName();
                int bytesCount = 0;
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        bytesCount = socketChannel.read(inputBuffer);
                        if (bytesCount == -1) break;
                        String msg = new String(inputBuffer.array(), 0, bytesCount,
                                StandardCharsets.UTF_8).trim();
                        if (!msg.split(":")[0].equals(client.name)) {
                            System.out.print(msg + "\n");
                        }
                        inputBuffer.clear();
                    } catch (ClosedByInterruptException e) {
                        //IO у нас блокирующий, поэтому нужно поймать это исключение
                        System.out.println(threadName + " завершил мониторинг ответов от сервера");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, "readerThread");
            readerThread.start();

            while (true) {
                if (client.name == null) {
                    System.out.println("Введите имя: ");
                    client.name = scanner.nextLine();
                    outputBuffer = ByteBuffer.wrap(("Connect " + client.name).getBytes(StandardCharsets.UTF_8));
                } else {
                    message = scanner.nextLine();
                    outputBuffer = ByteBuffer.wrap((message).getBytes(StandardCharsets.UTF_8));
                }

                if ("exit".equals(message)) {
                    readerThread.interrupt();
                    socketChannel.close();
                    break;
                }

                //пишем в канал
                int writtenBytes = socketChannel.write(outputBuffer);
               }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
