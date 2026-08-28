const { BASE_URL } = require('../config');

function request(path, method, data) {
  return new Promise((resolve, reject) => {
    let userId = '';
    try {
      const app = getApp();
      userId = ((app.globalData && app.globalData.userId) || wx.getStorageSync('userId') || '').toString();
    } catch (e) {
      // 登录还没完成时忽略
    }
    wx.request({
      url: BASE_URL + path,
      method: method || 'GET',
      data: data || {},
      header: {
        'content-type': 'application/json',
        'X-User-Id': userId
      },
      success(res) {
        if (res.statusCode === 200 && res.data && res.data.code === 0) {
          resolve(res.data.data);
        } else {
          const msg = (res.data && res.data.message) || ('请求失败：' + res.statusCode);
          wx.showToast({ title: msg, icon: 'none', duration: 2500 });
          reject(msg);
        }
      },
      fail(err) {
        wx.showToast({ title: '网络异常，请确认后端已启动', icon: 'none', duration: 2500 });
        reject(err);
      }
    });
  });
}

module.exports = {
  request,
  BASE_URL
};
