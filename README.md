# SpringBoot data jdbc专题
# 一.入门例子
1.引入jar包:</br>
```
 <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-jdbc</artifactId>
 </dependency>
```
2.测试代码:</br>
```
 public void testConn() throws SQLException {
        System.out.println(dataSource.getClass());
        Connection connection = dataSource.getConnection();
        System.out.println(connection);
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("select * from sys_user");
        // 展开结果集数据库
        while (rs.next()) {
            // 通过字段检索
            int id = rs.getInt("id");
            String email = rs.getString("email");
            String nickname = rs.getString("nickname");

            // 输出数据
            System.out.println("id: " + id + ",email:" + email + ",nickname" + nickname);
        }
        // 完成后关闭
        rs.close();
        stmt.close();
        connection.close();
    }
```
3.配置文件:</br>
```
spring:
  datasource:
    username: root
    password: Windows8
    url: jdbc:mysql://47.94.192.199:3306/skydb
    driver-class-name: com.mysql.cj.jdbc.Driver
```
4.结论:
```
// 默认使用的数据源是HikariDataSource
HikariDataSource
// yml中的属性都是再下面类中
DataSourceProperties
```
# 二.自动配置原理探究
1.查看spring-boot-autoconfigure中jdbc包中的类:</br>
```
org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
此类spring.datasource可以查看数据源可以配置的参数






org.springframework.boot.autoconfigure.jdbc.DataSourceConfiguration
根据不同环境条件,为环境创建并添加数据源;也可以在配置文件中通过spring.datasource.type=***来指定一种数据源类型,默认可以
支持的有org.apache.commons.dbcp2.BasicDataSource,com.zaxxer.hikari.HikariDataSource和org.apache.tomcat.jdbc.pool.DataSource
可以自定义数据源类型:
@Configuration
@ConditionalOnMissingBean(DataSource.class)
@ConditionalOnProperty(name = "spring.datasource.type")
static class Generic {
    @Bean
    public DataSource dataSource(DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }
}
这里是通过数据源属性的类DataSourceProperties中的两个方法创建的数据源,其中的type是实现了DataSource的实现类
public DataSourceBuilder<?> initializeDataSourceBuilder() {
    return DataSourceBuilder.create(getClassLoader()).type(getType())
            .driverClassName(determineDriverClassName()).url(determineUrl())
            .username(determineUsername()).password(determinePassword());
}
public T build() {
    Class<? extends DataSource> type = getType();
    DataSource result = BeanUtils.instantiateClass(type);
    maybeGetDriverClassName();
    bind(result);
    return (T) result;
}






org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
可以执行一些项目启动的脚本DataSourceInitializer类,比如DML和DDL数据库脚本
public void initSchema() {
    List<Resource> scripts = getScripts("spring.datasource.data",
            this.properties.getData(), "data");
    if (!scripts.isEmpty()) {
        if (!isEnabled()) {
            logger.debug("Initialization disabled (not running data scripts)");
            return;
        }
        String username = this.properties.getDataUsername();
        String password = this.properties.getDataPassword();
        runScripts(scripts, username, password);
    }
}
private List<Resource> getScripts(String propertyName, List<String> resources,
			String fallback) {
    if (resources != null) {
        return getResources(propertyName, resources, true);
    }
    String platform = this.properties.getPlatform();
    List<String> fallbackResources = new ArrayList<>();
    fallbackResources.add("classpath*:" + fallback + "-" + platform + ".sql");
    fallbackResources.add("classpath*:" + fallback + ".sql");
    return getResources(propertyName, fallbackResources, false);
}
schema-*.sql,data-*.sql
默认规则:schema.sql,schema-all.sql;
可以在配置文件做如下配置:
schema:
      - classpath:user.sql
      - classpath:role.sql
spring:
  datasource:
    username: root
    password: Windows8
    url: jdbc:mysql://47.94.192.199:3306/skydb
    driver-class-name: com.mysql.cj.jdbc.Driver
    schema:
      - classpath:user.sql
      - classpath:role.sql





org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration
容器启动,给容器中注入了NamedParameterJdbcTemplate和jdbcTemplate

```
# 三.使用druid数据源
druid数据源是阿里提供的数据源,性能虽然比HikariDataSource差了点,但监控安全这些做的完善,开发中会选择这个.

