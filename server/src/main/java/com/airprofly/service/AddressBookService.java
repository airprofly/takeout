package com.airprofly.service;

import com.airprofly.entity.AddressBook;

import java.util.List;

/**
 * 地址簿业务接口
 */
public interface AddressBookService {

    /**
     * 新增地址
     *
     * @param addressBook 地址簿对象
     */
    void save(AddressBook addressBook);

    /**
     * 根据ID查询地址
     *
     * @param id 地址ID
     * @return 地址簿对象
     */
    AddressBook getById(Long id);

    /**
     * 查询当前登录用户的所有地址
     *
     * @return 地址列表
     */
    List<AddressBook> list();

    /**
     * 根据ID修改地址
     *
     * @param addressBook 地址簿对象
     */
    void update(AddressBook addressBook);

    /**
     * 设置默认地址
     *
     * @param addressBook 地址簿对象
     */
    void setDefault(AddressBook addressBook);

    /**
     * 根据ID删除地址
     *
     * @param id 地址ID
     */
    void deleteById(Long id);
}
