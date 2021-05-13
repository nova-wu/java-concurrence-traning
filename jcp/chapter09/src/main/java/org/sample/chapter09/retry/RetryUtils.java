package org.sample.chapter09.retry;

import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 *
 * @param <T> 参数类型
 * @param <R> 返回值类型
 */
public class RetryUtils<T, R> {
    // 最大重试次数
    private int         maxRetries;
    // 延迟执行时间
    private long        delay = TimeUnit.SECONDS.toMillis(1);
    // 执行间隔
    private long        interval = TimeUnit.SECONDS.toMillis(1);

    // 轮询函数
    private Function<T, R>      loopFunc = null;
    // 轮询参数
    private T                   loopFuncParam = null;
    // 轮询结果校验函数
    private Predicate<R>        predicate = null;
    // 执行成功后的回调函数
    private Consumer<R> onComplete = null;
    // 超时后的回调函数
    private Consumer<R>    onTimeout = null;

    private ScheduledExecutorService    schedService = Executors.newScheduledThreadPool(10);

    public RetryUtils() {
    }

    // region 参数构建
    /**
     * 设置最大重试次数
     * @param maxRetries 最大重试次数
     * @return Retrying实例
     */
    public org.sample.chapter09.retry.RetryUtils<T, R> atMost(int maxRetries) {
        this.maxRetries = maxRetries;
        return this;
    }


    /**
     * 设置延迟执行时间
     * @param delay 时间
     * @param unit  单位
     * @return Retrying实例
     */
    public org.sample.chapter09.retry.RetryUtils<T, R> withDelay(int delay, TimeUnit unit) {
        this.delay = unit.toMillis(delay);
        return this;
    }

    /**
     * 设置执行间隔时间
     * @param interval 时间
     * @param unit     单位
     * @return Retrying实例
     */
    public org.sample.chapter09.retry.RetryUtils<T, R> withInterval(int interval, TimeUnit unit) {
        this.interval = unit.toMillis(interval);
        return this;
    }

    /**
     * 设置轮询函数
     * @param function 轮询函数
     * @return Retrying实例
     */
    public org.sample.chapter09.retry.RetryUtils<T, R> withLoopFunc(Function<T, R> function) {
        this.loopFunc = function;
        return this;
    }

    /**
     * 设置轮询函数的参数
     * @param param 参数
     * @return Retrying实例
     */
    public org.sample.chapter09.retry.RetryUtils<T, R> withLoopFuncParam(T param) {
        this.loopFuncParam = param;
        return this;
    }

    /**
     * 设置轮询结果校验函数
     * @param predicate 校验函数
     * @return Retrying实例
     */
    public org.sample.chapter09.retry.RetryUtils<T, R> until(Predicate<R> predicate) {
        this.predicate = predicate;
        return this;
    }

    /**
     * 设置执行成功后的回调函数
     * @param onComplete 回调函数
     * @return Retrying实例
     */
    public org.sample.chapter09.retry.RetryUtils<T, R> doOnComplete(Consumer<R> onComplete) {
        this.onComplete = onComplete;
        return this;
    }

    /**
     * 设置执行超时后的回调函数
     * @param onTimeout 回调函数
     * @return Retrying实例
     */
    public org.sample.chapter09.retry.RetryUtils<T, R> doOnTimeOut(Consumer<R> onTimeout) {
        this.onTimeout = onTimeout;
        return this;
    }
    // endregion

    /**
     * 同步执行
     * @return 轮询函数执行成功后的结果
     */
    public R executeSync() {
        RetryingFuture<R> retryingFuture = new RetryingFuture<>();
        MyTimerTask myTimerTask = new MyTimerTask(
                maxRetries, loopFunc, loopFuncParam, predicate, onComplete, onTimeout, retryingFuture);
        schedService.schedule(myTimerTask, delay, TimeUnit.MILLISECONDS);

        retryingFuture.await();
        return retryingFuture.result;
    }

    /**
     * 异步执行
     */
    public void executeAsync() {
        MyTimerTask myTimerTask = new MyTimerTask(
                maxRetries, loopFunc, loopFuncParam, predicate, onComplete, onTimeout, null);
        schedService.schedule(myTimerTask, delay, TimeUnit.MILLISECONDS);
    }

    private class Pair<K, V> {
        private K key;
        private V value;

        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private class RetryingFuture<T> {
        private final Lock lock = new ReentrantLock();
        private final Condition done = lock.newCondition();
        private volatile boolean isDone = false;
        private volatile T result;

        public void await() {
            lock.lock();
            try {
                while (!isDone) {
                    done.await();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
            }
        }

        public void signalAll() {
            lock.lock();
            try {
                this.isDone = true;
                done.signalAll();
            } finally {
                lock.unlock();
            }
        }
    }

    private class MyTimerTask<T, R> implements Runnable {
        final private int                 maxRetries;
        final private Function<T, R>      function;
        final private T                   param;
        final private Predicate<R>        predicate;
        final private Consumer<R>    onComplete;
        final private Consumer<R>    onTimeout;
        final private RetryingFuture      retryingFuture;

        private int idx = 0;

        public MyTimerTask(int maxRetries, Function<T, R> function, T param, Predicate<R> predicate,
                           Consumer<R> onComplete, Consumer<R> onTimeout,
                           RetryingFuture retryingFuture) {
            this.maxRetries = maxRetries;
            this.function = function;
            this.param = param;
            this.predicate = predicate;
            this.onComplete = onComplete;
            this.onTimeout = onTimeout;
            this.retryingFuture = retryingFuture;
        }

        /**
         * 示例程序，忽略异常处理
         */
        @Override
        public void run() {
            // 调用轮询函数
            R result = function.apply(param);
            // 测试结果
            boolean isOk = predicate.test(result);
            // 如果没有通过测试，并且允许重试
            if (!isOk && ++idx < maxRetries) {
                // 重新调度
                schedService.schedule(this, interval, TimeUnit.MILLISECONDS);
            } else {
                // 通过测试
                if (isOk) {
                    if (onComplete != null) {
                        onComplete.accept(result);
                    }
                    if (retryingFuture != null) {
                        retryingFuture.result = result;
                        retryingFuture.signalAll();
                    }
                } else {
                    // 超过最大重试次数
                    if (onTimeout != null) {
                        onTimeout.accept(result);
                    }
                    // 同步执行
                    if (retryingFuture != null) {
                        retryingFuture.signalAll();
                    }
                }
            }
        }
    }
}
