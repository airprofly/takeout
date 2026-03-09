package com.airprofly.mapper;

import com.airprofly.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 套餐菜品关系 Mapper 接口
 */
@Mapper
public interface SetmealDishMapper {

    /**
     * 根据菜品ID查询套餐ID
     * @param dishIds 菜品ID列表
     * @return 套餐ID列表
     */
    List<Long> getSetmealIdByDishIds(@Param("dishIds") List<Long> dishIds);

    /**
     * 批量插入套餐菜品关系
     * @param setmealDishes 套餐菜品关系列表
     */
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertBatch(List<SetmealDish> setmealDishes);

    /**
     * 根据套餐ID删除关联
     * @param setmealId 套餐ID
     */
    void deleteBySetmealId(Long setmealId);

    /**
     * 根据套餐ID查询菜品
     * @param setmealId 套餐ID
     * @return 菜品列表
     */
    List<SetmealDish> getDishBySetmealId(Long setmealId);
}
