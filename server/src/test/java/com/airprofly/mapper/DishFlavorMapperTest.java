package com.airprofly.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.airprofly.entity.DishFlavor;

/**
 * DishFlavorMapper 测试类
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
@Tag("dish-flavor")
@DisplayName("菜品口味 Mapper 测试")
public class DishFlavorMapperTest {

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    // ==================== 测试数据工厂方法 ====================

    /**
     * 创建标准的测试菜品口味对象
     */
    private DishFlavor createTestDishFlavor(Long dishId, String name, String value) {
        return DishFlavor.builder()
                .dishId(dishId)
                .name(name)
                .value(value)
                .build();
    }

    /**
     * 创建并插入测试菜品口味,返回插入后的对象(包含生成的ID)
     */
    private List<DishFlavor> insertTestDishFlavors(Long dishId) {
        List<DishFlavor> flavors = Arrays.asList(
                createTestDishFlavor(dishId, "辣度", "[\"不辣\",\"微辣\",\"中辣\",\"特辣\"]"),
                createTestDishFlavor(dishId, "温度", "[\"热\",\"冷\"]"),
                createTestDishFlavor(dishId, "份量", "[\"小份\",\"中份\",\"大份\"]")
        );
        dishFlavorMapper.insertBatch(flavors);
        return flavors;
    }

    // ==================== insertBatch 方法测试 ====================

    @Nested
    @DisplayName("insertBatch 方法测试")
    @Tag("create")
    class InsertBatchMethodTests {

        @Test
        @DisplayName("批量新增菜品口味 - 成功场景")
        void shouldInsertFlavorsWhenValidData() {
            // Given
            Long dishId = 1L;
            List<DishFlavor> flavors = Arrays.asList(
                    createTestDishFlavor(dishId, "甜度", "[\"无糖\",\"少糖\",\"正常\"]"),
                    createTestDishFlavor(dishId, "配料", "[\"加葱\",\"不加葱\"]")
            );

            // When
            dishFlavorMapper.insertBatch(flavors);

            // Then
            assertAll("批量插入验证",
                    () -> assertNotNull(flavors.get(0).getId(), "插入后第一个口味ID应自动生成"),
                    () -> assertNotNull(flavors.get(1).getId(), "插入后第二个口味ID应自动生成"),
                    () -> {
                        List<DishFlavor> inserted = dishFlavorMapper.getByDishId(dishId);
                        assertTrue(inserted.size() >= 2, "应能查询到至少2条口味记录");
                    }
            );
        }

        @Test
        @DisplayName("批量新增单条口味")
        void shouldInsertSingleFlavor() {
            // Given
            Long dishId = 2L;
            List<DishFlavor> flavors = Collections.singletonList(
                    createTestDishFlavor(dishId, "口味", "[\"清淡\",\"浓郁\"]")
            );

            // When
            dishFlavorMapper.insertBatch(flavors);

            // Then
            assertAll("单条插入验证",
                    () -> assertNotNull(flavors.get(0).getId(), "插入后ID应自动生成"),
                    () -> {
                        List<DishFlavor> inserted = dishFlavorMapper.getByDishId(dishId);
                        assertTrue(inserted.size() >= 1, "应能查询到至少1条口味记录");
                    }
            );
        }


        @Test
        @DisplayName("批量新增多个菜品的口味")
        void shouldInsertFlavorsForMultipleDishes() {
            // Given
            List<DishFlavor> flavors = Arrays.asList(
                    createTestDishFlavor(10L, "辣度", "[\"不辣\",\"微辣\"]"),
                    createTestDishFlavor(11L, "甜度", "[\"少糖\",\"正常\"]"),
                    createTestDishFlavor(12L, "温度", "[\"热\",\"冷\"]")
            );

            // When
            dishFlavorMapper.insertBatch(flavors);

            // Then
            assertAll("多菜品口味验证",
                    () -> assertNotNull(flavors.get(0).getId(), "第一个口味ID应生成"),
                    () -> assertNotNull(flavors.get(1).getId(), "第二个口味ID应生成"),
                    () -> assertNotNull(flavors.get(2).getId(), "第三个口味ID应生成")
            );
        }
    }

    // ==================== deleteByDishId 方法测试 ====================

    @Nested
    @DisplayName("deleteByDishId 方法测试")
    @Tag("delete")
    class DeleteByDishIdMethodTests {

        @Test
        @DisplayName("根据菜品ID删除口味 - 成功场景")
        void shouldDeleteFlavorsWhenDishIdExists() {
            // Given
            Long dishId = 100L;
            insertTestDishFlavors(dishId);

            // When
            dishFlavorMapper.deleteByDishId(dishId);

            // Then
            List<DishFlavor> deleted = dishFlavorMapper.getByDishId(dishId);
            assertTrue(deleted.isEmpty(), "删除后应查询不到口味数据");
        }

        @Test
        @DisplayName("删除不存在的菜品ID时不报错")
        @Tag("edge-case")
        void shouldNotThrowExceptionWhenDeletingNonExistentDishId() {
            // Given
            Long nonExistentDishId = 999999L;

            // When & Then
            assertDoesNotThrow(() -> {
                dishFlavorMapper.deleteByDishId(nonExistentDishId);
            }, "删除不存在的菜品ID时不应抛出异常");
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("菜品ID为null时不报错")
        @Tag("edge-case")
        void shouldNotThrowExceptionWhenDishIdIsNull(Long dishId) {
            // When & Then
            assertDoesNotThrow(() -> {
                dishFlavorMapper.deleteByDishId(dishId);
            }, "菜品ID为null时不应抛出异常");
        }

        @Test
        @DisplayName("删除后重新插入口味")
        void shouldReinsertFlavorsAfterDeletion() {
            // Given
            Long dishId = 101L;
            insertTestDishFlavors(dishId);
            dishFlavorMapper.deleteByDishId(dishId);

            // When
            List<DishFlavor> newFlavors = Collections.singletonList(
                    createTestDishFlavor(dishId, "新口味", "[\"value1\"]")
            );
            dishFlavorMapper.insertBatch(newFlavors);

            // Then
            List<DishFlavor> reinserted = dishFlavorMapper.getByDishId(dishId);
            assertAll("重新插入验证",
                    () -> assertFalse(reinserted.isEmpty(), "应能查询到新插入的口味"),
                    () -> assertEquals("新口味", reinserted.get(0).getName(), "口味名称应匹配")
            );
        }
    }

    // ==================== deleteByDishIds 方法测试 ====================

    @Nested
    @DisplayName("deleteByDishIds 方法测试")
    @Tag("delete")
    class DeleteByDishIdsMethodTests {

        @Test
        @DisplayName("根据菜品ID批量删除口味 - 成功场景")
        void shouldDeleteFlavorsWhenDishIdsExist() {
            // Given
            Long dishId1 = 200L;
            Long dishId2 = 201L;
            Long dishId3 = 202L;
            insertTestDishFlavors(dishId1);
            insertTestDishFlavors(dishId2);
            insertTestDishFlavors(dishId3);

            // When
            List<Long> dishIds = Arrays.asList(dishId1, dishId2, dishId3);
            dishFlavorMapper.deleteByDishIds(dishIds);

            // Then
            assertAll("批量删除验证",
                    () -> assertTrue(dishFlavorMapper.getByDishId(dishId1).isEmpty(), "菜品1的口味应被删除"),
                    () -> assertTrue(dishFlavorMapper.getByDishId(dishId2).isEmpty(), "菜品2的口味应被删除"),
                    () -> assertTrue(dishFlavorMapper.getByDishId(dishId3).isEmpty(), "菜品3的口味应被删除")
            );
        }

        @Test
        @DisplayName("批量删除单个菜品ID")
        void shouldDeleteFlavorsWhenDishIdsContainsSingleId() {
            // Given
            Long dishId = 203L;
            insertTestDishFlavors(dishId);

            // When
            List<Long> dishIds = Collections.singletonList(dishId);
            dishFlavorMapper.deleteByDishIds(dishIds);

            // Then
            List<DishFlavor> deleted = dishFlavorMapper.getByDishId(dishId);
            assertTrue(deleted.isEmpty(), "删除后应查询不到口味数据");
        }

        @Test
        @DisplayName("批量删除空列表时不报错")
        @Tag("edge-case")
        void shouldNotThrowExceptionWhenDeletingEmptyList() {
            // Given
            List<Long> emptyList = Collections.emptyList();

            // When & Then
            assertDoesNotThrow(() -> {
                dishFlavorMapper.deleteByDishIds(emptyList);
            }, "删除空列表时不应抛出异常");
        }

        @Test
        @DisplayName("批量删除包含不存在的菜品ID")
        void shouldDeleteFlavorsWhenDishIdsContainsNonExistentId() {
            // Given
            Long dishId1 = 204L;
            Long nonExistentDishId = 999999L;
            insertTestDishFlavors(dishId1);

            // When
            List<Long> dishIds = Arrays.asList(dishId1, nonExistentDishId);
            dishFlavorMapper.deleteByDishIds(dishIds);

            // Then
            assertTrue(dishFlavorMapper.getByDishId(dishId1).isEmpty(), "存在的菜品口味应被删除");
        }

        @Test
        @DisplayName("批量删除部分存在部分不存在的菜品ID")
        void shouldHandleMixedExistenceDishIds() {
            // Given
            Long dishId1 = 205L;
            Long dishId2 = 206L;
            Long nonExistentDishId = 999998L;
            insertTestDishFlavors(dishId1);
            // dishId2 没有插入数据

            // When
            List<Long> dishIds = Arrays.asList(dishId1, dishId2, nonExistentDishId);
            dishFlavorMapper.deleteByDishIds(dishIds);

            // Then
            assertAll("混合存在性验证",
                    () -> assertTrue(dishFlavorMapper.getByDishId(dishId1).isEmpty(), "存在的菜品口味应被删除"),
                    () -> assertTrue(dishFlavorMapper.getByDishId(dishId2).isEmpty(), "不存在的菜品应保持为空")
            );
        }
    }

    // ==================== getByDishId 方法测试 ====================

    @Nested
    @DisplayName("getByDishId 方法测试")
    @Tag("query")
    class GetByDishIdMethodTests {

        @Test
        @DisplayName("根据菜品ID查询口味 - 成功场景")
        void shouldReturnFlavorsWhenDishIdExists() {
            // Given
            Long dishId = 300L;
            List<DishFlavor> inserted = insertTestDishFlavors(dishId);

            // When
            List<DishFlavor> found = dishFlavorMapper.getByDishId(dishId);

            // Then
            assertAll("查询验证",
                    () -> assertNotNull(found, "口味列表不应为null"),
                    () -> assertTrue(found.size() >= 3, "应能查询到至少3条口味记录"),
                    () -> {
                        boolean hasSpiciness = found.stream()
                                .anyMatch(f -> "辣度".equals(f.getName()));
                        assertTrue(hasSpiciness, "应包含'辣度'口味");
                    }
            );
        }

        @Test
        @DisplayName("菜品ID不存在时返回空列表")
        void shouldReturnEmptyListWhenDishIdNotExists() {
            // Given
            Long nonExistentDishId = 999999L;

            // When
            List<DishFlavor> found = dishFlavorMapper.getByDishId(nonExistentDishId);

            // Then
            assertAll("空列表验证",
                    () -> assertNotNull(found, "口味列表不应为null"),
                    () -> assertTrue(found.isEmpty(), "不存在的菜品ID应返回空列表")
            );
        }

        @ParameterizedTest
        @ValueSource(longs = {999999L, -1L, 0L})
        @DisplayName("无效ID时返回空列表")
        @Tag("edge-case")
        void shouldReturnEmptyListWhenIdIsInvalid(Long invalidId) {
            // When
            List<DishFlavor> found = dishFlavorMapper.getByDishId(invalidId);

            // Then
            assertAll("无效ID验证",
                    () -> assertNotNull(found, "口味列表不应为null"),
                    () -> assertTrue(found.isEmpty(), "无效ID应返回空列表")
            );
        }

        @Test
        @DisplayName("验证返回数据完整性")
        void shouldReturnCompleteFlavorData() {
            // Given
            Long dishId = 301L;
            insertTestDishFlavors(dishId);

            // When
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(dishId);

            // Then
            if (!flavors.isEmpty()) {
                DishFlavor firstFlavor = flavors.get(0);
                assertAll("口味数据完整性验证",
                        () -> assertNotNull(firstFlavor.getId(), "口味ID不应为null"),
                        () -> assertNotNull(firstFlavor.getDishId(), "菜品ID不应为null"),
                        () -> assertNotNull(firstFlavor.getName(), "口味名称不应为null"),
                        () -> assertNotNull(firstFlavor.getValue(), "口味数据不应为null")
                );
            }
        }

        @Test
        @DisplayName("验证JSON格式的value字段")
        void shouldReturnFlavorWithJsonValue() {
            // Given
            Long dishId = 302L;
            String jsonValue = "[\"不辣\",\"微辣\",\"中辣\",\"特辣\"]";
            List<DishFlavor> flavors = Collections.singletonList(
                    createTestDishFlavor(dishId, "辣度", jsonValue)
            );
            dishFlavorMapper.insertBatch(flavors);

            // When
            List<DishFlavor> found = dishFlavorMapper.getByDishId(dishId);

            // Then
            assertAll("JSON格式验证",
                    () -> assertFalse(found.isEmpty(), "应能查询到口味"),
                    () -> assertEquals(jsonValue, found.get(0).getValue(), "JSON数据应完整保存")
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
            // 1. Create - 批量新增口味
            Long dishId = 400L;
            List<DishFlavor> flavors = insertTestDishFlavors(dishId);
            assertAll("新增验证",
                    () -> assertNotNull(flavors.get(0).getId(), "新增后ID应自动生成"),
                    () -> assertNotNull(flavors.get(1).getId(), "新增后ID应自动生成"),
                    () -> assertNotNull(flavors.get(2).getId(), "新增后ID应自动生成")
            );

            // 2. Read - 查询口味
            List<DishFlavor> found = dishFlavorMapper.getByDishId(dishId);
            assertTrue(found.size() >= 3, "应能查询到至少3条口味记录");

            // 3. Delete - 删除口味
            dishFlavorMapper.deleteByDishId(dishId);
            List<DishFlavor> deleted = dishFlavorMapper.getByDishId(dishId);
            assertTrue(deleted.isEmpty(), "删除后应查询不到数据");
        }

        @Test
        @DisplayName("菜品口味管理流程测试")
        void shouldManageDishFlavorsCorrectly() {
            // Given - 新增菜品的口味
            Long dishId = 401L;
            insertTestDishFlavors(dishId);

            // When - 查询初始口味
            List<DishFlavor> initialFlavors = dishFlavorMapper.getByDishId(dishId);
            assertTrue(initialFlavors.size() >= 3, "初始应有至少3个口味");

            // When - 删除所有口味
            dishFlavorMapper.deleteByDishId(dishId);
            List<DishFlavor> afterDeletion = dishFlavorMapper.getByDishId(dishId);
            assertTrue(afterDeletion.isEmpty(), "删除后应无口味");

            // When - 重新插入新口味
            List<DishFlavor> newFlavors = Collections.singletonList(
                    createTestDishFlavor(dishId, "新口味", "[\"value1\",\"value2\"]")
            );
            dishFlavorMapper.insertBatch(newFlavors);

            // Then - 验证新口味
            List<DishFlavor> finalFlavors = dishFlavorMapper.getByDishId(dishId);
            assertAll("新口味验证",
                    () -> assertEquals(1, finalFlavors.size(), "应有1个新口味"),
                    () -> assertEquals("新口味", finalFlavors.get(0).getName(), "口味名称应匹配")
            );
        }

        @Test
        @DisplayName("批量菜品口味管理流程测试")
        void shouldManageMultipleDishFlavorsCorrectly() {
            // Given - 为多个菜品添加口味
            Long dishId1 = 402L;
            Long dishId2 = 403L;
            Long dishId3 = 404L;
            insertTestDishFlavors(dishId1);
            insertTestDishFlavors(dishId2);
            insertTestDishFlavors(dishId3);

            // When - 验证每个菜品都有口味
            List<DishFlavor> flavors1 = dishFlavorMapper.getByDishId(dishId1);
            List<DishFlavor> flavors2 = dishFlavorMapper.getByDishId(dishId2);
            List<DishFlavor> flavors3 = dishFlavorMapper.getByDishId(dishId3);

            assertAll("批量查询验证",
                    () -> assertTrue(flavors1.size() >= 3, "菜品1应有至少3个口味"),
                    () -> assertTrue(flavors2.size() >= 3, "菜品2应有至少3个口味"),
                    () -> assertTrue(flavors3.size() >= 3, "菜品3应有至少3个口味")
            );

            // When - 批量删除所有菜品的口味
            List<Long> dishIds = Arrays.asList(dishId1, dishId2, dishId3);
            dishFlavorMapper.deleteByDishIds(dishIds);

            // Then - 验证所有菜品的口味都被删除
            assertAll("批量删除验证",
                    () -> assertTrue(dishFlavorMapper.getByDishId(dishId1).isEmpty(), "菜品1的口味应被删除"),
                    () -> assertTrue(dishFlavorMapper.getByDishId(dishId2).isEmpty(), "菜品2的口味应被删除"),
                    () -> assertTrue(dishFlavorMapper.getByDishId(dishId3).isEmpty(), "菜品3的口味应被删除")
            );
        }

        @Test
        @DisplayName("口味数据完整性验证流程")
        void shouldMaintainFlavorDataIntegrity() {
            // Given - 插入包含复杂JSON的口味
            Long dishId = 405L;
            String complexJson = "[{\"name\":\"不辣\",\"value\":\"0\"},{\"name\":\"微辣\",\"value\":\"1\"}]";
            List<DishFlavor> flavors = Collections.singletonList(
                    createTestDishFlavor(dishId, "口味选择", complexJson)
            );
            dishFlavorMapper.insertBatch(flavors);

            // When - 查询口味
            List<DishFlavor> found = dishFlavorMapper.getByDishId(dishId);

            // Then - 验证数据完整性
            assertAll("数据完整性验证",
                    () -> assertFalse(found.isEmpty(), "应能查询到口味"),
                    () -> assertEquals("口味选择", found.get(0).getName(), "名称应匹配"),
                    () -> assertEquals(complexJson, found.get(0).getValue(), "JSON数据应完整")
            );
        }
    }
}
