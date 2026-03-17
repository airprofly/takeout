package com.airprofly.dto;

import com.airprofly.entity.SetmealDish;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 套餐 DTO
 * 用于套餐的新增和修改
 */
@Data
public class SetmealDTO implements Serializable {

    private static final long serialVersionUID = 100000002397826253L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 套餐名称
     */
    private String name;

    /**
     * 套餐价格
     */
    private BigDecimal price;

    /**
     * 状态 0停用 1启用
     */
    private Integer status;

    /**
     * 描述信息
     */
    private String description;

    /**
     * 图片
     */
    private String image;

    /**
     * 套餐菜品关系
     */
    private List<SetmealDish> setmealDishes = new ArrayList<>();

}
