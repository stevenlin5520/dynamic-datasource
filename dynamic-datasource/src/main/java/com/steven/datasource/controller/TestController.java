package com.steven.datasource.controller;

import com.steven.datasource.service.TestService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author steven
 * @desc
 * @date 2021/6/24 15:54
 */
@RestController
public class TestController {

    @Resource
    private TestService testService;

    @GetMapping("test")
    public String test(){
        testService.test();
        return "SUCCESS";
    }

}
