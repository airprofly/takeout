package com.airprofly.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 套餐分页查询 DTO
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SetmealPageQueryDTO extends PageQueryDTO {

    @Serial
    private static final long serialVersionUID = 100000004456789015L;

    /**
     * 套餐名称
     */
    private String name;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 状态 0表示禁用 1表示启用
     */
    private Integer status;

}
