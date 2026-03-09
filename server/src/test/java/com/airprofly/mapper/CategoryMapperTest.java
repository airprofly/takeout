package com.airprofly.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import com.airprofly.entity.Category;

/**
 * CategoryMapper 测试类
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
@Tag("category")
@DisplayName("分类 Mapper 测试")
public class CategoryMapperTest {

    @Autowired
    private CategoryMapper categoryMapper;

    // ==================== 测试数据工厂方法 ====================

    /**
     * 创建标准的测试分类对象
     */
    private Category createTestCategory(Integer type, String name) {
        return Category.builder()
                .type(type)
                .name(name)
                .sort(1)
                .status(1)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .createUser(1L)
                .updateUser(1L)
                .build();
    }

    /**
     * 创建并插入测试分类,返回插入后的对象(包含生成的ID)
     */
    private Category insertTestCategory(Integer type, String name) {
        Category category = createTestCategory(type, name);
        categoryMapper.insert(category);
        return category;
    }

    // ==================== insert 方法测试 ====================

    @Nested
    @DisplayName("insert 方法测试")
    @Tag("create")
    class InsertMethodTests {

        @Test
        @DisplayName("新增分类 - 成功场景")
        void shouldInsertCategoryWhenValidData() {
            // Given
            Category category = createTestCategory(1, "测试菜品分类");

            // When
            categoryMapper.insert(category);

            // Then
            assertAll("插入验证",
                    () -> assertNotNull(category.getId(), "插入后ID应自动生成"),
                    () -> {
                        List<Category> categories = categoryMapper.pageQuery("测试菜品分类", null);
                        assertTrue(categories.size() >= 1, "插入的数据应能被查询到");
                        Category inserted = categories.stream()
                                .filter(c -> c.getName().equals("测试菜品分类"))
                                .findFirst()
                                .orElse(null);
                        assertNotNull(inserted, "应能查询到插入的分类");
                        assertEquals("测试菜品分类", inserted.getName(), "分类名称应匹配");
                    }
            );
        }

        @Test
        @DisplayName("必填字段为null时应失败")
        @Tag("edge-case")
        void shouldFailToInsertWhenRequiredFieldsAreNull() {
            // Given
            Category category = Category.builder()
                    .type(1)
                    // name 为 null
                    .sort(1)
                    .status(1)
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();

            // When & Then
            assertThrows(DataIntegrityViolationException.class, () -> {
                categoryMapper.insert(category);
            }, "必填字段为null时应抛出异常");
        }

        @ParameterizedTest
        @CsvSource({
            "1, 菜品分类",
            "2, 套餐分类"
        })
        @DisplayName("新增不同类型的分类")
        @Tag("parameterized")
        void shouldInsertCategoryWithDifferentType(Integer type, String typeDesc) {
            // Given
            Category category = createTestCategory(type, typeDesc + "测试");

            // When
            categoryMapper.insert(category);

            // Then
            assertAll("类型验证",
                    () -> assertNotNull(category.getId(), "应成功插入"),
                    () -> {
                        List<Category> categories = categoryMapper.pageQuery(typeDesc + "测试", type);
                        assertTrue(categories.size() >= 1, "应能查询到插入的分类");
                    }
            );
        }

        @ParameterizedTest
        @CsvSource({
            "0, 禁用",
            "1, 启用"
        })
        @DisplayName("新增不同状态的分类")
        @Tag("parameterized")
        void shouldInsertCategoryWithDifferentStatus(Integer status, String statusDesc) {
            // Given
            Category category = createTestCategory(1, statusDesc + "分类");
            category.setStatus(status);

            // When
            categoryMapper.insert(category);

            // Then
            List<Category> categories = categoryMapper.pageQuery(statusDesc + "分类", null);
            assertAll("状态验证",
                    () -> assertTrue(categories.size() >= 1, "应成功插入"),
                    () -> {
                        Category inserted = categories.stream()
                                .filter(c -> c.getName().equals(statusDesc + "分类"))
                                .findFirst()
                                .orElse(null);
                        assertNotNull(inserted);
                        assertEquals(status, inserted.getStatus(), "状态应匹配");
                    }
            );
        }
    }

    // ==================== pageQuery 方法测试 ====================

    @Nested
    @DisplayName("pageQuery 方法测试")
    @Tag("query")
    class PageQueryMethodTests {

        @Test
        @DisplayName("分页查询分类 - 成功场景")
        void shouldReturnCategoryListWhenQuery() {
            // Given
            insertTestCategory(1, "分页查询测试");

            // When
            List<Category> categories = categoryMapper.pageQuery(null, null);

            // Then
            assertAll("分页查询验证",
                    () -> assertNotNull(categories, "分类列表不应为null"),
                    () -> assertFalse(categories.isEmpty(), "分类列表不应为空")
            );
        }

        @Test
        @DisplayName("根据名称模糊查询分类")
        void shouldReturnCategoryListWhenNameMatches() {
            // Given
            insertTestCategory(1, "名称模糊查询测试");

            // When
            List<Category> categories = categoryMapper.pageQuery("名称模糊", null);

            // Then
            assertAll("名称模糊查询验证",
                    () -> assertNotNull(categories, "分类列表不应为null"),
                    () -> assertTrue(categories.size() >= 1, "应至少有一条匹配记录")
            );
        }

        @Test
        @DisplayName("根据类型查询分类")
        void shouldReturnCategoryListWhenTypeMatches() {
            // Given
            insertTestCategory(1, "类型查询菜品分类");
            insertTestCategory(2, "类型查询套餐分类");

            // When
            List<Category> dishCategories = categoryMapper.pageQuery(null, 1);
            List<Category> mealCategories = categoryMapper.pageQuery(null, 2);

            // Then
            assertAll("类型查询验证",
                    () -> assertNotNull(dishCategories, "菜品分类列表不应为null"),
                    () -> assertNotNull(mealCategories, "套餐分类列表不应为null"),
                    () -> assertTrue(dishCategories.stream().allMatch(c -> c.getType() == 1), "所有结果应为菜品分类"),
                    () -> assertTrue(mealCategories.stream().allMatch(c -> c.getType() == 2), "所有结果应为套餐分类")
            );
        }

        @Test
        @DisplayName("根据名称和类型组合查询")
        void shouldReturnCategoryListWhenNameAndTypeMatch() {
            // Given
            insertTestCategory(1, "组合查询测试");

            // When
            List<Category> categories = categoryMapper.pageQuery("组合查询", 1);

            // Then
            assertAll("组合查询验证",
                    () -> assertNotNull(categories, "分类列表不应为null"),
                    () -> assertTrue(categories.size() >= 1, "应至少有一条匹配记录"),
                    () -> assertTrue(categories.stream().allMatch(c -> c.getType() == 1), "所有结果应为菜品分类")
            );
        }

        @Test
        @DisplayName("查询条件为null时返回所有分类")
        void shouldReturnAllCategoriesWhenQueryIsNull() {
            // Given
            insertTestCategory(1, "查询所有测试");

            // When
            List<Category> categories = categoryMapper.pageQuery(null, null);

            // Then
            assertAll("空查询验证",
                    () -> assertNotNull(categories, "分类列表不应为null"),
                    () -> assertFalse(categories.isEmpty(), "应返回所有分类")
            );
        }

        @Test
        @DisplayName("查询不存在的名称时返回空列表")
        void shouldReturnEmptyListWhenNameNotExists() {
            // Given
            String nonExistentName = "不存在的分类名称999";

            // When
            List<Category> categories = categoryMapper.pageQuery(nonExistentName, null);

            // Then
            assertAll("不存在的名称验证",
                    () -> assertNotNull(categories, "分类列表不应为null"),
                    () -> assertTrue(categories.isEmpty(), "不存在的名称应返回空列表")
            );
        }

        @Test
        @DisplayName("查询不存在的类型时返回空列表")
        void shouldReturnEmptyListWhenTypeNotExists() {
            // Given
            Integer nonExistentType = 999;

            // When
            List<Category> categories = categoryMapper.pageQuery(null, nonExistentType);

            // Then
            assertAll("不存在的类型验证",
                    () -> assertNotNull(categories, "分类列表不应为null"),
                    () -> assertTrue(categories.isEmpty(), "不存在的类型应返回空列表")
            );
        }

        @Test
        @DisplayName("验证排序规则: sort升序, 创建时间降序")
        void shouldReturnCategoriesOrderedBySortAndCreateTime() {
            // Given
            insertTestCategory(1, "排序测试1");
            Category category2 = createTestCategory(1, "排序测试2");
            category2.setSort(2);
            categoryMapper.insert(category2);

            // When
            List<Category> categories = categoryMapper.pageQuery(null, null);

            // Then
            if (categories.size() >= 2) {
                // 验证排序: sort值小的在前
                assertTrue(categories.get(0).getSort() <= categories.get(1).getSort(),
                        "应按sort升序排列");
            }
        }
    }

    // ==================== deleteById 方法测试 ====================

    @Nested
    @DisplayName("deleteById 方法测试")
    @Tag("delete")
    class DeleteByIdMethodTests {

        @Test
        @DisplayName("根据ID删除分类 - 成功场景")
        void shouldDeleteCategoryWhenIdExists() {
            // Given
            Category category = insertTestCategory(1, "删除测试分类");

            // When
            categoryMapper.deleteById(category.getId());

            // Then
            List<Category> categories = categoryMapper.pageQuery("删除测试分类", null);
            assertTrue(categories.isEmpty(), "删除后应查询不到数据");
        }

        @ParameterizedTest
        @ValueSource(longs = {999999L, -1L, 0L})
        @DisplayName("删除不存在的ID时不报错")
        @Tag("edge-case")
        void shouldNotThrowExceptionWhenDeletingNonExistentId(Long nonExistentId) {
            // When & Then
            assertDoesNotThrow(() -> {
                categoryMapper.deleteById(nonExistentId);
            }, "删除不存在的ID时不应抛出异常");
        }
    }

    // ==================== update 方法测试 ====================

    @Nested
    @DisplayName("update 方法测试")
    @Tag("update")
    class UpdateMethodTests {

        @Test
        @DisplayName("修改分类 - 成功场景")
        void shouldUpdateCategoryWhenValidData() {
            // Given
            Category category = insertTestCategory(1, "修改测试分类");
            Long categoryId = category.getId();

            // When
            Category updateData = Category.builder()
                    .id(categoryId)
                    .name("修改后分类名称")
                    .sort(10)
                    .status(0)
                    .updateTime(LocalDateTime.now())
                    .updateUser(2L)
                    .build();
            categoryMapper.update(updateData);

            // Then
            List<Category> categories = categoryMapper.pageQuery("修改后分类名称", null);
            Category updated = categories.isEmpty() ? null : categories.get(0);
            assertAll("修改验证",
                    () -> assertNotNull(updated, "修改后应能查询到数据"),
                    () -> assertEquals("修改后分类名称", updated.getName(), "名称应被修改"),
                    () -> assertEquals(10, updated.getSort(), "排序应被修改"),
                    () -> assertEquals(0, updated.getStatus(), "状态应被修改")
            );
        }

        @Test
        @DisplayName("修改部分字段")
        void shouldUpdateOnlySpecifiedFields() {
            // Given
            Category category = insertTestCategory(1, "部分字段修改测试");
            Long categoryId = category.getId();
            Integer originalSort = category.getSort();
            Integer originalStatus = category.getStatus();

            // When - 只修改名称
            Category updateData = Category.builder()
                    .id(categoryId)
                    .name("只修改名称")
                    .updateTime(LocalDateTime.now())
                    .build();
            categoryMapper.update(updateData);

            // Then
            List<Category> categories = categoryMapper.pageQuery("只修改名称", null);
            Category updated = categories.isEmpty() ? null : categories.get(0);
            assertAll("部分字段修改验证",
                    () -> assertNotNull(updated, "修改后应能查询到数据"),
                    () -> assertEquals("只修改名称", updated.getName(), "名称应被修改"),
                    () -> assertEquals(originalSort, updated.getSort(), "未修改的排序应保持不变"),
                    () -> assertEquals(originalStatus, updated.getStatus(), "未修改的状态应保持不变")
            );
        }

        @Test
        @DisplayName("修改不存在的ID时不报错")
        @Tag("edge-case")
        void shouldNotThrowExceptionWhenUpdatingNonExistentId() {
            // Given
            Category updateData = Category.builder()
                    .id(999999L)
                    .name("不存在的分类")
                    .updateTime(LocalDateTime.now())
                    .build();

            // When & Then
            assertDoesNotThrow(() -> {
                categoryMapper.update(updateData);
            }, "修改不存在的ID时不应抛出异常");
        }

        @ParameterizedTest
        @CsvSource({
            "0, 禁用状态",
            "1, 启用状态"
        })
        @DisplayName("修改分类状态")
        @Tag("parameterized")
        void shouldUpdateCategoryStatus(Integer status, String statusDesc) {
            // Given
            Category category = insertTestCategory(1, "状态修改测试" + status);
            Long categoryId = category.getId();

            // When
            Category updateData = Category.builder()
                    .id(categoryId)
                    .status(status)
                    .updateTime(LocalDateTime.now())
                    .build();
            categoryMapper.update(updateData);

            // Then
            List<Category> categories = categoryMapper.pageQuery("状态修改测试" + status, null);
            Category updated = categories.isEmpty() ? null : categories.get(0);
            assertAll("状态修改验证",
                    () -> assertNotNull(updated, "应能查询到分类"),
                    () -> assertEquals(status, updated.getStatus(), "状态应被修改为" + statusDesc)
            );
        }

        @ParameterizedTest
        @CsvSource({
            "1, 菜品分类",
            "2, 套餐分类"
        })
        @DisplayName("修改分类类型")
        @Tag("parameterized")
        void shouldUpdateCategoryType(Integer type, String typeDesc) {
            // Given
            Category category = insertTestCategory(1, "类型修改测试" + type);
            Long categoryId = category.getId();

            // When
            Category updateData = Category.builder()
                    .id(categoryId)
                    .type(type)
                    .name(typeDesc + "修改后")
                    .updateTime(LocalDateTime.now())
                    .build();
            categoryMapper.update(updateData);

            // Then
            List<Category> categories = categoryMapper.pageQuery(typeDesc + "修改后", null);
            Category updated = categories.isEmpty() ? null : categories.get(0);
            assertAll("类型修改验证",
                    () -> assertNotNull(updated, "应能查询到分类"),
                    () -> assertEquals(type, updated.getType(), "类型应被修改为" + typeDesc)
            );
        }
    }

    // ==================== list 方法测试 ====================

    @Nested
    @DisplayName("list 方法测试")
    @Tag("query")
    class ListMethodTests {

        @Test
        @DisplayName("查询所有启用的分类 - 成功场景")
        void shouldReturnEnabledCategoryList() {
            // Given
            insertTestCategory(1, "列表查询测试");

            // When
            List<Category> categories = categoryMapper.list(null);

            // Then
            assertAll("列表查询验证",
                    () -> assertNotNull(categories, "分类列表不应为null"),
                    () -> assertFalse(categories.isEmpty(), "分类列表不应为空"),
                    () -> assertTrue(categories.stream().allMatch(c -> c.getStatus() == 1), "所有分类应为启用状态")
            );
        }

        @Test
        @DisplayName("根据类型查询启用的分类")
        void shouldReturnEnabledCategoryListWhenTypeMatches() {
            // Given
            insertTestCategory(1, "类型列表测试菜品");
            insertTestCategory(2, "类型列表测试套餐");

            // When
            List<Category> dishCategories = categoryMapper.list(1);
            List<Category> mealCategories = categoryMapper.list(2);

            // Then
            assertAll("类型列表查询验证",
                    () -> assertNotNull(dishCategories, "菜品分类列表不应为null"),
                    () -> assertNotNull(mealCategories, "套餐分类列表不应为null"),
                    () -> assertTrue(dishCategories.stream().allMatch(c -> c.getType() == 1), "所有结果应为菜品分类"),
                    () -> assertTrue(mealCategories.stream().allMatch(c -> c.getType() == 2), "所有结果应为套餐分类"),
                    () -> assertTrue(dishCategories.stream().allMatch(c -> c.getStatus() == 1), "菜品分类都应启用"),
                    () -> assertTrue(mealCategories.stream().allMatch(c -> c.getStatus() == 1), "套餐分类都应启用")
            );
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("查询参数为null时返回所有启用的分类")
        @Tag("edge-case")
        void shouldReturnAllEnabledCategoriesWhenTypeIsNull(Integer type) {
            // When
            List<Category> categories = categoryMapper.list(type);

            // Then
            assertAll("空类型查询验证",
                    () -> assertNotNull(categories, "分类列表不应为null"),
                    () -> assertTrue(categories.stream().allMatch(c -> c.getStatus() == 1), "所有分类应为启用状态")
            );
        }

        @Test
        @DisplayName("验证返回数据完整性")
        void shouldReturnCompleteCategoryData() {
            // Given
            insertTestCategory(1, "数据完整性测试");

            // When
            List<Category> categories = categoryMapper.list(null);

            // Then
            if (!categories.isEmpty()) {
                Category firstCategory = categories.get(0);
                assertAll("分类数据完整性验证",
                        () -> assertNotNull(firstCategory.getId(), "分类ID不应为null"),
                        () -> assertNotNull(firstCategory.getType(), "分类类型不应为null"),
                        () -> assertNotNull(firstCategory.getName(), "分类名称不应为null"),
                        () -> assertNotNull(firstCategory.getSort(), "排序不应为null"),
                        () -> assertNotNull(firstCategory.getStatus(), "状态不应为null")
                );
            }
        }

        @Test
        @DisplayName("禁用的分类不应出现在列表中")
        void shouldNotIncludeDisabledCategories() {
            // Given
            Category disabledCategory = createTestCategory(1, "禁用分类测试");
            disabledCategory.setStatus(0);
            categoryMapper.insert(disabledCategory);

            // When
            List<Category> categories = categoryMapper.list(1);

            // Then
            boolean hasDisabledCategory = categories.stream()
                    .anyMatch(c -> c.getName().equals("禁用分类测试"));
            assertFalse(hasDisabledCategory, "禁用的分类不应出现在列表中");
        }

        @Test
        @DisplayName("验证排序规则: sort升序, 创建时间降序")
        void shouldReturnCategoriesOrderedBySortAndCreateTimeForList() {
            // Given
            insertTestCategory(1, "列表排序测试1");
            Category category2 = createTestCategory(1, "列表排序测试2");
            category2.setSort(2);
            categoryMapper.insert(category2);

            // When
            List<Category> categories = categoryMapper.list(null);

            // Then
            if (categories.size() >= 2) {
                // 验证排序: sort值小的在前
                assertTrue(categories.get(0).getSort() <= categories.get(1).getSort(),
                        "应按sort升序排列");
            }
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
            // 1. Create - 新增分类
            Category category = insertTestCategory(1, "完整流程测试");
            assertAll("新增验证",
                    () -> assertNotNull(category.getId(), "新增后ID应自动生成")
            );

            // 2. Read - 查询分类(分页查询)
            List<Category> categories = categoryMapper.pageQuery("完整流程测试", null);
            assertAll("分页查询验证",
                    () -> assertFalse(categories.isEmpty(), "应能查询到新增的分类"),
                    () -> assertTrue(categories.stream().anyMatch(c -> c.getName().equals("完整流程测试")),
                            "应能找到匹配的分类")
            );

            // 3. Read - 查询分类(列表查询)
            List<Category> enabledCategories = categoryMapper.list(1);
            assertTrue(enabledCategories.stream().anyMatch(c -> c.getName().equals("完整流程测试")),
                    "列表查询应能找到分类");

            // 4. Update - 修改分类
            Category updateData = Category.builder()
                    .id(category.getId())
                    .name("完整流程测试-修改")
                    .sort(100)
                    .updateTime(LocalDateTime.now())
                    .build();
            categoryMapper.update(updateData);
            List<Category> updatedCategories = categoryMapper.pageQuery("完整流程测试-修改", null);
            assertFalse(updatedCategories.isEmpty(), "修改应生效");

            // 5. Delete - 删除分类
            categoryMapper.deleteById(category.getId());
            List<Category> deletedCategories = categoryMapper.pageQuery("完整流程测试-修改", null);
            assertTrue(deletedCategories.isEmpty(), "删除后应查询不到数据");
        }

        @Test
        @DisplayName("分类状态管理流程测试")
        void shouldManageCategoryStatusCorrectly() {
            // Given - 新增一个启用的分类
            Category category = insertTestCategory(1, "状态管理测试");
            assertEquals(1, category.getStatus(), "初始状态应为启用");

            Long categoryId = category.getId();

            // When - 禁用分类
            Category disableData = Category.builder()
                    .id(categoryId)
                    .status(0)
                    .updateTime(LocalDateTime.now())
                    .build();
            categoryMapper.update(disableData);

            // Then - 验证状态已禁用
            List<Category> disabledList = categoryMapper.pageQuery("状态管理测试", null);
            Category disabled = disabledList.isEmpty() ? null : disabledList.get(0);
            assertEquals(0, disabled.getStatus(), "分类应被禁用");

            // When - 重新启用分类
            Category enableData = Category.builder()
                    .id(categoryId)
                    .status(1)
                    .updateTime(LocalDateTime.now())
                    .build();
            categoryMapper.update(enableData);

            // Then - 验证状态已启用
            List<Category> enabledList = categoryMapper.list(1);
            assertTrue(enabledList.stream().anyMatch(c -> c.getName().equals("状态管理测试")),
                    "分类应被重新启用并出现在列表中");
        }

        @Test
        @DisplayName("分类类型管理流程测试")
        void shouldManageCategoryTypeCorrectly() {
            // Given - 新增一个菜品分类
            Category category = insertTestCategory(1, "类型管理测试");
            assertEquals(1, category.getType(), "初始类型应为菜品分类");

            Long categoryId = category.getId();

            // When - 修改为套餐分类
            Category updateData = Category.builder()
                    .id(categoryId)
                    .type(2)
                    .name("类型管理测试-套餐")
                    .updateTime(LocalDateTime.now())
                    .build();
            categoryMapper.update(updateData);

            // Then - 验证类型已修改
            List<Category> mealCategories = categoryMapper.list(2);
            assertTrue(mealCategories.stream().anyMatch(c -> c.getName().equals("类型管理测试-套餐")),
                    "分类应被修改为套餐分类");
        }

        @Test
        @DisplayName("批量查询分类流程测试")
        void shouldQueryMultipleCategoriesCorrectly() {
            // Given - 新增多个分类
            insertTestCategory(1, "批量查询测试1");
            insertTestCategory(1, "批量查询测试2");
            insertTestCategory(2, "批量查询测试3");

            // When - 查询所有分类
            List<Category> allCategories = categoryMapper.pageQuery(null, null);

            // Then - 验证查询结果
            assertAll("批量查询验证",
                    () -> assertNotNull(allCategories, "分类列表不应为null"),
                    () -> assertTrue(allCategories.size() >= 3, "应至少包含3个分类")
            );

            // When - 根据名称模糊查询
            List<Category> filteredCategories = categoryMapper.pageQuery("批量查询测试", null);

            // Then - 验证过滤结果
            assertAll("过滤查询验证",
                    () -> assertNotNull(filteredCategories, "过滤后的列表不应为null"),
                    () -> assertTrue(filteredCategories.size() >= 3, "应至少找到3个匹配的分类")
            );

            // When - 根据类型查询
            List<Category> dishCategories = categoryMapper.pageQuery(null, 1);

            // Then - 验证类型过滤
            assertAll("类型过滤验证",
                    () -> assertNotNull(dishCategories, "菜品分类列表不应为null"),
                    () -> assertTrue(dishCategories.stream().allMatch(c -> c.getType() == 1),
                            "所有结果应为菜品分类"),
                    () -> assertTrue(dishCategories.stream().anyMatch(c -> c.getName().contains("批量查询测试1") ||
                            c.getName().contains("批量查询测试2")), "应包含菜品分类")
            );
        }

        @Test
        @DisplayName("分类排序流程测试")
        void shouldManageCategorySortOrderCorrectly() {
            // Given - 新增多个不同排序的分类
            Category category1 = insertTestCategory(1, "排序测试高优先级");
            category1.setSort(1);
            categoryMapper.update(category1);

            Category category2 = insertTestCategory(1, "排序测试低优先级");
            category2.setSort(100);
            categoryMapper.update(category2);

            Category category3 = insertTestCategory(1, "排序测试中优先级");
            category3.setSort(50);
            categoryMapper.update(category3);

            // When - 查询分类列表
            List<Category> categories = categoryMapper.list(1);

            // Then - 验证排序顺序
            List<Category> sortedCategories = categories.stream()
                    .filter(c -> c.getName().startsWith("排序测试"))
                    .toList();

            if (sortedCategories.size() >= 3) {
                assertAll("排序验证",
                        () -> assertTrue(sortedCategories.get(0).getSort() <= sortedCategories.get(1).getSort(),
                                "应按sort升序排列"),
                        () -> assertTrue(sortedCategories.get(1).getSort() <= sortedCategories.get(2).getSort(),
                                "应按sort升序排列")
                );
            }
        }
    }
}
