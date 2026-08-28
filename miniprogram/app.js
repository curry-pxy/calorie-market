App({
  globalData: {
    userId: null
  },

  onLaunch() {
    const userId = wx.getStorageSync('userId');
    if (userId) {
      this.globalData.userId = userId;
    }
  }
});
