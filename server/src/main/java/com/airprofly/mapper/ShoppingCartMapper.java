package com.airprofly.mapper;

import com.airprofly.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import java.util.List;

/**
 * 购物车 Mapper 接口
 */
@Mapper
public interface ShoppingCartMapper {

    /**
     * 条件查询购物车
     * @param shoppingCart 购物车对象
     * @return 购物车列表
     */
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    /**
     * 更新购物车商品数量
     * @param shoppingCart 购物车对象
     */
    void updateNumberById(ShoppingCart shoppingCart);

    /**
     * 新增购物车商品
     * @param shoppingCart 购物车对象
     */
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ShoppingCart shoppingCart);

    /**
     * 根据用户ID清空购物车
     * @param userId 用户ID
     */
    @Delete("delete from shopping_cart where user_id = #{userId}")
    void deleteByUserId(Long userId);

    /**
     * 根据ID删除购物车商品
     * @param id 购物车商品ID
     */
    @Delete("delete from shopping_cart where id = #{id}")
    void deleteById(Long id);

    /**
     * 批量插入购物车商品
     * @param shoppingCartList 购物车商品列表
     */
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertBatch(List<ShoppingCart> shoppingCartList);
}
