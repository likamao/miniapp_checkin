const { API_BASE_URL } = require('../../utils/apiConfig');

Page({
  data: {
    topicId: null,
    topic: {},
    records: [],
    isLoading: true,
    showCheckinModal: false,
    showSuccessModal: false,
    checkinForm: {
      title: '',
      content: ''
    },
    checkinResult: {}
  },

  onLoad(options) {
    if (options.topicId) {
      this.setData({ topicId: options.topicId });
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
          this.setData({
            topic: res.data.topic,
            isLoading: false
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
  }
});