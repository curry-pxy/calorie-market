const api = require('../../utils/api');
const dateUtil = require('../../utils/date');
const QUICK = require('../../utils/quick');

Page({
  data: {
    sectors: [],
    activeId: null,
    quickItems: [],
    itemSign: '+'
  },

  onShow() {
    if (!getApp().globalData.userId) {
      wx.reLaunch({ url: '/pages/login/login' });
      return;
    }
    this.loadSectors();
  },

  loadSectors() {
    const that = this;
    api.getSectors().then((sectors) => {
      const recordable = sectors.filter((s) => s.type !== 'base');
      let activeId = that.data.activeId;
      if (!recordable.some((s) => s.id === activeId)) {
        activeId = recordable.length ? recordable[0].id : null;
      }
      that.setData({ sectors: recordable, activeId });
      that.buildQuick();
    }).catch(() => {});
  },

  onChipTap(e) {
    this.setData({ activeId: e.currentTarget.dataset.id });
    this.buildQuick();
  },

  buildQuick() {
    const id = this.data.activeId;
    const sector = this.data.sectors.find((s) => s.id === id);
    const items = (QUICK[id] || []).slice(0, 8);
    this.setData({
      quickItems: items,
      itemSign: sector && sector.type === 'in' ? '+' : '-'
    });
  },

  onQuickTap(e) {
    const d = e.currentTarget.dataset;
    api.addRecord({
      sectorId: this.data.activeId,
      name: d.name,
      kcal: Number(d.kcal),
      qty: d.qty,
      time: dateUtil.nowHM()
    }).then(() => {
      wx.showToast({ title: '已记一笔', icon: 'success' });
    }).catch(() => {});
  },

  goCustom() {
    wx.navigateTo({
      url: '/pages/record-add/record-add?sectorId=' + (this.data.activeId || '')
    });
  }
});
