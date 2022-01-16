import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.concurrent.*;

//импортнем их чтобы в тексте покороче писать
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

public class ClientHandlerTest {
    /**
     * Проверим позитивный сценарий работы handleClient().
     * Проверим, что запись о клиенте сохранилась в "базу"
     * Проверим, что имя клиента в сохраненной записи верное - то, которое передал клиент
     *
     * @throws InterruptedException
     */
    int threadCounter = 0;

    @Test
    void testHandleClientPositive() throws InterruptedException {
        //given
        //server
        Callable<Integer> serverAsCallable = new ServerAsCallable();
        Callable<Integer> clientAsCallable = new ClientAsCallable();

        final ExecutorService threadPool = Executors.newFixedThreadPool(
                3, r -> new Thread(r, "трэд" + ++threadCounter)
        );

        final Future<Integer> serverTask = threadPool.submit(serverAsCallable);
        final Future<Integer> clientTask = threadPool.submit(clientAsCallable);

        try {
            serverTask.get();
            clientTask.get();
        } catch (ExecutionException e) {
            if (!(e.getCause() instanceof org.opentest4j.AssertionFailedError)) {
                Assertions.fail("Возникли ошибки при взаимодействии потоков клиента и сервера");
            }
            e.printStackTrace();
            Assertions.fail();
        }

        threadPool.shutdown();
    }

    /**
     * Проверим негативный сценарий работы handleClient().
     * Проверим, что при некорректном сообщении подключения клиента, запись о клиенте не сохранилась в "базу"
     * @throws InterruptedException
     */
    @Test
    void testHandleClientNegative() throws InterruptedException {
        //given
        final HashMap<String, SocketChannel> db = new HashMap<>();        //server
        Thread serverThread = new Thread(() -> {
            try {
                ServerSocketChannel serverChannel = ServerSocketChannel.open();
                serverChannel.bind(new InetSocketAddress("localhost", 23334));

                //замокаем MessageBroker
                MessageBroker messageBroker = mock(MessageBroker.class);
                doNothing().when(messageBroker).registerOnlineClient(isA(SocketChannel.class));

                ClientHandler clientHandler = new ClientHandler(serverChannel, messageBroker);
                //проверим что нет записей

                Assertions.assertEquals(clientHandler.getClientsDB().size(),0);
                serverChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, "serverThread");
        serverThread.start();

        //client
        Thread clientThread = new Thread(() -> {
            try {
                SocketChannel clientChannel = SocketChannel.open();
                clientChannel.connect(new InetSocketAddress("localhost", 23334));
                clientChannel.configureBlocking(true);
                ByteBuffer outputBuffer = ByteBuffer.wrap(("tra ta ta").getBytes(StandardCharsets.UTF_8));
                clientChannel.write(outputBuffer);
            } catch (IOException e) {
                e.printStackTrace();
                Assertions.fail();
            }
        }
                , "clientThread");
        clientThread.start();

        clientThread.join();
        serverThread.join();
    }
}
