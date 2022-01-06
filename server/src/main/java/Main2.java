import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class Main2 {

    private static final ArrayList<Msg> feed = new ArrayList<>();
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true); //честный
    public static void main(String[] args) {
        Thread adder1 = new Thread(null,
                () -> {
                    for (int i = 0; i < 5; i++) {
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        lock.writeLock().lock();
                        feed.add(new Msg("cli1",0," cli1 добавил " + i));
                        lock.writeLock().unlock();
                    }
        },"Поток добавлятель 1");

        Thread adder2 = new Thread(null,
                () -> {
                    for (int i = 0; i < 5; i++) {
                        lock.writeLock().lock();
                        feed.add(new Msg("cli2",0," cli2 добавил " + i));
                        lock.writeLock().unlock();
                    }
                },"Поток добавлятель 2");

        Thread reader = new Thread(null,
                () -> {
                    /* lock.readLock().lock();
                    feed.forEach(System.out::println);
                    lock.readLock().unlock();*/
                },
                "Поток читатель");

        adder1.start();
        adder2.start();
        reader.start();
        System.out.println("Все потоки стартовали");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        feed.forEach(System.out::println);



    }
}
