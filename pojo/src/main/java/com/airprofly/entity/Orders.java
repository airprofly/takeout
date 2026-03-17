package com.airprofly.entity;

import com.airprofly.enumeration.OrderStatus;
import com.airprofly.enumeration.PayStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class Orders implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    // 订单号
    private String number;

    // 订单状态 1:待付款 2:待接单 3:已接单 4:派送中 5:已完成 6:已取消
    private Integer status;

    // 下单用户ID
    private Long userId;

    // 地址簿ID
    private Long addressBookId;

    // 下单时间
    private LocalDateTime orderTime;

    // 结账时间
    private LocalDateTime checkoutTime;

    // 支付方式 1:微信 2:支付宝
    private Integer payMethod;

    // 支付状态 0:未支付 1:已支付 2:退款
    private Integer payStatus;

    // 实收金额
    private BigDecimal amount;

    // 备注
    private String remark;

    // 用户名
    private String userName;

    // 手机号
    private String phone;

    // 地址
    private String address;

    // 收货人
    private String consignee;

    // 订单取消原因
    private String cancelReason;

    // 订单拒绝原因
    private String rejectionReason;

    // 订单取消时间
    private LocalDateTime cancelTime;

    // 预计送达时间
    private LocalDateTime estimatedDeliveryTime;

    // 配送状态 1:立即送出 0:选择具体时间
    private Integer deliveryStatus;

    // 送达时间
    private LocalDateTime deliveryTime;

    // 打包费
    private int packAmount;

    // 餐具数量
    private int tablewareNumber;

    // 餐具数量状态 1:按餐量提供 0:选择具体数量
    private Integer tablewareStatus;

    /**
     * 获取订单状态枚举
     *
     * @return 订单状态枚举，如果 status 为 null 则返回 null
     */
    public OrderStatus getStatusEnum() {
        return status != null ? OrderStatus.fromCode(status) : null;
    }

    /**
     * 设置订单状态（通过枚举）
     *
     * @param orderStatus 订单状态枚举
     * @return 当前对象
     */
    public Orders setStatusEnum(OrderStatus orderStatus) {
        this.status = orderStatus != null ? orderStatus.getCode() : null;
        return this;
    }

    /**
     * 获取支付状态枚举
     *
     * @return 支付状态枚举，如果 payStatus 为 null 则返回 null
     */
    public PayStatus getPayStatusEnum() {
        return payStatus != null ? PayStatus.fromCode(payStatus) : null;
    }

    /**
     * 设置支付状态（通过枚举）
     *
     * @param payStatus 支付状态枚举
     * @return 当前对象
     */
    public Orders setPayStatusEnum(PayStatus payStatus) {
        this.payStatus = payStatus != null ? payStatus.getCode() : null;
        return this;
    }
}
