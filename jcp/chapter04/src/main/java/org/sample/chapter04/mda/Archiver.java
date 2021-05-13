package org.sample.chapter04.mda;

import java.util.concurrent.*;

public class Archiver {
    final private ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
            3, 3,
            5, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(1000),
            Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());

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

    public void archive(String name) {
        threadPool.submit(new ArchiveTask(name));
    }

    public static void main(String[] args) throws Exception {
        Archiver archiver = new Archiver();
        for (int idx = 1; idx <= 20; idx++) {
            archiver.archive("Table" + idx);
        }

        archiver.threadPool.shutdown();
    }
}
