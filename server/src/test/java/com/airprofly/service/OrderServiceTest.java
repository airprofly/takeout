package com.airprofly.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
import com.airprofly.service.impl.OrderServiceImpl;
import com.airprofly.vo.OrderStatisticsVO;
import com.airprofly.vo.OrderVO;

/**
 * OrderService 测试类
 *
 * 测试覆盖:
 * - 订单 CRUD 操作测试
 * - 订单状态流转测试
 * - 业务逻辑验证测试
 * - 边界条件测试
 *
 * 测试策略:
 * - 使用 Mockito 进行依赖项模拟
 * - 验证业务逻辑正确性
 * - 验证订单状态流转
 * - 验证订单明细关联处理
 */
@ExtendWith(MockitoExtension.class)
@Tag("service")
@Tag("order")
@DisplayName("订单 Service 测试")
public class OrderServiceTest {

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderDetailMapper orderDetailMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    private OrdersSubmitDTO testOrdersSubmitDTO;
    private OrdersConfirmDTO testOrdersConfirmDTO;
    private OrdersRejectionDTO testOrdersRejectionDTO;
    private Orders testOrders;
    private OrderDetail testOrderDetail;

    @BeforeEach
    void setUp() {
        // 初始化订单提交 DTO 测试数据
        testOrdersSubmitDTO = new OrdersSubmitDTO();
        testOrdersSubmitDTO.setAddressBookId(1L);
        // OrdersSubmitDTO 没有 orderTime 字段
        testOrdersSubmitDTO.setPayMethod(1);
        testOrdersSubmitDTO.setRemark("少放辣");
        testOrdersSubmitDTO.setPackAmount(0); // 设置默认值，避免 null 复制到 int 类型
        testOrdersSubmitDTO.setTablewareNumber(1); // 设置默认餐具数量
        testOrdersSubmitDTO.setDeliveryStatus(1); // 设置默认配送状态

        // 初始化订单确认 DTO 测试数据
        testOrdersConfirmDTO = new OrdersConfirmDTO();
        testOrdersConfirmDTO.setId(1L);

        // 初始化订单拒绝 DTO 测试数据
        testOrdersRejectionDTO = new OrdersRejectionDTO();
        testOrdersRejectionDTO.setId(1L);
        testOrdersRejectionDTO.setRejectionReason("库存不足");

        // 初始化订单实体测试数据
        testOrders = Orders.builder()
                .id(1L)
                .number("ORD001")
                .status(OrderStatus.TO_BE_CONFIRMED.getCode())
                .userId(1L)
                .addressBookId(1L)
                .orderTime(LocalDateTime.now())
                .payStatus(PayStatus.UN_PAID.getCode())
                .payMethod(1)
                .remark("少放辣")
                .build();

        // 初始化订单明细测试数据
        testOrderDetail = OrderDetail.builder()
                .id(1L)
                .orderId(1L)
                .dishId(1L)
                .name("测试菜品")
                .number(2)
                .amount(new BigDecimal("5600"))
                .build();
    }

    // ==================== submit 方法测试 ====================

    @Nested
    @DisplayName("submit 方法测试")
    @Tag("create")
    class SubmitMethodTests {

        @Test
        @DisplayName("提交订单 - 成功场景")
        void shouldSubmitOrderWhenValidData() {
            // Given
            doNothing().when(orderMapper).insert(any(Orders.class));

            // When
            OrderVO result = orderService.submit(testOrdersSubmitDTO);

            // Then
            assertAll("订单提交验证",
                    () -> assertNotNull(result, "订单VO不应为null"),
                    () -> verify(orderMapper, times(1)).insert(argThat(order ->
                            order.getStatus() == OrderStatus.PENDING_PAYMENT.getCode() &&
                            order.getPayStatus() == PayStatus.UN_PAID.getCode()
                    ))
            );
        }

        @Test
        @DisplayName("提交订单时设置订单时间")
        void shouldSetOrderTimeWhenSubmitting() {
            // Given
            doNothing().when(orderMapper).insert(any(Orders.class));

            // When
            orderService.submit(testOrdersSubmitDTO);

            // Then
            verify(orderMapper).insert(argThat(order ->
                    order.getOrderTime() != null
            ));
        }

