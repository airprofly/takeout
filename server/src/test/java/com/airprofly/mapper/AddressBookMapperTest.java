package com.airprofly.mapper;

import static org.junit.jupiter.api.Assertions.*;

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

import com.airprofly.entity.AddressBook;

/**
 * AddressBookMapper 测试类
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
@Tag("address-book")
@DisplayName("地址簿 Mapper 测试")
public class AddressBookMapperTest {

    @Autowired
    private AddressBookMapper addressBookMapper;

    // ==================== 测试数据工厂方法 ====================

    /**
     * 创建标准的测试地址簿对象
     */
    private AddressBook createTestAddressBook(Long userId) {
        return AddressBook.builder()
                .userId(userId)
                .consignee("测试收货人")
                .phone("13800138000")
                .sex("1")
                .provinceCode("110000")
                .provinceName("北京市")
                .cityCode("110100")
                .cityName("市辖区")
                .districtCode("110101")
                .districtName("东城区")
                .detail("测试详细地址")
                .label("家")
                .isDefault(0)
                .build();
    }

    /**
     * 创建并插入测试地址簿,返回插入后的对象(包含生成的ID)
     */
    private AddressBook insertTestAddressBook(Long userId) {
        AddressBook addressBook = createTestAddressBook(userId);
        addressBookMapper.insert(addressBook);
        return addressBook;
    }

    // ==================== list 方法测试 ====================

    @Nested
    @DisplayName("list 方法测试")
    @Tag("query")
    class ListMethodTests {

        @Test
        @DisplayName("根据用户ID查询地址簿列表 - 成功场景")
        void shouldReturnAddressBookListWhenUserIdExists() {
            // Given
            AddressBook query = AddressBook.builder()
                    .userId(1L)
                    .build();

            // When
            List<AddressBook> addressBooks = addressBookMapper.list(query);

            // Then
            assertNotNull(addressBooks, "地址簿列表不应为null");
        }

        @Test
        @DisplayName("用户不存在时返回空列表")
        void shouldReturnEmptyListWhenUserIdNotExists() {
            // Given
            AddressBook query = AddressBook.builder()
                    .userId(999999L)
                    .build();

            // When
            List<AddressBook> addressBooks = addressBookMapper.list(query);

            // Then
            assertAll("空列表验证",
                    () -> assertNotNull(addressBooks, "地址簿列表不应为null"),
                    () -> assertTrue(addressBooks.isEmpty(), "不存在的用户ID应返回空列表")
            );
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("查询参数为null时返回空列表")
        @Tag("edge-case")
        void shouldReturnEmptyListWhenQueryIsNull(AddressBook query) {
            // When
            List<AddressBook> addressBooks = addressBookMapper.list(query);

            // Then
            assertNotNull(addressBooks, "地址簿列表不应为null");
        }

        @Test
        @DisplayName("验证返回数据完整性")
        void shouldReturnCompleteAddressBookData() {
            // Given
            AddressBook query = AddressBook.builder()
                    .userId(1L)
                    .build();

            // When
            List<AddressBook> addressBooks = addressBookMapper.list(query);

            // Then
            if (!addressBooks.isEmpty()) {
                AddressBook firstAddress = addressBooks.get(0);
                assertAll("地址簿数据完整性验证",
                        () -> assertNotNull(firstAddress.getId(), "地址簿ID不应为null"),
                        () -> assertNotNull(firstAddress.getUserId(), "用户ID不应为null"),
                        () -> assertNotNull(firstAddress.getConsignee(), "收货人不应为null"),
                        () -> assertNotNull(firstAddress.getPhone(), "手机号不应为null"),
                        () -> assertNotNull(firstAddress.getDetail(), "详细地址不应为null")
                );
            }
        }

        @Test
        @DisplayName("验证默认地址查询")
        void shouldReturnDefaultAddressWhenIsDefaultIsOne() {
            // Given
            insertTestAddressBook(1L);
            AddressBook query = AddressBook.builder()
                    .userId(1L)
                    .isDefault(1)
                    .build();

            // When
            List<AddressBook> addressBooks = addressBookMapper.list(query);

            // Then
            assertNotNull(addressBooks, "地址簿列表不应为null");
        }
    }

    // ==================== insert 方法测试 ====================

    @Nested
    @DisplayName("insert 方法测试")
    @Tag("create")
    class InsertMethodTests {

        @Test
        @DisplayName("新增地址簿 - 成功场景")
        void shouldInsertAddressBookWhenValidData() {
            // Given
            AddressBook addressBook = createTestAddressBook(1L);

            // When
            addressBookMapper.insert(addressBook);

            // Then
            assertAll("插入验证",
                    () -> assertNotNull(addressBook.getId(), "插入后ID应自动生成"),
                    () -> {
                        AddressBook inserted = addressBookMapper.getById(addressBook.getId());
                        assertNotNull(inserted, "插入的数据应能被查询到");
                        assertEquals("测试收货人", inserted.getConsignee(), "收货人应匹配");
                        assertEquals("13800138000", inserted.getPhone(), "手机号应匹配");
                    }
            );
        }

        @Test
        @DisplayName("必填字段为null时应失败")
        @Tag("edge-case")
        void shouldFailToInsertWhenRequiredFieldsAreNull() {
            // Given
            AddressBook addressBook = AddressBook.builder()
                    .userId(1L)
                    // consignee 为 null
                    .phone("13800138000")
                    .build();

            // When & Then
            assertThrows(DataIntegrityViolationException.class, () -> {
                addressBookMapper.insert(addressBook);
            }, "必填字段为null时应抛出异常");
        }

        @ParameterizedTest
        @CsvSource({
            "0, 女",
            "1, 男"
        })
        @DisplayName("新增不同性别的地址簿")
        @Tag("parameterized")
        void shouldInsertAddressBookWithDifferentSex(String sex, String sexDesc) {
            // Given
            AddressBook addressBook = createTestAddressBook(1L);
            addressBook.setSex(sex);

            // When
            addressBookMapper.insert(addressBook);

            // Then
            AddressBook inserted = addressBookMapper.getById(addressBook.getId());
            assertAll("性别验证",
                    () -> assertNotNull(inserted, "应成功插入"),
                    () -> assertEquals(sex, inserted.getSex(), "性别应匹配")
            );
        }
    }

    // ==================== getById 方法测试 ====================

    @Nested
    @DisplayName("getById 方法测试")
    @Tag("query")
    class GetByIdMethodTests {

        @Test
        @DisplayName("根据ID查询地址簿 - 成功场景")
        void shouldReturnAddressBookWhenIdExists() {
            // Given
            AddressBook inserted = insertTestAddressBook(1L);
            Long insertedId = inserted.getId();

            // When
            AddressBook found = addressBookMapper.getById(insertedId);

            // Then
            assertAll("查询验证",
                    () -> assertNotNull(found, "应能查询到插入的地址簿"),
                    () -> assertEquals(insertedId, found.getId(), "ID应匹配"),
                    () -> assertEquals("测试收货人", found.getConsignee(), "收货人应匹配")
            );
        }

        @ParameterizedTest
        @ValueSource(longs = {999999L, -1L, 0L})
        @DisplayName("ID不存在时返回null")
        @Tag("edge-case")
        void shouldReturnNullWhenIdNotExists(Long nonExistentId) {
            // When
            AddressBook found = addressBookMapper.getById(nonExistentId);

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
        @DisplayName("修改地址簿 - 成功场景")
        void shouldUpdateAddressBookWhenValidData() {
            // Given
            AddressBook addressBook = insertTestAddressBook(1L);
            Long insertedId = addressBook.getId();

            // When
            AddressBook updateData = AddressBook.builder()
                    .id(insertedId)
                    .consignee("修改后收货人")
                    .phone("13900139000")
                    .detail("修改后详细地址")
                    .build();
            addressBookMapper.update(updateData);

            // Then
            AddressBook updated = addressBookMapper.getById(insertedId);
            assertAll("修改验证",
                    () -> assertNotNull(updated, "修改后应能查询到数据"),
                    () -> assertEquals("修改后收货人", updated.getConsignee(), "收货人应被修改"),
                    () -> assertEquals("13900139000", updated.getPhone(), "手机号应被修改"),
                    () -> assertEquals("修改后详细地址", updated.getDetail(), "详细地址应被修改")
            );
        }

        @Test
        @DisplayName("修改不存在的ID时不报错")
        @Tag("edge-case")
        void shouldNotThrowExceptionWhenUpdatingNonExistentId() {
            // Given
            AddressBook updateData = AddressBook.builder()
                    .id(999999L)
                    .consignee("不存在的收货人")
                    .build();

            // When & Then
            assertDoesNotThrow(() -> {
                addressBookMapper.update(updateData);
            }, "修改不存在的ID时不应抛出异常");
        }
    }

    // ==================== updateIsDefaultByUserId 方法测试 ====================

    @Nested
    @DisplayName("updateIsDefaultByUserId 方法测试")
    @Tag("update")
    class UpdateIsDefaultByUserIdMethodTests {

        @Test
        @DisplayName("根据用户ID修改默认地址 - 成功场景")
        void shouldUpdateDefaultAddressWhenUserIdExists() {
            // Given
            insertTestAddressBook(1L);

            // When
            AddressBook updateParam = AddressBook.builder()
                    .userId(1L)
                    .isDefault(0)
                    .build();
            addressBookMapper.updateIsDefaultByUserId(updateParam);

            // Then
            List<AddressBook> addressBooks = addressBookMapper.list(
                    AddressBook.builder().userId(1L).build()
            );
            for (AddressBook address : addressBooks) {
                assertEquals(0, address.getIsDefault(), "所有地址应被设置为非默认");
            }
        }

        @Test
        @DisplayName("用户不存在时不报错")
        @Tag("edge-case")
        void shouldNotThrowExceptionWhenUpdatingNonExistentUserId() {
            // Given
            AddressBook updateParam = AddressBook.builder()
                    .userId(999999L)
                    .isDefault(1)
                    .build();

            // When & Then
            assertDoesNotThrow(() -> {
                addressBookMapper.updateIsDefaultByUserId(updateParam);
            }, "修改不存在的用户ID时不应抛出异常");
        }
    }

    // ==================== deleteById 方法测试 ====================

    @Nested
    @DisplayName("deleteById 方法测试")
    @Tag("delete")
    class DeleteByIdMethodTests {

        @Test
        @DisplayName("根据ID删除地址 - 成功场景")
        void shouldDeleteAddressWhenIdExists() {
            // Given
            AddressBook addressBook = insertTestAddressBook(1L);
            Long insertedId = addressBook.getId();

            // When
            addressBookMapper.deleteById(insertedId);

            // Then
            AddressBook deleted = addressBookMapper.getById(insertedId);
            assertNull(deleted, "删除后应查询不到数据");
        }

        @ParameterizedTest
        @ValueSource(longs = {999999L, -1L, 0L})
        @DisplayName("删除不存在的ID时不报错")
        @Tag("edge-case")
        void shouldNotThrowExceptionWhenDeletingNonExistentId(Long nonExistentId) {
            // When & Then
            assertDoesNotThrow(() -> {
                addressBookMapper.deleteById(nonExistentId);
            }, "删除不存在的ID时不应抛出异常");
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
            // 1. Create - 新增地址
            AddressBook addressBook = insertTestAddressBook(1L);
            assertAll("新增验证",
                    () -> assertNotNull(addressBook.getId(), "新增后ID应自动生成")
            );

            // 2. Read - 查询地址
            AddressBook found = addressBookMapper.getById(addressBook.getId());
            assertAll("查询验证",
                    () -> assertNotNull(found, "应能查询到新增的地址"),
                    () -> assertEquals("测试收货人", found.getConsignee(), "收货人应匹配")
            );

            // 3. Update - 修改地址
            AddressBook updateData = AddressBook.builder()
                    .id(addressBook.getId())
                    .consignee("完整流程测试-修改")
                    .build();
            addressBookMapper.update(updateData);
            AddressBook updated = addressBookMapper.getById(addressBook.getId());
            assertEquals("完整流程测试-修改", updated.getConsignee(), "修改应生效");

            // 4. Delete - 删除地址
            addressBookMapper.deleteById(addressBook.getId());
            AddressBook deleted = addressBookMapper.getById(addressBook.getId());
            assertNull(deleted, "删除后应查询不到数据");
        }

        @Test
        @DisplayName("设置默认地址流程测试")
        void shouldSetDefaultAddressCorrectly() {
            // Given - 为用户插入2个地址
            Long userId = 9999L;

            AddressBook address1 = createTestAddressBook(userId);
            address1.setConsignee("地址1");
            address1.setIsDefault(1);
            addressBookMapper.insert(address1);

            AddressBook address2 = createTestAddressBook(userId);
            address2.setConsignee("地址2");
            address2.setIsDefault(0);
            addressBookMapper.insert(address2);

            // When - 将所有地址设置为非默认
            addressBookMapper.updateIsDefaultByUserId(
                    AddressBook.builder()
                            .userId(userId)
                            .isDefault(0)
                            .build()
            );

            // Then - 验证所有地址都是非默认
            List<AddressBook> addressBooks = addressBookMapper.list(
                    AddressBook.builder().userId(userId).build()
            );
            assertAll("默认地址验证",
                    () -> assertEquals(2, addressBooks.size(), "应有2个地址"),
                    () -> {
                        for (AddressBook address : addressBooks) {
                            assertEquals(0, address.getIsDefault(), "所有地址应是非默认");
                        }
                    }
            );
        }
    }
}
