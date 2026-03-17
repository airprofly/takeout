package com.airprofly.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜品分页查询 DTO
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DishPageQueryDTO extends PageQueryDTO {

    private static final long serialVersionUID = 100000004356789014L;

    /**
     * 菜品名称
     */
    private String name;

    /**
     * 分类ID
     */
    private Integer categoryId;

    /**
     * 状态 0表示禁用 1表示启用
     */
    private Integer status;

}
