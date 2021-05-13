package org.sample.chapter11.mrp.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Order {
    /**
     * BOM Id
     */
    private long bomId;
    /**
     * 下单数量
     */
    private long qty;

    public Order(long bomId, long qty) {
        this.bomId = bomId;
        this.qty = qty;
    }

    /**
     * 仅并发版本使用
     * BOM实例对象，并发优化
     */
    transient private BOM bom;
}
