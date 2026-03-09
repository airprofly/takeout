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

import com.airprofly.entity.Employee;

/**
 * EmployeeMapper 测试类
 *
 * 测试覆盖:
 * - CRUD 操作测试
 * - 查询条件测试
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
@Tag("employee")
@DisplayName("员工 Mapper 测试")
public class EmployeeMapperTest {

    @Autowired
    private EmployeeMapper employeeMapper;

    // ==================== 测试数据工厂方法 ====================

    /**
     * 创建标准的测试员工对象
     */
    private Employee createTestEmployee(String username) {
        return Employee.builder()
                .username(username)
                .name("测试员工")
                .password("123456")
                .phone("13800138000")
                .sex("1")
                .idNumber("110101199001011234")
                .status(1)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建并插入测试员工,返回插入后的对象(包含生成的ID)
     */
    private Employee insertTestEmployee(String username) {
        Employee employee = createTestEmployee(username);
        employeeMapper.insert(employee);
        return employee;
    }

    // ==================== getByUsername 方法测试 ====================

    @Nested
    @DisplayName("getByUsername 方法测试")
    @Tag("query")
    class GetByUsernameMethodTests {

        @Test
        @DisplayName("根据用户名查询员工 - 成功场景")
        void shouldReturnEmployeeWhenUsernameExists() {
            // Given
            Employee employee = insertTestEmployee("testuser001");

            // When
            Employee found = employeeMapper.getByUsername(employee.getUsername());

            // Then
            assertAll("查询验证",
                    () -> assertNotNull(found, "应能查询到员工"),
                    () -> assertEquals("testuser001", found.getUsername(), "用户名应匹配"),
                    () -> assertEquals("测试员工", found.getName(), "姓名应匹配"));
        }

        @Test
        @DisplayName("用户名不存在时返回null")
        void shouldReturnNullWhenUsernameNotExists() {
            // Given
            String nonExistentUsername = "nonexistentuser999";

            // When
            Employee found = employeeMapper.getByUsername(nonExistentUsername);

            // Then
            assertNull(found, "不存在的用户名应返回null");
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("用户名为null时返回null")
        @Tag("edge-case")
        void shouldReturnNullWhenUsernameIsNull(String username) {
            // When
            Employee found = employeeMapper.getByUsername(username);

            // Then
            assertNull(found, "null用户名应返回null");
        }
    }

    // ==================== insert 方法测试 ====================

    @Nested
    @DisplayName("insert 方法测试")
    @Tag("create")
    class InsertMethodTests {

        @Test
        @DisplayName("新增员工 - 成功场景")
        void shouldInsertEmployeeWhenValidData() {
            // Given
            Employee employee = createTestEmployee("newuser001");

            // When
            employeeMapper.insert(employee);

            // Then
            assertAll("插入验证",
                    () -> assertNotNull(employee.getId(), "插入后ID应自动生成"),
                    () -> {
                        Employee inserted = employeeMapper.getById(employee.getId());
                        assertNotNull(inserted, "插入的数据应能被查询到");
                        assertEquals("newuser001", inserted.getUsername(), "用户名应匹配");
                        assertEquals("测试员工", inserted.getName(), "姓名应匹配");
                    });
        }

        @Test
        @DisplayName("必填字段为null时应失败")
        @Tag("edge-case")
        void shouldFailToInsertWhenRequiredFieldsAreNull() {
            // Given
            Employee employee = Employee.builder()
                    .username("testuser002")
                    // name 为 null
                    .password("123456")
                    .build();

            // When & Then
            assertThrows(DataIntegrityViolationException.class, () -> {
                employeeMapper.insert(employee);
            }, "必填字段为null时应抛出异常");
        }

        @ParameterizedTest
        @CsvSource({
                "0, 禁用",
                "1, 启用"
        })
        @DisplayName("新增不同状态的员工")
        @Tag("parameterized")
        void shouldInsertEmployeeWithDifferentStatus(Integer status, String statusDesc) {
            // Given
            Employee employee = createTestEmployee("statususer" + status);
            employee.setStatus(status);

            // When
            employeeMapper.insert(employee);

            // Then
            Employee inserted = employeeMapper.getById(employee.getId());
            assertAll("状态验证",
                    () -> assertNotNull(inserted, "应成功插入"),
                    () -> assertEquals(status, inserted.getStatus(), "状态应匹配"));
        }

        @ParameterizedTest
        @CsvSource({
                "0, 女",
                "1, 男"
        })
        @DisplayName("新增不同性别的员工")
        @Tag("parameterized")
        void shouldInsertEmployeeWithDifferentSex(String sex, String sexDesc) {
            // Given
            Employee employee = createTestEmployee("sexuser" + sex);
            employee.setSex(sex);

            // When
            employeeMapper.insert(employee);

            // Then
            Employee inserted = employeeMapper.getById(employee.getId());
            assertAll("性别验证",
                    () -> assertNotNull(inserted, "应成功插入"),
                    () -> assertEquals(sex, inserted.getSex(), "性别应匹配"));
        }
    }

    // ==================== pageQuery 方法测试 ====================

    @Nested
    @DisplayName("pageQuery 方法测试")
    @Tag("query")
    class PageQueryMethodTests {

        @Test
        @DisplayName("分页查询员工 - 成功场景")
        void shouldReturnEmployeeListWhenQuery() {
            // Given
            insertTestEmployee("pageuser001");

            // When
            List<Employee> employees = employeeMapper.pageQuery(null);

            // Then
            assertAll("分页查询验证",
                    () -> assertNotNull(employees, "员工列表不应为null"),
                    () -> assertFalse(employees.isEmpty(), "员工列表不应为空"));
        }

        @Test
        @DisplayName("根据姓名模糊查询员工")
        void shouldReturnEmployeeListWhenNameMatches() {
            // Given
            insertTestEmployee("nameuser001");

            // When
            List<Employee> employees = employeeMapper.pageQuery("测试员工");

            // Then
            assertAll("姓名模糊查询验证",
                    () -> assertNotNull(employees, "员工列表不应为null"),
                    () -> assertTrue(employees.size() >= 1, "应至少有一条匹配记录"));
        }

        @Test
        @DisplayName("查询条件为null时返回所有员工")
        void shouldReturnAllEmployeesWhenQueryIsNull() {
            // Given
            insertTestEmployee("alluser001");

            // When
            List<Employee> employees = employeeMapper.pageQuery(null);

            // Then
            assertAll("空查询验证",
                    () -> assertNotNull(employees, "员工列表不应为null"),
                    () -> assertFalse(employees.isEmpty(), "应返回所有员工"));
        }

        @Test
        @DisplayName("查询不存在的姓名时返回空列表")
        void shouldReturnEmptyListWhenNameNotExists() {
            // Given
            String nonExistentName = "不存在的姓名999";

            // When
            List<Employee> employees = employeeMapper.pageQuery(nonExistentName);

            // Then
            assertAll("不存在的姓名验证",
                    () -> assertNotNull(employees, "员工列表不应为null"),
                    () -> assertTrue(employees.isEmpty(), "不存在的姓名应返回空列表"));
        }
    }

    // ==================== getById 方法测试 ====================

    @Nested
    @DisplayName("getById 方法测试")
    @Tag("query")
    class GetByIdMethodTests {

        @Test
        @DisplayName("根据ID查询员工 - 成功场景")
        void shouldReturnEmployeeWhenIdExists() {
            // Given
            Employee inserted = insertTestEmployee("getbyid001");
            Long insertedId = inserted.getId();

            // When
            Employee found = employeeMapper.getById(insertedId);

            // Then
            assertAll("查询验证",
                    () -> assertNotNull(found, "应能查询到插入的员工"),
                    () -> assertEquals(insertedId, found.getId(), "ID应匹配"),
                    () -> assertEquals("getbyid001", found.getUsername(), "用户名应匹配"),
                    () -> assertEquals("测试员工", found.getName(), "姓名应匹配"));
        }

        @ParameterizedTest
        @ValueSource(longs = { 999999L, -1L, 0L })
        @DisplayName("ID不存在时返回null")
        @Tag("edge-case")
        void shouldReturnNullWhenIdNotExists(Long nonExistentId) {
            // When
            Employee found = employeeMapper.getById(nonExistentId);

            // Then
            assertNull(found, "不存在的ID应返回null");
        }
    }

    // ==================== update 方法测试 ====================

    @Nested
    @DisplayName("update 方法测试")
    @Tag("update")
    class UpdateMethodTests {

        @Test
        @DisplayName("修改员工信息 - 成功场景")
        void shouldUpdateEmployeeWhenValidData() {
            // Given
            Employee employee = insertTestEmployee("updateuser001");
            Long insertedId = employee.getId();

            // When
            Employee updateData = Employee.builder()
                    .id(insertedId)
                    .name("修改后姓名")
                    .phone("13900139000")
                    .sex("0")
                    .status(0)
                    .updateTime(LocalDateTime.now())
                    .build();
            employeeMapper.update(updateData);

            // Then
            Employee updated = employeeMapper.getById(insertedId);
            assertAll("修改验证",
                    () -> assertNotNull(updated, "修改后应能查询到数据"),
                    () -> assertEquals("修改后姓名", updated.getName(), "姓名应被修改"),
                    () -> assertEquals("13900139000", updated.getPhone(), "手机号应被修改"),
                    () -> assertEquals("0", updated.getSex(), "性别应被修改"),
                    () -> assertEquals(0, updated.getStatus(), "状态应被修改"));
        }

        @Test
        @DisplayName("修改部分字段")
        void shouldUpdateOnlySpecifiedFields() {
            // Given
            Employee employee = insertTestEmployee("partialuser001");
            Long insertedId = employee.getId();
            String originalPhone = employee.getPhone();

            // When - 只修改姓名
            Employee updateData = Employee.builder()
                    .id(insertedId)
                    .name("只修改姓名")
                    .updateTime(LocalDateTime.now())
                    .build();
            employeeMapper.update(updateData);

            // Then
            Employee updated = employeeMapper.getById(insertedId);
            assertAll("部分字段修改验证",
                    () -> assertNotNull(updated, "修改后应能查询到数据"),
                    () -> assertEquals("只修改姓名", updated.getName(), "姓名应被修改"),
                    () -> assertEquals(originalPhone, updated.getPhone(), "未修改的字段应保持不变"));
        }

        @Test
        @DisplayName("修改不存在的ID时不报错")
        @Tag("edge-case")
        void shouldNotThrowExceptionWhenUpdatingNonExistentId() {
            // Given
            Employee updateData = Employee.builder()
                    .id(999999L)
                    .name("不存在的员工")
                    .updateTime(LocalDateTime.now())
                    .build();

            // When & Then
            assertDoesNotThrow(() -> {
                employeeMapper.update(updateData);
            }, "修改不存在的ID时不应抛出异常");
        }

        @ParameterizedTest
        @CsvSource({
                "0, 禁用状态",
                "1, 启用状态"
        })
        @DisplayName("修改员工状态")
        @Tag("parameterized")
        void shouldUpdateEmployeeStatus(Integer status, String statusDesc) {
            // Given
            Employee employee = insertTestEmployee("statusupdate" + status);
            Long insertedId = employee.getId();

            // When
            Employee updateData = Employee.builder()
                    .id(insertedId)
                    .status(status)
                    .updateTime(LocalDateTime.now())
                    .build();
            employeeMapper.update(updateData);

            // Then
            Employee updated = employeeMapper.getById(insertedId);
            assertEquals(status, updated.getStatus(), "状态应被修改为" + statusDesc);
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
            // 1. Create - 新增员工
            Employee employee = insertTestEmployee("cruduser001");
            assertAll("新增验证",
                    () -> assertNotNull(employee.getId(), "新增后ID应自动生成"));

            // 2. Read - 查询员工(通过用户名)
            Employee foundByUsername = employeeMapper.getByUsername(employee.getUsername());
            assertAll("用户名查询验证",
                    () -> assertNotNull(foundByUsername, "应能通过用户名查询到员工"),
                    () -> assertEquals("cruduser001", foundByUsername.getUsername(), "用户名应匹配"));

            // 3. Read - 查询员工(通过ID)
            Employee foundById = employeeMapper.getById(employee.getId());
            assertEquals("测试员工", foundById.getName(), "姓名应匹配");

            // 4. Update - 修改员工
            Employee updateData = Employee.builder()
                    .id(employee.getId())
                    .name("完整流程测试-修改")
                    .updateTime(LocalDateTime.now())
                    .build();
            employeeMapper.update(updateData);
            Employee updated = employeeMapper.getById(employee.getId());
            assertEquals("完整流程测试-修改", updated.getName(), "修改应生效");

            // 5. Query - 分页查询验证
            List<Employee> employees = employeeMapper.pageQuery("完整流程测试-修改");
            assertTrue(employees.size() >= 1, "分页查询应能找到修改后的员工");
        }

        @Test
        @DisplayName("员工状态管理流程测试")
        void shouldManageEmployeeStatusCorrectly() {
            // Given - 新增一个启用的员工
            Employee employee = insertTestEmployee("statususer001");
            assertEquals(1, employee.getStatus(), "初始状态应为启用");

            Long employeeId = employee.getId();

            // When - 禁用员工
            Employee disableData = Employee.builder()
                    .id(employeeId)
                    .status(0)
                    .updateTime(LocalDateTime.now())
                    .build();
            employeeMapper.update(disableData);

            // Then - 验证状态已禁用
            Employee disabled = employeeMapper.getById(employeeId);
            assertEquals(0, disabled.getStatus(), "员工应被禁用");

            // When - 重新启用员工
            Employee enableData = Employee.builder()
                    .id(employeeId)
                    .status(1)
                    .updateTime(LocalDateTime.now())
                    .build();
            employeeMapper.update(enableData);

            // Then - 验证状态已启用
            Employee enabled = employeeMapper.getById(employeeId);
            assertEquals(1, enabled.getStatus(), "员工应被重新启用");
        }

        @Test
        @DisplayName("批量查询员工流程测试")
        void shouldQueryMultipleEmployeesCorrectly() {
            // Given - 新增多个员工
            insertTestEmployee("batchuser001");
            insertTestEmployee("batchuser002");
            insertTestEmployee("batchuser003");

            // When - 查询所有员工
            List<Employee> allEmployees = employeeMapper.pageQuery(null);

            // Then - 验证查询结果 (只检查当前测试插入的数据)
            assertAll("批量查询验证",
                    () -> assertNotNull(allEmployees, "员工列表不应为null"),
                    () -> assertTrue(allEmployees.size() >= 3, "应至少包含当前测试插入的3个员工"));

            // When - 根据姓名模糊查询
            List<Employee> filteredEmployees = employeeMapper.pageQuery("测试员工");

            // Then - 验证过滤结果
            assertAll("过滤查询验证",
                    () -> assertNotNull(filteredEmployees, "过滤后的列表不应为null"),
                    () -> assertTrue(filteredEmployees.size() >= 3, "应至少找到3个匹配的员工"));
        }
    }
}
