import java.io.IOException;
import java.nio.channels.*;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Set;

/**
 * Выполняет работу с сообщениями от клиентов
 */
public class MessageBroker implements Runnable {
    private Selector selector;
    private ClientHandler clientHandler;

    public void setClientHandler(ClientHandler clientHandler) {
        this.clientHandler = clientHandler;
    }

    //в конструкторе открываем селектор
    MessageBroker() {
        try {
            selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Регистрирует канал (клиента) как активный для прослушивания (регистрирует канал в селекторе).
     * Эта связь обеспечивает прослушивание всех зарегистрированных каналов на появление в них сообщений от клиентов
     * @param socketChannel - канал
     * @throws ClosedChannelException
     */
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

    /**
     * Рассылает всем онлайн (SelectionKey::isValid) клиентам переданное сообщение
     * @param msg - Сообщение
     */
    public void notifyConnectedClients(Msg msg) {
        selector.keys().stream().filter(SelectionKey::isValid).forEach(k -> {
            clientHandler.writeMsg(msg.getClient()+ ": " +msg.getMessage(),(SocketChannel) k.channel());
        });
   }

    /**
     * "Слушает" список онлайн клиентов, при получении сообщения от клиента записывает его в серверный лог и
     * рассылает его всем онлайн клиентам. Обнаруживает отключение клиента, и обновляет статус подключения
     * клиента как у себя, так и в ClientHandler.
     * Метод оформлен как реализация Run() для того, чтобы его удобно было запустить в отдельном потоке
     */
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
                            Logger.writeMsgToFile(Config.SERVER_LOG_FILE,msg);
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
        }
    }
}
