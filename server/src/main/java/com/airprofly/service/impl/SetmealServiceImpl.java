package com.airprofly.service.impl;

import com.airprofly.dto.SetmealDTO;
import com.airprofly.dto.SetmealPageQueryDTO;
import com.airprofly.entity.Setmeal;
import com.airprofly.entity.SetmealDish;
import com.airprofly.mapper.SetmealDishMapper;
import com.airprofly.mapper.SetmealMapper;
import com.airprofly.result.PageResult;
import com.airprofly.service.SetmealService;
import com.airprofly.util.PageHelperUtil;
import com.airprofly.vo.SetmealVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 套餐业务实现
 */
@Service
@RequiredArgsConstructor
public class SetmealServiceImpl implements SetmealService {

    private final SetmealMapper setmealMapper;
    private final SetmealDishMapper setmealDishMapper;
    /**
     * 新增套餐
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDTO setmealDTO) {
        // 保存套餐
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.insert(setmeal);

        // 获取套餐ID
        Long setmealId = setmeal.getId();

        // 保存套餐菜品关系
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && !setmealDishes.isEmpty()) {
            setmealDishes.forEach(setmealDish -> setmealDish.setSetmealId(setmealId));
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }

    /**
     * 分页查询套餐
     */
    @Override
    public PageResult<SetmealVO> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelperUtil.startPage(setmealPageQueryDTO);
        List<Setmeal> list = setmealMapper.pageQuery(
                setmealPageQueryDTO.getName(),
                setmealPageQueryDTO.getCategoryId(),
                setmealPageQueryDTO.getStatus()
        );

        List<SetmealVO> setmealVOList = new ArrayList<>();
        for (Setmeal setmeal : list) {
            SetmealVO setmealVO = new SetmealVO();
            BeanUtils.copyProperties(setmeal, setmealVO);
            setmealVOList.add(setmealVO);
        }

        return PageHelperUtil.of(list, setmealVOList);
    }

    /**
     * 根据ID删除套餐
     */
    @Override
    @Transactional
    public void deleteById(Long id) {
        // 查询套餐状态
        Setmeal setmeal = setmealMapper.getById(id);
        if (setmeal != null && setmeal.getStatus() == 1) {
            throw new RuntimeException("套餐起售中，不能删除");
        }

        // 删除套餐
        setmealMapper.deleteById(id);

        // 删除套餐菜品关系
        setmealDishMapper.deleteBySetmealId(id);
    }

    /**
     * 根据ID查询套餐
     */
    @Override
    public SetmealVO getByIdWithDish(Long id) {
        // 查询套餐
        Setmeal setmeal = setmealMapper.getById(id);

        // 查询套餐菜品关系
        List<SetmealDish> setmealDishes = setmealDishMapper.getDishBySetmealId(id);

        // 封装数据
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);
        setmealVO.setSetmealDishes(setmealDishes);

        return setmealVO;
    }

    /**
     * 更新套餐
     */
    @Override
    @Transactional
    public void updateWithDish(SetmealDTO setmealDTO) {
        // 修改套餐基本信息
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.update(setmeal);

        // 删除原套餐菜品关系
        setmealDishMapper.deleteBySetmealId(setmealDTO.getId());

        // 保存新套餐菜品关系
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && !setmealDishes.isEmpty()) {
            setmealDishes.forEach(setmealDish -> setmealDish.setSetmealId(setmealDTO.getId()));
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }

    /**
     * 启用或禁用套餐
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();
        setmealMapper.update(setmeal);
    }

    /**
     * 条件查询套餐
     */
    @Override
    public List<Setmeal> list(Setmeal setmeal) {
        return setmealMapper.list(setmeal);
    }
}
