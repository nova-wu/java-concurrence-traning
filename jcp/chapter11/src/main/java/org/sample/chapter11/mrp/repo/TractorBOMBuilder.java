package org.sample.chapter11.mrp.repo;

import org.sample.chapter11.mrp.domain.BOM;

import java.math.BigDecimal;

public class TractorBOMBuilder {
    public static BOM build() {
        BOM tractor = new BOM(5L, "东方红", new BigDecimal(1),0);
        BOM wheels = new BOM(6L,"东方红轮子", new BigDecimal(4),1);
        BOM chair = new BOM(7L,"东方红椅子", new BigDecimal(1), 1);
        BOM glass = new BOM(4L, "福耀玻璃", new BigDecimal(4), 1);

        tractor.addChild(wheels);
        tractor.addChild(chair);
        tractor.addChild(glass);
        return tractor;
    }
}
