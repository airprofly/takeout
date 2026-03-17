package com.airprofly.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 菜品实体类
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class Dish implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    // 菜品名称
    private String name;

    // 菜品分类ID
    private Long categoryId;

    // 菜品价格
    private BigDecimal price;

    // 图片
    private String image;

    // 描述信息
    private String description;

    // 状态 0:停售 1:起售
    private Integer status;

    // 创建时间
    private LocalDateTime createTime;

    // 更新时间
    private LocalDateTime updateTime;

    // 创建人ID
    private Long createUser;

    // 更新人ID
    private Long updateUser;
}
