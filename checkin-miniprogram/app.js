const { API_BASE_URL } = require('./utils/apiConfig');

App({
  globalData: {
    userInfo: null,
    token: null,
    apiBaseUrl: API_BASE_URL
  },
  onLaunch: function() {
    // 检查本地存储的token
    const token = wx.getStorageSync('token');
    if (token) {
      this.globalData.token = token;
    }
  },
  login: function(code, callback) {
    wx.request({
      url: `${this.globalData.apiBaseUrl}/api/auth/login`,
      method: 'POST',
      data: { code: code },
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
    return !!this.globalData.token;
  },
  logout: function() {
    this.globalData.token = null;
    this.globalData.userInfo = null;
    wx.removeStorageSync('token');
    wx.removeStorageSync('userInfo');
  }
})