#认证中心



##1.登录
POST:localhost:8888/auth/login?username=user&password=123456

返回示例：
```json

{
    "access_token": "e642de4d5c8f27ddda92a54748089c6f",
    "token_type": "bearer",
    "expires_in": 43200
}

```
   
  
 ### 2 测试
 
 
### 2.1 直接访问
- GET: localhost:8888/auth/test

返回：

```json
{
    "error_description": "Full authentication is required to access this resource"
}
```


### 2.2携带accessToken访问
- Authorization: Bearer e642de4d5c8f27ddda92a54748089c6f
- GET: localhost:8888/auth/test

返回：
ok


### 2.3  需要USER权限
- Authorization: Bearer e642de4d5c8f27ddda92a54748089c6f
- GET: localhost:8888/auth/user

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
    "accountNonExpired": true,
    "credentialsNonExpired": true,
    "accountNonLocked": true
}
```


### 2.4 需要ADMIN权限

- Authorization: Bearer e642de4d5c8f27ddda92a54748089c6f
- GET: localhost:8888/auth/admin

```json

{
    "error_description": "不允许访问"
}
```


  
  