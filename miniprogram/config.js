/**
 * 本地开发后端地址配置。
 *
 * - 微信开发者工具：127.0.0.1 或电脑局域网 IP 都可以
 * - 手机真机调试：必须填电脑的局域网 IP（手机和电脑连同一个 Wi-Fi）
 * - 正式上线：改成 https://www.chuanyan.top（备案通过、HTTPS 配好后切换）
 *
 * 电脑 IP 变了就改这里（终端执行 ipconfig getifaddr en0 查看）。
 */
const BASE_URL = 'http://10.128.100.59:8080';

module.exports = {
  BASE_URL
};
