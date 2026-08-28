# 减肥行情小程序

把减肥记录做成"股票行情"的小程序：红色 = 增加能量（摄入），绿色 = 减少能量（消耗/达标），历史数据用 K 线（日K/周K/月K/年K）展示。

## 目录结构

```
calorie-market/
├── backend/                  # Java 后端（Spring Boot 2.7 + MyBatis）
│   └── src/main/
│       ├── java/com/calorie/market/
│       │   ├── controller/   # 接口层
│       │   ├── service/      # 业务层
│       │   ├── mapper/       # MyBatis Mapper 接口
│       │   ├── domain/       # 实体
│       │   ├── dto/          # 请求/响应对象
│       │   ├── common/       # 统一返回与异常处理
│       │   └── config/       # 跨域等配置
│       └── resources/
│           ├── mapper/       # SQL（mapper.xml）
│           └── db/schema.sql # 建表脚本（启动自动执行）
├── miniprogram/              # 微信小程序前端（原生语法）
│   ├── pages/
│   │   ├── dashboard/        # 行情大盘
│   │   ├── record/           # 快速记账
│   │   ├── record-add/       # 自定义记账
│   │   ├── history/          # 历史K线
│   │   └── mine/             # 我的/设置
│   └── utils/                # 请求封装、日期、常用食物库
└── sql/init.sql              # 一次性建库脚本（手动导入用）
```

## 技术栈

- 后端：Java 8 + Spring Boot 2.7.18 + MyBatis + MySQL
- 前端：微信小程序原生（WXML/WXSS/JS），K 线用 Canvas 2D 自绘

## 本地启动

### 1. 准备 MySQL

本机已通过 Homebrew 安装 **MySQL 8.0.46** 并建好 `calorie_market` 库（含 4 张表），无需再操作。

常用命令：

```bash
# 启动 / 停止 MySQL
brew services start mysql@8.0
brew services stop mysql@8.0

# 连接数据库（mysql@8.0 是 keg-only，命令不在 PATH）
/opt/homebrew/opt/mysql@8.0/bin/mysql -u root
```

如需从零重建数据库：

```bash
/opt/homebrew/opt/mysql@8.0/bin/mysql -u root < sql/init.sql
```

> 应用启动时也会自动执行 `schema.sql` 建表，库存在即可。

### 2. 启动后端

```bash
cd backend
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_281.jdk/Contents/Home
source ../.env   # 加载本地环境变量（含微信 AppSecret、MySQL 账号）
mvn spring-boot:run
```

默认连接 `127.0.0.1:3306/calorie_market`，账号 root、密码为空。如果本机密码不同，用环境变量覆盖：

```bash
MYSQL_USER=root MYSQL_PASSWORD=你的密码 mvn spring-boot:run
```

### 3. 运行小程序

1. 打开微信开发者工具 → 导入项目，目录选择 `miniprogram/`
2. AppID 选「测试号」（项目已默认配置 `touristappid`）
3. 详情 → 本地设置 → 勾选「不校验合法域名」（本地 http 调试需要）

## 接口一览

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | /api/auth/login | 登录（传 wx.login 的 code） |
| GET | /api/user/profile | 获取个人资料 |
| PUT | /api/user/profile | 更新资料（自动重算基础代谢） |
| GET | /api/sectors | 板块列表 |
| POST | /api/sectors | 新增自定义板块 |
| DELETE | /api/sectors/{id} | 删除自定义板块 |
| GET | /api/records?date= | 某天记录 |
| POST | /api/records | 新增记录 |
| DELETE | /api/records/{id} | 删除记录 |
| GET | /api/summary/today | 今日大盘汇总（含连续达标天数） |
| GET | /api/kline?type=cal\|weight&period=day\|week\|month\|year | K 线数据 |
| GET | /api/weights?limit= | 体重记录 |
| POST | /api/weights | 记录体重 |

除登录外，其余接口通过请求头 `X-User-Id` 标识用户（开发阶段简化，上线前会换成 token）。

## 上线前需要处理的事项

> 完整上线流程见 [DEPLOY.md](DEPLOY.md)（购买服务器/域名 → 备案 → 部署 → 审核发布）。

- 小程序 AppID 已配置（`wxe37e739e2b612ca3`）；还需要 **AppSecret**：小程序后台「开发管理 → 开发设置」查看/生成，启动后端时传入：

  ```bash
  source ../.env
  mvn spring-boot:run
  ```

  > 密钥保存在项目根目录 `.env`（已被 `.gitignore` 忽略）。只有 AppID 和 AppSecret 都配置时，后端才会调用微信 `code2session` 做真实登录；否则自动退回本地开发模式（传任意 code 即登录）。
- 数据库从本机 MySQL 切换到阿里云 RDS（改 `application.yml` 的 datasource 地址/账号/密码即可）
- 后端部署到服务器，配置 HTTPS 域名并完成 ICP 备案
- 登录鉴权从 `X-User-Id` 升级为 token 机制
