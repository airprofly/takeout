package com.airprofly.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import com.airprofly.entity.AddressBook;

@Mapper
public interface AddressBookMapper {

    /**
     * 根据用户ID查询地址簿列表
     * @param addressBook 包含用户ID的地址簿对象
     * @return 地址簿列表
     */
    List<AddressBook> list(AddressBook addressBook);

}
