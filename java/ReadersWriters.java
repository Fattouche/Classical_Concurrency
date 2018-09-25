import java.util.Random;
import java.util.concurrent.Semaphore;

//Solution taken from little book of semaphores and applied to java
public class ReadersWriters {

  static Semaphore mutex = new Semaphore(1);
  static Semaphore roomEmpty = new Semaphore(1);
  static int numReaders;
  static int valueToRead;

  public static void main(String args[]) {
    valueToRead = 0;
    numReaders = 0;
    Reader reader = new Reader();
    Writer writer = new Writer();

    // init readers
    Thread thread0 = new Thread(reader);
    Thread thread1 = new Thread(reader);
    Thread thread2 = new Thread(reader);
    Thread thread3 = new Thread(reader);
    Thread thread4 = new Thread(reader);

    thread0.setName("thread0");
    thread1.setName("thread1");
    thread2.setName("thread2");
    thread3.setName("thread3");
    thread4.setName("thread4");

    // init writers
    Thread thread5 = new Thread(writer);
    Thread thread6 = new Thread(writer);
    Thread thread7 = new Thread(writer);
    Thread thread8 = new Thread(writer);
    Thread thread9 = new Thread(writer);

    thread5.setName("thread5");
    thread6.setName("thread6");
    thread7.setName("thread7");
    thread8.setName("thread8");
    thread9.setName("thread9");

    thread0.start();
    thread1.start();
    thread2.start();
    thread3.start();
    thread4.start();
    thread5.start();
    thread6.start();
    thread7.start();
    thread8.start();
    thread9.start();
  }

  static class Reader implements Runnable {
    @Override
    public void run() {
      try {
        Random rand = new Random();
        Thread.sleep(rand.nextInt(1000) + 250);
        // Make sure no other reader is trying to add itself
        mutex.acquire();
        // Increment number of readers
        numReaders++;
        // If first reader, notify everyone that the room is not empty anymore
        if (numReaders == 1) {
          roomEmpty.acquire();
        }
        mutex.release();
        // Read value
        System.out.println("Thread: " + Thread.currentThread().getName() + " Reading value " + valueToRead);
        Thread.sleep(250);
        mutex.acquire();
        // Decrement readers
        numReaders--;
        if (numReaders == 0) {
          // Notify room is empty
          roomEmpty.release();
        }
        mutex.release();

      } catch (InterruptedException e) {
        System.out.println(e.getMessage());
      }
    }
  }

  static class Writer implements Runnable {
    @Override
    public void run() {
      try {
        Random rand = new Random();
        Thread.sleep(rand.nextInt(500) + 250);
        // Make sure the room is empty
        roomEmpty.acquire();
        int val = rand.nextInt(10) + 1;
        // Write value
        valueToRead = val;
        Thread.sleep(500);
        System.out.println("Thread: " + Thread.currentThread().getName() + " Writing value " + valueToRead);
        // Finish with the room
        roomEmpty.release();
      } catch (InterruptedException e) {
        System.out.println(e.getMessage());
      }
    }
  }
}
