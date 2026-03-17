package com.airprofly.service;

import com.airprofly.dto.OrdersConfirmDTO;
import com.airprofly.dto.OrdersPageQueryDTO;
import com.airprofly.dto.OrdersRejectionDTO;
import com.airprofly.dto.OrdersSubmitDTO;
import com.airprofly.entity.Orders;
import com.airprofly.result.PageResult;
import com.airprofly.vo.OrderVO;
import com.airprofly.vo.OrderStatisticsVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单业务接口
 */
public interface OrderService {

    /**
     * 用户下单
     *
     * @param ordersSubmitDTO 订单提交DTO
     * @return 订单提交VO
     */
    OrderVO submit(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 订单确认
     *
     * @param ordersConfirmDTO 订单确认DTO
     */
    void confirm(OrdersConfirmDTO ordersConfirmDTO);

    /**
     * 订单拒绝
     *
     * @param ordersRejectionDTO 订单拒绝DTO
     */
    void rejection(OrdersRejectionDTO ordersRejectionDTO);

    /**
     * 取消订单
     *
     * @param id 订单ID
     */
    void cancel(Long id);

    /**
     * 完成订单
     *
     * @param id 订单ID
     */
    void complete(Long id);

    /**
     * 订单分页查询
     *
     * @param ordersPageQueryDTO 分页查询条件
     * @return 分页结果
     */
    PageResult<OrderVO> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据ID查询订单
     *
     * @param id 订单ID
     * @return 订单VO
     */
    OrderVO details(Long id);

    /**
     * 用户催单
     *
     * @param id 订单ID
     */
    void reminder(Long id);

    /**
     * 各个状态的订单数量统计
     *
     * @return 订单统计VO
     */
    OrderStatisticsVO statistics();

    /**
     * 查询超时未支付的订单
     *
     * @param now 当前时间
     * @return 订单列表
     */
    List<Orders> getUnpaidOrders(LocalDateTime now);

    /**
     * 查询处理中的订单
     *
     * @param now 当前时间
     * @return 订单列表
     */
    List<Orders> getProcessingOrders(LocalDateTime now);
}
