import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AlreadyBoundException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Callable;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

/**
 * Callable вариант сервера для тестов ClientHandler
 * с моками MessageBroker
 */
public class ServerAsCallableWithClientHandler implements Callable<Integer> {
    final int clientDbCount;
    final String clientMessage;

    public ServerAsCallableWithClientHandler(int clientDbCount, String clientMessage) {
        this.clientDbCount = clientDbCount;
        this.clientMessage = clientMessage;
    }

    @Override
    public Integer call() {
        Integer res = 0;
        try {
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            try {
                serverChannel.bind(new InetSocketAddress("localhost", 23334));
            } catch (AlreadyBoundException e) {
                System.out.println("Socket already bound, skipping binding");
            }

            //замокаем MessageBroker
            MessageBroker messageBroker = mock(MessageBroker.class);
            doNothing().when(messageBroker).registerOnlineClient(isA(SocketChannel.class));

            ClientHandler clientHandler = new ClientHandler(serverChannel, messageBroker);

            //проверим что нет записей
            clientHandler.handleClient();

            //проверим что нет записей
            Assertions.assertEquals(clientHandler.getClientsDB().size(),clientDbCount);
            if (clientHandler.getClientsDB().size()!=clientDbCount) {
                res++;
            }

            if (clientDbCount != 0) {
                String client = clientHandler.getClientsDB().entrySet().stream().findAny().get().getKey();
                Assertions.assertEquals(client, clientMessage.split(" ")[1]);
                if (!client.equals(clientMessage.split(" ")[1])) res++;
            }

            serverChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
            res++;
        }
        return res;
    }
}
