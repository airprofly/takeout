package com.airprofly.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.airprofly.dto.CategoryDTO;
import com.airprofly.dto.CategoryPageQueryDTO;
import com.airprofly.entity.Category;
import com.airprofly.mapper.CategoryMapper;
import com.airprofly.mapper.DishMapper;
import com.airprofly.mapper.SetmealMapper;
import com.airprofly.result.PageResult;

/**
 * CategoryService 测试类
 *
 * 测试覆盖:
 * - CRUD 操作测试
 * - 业务逻辑验证测试
 * - 异常场景测试
 * - 边界条件测试
 *
 * 测试策略:
 * - 使用 Mockito 进行依赖项模拟
 * - 验证业务逻辑正确性
 * - 验证异常抛出场景
 * - 验证 Mapper 调用情况
 */
@ExtendWith(MockitoExtension.class)
@Tag("service")
@Tag("category")
@DisplayName("分类 Service 测试")
public class CategoryServiceTest {

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private DishMapper dishMapper;

    @Mock
    private SetmealMapper setmealMapper;

    @InjectMocks
    private com.airprofly.service.impl.CategoryServiceImpl categoryService;

    private CategoryDTO testCategoryDTO;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        testCategoryDTO = new CategoryDTO();
        testCategoryDTO.setType(1);
        testCategoryDTO.setName("测试菜品分类");
        testCategoryDTO.setSort(1);

