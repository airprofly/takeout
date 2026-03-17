package com.airprofly.service;

import com.airprofly.dto.UserLoginDTO;
import com.airprofly.entity.User;
import com.airprofly.vo.UserLoginVO;

/**
 * 用户业务接口
 */
public interface UserService {

    /**
     * C端用户登录
     *
     * @param userLoginDTO 登录DTO
     * @return 登录VO
     */
    UserLoginVO login(UserLoginDTO userLoginDTO);

    /**
     * 根据ID查询用户
     *
     * @param userId 用户ID
     * @return 用户对象
     */
    User getById(Long userId);

    /**
     * 微信登录
     *
     * @param code 微信授权码
     * @return 登录VO
     */
    UserLoginVO wxLogin(String code);
}
