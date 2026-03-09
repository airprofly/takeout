package com.airprofly.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

import com.airprofly.entity.Setmeal;

/**
 * SetmealMapper 测试类
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
@Tag("setmeal")
@DisplayName("套餐 Mapper 测试")
public class SetmealMapperTest {

    @Autowired
    private SetmealMapper setmealMapper;

    // ==================== 测试数据工厂方法 ====================

    /**
     * 创建标准的测试套餐对象
     */
    private Setmeal createTestSetmeal(String name, Long categoryId) {
        return Setmeal.builder()
                .categoryId(categoryId)
                .name(name)
                .price(new BigDecimal("58.00"))
                .status(1)
                .description("测试套餐描述")
                .image("setmeal.jpg")
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建并插入测试套餐,返回插入后的对象(包含生成的ID)
     */
    private Setmeal insertTestSetmeal(String name, Long categoryId) {
        Setmeal setmeal = createTestSetmeal(name, categoryId);
        setmealMapper.insert(setmeal);
        return setmeal;
    }

    // ==================== countByCategoryId 方法测试 ====================

    @Nested
    @DisplayName("countByCategoryId 方法测试")
    @Tag("query")
    class CountByCategoryIdMethodTests {

        @Test
        @DisplayName("根据分类ID统计套餐数量 - 成功场景")
        void shouldReturnCountWhenCategoryIdExists() {
            // Given
            Long categoryId = 1L;
            insertTestSetmeal("测试套餐1", categoryId);
            insertTestSetmeal("测试套餐2", categoryId);

            // When
            Integer count = setmealMapper.countByCategoryId(categoryId);

            // Then
            assertAll("统计验证",
                    () -> assertNotNull(count, "统计结果不应为null"),
                    () -> assertTrue(count >= 2, "应至少有2个套餐")
            );
        }

        @Test
        @DisplayName("分类ID不存在时返回0")
        void shouldReturnZeroWhenCategoryIdNotExists() {
            // Given
            Long nonExistentCategoryId = 999999L;

            // When
            Integer count = setmealMapper.countByCategoryId(nonExistentCategoryId);

            // Then
            assertEquals(0, count, "不存在的分类ID应返回0");
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("分类ID为null时返回0")
        @Tag("edge-case")
        void shouldReturnZeroWhenCategoryIdIsNull(Long categoryId) {
            // When
            Integer count = setmealMapper.countByCategoryId(categoryId);

            // Then
            assertEquals(0, count, "null分类ID应返回0");
        }
    }

    // ==================== insert 方法测试 ====================

    @Nested
    @DisplayName("insert 方法测试")
    @Tag("create")
    class InsertMethodTests {

        @Test
        @DisplayName("新增套餐 - 成功场景")
        void shouldInsertSetmealWhenValidData() {
            // Given
            Setmeal setmeal = createTestSetmeal("新套餐", 1L);

            // When
            setmealMapper.insert(setmeal);

            // Then
            assertAll("插入验证",
                    () -> assertNotNull(setmeal.getId(), "插入后ID应自动生成"),
                    () -> {
                        Setmeal inserted = setmealMapper.getById(setmeal.getId());
                        assertNotNull(inserted, "插入的数据应能被查询到");
                        assertEquals("新套餐", inserted.getName(), "套餐名称应匹配");
                        assertEquals(new BigDecimal("58.00"), inserted.getPrice(), "价格应匹配");
                    }
            );
        }

        @Test
        @DisplayName("必填字段为null时应失败")
        @Tag("edge-case")
        void shouldFailToInsertWhenRequiredFieldsAreNull() {
            // Given
            Setmeal setmeal = Setmeal.builder()
                    .categoryId(1L)
                    // name 为 null
                    .price(new BigDecimal("58.00"))
                    .build();

            // When & Then
            assertThrows(DataIntegrityViolationException.class, () -> {
                setmealMapper.insert(setmeal);
            }, "必填字段为null时应抛出异常");
        }

        @ParameterizedTest
        @CsvSource({
            "0, 停用",
            "1, 启用"
        })
        @DisplayName("新增不同状态的套餐")
        @Tag("parameterized")
        void shouldInsertSetmealWithDifferentStatus(Integer status, String statusDesc) {
            // Given
            Setmeal setmeal = createTestSetmeal("状态套餐" + status, 1L);
            setmeal.setStatus(status);

            // When
            setmealMapper.insert(setmeal);

            // Then
            Setmeal inserted = setmealMapper.getById(setmeal.getId());
            assertAll("状态验证",
                    () -> assertNotNull(inserted, "应成功插入"),
                    () -> assertEquals(status, inserted.getStatus(), "状态应匹配")
            );
        }
    }

    // ==================== pageQuery 方法测试 ====================

    @Nested
    @DisplayName("pageQuery 方法测试")
    @Tag("query")
    class PageQueryMethodTests {

        @Test
        @DisplayName("分页查询套餐 - 成功场景")
        void shouldReturnSetmealListWhenQuery() {
            // Given
            insertTestSetmeal("分页测试套餐", 1L);

            // When
            List<Setmeal> setmeals = setmealMapper.pageQuery(null, null, null);

            // Then
            assertAll("分页查询验证",
                    () -> assertNotNull(setmeals, "套餐列表不应为null"),
                    () -> assertFalse(setmeals.isEmpty(), "套餐列表不应为空")
            );
        }

        @Test
        @DisplayName("根据套餐名称模糊查询")
        void shouldReturnSetmealListWhenNameMatches() {
            // Given
            insertTestSetmeal("超值双人套餐", 1L);

            // When
            List<Setmeal> setmeals = setmealMapper.pageQuery("双人", null, null);

            // Then
            assertAll("名称模糊查询验证",
                    () -> assertNotNull(setmeals, "套餐列表不应为null"),
                    () -> assertTrue(setmeals.size() >= 1, "应至少有一条匹配记录")
            );
        }

        @Test
        @DisplayName("根据分类ID查询套餐")
        void shouldReturnSetmealListWhenCategoryIdMatches() {
            // Given
            Long categoryId = 10L;
            insertTestSetmeal("分类套餐1", categoryId);
            insertTestSetmeal("分类套餐2", categoryId);

            // When
            List<Setmeal> setmeals = setmealMapper.pageQuery(null, categoryId, null);

            // Then
            assertAll("分类查询验证",
                    () -> assertNotNull(setmeals, "套餐列表不应为null"),
                    () -> assertTrue(setmeals.size() >= 2, "应至少有2个匹配记录")
            );
        }

        @Test
        @DisplayName("根据状态查询套餐")
        void shouldReturnSetmealListWhenStatusMatches() {
            // Given
            Setmeal setmeal1 = createTestSetmeal("启用套餐", 1L);
            setmeal1.setStatus(1);
            setmealMapper.insert(setmeal1);

            Setmeal setmeal2 = createTestSetmeal("停用套餐", 1L);
            setmeal2.setStatus(0);
            setmealMapper.insert(setmeal2);

            // When - 查询启用的套餐
            List<Setmeal> enabledSetmeals = setmealMapper.pageQuery(null, null, 1);

            // Then
            assertAll("状态查询验证",
                    () -> assertNotNull(enabledSetmeals, "套餐列表不应为null"),
                    () -> assertTrue(enabledSetmeals.stream().allMatch(s -> s.getStatus() == 1),
                            "所有套餐应都是启用状态")
            );
        }

        @Test
        @DisplayName("组合条件查询套餐")
        void shouldReturnSetmealListWhenMultipleConditionsMatch() {
            // Given
            Long categoryId = 20L;
            Setmeal setmeal = createTestSetmeal("组合查询套餐", categoryId);
            setmeal.setStatus(1);
            setmealMapper.insert(setmeal);

            // When
            List<Setmeal> setmeals = setmealMapper.pageQuery("组合", categoryId, 1);

            // Then
            assertAll("组合查询验证",
                    () -> assertNotNull(setmeals, "套餐列表不应为null"),
                    () -> assertTrue(setmeals.size() >= 1, "应至少有1条匹配记录")
            );
        }

        @Test
        @DisplayName("查询不存在的套餐时返回空列表")
        void shouldReturnEmptyListWhenSetmealNotExists() {
            // Given
            String nonExistentName = "不存在的套餐999";

            // When
            List<Setmeal> setmeals = setmealMapper.pageQuery(nonExistentName, 999999L, null);

            // Then
            assertAll("不存在的套餐验证",
                    () -> assertNotNull(setmeals, "套餐列表不应为null"),
                    () -> assertTrue(setmeals.isEmpty(), "不存在的套餐应返回空列表")
            );
        }
    }

    // ==================== getById 方法测试 ====================

    @Nested
    @DisplayName("getById 方法测试")
    @Tag("query")
    class GetByIdMethodTests {

        @Test
        @DisplayName("根据ID查询套餐 - 成功场景")
        void shouldReturnSetmealWhenIdExists() {
            // Given
            Setmeal inserted = insertTestSetmeal("ID查询套餐", 1L);
            Long insertedId = inserted.getId();

            // When
            Setmeal found = setmealMapper.getById(insertedId);

            // Then
            assertAll("查询验证",
                    () -> assertNotNull(found, "应能查询到插入的套餐"),
                    () -> assertEquals(insertedId, found.getId(), "ID应匹配"),
                    () -> assertEquals("ID查询套餐", found.getName(), "套餐名称应匹配")
            );
        }

        @ParameterizedTest
        @ValueSource(longs = {999999L, -1L, 0L})
        @DisplayName("ID不存在时返回null")
        @Tag("edge-case")
        void shouldReturnNullWhenIdNotExists(Long nonExistentId) {
            // When
            Setmeal found = setmealMapper.getById(nonExistentId);

            // Then
            assertNull(found, "不存在的ID应返回null");
        }
    }

    // ==================== deleteById 方法测试 ====================

    @Nested
    @DisplayName("deleteById 方法测试")
    @Tag("delete")
    class DeleteByIdMethodTests {

        @Test
        @DisplayName("根据ID删除套餐 - 成功场景")
        void shouldDeleteSetmealWhenIdExists() {
            // Given
            Setmeal setmeal = insertTestSetmeal("待删除套餐", 1L);
            Long insertedId = setmeal.getId();

            // When
            setmealMapper.deleteById(insertedId);

            // Then
            Setmeal deleted = setmealMapper.getById(insertedId);
            assertNull(deleted, "删除后应查询不到数据");
        }

        @ParameterizedTest
        @ValueSource(longs = {999999L, -1L, 0L})
        @DisplayName("删除不存在的ID时不报错")
        @Tag("edge-case")
        void shouldNotThrowExceptionWhenDeletingNonExistentId(Long nonExistentId) {
            // When & Then
            assertDoesNotThrow(() -> {
                setmealMapper.deleteById(nonExistentId);
            }, "删除不存在的ID时不应抛出异常");
        }
    }

    // ==================== update 方法测试 ====================

    @Nested
    @DisplayName("update 方法测试")
    @Tag("update")
    class UpdateMethodTests {

        @Test
        @DisplayName("修改套餐信息 - 成功场景")
        void shouldUpdateSetmealWhenValidData() {
            // Given
            Setmeal setmeal = insertTestSetmeal("修改前套餐", 1L);
            Long insertedId = setmeal.getId();

            // When
            Setmeal updateData = Setmeal.builder()
                    .id(insertedId)
                    .name("修改后套餐")
                    .price(new BigDecimal("68.00"))
                    .status(0)
                    .description("修改后描述")
                    .updateTime(LocalDateTime.now())
                    .build();
            setmealMapper.update(updateData);

            // Then
            Setmeal updated = setmealMapper.getById(insertedId);
            assertAll("修改验证",
                    () -> assertNotNull(updated, "修改后应能查询到数据"),
                    () -> assertEquals("修改后套餐", updated.getName(), "名称应被修改"),
                    () -> assertEquals(new BigDecimal("68.00"), updated.getPrice(), "价格应被修改"),
                    () -> assertEquals(0, updated.getStatus(), "状态应被修改"),
                    () -> assertEquals("修改后描述", updated.getDescription(), "描述应被修改")
            );
        }

        @Test
        @DisplayName("修改部分字段")
        void shouldUpdateOnlySpecifiedFields() {
            // Given
            Setmeal setmeal = insertTestSetmeal("部分修改套餐", 1L);
            Long insertedId = setmeal.getId();
            BigDecimal originalPrice = setmeal.getPrice();

            // When - 只修改名称
            Setmeal updateData = Setmeal.builder()
                    .id(insertedId)
                    .name("只修改名称")
                    .updateTime(LocalDateTime.now())
                    .build();
            setmealMapper.update(updateData);

            // Then
            Setmeal updated = setmealMapper.getById(insertedId);
            assertAll("部分字段修改验证",
                    () -> assertNotNull(updated, "修改后应能查询到数据"),
                    () -> assertEquals("只修改名称", updated.getName(), "名称应被修改"),
                    () -> assertEquals(originalPrice, updated.getPrice(), "未修改的字段应保持不变")
            );
        }

        @Test
        @DisplayName("修改不存在的ID时不报错")
        @Tag("edge-case")
        void shouldNotThrowExceptionWhenUpdatingNonExistentId() {
            // Given
            Setmeal updateData = Setmeal.builder()
                    .id(999999L)
                    .name("不存在的套餐")
                    .updateTime(LocalDateTime.now())
                    .build();

            // When & Then
            assertDoesNotThrow(() -> {
                setmealMapper.update(updateData);
            }, "修改不存在的ID时不应抛出异常");
        }

        @ParameterizedTest
        @CsvSource({
            "0, 停用状态",
            "1, 启用状态"
        })
        @DisplayName("修改套餐状态")
        @Tag("parameterized")
        void shouldUpdateSetmealStatus(Integer status, String statusDesc) {
            // Given
            Setmeal setmeal = insertTestSetmeal("状态修改套餐" + status, 1L);
            Long insertedId = setmeal.getId();

            // When
            Setmeal updateData = Setmeal.builder()
                    .id(insertedId)
                    .status(status)
                    .updateTime(LocalDateTime.now())
                    .build();
            setmealMapper.update(updateData);

            // Then
            Setmeal updated = setmealMapper.getById(insertedId);
            assertEquals(status, updated.getStatus(), "状态应被修改为" + statusDesc);
        }
    }

    // ==================== list 方法测试 ====================

    @Nested
    @DisplayName("list 方法测试")
    @Tag("query")
    class ListMethodTests {

        @Test
        @DisplayName("条件查询套餐列表 - 成功场景")
        void shouldReturnSetmealListWhenConditionMatches() {
            // Given
            Long categoryId = 30L;
            insertTestSetmeal("条件查询套餐1", categoryId);

            Setmeal query = Setmeal.builder()
                    .categoryId(categoryId)
                    .build();

            // When
            List<Setmeal> setmeals = setmealMapper.list(query);

            // Then
            assertAll("条件查询验证",
                    () -> assertNotNull(setmeals, "套餐列表不应为null"),
                    () -> assertTrue(setmeals.size() >= 1, "应至少有1条匹配记录")
            );
        }

        @Test
        @DisplayName("查询参数为null时返回所有套餐")
        void shouldReturnAllSetmealsWhenQueryIsNull() {
            // Given
            insertTestSetmeal("查询测试套餐", 1L);

            // When
            List<Setmeal> setmeals = setmealMapper.list(null);

            // Then
            assertAll("空查询验证",
                    () -> assertNotNull(setmeals, "套餐列表不应为null"),
                    () -> assertFalse(setmeals.isEmpty(), "应返回所有套餐")
            );
        }

        @Test
        @DisplayName("根据状态查询套餐列表")
        void shouldReturnSetmealListWhenStatusMatches() {
            // Given
            Setmeal setmeal1 = createTestSetmeal("启用状态套餐", 1L);
            setmeal1.setStatus(1);
            setmealMapper.insert(setmeal1);

            Setmeal query = Setmeal.builder()
                    .status(1)
                    .build();

            // When
            List<Setmeal> setmeals = setmealMapper.list(query);

            // Then
            assertAll("状态查询验证",
                    () -> assertNotNull(setmeals, "套餐列表不应为null"),
                    () -> assertTrue(setmeals.stream().allMatch(s -> s.getStatus() == 1),
                            "所有套餐应都是启用状态")
            );
        }
    }

    // ==================== countByMap 方法测试 ====================

    @Nested
    @DisplayName("countByMap 方法测试")
    @Tag("query")
    class CountByMapMethodTests {

        @Test
        @DisplayName("根据Map条件统计套餐数量 - 成功场景")
        void shouldReturnCountWhenMapConditionMatches() {
            // Given
            Setmeal setmeal = createTestSetmeal("Map统计套餐", 1L);
            setmeal.setStatus(1);
            setmealMapper.insert(setmeal);

            Map<String, Integer> map = new HashMap<>();
            map.put("status", 1);

            // When
            Integer count = setmealMapper.countByMap(map);

            // Then
            assertAll("Map统计验证",
                    () -> assertNotNull(count, "统计结果不应为null"),
                    () -> assertTrue(count >= 1, "应至少有1个匹配的套餐")
            );
        }

        @Test
        @DisplayName("Map为空时返回所有套餐数量")
        void shouldReturnTotalCountWhenMapIsEmpty() {
            // Given
            insertTestSetmeal("空Map统计套餐1", 1L);
            insertTestSetmeal("空Map统计套餐2", 1L);

            Map<String, Integer> map = new HashMap<>();

            // When
            Integer count = setmealMapper.countByMap(map);

            // Then
            assertAll("空Map验证",
                    () -> assertNotNull(count, "统计结果不应为null"),
                    () -> assertTrue(count >= 2, "应至少有2个套餐")
            );
        }

        @Test
        @DisplayName("Map条件不匹配时返回0")
        void shouldReturnZeroWhenMapConditionNotMatches() {
            // Given
            Map<String, Integer> map = new HashMap<>();
            map.put("status", 999);

            // When
            Integer count = setmealMapper.countByMap(map);

            // Then
            assertEquals(0, count, "不匹配的条件应返回0");
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
            // 1. Create - 新增套餐
            Setmeal setmeal = insertTestSetmeal("CRUD测试套餐", 1L);
            assertAll("新增验证",
                    () -> assertNotNull(setmeal.getId(), "新增后ID应自动生成")
            );

            // 2. Read - 查询套餐
            Setmeal found = setmealMapper.getById(setmeal.getId());
            assertAll("查询验证",
                    () -> assertNotNull(found, "应能查询到新增的套餐"),
                    () -> assertEquals("CRUD测试套餐", found.getName(), "套餐名称应匹配")
            );

            // 3. Update - 修改套餐
            Setmeal updateData = Setmeal.builder()
                    .id(setmeal.getId())
                    .name("CRUD测试套餐-修改")
                    .updateTime(LocalDateTime.now())
                    .build();
            setmealMapper.update(updateData);
            Setmeal updated = setmealMapper.getById(setmeal.getId());
            assertEquals("CRUD测试套餐-修改", updated.getName(), "修改应生效");

            // 4. Query - 分页查询验证
            List<Setmeal> setmeals = setmealMapper.pageQuery("CRUD测试套餐-修改", null, null);
            assertTrue(setmeals.size() >= 1, "分页查询应能找到修改后的套餐");

            // 5. Delete - 删除套餐
            setmealMapper.deleteById(setmeal.getId());
            Setmeal deleted = setmealMapper.getById(setmeal.getId());
            assertNull(deleted, "删除后应查询不到数据");
        }

        @Test
        @DisplayName("套餐状态管理流程测试")
        void shouldManageSetmealStatusCorrectly() {
            // Given - 新增一个启用的套餐
            Setmeal setmeal = insertTestSetmeal("状态管理套餐", 1L);
            assertEquals(1, setmeal.getStatus(), "初始状态应为启用");

            Long setmealId = setmeal.getId();

            // When - 停用套餐
            Setmeal disableData = Setmeal.builder()
                    .id(setmealId)
                    .status(0)
                    .updateTime(LocalDateTime.now())
                    .build();
            setmealMapper.update(disableData);

            // Then - 验证状态已停用
            Setmeal disabled = setmealMapper.getById(setmealId);
            assertEquals(0, disabled.getStatus(), "套餐应被停用");

            // When - 重新启用套餐
            Setmeal enableData = Setmeal.builder()
                    .id(setmealId)
                    .status(1)
                    .updateTime(LocalDateTime.now())
                    .build();
            setmealMapper.update(enableData);

            // Then - 验证状态已启用
            Setmeal enabled = setmealMapper.getById(setmealId);
            assertEquals(1, enabled.getStatus(), "套餐应被重新启用");
        }

        @Test
        @DisplayName("套餐分类统计流程测试")
        void shouldCountSetmealsByCategoryCorrectly() {
            // Given - 为不同分类新增套餐
            Long categoryId1 = 100L;
            Long categoryId2 = 200L;

            insertTestSetmeal("分类1套餐1", categoryId1);
            insertTestSetmeal("分类1套餐2", categoryId1);
            insertTestSetmeal("分类2套餐1", categoryId2);

            // When - 统计分类1的套餐数量
            Integer count1 = setmealMapper.countByCategoryId(categoryId1);

            // When - 统计分类2的套餐数量
            Integer count2 = setmealMapper.countByCategoryId(categoryId2);

            // Then - 验证统计结果
            assertAll("分类统计验证",
                    () -> assertTrue(count1 >= 2, "分类1应有至少2个套餐"),
                    () -> assertTrue(count2 >= 1, "分类2应有至少1个套餐")
            );
        }
    }
}
