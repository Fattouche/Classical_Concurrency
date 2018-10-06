import java.util.Random;
import java.util.concurrent.Semaphore;

//Solution taken from little book of semaphores and applied to java
public class SantaClaus {

    static Semaphore mutex = new Semaphore(1);
    static Semaphore elfTex = new Semaphore(1);
    static Semaphore santaSem = new Semaphore(0);
    static Semaphore reindeerSem = new Semaphore(0);
    static int maxReindeer = 9;
    static int maxElves = 100;
    static int elves = 0;
    static int reindeer = 0;

    public static void sleepThread(int time) {
        try {
            Thread.sleep((long) (Math.random() * time));
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String args[]) {
        Elf elf = new Elf();
        Reindeer reindeer = new Reindeer();
        Santa santa = new Santa();

        // init santa
        Thread santaThread = new Thread(santa);
        santaThread.setName("santa");
        santaThread.start();

        // init reindeer
        Thread[] reindeers = new Thread[maxReindeer];
        for (int i = 0; i < reindeers.length; i++) {
            reindeers[i] = new Thread(reindeer);
            reindeers[i].setName("reindeer " + i);
            reindeers[i].setDaemon(true);
            reindeers[i].start();
        }

        // init elves
        Thread[] elves = new Thread[maxElves];
        for (int i = 0; i < elves.length; i++) {
            elves[i] = new Thread(elf);
            elves[i].setName("elf " + i);
            elves[i].setDaemon(true);
            elves[i].start();
        }
    }

    static class Elf implements Runnable {
        @Override
        public void run() {
            try {
                sleepThread(250);
                // Make sure no other elves are incrementing elves variable
                elfTex.acquire();
                mutex.acquire();
                elves++;
                // If max elves, notify santa otherwise move on
                if (elves == 3) {
                    santaSem.release();
                } else {
                    elfTex.release();
                }
                mutex.release();

                // Get santas help
                sleepThread(250);

                mutex.acquire();
                elves--;
                if (elves == 0) {
                    elfTex.release();
                }
                mutex.release();

            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    static class Santa implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    santaSem.acquire();
                    mutex.acquire();
                    // Wait for reindeer to arrive
                    if (reindeer >= maxReindeer) {
                        System.out.println("Reindeers finished, hitching sled!");
                        reindeerSem.release(9);
                        reindeer -= 9;
                        return;
                    } else if (elves == 3) {
                        System.out.println("Helping three elves");
                        sleepThread(250);
                    }
                    mutex.release();
                }

            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    static class Reindeer implements Runnable {
        @Override
        public void run() {
            try {
                sleepThread(2500);
                mutex.acquire();
                reindeer++;
                System.out.println("New reindeer arrived");
                if (reindeer == maxReindeer) {
                    System.out.println("Last reindeer arrived, notifying santa");
                    santaSem.release();
                }
                mutex.release();
                reindeerSem.acquire();
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }

        }
    }
}
