const { API_BASE_URL } = require('../../utils/apiConfig');

Page({
  data: {
    recent7DaysCount: 0,
    monthlyCount: 0,
    monthlyProgress: 0,
    currentMonth: '',
    currentWeekInfo: ''
  },
  onLoad: function() {
    // 计算当前月份和周信息
    this.calculateDateInfo();
  },
  onShow: function() {
    // 每次显示页面时检查登录状态
    const app = getApp();
    if (!app.checkLogin()) {
      // 未登录，弹出登录提示
      wx.showModal({
        title: '提示',
        content: '请先登录以查看统计数据',
        confirmText: '去登录',
        success: (res) => {
          if (res.confirm) {
            // 保存回调函数，登录成功后返回统计页面
            app.pendingCallback = () => {
              this.loadStatistics();
            };
            wx.navigateTo({ url: '/pages/login/login' });
          } else {
            wx.switchTab({ url: '/pages/square/square' });
          }
        }
      });
      return;
    }
    
    // 进入页面时刷新数据
    this.loadStatistics();
  },
  calculateDateInfo: function() {
    const now = new Date();
    const year = now.getFullYear();
    const month = now.getMonth() + 1;
    
    // 计算当前月份
    const currentMonth = `${year}年${month}月`;
    
    // 计算当前周信息
    const weekInfo = this.getWeekInfo(now);
    const currentWeekInfo = `${month}月第${weekInfo.weekNumber}周`;
    
    this.setData({
      currentMonth,
      currentWeekInfo
    });
  },
  getWeekInfo: function(date) {
    const now = date || new Date();
    const year = now.getFullYear();
    const month = now.getMonth();
    
    // 获取当月第一天
    const firstDay = new Date(year, month, 1);
    // 获取当月第一天是星期几（0-6，0是星期日）
    const firstDayOfWeek = firstDay.getDay() || 7; // 转换为1-7，1是星期一
    
    // 获取当前日期是当月第几天
    const currentDate = now.getDate();
    
    // 计算当前周是当月第几周
    const weekNumber = Math.ceil((currentDate + firstDayOfWeek - 1) / 7);
    
    return { weekNumber };
  },
  loadStatistics: function() {
    wx.showLoading({ title: '加载中...' });
    
    const app = getApp();
    
    wx.request({
      url: `${API_BASE_URL}/api/checkin/statistics`,
      method: 'GET',
      header: {
        'Authorization': 'Bearer ' + wx.getStorageSync('token')
      },
      success: (res) => {
        wx.hideLoading();
        if (res.data.error) {
          wx.showToast({ title: res.data.error, icon: 'none' });
        } else {
          // 计算月度进度
          const now = new Date();
          const month = now.getMonth() + 1;
          const year = now.getFullYear();
          const daysInMonth = new Date(year, month, 0).getDate();
          const currentDay = now.getDate();
          const monthlyProgress = Math.round((currentDay / daysInMonth) * 100);
          
          this.setData({
            recent7DaysCount: res.data.recent7DaysCount,
            monthlyCount: res.data.recent7DaysCount, // 这里需要后端提供月度数据，暂时用7天数据代替
            monthlyProgress
          });
        }
      },
      fail: (err) => {
        wx.hideLoading();
        wx.showToast({ title: '网络错误', icon: 'none' });
      }
    });
  },
  // 触发式刷新方法，供其他页面调用
  refreshStatistics: function() {
    this.loadStatistics();
  },

  // 导航到设置页面
  navigateToSettings: function() {
    wx.navigateTo({
      url: '/pages/settings/settings'
    });
  }
})