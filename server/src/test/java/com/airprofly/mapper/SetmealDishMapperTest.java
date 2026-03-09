package com.airprofly.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.airprofly.entity.SetmealDish;

/**
 * SetmealDishMapper 测试类
 *
 * 测试覆盖:
 * - 批量操作测试
 * - 查询操作测试
 * - 删除操作测试
 * - 边界条件测试
 * - 综合业务流程测试
 *
 * 最佳实践:
 * - 使用 @Nested 组织相关测试
 * - 使用 @Tag 分类测试
 * - 使用 assertAll 确保所有断言都执行
 * - 使用参数化测试减少重复代码
 */
@SpringBootTest
@Transactional
@Tag("mapper")
@Tag("setmeal-dish")
@DisplayName("套餐菜品关系 Mapper 测试")
public class SetmealDishMapperTest {

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    // ==================== 测试数据工厂方法 ====================

    /**
     * 创建标准的测试套餐菜品关系对象
     */
    private SetmealDish createTestSetmealDish(Long setmealId, Long dishId, String name, Integer copies) {
        return SetmealDish.builder()
                .setmealId(setmealId)
                .dishId(dishId)
                .name(name)
                .price(new BigDecimal("28.00"))
                .copies(copies)
                .build();
    }

    /**
     * 创建并插入测试套餐菜品关系,返回插入后的对象(包含生成的ID)
     */
    private List<SetmealDish> insertTestSetmealDishes(Long setmealId) {
        List<SetmealDish> setmealDishes = Arrays.asList(
                createTestSetmealDish(setmealId, 1L, "宫保鸡丁", 1),
                createTestSetmealDish(setmealId, 2L, "鱼香肉丝", 1),
                createTestSetmealDish(setmealId, 3L, "麻婆豆腐", 2)
        );
        setmealDishMapper.insertBatch(setmealDishes);
        return setmealDishes;
    }

    // ==================== getSetmealIdByDishIds 方法测试 ====================

    @Nested
    @DisplayName("getSetmealIdByDishIds 方法测试")
    @Tag("query")
    class GetSetmealIdByDishIdsMethodTests {

        @Test
        @DisplayName("根据菜品ID查询套餐ID - 成功场景")
        void shouldReturnSetmealIdsWhenDishIdsExist() {
            // Given
            Long setmealId = 1L;
            Long dishId1 = 10L;
            Long dishId2 = 11L;
            insertTestSetmealDishes(setmealId);

            // When
            List<Long> setmealIds = setmealDishMapper.getSetmealIdByDishIds(Arrays.asList(dishId1, dishId2));

            // Then
            assertAll("查询验证",
                    () -> assertNotNull(setmealIds, "套餐ID列表不应为null"),
                    () -> assertTrue(setmealIds.size() >= 1, "应至少有1个套餐ID")
            );
        }

        @Test
        @DisplayName("菜品ID不存在时返回空列表")
        void shouldReturnEmptyListWhenDishIdsNotExists() {
            // Given
            List<Long> nonExistentDishIds = Arrays.asList(999999L, 999998L);

            // When
            List<Long> setmealIds = setmealDishMapper.getSetmealIdByDishIds(nonExistentDishIds);

            // Then
            assertAll("空列表验证",
                    () -> assertNotNull(setmealIds, "套餐ID列表不应为null"),
                    () -> assertTrue(setmealIds.isEmpty(), "不存在的菜品ID应返回空列表")
            );
        }

        @Test
        @DisplayName("单个菜品ID查询")
        void shouldReturnSetmealIdsWhenDishIdsContainsSingleId() {
            // Given
            Long setmealId = 2L;
            Long dishId = 20L;
            insertTestSetmealDishes(setmealId);

            // When
            List<Long> setmealIds = setmealDishMapper.getSetmealIdByDishIds(Collections.singletonList(dishId));

            // Then
            assertAll("单个菜品ID查询验证",
                    () -> assertNotNull(setmealIds, "套餐ID列表不应为null"),
                    () -> assertTrue(setmealIds.size() >= 1, "应至少有1个套餐ID")
            );
        }
    }

