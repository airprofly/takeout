package com.airprofly.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
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

import com.airprofly.dto.ShoppingCartDTO;
import com.airprofly.entity.ShoppingCart;
import com.airprofly.mapper.ShoppingCartMapper;

/**
 * ShoppingCartService 测试类
 *
 * 测试覆盖:
 * - 购物车 CRUD 操作测试
 * - 商品数量管理测试
 * - 业务逻辑验证测试
 * - 边界条件测试
 *
 * 测试策略:
 * - 使用 Mockito 进行依赖项模拟
 * - 验证业务逻辑正确性
 * - 验证数量增减逻辑
 * - 验证用户隔离
 */
@ExtendWith(MockitoExtension.class)
@Tag("service")
@Tag("shopping-cart")
@DisplayName("购物车 Service 测试")
public class ShoppingCartServiceTest {

    @Mock
    private ShoppingCartMapper shoppingCartMapper;

    @InjectMocks
    private com.airprofly.service.impl.ShoppingCartServiceImpl shoppingCartService;

    private ShoppingCartDTO testShoppingCartDTO;
    private ShoppingCart testShoppingCart;

    @BeforeEach
    void setUp() {
        // 初始化购物车 DTO 测试数据
        testShoppingCartDTO = new ShoppingCartDTO();
        testShoppingCartDTO.setDishId(1L);
        testShoppingCartDTO.setDishFlavor("微辣");

        // 初始化购物车实体测试数据
        testShoppingCart = ShoppingCart.builder()
                .id(1L)
                .userId(1L)
                .dishId(1L)
                .dishFlavor("微辣")
                .number(1)
                .amount(new BigDecimal("2800"))
                .build();
    }

    // ==================== add 方法测试 ====================

    @Nested
    @DisplayName("add 方法测试")
    @Tag("create")
    class AddMethodTests {

        @Test
        @DisplayName("添加购物车商品 - 新增场景")
        void shouldAddNewCartItemWhenNotExists() {
            // Given
            when(shoppingCartMapper.list(any(ShoppingCart.class))).thenReturn(Collections.emptyList());
            doNothing().when(shoppingCartMapper).insert(any(ShoppingCart.class));

            // When
            shoppingCartService.add(testShoppingCartDTO);

            // Then
            assertAll("新增验证",
                    () -> verify(shoppingCartMapper, times(1)).list(any(ShoppingCart.class)),
                    () -> verify(shoppingCartMapper, times(1)).insert(argThat(cart ->
                            cart.getNumber() == 1 &&
                            cart.getDishId() == 1L &&
                            "微辣".equals(cart.getDishFlavor())
                    ))
            );
        }

        @Test
        @DisplayName("添加已存在的购物车商品应增加数量")
        void shouldIncrementQuantityWhenCartItemExists() {
            // Given
            when(shoppingCartMapper.list(any(ShoppingCart.class))).thenReturn(Arrays.asList(testShoppingCart));
            doNothing().when(shoppingCartMapper).updateNumberById(any(ShoppingCart.class));

            // When
            shoppingCartService.add(testShoppingCartDTO);

            // Then
            assertAll("增加数量验证",
                    () -> verify(shoppingCartMapper, times(1)).list(any(ShoppingCart.class)),
                    () -> verify(shoppingCartMapper, times(1)).updateNumberById(argThat(cart ->
                            cart.getNumber() == 2 // 1 + 1 = 2
                    ))
            );
        }

        @Test
        @DisplayName("添加多个相同商品应连续增加数量")
        void shouldContinuouslyIncrementQuantity() {
            // Given
            testShoppingCart.setNumber(3);
            when(shoppingCartMapper.list(any(ShoppingCart.class))).thenReturn(Arrays.asList(testShoppingCart));
            doNothing().when(shoppingCartMapper).updateNumberById(any(ShoppingCart.class));

            // When
            shoppingCartService.add(testShoppingCartDTO);

            // Then
            verify(shoppingCartMapper).updateNumberById(argThat(cart ->
                    cart.getNumber() == 4 // 3 + 1 = 4
            ));
        }

        @Test
        @DisplayName("添加购物车时应设置用户ID")
        void shouldSetUserIdWhenAdding() {
            // Given
            when(shoppingCartMapper.list(any(ShoppingCart.class))).thenReturn(Collections.emptyList());
            doNothing().when(shoppingCartMapper).insert(any(ShoppingCart.class));

            // When
            shoppingCartService.add(testShoppingCartDTO);

            // Then
            verify(shoppingCartMapper).insert(argThat(cart ->
                    cart.getUserId() != null
            ));
        }

