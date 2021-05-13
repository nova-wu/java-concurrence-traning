package org.sample.chapter08.checking;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ThreadPoolSimpleChecking {
    public static void main(String[] args) throws InterruptedException{
        Executor executor = Executors.newFixedThreadPool(2);

        // 查询未对账订单
        final int[] as = new int[2];
        CountDownLatch latch = new CountDownLatch(2);

        executor.execute(()-> {
            as[0] = 1;
            latch.countDown();
        });

        // 查询派送单
        executor.execute(()-> {
            as[1] = 2;
            latch.countDown();
        });

        // 等待T1、T2结束
        latch.await();
        // 执行对账操作
        int x = as[0] + as[1];
        // 差异写入差异库
        System.out.println(x);
    }
}
