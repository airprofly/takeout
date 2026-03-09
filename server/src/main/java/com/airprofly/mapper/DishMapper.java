package com.airprofly.mapper;

import com.airprofly.annotation.AutoFill;
import com.airprofly.entity.Dish;
import com.airprofly.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 菜品 Mapper 接口
 */
@Mapper
public interface DishMapper {

    /**
     * 根据分类ID统计菜品数量
     * @param categoryId 分类ID
     * @return 菜品数量
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    /**
     * 新增菜品
     * @param dish 菜品对象
     */
    @AutoFill(OperationType.INSERT)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Dish dish);

    /**
     * 分页查询菜品
     * @param name 菜品名称(模糊查询)
     * @param categoryId 分类ID
     * @param status 状态
     * @return 菜品列表
     */
    List<Dish> pageQuery(String name, Long categoryId, Integer status);

    /**
     * 根据ID查询菜品
     * @param id 菜品ID
     * @return 菜品对象
     */
    @Select("select * from dish where id = #{id}")
    Dish getById(Long id);

    /**
     * 根据ID删除菜品
     * @param id 菜品ID
     */
    @Delete("delete from dish where id = #{id}")
    void deleteById(Long id);

    /**
     * 批量删除菜品
     * @param ids 菜品ID列表
     */
    void deleteByIds(List<Long> ids);

    /**
     * 更新菜品
     * @param dish 菜品对象
     */
    @AutoFill(OperationType.UPDATE)
    void update(Dish dish);

    /**
     * 条件查询菜品
     * @param dish 菜品对象
     * @return 菜品列表
     */
    List<Dish> list(Dish dish);

    /**
     * 根据套餐ID查询菜品
     * @param setmealId 套餐ID
     * @return 菜品列表
     */
    @Select("select d.* from dish d " +
            "left join setmeal_dish sd on d.id = sd.dish_id " +
            "where sd.setmeal_id = #{setmealId}")
    List<Dish> getBySetmealId(Long setmealId);

    /**
     * 根据条件统计菜品数量
     * @param map 查询条件
     * @return 菜品数量
     */
    Integer countByMap(Map<String, Integer> map);
}
