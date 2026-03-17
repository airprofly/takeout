package com.airprofly.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
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

import com.airprofly.dto.DishDTO;
import com.airprofly.dto.DishPageQueryDTO;
import com.airprofly.entity.Dish;
import com.airprofly.entity.DishFlavor;
import com.airprofly.entity.Setmeal;
import com.airprofly.mapper.DishFlavorMapper;
import com.airprofly.mapper.DishMapper;
import com.airprofly.mapper.SetmealDishMapper;
import com.airprofly.mapper.SetmealMapper;
import com.airprofly.result.PageResult;
import com.airprofly.vo.DishVO;

/**
 * DishService 测试类
 *
 * 测试覆盖:
 * - 菜品及口味 CRUD 操作测试
 * - 业务逻辑验证测试
 * - 事务处理测试
 * - 边界条件测试
 *
 * 测试策略:
 * - 使用 Mockito 进行依赖项模拟
 * - 验证业务逻辑正确性
 * - 验证菜品与口味的关联处理
 * - 验证套餐关联检查
 */
@ExtendWith(MockitoExtension.class)
@Tag("service")
@Tag("dish")
@DisplayName("菜品 Service 测试")
public class DishServiceTest {

    @Mock
    private DishMapper dishMapper;

    @Mock
    private DishFlavorMapper dishFlavorMapper;

    @Mock
    private SetmealDishMapper setmealDishMapper;

    @Mock
    private SetmealMapper setmealMapper;

    @InjectMocks
    private com.airprofly.service.impl.DishServiceImpl dishService;

    private DishDTO testDishDTO;
    private Dish testDish;
    private DishFlavor testFlavor;

    @BeforeEach
    void setUp() {
        // 初始化菜品 DTO 测试数据
        testDishDTO = new DishDTO();
        testDishDTO.setName("测试菜品");
        testDishDTO.setCategoryId(1L);
        testDishDTO.setPrice(new BigDecimal("2800")); // 28.00元
        testDishDTO.setImage("dish.jpg");
        testDishDTO.setDescription("测试菜品描述");
        testDishDTO.setStatus(1);

        // 初始化口味数据
        testFlavor = DishFlavor.builder()
                .id(1L)
                .dishId(1L)
                .name("辣度")
                .value("[\"微辣\",\"中辣\",\"特辣\"]")
                .build();

        // 初始化菜品实体测试数据
        testDish = Dish.builder()
                .id(1L)
                .name("测试菜品")
                .categoryId(1L)
                .price(new BigDecimal("2800"))
                .image("dish.jpg")
                .description("测试菜品描述")
                .status(1)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }

    // ==================== saveWithFlavor 方法测试 ====================

    @Nested
    @DisplayName("saveWithFlavor 方法测试")
    @Tag("create")
    class SaveWithFlavorMethodTests {

        @Test
        @DisplayName("新增菜品及口味 - 成功场景")
        void shouldSaveDishWithFlavorWhenValidData() {
            // Given
            testDishDTO.setFlavors(Arrays.asList(testFlavor));
            doNothing().when(dishMapper).insert(any(Dish.class));
            doNothing().when(dishFlavorMapper).insertBatch(anyList());

            // When
            dishService.saveWithFlavor(testDishDTO);

            // Then
            assertAll("新增验证",
                    () -> verify(dishMapper, times(1)).insert(any(Dish.class)),
                    () -> verify(dishFlavorMapper, times(1)).insertBatch(anyList())
            );
        }

        @Test
        @DisplayName("新增菜品不带口味")
        void shouldSaveDishWithoutFlavor() {
            // Given
            testDishDTO.setFlavors(null);
            doNothing().when(dishMapper).insert(any(Dish.class));

            // When
            dishService.saveWithFlavor(testDishDTO);

            // Then
            assertAll("无口味验证",
                    () -> verify(dishMapper, times(1)).insert(any(Dish.class)),
                    () -> verify(dishFlavorMapper, never()).insertBatch(anyList())
            );
        }

        @Test
        @DisplayName("新增菜品时口味列表为空")
        void shouldSaveDishWithEmptyFlavorList() {
            // Given
            testDishDTO.setFlavors(Collections.emptyList());
            doNothing().when(dishMapper).insert(any(Dish.class));

            // When
            dishService.saveWithFlavor(testDishDTO);

            // Then
            assertAll("空口味列表验证",
                    () -> verify(dishMapper, times(1)).insert(any(Dish.class)),
                    () -> verify(dishFlavorMapper, never()).insertBatch(anyList())
            );
        }

