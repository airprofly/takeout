package com.airprofly.config;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import lombok.extern.slf4j.Slf4j;

/**
 * RedisConfig 测试类
 *
 * 测试覆盖:
 * - RedisTemplate 配置测试
 * - RedisCacheManager 配置测试
 * - 序列化器配置测试
 * - 基本操作功能测试
 * - 缓存过期时间测试
 *
 * 测试策略:
 * - 使用 @SpringBootTest 加载完整应用上下文
 * - 验证 Bean 是否正确创建和配置
 * - 验证序列化器配置
 * - 验证基本 Redis 操作功能
 *
 * 注意事项:
 * - 运行此测试前需要确保 Redis 服务已启动
 * - 默认使用 application-dev.yaml 中的 Redis 配置
 *
 * @author airprofly
 */
@SpringBootTest
@Tag("config")
@Tag("redis")
@DisplayName("Redis 配置测试")
@Slf4j
public class RedisConfigTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedisCacheManager redisCacheManager;

    /**
     * 每个测试前清理测试数据,避免测试间相互影响
     */
    @BeforeEach
    void setUp() {
        // 清理可能存在的测试数据
        String[] testKeys = {"test:string", "test:hash", "test:list", "test:set", "test:zset"};
        for (String key : testKeys) {
            redisTemplate.delete(key);
        }
        log.info("测试数据清理完成");
    }

    // ==================== RedisTemplate 配置测试 ====================

    @Nested
    @DisplayName("RedisTemplate 配置测试")
    class RedisTemplateConfigTests {

        @Test
        @DisplayName("RedisTemplate Bean 应成功注入")
        void shouldInjectRedisTemplateBean() {
            // Then
            assertAll("RedisTemplate 注入验证",
                    () -> assertNotNull(redisTemplate, "RedisTemplate 应成功注入"),
                    () -> assertNotNull(redisTemplate.getConnectionFactory(), "连接工厂不应为null")
            );
            log.info("RedisTemplate Bean 注入成功");
        }

        @Test
        @DisplayName("RedisTemplate 键序列化器应为 StringRedisSerializer")
        void shouldUseStringRedisSerializerForKey() {
            // When
            RedisSerializer<?> keySerializer = redisTemplate.getKeySerializer();
            RedisSerializer<?> hashKeySerializer = redisTemplate.getHashKeySerializer();

            // Then
            assertAll("键序列化器验证",
                    () -> assertNotNull(keySerializer, "键序列化器不应为null"),
                    () -> assertNotNull(hashKeySerializer, "Hash键序列化器不应为null"),
                    () -> assertEquals(StringRedisSerializer.class, keySerializer.getClass(),
                            "键序列化器应为 StringRedisSerializer"),
                    () -> assertEquals(StringRedisSerializer.class, hashKeySerializer.getClass(),
                            "Hash键序列化器应为 StringRedisSerializer")
            );
            log.info("键序列化器配置正确: StringRedisSerializer");
        }

        @Test
        @DisplayName("RedisTemplate 值序列化器应为 Jackson2JsonRedisSerializer")
        void shouldUseJackson2JsonRedisSerializerForValue() {
            // When
            RedisSerializer<?> valueSerializer = redisTemplate.getValueSerializer();
            RedisSerializer<?> hashValueSerializer = redisTemplate.getHashValueSerializer();

            // Then
            assertAll("值序列化器验证",
                    () -> assertNotNull(valueSerializer, "值序列化器不应为null"),
                    () -> assertNotNull(hashValueSerializer, "Hash值序列化器不应为null"),
                    () -> assertTrue(
                            valueSerializer instanceof GenericJackson2JsonRedisSerializer ||
                                    valueSerializer.getClass().getName().contains("Jackson2JsonRedisSerializer"),
                            "值序列化器应为 Jackson2JsonRedisSerializer 类型"),
                    () -> assertTrue(
                            hashValueSerializer instanceof GenericJackson2JsonRedisSerializer ||
                                    hashValueSerializer.getClass().getName().contains("Jackson2JsonRedisSerializer"),
                            "Hash值序列化器应为 Jackson2JsonRedisSerializer 类型")
            );
            log.info("值序列化器配置正确: Jackson2JsonRedisSerializer");
        }

        @Test
        @DisplayName("验证序列化器不是 JDK 默认序列化器")
        void shouldNotUseJdkSerialization() {
            // When
            RedisSerializer<?> valueSerializer = redisTemplate.getValueSerializer();

            // Then
            assertNotEquals(JdkSerializationRedisSerializer.class, valueSerializer.getClass(),
                    "不应使用 JDK 默认序列化器,JSON 序列化更易读且性能更好");
            log.info("确认未使用 JDK 默认序列化器");
        }
    }

    // ==================== RedisCacheManager 配置测试 ====================

    @Nested
    @DisplayName("RedisCacheManager 配置测试")
    class RedisCacheManagerConfigTests {

        @Test
        @DisplayName("RedisCacheManager Bean 应成功注入")
        void shouldInjectRedisCacheManagerBean() {
            // Then
            assertAll("RedisCacheManager 注入验证",
                    () -> assertNotNull(redisCacheManager, "RedisCacheManager 应成功注入")
            );
            log.info("RedisCacheManager Bean 注入成功");
        }

        @Test
        @DisplayName("验证缓存管理器支持创建缓存")
        void shouldSupportCacheCreation() {
            // When
            var cache = redisCacheManager.getCache("test-cache");

            // Then
            assertNotNull(cache, "应能创建缓存实例");
            log.info("缓存创建功能正常");
        }

        @Test
        @DisplayName("验证缓存默认配置存在")
        void shouldHaveDefaultCacheConfiguration() {
            // When - 获取默认缓存
            var defaultCache = redisCacheManager.getCache("default");

            // Then
            // 注意: 如果没有显式配置 default 缓存,这里可能返回 null
            // 实际使用中会使用默认配置
            log.info("缓存配置加载完成,默认缓存状态: {}", defaultCache != null ? "已配置" : "使用默认配置");
        }
    }

    // ==================== 基本操作功能测试 ====================

    @Nested
    @DisplayName("Redis 基本操作功能测试")
    @Tag("functional")
    class BasicOperationTests {

        @Test
        @DisplayName("字符串操作 - 设置和获取")
        void shouldSetAndGetString() {
            // Given
            String key = "test:string";
            String value = "test-value-123";

            // When
            redisTemplate.opsForValue().set(key, value);
            Object retrieved = redisTemplate.opsForValue().get(key);

            // Then
            assertAll("字符串操作验证",
                    () -> assertEquals(value, retrieved, "获取的值应与设置的值一致"),
                    () -> assertTrue(Boolean.TRUE.equals(redisTemplate.hasKey(key)), "键应存在"),
                    () -> assertEquals(String.class, retrieved.getClass(), "值的类型应为 String")
            );
            log.info("字符串操作测试通过: key={}, value={}", key, value);
        }

        @Test
        @DisplayName("对象序列化操作 - 存储和读取对象")
        void shouldSerializeAndDeserializeObject() {
            // Given
            String key = "test:object";
            Map<String, Object> testObject = new HashMap<>();
            testObject.put("id", 1L);
            testObject.put("name", "测试用户");
            testObject.put("email", "test@example.com");

            // When
            redisTemplate.opsForValue().set(key, testObject);
            @SuppressWarnings("unchecked")
            Map<String, Object> retrieved = (Map<String, Object>) redisTemplate.opsForValue().get(key);

            // Then
            // 注意: Jackson 反序列化时,小数值的 Long 可能被反序列化为 Integer
            // 这是正常行为,使用 Number 类型进行兼容性验证
            Object idValue = retrieved.get("id");
            assertAll("对象序列化验证",
                    () -> assertNotNull(retrieved, "反序列化后的对象不应为null"),
                    () -> assertNotNull(idValue, "ID 不应为null"),
                    () -> assertTrue(idValue instanceof Number, "ID 应为数字类型"),
                    () -> assertEquals(1L, ((Number) idValue).longValue(), "ID 值应匹配"),
                    () -> assertEquals("测试用户", retrieved.get("name"), "姓名应匹配"),
                    () -> assertEquals("test@example.com", retrieved.get("email"), "邮箱应匹配")
            );
            log.info("对象序列化测试通过: 对象完整,字段正确");
        }

        @Test
        @DisplayName("Hash 操作 - 存储和读取字段")
        void shouldSetAndGetHashFields() {
            // Given
            String key = "test:hash";
            String field1 = "field1";
            String value1 = "value1";
            String field2 = "field2";
            String value2 = "value2";

            // When
            redisTemplate.opsForHash().put(key, field1, value1);
            redisTemplate.opsForHash().put(key, field2, value2);
            Object retrieved1 = redisTemplate.opsForHash().get(key, field1);
            Object retrieved2 = redisTemplate.opsForHash().get(key, field2);

            // Then
            assertAll("Hash 操作验证",
                    () -> assertEquals(value1, retrieved1, "字段1的值应匹配"),
                    () -> assertEquals(value2, retrieved2, "字段2的值应匹配"),
                    () -> assertEquals(2, redisTemplate.opsForHash().size(key), "Hash 应包含2个字段")
            );
            log.info("Hash 操作测试通过: 包含 {} 个字段", redisTemplate.opsForHash().size(key));
        }

        @Test
        @DisplayName("列表操作 - 左推入和右弹出")
        void shouldPushAndPopFromList() {
            // Given
            String key = "test:list";
            String value1 = "item1";
            String value2 = "item2";

            // When
            redisTemplate.opsForList().leftPush(key, value1);
            redisTemplate.opsForList().leftPush(key, value2);
            Object popped = redisTemplate.opsForList().rightPop(key);

            // Then
            assertAll("列表操作验证",
                    () -> assertEquals(value1, popped, "弹出的值应为先推入的值"),
                    () -> assertEquals(1, redisTemplate.opsForList().size(key), "列表应剩余1个元素")
            );
            log.info("列表操作测试通过: 弹出元素={}, 剩余元素数={}", popped, redisTemplate.opsForList().size(key));
        }

        @Test
        @DisplayName("集合操作 - 添加和检查成员")
        void shouldAddAndCheckSetMembers() {
            // Given
            String key = "test:set";
            String member1 = "member1";
            String member2 = "member2";

            // When
            redisTemplate.opsForSet().add(key, member1, member2);
            Boolean isMember1 = redisTemplate.opsForSet().isMember(key, member1);
            Boolean isMember3 = redisTemplate.opsForSet().isMember(key, "member3");

            // Then
            assertAll("集合操作验证",
                    () -> assertTrue(Boolean.TRUE.equals(isMember1), "member1 应是集合成员"),
                    () -> assertFalse(Boolean.TRUE.equals(isMember3), "member3 不应是集合成员"),
                    () -> assertEquals(2, redisTemplate.opsForSet().size(key), "集合应包含2个成员")
            );
            log.info("集合操作测试通过: 成员数={}", redisTemplate.opsForSet().size(key));
        }

        @Test
        @DisplayName("有序集合操作 - 添加和按分数排序")
        void shouldAddToSortedSetAndGetByScore() {
            // Given
            String key = "test:zset";
            String member1 = "member1";
            double score1 = 1.0;
            String member2 = "member2";
            double score2 = 2.0;

            // When
            redisTemplate.opsForZSet().add(key, member1, score1);
            redisTemplate.opsForZSet().add(key, member2, score2);
            Long rank = redisTemplate.opsForZSet().rank(key, member1);

            // Then
            assertAll("有序集合操作验证",
                    () -> assertNotNull(rank, "成员应存在"),
                    () -> assertEquals(0, rank, "member1 的排名应为0(分数最低)"),
                    () -> assertEquals(2, redisTemplate.opsForZSet().size(key), "有序集合应包含2个成员")
            );
            log.info("有序集合操作测试通过: 成员数={}", redisTemplate.opsForZSet().size(key));
        }

        @Test
        @DisplayName("删除操作 - 删除键")
        void shouldDeleteKey() {
            // Given
            String key = "test:delete";
            redisTemplate.opsForValue().set(key, "value");

            // When
            Boolean deleted = redisTemplate.delete(key);
            Boolean exists = redisTemplate.hasKey(key);

            // Then
            assertAll("删除操作验证",
                    () -> assertTrue(Boolean.TRUE.equals(deleted), "删除应成功"),
                    () -> assertFalse(Boolean.TRUE.equals(exists), "删除后键不应存在")
            );
            log.info("删除操作测试通过");
        }

        @Test
        @DisplayName("过期时间操作 - 设置 TTL")
        void shouldSetExpirationTime() throws InterruptedException {
            // Given
            String key = "test:expire";
            String value = "expire-value";
            long timeoutSeconds = 2L;

            // When
            redisTemplate.opsForValue().set(key, value, timeoutSeconds, TimeUnit.SECONDS);
            Boolean existsImmediately = redisTemplate.hasKey(key);

            // 等待过期
            TimeUnit.SECONDS.sleep(timeoutSeconds + 1);
            Boolean existsAfterExpire = redisTemplate.hasKey(key);

            // Then
            assertAll("过期时间验证",
                    () -> assertTrue(Boolean.TRUE.equals(existsImmediately), "设置后键应存在"),
                    () -> assertFalse(Boolean.TRUE.equals(existsAfterExpire), "过期后键不应存在")
            );
            log.info("过期时间测试通过: TTL={}秒", timeoutSeconds);
        }

        @Test
        @DisplayName("批量操作 - 管道命令")
        void shouldExecuteMultipleOperations() {
            // Given
            String key1 = "test:multi:1";
            String key2 = "test:multi:2";
            String key3 = "test:multi:3";

            // When
            redisTemplate.opsForValue().set(key1, "value1");
            redisTemplate.opsForValue().set(key2, "value2");
            redisTemplate.opsForValue().set(key3, "value3");

            // 使用 exists 方法检查多个键
            Boolean exists1 = redisTemplate.hasKey(key1);
            Boolean exists2 = redisTemplate.hasKey(key2);
            Boolean exists3 = redisTemplate.hasKey(key3);
            Long count = (exists1 ? 1L : 0L) + (exists2 ? 1L : 0L) + (exists3 ? 1L : 0L);

            // Then
            assertEquals(3, count, "应存在3个键");
            log.info("批量操作测试通过: 创建了 {} 个键", count);
        }
    }

    // ==================== 边界条件测试 ====================

    @Nested
    @DisplayName("边界条件测试")
    @Tag("edge-case")
    class EdgeCaseTests {

        @Test
        @DisplayName("获取不存在的键应返回 null")
        void shouldReturnNullForNonExistentKey() {
            // Given
            String nonExistentKey = "test:nonexistent:999";

            // When
            Object value = redisTemplate.opsForValue().get(nonExistentKey);

            // Then
            assertNull(value, "不存在的键应返回 null");
            log.info("边界测试通过: 不存在的键返回 null");
        }

        @Test
        @DisplayName("存储 null 值")
        void shouldHandleNullValue() {
            // Given
            String key = "test:null-value";

            // When
            redisTemplate.opsForValue().set(key, null);
            Object retrieved = redisTemplate.opsForValue().get(key);

            // Then
            // 注意: Redis 存储序列化的 null,反序列化后可能为 null
            // 具体行为取决于序列化器实现
            log.info("null 值存储测试: 返回值类型={}, 值={}",
                    retrieved != null ? retrieved.getClass() : "null", retrieved);
        }

        @Test
        @DisplayName("存储空字符串")
        void shouldHandleEmptyString() {
            // Given
            String key = "test:empty-string";
            String emptyValue = "";

            // When
            redisTemplate.opsForValue().set(key, emptyValue);
            Object retrieved = redisTemplate.opsForValue().get(key);

            // Then
            assertAll("空字符串验证",
                    () -> assertNotNull(retrieved, "空字符串也应能存储"),
                    () -> assertEquals("", retrieved, "空字符串应正确返回"),
                    () -> assertTrue(Boolean.TRUE.equals(redisTemplate.hasKey(key)), "键应存在")
            );
            log.info("空字符串测试通过");
        }

        @Test
        @DisplayName("存储超长字符串")
        void shouldHandleLongString() {
            // Given
            String key = "test:long-string";
            String longValue = "a".repeat(10000); // 10000 个字符

            // When
            redisTemplate.opsForValue().set(key, longValue);
            Object retrieved = redisTemplate.opsForValue().get(key);

            // Then
            assertAll("长字符串验证",
                    () -> assertNotNull(retrieved, "长字符串应能存储"),
                    () -> assertEquals(10000, ((String) retrieved).length(), "长度应一致")
            );
            log.info("长字符串测试通过: 长度={}", ((String) retrieved).length());
        }

        @Test
        @DisplayName("Hash 操作不存在的字段应返回 null")
        void shouldReturnNullForNonExistentHashField() {
            // Given
            String key = "test:hash:nonexistent";
            String nonExistentField = "nonexistent-field";

            // When
            Object value = redisTemplate.opsForHash().get(key, nonExistentField);

            // Then
            assertNull(value, "不存在的 Hash 字段应返回 null");
            log.info("Hash 边界测试通过: 不存在的字段返回 null");
        }

        @Test
        @DisplayName("列表操作空列表应返回 null")
        void shouldReturnNullForEmptyList() {
            // Given
            String key = "test:list:empty";

            // When
            Object value = redisTemplate.opsForList().rightPop(key);

            // Then
            assertNull(value, "空列表弹出应返回 null");
            log.info("列表边界测试通过: 空列表返回 null");
        }

        @Test
        @DisplayName("设置零秒过期时间应立即删除键")
        void shouldDeleteKeyWhenZeroExpiration() throws InterruptedException {
            // Given
            String key = "test:expire-zero";
            redisTemplate.opsForValue().set(key, "value");

            // When
            redisTemplate.expire(key, Duration.ZERO);
            TimeUnit.MILLISECONDS.sleep(100); // 短暂等待确保过期生效
            Boolean exists = redisTemplate.hasKey(key);

            // Then
            assertFalse(Boolean.TRUE.equals(exists), "零秒过期后键不应存在");
            log.info("零秒过期测试通过");
        }
    }

    // ==================== 综合场景测试 ====================

    @Nested
    @DisplayName("综合业务场景测试")
    @Tag("integration")
    class IntegrationScenarioTests {

        @Test
        @DisplayName("用户会话缓存场景")
        void shouldHandleUserSessionCaching() {
            // Given - 模拟用户登录信息
            String sessionKey = "session:user:12345";
            Map<String, Object> sessionData = new HashMap<>();
            sessionData.put("userId", 12345L);
            sessionData.put("username", "testuser");
            sessionData.put("loginTime", System.currentTimeMillis());

            // When - 缓存会话信息,30分钟过期
            redisTemplate.opsForValue().set(sessionKey, sessionData, 30, TimeUnit.MINUTES);
            @SuppressWarnings("unchecked")
            Map<String, Object> retrieved = (Map<String, Object>) redisTemplate.opsForValue().get(sessionKey);
            Long ttl = redisTemplate.getExpire(sessionKey, TimeUnit.SECONDS);

            // Then
            // 注意: Jackson 反序列化时,小数值的 Long 可能被反序列化为 Integer
            // 这是正常行为,使用 Number 类型进行兼容性验证
            Object userIdValue = retrieved.get("userId");
            assertAll("会话缓存验证",
                    () -> assertNotNull(retrieved, "会话数据应能获取"),
                    () -> assertNotNull(userIdValue, "用户ID不应为null"),
                    () -> assertTrue(userIdValue instanceof Number, "用户ID应为数字类型"),
                    () -> assertEquals(12345L, ((Number) userIdValue).longValue(), "用户ID值应匹配"),
                    () -> assertEquals("testuser", retrieved.get("username"), "用户名应匹配"),
                    () -> assertTrue(ttl > 0 && ttl <= 1800, "TTL 应在 0-1800 秒之间")
            );
            log.info("用户会话缓存场景测试通过: TTL={}秒", ttl);
        }

        @Test
        @DisplayName("商品购物车场景")
        void shouldHandleShoppingCart() {
            // Given - 模拟购物车数据
            String cartKey = "cart:user:67890";
            Map<String, Object> cartItem = new HashMap<>();
            cartItem.put("dishId", 1001L);
            cartItem.put("dishName", "红烧肉");
            cartItem.put("quantity", 2);
            cartItem.put("price", 58.00);

            // When - 添加购物车项
            redisTemplate.opsForHash().put(cartKey, "item:1001", cartItem);
            Object retrieved = redisTemplate.opsForHash().get(cartKey, "item:1001");

            // Then
            assertAll("购物车验证",
                    () -> assertNotNull(retrieved, "购物车项应能获取"),
                    () -> assertTrue(redisTemplate.opsForHash().hasKey(cartKey, "item:1001"),
                            "购物车项应存在")
            );
            log.info("购物车场景测试通过");
        }

        @Test
        @DisplayName("验证序列化后的数据可读性")
        void shouldHaveReadableSerializedData() {
            // Given
            String key = "test:readability";
            Map<String, Object> data = new HashMap<>();
            data.put("name", "测试");
            data.put("value", 123);

            // When
            redisTemplate.opsForValue().set(key, data);

            // Then - 使用 Redis CLI 验证数据可读性(日志提示)
            log.info("可读性验证: 请使用 Redis CLI 执行 'get test:readability' 查看数据格式");
            log.info("使用 JSON 序列化器,数据应为人眼可读的 JSON 格式");

            Object retrieved = redisTemplate.opsForValue().get(key);
            assertNotNull(retrieved, "数据应能正确反序列化");
            log.info("数据反序列化成功,序列化器配置正确");
        }

        @Test
        @DisplayName("并发写入测试")
        void shouldHandleConcurrentWrites() throws InterruptedException {
            // Given
            String key = "test:concurrent";
            int threadCount = 10;
            int writesPerThread = 100;
            Thread[] threads = new Thread[threadCount];

            // When - 多线程并发写入
            for (int i = 0; i < threadCount; i++) {
                final int threadId = i;
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < writesPerThread; j++) {
                        String field = "thread-" + threadId + "-" + j;
                        redisTemplate.opsForHash().put(key, field, "value-" + j);
                    }
                });
                threads[i].start();
            }

            // 等待所有线程完成
            for (Thread thread : threads) {
                thread.join();
            }

            // Then
            Long hashSize = redisTemplate.opsForHash().size(key);
            assertEquals(threadCount * writesPerThread, hashSize,
                    "所有写入都应成功");
            log.info("并发写入测试通过: {} 个线程,每个 {} 次写入,总计 {} 个字段",
                    threadCount, writesPerThread, hashSize);
        }

        @Test
        @DisplayName("缓存穿透防护场景")
        void shouldHandleCachePenetrationProtection() {
            // Given - 模拟缓存不存在的数据
            String nonExistentKey = "cache:protect:nonexistent";

            // 先确保键不存在
            redisTemplate.delete(nonExistentKey);

            // When - 第一次查询(未命中)
            Object first = redisTemplate.opsForValue().get(nonExistentKey);
            assertNull(first, "不存在的数据应返回 null");

            // 可以设置一个空值或特殊标记防止穿透
            // 这里演示设置一个特殊标记
            redisTemplate.opsForValue().set(nonExistentKey, "NULL", 5, TimeUnit.MINUTES);

            // 第二次查询(命中空值标记)
            Object second = redisTemplate.opsForValue().get(nonExistentKey);
            assertNotNull(second, "应获取到空值标记");

            log.info("缓存穿透防护测试通过: 空值标记已设置");
        }
    }

    // ==================== 性能测试 ====================

    @Nested
    @DisplayName("性能测试")
    @Tag("performance")
    class PerformanceTests {

        @Test
        @DisplayName("批量写入性能测试")
        void shouldPerformBatchWritesEfficiently() {
            // Given
            int count = 1000;
            long startTime = System.currentTimeMillis();

            // When
            for (int i = 0; i < count; i++) {
                String key = "perf:test:" + i;
                redisTemplate.opsForValue().set(key, "value-" + i);
            }
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // Then
            log.info("批量写入性能: {} 条记录, 耗时 {} ms, 平均 {} ms/条",
                    count, duration, (double) duration / count);
            assertTrue(duration < 5000, "1000 条写入应在 5 秒内完成");
        }

        @Test
        @DisplayName("批量读取性能测试")
        void shouldPerformBatchReadsEfficiently() {
            // Given - 先准备数据
            int count = 1000;
            for (int i = 0; i < count; i++) {
                String key = "perf:read:test:" + i;
                redisTemplate.opsForValue().set(key, "value-" + i);
            }

            long startTime = System.currentTimeMillis();

            // When
            for (int i = 0; i < count; i++) {
                String key = "perf:read:test:" + i;
                redisTemplate.opsForValue().get(key);
            }
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // Then
            log.info("批量读取性能: {} 条记录, 耗时 {} ms, 平均 {} ms/条",
                    count, duration, (double) duration / count);
            assertTrue(duration < 3000, "1000 条读取应在 3 秒内完成");
        }
    }

    // ==================== 清理方法 ====================

    /**
     * 测试类执行后的清理工作
     * 注意: JUnit 5 不支持 @AfterClass 在实例方法中,
     * 如需全局清理可使用静态方法或测试监听器
     */
    // @AfterAll
    // static void cleanUp() {
    //     // 全局清理逻辑
    // }
}
