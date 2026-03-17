package com.airprofly.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付状态枚举
 * <p>
 * 定义订单支付的各种状态及其对应的业务含义
 *
 * @author airprofly
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum PayStatus {

    /**
     * 未支付
     */
    UN_PAID(0, "未支付"),

    /**
     * 已支付
     */
    PAID(1, "已支付"),

    /**
     * 退款
     */
    REFUND(2, "退款");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 状态描述
     */
    private final String description;

    /**
     * 根据状态码获取枚举
     *
     * @param code 状态码
     * @return 支付状态枚举
     */
    public static PayStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PayStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的支付状态码: " + code);
    }

    /**
     * 判断是否已支付
     *
     * @return true if paid
     */
    public boolean isPaid() {
        return this == PAID;
    }

    /**
     * 判断是否可以退款
     *
     * @return true if refundable
     */
    public boolean isRefundable() {
        return this == PAID;
    }
}
