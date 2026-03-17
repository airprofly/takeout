package com.airprofly.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 分类 DTO
 * 用于分类的新增和修改
 */
@Data
public class CategoryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 100000003129892160L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 类型 1菜品分类 2套餐分类
     */
    private Integer type;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 排序
     */
    private Integer sort;

}
