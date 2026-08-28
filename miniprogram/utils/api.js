const { request } = require('./request');

const login = (code) => request('/api/auth/login', 'POST', { code });
const wxLogin = (code) => request('/api/auth/wx-login', 'POST', { code });
const phoneLogin = (data) => request('/api/auth/phone/login', 'POST', data);
const phoneRegister = (data) => request('/api/auth/phone/register', 'POST', data);
const getProfile = () => request('/api/user/profile');
const saveProfile = (data) => request('/api/user/profile', 'PUT', data);
const getSectors = () => request('/api/sectors');
const addSector = (data) => request('/api/sectors', 'POST', data);
const deleteSector = (id) => request('/api/sectors/' + id, 'DELETE');
const getRecords = (date) => request('/api/records?date=' + (date || ''));
const addRecord = (data) => request('/api/records', 'POST', data);
const deleteRecord = (id) => request('/api/records/' + id, 'DELETE');
const getTodaySummary = () => request('/api/summary/today');
const getKline = (type, period) => request('/api/kline?type=' + type + '&period=' + period);
const getWeights = (limit) => request('/api/weights?limit=' + (limit || 30));
const addWeight = (data) => request('/api/weights', 'POST', data);

module.exports = {
  login,
  wxLogin,
  phoneLogin,
  phoneRegister,
  getProfile,
  saveProfile,
  getSectors,
  addSector,
  deleteSector,
  getRecords,
  addRecord,
  deleteRecord,
  getTodaySummary,
  getKline,
  getWeights,
  addWeight
};
