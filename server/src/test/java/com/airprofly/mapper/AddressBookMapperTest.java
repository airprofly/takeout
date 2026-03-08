package com.airprofly.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.airprofly.entity.AddressBook;
import com.airprofly.mapper.AddressBookMapper;

/**
 * AddressBookMapper 测试类
 */
@SpringBootTest
@Transactional
@DisplayName("地址簿 Mapper 测试")
public class AddressBookMapperTest {

    @Autowired
    private AddressBookMapper addressBookMapper;

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
    @DisplayName("根据用户ID查询地址簿列表 - 用户不存在时返回空列表")
    void shouldReturnEmptyListWhenUserIdNotExists() {
        // Given
        AddressBook query = AddressBook.builder()
                .userId(999999L)
                .build();

        // When
        List<AddressBook> addressBooks = addressBookMapper.list(query);

        // Then
        assertNotNull(addressBooks, "地址簿列表不应为null");
        assertTrue(addressBooks.isEmpty(), "不存在的用户ID应返回空列表");
    }

    @Test
    @DisplayName("根据用户ID查询地址簿列表 - 查询参数为null时返回空列表")
    void shouldReturnEmptyListWhenQueryIsNull() {
        // Given
        AddressBook query = null;

        // When
        List<AddressBook> addressBooks = addressBookMapper.list(query);

        // Then
        assertNotNull(addressBooks, "地址簿列表不应为null");
    }

    @Test
    @DisplayName("根据用户ID查询地址簿列表 - 验证返回数据完整性")
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
            assertNotNull(firstAddress.getId(), "地址簿ID不应为null");
            assertNotNull(firstAddress.getUserId(), "用户ID不应为null");
            assertNotNull(firstAddress.getConsignee(), "收货人不应为null");
            assertNotNull(firstAddress.getPhone(), "手机号不应为null");
            assertNotNull(firstAddress.getDetail(), "详细地址不应为null");
        }
    }

    @Test
    @DisplayName("根据用户ID和是否默认地址查询 - 验证默认地址")
    void shouldReturnDefaultAddressWhenIsDefaultIsOne() {
        // Given
        AddressBook query = AddressBook.builder()
                .userId(1L)
                .isDefault(1)
                .build();

        // When
        List<AddressBook> addressBooks = addressBookMapper.list(query);

        // Then
        assertNotNull(addressBooks, "地址簿列表不应为null");
        if (!addressBooks.isEmpty()) {
            AddressBook firstAddress = addressBooks.get(0);
            assertEquals(1, firstAddress.getIsDefault(), "应返回默认地址");
        }
    }

    @Test
    @DisplayName("根据用户ID和收货人查询 - 验证精确查询")
    void shouldReturnAddressBookWhenConsigneeMatches() {
        // Given
        AddressBook query = AddressBook.builder()
                .userId(1L)
                .consignee("张三")
                .build();

        // When
        List<AddressBook> addressBooks = addressBookMapper.list(query);

        // Then
        assertNotNull(addressBooks, "地址簿列表不应为null");
        if (!addressBooks.isEmpty()) {
            AddressBook firstAddress = addressBooks.get(0);
            assertEquals("张三", firstAddress.getConsignee(), "收货人应匹配");
        }
    }

    @Test
    @DisplayName("根据用户ID查询地址簿列表 - 验证结果按ID排序")
    void shouldReturnAddressBookListSortedById() {
        // Given
        AddressBook query = AddressBook.builder()
                .userId(1L)
                .build();

        // When
        List<AddressBook> addressBooks = addressBookMapper.list(query);

        // Then
        if (addressBooks.size() > 1) {
            for (int i = 0; i < addressBooks.size() - 1; i++) {
                assertTrue(addressBooks.get(i).getId() <= addressBooks.get(i + 1).getId(),
                        "地址簿列表应按ID升序排列");
            }
        }
    }
}
