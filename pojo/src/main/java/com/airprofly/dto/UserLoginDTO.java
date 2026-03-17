package com.airprofly.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * C端用户登录 DTO
 * 用于微信小程序用户登录
 */
@Data
public class UserLoginDTO implements Serializable {

    private static final long serialVersionUID = 100000001189367132L;
    /**
     * 微信授权码
     */
    private String code;

}
