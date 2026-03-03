const apiConfig = require('../../utils/apiConfig');

Page({
  data: {
    reportType: 'weekly', // weekly 或 monthly
    topics: [],
    selectedTopicIndex: 0,
    selectedTopic: null,
    reportData: null,
    loading: false,
    error: null,
    reportTitle: '打卡报告',
    dateRange: '',
    shareImageUrl: '',
    shareTopicId: ''
  },

  onLoad: function(options) {
    // 接收从square页面或分享链接传递过来的参数
    if (options.reportType) {
      this.setData({
        reportType: options.reportType
      });
    }
    // 保存topicId参数，用于后续加载主题
    if (options.topicId) {
      this.setData({
        shareTopicId: options.topicId
      });
    }
    // 检查用户权限
    this.checkUserPermissions();
  },

  // 检查用户权限
  checkUserPermissions: function() {
    const token = wx.getStorageSync('token');
    if (!token) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      wx.navigateTo({ url: '/pages/login/login' });
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
          this.setData({
            user: res.data.user
          });
          // 加载主题列表
          this.loadTopics();
        } else {
          wx.showToast({ title: '获取用户信息失败', icon: 'none' });
          wx.navigateTo({ url: '/pages/login/login' });
        }
      },
      fail: (err) => {
        console.error('获取用户权限失败:', err);
        wx.showToast({ title: '网络错误，请重试', icon: 'none' });
        wx.navigateBack();
      }
    });
  },

  // 加载主题列表
  loadTopics: function() {
    const token = wx.getStorageSync('token');
    wx.request({
      url: apiConfig.API_BASE_URL + '/api/checkin/topics',
      method: 'GET',
      header: {
        'Authorization': 'Bearer ' + token
      },
      success: (res) => {
        if (res.data && res.data.topics) {
          // 为每个主题检查用户是否具有查看报告的权限
          const topicsWithPermission = res.data.topics.map(topic => {
            const currentUserId = this.data.user.user.id;
            const hasAdminRole = this.data.user.roles && this.data.user.roles.includes('ADMIN');
            const isTopicCreator = topic.createdBy === currentUserId;
            const canViewReport = hasAdminRole || isTopicCreator;
            return {
              ...topic,
              canViewReport
            };
          });
          
          // 过滤出用户有权限查看的主题
          const accessibleTopics = topicsWithPermission.filter(topic => topic.canViewReport);
          
          this.setData({
            topics: accessibleTopics
          });
          
          if (accessibleTopics.length > 0) {
            let selectedTopic = accessibleTopics[0];
            let selectedIndex = 0;
            
            // 如果有分享的topicId，尝试选择对应的主题
            if (this.data.shareTopicId) {
              const sharedTopic = accessibleTopics.find(topic => topic.id == this.data.shareTopicId);
              if (sharedTopic) {
                selectedTopic = sharedTopic;
                selectedIndex = accessibleTopics.indexOf(sharedTopic);
              }
            }
            
            this.setData({
              selectedTopic: selectedTopic,
              selectedTopicIndex: selectedIndex
            });
            this.loadReportData();
          } else {
            this.setData({
              error: '您没有查看任何主题报告的权限'
            });
          }
        } else {
          this.setData({
            error: '加载主题列表失败，请重试'
          });
        }
      },
      fail: (err) => {
        console.error('加载主题列表失败:', err);
        this.setData({
          error: '加载主题列表失败，请重试'
        });
      }
    });
  },

  // 选择报告类型
  selectReportType: function(e) {
    const type = e.currentTarget.dataset.type;
    this.setData({
      reportType: type,
      reportData: null
    });
    if (this.data.selectedTopic) {
      this.loadReportData();
    }
  },

  // 主题选择器变化
  bindTopicChange: function(e) {
    const index = e.detail.value;
    const selectedTopic = this.data.topics[index];
    this.setData({
      selectedTopicIndex: index,
      selectedTopic: selectedTopic,
      reportData: null
    });
    this.loadReportData();
  },

  // 加载报告数据
  loadReportData: function() {
    if (!this.data.selectedTopic) return;

    this.setData({
      loading: true,
      error: null,
      reportData: null // 清空之前的数据，避免显示旧数据
    });

    const token = wx.getStorageSync('token');
    const topicId = this.data.selectedTopic.id;
    const endpoint = this.data.reportType === 'weekly' 
      ? `/api/checkin/topics/${topicId}/weekly-report` 
      : `/api/checkin/topics/${topicId}/monthly-report`;

    wx.request({
      url: apiConfig.API_BASE_URL + endpoint,
      method: 'GET',
      header: {
        'Authorization': 'Bearer ' + token
      },
      success: (res) => {
        if (res.data && res.data.error) {
          // 处理权限错误
          if (res.data.error.includes('权限不足')) {
            this.setData({
              error: '您没有权限查看此主题的报告',
              loading: false
            });
          } else {
            this.setData({
              error: res.data.error,
              loading: false
            });
          }
        } else if (res.data && res.data.report) {
          // 处理报告数据，计算用户活跃度百分比和平均打卡次数
          const reportData = res.data.report;
          console.log('完整报告数据:', reportData);
          
          // 检查数据完整性
          if (!reportData.metrics) {
            reportData.metrics = {
              totalCheckinCount: 0,
              participantCount: 0,
              averageCheckinCount: 0,
              userActivityRate: 0
            };
          }
          
          if (!reportData.heatmapData) {
            reportData.heatmapData = [];
          }
          
          if (!reportData.trendData) {
            reportData.trendData = {
              dates: [],
              checkinCounts: []
            };
          }
          
          // 计算格式化数据
          reportData.metrics.userActivityRatePercent = Math.round(reportData.metrics.userActivityRate * 100);
          reportData.metrics.averageCheckinCountFormatted = reportData.metrics.averageCheckinCount.toFixed(1);
          
          this.setData({
            reportData: reportData,
            loading: false,
            reportTitle: `${this.data.selectedTopic.title}${this.data.reportType === 'weekly' ? '周报' : '月报'}`,
            dateRange: `${reportData.startDate} 至 ${reportData.endDate}`
          });
          
          // 延迟绘制图表，确保数据已更新
          setTimeout(() => {
            this.drawCharts();
          }, 100);
        } else {
          this.setData({
            error: '获取报告数据失败',
            loading: false
          });
        }
      },
      fail: (err) => {
        console.error('获取报告数据失败:', err);
        this.setData({
          error: '获取报告数据失败，请重试',
          loading: false
        });
      }
    });
  },

  // 绘制图表
  drawCharts: function() {
    if (!this.data.reportData) return;

    // 绘制趋势图
    this.drawTrendChart();
    // 绘制热力日历
    this.drawHeatmapChart();
  },

  // 绘制趋势图
  drawTrendChart: function() {
    const trendData = this.data.reportData.trendData;
    if (!trendData) return;

    // 获取canvas上下文和实际尺寸
    const query = wx.createSelectorQuery();
    query.select('#trendChart').boundingClientRect();
    query.exec((res) => {
      if (!res || !res[0]) return;
      
      const canvasWidth = res[0].width;
      const canvasHeight = res[0].height;
      const padding = 40;
      
      const ctx = wx.createCanvasContext('trendChart');

      // 绘制背景
      ctx.setFillStyle('#ffffff');
      ctx.fillRect(0, 0, canvasWidth, canvasHeight);

      // 绘制标题
      ctx.setFontSize(16);
      ctx.setFillStyle('#333333');
      ctx.setTextAlign('center');
      ctx.fillText('打卡趋势', canvasWidth / 2, 20);

      // 计算数据范围
      const maxCount = Math.max(...trendData.checkinCounts, 1);
      const dataPoints = trendData.dates.length;
      const stepX = (canvasWidth - 2 * padding) / (dataPoints - 1);
      const stepY = (canvasHeight - 2 * padding) / maxCount;

      // 绘制坐标轴
      ctx.setStrokeStyle('#e8e8e8');
      ctx.setLineWidth(1);
      // X轴
      ctx.beginPath();
      ctx.moveTo(padding, canvasHeight - padding);
      ctx.lineTo(canvasWidth - padding, canvasHeight - padding);
      ctx.stroke();
      // Y轴
      ctx.beginPath();
      ctx.moveTo(padding, padding);
      ctx.lineTo(padding, canvasHeight - padding);
      ctx.stroke();

      // 绘制数据点和连线
      ctx.setStrokeStyle('#1989fa');
      ctx.setLineWidth(2);
      ctx.beginPath();
      
      trendData.checkinCounts.forEach((count, index) => {
        const x = padding + index * stepX;
        const y = canvasHeight - padding - count * stepY;
        
        if (index === 0) {
          ctx.moveTo(x, y);
        } else {
          ctx.lineTo(x, y);
        }
        
        // 绘制数据点
        ctx.setFillStyle('#1989fa');
        ctx.beginPath();
        ctx.arc(x, y, 3, 0, 2 * Math.PI);
        ctx.fill();
      });
      ctx.stroke();

      // 绘制日期标签
      ctx.setFontSize(10);
      ctx.setFillStyle('#666666');
      ctx.setTextAlign('center');
      trendData.dates.forEach((date, index) => {
        const x = padding + index * stepX;
        ctx.fillText(date, x, canvasHeight - padding + 15);
      });

      ctx.draw();
    });
  },

  // 绘制热力日历
  drawHeatmapChart: function() {
    const heatmapData = this.data.reportData.heatmapData;
    if (!heatmapData) return;
    console.log('热力图数据:', heatmapData);
    console.log('热力图数据长度:', heatmapData.length);
    // 检查是否有打卡次数大于0的数据
    const hasData = heatmapData.some(item => item.checkinCount > 0);
    console.log('是否有打卡数据:', hasData);
    // 打印前几个数据项
    console.log('前5个数据项:', heatmapData.slice(0, 5));

    // 获取canvas上下文和实际尺寸
    const query = wx.createSelectorQuery();
    query.select('#heatmapChart').boundingClientRect();
    query.exec((res) => {
      if (!res || !res[0]) return;
      
      const canvasWidth = res[0].width;
      const canvasHeight = res[0].height;
      const padding = 30;
      
      const ctx = wx.createCanvasContext('heatmapChart');

      // 绘制背景
      ctx.setFillStyle('#ffffff');
      ctx.fillRect(0, 0, canvasWidth, canvasHeight);

      // 绘制标题
      ctx.setFontSize(16);
      ctx.setFillStyle('#333333');
      ctx.setTextAlign('center');
      ctx.fillText('打卡热力图', canvasWidth / 2, 20);

      // 计算网格大小
      const cellSize = (canvasWidth - 2 * padding) / 7;
      const rows = Math.ceil(heatmapData.length / 7);

      // 定义颜色梯度
      const colors = ['#e8e8e8', '#c0e6ff', '#73c0de', '#1989fa', '#005cc5'];
      
      // 绘制热力网格
      heatmapData.forEach((item, index) => {
        const row = Math.floor(index / 7);
        const col = index % 7;
        const x = padding + col * cellSize;
        const y = padding + row * cellSize;
        
        // 根据打卡次数选择颜色
        const checkinCount = item.checkinCount;
        console.log('当前日期打卡次数:', item.date, checkinCount);
        let colorIndex = 0;
        if (checkinCount > 0) {
          colorIndex = Math.min(Math.floor(checkinCount / 1), colors.length - 1);
          // 确保至少使用第二种颜色（不是灰色）
          if (colorIndex === 0) {
            colorIndex = 1;
          }
        }
        
        ctx.setFillStyle(colors[colorIndex]);
        ctx.fillRect(x, y, cellSize - 2, cellSize - 2);
        
        // 绘制日期
        ctx.setFontSize(10);
        ctx.setFillStyle('#333333');
        ctx.setTextAlign('center');
        ctx.fillText(item.date.substring(8), x + cellSize / 2, y + cellSize / 2 + 4);
      });

      ctx.draw();
    });
  },

  // 生成报告图片
  generateReportImage: function() {
    if (!this.data.reportData) {
      wx.showToast({ title: '请先加载报告数据', icon: 'none' });
      return;
    }

    wx.showToast({
      title: '生成中...',
      icon: 'loading',
      duration: 10000
    });

    // 获取屏幕宽度，用于计算canvas尺寸
    const windowInfo = wx.getWindowInfo ? wx.getWindowInfo() : wx.getSystemInfoSync();
    const screenWidth = windowInfo.windowWidth || windowInfo.screenWidth;
    const canvasWidth = screenWidth; // 使用全屏宽度
    const canvasHeight = 2500; // 增加高度，确保足够容纳用户详细打卡记录

    // 创建canvas上下文
    const canvasId = 'reportCanvas';
    const ctx = wx.createCanvasContext(canvasId);

    // 绘制背景
    ctx.setFillStyle('#ffffff');
    ctx.fillRect(0, 0, canvasWidth, canvasHeight);

    // 绘制标题
    ctx.setFontSize(24);
    ctx.setFillStyle('#333333');
    ctx.setTextAlign('center');
    ctx.fillText(this.data.reportTitle, canvasWidth / 2, 60);

    // 绘制日期范围
    ctx.setFontSize(16);
    ctx.setFillStyle('#666666');
    ctx.fillText(this.data.dateRange, canvasWidth / 2, 90);

    // 绘制统计数据
    const metrics = this.data.reportData.metrics;
    const metricItems = [
      { label: '总打卡次数', value: metrics.totalCheckinCount },
      { label: '参与用户数', value: metrics.participantCount },
      { label: '平均打卡次数', value: metrics.averageCheckinCountFormatted },
      { label: '用户活跃度', value: metrics.userActivityRatePercent + '%' }
    ];

    let yPosition = 140;
    metricItems.forEach((item, index) => {
      ctx.setFontSize(16);
      ctx.setFillStyle('#333333');
      ctx.setTextAlign('left');
      ctx.fillText(item.label, 20, yPosition);
      
      ctx.setFontSize(18);
      ctx.setFillStyle('#1989fa');
      ctx.setTextAlign('right');
      ctx.fillText(item.value, canvasWidth - 20, yPosition);
      
      yPosition += 50;
    });

    // 绘制趋势图
    const trendData = this.data.reportData.trendData;
    if (trendData && trendData.dates && trendData.dates.length > 0) {
      // 绘制趋势图区域
      ctx.setStrokeStyle('#e8e8e8');
      ctx.setLineWidth(1);
      ctx.strokeRect(20, yPosition, canvasWidth - 40, 200);
      ctx.setFontSize(14);
      ctx.setFillStyle('#666666');
      ctx.setTextAlign('center');
      ctx.fillText('打卡趋势图', canvasWidth / 2, yPosition + 20);
      
      // 计算数据范围
      const maxCount = Math.max(...trendData.checkinCounts, 1);
      const dataPoints = trendData.dates.length;
      const chartWidth = canvasWidth - 80;
      const chartHeight = 160;
      const stepX = chartWidth / (dataPoints - 1);
      const stepY = chartHeight / maxCount;
      
      // 绘制坐标轴
      ctx.setStrokeStyle('#e8e8e8');
      ctx.setLineWidth(1);
      // X轴
      ctx.beginPath();
      ctx.moveTo(40, yPosition + 180);
      ctx.lineTo(canvasWidth - 40, yPosition + 180);
      ctx.stroke();
      // Y轴
      ctx.beginPath();
      ctx.moveTo(40, yPosition + 20);
      ctx.lineTo(40, yPosition + 180);
      ctx.stroke();
      
      // 绘制数据点和连线
      ctx.setStrokeStyle('#1989fa');
      ctx.setLineWidth(2);
      ctx.beginPath();
      
      trendData.checkinCounts.forEach((count, index) => {
        const x = 40 + index * stepX;
        const y = yPosition + 180 - count * stepY;
        
        if (index === 0) {
          ctx.moveTo(x, y);
        } else {
          ctx.lineTo(x, y);
        }
        
        // 绘制数据点
        ctx.setFillStyle('#1989fa');
        ctx.beginPath();
        ctx.arc(x, y, 3, 0, 2 * Math.PI);
        ctx.fill();
      });
      ctx.stroke();
      
      // 绘制日期标签
      ctx.setFontSize(10);
      ctx.setFillStyle('#666666');
      ctx.setTextAlign('center');
      trendData.dates.forEach((date, index) => {
        const x = 40 + index * stepX;
        ctx.fillText(date, x, yPosition + 195);
      });
    } else {
      // 绘制空趋势图区域
      ctx.setStrokeStyle('#e8e8e8');
      ctx.setLineWidth(1);
      ctx.strokeRect(20, yPosition, canvasWidth - 40, 200);
      ctx.setFontSize(14);
      ctx.setFillStyle('#666666');
      ctx.setTextAlign('center');
      ctx.fillText('打卡趋势图', canvasWidth / 2, yPosition + 20);
      ctx.fillText('暂无数据', canvasWidth / 2, yPosition + 120);
    }

    // 绘制热力图
    const heatmapData = this.data.reportData.heatmapData;
    if (heatmapData && heatmapData.length > 0) {
      yPosition += 220;
      // 绘制热力图区域
      ctx.setStrokeStyle('#e8e8e8');
      ctx.setLineWidth(1);
      ctx.strokeRect(20, yPosition, canvasWidth - 40, 200);
      ctx.setFontSize(14);
      ctx.setFillStyle('#666666');
      ctx.setTextAlign('center');
      ctx.fillText('打卡热力图', canvasWidth / 2, yPosition + 20);
      
      // 计算网格大小
      const cellSize = (canvasWidth - 80) / 7;
      
      // 定义颜色梯度
      const colors = ['#e8e8e8', '#c0e6ff', '#73c0de', '#1989fa', '#005cc5'];
      
      // 绘制热力网格
      heatmapData.forEach((item, index) => {
        const row = Math.floor(index / 7);
        const col = index % 7;
        const x = 40 + col * cellSize;
        const y = yPosition + 40 + row * cellSize;
        
        // 根据打卡次数选择颜色
        const checkinCount = item.checkinCount;
        let colorIndex = 0;
        if (checkinCount > 0) {
          colorIndex = Math.min(Math.floor(checkinCount / 1), colors.length - 1);
          // 确保至少使用第二种颜色（不是灰色）
          if (colorIndex === 0) {
            colorIndex = 1;
          }
        }
        
        ctx.setFillStyle(colors[colorIndex]);
        ctx.fillRect(x, y, cellSize - 2, cellSize - 2);
        
        // 绘制日期
        ctx.setFontSize(10);
        ctx.setFillStyle('#333333');
        ctx.setTextAlign('center');
        ctx.fillText(item.date.substring(8), x + cellSize / 2, y + cellSize / 2 + 4);
      });
    } else {
      yPosition += 220;
      // 绘制空热力图区域
      ctx.setStrokeStyle('#e8e8e8');
      ctx.setLineWidth(1);
      ctx.strokeRect(20, yPosition, canvasWidth - 40, 200);
      ctx.setFontSize(14);
      ctx.setFillStyle('#666666');
      ctx.setTextAlign('center');
      ctx.fillText('打卡热力图', canvasWidth / 2, yPosition + 20);
      ctx.fillText('暂无数据', canvasWidth / 2, yPosition + 120);
    }

    // 绘制用户列表
    yPosition += 220;
    ctx.setFontSize(16);
    ctx.setFillStyle('#333333');
    ctx.setTextAlign('left');
    ctx.fillText('用户打卡详情', 20, yPosition);

    yPosition += 30;
    const userDetails = this.data.reportData.userDetails;
    if (userDetails && userDetails.users && userDetails.users.length > 0) {
      const users = userDetails.users.slice(0, 5); // 只显示前5名
      users.forEach((user, index) => {
        // 绘制用户基本信息（排名和用户ID）
        ctx.setFontSize(14);
        ctx.setFillStyle('#666666');
        ctx.setTextAlign('left');
        ctx.fillText(`${index + 1}. 用户${user.userId}`, 20, yPosition);
        
        yPosition += 25;
        
        // 绘制用户统计信息（打卡次数和连续天数）
        ctx.setFontSize(12);
        ctx.setFillStyle('#666666');
        ctx.setTextAlign('left');
        ctx.fillText(`${user.checkinCount}次 | 连续${user.maxConsecutiveDays}天`, 40, yPosition);
        
        yPosition += 30;
        
        // 检查用户隐私设置，只有当允许显示详情时才绘制
        if (user.allowStatsDisplay !== false && user.checkinDetails && user.checkinDetails.length > 0) {
          user.checkinDetails.forEach((checkin, checkinIndex) => {
            // 绘制打卡日期
            ctx.setFontSize(11);
            ctx.setFillStyle('#999999');
            ctx.setTextAlign('left');
            ctx.fillText(`打卡时间: ${checkin.checkinDate}`, 60, yPosition);
            yPosition += 18;
            
            // 绘制打卡标题
            if (checkin.title) {
              ctx.setFontSize(11);
              ctx.setFillStyle('#333333');
              ctx.setTextAlign('left');
              ctx.fillText(`标题: ${checkin.title}`, 60, yPosition);
              yPosition += 18;
            }
            
            // 绘制打卡描述
            if (checkin.content) {
              ctx.setFontSize(11);
              ctx.setFillStyle('#666666');
              ctx.setTextAlign('left');
              // 限制描述长度，避免过长
              const content = checkin.content.length > 50 ? checkin.content.substring(0, 50) + '...' : checkin.content;
              ctx.fillText(`描述: ${content}`, 60, yPosition);
              yPosition += 18;
            }
          });
        } else if (user.allowStatsDisplay === false) {
          // 显示隐私保护提示
          ctx.setFontSize(11);
          ctx.setFillStyle('#999999');
          ctx.setTextAlign('left');
          ctx.fillText('该用户已设置隐私保护，隐藏打卡详情', 60, yPosition);
          yPosition += 18;
        }
        
        // 添加用户之间的分隔线
        ctx.setStrokeStyle('#e8e8e8');
        ctx.setLineWidth(1);
        ctx.beginPath();
        ctx.moveTo(20, yPosition);
        ctx.lineTo(canvasWidth - 20, yPosition);
        ctx.stroke();
        yPosition += 20;
      });
    } else {
      ctx.setFontSize(14);
      ctx.setFillStyle('#666666');
      ctx.setTextAlign('center');
      ctx.fillText('暂无用户数据', canvasWidth / 2, yPosition);
      yPosition += 35;
    }

    // 绘制底部信息
    ctx.setFontSize(14);
    ctx.setFillStyle('#999999');
    ctx.setTextAlign('center');
    ctx.fillText('打卡系统报告', canvasWidth / 2, yPosition + 30);

    // 绘制完成，转换为图片
    ctx.draw(false, () => {
      wx.canvasToTempFilePath({
        canvasId: canvasId,
        success: (res) => {
          wx.hideToast();
          // 保存图片到相册
          wx.saveImageToPhotosAlbum({
            filePath: res.tempFilePath,
            success: () => {
              wx.showToast({ title: '图片已保存到相册', icon: 'success' });
            },
            fail: (err) => {
              console.error('保存图片失败:', err);
              wx.showToast({ title: '保存图片失败，请重试', icon: 'none' });
            }
          });
        },
        fail: (err) => {
          console.error('生成图片失败:', err);
          wx.hideToast();
          wx.showToast({ title: '生成图片失败，请重试', icon: 'none' });
        }
      });
    });
  },

  // 分享报告
  shareReport: function() {
    if (!this.data.reportData) {
      wx.showToast({ title: '请先加载报告数据', icon: 'none' });
      return;
    }

    // 生成分享图片
    this.generateShareImage();
  },

  // 生成分享图片
  generateShareImage: function() {
    wx.showToast({
      title: '生成分享图片中...',
      icon: 'loading',
      duration: 5000
    });

    // 获取屏幕宽度，用于计算canvas尺寸
    const windowInfo = wx.getWindowInfo ? wx.getWindowInfo() : wx.getSystemInfoSync();
    const screenWidth = windowInfo.windowWidth || windowInfo.screenWidth;
    const canvasWidth = screenWidth * 0.8;
    const canvasHeight = canvasWidth * 1.2; // 增加高度以容纳更多内容

    // 创建canvas上下文
    const canvasId = 'shareCanvas';
    const ctx = wx.createCanvasContext(canvasId);

    // 绘制背景
    ctx.setFillStyle('#ffffff');
    ctx.fillRect(0, 0, canvasWidth, canvasHeight);

    // 绘制标题
    ctx.setFontSize(20);
    ctx.setFillStyle('#333333');
    ctx.setTextAlign('center');
    ctx.fillText(this.data.reportTitle, canvasWidth / 2, 40);

    // 绘制日期范围
    ctx.setFontSize(14);
    ctx.setFillStyle('#666666');
    ctx.fillText(this.data.dateRange, canvasWidth / 2, 65);

    // 绘制统计数据
    const metrics = this.data.reportData.metrics;
    const metricItems = [
      { label: '总打卡次数', value: metrics.totalCheckinCount },
      { label: '参与用户数', value: metrics.participantCount },
      { label: '平均打卡次数', value: metrics.averageCheckinCountFormatted },
      { label: '用户活跃度', value: metrics.userActivityRatePercent + '%' }
    ];

    let yPosition = 100;
    metricItems.forEach((item, index) => {
      ctx.setFontSize(14);
      ctx.setFillStyle('#333333');
      ctx.setTextAlign('left');
      ctx.fillText(item.label, canvasWidth * 0.2, yPosition);
      
      ctx.setFontSize(14);
      ctx.setFillStyle('#1989fa');
      ctx.setTextAlign('right');
      ctx.fillText(item.value, canvasWidth * 0.8, yPosition);
      
      yPosition += 25;
    });

    // 绘制趋势图
    const trendData = this.data.reportData.trendData;
    if (trendData && trendData.dates && trendData.dates.length > 0) {
      // 绘制趋势图区域
      ctx.setStrokeStyle('#e8e8e8');
      ctx.setLineWidth(1);
      ctx.strokeRect(canvasWidth * 0.1, yPosition, canvasWidth * 0.8, 100);
      ctx.setFontSize(12);
      ctx.setFillStyle('#666666');
      ctx.setTextAlign('center');
      ctx.fillText('打卡趋势', canvasWidth / 2, yPosition + 15);
      
      // 计算数据范围
      const maxCount = Math.max(...trendData.checkinCounts, 1);
      const dataPoints = trendData.dates.length;
      const chartWidth = canvasWidth * 0.7;
      const chartHeight = 70;
      const stepX = chartWidth / (dataPoints - 1);
      const stepY = chartHeight / maxCount;
      
      // 绘制数据点和连线
      ctx.setStrokeStyle('#1989fa');
      ctx.setLineWidth(2);
      ctx.beginPath();
      
      trendData.checkinCounts.forEach((count, index) => {
        const x = canvasWidth * 0.15 + index * stepX;
        const y = yPosition + 85 - count * stepY;
        
        if (index === 0) {
          ctx.moveTo(x, y);
        } else {
          ctx.lineTo(x, y);
        }
        
        // 绘制数据点
        ctx.setFillStyle('#1989fa');
        ctx.beginPath();
        ctx.arc(x, y, 2, 0, 2 * Math.PI);
        ctx.fill();
      });
      ctx.stroke();
    }

    // 绘制底部信息
    ctx.setFontSize(12);
    ctx.setFillStyle('#999999');
    ctx.fillText('打卡系统报告', canvasWidth / 2, canvasHeight - 20);

    // 绘制完成，转换为图片
    ctx.draw(false, () => {
      wx.canvasToTempFilePath({
        canvasId: canvasId,
        success: (res) => {
          wx.hideToast();
          // 保存分享图片路径
          this.setData({
            shareImageUrl: res.tempFilePath
          });
          // 显示分享菜单
          wx.showShareMenu({
            withShareTicket: true,
            menus: ['shareAppMessage', 'shareTimeline']
          });
        },
        fail: (err) => {
          console.error('生成分享图片失败:', err);
          wx.hideToast();
          wx.showToast({ title: '生成分享图片失败，请重试', icon: 'none' });
        }
      });
    });
  },

  // 分享到好友
  onShareAppMessage: function() {
    return {
      title: this.data.reportTitle,
      path: `/pages/report/report?reportType=${this.data.reportType}&topicId=${this.data.selectedTopic ? this.data.selectedTopic.id : ''}`,
      imageUrl: this.data.shareImageUrl || '',
      success: function(res) {
        wx.showToast({ title: '分享成功', icon: 'success' });
      },
      fail: function(res) {
        wx.showToast({ title: '分享失败', icon: 'none' });
      }
    };
  },

  // 分享到朋友圈
  onShareTimeline: function() {
    return {
      title: this.data.reportTitle,
      query: `reportType=${this.data.reportType}&topicId=${this.data.selectedTopic ? this.data.selectedTopic.id : ''}`,
      imageUrl: this.data.shareImageUrl || '',
      success: function(res) {
        wx.showToast({ title: '分享成功', icon: 'success' });
      },
      fail: function(res) {
        wx.showToast({ title: '分享失败', icon: 'none' });
      }
    };
  }
});