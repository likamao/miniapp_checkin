Page({
  data: {
  },
  
  onLoad: function() {
    this.initApp();
  },
  
  onShow: function() {
  },
  
  initApp: function() {
    const app = getApp();
    
    // 开屏显示时间（毫秒）
    const splashTime = 2500;
    const startTime = Date.now();
    
    // 检查登录状态，如果有 token 则验证
    const token = wx.getStorageSync('token');
    if (token) {
      app.globalData.token = token;
      const userInfo = wx.getStorageSync('userInfo');
      if (userInfo) {
        app.globalData.userInfo = userInfo;
      }
    }
    
    // 等待指定时间后跳转到主页面
    const elapsedTime = Date.now() - startTime;
    const delayTime = Math.max(0, splashTime - elapsedTime);
    
    setTimeout(() => {
      this.navigateToMain();
    }, delayTime);
  },
  
  navigateToMain: function() {
    wx.switchTab({
      url: '/pages/square/square',
      success: () => {
        console.log('跳转到主页面成功');
      },
      fail: (err) => {
        console.error('跳转到主页面失败:', err);
        // 失败时尝试普通跳转
        wx.reLaunch({
          url: '/pages/square/square'
        });
      }
    });
  }
});
