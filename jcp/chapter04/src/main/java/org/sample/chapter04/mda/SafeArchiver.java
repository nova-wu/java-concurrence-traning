package org.sample.chapter04.mda;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SafeArchiver {
    final private ThreadPoolExecutor archivePool = new ThreadPoolExecutor(
            3, 3,
            5, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(1000),
            Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
    final private ThreadPoolExecutor parsePool = new ThreadPoolExecutor(
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

    public class ParseTask implements Callable<String> {
        // 匹配[数字~数字]结尾
        private final Pattern pattern = Pattern.compile("(\\w+)\\[(\\d+)~(\\d+)\\]$");

        private String taskName;
        public ParseTask(String taskName) {
            this.taskName = taskName;
        }

        private List<String> forkTask(String name, int from, int to) {
            assert from < to : "from > to";
            List<String> result = new ArrayList<>();
            for (int idx = from; idx <= to; idx++) {
                result.add(name + idx);
            }

            return result;
        }

        @Override
        public String call() throws Exception {
            List<String> result = new ArrayList<>();
            // 如果表名以[数字~数字]结尾，需要展开任务
            Matcher m = pattern.matcher(taskName);
            if (m.find()) {
                String name = m.group(1);
                int from = Integer.parseInt(m.group(2));
                int to = Integer.parseInt(m.group(3));

                result.addAll(forkTask(name, from, to));
            } else {
                result.add(taskName);
            }

            for (String parsedName : result) {
                try {
                    Future<String> future = archivePool.submit(new ArchiveTask(parsedName));
                    String name = future.get();
                    System.out.println("完成聚合任务：" + name);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return taskName;
        }
    }
    public void archive(String name) {
        parsePool.submit(new ParseTask(name));
    }

    public static void main(String[] args) throws Exception {
        SafeArchiver archiver = new SafeArchiver();
        for (int idx = 1; idx <= 20; idx++) {
            if (idx % 3 == 0) {
                String taskName = "Table[" + (idx-2) + "~" + idx + "]";
                System.out.println("待结转任务：" + taskName);
                archiver.archive(taskName);
            }
        }

        Thread.sleep(1000);
        archiver.archivePool.shutdown();
        archiver.parsePool.shutdown();

        while (!archiver.archivePool.isTerminated() || !archiver.parsePool.isTerminated() ) {
            Thread.sleep(1000);
        }

        System.out.println("END");
    }
}
