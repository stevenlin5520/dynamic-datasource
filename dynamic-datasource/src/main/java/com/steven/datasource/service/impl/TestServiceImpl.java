package com.steven.datasource.service.impl;

import com.steven.datasource.entity.GradeEntity;
import com.steven.datasource.entity.UserEntity;
import com.steven.datasource.service.GradeServvice;
import com.steven.datasource.service.TestService;
import com.steven.datasource.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author steven
 * @desc
 * @date 2021/6/24 15:56
 */
@Service
public class TestServiceImpl implements TestService {

    @Resource
    private UserService userService;
    @Resource
    private GradeServvice gradeServvice;

    @Override
    public void test(){
        List<UserEntity> users = userService.selectAll();
        System.out.println("db1.users = " + users);

        List<GradeEntity> gradeEntities = gradeServvice.selectAll();
        System.out.println("gradeEntities = " + gradeEntities);

        final List<GradeEntity> gradeEntities1 = userService.selectGrade1();
        System.out.println("db1.grades  = " + gradeEntities1);

        final List<GradeEntity> gradeEntities2 = userService.selectGrade2();
        System.out.println("db2.grades  = " + gradeEntities2);

    }

}
