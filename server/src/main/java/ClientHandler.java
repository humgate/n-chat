import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class ClientHandler implements Runnable {
    static final String CONNECTION_INIT_ERROR_MSG =
            "Подключение не удалось. Клиент с указанным именем уже в чате или недопустимый формат подключения";

    private final HashMap<String, SocketChannel> clientsDB = new HashMap<>();
    private final ServerSocketChannel serverChannel;
    private final MessageBroker msgBroker;

    ClientHandler(ServerSocketChannel serverChannel, MessageBroker msgBroker)
    {
        this.serverChannel = serverChannel;
        this.msgBroker = msgBroker;
    }

    public void registerClient(String name, SocketChannel socketChannel) {
            clientsDB.put(name, socketChannel);
    }

    public String getNameBySocketChannel(SocketChannel socketChannel) {
        Iterator<Map.Entry<String, SocketChannel>> iter = clientsDB.entrySet().iterator();
        String name = null;
        while (iter.hasNext()) {
            Map.Entry<String, SocketChannel> entry = iter.next();
            if (entry.getValue() != null && entry.getValue().equals(socketChannel)) {
                name = entry.getKey();
            }
        }
        return name;
    }

    public String readClientMsg(SocketChannel socketChannel) throws IOException {
        final ByteBuffer inputBuffer = ByteBuffer.allocate(Config.BUFFER_SIZE);
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
            socketChannel.write(outputBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean validateConnMsg(String msg) {
        return !(msg == null ||
                msg.split(" ").length < 2 ||
                !msg.split(" ")[0].equals(Config.CLIENT_CONNECTION_MSG_PFX) ||
                msg.split(" ")[1].isEmpty() ||
                clientsDB.containsKey(msg.split(" ")[1]));
    }

    public void displayConnectedClients() {
        clientsDB.entrySet().forEach(System.out::println);
    }

    public void closeAllConnections() {
        clientsDB.forEach((k, v) -> {
            try {
                if (v!=null) {
                    v.close();
                }
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

                System.out.println(Thread.currentThread().getName() + ": Подключился клиент...");

                //установим socketChannel в блокирующий режим чтобы поток блокировался ожидая сообщения клиента
                socketChannel.configureBlocking(true);

                //читаем что передал клиент в качестве инициирующего сообщения
                String msg = readClientMsg(socketChannel);

                //первое сообщение от клиента при коннекте должно содержать "Connect" и через пробел имя
                if (validateConnMsg(msg)) {
                    String clientName = msg.split(" ")[1];
                    writeMsg(clientName + " подключился", socketChannel);
                    registerClient(clientName, socketChannel);
                    //переводим socketChannel в неблокирующий режим для возможности работы через Selector
                    socketChannel.configureBlocking(false);
                    //регистрируем ключ в селекторе
                    msgBroker.registerOnlineClient(socketChannel);
                } else {
                    writeMsg(CONNECTION_INIT_ERROR_MSG, socketChannel);
                    socketChannel.close();
                }
                displayConnectedClients();
            }
        } catch (ClosedByInterruptException e) {
            System.out.println(Thread.currentThread().getName() + ": получена команда interrupt");
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        finally {
            System.out.println(Thread.currentThread().getName()+ ": закрытие всех подключений клиентов");
            closeAllConnections();
        }
    }
}