    // ==================== insertBatch 方法测试 ====================

    @Nested
    @DisplayName("insertBatch 方法测试")
    @Tag("create")
    class InsertBatchMethodTests {

        @Test
        @DisplayName("批量新增套餐菜品关系 - 成功场景")
        void shouldInsertSetmealDishesWhenValidData() {
            // Given
            Long setmealId = 10L;
            List<SetmealDish> setmealDishes = Arrays.asList(
                    createTestSetmealDish(setmealId, 100L, "菜品1", 1),
                    createTestSetmealDish(setmealId, 101L, "菜品2", 2)
            );

            // When
            setmealDishMapper.insertBatch(setmealDishes);

            // Then
            assertAll("批量插入验证",
                    () -> assertNotNull(setmealDishes.get(0).getId(), "插入后第一个关系ID应自动生成"),
                    () -> assertNotNull(setmealDishes.get(1).getId(), "插入后第二个关系ID应自动生成"),
                    () -> {
                        List<SetmealDish> inserted = setmealDishMapper.getDishBySetmealId(setmealId);
                        assertTrue(inserted.size() >= 2, "应能查询到至少2条关系记录");
                    }
            );
        }

        @Test
        @DisplayName("批量新增单条关系")
        void shouldInsertSingleSetmealDish() {
            // Given
            Long setmealId = 11L;
            List<SetmealDish> setmealDishes = Collections.singletonList(
                    createTestSetmealDish(setmealId, 110L, "单独菜品", 1)
            );

            // When
            setmealDishMapper.insertBatch(setmealDishes);

            // Then
            assertAll("单条插入验证",
                    () -> assertNotNull(setmealDishes.get(0).getId(), "插入后ID应自动生成"),
                    () -> {
                        List<SetmealDish> inserted = setmealDishMapper.getDishBySetmealId(setmealId);
                        assertTrue(inserted.size() >= 1, "应能查询到至少1条关系记录");
                    }
            );
        }


        @ParameterizedTest
        @CsvSource({
            "1, 1份",
            "2, 2份",
            "5, 5份"
        })
        @DisplayName("新增不同份数的套餐菜品关系")
        @Tag("parameterized")
        void shouldInsertSetmealDishWithDifferentCopies(Integer copies, String copiesDesc) {
            // Given
            Long setmealId = 13L;
            List<SetmealDish> setmealDishes = Collections.singletonList(
                    createTestSetmealDish(setmealId, 130L, "份数测试菜品", copies)
            );

            // When
            setmealDishMapper.insertBatch(setmealDishes);

            // Then
            List<SetmealDish> inserted = setmealDishMapper.getDishBySetmealId(setmealId);
            assertAll("份数验证",
                    () -> assertFalse(inserted.isEmpty(), "应成功插入"),
                    () -> assertEquals(copies, inserted.get(0).getCopies(), "份数应匹配")
            );
        }
    }

    // ==================== deleteBySetmealId 方法测试 ====================

    @Nested
    @DisplayName("deleteBySetmealId 方法测试")
    @Tag("delete")
    class DeleteBySetmealIdMethodTests {

        @Test
        @DisplayName("根据套餐ID删除菜品关系 - 成功场景")
        void shouldDeleteSetmealDishesWhenSetmealIdExists() {
            // Given
            Long setmealId = 100L;
            insertTestSetmealDishes(setmealId);

            // When
            setmealDishMapper.deleteBySetmealId(setmealId);

            // Then
            List<SetmealDish> deleted = setmealDishMapper.getDishBySetmealId(setmealId);
            assertTrue(deleted.isEmpty(), "删除后应查询不到菜品关系数据");
        }

