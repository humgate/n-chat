import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

public class ClientAsCallable implements Callable<Integer> {
    @Override
    public Integer call() {
        Integer res = 0;
        try {
            SocketChannel clientChannel = SocketChannel.open();
            clientChannel.connect(new InetSocketAddress("localhost", 23334));
            clientChannel.configureBlocking(true);
            ByteBuffer outputBuffer = ByteBuffer.wrap(("Connect testClientName").getBytes(StandardCharsets.UTF_8));
            clientChannel.write(outputBuffer);
        } catch (IOException e) {
            e.printStackTrace();
            Assertions.fail();
            res++;
        }
        return res;
    }
}
