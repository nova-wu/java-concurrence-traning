package org.sample.chapter04.mda;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 该示例程序是数据结转程序的高级版本
 * 需求：能够结转分表的数据表，分表规则：表名[1~n] 或 表名[n+1~m]
 * 方案：将结转任务分成两个阶段：
 *      第一阶段用于分解任务，例如：将用户输入的 表名[1~3] 解析成3个任务 表名1，表名2，表名3
 *      第二阶段用于执行结转任务
 * 注意：该实现存在死锁
 */
public class DeadLockArchiver {
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
     * 逻辑任务：表名=物理表名或者分表表名
     */
    public class LogicTask implements Callable<String> {
        // 匹配[数字~数字]结尾
        private final Pattern pattern = Pattern.compile("(\\w+)\\[(\\d+)~(\\d+)\\]$");

        private String taskName;
        public LogicTask(String taskName) {
            this.taskName = taskName;
        }

        /**
         * 分解任务
         * @param name 表名
         * @param from 分表索引从
         * @param to   分表索引到
         * @return 需要结转的物理表名
         */
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
        // 第一阶段：分解任务
            List<String> result = new ArrayList<>();
            // 如果表名以[数字~数字]结尾，需要分解任务
            Matcher m = pattern.matcher(taskName);
            if (m.find()) {
                String name = m.group(1);
                int from = Integer.parseInt(m.group(2));
                int to = Integer.parseInt(m.group(3));
                // 分解任务
                result.addAll(forkTask(name, from, to));
            } else {
                // 无需分解，表名即物理表名
                result.add(taskName);
            }

        // 第二阶段：执行结转任务
            for (String parsedName : result) {
                try {
                    Future<String> future = archivePool.submit(new ArchiveTask(parsedName));
                    String name = future.get();
                    System.out.println("完成聚合子任务：" + name);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return taskName;
        }
    }

    /**
     * 结转数据库中的表
     * @param name 逻辑表名
     */
    public void archive(String name) {
        archivePool.submit(new LogicTask(name));
    }

    public static void main(String[] args) throws Exception {
        DeadLockArchiver archiver = new DeadLockArchiver();
        for (int idx = 1; idx <= 20; idx++) {
            if (idx % 3 == 0) {
                String taskName = "Table[" + (idx-2) + "~" + idx + "]";
                System.out.println("待结转任务：" + taskName);
                archiver.archive(taskName);
            }
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
