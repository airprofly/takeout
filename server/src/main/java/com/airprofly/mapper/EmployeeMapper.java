package com.airprofly.mapper;

import com.airprofly.annotation.AutoFill;
import com.airprofly.entity.Employee;
import com.airprofly.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 员工 Mapper 接口
 */
@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * @param username 用户名
     * @return 员工对象
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    /**
     * 新增员工
     * @param employee 员工对象
     */
    @AutoFill(OperationType.INSERT)
    @Insert("insert into employee (name, username, password, phone, sex, id_number, status, create_time, update_time, create_user, update_user) " +
            "values (#{name}, #{username}, #{password}, #{phone}, #{sex}, #{idNumber}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Employee employee);

    /**
     * 分页查询员工
     * @param name 员工姓名(模糊查询)
     * @return 员工列表
     */
    List<Employee> pageQuery(String name);

    /**
     * 根据ID查询员工
     * @param id 员工ID
     * @return 员工对象
     */
    @Select("select * from employee where id = #{id}")
    Employee getById(Long id);

    /**
     * 更新员工信息
     * @param employee 员工对象
     */
    @AutoFill(OperationType.UPDATE)
    void update(Employee employee);
}
