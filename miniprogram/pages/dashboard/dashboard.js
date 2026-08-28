const api = require('../../utils/api');
const dateUtil = require('../../utils/date');

const FAB_W = 118;
const FAB_H = 46;

Page({
  data: {
    dateText: '',
    summary: null,
    sectors: [],
    tiles: [],
    records: [],
    rank: [],
    viewMode: 'list',
    fabDx: 0,
    fabDy: 0,
    fabVisible: true,
    netText: '0',
    netClass: 'up',
    statusText: '',
    statusClass: 'badge-wip',
    goalText: '',
    meterWidth: 0
  },

  onLoad() {
    const state = wx.getStorageSync('fabStateV2');
    if (state && typeof state.dx === 'number') {
      this.setData({
        fabDx: state.dx,
        fabDy: typeof state.dy === 'number' ? state.dy : 0
      });
      if (state.hidden) {
        this.setData({ fabVisible: false });
      }
    } else {
      // 旧版本留下的隐藏/位置缓存，统一清掉，保证按钮默认显示
      wx.removeStorageSync('fabPos');
      wx.removeStorageSync('fabHidden');
    }
  },

  onShow() {
    if (!this.ensureLogin()) return;
    this.load();
  },

  ensureLogin() {
    if (!getApp().globalData.userId) {
      wx.reLaunch({ url: '/pages/login/login' });
      return false;
    }
    return true;
  },

  load() {
    const that = this;
    api.getTodaySummary()
      .then((summary) => {
        that.setData({
          summary,
          dateText: dateUtil.todayStr() + ' ' + dateUtil.weekDay()
        });
        that.buildSummaryView(summary);
        that.buildSectors(summary);
        that.buildTiles(summary);
        that.buildRank(summary);
      })
      .catch(() => {});

    api.getRecords(dateUtil.todayStr())
      .then((records) => {
        that.setData({
          records: records.map((r) => ({
            id: r.id,
            timeText: (r.recordTime || '').substring(0, 5),
            name: r.name,
            sectorName: r.sectorName || '',
            kcalText: (r.sectorType === 'in' ? '+' : '-') + r.kcal,
            cls: r.sectorType === 'in' ? 'up' : 'down'
          }))
        });
      })
      .catch(() => {});
  },

  buildSummaryView(summary) {
    const net = summary.net;
    const cls = net <= 0 ? 'down' : 'up';
    let statusText = '';
    let statusClass = '';
    if (summary.status === 'OK') {
      statusText = '已达标';
      statusClass = 'badge-ok';
    } else if (summary.status === 'WIP') {
      statusText = '进行中';
      statusClass = 'badge-wip';
    } else {
      statusText = '超标';
      statusClass = 'badge-bad';
    }
    const remain = summary.remain;
    const goalText = remain <= 0 ? '已超额 ' + (-remain) + ' kcal' : '还差 ' + remain + ' kcal 达标';
    let meterWidth = 0;
    if (net <= -summary.target) {
      meterWidth = 100;
    } else if (net < 0) {
      meterWidth = Math.round(-net / summary.target * 100);
    }
    this.setData({
      netText: (net > 0 ? '+' : '') + net,
      netClass: cls,
      statusText,
      statusClass,
      goalText,
      meterWidth
    });
  },

  buildSectors(summary) {
    this.setData({
      sectors: (summary.sectors || []).map((s) => ({
        sectorId: s.sectorId,
        name: s.name,
        type: s.type,
        valueText: (s.value > 0 && s.type === 'in' ? '+' : '') + s.value,
        countText: s.type === 'base' ? '自动' : s.count + ' 笔'
      }))
    });
  },

  buildTiles(summary) {
    const items = (summary.sectors || [])
      .filter((s) => s.value !== 0)
      .map((s) => ({
        sectorId: s.sectorId,
        name: s.name,
        type: s.type,
        cls: s.type === 'in' ? 'tile-up' : 'tile-down',
        valueText: (s.value > 0 && s.type === 'in' ? '+' : '') + s.value,
        weight: Math.abs(s.value)
      }));
    if (!items.length) {
      this.setData({ tiles: [] });
      return;
    }
    const rects = bigLeftLayout(items.map((it) => it.weight), 100, 100);
    const round1 = (v) => Math.round(v * 10) / 10;
    const rectByIdx = {};
    rects.forEach((r) => {
      rectByIdx[r.i] = r;
    });
    this.setData({
      tiles: items.map((it, idx) => {
        const r = rectByIdx[idx];
        return {
          sectorId: it.sectorId,
          name: it.name,
          type: it.type,
          cls: it.cls,
          valueText: it.valueText,
          x: round1(r.x),
          y: round1(r.y),
          w: round1(r.w),
          h: round1(r.h),
          showName: r.h >= 5,
          showVal: r.h >= 9 && r.w >= 15
        };
      })
    });
  },

  switchView(e) {
    const mode = e.currentTarget.dataset.mode;
    if (mode === this.data.viewMode) return;
    this.setData({ viewMode: mode });
  },

  buildRank(summary) {
    const items = (summary.sectors || [])
      .filter((s) => s.value !== 0)
      .sort((a, b) => Math.abs(b.value) - Math.abs(a.value))
      .slice(0, 5);
    if (!items.length) {
      this.setData({ rank: [] });
      return;
    }
    const max = Math.abs(items[0].value);
    const medals = ['🥇', '🥈', '🥉'];
    this.setData({
      rank: items.map((s, i) => ({
        rank: medals[i] || (i + 1),
        name: s.name,
        cls: s.value > 0 ? 'up' : 'down',
        text: (s.value > 0 ? '+' : '') + s.value,
        width: Math.round(Math.abs(s.value) / max * 100)
      }))
    });
  },

  onSectorTap(e) {
    const type = e.currentTarget.dataset.type;
    if (type === 'base') {
      wx.showToast({ title: '基础代谢按身高体重自动估算', icon: 'none' });
      return;
    }
    wx.navigateTo({
      url: '/pages/record-add/record-add?sectorId=' + e.currentTarget.dataset.id
    });
  },

  onDeleteRecord(e) {
    const id = e.currentTarget.dataset.id;
    api.deleteRecord(id).then(() => {
      wx.showToast({ title: '已删除', icon: 'none' });
      this.load();
    }).catch(() => {});
  },

  goRecord() {
    wx.switchTab({ url: '/pages/record/record' });
  },

  getWin() {
    try {
      return wx.getWindowInfo ? wx.getWindowInfo() : wx.getSystemInfoSync();
    } catch (e) {
      return wx.getSystemInfoSync();
    }
  },

  onFabTouchStart(e) {
    const touch = e.touches[0];
    this._fabDrag = {
      startX: touch.clientX,
      startY: touch.clientY,
      dx: this.data.fabDx,
      dy: this.data.fabDy,
      action: (e.target && e.target.dataset && e.target.dataset.action) || 'record',
      moved: false
    };
  },

  onFabTouchMove(e) {
    const d = this._fabDrag;
    if (!d) return;
    const touch = e.touches[0];
    const dx = touch.clientX - d.startX;
    const dy = touch.clientY - d.startY;
    if (!d.moved && Math.abs(dx) < 4 && Math.abs(dy) < 4) return;
    d.moved = true;
    const win = this.getWin();
    const minDx = FAB_W + 28 - win.windowWidth;
    const maxDx = 12;
    const minDy = FAB_H + 108 - win.windowHeight;
    const maxDy = 0;
    this.setData({
      fabDx: Math.min(Math.max(d.dx + dx, minDx), maxDx),
      fabDy: Math.min(Math.max(d.dy + dy, minDy), maxDy)
    });
  },

  onFabTouchEnd() {
    const d = this._fabDrag;
    this._fabDrag = null;
    if (!d) return;
    if (d.moved) {
      this.saveFabState();
      return;
    }
    if (d.action === 'hide') {
      this.hideFab();
    } else {
      this.goRecord();
    }
  },

  hideFab() {
    this.setData({ fabVisible: false });
    this.saveFabState();
    wx.showToast({ title: '已隐藏，点小减号恢复', icon: 'none' });
  },

  showFab() {
    this.setData({ fabVisible: true });
    this.saveFabState();
  },

  saveFabState() {
    wx.setStorageSync('fabStateV2', {
      dx: this.data.fabDx,
      dy: this.data.fabDy,
      hidden: !this.data.fabVisible
    });
  }
});

