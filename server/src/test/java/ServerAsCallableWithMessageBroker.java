import org.junit.jupiter.api.Assertions;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.util.concurrent.Callable;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

/**
 * Callable вариант сервера для тестов MessageBroker
 * с моками ClientHandler
 */
public class ServerAsCallableWithMessageBroker implements Callable<Integer> {
    @Override
    public Integer call() {
        try {
            Logger.createLogDir(Config.SERVER_LOG_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Integer res = 0;
        try {
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress("localhost", 23334));
            MessageBroker messageBroker = new MessageBroker();
            ClientHandler clientHandler = mock(ClientHandler.class);
//            MockedStatic<Logger> logger = Mockito.mockStatic(Logger.class);
//            logger.when(()-> Logger.writeMsgToFile(any(),any())).thenReturn(true);

            Mockito.when(clientHandler.getNameBySocketChannel(isA(SocketChannel.class))).thenReturn("someclient");
            Mockito.doNothing().when(clientHandler).registerClient(isA(String.class), isA(SocketChannel.class));
            messageBroker.setClientHandler(clientHandler);

            //ждем трех клиентов
            for (int i = 0; i < 3; i++) {
                SocketChannel socketChannel = serverChannel.accept();
                System.out.println(Thread.currentThread()+" Подключился клиент");
                socketChannel.configureBlocking(false);
                messageBroker.registerOnlineClient(socketChannel);
                System.out.println(Thread.currentThread()+" Клиент зарегистрирован в селекторе");
            }
            for (int i = 0; i < 2; i++) {
                messageBroker.listenToClients();
            }
            serverChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
            res++;
        }
            return res;
    }
}
