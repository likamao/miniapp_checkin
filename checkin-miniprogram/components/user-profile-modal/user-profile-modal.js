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
    nickname: ''
  },

  observers: {
    'visible': function(newVal) {
      if (newVal) {
        this.setData({
          nickname: this.properties.currentNickname || ''
        });
      }
    }
  },

  methods: {
    stopPropagation() {
    },

    onNicknameInput(e) {
      this.setData({ nickname: e.detail.value });
    },

    onClose() {
      this.triggerEvent('close', {
        useDefault: true,
        nickname: this.properties.defaultNickname
      });
    },

    onConfirm() {
      const nickname = this.data.nickname || this.properties.defaultNickname;
      
      this.triggerEvent('confirm', {
        nickname
      });
    }
  }
});