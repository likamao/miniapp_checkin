Component({
  properties: {
    topic: {
      type: Object,
      value: null
    }
  },

  data: {
    canvasWidth: 750,
    canvasHeight: 1000,
    posterImage: '',
    isGenerating: false
  },

  methods: {
    async generatePoster() {
      if (this.data.isGenerating) return;
      
      this.setData({ isGenerating: true });
      
      try {
        const query = this.createSelectorQuery();
        const canvasNode = await new Promise((resolve) => {
          query.select('#posterCanvas')
            .fields({ node: true, size: true })
            .exec((res) => {
              resolve(res[0]);
            });
        });

        if (!canvasNode) {
          throw new Error('Canvas not found');
        }

        const canvas = canvasNode.node;
        const ctx = canvas.getContext('2d');
        
        const dpr = wx.getSystemInfoSync().pixelRatio;
        canvas.width = this.data.canvasWidth * dpr;
        canvas.height = this.data.canvasHeight * dpr;
        ctx.scale(dpr, dpr);

        await this.drawPoster(ctx);

        const tempFilePath = await new Promise((resolve, reject) => {
          wx.canvasToTempFilePath({
            canvas: canvas,
            width: this.data.canvasWidth,
            height: this.data.canvasHeight,
            destWidth: this.data.canvasWidth * 2,
            destHeight: this.data.canvasHeight * 2,
            fileType: 'jpg',
            quality: 1,
            success: (res) => {
              resolve(res.tempFilePath);
            },
            fail: (err) => {
              reject(err);
            }
          }, this);
        });

        this.setData({ 
          posterImage: tempFilePath,
          isGenerating: false 
        });
        
        this.triggerEvent('posterGenerated', { imagePath: tempFilePath });
        return tempFilePath;
        
      } catch (error) {
        console.error('生成分享图失败:', error);
        this.setData({ isGenerating: false });
        this.triggerEvent('posterFailed', { error: error });
        return null;
      }
    },

    async drawPoster(ctx) {
      const width = this.data.canvasWidth;
      const height = this.data.canvasHeight;
      const topic = this.properties.topic || {};

      ctx.fillStyle = '#ffffff';
      ctx.fillRect(0, 0, width, height);

      this.drawWaveBackground(ctx, width, height);
      this.drawBubbles(ctx, width, height);
      this.drawTopicContent(ctx, width, height, topic);
      this.drawQRCodePlaceholder(ctx, width, height);
    },

    drawWaveBackground(ctx, width, height) {
      this.drawWave(ctx, width, height, 0, '#e3f2fd', 0.3);
      this.drawWave(ctx, width, height, 80, '#bbdefb', 0.4);
      this.drawWave(ctx, width, height, 160, '#90caf9', 0.2);
      
      const gradient = ctx.createLinearGradient(0, 0, 0, height);
      gradient.addColorStop(0, '#e3f2fd');
      gradient.addColorStop(0.5, '#bbdefb');
      gradient.addColorStop(1, '#ffffff');
      ctx.fillStyle = gradient;
      ctx.fillRect(0, 0, width, height);
    },

    drawWave(ctx, width, height, offsetY, color, alpha) {
      ctx.save();
      ctx.globalAlpha = alpha;
      ctx.fillStyle = color;
      ctx.beginPath();
      
      const waveHeight = 40;
      const waveWidth = width / 4;
      
      ctx.moveTo(0, offsetY);
      
      for (let x = 0; x <= width; x += 10) {
        const y = offsetY + Math.sin(x / waveWidth * Math.PI * 2) * waveHeight;
        ctx.lineTo(x, y);
      }
      
      ctx.lineTo(width, height);
      ctx.lineTo(0, height);
      ctx.closePath();
      ctx.fill();
      ctx.restore();
    },

    drawBubbles(ctx, width, height) {
      const bubbles = [
        { x: 100, y: 150, r: 20, alpha: 0.4 },
        { x: 200, y: 80, r: 15, alpha: 0.3 },
        { x: width - 120, y: 200, r: 25, alpha: 0.35 },
        { x: width - 180, y: 120, r: 18, alpha: 0.25 },
        { x: width / 2, y: 180, r: 12, alpha: 0.3 }
      ];

      bubbles.forEach(bubble => {
        ctx.save();
        ctx.globalAlpha = bubble.alpha;
        
        const gradient = ctx.createRadialGradient(
          bubble.x - bubble.r * 0.3, 
          bubble.y - bubble.r * 0.3, 
          0,
          bubble.x, 
          bubble.y, 
          bubble.r
        );
        gradient.addColorStop(0, 'rgba(255, 255, 255, 0.9)');
        gradient.addColorStop(1, 'rgba(187, 222, 251, 0.3)');
        
        ctx.fillStyle = gradient;
        ctx.beginPath();
        ctx.arc(bubble.x, bubble.y, bubble.r, 0, Math.PI * 2);
        ctx.fill();
        ctx.restore();
      });
    },

    drawTopicContent(ctx, width, height, topic) {
      const title = topic.title || '打卡主题';
      const description = topic.description || '暂无描述';
      
      let currentY = 180;
      
      ctx.save();
      ctx.fillStyle = '#1565c0';
      ctx.fillRect(40, currentY, 6, 60);
      ctx.restore();

      ctx.save();
      ctx.font = 'bold 48px -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif';
      ctx.fillStyle = '#1565c0';
      ctx.textAlign = 'left';
      
      const titleX = 60;
      let titleY = currentY + 45;
      const maxTitleWidth = width - 120;
      
      this.wrapText(ctx, title, titleX, titleY, maxTitleWidth, 55);
      
      const titleLines = this.getWrappedLines(ctx, title, maxTitleWidth);
      currentY = titleY + (titleLines.length - 1) * 55 + 60;

      ctx.save();
      ctx.font = 'bold 32px -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif';
      ctx.fillStyle = '#1976d2';
      ctx.fillText('主题描述', 40, currentY);
      ctx.restore();

      currentY += 50;

      ctx.save();
      ctx.fillStyle = '#f5f5f5';
      ctx.strokeStyle = '#e3f2fd';
      ctx.lineWidth = 2;
      this.roundRect(ctx, 40, currentY - 20, width - 80, 200, 16);
      ctx.fill();
      ctx.stroke();
      ctx.restore();

      ctx.save();
      ctx.font = '28px -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif';
      ctx.fillStyle = '#64b5f6';
      ctx.textAlign = 'left';
      
      this.wrapText(ctx, description, 60, currentY + 30, width - 120, 40);
      ctx.restore();

      currentY += 220;

      ctx.save();
      ctx.fillStyle = '#e3f2fd';
      ctx.fillRect(40, currentY, width - 80, 2);
      ctx.restore();
    },

    drawQRCodePlaceholder(ctx, width, height) {
      const qrSize = 160;
      const qrX = (width - qrSize) / 2;
      const qrY = height - qrSize - 80;

      ctx.save();
      ctx.fillStyle = '#ffffff';
      ctx.strokeStyle = '#90caf9';
      ctx.lineWidth = 2;
      this.roundRect(ctx, qrX - 20, qrY - 20, qrSize + 40, qrSize + 40, 16);
      ctx.fill();
      ctx.stroke();
      ctx.restore();

      ctx.save();
      const gradient = ctx.createLinearGradient(qrX, qrY, qrX + qrSize, qrY + qrSize);
      gradient.addColorStop(0, '#e3f2fd');
      gradient.addColorStop(1, '#bbdefb');
      ctx.fillStyle = gradient;
      this.roundRect(ctx, qrX, qrY, qrSize, qrSize, 12);
      ctx.fill();
      ctx.restore();

      ctx.save();
      ctx.font = '24px -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif';
      ctx.fillStyle = '#1976d2';
      ctx.textAlign = 'center';
      ctx.fillText('扫码查看详情', width / 2, qrY + qrSize + 50);
      ctx.restore();
    },

    roundRect(ctx, x, y, width, height, radius) {
      ctx.beginPath();
      ctx.moveTo(x + radius, y);
      ctx.arcTo(x + width, y, x + width, y + height, radius);
      ctx.arcTo(x + width, y + height, x, y + height, radius);
      ctx.arcTo(x, y + height, x, y, radius);
      ctx.arcTo(x, y, x + width, y, radius);
      ctx.closePath();
    },

    wrapText(ctx, text, x, y, maxWidth, lineHeight) {
      const lines = this.getWrappedLines(ctx, text, maxWidth);
      lines.forEach((line, index) => {
        ctx.fillText(line, x, y + index * lineHeight);
      });
    },

    getWrappedLines(ctx, text, maxWidth) {
      const words = text.split('');
      const lines = [];
      let currentLine = '';

      for (let i = 0; i < words.length; i++) {
        const testLine = currentLine + words[i];
        const metrics = ctx.measureText(testLine);
        
        if (metrics.width > maxWidth && i > 0) {
          lines.push(currentLine);
          currentLine = words[i];
        } else {
          currentLine = testLine;
        }
      }
      lines.push(currentLine);
      return lines;
    }
  }
});
