import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

/**
 * Тестовый клиент писатель. Подключается и пишет заданное сообщение на сервер.
 * Thread.sleep(3000) необходим для того, чтобы сервер в процессе теста, успел не только обнаружить сообщение
 * от клиента в своем селекторе, но и прочитать его. Если таймаут не поставить, то треэд клиент
 * завершается в процессе чтения сервером сообщения, соединение разрывается и сервер не успевает прочитать
 * сообщение.
 */
public class ClientWriterAsCallable implements Callable<Integer> {
    private final String clientMessage;

    public ClientWriterAsCallable(String clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public Integer call() {
        Integer res = 0;
        try (SocketChannel clientChannel = SocketChannel.open()) {
            clientChannel.connect(new InetSocketAddress("localhost", 23334));
            clientChannel.configureBlocking(true);
            ByteBuffer outputBuffer = ByteBuffer.wrap((clientMessage).getBytes(StandardCharsets.UTF_8));
            clientChannel.write(outputBuffer);
            Thread.sleep(2000);
        } catch (IOException e) {
            e.printStackTrace();
            Assertions.fail();
            res++;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return res;
    }
}
