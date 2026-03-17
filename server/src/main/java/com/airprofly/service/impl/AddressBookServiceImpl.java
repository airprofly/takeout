package com.airprofly.service.impl;

import com.airprofly.entity.AddressBook;
import com.airprofly.mapper.AddressBookMapper;
import com.airprofly.service.AddressBookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 地址簿业务实现
 */
@Service
@RequiredArgsConstructor
public class AddressBookServiceImpl implements AddressBookService {

    private final AddressBookMapper addressBookMapper;

    /**
     * 新增地址
     */
    @Override
    public void save(AddressBook addressBook) {
        // TODO: 从当前上下文获取用户ID
        Long userId = 1L; // 临时写死
        addressBook.setUserId(userId);

        // 如果设置为默认地址，先将其他地址设为非默认
        if (addressBook.getIsDefault() == 1) {
            AddressBook defaultAddress = AddressBook.builder()
                    .userId(userId)
                    .isDefault(0)
                    .build();
            addressBookMapper.updateIsDefaultByUserId(defaultAddress);
        }

        addressBookMapper.insert(addressBook);
    }

    /**
     * 根据ID查询地址
     */
    @Override
    public AddressBook getById(Long id) {
        return addressBookMapper.getById(id);
    }

    /**
     * 查询当前登录用户的所有地址
     */
    @Override
    public List<AddressBook> list() {
        // TODO: 从当前上下文获取用户ID
        Long userId = 1L; // 临时写死

        AddressBook addressBook = AddressBook.builder()
                .userId(userId)
                .build();

        return addressBookMapper.list(addressBook);
    }

    /**
     * 根据ID修改地址
     */
    @Override
    public void update(AddressBook addressBook) {
        addressBookMapper.update(addressBook);
    }

    /**
     * 设置默认地址
     */
    @Override
    public void setDefault(AddressBook addressBook) {
        // TODO: 从当前上下文获取用户ID
        Long userId = 1L; // 临时写死

        // 先将该用户的所有地址设为非默认
        AddressBook defaultAddress = AddressBook.builder()
                .userId(userId)
                .isDefault(0)
                .build();
        addressBookMapper.updateIsDefaultByUserId(defaultAddress);

        // 再将当前地址设为默认
        addressBook.setIsDefault(1);
        addressBookMapper.update(addressBook);
    }

    /**
     * 根据ID删除地址
     */
    @Override
    public void deleteById(Long id) {
        addressBookMapper.deleteById(id);
    }
}
