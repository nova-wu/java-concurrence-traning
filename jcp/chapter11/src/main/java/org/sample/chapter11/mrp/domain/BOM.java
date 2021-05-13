package org.sample.chapter11.mrp.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class BOM {
    /**
     * BOM Id
     */
    private long id;
    /**
     * 零件
     */
    private String component;
    /**
     * 用量
     */
    private BigDecimal qty;
    /**
     * 低阶码
     */
    private int lowLevelCode;

    /**
     * 所有子件
     */
    private List<BOM> children = new ArrayList<>();

    public BOM(long id, String component, BigDecimal qty, int lowLevelCode) {
        this.id = id;
        this.component = component;
        this.qty = qty;
        this.lowLevelCode = lowLevelCode;
    }

    public void addChild(BOM child) {
        children.add(child);
    }
}