        @Test
        @DisplayName("提交订单时状态应为待付款")
        void shouldSetPendingPaymentStatusWhenSubmitting() {
            // Given
            doNothing().when(orderMapper).insert(any(Orders.class));

            // When
            orderService.submit(testOrdersSubmitDTO);

            // Then
            verify(orderMapper).insert(argThat(order ->
                    order.getStatus() == OrderStatus.PENDING_PAYMENT.getCode()
            ));
        }

        @Test
        @DisplayName("提交订单时支付状态应为未支付")
        void shouldSetUnpaidStatusWhenSubmitting() {
            // Given
            doNothing().when(orderMapper).insert(any(Orders.class));

            // When
            orderService.submit(testOrdersSubmitDTO);

            // Then
            verify(orderMapper).insert(argThat(order ->
                    order.getPayStatus() == PayStatus.UN_PAID.getCode()
            ));
        }
    }

    // ==================== confirm 方法测试 ====================

    @Nested
    @DisplayName("confirm 方法测试")
    @Tag("update")
    class ConfirmMethodTests {

        @Test
        @DisplayName("确认订单 - 成功场景")
        void shouldConfirmOrderWhenValidData() {
            // Given
            doNothing().when(orderMapper).update(any(Orders.class));

            // When
            orderService.confirm(testOrdersConfirmDTO);

            // Then
            verify(orderMapper, times(1)).update(argThat(order ->
                    order.getId() == 1L &&
                    order.getStatus() == OrderStatus.TO_BE_CONFIRMED.getCode()
            ));
        }

        @Test
        @DisplayName("确认订单时状态应设置为待接单")
        void shouldSetToBeConfirmedStatusWhenConfirming() {
            // Given
            doNothing().when(orderMapper).update(any(Orders.class));

            // When
            orderService.confirm(testOrdersConfirmDTO);

            // Then
            verify(orderMapper).update(argThat(order ->
                    order.getStatus() == OrderStatus.TO_BE_CONFIRMED.getCode()
            ));
        }
    }

    // ==================== rejection 方法测试 ====================

    @Nested
    @DisplayName("rejection 方法测试")
    @Tag("update")
    class RejectionMethodTests {

        @Test
        @DisplayName("拒绝订单 - 成功场景")
        void shouldRejectOrderWhenValidData() {
            // Given
            doNothing().when(orderMapper).update(any(Orders.class));

            // When
            orderService.rejection(testOrdersRejectionDTO);

            // Then
            verify(orderMapper, times(1)).update(argThat(order ->
                    order.getId() == 1L &&
                    order.getStatus() == OrderStatus.CANCELLED.getCode() &&
                    "库存不足".equals(order.getRejectionReason())
            ));
        }

        @Test
        @DisplayName("拒绝订单时状态应设置为已取消")
        void shouldSetCancelledStatusWhenRejecting() {
            // Given
            doNothing().when(orderMapper).update(any(Orders.class));

            // When
            orderService.rejection(testOrdersRejectionDTO);

            // Then
            verify(orderMapper).update(argThat(order ->
                    order.getStatus() == OrderStatus.CANCELLED.getCode()
            ));
        }

        @Test
        @DisplayName("拒绝订单时记录拒绝原因")
        void shouldSetRejectionReasonWhenRejecting() {
            // Given
            testOrdersRejectionDTO.setRejectionReason("菜品已售罄");
            doNothing().when(orderMapper).update(any(Orders.class));

            // When
            orderService.rejection(testOrdersRejectionDTO);

            // Then
            verify(orderMapper).update(argThat(order ->
                    "菜品已售罄".equals(order.getRejectionReason())
            ));
        }
    }

    // ==================== cancel 方法测试 ====================

    @Nested
    @DisplayName("cancel 方法测试")
    @Tag("update")
    class CancelMethodTests {

        @Test
        @DisplayName("取消订单 - 成功场景")
        void shouldCancelOrderWhenValidId() {
            // Given
            Long orderId = 1L;
            doNothing().when(orderMapper).update(any(Orders.class));

            // When
            orderService.cancel(orderId);

            // Then
            verify(orderMapper, times(1)).update(argThat(order ->
                    order.getId() == orderId &&
                    order.getStatus() == OrderStatus.CANCELLED.getCode()
            ));
        }

