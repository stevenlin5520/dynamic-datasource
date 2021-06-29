package com.steven.datasource.mapper.db1;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steven.datasource.entity.UserEntity;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author steven
 * @desc
 * @date 2021/4/6 19:13
 */
public interface UserMapper extends BaseMapper<UserEntity> {

    @Select("select * from user")
    List<UserEntity> selectAll();

}
