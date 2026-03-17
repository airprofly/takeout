package com.airprofly.service.impl;

import com.airprofly.dto.UserLoginDTO;
import com.airprofly.entity.User;
import com.airprofly.mapper.UserMapper;
import com.airprofly.service.UserService;
import com.airprofly.vo.UserLoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户业务实现
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    /**
     * C端用户登录
     */
    @Override
    public UserLoginVO login(UserLoginDTO userLoginDTO) {
        // TODO: 调用微信接口获取OpenID
        String openid = "mock_openid_" + System.currentTimeMillis();

        // 查询用户是否存在
        User user = userMapper.getByOpenid(openid);

        if (user == null) {
            // 新用户，自动注册
            user = new User();
            user.setOpenid(openid);
            user.setCreateTime(LocalDateTime.now());
            userMapper.insert(user);
        }

        UserLoginVO userLoginVO = new UserLoginVO();
        BeanUtils.copyProperties(user, userLoginVO);

        // TODO: 生成JWT令牌
        userLoginVO.setToken("mock_token_" + user.getId());

        return userLoginVO;
    }

    /**
     * 根据ID查询用户
     */
    @Override
    public User getById(Long userId) {
        return userMapper.getById(userId);
    }

    /**
     * 微信登录
     */
    @Override
    public UserLoginVO wxLogin(String code) {
        UserLoginDTO userLoginDTO = new UserLoginDTO();
        userLoginDTO.setCode(code);
        return login(userLoginDTO);
    }
}
