const { API_BASE_URL } = require('./utils/apiConfig');

App({
  globalData: {
    userInfo: null,
    token: null,
    apiBaseUrl: API_BASE_URL
  },
  onLaunch: function() {
    // 小程序启动时，仅从本地缓存恢复登录状态，不进行验证和跳转
    const token = wx.getStorageSync('token');
    if (token) {
      this.globalData.token = token;
      const userInfo = wx.getStorageSync('userInfo');
      if (userInfo) {
        this.globalData.userInfo = userInfo;
      }
    }
  },
  login: function(code, userInfo, callback) {
    const requestData = { code: code };
    // 如果提供了用户信息，添加到请求数据中
    if (userInfo) {
      requestData.nickname = userInfo.nickName || '';
    }
    
    wx.request({
      url: `${this.globalData.apiBaseUrl}/api/auth/login`,
      method: 'POST',
      data: requestData,
      success: (res) => {
        if (res.data.token) {
          this.globalData.token = res.data.token;
          this.globalData.userInfo = res.data.user;
          wx.setStorageSync('token', res.data.token);
          wx.setStorageSync('userInfo', res.data.user);
          callback(res.data);
        } else {
          callback({ error: '登录失败' });
        }
      },
      fail: (err) => {
        callback({ error: '网络错误' });
      }
    });
  },
  checkLogin: function() {
    // 优先检查 globalData，如果没有则检查 storage
    const token = this.globalData.token || wx.getStorageSync('token');
    return !!token;
  },
  logout: function() {
    this.clearLoginState();
  },
  clearLoginState: function() {
    this.globalData.token = null;
    this.globalData.userInfo = null;
    wx.removeStorageSync('token');
    wx.removeStorageSync('userInfo');
  },
  // 封装的请求方法，自动处理 token 过期
  request: function(options) {
    const app = this;
    
    // 优先从 globalData 获取 token，如果没有则从 storage 获取
    let token = app.globalData.token || wx.getStorageSync('token');
    
    // 如果没有 token，直接跳转登录页
    if (!token) {
      wx.showModal({
        title: '提示',
        content: '登录已过期，请重新登录',
        showCancel: false,
        success: () => {
          app.clearLoginState();
          wx.redirectTo({ url: '/pages/login/login' });
        }
      });
      return Promise.reject({ error: '未登录' });
    }
    
    // 添加 token 到请求头
    const header = options.header || {};
    header['Authorization'] = 'Bearer ' + token;
    options.header = header;
    
    // 发起请求
    const originalSuccess = options.success;
    const originalFail = options.fail;
    
    options.success = function(res) {
      // 检查 token 是否过期（401 状态码）
      if (res.statusCode === 401 || (res.data && res.data.error && (res.data.error.includes('token') || res.data.error.includes('expired')))) {
        wx.showModal({
          title: '提示',
          content: '登录已过期，请重新登录',
          showCancel: false,
          success: () => {
            app.clearLoginState();
            wx.redirectTo({ url: '/pages/login/login' });
          }
        });
        return;
      }
      
      // 调用原始 success 回调
      if (originalSuccess) {
        originalSuccess(res);
      }
    };
    
    options.fail = function(err) {
      // 网络错误
      wx.showToast({
        title: '网络错误',
        icon: 'none'
      });
      
      if (originalFail) {
        originalFail(err);
      }
    };
    
    return wx.request(options);
  },
  
  // 同步获取 token 的方法
  getToken: function() {
    return this.globalData.token || wx.getStorageSync('token');
  }
})
