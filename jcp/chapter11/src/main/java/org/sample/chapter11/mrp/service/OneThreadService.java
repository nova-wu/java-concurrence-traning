package org.sample.chapter11.mrp.service;

import org.sample.chapter11.mrp.domain.BOM;
import org.sample.chapter11.mrp.domain.Order;
import org.sample.chapter11.mrp.repo.BOMRepo;
import org.sample.chapter11.mrp.repo.OrderRepo;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 单线程计算mrp
 */
public class OneThreadService implements MrpService {
    private OrderRepo orderRepo = new OrderRepo();
    private BOMRepo bomRepo = new BOMRepo();

    @Override
    public Map<String, BigDecimal> calc() {
        Map<String, BigDecimal> result = new HashMap<>();
        int count = orderRepo.getCounter();
        for (int idx=0; idx<count; idx++) {
            // 计算每一条订单的物料需求
            Order order = orderRepo.findById(idx);
            long bomId = order.getBomId();
            BOM bom = bomRepo.getById(bomId);
            BigDecimal curQty = BigDecimal.valueOf(order.getQty()).multiply(bom.getQty());
            calc(result, curQty, bom);
        }

        return result;
    }

    /**
     * 计算用量
     * @param result 中间结果
     * @param reqQty 需求数量
     * @param bom 物料清单
     */
    private void calc(Map<String, BigDecimal> result, BigDecimal reqQty, BOM bom) {
        // 计算母件需求数量
        BigDecimal curQty = reqQty.multiply(bom.getQty());
        BigDecimal totalQty = result.get(bom.getComponent());
        if (totalQty == null) {
            totalQty = curQty;
        } else {
            totalQty = totalQty.add(curQty);
        }
        result.put(bom.getComponent(), totalQty);
        // 计算子件需求数量
        for (BOM child : bom.getChildren()) {
            calc(result, reqQty, child);
        }
    }
}
