# API配置说明

## 1. API基础地址配置

项目使用统一的API基础地址配置，位于 `utils/apiConfig.js` 文件中：

```javascript
// API基础地址配置
const API_BASE_URL = 'http://localhost:8080';

module.exports = {
  API_BASE_URL
};
```

## 2. 使用方法

### 2.1 导入配置

在需要调用API的文件中，首先导入API基础地址：

```javascript
const { API_BASE_URL } = require('../../utils/apiConfig');
```

### 2.2 构建API请求URL

使用模板字符串构建完整的API请求URL：

```javascript
const url = `${API_BASE_URL}/api/auth/login`;
```

### 2.3 示例代码

```javascript
wx.request({
  url: `${API_BASE_URL}/api/checkin/create`,
  method: 'POST',
  header: {
    'Authorization': 'Bearer ' + token
  },
  data: { title, content },
  success: (res) => {
    // 处理响应
  }
});
```

## 3. 注意事项

1. **环境切换**：在不同环境（开发、测试、生产）中，需要修改 `API_BASE_URL` 的值
2. **路径拼接**：确保API路径以 `/` 开头，避免路径拼接错误
3. **一致性**：所有API请求必须使用此常量，不得硬编码URL
4. **安全性**：生产环境中应使用HTTPS协议

## 4. 维护指南

当需要修改API基础地址时，只需修改 `utils/apiConfig.js` 文件中的 `API_BASE_URL` 常量，所有使用此常量的代码将自动更新。

## 5. 修改记录

### 5.1 2026-02-28
- 创建 `utils/apiConfig.js` 文件，定义 `API_BASE_URL` 常量
- 修改 `app.js`，使用 `API_BASE_URL` 常量
- 修改 `pages/checkin/checkin.js`，使用 `API_BASE_URL` 常量
- 修改 `pages/records/records.js`，使用 `API_BASE_URL` 常量
- 修改 `pages/statistics/statistics.js`，使用 `API_BASE_URL` 常量
- 创建此配置说明文档