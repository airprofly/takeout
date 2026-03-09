package com.airprofly.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import com.airprofly.entity.Dish;

/**
 * DishMapper 测试类
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
@Tag("dish")
@DisplayName("菜品 Mapper 测试")
public class DishMapperTest {

    @Autowired
    private DishMapper dishMapper;

    // ==================== 测试数据工厂方法 ====================

    /**
     * 创建标准的测试菜品对象
     */
    private Dish createTestDish(Long categoryId) {
        return Dish.builder()
                .name("测试菜品_" + System.currentTimeMillis())  // 添加时间戳确保唯一性
                .categoryId(categoryId)
                .price(new BigDecimal("38.00"))
                .image("test.jpg")
                .description("测试菜品描述")
                .status(1)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建并插入测试菜品,返回插入后的对象(包含生成的ID)
     */
    private Dish insertTestDish(Long categoryId) {
        Dish dish = createTestDish(categoryId);
        dishMapper.insert(dish);
        return dish;
    }

    // ==================== countByCategoryId 方法测试 ====================

    @Nested
    @DisplayName("countByCategoryId 方法测试")
    @Tag("query")
    class CountByCategoryIdMethodTests {

        @Test
        @DisplayName("根据分类ID统计菜品数量 - 成功场景")
        void shouldReturnDishCountWhenCategoryIdExists() {
            // Given
            Long categoryId = 1L;
            insertTestDish(categoryId);

            // When
            Integer count = dishMapper.countByCategoryId(categoryId);

            // Then
            assertAll("统计验证",
                    () -> assertNotNull(count, "统计结果不应为null"),
                    () -> assertTrue(count >= 1, "分类下应至少有1个菜品")
            );
        }

        @Test
        @DisplayName("分类下没有菜品时返回0")
        void shouldReturnZeroWhenCategoryIdHasNoDishes() {
            // Given
            Long nonExistentCategoryId = 999999L;

            // When
            Integer count = dishMapper.countByCategoryId(nonExistentCategoryId);

            // Then
            assertEquals(0, count, "不存在的分类应返回0");
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("分类ID为null时返回0")
        @Tag("edge-case")
        void shouldReturnZeroWhenCategoryIdIsNull(Long categoryId) {
            // When
            Integer count = dishMapper.countByCategoryId(categoryId);

            // Then
            assertEquals(0, count, "null分类ID应返回0");
        }

        @Test
        @DisplayName("统计多个菜品的分类")
        void shouldReturnCorrectCountForMultipleDishes() {
            // Given
            Long categoryId = 2L;
            insertTestDish(categoryId);
            insertTestDish(categoryId);
            insertTestDish(categoryId);

            // When
            Integer count = dishMapper.countByCategoryId(categoryId);

            // Then
            assertTrue(count >= 3, "应统计到至少3个菜品");
        }
    }

    // ==================== insert 方法测试 ====================

    @Nested
    @DisplayName("insert 方法测试")
    @Tag("create")
    class InsertMethodTests {

        @Test
        @DisplayName("新增菜品 - 成功场景")
        void shouldInsertDishWhenValidData() {
            // Given
            Dish dish = createTestDish(1L);
            String expectedName = dish.getName();  // 保存预期的名称(包含时间戳)

            // When
            dishMapper.insert(dish);

            // Then
            assertAll("插入验证",
                    () -> assertNotNull(dish.getId(), "插入后ID应自动生成"),
                    () -> {
                        Dish inserted = dishMapper.getById(dish.getId());
                        assertNotNull(inserted, "插入的数据应能被查询到");
                        assertEquals(expectedName, inserted.getName(), "菜品名称应匹配");
                        assertEquals(new BigDecimal("38.00"), inserted.getPrice(), "价格应匹配");
                        assertEquals(1, inserted.getStatus(), "状态应匹配");
                    }
            );
        }

        @Test
        @DisplayName("必填字段为null时应失败")
        @Tag("edge-case")
        void shouldFailToInsertWhenRequiredFieldsAreNull() {
            // Given
            Dish dish = Dish.builder()
                    .categoryId(1L)
                    // name 为 null
                    .price(new BigDecimal("38.00"))
                    .build();

            // When & Then
            assertThrows(DataIntegrityViolationException.class, () -> {
                dishMapper.insert(dish);
            }, "必填字段为null时应抛出异常");
        }

        @ParameterizedTest
        @CsvSource({
            "0, 停售",
            "1, 起售"
        })
        @DisplayName("新增不同状态的菜品")
        @Tag("parameterized")
        void shouldInsertDishWithDifferentStatus(Integer status, String statusDesc) {
            // Given
            Dish dish = createTestDish(1L);
            dish.setStatus(status);

            // When
            dishMapper.insert(dish);

            // Then
            Dish inserted = dishMapper.getById(dish.getId());
            assertAll("状态验证",
                    () -> assertNotNull(inserted, "应成功插入"),
                    () -> assertEquals(status, inserted.getStatus(), "状态应匹配为" + statusDesc)
            );
        }

        @Test
        @DisplayName("新增不同价格的菜品")
        void shouldInsertDishWithDifferentPrice() {
            // Given
            Dish dish = createTestDish(1L);
            dish.setPrice(new BigDecimal("99.99"));

            // When
            dishMapper.insert(dish);

            // Then
            Dish inserted = dishMapper.getById(dish.getId());
            assertAll("价格验证",
                    () -> assertNotNull(inserted, "应成功插入"),
                    () -> assertEquals(new BigDecimal("99.99"), inserted.getPrice(), "价格应匹配")
            );
        }
    }

    // ==================== pageQuery 方法测试 ====================

    @Nested
    @DisplayName("pageQuery 方法测试")
    @Tag("query")
    class PageQueryMethodTests {

        @Test
        @DisplayName("分页查询菜品 - 成功场景")
        void shouldReturnDishListWhenQuery() {
            // Given
            insertTestDish(1L);

            // When
            List<Dish> dishes = dishMapper.pageQuery(null, null, null);

            // Then
            assertAll("分页查询验证",
                    () -> assertNotNull(dishes, "菜品列表不应为null"),
                    () -> assertFalse(dishes.isEmpty(), "菜品列表不应为空")
            );
        }

        @Test
        @DisplayName("根据菜品名称模糊查询")
        void shouldReturnDishListWhenNameMatches() {
            // Given
            insertTestDish(1L);

            // When
            List<Dish> dishes = dishMapper.pageQuery("测试", null, null);

            // Then
            assertAll("名称模糊查询验证",
                    () -> assertNotNull(dishes, "菜品列表不应为null"),
                    () -> assertTrue(dishes.size() >= 1, "应至少有一条匹配记录")
            );
        }

        @Test
        @DisplayName("根据分类ID查询菜品")
        void shouldReturnDishListWhenCategoryIdMatches() {
            // Given
            Long categoryId = 3L;
            insertTestDish(categoryId);

            // When
            List<Dish> dishes = dishMapper.pageQuery(null, categoryId, null);

            // Then
            assertAll("分类ID查询验证",
                    () -> assertNotNull(dishes, "菜品列表不应为null"),
                    () -> assertTrue(dishes.size() >= 1, "应至少有一条匹配记录")
            );
        }

        @ParameterizedTest
        @CsvSource({
            "0, 停售菜品",
            "1, 起售菜品"
        })
        @DisplayName("根据状态查询菜品")
        @Tag("parameterized")
        void shouldReturnDishListWhenStatusMatches(Integer status, String statusDesc) {
            // Given
            Dish dish = createTestDish(1L);
            dish.setStatus(status);
            dishMapper.insert(dish);

            // When
            List<Dish> dishes = dishMapper.pageQuery(null, null, status);

            // Then
            assertAll("状态查询验证",
                    () -> assertNotNull(dishes, "菜品列表不应为null"),
                    () -> assertTrue(dishes.size() >= 1, "应至少有一条" + statusDesc + "记录")
            );
        }

        @Test
        @DisplayName("组合查询条件 - 分类ID和状态")
        void shouldReturnDishListWhenCombiningConditions() {
            // Given
            Long categoryId = 4L;
            Integer status = 1;
            insertTestDish(categoryId);

            // When
            List<Dish> dishes = dishMapper.pageQuery(null, categoryId, status);

            // Then
            assertAll("组合查询验证",
                    () -> assertNotNull(dishes, "菜品列表不应为null"),
                    () -> assertTrue(dishes.size() >= 1, "应至少有一条匹配记录")
            );
        }

        @Test
        @DisplayName("查询不存在的菜品名称时返回空列表")
        void shouldReturnEmptyListWhenNameNotExists() {
            // Given
            String nonExistentName = "不存在的菜品999";

            // When
            List<Dish> dishes = dishMapper.pageQuery(nonExistentName, null, null);

            // Then
            assertAll("不存在的名称验证",
                    () -> assertNotNull(dishes, "菜品列表不应为null"),
                    () -> assertTrue(dishes.isEmpty(), "不存在的名称应返回空列表")
            );
        }

        @Test
        @DisplayName("所有查询条件为null时返回所有菜品")
        void shouldReturnAllDishesWhenAllConditionsAreNull() {
            // Given
            insertTestDish(1L);

            // When
            List<Dish> dishes = dishMapper.pageQuery(null, null, null);

            // Then
            assertAll("空查询验证",
                    () -> assertNotNull(dishes, "菜品列表不应为null"),
                    () -> assertFalse(dishes.isEmpty(), "应返回所有菜品")
            );
        }
    }

    // ==================== getById 方法测试 ====================

    @Nested
    @DisplayName("getById 方法测试")
    @Tag("query")
    class GetByIdMethodTests {

        @Test
        @DisplayName("根据ID查询菜品 - 成功场景")
        void shouldReturnDishWhenIdExists() {
            // Given
            Dish inserted = insertTestDish(1L);
            Long insertedId = inserted.getId();
            String expectedName = inserted.getName();  // 保存预期的名称(包含时间戳)

            // When
            Dish found = dishMapper.getById(insertedId);

            // Then
            assertAll("查询验证",
                    () -> assertNotNull(found, "应能查询到插入的菜品"),
                    () -> assertEquals(insertedId, found.getId(), "ID应匹配"),
                    () -> assertEquals(expectedName, found.getName(), "菜品名称应匹配"),
                    () -> assertEquals(1, found.getStatus(), "状态应匹配")
            );
        }

        @ParameterizedTest
        @ValueSource(longs = {999999L, -1L, 0L})
        @DisplayName("ID不存在时返回null")
        @Tag("edge-case")
        void shouldReturnNullWhenIdNotExists(Long nonExistentId) {
            // When
            Dish found = dishMapper.getById(nonExistentId);

            // Then
            assertNull(found, "不存在的ID应返回null");
        }

        @Test
        @DisplayName("验证返回数据的完整性")
        void shouldReturnCompleteDishData() {
            // Given
            Dish inserted = insertTestDish(1L);

            // When
            Dish found = dishMapper.getById(inserted.getId());

            // Then
            assertAll("数据完整性验证",
                    () -> assertNotNull(found, "菜品对象不应为null"),
                    () -> assertNotNull(found.getId(), "ID不应为null"),
                    () -> assertNotNull(found.getName(), "名称不应为null"),
                    () -> assertNotNull(found.getCategoryId(), "分类ID不应为null"),
                    () -> assertNotNull(found.getPrice(), "价格不应为null"),
                    () -> assertNotNull(found.getStatus(), "状态不应为null")
            );
        }
    }

    // ==================== deleteById 方法测试 ====================

    @Nested
    @DisplayName("deleteById 方法测试")
    @Tag("delete")
    class DeleteByIdMethodTests {

        @Test
        @DisplayName("根据ID删除菜品 - 成功场景")
        void shouldDeleteDishWhenIdExists() {
            // Given
            Dish dish = insertTestDish(1L);
            Long insertedId = dish.getId();

            // When
            dishMapper.deleteById(insertedId);

            // Then
            Dish deleted = dishMapper.getById(insertedId);
            assertNull(deleted, "删除后应查询不到数据");
        }

        @ParameterizedTest
        @ValueSource(longs = {999999L, -1L, 0L})
        @DisplayName("删除不存在的ID时不报错")
        @Tag("edge-case")
        void shouldNotThrowExceptionWhenDeletingNonExistentId(Long nonExistentId) {
            // When & Then
            assertDoesNotThrow(() -> {
                dishMapper.deleteById(nonExistentId);
            }, "删除不存在的ID时不应抛出异常");
        }

        @Test
        @DisplayName("删除菜品后该分类的统计数量应减少")
        void shouldDecreaseCategoryCountAfterDeletion() {
            // Given
            Long categoryId = 5L;
            insertTestDish(categoryId);
            Integer countBefore = dishMapper.countByCategoryId(categoryId);

            Dish dish = insertTestDish(categoryId);
            Integer countAfterInsert = dishMapper.countByCategoryId(categoryId);

            // When
            dishMapper.deleteById(dish.getId());
            Integer countAfterDelete = dishMapper.countByCategoryId(categoryId);

            // Then
            assertAll("统计数量验证",
                    () -> assertTrue(countAfterInsert > countBefore, "插入后数量应增加"),
                    () -> assertEquals(countBefore, countAfterDelete, "删除后数量应恢复到之前")
            );
        }
    }

    // ==================== deleteByIds 方法测试 ====================

    @Nested
    @DisplayName("deleteByIds 方法测试")
    @Tag("delete")
    class DeleteByIdsMethodTests {

        @Test
        @DisplayName("批量删除菜品 - 成功场景")
        void shouldDeleteMultipleDishesWhenIdsExist() {
            // Given
            Dish dish1 = insertTestDish(1L);
            Dish dish2 = insertTestDish(1L);
            Dish dish3 = insertTestDish(1L);
            List<Long> ids = Arrays.asList(dish1.getId(), dish2.getId(), dish3.getId());

            // When
            dishMapper.deleteByIds(ids);

            // Then
            assertAll("批量删除验证",
                    () -> assertNull(dishMapper.getById(dish1.getId()), "菜品1应被删除"),
                    () -> assertNull(dishMapper.getById(dish2.getId()), "菜品2应被删除"),
                    () -> assertNull(dishMapper.getById(dish3.getId()), "菜品3应被删除")
            );
        }

        @Test
        @DisplayName("批量删除包含不存在的ID时不报错")
        @Tag("edge-case")
        void shouldNotThrowExceptionWhenIdsContainNonExistent() {
            // Given
            Dish dish1 = insertTestDish(1L);
            List<Long> ids = Arrays.asList(dish1.getId(), 999999L, -1L);

            // When & Then
            assertDoesNotThrow(() -> {
                dishMapper.deleteByIds(ids);
            }, "批量删除包含不存在的ID时不应抛出异常");

            // And
            assertNull(dishMapper.getById(dish1.getId()), "存在的ID应被删除");
        }

        @Test
        @DisplayName("批量删除空列表时不报错")
        @Tag("edge-case")
        void shouldNotThrowExceptionWhenIdsListIsEmpty() {
            // Given
            List<Long> emptyIds = Arrays.asList();

            // When & Then
            // 空列表会导致SQL语法错误,这应该由Service层处理
            // Mapper层不处理此边界情况
            assertThrows(Exception.class, () -> {
                dishMapper.deleteByIds(emptyIds);
            }, "批量删除空列表时应抛出异常(Services层应防止此情况)");
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("批量删除null时不报错")
        @Tag("edge-case")
        void shouldNotThrowExceptionWhenIdsListIsNull(List<Long> ids) {
            // When & Then
            // null列表会导致MyBatis异常,这应该由Service层处理
            // Mapper层不处理此边界情况
            assertThrows(Exception.class, () -> {
                dishMapper.deleteByIds(ids);
            }, "批量删除null时应抛出异常(Service层应防止此情况)");
        }

        @Test
        @DisplayName("批量删除单个菜品")
        void shouldDeleteSingleDishWhenIdsListHasOneElement() {
            // Given
            Dish dish = insertTestDish(1L);
            List<Long> ids = Arrays.asList(dish.getId());

            // When
            dishMapper.deleteByIds(ids);

            // Then
            assertNull(dishMapper.getById(dish.getId()), "菜品应被删除");
        }
    }

    // ==================== update 方法测试 ====================

    @Nested
    @DisplayName("update 方法测试")
    @Tag("update")
    class UpdateMethodTests {

        @Test
        @DisplayName("修改菜品 - 成功场景")
        void shouldUpdateDishWhenValidData() {
            // Given
            Dish dish = insertTestDish(1L);
            Long insertedId = dish.getId();

            // When
            Dish updateData = Dish.builder()
                    .id(insertedId)
                    .name("修改后菜品名称")
                    .price(new BigDecimal("58.00"))
                    .image("updated.jpg")
                    .description("修改后描述")
                    .status(0)
                    .updateTime(LocalDateTime.now())
                    .build();
            dishMapper.update(updateData);

            // Then
            Dish updated = dishMapper.getById(insertedId);
            assertAll("修改验证",
                    () -> assertNotNull(updated, "修改后应能查询到数据"),
                    () -> assertEquals("修改后菜品名称", updated.getName(), "名称应被修改"),
                    () -> assertEquals(new BigDecimal("58.00"), updated.getPrice(), "价格应被修改"),
                    () -> assertEquals("updated.jpg", updated.getImage(), "图片应被修改"),
                    () -> assertEquals("修改后描述", updated.getDescription(), "描述应被修改"),
                    () -> assertEquals(0, updated.getStatus(), "状态应被修改")
            );
        }

        @Test
        @DisplayName("修改部分字段")
        void shouldUpdateOnlySpecifiedFields() {
            // Given
            Dish dish = insertTestDish(1L);
            Long insertedId = dish.getId();
            String originalImage = dish.getImage();
            Integer originalStatus = dish.getStatus();

            // When - 只修改名称和价格
            Dish updateData = Dish.builder()
                    .id(insertedId)
                    .name("只修改名称")
                    .price(new BigDecimal("68.00"))
                    .updateTime(LocalDateTime.now())
                    .build();
            dishMapper.update(updateData);

            // Then
            Dish updated = dishMapper.getById(insertedId);
            assertAll("部分字段修改验证",
                    () -> assertNotNull(updated, "修改后应能查询到数据"),
                    () -> assertEquals("只修改名称", updated.getName(), "名称应被修改"),
                    () -> assertEquals(new BigDecimal("68.00"), updated.getPrice(), "价格应被修改"),
                    () -> assertEquals(originalImage, updated.getImage(), "未修改的字段应保持不变"),
                    () -> assertEquals(originalStatus, updated.getStatus(), "未修改的状态应保持不变")
            );
        }

        @Test
        @DisplayName("修改不存在的ID时不报错")
        @Tag("edge-case")
        void shouldNotThrowExceptionWhenUpdatingNonExistentId() {
            // Given
            Dish updateData = Dish.builder()
                    .id(999999L)
                    .name("不存在的菜品")
                    .updateTime(LocalDateTime.now())
                    .build();

            // When & Then
            assertDoesNotThrow(() -> {
                dishMapper.update(updateData);
            }, "修改不存在的ID时不应抛出异常");
        }

        @ParameterizedTest
        @CsvSource({
            "0, 停售状态",
            "1, 起售状态"
        })
        @DisplayName("修改菜品状态")
        @Tag("parameterized")
        void shouldUpdateDishStatus(Integer status, String statusDesc) {
            // Given
            Dish dish = insertTestDish(1L);
            Long insertedId = dish.getId();

            // When
            Dish updateData = Dish.builder()
                    .id(insertedId)
                    .status(status)
                    .updateTime(LocalDateTime.now())
                    .build();
            dishMapper.update(updateData);

            // Then
            Dish updated = dishMapper.getById(insertedId);
            assertEquals(status, updated.getStatus(), "状态应被修改为" + statusDesc);
        }

        @Test
        @DisplayName("修改菜品分类")
        void shouldUpdateDishCategory() {
            // Given
            Dish dish = insertTestDish(1L);
            Long insertedId = dish.getId();
            Long newCategoryId = 10L;

            // When
            Dish updateData = Dish.builder()
                    .id(insertedId)
                    .categoryId(newCategoryId)
                    .updateTime(LocalDateTime.now())
                    .build();
            dishMapper.update(updateData);

            // Then
            Dish updated = dishMapper.getById(insertedId);
            assertEquals(newCategoryId, updated.getCategoryId(), "分类应被修改");
        }
    }

    // ==================== list 方法测试 ====================

    @Nested
    @DisplayName("list 方法测试")
    @Tag("query")
    class ListMethodTests {

        @Test
        @DisplayName("根据分类ID查询菜品列表")
        void shouldReturnDishListWhenCategoryIdExists() {
            // Given
            Long categoryId = 6L;
            insertTestDish(categoryId);
            Dish query = Dish.builder()
                    .categoryId(categoryId)
                    .build();

            // When
            List<Dish> dishes = dishMapper.list(query);

            // Then
            assertAll("分类查询验证",
                    () -> assertNotNull(dishes, "菜品列表不应为null"),
                    () -> assertTrue(dishes.size() >= 1, "应至少有一条匹配记录")
            );
        }

        @Test
        @DisplayName("根据状态查询菜品列表")
        void shouldReturnDishListWhenStatusExists() {
            // Given
            Dish dish = createTestDish(1L);
            dish.setStatus(1);
            dishMapper.insert(dish);
            Dish query = Dish.builder()
                    .status(1)
                    .build();

            // When
            List<Dish> dishes = dishMapper.list(query);

            // Then
            assertAll("状态查询验证",
                    () -> assertNotNull(dishes, "菜品列表不应为null"),
                    () -> assertTrue(dishes.size() >= 1, "应至少有一条匹配记录")
            );
        }

        @Test
        @DisplayName("组合查询条件 - 分类ID和状态")
        void shouldReturnDishListWhenCombiningQueryConditions() {
            // Given
            Long categoryId = 7L;
            Integer status = 1;
            insertTestDish(categoryId);
            Dish query = Dish.builder()
                    .categoryId(categoryId)
                    .status(status)
                    .build();

            // When
            List<Dish> dishes = dishMapper.list(query);

            // Then
            assertAll("组合查询验证",
                    () -> assertNotNull(dishes, "菜品列表不应为null"),
                    () -> assertTrue(dishes.size() >= 1, "应至少有一条匹配记录")
            );
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("查询条件为null时返回所有菜品")
        @Tag("edge-case")
        void shouldReturnAllDishesWhenQueryIsNull(Dish query) {
            // Given
            insertTestDish(1L);

            // When
            List<Dish> dishes = dishMapper.list(query);

            // Then
            assertNotNull(dishes, "菜品列表不应为null");
        }

        @Test
        @DisplayName("查询不存在的条件时返回空列表")
        void shouldReturnEmptyListWhenConditionNotExists() {
            // Given
            Dish query = Dish.builder()
                    .categoryId(999999L)
                    .status(1)
                    .build();

            // When
            List<Dish> dishes = dishMapper.list(query);

            // Then
            assertAll("空列表验证",
                    () -> assertNotNull(dishes, "菜品列表不应为null"),
                    () -> assertTrue(dishes.isEmpty(), "不存在的条件应返回空列表")
            );
        }
    }

    // ==================== getBySetmealId 方法测试 ====================

    @Nested
    @DisplayName("getBySetmealId 方法测试")
    @Tag("query")
    class GetBySetmealIdMethodTests {

        @Test
        @DisplayName("根据套餐ID查询菜品 - 成功场景")
        void shouldReturnDishListWhenSetmealIdExists() {
            // Given
            Long setmealId = 1L;

            // When
            List<Dish> dishes = dishMapper.getBySetmealId(setmealId);

            // Then
            assertNotNull(dishes, "菜品列表不应为null");
        }

        @Test
        @DisplayName("套餐下没有菜品时返回空列表")
        void shouldReturnEmptyListWhenSetmealHasNoDishes() {
            // Given
            Long nonExistentSetmealId = 999999L;

            // When
            List<Dish> dishes = dishMapper.getBySetmealId(nonExistentSetmealId);

            // Then
            assertAll("空列表验证",
                    () -> assertNotNull(dishes, "菜品列表不应为null"),
                    () -> assertTrue(dishes.isEmpty(), "不存在的套餐ID应返回空列表")
            );
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L})
        @DisplayName("无效的套餐ID返回空列表")
        @Tag("edge-case")
        void shouldReturnEmptyListWhenSetmealIdIsInvalid(Long invalidSetmealId) {
            // When
            List<Dish> dishes = dishMapper.getBySetmealId(invalidSetmealId);

            // Then
            assertNotNull(dishes, "菜品列表不应为null");
        }
    }

    // ==================== countByMap 方法测试 ====================

    @Nested
    @DisplayName("countByMap 方法测试")
    @Tag("query")
    class CountByMapMethodTests {

        @Test
        @DisplayName("根据条件统计菜品数量 - 成功场景")
        void shouldReturnDishCountWhenConditionMatches() {
            // Given
            Long categoryId = 8L;
            insertTestDish(categoryId);
            Map<String, Integer> condition = new HashMap<>();
            condition.put("category_id", categoryId.intValue());
            condition.put("status", 1);

            // When
            Integer count = dishMapper.countByMap(condition);

            // Then
            assertAll("统计验证",
                    () -> assertNotNull(count, "统计结果不应为null"),
                    () -> assertTrue(count >= 1, "应至少统计到1个菜品")
            );
        }

        @Test
        @DisplayName("根据状态统计菜品数量")
        void shouldReturnDishCountWhenStatusMatches() {
            // Given
            Map<String, Integer> condition = new HashMap<>();
            condition.put("status", 1);

            // When
            Integer count = dishMapper.countByMap(condition);

            // Then
            assertNotNull(count, "统计结果不应为null");
        }

        @Test
        @DisplayName("不存在的条件返回0")
        void shouldReturnZeroWhenConditionNotExists() {
            // Given
            Map<String, Integer> condition = new HashMap<>();
            condition.put("category_id", 999999);
            condition.put("status", 999);

            // When
            Integer count = dishMapper.countByMap(condition);

            // Then
            assertEquals(0, count, "不存在的条件应返回0");
        }

        @Test
        @DisplayName("空Map返回所有菜品数量")
        void shouldReturnAllDishCountWhenMapIsEmpty() {
            // Given
            insertTestDish(1L);
            Map<String, Integer> condition = new HashMap<>();

            // When
            Integer count = dishMapper.countByMap(condition);

            // Then
            assertNotNull(count, "统计结果不应为null");
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("null Map返回所有菜品数量")
        @Tag("edge-case")
        void shouldReturnAllDishCountWhenMapIsNull(Map<String, Integer> condition) {
            // When
            Integer count = dishMapper.countByMap(condition);

            // Then
            // null Map会被当作空Map处理,返回所有菜品数量(包含初始数据)
            assertNotNull(count, "统计结果不应为null");
            assertTrue(count > 0, "应返回所有菜品数量(包含初始数据)");
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
            // 1. Create - 新增菜品
            Dish dish = insertTestDish(1L);
            String expectedName = dish.getName();  // 保存预期的名称(包含时间戳)
            assertAll("新增验证",
                    () -> assertNotNull(dish.getId(), "新增后ID应自动生成")
            );

            // 2. Read - 查询菜品(通过ID)
            Dish found = dishMapper.getById(dish.getId());
            assertAll("查询验证",
                    () -> assertNotNull(found, "应能查询到新增的菜品"),
                    () -> assertEquals(expectedName, found.getName(), "菜品名称应匹配")
            );

            // 3. Read - 分页查询(使用"测试菜品"前缀进行模糊查询)
            List<Dish> dishes = dishMapper.pageQuery("测试菜品", null, null);
            assertTrue(dishes.size() >= 1, "分页查询应能找到新增的菜品");

            // 4. Update - 修改菜品
            Dish updateData = Dish.builder()
                    .id(dish.getId())
                    .name("完整流程测试-修改")
                    .updateTime(LocalDateTime.now())
                    .build();
            dishMapper.update(updateData);
            Dish updated = dishMapper.getById(dish.getId());
            assertEquals("完整流程测试-修改", updated.getName(), "修改应生效");

            // 5. Delete - 删除菜品
            dishMapper.deleteById(dish.getId());
            Dish deleted = dishMapper.getById(dish.getId());
            assertNull(deleted, "删除后应查询不到数据");
        }

        @Test
        @DisplayName("菜品状态管理流程测试")
        void shouldManageDishStatusCorrectly() {
            // Given - 新增一个起售的菜品
            Dish dish = insertTestDish(1L);
            assertEquals(1, dish.getStatus(), "初始状态应为起售");

            Long dishId = dish.getId();

            // When - 停售菜品
            Dish disableData = Dish.builder()
                    .id(dishId)
                    .status(0)
                    .updateTime(LocalDateTime.now())
                    .build();
            dishMapper.update(disableData);

            // Then - 验证状态已停售
            Dish disabled = dishMapper.getById(dishId);
            assertEquals(0, disabled.getStatus(), "菜品应被停售");

            // When - 重新起售菜品
            Dish enableData = Dish.builder()
                    .id(dishId)
                    .status(1)
                    .updateTime(LocalDateTime.now())
                    .build();
            dishMapper.update(enableData);

            // Then - 验证状态已起售
            Dish enabled = dishMapper.getById(dishId);
            assertEquals(1, enabled.getStatus(), "菜品应被重新起售");

            // When - 查询起售的菜品
            List<Dish> activeDishes = dishMapper.pageQuery(null, null, 1);

            // Then - 验证查询结果包含该菜品
            assertTrue(activeDishes.stream().anyMatch(d -> d.getId().equals(dishId)),
                    "起售菜品列表应包含该菜品");
        }

        @Test
        @DisplayName("分类菜品管理流程测试")
        void shouldManageCategoryDishesCorrectly() {
            // Given - 为分类新增多个菜品
            Long categoryId = 100L;

            insertTestDish(categoryId);
            insertTestDish(categoryId);
            insertTestDish(categoryId);

            // When - 统计分类下的菜品数量
            Integer count = dishMapper.countByCategoryId(categoryId);

            // Then - 验证统计结果
            assertTrue(count >= 3, "分类下应有至少3个菜品");

            // When - 根据分类ID查询菜品
            Dish query = Dish.builder()
                    .categoryId(categoryId)
                    .build();
            List<Dish> categoryDishes = dishMapper.list(query);

            // Then - 验证查询结果
            assertTrue(categoryDishes.size() >= 3, "分类下应查询到至少3个菜品");

            // When - 批量删除分类下的所有菜品
            List<Long> ids = categoryDishes.stream()
                    .map(Dish::getId)
                    .limit(3)
                    .toList();
            dishMapper.deleteByIds(ids);

            // Then - 验证删除后统计数量
            Integer countAfterDelete = dishMapper.countByCategoryId(categoryId);
            assertTrue(countAfterDelete < count, "删除后数量应减少");
        }

        @Test
        @DisplayName("菜品价格修改流程测试")
        void shouldUpdateDishPriceCorrectly() {
            // Given - 新增菜品
            Dish dish = insertTestDish(1L);
            BigDecimal originalPrice = dish.getPrice();
            Long dishId = dish.getId();

            // When - 修改价格为促销价
            BigDecimal promotionalPrice = new BigDecimal("28.00");
            Dish updateData = Dish.builder()
                    .id(dishId)
                    .price(promotionalPrice)
                    .updateTime(LocalDateTime.now())
                    .build();
            dishMapper.update(updateData);

            // Then - 验证价格已修改
            Dish updated = dishMapper.getById(dishId);
            assertAll("价格修改验证",
                    () -> assertEquals(promotionalPrice, updated.getPrice(), "价格应被修改为促销价"),
                    () -> assertNotEquals(originalPrice, updated.getPrice(), "价格应与原价不同")
            );

            // When - 恢复原价
            Dish restoreData = Dish.builder()
                    .id(dishId)
                    .price(originalPrice)
                    .updateTime(LocalDateTime.now())
                    .build();
            dishMapper.update(restoreData);

            // Then - 验证价格已恢复
            Dish restored = dishMapper.getById(dishId);
            assertEquals(originalPrice, restored.getPrice(), "价格应恢复为原价");
        }

        @Test
        @DisplayName("多条件组合查询流程测试")
        void shouldQueryWithMultipleConditionsCorrectly() {
            // Given - 准备测试数据
            Long categoryId = 200L;

            Dish dish1 = createTestDish(categoryId);
            dish1.setName("川菜-麻婆豆腐");
            dish1.setStatus(1);
            dishMapper.insert(dish1);

            Dish dish2 = createTestDish(categoryId);
            dish2.setName("川菜-水煮鱼");
            dish2.setStatus(1);
            dishMapper.insert(dish2);

            Dish dish3 = createTestDish(categoryId);
            dish3.setName("湘菜-剁椒鱼头");
            dish3.setStatus(0);
            dishMapper.insert(dish3);

            // When - 查询起售的川菜
            List<Dish> sichuanDishes = dishMapper.pageQuery("川菜", categoryId, 1);

            // Then - 验证查询结果
            assertAll("多条件查询验证",
                    () -> assertNotNull(sichuanDishes, "查询结果不应为null"),
                    () -> assertTrue(sichuanDishes.size() >= 2, "应至少查询到2个川菜"),
                    () -> assertTrue(sichuanDishes.stream().allMatch(d -> d.getStatus() == 1),
                            "所有结果都应是起售状态")
            );

            // When - 查询湘菜
            List<Dish> hunanDishes = dishMapper.pageQuery("湘菜", categoryId, null);

            // Then - 验证查询结果
            assertAll("湘菜查询验证",
                    () -> assertNotNull(hunanDishes, "查询结果不应为null"),
                    () -> assertTrue(hunanDishes.size() >= 1, "应至少查询到1个湘菜")
            );

            // When - 查询停售的菜品
            List<Dish> inactiveDishes = dishMapper.pageQuery(null, categoryId, 0);

            // Then - 验证查询结果
            assertAll("停售菜品查询验证",
                    () -> assertNotNull(inactiveDishes, "查询结果不应为null"),
                    () -> assertTrue(inactiveDishes.stream().allMatch(d -> d.getStatus() == 0),
                            "所有结果都应是停售状态")
            );
        }
    }
}
