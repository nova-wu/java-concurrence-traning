package org.sample.chapter08.checking;

import java.util.Vector;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 基于CyclicBarrier的对账模拟程序
 */
public class CyclicBarrierChecking {
    // 订单队列
    static final Vector<Object> pos = new Vector<>();
    // 派送单队列
    static final Vector<Object> dos = new Vector<>();
    // 执行回调的线程池
    static final ExecutorService executor = Executors.newSingleThreadExecutor();

    static final CyclicBarrier barrier = new CyclicBarrier(2, () -> {
        executor.execute(() -> check());
    });

    /**
     * 模拟对账操作
     */
    static void check() {
        Object p = pos.remove(0);
        Object d = dos.remove(0);
        // 以下模拟对账操作
        System.out.println("P:" + p + " D:" + d);
    }

    public static void main(String[] args) throws Exception {
        // 模拟订单查询
        Thread T1 = new Thread(() -> {
            int idx = 0;
            while (idx < 10000) {
                pos.add(idx++);
                try {
                    barrier.await();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        // 模拟派送单查询
        Thread T2 = new Thread(() -> {
            int idx = 0;
            while (idx < 10000) {
                try {
                    dos.add(idx++);
                    barrier.await();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        T1.setName("T1");
        T2.setName("T2");

        T1.start();
        T2.start();

        T1.join();
        T2.join();

        executor.shutdown();
    }
}