        @Test
        @DisplayName("删除不存在的套餐ID时不报错")
        @Tag("edge-case")
        void shouldNotThrowExceptionWhenDeletingNonExistentSetmealId() {
            // Given
            Long nonExistentSetmealId = 999999L;

            // When & Then
            assertDoesNotThrow(() -> {
                setmealDishMapper.deleteBySetmealId(nonExistentSetmealId);
            }, "删除不存在的套餐ID时不应抛出异常");
        }

        @ParameterizedTest
        @ValueSource(longs = {999999L, -1L, 0L})
        @DisplayName("删除无效套餐ID时不报错")
        @Tag("edge-case")
        void shouldNotThrowExceptionWhenDeletingInvalidId(Long invalidSetmealId) {
            // When & Then
            assertDoesNotThrow(() -> {
                setmealDishMapper.deleteBySetmealId(invalidSetmealId);
            }, "删除无效套餐ID时不应抛出异常");
        }

        @Test
        @DisplayName("删除后重新插入菜品关系")
        void shouldReinsertSetmealDishesAfterDeletion() {
            // Given
            Long setmealId = 101L;
            insertTestSetmealDishes(setmealId);
            setmealDishMapper.deleteBySetmealId(setmealId);

            // When
            List<SetmealDish> newSetmealDishes = Collections.singletonList(
                    createTestSetmealDish(setmealId, 1000L, "新菜品", 1)
            );
            setmealDishMapper.insertBatch(newSetmealDishes);

            // Then
            List<SetmealDish> reinserted = setmealDishMapper.getDishBySetmealId(setmealId);
            assertAll("重新插入验证",
                    () -> assertFalse(reinserted.isEmpty(), "应能查询到新插入的菜品关系"),
                    () -> assertEquals("新菜品", reinserted.get(0).getName(), "菜品名称应匹配")
            );
        }
    }

    // ==================== getDishBySetmealId 方法测试 ====================

    @Nested
    @DisplayName("getDishBySetmealId 方法测试")
    @Tag("query")
    class GetDishBySetmealIdMethodTests {

        @Test
        @DisplayName("根据套餐ID查询菜品 - 成功场景")
        void shouldReturnDishesWhenSetmealIdExists() {
            // Given
            Long setmealId = 200L;
            insertTestSetmealDishes(setmealId);

            // When
            List<SetmealDish> found = setmealDishMapper.getDishBySetmealId(setmealId);

            // Then
            assertAll("查询验证",
                    () -> assertNotNull(found, "菜品列表不应为null"),
                    () -> assertTrue(found.size() >= 3, "应能查询到至少3条菜品记录"),
                    () -> {
                        boolean hasTargetDish = found.stream()
                                .anyMatch(d -> "宫保鸡丁".equals(d.getName()));
                        assertTrue(hasTargetDish, "应包含'宫保鸡丁'菜品");
                    }
            );
        }

        @Test
        @DisplayName("套餐ID不存在时返回空列表")
        void shouldReturnEmptyListWhenSetmealIdNotExists() {
            // Given
            Long nonExistentSetmealId = 999999L;

            // When
            List<SetmealDish> found = setmealDishMapper.getDishBySetmealId(nonExistentSetmealId);

            // Then
            assertAll("空列表验证",
                    () -> assertNotNull(found, "菜品列表不应为null"),
                    () -> assertTrue(found.isEmpty(), "不存在的套餐ID应返回空列表")
            );
        }

        @ParameterizedTest
        @ValueSource(longs = {999999L, -1L, 0L})
        @DisplayName("无效套餐ID时返回空列表")
        @Tag("edge-case")
        void shouldReturnEmptyListWhenSetmealIdIsInvalid(Long invalidSetmealId) {
            // When
            List<SetmealDish> found = setmealDishMapper.getDishBySetmealId(invalidSetmealId);

            // Then
            assertAll("无效ID验证",
                    () -> assertNotNull(found, "菜品列表不应为null"),
                    () -> assertTrue(found.isEmpty(), "无效套餐ID应返回空列表")
            );
        }

