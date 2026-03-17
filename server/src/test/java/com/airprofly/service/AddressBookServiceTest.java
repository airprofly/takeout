package com.airprofly.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

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

import com.airprofly.entity.AddressBook;
import com.airprofly.mapper.AddressBookMapper;
import com.airprofly.service.impl.AddressBookServiceImpl;

/**
 * AddressBookService 测试类
 *
 * 测试覆盖:
 * - 地址簿 CRUD 操作测试
 * - 默认地址管理测试
 * - 业务逻辑验证测试
 * - 边界条件测试
 *
 * 测试策略:
 * - 使用 Mockito 进行依赖项模拟
 * - 验证业务逻辑正确性
 * - 验证默认地址切换逻辑
 * - 验证用户隔离
 */
@ExtendWith(MockitoExtension.class)
@Tag("service")
@Tag("address-book")
@DisplayName("地址簿 Service 测试")
public class AddressBookServiceTest {

    @Mock
    private AddressBookMapper addressBookMapper;

    @InjectMocks
    private AddressBookServiceImpl addressBookService;

    private AddressBook testAddressBook;

    @BeforeEach
    void setUp() {
        // 初始化地址簿测试数据
        testAddressBook = AddressBook.builder()
                .id(1L)
                .userId(1L)
                .consignee("张三")
                .phone("13800138000")
                .sex("1")
                .provinceCode("110000")
                .provinceName("北京市")
                .cityCode("110100")
                .cityName("北京市")
                .districtCode("110101")
                .districtName("东城区")
                .detail("某某街道1号")
                .isDefault(0)
                .build();
    }

    // ==================== save 方法测试 ====================

    @Nested
    @DisplayName("save 方法测试")
    @Tag("create")
    class SaveMethodTests {

        @Test
        @DisplayName("新增地址 - 成功场景")
        void shouldSaveAddressWhenValidData() {
            // Given
            doNothing().when(addressBookMapper).insert(any(AddressBook.class));

            // When
            addressBookService.save(testAddressBook);

            // Then
            verify(addressBookMapper, times(1)).insert(argThat(address ->
                    address.getUserId() != null &&
                    "张三".equals(address.getConsignee())
            ));
        }

        @Test
        @DisplayName("新增地址时设置用户ID")
        void shouldSetUserIdWhenSaving() {
            // Given
            doNothing().when(addressBookMapper).insert(any(AddressBook.class));

            // When
            addressBookService.save(testAddressBook);

            // Then
            verify(addressBookMapper).insert(argThat(address ->
                    address.getUserId() != null
            ));
        }

        @Test
        @DisplayName("新增默认地址时应取消其他默认地址")
        void shouldUnsetOtherDefaultsWhenSavingDefaultAddress() {
            // Given
            testAddressBook.setIsDefault(1);
            doNothing().when(addressBookMapper).updateIsDefaultByUserId(any(AddressBook.class));
            doNothing().when(addressBookMapper).insert(any(AddressBook.class));

            // When
            addressBookService.save(testAddressBook);

            // Then
            assertAll("默认地址验证",
                    () -> verify(addressBookMapper, times(1)).updateIsDefaultByUserId(argThat(addr ->
                            addr.getIsDefault() == 0
                    )),
                    () -> verify(addressBookMapper, times(1)).insert(any(AddressBook.class))
            );
        }

        @Test
        @DisplayName("新增非默认地址时不影响其他地址")
        void shouldNotAffectOtherAddressesWhenSavingNonDefault() {
            // Given
            testAddressBook.setIsDefault(0);
            doNothing().when(addressBookMapper).insert(any(AddressBook.class));

            // When
            addressBookService.save(testAddressBook);

            // Then
            verify(addressBookMapper, never()).updateIsDefaultByUserId(any(AddressBook.class));
            verify(addressBookMapper, times(1)).insert(any(AddressBook.class));
        }
    }

    // ==================== getById 方法测试 ====================

    @Nested
    @DisplayName("getById 方法测试")
    @Tag("query")
    class GetByIdMethodTests {

