package com.steven.datasource.service;

import com.steven.datasource.entity.GradeEntity;
import com.steven.datasource.entity.UserEntity;

import java.util.List;

/**
 * @author steven
 * @desc
 * @date 2021/6/24 16:31
 */
public interface UserService {

    List<UserEntity> selectAll();
    List<GradeEntity> selectGrade1();
    List<GradeEntity> selectGrade2();

}
