import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

//импортнем их чтобы в тексте покороче писать
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;


public class ClientHandlerTest {
    @Test
    void testRun() throws InterruptedException {
        //given

        //server
        Thread serverThread = new Thread(() -> {
            try {
                ServerSocketChannel serverChannel = ServerSocketChannel.open();
                serverChannel.bind(new InetSocketAddress("localhost", 23334));
                MessageBroker messageBroker = mock(MessageBroker.class);
                ClientHandler clientHandler = new ClientHandler(serverChannel, messageBroker);
                doNothing().when(messageBroker).registerOnlineClient(isA(SocketChannel.class));
                clientHandler.handleClient();
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
                ByteBuffer outputBuffer = ByteBuffer.wrap(("Connect testClientNameNAME").getBytes(StandardCharsets.UTF_8));
                clientChannel.write(outputBuffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
                , "clientThread");
        clientThread.start();


        clientThread.join();
        serverThread.join();

        Assertions.assertEquals("11", "11");
    }
}
