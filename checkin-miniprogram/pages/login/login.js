Page({
  data: {
    userInfo: null
  },
  onLoad: function() {
    // 检查是否已登录
    const app = getApp();
    if (app.checkLogin()) {
      wx.switchTab({ url: '/pages/checkin/checkin' });
    }
  },
  login: function() {
    wx.showLoading({ title: '登录中...' });
    // 获取微信登录code
    wx.login({
      success: (res) => {
        if (res.code) {
          const app = getApp();
          app.login(res.code, (result) => {
            wx.hideLoading();
            if (result.error) {
              wx.showToast({ title: result.error, icon: 'none' });
            } else {
              wx.switchTab({ url: '/pages/checkin/checkin' });
            }
          });
        } else {
          wx.hideLoading();
          wx.showToast({ title: '登录失败', icon: 'none' });
        }
      },
      fail: (err) => {
        wx.hideLoading();
        wx.showToast({ title: '网络错误', icon: 'none' });
      }
    });
  }
})