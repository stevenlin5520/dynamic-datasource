package com.steven.datasource.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steven.datasource.entity.GradeEntity;
import com.steven.datasource.mapper.db2.GradeMapper;
import com.steven.datasource.service.GradeServvice;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author steven
 * @desc
 * @date 2021/6/24 16:34
 */
@Service
public class GradeServiceImpl extends ServiceImpl<GradeMapper, GradeEntity> implements GradeServvice {

    @Resource
    private GradeMapper gradeMapper;

    @Override
    public List<GradeEntity> selectAll() {
        return gradeMapper.selectAll();
    }
}
