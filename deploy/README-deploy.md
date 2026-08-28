# 服务器部署速查（阿里云 ECS）

## 服务器端环境变量文件

上传 jar 到 `/opt/calorie-market/` 后，创建 `/etc/calorie-market.env`：

```bash
cat > /etc/calorie-market.env <<'EOF'
MYSQL_USER=root
MYSQL_PASSWORD=换成服务器数据库密码
WX_APPID=你的微信小程序AppID
WX_SECRET=你的微信小程序AppSecret
EOF
chmod 600 /etc/calorie-market.env
```

## 启动后端

```bash
systemctl daemon-reload
systemctl enable --now calorie-market
systemctl status calorie-market
journalctl -u calorie-market -f   # 看日志
```

## Nginx

```bash
cp nginx-calorie.conf /etc/nginx/conf.d/calorie.conf
nginx -t && systemctl reload nginx
```

## 阿里云控制台要做的三件事

1. **安全组**：ECS 实例 → 安全组 → 入方向放行 22、80、443（8080 只在自己调试时用，部署完可不开）
2. **域名解析**：云解析 DNS 添加 A 记录 `chuanyan.top` 和 `www` → 服务器公网 IP
3. **备案**：阿里云控制台搜索「ICP 备案」提交；未备案前 80/443 可能被拦截，微信合法域名也要求备案
