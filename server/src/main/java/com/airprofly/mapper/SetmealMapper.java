package com.airprofly.mapper;

import com.airprofly.entity.Setmeal;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 套餐 Mapper 接口
 */
@Mapper
public interface SetmealMapper {

    /**
     * 根据分类ID统计套餐数量
     * @param categoryId 分类ID
     * @return 套餐数量
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    /**
     * 新增套餐
     * @param setmeal 套餐对象
     */
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Setmeal setmeal);

    /**
     * 分页查询套餐
     * @param name 套餐名称(模糊查询)
     * @param categoryId 分类ID
     * @param status 状态
     * @return 套餐列表
     */
    List<Setmeal> pageQuery(String name, Long categoryId, Integer status);

    /**
     * 根据ID查询套餐
     * @param id 套餐ID
     * @return 套餐对象
     */
    @Select("select * from setmeal where id = #{id}")
    Setmeal getById(Long id);

    /**
     * 根据ID删除套餐
     * @param id 套餐ID
     */
    @Delete("delete from setmeal where id = #{id}")
    void deleteById(Long id);

    /**
     * 更新套餐
     * @param setmeal 套餐对象
     */
    void update(Setmeal setmeal);

    /**
     * 条件查询套餐
     * @param setmeal 套餐对象
     * @return 套餐列表
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据套餐ID查询菜品
     * @param setmealId 套餐ID
     * @return 菜品列表
     */
    List<Setmeal> getDishItemBySetmealId(Long setmealId);

    /**
     * 根据条件统计套餐数量
     * @param map 查询条件
     * @return 套餐数量
     */
    Integer countByMap(Map<String, Integer> map);
}