        @Test
        @DisplayName("取消订单时设置取消时间")
        void shouldSetCancelTimeWhenCancelling() {
            // Given
            Long orderId = 1L;
            doNothing().when(orderMapper).update(any(Orders.class));

            // When
            orderService.cancel(orderId);

            // Then
            verify(orderMapper).update(argThat(order ->
                    order.getCancelTime() != null
            ));
        }

        @Test
        @DisplayName("取消订单时状态应设置为已取消")
        void shouldSetCancelledStatusWhenCancelling() {
            // Given
            Long orderId = 1L;
            doNothing().when(orderMapper).update(any(Orders.class));

            // When
            orderService.cancel(orderId);

            // Then
            verify(orderMapper).update(argThat(order ->
                    order.getStatus() == OrderStatus.CANCELLED.getCode()
            ));
        }
    }

    // ==================== complete 方法测试 ====================

    @Nested
    @DisplayName("complete 方法测试")
    @Tag("update")
    class CompleteMethodTests {

        @Test
        @DisplayName("完成订单 - 成功场景")
        void shouldCompleteOrderWhenValidId() {
            // Given
            Long orderId = 1L;
            doNothing().when(orderMapper).update(any(Orders.class));

            // When
            orderService.complete(orderId);

            // Then
            verify(orderMapper, times(1)).update(argThat(order ->
                    order.getId() == orderId &&
                    order.getStatus() == OrderStatus.COMPLETED.getCode()
            ));
        }

        @Test
        @DisplayName("完成订单时设置完成时间")
        void shouldSetCheckoutTimeWhenCompleting() {
            // Given
            Long orderId = 1L;
            doNothing().when(orderMapper).update(any(Orders.class));

            // When
            orderService.complete(orderId);

            // Then
            verify(orderMapper).update(argThat(order ->
                    order.getCheckoutTime() != null
            ));
        }

        @Test
        @DisplayName("完成订单时状态应设置为已完成")
        void shouldSetCompletedStatusWhenCompleting() {
            // Given
            Long orderId = 1L;
            doNothing().when(orderMapper).update(any(Orders.class));

            // When
            orderService.complete(orderId);

            // Then
            verify(orderMapper).update(argThat(order ->
                    order.getStatus() == OrderStatus.COMPLETED.getCode()
            ));
        }
    }

    // ==================== pageQuery 方法测试 ====================

    @Nested
    @DisplayName("pageQuery 方法测试")
    @Tag("query")
    class PageQueryMethodTests {

        @Test
        @DisplayName("分页查询订单 - 成功场景")
        void shouldReturnPageResultWhenQuery() {
            // Given
            OrdersPageQueryDTO queryDTO = new OrdersPageQueryDTO();
            queryDTO.setPage(1);
            queryDTO.setPageSize(10);
            queryDTO.setUserId(1L);

            List<Orders> orders = Arrays.asList(testOrders);
            when(orderMapper.pageQuery(any(), any(), any(), any(), any(), any())).thenReturn(orders);
            when(orderDetailMapper.getByOrderId(anyLong())).thenReturn(Arrays.asList(testOrderDetail));

            // When
            PageResult<OrderVO> result = orderService.pageQuery(queryDTO);

            // Then
            assertAll("分页查询验证",
                    () -> assertNotNull(result, "分页结果不应为null"),
                    () -> verify(orderMapper, times(1)).pageQuery(any(), any(), any(), any(), any(), any())
            );
        }

        @Test
        @DisplayName("分页查询时所有参数为 null")
        void shouldHandleNullParameters() {
            // Given
            OrdersPageQueryDTO queryDTO = new OrdersPageQueryDTO();
            queryDTO.setPage(1);
            queryDTO.setPageSize(10);

            when(orderMapper.pageQuery(any(), any(), any(), any(), any(), any())).thenReturn(Collections.emptyList());

            // When
            PageResult<OrderVO> result = orderService.pageQuery(queryDTO);

            // Then
            assertAll("空参数验证",
                    () -> assertNotNull(result, "分页结果不应为null"),
                    () -> verify(orderMapper, times(1)).pageQuery(eq(null), eq(null), eq(null), eq(null), eq(null), eq(null))
            );
        }

