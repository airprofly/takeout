package com.airprofly.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 分类实体类
 * 类型: 1菜品分类 2套餐分类
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    // 分类类型 1:菜品分类 2:套餐分类
    private Integer type;

    // 分类名称
    private String name;

    // 排序
    private Integer sort;

    // 状态 0:禁用 1:启用
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