        @Test
        @DisplayName("验证返回数据完整性")
        void shouldReturnCompleteSetmealDishData() {
            // Given
            Long setmealId = 201L;
            insertTestSetmealDishes(setmealId);

            // When
            List<SetmealDish> dishes = setmealDishMapper.getDishBySetmealId(setmealId);

            // Then
            if (!dishes.isEmpty()) {
                SetmealDish firstDish = dishes.get(0);
                assertAll("菜品数据完整性验证",
                        () -> assertNotNull(firstDish.getId(), "关系ID不应为null"),
                        () -> assertNotNull(firstDish.getSetmealId(), "套餐ID不应为null"),
                        () -> assertNotNull(firstDish.getDishId(), "菜品ID不应为null"),
                        () -> assertNotNull(firstDish.getName(), "菜品名称不应为null"),
                        () -> assertNotNull(firstDish.getPrice(), "价格不应为null"),
                        () -> assertNotNull(firstDish.getCopies(), "份数不应为null")
                );
            }
        }

        @Test
        @DisplayName("验证份数字段的正确性")
        void shouldReturnDishWithCorrectCopies() {
            // Given
            Long setmealId = 202L;
            Integer expectedCopies = 3;
            List<SetmealDish> setmealDishes = Collections.singletonList(
                    createTestSetmealDish(setmealId, 2000L, "多份菜品", expectedCopies)
            );
            setmealDishMapper.insertBatch(setmealDishes);

            // When
            List<SetmealDish> found = setmealDishMapper.getDishBySetmealId(setmealId);

            // Then
            assertAll("份数验证",
                    () -> assertFalse(found.isEmpty(), "应能查询到菜品"),
                    () -> assertEquals(expectedCopies, found.get(0).getCopies(), "份数应匹配")
            );
        }

        @Test
        @DisplayName("验证价格字段的正确性")
        void shouldReturnDishWithCorrectPrice() {
            // Given
            Long setmealId = 203L;
            BigDecimal expectedPrice = new BigDecimal("38.50");
            SetmealDish setmealDish = createTestSetmealDish(setmealId, 2001L, "特定价格菜品", 1);
            setmealDish.setPrice(expectedPrice);
            setmealDishMapper.insertBatch(Collections.singletonList(setmealDish));

            // When
            List<SetmealDish> found = setmealDishMapper.getDishBySetmealId(setmealId);

            // Then
            assertAll("价格验证",
                    () -> assertFalse(found.isEmpty(), "应能查询到菜品"),
                    () -> assertEquals(expectedPrice, found.get(0).getPrice(), "价格应匹配")
            );
        }
    }

    // ==================== 综合场景测试 ====================

    @Nested
    @DisplayName("综合业务流程测试")
    @Tag("integration")
    class IntegrationTests {

        @Test
        @DisplayName("完整CRUD流程测试")
        void shouldCompleteFullCRUDProcess() {
            // 1. Create - 批量新增套餐菜品关系
            Long setmealId = 300L;
            List<SetmealDish> setmealDishes = insertTestSetmealDishes(setmealId);
            assertAll("新增验证",
                    () -> assertNotNull(setmealDishes.get(0).getId(), "新增后ID应自动生成"),
                    () -> assertNotNull(setmealDishes.get(1).getId(), "新增后ID应自动生成"),
                    () -> assertNotNull(setmealDishes.get(2).getId(), "新增后ID应自动生成")
            );

            // 2. Read - 查询菜品
            List<SetmealDish> found = setmealDishMapper.getDishBySetmealId(setmealId);
            assertTrue(found.size() >= 3, "应能查询到至少3条菜品记录");

            // 3. Delete - 删除菜品关系
            setmealDishMapper.deleteBySetmealId(setmealId);
            List<SetmealDish> deleted = setmealDishMapper.getDishBySetmealId(setmealId);
            assertTrue(deleted.isEmpty(), "删除后应查询不到数据");
        }

