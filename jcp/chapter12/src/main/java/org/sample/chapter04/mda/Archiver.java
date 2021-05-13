package org.sample.chapter04.mda;

import java.util.concurrent.*;

/**
 * 该示例程序是数据结转程序的简化
 * 需求：能够同时结转多个数据库中的表
 */
public class Archiver {
    /**
     * 执行结转任务的线程池
     */
    final private ThreadPoolExecutor archivePool = new ThreadPoolExecutor(
            3, 3,
            5, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(1000),
            Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());

    /**
     * 结转任务：该任务会提交到线程池中执行
     */
    public class ArchiveTask implements Callable<String> {
        private final String taskName;
        public ArchiveTask(String taskName) {
            this.taskName = taskName;
        }

        @Override
        public String call() throws Exception {
            System.out.println("结转任务：" + taskName);
            return taskName;
        }
    }

    /**
     * 结转数据库中的表
     * @param name 表名
     */
    public void archive(String name) {
        archivePool.submit(new ArchiveTask(name));
    }

    public static void main(String[] args) throws Exception {
        Archiver archiver = new Archiver();
        for (int idx = 1; idx <= 20; idx++) {
            archiver.archive("Table" + idx);
        }

        // 等待1秒
        Thread.sleep(1000);
        // 关闭线程池
        archiver.archivePool.shutdown();
        while ( !archiver.archivePool.isTerminated() ) {
            Thread.sleep(1000);
        }

        System.out.println("END");
    }
}
