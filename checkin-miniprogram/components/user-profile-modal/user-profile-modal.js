Component({
  properties: {
    visible: {
      type: Boolean,
      value: false
    },
    defaultNickname: {
      type: String,
      value: '微信用户'
    },
    currentNickname: {
      type: String,
      value: ''
    }
  },

  data: {
    nickname: '',
    originalNickname: '',
    isModified: false
  },

  observers: {
    'visible': function(newVal) {
      if (newVal) {
        const initialNickname = this.properties.currentNickname || '';
        this.setData({
          nickname: initialNickname,
          originalNickname: initialNickname,
          isModified: false
        });
      }
    }
  },

  methods: {
    stopPropagation() {
    },

    onNicknameInput(e) {
      const newNickname = e.detail.value;
      const isModified = newNickname !== this.data.originalNickname;
      this.setData({ 
        nickname: newNickname,
        isModified: isModified
      });
    },

    onClose() {
      // 如果用户没有修改昵称，直接关闭不触发事件
      if (!this.data.isModified) {
        this.triggerEvent('close', {
          useDefault: false,
          nickname: null,
          isModified: false
        });
        return;
      }
      
      // 用户有修改，触发关闭事件
      this.triggerEvent('close', {
        useDefault: true,
        nickname: this.data.nickname || this.properties.defaultNickname,
        isModified: true
      });
    },

    onConfirm(e) {
      const nickname = this.data.nickname || this.properties.defaultNickname;
      
      this.createRipple(e);
      
      setTimeout(() => {
        this.triggerEvent('confirm', {
          nickname,
          isModified: this.data.isModified
        });
      }, 200);
    },

    createRipple(e) {
      const query = this.createSelectorQuery();
      query.select('.btn-confirm').boundingClientRect();
      query.exec((res) => {
        if (res && res[0]) {
          const rect = res[0];
          const x = e.detail.x - rect.left;
          const y = e.detail.y - rect.top;
          
          const ripple = {
            left: x + 'px',
            top: y + 'px'
          };
          
          this.setData({ ripple });
        }
      });
    }
  }
});
