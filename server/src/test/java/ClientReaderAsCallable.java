import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

public class ClientReaderAsCallable implements Callable<String> {
    @Override
    public String call() throws Exception {
        StringBuilder res = new StringBuilder();
        try {
            SocketChannel clientChannel = SocketChannel.open();
            clientChannel.connect(new InetSocketAddress("localhost", 23334));
            clientChannel.configureBlocking(true);
            final ByteBuffer inputBuffer = ByteBuffer.allocate(4096);
            int bytesCount;
            //В тесте у нас будут посылать сообщения 2 других клиента, а этот будет их читать
            while (true) {
                System.out.println("Клиент читаю");
                bytesCount = clientChannel.read(inputBuffer);
                System.out.println("Клиент прочитал");
                if (bytesCount == -1) break;
                res.append(" ").append(new String(inputBuffer.array(), 0, bytesCount,
                        StandardCharsets.UTF_8).trim());
                inputBuffer.clear();
            }
        } catch (IOException e) {

            e.printStackTrace();
        }
        return res.toString();
    }
}

