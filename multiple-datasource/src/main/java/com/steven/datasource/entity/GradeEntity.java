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
@TableName("grade")
public class GradeEntity implements Serializable {

    @TableId
    private int id;
    private int score;
}
