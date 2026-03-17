package com.airprofly.service;

import com.airprofly.dto.EmployeeDTO;
import com.airprofly.dto.EmployeePageQueryDTO;
import com.airprofly.dto.EmployeeLoginDTO;
import com.airprofly.entity.Employee;
import com.airprofly.result.PageResult;
import com.airprofly.vo.EmployeeLoginVO;

/**
 * 员工业务接口
 */
public interface EmployeeService {

    /**
     * 员工登录
     *
     * @param employeeLoginDTO 登录DTO
     * @return 登录VO
     */
    EmployeeLoginVO login(EmployeeLoginDTO employeeLoginDTO);

    /**
     * 新增员工
     *
     * @param employeeDTO 员工DTO
     */
    void save(EmployeeDTO employeeDTO);

    /**
     * 分页查询员工
     *
     * @param employeePageQueryDTO 分页查询条件
     * @return 分页结果
     */
    PageResult<Employee> pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 根据ID查询员工
     *
     * @param id 员工ID
     * @return 员工对象
     */
    Employee getById(Long id);

    /**
     * 编辑员工信息
     *
     * @param employeeDTO 员工DTO
     */
    void update(EmployeeDTO employeeDTO);

    /**
     * 启用或禁用员工账号
     *
     * @param status 状态
     * @param id     员工ID
     */
    void startOrStop(Integer status, Long id);

    /**
     * 修改密码
     *
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    void editPassword(String oldPassword, String newPassword);
}
