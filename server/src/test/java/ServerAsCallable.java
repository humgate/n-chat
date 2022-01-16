import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Callable;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

public class ServerAsCallable implements Callable<Integer> {
    @Override
    public Integer call() {
        Integer res = 0;
        try {
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress("localhost", 23334));

            //замокаем MessageBroker
            MessageBroker messageBroker = mock(MessageBroker.class);
            doNothing().when(messageBroker).registerOnlineClient(isA(SocketChannel.class));

            ClientHandler clientHandler = new ClientHandler(serverChannel, messageBroker);
            //проверим что нет записей
            clientHandler.handleClient();

            Assertions.assertEquals(clientHandler.getClientsDB().size(),1);
            if (clientHandler.getClientsDB().size() != 1) res++;

            String client = clientHandler.getClientsDB().entrySet().stream().findAny().get().getKey();

            Assertions.assertEquals(client, "testClientName");
            if (!client.equals("testClientName")) res++;

            serverChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
            res++;
        }
        return res;
    }
}