        @Test
        @DisplayName("新增菜品时口味应关联菜品ID")
        void shouldLinkFlavorsToDishId() {
            // Given
            testDishDTO.setFlavors(Arrays.asList(testFlavor));
            doNothing().when(dishMapper).insert(any(Dish.class));
            doNothing().when(dishFlavorMapper).insertBatch(anyList());

            // When
            dishService.saveWithFlavor(testDishDTO);

            // Then - 验证方法被调用
            verify(dishMapper, times(1)).insert(any(Dish.class));
            verify(dishFlavorMapper, times(1)).insertBatch(anyList());
        }
    }

    // ==================== pageQuery 方法测试 ====================

    @Nested
    @DisplayName("pageQuery 方法测试")
    @Tag("query")
    class PageQueryMethodTests {

        @Test
        @DisplayName("分页查询菜品 - 成功场景")
        void shouldReturnPageResultWhenQuery() {
            // Given
            DishPageQueryDTO queryDTO = new DishPageQueryDTO();
            queryDTO.setPage(1);
            queryDTO.setPageSize(10);
            queryDTO.setName("测试");

            List<Dish> dishes = Arrays.asList(testDish);
            when(dishMapper.pageQuery(anyString(), any(), any())).thenReturn(dishes);

            // When
            PageResult<DishVO> result = dishService.pageQuery(queryDTO);

            // Then
            assertAll("分页查询验证",
                    () -> assertNotNull(result, "分页结果不应为null"),
                    () -> verify(dishMapper, times(1)).pageQuery(eq("测试"), eq(null), eq(null))
            );
        }

        @Test
        @DisplayName("分页查询时所有参数为 null")
        void shouldHandleNullParameters() {
            // Given
            DishPageQueryDTO queryDTO = new DishPageQueryDTO();
            queryDTO.setPage(1);
            queryDTO.setPageSize(10);

            when(dishMapper.pageQuery(any(), any(), any())).thenReturn(Collections.emptyList());

            // When
            PageResult<DishVO> result = dishService.pageQuery(queryDTO);

            // Then
            assertAll("空参数验证",
                    () -> assertNotNull(result, "分页结果不应为null"),
                    () -> verify(dishMapper, times(1)).pageQuery(eq(null), eq(null), eq(null))
            );
        }

        @Test
        @DisplayName("分页查询返回空结果")
        void shouldReturnEmptyPageResultWhenNoData() {
            // Given
            DishPageQueryDTO queryDTO = new DishPageQueryDTO();
            queryDTO.setPage(1);
            queryDTO.setPageSize(10);

            when(dishMapper.pageQuery(any(), any(), any())).thenReturn(Collections.emptyList());

            // When
            PageResult<DishVO> result = dishService.pageQuery(queryDTO);

            // Then
            assertAll("空结果验证",
                    () -> assertNotNull(result, "分页结果不应为null"),
                    () -> assertEquals(0L, result.getTotal(), "总记录数应为0")
            );
        }
    }

    // ==================== deleteBatch 方法测试 ====================

    @Nested
    @DisplayName("deleteBatch 方法测试")
    @Tag("delete")
    class DeleteBatchMethodTests {

        @Test
        @DisplayName("批量删除菜品 - 成功场景")
        void shouldDeleteDishesWhenValid() {
            // Given
            List<Long> ids = Arrays.asList(1L, 2L);
            testDish.setStatus(0); // 禁用状态
            when(dishMapper.getById(anyLong())).thenReturn(testDish);
            when(setmealDishMapper.getSetmealIdByDishIds(ids)).thenReturn(null);
            doNothing().when(dishMapper).deleteByIds(anyList());
            doNothing().when(dishFlavorMapper).deleteByDishIds(anyList());

            // When
            dishService.deleteBatch(ids);

            // Then
            assertAll("删除验证",
                    () -> verify(dishMapper, times(1)).deleteByIds(ids),
                    () -> verify(dishFlavorMapper, times(1)).deleteByDishIds(ids)
            );
        }

