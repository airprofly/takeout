package com.airprofly.service.impl;

import com.airprofly.dto.CategoryDTO;
import com.airprofly.dto.CategoryPageQueryDTO;
import com.airprofly.entity.Category;
import com.airprofly.mapper.CategoryMapper;
import com.airprofly.mapper.DishMapper;
import com.airprofly.mapper.SetmealMapper;
import com.airprofly.result.PageResult;
import com.airprofly.service.CategoryService;
import com.airprofly.util.PageHelperUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分类业务实现
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final DishMapper dishMapper;
    private final SetmealMapper setmealMapper;

    /**
     * 新增分类
     */
    @Override
    public void save(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);

        // 分类状态默认为禁用状态0
        category.setStatus(0);

        categoryMapper.insert(category);
    }

    /**
     * 分页查询分类
     */
    @Override
    public PageResult<Category> pageQuery(CategoryPageQueryDTO categoryPageQueryDTO) {
        // 使用 PageHelper 进行分页
        PageHelperUtil.startPage(categoryPageQueryDTO);
        List<Category> list = categoryMapper.pageQuery(
                categoryPageQueryDTO.getName(),
                categoryPageQueryDTO.getType()
        );
        // 构建分页结果
        return PageHelperUtil.of(list);
    }

    /**
     * 根据ID删除分类
     */
    @Override
    public void deleteById(Long id) {
        // 查询当前分类是否关联了菜品
        Integer count = dishMapper.countByCategoryId(id);
        if (count > 0) {
            throw new RuntimeException("当前分类下有菜品，不能删除");
        }

        // 查询当前分类是否关联了套餐
        count = setmealMapper.countByCategoryId(id);
        if (count > 0) {
            throw new RuntimeException("当前分类下有套餐，不能删除");
        }

        // 删除分类数据
        categoryMapper.deleteById(id);
    }

    /**
     * 修改分类
     */
    @Override
    public void update(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
        categoryMapper.update(category);
    }

    /**
     * 启用或禁用分类
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        Category category = Category.builder()
                .id(id)
                .status(status)
                .build();
        categoryMapper.update(category);
    }

    /**
     * 根据类型查询分类
     */
    @Override
    public List<Category> list(Integer type) {
        return categoryMapper.list(type);
    }
}
