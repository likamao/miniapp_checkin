const apiConfig = require('../../utils/apiConfig');

Page({
  data: {
    user: null,
    allowStatsDisplay: false,
    showProfileModal: false,
    defaultNickname: '微信用户',
    currentNickname: '',
    dataSettingsTitle: '数据设置',
    dataSettings: []
  },

  onLoad() {
    this.loadUserInfo();
    this.loadDataSettings();
  },

  loadDataSettings() {
    const token = wx.getStorageSync('token');
    wx.request({
      url: apiConfig.API_BASE_URL + '/api/users/data-settings',
      method: 'GET',
      header: {
        'Authorization': 'Bearer ' + token
      },
      success: (res) => {
        if (res.data) {
          this.setData({
            dataSettingsTitle: res.data.title || '数据设置',
            dataSettings: res.data.items || []
          });
        }
      },
      fail: (err) => {
        console.error('获取数据设置失败:', err);
        // 使用默认数据
        this.setData({
          dataSettingsTitle: '数据设置',
          dataSettings: [
            { icon: '🌊', text: '当您登录时，你只能查看你自己的打卡数据' },
            { icon: '🌊', text: '只有管理员，对应主题发布者才能查看主题内所有人的周报和月报' },
            { icon: '🌊', text: '未开启此开关时，您的信息将被匿名化处理，显示为"微信用户"' },
            { icon: '🌊', text: '您可以随时在设置中开启或关闭此功能' }
          ]
        });
      }
    });
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

  onProfileModalClose(e) {
    this.setData({ showProfileModal: false });
    
    // 如果用户没有修改昵称，不调用接口
    if (!e.detail.isModified) {
      return;
    }
    
    // 用户有修改，调用更新接口
    const { nickname } = e.detail;
    if (nickname) {
      this.updateUserProfile(nickname);
    }
  },

  onProfileModalConfirm(e) {
    const { nickname, isModified } = e.detail;
    
    // 关闭弹框
    this.setData({ showProfileModal: false });
    
    // 如果用户没有修改昵称，不调用接口
    if (!isModified) {
      return;
    }
    
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
