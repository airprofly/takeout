package com.airprofly.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 业务数据概览 VO
 * 用于显示营业额、订单数等统计信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessDataVO implements Serializable {

    private static final long serialVersionUID = 100000004072115063L;

    /**
     * 营业额
     */
    private Double turnover;

    /**
     * 有效订单数
     */
    private Integer validOrderCount;

    /**
     * 订单完成率
     */
    private Double orderCompletionRate;

    /**
     * 平均客单价
     */
    private Double unitPrice;

    /**
     * 新增用户数
     */
    private Integer newUsers;

}
