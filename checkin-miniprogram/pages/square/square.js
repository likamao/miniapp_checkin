const apiConfig = require('../../utils/apiConfig');

Page({
  data: {
    topics: [],
    token: '',
    showPublishModal: false,
    newTopic: {
      title: '',
      description: ''
    },
    titleInputHeight: 80, // 标题输入框初始高度
    descriptionInputHeight: 200, // 描述输入框初始高度
    maxTitleInputHeight: 160, // 标题输入框最大高度
    maxDescriptionInputHeight: 400 // 描述输入框最大高度
  },

  onLoad() {
    // 获取存储的token
    const token = wx.getStorageSync('token');
    this.setData({ token });
    
    // 加载主题列表
    this.loadTopics();
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
          // 为每个主题获取打卡人数和用户打卡状态
          this.getTopicsDetails(res.data.topics);
        }
      },
      fail: (err) => {
        wx.hideLoading();
        wx.showToast({ title: '加载失败', icon: 'none' });
        console.error('获取主题列表失败:', err);
      }
    });
  },

  // 获取主题详情（打卡人数和用户打卡状态）
  getTopicsDetails(topics) {
    const token = this.data.token;
    const topicsWithDetails = [...topics];
    
    // 为每个主题获取打卡人数和用户打卡状态
    topics.forEach((topic, index) => {
      // 获取打卡人数
      wx.request({
        url: apiConfig.API_BASE_URL + '/api/checkin/topic-checkin-count/' + topic.id,
        method: 'GET',
        header: {
          'Authorization': 'Bearer ' + token
        },
        success: (res) => {
          if (res.data && res.data.count !== undefined) {
            topicsWithDetails[index].checkinCount = res.data.count;
            this.setData({ topics: topicsWithDetails });
          }
        },
        fail: (err) => {
          console.error('获取主题打卡人数失败:', err);
        }
      });

      // 获取用户打卡状态
      wx.request({
        url: apiConfig.API_BASE_URL + '/api/checkin/check-topic/' + topic.id,
        method: 'GET',
        header: {
          'Authorization': 'Bearer ' + token
        },
        success: (res) => {
          if (res.data && res.data.hasCheckedInTopic !== undefined) {
            topicsWithDetails[index].hasCheckedIn = res.data.hasCheckedInTopic;
            this.setData({ topics: topicsWithDetails });
          }
        },
        fail: (err) => {
          console.error('获取用户打卡状态失败:', err);
        }
      });
    });

    this.setData({ topics: topicsWithDetails });
  },

  // 导航到主题详情页面
  navigateToTopicDetail(e) {
    const topicId = e.currentTarget.dataset.topicId;
    wx.navigateTo({
      url: '/pages/checkin/checkin?topicId=' + topicId
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
        description
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
  }
});
