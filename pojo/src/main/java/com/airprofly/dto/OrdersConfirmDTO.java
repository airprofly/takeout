package com.airprofly.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class OrdersConfirmDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 100000003156260569L;
    private Long id;
    //订单状态 1待付款 2待接单 3 已接单 4 派送中 5 已完成 6 已取消 7 退款
    private Integer status;

}
