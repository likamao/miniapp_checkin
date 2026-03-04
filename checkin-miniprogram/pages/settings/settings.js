const apiConfig = require('../../utils/apiConfig');

Page({
  data: {
    user: null,
    allowStatsDisplay: false,
    showProfileModal: false,
    defaultNickname: '微信用户',
    currentNickname: ''
  },

  onLoad() {
    this.loadUserInfo();
  },

  loadUserInfo() {
    const token = wx.getStorageSync('token');
    wx.request({
      url: apiConfig.API_BASE_URL + '/api/users/me',
      method: 'GET',
      header: {
        'Authorization': 'Bearer ' + token
      },
      success: (res) => {
        if (res.data && res.data.user) {
          // 检查用户是否有管理员角色
          const hasAdminRole = res.data.user.roles && res.data.user.roles.includes('ADMIN');
          
          this.setData({
            user: res.data.user,
            allowStatsDisplay: res.data.user.allowStatsDisplay || false,
            hasReportPermission: hasAdminRole,
            currentNickname: res.data.user.nickname || ''
          });
        }
      },
      fail: (err) => {
        console.error('获取用户信息失败:', err);
        this.setData({
          hasReportPermission: false
        });
      }
    });
  },

  onEditProfile() {
    // 重新加载用户信息，确保获取到最新的昵称
    const token = wx.getStorageSync('token');
    wx.request({
      url: apiConfig.API_BASE_URL + '/api/users/me',
      method: 'GET',
      header: {
        'Authorization': 'Bearer ' + token
      },
      success: (res) => {
        if (res.data && res.data.user) {
          this.setData({
            user: res.data.user,
            currentNickname: res.data.user.nickname || ''
          });
          // 数据加载完成后显示模态框
          this.setData({ showProfileModal: true });
        }
      },
      fail: (err) => {
        console.error('获取用户信息失败:', err);
        // 即使失败也显示模态框，使用默认值
        this.setData({ showProfileModal: true });
      }
    });
  },

  onProfileModalClose() {
    this.setData({ showProfileModal: false });
  },

  onProfileModalConfirm(e) {
    const { nickname } = e.detail;
    this.updateUserProfile(nickname);
  },

  updateUserProfile(nickname) {
    this.setData({ showProfileModal: false });
    
    wx.showLoading({ title: '保存中...' });
    
    const token = wx.getStorageSync('token');
    
    // 先调用后端更新用户信息
    wx.request({
      url: apiConfig.API_BASE_URL + '/api/users/me/profile',
      method: 'PUT',
      header: {
        'Authorization': 'Bearer ' + token,
        'Content-Type': 'application/json'
      },
      data: {
        nickname: nickname
      },
      success: (res) => {
        wx.hideLoading();
        if (res.data && res.data.user) {
          // 更新本地存储
          const app = getApp();
          app.globalData.userInfo = res.data.user;
          wx.setStorageSync('userInfo', res.data.user);
          
          // 更新页面数据
          this.setData({
            user: res.data.user,
            currentNickname: res.data.user.nickname || ''
          });
          
          wx.showToast({
            title: '保存成功',
            icon: 'success'
          });
        }
      },
      fail: (err) => {
        wx.hideLoading();
        console.error('更新用户信息失败:', err);
        wx.showToast({
          title: '保存失败',
          icon: 'none'
        });
      }
    });
  },

  onAllowStatsDisplayChange(e) {
    const allowStatsDisplay = e.detail.value;
    this.setData({ allowStatsDisplay });
    
    const token = wx.getStorageSync('token');
    wx.request({
      url: apiConfig.API_BASE_URL + '/api/users/me/settings',
      method: 'PUT',
      header: {
        'Authorization': 'Bearer ' + token,
        'Content-Type': 'application/json'
      },
      data: {
        allowStatsDisplay: allowStatsDisplay
      },
      success: (res) => {
        if (res.data && res.data.user) {
          this.setData({
            user: res.data.user,
            allowStatsDisplay: res.data.user.allowStatsDisplay || false
          });
          wx.showToast({
            title: '设置已保存',
            icon: 'success'
          });
        }
      },
      fail: (err) => {
        console.error('更新设置失败:', err);
        wx.showToast({
          title: '保存失败',
          icon: 'none'
        });
        this.setData({
          allowStatsDisplay: !allowStatsDisplay
        });
      }
    });
  }
});
