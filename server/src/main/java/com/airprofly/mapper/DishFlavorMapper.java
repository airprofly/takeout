package com.airprofly.mapper;

import com.airprofly.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 菜品口味 Mapper 接口
 */
@Mapper
public interface DishFlavorMapper {

    /**
     * 批量新增菜品口味
     * @param flavors 口味列表
     */
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertBatch(List<DishFlavor> flavors);

    /**
     * 根据菜品ID删除口味
     * @param dishId 菜品ID
     */
    void deleteByDishId(Long dishId);

    /**
     * 根据菜品ID批量删除口味
     * @param ids 菜品ID列表
     */
    void deleteByDishIds(List<Long> ids);

    /**
     * 根据菜品ID查询口味
     * @param dishId 菜品ID
     * @return 口味列表
     */
    @Select("select * from dish_flavor where dish_id = #{dishId}")
    List<DishFlavor> getByDishId(Long dishId);
}
