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
