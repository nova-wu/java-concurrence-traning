package org.sample.chapter11.mrp.repo;

import org.sample.chapter11.mrp.domain.BOM;

import java.math.BigDecimal;

public class CarBOMBuilder {
    public static BOM build() {
        BOM car = new BOM(1L, "奔驰", new BigDecimal(1), 0);
        BOM wheels = new BOM(2L, "奔驰轮子", new BigDecimal(4), 1);
        BOM chair = new BOM(3L, "奔驰椅子", new BigDecimal(2), 1);
        BOM glass = new BOM(4L, "福耀玻璃", new BigDecimal(5), 1);

        car.addChild(wheels);
        car.addChild(chair);
        car.addChild(glass);

        return car;
    }
}