        @Test
        @DisplayName("分页查询返回空结果")
        void shouldReturnEmptyPageResultWhenNoData() {
            // Given
            OrdersPageQueryDTO queryDTO = new OrdersPageQueryDTO();
            queryDTO.setPage(1);
            queryDTO.setPageSize(10);

            when(orderMapper.pageQuery(any(), any(), any(), any(), any(), any())).thenReturn(Collections.emptyList());

            // When
            PageResult<OrderVO> result = orderService.pageQuery(queryDTO);

            // Then
            assertAll("空结果验证",
                    () -> assertNotNull(result, "分页结果不应为null"),
                    () -> assertEquals(0L, result.getTotal(), "总记录数应为0")
            );
        }
    }

    // ==================== details 方法测试 ====================

    @Nested
    @DisplayName("details 方法测试")
    @Tag("query")
    class DetailsMethodTests {

        @Test
        @DisplayName("根据ID查询订单详情 - 成功场景")
        void shouldReturnOrderWithDetailsWhenIdExists() {
            // Given
            Long orderId = 1L;
            when(orderMapper.getById(orderId)).thenReturn(testOrders);
            when(orderDetailMapper.getByOrderId(orderId)).thenReturn(Arrays.asList(testOrderDetail));

            // When
            OrderVO result = orderService.details(orderId);

            // Then
            assertAll("查询验证",
                    () -> assertNotNull(result, "订单VO不应为null"),
                    () -> assertEquals(orderId, result.getId(), "ID应匹配"),
                    () -> assertNotNull(result.getOrderDetailList(), "订单明细列表不应为null"),
                    () -> verify(orderMapper, times(1)).getById(orderId),
                    () -> verify(orderDetailMapper, times(1)).getByOrderId(orderId)
            );
        }

        @Test
        @DisplayName("查询不存在的订单ID")
        void shouldThrowExceptionWhenOrderNotExists() {
            // Given
            Long nonExistentId = 999999L;
            when(orderMapper.getById(nonExistentId)).thenReturn(null);

            // When & Then
            // 注意：BeanUtils.copyProperties 在 source 为 null 时会抛出 IllegalArgumentException
            // 这是实际 Service 的行为，测试需要反映这一点
            assertThrows(IllegalArgumentException.class, () -> {
                orderService.details(nonExistentId);
            }, "不存在的订单ID应抛出异常");
            verify(orderMapper, times(1)).getById(nonExistentId);
            verify(orderDetailMapper, never()).getByOrderId(anyLong());
        }

        @Test
        @DisplayName("查询订单但没有明细")
        void shouldReturnOrderWithEmptyDetailList() {
            // Given
            Long orderId = 1L;
            when(orderMapper.getById(orderId)).thenReturn(testOrders);
            when(orderDetailMapper.getByOrderId(orderId)).thenReturn(Collections.emptyList());

            // When
            OrderVO result = orderService.details(orderId);

            // Then
            assertAll("无明细验证",
                    () -> assertNotNull(result, "订单VO不应为null"),
                    () -> assertNotNull(result.getOrderDetailList(), "订单明细列表不应为null"),
                    () -> assertTrue(result.getOrderDetailList().isEmpty(), "订单明细列表应为空")
            );
        }
    }

    // ==================== reminder 方法测试 ====================

    @Nested
    @DisplayName("reminder 方法测试")
    @Tag("update")
    class ReminderMethodTests {

        @Test
        @DisplayName("催单 - TODO 待实现")
        void shouldReminderWhenImplemented() {
            // Given
            Long orderId = 1L;

            // When & Then
            // 此方法当前为 TODO 状态，实现后需添加完整测试
            assertDoesNotThrow(() -> {
                orderService.reminder(orderId);
            }, "催单方法当前为空实现");
        }
    }

    // ==================== statistics 方法测试 ====================

    @Nested
    @DisplayName("statistics 方法测试")
    @Tag("query")
    class StatisticsMethodTests {

