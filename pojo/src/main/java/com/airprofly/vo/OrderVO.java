package com.airprofly.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.List;

import com.airprofly.entity.OrderDetail;
import com.airprofly.entity.Orders;

/**
 * 订单 VO
 * 用于订单信息展示,包含订单基本信息和详情
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false) // 不使用父类属性
public class OrderVO extends Orders {

    @Serial
    private static final long serialVersionUID = 100000004656789017L;

    /**
     * 订单菜品信息
     */
    private String orderDishes;

    /**
     * 订单详情列表
     */
    private List<OrderDetail> orderDetailList;

}
