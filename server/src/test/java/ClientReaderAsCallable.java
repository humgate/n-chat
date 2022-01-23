import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

public class ClientReaderAsCallable implements Callable<String> {
    @Override
    public String call() {
        StringBuilder res = new StringBuilder();
        try {
            SocketChannel clientChannel = SocketChannel.open();
            clientChannel.connect(new InetSocketAddress("localhost", 23334));
            clientChannel.configureBlocking(true);
            final ByteBuffer inputBuffer = ByteBuffer.allocate(4096);
            int bytesCount;
            //В тесте у нас будут посылать сообщения 2 других клиента, а этот будет их читать
            for (int i = 0; i < 2; i++) {
                bytesCount = clientChannel.read(inputBuffer);
                if (bytesCount == -1) break;
                System.out.println(Thread.currentThread()+": Клиент прочитал");
                  String msg = new String(inputBuffer.array(), 0, bytesCount,
                        StandardCharsets.UTF_8).trim();
                System.out.println(Thread.currentThread()+ ": message: " + msg);
                res.append(" ").append(msg);
                inputBuffer.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res.toString();
    }
}

