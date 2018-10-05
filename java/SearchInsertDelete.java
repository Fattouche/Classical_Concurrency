import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.*;

//Solution taken from little book of semaphores and applied to java
public class SearchInsertDelete {

    static Semaphore insertMutex = new Semaphore(1);
    static Semaphore noSearcher = new Semaphore(1);
    static Semaphore noInserter = new Semaphore(1);
    static Lightswitch searchSwitch = new Lightswitch();
    static Lightswitch insertSwitch = new Lightswitch();
    private static List<Integer> myList;

    static int maxSearchers = 50;
    static int maxInserters = 20;
    static int maxDeleters = 10;

    public static class Lightswitch {
        private Semaphore mutex = new Semaphore(1);
        private int counter = 0;

        public void wait(Semaphore semaphore) {
            try {
                mutex.acquire();
                counter++;
                if (counter == 1) {
                    semaphore.acquire();
                }
                mutex.release();
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }

        public void signal(Semaphore semaphore) {
            try {
                mutex.acquire();
                counter--;
                if (counter == 0) {
                    semaphore.release();
                }
                mutex.release();
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public static void sleepThread(int time) {
        try {
            Thread.sleep((long) (Math.random() * time));
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String args[]) {
        myList = new ArrayList<Integer>();

        System.out.println("Starting inserters");
        myList.add(1);
        myList.add(2);
        myList.add(3);

        Inserter inserter = new Inserter();
        Searcher searcher = new Searcher();
        Deleter deleter = new Deleter();

        // init inserters
        Thread[] inserters = new Thread[maxInserters];
        for (int i = 0; i < inserters.length; i++) {
            inserters[i] = new Thread(inserter);
            inserters[i].setName("inserter " + i);
            inserters[i].start();
        }

        System.out.println("Starting searchers");
        // init searchers
        Thread[] searchers = new Thread[maxSearchers];
        for (int i = 0; i < searchers.length; i++) {
            searchers[i] = new Thread(searcher);
            searchers[i].setName("searcher " + i);
            searchers[i].start();
        }

        System.out.println("Starting deleters");
        // init deleters
        Thread[] deleters = new Thread[maxDeleters];
        for (int i = 0; i < deleters.length; i++) {
            deleters[i] = new Thread(deleter);
            deleters[i].setName("deleter " + i);
            deleters[i].start();
        }
    }

    static class Inserter implements Runnable {
        @Override
        public void run() {
            try {
                sleepThread(250);
                insertSwitch.wait(noInserter);
                insertMutex.acquire();
                Random rand = new Random();
                int val = rand.nextInt(100);
                System.out.println("Inserting: " + val);
                myList.add(val);
                sleepThread(250);
                insertMutex.release();
                insertSwitch.signal(noInserter);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    static class Searcher implements Runnable {
        @Override
        public void run() {
            sleepThread(250);
            searchSwitch.wait(noSearcher);
            System.out.println("Searched: " + myList.get(0));
            sleepThread(250);
            searchSwitch.signal(noSearcher);
        }
    }

    static class Deleter implements Runnable {
        @Override
        public void run() {
            try {
                sleepThread(250);
                noSearcher.acquire();
                noInserter.acquire();
                int val = myList.get(myList.size() - 1);
                System.out.println("Deleting " + val);
                myList.remove(myList.size() - 1);
                sleepThread(250);
                noInserter.release();
                noSearcher.release();
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }

        }
    }
}
