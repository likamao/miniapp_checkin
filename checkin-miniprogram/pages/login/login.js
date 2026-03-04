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

  // 点击登录按钮
  login: function() {
    wx.showLoading({ title: '登录中...' });

    wx.login({
      success: (codeRes) => {
        if (codeRes.code) {
          this.handleLogin(codeRes.code);
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

  // 处理登录
  handleLogin: function(code) {
    const app = getApp();

    // 直接调用登录接口
    app.login(code, null, (result) => {
      wx.hideLoading();

      if (result.error) {
        wx.showToast({ title: result.error, icon: 'none' });
        return;
      }

      // 保存用户信息
      if (result.user) {
        this.setData({ userInfo: result.user });
      }

      // 根据 profileSetupCompleted 判断是否弹出昵称框
      if (result.user && result.user.profileSetupCompleted) {
        // 资料已完善，直接进入主页
        wx.switchTab({ url: '/pages/checkin/checkin' });
      } else {
        // 需要完善资料，弹出昵称框
        this.setData({
          pendingLoginCode: code,
          showProfileModal: true
        });
      }
    });
  },

  // 弹框关闭事件
  onProfileModalClose: function(e) {
    const { nickname, isModified } = e.detail;

    // 如果用户没有修改昵称，使用默认昵称
    const finalNickname = isModified ? nickname : this.data.defaultNickname;
    this.updateNicknameAndLogin(finalNickname);
  },

  // 弹框确认事件
  onProfileModalConfirm: function(e) {
    const { nickname } = e.detail;
    this.updateNicknameAndLogin(nickname);
  },

  // 更新昵称并完成登录
  updateNicknameAndLogin: function(nickname) {
    this.setData({ showProfileModal: false });

    wx.showLoading({ title: '保存中...' });

    const app = getApp();
    const token = app.globalData.token;

    // 调用更新用户资料接口
    wx.request({
      url: `${app.globalData.apiBaseUrl}/api/users/me/profile`,
      method: 'PUT',
      header: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      data: { nickname: nickname },
      success: (res) => {
        wx.hideLoading();

        if (res.data && res.data.user) {
          // 更新本地用户信息
          app.globalData.userInfo = res.data.user;
          wx.setStorageSync('userInfo', res.data.user);

          // 进入主页
          wx.switchTab({ url: '/pages/checkin/checkin' });
        } else {
          wx.showToast({ title: '保存失败', icon: 'none' });
        }
      },
      fail: () => {
        wx.hideLoading();
        wx.showToast({ title: '网络错误', icon: 'none' });
      }
    });
  }
});
