import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;

public class MessageBrokerTest {
    @Test
    void testListenToClients() throws InterruptedException {
        //given
        String result = null;
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

            System.out.println(result);

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