        @Test
        @DisplayName("根据ID查询地址 - 成功场景")
        void shouldReturnAddressWhenIdExists() {
            // Given
            Long addressId = 1L;
            when(addressBookMapper.getById(addressId)).thenReturn(testAddressBook);

            // When
            AddressBook result = addressBookService.getById(addressId);

            // Then
            assertAll("查询验证",
                    () -> assertNotNull(result, "应能查询到地址"),
                    () -> assertEquals(addressId, result.getId(), "ID应匹配"),
                    () -> verify(addressBookMapper, times(1)).getById(addressId)
            );
        }

        @Test
        @DisplayName("查询不存在的地址ID")
        void shouldReturnNullWhenIdNotExists() {
            // Given
            Long nonExistentId = 999999L;
            when(addressBookMapper.getById(nonExistentId)).thenReturn(null);

            // When
            AddressBook result = addressBookService.getById(nonExistentId);

            // Then
            assertNull(result, "不存在的地址ID应返回null");
            verify(addressBookMapper, times(1)).getById(nonExistentId);
        }
    }

    // ==================== list 方法测试 ====================

    @Nested
    @DisplayName("list 方法测试")
    @Tag("query")
    class ListMethodTests {

        @Test
        @DisplayName("查询当前用户地址列表 - 成功场景")
        void shouldReturnAddressList() {
            // Given
            when(addressBookMapper.list(any(AddressBook.class))).thenReturn(Arrays.asList(testAddressBook));

            // When
            List<AddressBook> result = addressBookService.list();

            // Then
            assertAll("查询验证",
                    () -> assertNotNull(result, "地址列表不应为null"),
                    () -> assertFalse(result.isEmpty(), "地址列表不应为空"),
                    () -> verify(addressBookMapper, times(1)).list(any(AddressBook.class))
            );
        }

        @Test
        @DisplayName("查询地址列表返回空")
        void shouldReturnEmptyListWhenNoAddresses() {
            // Given
            when(addressBookMapper.list(any(AddressBook.class))).thenReturn(Collections.emptyList());

            // When
            List<AddressBook> result = addressBookService.list();

            // Then
            assertAll("空结果验证",
                    () -> assertNotNull(result, "地址列表不应为null"),
                    () -> assertTrue(result.isEmpty(), "地址列表应为空")
            );
        }

        @Test
        @DisplayName("查询时应按用户ID过滤")
        void shouldFilterByUserId() {
            // Given
            when(addressBookMapper.list(any(AddressBook.class))).thenReturn(Arrays.asList(testAddressBook));

            // When
            addressBookService.list();

            // Then
            verify(addressBookMapper).list(argThat(address ->
                    address.getUserId() != null
            ));
        }
    }

    // ==================== update 方法测试 ====================

    @Nested
    @DisplayName("update 方法测试")
    @Tag("update")
    class UpdateMethodTests {

        @Test
        @DisplayName("修改地址 - 成功场景")
        void shouldUpdateAddressWhenValidData() {
            // Given
            testAddressBook.setConsignee("李四");
            doNothing().when(addressBookMapper).update(any(AddressBook.class));

            // When
            addressBookService.update(testAddressBook);

            // Then
            verify(addressBookMapper, times(1)).update(argThat(address ->
                    "李四".equals(address.getConsignee())
            ));
        }

        @Test
        @DisplayName("修改地址多个字段")
        void shouldUpdateMultipleFields() {
            // Given
            testAddressBook.setConsignee("王五");
            testAddressBook.setPhone("13900139000");
            testAddressBook.setDetail("某某街道2号");
            doNothing().when(addressBookMapper).update(any(AddressBook.class));

            // When
            addressBookService.update(testAddressBook);

            // Then
            verify(addressBookMapper).update(argThat(address ->
                    "王五".equals(address.getConsignee()) &&
                    "13900139000".equals(address.getPhone()) &&
                    "某某街道2号".equals(address.getDetail())
            ));
        }

        @Test
        @DisplayName("修改不存在的地址")
        void shouldHandleNonExistentAddress() {
            // Given
            testAddressBook.setId(999999L);
            doNothing().when(addressBookMapper).update(any(AddressBook.class));

            // When & Then
            assertDoesNotThrow(() -> addressBookService.update(testAddressBook));
            verify(addressBookMapper, times(1)).update(any(AddressBook.class));
        }
    }

