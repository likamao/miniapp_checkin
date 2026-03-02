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
    
    // 先获取微信用户信息
    wx.getUserProfile({
      desc: '用于完善用户资料',
      success: (profileRes) => {
        const userInfo = profileRes.userInfo;
        this.setData({ userInfo });
        
        // 获取微信登录 code
        wx.login({
          success: (codeRes) => {
            if (codeRes.code) {
              const app = getApp();
              // 传递用户信息给 app.js 的 login 方法
              app.login(codeRes.code, userInfo, (result) => {
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
      },
      fail: (err) => {
        wx.hideLoading();
        // 如果用户拒绝授权，尝试直接登录（不获取用户信息）
        wx.login({
          success: (res) => {
            if (res.code) {
              const app = getApp();
              app.login(res.code, null, (result) => {
                if (result.error) {
                  wx.showToast({ title: result.error, icon: 'none' });
                } else {
                  wx.switchTab({ url: '/pages/checkin/checkin' });
                }
              });
            } else {
              wx.showToast({ title: '登录失败', icon: 'none' });
            }
          },
          fail: (err) => {
            wx.showToast({ title: '网络错误', icon: 'none' });
          }
        });
      }
    });
  }
})