        @Test
        @DisplayName("订单统计 - 成功场景")
        void shouldReturnStatisticsWhenCalled() {
            // Given
            when(orderMapper.countStatus(OrderStatus.TO_BE_CONFIRMED.getCode())).thenReturn(5);
            when(orderMapper.countStatus(OrderStatus.CONFIRMED.getCode())).thenReturn(3);
            when(orderMapper.countStatus(OrderStatus.DELIVERY_IN_PROGRESS.getCode())).thenReturn(2);

            // When
            OrderStatisticsVO result = orderService.statistics();

            // Then
            assertAll("统计验证",
                    () -> assertNotNull(result, "统计结果不应为null"),
                    () -> assertEquals(5, result.getToBeConfirmed(), "待接单数量应匹配"),
                    () -> assertEquals(3, result.getConfirmed(), "已接单数量应匹配"),
                    () -> assertEquals(2, result.getDeliveryInProgress(), "配送中数量应匹配"),
                    () -> verify(orderMapper, times(1)).countStatus(OrderStatus.TO_BE_CONFIRMED.getCode()),
                    () -> verify(orderMapper, times(1)).countStatus(OrderStatus.CONFIRMED.getCode()),
                    () -> verify(orderMapper, times(1)).countStatus(OrderStatus.DELIVERY_IN_PROGRESS.getCode())
            );
        }

        @Test
        @DisplayName("订单统计返回零值")
        void shouldReturnZeroStatisticsWhenNoOrders() {
            // Given
            when(orderMapper.countStatus(anyInt())).thenReturn(0);

            // When
            OrderStatisticsVO result = orderService.statistics();

            // Then
            assertAll("零统计验证",
                    () -> assertNotNull(result, "统计结果不应为null"),
                    () -> assertEquals(0, result.getToBeConfirmed(), "待接单数量应为0"),
                    () -> assertEquals(0, result.getConfirmed(), "已接单数量应为0"),
                    () -> assertEquals(0, result.getDeliveryInProgress(), "配送中数量应为0")
            );
        }
    }

    // ==================== getUnpaidOrders 方法测试 ====================

    @Nested
    @DisplayName("getUnpaidOrders 方法测试")
    @Tag("query")
    class GetUnpaidOrdersMethodTests {

        @Test
        @DisplayName("查询超时未支付订单 - 成功场景")
        void shouldReturnUnpaidOrdersWhenExist() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            when(orderMapper.getByStatusAndOrderTimeLT(
                    OrderStatus.PENDING_PAYMENT.getCode(),
                    now.minusMinutes(15)
            )).thenReturn(Arrays.asList(testOrders));

            // When
            List<Orders> result = orderService.getUnpaidOrders(now);

            // Then
            assertAll("查询验证",
                    () -> assertNotNull(result, "订单列表不应为null"),
                    () -> verify(orderMapper, times(1)).getByStatusAndOrderTimeLT(
                            eq(OrderStatus.PENDING_PAYMENT.getCode()),
                            eq(now.minusMinutes(15))
                    )
            );
        }

        @Test
        @DisplayName("查询超时未支付订单返回空列表")
        void shouldReturnEmptyListWhenNoUnpaidOrders() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            when(orderMapper.getByStatusAndOrderTimeLT(anyInt(), any())).thenReturn(Collections.emptyList());

            // When
            List<Orders> result = orderService.getUnpaidOrders(now);

