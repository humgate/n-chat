import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Scanner;

/**
 * Client
 */
public class Client {
    static SocketChannel socketChannel = null;
    private String name;

    public static void main(String[] args) throws IOException {
        //создаем фолдер для лога если его еще нет
        Logger.createLogDir(Config.CLIENT_LOG_FILE);
        Scanner scanner = new Scanner(System.in);
        Client client = new Client();

        // Определяем сокет сервера
        InetSocketAddress socketAddress = new InetSocketAddress(Config.IP_ADDRESS, Config.PORT);

        try {
            socketChannel = SocketChannel.open();
            // подключаемся к серверу
            socketChannel.connect(socketAddress);

            //устанавливаем blocking IO см. комментарии ниже
            socketChannel.configureBlocking(true);

            // Определяем буфер для получения и отправки данных
            final ByteBuffer inputBuffer = ByteBuffer.allocate(Config.BUFFER_SIZE);
            ByteBuffer outputBuffer;

            /*
             * Зачитывание и отображение ответов сервера сделано в отдельном потоке, который
             * получает команду interrupt из основного потока если пользователь решил закончить работу
             * введя exit.
             */
            Thread readerThread = new Thread(() -> {
                String threadName = Thread.currentThread().getName();
                int bytesCount;
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        bytesCount = socketChannel.read(inputBuffer);
                        if (bytesCount == -1) break;
                        String msg = new String(inputBuffer.array(), 0, bytesCount,
                                StandardCharsets.UTF_8).trim();

                        if (!msg.startsWith(client.name)) {
                            System.out.print(msg + "\n");
                            Logger.writeStringToFile(Config.CLIENT_LOG_FILE, msg);
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

            /*
            * В основном потоке сначала формируем первое сообщение от клиента в формате сообщения
            * подключения и отправляем на сервер. Затем начинаем в цикле зачитывать ввод пользователя с
            * консоли и отправляем на сервер уже как сообщения в чат
             */
            while (true) {
                String message = null;
                if (client.name == null) {
                    System.out.println("Введите имя: ");
                    client.name = scanner.nextLine();
                    outputBuffer = ByteBuffer.wrap(("Connect " + client.name).getBytes(StandardCharsets.UTF_8));
                } else {
                    message = scanner.nextLine();
                    if (!message.equals("exit")) {
                        outputBuffer = ByteBuffer.wrap((message).getBytes(StandardCharsets.UTF_8));
                        Logger.writeStringToFile(
                                Config.CLIENT_LOG_FILE,LocalDateTime.now() + ": " + client.name + ": "+ message);
                    } else {
                        readerThread.interrupt();
                        socketChannel.close();
                        break;
                    }
                }
                //пишем в канал
                socketChannel.write(outputBuffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socketChannel.close();
        }
    }
}