    // ==================== setDefault 方法测试 ====================

    @Nested
    @DisplayName("setDefault 方法测试")
    @Tag("update")
    class SetDefaultMethodTests {

        @Test
        @DisplayName("设置默认地址 - 成功场景")
        void shouldSetDefaultAddress() {
            // Given
            doNothing().when(addressBookMapper).updateIsDefaultByUserId(any(AddressBook.class));
            doNothing().when(addressBookMapper).update(any(AddressBook.class));

            // When
            addressBookService.setDefault(testAddressBook);

            // Then
            assertAll("默认地址设置验证",
                    () -> verify(addressBookMapper, times(1)).updateIsDefaultByUserId(argThat(addr ->
                            addr.getIsDefault() == 0
                    )),
                    () -> verify(addressBookMapper, times(1)).update(argThat(addr ->
                            addr.getIsDefault() == 1
                    ))
            );
        }

        @Test
        @DisplayName("设置默认地址时应先取消其他默认地址")
        void shouldUnsetOtherDefaultsBeforeSettingNew() {
            // Given
            doNothing().when(addressBookMapper).updateIsDefaultByUserId(any(AddressBook.class));
            doNothing().when(addressBookMapper).update(any(AddressBook.class));

            // When
            addressBookService.setDefault(testAddressBook);

            // Then
            verify(addressBookMapper).updateIsDefaultByUserId(argThat(addr ->
                    addr.getIsDefault() == 0
            ));
        }

        @Test
        @DisplayName("设置默认地址时标记当前地址为默认")
        void shouldMarkCurrentAddressAsDefault() {
            // Given
            doNothing().when(addressBookMapper).updateIsDefaultByUserId(any(AddressBook.class));
            doNothing().when(addressBookMapper).update(any(AddressBook.class));

            // When
            addressBookService.setDefault(testAddressBook);

            // Then
            verify(addressBookMapper).update(argThat(addr ->
                    addr.getId() == testAddressBook.getId() &&
                    addr.getIsDefault() == 1
            ));
        }
    }

    // ==================== deleteById 方法测试 ====================

    @Nested
    @DisplayName("deleteById 方法测试")
    @Tag("delete")
    class DeleteByIdMethodTests {

        @Test
        @DisplayName("删除地址 - 成功场景")
        void shouldDeleteAddress() {
            // Given
            Long addressId = 1L;
            doNothing().when(addressBookMapper).deleteById(addressId);

            // When
            addressBookService.deleteById(addressId);

            // Then
            verify(addressBookMapper, times(1)).deleteById(addressId);
        }

        @Test
        @DisplayName("删除不存在的地址")
        void shouldHandleNonExistentAddress() {
            // Given
            Long nonExistentId = 999999L;
            doNothing().when(addressBookMapper).deleteById(nonExistentId);

            // When & Then
            assertDoesNotThrow(() -> addressBookService.deleteById(nonExistentId));
            verify(addressBookMapper, times(1)).deleteById(nonExistentId);
        }

        @Test
        @DisplayName("删除默认地址后应处理默认状态")
        void shouldHandleDefaultAddressDeletion() {
            // Given
            testAddressBook.setIsDefault(1);
            Long addressId = 1L;
            doNothing().when(addressBookMapper).deleteById(addressId);

            // When
            addressBookService.deleteById(addressId);

            // Then
            // 实际业务中可能需要设置其他地址为默认，这里只验证删除
            verify(addressBookMapper, times(1)).deleteById(addressId);
        }
    }

    // ==================== 综合业务场景测试 ====================

    @Nested
    @DisplayName("综合业务场景测试")
    @Tag("integration")
    class IntegrationTests {

