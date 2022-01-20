import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class ChannelReaderWriter {
    /**
     * Записывает строку в канал
     * @param msg - строка
     * @param socketChannel - канал
     */
    public static void writeMsg(String msg, SocketChannel socketChannel) {
        //заносим строку в выходной буфер
        final ByteBuffer outputBuffer = ByteBuffer.wrap((msg).getBytes(StandardCharsets.UTF_8));

        if (!socketChannel.isConnected()) return;
        //пишем из буфера в канал
        try {
            socketChannel.write(outputBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Читает строку из канала
     * @param socketChannel - канал
     * @return строка переданная В канал
     * @throws IOException - выбрасывается операцией read канала в случае возникновения ошибки при чтении
     */
    public static String readClientMsg(SocketChannel socketChannel) throws IOException {
        final ByteBuffer inputBuffer = ByteBuffer.allocate(Config.BUFFER_SIZE);
        if (!socketChannel.isConnected()) return null;
        // читаем данные из канала в буфер
        int bytesCount = socketChannel.read(inputBuffer);
        // переносим данные клиента из буфера в строку в нужной кодировке
        return new String(inputBuffer.array(), 0, bytesCount,
                StandardCharsets.UTF_8);
    }



}
