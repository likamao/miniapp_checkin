const apiConfig = require('../../utils/apiConfig');

Page({
  data: {
    user: null,
    allowStatsDisplay: false
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
            allowStatsDisplay: res.data.user.user.allowStatsDisplay || false,
            hasReportPermission: hasAdminRole
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
            user: res.data.user
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
