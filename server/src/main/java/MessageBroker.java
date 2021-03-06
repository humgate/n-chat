import java.io.IOException;
import java.nio.channels.*;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Set;

/**
 * Содержит список онлайн каналов клиентов. Слушает клиентов и обрабатывает полученные от них сообщения
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
     * @throws IOException - выбрасывается один из наследников IOException в случае проблем с каналом
     * или селектором, которые не позволяют выполнить их регистрацию
     */
    public void registerOnlineClient(SocketChannel socketChannel) throws IOException   {
        socketChannel.register(selector, SelectionKey.OP_READ);
        System.out.println(Thread.currentThread().getName()+": текущий список ключей селектора:");
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
    private void notifyConnectedClients(Msg msg) {
        selector.keys().stream().filter(SelectionKey::isValid).forEach(k -> {
            ChannelReaderWriter.writeMsg(msg.getClient()+ ": " +msg.getMessage(),(SocketChannel) k.channel());
        });
   }

    /**
     * Выполняет одну итерацию "слушанья" списка онлайн клиентов на предмет появления от кого-то из них сообщения.
     * При получении сообщений от клиентов записывает их в серверный лог и
     * рассылает его всем онлайн клиентам и на этом заканчивается итерация.
     * Так же обнаруживает отключение клиента, и обновляет статус подключения
     * клиента как у себя, так и в ClientHandler.
     */
   public void listenToClients() throws IOException {
       //пишем просто для удобства отслеживания, что происходит на сервере
       System.out.println(Thread.currentThread().getName() +
               ": итерация цикла прослушивания клиентов в run()");
       //Список всех каналов в селекторе. Отключенные будут иметь признак invalid
       selector.keys().forEach(System.out::println);
       //здесь поток блокируется, пока не появится что-то хотя бы от одного клиента
       System.out.println(Thread.currentThread().getName() +
               ": число клиентов приславших сообщение: " + selector.select());

       //здесь есть какая-то активность минимум от одного клиента, читаем, обрабатываем

       //набор ключей клиентов, в каналах которых что-то произошло
       Set<SelectionKey> selectedKeys = selector.selectedKeys();
       Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
       //проходим по набору ключей клиентов которые что-то прислали
       while (keyIterator.hasNext()) {
           SelectionKey key = keyIterator.next();

           if (key.isReadable()) {//произошло именно появление сообщения от клиента
               //извлекаем клиентский SocketChannel из канала селектора явным приведением типа
               SocketChannel clientSocket = (SocketChannel) key.channel();
               //находим по базе клиентов кто по этому каналу подключен
               String clientName = clientHandler.getNameBySocketChannel(clientSocket);
               try {
                   //если клиент вдруг упал или недоступен здесь выбросится IOException
                   String clientMsg = ChannelReaderWriter.readClientMsg(clientSocket);

                   //создаем объект msg
                   Msg msg = new Msg(clientName,clientMsg, LocalDateTime.now());

                   //пишем сообщение в лог
                   Logger.writeMsgToFile(Config.SERVER_LOG_FILE,msg);

                   //рассылаем его всем онлайн клиентам
                   notifyConnectedClients(msg);
               } catch (IOException ex) {
                   //убираем регистрацию канала в селекторе (то есть регистрацию клиента как онлайн)
                   key.cancel();

                   //проставляем в базе клиентов сокет клиента в null
                   clientHandler.registerClient(clientName, null);

                   //запишем об этом
                   System.out.println(Thread.currentThread().getName() + " : отключился клиент " + clientName);
               }
           }
           keyIterator.remove();
       }
       selectedKeys.clear();
   }

    /**
     * Запускаем слушанье клиентов в цикле.
     * Метод оформлен как реализация Run() для того, чтобы его удобно было запустить в отдельном потоке
     */
    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                listenToClients();
            }
        } catch (IOException e) {
            System.out.println(Thread.currentThread().getName() + ": Исключение в selector.select() ");
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
