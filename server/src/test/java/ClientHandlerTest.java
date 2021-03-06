import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.util.concurrent.*;

/**
 * Тест handleClient - основной метод ClientHandler.
 * Содержит два теста - первый позитивный сценарий и второй негативный
 *
 * Тесты устроены так:
 * Тест запускает Callable тред сервера и Callable трэд клиента. Клиент подключается к серверу и
 * отправляет строку конекта. Проверяется, что в базу клиентов записалось правильное имя клиента и оно одно.
 * Если же строка конекта некорректная, то проверится что в базе клиентов не создалась запись
 *
 * Callable использовано в данном тесте, несмотря на то что, реально нам не нужно ничего получать от потоков
 * по следующей причине: Runnable, как выяснилось экспериментально, не подходит для
 * этого, потому что исключения, выбрасываемые внутри треда, запущенного как Runnable, никак
 * не пробрасываются в тред, запустивший Runnable трэд. А Assertions в тестах внутри этих потоков как раз выбрасывают,
 * исключение AssertionFailedError и для результата теста нужно знать, выброшено исключение или нет.
 * В случае c Callable, Future.get() выбрасывает ExecutionException, если внутри Callable треда что-то "упало".
 * Причем, если внутри Callable трэда "упал" именно какой-либо из Assertions, то ExecutionException будет
 * instanceof org.opentest4j.AssertionFailedError и мы в основном треде теста можем это проверить и уже
 * сами выкинуть Assertions.fail(). Таким образом результат теста будет отражен правильно.
 */

public class ClientHandlerTest {
    /**
     * Проверим позитивный сценарий работы handleClient().
     * Проверим, что запись о клиенте сохранилась в "базу"
     * Проверим, что имя клиента в сохраненной записи верное - то, которое передал клиент
     */
    @Test
    void testHandleClientPositive()  {
        //given
        //server
        Callable<Integer> serverAsCallable = new ServerAsCallableWithClientHandler(
                1, "Connect testClientName");
        //client
        Callable<Integer> clientAsCallable = new ClientWriterAsCallable(
                "Connect testClientName");

        final ExecutorService threadPool = Executors.newFixedThreadPool(3);
        final Future<Integer> serverTask = threadPool.submit(serverAsCallable);
        final Future<Integer> clientTask = threadPool.submit(clientAsCallable);

        try {
            serverTask.get();
            clientTask.get();
        } catch (ExecutionException | InterruptedException e) {
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
     */
    @Test
    void testHandleClientNegative() {
        //server
        Callable<Integer> serverAsCallable = new ServerAsCallableWithClientHandler(0, null);
        //client
        Callable<Integer> clientAsCallable = new ClientWriterAsCallable("testClientName");

        final ExecutorService threadPool = Executors.newFixedThreadPool(3);
        final Future<Integer> serverTask = threadPool.submit(serverAsCallable);
        final Future<Integer> clientTask = threadPool.submit(clientAsCallable);

        try {
            serverTask.get();
            clientTask.get();
        } catch (ExecutionException | InterruptedException e) {
            if (!(e.getCause() instanceof org.opentest4j.AssertionFailedError)) {
                Assertions.fail("Возникли ошибки при взаимодействии потоков клиента и сервера");
            }
            e.printStackTrace();
            Assertions.fail();
        }
        threadPool.shutdown();
    }
}
