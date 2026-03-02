const { API_BASE_URL } = require('../../utils/apiConfig');

Page({
  data: {
    title: '',
    content: '',
    hasCheckedInToday: false,
    showCelebration: false,
    fireworks: [],
    encouragementText: '感谢您的坚持，明天再来哦！',
    isLoading: true,
    isLoggedIn: false,
    cardBottom: 0,
    minBottomDistance: 200, // 距离底部的最小距离（rpx）
    topics: [],
    selectedTopicId: null,
    selectedTopic: null
  },
  onLoad: function(options) {
    // 检查是否已登录
    this.checkLoginStatus();
    
    // 获取屏幕高度
    const systemInfo = wx.getSystemInfoSync();
    this.setData({
      screenHeight: systemInfo.windowHeight
    });
    
    // 检查是否有主题ID参数
    if (options.topicId) {
      this.setData({ selectedTopicId: options.topicId });
    }
    
    // 加载主题列表
    this.loadTopics();
  },
  onShow: function() {
    // 页面显示时获取卡片位置
    this.getCardPosition();
  },
  // 获取卡片位置
  getCardPosition: function() {
    const query = wx.createSelectorQuery();
    query.select('.card').boundingClientRect();
    query.selectViewport().scrollOffset();
    query.exec((res) => {
      if (res[0]) {
        this.setData({
          cardTop: res[0].top,
          cardHeight: res[0].height
        });
      }
    });
  },
  // 监听内容输入
  onContentInput: function(e) {
    this.setData({
      content: e.detail.value
    });
    // 调整卡片位置
    this.adjustCardPosition();
  },
  // 调整卡片位置
  adjustCardPosition: function() {
    const query = wx.createSelectorQuery();
    query.select('.card').boundingClientRect();
    query.exec((res) => {
      if (res[0]) {
        const cardHeight = res[0].height;
        const screenHeight = this.data.screenHeight;
        const minBottomDistance = this.data.minBottomDistance;
        
        // 计算卡片底部距离屏幕底部的距离
        const cardBottom = screenHeight - (res[0].top + cardHeight);
        
        // 如果距离小于最小距离，调整卡片位置
        if (cardBottom < minBottomDistance) {
          const scrollDistance = minBottomDistance - cardBottom;
          wx.pageScrollTo({
            scrollTop: res[0].top + scrollDistance,
            duration: 300
          });
        }
      }
    });
  },
  // 检查登录状态
  checkLoginStatus: function() {
    const app = getApp();
    if (!app.checkLogin()) {
      // 未登录，跳转到登录页面
      setTimeout(() => {
        wx.redirectTo({ url: '/pages/login/login' });
      }, 500);
      return;
    }
    
    // 已登录，设置登录状态并检查打卡状态
    this.setData({ isLoggedIn: true });
    this.checkTodayCheckin();
  },
  checkTodayCheckin: function() {
    wx.showLoading({ title: '加载中...' });
    
    const app = getApp();
    wx.request({
      url: `${API_BASE_URL}/api/checkin/check-today`,
      method: 'GET',
      header: {
        'Authorization': 'Bearer ' + app.globalData.token
      },
      success: (res) => {
        wx.hideLoading();
        if (res.data.error) {
          wx.showToast({ title: res.data.error, icon: 'none' });
        } else {
          this.setData({ 
            hasCheckedInToday: res.data.hasCheckedInToday,
            isLoading: false
          });
          // 如果已经打卡，获取鼓励性文本
          if (res.data.hasCheckedInToday) {
            this.getEncouragementText();
          }
        }
      },
      fail: (err) => {
        wx.hideLoading();
        wx.showToast({ title: '网络错误', icon: 'none' });
        this.setData({ isLoading: false });
      }
    });
  },
  submitCheckin: function(e) {
    const { title, content } = e.detail.value;
    
    // 表单验证
    if (!title || !content) {
      wx.showToast({ title: '请填写完整信息', icon: 'none' });
      return;
    }
    
    if (title.length > 50) {
      wx.showToast({ title: '标题不能超过50字', icon: 'none' });
      return;
    }
    
    if (content.length > 500) {
      wx.showToast({ title: '内容不能超过500字', icon: 'none' });
      return;
    }
    
    wx.showLoading({ title: '提交中...' });
    
    // 调用后端API提交打卡数据
    const app = getApp();
    wx.request({
      url: `${API_BASE_URL}/api/checkin/create`,
      method: 'POST',
      header: {
        'Authorization': 'Bearer ' + app.globalData.token
      },
      data: { title, content },
      success: (res) => {
        wx.hideLoading();
        if (res.data.error) {
          wx.showToast({ title: res.data.error, icon: 'none' });
        } else {
          wx.showToast({ title: '打卡成功', icon: 'success' });
          // 更新打卡状态
          this.setData({ hasCheckedInToday: true });
          // 显示庆祝动画
          this.showCelebration();
          // 获取鼓励性文本
          this.getEncouragementText();
          // 刷新统计页面数据
          this.refreshStatistics();
        }
      },
      fail: (err) => {
        wx.hideLoading();
        wx.showToast({ title: '网络错误', icon: 'none' });
      }
    });
  },
  showCelebration: function() {
    // 生成烟花效果
    const fireworks = [];
    for (let i = 0; i < 10; i++) {
      fireworks.push({
        left: Math.random() * wx.getSystemInfoSync().windowWidth,
        top: Math.random() * wx.getSystemInfoSync().windowHeight * 0.7,
        delay: Math.random() * 2
      });
    }
    
    this.setData({
      showCelebration: true,
      fireworks
    });
    
    // 3秒后隐藏庆祝动画
    setTimeout(() => {
      this.setData({ showCelebration: false });
    }, 3000);
  },
  refreshStatistics: function() {
    // 触发统计页面的刷新
    const pages = getCurrentPages();
    for (let i = 0; i < pages.length; i++) {
      if (pages[i].route === 'pages/statistics/statistics') {
        pages[i].refreshStatistics();
        break;
      }
    }
  },
  // 获取随机鼓励性文本
  getEncouragementText: function() {
    wx.request({
      url: 'https://v1.hitokoto.cn/',
      method: 'GET',
      timeout: 3000, // 设置3秒超时
      data: {
        c: 'b', // 漫画
        c: 'd', // 文学
        c: 'k', // 哲学
        encode: 'json'
      },
      success: (res) => {
        if (res.data && res.data.hitokoto) {
          this.setData({
            encouragementText: res.data.hitokoto
          });
        }
      },
      fail: (err) => {
        // API调用失败时，保持默认文本
        console.log('获取鼓励文本失败:', err);
      }
    });
  },
  
  // 加载主题列表
  loadTopics: function() {
    const app = getApp();
    wx.request({
      url: `${API_BASE_URL}/api/checkin/topics`,
      method: 'GET',
      header: {
        'Authorization': 'Bearer ' + app.globalData.token
      },
      success: (res) => {
        if (res.data && res.data.topics) {
          this.setData({ topics: res.data.topics });
          
          // 如果有选中的主题ID，找到对应的主题
          if (this.data.selectedTopicId) {
            const selectedTopic = res.data.topics.find(topic => topic.id == this.data.selectedTopicId);
            if (selectedTopic) {
              this.setData({ selectedTopic });
            }
          }
        }
      },
      fail: (err) => {
        console.error('获取主题列表失败:', err);
      }
    });
  },
  
  // 选择主题
  selectTopic: function(e) {
    const topicId = e.currentTarget.dataset.topicId;
    const topic = this.data.topics.find(t => t.id == topicId);
    this.setData({ selectedTopicId: topicId, selectedTopic: topic });
  },
  
  // 提交打卡（支持主题）
  submitCheckin: function(e) {
    const { title, content } = e.detail.value;
    
    // 表单验证
    if (!title || !content) {
      wx.showToast({ title: '请填写完整信息', icon: 'none' });
      return;
    }
    
    if (title.length > 50) {
      wx.showToast({ title: '标题不能超过50字', icon: 'none' });
      return;
    }
    
    if (content.length > 500) {
      wx.showToast({ title: '内容不能超过500字', icon: 'none' });
      return;
    }
    
    wx.showLoading({ title: '提交中...' });
    
    // 调用后端API提交打卡数据
    const app = getApp();
    wx.request({
      url: `${API_BASE_URL}/api/checkin/create-with-topic`,
      method: 'POST',
      header: {
        'Authorization': 'Bearer ' + app.globalData.token
      },
      data: { 
        title, 
        content, 
        topicId: this.data.selectedTopicId 
      },
      success: (res) => {
        wx.hideLoading();
        if (res.data.error) {
          wx.showToast({ title: res.data.error, icon: 'none' });
        } else {
          wx.showToast({ title: '打卡成功', icon: 'success' });
          // 更新打卡状态
          this.setData({ hasCheckedInToday: true });
          // 显示庆祝动画
          this.showCelebration();
          // 获取鼓励性文本
          this.getEncouragementText();
          // 刷新统计页面数据
          this.refreshStatistics();
        }
      },
      fail: (err) => {
        wx.hideLoading();
        wx.showToast({ title: '网络错误', icon: 'none' });
      }
    });
  }
})