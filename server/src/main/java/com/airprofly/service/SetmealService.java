package com.airprofly.service;

import com.airprofly.dto.SetmealDTO;
import com.airprofly.dto.SetmealPageQueryDTO;
import com.airprofly.entity.Setmeal;
import com.airprofly.result.PageResult;
import com.airprofly.vo.DishItemVO;
import com.airprofly.vo.SetmealVO;

import java.util.List;

/**
 * 套餐业务接口
 */
public interface SetmealService {

    /**
     * 新增套餐
     *
     * @param setmealDTO 套餐DTO
     */
    void saveWithDish(SetmealDTO setmealDTO);

    /**
     * 分页查询套餐
     *
     * @param setmealPageQueryDTO 分页查询条件
     * @return 分页结果
     */
    PageResult<SetmealVO> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 根据ID删除套餐
     *
     * @param id 套餐ID
     */
    void deleteById(Long id);

    /**
     * 根据ID查询套餐
     *
     * @param id 套餐ID
     * @return 套餐VO
     */
    SetmealVO getByIdWithDish(Long id);

    /**
     * 更新套餐
     *
     * @param setmealDTO 套餐DTO
     */
    void updateWithDish(SetmealDTO setmealDTO);

    /**
     * 启用或禁用套餐
     *
     * @param status 状态
     * @param id     套餐ID
     */
    void startOrStop(Integer status, Long id);

    /**
     * 条件查询套餐
     *
     * @param setmeal 查询条件
     * @return 套餐列表
     */
    List<Setmeal> list(Setmeal setmeal);
}
