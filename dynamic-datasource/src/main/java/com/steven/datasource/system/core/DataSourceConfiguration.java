package com.steven.datasource.system.core;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.steven.datasource.system.common.enums.DataSourceType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * @desc 多数据源配置
 * @author steven
 * @date 2021/6/25 11:41
 */
@Configuration
@MapperScan("com.steven.datasource.mapper")
public class DataSourceConfiguration {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 主库
     */
    @Bean(name = "dataSource1")
    @ConfigurationProperties("spring.datasource.dynamic.datasource.db1")
    public DataSource dataSource1(DataSourceProperties dataSourceProperties) {
        logger.info("初始化数据源:{}",DataSourceType.DB1.name());
        return dataSourceProperties.setDataSource(DruidDataSourceBuilder.create().build());
    }


    /**
     * 从库
     * havingValue 是否开启数据源开关---若不开启 默认适用默认数据源
     */
    @Bean(name = "dataSource2")
    //TODO 去除下一行代码才能切换数据源
    //@ConditionalOnProperty( prefix = "spring.datasource.dynamic.datasource.db2", name = "enable", havingValue = "true")
    @ConfigurationProperties("spring.datasource.dynamic.datasource.db2")
    public DataSource dataSource2(DataSourceProperties dataSourceProperties) {
        logger.info("初始化数据源:{}",DataSourceType.DB2.name());
        return dataSourceProperties.setDataSource(DruidDataSourceBuilder.create().build());
    }

    /**
     * 设置数据源
     */
    @Bean(name = "dynamicDataSource")
    public DynamicDataSource dynamicDataSource(DataSource dataSource1, DataSource dataSource2) {
        Map<Object, Object> targetDataSources = new HashMap<>(16);
        DynamicDataSource dynamicDataSource = DynamicDataSource.build();
        targetDataSources.put(DataSourceType.DB1.name(), dataSource1);
        targetDataSources.put(DataSourceType.DB2.name(), dataSource2);
        //默认数据源配置 DefaultTargetDataSource
        dynamicDataSource.setDefaultTargetDataSource(dataSource1);
        //额外数据源配置 TargetDataSources
        dynamicDataSource.setTargetDataSources(targetDataSources);
        dynamicDataSource.afterPropertiesSet();
        logger.info("设置动态数据源，默认数据源：{}，所有数据源{}",DataSourceType.DB1.name(),targetDataSources.toString());
        return dynamicDataSource;
    }


    @Bean("sqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(@Qualifier("dynamicDataSource") DynamicDataSource dynamicDataSource) throws Exception {
        MybatisSqlSessionFactoryBean bean = new MybatisSqlSessionFactoryBean();
        bean.setDataSource(dynamicDataSource);
        // mapper的xml形式文件位置必须要配置，不然将报错：no statement （这种错误也可能是mapper的xml中，namespace与项目的路径不一致导致）
        bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath*:mapping/*.xml"));
        return bean.getObject();
    }

    @Primary
    @Bean("sqlSessionTemplate")
    public SqlSessionTemplate sqlSessionTemplate(@Qualifier("sqlSessionFactory") SqlSessionFactory sqlSessionFactory){
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
