package com.airprofly.service.impl;

import com.airprofly.dto.OrdersConfirmDTO;
import com.airprofly.dto.OrdersPageQueryDTO;
import com.airprofly.dto.OrdersRejectionDTO;
import com.airprofly.dto.OrdersSubmitDTO;
import com.airprofly.entity.OrderDetail;
import com.airprofly.entity.Orders;
import com.airprofly.enumeration.OrderStatus;
import com.airprofly.enumeration.PayStatus;
import com.airprofly.mapper.OrderDetailMapper;
import com.airprofly.mapper.OrderMapper;
import com.airprofly.result.PageResult;
import com.airprofly.service.OrderService;
import com.airprofly.util.PageHelperUtil;
import com.airprofly.vo.OrderStatisticsVO;
import com.airprofly.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单业务实现
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderDetailMapper orderDetailMapper;

    /**
     * 用户下单
     */
    @Override
    @Transactional
    public OrderVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        // TODO: 实现下单逻辑 - 需要结合购物车、地址簿等
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);

        // 设置订单状态
        orders.setStatus(OrderStatus.PENDING_PAYMENT.getCode());
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(PayStatus.UN_PAID.getCode());

        orderMapper.insert(orders);

        // TODO: 保存订单明细

        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        return orderVO;
    }

    /**
     * 订单确认
     */
    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(OrderStatus.TO_BE_CONFIRMED.getCode())
                .build();
        orderMapper.update(orders);
    }

    /**
     * 订单拒绝
     */
    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        Orders orders = Orders.builder()
                .id(ordersRejectionDTO.getId())
                .status(OrderStatus.CANCELLED.getCode())
                .rejectionReason(ordersRejectionDTO.getRejectionReason())
                .build();
        orderMapper.update(orders);
    }

    /**
     * 取消订单
     */
    @Override
    public void cancel(Long id) {
        Orders orders = Orders.builder()
                .id(id)
                .status(OrderStatus.CANCELLED.getCode())
                .cancelTime(LocalDateTime.now())
                .build();
        orderMapper.update(orders);
    }

    /**
     * 完成订单
     */
    @Override
    public void complete(Long id) {
        Orders orders = Orders.builder()
                .id(id)
                .status(OrderStatus.COMPLETED.getCode())
                .checkoutTime(LocalDateTime.now())
                .build();
        orderMapper.update(orders);
    }

    /**
     * 订单分页查询
     */
    @Override
    public PageResult<OrderVO> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelperUtil.startPage(ordersPageQueryDTO);
        List<Orders> list = orderMapper.pageQuery(
                ordersPageQueryDTO.getUserId(),
                ordersPageQueryDTO.getNumber(),
                ordersPageQueryDTO.getPhone(),
                ordersPageQueryDTO.getStatus(),
                ordersPageQueryDTO.getBeginTime(),
                ordersPageQueryDTO.getEndTime()
        );

        List<OrderVO> orderVOList = new java.util.ArrayList<>();
        for (Orders orders : list) {
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(orders, orderVO);

            // 查询订单明细
            List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orders.getId());
            orderVO.setOrderDetailList(orderDetails);

            orderVOList.add(orderVO);
        }

        return PageHelperUtil.of(list, orderVOList);
    }

    /**
     * 根据ID查询订单
     */
    @Override
    public OrderVO details(Long id) {
        Orders orders = orderMapper.getById(id);

        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);

        // 查询订单明细
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(id);
        orderVO.setOrderDetailList(orderDetails);

        return orderVO;
    }

    /**
     * 用户催单
     */
    @Override
    public void reminder(Long id) {
        // TODO: 实现催单逻辑 - 可以发送通知给商家
    }

    /**
     * 各个状态的订单数量统计
     */
    @Override
    public OrderStatisticsVO statistics() {
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(orderMapper.countStatus(OrderStatus.TO_BE_CONFIRMED.getCode()));
        orderStatisticsVO.setConfirmed(orderMapper.countStatus(OrderStatus.CONFIRMED.getCode()));
        orderStatisticsVO.setDeliveryInProgress(orderMapper.countStatus(OrderStatus.DELIVERY_IN_PROGRESS.getCode()));
        return orderStatisticsVO;
    }

    /**
     * 查询超时未支付的订单
     */
    @Override
    public List<Orders> getUnpaidOrders(LocalDateTime now) {
        return orderMapper.getByStatusAndOrderTimeLT(OrderStatus.PENDING_PAYMENT.getCode(), now.minusMinutes(15));
    }

    /**
     * 查询处理中的订单
     */
    @Override
    public List<Orders> getProcessingOrders(LocalDateTime now) {
        return orderMapper.getByStatusAndOrderTimeLT(OrderStatus.TO_BE_CONFIRMED.getCode(), now.minusMinutes(60));
    }
}
