package com.airprofly.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class OrdersCancelDTO implements Serializable {

    private static final long serialVersionUID = 100000003084237202L;
    private Long id;
    //订单取消原因
    private String cancelReason;

}
