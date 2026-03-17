package com.airprofly.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 密码修改 DTO
 * 用于员工修改密码
 */
@Data
public class PasswordEditDTO implements Serializable {

    private static final long serialVersionUID = 100000000236462610L;

    /**
     * 员工ID
     */
    private Long empId;

    /**
     * 旧密码
     */
    private String oldPassword;

    /**
     * 新密码
     */
    private String newPassword;

}
