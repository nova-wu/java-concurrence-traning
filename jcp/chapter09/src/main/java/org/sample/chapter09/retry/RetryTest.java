package org.sample.chapter09.retry;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class RetryTest {
    public static void main(String[] args) {

        RetryUtils<Random, Integer> retrying = new RetryUtils<>();
        // 异步执行
        retrying.withDelay(1, TimeUnit.SECONDS)          // 延迟1秒执行
                .withInterval(1, TimeUnit.SECONDS)      // 调用间隔1秒
                .atMost(10)                                   // 最多执行10次
                .withLoopFuncParam(new Random())
                .withLoopFunc(req -> req.nextInt(5))       // 每次循环生成新的随机数
                .until(r -> r != null && r.intValue()==3)
                .doOnTimeOut((t) -> {
                    System.out.println("Timeout in Async:" + t);
                })
                .doOnComplete((t) -> {
                    System.out.println("Complete in Async:" + t);
                })
                .executeAsync();
        // 同步执行
        Integer result = retrying.withDelay(1, TimeUnit.SECONDS)          // 延迟1秒执行
                .withInterval(1, TimeUnit.SECONDS)        // 调用间隔1秒
                .atMost(2)                                         // 最多执行2次
                .withLoopFuncParam(new Random())
                .withLoopFunc(req -> req.nextInt(6))       // 每次循环生成新的随机数
                .until(r -> r != null && r.intValue()==3)
                .doOnTimeOut((t) -> {
                    System.out.println("Timeout in Sync:" + t);
                })
                .doOnComplete((t) -> {
                    System.out.println("Complete in Sync:" + t);
                })
                .executeSync();
        System.out.println("Result:" + result);
        
    }
}
