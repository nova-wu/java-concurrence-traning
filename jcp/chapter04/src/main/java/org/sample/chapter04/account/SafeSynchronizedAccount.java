package org.sample.chapter04.account;

public class SafeSynchronizedAccount {
    private long id;
    private int balance;

    public SafeSynchronizedAccount(long id, int balance) {
        this.id = id;
        this.balance = balance;
    }
    //
    public void transfer(SafeSynchronizedAccount target, int amt){
        SafeSynchronizedAccount left = this;
        SafeSynchronizedAccount right = target;
        if (this.id > target.id) {
            left = target;
            right = this;
        }

        synchronized(left) {
            synchronized(right) {
                if (this.balance > amt) {
                    this.balance -= amt;
                    target.balance += amt;
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        final SafeSynchronizedAccount a = new SafeSynchronizedAccount(1,200);
        final SafeSynchronizedAccount b = new SafeSynchronizedAccount(2, 200);

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
