package org.sample.chapter03.account;


public class SynchronizedAccount {
    /**
     * 余额
     */
    private int balance;

    public SynchronizedAccount(int balance) {
        this.balance = balance;
    }

    /**
     * 转账
     * @param target 目标账户
     * @param amt 转账金额
     */
    public void transfer(SynchronizedAccount target, int amt){
        synchronized(SynchronizedAccount.class) {
            if (this.balance > amt) {
                this.balance -= amt;
                target.balance += amt;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        final SynchronizedAccount a = new SynchronizedAccount(200);
        final SynchronizedAccount b = new SynchronizedAccount(200);

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
