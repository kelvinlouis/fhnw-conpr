package worksheet;

public class ThreadSkete {
    public static void main(String[] args) throws Exception {
        long l = 0;
        while(true) {
            Thread t = new Thread(new R1());
            t.start();
            System.out.println(l++);
        }
    }
}

class R1 implements Runnable {

    public R1() {}

    public void run() {
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}