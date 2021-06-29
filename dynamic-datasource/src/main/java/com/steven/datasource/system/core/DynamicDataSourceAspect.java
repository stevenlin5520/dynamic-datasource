package com.steven.datasource.system.core;

import com.steven.datasource.system.common.annotation.DataSource;
import com.steven.datasource.system.common.enums.DataSourceType;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @desc 多数据源切面配置类，用于获取注解上的注解，进行动态切换数据源
 * @author steven
 * @date 2021/6/25 14:09
 */
@Aspect
@Component
@Order(-1) // 保证该AOP在@Transactional之前执行
public class DynamicDataSourceAspect {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    // @annotation匹配方法上的注解，@within匹配类上的注解
    @Pointcut("@annotation(com.steven.datasource.system.common.annotation.DataSource)" + "|| @within(com.steven.datasource.system.common.annotation.DataSource)")
    public void dsPointCut()  {
    }

    @Around("dsPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        Signature signature = point.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method agentMethod = methodSignature.getMethod();
        Method targetMethod = point.getTarget().getClass().getMethod(agentMethod.getName(), agentMethod.getParameterTypes());
        //获取要切换的数据源
        DataSource dataSource = targetMethod.getAnnotation(DataSource.class);
        if (dataSource != null)  {
            DynamicDataSourceContextHolder.setDataSourceType(dataSource.value().name());
        }else{
            // 获取类上的注解
            dataSource = point.getTarget().getClass().getAnnotation(DataSource.class);
            if (dataSource == null) {
                DynamicDataSourceContextHolder.setDataSourceType(DataSourceType.DB1.name());
            } else {
                DynamicDataSourceContextHolder.setDataSourceType(dataSource.value().name());
            }
        }
        logger.info("切换数据源:{}",DynamicDataSourceContextHolder.getDataSourceType());
        try {
            //TODO 通过创建对象的形式才能保证正常；直接return point.proceed()则会导致该方法执行两次
            final Object proceed = point.proceed();
            return proceed;
        }
        finally  {
            // 销毁数据源 在执行方法之后
            DynamicDataSourceContextHolder.removeDataSourceType();
        }
    }

}
