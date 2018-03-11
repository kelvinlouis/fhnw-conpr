package semaphore;

/**
 * Created by Kelvin on 11-Mar-18.
 */
public class SemaphoreQueue {
    private final int size;
    private final Semaphore used;
    private final Semaphore free;
    private Object[] buf;

    SemaphoreQueue(int size) {
        this.size = size;
        this.free = new SemaphoreImpl(size);
        this.used = new SemaphoreImpl(0);
        buf = new Object[size];
    }

    public Object dequeue() {
        used.acquire();
        int i = used.available();
        int head = (size-i) % size;
        Object result = buf[head];
//        System.out.println("dequeue " + head + " " + result);
        buf[head] = null;
        free.release();
        return  result;
    }

    public void enqueue(Object x) {
        free.acquire();
        int i = free.available();
        int tail = (i+1) % size;
//        System.out.println("enqueue " + tail + " " + x);
        buf[tail] = x;
        used.release();
    }

    public static void main(String[] args) throws InterruptedException {
        int size = 4;
        final SemaphoreQueue queue = new SemaphoreQueue(size);

        Thread t0 = run("t0", new Runnable() {
            public void run() {
                queue.enqueue(0);
                sleep(50);
                queue.dequeue();
                queue.dequeue();
            }
        });

        Thread t1 = run("t1", new Runnable() {
            public void run() {
                queue.enqueue(1);
                sleep(50);
                queue.dequeue();
                queue.dequeue();
            }
        });

        Thread t2 = run("t2", new Runnable() {
            public void run() {
                queue.enqueue(2);
                sleep(2000);
                queue.enqueue(5);
                queue.enqueue(6);
                queue.enqueue(7);
            }
        });

        Thread t3 = run("t3", new Runnable() {
            public void run() {
                queue.enqueue(3);
                sleep(100);
                queue.dequeue();
                queue.dequeue();
            }
        });

        t0.join();
        t1.join();
        t2.join();
        t3.join();
        System.out.println(queue);
    }

    static Thread run(String name, Runnable r) {
        Thread t = new Thread(r, name);
        t.setDaemon(true);
        t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                System.err.println(t.getName() + " failed: " + e);
            }
        });
        t.start();
        return t;
    }

    static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {}
    }
}