function bigLeftLayout(weights, width, height) {
  const items = weights.map((w, i) => ({ w: w, i: i })).filter((it) => it.w > 0);
  const total = items.reduce((s, it) => s + it.w, 0);
  if (!total) return [];
  if (items.length === 1) {
    return [{ x: 0, y: 0, w: width, h: height, i: items[0].i }];
  }
  items.sort((a, b) => b.w - a.w);

  const out = [];
  const big = items[0];
  const smalls = items.slice(1);
  const smallTotal = smalls.reduce((s, it) => s + it.w, 0);

  const leftW = clamp(big.w / total * width, width * 0.4, width * 0.68);
  const rightW = width - leftW;

  out.push({ x: 0, y: 0, w: leftW, h: height, i: big.i });

  const minH = Math.min(height * 0.06, height / smalls.length * 0.9);
  const hs = smalls.map((it) => height * it.w / smallTotal);
  let deficit = 0;
  for (let k = 0; k < hs.length; k++) {
    if (hs[k] < minH) {
      deficit += minH - hs[k];
      hs[k] = minH;
    }
  }
  if (deficit > 0) {
    const order = smalls
      .map((it, k) => ({ k: k, h: hs[k] }))
      .filter((o) => hs[o.k] > minH + 1e-9)
      .sort((a, b) => b.h - a.h);
    for (const o of order) {
      const take = Math.min(hs[o.k] - minH, deficit);
      hs[o.k] -= take;
      deficit -= take;
      if (deficit <= 1e-9) break;
    }
  }

  let y = 0;
  for (let k = 0; k < smalls.length; k++) {
    out.push({ x: leftW, y: y, w: rightW, h: hs[k], i: smalls[k].i });
    y += hs[k];
  }
  return out;
}

function clamp(v, lo, hi) {
  return Math.min(Math.max(v, lo), hi);
}
