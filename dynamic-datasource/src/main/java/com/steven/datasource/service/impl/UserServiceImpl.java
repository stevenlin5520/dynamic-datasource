package com.steven.datasource.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steven.datasource.system.common.annotation.DataSource;
import com.steven.datasource.system.common.enums.DataSourceType;
import com.steven.datasource.entity.GradeEntity;
import com.steven.datasource.entity.UserEntity;
import com.steven.datasource.mapper.GradeMapper;
import com.steven.datasource.mapper.UserMapper;
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
    @Resource
    private GradeMapper gradeMapper;

    @Override
    public List<UserEntity> selectAll() {
        List<UserEntity> users = userMapper.selectAll();
        return users;
    }


    @DataSource(DataSourceType.DB1)
    @Override
    public List<GradeEntity> selectAll2() {
        final List<GradeEntity> gradeEntities = gradeMapper.selectAll();
        return gradeEntities;
    }
}
