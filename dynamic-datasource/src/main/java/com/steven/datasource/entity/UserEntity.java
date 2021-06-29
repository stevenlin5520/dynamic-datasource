package com.steven.datasource.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @author steven
 * @desc
 * @date 2021/4/6 19:11
 */
@Data
@TableName("user")
public class UserEntity implements Serializable {

    @TableId
    private int id;
    private String name;
    private int age;
}
