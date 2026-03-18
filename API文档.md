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
      "unionid": "xxx",
      "createdAt": "2026-02-28 16:00:00",
      "updatedAt": "2026-02-28 16:00:00",
      "allowStatsDisplay": true,
      "profileSetupCompleted": true
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
- **响应格式**:
  ```json
  {
    "user": {
      "id": 1,
      "openid": "xxx",
      "nickname": "xxx"
    }
  }
  ```

### 1.3 验证token
- **接口地址**: `/api/auth/verify`
- **请求方法**: GET
- **请求头**:
  | 头部名 | 值 |
  | --- | --- |
  | Authorization | Bearer {token} |
- **响应格式**:
  ```json
  {
    "valid": true,
    "user": {
      "id": 1,
      "openid": "xxx",
      "nickname": "xxx"
    }
  }
  ```

### 1.4 检查用户信息
- **接口地址**: `/api/auth/checkUser`
- **请求方法**: POST
- **请求参数**:
  | 参数名 | 类型 | 必选 | 描述 |
  | --- | --- | --- | --- |
  | code | string | 是 | 微信登录code |
- **响应格式**:
  ```json
  {
    "exists": true,
    "profileSetupCompleted": true,
    "nickname": "xxx"
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
      "checkinTime": "2026-02-28 16:00:00",
      "createdAt": "2026-02-28 16:00:00",
      "updatedAt": "2026-02-28 16:00:00"
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
- **请求参数**:
  | 参数名 | 类型 | 必选 | 描述 |
  | --- | --- | --- | --- |
  | year | int | 否 | 年份 |
  | month | int | 否 | 月份 |
  | page | int | 否 | 页码（默认1） |
  | pageSize | int | 否 | 每页大小（默认10） |
- **响应格式**:
  ```json
  {
    "records": [
      {
        "id": 1,
        "title": "xxx",
        "content": "xxx",
        "checkinTime": "2026-02-28 16:00:00",
        "createdAt": "2026-02-28 16:00:00",
        "updatedAt": "2026-02-28 16:00:00"
      }
    ],
    "pagination": {
      "total": 10,
      "totalPages": 1,
      "currentPage": 1,
      "pageSize": 10
    }
  }
  ```

### 2.3 获取打卡统计数据
- **接口地址**: `/api/checkin/statistics`
- **请求方法**: GET
- **请求头**:
  | 头部名 | 值 |
  | --- | --- |
  | Authorization | Bearer {token} |
- **响应格式**:
  ```json
  {
    "recent7DaysCount": 5
  }
  ```

### 2.4 检查今日打卡状态
- **接口地址**: `/api/checkin/check-today`
- **请求方法**: GET
- **请求头**:
  | 头部名 | 值 |
  | --- | --- |
  | Authorization | Bearer {token} |
- **响应格式**:
  ```json
  {
    "hasCheckedInToday": true
  }
  ```

## 3. 主题接口

### 3.1 获取所有主题列表
- **接口地址**: `/api/checkin/topics`
- **请求方法**: GET
- **请求头**:
  | 头部名 | 值 |
  | --- | --- |
  | Authorization | Bearer {token} (可选) |
- **响应格式**:
  ```json
  {
    "topics": [
      {
        "id": 1,
        "title": "xxx",
        "description": "xxx",
        "startDatetime": "2026-02-28 16:00:00",
        "endDatetime": "2026-03-07 16:00:00",
        "durationDays": 7,
        "status": "active",
        "createdBy": 1,
        "visibility": "public",
        "checkinCount": 10,
        "hasCheckedInToday": false
      }
    ]
  }
  ```

### 3.2 获取有效主题列表
- **接口地址**: `/api/checkin/topics/active`
- **请求方法**: GET
- **请求头**:
  | 头部名 | 值 |
  | --- | --- |
  | Authorization | Bearer {token} |
- **响应格式**:
  ```json
  {
    "topics": [
      {
        "id": 1,
        "title": "xxx",
        "description": "xxx",
        "startDatetime": "2026-02-28 16:00:00",
        "endDatetime": "2026-03-07 16:00:00",
        "durationDays": 7,
        "status": "active",
        "createdBy": 1,
        "visibility": "public",
        "checkinCount": 10,
        "hasCheckedInToday": false
      }
    ]
  }
  ```

### 3.3 获取主题详情
- **接口地址**: `/api/checkin/topics/{topicId}`
- **请求方法**: GET
- **请求头**:
  | 头部名 | 值 |
  | --- | --- |
  | Authorization | Bearer {token} (可选) |
- **响应格式**:
  ```json
  {
    "topic": {
      "id": 1,
      "title": "xxx",
      "description": "xxx",
      "startDatetime": "2026-02-28 16:00:00",
      "endDatetime": "2026-03-07 16:00:00",
      "durationDays": 7,
      "status": "active",
      "createdBy": 1,
      "visibility": "public",
      "checkinCount": 10,
      "hasCheckedInToday": false,
      "remainingTime": "2 days"
    }
  }
  ```

### 3.4 创建主题
- **接口地址**: `/api/checkin/topics`
- **请求方法**: POST
- **请求头**:
  | 头部名 | 值 |
  | --- | --- |
  | Authorization | Bearer {token} |
- **请求参数**:
  | 参数名 | 类型 | 必选 | 描述 |
  | --- | --- | --- | --- |
  | title | string | 是 | 主题标题（100字以内） |
  | description | string | 否 | 主题描述 |
  | durationDays | int | 否 | 持续天数 |
  | isPrivate | boolean | 否 | 是否私有 |
- **响应格式**:
  ```json
  {
    "topic": {
      "id": 1,
      "title": "xxx",
      "description": "xxx",
      "startDatetime": "2026-02-28 16:00:00",
      "endDatetime": "2026-03-07 16:00:00",
      "durationDays": 7,
      "createdBy": 1
    }
  }
  ```

### 3.5 更新主题
- **接口地址**: `/api/checkin/topics/{topicId}`
- **请求方法**: PUT
- **请求头**:
  | 头部名 | 值 |
  | --- | --- |
  | Authorization | Bearer {token} |
- **请求参数**:
  | 参数名 | 类型 | 必选 | 描述 |
  | --- | --- | --- | --- |
  | title | string | 是 | 主题标题（100字以内） |
  | description | string | 否 | 主题描述 |
  | durationDays | int | 否 | 持续天数 |
- **响应格式**:
  ```json
  {
    "topic": {
      "id": 1,
      "title": "xxx",
      "description": "xxx",
      "startDatetime": "2026-02-28 16:00:00",
      "endDatetime": "2026-03-07 16:00:00",
      "durationDays": 7,
      "createdBy": 1,
      "visibility": "public"
    }
  }
  ```

### 3.6 主题打卡
- **接口地址**: `/api/checkin/topics/{topicId}/checkin`
- **请求方法**: POST
- **请求头**:
  | 头部名 | 值 |
  | --- | --- |
  | Authorization | Bearer {token} |
- **请求参数**:
  | 参数名 | 类型 | 必选 | 描述 |
  | --- | --- | --- | --- |
  | title | string | 是 | 打卡标题 |
  | content | string | 是 | 打卡内容 |
- **响应格式**:
  ```json
  {
    "record": {
      "id": 1,
      "topicId": 1,
      "checkinDate": "2026-02-28",
      "checkinDatetime": "2026-02-28 16:00:00",
      "consecutiveDays": 3,
      "checkinCount": 10
    }
  }
  ```

### 3.7 检查主题今日打卡状态
- **接口地址**: `/api/checkin/check-topic-today/{topicId}`
- **请求方法**: GET
- **请求头**:
  | 头部名 | 值 |
  | --- | --- |
  | Authorization | Bearer {token} |
- **响应格式**:
  ```json
  {
    "hasCheckedInToday": true
  }
  ```

### 3.8 获取主题打卡记录
- **接口地址**: `/api/checkin/topics/{topicId}/checkin-records`
- **请求方法**: GET
- **请求头**:
  | 头部名 | 值 |
  | --- | --- |
  | Authorization | Bearer {token} (可选) |
- **请求参数**:
  | 参数名 | 类型 | 必选 | 描述 |
  | --- | --- | --- | --- |
  | page | int | 否 | 页码（默认1） |
  | pageSize | int | 否 | 每页大小（默认20） |
- **响应格式**:
  ```json
  {
    "records": [
      {
        "id": 1,
        "userId": 1,
        "nickname": "xxx",
        "checkinDate": "2026-02-28",
        "checkinDatetime": "2026-02-28 16:00:00",
        "consecutiveDays": 3
      }
    ],
    "pagination": {
      "total": 10,
      "totalPages": 1,
      "currentPage": 1,
      "pageSize": 20
    }
  }
  ```

### 3.9 获取周报数据
- **接口地址**: `/api/checkin/topics/{topicId}/weekly-report`
- **请求方法**: GET
- **请求头**:
  | 头部名 | 值 |
  | --- | --- |
  | Authorization | Bearer {token} |
- **响应格式**:
  ```json
  {
    "report": {
      "weeklyData": [...]
    }
  }
  ```

### 3.10 获取月报数据
- **接口地址**: `/api/checkin/topics/{topicId}/monthly-report`
- **请求方法**: GET
- **请求头**:
  | 头部名 | 值 |
  | --- | --- |
  | Authorization | Bearer {token} |
- **响应格式**:
  ```json
  {
    "report": {
      "monthlyData": [...]
    }
  }
  ```

### 3.11 获取报告主题列表
- **接口地址**: `/api/checkin/topics/report-list`
- **请求方法**: GET
- **请求头**:
  | 头部名 | 值 |
  | --- | --- |
  | Authorization | Bearer {token} |
- **响应格式**:
  ```json
  {
    "topics": [
      {
        "id": 1,
        "title": "xxx",
        "createdBy": 1,
        "visibility": "public"
      }
    ]
  }
  ```

## 4. 错误响应格式

当接口调用失败时，返回以下格式：

```json
{
  "error": "错误信息"
}
```

## 5. 认证错误

当token无效或过期时，返回401状态码和以下错误信息：

```json
{
  "error": "Unauthorized"
}
```