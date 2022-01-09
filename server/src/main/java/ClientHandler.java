import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;


public class ClientHandler implements Runnable {
    static final int BUFFER_SIZE = 2 << 20;
    static final String CONNECTION_CLIENT_MSG_PFX = "Connect";
    static final String CONNECTION_INIT_ERROR_MSG =
            "Подключение не удалось. Недопустимый формат подключения к серверу";


    private final HashMap<String, SocketChannel> clientsDB = new HashMap<>();
    private final HashMap<SocketChannel, String> socketsDB = new HashMap<>();

    private final ServerSocketChannel serverChannel;
    private final MessageBroker msgBroker;

    ClientHandler(ServerSocketChannel serverChannel, MessageBroker msgBroker)
    {
        this.serverChannel = serverChannel;
        this.msgBroker = msgBroker;
    }


    public boolean exists(String name) {
        return clientsDB.containsKey(name);
    }

    public boolean registerClient(String name, SocketChannel socketChannel) {
        if (!exists(name)) {
            clientsDB.put(name, socketChannel);
            socketsDB.put(socketChannel, name);
            return true;
        } else {
            return false;
        }
    }

    public String getNameBySocketChannel (SocketChannel socketChannel) {
        return (socketChannel != null) ? socketsDB.get(socketChannel):null;
    }

    public String readClientMsg(SocketChannel socketChannel) throws IOException {
        final ByteBuffer inputBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        if (!socketChannel.isConnected()) return null;
        // читаем данные из канала в буфер
        int bytesCount = socketChannel.read(inputBuffer);
            // переносим данные клиента из буфера в строку в нужной кодировке
            final String msg = new String(inputBuffer.array(), 0, bytesCount,
                    StandardCharsets.UTF_8);
            return msg;
        }

    public void writeMsg(String msg, SocketChannel socketChannel) {
        //заносим строку в выходной буфер
        final ByteBuffer outputBuffer = ByteBuffer.wrap((msg).getBytes(StandardCharsets.UTF_8));

        if (!socketChannel.isConnected()) return;
        //пишем из буфера в канал
        try {
            int bytesCount = socketChannel.write(outputBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean validateConnMsg(String msg) {
        return !(msg == null ||
                msg.split(" ").length < 2 ||
                !msg.split(" ")[0].equals(CONNECTION_CLIENT_MSG_PFX) ||
                msg.split(" ")[1].isEmpty());
    }

    public void allConnectedClients() {
        clientsDB.entrySet().forEach(System.out::println);
    }

    public void closeAllConnections() {
        clientsDB.forEach((k, v) -> {
            try {
                v.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                // Ждем подключения клиента и получаем потоки для дальнейшей работы
                SocketChannel socketChannel = serverChannel.accept();

                System.out.println("Подключился клиент...");

                //установим socketChannel в блокирующий режим чтобы поток блокировался ожидая сообщения клиента
                socketChannel.configureBlocking(true);

                //читаем что передал клиент в качестве инициирующего сообщения
                String msg = readClientMsg(socketChannel);

                //первое сообщение от клиента при коннекте должно содержать "Connect" и через пробел имя
                if (validateConnMsg(msg)) {
                    writeMsg(msg, socketChannel);
                    registerClient(msg.split(" ")[1], socketChannel);
                    //переводим socketChannel в неблокирующий режим для возможности работы через Selector
                    socketChannel.configureBlocking(false);
                    msgBroker.registerOnlineClient(socketChannel);
                } else {
                    writeMsg(CONNECTION_INIT_ERROR_MSG, socketChannel);
                    socketChannel.close();
                }
                allConnectedClients();
            }
        } catch (ClosedByInterruptException e) {
            System.out.println(Thread.currentThread().getName() + " получил команду interrupt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            System.out.println("Закрытие всех подключений клиентов");
            closeAllConnections();
            clientsDB.forEach((k, v) -> System.out.println(k + "." + v));
        }
    }
}

