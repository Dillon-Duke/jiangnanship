package com.caidao.enums;

import com.baomidou.mybatisplus.core.enums.IEnum;

import java.io.Serializable;

/**
 * @author tom
 */

public enum CarTypeEnums implements IEnum {

    //平板车
    FPINGBANCHE( "平板车"),
    //龙门吊
    LONGMENDIAO("龙门吊"),
    //塔吊
    TADIAO( "塔吊"),
    //叉车
    CHACHE("叉车"),
    //汽车吊
    QICHEDIAO("汽车吊");

    private String label;

    CarTypeEnums(String label) {
        this.label = label;
    }

    @Override
    public Serializable getValue() {
        return this.label;
    }
}

