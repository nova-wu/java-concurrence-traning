package org.sample.chapter03.tea;


import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

public class FutureTest {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        FutureTask<String> ft2
                = new FutureTask<>(new T2Task());

        FutureTask<String> ft1
                = new FutureTask<>(new T1Task(ft2));


        Thread T1 = new Thread(ft1);
        T1.start();

        Thread T2 = new Thread(ft2);
        T2.start();

        System.out.println(ft1.get());
    }

    static class T1Task implements Callable<String> {
        private FutureTask<String> teaLeaveFuture;

        public T1Task(FutureTask<String> tlf){
            this.teaLeaveFuture = tlf;
        }
        @Override
        public String call() throws Exception {
            System.out.println("T1:洗水壶...");
            TimeUnit.SECONDS.sleep(1);

            System.out.println("T1:烧开水...");
            TimeUnit.SECONDS.sleep(15);

            String tf = teaLeaveFuture.get();
            System.out.println("T1:拿到茶叶:"+tf);

            System.out.println("T1:泡茶...");
            return "上茶:" + tf;
        }
    }


    static class T2Task implements Callable<String> {
        @Override
        public String call() throws Exception {
            System.out.println("T2:洗茶壶...");
            TimeUnit.SECONDS.sleep(1);

            System.out.println("T2:洗茶杯...");
            TimeUnit.SECONDS.sleep(2);

            System.out.println("T2:拿茶叶...");
            TimeUnit.SECONDS.sleep(1);

            return "龙井";
        }
    }
}
