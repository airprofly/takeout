package com.airprofly.mapper;

import com.airprofly.entity.Category;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import java.util.List;

/**
 * 分类 Mapper 接口
 */
@Mapper
public interface CategoryMapper {

    /**
     * 新增分类
     * @param category 分类对象
     */
    @Insert("insert into category (type, name, sort, status, create_time, update_time, create_user, update_user) " +
            "values (#{type}, #{name}, #{sort}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Category category);

    /**
     * 分页查询分类
     * @param name 分类名称(模糊查询)
     * @param type 分类类型
     * @return 分类列表
     */
    List<Category> pageQuery(String name, Integer type);

    /**
     * 根据ID删除分类
     * @param id 分类ID
     */
    @Delete("delete from category where id = #{id}")
    void deleteById(Long id);

    /**
     * 更新分类
     * @param category 分类对象
     */
    void update(Category category);

    /**
     * 根据类型查询分类
     * @param type 分类类型
     * @return 分类列表
     */
    List<Category> list(Integer type);
}
