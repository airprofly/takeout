package com.airprofly.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 员工实体类
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class Employee implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    // 用户名
    private String username;

    // 姓名
    private String name;

    // 密码
    private String password;

    // 手机号
    private String phone;

    // 性别
    private String sex;

    // 身份证号
    private String idNumber;

    // 状态 0:禁用 1:启用
    private Integer status;

    // 创建时间
    private LocalDateTime createTime;

    // 更新时间
    private LocalDateTime updateTime;

    // 创建人ID
    private Long createUser;

    // 更新人ID
    private Long updateUser;
}
