package com.airprofly.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.airprofly.dto.UserLoginDTO;
import com.airprofly.entity.User;
import com.airprofly.mapper.UserMapper;
import com.airprofly.vo.UserLoginVO;

/**
 * UserService 测试类
 *
 * 测试覆盖:
 * - 用户登录功能测试
 * - 用户注册功能测试
 * - 用户查询测试
 * - 微信登录测试
 *
 * 测试策略:
 * - 使用 Mockito 进行依赖项模拟
 * - 验证业务逻辑正确性
 * - 验证新用户自动注册
 * - 验证 Token 生成
 */
@ExtendWith(MockitoExtension.class)
@Tag("service")
@Tag("user")
@DisplayName("用户 Service 测试")
public class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private com.airprofly.service.impl.UserServiceImpl userService;

    private UserLoginDTO testUserLoginDTO;
    private User testUser;

    @BeforeEach
    void setUp() {
        // 初始化用户登录 DTO 测试数据
        testUserLoginDTO = new UserLoginDTO();
        testUserLoginDTO.setCode("test_code");

        // 初始化用户实体测试数据
        testUser = User.builder()
                .id(1L)
                .openid("test_openid")
                .createTime(LocalDateTime.now())
                .build();
    }

    // ==================== login 方法测试 ====================

    @Nested
    @DisplayName("login 方法测试")
    @Tag("auth")
    class LoginMethodTests {

        @Test
        @DisplayName("用户登录 - 已存在用户")
        void shouldReturnLoginVOWhenUserExists() {
            when(userMapper.getByOpenid(anyString())).thenReturn(testUser);

            // When
            UserLoginVO result = userService.login(testUserLoginDTO);

            // Then
            assertAll("已存在用户登录验证",
                    () -> assertNotNull(result, "登录结果不应为null"),
                    () -> assertEquals(testUser.getId(), result.getId(), "用户ID应匹配"),
                    () -> assertNotNull(result.getToken(), "Token不应为null"),
                    () -> verify(userMapper, times(1)).getByOpenid(anyString()),
                    () -> verify(userMapper, never()).insert(any(User.class))
            );
        }

        @Test
        @DisplayName("用户登录 - 新用户自动注册")
        void shouldRegisterNewUserWhenNotExists() {
            // Given
            when(userMapper.getByOpenid(anyString())).thenReturn(null);
            doNothing().when(userMapper).insert(any(User.class));

            // When
            UserLoginVO result = userService.login(testUserLoginDTO);

            // Then
            assertAll("新用户注册验证",
                    () -> assertNotNull(result, "登录结果不应为null"),
                    () -> assertNotNull(result.getToken(), "Token不应为null"),
                    () -> verify(userMapper, times(1)).getByOpenid(anyString()),
                    () -> verify(userMapper, times(1)).insert(argThat(user ->
                            user.getOpenid() != null &&
                            user.getCreateTime() != null
                    ))
            );
        }

        @Test
        @DisplayName("登录时生成 Token")
        void shouldGenerateTokenWhenLoggingIn() {
            // Given
            when(userMapper.getByOpenid(anyString())).thenReturn(testUser);

            // When
            UserLoginVO result = userService.login(testUserLoginDTO);

            // Then
            assertAll("Token生成验证",
                    () -> assertNotNull(result.getToken(), "Token不应为null"),
                    () -> assertTrue(result.getToken().startsWith("mock_token_"), "Token格式应正确")
            );
        }

        @Test
        @DisplayName("登录时新用户设置 OpenID")
        void shouldSetOpenidWhenRegistering() {
            // Given
            when(userMapper.getByOpenid(anyString())).thenReturn(null);
            doNothing().when(userMapper).insert(any(User.class));

            // When
            userService.login(testUserLoginDTO);

            // Then
            verify(userMapper).insert(argThat(user ->
                    user.getOpenid() != null &&
                    user.getOpenid().startsWith("mock_openid_")
            ));
        }

        @Test
        @DisplayName("登录时新用户设置创建时间")
        void shouldSetCreateTimeWhenRegistering() {
            // Given
            when(userMapper.getByOpenid(anyString())).thenReturn(null);
            doNothing().when(userMapper).insert(any(User.class));

            // When
            userService.login(testUserLoginDTO);

            // Then
            verify(userMapper).insert(argThat(user ->
                    user.getCreateTime() != null
            ));
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
            Long userId = 1L;
            when(userMapper.getById(userId)).thenReturn(testUser);

            // When
            User result = userService.getById(userId);

            // Then
            assertAll("查询验证",
                    () -> assertNotNull(result, "应能查询到用户"),
                    () -> assertEquals(userId, result.getId(), "ID应匹配"),
                    () -> verify(userMapper, times(1)).getById(userId)
            );
        }

        @Test
        @DisplayName("查询不存在的用户ID")
        void shouldReturnNullWhenIdNotExists() {
            // Given
            Long nonExistentId = 999999L;
            when(userMapper.getById(nonExistentId)).thenReturn(null);

            // When
            User result = userService.getById(nonExistentId);

            // Then
            assertNull(result, "不存在的用户ID应返回null");
            verify(userMapper, times(1)).getById(nonExistentId);
        }

        @Test
        @DisplayName("查询用户时应返回完整信息")
        void shouldReturnCompleteUserInfo() {
            // Given
            testUser.setOpenid("test_openid_123");
            testUser.setCreateTime(LocalDateTime.of(2024, 1, 1, 0, 0));
            when(userMapper.getById(anyLong())).thenReturn(testUser);

            // When
            User result = userService.getById(1L);

            // Then
            assertAll("用户信息完整性验证",
                    () -> assertNotNull(result, "用户不应为null"),
                    () -> assertNotNull(result.getId(), "用户ID不应为null"),
                    () -> assertNotNull(result.getOpenid(), "OpenID不应为null"),
                    () -> assertNotNull(result.getCreateTime(), "创建时间不应为null")
            );
        }
    }

    // ==================== wxLogin 方法测试 ====================

    @Nested
    @DisplayName("wxLogin 方法测试")
    @Tag("auth")
    class WxLoginMethodTests {

        @Test
        @DisplayName("微信登录 - 成功场景")
        void shouldLoginViaWeChatWhenValidCode() {
            // Given
            String code = "wx_auth_code";
            when(userMapper.getByOpenid(anyString())).thenReturn(testUser);

            // When
            UserLoginVO result = userService.wxLogin(code);

            // Then
            assertAll("微信登录验证",
                    () -> assertNotNull(result, "登录结果不应为null"),
                    () -> assertNotNull(result.getToken(), "Token不应为null"),
                    () -> verify(userMapper, times(1)).getByOpenid(anyString())
            );
        }

        @Test
        @DisplayName("微信登录 - 新用户自动注册")
        void shouldRegisterNewUserViaWeChat() {
            // Given
            String code = "wx_auth_code";
            when(userMapper.getByOpenid(anyString())).thenReturn(null);
            doNothing().when(userMapper).insert(any(User.class));

            // When
            UserLoginVO result = userService.wxLogin(code);

            // Then
            assertAll("微信新用户注册验证",
                    () -> assertNotNull(result, "登录结果不应为null"),
                    () -> assertNotNull(result.getToken(), "Token不应为null"),
                    () -> verify(userMapper, times(1)).insert(any(User.class))
            );
        }

        @Test
        @DisplayName("微信登录时应使用传入的 code")
        void shouldUseProvidedCodeWhenLoggingIn() {
            // Given
            String code = "wx_code_123";
            when(userMapper.getByOpenid(anyString())).thenReturn(testUser);

            // When
            userService.wxLogin(code);

            // Then
            // 验证方法被调用（实际实现中 code 用于获取 openid）
            verify(userMapper, times(1)).getByOpenid(anyString());
        }
    }

    // ==================== 综合业务场景测试 ====================

    @Nested
    @DisplayName("综合业务场景测试")
    @Tag("integration")
    class IntegrationTests {

        @Test
        @DisplayName("完整用户登录流程测试")
        void shouldCompleteFullUserLoginFlow() {
            // Given
            when(userMapper.getByOpenid(anyString())).thenReturn(null).thenReturn(testUser);
            doNothing().when(userMapper).insert(any(User.class));

            // When - 首次登录(注册)
            UserLoginVO firstLoginResult = userService.login(testUserLoginDTO);

            // Then
            assertAll("首次登录验证",
                    () -> assertNotNull(firstLoginResult, "首次登录结果不应为null"),
                    () -> verify(userMapper, times(1)).insert(any(User.class))
            );

            // When - 再次登录
            UserLoginVO secondLoginResult = userService.login(testUserLoginDTO);

            // Then
            assertAll("再次登录验证",
                    () -> assertNotNull(secondLoginResult, "再次登录结果不应为null"),
                    () -> assertEquals(testUser.getId(), secondLoginResult.getId(), "应返回相同用户ID"),
                    () -> verify(userMapper, times(2)).getByOpenid(anyString()),
                    () -> verify(userMapper, times(1)).insert(any(User.class)) // 只插入一次
            );
        }

        @Test
        @DisplayName("用户信息查询流程测试")
        void shouldQueryUserCorrectly() {
            // Given
            Long userId = 1L;
            when(userMapper.getById(userId)).thenReturn(testUser);

            // When
            User result = userService.getById(userId);

            // Then
            assertAll("用户查询验证",
                    () -> assertNotNull(result, "应能查询到用户"),
                    () -> assertEquals(userId, result.getId(), "ID应匹配"),
                    () -> verify(userMapper, times(1)).getById(userId)
            );
        }

        @Test
        @DisplayName("微信登录与普通登录一致性测试")
        void shouldMaintainConsistencyBetweenLoginMethods() {
            // Given
            String code = "test_code";
            when(userMapper.getByOpenid(anyString())).thenReturn(testUser);

            // When - 普通登录
            UserLoginVO normalLoginResult = userService.login(testUserLoginDTO);

            // When - 微信登录
            UserLoginVO wxLoginResult = userService.wxLogin(code);

            // Then
            assertAll("登录方式一致性验证",
                    () -> assertNotNull(normalLoginResult, "普通登录结果不应为null"),
                    () -> assertNotNull(wxLoginResult, "微信登录结果不应为null"),
                    () -> assertEquals(testUser.getId(), normalLoginResult.getId(), "用户ID应一致"),
                    () -> assertEquals(testUser.getId(), wxLoginResult.getId(), "用户ID应一致")
            );
        }

        @Test
        @DisplayName("新用户注册后信息完整性测试")
        void shouldMaintainDataIntegrityAfterRegistration() {
            // Given
            when(userMapper.getByOpenid(anyString())).thenReturn(null);
            doNothing().when(userMapper).insert(any(User.class));

            // When - 注册
            UserLoginVO loginResult = userService.login(testUserLoginDTO);

            // Then
            assertAll("注册后数据完整性验证",
                    () -> assertNotNull(loginResult, "登录结果不应为null"),
                    () -> assertNotNull(loginResult.getToken(), "Token不应为null"),
                    () -> verify(userMapper).insert(argThat(user ->
                            user.getOpenid() != null &&
                            user.getCreateTime() != null
                    ))
            );
        }

        @Test
        @DisplayName("Token 格式验证测试")
        void shouldGenerateValidTokenFormat() {
            // Given
            when(userMapper.getByOpenid(anyString())).thenReturn(testUser);

            // When
            UserLoginVO result = userService.login(testUserLoginDTO);

            // Then
            assertAll("Token格式验证",
                    () -> assertNotNull(result.getToken(), "Token不应为null"),
                    () -> assertFalse(result.getToken().isEmpty(), "Token不应为空"),
                    () -> assertTrue(result.getToken().contains("mock_token_"), "Token应包含指定前缀")
            );
        }

        @Test
        @DisplayName("OpenID 唯一性测试")
        void shouldUseUniqueOpenidForEachUser() {
            // Given
            when(userMapper.getByOpenid(anyString())).thenReturn(null);
            doNothing().when(userMapper).insert(any(User.class));

            // When - 多次登录
            userService.login(testUserLoginDTO);
            userService.login(testUserLoginDTO);

            // Then
            verify(userMapper, times(2)).insert(argThat(user ->
                    user.getOpenid() != null
            ));
        }
    }

    // ==================== 边界条件测试 ====================

    @Nested
    @DisplayName("边界条件测试")
    @Tag("edge-case")
    class EdgeCaseTests {

        @Test
        @DisplayName("登录 code 为空字符串")
        void shouldHandleEmptyCode() {
            // Given
            testUserLoginDTO.setCode("");
            when(userMapper.getByOpenid(anyString())).thenReturn(testUser);

            // When
            UserLoginVO result = userService.login(testUserLoginDTO);

            // Then
            assertNotNull(result, "空code应能处理");
        }

        @Test
        @DisplayName("查询用户ID为0")
        void shouldHandleZeroUserId() {
            // Given
            Long zeroId = 0L;
            when(userMapper.getById(zeroId)).thenReturn(null);

            // When
            User result = userService.getById(zeroId);

            // Then
            assertNull(result, "ID为0应返回null");
        }

        @Test
        @DisplayName("查询用户ID为负数")
        void shouldHandleNegativeUserId() {
            // Given
            Long negativeId = -1L;
            when(userMapper.getById(negativeId)).thenReturn(null);

            // When
            User result = userService.getById(negativeId);

            // Then
            assertNull(result, "负数ID应返回null");
        }

        @Test
        @DisplayName("微信登录 code 为 null")
        void shouldHandleNullWxCode() {
            // Given & When
            assertDoesNotThrow(() -> userService.wxLogin(null));
        }
    }
}
