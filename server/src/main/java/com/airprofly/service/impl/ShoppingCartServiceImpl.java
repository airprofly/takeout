package com.airprofly.service.impl;

import com.airprofly.dto.ShoppingCartDTO;
import com.airprofly.entity.ShoppingCart;
import com.airprofly.mapper.ShoppingCartMapper;
import com.airprofly.service.ShoppingCartService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 购物车业务实现
 */
@Service
@RequiredArgsConstructor
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private final ShoppingCartMapper shoppingCartMapper;
    /**
     * 添加购物车
     */
    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        // 判断当前加入购物车的商品是否已经存在
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);

        // TODO: 从当前上下文获取用户ID
        Long userId = 1L; // 临时写死
        shoppingCart.setUserId(userId);

        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        if (list != null && !list.isEmpty()) {
            // 如果已经存在，数量加1
            ShoppingCart cart = list.get(0);
            cart.setNumber(cart.getNumber() + 1);
            shoppingCartMapper.updateNumberById(cart);
        } else {
            // 如果不存在，新增
            shoppingCart.setNumber(1);
            shoppingCartMapper.insert(shoppingCart);
        }
    }

    /**
     * 查看购物车
     */
    @Override
    public List<ShoppingCart> list() {
        // TODO: 从当前上下文获取用户ID
        Long userId = 1L; // 临时写死

        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(userId)
                .build();

        return shoppingCartMapper.list(shoppingCart);
    }

    /**
     * 清空购物车
     */
    @Override
    public void clean() {
        // TODO: 从当前上下文获取用户ID
        Long userId = 1L; // 临时写死
        shoppingCartMapper.deleteByUserId(userId);
    }

    /**
     * 删除购物车商品
     */
    @Override
    public void sub(Long id) {
        shoppingCartMapper.deleteById(id);
    }

    /**
     * 从购物车中减去商品数量
     */
    @Override
    public void subShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);

        // TODO: 从当前上下文获取用户ID
        Long userId = 1L; // 临时写死
        shoppingCart.setUserId(userId);

        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        if (list != null && !list.isEmpty()) {
            ShoppingCart cart = list.get(0);
            if (cart.getNumber() > 1) {
                // 数量大于1，减1
                cart.setNumber(cart.getNumber() - 1);
                shoppingCartMapper.updateNumberById(cart);
            } else {
                // 数量等于1，直接删除
                shoppingCartMapper.deleteById(cart.getId());
            }
        }
    }
}
