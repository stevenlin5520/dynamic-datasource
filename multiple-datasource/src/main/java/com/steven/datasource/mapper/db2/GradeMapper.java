package com.steven.datasource.mapper.db2;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steven.datasource.entity.GradeEntity;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author steven
 * @desc
 * @date 2021/4/6 19:14
 */
public interface GradeMapper extends BaseMapper<GradeEntity> {

    @Select("select * from grade")
    List<GradeEntity> selectAll();
}
