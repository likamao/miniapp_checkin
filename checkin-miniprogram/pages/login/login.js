Page({
  data: {
    userInfo: null,
    showProfileModal: false,
    defaultNickname: '微信用户',
    pendingLoginCode: null
  },
  onLoad: function() {
    const app = getApp();
    if (app.checkLogin()) {
      const storedUserInfo = wx.getStorageSync('userInfo');
      if (storedUserInfo) {
        this.setData({ userInfo: storedUserInfo });
      }
    }
  },
  login: function() {
    wx.showLoading({ title: '登录中...' });
    
    wx.login({
      success: (codeRes) => {
        if (codeRes.code) {
          this.checkUserAndLogin(codeRes.code);
        } else {
          wx.hideLoading();
          wx.showToast({ title: '登录失败', icon: 'none' });
        }
      },
      fail: () => {
        wx.hideLoading();
        wx.showToast({ title: '网络错误', icon: 'none' });
      }
    });
  },
  checkUserAndLogin: function(code) {
    const app = getApp();
    
    wx.request({
      url: `${app.globalData.apiBaseUrl}/api/auth/checkUser`,
      method: 'POST',
      data: { code: code },
      success: (res) => {
        wx.hideLoading();
        
        if (res.data.exists && res.data.profileSetupCompleted) {
          this.completeLoginDirectly(code);
        } else {
          this.setData({
            pendingLoginCode: code,
            showProfileModal: true
          });
        }
      },
      fail: () => {
        wx.hideLoading();
        this.setData({
          pendingLoginCode: code,
          showProfileModal: true
        });
      }
    });
  },
  completeLoginDirectly: function(code) {
    wx.showLoading({ title: '登录中...' });
    
    const app = getApp();
    app.login(code, null, (result) => {
      wx.hideLoading();
      if (result.error) {
        wx.showToast({ title: result.error, icon: 'none' });
      } else {
        if (result.user) {
          this.setData({ userInfo: result.user });
        }
        setTimeout(() => {
          wx.switchTab({ url: '/pages/checkin/checkin' });
        }, 1000);
      }
    });
  },
  onProfileModalClose: function(e) {
    const { nickname, isModified } = e.detail;
    
    // 登录流程中，如果用户没有修改，使用默认昵称完成登录
    const finalNickname = isModified ? nickname : this.data.defaultNickname;
    this.completeLogin(finalNickname);
  },
  onProfileModalConfirm: function(e) {
    const { nickname } = e.detail;
    this.completeLogin(nickname);
  },
  completeLogin: function(nickname) {
    this.setData({ showProfileModal: false });
    
    wx.showLoading({ title: '登录中...' });
    
    const userInfo = {
      nickName: nickname
    };
    
    const app = getApp();
    app.login(this.data.pendingLoginCode, userInfo, (result) => {
      wx.hideLoading();
      if (result.error) {
        wx.showToast({ title: result.error, icon: 'none' });
      } else {
        if (result.user) {
          this.setData({ userInfo: result.user });
        }
        setTimeout(() => {
          wx.switchTab({ url: '/pages/checkin/checkin' });
        }, 1000);
      }
    });
  }
})