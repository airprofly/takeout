package com.airprofly.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.airprofly.entity.ShoppingCart;

/**
 * ShoppingCartMapper 测试类
 *
 * 测试覆盖:
 * - CRUD 操作测试
 * - 查询条件测试
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
@Tag("shopping-cart")
@DisplayName("购物车 Mapper 测试")
public class ShoppingCartMapperTest {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    // ==================== 测试数据工厂方法 ====================

    /**
     * 创建标准的测试购物车对象(菜品)
     */
    private ShoppingCart createTestDishCart(Long userId) {
        return ShoppingCart.builder()
                .userId(userId)
                .name("测试菜品")
                .dishId(1L)
                .dishFlavor("微辣")
                .number(1)
                .amount(new BigDecimal("28.00"))
                .image("dish_image.jpg")
                .createTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建标准的测试购物车对象(套餐)
     */
    private ShoppingCart createTestSetmealCart(Long userId) {
        return ShoppingCart.builder()
                .userId(userId)
                .name("测试套餐")
                .setmealId(1L)
                .number(1)
                .amount(new BigDecimal("58.00"))
                .image("setmeal_image.jpg")
                .createTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建并插入测试购物车项,返回插入后的对象(包含生成的ID)
     */
    private ShoppingCart insertTestDishCart(Long userId) {
        ShoppingCart shoppingCart = createTestDishCart(userId);
        shoppingCartMapper.insert(shoppingCart);
        return shoppingCart;
    }

    /**
     * 创建并插入测试购物车项(套餐),返回插入后的对象(包含生成的ID)
     */
    private ShoppingCart insertTestSetmealCart(Long userId) {
        ShoppingCart shoppingCart = createTestSetmealCart(userId);
        shoppingCartMapper.insert(shoppingCart);
        return shoppingCart;
    }

    // ==================== list 方法测试 ====================

    @Nested
    @DisplayName("list 方法测试")
    @Tag("query")
    class ListMethodTests {

        @Test
        @DisplayName("根据用户ID查询购物车列表 - 成功场景")
        void shouldReturnShoppingCartListWhenUserIdExists() {
            // Given
            ShoppingCart query = ShoppingCart.builder()
                    .userId(1L)
                    .build();

            // When
            List<ShoppingCart> shoppingCarts = shoppingCartMapper.list(query);

            // Then
            assertNotNull(shoppingCarts, "购物车列表不应为null");
        }

        @Test
        @DisplayName("用户不存在时返回空列表")
        void shouldReturnEmptyListWhenUserIdNotExists() {
            // Given
            ShoppingCart query = ShoppingCart.builder()
                    .userId(999999L)
                    .build();

            // When
            List<ShoppingCart> shoppingCarts = shoppingCartMapper.list(query);

            // Then
            assertAll("空列表验证",
                    () -> assertNotNull(shoppingCarts, "购物车列表不应为null"),
                    () -> assertTrue(shoppingCarts.isEmpty(), "不存在的用户ID应返回空列表"));
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("查询参数为null时返回空列表")
        @Tag("edge-case")
        void shouldReturnEmptyListWhenQueryIsNull(ShoppingCart query) {
            // When
            List<ShoppingCart> shoppingCarts = shoppingCartMapper.list(query);

            // Then
            assertNotNull(shoppingCarts, "购物车列表不应为null");
        }

        @Test
        @DisplayName("根据菜品ID查询购物车列表")
        void shouldReturnShoppingCartListWhenDishIdExists() {
            // Given
            Long userId = 8888L;
            insertTestDishCart(userId);
            ShoppingCart query = ShoppingCart.builder()
                    .userId(userId)
                    .dishId(1L)
                    .build();

            // When
            List<ShoppingCart> shoppingCarts = shoppingCartMapper.list(query);

            // Then
            assertAll("菜品查询验证",
                    () -> assertNotNull(shoppingCarts, "购物车列表不应为null"),
                    () -> assertFalse(shoppingCarts.isEmpty(), "应查询到菜品"));
        }

        @Test
        @DisplayName("根据套餐ID查询购物车列表")
        void shouldReturnShoppingCartListWhenSetmealIdExists() {
            // Given
            Long userId = 8889L;
            insertTestSetmealCart(userId);
            ShoppingCart query = ShoppingCart.builder()
                    .userId(userId)
                    .setmealId(1L)
                    .build();

            // When
            List<ShoppingCart> shoppingCarts = shoppingCartMapper.list(query);

            // Then
            assertAll("套餐查询验证",
                    () -> assertNotNull(shoppingCarts, "购物车列表不应为null"),
                    () -> assertFalse(shoppingCarts.isEmpty(), "应查询到套餐"));
        }

        @Test
        @DisplayName("根据菜品ID和口味查询购物车")
        void shouldReturnShoppingCartWhenDishIdAndFlavorMatch() {
            // Given
            Long userId = 8890L;
            insertTestDishCart(userId);
            ShoppingCart query = ShoppingCart.builder()
                    .userId(userId)
                    .dishId(1L)
                    .dishFlavor("微辣")
                    .build();

            // When
            List<ShoppingCart> shoppingCarts = shoppingCartMapper.list(query);

            // Then
            assertAll("菜品口味查询验证",
                    () -> assertNotNull(shoppingCarts, "购物车列表不应为null"),
                    () -> assertFalse(shoppingCarts.isEmpty(), "应查询到指定口味的菜品"));
        }

        @Test
        @DisplayName("验证返回数据完整性")
        void shouldReturnCompleteShoppingCartData() {
            // Given
            Long userId = 8891L;
            insertTestDishCart(userId);
            ShoppingCart query = ShoppingCart.builder()
                    .userId(userId)
                    .build();

            // When
            List<ShoppingCart> shoppingCarts = shoppingCartMapper.list(query);

            // Then
            if (!shoppingCarts.isEmpty()) {
                ShoppingCart firstCart = shoppingCarts.get(0);
                assertAll("购物车数据完整性验证",
                        () -> assertNotNull(firstCart.getId(), "购物车ID不应为null"),
                        () -> assertNotNull(firstCart.getUserId(), "用户ID不应为null"),
                        () -> assertNotNull(firstCart.getName(), "商品名称不应为null"),
                        () -> assertNotNull(firstCart.getNumber(), "数量不应为null"),
                        () -> assertNotNull(firstCart.getAmount(), "金额不应为null"),
                        () -> assertNotNull(firstCart.getImage(), "图片不应为null"),
                        () -> assertNotNull(firstCart.getCreateTime(), "创建时间不应为null"));
            }
        }
    }

    // ==================== insert 方法测试 ====================

    @Nested
    @DisplayName("insert 方法测试")
    @Tag("create")
    class InsertMethodTests {

        @Test
        @DisplayName("新增菜品购物车 - 成功场景")
        void shouldInsertDishCartWhenValidData() {
            // Given
            ShoppingCart shoppingCart = createTestDishCart(1L);

            // When
            shoppingCartMapper.insert(shoppingCart);

            // Then
            assertAll("插入验证",
                    () -> assertNotNull(shoppingCart.getId(), "插入后ID应自动生成"),
                    () -> {
                        List<ShoppingCart> carts = shoppingCartMapper.list(
                                ShoppingCart.builder().userId(1L).build());
                        assertFalse(carts.isEmpty(), "插入的数据应能被查询到");
                    });
        }

        @Test
        @DisplayName("新增套餐购物车 - 成功场景")
        void shouldInsertSetmealCartWhenValidData() {
            // Given
            ShoppingCart shoppingCart = createTestSetmealCart(1L);

            // When
            shoppingCartMapper.insert(shoppingCart);

            // Then
            assertAll("插入验证",
                    () -> assertNotNull(shoppingCart.getId(), "插入后ID应自动生成"),
                    () -> {
                        List<ShoppingCart> carts = shoppingCartMapper.list(
                                ShoppingCart.builder().userId(1L).build());
                        assertFalse(carts.isEmpty(), "插入的数据应能被查询到");
                    });
        }

        @Test
        @DisplayName("可选字段为null时可成功新增")
        @Tag("edge-case")
        void shouldInsertWhenOptionalFieldsAreNull() {
            // Given
            ShoppingCart shoppingCart = ShoppingCart.builder()
                    .userId(1L)
                    .name(null)
                    .dishFlavor(null)
                    .image(null)
                    .number(1)
                    .amount(new BigDecimal("28.00"))
                    .build();

            // When
            shoppingCartMapper.insert(shoppingCart);

            // Then
            assertAll("空字段插入验证",
                    () -> assertNotNull(shoppingCart.getId(), "插入后ID应自动生成"),
                    () -> {
                        List<ShoppingCart> carts = shoppingCartMapper.list(
                                ShoppingCart.builder().userId(1L).build());
                        assertFalse(carts.isEmpty(), "应能查询到插入数据");
                    });
        }

        @ParameterizedTest
        @CsvSource({
                "1, 微辣",
                "2, 中辣",
                "3, 特辣"
        })
        @DisplayName("新增不同口味的菜品购物车")
        @Tag("parameterized")
        void shouldInsertDishCartWithDifferentFlavors(int number, String flavor) {
            // Given
            ShoppingCart shoppingCart = createTestDishCart(1L);
            shoppingCart.setDishFlavor(flavor);

            // When
            shoppingCartMapper.insert(shoppingCart);

            // Then
            List<ShoppingCart> carts = shoppingCartMapper.list(
                    ShoppingCart.builder()
                            .userId(1L)
                            .dishId(1L)
                            .dishFlavor(flavor)
                            .build());
            assertAll("口味验证",
                    () -> assertFalse(carts.isEmpty(), "应成功插入"),
                    () -> assertEquals(flavor, carts.get(0).getDishFlavor(), "口味应匹配"));
        }
    }

    // ==================== updateNumberById 方法测试 ====================

    @Nested
    @DisplayName("updateNumberById 方法测试")
    @Tag("update")
    class UpdateNumberByIdMethodTests {

        @Test
        @DisplayName("更新购物车商品数量 - 成功场景")
        void shouldUpdateNumberWhenIdExists() {
            // Given
            ShoppingCart shoppingCart = insertTestDishCart(1L);
            Long cartId = shoppingCart.getId();

            // When
            ShoppingCart updateCart = ShoppingCart.builder()
                    .id(cartId)
                    .number(5)
                    .build();
            shoppingCartMapper.updateNumberById(updateCart);

            // Then
            List<ShoppingCart> carts = shoppingCartMapper.list(
                    ShoppingCart.builder().userId(1L).build());
            ShoppingCart updated = carts.stream()
                    .filter(c -> c.getId().equals(cartId))
                    .findFirst()
                    .orElse(null);
            assertAll("更新验证",
                    () -> assertNotNull(updated, "应能查询到更新的购物车项"),
                    () -> assertEquals(5, updated.getNumber(), "数量应被更新"));
        }

        @Test
        @DisplayName("更新为0数量(清空)")
        void shouldUpdateToZeroWhenNumberIsZero() {
            // Given
            ShoppingCart shoppingCart = insertTestDishCart(1L);
            Long cartId = shoppingCart.getId();

            // When
            ShoppingCart updateCart = ShoppingCart.builder()
                    .id(cartId)
                    .number(0)
                    .build();
            shoppingCartMapper.updateNumberById(updateCart);

            // Then
            List<ShoppingCart> carts = shoppingCartMapper.list(
                    ShoppingCart.builder().userId(1L).build());
            ShoppingCart updated = carts.stream()
                    .filter(c -> c.getId().equals(cartId))
                    .findFirst()
                    .orElse(null);
            if (updated != null) {
                assertEquals(0, updated.getNumber(), "数量应被更新为0");
            }
        }

        @Test
        @DisplayName("更新不存在的ID时不报错")
        @Tag("edge-case")
        void shouldNotThrowExceptionWhenUpdatingNonExistentId() {
            // Given
            ShoppingCart updateCart = ShoppingCart.builder()
                    .id(999999L)
                    .number(10)
                    .build();

            // When & Then
            assertDoesNotThrow(() -> {
                shoppingCartMapper.updateNumberById(updateCart);
            }, "更新不存在的ID时不应抛出异常");
        }

        @ParameterizedTest
        @ValueSource(ints = { 1, 5, 10, 99 })
        @DisplayName("更新不同数量的商品")
        @Tag("parameterized")
        void shouldUpdateNumberWithDifferentValues(int number) {
            // Given
            ShoppingCart shoppingCart = insertTestDishCart(1L);
            Long cartId = shoppingCart.getId();

            // When
            ShoppingCart updateCart = ShoppingCart.builder()
                    .id(cartId)
                    .number(number)
                    .build();
            shoppingCartMapper.updateNumberById(updateCart);

            // Then
            List<ShoppingCart> carts = shoppingCartMapper.list(
                    ShoppingCart.builder().userId(1L).build());
            ShoppingCart updated = carts.stream()
                    .filter(c -> c.getId().equals(cartId))
                    .findFirst()
                    .orElse(null);
            if (updated != null) {
                assertEquals(number, updated.getNumber(), "数量应被更新");
            }
        }
    }

    // ==================== deleteByUserId 方法测试 ====================

    @Nested
    @DisplayName("deleteByUserId 方法测试")
    @Tag("delete")
    class DeleteByUserIdMethodTests {

        @Test
        @DisplayName("根据用户ID清空购物车 - 成功场景")
        void shouldDeleteAllCartsWhenUserIdExists() {
            // Given
            Long userId = 9001L;
            insertTestDishCart(userId);
            insertTestSetmealCart(userId);

            // When
            shoppingCartMapper.deleteByUserId(userId);

            // Then
            List<ShoppingCart> carts = shoppingCartMapper.list(
                    ShoppingCart.builder().userId(userId).build());
            assertTrue(carts.isEmpty(), "购物车应被清空");
        }

        @Test
        @DisplayName("清空空购物车不报错")
        @Tag("edge-case")
        void shouldNotThrowExceptionWhenDeletingEmptyCart() {
            // Given
            Long userId = 9002L;

            // When & Then
            assertDoesNotThrow(() -> {
                shoppingCartMapper.deleteByUserId(userId);
            }, "清空空购物车时不应抛出异常");
        }

        @Test
        @DisplayName("清空购物车不影响其他用户")
        void shouldNotDeleteOtherUsersCarts() {
            // Given
            Long user1 = 9003L;
            Long user2 = 9004L;
            insertTestDishCart(user1);
            insertTestDishCart(user2);

            // When - 清空用户1的购物车
            shoppingCartMapper.deleteByUserId(user1);

            // Then - 用户2的购物车应该还在
            List<ShoppingCart> user2Carts = shoppingCartMapper.list(
                    ShoppingCart.builder().userId(user2).build());
            assertFalse(user2Carts.isEmpty(), "用户2的购物车不应被清空");
        }
    }

    // ==================== deleteById 方法测试 ====================

    @Nested
    @DisplayName("deleteById 方法测试")
    @Tag("delete")
    class DeleteByIdMethodTests {

        @Test
        @DisplayName("根据ID删除购物车商品 - 成功场景")
        void shouldDeleteCartWhenIdExists() {
            // Given
            ShoppingCart shoppingCart = insertTestDishCart(1L);
            Long cartId = shoppingCart.getId();

            // When
            shoppingCartMapper.deleteById(cartId);

            // Then
            List<ShoppingCart> carts = shoppingCartMapper.list(
                    ShoppingCart.builder().userId(1L).build());
            boolean deleted = carts.stream().noneMatch(c -> c.getId().equals(cartId));
            assertTrue(deleted, "购物车项应被删除");
        }

        @ParameterizedTest
        @ValueSource(longs = { 999999L, -1L, 0L })
        @DisplayName("删除不存在的ID时不报错")
        @Tag("edge-case")
        void shouldNotThrowExceptionWhenDeletingNonExistentId(Long nonExistentId) {
            // When & Then
            assertDoesNotThrow(() -> {
                shoppingCartMapper.deleteById(nonExistentId);
            }, "删除不存在的ID时不应抛出异常");
        }

        @Test
        @DisplayName("删除单个商品不影响其他商品")
        void shouldNotDeleteOtherCartsWhenDeletingOne() {
            // Given
            Long userId = 9005L;
            ShoppingCart cart1 = insertTestDishCart(userId);
            ShoppingCart cart2 = insertTestSetmealCart(userId);

            // When - 删除cart1
            shoppingCartMapper.deleteById(cart1.getId());

            // Then - cart2应该还在
            List<ShoppingCart> carts = shoppingCartMapper.list(
                    ShoppingCart.builder().userId(userId).build());
            boolean cart2Exists = carts.stream().anyMatch(c -> c.getId().equals(cart2.getId()));
            assertTrue(cart2Exists, "其他购物车项不应被删除");
        }
    }

    // ==================== insertBatch 方法测试 ====================

    @Nested
    @DisplayName("insertBatch 方法测试")
    @Tag("create")
    class InsertBatchMethodTests {

        @Test
        @DisplayName("批量插入购物车商品 - 成功场景")
        void shouldInsertBatchWhenValidData() {
            // Given
            Long userId = 9006L;
            List<ShoppingCart> cartList = new ArrayList<>();
            for (int i = 1; i <= 3; i++) {
                ShoppingCart cart = createTestDishCart(userId);
                cart.setName("测试菜品" + i);
                cart.setDishId((long) i);
                cartList.add(cart);
            }

            // When
            shoppingCartMapper.insertBatch(cartList);

            // Then
            assertAll("批量插入验证",
                    () -> cartList.forEach(cart -> {
                        assertNotNull(cart.getId(), "批量插入后ID应自动生成");
                    }),
                    () -> {
                        List<ShoppingCart> carts = shoppingCartMapper.list(
                                ShoppingCart.builder().userId(userId).build());
                        assertTrue(carts.size() >= 3, "批量插入的数据应能被查询到");
                    });
        }

        @Test
        @DisplayName("批量插入混合商品(菜品和套餐)")
        void shouldInsertBatchWithMixedDishAndSetmeal() {
            // Given
            Long userId = 9007L;
            List<ShoppingCart> cartList = new ArrayList<>();

            ShoppingCart dishCart = createTestDishCart(userId);
            cartList.add(dishCart);

            ShoppingCart setmealCart = createTestSetmealCart(userId);
            cartList.add(setmealCart);

            // When
            shoppingCartMapper.insertBatch(cartList);

            // Then
            List<ShoppingCart> carts = shoppingCartMapper.list(
                    ShoppingCart.builder().userId(userId).build());
            assertAll("混合商品验证",
                    () -> assertTrue(carts.size() >= 2, "应插入2个商品"),
                    () -> {
                        boolean hasDish = carts.stream().anyMatch(c -> c.getDishId() != null);
                        boolean hasSetmeal = carts.stream().anyMatch(c -> c.getSetmealId() != null);
                        assertTrue(hasDish, "应包含菜品");
                        assertTrue(hasSetmeal, "应包含套餐");
                    });
        }

        @ParameterizedTest
        @ValueSource(ints = { 1, 5, 10 })
        @DisplayName("批量插入不同数量的商品")
        @Tag("parameterized")
        void shouldInsertBatchWithDifferentSizes(int size) {
            // Given
            Long userId = 9008L;
            List<ShoppingCart> cartList = new ArrayList<>();
            for (int i = 1; i <= size; i++) {
                ShoppingCart cart = createTestDishCart(userId);
                cart.setName("测试菜品" + i);
                cart.setDishId((long) i);
                cartList.add(cart);
            }

            // When
            shoppingCartMapper.insertBatch(cartList);

            // Then
            assertAll("数量验证",
                    () -> cartList.forEach(cart -> {
                        assertNotNull(cart.getId(), "ID应自动生成");
                    }),
                    () -> assertEquals(size, cartList.size(), "列表大小应匹配"));
        }
    }

    // ==================== 综合场景测试 ====================

    @Nested
    @DisplayName("综合业务流程测试")
    @Tag("integration")
    class IntegrationTests {

        @Test
        @DisplayName("完整购物车流程测试(菜品)")
        void shouldCompleteFullShoppingCartProcessForDish() {
            // 1. Create - 添加菜品到购物车
            ShoppingCart cart = insertTestDishCart(1L);
            assertAll("新增验证",
                    () -> assertNotNull(cart.getId(), "新增后ID应自动生成"));

            // 2. Read - 查询购物车
            List<ShoppingCart> carts = shoppingCartMapper.list(
                    ShoppingCart.builder().userId(1L).build());
            assertFalse(carts.isEmpty(), "应能查询到新增的菜品");

            // 3. Update - 更新数量
            ShoppingCart updateCart = ShoppingCart.builder()
                    .id(cart.getId())
                    .number(3)
                    .build();
            shoppingCartMapper.updateNumberById(updateCart);
            carts = shoppingCartMapper.list(
                    ShoppingCart.builder().userId(1L).build());
            ShoppingCart updated = carts.stream()
                    .filter(c -> c.getId().equals(cart.getId()))
                    .findFirst()
                    .orElse(null);
            if (updated != null) {
                assertEquals(3, updated.getNumber(), "数量应被更新");
            }

            // 4. Delete - 删除购物车商品
            shoppingCartMapper.deleteById(cart.getId());
            carts = shoppingCartMapper.list(
                    ShoppingCart.builder().userId(1L).build());
            boolean deleted = carts.stream().noneMatch(c -> c.getId().equals(cart.getId()));
            assertTrue(deleted, "删除后应查询不到数据");
        }

        @Test
        @DisplayName("完整购物车流程测试(套餐)")
        void shouldCompleteFullShoppingCartProcessForSetmeal() {
            // 1. Create - 添加套餐到购物车
            ShoppingCart cart = insertTestSetmealCart(1L);
            assertAll("新增验证",
                    () -> assertNotNull(cart.getId(), "新增后ID应自动生成"));

            // 2. Read - 查询购物车
            List<ShoppingCart> carts = shoppingCartMapper.list(
                    ShoppingCart.builder().userId(1L).build());
            assertFalse(carts.isEmpty(), "应能查询到新增的套餐");

            // 3. Update - 更新数量
            ShoppingCart updateCart = ShoppingCart.builder()
                    .id(cart.getId())
                    .number(2)
                    .build();
            shoppingCartMapper.updateNumberById(updateCart);
            carts = shoppingCartMapper.list(
                    ShoppingCart.builder().userId(1L).build());
            ShoppingCart updated = carts.stream()
                    .filter(c -> c.getId().equals(cart.getId()))
                    .findFirst()
                    .orElse(null);
            if (updated != null) {
                assertEquals(2, updated.getNumber(), "数量应被更新");
            }

            // 4. Delete - 删除购物车商品
            shoppingCartMapper.deleteById(cart.getId());
            carts = shoppingCartMapper.list(
                    ShoppingCart.builder().userId(1L).build());
            boolean deleted = carts.stream().noneMatch(c -> c.getId().equals(cart.getId()));
            assertTrue(deleted, "删除后应查询不到数据");
        }

        @Test
        @DisplayName("清空购物车流程测试")
        void shouldClearCartCorrectly() {
            // Given - 为用户添加多个商品
            Long userId = 9009L;
            insertTestDishCart(userId);
            insertTestSetmealCart(userId);
            insertTestDishCart(userId);

            // When - 清空购物车
            shoppingCartMapper.deleteByUserId(userId);

            // Then - 验证购物车已清空
            List<ShoppingCart> carts = shoppingCartMapper.list(
                    ShoppingCart.builder().userId(userId).build());
            assertTrue(carts.isEmpty(), "购物车应被完全清空");
        }

        @Test
        @DisplayName("批量添加商品流程测试")
        void shouldBatchAddItemsCorrectly() {
            // Given
            Long userId = 9010L;
            List<ShoppingCart> cartList = new ArrayList<>();
            for (int i = 1; i <= 5; i++) {
                ShoppingCart cart = createTestDishCart(userId);
                cart.setName("批量菜品" + i);
                cart.setDishId((long) i);
                cart.setNumber(i);
                cartList.add(cart);
            }

            // When - 批量插入
            shoppingCartMapper.insertBatch(cartList);

            // Then - 验证所有商品都已添加
            List<ShoppingCart> carts = shoppingCartMapper.list(
                    ShoppingCart.builder().userId(userId).build());
            assertTrue(carts.size() >= 5, "应添加5个商品");

            // 清空购物车
            shoppingCartMapper.deleteByUserId(userId);
            carts = shoppingCartMapper.list(
                    ShoppingCart.builder().userId(userId).build());
            assertTrue(carts.isEmpty(), "批量添加后应能清空");
        }

        @Test
        @DisplayName("同菜品不同口味共存测试")
        void shouldAllowSameDishWithDifferentFlavors() {
            // Given
            Long userId = 9011L;

            // When - 添加同一菜品的不同口味
            ShoppingCart cart1 = createTestDishCart(userId);
            cart1.setDishFlavor("微辣");
            shoppingCartMapper.insert(cart1);

            ShoppingCart cart2 = createTestDishCart(userId);
            cart2.setDishFlavor("中辣");
            shoppingCartMapper.insert(cart2);

            ShoppingCart cart3 = createTestDishCart(userId);
            cart3.setDishFlavor("特辣");
            shoppingCartMapper.insert(cart3);

            // Then - 验证不同口味的菜品都能共存
            List<ShoppingCart> carts = shoppingCartMapper.list(
                    ShoppingCart.builder()
                            .userId(userId)
                            .dishId(1L)
                            .build());
            assertTrue(carts.size() >= 3, "同一菜品的不同口味应能共存");

            // 验证口味不同
            List<String> flavors = carts.stream()
                    .map(ShoppingCart::getDishFlavor)
                    .distinct()
                    .toList();
            assertTrue(flavors.size() >= 3, "应有3种不同口味");
        }
    }
}
