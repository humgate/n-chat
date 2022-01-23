import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;

/**
 * Тест для MessageBroker.
 * Создает 4 Callable потока: 1 - сервер, 2 - пишущий сообщение1 клиент, 3 - пишущий сообщение2 клиент,
 * 4 - читающий клиент. Сервер сначала всех 3 клиентов подключает, а затем два раза запускает
 * MessageBroker.listenToClients() - основной метод сервера по обработке сообщение клиентов.
 * 2 раза необходимо запускать listenToClients(), потому что первый раз селектор может не увидеть сообщения клиентов
 * если они зарегистрировались после запуска прослушивание селектора.
 * В тесте дожидаемся когда Futre.get читающего клиента вернет прочитанные им сообщения и сравним с теми,
 * что передавали клиент 1 и 2.
 *
 */
public class MessageBrokerTest {
    @Test
    void testListenToClients() throws InterruptedException {
        //given
        String result;
        //сервер
        Callable<Integer> serverAsCallable = new ServerAsCallableWithMessageBroker();

        //пишущий клиент1
        Callable<Integer> clientAsCallable1 = new ClientWriterAsCallable("testMessage1");

        //пишущий клиент2
        Callable<Integer> clientAsCallable2 = new ClientWriterAsCallable("testMessage2");

        //читающй клиент
        Callable<String> clientAsCallable3 = new ClientReaderAsCallable();

        final ExecutorService threadPool = Executors.newFixedThreadPool(4);
        final Future<Integer> serverTask = threadPool.submit(serverAsCallable);
        final Future<Integer> clientTask1 = threadPool.submit(clientAsCallable1);
        final Future<Integer> clientTask2 = threadPool.submit(clientAsCallable2);
        final Future<String> clientTask3 = threadPool.submit(clientAsCallable3);

        try {
            serverTask.get();
            clientTask1.get();
            clientTask2.get();
            result = clientTask3.get();

            //вычленяем сам текст сообщения 1
            String msg1 = result.trim().split(" ")[1];
            System.out.println(msg1);

            //вычленяем сам текст сообщения 2
            String msg2 = result.trim().split(" ")[3];

            //проверяем что это сообщения от первых двух клиентов писателей
            Assertions.assertTrue(msg1.equals("testMessage1") || msg1.equals("testMessage2"));
            Assertions.assertTrue(msg2.equals("testMessage1") || msg2.equals("testMessage2"));

        } catch (ExecutionException e) {
            if (!(e.getCause() instanceof org.opentest4j.AssertionFailedError)) {
                Assertions.fail("Возникли ошибки при взаимодействии потоков клиента и сервера");
            }
            e.printStackTrace();
            Assertions.fail();
        }
        threadPool.shutdown();
    }

}
