package com.steven.datasource.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steven.datasource.entity.UserEntity;
import com.steven.datasource.mapper.db1.UserMapper;
import com.steven.datasource.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author steven
 * @desc
 * @date 2021/6/24 16:32
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {

    @Resource
    private UserMapper userMapper;

    @Override
    public List<UserEntity> selectAll() {
        List<UserEntity> users = userMapper.selectAll();
        return users;
    }
}
