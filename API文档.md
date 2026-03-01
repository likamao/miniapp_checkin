# 打卡系统API文档

## 1. 认证接口

### 1.1 微信登录
- **接口地址**: `/api/auth/login`
- **请求方法**: POST
- **请求参数**:
  | 参数名 | 类型 | 必选 | 描述 |
  | --- | --- | --- | --- |
  | code | string | 是 | 微信登录code |
- **响应格式**:
  ```json
  {
    "token": "JWT token",
    "user": {
      "id": 1,
      "openid": "xxx",
      "nickname": "xxx",
      "avatarUrl": "xxx"
    }
  }
  ```

### 1.2 更新用户信息
- **接口地址**: `/api/auth/updateInfo`
- **请求方法**: POST
- **请求参数**:
  | 参数名 | 类型 | 必选 | 描述 |
  | --- | --- | --- | --- |
  | userId | string | 是 | 用户ID |
  | nickname | string | 是 | 用户昵称 |
  | avatarUrl | string | 是 | 头像URL |
- **响应格式**:
  ```json
  {
    "user": {
      "id": 1,
      "openid": "xxx",
      "nickname": "xxx",
      "avatarUrl": "xxx"
    }
  }
  ```

## 2. 打卡接口

### 2.1 创建打卡记录
- **接口地址**: `/api/checkin/create`
- **请求方法**: POST
- **请求头**:
  | 头部名 | 值 |
  | --- | --- |
  | Authorization | Bearer {token} |
- **请求参数**:
  | 参数名 | 类型 | 必选 | 描述 |
  | --- | --- | --- | --- |
  | title | string | 是 | 打卡标题（50字以内） |
  | content | string | 是 | 打卡内容（500字以内） |
- **响应格式**:
  ```json
  {
    "record": {
      "id": 1,
      "title": "xxx",
      "content": "xxx",
      "checkinTime": "2026-02-28 16:00:00"
    }
  }
  ```

### 2.2 获取打卡记录列表
- **接口地址**: `/api/checkin/records`
- **请求方法**: GET
- **请求头**:
  | 头部名 | 值 |
  | --- | --- |
  | Authorization | Bearer {token} |
- **响应格式**:
  ```json
  {
    "records": [
      {
        "id": 1,
        "title": "xxx",
        "content": "xxx",
        "checkinTime": "2026-02-28 16:00:00"
      }
    ]
  }
  ```

### 2.3 获取打卡统计数据
- **接口地址**: `/api/checkin/statistics`
- **请求方法**: GET
- **请求头**:
  | 头部名 | 值 |
  | --- | --- |
  | Authorization | Bearer {token} |
- **请求参数**:
  | 参数名 | 类型 | 必选 | 描述 |
  | --- | --- | --- | --- |
  | year | int | 是 | 年份 |
  | month | int | 是 | 月份 |
- **响应格式**:
  ```json
  {
    "statistics": [
      {
        "week": 1,
        "checkinCount": 5
      },
      {
        "week": 2,
        "checkinCount": 7
      }
    ]
  }
  ```

## 3. 错误响应格式

当接口调用失败时，返回以下格式：

```json
{
  "error": "错误信息"
}
```

## 4. 认证错误

当token无效或过期时，返回401状态码和以下错误信息：

```json
{
  "error": "Unauthorized"
}
```