const apiConfig = require('../../utils/apiConfig');
const { getValidToken, validateCheckinForm, validateTopicForm, cleanObject } = require('../../utils/validate');

Page({
  data: {
    topics: [],
    token: '',
    showPublishModal: false,
    showCheckinModal: false,
    newTopic: {
      title: '',
      description: '',
      durationUnit: 'week',
      durationDays: 7,
      isPrivate: false // 主题可见性，false 为公开，true 为私有
    },
    titleInputHeight: 80, // 标题输入框初始高度
    descriptionInputHeight: 200, // 描述输入框初始高度
    maxTitleInputHeight: 160, // 标题输入框最大高度
    maxDescriptionInputHeight: 400, // 描述输入框最大高度
    hasPublishPermission: false, // 是否有发布权限
    hasReportPermission: false, // 是否有报告查看权限
    isPrivateTopic: false, // 是否创建的是私有主题（USER/VIEWER角色）
    canSelectVisibility: false, // 是否可以选择主题可见性（PUBLISHER/ADMIN角色）
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
    // 获取存储的token并同步到globalData
    const app = getApp();
    const token = wx.getStorageSync('token');
    this.setData({ token });
    app.globalData.token = token;
    
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
    const app = getApp();
    // 优先从 globalData 获取 token，如果没有则从 storage 获取
    const token = app.globalData.token || wx.getStorageSync('token');
    this.setData({ token });
    if (!token) {
      this.setData({
        hasPublishPermission: false,
        hasReportPermission: false,
        isPrivateTopic: false,
        canSelectVisibility: false
      });
      return;
    }
    
    wx.request({
      url: apiConfig.API_BASE_URL + '/api/users/me',
      method: 'GET',
      header: {
        'Authorization': 'Bearer ' + token
      },
      success: (res) => {
        if (res.data && res.data.user) {
          // 检查用户角色
          console.log(res.data.user);
          const roles = res.data.user.roles || [];
          const hasAdminRole = roles.includes('ADMIN');
          const isPublisher = roles.includes('PUBLISHER');
          
          // USER 和 VIEWER 角色创建的主题默认为私有
          const isPrivateTopic = !hasAdminRole && !isPublisher;
          // PUBLISHER 和 ADMIN 角色可以选择主题可见性
          const canSelectVisibility = hasAdminRole || isPublisher;
          
          this.setData({
            hasPublishPermission: true, // 后端已支持所有用户创建主题（USER创建默认为私有）
            hasReportPermission: hasAdminRole || isPublisher,
            isPrivateTopic: isPrivateTopic,
            canSelectVisibility: canSelectVisibility
          });
        } else {
          this.setData({
            hasPublishPermission: false,
            hasReportPermission: false,
            isPrivateTopic: false,
            canSelectVisibility: false
          });
        }
      },
      fail: (err) => {
        console.error('获取用户权限失败:', err);
        this.setData({
          hasPublishPermission: false,
          hasReportPermission: false,
          isPrivateTopic: false,
          canSelectVisibility: false
        });
      }
    });
  },

  // 显示打卡弹框
  showCheckinModal() {
    // 检查登录状态
    if (!this.checkLoginStatus()) {
      return;
    }
    
    this.setData({ showCheckinModal: true, checkinIsLoading: true });
    // 禁止背景滚动
    wx.pageScrollTo({ scrollTop: 0 });
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
    const token = wx.getStorageSync('token');
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

  // 加载打卡主题列表（仅有效主题）
  checkinLoadTopics() {
    const token = wx.getStorageSync('token');
    wx.request({
      url: apiConfig.API_BASE_URL + '/api/checkin/topics/active',
      method: 'GET',
      header: {
        'Authorization': 'Bearer ' + token
      },
      success: (res) => {
        if (res.data && res.data.topics) {
          // 确保 topics 是数组
          const topics = Array.isArray(res.data.topics) ? res.data.topics : [];
          
          // 获取当前用户信息
          wx.request({
            url: apiConfig.API_BASE_URL + '/api/users/me',
            method: 'GET',
            header: {
              'Authorization': 'Bearer ' + token
            },
            success: (userRes) => {
              if (userRes.data && userRes.data.user) {
                const currentUserId = userRes.data.user.user?.id;
                const hasAdminRole = userRes.data.user.roles && userRes.data.user.roles.includes('ADMIN');
                
                // 为每个主题检查用户是否具有查看报告的权限
                const topicsWithPermission = topics.map(topic => {
                  const isTopicCreator = topic.createdBy === currentUserId;
                  const canViewReport = hasAdminRole || isTopicCreator;
                  return {
                    ...topic,
                    canViewReport
                  };
                });
                
                this.setData({ checkinTopics: topicsWithPermission });
              } else {
                this.setData({ checkinTopics: topics });
              }
            },
            fail: () => {
              this.setData({ checkinTopics: topics });
            }
          });
        } else {
          // 没有主题数据
          this.setData({ checkinTopics: [] });
        }
      },
      fail: (err) => {
        console.error('获取主题列表失败:', err);
        this.setData({ checkinTopics: [] });
        wx.showToast({ title: '获取主题失败', icon: 'none' });
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
    
    // 再次检查登录状态（防止登录过期）
    if (!this.checkLoginStatus()) {
      return;
    }
    
    // 表单验证
    const formData = { title, content };
    const validationResult = validateCheckinForm(formData);
    if (!validationResult.isValid) {
      wx.showToast({ title: validationResult.error, icon: 'none' });
      return;
    }
    
    if (!topicId) {
      wx.showToast({ title: '请选择主题', icon: 'none' });
      return;
    }
    
    wx.showLoading({ title: '提交中...' });
    
    // 调用后端API提交打卡数据
    const token = getValidToken();
    if (!token) {
      wx.hideLoading();
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }
    
    // 清理数据，确保不会发送无效参数
    const cleanedData = cleanObject({ title, content });
    
    wx.request({
      url: apiConfig.API_BASE_URL + '/api/checkin/topics/' + topicId + '/checkin',
      method: 'POST',
      header: {
        'Authorization': 'Bearer ' + token,
        'Content-Type': 'application/json'
      },
      data: cleanedData,
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
    // 清除之前的定时器，避免重复动画
    if (this.celebrationTimer) {
      clearTimeout(this.celebrationTimer);
      this.celebrationTimer = null;
    }
    
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
    this.celebrationTimer = setTimeout(() => {
      this.setData({ checkinShowCelebration: false });
    }, 3000);
  },

  // 页面卸载时清理资源
  onUnload: function() {
    // 清理定时器
    if (this.celebrationTimer) {
      clearTimeout(this.celebrationTimer);
      this.celebrationTimer = null;
    }
    
    // 清理其他资源
    this.setData({
      topics: [],
      newTopic: { title: '', description: '' },
      checkinFireworks: []
    });
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
    // 检查用户权限，确保发布主题按钮的显示状态正确
    this.checkUserPermissions();
  },

  // 加载主题列表
  loadTopics() {
    wx.showLoading({ title: '加载中...' });
    
    // 获取有效的 token
    const token = getValidToken();
    
    // 构建请求头
    const header = {};
    if (token) {
      header['Authorization'] = 'Bearer ' + token;
    }
    
    wx.request({
      url: apiConfig.API_BASE_URL + '/api/checkin/topics',
      method: 'GET',
      header: header,
      success: (res) => {
        wx.hideLoading();
        if (res.data && res.data.topics) {
          // 获取当前用户信息（如果有token）
          if (token) {
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
                  const isPublisher = userRes.data.user.roles && userRes.data.user.roles.includes('PUBLISHER');
                  
                  // 为每个主题检查用户是否具有查看报告的权限，并检测是否过期
                  const topicsWithPermission = res.data.topics.map(topic => {
                    const isTopicCreator = topic.createdBy === currentUserId;
                    const canViewReport = hasAdminRole || isTopicCreator;
                    const isExpired = this.checkTopicExpired(topic.endDatetime);
                    return {
                      ...topic,
                      canViewReport,
                      isExpired
                    };
                  });
                  
                  this.setData({ 
                    topics: topicsWithPermission,
                    hasReportPermission: hasAdminRole || topicsWithPermission.some(topic => topic.canViewReport)
                  });
                } else {
                  // 添加过期检测
                  const topicsWithExpired = res.data.topics.map(topic => {
                    return {
                      ...topic,
                      isExpired: this.checkTopicExpired(topic.endDatetime)
                    };
                  });
                  this.setData({ topics: topicsWithExpired });
                }
              },
              fail: () => {
                // 添加过期检测
                const topicsWithExpired = res.data.topics.map(topic => {
                  return {
                    ...topic,
                    isExpired: this.checkTopicExpired(topic.endDatetime)
                  };
                });
                this.setData({ topics: topicsWithExpired });
              }
            });
          } else {
            // 没有token，只添加过期检测
            const topicsWithExpired = res.data.topics.map(topic => {
              return {
                ...topic,
                isExpired: this.checkTopicExpired(topic.endDatetime)
              };
            });
            this.setData({ topics: topicsWithExpired });
          }
        }
      },
      fail: (err) => {
        wx.hideLoading();
        wx.showToast({ title: '加载失败', icon: 'none' });
        console.error('获取主题列表失败:', err);
      }
    });
  },

  // 检查主题是否过期
  checkTopicExpired(endDatetimeStr) {
    const endTime = new Date(endDatetimeStr).getTime();
    const currentTime = new Date().getTime();
    
    return currentTime > endTime;
  },

  // 导航到主题详情页面
  navigateToTopicDetail(e) {
    const topicId = e.currentTarget.dataset.topicId;
    wx.navigateTo({
      url: '/pages/topic-detail/topic-detail?topicId=' + topicId
    });
  },

  // 检查登录状态，未登录时弹出登录提示
  checkLoginStatus(callback) {
    const app = getApp();
    if (app.checkLogin()) {
      // 已登录，执行回调
      if (callback) callback();
      return true;
    } else {
      // 未登录，显示登录提示
      this.showLoginRequired(callback);
      return false;
    }
  },

  // 显示登录提示弹窗
  showLoginRequired(callback) {
    wx.showModal({
      title: '提示',
      content: '请先登录以使用此功能',
      confirmText: '去登录',
      success: (res) => {
        if (res.confirm) {
          // 保存回调函数到全局，登录成功后调用
          const app = getApp();
          app.pendingCallback = callback;
          // 跳转到登录页面
          wx.navigateTo({
            url: '/pages/login/login'
          });
        }
      }
    });
  },

  // 显示发布主题弹框
  showPublishModal() {
    this.setData({
      showPublishModal: true,
      newTopic: {
        title: '',
        description: '',
        durationUnit: 'week',
        durationDays: 7,
        isPrivate: false // 默认设置为公开
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

  // 选择有效期
  selectDuration(e) {
    const durationUnit = e.currentTarget.dataset.duration;
    const durationDays = parseInt(e.currentTarget.dataset.days);
    this.setData({
      'newTopic.durationUnit': durationUnit,
      'newTopic.durationDays': durationDays
    });
  },

  // 切换主题可见性
  toggleVisibility(e) {
    const isPrivate = e.currentTarget.dataset.private === 'true';
    this.setData({
      'newTopic.isPrivate': isPrivate
    });
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
    const { title, description, durationDays, isPrivate } = this.data.newTopic;
    
    // 表单验证
    const formData = { title, description, durationDays };
    const validationResult = validateTopicForm(formData);
    if (!validationResult.isValid) {
      wx.showToast({ title: validationResult.error, icon: 'none' });
      return;
    }
    
    wx.showLoading({ title: '发布中...' });
    
    // 调用后端 API 发布主题
    const token = getValidToken();
    if (!token) {
      wx.hideLoading();
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }
    
    // 构建发布数据
    const publishData = {
      title,
      description,
      durationDays
    };
    
    // 只有 PUBLISHER 和 ADMIN 角色需要传递可见性参数
    if (this.data.canSelectVisibility) {
      publishData.isPrivate = isPrivate;
    }
    
    // 清理数据，确保不会发送无效参数
    const cleanedData = cleanObject(publishData);
    
    wx.request({
      url: apiConfig.API_BASE_URL + '/api/checkin/topics',
      method: 'POST',
      header: {
        'Authorization': 'Bearer ' + token,
        'Content-Type': 'application/json'
      },
      data: cleanedData,
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
    // 检查登录状态
    if (!this.checkLoginStatus()) {
      return;
    }
    
    // 前端权限验证
    const token = wx.getStorageSync('token');
    if (!token || !this.data.hasReportPermission) {
      wx.showToast({ title: '权限不足', icon: 'none' });
      return;
    }
    
    wx.navigateTo({
      url: '/pages/report/report'
    });
  }
});