1.引入jar包:</br>
```
 <dependency>
     <groupId>com.alibaba</groupId>
     <artifactId>druid</artifactId>
     <version>1.1.19</version>
 </dependency>
```
2.修改配置指定为使用的数据源:</br>

```
spring:
  datasource:
    username: root
    password: Windows8
    url: jdbc:mysql://47.94.192.199:3306/skydb?autoReconnect=true&useUnicode=true&characterEncoding=utf-8&allowMultiQueries=true&zeroDateTimeBehavior=convertToNull&useSSL=false
    driver-class-name: com.mysql.cj.jdbc.Driver
    ###################以下为druid增加的配置###########################
    type: com.alibaba.druid.pool.DruidDataSource
    # 初始化连接池个数
    initialSize: 5
    # 最小连接池个数——》已经不再使用，配置了也没效果
    minIdle: 2
    # 最大连接池个数
    maxActive: 20
    # 配置获取连接等待超时的时间，单位毫秒，缺省启用公平锁，并发效率会有所下降
    maxWait: 60000
    # 配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒
    timeBetweenEvictionRunsMillis: 60000
    # 配置一个连接在池中最小生存的时间，单位是毫秒
    minEvictableIdleTimeMillis: 300000
    # 用来检测连接是否有效的sql，要求是一个查询语句。
    # 如果validationQuery为null，testOnBorrow、testOnReturn、testWhileIdle都不会起作用
    validationQuery: SELECT 1 FROM DUAL
    # 建议配置为true，不影响性能，并且保证安全性。
    # 申请连接的时候检测，如果空闲时间大于timeBetweenEvictionRunsMillis，执行validationQuery检测连接是否有效。
    testWhileIdle: true
    # 申请连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能
    testOnBorrow: false
    # 归还连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能
    testOnReturn: false
    # 打开PSCache，并且指定每个连接上PSCache的大小
    poolPreparedStatements: true
    maxPoolPreparedStatementPerConnectionSize: 20
    # 通过别名的方式配置扩展插件，多个英文逗号分隔，常用的插件有：
    # 监控统计用的filter:stat
    # 日志用的filter:log4j
    # 防御sql注入的filter:wall
    filters: stat,wall,log4j
    # 通过connectProperties属性来打开mergeSql功能；慢SQL记录
    connectionProperties: druid.stat.mergeSql=true;druid.stat.slowSqlMillis=5000
    # 合并多个DruidDataSource的监控数据
    useGlobalDataSourceStat: true

#    schema:
#      - classpath:user.sql
#      - classpath:role.sql

```
3.虽然增加了如下配置,但initialSize这些属性,在DataSourceProperties里是用不到的,没有对应的属性,创建自己的数据源

```
@ConfigurationProperties(prefix = "spring.datasource")
@Bean
public DataSource druid(){
    return new DruidDataSource();
}
```

4.druid后台监控和过滤器配置
```

    /**
     *  主要实现WEB监控的配置处理
     */
    /**
     * 配置管理后台的Servlet
     * @return
     */
    @Bean
    public ServletRegistrationBean statViewServlet() {
        // 现在要进行druid监控的配置处理操作
        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(
                new StatViewServlet(), "/druid/*");
        // 白名单,多个用逗号分割， 如果allow没有配置或者为空，则允许所有访问
        servletRegistrationBean.addInitParameter("allow", "127.0.0.1,172.29.32.54");
        // 黑名单,多个用逗号分割 (共同存在时，deny优先于allow)
        servletRegistrationBean.addInitParameter("deny", "192.168.1.110");
        // 控制台管理用户名
        servletRegistrationBean.addInitParameter("loginUsername", "admin");
        // 控制台管理密码
        servletRegistrationBean.addInitParameter("loginPassword", "admin");
        // 是否可以重置数据源，禁用HTML页面上的“Reset All”功能
        servletRegistrationBean.addInitParameter("resetEnable", "false");
        return servletRegistrationBean ;
    }


    /**
     * 配置监控的filter
     * @return
     */
    @Bean
    public FilterRegistrationBean webStatFilter() {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean() ;
        filterRegistrationBean.setFilter(new WebStatFilter());
        //所有请求进行监控处理
        filterRegistrationBean.addUrlPatterns("/*");
        //添加不需要忽略的格式信息
        filterRegistrationBean.addInitParameter("exclusions", "*.js,*.gif,*.jpg,*.css,/druid/*");
        return filterRegistrationBean ;
    }

```
5.访问druid监控后台
```
http://localhost:8080/druid/api.html
```