            // Then
            assertAll("空结果验证",
                    () -> assertNotNull(result, "订单列表不应为null"),
                    () -> assertTrue(result.isEmpty(), "订单列表应为空")
            );
        }
    }

    // ==================== getProcessingOrders 方法测试 ====================

    @Nested
    @DisplayName("getProcessingOrders 方法测试")
    @Tag("query")
    class GetProcessingOrdersMethodTests {

        @Test
        @DisplayName("查询处理中订单 - 成功场景")
        void shouldReturnProcessingOrdersWhenExist() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            when(orderMapper.getByStatusAndOrderTimeLT(
                    OrderStatus.TO_BE_CONFIRMED.getCode(),
                    now.minusMinutes(60)
            )).thenReturn(Arrays.asList(testOrders));

            // When
            List<Orders> result = orderService.getProcessingOrders(now);

            // Then
            assertAll("查询验证",
                    () -> assertNotNull(result, "订单列表不应为null"),
                    () -> verify(orderMapper, times(1)).getByStatusAndOrderTimeLT(
                            eq(OrderStatus.TO_BE_CONFIRMED.getCode()),
                            eq(now.minusMinutes(60))
                    )
            );
        }

        @Test
        @DisplayName("查询处理中订单返回空列表")
        void shouldReturnEmptyListWhenNoProcessingOrders() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            when(orderMapper.getByStatusAndOrderTimeLT(anyInt(), any())).thenReturn(Collections.emptyList());

            // When
            List<Orders> result = orderService.getProcessingOrders(now);

            // Then
            assertAll("空结果验证",
                    () -> assertNotNull(result, "订单列表不应为null"),
                    () -> assertTrue(result.isEmpty(), "订单列表应为空")
            );
        }
    }

    // ==================== 综合业务场景测试 ====================

    @Nested
    @DisplayName("综合业务场景测试")
    @Tag("integration")
    class IntegrationTests {

        @Test
        @DisplayName("完整订单生命周期测试")
        void shouldCompleteFullOrderLifecycle() {
            // Given
            doNothing().when(orderMapper).insert(any(Orders.class));
            doNothing().when(orderMapper).update(any(Orders.class));
            when(orderMapper.getById(anyLong())).thenReturn(testOrders);
            when(orderDetailMapper.getByOrderId(anyLong())).thenReturn(Arrays.asList(testOrderDetail));

            // 1. 提交订单
            orderService.submit(testOrdersSubmitDTO);
            verify(orderMapper, times(1)).insert(any(Orders.class));

            // 2. 查询订单详情
            orderService.details(1L);
            verify(orderMapper, times(1)).getById(anyLong());

            // 3. 确认订单
            orderService.confirm(testOrdersConfirmDTO);
            verify(orderMapper, atLeastOnce()).update(any(Orders.class));

            // 4. 完成订单
            orderService.complete(1L);
            verify(orderMapper, atLeastOnce()).update(any(Orders.class));
        }

        @Test
        @DisplayName("订单状态流转测试")
        void shouldHandleOrderStatusFlowCorrectly() {
            // Given
            doNothing().when(orderMapper).update(any(Orders.class));

            // When - 提交订单(待付款)
            // OrdersSubmitDTO 没有 id 字段
            orderService.submit(testOrdersSubmitDTO);

            // When - 确认订单(待接单)
            orderService.confirm(testOrdersConfirmDTO);
            verify(orderMapper).update(argThat(o -> o.getStatus() == OrderStatus.TO_BE_CONFIRMED.getCode()));

            // When - 完成订单
            orderService.complete(1L);
            verify(orderMapper).update(argThat(o -> o.getStatus() == OrderStatus.COMPLETED.getCode()));
        }

        @Test
        @DisplayName("订单取消流程测试")
        void shouldCancelOrderCorrectly() {
            // Given
            doNothing().when(orderMapper).update(any(Orders.class));

            // When - 拒绝订单
            orderService.rejection(testOrdersRejectionDTO);
            verify(orderMapper).update(argThat(o ->
                    o.getStatus() == OrderStatus.CANCELLED.getCode() &&
                    "库存不足".equals(o.getRejectionReason())
            ));

            // When - 取消订单
            orderService.cancel(1L);
            verify(orderMapper).update(argThat(o ->
                    o.getStatus() == OrderStatus.CANCELLED.getCode() &&
                    o.getCancelTime() != null
            ));
        }

        @Test
        @DisplayName("超时订单处理测试")
        void shouldHandleTimeoutOrders() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            when(orderMapper.getByStatusAndOrderTimeLT(anyInt(), any())).thenReturn(Arrays.asList(testOrders));

            // When - 查询超时未支付订单
            List<Orders> unpaidOrders = orderService.getUnpaidOrders(now);
            assertFalse(unpaidOrders.isEmpty(), "应找到超时未支付订单");

            // When - 查询处理中超时订单
            List<Orders> processingOrders = orderService.getProcessingOrders(now);
            assertFalse(processingOrders.isEmpty(), "应找到处理中超时订单");
        }
    }
}