        @Test
        @DisplayName("套餐菜品管理流程测试")
        void shouldManageSetmealDishesCorrectly() {
            // Given - 新增套餐的菜品
            Long setmealId = 301L;
            insertTestSetmealDishes(setmealId);

            // When - 查询初始菜品
            List<SetmealDish> initialDishes = setmealDishMapper.getDishBySetmealId(setmealId);
            assertTrue(initialDishes.size() >= 3, "初始应有至少3个菜品");

            // When - 删除所有菜品
            setmealDishMapper.deleteBySetmealId(setmealId);
            List<SetmealDish> afterDeletion = setmealDishMapper.getDishBySetmealId(setmealId);
            assertTrue(afterDeletion.isEmpty(), "删除后应无菜品");

            // When - 重新插入新菜品
            List<SetmealDish> newDishes = Collections.singletonList(
                    createTestSetmealDish(setmealId, 3000L, "新菜品", 2)
            );
            setmealDishMapper.insertBatch(newDishes);

            // Then - 验证新菜品
            List<SetmealDish> finalDishes = setmealDishMapper.getDishBySetmealId(setmealId);
            assertAll("新菜品验证",
                    () -> assertEquals(1, finalDishes.size(), "应有1个新菜品"),
                    () -> assertEquals("新菜品", finalDishes.get(0).getName(), "菜品名称应匹配"),
                    () -> assertEquals(2, finalDishes.get(0).getCopies(), "份数应匹配")
            );
        }

        @Test
        @DisplayName("多套餐菜品管理流程测试")
        void shouldManageMultipleSetmealDishesCorrectly() {
            // Given - 为多个套餐添加菜品
            Long setmealId1 = 302L;
            Long setmealId2 = 303L;
            insertTestSetmealDishes(setmealId1);
            insertTestSetmealDishes(setmealId2);

            // When - 验证每个套餐都有菜品
            List<SetmealDish> dishes1 = setmealDishMapper.getDishBySetmealId(setmealId1);
            List<SetmealDish> dishes2 = setmealDishMapper.getDishBySetmealId(setmealId2);

            assertAll("批量查询验证",
                    () -> assertTrue(dishes1.size() >= 3, "套餐1应有至少3个菜品"),
                    () -> assertTrue(dishes2.size() >= 3, "套餐2应有至少3个菜品")
            );

            // When - 删除套餐1的菜品
            setmealDishMapper.deleteBySetmealId(setmealId1);

            // Then - 验证套餐1的菜品被删除,套餐2的菜品仍存在
            assertAll("选择性删除验证",
                    () -> assertTrue(setmealDishMapper.getDishBySetmealId(setmealId1).isEmpty(),
                            "套餐1的菜品应被删除"),
                    () -> assertTrue(setmealDishMapper.getDishBySetmealId(setmealId2).size() >= 3,
                            "套餐2的菜品应仍存在")
            );
        }

        @Test
        @DisplayName("菜品反向查询套餐流程测试")
        void shouldQuerySetmealByDishIdsCorrectly() {
            // Given - 为套餐添加菜品
            Long setmealId = 304L;
            Long dishId1 = 3040L;
            Long dishId2 = 3041L;
            List<SetmealDish> setmealDishes = Arrays.asList(
                    createTestSetmealDish(setmealId, dishId1, "菜品A", 1),
                    createTestSetmealDish(setmealId, dishId2, "菜品B", 1)
            );
            setmealDishMapper.insertBatch(setmealDishes);

            // When - 根据菜品ID查询套餐ID
            List<Long> setmealIds = setmealDishMapper.getSetmealIdByDishIds(Arrays.asList(dishId1, dishId2));

            // Then - 验证查询结果
            assertAll("反向查询验证",
                    () -> assertNotNull(setmealIds, "套餐ID列表不应为null"),
                    () -> assertTrue(setmealIds.contains(setmealId), "应包含指定的套餐ID")
            );
        }
    }
}
