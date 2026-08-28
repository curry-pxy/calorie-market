-- 减肥行情小程序建表脚本（应用启动时自动执行）

CREATE TABLE IF NOT EXISTS t_user (
    id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    openid     VARCHAR(64)  DEFAULT NULL COMMENT '微信 openid（手机号注册用户为空）',
    phone      VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    password   VARCHAR(128) DEFAULT NULL COMMENT '密码哈希（格式：salt:hash）',
    nickname   VARCHAR(64)  DEFAULT NULL COMMENT '昵称',
    gender     VARCHAR(8)   DEFAULT '男' COMMENT '性别',
    age        INT          DEFAULT 28 COMMENT '年龄',
    height     INT          DEFAULT 175 COMMENT '身高 cm',
    weight     DOUBLE       DEFAULT 63 COMMENT '体重 kg',
    target     INT          DEFAULT 400 COMMENT '每日目标赤字 kcal',
    bmr        INT          DEFAULT 1589 COMMENT '基础代谢 kcal',
    created_at DATETIME     DEFAULT NULL,
    updated_at DATETIME     DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_openid (openid),
    UNIQUE KEY uk_phone (phone)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户';

CREATE TABLE IF NOT EXISTS t_sector (
    id         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id    BIGINT      NOT NULL COMMENT '用户 id',
    name       VARCHAR(20) NOT NULL COMMENT '板块名',
    type       VARCHAR(8)  NOT NULL COMMENT 'in=增加能量/out=减少能量/base=基础代谢',
    custom     TINYINT(1)  DEFAULT 0 COMMENT '是否自定义板块',
    deleted    TINYINT(1)  DEFAULT 0 COMMENT '是否已删除',
    sort_order INT         DEFAULT 0 COMMENT '排序',
    created_at DATETIME    DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_user (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '板块';

CREATE TABLE IF NOT EXISTS t_record (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id     BIGINT      NOT NULL COMMENT '用户 id',
    sector_id   BIGINT      NOT NULL COMMENT '板块 id',
    record_date DATE        NOT NULL COMMENT '记录日期',
    record_time TIME        DEFAULT NULL COMMENT '记录时间',
    name        VARCHAR(60) NOT NULL COMMENT '名称',
    kcal        INT         NOT NULL COMMENT '热量绝对值 kcal',
    qty         VARCHAR(30) DEFAULT NULL COMMENT '份量/时长',
    created_at  DATETIME    DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_user_date (user_id, record_date)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '每日记录';

CREATE TABLE IF NOT EXISTS t_weight (
    id          BIGINT     NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id     BIGINT     NOT NULL COMMENT '用户 id',
    weight_date DATE       NOT NULL COMMENT '称重日期',
    weight_time TIME       DEFAULT NULL COMMENT '称重时间',
    kg          DOUBLE     NOT NULL COMMENT '体重 kg',
    tag         VARCHAR(8) DEFAULT '晨重' COMMENT '时段：晨重/晚重',
    created_at  DATETIME   DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_user_date (user_id, weight_date)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '体重记录';