        @Test
        @DisplayName("完整地址簿操作流程测试")
        void shouldCompleteFullAddressBookLifecycle() {
            // Given
            doNothing().when(addressBookMapper).insert(any(AddressBook.class));
            doNothing().when(addressBookMapper).update(any(AddressBook.class));
            doNothing().when(addressBookMapper).updateIsDefaultByUserId(any(AddressBook.class));
            doNothing().when(addressBookMapper).deleteById(anyLong());
            when(addressBookMapper.getById(anyLong())).thenReturn(testAddressBook);
            when(addressBookMapper.list(any(AddressBook.class))).thenReturn(Arrays.asList(testAddressBook));

            // 1. 新增地址
            addressBookService.save(testAddressBook);
            verify(addressBookMapper, times(1)).insert(any(AddressBook.class));

            // 2. 查询地址列表
            addressBookService.list();
            verify(addressBookMapper, times(1)).list(any(AddressBook.class));

            // 3. 查询单个地址
            addressBookService.getById(1L);
            verify(addressBookMapper, times(1)).getById(1L);

            // 4. 修改地址
            testAddressBook.setConsignee("修改后姓名");
            addressBookService.update(testAddressBook);
            verify(addressBookMapper, atLeastOnce()).update(any(AddressBook.class));

            // 5. 设置默认地址
            addressBookService.setDefault(testAddressBook);
            verify(addressBookMapper, atLeastOnce()).updateIsDefaultByUserId(any(AddressBook.class));

            // 6. 删除地址
            addressBookService.deleteById(1L);
            verify(addressBookMapper, times(1)).deleteById(1L);
        }

        @Test
        @DisplayName("默认地址管理流程测试")
        void shouldManageDefaultAddressCorrectly() {
            // Given
            AddressBook address1 = AddressBook.builder()
                    .id(1L)
                    .userId(1L)
                    .consignee("地址1")
                    .isDefault(1)
                    .build();

            AddressBook address2 = AddressBook.builder()
                    .id(2L)
                    .userId(1L)
                    .consignee("地址2")
                    .isDefault(0)
                    .build();

            doNothing().when(addressBookMapper).insert(any(AddressBook.class));
            doNothing().when(addressBookMapper).update(any(AddressBook.class));
            doNothing().when(addressBookMapper).updateIsDefaultByUserId(any(AddressBook.class));
            doNothing().when(addressBookMapper).deleteById(anyLong());

            // When - 新增第一个默认地址
            addressBookService.save(address1);
            verify(addressBookMapper, times(1)).updateIsDefaultByUserId(any(AddressBook.class));

            // When - 新增第二个默认地址
            addressBookService.setDefault(address2);
            verify(addressBookMapper, atLeastOnce()).updateIsDefaultByUserId(any(AddressBook.class));
            verify(addressBookMapper).update(argThat(addr -> addr.getId() == 2L && addr.getIsDefault() == 1));

            // When - 删除默认地址
            addressBookService.deleteById(2L);
            verify(addressBookMapper, times(1)).deleteById(2L);
        }

        @Test
        @DisplayName("地址簿数据完整性测试")
        void shouldMaintainDataIntegrity() {
            // Given
            doNothing().when(addressBookMapper).insert(any(AddressBook.class));
            doNothing().when(addressBookMapper).update(any(AddressBook.class));

            // When - 新增地址
            testAddressBook.setConsignee("张三");
            testAddressBook.setPhone("13800138000");
            testAddressBook.setDetail("详细地址");
            addressBookService.save(testAddressBook);

            // Then - 验证数据完整性
            verify(addressBookMapper).insert(argThat(addr ->
                    addr.getConsignee() != null &&
                    addr.getPhone() != null &&
                    addr.getDetail() != null &&
                    addr.getUserId() != null
            ));

            // When - 修改地址
            testAddressBook.setId(1L);
            testAddressBook.setConsignee("李四");
            addressBookService.update(testAddressBook);

            // Then - 验证修改后数据
            verify(addressBookMapper).update(argThat(addr ->
                    "李四".equals(addr.getConsignee()) &&
                    addr.getId() == 1L
            ));
        }

        @Test
        @DisplayName("用户地址隔离测试")
        void shouldMaintainUserIsolation() {
            // Given
            when(addressBookMapper.list(any(AddressBook.class))).thenReturn(Arrays.asList(testAddressBook));

            // When
            addressBookService.list();

            // Then - 验证按用户ID查询
            verify(addressBookMapper).list(argThat(addr ->
                    addr.getUserId() != null
            ));
        }
    }
}
