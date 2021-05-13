package org.sample.chapter08.checking;

import java.util.concurrent.CountDownLatch;

public class SimpleChecking {
    public static void main(String[] args) throws InterruptedException{
        // 查询未对账订单
        final int[] as = new int[2];
        CountDownLatch latch = new CountDownLatch(2);
        Thread T1 = new Thread(()-> {
            as[0] = 1;
            latch.countDown();
        });
        T1.start();
        // 查询派送单
        Thread T2 =new Thread(()-> {
            as[1] = 2;
            latch.countDown();
        });
        T2.start();
        // 等待T1、T2结束
        latch.await();
        // 执行对账操作
        int x = as[0] + as[1];
        // 差异写入差异库
        System.out.println(x);
    }
}
