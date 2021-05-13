package org.sample.chapter04.account;

import java.util.concurrent.locks.ReentrantLock;

public class LockAccount {
    private long id;
    private int balance;
    private final ReentrantLock lock = new ReentrantLock();

    //
    public void transfer(LockAccount target, int amt) {
        this.lock.lock();
        try {
            target.lock.lock();
            try {
                if (this.balance > amt) {
                    this.balance -= amt;
                    target.balance += amt;
                }
            } finally {
                target.lock.unlock();
            }
        } finally {
            this.lock.unlock();
        }
    }

    public LockAccount(int balance) {
        this.balance = balance;
    }

    public static void main(String[] args) throws Exception {
        final LockAccount a = new LockAccount(200);
        final LockAccount b = new LockAccount(200);

        Thread th1 = new Thread(()-> {
            int idx = 0;
            while (idx++ < 100)
                a.transfer(b, 100);

        }, "T1");
        Thread th2 = new Thread(()->{
            int idx = 0;
            while (idx++ < 100)
                b.transfer(a, 100);
        }, "T2");

        th1.start();
        th2.start();
        th1.join();
        th2.join();

        System.out.println("End");
    }
}
