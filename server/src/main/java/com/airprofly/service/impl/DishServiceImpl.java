package com.airprofly.service.impl;

import com.airprofly.dto.DishDTO;
import com.airprofly.dto.DishPageQueryDTO;
import com.airprofly.entity.Dish;
import com.airprofly.entity.DishFlavor;
import com.airprofly.entity.Setmeal;
import com.airprofly.mapper.DishFlavorMapper;
import com.airprofly.mapper.DishMapper;
import com.airprofly.mapper.SetmealDishMapper;
import com.airprofly.mapper.SetmealMapper;
import com.airprofly.result.PageResult;
import com.airprofly.service.DishService;
import com.airprofly.util.PageHelperUtil;
import com.airprofly.vo.DishVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜品业务实现
 */
@Service
@RequiredArgsConstructor
public class DishServiceImpl implements DishService {

    private final DishMapper dishMapper;
    private final DishFlavorMapper dishFlavorMapper;
    private final SetmealDishMapper setmealDishMapper;
    private final SetmealMapper setmealMapper;

    /**
     * 新增菜品以及保存口味
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        // 保存菜品
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.insert(dish);

        // 获取菜品ID
        Long dishId = dish.getId();

        // 保存口味
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            flavors.forEach(flavor -> flavor.setDishId(dishId));
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 分页查询菜品
     */
    @Override
    public PageResult<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelperUtil.startPage(dishPageQueryDTO);
        List<Dish> list = dishMapper.pageQuery(
                dishPageQueryDTO.getName(),
                dishPageQueryDTO.getCategoryId(),
                dishPageQueryDTO.getStatus()
        );

        List<DishVO> dishVOList = new ArrayList<>();
        for (Dish dish : list) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(dish, dishVO);
            dishVOList.add(dishVO);
        }

        return PageHelperUtil.of(list, dishVOList);
    }

    /**
     * 删除菜品
     */
    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        // 判断菜品是否能够删除 - 是否启用
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if (dish != null && dish.getStatus() == 1) {
                throw new RuntimeException("菜品起售中，不能删除");
            }
        }

        // 是否被套餐引用
        List<Long> setmealIds = setmealDishMapper.getSetmealIdByDishIds(ids);
        if (setmealIds != null && !setmealIds.isEmpty()) {
            throw new RuntimeException("菜品被套餐引用，不能删除");
        }

        // 批量删除菜品数据
        dishMapper.deleteByIds(ids);

        // 批量删除菜品关联的口味数据
        dishFlavorMapper.deleteByDishIds(ids);
    }

    /**
     * 根据ID查询菜品
     */
    @Override
    public DishVO getByIdWithFlavor(Long id) {
        // 根据ID查询菜品
        Dish dish = dishMapper.getById(id);

        // 根据ID查询菜品口味
        List<DishFlavor> flavors = dishFlavorMapper.getByDishId(id);

        // 封装数据
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(flavors);

        return dishVO;
    }

    /**
     * 更新菜品以及口味
     */
    @Override
    @Transactional
    public void updateWithFlavor(DishDTO dishDTO) {
        // 修改菜品的基本信息
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);

        // 删除菜品的口味
        dishFlavorMapper.deleteByDishId(dishDTO.getId());

        // 保存菜品的口味
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            flavors.forEach(flavor -> flavor.setDishId(dishDTO.getId()));
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 启用或禁用菜品
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        Dish dish = Dish.builder()
                .id(id)
                .status(status)
                .build();
        dishMapper.update(dish);

        // 如果是禁用，需要将套餐中的菜品也禁用
        if (status == 0) {
            List<Long> dishIds = new ArrayList<>();
            dishIds.add(id);
            List<Long> setmealIds = setmealDishMapper.getSetmealIdByDishIds(dishIds);
            if (setmealIds != null && !setmealIds.isEmpty()) {
                for (Long setmealId : setmealIds) {
                    Setmeal setmeal = Setmeal.builder()
                            .id(setmealId)
                            .status(0)
                            .build();
                    setmealMapper.update(setmeal);
                }
            }
        }
    }

    /**
     * 根据菜品分类ID查询菜品
     */
    @Override
    public List<Dish> list(Long categoryId) {
        Dish dish = Dish.builder()
                .categoryId(categoryId)
                .status(1)
                .build();
        return dishMapper.list(dish);
    }

    /**
     * 条件查询菜品和口味
     */
    @Override
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d, dishVO);

            // 根据菜品ID查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());
            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }
}
