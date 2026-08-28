const api = require('../../utils/api');

const RED = '#f23645';
const GREEN = '#0bbf6b';
const YELLOW = '#f0b90b';
const MUTED = '#8b96ab';
const TEXT = '#e8edf6';

Page({
  data: {
    mode: 'cal',
    period: 'day',
    statsText: '加载中…'
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
    api.getKline(this.data.mode, this.data.period)
      .then((res) => {
        that.series = res.candles || [];
        that.unit = res.unit || 'kcal';
        that.updateStats();
        that.draw();
      })
      .catch(() => {});
  },

  onMode(e) {
    this.setData({ mode: e.currentTarget.dataset.mode });
    this.load();
  },

  onPeriod(e) {
    this.setData({ period: e.currentTarget.dataset.period });
    this.load();
  },

  fmt(v) {
    return this.data.mode === 'weight' ? v.toFixed(2) : Math.round(v);
  },

  updateStats() {
    const ser = this.series;
    if (!ser || !ser.length) {
      this.setData({ statsText: '暂无数据' });
      return;
    }
    const last = ser[ser.length - 1];
    const prev = ser.length > 1 ? ser[ser.length - 2] : last;
    const diff = last.close - prev.close;
    let ma5 = '-';
    if (ser.length >= 5) {
      let sum = 0;
      for (let i = ser.length - 5; i < ser.length; i++) {
        sum += ser[i].close;
      }
      ma5 = this.fmt(sum / 5);
    }
    this.setData({
      statsText: '收盘 ' + this.fmt(last.close) + ' ' + this.unit +
        ' · 涨跌 ' + (diff >= 0 ? '+' : '') + this.fmt(diff) +
        ' · MA5 ' + ma5
    });
  },

  draw() {
    const that = this;
    wx.createSelectorQuery()
      .select('#kline')
      .fields({ node: true, size: true })
      .exec((res) => {
        if (!res || !res[0] || !res[0].node) {
          return;
        }
        const canvas = res[0].node;
        const width = res[0].width;
        const height = res[0].height;
        const dpr = wx.getSystemInfoSync().pixelRatio || 2;
        canvas.width = width * dpr;
        canvas.height = height * dpr;
        const ctx = canvas.getContext('2d');
        ctx.scale(dpr, dpr);
        that.ctx = ctx;
        that.width = width;
        that.height = height;
        that.hover = -1;
        that.renderChart();
      });
  },

  xLabel(s) {
    if (this.data.period === 'year') {
      return s;
    }
    if (this.data.period === 'month') {
      return s.substring(2);
    }
    return s.substring(5);
  },

  niceStep(raw) {
    const p = Math.pow(10, Math.floor(Math.log10(raw)));
    const r = raw / p;
    let m = 1;
    if (r <= 1) m = 1;
    else if (r <= 2) m = 2;
    else if (r <= 2.5) m = 2.5;
    else if (r <= 5) m = 5;
    else m = 10;
    return m * p;
  },

  renderChart() {
    const ctx = this.ctx;
    if (!ctx) {
      return;
    }
    const w = this.width;
    const h = this.height;
    const pad = { l: 52, r: 10, t: 12, b: 22 };
    const plotW = w - pad.l - pad.r;
    const plotH = h - pad.t - pad.b;
    const ser = this.series || [];
    ctx.clearRect(0, 0, w, h);
    if (!ser.length) {
      return;
    }

    let min = Infinity;
    let max = -Infinity;
    ser.forEach((c) => {
      min = Math.min(min, c.low);
      max = Math.max(max, c.high);
    });
    let span = (max - min) || 1;
    min -= span * 0.08;
    max += span * 0.08;
    const y = (v) => pad.t + plotH * (1 - (v - min) / (max - min));

    const step = this.niceStep((max - min) / 4);
    ctx.font = '10px sans-serif';
    ctx.strokeStyle = 'rgba(139,150,171,0.12)';
    ctx.lineWidth = 1;
    for (let v = Math.ceil(min / step) * step; v <= max; v += step) {
      const yy = Math.round(y(v)) + 0.5;
      ctx.beginPath();
      ctx.moveTo(pad.l, yy);
      ctx.lineTo(w - pad.r, yy);
      ctx.stroke();
      ctx.fillStyle = MUTED;
      ctx.fillText(this.fmt(v), pad.l - 4, yy + 3);
    }

    const n = ser.length;
    const slot = plotW / n;
    const cw = Math.max(2, Math.min(12, slot * 0.62));
    ser.forEach((c, i) => {
      const x = pad.l + slot * i + slot / 2;
      const col = c.close >= c.open ? RED : GREEN;
      ctx.strokeStyle = col;
      ctx.fillStyle = col;
      ctx.beginPath();
      ctx.moveTo(x, y(c.high));
      ctx.lineTo(x, y(c.low));
      ctx.stroke();
      const yo = y(c.open);
      const yc = y(c.close);
      const top = Math.min(yo, yc);
      const bh = Math.max(1, Math.abs(yc - yo));
      ctx.fillRect(x - cw / 2, top, cw, bh);
    });

    if (n >= 5) {
      ctx.strokeStyle = YELLOW;
      ctx.lineWidth = 1.4;
      ctx.beginPath();
      for (let i = 4; i < n; i++) {
        const sum = ser[i].close + ser[i - 1].close + ser[i - 2].close + ser[i - 3].close + ser[i - 4].close;
        const x = pad.l + slot * i + slot / 2;
        const yv = y(sum / 5);
        if (i === 4) {
          ctx.moveTo(x, yv);
        } else {
          ctx.lineTo(x, yv);
        }
      }
      ctx.stroke();
    }

    const stepIdx = Math.max(1, Math.ceil(n / 5));
    ctx.fillStyle = MUTED;
    ctx.textAlign = 'center';
    for (let i = 0; i < n; i += stepIdx) {
      ctx.fillText(this.xLabel(ser[i].date), pad.l + slot * i + slot / 2, h - 7);
    }

    if (this.hover >= 0 && this.hover < n) {
      const c = ser[this.hover];
      const x = pad.l + slot * this.hover + slot / 2;
      ctx.strokeStyle = 'rgba(232,237,246,0.4)';
      ctx.beginPath();
      ctx.moveTo(Math.round(x) + 0.5, pad.t);
      ctx.lineTo(Math.round(x) + 0.5, h - pad.b);
      ctx.stroke();
      ctx.fillStyle = c.close >= c.open ? RED : GREEN;
      ctx.beginPath();
      ctx.arc(x, y(c.close), 3.5, 0, Math.PI * 2);
      ctx.fill();
      this.drawTooltip(c, x, this.hoverY, w, h);
    }
    ctx.textAlign = 'left';
  },

  maAt(idx) {
    const ser = this.series || [];
    if (idx < 4 || !ser.length) {
      return '-';
    }
    let sum = 0;
    for (let i = idx - 4; i <= idx; i++) {
      sum += ser[i].close;
    }
    return this.fmt(sum / 5);
  },

  onTouch(e) {
    const touch = e.touches[0];
    if (!touch) {
      return;
    }
    const x = typeof touch.x === 'number' ? touch.x : touch.clientX;
    const y = typeof touch.y === 'number' ? touch.y : touch.clientY;
    const ser = this.series || [];
    if (!ser.length) {
      return;
    }
    const slot = (this.width - 52 - 10) / ser.length;
    const idx = Math.max(0, Math.min(ser.length - 1, Math.floor((x - 52) / slot)));
    this.hover = idx;
    this.hoverY = y;
    this.renderChart();
  },

  drawTooltip(c, x, y, w, h) {
    const ctx = this.ctx;
    const lines = [
      c.date,
      '开 ' + this.fmt(c.open) + '  收 ' + this.fmt(c.close),
      '高 ' + this.fmt(c.high) + '  低 ' + this.fmt(c.low),
      'MA5 ' + this.maAt(this.hover)
    ];
    const tw = 112;
    const th = 58;
    let tx = x + 12;
    let ty = y - th - 8;
    if (tx + tw > w) {
      tx = x - tw - 12;
    }
    if (ty < 0) {
      ty = 4;
    }
    ctx.font = '10px sans-serif';
    ctx.fillStyle = 'rgba(10,15,26,0.94)';
    ctx.strokeStyle = '#222c3f';
    ctx.fillRect(tx, ty, tw, th);
    ctx.strokeRect(tx, ty, tw, th);
    ctx.fillStyle = MUTED;
    ctx.fillText(lines[0], tx + 6, ty + 12);
    ctx.fillStyle = TEXT;
    ctx.fillText(lines[1], tx + 6, ty + 26);
    ctx.fillText(lines[2], tx + 6, ty + 40);
    ctx.fillStyle = YELLOW;
    ctx.fillText(lines[3], tx + 6, ty + 53);
  }
});
