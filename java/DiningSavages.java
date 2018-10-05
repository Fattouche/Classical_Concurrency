import java.util.Random;
import java.util.concurrent.Semaphore;

//Solution taken from little book of semaphores and applied to java
public class DiningSavages {

    static Semaphore mutex = new Semaphore(1);
    static Semaphore emptyPot = new Semaphore(0);
    static Semaphore fullPot = new Semaphore(0);
    static int servingSize = 10;
    static int savages = 100;
    static int servings = 10;

    public static void main(String args[]) {
        Cook cook = new Cook();
        Savage savage = new Savage();

        // init cook
        Thread cookThread = new Thread(cook);
        cookThread.setName("cook");
        // quit once all savages have eaten
        cookThread.setDaemon(true);
        cookThread.start();

        Thread[] threads = new Thread[savages];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(savage);
            threads[i].setName("Savage " + i);
            threads[i].start();
        }
    }

    static class Savage implements Runnable {
        @Override
        public void run() {
            try {
                mutex.acquire();
                // If pot is empty, notify cook
                if (servings == 0) {
                    emptyPot.release();
                    fullPot.acquire();
                    servings = servingSize;
                }
                servings -= 1;
                System.out.println("eating");
                mutex.release();

            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    static class Cook implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    // wait for pot to be empty
                    emptyPot.acquire();

                    // Refill pot
                    System.out.println("Replenishing pot");

                    // Notify pot is full
                    fullPot.release();
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }
}
