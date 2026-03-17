package com.airprofly.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 员工登录 DTO
 * 用于员工登录时的数据传输
 */
@Data
public class EmployeeLoginDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 100000002576992067L;
    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

}
