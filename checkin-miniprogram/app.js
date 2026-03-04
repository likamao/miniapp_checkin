const { API_BASE_URL } = require('./utils/apiConfig');

App({
  globalData: {
    userInfo: null,
    token: null,
    apiBaseUrl: API_BASE_URL
  },
  onLaunch: function() {
    const token = wx.getStorageSync('token');
    if (token) {
      this.globalData.token = token;
      this.verifyLoginStatus();
    }
  },
  verifyLoginStatus: function() {
    const token = wx.getStorageSync('token');
    if (!token) {
      return false;
    }

    const that = this;
    wx.request({
      url: `${this.globalData.apiBaseUrl}/api/auth/verify`,
      method: 'GET',
      header: {
        'Authorization': `Bearer ${token}`
      },
      success: (res) => {
        if (res.data.valid) {
          that.globalData.token = token;
          that.globalData.userInfo = res.data.user;
          wx.setStorageSync('userInfo', res.data.user);
          wx.switchTab({ url: '/pages/checkin/checkin' });
        } else {
          that.clearLoginState();
        }
      },
      fail: () => {
        that.clearLoginState();
      }
    });
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
    return !!this.globalData.token;
  },
  logout: function() {
    this.clearLoginState();
  },
  clearLoginState: function() {
    this.globalData.token = null;
    this.globalData.userInfo = null;
    wx.removeStorageSync('token');
    wx.removeStorageSync('userInfo');
  }
})