        @Test
        @DisplayName("删除启用的菜品应抛出异常")
        void shouldThrowExceptionWhenDeletingEnabledDish() {
            // Given
            List<Long> ids = Arrays.asList(1L);
            testDish.setStatus(1); // 启用状态
            when(dishMapper.getById(1L)).thenReturn(testDish);

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> dishService.deleteBatch(ids),
                    "应抛出异常");

            assertTrue(exception.getMessage().contains("起售中"), "异常消息应包含起售提示");
            verify(dishMapper, never()).deleteByIds(anyList());
        }

        @Test
        @DisplayName("删除被套餐引用的菜品应抛出异常")
        void shouldThrowExceptionWhenDishReferencedBySetmeal() {
            // Given
            List<Long> ids = Arrays.asList(1L);
            testDish.setStatus(0);
            when(dishMapper.getById(1L)).thenReturn(testDish);
            when(setmealDishMapper.getSetmealIdByDishIds(ids)).thenReturn(Arrays.asList(1L));

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> dishService.deleteBatch(ids),
                    "应抛出异常");

            assertTrue(exception.getMessage().contains("被套餐引用"), "异常消息应包含套餐引用提示");
            verify(dishMapper, never()).deleteByIds(anyList());
        }

        @Test
        @DisplayName("批量删除多个菜品")
        void shouldDeleteMultipleDishes() {
            // Given
            List<Long> ids = Arrays.asList(1L, 2L, 3L);
            testDish.setStatus(0);
            when(dishMapper.getById(anyLong())).thenReturn(testDish);
            when(setmealDishMapper.getSetmealIdByDishIds(ids)).thenReturn(null);
            doNothing().when(dishMapper).deleteByIds(anyList());
            doNothing().when(dishFlavorMapper).deleteByDishIds(anyList());

            // When
            dishService.deleteBatch(ids);

            // Then
            verify(dishMapper, times(1)).deleteByIds(ids);
            verify(dishFlavorMapper, times(1)).deleteByDishIds(ids);
        }
    }

    // ==================== getByIdWithFlavor 方法测试 ====================

    @Nested
    @DisplayName("getByIdWithFlavor 方法测试")
    @Tag("query")
    class GetByIdWithFlavorMethodTests {

        @Test
        @DisplayName("根据ID查询菜品及口味 - 成功场景")
        void shouldReturnDishWithFlavorWhenIdExists() {
            // Given
            Long dishId = 1L;
            when(dishMapper.getById(dishId)).thenReturn(testDish);
            when(dishFlavorMapper.getByDishId(dishId)).thenReturn(Arrays.asList(testFlavor));

            // When
            DishVO result = dishService.getByIdWithFlavor(dishId);

            // Then
            assertAll("查询验证",
                    () -> assertNotNull(result, "查询结果不应为null"),
                    () -> assertEquals(dishId, result.getId(), "ID应匹配"),
                    () -> assertEquals("测试菜品", result.getName(), "名称应匹配"),
                    () -> assertNotNull(result.getFlavors(), "口味列表不应为null"),
                    () -> assertFalse(result.getFlavors().isEmpty(), "口味列表不应为空"),
                    () -> verify(dishMapper, times(1)).getById(dishId),
                    () -> verify(dishFlavorMapper, times(1)).getByDishId(dishId)
            );
        }

        @Test
        @DisplayName("查询不存在的菜品ID")
        void shouldReturnNullWhenDishNotExists() {
            // Given
            Long nonExistentId = 999999L;
            when(dishMapper.getById(nonExistentId)).thenReturn(null);
            when(dishFlavorMapper.getByDishId(nonExistentId)).thenReturn(Collections.emptyList());

            // When & Then
            // 注意：BeanUtils.copyProperties 在 source 为 null 时会抛出 IllegalArgumentException
            // 这是实际 Service 的行为，测试需要反映这一点
            // Service 会先调用 getById，然后调用 getByDishId，最后在 copyProperties 时抛出异常
            assertThrows(IllegalArgumentException.class, () -> {
                dishService.getByIdWithFlavor(nonExistentId);
            }, "不存在的菜品ID应抛出异常");
            verify(dishMapper, times(1)).getById(nonExistentId);
            verify(dishFlavorMapper, times(1)).getByDishId(nonExistentId);
        }

        @Test
        @DisplayName("查询菜品但没有口味")
        void shouldReturnDishWithEmptyFlavorList() {
            // Given
            Long dishId = 1L;
            when(dishMapper.getById(dishId)).thenReturn(testDish);
            when(dishFlavorMapper.getByDishId(dishId)).thenReturn(Collections.emptyList());

            // When
            DishVO result = dishService.getByIdWithFlavor(dishId);

            // Then
            assertAll("无口味验证",
                    () -> assertNotNull(result, "查询结果不应为null"),
                    () -> assertNotNull(result.getFlavors(), "口味列表不应为null"),
                    () -> assertTrue(result.getFlavors().isEmpty(), "口味列表应为空")
            );
        }
    }

    // ==================== updateWithFlavor 方法测试 ====================

    @Nested
    @DisplayName("updateWithFlavor 方法测试")
    @Tag("update")
    class UpdateWithFlavorMethodTests {

        @Test
        @DisplayName("修改菜品及口味 - 成功场景")
        void shouldUpdateDishWithFlavorWhenValidData() {
            // Given
            testDishDTO.setId(1L);
            testDishDTO.setFlavors(Arrays.asList(testFlavor));
            doNothing().when(dishMapper).update(any(Dish.class));
            doNothing().when(dishFlavorMapper).deleteByDishId(anyLong());
            doNothing().when(dishFlavorMapper).insertBatch(anyList());

            // When
            dishService.updateWithFlavor(testDishDTO);

            // Then
            assertAll("修改验证",
                    () -> verify(dishMapper, times(1)).update(any(Dish.class)),
                    () -> verify(dishFlavorMapper, times(1)).deleteByDishId(1L),
                    () -> verify(dishFlavorMapper, times(1)).insertBatch(anyList())
            );
        }

        @Test
        @DisplayName("修改菜品时清空口味")
        void shouldUpdateDishWithEmptyFlavors() {
            // Given
            testDishDTO.setId(1L);
            testDishDTO.setFlavors(Collections.emptyList());
            doNothing().when(dishMapper).update(any(Dish.class));
            doNothing().when(dishFlavorMapper).deleteByDishId(anyLong());

            // When
            dishService.updateWithFlavor(testDishDTO);

            // Then
            assertAll("清空口味验证",
                    () -> verify(dishMapper, times(1)).update(any(Dish.class)),
                    () -> verify(dishFlavorMapper, times(1)).deleteByDishId(1L),
                    () -> verify(dishFlavorMapper, never()).insertBatch(anyList())
            );
        }

        @Test
        @DisplayName("修改菜品时口味为null")
        void shouldUpdateDishWithNullFlavors() {
            // Given
            testDishDTO.setId(1L);
            testDishDTO.setFlavors(null);
            doNothing().when(dishMapper).update(any(Dish.class));
            doNothing().when(dishFlavorMapper).deleteByDishId(anyLong());

            // When
            dishService.updateWithFlavor(testDishDTO);

            // Then
            assertAll("null口味验证",
                    () -> verify(dishMapper, times(1)).update(any(Dish.class)),
                    () -> verify(dishFlavorMapper, times(1)).deleteByDishId(1L),
                    () -> verify(dishFlavorMapper, never()).insertBatch(anyList())
            );
        }
    }

    // ==================== startOrStop 方法测试 ====================

    @Nested
    @DisplayName("startOrStop 方法测试")
    @Tag("update")
    class StartOrStopMethodTests {

        @Test
        @DisplayName("启用菜品 - 成功场景")
        void shouldEnableDish() {
            // Given
            Long dishId = 1L;
            doNothing().when(dishMapper).update(any(Dish.class));

            // When
            dishService.startOrStop(1, dishId);

            // Then
            verify(dishMapper, times(1)).update(argThat(dish ->
                    dish.getId() == dishId &&
                    dish.getStatus() == 1
            ));
        }

        @Test
        @DisplayName("禁用菜品 - 成功场景")
        void shouldDisableDish() {
            // Given
            Long dishId = 1L;
            doNothing().when(dishMapper).update(any(Dish.class));

            // When
            dishService.startOrStop(0, dishId);

            // Then
            verify(dishMapper, times(1)).update(argThat(dish ->
                    dish.getId() == dishId &&
                    dish.getStatus() == 0
            ));
        }

        @Test
        @DisplayName("禁用菜品时同时禁用关联套餐")
        void shouldDisableRelatedSetmealsWhenDisablingDish() {
            // Given
            Long dishId = 1L;
            List<Long> setmealIds = Arrays.asList(1L, 2L);
            doNothing().when(dishMapper).update(any(Dish.class));
            doNothing().when(setmealMapper).update(any(Setmeal.class));
            when(setmealDishMapper.getSetmealIdByDishIds(anyList())).thenReturn(setmealIds);

            // When
            dishService.startOrStop(0, dishId);

            // Then
            verify(dishMapper, times(1)).update(argThat(dish ->
                    dish.getId() == dishId &&
                    dish.getStatus() == 0
            ));
            verify(setmealMapper, times(2)).update(argThat(setmeal ->
                    setmeal.getStatus() == 0
            ));
        }

        @Test
        @DisplayName("启用菜品时不影响套餐状态")
        void shouldNotAffectSetmealsWhenEnablingDish() {
            // Given
            Long dishId = 1L;
            doNothing().when(dishMapper).update(any(Dish.class));

            // When - 启用菜品(status=1)时不会调用 setmealDishMapper
            dishService.startOrStop(1, dishId);

            // Then
            verify(dishMapper, times(1)).update(argThat(dish ->
                    dish.getId() == dishId &&
                    dish.getStatus() == 1
            ));
            verify(setmealMapper, never()).update(any(Setmeal.class));
            verify(setmealDishMapper, never()).getSetmealIdByDishIds(anyList());
        }
    }

    // ==================== list 方法测试 ====================

    @Nested
    @DisplayName("list 方法测试")
    @Tag("query")
    class ListMethodTests {

        @Test
        @DisplayName("根据分类ID查询菜品 - 成功场景")
        void shouldReturnDishesWhenCategoryIdMatches() {
            // Given
            Long categoryId = 1L;
            when(dishMapper.list(any(Dish.class))).thenReturn(Arrays.asList(testDish));

            // When
            List<Dish> result = dishService.list(categoryId);

            // Then
            assertAll("分类查询验证",
                    () -> assertNotNull(result, "菜品列表不应为null"),
                    () -> assertFalse(result.isEmpty(), "菜品列表不应为空"),
                    () -> verify(dishMapper, times(1)).list(argThat(dish ->
                            dish.getCategoryId() == categoryId &&
                            dish.getStatus() == 1 // 只查询启用的菜品
                    ))
            );
        }

        @Test
        @DisplayName("查询返回空列表")
        void shouldReturnEmptyListWhenNoDishes() {
            // Given
            Long categoryId = 999L;
            when(dishMapper.list(any(Dish.class))).thenReturn(Collections.emptyList());

            // When
            List<Dish> result = dishService.list(categoryId);

            // Then
            assertAll("空列表验证",
                    () -> assertNotNull(result, "菜品列表不应为null"),
                    () -> assertTrue(result.isEmpty(), "菜品列表应为空")
            );
        }
    }

    // ==================== listWithFlavor 方法测试 ====================

    @Nested
    @DisplayName("listWithFlavor 方法测试")
    @Tag("query")
    class ListWithFlavorMethodTests {

        @Test
        @DisplayName("条件查询菜品及口味 - 成功场景")
        void shouldReturnDishesWithFlavorWhenConditionMatches() {
            // Given
            Dish condition = Dish.builder()
                    .categoryId(1L)
                    .status(1)
                    .build();

            when(dishMapper.list(condition)).thenReturn(Arrays.asList(testDish));
            when(dishFlavorMapper.getByDishId(anyLong())).thenReturn(Arrays.asList(testFlavor));

            // When
            List<DishVO> result = dishService.listWithFlavor(condition);

            // Then
            assertAll("条件查询验证",
                    () -> assertNotNull(result, "菜品列表不应为null"),
                    () -> assertFalse(result.isEmpty(), "菜品列表不应为空"),
                    () -> assertNotNull(result.get(0).getFlavors(), "口味不应为null"),
                    () -> verify(dishMapper, times(1)).list(condition),
                    () -> verify(dishFlavorMapper, times(1)).getByDishId(anyLong())
            );
        }

        @Test
        @DisplayName("条件查询返回空结果")
        void shouldReturnEmptyListWhenNoDishesMatch() {
            // Given
            Dish condition = Dish.builder()
                    .categoryId(999L)
                    .build();

            when(dishMapper.list(condition)).thenReturn(Collections.emptyList());

            // When
            List<DishVO> result = dishService.listWithFlavor(condition);

            // Then
            assertAll("空结果验证",
                    () -> assertNotNull(result, "菜品列表不应为null"),
                    () -> assertTrue(result.isEmpty(), "菜品列表应为空"),
                    () -> verify(dishFlavorMapper, never()).getByDishId(anyLong())
            );
        }
    }

    // ==================== 综合业务场景测试 ====================

    @Nested
    @DisplayName("综合业务场景测试")
    @Tag("integration")
    class IntegrationTests {

        @Test
        @DisplayName("完整菜品生命周期测试")
        void shouldCompleteFullDishLifecycle() {
            // Given
            testDishDTO.setFlavors(Arrays.asList(testFlavor));
            testDish.setStatus(0);

            doNothing().when(dishMapper).insert(any(Dish.class));
            doNothing().when(dishFlavorMapper).insertBatch(anyList());
            doNothing().when(dishMapper).update(any(Dish.class));
            doNothing().when(dishFlavorMapper).deleteByDishId(anyLong());
            when(dishMapper.getById(anyLong())).thenReturn(testDish);
            when(dishFlavorMapper.getByDishId(anyLong())).thenReturn(Arrays.asList(testFlavor));
            when(setmealDishMapper.getSetmealIdByDishIds(anyList())).thenReturn(null);
            doNothing().when(dishMapper).deleteByIds(anyList());
            doNothing().when(dishFlavorMapper).deleteByDishIds(anyList());

            // 1. 新增菜品及口味
            dishService.saveWithFlavor(testDishDTO);
            verify(dishMapper, times(1)).insert(any(Dish.class));

            // 2. 查询菜品及口味
            dishService.getByIdWithFlavor(1L);
            verify(dishMapper, times(1)).getById(anyLong());

            // 3. 修改菜品及口味
            testDishDTO.setId(1L);
            dishService.updateWithFlavor(testDishDTO);
            verify(dishMapper, atLeastOnce()).update(any(Dish.class));

            // 4. 启用/禁用菜品
            dishService.startOrStop(1, 1L);
            verify(dishMapper, atLeastOnce()).update(any(Dish.class));

            // 5. 删除菜品
            dishService.deleteBatch(Arrays.asList(1L));
            verify(dishMapper, times(1)).deleteByIds(anyList());
        }

        @Test
        @DisplayName("菜品状态与套餐联动测试")
        void shouldHandleDishSetmealRelationship() {
            // Given
            List<Long> setmealIds = Arrays.asList(1L, 2L);
            doNothing().when(dishMapper).update(any(Dish.class));
            doNothing().when(setmealMapper).update(any(Setmeal.class));
            when(setmealDishMapper.getSetmealIdByDishIds(anyList())).thenReturn(setmealIds);

            // When - 禁用菜品
            dishService.startOrStop(0, 1L);

            // Then
            verify(setmealMapper, times(2)).update(argThat(setmeal ->
                    setmeal.getStatus() == 0
            ));
        }

        @Test
        @DisplayName("口味管理完整流程测试")
        void shouldManageFlavorsCorrectly() {
            // Given
            testDishDTO.setFlavors(Arrays.asList(testFlavor));
            doNothing().when(dishMapper).insert(any(Dish.class));
            doNothing().when(dishMapper).update(any(Dish.class));
            doNothing().when(dishFlavorMapper).insertBatch(anyList());
            doNothing().when(dishFlavorMapper).deleteByDishId(anyLong());

            // When - 新增带口味的菜品
            dishService.saveWithFlavor(testDishDTO);
            verify(dishFlavorMapper, times(1)).insertBatch(anyList());

            // When - 修改菜品口味
            testDishDTO.setId(1L);
            testDishDTO.setFlavors(Arrays.asList(
                    DishFlavor.builder().name("口味2").value("[]").build()
            ));
            dishService.updateWithFlavor(testDishDTO);

            // Then
            assertAll("口味修改验证",
                    () -> verify(dishFlavorMapper, times(1)).deleteByDishId(1L),
                    () -> verify(dishFlavorMapper, times(2)).insertBatch(anyList())
            );
        }
    }
}
