package org.sample.chapter11.mrp.repo;

import org.sample.chapter11.mrp.utils.MrpUtils;
import org.sample.chapter11.mrp.domain.BOM;

import java.util.Arrays;
import java.util.List;

/**
 * 模拟数据库操作
 */
public class BOMRepo {
    private List<BOM> boms = Arrays.asList(CarBOMBuilder.build(), TractorBOMBuilder.build());

    public BOM getById(long id) {
        MrpUtils.sleep(50);
        return boms.get((int) id);
    }
}
