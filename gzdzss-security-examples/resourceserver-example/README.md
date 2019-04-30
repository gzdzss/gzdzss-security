#资源服务




## 1.通过认证中心获取  accessToken


## 2. 调用接口测试

### 2.1 直接访问
- GET: localhost:8887/test

返回：

```json
{
    "error_description": "Full authentication is required to access this resource"
}
```


### 2.2携带accessToken访问
- Authorization: Bearer e642de4d5c8f27ddda92a54748089c6f
- GET: localhost:8887/test

返回：
ok


### 2.3  需要USER权限
- Authorization: Bearer e642de4d5c8f27ddda92a54748089c6f
- GET: localhost:8887/user

返回:

```json

{
    "id": 1,
    "avatarUrl": null,
    "nickName": null,
    "password": null,
    "username": "user",
    "enabled": true,
    "authorities": [
        {
            "authority": "USER"
        }
    ],
    "credentialsNonExpired": true,
    "accountNonExpired": true,
    "accountNonLocked": true
}
```


### 2.4 需要ADMIN权限

- Authorization: Bearer e642de4d5c8f27ddda92a54748089c6f
- GET: localhost:8887/admin

```json

{
    "error_description": "不允许访问"
}
```


  
  
  