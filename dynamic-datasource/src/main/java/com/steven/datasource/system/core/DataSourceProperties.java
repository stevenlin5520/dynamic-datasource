package com.steven.datasource.system.core;

import com.alibaba.druid.pool.DruidDataSource;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @desc 数据源配置文件属性定义
 * @author steven
 * @date 2021/6/25 11:48
 */
@Setter
@Configuration
@ConfigurationProperties(prefix = "spring.datasource.druid")
public class DataSourceProperties {

    private int initialSize;

    private int minIdle;

    private int maxActive;

    private int maxWait;

    public DruidDataSource setDataSource(DruidDataSource datasource) {
        datasource.setInitialSize(initialSize);
        /** 配置初始化大小、最小、最大 */
        datasource.setInitialSize(initialSize);
        datasource.setMaxActive(maxActive);
        datasource.setMinIdle(minIdle);
        /** 配置获取连接等待超时的时间 */
        datasource.setMaxWait(maxWait);
        return datasource;
    }

}
