package org.sample.chapter11.mrp.utils;

import lombok.SneakyThrows;

import java.math.BigDecimal;
import java.util.Map;

public class MrpUtils {

    @SneakyThrows
    public static void sleep(long millis) {
        Thread.sleep(millis);
    }

    public static void print(Map<String, BigDecimal> map) {
        System.out.println("----------------------");
        for (String key : map.keySet()) {
            System.out.println(key + ":" + map.get(key));
        }
    }
}
