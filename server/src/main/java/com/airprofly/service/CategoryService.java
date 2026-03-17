package com.airprofly.service;

import com.airprofly.dto.CategoryDTO;
import com.airprofly.dto.CategoryPageQueryDTO;
import com.airprofly.entity.Category;
import com.airprofly.result.PageResult;

import java.util.List;

/**
 * 分类业务接口
 */
public interface CategoryService {

    /**
     * 新增分类
     *
     * @param categoryDTO 分类DTO
     */
    void save(CategoryDTO categoryDTO);

    /**
     * 分页查询分类
     *
     * @param categoryPageQueryDTO 分页查询条件
     * @return 分页结果
     */
    PageResult<Category> pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);

    /**
     * 根据ID删除分类
     *
     * @param id 分类ID
     */
    void deleteById(Long id);

    /**
     * 修改分类
     *
     * @param categoryDTO 分类DTO
     */
    void update(CategoryDTO categoryDTO);

    /**
     * 启用或禁用分类
     *
     * @param status 状态
     * @param id     分类ID
     */
    void startOrStop(Integer status, Long id);

    /**
     * 根据类型查询分类
     *
     * @param type 分类类型
     * @return 分类列表
     */
    List<Category> list(Integer type);
}
