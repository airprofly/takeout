package com.airprofly.dto;

import lombok.Data;
import java.io.Serial;
import java.io.Serializable;

@Data
public class OrdersPaymentDTO implements Serializable {
    //订单号
    @Serial
    private static final long serialVersionUID = 100000000570941494L;
    private String orderNumber;

    //付款方式
    private Integer payMethod;

}
