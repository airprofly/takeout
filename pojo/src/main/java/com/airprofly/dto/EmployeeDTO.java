package com.airprofly.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 员工 DTO
 * 用于员工的新增和修改
 */
@Data
public class EmployeeDTO implements Serializable {

    private static final long serialVersionUID = 100000000132795691L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 姓名
     */
    private String name;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 性别
     */
    private String sex;

    /**
     * 身份证号
     */
    private String idNumber;

}
