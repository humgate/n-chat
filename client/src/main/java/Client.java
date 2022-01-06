import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Client {
    static final String IP_ADDRESS = "localhost";
    static final short PORT = 23334;
    static final int BUFFER_SIZE = 2 << 20;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Определяем сокет сервера
        InetSocketAddress socketAddress = new InetSocketAddress(IP_ADDRESS, PORT);

        try (final SocketChannel socketChannel = SocketChannel.open()) {
            // подключаемся к серверу
            socketChannel.connect(socketAddress);

            //устанавливаем blocking IO см. комментарии ниже
            socketChannel.configureBlocking(true);

            /*
             * В данном примере используется по смыслу блокирующий I/O, хотя формально
             * мы применили каналы и буферы из java.nio.
             * Операции чтения в отдельном потоке readerThread блокирующая, потому что она блокирует поток
             * чтения до тех пор, пока не будет полностью считана строка из канала. В принципе по смыслу
             * задачи, именно такое поведение и нужно. Если же установить socketChannel.configureBlocking(false),
             * то в readerThread будет в бесконечном цикле считывать 0 из канала, не важно, есть там что-то
             * или нет.
             * Что же касается записи в канал в основном потоке main, то она тоже блокирующая, то есть пока
             * в канал не будет записана вся строка из буфера, поток main блокируется. Однако поскольку,
             * строки вводимые пользователем в этом пример очень маленькие, то заметить эту блокировку
             * в этом примере невозможно.
             *
             */

            System.out.println("socketChannel.isBlocking()==" + socketChannel.isBlocking());

            // Определяем буфер для получения и отправки данных
            final ByteBuffer inputBuffer = ByteBuffer.allocate(BUFFER_SIZE);
            ByteBuffer outputBuffer;


            /*
             * Для того чтобы задача имела какой-то практический смысл, сервер сделан так, что обрабатывает
             * полученную от клиента строку 5 секунд.
             * Поэтому интересно реализовать клиента так, чтобы пользователь мог вводить строки,
             * независимо от того, получен ответ от сервера на введенную строку или нет и по мере получения
             * от сервера обработанных строк отображать их.
             * Поэтому зачитывание и отображение ответов сервера сделано в отдельном потоке, который
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
                            System.out.println("\n" + threadName + ". Зачитано байт: " + bytesCount);
                            System.out.println(threadName + ". Ответ сервера: " +
                                    new String(inputBuffer.array(), 0, bytesCount,
                                            StandardCharsets.UTF_8).trim());
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
                System.out.println("Введите строку...");
                String msg = scanner.nextLine();

                if ("end".equals(msg)) {
                    readerThread.interrupt();
                    socketChannel.close();
                    break;
                }

                //помещаем считанную строку в выходной байтовый буфер
                outputBuffer = ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8));

                //пишем в канал
                int writtenBytes = socketChannel.write(outputBuffer);
                System.out.println("Записано байт: " + writtenBytes);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
