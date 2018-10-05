import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CacheTester {

    public static void sleepThread(int time) {
        try {
            Thread.sleep((long) (Math.random() * time));
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }

    static class Cache {
        private Map<String, Object> objects = new HashMap<String, Object>();
        private ReentrantReadWriteLock mutex = new ReentrantReadWriteLock();

        public boolean insert(String key, String value, long expiration) {
            mutex.writeLock().lock();
            if (objects.containsKey(key)) {
                mutex.writeLock().unlock();
                return false;
            }
            objects.put(key, new Object(expiration, value));
            mutex.writeLock().unlock();
            return true;
        }

        public boolean remove(String key) {
            mutex.writeLock().lock();
            Object obj = objects.get(key);
            if (obj == null) {
                mutex.writeLock().unlock();
                return false;
            }
            if (obj.isExpired()) {
                objects.remove(key);
                mutex.writeLock().unlock();
                return false;
            }
            objects.remove(key);
            mutex.writeLock().unlock();
            return true;
        }

        public boolean exists(String key) {
            mutex.readLock().lock();
            Object obj = objects.get(key);
            if (obj == null) {
                mutex.readLock().unlock();
                return false;
            }
            if (obj.isExpired()) {
                objects.remove(key);
                mutex.readLock().unlock();
                return false;
            }
            mutex.readLock().unlock();
            return true;
        }

        public String get(String key) {
            mutex.readLock().lock();
            Object obj = objects.get(key);
            if (obj == null) {
                mutex.readLock().unlock();
                return null;
            }
            if (obj.isExpired()) {
                mutex.readLock().unlock();
                return null;
            }
            mutex.readLock().unlock();
            return obj.getValue();
        }
    }

    static class Object {
        private long expiration;
        private String value;

        public Object(long expiration, String value) {
            this.expiration = System.currentTimeMillis() + expiration * 1000;
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public boolean isExpired() {
            if (expiration == 0) {
                return true;
            }
            return System.currentTimeMillis() >= expiration;
        }
    }

    static class Inserter implements Runnable {
        Cache cache;

        public Inserter(Cache cache) {
            this.cache = cache;
        }

        @Override
        public void run() {
            System.out.println("Inserting 1-10");
            cache.insert("1", "1", 2);
            cache.insert("2", "2", 5);
            cache.insert("3", "3", 3);
            cache.insert("4", "4", 2);
            cache.insert("5", "5", 5);
            cache.insert("6", "6", 3);
            cache.insert("7", "7", 2);
            cache.insert("8", "8", 5);
            cache.insert("9", "9", 3);
            cache.insert("10", "10", 1);
        }
    }

    static class Searcher implements Runnable {
        Cache cache;

        public Searcher(Cache cache) {
            this.cache = cache;
        }

        @Override
        public void run() {
            sleepThread(100);
            System.out.println("Searching 1");
            String val = cache.get("1");
            if (val == null) {
                System.out.println("Failed to find 1");
            } else {
                System.out.println("Found 1 with value " + val);
            }

            sleepThread(100);
            System.out.println("Searching 2");
            val = cache.get("2");
            if (val == null) {
                System.out.println("Failed to find 2");
            } else {
                System.out.println("Found 2 with value " + val);
            }

            sleepThread(100);
            System.out.println("Searching 5");
            val = cache.get("5");
            if (val == null) {
                System.out.println("Failed to find 5");
            } else {
                System.out.println("Found 5 with value " + val);
            }

            sleepThread(100);
            System.out.println("Searching 6");
            val = cache.get("6");
            if (val == null) {
                System.out.println("Failed to find 6");
            } else {
                System.out.println("Found 6 with value " + val);
            }
        }
    }

    static class Deleter implements Runnable {
        Cache cache;

        public Deleter(Cache cache) {
            this.cache = cache;
        }

        @Override
        public void run() {
            sleepThread(100);
            System.out.println("Deleting 1");
            boolean removed = cache.remove("1");
            if (!removed) {
                System.out.println("Failed to delete 1");
            } else {
                System.out.println("deleted 1 from cache");
            }

            sleepThread(100);
            System.out.println("Deleting 2");
            removed = cache.remove("2");
            if (!removed) {
                System.out.println("Failed to delete 2");
            } else {
                System.out.println("deleted 2 from cache");
            }

            sleepThread(100);
            System.out.println("Deleting 7");
            removed = cache.remove("7");
            if (!removed) {
                System.out.println("Failed to delete 7");
            } else {
                System.out.println("deleted 7 from cache");
            }

            sleepThread(100);
            System.out.println("Deleting 9");
            removed = cache.remove("9");
            if (!removed) {
                System.out.println("Failed to delete 9");
            } else {
                System.out.println("deleted 9 from cache");
            }
        }
    }

    public static void main(String[] args) {
        Cache myCache = new Cache();

        Inserter inserter = new Inserter(myCache);
        Searcher searcher = new Searcher(myCache);
        Deleter deleter = new Deleter(myCache);

        Thread inserterThread = new Thread(inserter);
        Thread searcherThread = new Thread(searcher);
        Thread deleterThread = new Thread(deleter);

        inserterThread.start();
        searcherThread.start();
        deleterThread.start();

    }

}