package com.airprofly.dto;

import com.airprofly.entity.DishFlavor;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 菜品 DTO
 * 用于菜品的新增和修改
 */
@Data
public class DishDTO implements Serializable {

    private static final long serialVersionUID = 100000003174526667L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 菜品名称
     */
    private String name;

    /**
     * 菜品分类ID
     */
    private Long categoryId;

    /**
     * 菜品价格
     */
    private BigDecimal price;

    /**
     * 图片
     */
    private String image;

    /**
     * 描述信息
     */
    private String description;

    /**
     * 状态 0停售 1起售
     */
    private Integer status;

    /**
     * 菜品口味
     */
    private List<DishFlavor> flavors = new ArrayList<>();

}
