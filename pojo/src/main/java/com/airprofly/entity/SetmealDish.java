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
 * 套餐菜品关系实体类
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class SetmealDish implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    // 套餐ID
    private Long setmealId;

    // 菜品ID
    private Long dishId;

    // 菜品名称 (冗余字段)
    private String name;

    // 菜品原价
    private BigDecimal price;

    // 份数
    private Integer copies;
}
