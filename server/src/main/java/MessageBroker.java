import java.io.IOException;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

public class MessageBroker implements Runnable {
    private Selector selector;
    private ClientHandler clientHandler;

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


    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                System.out.println("run");
                selector.keys().forEach(System.out::println);
                System.out.println(selector.select());

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();

                    if (key.isReadable()) {
                        // a channel is ready for reading
                        String msg = null;
                        msg = clientHandler.readClientMsg((SocketChannel) key.channel());
                        System.out.println(clientHandler.getNameBySocketChannel((SocketChannel) key.channel()) + "." + msg);
                    }
                    keyIterator.remove();
                    System.out.println(keyIterator.hasNext());
                    System.out.println("РЕМУВЕД");
                }
                selectedKeys.clear();
            }
            selector.close();
            System.out.println(Thread.currentThread().getName() + " Selector закрыт");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
