package org.sample.chapter07.ratelimiter;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * 使用信号量实现令牌桶限流器
 */
public class SemaphoreRateLimiter {
    private Semaphore semaphore;
    private int maxPermits;
    private TimeUnit timePeriod;
    private ScheduledExecutorService scheduler;

    /**
     * 创建限流器
     * @param permits
     * @param timePeriod
     * @return
     */
    public static SemaphoreRateLimiter create(int permits, TimeUnit timePeriod) {
        SemaphoreRateLimiter limiter = new SemaphoreRateLimiter(permits, timePeriod);
        limiter.replenishTokensAtFixedRate();
        return limiter;
    }

    private SemaphoreRateLimiter(int permits, TimeUnit timePeriod) {
        this.semaphore = new Semaphore(permits);
        this.maxPermits = permits;
        this.timePeriod = timePeriod;
    }

    /**
     * 获取1个令牌
     * @return
     */
    public boolean tryAcquire() {
        return semaphore.tryAcquire();
    }

    public void stop() {
        scheduler.shutdownNow();
    }

    /**
     * 以固定频率向令牌桶中补充令牌
     */
    public void replenishTokensAtFixedRate() {
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            semaphore.release(maxPermits - semaphore.availablePermits());
        }, 1, 1, timePeriod);
    }

    public static void main(String[] args) throws Exception {
        final SemaphoreRateLimiter limiter = SemaphoreRateLimiter.create(5, TimeUnit.SECONDS);

        Thread[] threads = new Thread[10];
        for (int idx=0; idx<10; idx++) {
            threads[idx] = new Thread(()->{
                try {
                    String name = Thread.currentThread().getName();
                    boolean isPermit = limiter.tryAcquire();
                    System.out.println(name + ":-" + isPermit);

                    Thread.sleep(1050);
                    isPermit = limiter.tryAcquire();
                    System.out.println(" " + name + ":+" + isPermit);

                    Thread.sleep(1050);
                    isPermit = limiter.tryAcquire();
                    System.out.println("  " + name + ":+" + isPermit);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, String.valueOf(idx));
        }

        for (int idx=0; idx<10; idx++) {
            threads[idx].start();
        }

        for (int idx=0; idx<10; idx++) {
            threads[idx].join();
        }

        limiter.stop();
    }
}
