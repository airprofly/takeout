package com.airprofly.vo;

import lombok.Data;
import java.io.Serial;
import java.io.Serializable;

@Data
public class OrderStatisticsVO implements Serializable {
    //待接单数量
    @Serial
    private static final long serialVersionUID = 100000000552605762L;
    private Integer toBeConfirmed;

    //待派送数量
    private Integer confirmed;

    //派送中数量
    private Integer deliveryInProgress;
}
