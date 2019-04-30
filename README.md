# gzdzss-security (认证服务)


## 1.What's this？
 - 如何30秒构建一个认证中心？ 
 - 通过一个注解,简单适配,即可启动一个认证中心
 - 通过一个注解,简单适配,即可启动一个资源中心
 


## 2.架构图
![image](https://raw.githubusercontent.com/gzdzss/gzdzss-security/master/gzdzss-security.jpg)

名词说明：

- accessToken: 一串没有意义的字符串，作为用户的登录凭证
- jwtToken:  (JSON WEB TOKEN) 其中保存了用户的基本信息以及权限

流程说明：
  -  1、客户端调用认证中心登录
  -  2、认证中心，验证用户
  -  3、验证无误后，生成accessToken，以及对应的jwtToken存储到redis
  -  4、返回accessToken
  -  5、用户拿到accessToken后，将accessToken放到请求头，调用资源服务
  -  6、资源服务根据accessToken去redis换取jwtToken,解析为用户凭证
  -  7、认证无误，返回资源
  
  
## 3.如何集成

## 3.1  authserver(认证中心 )
## [demo坐标：authserver-example](https://github.com/gzdzss/gzdzss-security/tree/master/gzdzss-security-examples/authserver-example)

- 1引入pom配置
```xml
       <dependency>
            <groupId>com.gzdzss</groupId>
            <artifactId>gzdzss-security-spring-boot-starter</artifactId>
            <version>${project.version}</version>
        </dependency>
```
-  2添加注解 @EnableGzdzssAuthServer
```java
@EnableGzdzssAuthServer
@SpringBootApplication
public class AuthServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServerApplication.class, args);
    }
}
```
- 3实现服务适配(用户，加密模式)
```java
@Component
public class BaseServiceImpl implements GzdzssAuthBaseService {

    private static final Long USER_ID = 1L;
    private static final String USER = "user";
    private static final String U_PASSWORD = "123456";
    private static final String AUTHORITY = "USER";


    @Override
    public GzdzssUserDetailsService gzdzssUserDetailsService() {
        return (String username) -> {
            if (USER.equals(username)) {
                Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
                authorities.add(new SimpleGrantedAuthority(AUTHORITY));
                GzdzssUserDetails gzdzssUserDetails = new GzdzssUserDetails(USER_ID, USER, passwordEncoder().encode(U_PASSWORD), true, authorities);
                return gzdzssUserDetails;
            }
            throw new UsernameNotFoundException("用户不存在");
        };
    }


    @Override
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
```
- 4.配置application
```yaml
spring:
  redis:
    database: 0
    host: 127.0.0.1
    port: 6379
    password: gzdzssredispassword


gzdzss:
  security:
    ## jwt 签名
    signing-key: gzdzssSigning
    ## 不需要鉴权的uri
    ignore-uris: /aa,/bb
    ## 默认过期时间
    auth-expires-in-seconds: 43200

```
 
## 3.2 resourceserver(资源服务)

## [demo坐标：resourceserver-example](https://github.com/gzdzss/gzdzss-security/tree/master/gzdzss-security-examples/resourceserver-example) 
- 1引入pom配置
```xml
       <dependency>
            <groupId>com.gzdzss</groupId>
            <artifactId>gzdzss-security-spring-boot-starter</artifactId>
            <version>${project.version}</version>
        </dependency>
```
-  2添加注解 @EnableGzdzssResourceServer
```java
@EnableGzdzssResourceServer
@SpringBootApplication
public class ResourceServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResourceServerApplication.class, args);
    }
}
```
  
- 3 配置application 

```yaml

spring:
  redis:
    database: 0
    host: 127.0.0.1
    port: 6379
    password: gzdzssredispassword 

  security:
    ## jwt 签名 需要与 authserver保持一致
    signing-key: gzdzssSigning
    ## 不需要鉴权的uri
    ignore-uris: /aa,/bb
```  










  