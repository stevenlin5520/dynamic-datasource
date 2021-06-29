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
        System.out.println("users = " + users);

        List<GradeEntity> gradeEntities = gradeServvice.selectAll();
        System.out.println("gradeEntities = " + gradeEntities);

        final List<GradeEntity> gradeEntities1 = userService.selectAll2();
        System.out.println("gradeEntities1 = " + gradeEntities1);

        List<UserEntity> users2 = userService.selectAll();
        System.out.println("users2 = " + users2);
    }

}
