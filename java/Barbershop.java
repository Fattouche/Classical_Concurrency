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
    static int maxCustomers;
    static int currentCustomers;

    public static void main(String args[]) {
        maxCustomers = 4;
        Customer customer = new Customer();
        Barber barber = new Barber();

        // init barber
        Thread barberThread = new Thread(barber);
        barberThread.setName("barber");
        // Quit program once customers are done
        barberThread.setDaemon(true);
        barberThread.start();

        // init customers
        Thread customer1 = new Thread(customer);
        Thread customer2 = new Thread(customer);
        Thread customer3 = new Thread(customer);
        Thread customer4 = new Thread(customer);
        Thread customer5 = new Thread(customer);
        Thread customer6 = new Thread(customer);
        Thread customer7 = new Thread(customer);
        Thread customer8 = new Thread(customer);
        Thread customer9 = new Thread(customer);
        Thread customer10 = new Thread(customer);
        Thread customer11 = new Thread(customer);
        Thread customer12 = new Thread(customer);
        Thread customer13 = new Thread(customer);
        Thread customer14 = new Thread(customer);
        Thread customer15 = new Thread(customer);

        customer1.setName("customer 1");
        customer2.setName("customer 2");
        customer3.setName("customer 3");
        customer4.setName("customer 4");
        customer5.setName("customer 5");
        customer6.setName("customer 6");
        customer7.setName("customer 7");
        customer8.setName("customer 8");
        customer9.setName("customer 9");
        customer10.setName("customer 10");
        customer11.setName("customer 11");
        customer12.setName("customer 12");
        customer13.setName("customer 13");
        customer14.setName("customer 14");
        customer15.setName("customer 15");

        customer1.start();
        customer2.start();
        customer3.start();
        customer4.start();
        customer5.start();
        customer6.start();
        customer7.start();
        customer8.start();
        customer9.start();
        customer10.start();
        customer11.start();
        customer12.start();
        customer13.start();
        customer14.start();
        customer15.start();
    }

    static class Customer implements Runnable {
        @Override
        public void run() {
            try {
                Random rand = new Random();
                Thread.sleep(rand.nextInt(2500) + 2000);
                // Make sure no other reader is trying to add itself
                mutex.acquire();
                if (currentCustomers == maxCustomers) {
                    mutex.release();
                    System.out.println("All chairs taken, " + Thread.currentThread().getName() + " is leaving");
                    return;
                }
                // Increment current customers to get a haircut next
                currentCustomers++;
                mutex.release();

                customerMutex.release();
                // Wait for barber to be available
                System.out.println(Thread.currentThread().getName() + " waiting in chair");
                barberMutex.acquire();

                // Get hair cut
                System.out.println(Thread.currentThread().getName() + " Getting hair cut");
                Thread.sleep(250);
                System.out.println(Thread.currentThread().getName() + " Finished hair cut");

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
                    Thread.sleep(250);

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
