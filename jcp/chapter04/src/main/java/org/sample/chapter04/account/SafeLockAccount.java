package org.sample.chapter04.account;

import java.util.concurrent.locks.ReentrantLock;

public class SafeLockAccount {
    private long id;
    private int balance;
    private final ReentrantLock lock = new ReentrantLock();

    //
    public void transfer(SafeLockAccount target, int amt) {
        SafeLockAccount left = this;
        SafeLockAccount right = target;
        if (this.id > target.id) {
            left = target;
            right = this;
        }

        left.lock.lock();
        try {
            right.lock.lock();
            try {
                if (this.balance > amt) {
                    this.balance -= amt;
                    target.balance += amt;
                }
            } finally {
                right.lock.unlock();
            }
        } finally {
            left.lock.unlock();
        }
    }

    public SafeLockAccount(long id, int balance) {
        this.id = id;
        this.balance = balance;
    }

    public static void main(String[] args) throws Exception {
        final SafeLockAccount a = new SafeLockAccount(1, 200);
        final SafeLockAccount b = new SafeLockAccount(2, 200);

        Thread th1 = new Thread(()->{
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
