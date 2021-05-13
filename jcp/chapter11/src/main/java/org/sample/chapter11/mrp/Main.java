package org.sample.chapter11.mrp;

import org.sample.chapter11.mrp.service.MrpService;
import org.sample.chapter11.mrp.service.OneThreadService;
import org.sample.chapter11.mrp.service.PipeLineService;
import org.sample.chapter11.mrp.utils.MrpUtils;

import java.math.BigDecimal;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        // 单线程执行
        MrpService service1 = new OneThreadService();
        long startAt = System.currentTimeMillis();
        Map<String, BigDecimal> result = service1.calc();
        System.out.println("service1 used:" + (System.currentTimeMillis()-startAt));
        MrpUtils.print(result);
        // 多线程执行
        System.out.println("");
        MrpService service2 = new PipeLineService();
        startAt = System.currentTimeMillis();
        Map<String, BigDecimal> result2 = service2.calc();
        System.out.println("service2 used:" + (System.currentTimeMillis()-startAt));
        MrpUtils.print(result2);

        ((PipeLineService)service2).close();
    }
}
