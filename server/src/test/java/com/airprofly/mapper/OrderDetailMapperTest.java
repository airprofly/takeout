package com.airprofly.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.airprofly.entity.OrderDetail;

/**
 * OrderDetailMapper 测试类
 *
 * 测试覆盖:
 * - 批量插入操作测试
 * - 根据订单ID查询测试
 * - 边界条件测试
 * - 综合业务流程测试
 *
 * 最佳实践:
 * - 使用 @Nested 组织相关测试
 * - 使用 @Tag 分类测试(快速测试、集成测试等)
 * - 使用 assertAll 确保所有断言都执行
 * - 使用参数化测试减少重复代码
 */
@SpringBootTest
@Transactional
@Tag("mapper")
@Tag("order-detail")
@DisplayName("订单明细 Mapper 测试")
public class OrderDetailMapperTest {

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    // ==================== 测试数据工厂方法 ====================

    /**
     * 创建标准的测试订单明细对象
     */
    private OrderDetail createTestOrderDetail(Long orderId, Long dishId) {
        return OrderDetail.builder()
                .name("测试菜品")
                .orderId(orderId)
                .dishId(dishId)
                .setmealId(null)
                .dishFlavor("微辣")
                .number(1)
                .amount(new BigDecimal("28.00"))
                .image("test.jpg")
                .build();
    }

    /**
     * 创建并插入测试订单明细,返回插入后的对象(包含生成的ID)
     */
    private OrderDetail insertTestOrderDetail(Long orderId, Long dishId) {
        OrderDetail orderDetail = createTestOrderDetail(orderId, dishId);
        List<OrderDetail> list = new ArrayList<>();
        list.add(orderDetail);
        orderDetailMapper.insertBatch(list);
        return orderDetail;
    }

    // ==================== insertBatch 方法测试 ====================

    @Nested
    @DisplayName("insertBatch 方法测试")
    @Tag("create")
    class InsertBatchMethodTests {

        @Test
        @DisplayName("批量插入订单明细 - 成功场景")
        void shouldInsertOrderDetailsWhenValidData() {
            // Given
            Long orderId = 800001L;
            List<OrderDetail> orderDetails = new ArrayList<>();
            orderDetails.add(createTestOrderDetail(orderId, 1L));
            orderDetails.add(createTestOrderDetail(orderId, 2L));
            orderDetails.add(createTestOrderDetail(orderId, 3L));

            // When
            orderDetailMapper.insertBatch(orderDetails);

            // Then
            assertAll("批量插入验证",
                    () -> assertNotNull(orderDetails.get(0).getId(), "第一条记录ID应自动生成"),
                    () -> assertNotNull(orderDetails.get(1).getId(), "第二条记录ID应自动生成"),
                    () -> assertNotNull(orderDetails.get(2).getId(), "第三条记录ID应自动生成"),
                    () -> {
                        List<OrderDetail> found = orderDetailMapper.getByOrderId(orderId);
                        assertEquals(3, found.size(), "应能查询到3条插入的订单明细");
                    });
        }

        @Test
        @DisplayName("批量插入单条记录")
        void shouldInsertSingleOrderDetail() {
            // Given
            Long orderId = 800002L;
            List<OrderDetail> orderDetails = new ArrayList<>();
            orderDetails.add(createTestOrderDetail(orderId, 1L));

            // When
            orderDetailMapper.insertBatch(orderDetails);

            // Then
            assertAll("单条插入验证",
                    () -> assertNotNull(orderDetails.get(0).getId(), "ID应自动生成"),
                    () -> {
                        List<OrderDetail> foundList = orderDetailMapper.getByOrderId(orderId);
                        assertEquals(1, foundList.size(), "该订单应仅有1条新增明细");
                        OrderDetail found = foundList.stream()
                                .filter(od -> orderDetails.get(0).getId().equals(od.getId()))
                                .findFirst()
                                .orElse(null);
                        assertNotNull(found, "应能根据ID找到刚插入的数据");
                        assertEquals("测试菜品", found.getName(), "菜品名称应匹配");
                    });
        }

        @ParameterizedTest
        @ValueSource(ints = { 1, 5, 10, 50 })
        @DisplayName("批量插入不同数量的订单明细")
        @Tag("parameterized")
        void shouldInsertDifferentNumberOfOrderDetails(int count) {
            // Given
            List<OrderDetail> orderDetails = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                orderDetails.add(createTestOrderDetail(100L + i, (long) i));
            }

            // When
            orderDetailMapper.insertBatch(orderDetails);

