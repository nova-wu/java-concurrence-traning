package org.sample.chapter08.report;

import lombok.SneakyThrows;
import java.util.concurrent.*;

/**
 * 异步上报
 * 将指标数据加入阻塞队列，异步汇总后上报汇总数据
 * 目标：降低上报频率
 */
public class Reporter {
    // 线程池
    private final ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

    // 为每一个指标分配一个阻塞队列
    private final ConcurrentHashMap<String, BlockingQueue<Integer>> queueMap = new ConcurrentHashMap<>();
    // 为每一个指标分配一个工作线程
    private final ConcurrentHashMap<String, ReportWorker> workerMap = new ConcurrentHashMap<>();

    /**
     * 汇报指标数据
     * @param key    指标关键字
     * @param value  指标数据
     */
    public synchronized void report(String key, Integer value) {
        BlockingQueue<Integer> queue = queueMap.get(key);
        // 多任务同时结转同一张表时，需要重新创建同步器
        if (queue == null) {
            createReportWorker(key);
            queue = queueMap.get(key);
        }

        // 替换最后一个
        queue.offer(value);
    }

    /**
     * 创建汇报线程
     * @param key 指标关键字
     */
    private void createReportWorker(String key) {
        ArrayBlockingQueue<Integer> curQueue = null;
        if (!queueMap.containsKey(key)) {
            curQueue = new ArrayBlockingQueue<Integer>(100);
            queueMap.putIfAbsent(key, curQueue);
        }
        if (!workerMap.containsKey(key)) {
            ReportWorker worker = new ReportWorker(key, curQueue);
            if (workerMap.putIfAbsent(key, worker) == null) {
                cachedThreadPool.execute(worker);
            }
        }
    }

    /**
     * 销毁汇报线程
     * @param key 指标关键字
     */
    public synchronized void destroyReportWorker(String key) {
        queueMap.remove(key);
        ReportWorker worker = workerMap.remove(key);
        if (worker != null) {
            worker.stop();
        }
    }

    /**
     * 执行汇报工作
     */
    private class ReportWorker implements Runnable {
        private final String name;
        private final BlockingQueue<Integer> queue;
        private volatile boolean running = true;

        public ReportWorker(String name, BlockingQueue<Integer> queue) {
            this.name = name;
            this.queue = queue;
        }

        public void stop() {
            running = false;
        }

        /**
         * 将队列中的指标数据全部取出，并汇总
         * @return 队列中所有指标数据的汇总值
         * @throws InterruptedException
         */
        private Integer pollAndCalcTotal() throws InterruptedException {
            Integer total = 0;
            // 不可以永远等待
            Integer curValue = this.queue.poll(5, TimeUnit.SECONDS);
            while (curValue != null) {
                total = total + curValue;
                curValue = this.queue.poll();
            }

            return total;
        }

        @SneakyThrows
        private void sleep(long millis) {
            Thread.sleep(millis);
        }

        @Override
        public void run() {
            Thread.currentThread().setName(this.name);
            while (running) {
                try {
//                    // 等待5秒
//                    sleep(5000);

                    Integer total = pollAndCalcTotal();
                    // 下面模拟执行汇报操作
                    if (total.compareTo(0) > 0) {
                        System.out.println("report:" + total + ":" + Thread.currentThread().getName());
                    }
                } catch (Exception e) {
                    System.err.println("error in report task" + e.getMessage());
                }
            }
            // 处理最后一个任务
            try {
                Integer total = pollAndCalcTotal();
                // 下面模拟执行汇报操作
                if (total.compareTo(0) > 0) {
                    System.out.println("report-end:" + total + ":" + Thread.currentThread().getName());
                }
            } catch (Exception e) {
                System.err.println("error in report task" + e.getMessage());
            }
        }
    }
}
