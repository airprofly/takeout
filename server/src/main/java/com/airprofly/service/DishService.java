package com.airprofly.service;

import com.airprofly.dto.DishDTO;
import com.airprofly.dto.DishPageQueryDTO;
import com.airprofly.entity.Dish;
import com.airprofly.result.PageResult;
import com.airprofly.vo.DishVO;

import java.util.List;

/**
 * 菜品业务接口
 */
public interface DishService {

    /**
     * 新增菜品以及保存口味
     *
     * @param dishDTO 菜品DTO
     */
    void saveWithFlavor(DishDTO dishDTO);

    /**
     * 分页查询菜品
     *
     * @param dishPageQueryDTO 分页查询条件
     * @return 分页结果
     */
    PageResult<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 删除菜品
     *
     * @param ids 菜品ID列表
     */
    void deleteBatch(List<Long> ids);

    /**
     * 根据ID查询菜品
     *
     * @param id 菜品ID
     * @return 菜品VO
     */
    DishVO getByIdWithFlavor(Long id);

    /**
     * 更新菜品以及口味
     *
     * @param dishDTO 菜品DTO
     */
    void updateWithFlavor(DishDTO dishDTO);

    /**
     * 启用或禁用菜品
     *
     * @param status 状态
     * @param id     菜品ID
     */
    void startOrStop(Integer status, Long id);

    /**
     * 根据分类ID查询菜品
     *
     * @param categoryId 分类ID
     * @return 菜品列表
     */
    List<Dish> list(Long categoryId);

    /**
     * 条件查询菜品和口味
     *
     * @param dish 查询条件
     * @return 菜品VO列表
     */
    List<DishVO> listWithFlavor(Dish dish);
}
