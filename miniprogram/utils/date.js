function pad(n) {
  return n < 10 ? '0' + n : '' + n;
}

function todayStr() {
  const d = new Date();
  return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate());
}

function nowHM() {
  const d = new Date();
  return pad(d.getHours()) + ':' + pad(d.getMinutes());
}

function weekDay() {
  const wd = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
  return wd[new Date().getDay()];
}

module.exports = { todayStr, nowHM, weekDay };