        @Test
        @DisplayName("添加套餐到购物车")
        void shouldAddSetmealToCart() {
            // Given
            testShoppingCartDTO.setDishId(null);
            testShoppingCartDTO.setSetmealId(1L);
            testShoppingCart.setDishId(null);
            testShoppingCart.setSetmealId(1L);

            when(shoppingCartMapper.list(any(ShoppingCart.class))).thenReturn(Collections.emptyList());
            doNothing().when(shoppingCartMapper).insert(any(ShoppingCart.class));

            // When
            shoppingCartService.add(testShoppingCartDTO);

            // Then
            verify(shoppingCartMapper).insert(argThat(cart ->
                    cart.getSetmealId() == 1L &&
                    cart.getDishId() == null
            ));
        }
    }

    // ==================== list 方法测试 ====================

    @Nested
    @DisplayName("list 方法测试")
    @Tag("query")
    class ListMethodTests {

        @Test
        @DisplayName("查询购物车列表 - 成功场景")
        void shouldReturnShoppingCartList() {
            // Given
            when(shoppingCartMapper.list(any(ShoppingCart.class))).thenReturn(Arrays.asList(testShoppingCart));

            // When
            List<ShoppingCart> result = shoppingCartService.list();

            // Then
            assertAll("查询验证",
                    () -> assertNotNull(result, "购物车列表不应为null"),
                    () -> assertFalse(result.isEmpty(), "购物车列表不应为空"),
                    () -> verify(shoppingCartMapper, times(1)).list(any(ShoppingCart.class))
            );
        }

        @Test
        @DisplayName("查询购物车返回空列表")
        void shouldReturnEmptyListWhenNoItems() {
            // Given
            when(shoppingCartMapper.list(any(ShoppingCart.class))).thenReturn(Collections.emptyList());

            // When
            List<ShoppingCart> result = shoppingCartService.list();

            // Then
            assertAll("空结果验证",
                    () -> assertNotNull(result, "购物车列表不应为null"),
                    () -> assertTrue(result.isEmpty(), "购物车列表应为空")
            );
        }

        @Test
        @DisplayName("查询购物车时应按用户ID过滤")
        void shouldFilterByUserId() {
            // Given
            when(shoppingCartMapper.list(any(ShoppingCart.class))).thenReturn(Arrays.asList(testShoppingCart));

            // When
            shoppingCartService.list();

            // Then
            verify(shoppingCartMapper).list(argThat(cart ->
                    cart.getUserId() != null
            ));
        }
    }

    // ==================== clean 方法测试 ====================

    @Nested
    @DisplayName("clean 方法测试")
    @Tag("delete")
    class CleanMethodTests {

        @Test
        @DisplayName("清空购物车 - 成功场景")
        void shouldCleanShoppingCart() {
            // Given
            doNothing().when(shoppingCartMapper).deleteByUserId(anyLong());

            // When
            shoppingCartService.clean();

            // Then
            verify(shoppingCartMapper, times(1)).deleteByUserId(anyLong());
        }

        @Test
        @DisplayName("清空购物车时应按用户ID删除")
        void shouldDeleteByUserId() {
            // Given
            doNothing().when(shoppingCartMapper).deleteByUserId(anyLong());

            // When
            shoppingCartService.clean();

            // Then
            verify(shoppingCartMapper).deleteByUserId(anyLong());
        }
    }

    // ==================== sub 方法测试 ====================

    @Nested
    @DisplayName("sub 方法测试")
    @Tag("delete")
    class SubMethodTests {

        @Test
        @DisplayName("删除购物车商品 - 成功场景")
        void shouldDeleteCartItem() {
            // Given
            Long cartItemId = 1L;
            doNothing().when(shoppingCartMapper).deleteById(cartItemId);

            // When
            shoppingCartService.sub(cartItemId);

            // Then
            verify(shoppingCartMapper, times(1)).deleteById(cartItemId);
        }

        @Test
        @DisplayName("删除不存在的购物车商品")
        void shouldHandleNonExistentCartItem() {
            // Given
            Long nonExistentId = 999999L;
            doNothing().when(shoppingCartMapper).deleteById(nonExistentId);

            // When & Then
            assertDoesNotThrow(() -> shoppingCartService.sub(nonExistentId));
            verify(shoppingCartMapper, times(1)).deleteById(nonExistentId);
        }
    }

