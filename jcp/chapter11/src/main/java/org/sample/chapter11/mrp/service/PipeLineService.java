package org.sample.chapter11.mrp.service;

import org.sample.chapter11.mrp.domain.BOM;
import org.sample.chapter11.mrp.domain.Order;
import org.sample.chapter11.mrp.repo.BOMRepo;
import org.sample.chapter11.mrp.repo.OrderRepo;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 多线程基于pipeline计算mrp
 */
public class PipeLineService implements MrpService {
    private OrderRepo orderRepo = new OrderRepo();
    private BOMRepo bomRepo = new BOMRepo();

    private ExecutorService producer = Executors.newFixedThreadPool(10);
    private BlockingQueue<Order> pipeline = new LinkedBlockingQueue<>();

    public void close() {
        producer.shutdown();
    }

    @Override
    public Map<String, BigDecimal> calc() {
        /**  单线程代码：
         *         Map<String, BigDecimal> result = new HashMap<>();
         *         int count = orderRepo.getCounter();
         *         for (int idx=0; idx<count; idx++) {
         *             Order order = orderRepo.getById(idx);
         *             long bomId = order.getBomId();
         *             BOM bom = bomRepo.getById(bomId);
         *             BigDecimal curQty = BigDecimal.valueOf(order.getQty()).multiply(bom.getQty());
         *             calc(result, curQty, bom);
         *         }
         */
        // 第一阶段，作为生产者，查询数据库，完成数据内存化
        int count = orderRepo.getCounter();
        for (int idx=0; idx<count; idx++) {
            int curIdx = idx;
            producer.execute(() -> {
                try {
                    Order order = orderRepo.findById(curIdx);
                    long bomId = order.getBomId();
                    BOM bom = bomRepo.getById(bomId);
                    order.setBom(bom);
                    pipeline.put(order);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        /**
         * 计算结果，支持并发访问，由于内存计算速度足够快，所以只使用了1个线程
         */
        Map<String, BigDecimal> result = new HashMap<>();
        for (int idx=0; idx<count; idx++) {
            try {
                Order order = pipeline.take();
                BOM bom = order.getBom();
                BigDecimal curQty = BigDecimal.valueOf(order.getQty()).multiply(bom.getQty());
                calc(result, curQty, bom);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    /**
     * 计算用量 （同单线程算法）
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
