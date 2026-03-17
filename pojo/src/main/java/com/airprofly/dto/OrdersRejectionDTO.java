package com.airprofly.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class OrdersRejectionDTO implements Serializable {

    private static final long serialVersionUID = 100000001576517222L;
    private Long id;

    //订单拒绝原因
    private String rejectionReason;

}
