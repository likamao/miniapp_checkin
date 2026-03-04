// 开屏页面逻辑
Page({
  data: {
    // 页面数据
  },
  
  onLoad: function() {
    // 页面加载时执行
    this.initApp();
  },
  
  onShow: function() {
    // 页面显示时执行
  },
  
  // 初始化应用
  initApp: function() {
    const app = getApp();
    
    // 开屏最小显示时间（毫秒）
    const minSplashTime = 1800;
    // 记录开始时间
    const startTime = Date.now();
    
    // 检查登录状态
    const token = wx.getStorageSync('token');
    
    if (token) {
      // 验证登录状态
      this.verifyLoginStatus(startTime, minSplashTime);
    } else {
      // 未登录，跳转到登录页面，确保至少显示指定时间
      const elapsedTime = Date.now() - startTime;
      const delayTime = Math.max(0, minSplashTime - elapsedTime);
      
      setTimeout(() => {
        this.navigateToLogin();
      }, delayTime);
    }
  },
  
  // 验证登录状态
  verifyLoginStatus: function(startTime, minSplashTime) {
    const app = getApp();
    const token = wx.getStorageSync('token');
    
    if (!token) {
      // 确保至少显示指定时间
      const elapsedTime = Date.now() - startTime;
      const delayTime = Math.max(0, minSplashTime - elapsedTime);
      
      setTimeout(() => {
        this.navigateToLogin();
      }, delayTime);
      return;
    }
    
    wx.request({
      url: `${app.globalData.apiBaseUrl}/api/auth/verify`,
      method: 'GET',
      header: {
        'Authorization': `Bearer ${token}`
      },
      success: (res) => {
        // 确保至少显示指定时间
        const elapsedTime = Date.now() - startTime;
        const delayTime = Math.max(0, minSplashTime - elapsedTime);
        
        setTimeout(() => {
          if (res.data.valid) {
            // 登录状态有效，跳转到主页面
            app.globalData.token = token;
            app.globalData.userInfo = res.data.user;
            wx.setStorageSync('userInfo', res.data.user);
            this.navigateToMain();
          } else {
            // 登录状态无效，跳转到登录页面
            app.clearLoginState();
            this.navigateToLogin();
          }
        }, delayTime);
      },
      fail: () => {
        // 确保至少显示指定时间
        const elapsedTime = Date.now() - startTime;
        const delayTime = Math.max(0, minSplashTime - elapsedTime);
        
        setTimeout(() => {
          // 网络错误，跳转到登录页面
          app.clearLoginState();
          this.navigateToLogin();
        }, delayTime);
      }
    });
  },
  
  // 跳转到主页面
  navigateToMain: function() {
    wx.switchTab({
      url: '/pages/square/square',
      success: () => {
        console.log('跳转到主页面成功');
      },
      fail: (err) => {
        console.error('跳转到主页面失败:', err);
        // 失败时尝试跳转到登录页面
        this.navigateToLogin();
      }
    });
  },
  
  // 跳转到登录页面
  navigateToLogin: function() {
    wx.redirectTo({
      url: '/pages/login/login',
      success: () => {
        console.log('跳转到登录页面成功');
      },
      fail: (err) => {
        console.error('跳转到登录页面失败:', err);
      }
    });
  }
});