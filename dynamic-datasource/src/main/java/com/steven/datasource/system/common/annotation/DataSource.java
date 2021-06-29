package com.steven.datasource.system.common.annotation;

import com.steven.datasource.system.common.enums.DataSourceType;

import java.lang.annotation.*;

/**
 * @desc 数据源自定义注解 对类和方法都适用
 * @author steven
 * @date 2021/6/25 9:59
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
@Inherited
public @interface DataSource {

    /**
     * 选择数据源
     * @return
     */
    DataSourceType value() default DataSourceType.DB1;

}
