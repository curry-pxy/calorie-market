const api = require('../../utils/api');
const dateUtil = require('../../utils/date');

Page({
  data: {
    sectors: [],
    sectorIndex: 0,
    sectorNames: [],
    kcalLabel: '热量',
    qtyLabel: '份量/时长',
    name: '',
    kcal: '',
    qty: ''
  },

  onLoad(options) {
    const preset = options.sectorId ? Number(options.sectorId) : null;
    const that = this;
    api.getSectors().then((sectors) => {
      const recordable = sectors.filter((s) => s.type !== 'base');
      let index = 0;
      if (preset) {
        const idx = recordable.findIndex((s) => s.id === preset);
        if (idx >= 0) index = idx;
      }
      that.setData({
        sectors: recordable,
        sectorIndex: index,
        sectorNames: recordable.map((s) => s.name)
      });
      that.refreshLabels();
    }).catch(() => {});
  },

  onSector(e) {
    this.setData({ sectorIndex: Number(e.detail.value) });
    this.refreshLabels();
  },

  refreshLabels() {
    const sector = this.data.sectors[this.data.sectorIndex];
    if (!sector) return;
    this.setData({
      kcalLabel: sector.type === 'in' ? '热量' : '消耗',
      qtyLabel: sector.name === '运动' ? '时长（分钟）' : '份量/时长'
    });
  },

  onName(e) { this.setData({ name: e.detail.value }); },
  onKcal(e) { this.setData({ kcal: e.detail.value }); },
  onQty(e) { this.setData({ qty: e.detail.value }); },

  save() {
    const sector = this.data.sectors[this.data.sectorIndex];
    const kcal = Number(this.data.kcal);
    if (!this.data.name.trim()) {
      wx.showToast({ title: '请填写名称', icon: 'none' });
      return;
    }
    if (!kcal || kcal <= 0) {
      wx.showToast({ title: '请填写大于 0 的热量/消耗', icon: 'none' });
      return;
    }
    api.addRecord({
      sectorId: sector.id,
      name: this.data.name.trim(),
      kcal: Math.round(kcal),
      qty: this.data.qty.trim(),
      time: dateUtil.nowHM()
    }).then(() => {
      wx.showToast({ title: '已记一笔', icon: 'success' });
      setTimeout(() => wx.navigateBack(), 500);
    }).catch(() => {});
  }
});
