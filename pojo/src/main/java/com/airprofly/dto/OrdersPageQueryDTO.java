package com.airprofly.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 订单分页查询 DTO
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class OrdersPageQueryDTO extends PageQueryDTO {

    private static final long serialVersionUID = 100000004556789016L;

    /**
     * 订单号
     */
    private String number;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 订单状态 1待付款 2待派送 3已派送 4已完成 5已取消
     */
    private Integer status;

    /**
     * 查询开始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime beginTime;

    /**
     * 查询结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 用户ID
     */
    private Long userId;

}
