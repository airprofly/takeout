package com.airprofly.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单明细实体类
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetail implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    // 名称
    private String name;

    // 订单ID
    private Long orderId;

    // 菜品ID
    private Long dishId;

    // 套餐ID
    private Long setmealId;

    // 口味
    private String dishFlavor;

    // 数量
    private Integer number;

    // 金额
    private BigDecimal amount;

    // 图片
    private String image;
}
