package com.airprofly.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单状态枚举
 * <p>
 * 定义订单的各种状态及其对应的业务含义
 *
 * @author airprofly
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum OrderStatus {

    /**
     * 待付款
     */
    PENDING_PAYMENT(1, "待付款"),

    /**
     * 待接单
     */
    TO_BE_CONFIRMED(2, "待接单"),

    /**
     * 已接单
     */
    CONFIRMED(3, "已接单"),

    /**
     * 派送中
     */
    DELIVERY_IN_PROGRESS(4, "派送中"),

    /**
     * 已完成
     */
    COMPLETED(5, "已完成"),

    /**
     * 已取消
     */
    CANCELLED(6, "已取消");

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
     * @return 订单状态枚举
     */
    public static OrderStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (OrderStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的订单状态码: " + code);
    }

    /**
     * 判断是否为终态（已完成或已取消）
     *
     * @return true if terminal status
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == CANCELLED;
    }

    /**
     * 判断是否可以取消
     *
     * @return true if can be cancelled
     */
    public boolean isCancellable() {
        return this == PENDING_PAYMENT || this == TO_BE_CONFIRMED;
    }

    /**
     * 判断是否已支付
     *
     * @return true if paid
     */
    public boolean isPaid() {
        return this != PENDING_PAYMENT;
    }
}
