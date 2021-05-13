package org.sample.chapter07.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Cache3<K, V> {
    final Map<Integer, Integer> m = new HashMap<>();
    final ReentrantReadWriteLock rwl
            = new ReentrantReadWriteLock();
    final Lock r = rwl.readLock();
    final Lock w = rwl.writeLock();

    final Random wr = new Random(0);

    Integer getNext(){
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return wr.nextInt();
    }

    Integer get(Integer key) {
        Integer v = null;
        //读缓存
        r.lock();
        try {
            v = m.get(key);
            System.out.println("yyyyy");
            w.lock();
            v = 111;
            w.unlock();

            System.out.println("xxxxx");

        }
        finally{r.unlock();}
        //缓存中存在，返回
        if(v != null) {
            return v;
        }
        //缓存中不存在，查询数据库
        w.lock();
        try {
            //再次验证
            //其他线程可能已经查询过数据库
            v = m.get(key);
            if(v == null){
                //查询数据库
                //V value=省略代码无数
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                v = key;
                m.put(key, v);
            }
        }finally{
            w.unlock();
        }
        return v;
    }


    public static void main(String [] args) throws InterruptedException {
        ExecutorService executor = Executors.newCachedThreadPool();
        Cache3<String, Integer> cache = new Cache3<>();
        final Random rr = new Random(0);

        for(int i=0; i<10; i++) {
            final Integer k = rr.nextInt();
            executor.execute(()->{
                Integer v = cache.get(k);
                System.out.println("---" + System.currentTimeMillis() + "--: " + k + ":" + v );

            });

            executor.execute(()->{
                Integer v = cache.get(k);
                System.out.println("+++" + System.currentTimeMillis() + "--: " + k + ":" + v );

            });
        }


        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.DAYS);
    }
}