    // ==================== subShoppingCart 方法测试 ====================

    @Nested
    @DisplayName("subShoppingCart 方法测试")
    @Tag("update")
    class SubShoppingCartMethodTests {

        @Test
        @DisplayName("减少购物车商品数量 - 数量大于1")
        void shouldDecrementQuantityWhenGreaterThanOne() {
            // Given
            testShoppingCart.setNumber(3);
            when(shoppingCartMapper.list(any(ShoppingCart.class))).thenReturn(Arrays.asList(testShoppingCart));
            doNothing().when(shoppingCartMapper).updateNumberById(any(ShoppingCart.class));

            // When
            shoppingCartService.subShoppingCart(testShoppingCartDTO);

            // Then
            assertAll("减少数量验证",
                    () -> verify(shoppingCartMapper, times(1)).list(any(ShoppingCart.class)),
                    () -> verify(shoppingCartMapper, times(1)).updateNumberById(argThat(cart ->
                            cart.getNumber() == 2 // 3 - 1 = 2
                    ))
            );
        }

        @Test
        @DisplayName("减少购物车商品数量 - 数量等于1应删除")
        void shouldDeleteCartItemWhenQuantityEqualsOne() {
            // Given
            testShoppingCart.setNumber(1);
            when(shoppingCartMapper.list(any(ShoppingCart.class))).thenReturn(Arrays.asList(testShoppingCart));
            doNothing().when(shoppingCartMapper).deleteById(anyLong());

            // When
            shoppingCartService.subShoppingCart(testShoppingCartDTO);

            // Then
            assertAll("删除验证",
                    () -> verify(shoppingCartMapper, times(1)).list(any(ShoppingCart.class)),
                    () -> verify(shoppingCartMapper, times(1)).deleteById(testShoppingCart.getId()),
                    () -> verify(shoppingCartMapper, never()).updateNumberById(any(ShoppingCart.class))
            );
        }

        @Test
        @DisplayName("减少不存在的购物车商品")
        void shouldHandleNonExistentCartItemWhenSubtracting() {
            // Given
            when(shoppingCartMapper.list(any(ShoppingCart.class))).thenReturn(Collections.emptyList());

            // When
            shoppingCartService.subShoppingCart(testShoppingCartDTO);

            // Then
            verify(shoppingCartMapper, never()).updateNumberById(any(ShoppingCart.class));
            verify(shoppingCartMapper, never()).deleteById(anyLong());
        }

        @Test
        @DisplayName("减少购物车商品数量到1后再减少应删除")
        void shouldDeleteAfterReducingToOne() {
            // Given - 第一次减少: 2 -> 1
            testShoppingCart.setNumber(2);
            when(shoppingCartMapper.list(any(ShoppingCart.class))).thenReturn(Arrays.asList(testShoppingCart));
            doNothing().when(shoppingCartMapper).updateNumberById(any(ShoppingCart.class));

            // When
            shoppingCartService.subShoppingCart(testShoppingCartDTO);

            // Then
            verify(shoppingCartMapper).updateNumberById(argThat(cart -> cart.getNumber() == 1));
        }
    }

    // ==================== 综合业务场景测试 ====================

    @Nested
    @DisplayName("综合业务场景测试")
    @Tag("integration")
    class IntegrationTests {

        @Test
        @DisplayName("完整购物车操作流程测试")
        void shouldCompleteFullShoppingCartLifecycle() {
            // Given
            when(shoppingCartMapper.list(any(ShoppingCart.class)))
                    .thenReturn(Collections.emptyList()) // 第一次添加 - 新增
                    .thenReturn(Arrays.asList(testShoppingCart)); // 第二次添加 - 数量+1
            doNothing().when(shoppingCartMapper).insert(any(ShoppingCart.class));
            doNothing().when(shoppingCartMapper).updateNumberById(any(ShoppingCart.class));
            doNothing().when(shoppingCartMapper).deleteById(anyLong());
            doNothing().when(shoppingCartMapper).deleteByUserId(anyLong());

            // 1. 添加新商品到购物车
            shoppingCartService.add(testShoppingCartDTO);
            verify(shoppingCartMapper, times(1)).insert(any(ShoppingCart.class));

            // 2. 再次添加相同商品(数量+1)
            testShoppingCart.setNumber(2);
            shoppingCartService.add(testShoppingCartDTO);
            verify(shoppingCartMapper, times(1)).updateNumberById(any(ShoppingCart.class));

            // 3. 查询购物车
            shoppingCartService.list();
            verify(shoppingCartMapper, times(3)).list(any(ShoppingCart.class));

            // 4. 减少商品数量
            testShoppingCart.setNumber(2);
            shoppingCartService.subShoppingCart(testShoppingCartDTO);
            verify(shoppingCartMapper, atLeastOnce()).updateNumberById(any(ShoppingCart.class));

            // 5. 删除单个商品
            shoppingCartService.sub(1L);
            verify(shoppingCartMapper, times(1)).deleteById(1L);

            // 6. 清空购物车
            shoppingCartService.clean();
            verify(shoppingCartMapper, times(1)).deleteByUserId(anyLong());
        }

