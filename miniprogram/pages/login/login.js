const api = require('../../utils/api');

Page({
  data: {
    mode: 'login',
    phone: '',
    password: ''
  },

  onLoad() {
    if (getApp().globalData.userId) {
      wx.reLaunch({ url: '/pages/dashboard/dashboard' });
    }
  },

  switchMode(e) {
    this.setData({ mode: e.currentTarget.dataset.mode });
  },

  onPhone(e) {
    this.setData({ phone: e.detail.value });
  },

  onPassword(e) {
    this.setData({ password: e.detail.value });
  },

  wxLogin() {
    wx.login({
      success: (res) => {
        if (!res.code) {
          wx.showToast({ title: '微信登录失败，请重试', icon: 'none' });
          return;
        }
        api.wxLogin(res.code)
          .then((r) => this.onLoggedIn(r))
          .catch(() => {});
      },
      fail: () => {
        wx.showToast({ title: '微信登录失败，请重试', icon: 'none' });
      }
    });
  },

  submit() {
    const phone = this.data.phone.trim();
    const password = this.data.password;
    if (!/^1[3-9]\d{9}$/.test(phone)) {
      wx.showToast({ title: '请输入正确的 11 位手机号', icon: 'none' });
      return;
    }
    if (password.length < 6 || password.length > 20) {
      wx.showToast({ title: '密码长度需为 6-20 位', icon: 'none' });
      return;
    }
    const call = this.data.mode === 'login' ? api.phoneLogin : api.phoneRegister;
    call({ phone, password })
      .then((r) => this.onLoggedIn(r))
      .catch(() => {});
  },

  onLoggedIn(res) {
    getApp().globalData.userId = res.userId;
    wx.setStorageSync('userId', res.userId);
    wx.reLaunch({ url: '/pages/dashboard/dashboard' });
  }
});
