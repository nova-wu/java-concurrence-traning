package org.sample.chapter11.mrp.repo;

import org.sample.chapter11.mrp.utils.MrpUtils;
import org.sample.chapter11.mrp.domain.Order;

import java.util.ArrayList;
import java.util.List;

/**
 * 模拟数据库操作
 */
public class OrderRepo {
    private static final int CNT_ORDER = 50;
    private List<Order> orders = new ArrayList<>();

    /**
     * 模拟数据库，创建50条订单记录
     */
    public OrderRepo() {
        for (int idx=0; idx<CNT_ORDER; idx++) {
            int bomId = idx % 2;
            Order order = new Order(bomId, idx+1);
            orders.add(order);
        }
    }

    /**
     * 模拟获取订单总数
     * @return 订单总数
     */
    public int getCounter() {
        return CNT_ORDER;
    }

    /**
     * 模拟数据库查询
     * @param id 订单Id
     * @return 匹配的订单
     */
    public Order findById(int id) {
        MrpUtils.sleep(50);
        return orders.get(id);
    }
}
