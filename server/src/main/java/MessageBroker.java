import java.io.IOException;
import java.nio.channels.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MessageBroker implements Runnable {
    private Selector selector;
    private ClientHandler clientHandler;
    private final List<Msg> msgFeed = new ArrayList<>();

    public Selector getSelector() {
        return selector;
    }

    public static void main(String[] args) {

    }

    public ClientHandler getClientHandler() {
        return clientHandler;
    }

    public void setClientHandler(ClientHandler clientHandler) {
        this.clientHandler = clientHandler;
    }

    MessageBroker() {
        try {
            selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registerOnlineClient(SocketChannel socketChannel) throws ClosedChannelException {
        socketChannel.register(selector, SelectionKey.OP_READ);
        System.out.println(Thread.currentThread().getName()+": текущий список ключей селектора");
        selector.keys().forEach(System.out::println);
        /*
         * Практически выяснено, что если регистрация связи канала с селектором
         * (socketChannel.register) происходит после первого вызова операции (selector.select()),
         * то поток, выполняющий selector.select() блокируется и остается заблокированным
         * в этом месте далее, даже когда возникает событие, которое должно заставить selector.select()
         * отработать.
         * Для того чтобы "встряхнуть" селектор в этой ситуации можно вызвать один раз selector.wakeup()
         * после регистрация связи канала с селектором (socketChannel.register). Далее
         * selector.select() начинает работать правильно.
         */
        selector.wakeup();
    }
    
    public void notifyConnectedClients(Msg msg) {
        selector.keys().stream().filter(SelectionKey::isValid).forEach(k -> {
            clientHandler.writeMsg(msg.getClient()+ ": " +msg.getMessage(),(SocketChannel) k.channel());
        });
        selector.keys().forEach(k -> {
            SocketChannel socketChannel = (SocketChannel) k.channel();

        });
        
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                System.out.println(Thread.currentThread().getName() + ": run");
                selector.keys().forEach(System.out::println);
                System.out.println(selector.select());

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();

                    if (key.isReadable()) {
                        // a channel is ready for reading
                        SocketChannel clientSocket = (SocketChannel) key.channel();
                        String clientName = clientHandler.getNameBySocketChannel(clientSocket);
                        try {
                            String clientMsg = clientHandler.readClientMsg(clientSocket);
                            Msg msg = new Msg(clientName,clientMsg, LocalDateTime.now());
                            msgFeed.add(msg);
                            Logger.writeMsgToFile(Logger.SERVER_LOG_FILE,msg);
                            notifyConnectedClients(msg);
                        } catch (IOException ex) {
                            //убираем регистрацию в селекторе
                            key.cancel();
                            //проставляем в базе клиентов сокет клиента в null
                            clientHandler.registerClient(clientName, null);
                            System.out.println(Thread.currentThread().getName() + " : отключился клиент " + clientName);
                        }
                    }
                    keyIterator.remove();
                }
                selectedKeys.clear();
            }
        } catch (IOException e) {
            System.out.println(Thread.currentThread().getName() +": Исключение при попытке selector.select() ");
            e.printStackTrace();
        } finally {
            try {
                selector.close();
                System.out.println(Thread.currentThread().getName() + ": selector закрыт");
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + ": список сообщений чата данной сессии:");
            for (int i = 0; i < msgFeed.size(); i++) {
                System.out.println((i + 1) + ". " + msgFeed.get(i).getClient() + ". " + msgFeed.get(i).getMessage());
            }
        }
    }
}
