const { API_BASE_URL } = require('../../utils/apiConfig');

Page({
  data: {
    records: [],
    groupedRecords: [],
    filterMode: 'month', // year 或 month
    selectedDate: '',
    loadingMore: false,
    hasMoreData: true,
    page: 1,
    pageSize: 10,
    showDetailModal: false,
    selectedRecord: {}
  },
  onLoad: function() {
    // 检查是否已登录
    const app = getApp();
    if (!app.checkLogin()) {
      wx.redirectTo({ url: '/pages/login/login' });
      return;
    }
    
    // 设置默认日期为当前月份
    const now = new Date();
    const year = now.getFullYear();
    const month = (now.getMonth() + 1).toString().padStart(2, '0');
    const defaultDate = `${year}-${month}`;
    this.setData({ selectedDate: defaultDate });
    
    // 加载记录
    this.loadRecords(true);
  },
  onShow: function() {
    // 页面显示时重新加载记录
    this.loadRecords(true);
  },
  onReachBottom: function() {
    // 滚动到底部时加载更多数据
    if (!this.data.loadingMore && this.data.hasMoreData) {
      this.loadRecords(false);
    }
  },
  // 切换筛选模式
  switchFilterMode: function(e) {
    const mode = e.currentTarget.dataset.mode;
    this.setData({ 
      filterMode: mode,
      page: 1,
      hasMoreData: true
    });
    this.loadRecords(true);
  },
  // 日期选择器变化
  bindDateChange: function(e) {
    const date = e.detail.value;
    this.setData({ 
      selectedDate: date,
      page: 1,
      hasMoreData: true
    });
    this.loadRecords(true);
  },
  // 加载记录
  loadRecords: function(reset) {
    if (reset) {
      wx.showLoading({ title: '加载中...' });
      this.setData({ page: 1, hasMoreData: true });
    } else {
      this.setData({ loadingMore: true });
    }
    
    const app = getApp();
    const { filterMode, selectedDate, page, pageSize } = this.data;
    
    // 构建请求参数
    let url = `${API_BASE_URL}/api/checkin/records`;
    if (filterMode === 'month') {
      const [year, month] = selectedDate.split('-');
      url += `?year=${year}&month=${month}`;
    } else {
      // 提取年份部分
      const year = selectedDate.split('-')[0];
      url += `?year=${year}`;
    }
    url += `&page=${page}&pageSize=${pageSize}`;
    
    wx.request({
      url: url,
      method: 'GET',
      header: {
        'Authorization': 'Bearer ' + app.globalData.token
      },
      success: (res) => {
        if (reset) {
          wx.hideLoading();
        } else {
          this.setData({ loadingMore: false });
        }
        
        if (res.data.error) {
          wx.showToast({ title: res.data.error, icon: 'none' });
        } else {
          let newRecords = res.data.records || [];
          if (reset) {
            this.setData({ records: newRecords });
          } else {
            this.setData({ records: [...this.data.records, ...newRecords] });
          }
          
          // 检查是否还有更多数据
          if (newRecords.length < pageSize) {
            this.setData({ hasMoreData: false });
          } else {
            this.setData({ page: page + 1 });
          }
          
          // 如果是年视图，按月份分组
          if (filterMode === 'year') {
            this.groupRecordsByMonth(this.data.records);
          }
        }
      },
      fail: (err) => {
        if (reset) {
          wx.hideLoading();
        } else {
          this.setData({ loadingMore: false });
        }
        wx.showToast({ title: '网络错误', icon: 'none' });
      }
    });
  },
  // 按月份分组记录
  groupRecordsByMonth: function(records) {
    const grouped = {};
    
    records.forEach(record => {
      const date = new Date(record.checkinTime);
      const month = date.getMonth() + 1;
      
      if (!grouped[month]) {
        grouped[month] = {
          month: month,
          records: []
        };
      }
      
      grouped[month].records.push(record);
    });
    
    // 转换为数组并按月份排序
    const groupedArray = Object.values(grouped).sort((a, b) => a.month - b.month);
    this.setData({ groupedRecords: groupedArray });
  },
  
  // 显示记录详情弹窗
  showRecordDetail: function(e) {
    const record = e.currentTarget.dataset.record;
    this.setData({
      selectedRecord: record,
      showDetailModal: true
    });
  },
  
  // 关闭记录详情弹窗
  closeDetailModal: function() {
    this.setData({
      showDetailModal: false,
      selectedRecord: {}
    });
  },
  
  // 阻止事件冒泡
  stopPropagation: function() {
    // 空方法，用于阻止事件冒泡
  }
})