        testCategory = Category.builder()
                .id(1L)
                .type(1)
                .name("测试菜品分类")
                .sort(1)
                .status(0)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }

    // ==================== save 方法测试 ====================

    @Nested
    @DisplayName("save 方法测试")
    @Tag("create")
    class SaveMethodTests {

        @Test
        @DisplayName("新增分类 - 成功场景")
        void shouldSaveCategoryWhenValidData() {
            // Given
            doNothing().when(categoryMapper).insert(any(Category.class));

            // When
            categoryService.save(testCategoryDTO);

            // Then
            verify(categoryMapper, times(1)).insert(any(Category.class));
        }

        @Test
        @DisplayName("新增分类时状态应默认设置为禁用")
        void shouldSetStatusToDisabledWhenSaving() {
            // Given
            // CategoryDTO 没有 status 字段，状态由服务层设置

            // When
            categoryService.save(testCategoryDTO);

            // Then
            verify(categoryMapper).insert(argThat(category -> category.getStatus() == 0));
        }

        @Test
        @DisplayName("新增分类时 DTO 字段应正确复制到 Entity")
        void shouldCopyDTOFieldsToEntity() {
            // Given
            testCategoryDTO.setType(2);
            testCategoryDTO.setName("测试套餐分类");
            testCategoryDTO.setSort(10);

            // When
            categoryService.save(testCategoryDTO);

            // Then
            verify(categoryMapper).insert(argThat(category ->
                    category.getType() == 2 &&
                    "测试套餐分类".equals(category.getName()) &&
                    category.getSort() == 10
            ));
        }
    }

    // ==================== pageQuery 方法测试 ====================

    @Nested
    @DisplayName("pageQuery 方法测试")
    @Tag("query")
    class PageQueryMethodTests {

        @Test
        @DisplayName("分页查询分类 - 成功场景")
        void shouldReturnPageResultWhenQuery() {
            // Given
            CategoryPageQueryDTO queryDTO = new CategoryPageQueryDTO();
            queryDTO.setPage(1);
            queryDTO.setPageSize(10);
            queryDTO.setName("测试");
            queryDTO.setType(1);

            List<Category> categories = Arrays.asList(testCategory);
            when(categoryMapper.pageQuery(anyString(), any())).thenReturn(categories);

            // When
            PageResult<Category> result = categoryService.pageQuery(queryDTO);

            // Then
            assertAll("分页查询验证",
                    () -> assertNotNull(result, "分页结果不应为null"),
                    () -> verify(categoryMapper, times(1)).pageQuery(eq("测试"), eq(1))
            );
        }

        @Test
        @DisplayName("分页查询时传入 null 参数")
        void shouldHandleNullParameters() {
            // Given
            CategoryPageQueryDTO queryDTO = new CategoryPageQueryDTO();
            queryDTO.setPage(1);
            queryDTO.setPageSize(10);

            when(categoryMapper.pageQuery(any(), any())).thenReturn(Collections.emptyList());

            // When
            PageResult<Category> result = categoryService.pageQuery(queryDTO);

            // Then
            assertAll("空参数验证",
                    () -> assertNotNull(result, "分页结果不应为null"),
                    () -> verify(categoryMapper, times(1)).pageQuery(eq(null), eq(null))
            );
        }

        @Test
        @DisplayName("分页查询返回空结果")
        void shouldReturnEmptyPageResultWhenNoData() {
            // Given
            CategoryPageQueryDTO queryDTO = new CategoryPageQueryDTO();
            queryDTO.setPage(1);
            queryDTO.setPageSize(10);

            when(categoryMapper.pageQuery(any(), any())).thenReturn(Collections.emptyList());

            // When
            PageResult<Category> result = categoryService.pageQuery(queryDTO);

            // Then
            assertAll("空结果验证",
                    () -> assertNotNull(result, "分页结果不应为null"),
                    () -> assertEquals(0L, result.getTotal(), "总记录数应为0")
            );
        }
    }

    // ==================== deleteById 方法测试 ====================

    @Nested
    @DisplayName("deleteById 方法测试")
    @Tag("delete")
    class DeleteByIdMethodTests {

        @Test
        @DisplayName("删除分类 - 成功场景")
        void shouldDeleteCategoryWhenNoReferences() {
            // Given
            Long categoryId = 1L;
            when(dishMapper.countByCategoryId(categoryId)).thenReturn(0);
            when(setmealMapper.countByCategoryId(categoryId)).thenReturn(0);
            doNothing().when(categoryMapper).deleteById(categoryId);

            // When
            categoryService.deleteById(categoryId);

            // Then
            assertAll("删除验证",
                    () -> verify(dishMapper, times(1)).countByCategoryId(categoryId),
                    () -> verify(setmealMapper, times(1)).countByCategoryId(categoryId),
                    () -> verify(categoryMapper, times(1)).deleteById(categoryId)
            );
        }

        @Test
        @DisplayName("删除有关联菜品的分类应抛出异常")
        void shouldThrowExceptionWhenCategoryHasDishes() {
            // Given
            Long categoryId = 1L;
            when(dishMapper.countByCategoryId(categoryId)).thenReturn(5);

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> categoryService.deleteById(categoryId),
                    "应抛出异常");

            assertTrue(exception.getMessage().contains("有菜品"), "异常消息应包含菜品提示");
            verify(categoryMapper, never()).deleteById(anyLong());
        }

        @Test
        @DisplayName("删除有关联套餐的分类应抛出异常")
        void shouldThrowExceptionWhenCategoryHasSetmeals() {
            // Given
            Long categoryId = 1L;
            when(dishMapper.countByCategoryId(categoryId)).thenReturn(0);
            when(setmealMapper.countByCategoryId(categoryId)).thenReturn(3);

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> categoryService.deleteById(categoryId),
                    "应抛出异常");

            assertTrue(exception.getMessage().contains("有套餐"), "异常消息应包含套餐提示");
            verify(categoryMapper, never()).deleteById(anyLong());
        }

        @Test
        @DisplayName("删除既有菜品又有套餐的分类应先检测菜品")
        void shouldCheckDishesFirstWhenCategoryHasBoth() {
            // Given
            Long categoryId = 1L;
            when(dishMapper.countByCategoryId(categoryId)).thenReturn(5);

            // When & Then
            assertThrows(RuntimeException.class, () -> categoryService.deleteById(categoryId));

            verify(setmealMapper, never()).countByCategoryId(anyLong());
            verify(categoryMapper, never()).deleteById(anyLong());
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
            testCategoryDTO.setId(1L);
            testCategoryDTO.setName("修改后分类名称");
            testCategoryDTO.setSort(10);

            doNothing().when(categoryMapper).update(any(Category.class));

            // When
            categoryService.update(testCategoryDTO);

            // Then
            verify(categoryMapper, times(1)).update(argThat(category ->
                    category.getId() == 1L &&
                    "修改后分类名称".equals(category.getName()) &&
                    category.getSort() == 10
            ));
        }

        @Test
        @DisplayName("修改分类时 DTO 字段应正确复制到 Entity")
        void shouldCopyDTOFieldsWhenUpdating() {
            // Given
            testCategoryDTO.setId(1L);
            testCategoryDTO.setType(2);
            testCategoryDTO.setName("套餐分类");
            testCategoryDTO.setSort(5);
            // CategoryDTO 没有 status 字段

            doNothing().when(categoryMapper).update(any(Category.class));

            // When
            categoryService.update(testCategoryDTO);

            // Then
            verify(categoryMapper).update(argThat(category ->
                    category.getId() == 1L &&
                    category.getType() == 2 &&
                    "套餐分类".equals(category.getName()) &&
                    category.getSort() == 5
            ));
        }
    }

    // ==================== startOrStop 方法测试 ====================

    @Nested
    @DisplayName("startOrStop 方法测试")
    @Tag("update")
    class StartOrStopMethodTests {

        @Test
        @DisplayName("启用分类 - 成功场景")
        void shouldEnableCategory() {
            // Given
            Long categoryId = 1L;
            doNothing().when(categoryMapper).update(any(Category.class));

            // When
            categoryService.startOrStop(1, categoryId);

            // Then
            verify(categoryMapper, times(1)).update(argThat(category ->
                    category.getId() == categoryId &&
                    category.getStatus() == 1
            ));
        }

        @Test
        @DisplayName("禁用分类 - 成功场景")
        void shouldDisableCategory() {
            // Given
            Long categoryId = 1L;
            doNothing().when(categoryMapper).update(any(Category.class));

            // When
            categoryService.startOrStop(0, categoryId);

            // Then
            verify(categoryMapper, times(1)).update(argThat(category ->
                    category.getId() == categoryId &&
                    category.getStatus() == 0
            ));
        }

        @ParameterizedTest
        @CsvSource({
                "0, 禁用",
                "1, 启用"
        })
        @DisplayName("设置分类状态")
        @Tag("parameterized")
        void shouldSetCategoryStatus(Integer status, String statusDesc) {
            // Given
            Long categoryId = 1L;
            doNothing().when(categoryMapper).update(any(Category.class));

            // When
            categoryService.startOrStop(status, categoryId);

            // Then
            verify(categoryMapper).update(argThat(category ->
                    category.getId() == categoryId &&
                    category.getStatus() == status
            ));
        }
    }

    // ==================== list 方法测试 ====================

    @Nested
    @DisplayName("list 方法测试")
    @Tag("query")
    class ListMethodTests {

        @Test
        @DisplayName("根据类型查询分类 - 成功场景")
        void shouldReturnCategoriesWhenTypeMatches() {
            // Given
            Integer type = 1;
            List<Category> categories = Arrays.asList(testCategory);
            when(categoryMapper.list(type)).thenReturn(categories);

            // When
            List<Category> result = categoryService.list(type);

            // Then
            assertAll("类型查询验证",
                    () -> assertNotNull(result, "分类列表不应为null"),
                    () -> assertFalse(result.isEmpty(), "分类列表不应为空"),
                    () -> verify(categoryMapper, times(1)).list(type)
            );
        }

        @Test
        @DisplayName("查询所有分类(类型为 null)")
        void shouldReturnAllCategoriesWhenTypeIsNull() {
            // Given
            List<Category> categories = Arrays.asList(
                    testCategory,
                    Category.builder().id(2L).type(2).name("套餐分类").build()
            );
            when(categoryMapper.list(null)).thenReturn(categories);

            // When
            List<Category> result = categoryService.list(null);

            // Then
            assertAll("查询所有分类验证",
                    () -> assertNotNull(result, "分类列表不应为null"),
                    () -> assertEquals(2, result.size(), "应返回所有分类"),
                    () -> verify(categoryMapper, times(1)).list(null)
            );
        }

        @Test
        @DisplayName("查询返回空列表")
        void shouldReturnEmptyListWhenNoCategories() {
            // Given
            when(categoryMapper.list(any())).thenReturn(Collections.emptyList());

            // When
            List<Category> result = categoryService.list(1);

            // Then
            assertAll("空列表验证",
                    () -> assertNotNull(result, "分类列表不应为null"),
                    () -> assertTrue(result.isEmpty(), "分类列表应为空")
            );
        }

        @ParameterizedTest
        @CsvSource({
                "1, 菜品分类",
                "2, 套餐分类"
        })
        @DisplayName("根据不同类型查询")
        @Tag("parameterized")
        void shouldQueryByDifferentTypes(Integer type, String typeDesc) {
            // Given
            when(categoryMapper.list(type)).thenReturn(Arrays.asList(testCategory));

            // When
            List<Category> result = categoryService.list(type);

            // Then
            assertAll("类型查询验证",
                    () -> assertNotNull(result, "分类列表不应为null"),
                    () -> verify(categoryMapper, times(1)).list(type)
            );
        }
    }

    // ==================== 综合业务场景测试 ====================

    @Nested
    @DisplayName("综合业务场景测试")
    @Tag("integration")
    class IntegrationTests {

        @Test
        @DisplayName("完整分类生命周期测试")
        void shouldCompleteFullCategoryLifecycle() {
            // Given
            when(dishMapper.countByCategoryId(anyLong())).thenReturn(0);
            when(setmealMapper.countByCategoryId(anyLong())).thenReturn(0);

            // 1. 新增分类
            categoryService.save(testCategoryDTO);
            verify(categoryMapper, times(1)).insert(any(Category.class));

            // 2. 修改分类
            testCategoryDTO.setId(1L);
            testCategoryDTO.setName("修改后分类");
            categoryService.update(testCategoryDTO);
            verify(categoryMapper, times(1)).update(any(Category.class));

            // 3. 启用分类
            categoryService.startOrStop(1, 1L);
            verify(categoryMapper, atLeastOnce()).update(any(Category.class));

            // 4. 删除分类
            categoryService.deleteById(1L);
            verify(categoryMapper, times(1)).deleteById(1L);
        }

        @Test
        @DisplayName("分类状态切换测试")
        void shouldToggleCategoryStatus() {
            // Given
            Long categoryId = 1L;
            doNothing().when(categoryMapper).update(any(Category.class));

            // When - 启用
            categoryService.startOrStop(1, categoryId);

            // Then
            verify(categoryMapper).update(argThat(c -> c.getStatus() == 1));

            // When - 禁用
            categoryService.startOrStop(0, categoryId);

            // Then
            verify(categoryMapper).update(argThat(c -> c.getStatus() == 0));
        }

        @Test
        @DisplayName("防止删除有关联数据的分类")
        void shouldPreventDeletionWhenCategoryHasReferences() {
            // Given - 菜品关联
            Long categoryId = 1L;
            when(dishMapper.countByCategoryId(categoryId)).thenReturn(1);

            // When & Then - 应抛出异常
            assertThrows(RuntimeException.class, () -> categoryService.deleteById(categoryId));

            // Given - 套餐关联
            when(dishMapper.countByCategoryId(categoryId)).thenReturn(0);
            when(setmealMapper.countByCategoryId(categoryId)).thenReturn(1);

            // When & Then - 应抛出异常
            assertThrows(RuntimeException.class, () -> categoryService.deleteById(categoryId));

            // 验证删除未被调用
            verify(categoryMapper, never()).deleteById(anyLong());
        }
    }
}
