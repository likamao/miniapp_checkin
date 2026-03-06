const { API_BASE_URL } = require('../../utils/apiConfig');

Page({
  data: {
    topicId: null,
    topic: {},
    records: [],
    isLoading: true,
    showCheckinModal: false,
    showSuccessModal: false,
    showEditModal: false,
    checkinForm: {
      title: '',
      content: ''
    },
    editForm: {
      title: '',
      description: ''
    },
    checkinResult: {},
    currentUserId: null,
    isTopicCreator: false,
    isTopicExpired: false
  },

  onLoad(options) {
    if (options.topicId) {
      this.setData({ topicId: options.topicId });
      this.getCurrentUserInfo();
      this.loadTopicDetail();
      this.loadCheckinRecords();
    }
  },

  onShow() {
    if (this.data.topicId) {
      this.loadTopicDetail();
      this.loadCheckinRecords();
    }
  },

  getCurrentUserInfo() {
    const app = getApp();
    wx.request({
      url: `${API_BASE_URL}/api/users/me`,
      method: 'GET',
      header: {
        'Authorization': 'Bearer ' + app.globalData.token
      },
      success: (res) => {
        if (res.data && res.data.user) {
          this.setData({
            currentUserId: res.data.user.user.id
          });
        }
      },
      fail: (err) => {
        console.error('获取用户信息失败:', err);
      }
    });
  },

  loadTopicDetail() {
    const app = getApp();
    wx.request({
      url: `${API_BASE_URL}/api/checkin/topics/${this.data.topicId}`,
      method: 'GET',
      header: {
        'Authorization': 'Bearer ' + app.globalData.token
      },
      success: (res) => {
        if (res.data && res.data.topic) {
          const topic = res.data.topic;
          const currentUserId = this.data.currentUserId;
          const isTopicCreator = currentUserId && topic.createdBy === currentUserId;
          
          // 判断主题是否过期：比较 endDatetime 和当前时间
          const isExpired = this.checkTopicExpired(topic.endDatetime);
          
          this.setData({
            topic: topic,
            isLoading: false,
            isTopicCreator: isTopicCreator,
            isTopicExpired: isExpired
          });
        }
      },
      fail: (err) => {
        console.error('获取主题详情失败:', err);
        wx.showToast({ title: '加载失败', icon: 'none' });
        this.setData({ isLoading: false });
      }
    });
  },

  // 检查主题是否过期
  checkTopicExpired(endDatetimeStr) {
    const endTime = new Date(endDatetimeStr).getTime();
    const currentTime = new Date().getTime();
    
    return currentTime > endTime;
  },

  loadCheckinRecords() {
    const app = getApp();
    wx.request({
      url: `${API_BASE_URL}/api/checkin/topics/${this.data.topicId}/checkin-records`,
      method: 'GET',
      header: {
        'Authorization': 'Bearer ' + app.globalData.token
      },
      data: {
        page: 1,
        pageSize: 20
      },
      success: (res) => {
        if (res.data && res.data.records) {
          this.setData({ records: res.data.records });
        }
      },
      fail: (err) => {
        console.error('获取打卡记录失败:', err);
      }
    });
  },

  showCheckinModal() {
    this.setData({
      showCheckinModal: true,
      checkinForm: {
        title: '',
        content: ''
      }
    });
  },

  hideCheckinModal() {
    this.setData({ showCheckinModal: false });
  },

  stopPropagation() {
  },

  onCheckinTitleInput(e) {
    this.setData({
      'checkinForm.title': e.detail.value
    });
  },

  onCheckinContentInput(e) {
    this.setData({
      'checkinForm.content': e.detail.value
    });
  },

  submitCheckin() {
    const { title, content } = this.data.checkinForm;

    if (!title) {
      wx.showToast({ title: '请输入打卡标题', icon: 'none' });
      return;
    }

    if (title.length > 50) {
      wx.showToast({ title: '标题不能超过50字', icon: 'none' });
      return;
    }

    if (!content) {
      wx.showToast({ title: '请输入打卡内容', icon: 'none' });
      return;
    }

    if (content.length > 500) {
      wx.showToast({ title: '内容不能超过500字', icon: 'none' });
      return;
    }

    wx.showLoading({ title: '提交中...' });

    const app = getApp();
    wx.request({
      url: `${API_BASE_URL}/api/checkin/topics/${this.data.topicId}/checkin`,
      method: 'POST',
      header: {
        'Authorization': 'Bearer ' + app.globalData.token,
        'Content-Type': 'application/json'
      },
      data: {
        title,
        content
      },
      success: (res) => {
        wx.hideLoading();
        if (res.data && res.data.error) {
          wx.showToast({ title: res.data.error, icon: 'none' });
        } else {
          this.setData({
            showCheckinModal: false,
            showSuccessModal: true,
            checkinResult: res.data.record
          });
          this.loadTopicDetail();
          this.loadCheckinRecords();
        }
      },
      fail: (err) => {
        wx.hideLoading();
        wx.showToast({ title: '网络错误', icon: 'none' });
        console.error('打卡失败:', err);
      }
    });
  },

  hideSuccessModal() {
    this.setData({ showSuccessModal: false });
  },

  showEditModal() {
    if (!this.data.isTopicCreator) {
      wx.showToast({ title: '无权限编辑', icon: 'none' });
      return;
    }
    
    this.setData({
      showEditModal: true,
      editForm: {
        title: this.data.topic.title || '',
        description: this.data.topic.description || ''
      }
    });
  },

  hideEditModal() {
    this.setData({ showEditModal: false });
  },

  onEditTitleInput(e) {
    this.setData({
      'editForm.title': e.detail.value
    });
  },

  onEditDescriptionInput(e) {
    this.setData({
      'editForm.description': e.detail.value
    });
  },

  saveEditTopic() {
    const { title, description } = this.data.editForm;

    if (!title) {
      wx.showToast({ title: '请输入主题标题', icon: 'none' });
      return;
    }

    if (title.length < 10) {
      wx.showToast({ title: '主题标题至少需要 10 个字符', icon: 'none' });
      return;
    }

    if (title.length > 100) {
      wx.showToast({ title: '主题标题不能超过 100 个字符', icon: 'none' });
      return;
    }

    wx.showLoading({ title: '保存中...' });

    const app = getApp();
    wx.request({
      url: `${API_BASE_URL}/api/checkin/topics/${this.data.topicId}`,
      method: 'PUT',
      header: {
        'Authorization': 'Bearer ' + app.globalData.token,
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
          wx.showToast({ title: '保存成功', icon: 'success' });
          this.setData({ showEditModal: false });
          this.loadTopicDetail();
        }
      },
      fail: (err) => {
        wx.hideLoading();
        wx.showToast({ title: '保存失败，请重试', icon: 'none' });
        console.error('保存主题失败:', err);
      }
    });
  }
});