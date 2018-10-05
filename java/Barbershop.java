import java.util.Random;
import java.util.concurrent.Semaphore;

//Solution taken from little book of semaphores and applied to java
public class Barbershop {

    static Semaphore mutex = new Semaphore(1);
    static Semaphore customerMutex = new Semaphore(0);
    static Semaphore barberMutex = new Semaphore(0);
    static Semaphore customerDoneMutex = new Semaphore(0);
    static Semaphore barberDoneMutex = new Semaphore(0);
    static int numReaders;
    static int valueToRead;
    static int chairs = 5;
    static int currentCustomers = 0;
    static int numCustomers = 100;

    public static void sleepThread() {
        try {
            Thread.sleep((long) (Math.random() * 250));
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String args[]) {
        Customer customer = new Customer();
        Barber barber = new Barber();

        // init barber
        Thread barberThread = new Thread(barber);
        barberThread.setName("barber");
        // Quit program once customers are done
        barberThread.setDaemon(true);
        barberThread.start();

        Thread[] threads = new Thread[numCustomers];
        for (int i = 0; i < threads.length; i++) {
            sleepThread();
            threads[i] = new Thread(customer);
            threads[i].setName("Customer " + i);
            threads[i].start();
        }
    }

    static class Customer implements Runnable {
        @Override
        public void run() {
            try {
                Random rand = new Random();
                System.out.println(Thread.currentThread().getName() + ": Arriving to barbershop");
                // Make sure no other reader is trying to add itself
                mutex.acquire();
                if (currentCustomers == chairs) {
                    mutex.release();
                    System.out.println(Thread.currentThread().getName() + ": All chairs taken, leaving");
                    return;
                }
                // Increment current customers to get a haircut next
                currentCustomers++;
                mutex.release();

                customerMutex.release();
                // Wait for barber to be available
                System.out.println(Thread.currentThread().getName() + ": Waiting in chair");
                barberMutex.acquire();

                // Get hair cut
                System.out.println(Thread.currentThread().getName() + ": Getting hair cut");

                customerDoneMutex.release();
                barberDoneMutex.acquire();

                // Decrement current customers
                mutex.acquire();
                currentCustomers--;
                mutex.release();

            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    static class Barber implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    // Make sure no customers are already being cut
                    customerMutex.acquire();
                    barberMutex.release();

                    // Cut hair
                    sleepThread();

                    // release and wait for next customer
                    customerDoneMutex.acquire();
                    barberDoneMutex.release();
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }
}
