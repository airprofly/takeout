package com.airprofly.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单提交 DTO
 * 用于用户提交订单时的数据传输
 */
@Data
public class OrdersSubmitDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 100000001337773802L;
    /**
     * 地址簿ID
     */
    private Long addressBookId;

    /**
     * 付款方式 1微信 2支付宝
     */
    private Integer payMethod;

    /**
     * 备注
     */
    private String remark;

    /**
     * 预计送达时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime estimatedDeliveryTime;

    /**
     * 配送状态 1立即送出 0选择具体时间
     */
    private Integer deliveryStatus;

    /**
     * 餐具数量
     */
    private Integer tablewareNumber;

    /**
     * 餐具数量状态 1按餐量提供 0选择具体数量
     */
    private Integer tablewareStatus;

    /**
     * 打包费
     */
    private Integer packAmount;

    /**
     * 总金额
     */
    private BigDecimal amount;

}
