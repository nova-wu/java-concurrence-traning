package org.sample.chapter07.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Cache<K, V> {
    final Map<K, V> m = new HashMap<>();
    final ReentrantReadWriteLock rwl
            = new ReentrantReadWriteLock();
    final Lock r = rwl.readLock();
    final Lock w = rwl.writeLock();

    V get(K key) {
        r.lock();
        try { return m.get(key); }
        finally { r.unlock(); }
    }
    V put(K key, V value) {
        w.lock();
        try { return m.put(key, value); }
        finally { w.unlock(); }
    }
    void clear() {
        w.lock();
        try { m.clear(); }
        finally { w.unlock(); }
    }

    public static void main(String [] args) throws InterruptedException {
        ExecutorService executor = Executors.newCachedThreadPool();
        Cache<String, Long> cache = new Cache<>();
        final Random wr = new Random(0);
        final Random rr = new Random(0);

        for(int i=0; i<10; i++) {
            executor.execute(()->{
                Long kv = wr.nextLong();
                cache.put(Long.toString(kv), kv);
                System.out.println("+++: " + kv);
            });

            executor.execute(()->{
                Long k = rr.nextLong();
                Long v = cache.get(Long.toString(k));
                System.out.println("--: " + k + ":" + v);
            });
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.DAYS);
    }
}
