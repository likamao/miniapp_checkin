const { API_BASE_URL } = require('../../utils/apiConfig');
const { getValidToken, validateCheckinForm, cleanObject } = require('../../utils/validate');

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
    const token = wx.getStorageSync('token');
    if (!token) {
      return;
    }
    
    wx.request({
      url: `${API_BASE_URL}/api/users/me`,
      method: 'GET',
      header: {
        'Authorization': 'Bearer ' + token
      },
      success: (res) => {
        if (res.data && res.data.user) {
          this.setData({
            currentUserId: res.data.user.user?.id
          });
        }
      },
      fail: (err) => {
        console.error('获取用户信息失败:', err);
      }
    });
  },

  loadTopicDetail() {
    // 获取有效的 token
    const token = getValidToken();
    
    // 构建请求头
    const header = {};
    if (token) {
      header['Authorization'] = 'Bearer ' + token;
    }
    
    wx.request({
      url: `${API_BASE_URL}/api/checkin/topics/${this.data.topicId}`,
      method: 'GET',
      header: header,
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
    // 获取有效的 token
    const token = getValidToken();
    
    // 构建请求头
    const header = {};
    if (token) {
      header['Authorization'] = 'Bearer ' + token;
    }
    
    wx.request({
      url: `${API_BASE_URL}/api/checkin/topics/${this.data.topicId}/checkin-records`,
      method: 'GET',
      header: header,
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
    // 检查登录状态
    const app = getApp();
    if (!app.checkLogin()) {
      wx.showModal({
        title: '提示',
        content: '请先登录以使用此功能',
        confirmText: '去登录',
        success: (res) => {
          if (res.confirm) {
            wx.navigateTo({ url: '/pages/login/login' });
          }
        }
      });
      return;
    }
    
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

    // 表单验证
    const formData = { title, content };
    const validationResult = validateCheckinForm(formData);
    if (!validationResult.isValid) {
      wx.showToast({ title: validationResult.error, icon: 'none' });
      return;
    }

    wx.showLoading({ title: '提交中...' });

    // 获取有效的 token
    const token = getValidToken();
    if (!token) {
      wx.hideLoading();
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }

    // 清理数据，确保不会发送无效参数
    const cleanedData = cleanObject({ title, content });

    wx.request({
      url: `${API_BASE_URL}/api/checkin/topics/${this.data.topicId}/checkin`,
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

    wx.request({
      url: `${API_BASE_URL}/api/checkin/topics/${this.data.topicId}`,
      method: 'PUT',
      header: {
        'Authorization': 'Bearer ' + wx.getStorageSync('token'),
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