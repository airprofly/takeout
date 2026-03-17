package com.airprofly.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 分类分页查询 DTO
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class CategoryPageQueryDTO extends PageQueryDTO {

    private static final long serialVersionUID = 100000004256789013L;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 分类类型 1菜品分类 2套餐分类
     */
    private Integer type;

}
