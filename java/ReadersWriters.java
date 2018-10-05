import java.util.Random;
import java.util.concurrent.Semaphore;

//Solution taken from little book of semaphores and applied to java
public class ReadersWriters {

  static Semaphore mutex = new Semaphore(1);
  static Semaphore roomEmpty = new Semaphore(1);
  static int numReaders;
  static int valueToRead;

  static int myGlobal;

  public static void sleepThread() {
    try {
      Thread.sleep((long) (Math.random() * 250));
    } catch (InterruptedException e) {
      System.out.println(e.getMessage());
    }
  }

  public static void main(String args[]) {
    int totalReaders = 10;
    int totatlWriters = 5;

    myGlobal = 0;
    numReaders = 0;
    Reader reader = new Reader();
    Writer writer = new Writer();

    Thread[] readers = new Thread[totalReaders];
    for (int i = 0; i < readers.length; i++) {
      readers[i] = new Thread(reader);
      readers[i].setName("Reader " + i);
      readers[i].start();
    }

    Thread[] writers = new Thread[totatlWriters];
    for (int i = 0; i < writers.length; i++) {
      writers[i] = new Thread(writer);
      writers[i].setName("Writer " + i);
      writers[i].start();
    }
  }

  static class Reader implements Runnable {
    @Override
    public void run() {
      try {
        sleepThread();
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
        System.out.println("Read: " + myGlobal);
        sleepThread();
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
        sleepThread();
        // Make sure the room is empty
        roomEmpty.acquire();
        int temp = myGlobal;
        // Write value
        myGlobal++;
        System.out.println("Write changed from " + temp + " to " + myGlobal);
        sleepThread();
        // Finish with the room
        roomEmpty.release();
      } catch (InterruptedException e) {
        System.out.println(e.getMessage());
      }
    }
  }
}
