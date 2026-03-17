package com.airprofly.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * C端用户登录 VO
 * 返回用户登录后的信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginVO implements Serializable {

    private static final long serialVersionUID = 100000001743440108L;
    /**
     * 用户ID
     */
    private Long id;

    /**
     * 微信OpenID
     */
    private String openid;

    /**
     * JWT令牌
     */
    private String token;

}
