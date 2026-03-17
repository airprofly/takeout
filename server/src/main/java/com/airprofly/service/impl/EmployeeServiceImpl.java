package com.airprofly.service.impl;

import com.airprofly.dto.EmployeeDTO;
import com.airprofly.dto.EmployeeLoginDTO;
import com.airprofly.dto.EmployeePageQueryDTO;
import com.airprofly.entity.Employee;
import com.airprofly.mapper.EmployeeMapper;
import com.airprofly.result.PageResult;
import com.airprofly.service.EmployeeService;
import com.airprofly.util.PageHelperUtil;
import com.airprofly.vo.EmployeeLoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 员工业务实现
 */
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeMapper employeeMapper;

    /**
     * 员工登录
     */
    @Override
    public EmployeeLoginVO login(EmployeeLoginDTO employeeLoginDTO) {
        // 根据用户名查询员工
        Employee employee = employeeMapper.getByUsername(employeeLoginDTO.getUsername());

        // TODO: 验证密码 - 需要加密处理

        EmployeeLoginVO employeeLoginVO = new EmployeeLoginVO();
        BeanUtils.copyProperties(employee, employeeLoginVO);

        return employeeLoginVO;
    }

    /**
     * 新增员工
     */
    @Override
    public void save(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);

        // 设置默认密码
        employee.setPassword("123456");

        // 设置状态
        employee.setStatus(1);

        employeeMapper.insert(employee);
    }

    /**
     * 分页查询员工
     */
    @Override
    public PageResult<Employee> pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        PageHelperUtil.startPage(employeePageQueryDTO);
        List<Employee> list = employeeMapper.pageQuery(employeePageQueryDTO.getName());
        return PageHelperUtil.of(list);
    }

    /**
     * 根据ID查询员工
     */
    @Override
    public Employee getById(Long id) {
        return employeeMapper.getById(id);
    }

    /**
     * 编辑员工信息
     */
    @Override
    public void update(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);
        employeeMapper.update(employee);
    }

    /**
     * 启用或禁用员工账号
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        Employee employee = Employee.builder()
                .id(id)
                .status(status)
                .build();
        employeeMapper.update(employee);
    }

    /**
     * 修改密码
     */
    @Override
    public void editPassword(String oldPassword, String newPassword) {
        // TODO: 实现密码修改逻辑 - 需要从当前上下文获取员工ID
    }
}
