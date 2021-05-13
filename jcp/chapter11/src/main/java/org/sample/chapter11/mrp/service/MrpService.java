package org.sample.chapter11.mrp.service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * mrp计算服务接口
 */
public interface MrpService {
    Map<String, BigDecimal> calc();
}
