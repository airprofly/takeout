package com.airprofly.service;

import com.airprofly.dto.ShoppingCartDTO;
import com.airprofly.entity.ShoppingCart;

import java.util.List;

/**
 * 购物车业务接口
 */
public interface ShoppingCartService {

    /**
     * 添加购物车
     *
     * @param shoppingCartDTO 购物车DTO
     */
    void add(ShoppingCartDTO shoppingCartDTO);

    /**
     * 查看购物车
     *
     * @return 购物车列表
     */
    List<ShoppingCart> list();

    /**
     * 清空购物车
     */
    void clean();

    /**
     * 删除购物车商品
     *
     * @param id 购物车商品ID
     */
    void sub(Long id);

    /**
     * 从购物车中减去商品数量
     *
     * @param shoppingCartDTO 购物车DTO
     */
    void subShoppingCart(ShoppingCartDTO shoppingCartDTO);
}
