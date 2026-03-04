const apiConfig = require('../../utils/apiConfig');

Page({
  data: {
    topics: [],
    token: '',
    showPublishModal: false,
    showCheckinModal: false,
    newTopic: {
      title: '',
      description: ''
    },
    titleInputHeight: 80, // 标题输入框初始高度
    descriptionInputHeight: 200, // 描述输入框初始高度
    maxTitleInputHeight: 160, // 标题输入框最大高度
    maxDescriptionInputHeight: 400, // 描述输入框最大高度
    hasPublishPermission: false, // 是否有发布权限
    hasReportPermission: false, // 是否有报告查看权限
    // 打卡相关数据
    checkinIsLoading: false,
    checkinHasCheckedInToday: false,
    checkinShowCelebration: false,
    checkinFireworks: [],
    checkinEncouragementText: '感谢您的坚持，明天再来哦！',
    checkinTopics: [],
    checkinSelectedTopicId: null,
    // 一言文本
    hitokotoText: '',
    hitokotoFrom: ''
  },

  onLoad() {
    // 获取存储的token
    const token = wx.getStorageSync('token');
    this.setData({ token });
    
    // 检查用户权限
    this.checkUserPermissions();
    
    // 加载主题列表
    this.loadTopics();
    
    // 获取一言文本
    this.getHitokotoText();
  },

  // 获取一言文本
  getHitokotoText() {
    wx.request({
      url: 'https://v1.hitokoto.cn/',
      method: 'GET',
      timeout: 3000,
      data: {
        c: 'b', // 漫画
        c: 'd', // 文学
        c: 'k', // 哲学
        encode: 'json'
      },
      success: (res) => {
        if (res.data && res.data.hitokoto) {
          this.setData({
            hitokotoText: res.data.hitokoto,
            hitokotoFrom: res.data.from || ''
          });
        }
      },
      fail: (err) => {
        console.log('获取一言文本失败:', err);
        // 使用默认文本
        this.setData({
          hitokotoText: '发现精彩主题，一起坚持打卡'
        });
      }
    });
  },

  // 检查用户权限
  checkUserPermissions() {
    const token = this.data.token;
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
            hasPublishPermission: true, // 暂时默认所有登录用户都有发布权限
            hasReportPermission: hasAdminRole
          });
        }
      },
      fail: (err) => {
        console.error('获取用户权限失败:', err);
        this.setData({
          hasPublishPermission: true,
          hasReportPermission: false
        });
      }
    });
  },

  // 显示打卡弹框
  showCheckinModal() {
    this.setData({ showCheckinModal: true, checkinIsLoading: true });
    // 检查打卡状态
    this.checkinCheckTodayCheckin();
    // 加载主题列表
    this.checkinLoadTopics();
  },

  // 隐藏打卡弹框
  hideCheckinModal() {
    this.setData({ showCheckinModal: false });
  },

  // 检查今日打卡状态
  checkinCheckTodayCheckin() {
    const token = this.data.token;
    wx.request({
      url: apiConfig.API_BASE_URL + '/api/checkin/check-today',
      method: 'GET',
      header: {
        'Authorization': 'Bearer ' + token
      },
      success: (res) => {
        this.setData({ checkinIsLoading: false });
        if (res.data.error) {
          wx.showToast({ title: res.data.error, icon: 'none' });
        } else {
          this.setData({ 
            checkinHasCheckedInToday: res.data.hasCheckedInToday
          });
          // 如果已经打卡，获取鼓励性文本
          if (res.data.hasCheckedInToday) {
            this.checkinGetEncouragementText();
          }
        }
      },
      fail: (err) => {
        this.setData({ checkinIsLoading: false });
        wx.showToast({ title: '网络错误', icon: 'none' });
        console.error('检查打卡状态失败:', err);
      }
    });
  },

  // 加载打卡主题列表
  checkinLoadTopics() {
    const token = this.data.token;
    wx.request({
      url: apiConfig.API_BASE_URL + '/api/checkin/topics',
      method: 'GET',
      header: {
        'Authorization': 'Bearer ' + token
      },
      success: (res) => {
        if (res.data && res.data.topics) {
          // 获取当前用户信息
          wx.request({
            url: apiConfig.API_BASE_URL + '/api/users/me',
            method: 'GET',
            header: {
              'Authorization': 'Bearer ' + token
            },
            success: (userRes) => {
              if (userRes.data && userRes.data.user) {
                const currentUserId = userRes.data.user.user.id;
                const hasAdminRole = userRes.data.user.roles && userRes.data.user.roles.includes('ADMIN');
                
                // 为每个主题检查用户是否具有查看报告的权限
                const topicsWithPermission = res.data.topics.map(topic => {
                  const isTopicCreator = topic.createdBy === currentUserId;
                  const canViewReport = hasAdminRole || isTopicCreator;
                  return {
                    ...topic,
                    canViewReport
                  };
                });
                
                this.setData({ checkinTopics: topicsWithPermission });
              } else {
                this.setData({ checkinTopics: res.data.topics });
              }
            },
            fail: () => {
              this.setData({ checkinTopics: res.data.topics });
            }
          });
        }
      },
      fail: (err) => {
        console.error('获取主题列表失败:', err);
      }
    });
  },

  // 选择主题
  checkinSelectTopic(e) {
    const topicId = e.currentTarget.dataset.topicId;
    this.setData({ checkinSelectedTopicId: topicId });
  },

  // 处理内容输入
  checkinOnContentInput(e) {
    // 可以在这里添加内容输入的处理逻辑
  },

  // 提交打卡
  checkinSubmitCheckin(e) {
    const { title, content } = e.detail.value;
    const topicId = this.data.checkinSelectedTopicId;
    
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
    
    if (!topicId) {
      wx.showToast({ title: '请选择主题', icon: 'none' });
      return;
    }
    
    wx.showLoading({ title: '提交中...' });
    
    // 调用后端API提交打卡数据
    const token = this.data.token;
    wx.request({
      url: apiConfig.API_BASE_URL + '/api/checkin/topics/' + topicId + '/checkin',
      method: 'POST',
      header: {
        'Authorization': 'Bearer ' + token,
        'Content-Type': 'application/json'
      },
      data: { 
        title, 
        content 
      },
      success: (res) => {
        wx.hideLoading();
        if (res.data.error) {
          wx.showToast({ title: res.data.error, icon: 'none' });
        } else {
          wx.showToast({ title: '打卡成功', icon: 'success' });
          // 更新打卡状态
          this.setData({ checkinHasCheckedInToday: true });
          // 显示庆祝动画
          this.checkinShowCelebration();
          // 获取鼓励性文本
          this.checkinGetEncouragementText();
          // 重新加载主题列表
          this.loadTopics();
        }
      },
      fail: (err) => {
        wx.hideLoading();
        wx.showToast({ title: '网络错误', icon: 'none' });
        console.error('提交打卡失败:', err);
      }
    });
  },

  // 显示庆祝动画
  checkinShowCelebration() {
    // 使用新的 API 获取屏幕信息
    const windowInfo = wx.getWindowInfo ? wx.getWindowInfo() : wx.getSystemInfoSync();
    const windowWidth = windowInfo.windowWidth || windowInfo.screenWidth;
    const windowHeight = windowInfo.windowHeight || windowInfo.screenHeight;
    
    // 生成烟花效果
    const fireworks = [];
    for (let i = 0; i < 10; i++) {
      fireworks.push({
        left: Math.random() * windowWidth,
        top: Math.random() * windowHeight * 0.7,
        delay: Math.random() * 2
      });
    }
    
    this.setData({
      checkinShowCelebration: true,
      checkinFireworks: fireworks
    });
    
    // 3秒后隐藏庆祝动画
    setTimeout(() => {
      this.setData({ checkinShowCelebration: false });
    }, 3000);
  },

  // 获取随机鼓励性文本
  checkinGetEncouragementText() {
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
            checkinEncouragementText: res.data.hitokoto
          });
        }
      },
      fail: (err) => {
        // API调用失败时，保持默认文本
        console.log('获取鼓励文本失败:', err);
      }
    });
  },

  onShow() {
    // 页面显示时重新加载主题列表，确保数据最新
    this.loadTopics();
  },

  // 加载主题列表
  loadTopics() {
    wx.showLoading({ title: '加载中...' });
    
    wx.request({
      url: apiConfig.API_BASE_URL + '/api/checkin/topics',
      method: 'GET',
      header: {
        'Authorization': 'Bearer ' + this.data.token
      },
      success: (res) => {
        wx.hideLoading();
        if (res.data && res.data.topics) {
          // 获取当前用户信息
          const token = this.data.token;
          wx.request({
            url: apiConfig.API_BASE_URL + '/api/users/me',
            method: 'GET',
            header: {
              'Authorization': 'Bearer ' + token
            },
            success: (userRes) => {
              if (userRes.data && userRes.data.user) {
                const currentUserId = userRes.data.user.user.id;
                const hasAdminRole = userRes.data.user.roles && userRes.data.user.roles.includes('ADMIN');
                
                // 为每个主题检查用户是否具有查看报告的权限
                const topicsWithPermission = res.data.topics.map(topic => {
                  const isTopicCreator = topic.createdBy === currentUserId;
                  const canViewReport = hasAdminRole || isTopicCreator;
                  return {
                    ...topic,
                    canViewReport
                  };
                });
                
                this.setData({ 
                  topics: topicsWithPermission,
                  hasReportPermission: hasAdminRole || topicsWithPermission.some(topic => topic.canViewReport)
                });
              } else {
                this.setData({ topics: res.data.topics });
              }
            },
            fail: () => {
              this.setData({ topics: res.data.topics });
            }
          });
        }
      },
      fail: (err) => {
        wx.hideLoading();
        wx.showToast({ title: '加载失败', icon: 'none' });
        console.error('获取主题列表失败:', err);
      }
    });
  },

  // 导航到主题详情页面
  navigateToTopicDetail(e) {
    const topicId = e.currentTarget.dataset.topicId;
    wx.navigateTo({
      url: '/pages/topic-detail/topic-detail?topicId=' + topicId
    });
  },

  // 显示发布主题弹框
  showPublishModal() {
    this.setData({
      showPublishModal: true,
      newTopic: {
        title: '',
        description: ''
      }
    });
  },

  // 隐藏发布主题弹框
  hidePublishModal() {
    this.setData({
      showPublishModal: false
    });
  },

  // 阻止事件冒泡
  stopPropagation() {
    // 阻止事件冒泡，防止点击弹框内容时关闭弹框
  },

  // 处理标题输入
  onTitleInput(e) {
    const value = e.detail.value;
    this.setData({
      'newTopic.title': value
    });
    
    // 计算标题输入框的高度
    this.calculateInputHeight(value, 'title');
  },

  // 处理描述输入
  onDescriptionInput(e) {
    const value = e.detail.value;
    this.setData({
      'newTopic.description': value
    });
    
    // 计算描述输入框的高度
    this.calculateInputHeight(value, 'description');
  },

  // 计算输入框高度
  calculateInputHeight(value, type) {
    // 创建一个临时的text元素来计算文本高度
    const query = wx.createSelectorQuery();
    
    if (type === 'title') {
      // 计算标题输入框高度
      const maxHeight = this.data.maxTitleInputHeight;
      const lineHeight = 30; // 每行的高度（rpx）
      const lines = Math.ceil(value.length / 15); // 假设每行15个字符
      let height = Math.max(80, Math.min(lines * lineHeight + 20, maxHeight));
      
      this.setData({
        titleInputHeight: height
      });
    } else if (type === 'description') {
      // 计算描述输入框高度
      const maxHeight = this.data.maxDescriptionInputHeight;
      const lineHeight = 30; // 每行的高度（rpx）
      const lines = Math.ceil(value.length / 20); // 假设每行20个字符
      let height = Math.max(200, Math.min(lines * lineHeight + 20, maxHeight));
      
      this.setData({
        descriptionInputHeight: height
      });
    }
  },

  // 发布主题
  publishTopic() {
    const { title, description } = this.data.newTopic;
    
    // 表单验证
    if (!title) {
      wx.showToast({ title: '请输入主题标题', icon: 'none' });
      return;
    }
    
    if (title.length < 10) {
      wx.showToast({ title: '主题标题至少需要10个字符', icon: 'none' });
      return;
    }
    
    if (title.length > 100) {
      wx.showToast({ title: '主题标题不能超过100个字符', icon: 'none' });
      return;
    }
    
    wx.showLoading({ title: '发布中...' });
    
    // 调用后端API发布主题
    const token = this.data.token;
    wx.request({
      url: apiConfig.API_BASE_URL + '/api/checkin/topics',
      method: 'POST',
      header: {
        'Authorization': 'Bearer ' + token,
        'Content-Type': 'application/json'
      },
      data: {
        title,
        description,
        durationDays: 7
      },
      success: (res) => {
        wx.hideLoading();
        if (res.data && res.data.error) {
          wx.showToast({ title: res.data.error, icon: 'none' });
        } else {
          wx.showToast({ title: '发布成功', icon: 'success' });
          // 隐藏弹框
          this.hidePublishModal();
          // 重新加载主题列表
          this.loadTopics();
        }
      },
      fail: (err) => {
        wx.hideLoading();
        wx.showToast({ title: '发布失败，请重试', icon: 'none' });
        console.error('发布主题失败:', err);
      }
    });
  },

  // 跳转到报告页面
  navigateToReport() {
    wx.navigateTo({
      url: '/pages/report/report'
    });
  }
});
