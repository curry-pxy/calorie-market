const api = require('../../utils/api');

Page({
  data: {
    genderList: ['男', '女'],
    genderIndex: 0,
    age: '',
    height: '',
    weight: '',
    target: '',
    bmr: 0,
    weekly: {
      okDays: 0,
      sum: 0,
      weightText: '0.0kg',
      weightCls: 'down'
    },
    tagList: ['晨重', '晚重'],
    tagIndex: 0,
    wKg: '',
    sectorTypeList: ['增加能量', '减少能量'],
    sectorTypeIndex: 0,
    sectorName: '',
    customSectors: [],
    profileOpen: false
  },

  onShow() {
    if (!getApp().globalData.userId) {
      wx.reLaunch({ url: '/pages/login/login' });
      return;
    }
    this.load();
  },

  load() {
    const that = this;
    api.getProfile()
      .then((u) => {
        that.setData({
          genderIndex: u.gender === '女' ? 1 : 0,
          age: u.age,
          height: u.height,
          weight: u.weight,
          target: u.target,
          bmr: u.bmr
        });
        that.loadWeekly();
      })
      .catch(() => {});

    api.getSectors()
      .then((sectors) => {
        that.setData({
          customSectors: sectors
            .filter((s) => s.custom)
            .map((s) => ({
              id: s.id,
              name: s.name,
              typeText: s.type === 'in' ? '增加能量' : '减少能量'
            }))
        });
      })
      .catch(() => {});
  },

  loadWeekly() {
    const that = this;
    const target = Number(that.data.target) || 400;
    api.getKline('cal', 'day')
      .then((res) => {
        const ser = res.candles || [];
        const last7 = ser.slice(-7);
        const okDays = last7.filter((c) => c.close <= -target).length;
        const sum = Math.abs(last7.reduce((a, c) => a + c.close, 0));
        that.setData({
          'weekly.okDays': okDays,
          'weekly.sum': sum
        });
      })
      .catch(() => {});

    api.getKline('weight', 'day')
      .then((res) => {
        const ser = res.candles || [];
        let diff = 0;
        if (ser.length >= 8) {
          diff = ser[ser.length - 1].close - ser[ser.length - 8].close;
        }
        that.setData({
          'weekly.weightText': (diff > 0 ? '+' : '') + diff.toFixed(1) + 'kg',
          'weekly.weightCls': diff <= 0 ? 'down' : 'up'
        });
      })
      .catch(() => {});
  },

  onGender(e) { this.setData({ genderIndex: Number(e.detail.value) }); },
  onAge(e) { this.setData({ age: e.detail.value }); },
  onHeight(e) { this.setData({ height: e.detail.value }); },
  onWeight(e) { this.setData({ weight: e.detail.value }); },
  onTarget(e) { this.setData({ target: e.detail.value }); },
  onWKg(e) { this.setData({ wKg: e.detail.value }); },
  onTag(e) { this.setData({ tagIndex: Number(e.detail.value) }); },
  onSectorName(e) { this.setData({ sectorName: e.detail.value }); },
  onSectorType(e) { this.setData({ sectorTypeIndex: Number(e.detail.value) }); },

  openProfile() {
    this.setData({ profileOpen: true });
  },

  closeProfile() {
    this.setData({ profileOpen: false });
  },

  saveProfile() {
    const age = Number(this.data.age);
    const height = Number(this.data.height);
    const weight = Number(this.data.weight);
    const target = Number(this.data.target);
    if (!age || !height || !weight || !target) {
      wx.showToast({ title: '请填写完整资料', icon: 'none' });
      return;
    }
    const that = this;
    api.saveProfile({
      gender: this.data.genderList[this.data.genderIndex],
      age,
      height,
      weight,
      target
    }).then((u) => {
      that.setData({ bmr: u.bmr, profileOpen: false });
      wx.showToast({ title: '已保存，基础代谢 ' + u.bmr + ' kcal', icon: 'none' });
      that.loadWeekly();
    }).catch(() => {});
  },

  saveWeight() {
    const kg = Number(this.data.wKg);
    if (!kg || kg <= 0) {
      wx.showToast({ title: '请填写体重', icon: 'none' });
      return;
    }
    const that = this;
    api.addWeight({
      kg,
      tag: this.data.tagList[this.data.tagIndex]
    }).then(() => {
      that.setData({ wKg: '' });
      wx.showToast({ title: '已记录体重 ' + kg + ' kg', icon: 'none' });
      that.loadWeekly();
    }).catch(() => {});
  },

  saveSector() {
    const name = this.data.sectorName.trim();
    if (!name) {
      wx.showToast({ title: '请填写板块名', icon: 'none' });
      return;
    }
    const that = this;
    api.addSector({
      name,
      type: this.data.sectorTypeIndex === 0 ? 'in' : 'out'
    }).then(() => {
      that.setData({ sectorName: '' });
      wx.showToast({ title: '已添加板块', icon: 'none' });
      that.load();
    }).catch(() => {});
  },

  deleteSector(e) {
    const id = e.currentTarget.dataset.id;
    const that = this;
    api.deleteSector(id).then(() => {
      wx.showToast({ title: '已删除板块', icon: 'none' });
      that.load();
    }).catch(() => {});
  }
});
