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

    MessageBroker () {
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
        while (true) {
            System.out.println("run");
            selector.keys().forEach(System.out::println);

            try {
                System.out.println(selector.select());
                System.out.println("selector.select();");
            } catch (IOException e) {
                e.printStackTrace();
            }
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

            while (keyIterator.hasNext()) {
                System.out.println("keyIterator.hasNext()");
                SelectionKey key = keyIterator.next();

                if (key.isAcceptable()) {
                    // a connection was accepted by a ServerSocketChannel.

                } else if (key.isConnectable()) {
                    // a connection was established with a remote server.

                } else if (key.isReadable()) {
                    // a channel is ready for reading
                       String msg = clientHandler.readClientMsg((SocketChannel) key.channel());
                    System.out.println(msg);

                } else if (key.isWritable()) {
                    // a channel is ready for writing

                }
                keyIterator.remove();
            }
            selectedKeys.clear();
        }
    }
}
