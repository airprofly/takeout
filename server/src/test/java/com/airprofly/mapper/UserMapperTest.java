package com.airprofly.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.HashMap;
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
import org.springframework.transaction.annotation.Transactional;

import com.airprofly.entity.User;

/**
 * UserMapper 测试类
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
@Tag("user")
@DisplayName("用户 Mapper 测试")
public class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    // ==================== 测试数据工厂方法 ====================

    /**
     * 创建标准的测试用户对象
     */
    private User createTestUser(String openid) {
        return User.builder()
                .openid(openid)
                .name("测试用户")
                .phone("13800138000")
                .sex("1")
                .idNumber("110101199001011234")
                .avatar("https://example.com/avatar.jpg")
                .createTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建并插入测试用户,返回插入后的对象(包含生成的ID)
     */
    private User insertTestUser(String openid) {
        User user = createTestUser(openid);
        userMapper.insert(user);
        return user;
    }

    // ==================== getByOpenid 方法测试 ====================

    @Nested
    @DisplayName("getByOpenid 方法测试")
    @Tag("query")
    class GetByOpenidMethodTests {

        @Test
        @DisplayName("根据OpenID查询用户 - 成功场景")
        void shouldReturnUserWhenOpenidExists() {
            // Given
            String testOpenid = "test_openid_001";
            insertTestUser(testOpenid);

            // When
            User found = userMapper.getByOpenid(testOpenid);

            // Then
            assertAll("查询验证",
                    () -> assertNotNull(found, "应能查询到用户"),
                    () -> assertEquals(testOpenid, found.getOpenid(), "OpenID应匹配"),
                    () -> assertEquals("测试用户", found.getName(), "姓名应匹配"),
                    () -> assertEquals("13800138000", found.getPhone(), "手机号应匹配"));
        }

        @Test
        @DisplayName("OpenID不存在时返回null")
        void shouldReturnNullWhenOpenidNotExists() {
            // Given
            String nonExistentOpenid = "nonexistent_openid_999";

            // When
            User found = userMapper.getByOpenid(nonExistentOpenid);

            // Then
            assertNull(found, "不存在的OpenID应返回null");
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("OpenID为null时返回null")
        @Tag("edge-case")
        void shouldReturnNullWhenOpenidIsNull(String openid) {
            // When
            User found = userMapper.getByOpenid(openid);

            // Then
            assertNull(found, "null OpenID应返回null");
        }

        @Test
        @DisplayName("验证返回数据完整性")
        void shouldReturnCompleteUserData() {
            // Given
            String testOpenid = "test_openid_complete";
            User testUser = createTestUser(testOpenid);
            userMapper.insert(testUser);

            // When
            User found = userMapper.getByOpenid(testOpenid);

            // Then
            assertAll("用户数据完整性验证",
                    () -> assertNotNull(found, "用户不应为null"),
                    () -> assertNotNull(found.getId(), "用户ID不应为null"),
                    () -> assertNotNull(found.getOpenid(), "OpenID不应为null"),
                    () -> assertNotNull(found.getName(), "姓名不应为null"),
                    () -> assertNotNull(found.getPhone(), "手机号不应为null"),
                    () -> assertNotNull(found.getCreateTime(), "创建时间不应为null"));
        }
    }

    // ==================== insert 方法测试 ====================

    @Nested
    @DisplayName("insert 方法测试")
    @Tag("create")
    class InsertMethodTests {

        @Test
        @DisplayName("新增用户 - 成功场景")
        void shouldInsertUserWhenValidData() {
            // Given
            User user = createTestUser("new_openid_001");

            // When
            userMapper.insert(user);

            // Then
            assertAll("插入验证",
                    () -> assertNotNull(user.getId(), "插入后ID应自动生成"),
                    () -> {
                        User inserted = userMapper.getById(user.getId());
                        assertNotNull(inserted, "插入的数据应能被查询到");
                        assertEquals("new_openid_001", inserted.getOpenid(), "OpenID应匹配");
                        assertEquals("测试用户", inserted.getName(), "姓名应匹配");
                        assertEquals("13800138000", inserted.getPhone(), "手机号应匹配");
                    });
        }

        @Test
        @DisplayName("可选字段为null时可成功新增")
        @Tag("edge-case")
        void shouldInsertWhenOptionalFieldsAreNull() {
            // Given
            User user = User.builder()
                    .openid("test_openid_002")
                    .name(null)
                    .phone(null)
                    .sex(null)
                    .idNumber(null)
                    .avatar(null)
                    .createTime(LocalDateTime.now())
                    .build();

            // When
            userMapper.insert(user);

            // Then
            assertAll("空字段插入验证",
                    () -> assertNotNull(user.getId(), "插入后ID应自动生成"),
                    () -> {
                        User inserted = userMapper.getById(user.getId());
                        assertNotNull(inserted, "应能查询到插入用户");
                        assertEquals("test_openid_002", inserted.getOpenid(), "OpenID应匹配");
                    });
        }

        @ParameterizedTest
        @CsvSource({
                "0, 女",
                "1, 男"
        })
        @DisplayName("新增不同性别的用户")
        @Tag("parameterized")
        void shouldInsertUserWithDifferentSex(String sex, String sexDesc) {
            // Given
            User user = createTestUser("sex_openid_" + sex);
            user.setSex(sex);

            // When
            userMapper.insert(user);

            // Then
            User inserted = userMapper.getById(user.getId());
            assertAll("性别验证",
                    () -> assertNotNull(inserted, "应成功插入"),
                    () -> assertEquals(sex, inserted.getSex(), "性别应匹配"));
        }

        @Test
        @DisplayName("新增用户时所有字段都应正确保存")
        void shouldInsertUserWithAllFields() {
            // Given
            String testOpenid = "full_fields_openid";
            User user = User.builder()
                    .openid(testOpenid)
                    .name("完整字段用户")
                    .phone("13900139000")
                    .sex("0")
                    .idNumber("110101199002021234")
                    .avatar("https://example.com/avatar2.jpg")
                    .createTime(LocalDateTime.now())
                    .build();

            // When
            userMapper.insert(user);

            // Then
            User inserted = userMapper.getById(user.getId());
            assertAll("所有字段验证",
                    () -> assertNotNull(inserted, "应成功插入"),
                    () -> assertEquals(testOpenid, inserted.getOpenid(), "OpenID应匹配"),
                    () -> assertEquals("完整字段用户", inserted.getName(), "姓名应匹配"),
                    () -> assertEquals("13900139000", inserted.getPhone(), "手机号应匹配"),
                    () -> assertEquals("0", inserted.getSex(), "性别应匹配"),
                    () -> assertEquals("110101199002021234", inserted.getIdNumber(), "身份证号应匹配"),
                    () -> assertEquals("https://example.com/avatar2.jpg", inserted.getAvatar(), "头像应匹配"));
        }
    }

    // ==================== getById 方法测试 ====================

    @Nested
    @DisplayName("getById 方法测试")
    @Tag("query")
    class GetByIdMethodTests {

        @Test
        @DisplayName("根据ID查询用户 - 成功场景")
        void shouldReturnUserWhenIdExists() {
            // Given
            User inserted = insertTestUser("getbyid_openid_001");
            Long insertedId = inserted.getId();

            // When
            User found = userMapper.getById(insertedId);

            // Then
            assertAll("查询验证",
                    () -> assertNotNull(found, "应能查询到插入的用户"),
                    () -> assertEquals(insertedId, found.getId(), "ID应匹配"),
                    () -> assertEquals("getbyid_openid_001", found.getOpenid(), "OpenID应匹配"),
                    () -> assertEquals("测试用户", found.getName(), "姓名应匹配"));
        }

        @ParameterizedTest
        @ValueSource(longs = { 999999L, -1L, 0L })
        @DisplayName("ID不存在时返回null")
        @Tag("edge-case")
        void shouldReturnNullWhenIdNotExists(Long nonExistentId) {
            // When
            User found = userMapper.getById(nonExistentId);

            // Then
            assertNull(found, "不存在的ID应返回null");
        }

        @Test
        @DisplayName("验证查询返回的用户数据完整性")
        void shouldReturnCompleteUserDataWhenGetById() {
            // Given
            User testUser = createTestUser("complete_data_openid");
            testUser.setIdNumber("110101199003031234");
            testUser.setAvatar("https://example.com/test.jpg");
            userMapper.insert(testUser);

            // When
            User found = userMapper.getById(testUser.getId());

            // Then
            assertAll("数据完整性验证",
                    () -> assertNotNull(found, "应能查询到用户"),
                    () -> assertEquals("complete_data_openid", found.getOpenid(), "OpenID应匹配"),
                    () -> assertEquals("测试用户", found.getName(), "姓名应匹配"),
                    () -> assertEquals("13800138000", found.getPhone(), "手机号应匹配"),
                    () -> assertEquals("1", found.getSex(), "性别应匹配"),
                    () -> assertEquals("110101199003031234", found.getIdNumber(), "身份证号应匹配"),
                    () -> assertEquals("https://example.com/test.jpg", found.getAvatar(), "头像应匹配"),
                    () -> assertNotNull(found.getCreateTime(), "创建时间不应为null"));
        }
    }

    // ==================== countByMap 方法测试 ====================

    @Nested
    @DisplayName("countByMap 方法测试")
    @Tag("query")
    class CountByMapMethodTests {

        @Test
        @DisplayName("根据条件统计用户数量 - 成功场景")
        void shouldReturnUserCountWhenConditionMatches() {
            // Given
            insertTestUser("count_openid_001");

            Map<String, Object> params = new HashMap<>();
            params.put("openid", "count_openid_001");

            // When
            Integer count = userMapper.countByMap(params);

            // Then
            assertAll("统计验证",
                    () -> assertNotNull(count, "统计结果不应为null"),
                    () -> assertTrue(count >= 1, "应至少统计到1个用户"));
        }

        @Test
        @DisplayName("无条件时统计所有用户")
        void shouldReturnAllUserCountWhenMapIsEmpty() {
            // Given
            insertTestUser("all_count_openid_001");
            insertTestUser("all_count_openid_002");

            Map<String, Object> params = new HashMap<>();

            // When
            Integer count = userMapper.countByMap(params);

            // Then
            assertAll("无条件统计验证",
                    () -> assertNotNull(count, "统计结果不应为null"),
                    () -> assertTrue(count >= 2, "应至少统计到2个用户"));
        }

        @Test
        @DisplayName("根据姓名统计用户数量")
        void shouldReturnUserCountWhenNameMatches() {
            // Given
            User user1 = createTestUser("name_count_openid_001");
            user1.setName("特定姓名");
            userMapper.insert(user1);

            User user2 = createTestUser("name_count_openid_002");
            user2.setName("特定姓名");
            userMapper.insert(user2);

            Map<String, Object> params = new HashMap<>();
            params.put("name", "特定姓名");

            // When
            Integer count = userMapper.countByMap(params);

            // Then
            assertAll("姓名统计验证",
                    () -> assertNotNull(count, "统计结果不应为null"),
                    () -> assertTrue(count >= 2, "应至少统计到2个用户"));
        }

        @Test
        @DisplayName("根据手机号统计用户数量")
        void shouldReturnUserCountWhenPhoneMatches() {
            // Given
            String specificPhone = "13700137000";
            User user = createTestUser("phone_count_openid_001");
            user.setPhone(specificPhone);
            userMapper.insert(user);

            Map<String, Object> params = new HashMap<>();
            params.put("phone", specificPhone);

            // When
            Integer count = userMapper.countByMap(params);

            // Then
            assertAll("手机号统计验证",
                    () -> assertNotNull(count, "统计结果不应为null"),
                    () -> assertTrue(count >= 1, "应至少统计到1个用户"));
        }

        @Test
        @DisplayName("根据性别统计用户数量")
        @Tag("parameterized")
        void shouldReturnUserCountWhenSexMatches() {
            // Given
            User user1 = createTestUser("sex_count_openid_001");
            user1.setSex("0");
            userMapper.insert(user1);

            User user2 = createTestUser("sex_count_openid_002");
            user2.setSex("0");
            userMapper.insert(user2);

            Map<String, Object> params = new HashMap<>();
            params.put("sex", "0");

            // When
            Integer count = userMapper.countByMap(params);

            // Then
            assertAll("性别统计验证",
                    () -> assertNotNull(count, "统计结果不应为null"),
                    () -> assertTrue(count >= 2, "应至少统计到2个女性用户"));
        }

        @Test
        @DisplayName("不存在的条件返回0")
        @Tag("edge-case")
        void shouldReturnZeroWhenConditionNotMatches() {
            // Given
            Map<String, Object> params = new HashMap<>();
            params.put("begin", LocalDateTime.now().plusDays(1));
            params.put("end", LocalDateTime.now().plusDays(2));

            // When
            Integer count = userMapper.countByMap(params);

            // Then
            assertAll("不存在的条件验证",
                    () -> assertNotNull(count, "统计结果不应为null"),
                    () -> assertEquals(0, count, "不存在的条件应返回0"));
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("查询参数为null时统计所有用户")
        @Tag("edge-case")
        void shouldReturnAllUserCountWhenMapIsNull(Map<String, Object> params) {
            // Given
            insertTestUser("null_map_count_openid_001");

            // When
            Integer count = userMapper.countByMap(params);

            // Then
            assertNotNull(count, "统计结果不应为null");
        }

        @Test
        @DisplayName("多条件组合统计用户数量")
        void shouldReturnUserCountWhenMultipleConditionsMatch() {
            // Given
            User user = createTestUser("multi_cond_openid_001");
            user.setSex("1");
            user.setPhone("13600136000");
            userMapper.insert(user);

            Map<String, Object> params = new HashMap<>();
            params.put("sex", "1");
            params.put("phone", "13600136000");

            // When
            Integer count = userMapper.countByMap(params);

            // Then
            assertAll("多条件统计验证",
                    () -> assertNotNull(count, "统计结果不应为null"),
                    () -> assertTrue(count >= 1, "应至少统计到1个用户"));
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
            // 1. Create - 新增用户
            User user = insertTestUser("crud_openid_001");
            assertAll("新增验证",
                    () -> assertNotNull(user.getId(), "新增后ID应自动生成"));

            // 2. Read - 查询用户(通过OpenID)
            User foundByOpenid = userMapper.getByOpenid(user.getOpenid());
            assertAll("OpenID查询验证",
                    () -> assertNotNull(foundByOpenid, "应能通过OpenID查询到用户"),
                    () -> assertEquals("crud_openid_001", foundByOpenid.getOpenid(), "OpenID应匹配"));

            // 3. Read - 查询用户(通过ID)
            User foundById = userMapper.getById(user.getId());
            assertEquals("测试用户", foundById.getName(), "姓名应匹配");

            // 4. Count - 统计验证
            Map<String, Object> params = new HashMap<>();
            params.put("begin", user.getCreateTime().minusSeconds(1));
            params.put("end", user.getCreateTime().plusSeconds(1));
            Integer count = userMapper.countByMap(params);
            assertTrue(count >= 1, "应统计到至少1个用户");
        }

        @Test
        @DisplayName("用户微信登录流程测试")
        void shouldHandleWeChatLoginProcess() {
            // Given - 模拟微信登录,获取到OpenID
            String wechatOpenid = "wechat_login_openid_001";

            // When - 第一次登录,用户不存在
            User firstTimeUser = userMapper.getByOpenid(wechatOpenid);
            assertNull(firstTimeUser, "首次登录时应查询不到用户");

            // When - 自动创建新用户
            User newUser = createTestUser(wechatOpenid);
            newUser.setName("微信用户");
            userMapper.insert(newUser);

            // Then - 再次登录时能查询到用户
            User existingUser = userMapper.getByOpenid(wechatOpenid);
            assertAll("登录流程验证",
                    () -> assertNotNull(existingUser, "应能查询到已注册用户"),
                    () -> assertEquals("微信用户", existingUser.getName(), "用户姓名应匹配"),
                    () -> assertEquals(wechatOpenid, existingUser.getOpenid(), "OpenID应匹配"));
        }

        @Test
        @DisplayName("用户信息查询流程测试")
        void shouldQueryUserInformationCorrectly() {
            // Given - 创建测试用户
            String testOpenid = "query_info_openid_001";
            User user = createTestUser(testOpenid);
            user.setName("查询测试用户");
            user.setPhone("13500135000");
            user.setSex("0");
            user.setIdNumber("110101199005051234");
            userMapper.insert(user);

            // When - 通过OpenID查询
            User foundByOpenid = userMapper.getByOpenid(testOpenid);

            // Then - 验证查询结果
            assertAll("用户信息查询验证",
                    () -> assertNotNull(foundByOpenid, "应能查询到用户"),
                    () -> assertEquals(testOpenid, foundByOpenid.getOpenid(), "OpenID应匹配"),
                    () -> assertEquals("查询测试用户", foundByOpenid.getName(), "姓名应匹配"),
                    () -> assertEquals("13500135000", foundByOpenid.getPhone(), "手机号应匹配"),
                    () -> assertEquals("0", foundByOpenid.getSex(), "性别应匹配"),
                    () -> assertEquals("110101199005051234", foundByOpenid.getIdNumber(), "身份证号应匹配"));

            // When - 通过ID查询
            User foundById = userMapper.getById(user.getId());

            // Then - 验证查询结果一致
            assertEquals(foundByOpenid.getOpenid(), foundById.getOpenid(), "两次查询的OpenID应一致");
        }

        @Test
        @DisplayName("批量用户统计流程测试")
        void shouldCountMultipleUsersCorrectly() {
            // Given - 批量创建用户
            insertTestUser("batch_count_openid_001");
            insertTestUser("batch_count_openid_002");
            insertTestUser("batch_count_openid_003");

            // When - 统计所有用户
            Map<String, Object> allParams = new HashMap<>();
            Integer totalCount = userMapper.countByMap(allParams);

            // Then - 验证总数
            assertTrue(totalCount >= 3, "应至少有3个用户");

            // When - 根据性别统计
            Map<String, Object> sexParams = new HashMap<>();
            sexParams.put("sex", "1");
            Integer maleCount = userMapper.countByMap(sexParams);

            // Then - 验证性别统计
            assertTrue(maleCount >= 3, "应至少有3个男性用户");
        }

        @Test
        @DisplayName("用户唯一性验证流程测试")
        void shouldValidateUserUniqueness() {
            // Given - 创建一个用户
            String uniqueOpenid = "unique_openid_001";
            insertTestUser(uniqueOpenid);

            // When - 查询该OpenID
            User found = userMapper.getByOpenid(uniqueOpenid);

            // Then - 验证唯一性
            assertAll("唯一性验证",
                    () -> assertNotNull(found, "应能查询到用户"),
                    () -> assertEquals(uniqueOpenid, found.getOpenid(), "OpenID应唯一"));

            // When - 统计该OpenID的数量
            Map<String, Object> params = new HashMap<>();
            params.put("begin", found.getCreateTime().minusSeconds(1));
            params.put("end", found.getCreateTime().plusSeconds(1));
            Integer count = userMapper.countByMap(params);

            // Then - 验证唯一性
            assertTrue(count >= 1, "统计结果应至少包含刚插入的用户");
        }
    }
}
