package org.sample.chapter04.account;


import lombok.SneakyThrows;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class TryLockAccount {
    private int balance;
    private final Lock lock = new ReentrantLock();

    public TryLockAccount(int balance) {
        this.balance = balance;
    }

    // 转账
    @SneakyThrows
    void transfer(TryLockAccount tar, int amt){
        while (true) {
            if(this.lock.tryLock()) {
                try {
                    if (tar.lock.tryLock()) {
                        try {
                            this.balance -= amt;
                            tar.balance += amt;
                            // 退出循环
                            break;
                        } finally {
                            tar.lock.unlock();
                        }
                    }//if
                } finally {
                    this.lock.unlock();
                }
            }//if
            // sleep一个随机时间避免活锁
            int randomInt = ThreadLocalRandom.current().nextInt(100, 500);
            Thread.sleep(randomInt);
        }//while
    }//transfer

    public static void main(String[] args) throws Exception {
        final TryLockAccount a = new TryLockAccount(200);
        final TryLockAccount b = new TryLockAccount(200);

        Thread th1 = new Thread(()-> {
            int idx = 0;
            while (idx++ < 1000)
                a.transfer(b, 100);

        }, "T1");
        Thread th2 = new Thread(()->{
            int idx = 0;
            while (idx++ < 1000)
                b.transfer(a, 100);
        }, "T2");

        th1.start();
        th2.start();
        th1.join();
        th2.join();

        System.out.println("End");
    }
}