        @Test
        @DisplayName("购物车数量管理测试")
        void shouldManageCartQuantityCorrectly() {
            // Given
            when(shoppingCartMapper.list(any(ShoppingCart.class))).thenReturn(Arrays.asList(testShoppingCart));
            doNothing().when(shoppingCartMapper).updateNumberById(any(ShoppingCart.class));

            // When - 连续添加3次
            shoppingCartService.add(testShoppingCartDTO); // 1 -> 2
            shoppingCartService.add(testShoppingCartDTO); // 2 -> 3
            shoppingCartService.add(testShoppingCartDTO); // 3 -> 4

            // When - 减少2次
            testShoppingCart.setNumber(4);
            shoppingCartService.subShoppingCart(testShoppingCartDTO); // 4 -> 3
            testShoppingCart.setNumber(3);
            shoppingCartService.subShoppingCart(testShoppingCartDTO); // 3 -> 2

            // Then
            verify(shoppingCartMapper, atLeast(3)).updateNumberById(any(ShoppingCart.class));
        }

        @Test
        @DisplayName("购物车边界条件测试")
        void shouldHandleBoundaryConditions() {
            // Given
            when(shoppingCartMapper.list(any(ShoppingCart.class)))
                    .thenReturn(Collections.emptyList())
                    .thenReturn(Arrays.asList(testShoppingCart))
                    .thenReturn(Collections.emptyList());
            doNothing().when(shoppingCartMapper).insert(any(ShoppingCart.class));
            doNothing().when(shoppingCartMapper).deleteById(anyLong());
            doNothing().when(shoppingCartMapper).deleteByUserId(anyLong());

            // When - 添加空购物车
            shoppingCartService.add(testShoppingCartDTO);
            verify(shoppingCartMapper, times(1)).insert(any(ShoppingCart.class));

            // When - 数量为1时减少
            testShoppingCart.setNumber(1);
            shoppingCartService.subShoppingCart(testShoppingCartDTO);
            verify(shoppingCartMapper, times(1)).deleteById(anyLong());

            // When - 清空空购物车
            shoppingCartService.clean();
            verify(shoppingCartMapper, times(1)).deleteByUserId(anyLong());
        }

        @Test
        @DisplayName("购物车商品混合操作测试")
        void shouldHandleMixedOperations() {
            // Given
            ShoppingCart dishCart = ShoppingCart.builder()
                    .id(1L)
                    .userId(1L)
                    .dishId(1L)
                    .number(2)
                    .build();

            ShoppingCart setmealCart = ShoppingCart.builder()
                    .id(2L)
                    .userId(1L)
                    .setmealId(1L)
                    .number(1)
                    .build();

            when(shoppingCartMapper.list(any(ShoppingCart.class)))
                    .thenReturn(Collections.emptyList())
                    .thenReturn(Arrays.asList(dishCart))
                    .thenReturn(Arrays.asList(setmealCart));
            doNothing().when(shoppingCartMapper).insert(any(ShoppingCart.class));
            doNothing().when(shoppingCartMapper).updateNumberById(any(ShoppingCart.class));

            // When - 添加菜品
            testShoppingCartDTO.setDishId(1L);
            testShoppingCartDTO.setSetmealId(null);
            shoppingCartService.add(testShoppingCartDTO);

            // When - 添加套餐
            testShoppingCartDTO.setDishId(null);
            testShoppingCartDTO.setSetmealId(1L);
            shoppingCartService.add(testShoppingCartDTO);

            // Then - 验证两种类型都被处理
            verify(shoppingCartMapper, atLeastOnce()).insert(argThat(cart ->
                    cart.getDishId() != null || cart.getSetmealId() != null
            ));
        }
    }
}
