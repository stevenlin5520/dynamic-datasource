# SpringBoot实现多数据源，动态数据源自由切换

## 业务场景
在开发中，可能涉及到在用户的业务中要去查询对应订单的数据，而用户和订单又是分处于不同的数据库的，这样的业务该怎么处理呢？
这种就是多数据源的场景，随着业务量的增大，其实这种情况还是经常能遇到的，比如多个数据库分属于不同的服务器，同一个服务器的不同数据库等。

## 实现原理
正常情况下，我们操作数据是通过配置一个DataSource数据源来连接数据库，然后绑定给SqlSessionFactory，然后通过Dao或Mapper指定SqlSessionFactory来操作数据库的。
![Alt](https://img-blog.csdnimg.cn/20210713193335293.jpg#pic_center)

而操作多数据源则更要复杂一点，可以通过如下两种方式来实现：

#### 方法一：普通的多数据源
多个DataSource数据源绑定多个SqlSessionFactory，每个数据源绑定一个SqlSessionFactory，然后通过Dao或Mapper指定SqlSessionFactory来操作数据库。
操作不同的数据源是通过在业务层调用对应的实现了不同数据源的方法来同时操作不同的数据源的。

![Alt](https://img-blog.csdnimg.cn/20210713193420150.jpg#pic_center)

#### 方法二：动态切换的数据源
以上方式，必须要使多个数据源之间完全的物理分离，如果存在一个用户表，几个数据库都有的情况，并且业务也类似，那写多套代码是冗余的，并且代码维护起来也更加困难，有没有更便捷的方式呢？

其实可以通过配置多个DataSource数据源到一个DynamicDataSource动态数据源上，动态数据源绑定一个SqlSessionFactory，除了中间多出一个动态数据源外，其他部分都是相同的。

![Alt](https://img-blog.csdnimg.cn/20210713193448432.jpg#pic_center)

那么这种方式是怎么实现数据源的切换的呢？

通过在业务类或方法上添加一个数据源标识，使用切面来监听这个标志，进而切换数据源，通过一个注解就可以更加灵活切换数据源。

## 数据库相关SQL脚本
项目所用的数据库脚本放在代码仓库下主目录的sql文件夹下，文件名对应数据源

![Alt](https://img-blog.csdnimg.cn/20210713193552626.jpg#pic_center)

## 普通的多数据源的实现

项目结构如下：

![Alt](https://img-blog.csdnimg.cn/202107131936244.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzMzNzk5MzY2,size_16,color_FFFFFF,t_70#pic_left)

其中/mapper下的db1放的是数据源1的表，/mapper下的db2放的是数据源2的表。
config文件夹下配置的是数据源的配置，DataSourceConfig1是数据源1的配置，DataSourceConfig2自然是数据源2的配置。
    
DataSourceConfig1代码如下：
```java
@Configuration
@MapperScan(basePackages = "com.steven.datasource.mapper.db1", sqlSessionFactoryRef = "db1SqlSessionFactory")
public class DataSourceConfig1 {

    @Primary // 表示这个数据源是默认数据源, 这个注解必须要加，因为不加的话spring将分不清楚那个为主数据源（默认数据源）
    @Bean("db1DataSource")
    @ConfigurationProperties(prefix = "spring.datasource.dynamic.datasource.db1") //读取application.yml中的配置参数映射成为一个对象
    public DataSource getDb1DataSource(){
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean("db1SqlSessionFactory")
    public SqlSessionFactory db1SqlSessionFactory(@Qualifier("db1DataSource") DataSource dataSource) throws Exception {
        MybatisSqlSessionFactoryBean bean = new MybatisSqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        // mapper的xml形式文件位置必须要配置，不然将报错：no statement （这种错误也可能是mapper的xml中，namespace与项目的路径不一致导致）
        bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath*:mapping/db1/*.xml"));
        return bean.getObject();
    }

    @Primary
    @Bean("db1SqlSessionTemplate")
    public SqlSessionTemplate db1SqlSessionTemplate(@Qualifier("db1SqlSessionFactory") SqlSessionFactory sqlSessionFactory){
        return new SqlSessionTemplate(sqlSessionFactory);
    }

}
```

DataSourceConfig2代码如下：
```java
@Configuration
@MapperScan(basePackages = "com.steven.datasource.mapper.db2", sqlSessionFactoryRef = "db2SqlSessionFactory")
public class DataSourceConfig2 {

    @Bean("db2DataSource")
    @ConfigurationProperties(prefix = "spring.datasource.dynamic.datasource.db2")
    public DataSource getDb1DataSource(){
        return DataSourceBuilder.create().build();
    }

    @Bean("db2SqlSessionFactory")
    public SqlSessionFactory db1SqlSessionFactory(@Qualifier("db2DataSource") DataSource dataSource) throws Exception {
        MybatisSqlSessionFactoryBean bean = new MybatisSqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath*:mapping/db2/*.xml"));
        return bean.getObject();
    }

    @Bean("db2SqlSessionTemplate")
    public SqlSessionTemplate db1SqlSessionTemplate(@Qualifier("db2SqlSessionFactory") SqlSessionFactory sqlSessionFactory){
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
```
可以看到，数据源db1DataSource，绑定了一个db1SqlSessionFactory，指定扫描db1下的mapper文件；数据源db2DataSource，绑定了一个db2SqlSessionFactory，指定扫描db2下的mapper文件。每个数据源分别进行管理、操作。

其他部分代码也是一样的，这里不作为重点讲述，给个代码地址参考：[multiple-datasource](https://github.com/stevenlin5520/dynamic-datasource/tree/master/multiple-datasource)，想了解的自行理解。

## 动态切换的数据源的实现（重点）

### 项目结构如下：

![Alt](https://img-blog.csdnimg.cn/20210713194256191.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzMzNzk5MzY2,size_16,color_FFFFFF,t_70#pic_left)

### 编写数据源配置核心类

1、创建数据源枚举定义DataSourceType.java
```java
public enum DataSourceType {

    /**
     * 主库
     */
    DB1,

    DB2

}
```
2、创建自定义的数据源注解DataSource.java，可以用于业务层的的类和方法上
```java
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
```
3、配置文件application.yml添加数据源配置
```yaml
spring:
  datasource:
    dynamic:
      primary: db1 # 配置默认数据库
      datasource:
        db1: # 数据源1配置
          url: jdbc:mysql://localhost:3306/steven?characterEncoding=utf8
          username: root
          password: root
          driver-class-name: com.mysql.cj.jdbc.Driver
        db2: # 数据源2配置
          url: jdbc:mysql://localhost:3306/steven2?characterEncoding=utf8
          username: root
          password: root
          driver-class-name: com.mysql.cj.jdbc.Driver

    druid:
      initial-size: 1
      max-active: 20
      min-idle: 1
      max-wait: 60000
```
4、数据源配置文件属性定义DataSourceProperties.java
```java
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
```
5、数据源的切换的配置
```java
/**
 * @desc 数据源切换进程
 * @author steven
 * @date 2021/6/25 11:35
 */
public class DynamicDataSourceContextHolder {

    public static final Logger log = LoggerFactory.getLogger(DynamicDataSourceContextHolder.class);

    /**
     *此类提供线程局部变量。这些变量不同于它们的正常对应关系是每个线程访问一个线程(通过get、set方法),有自己的独立初始化变量的副本。
     */
    private static final ThreadLocal<String> contextHolder = new ThreadLocal<>();

    /**
     * 设置当前线程的数据源变量
     */
    public static void setDataSourceType(String dataSourceType) {
        log.info("已切换到{}数据源", dataSourceType);
        contextHolder.set(dataSourceType);
    }

    /**
     * 获取当前线程的数据源变量
     */
    public static String getDataSourceType() {
        return contextHolder.get();
    }

    /**
     * 删除与当前线程绑定的数据源变量
     */
    public static void removeDataSourceType() {
        contextHolder.remove();
    }

}
```
6、定义动态数据源DynamicDataSource.java，继承AbstractRoutingDataSource类实现determineCurrentLookupKey方法，用于切换数据库
```java
/**
 * @desc 获取数据源（依赖于 spring）  定义一个类继承AbstractRoutingDataSource实现determineCurrentLookupKey方法，该方法可以实现数据库的动态切换
 * @author steven
 * @date 2021/6/25 11:37
 */
public class DynamicDataSource extends AbstractRoutingDataSource {

    public static  DynamicDataSource build() {
        return new DynamicDataSource();
    }

    /**
     * 获取与数据源相关的key
     * 此key是Map<String,DataSource> resolvedDataSources 中与数据源绑定的key值
     * 在通过determineTargetDataSource获取目标数据源时使用
     */
    @Override
    protected Object determineCurrentLookupKey() {
        return DynamicDataSourceContextHolder.getDataSourceType();
    }
}
```
7、多数据源置配置
```java
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
```
此处配置了两个数据源，dataSource1和dataSource2，并且将这些数据源都挂在动态数据源DynamicDataSource上，绑定一个SqlSessionFactory。

8、编写数据源切面，获取自定义注解，进而动态切换数据源
```java
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
```
**此处需要注意的一点**
通过切点的proceed方法返回对象，必须要定义一个对象来接收，然后再返回，直接返回point.proceed()会导致此环绕方法执行两次，第二次获取不到自定义注解，因此走的仍然是默认数据源，无法达到切换数据源的目的。

### 编写业务类
1、数据源中对应表结构和数据如下：

![Alt](https://img-blog.csdnimg.cn/20210713193835875.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzMzNzk5MzY2,size_16,color_FFFFFF,t_70#pic_left)

![Alt](https://img-blog.csdnimg.cn/20210713193855262.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzMzNzk5MzY2,size_16,color_FFFFFF,t_70#pic_right)

数据源1中包含grade表和user表；数据源2中包含grade表；两个数据源中的grade表结构是完全一样的，此处需求也是一样，都是查询全部的数据。

2、UserServiceImpl代码如下：
```java
/**
 * @author steven
 * @desc
 * @date 2021/6/24 16:32
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private GradeMapper gradeMapper;

    @Override
    public List<UserEntity> selectAll() {
        List<UserEntity> users = userMapper.selectAll();
        return users;
    }


    @DataSource(DataSourceType.DB1)
    @Override
    public List<GradeEntity> selectGrade1() {
        final List<GradeEntity> gradeEntities = gradeMapper.selectAll();
        return gradeEntities;
    }

    @DataSource(DataSourceType.DB2)
    @Override
    public List<GradeEntity> selectGrade2() {
        final List<GradeEntity> gradeEntities = gradeMapper.selectAll();
        return gradeEntities;
    }
}
```
未加自定义注解，使用默认的数据源，即db1；selectGrade1切换数据源db1，查询db1的全部成绩数据；selectGrade2切换数据源db2，查询db2的全部成绩数据。

3、GradeServiceImpl代码如下：
```java
/**
 * @author steven
 * @desc 
 * @date 2021/6/24 16:34
 */
@DataSource(DataSourceType.DB2)
@Service
public class GradeServiceImpl extends ServiceImpl<GradeMapper, GradeEntity> implements GradeServvice {

    @Resource
    private GradeMapper gradeMapper;

    @Override
    public List<GradeEntity> selectAll() {
        return gradeMapper.selectAll();
    }
}
```
GradeServiceImpl代码如下在类上使用自定义注解@DataSource(DataSourceType.DB2)，切换数据源2，整个GradeServiceImpl都会使用db2的数据源

4、业务代码TestServiceImpl如下：
```java
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
```
首先在UserService中查用户数据，使用默认数据源db1，获取db1的全部用户数据；然后在GradeServvice中调用数据源db2，查询全部的db2的成绩数据，该类下的所有方法都是调用的db2的数据源；
然后在UserService调用selectGrade1方法，切换数据源db1，获取db1的全部成绩数据；最后在UserService调用selectGrade2方法，切换数据源db2，获取db2的全部成绩数据。

5、创建测试的控制器TestController
```java
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
```

6、其他代码就不详细赘述，可以参考demo中的代码

### 数据源测试
启动项目，调用测试服务 http://127.0.0.1:8888/test，可以看到控制台中的打印结果如下，与需求结果一致，动态数据源算是成功了。

![Alt](https://img-blog.csdnimg.cn/2021071319395323.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzMzNzk5MzY2,size_16,color_FFFFFF,t_70#pic_center)

### demo地址：**[dynamic-datasource](https://github.com/stevenlin5520/dynamic-datasource/tree/master/dynamic-datasource)**

## 项目参考博客
[Spring-Boot 多数据源配置+动态数据源切换+多数据源事物配置实现主从数据库存储分离](https://www.cnblogs.com/fuzongle/p/13335304.html)

[深入理解spring多数据源配置](https://www.jb51.net/article/102282.htm)