            // Then
            long successCount = orderDetails.stream()
                    .filter(od -> od.getId() != null)
                    .count();
            assertEquals(count, successCount, "应成功插入" + count + "条记录");
        }
    }

    // ==================== getByOrderId 方法测试 ====================

    @Nested
    @DisplayName("getByOrderId 方法测试")
    @Tag("query")
    class GetByOrderIdMethodTests {

        @Test
        @DisplayName("根据订单ID查询订单明细 - 成功场景")
        void shouldReturnOrderDetailsWhenOrderIdExists() {
            // Given
            Long orderId = 1L;
            insertTestOrderDetail(orderId, 1L);
            insertTestOrderDetail(orderId, 2L);

            // When
            List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orderId);

            // Then
            assertAll("查询验证",
                    () -> assertNotNull(orderDetails, "订单明细列表不应为null"),
                    () -> assertTrue(orderDetails.size() >= 2, "应至少有2条订单明细"));
        }

        @Test
        @DisplayName("订单ID不存在时返回空列表")
        void shouldReturnEmptyListWhenOrderIdNotExists() {
            // Given
            Long nonExistentOrderId = 999999L;

            // When
            List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(nonExistentOrderId);

            // Then
            assertAll("空列表验证",
                    () -> assertNotNull(orderDetails, "订单明细列表不应为null"),
                    () -> assertTrue(orderDetails.isEmpty(), "不存在的订单ID应返回空列表"));
        }

        @ParameterizedTest
        @ValueSource(longs = { 999999L, -1L, 0L })
        @DisplayName("无效订单ID时返回空列表")
        @Tag("edge-case")
        void shouldReturnEmptyListWhenOrderIdIsInvalid(Long invalidOrderId) {
            // When
            List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(invalidOrderId);

            // Then
            assertAll("无效ID验证",
                    () -> assertNotNull(orderDetails, "订单明细列表不应为null"),
                    () -> assertTrue(orderDetails.isEmpty(), "无效订单ID应返回空列表"));
        }

        @Test
        @DisplayName("验证返回数据完整性")
        void shouldReturnCompleteOrderDetailData() {
            // Given
            Long orderId = 800003L;
            OrderDetail inserted = insertTestOrderDetail(orderId, 1L);

            // When
            List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orderId);

            // Then
            if (!orderDetails.isEmpty()) {
                OrderDetail firstDetail = orderDetails.get(0);
                assertAll("订单明细数据完整性验证",
                        () -> assertNotNull(firstDetail.getId(), "订单明细ID不应为null"),
                        () -> assertNotNull(firstDetail.getOrderId(), "订单ID不应为null"),
                        () -> assertNotNull(firstDetail.getName(), "菜品名称不应为null"),
                        () -> assertNotNull(firstDetail.getNumber(), "数量不应为null"),
                        () -> assertNotNull(firstDetail.getAmount(), "金额不应为null"));
            }
        }

        @Test
        @DisplayName("验证查询结果按订单ID分组")
        void shouldReturnOrderDetailsGroupedByOrderId() {
            // Given - 为不同订单插入明细
            insertTestOrderDetail(10L, 1L);
            insertTestOrderDetail(10L, 2L);
            insertTestOrderDetail(20L, 3L);

            // When - 查询订单10的明细
            List<OrderDetail> orderDetails10 = orderDetailMapper.getByOrderId(10L);

            // When - 查询订单20的明细
            List<OrderDetail> orderDetails20 = orderDetailMapper.getByOrderId(20L);

            // Then
            assertAll("分组查询验证",
                    () -> assertTrue(orderDetails10.size() >= 2, "订单10应有2条明细"),
                    () -> assertTrue(orderDetails20.size() >= 1, "订单20应有1条明细"),
                    () -> {
                        for (OrderDetail detail : orderDetails10) {
                            assertEquals(10L, detail.getOrderId(), "所有明细应属于订单10");
                        }
                    });
        }
    }

    // ==================== 综合场景测试 ====================

    @Nested
    @DisplayName("综合业务流程测试")
    @Tag("integration")
    class IntegrationTests {

        @Test
        @DisplayName("订单明细完整流程测试")
        void shouldCompleteFullOrderDetailProcess() {
            // Given
            Long orderId = 100L;
            List<OrderDetail> orderDetails = new ArrayList<>();
            orderDetails.add(createTestOrderDetail(orderId, 1L));
            orderDetails.add(createTestOrderDetail(orderId, 2L));
            orderDetails.add(createTestOrderDetail(orderId, 3L));

            // When - 批量插入
            orderDetailMapper.insertBatch(orderDetails);

            // Then - 验证插入
            assertAll("插入验证",
                    () -> assertNotNull(orderDetails.get(0).getId(), "第一条明细ID应生成"),
                    () -> assertNotNull(orderDetails.get(1).getId(), "第二条明细ID应生成"),
                    () -> assertNotNull(orderDetails.get(2).getId(), "第三条明细ID应生成"));

            // When - 查询订单明细
            List<OrderDetail> found = orderDetailMapper.getByOrderId(orderId);

            // Then - 验证查询
            assertAll("查询验证",
                    () -> assertNotNull(found, "应能查询到订单明细"),
                    () -> assertTrue(found.size() >= 3, "应有3条明细"),
                    () -> {
                        for (OrderDetail detail : found) {
                            assertEquals(orderId, detail.getOrderId(), "所有明细应属于同一订单");
                        }
                    });
        }

        @Test
        @DisplayName("多订单明细管理测试")
        void shouldManageMultipleOrdersDetails() {
            // Given - 为多个订单插入明细
            Long orderId1 = 200L;
            Long orderId2 = 300L;

            List<OrderDetail> orderDetails1 = new ArrayList<>();
            orderDetails1.add(createTestOrderDetail(orderId1, 1L));
            orderDetails1.add(createTestOrderDetail(orderId1, 2L));

            List<OrderDetail> orderDetails2 = new ArrayList<>();
            orderDetails2.add(createTestOrderDetail(orderId2, 3L));

            // When - 批量插入
            orderDetailMapper.insertBatch(orderDetails1);
            orderDetailMapper.insertBatch(orderDetails2);

            // Then - 验证各自订单的明细
            List<OrderDetail> found1 = orderDetailMapper.getByOrderId(orderId1);
            List<OrderDetail> found2 = orderDetailMapper.getByOrderId(orderId2);

            assertAll("多订单验证",
                    () -> assertTrue(found1.size() >= 2, "订单1应有2条明细"),
                    () -> assertTrue(found2.size() >= 1, "订单2应有1条明细"),
                    () -> {
                        for (OrderDetail detail : found1) {
                            assertEquals(orderId1, detail.getOrderId(), "订单1明细归属应正确");
                        }
                    },
                    () -> {
                        for (OrderDetail detail : found2) {
                            assertEquals(orderId2, detail.getOrderId(), "订单2明细归属应正确");
                        }
                    });
        }

        @Test
        @DisplayName("套餐和菜品订单明细混合测试")
        void shouldHandleDishAndSetmealOrderDetails() {
            // Given
            Long orderId = 400L;
            List<OrderDetail> orderDetails = new ArrayList<>();

            // 添加菜品明细
            OrderDetail dishDetail = createTestOrderDetail(orderId, 1L);
            dishDetail.setDishId(1L);
            dishDetail.setSetmealId(null);
            orderDetails.add(dishDetail);

            // 添加套餐明细
            OrderDetail setmealDetail = OrderDetail.builder()
                    .name("测试套餐")
                    .orderId(orderId)
                    .dishId(null)
                    .setmealId(1L)
                    .number(1)
                    .amount(new BigDecimal("58.00"))
                    .image("setmeal.jpg")
                    .build();
            orderDetails.add(setmealDetail);

            // When
            orderDetailMapper.insertBatch(orderDetails);

            // Then
            List<OrderDetail> found = orderDetailMapper.getByOrderId(orderId);
            assertTrue(found.size() >= 2, "应包含菜品和套餐明细");

            boolean hasDish = found.stream().anyMatch(od -> od.getDishId() != null);
            boolean hasSetmeal = found.stream().anyMatch(od -> od.getSetmealId() != null);

            assertAll("菜品和套餐混合验证",
                    () -> assertTrue(hasDish, "应包含菜品明细"),
                    () -> assertTrue(hasSetmeal, "应包含套餐明细"));
        }

        @Test
        @DisplayName("订单明细金额计算测试")
        void shouldCalculateOrderDetailAmountCorrectly() {
            // Given
            Long orderId = 500L;
            List<OrderDetail> orderDetails = new ArrayList<>();

            OrderDetail detail1 = createTestOrderDetail(orderId, 1L);
            detail1.setNumber(2);
            detail1.setAmount(new BigDecimal("56.00")); // 28 * 2
            orderDetails.add(detail1);

            OrderDetail detail2 = createTestOrderDetail(orderId, 2L);
            detail2.setNumber(3);
            detail2.setAmount(new BigDecimal("84.00")); // 28 * 3
            orderDetails.add(detail2);

            // When
            orderDetailMapper.insertBatch(orderDetails);

            // Then
            List<OrderDetail> found = orderDetailMapper.getByOrderId(orderId);
            BigDecimal totalAmount = found.stream()
                    .map(OrderDetail::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            assertEquals(new BigDecimal("140.00"), totalAmount, "订单总金额应为140");
        }
    }
}
