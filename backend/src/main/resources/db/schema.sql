-- ============================================================
-- 民宿管理系统 bnbms 数据库结构脚本（可重复执行）
-- MySQL 8.0
-- ============================================================

CREATE DATABASE IF NOT EXISTS bnbms
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

USE bnbms;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS user_coupon;
DROP TABLE IF EXISTS coupon;
DROP TABLE IF EXISTS notice;
DROP TABLE IF EXISTS banner;
DROP TABLE IF EXISTS review;
DROP TABLE IF EXISTS booking_order;
DROP TABLE IF EXISTS favorite;
DROP TABLE IF EXISTS homestay_facility;
DROP TABLE IF EXISTS facility;
DROP TABLE IF EXISTS homestay;
DROP TABLE IF EXISTS sys_user;

SET FOREIGN_KEY_CHECKS = 1;

-- ------------------------------------------------------------
-- 用户表（管理员 ADMIN / 房东 HOST / 租客 TENANT）
-- ------------------------------------------------------------
CREATE TABLE sys_user (
  id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  username     VARCHAR(50)  NOT NULL COMMENT '用户名',
  password     VARCHAR(100) NOT NULL COMMENT 'BCrypt 密码',
  real_name    VARCHAR(50)           DEFAULT NULL COMMENT '真实姓名/昵称',
  phone        VARCHAR(20)           DEFAULT NULL COMMENT '手机号',
  email        VARCHAR(100)          DEFAULT NULL COMMENT '邮箱',
  avatar       VARCHAR(500)          DEFAULT NULL COMMENT '头像 URL',
  role         VARCHAR(16)  NOT NULL DEFAULT 'TENANT' COMMENT 'ADMIN/HOST/TENANT',
  host_status  VARCHAR(16)  NOT NULL DEFAULT 'NONE' COMMENT '房东申请状态 NONE/PENDING/APPROVED/REJECTED',
  status       TINYINT      NOT NULL DEFAULT 1 COMMENT '0 禁用 1 正常',
  created_at   DATETIME              DEFAULT NULL,
  updated_at   DATETIME              DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_username (username),
  KEY idx_phone (phone),
  KEY idx_role (role)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='用户表';

-- ------------------------------------------------------------
-- 设施字典
-- ------------------------------------------------------------
CREATE TABLE facility (
  id         BIGINT      NOT NULL AUTO_INCREMENT,
  name       VARCHAR(32) NOT NULL COMMENT '设施名称',
  icon       VARCHAR(16)          DEFAULT NULL COMMENT '图标 emoji',
  sort       INT         NOT NULL DEFAULT 0 COMMENT '排序',
  created_at DATETIME             DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_name (name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='设施字典';

-- ------------------------------------------------------------
-- 房源表
-- ------------------------------------------------------------
CREATE TABLE homestay (
  id             BIGINT        NOT NULL AUTO_INCREMENT,
  host_id        BIGINT        NOT NULL COMMENT '房东用户 ID',
  title          VARCHAR(100)  NOT NULL COMMENT '房源标题',
  city           VARCHAR(50)   NOT NULL COMMENT '所在城市',
  address        VARCHAR(200)  NOT NULL COMMENT '详细地址',
  longitude      DECIMAL(10, 6)         DEFAULT NULL COMMENT '经度',
  latitude       DECIMAL(10, 6)         DEFAULT NULL COMMENT '纬度',
  cover_img      VARCHAR(500)           DEFAULT NULL COMMENT '封面图',
  images         TEXT                   DEFAULT NULL COMMENT '图片 URL 逗号分隔',
  type           VARCHAR(16)   NOT NULL DEFAULT '整租' COMMENT '整租/单间/公寓/别墅/小院',
  price          DECIMAL(10, 2) NOT NULL COMMENT '每晚价格',
  original_price DECIMAL(10, 2)         DEFAULT NULL COMMENT '划线价',
  area           DECIMAL(8, 2)          DEFAULT NULL COMMENT '面积(平米)',
  bedrooms       INT          NOT NULL DEFAULT 1 COMMENT '卧室数',
  living_rooms   INT          NOT NULL DEFAULT 1 COMMENT '客厅数',
  bathrooms      INT          NOT NULL DEFAULT 1 COMMENT '卫生间数',
  beds           INT          NOT NULL DEFAULT 1 COMMENT '床位数',
  max_guests     INT          NOT NULL DEFAULT 2 COMMENT '可住人数',
  floor_info     VARCHAR(20)            DEFAULT NULL COMMENT '楼层',
  description    TEXT                   DEFAULT NULL COMMENT '房源描述',
  rules          VARCHAR(1000)          DEFAULT NULL COMMENT '入住说明',
  status         VARCHAR(16)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED/OFFLINE',
  audit_remark   VARCHAR(255)           DEFAULT NULL COMMENT '审核备注/驳回原因',
  view_count     INT          NOT NULL DEFAULT 0 COMMENT '浏览量',
  favorite_count INT          NOT NULL DEFAULT 0 COMMENT '收藏数',
  order_count    INT          NOT NULL DEFAULT 0 COMMENT '成交订单数',
  rating         DECIMAL(2, 1) NOT NULL DEFAULT 0.0 COMMENT '平均评分',
  review_count   INT          NOT NULL DEFAULT 0 COMMENT '评价数',
  created_at     DATETIME               DEFAULT NULL,
  updated_at     DATETIME               DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_host (host_id),
  KEY idx_status_city (status, city),
  KEY idx_type (type),
  KEY idx_price (price),
  KEY idx_created (created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='房源表';

-- ------------------------------------------------------------
-- 房源-设施 关联
-- ------------------------------------------------------------
CREATE TABLE homestay_facility (
  id           BIGINT NOT NULL AUTO_INCREMENT,
  homestay_id  BIGINT NOT NULL,
  facility_id  BIGINT NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_homestay_facility (homestay_id, facility_id),
  KEY idx_facility (facility_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='房源设施关联';

-- ------------------------------------------------------------
-- 收藏
-- ------------------------------------------------------------
CREATE TABLE favorite (
  id           BIGINT NOT NULL AUTO_INCREMENT,
  user_id      BIGINT NOT NULL,
  homestay_id  BIGINT NOT NULL,
  created_at   DATETIME DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_homestay (user_id, homestay_id),
  KEY idx_homestay (homestay_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='收藏表';

-- ------------------------------------------------------------
-- 订单
-- ------------------------------------------------------------
CREATE TABLE booking_order (
  id              BIGINT        NOT NULL AUTO_INCREMENT,
  order_no        VARCHAR(32)   NOT NULL COMMENT '订单号',
  tenant_id       BIGINT        NOT NULL COMMENT '租客 ID',
  host_id         BIGINT        NOT NULL COMMENT '房东 ID',
  homestay_id     BIGINT        NOT NULL COMMENT '房源 ID',
  check_in_date   DATE          NOT NULL COMMENT '入住日期',
  check_out_date  DATE          NOT NULL COMMENT '退房日期',
  nights          INT           NOT NULL COMMENT '晚数',
  guest_count     INT           NOT NULL DEFAULT 1 COMMENT '入住人数',
  unit_price      DECIMAL(10, 2) NOT NULL COMMENT '下单时单价',
  total_amount    DECIMAL(10, 2) NOT NULL COMMENT '订单总额（优惠前）',
  coupon_id       BIGINT                 DEFAULT NULL COMMENT '使用的用户优惠券 ID（user_coupon.id）',
  coupon_name     VARCHAR(50)            DEFAULT NULL COMMENT '优惠券名称快照',
  discount_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '优惠券抵扣金额',
  pay_amount      DECIMAL(10, 2) NOT NULL COMMENT '实付金额（优惠后，即支付宝支付金额）',
  contact_name    VARCHAR(50)            DEFAULT NULL COMMENT '入住人',
  contact_phone   VARCHAR(20)            DEFAULT NULL COMMENT '联系电话',
  status          VARCHAR(16)   NOT NULL DEFAULT 'PENDING_PAY'
                  COMMENT 'PENDING_PAY/PAID/CHECKED_IN/COMPLETED/CANCELLED',
  pay_time        DATETIME               DEFAULT NULL COMMENT '支付时间',
  cancel_time     DATETIME               DEFAULT NULL COMMENT '取消时间',
  cancel_reason   VARCHAR(255)           DEFAULT NULL COMMENT '取消原因',
  reviewed        TINYINT       NOT NULL DEFAULT 0 COMMENT '是否已评价',
  created_at      DATETIME               DEFAULT NULL,
  updated_at      DATETIME               DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_order_no (order_no),
  KEY idx_tenant (tenant_id, status),
  KEY idx_host (host_id, status),
  KEY idx_homestay_date (homestay_id, check_in_date, check_out_date),
  KEY idx_status_created (status, created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='订单表';

-- ------------------------------------------------------------
-- 评价
-- ------------------------------------------------------------
CREATE TABLE review (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  order_id    BIGINT       NOT NULL COMMENT '订单 ID',
  homestay_id BIGINT       NOT NULL,
  tenant_id   BIGINT       NOT NULL,
  rating      INT          NOT NULL COMMENT '评分 1-5',
  content     VARCHAR(1000) NOT NULL COMMENT '评价内容',
  images      VARCHAR(1000) DEFAULT NULL COMMENT '评价图片逗号分隔',
  reply       VARCHAR(1000) DEFAULT NULL COMMENT '房东回复',
  reply_at    DATETIME      DEFAULT NULL,
  created_at  DATETIME      DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_order (order_id),
  KEY idx_homestay (homestay_id),
  KEY idx_tenant (tenant_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='评价表';

-- ------------------------------------------------------------
-- 轮播图
-- ------------------------------------------------------------
CREATE TABLE banner (
  id         BIGINT       NOT NULL AUTO_INCREMENT,
  title      VARCHAR(100) NOT NULL,
  image_url  VARCHAR(500) NOT NULL,
  link       VARCHAR(500) DEFAULT NULL,
  sort       INT          NOT NULL DEFAULT 0,
  status     TINYINT      NOT NULL DEFAULT 1 COMMENT '0 下架 1 启用',
  created_at DATETIME     DEFAULT NULL,
  updated_at DATETIME     DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='轮播图';

-- ------------------------------------------------------------
-- 公告
-- ------------------------------------------------------------
CREATE TABLE notice (
  id         BIGINT       NOT NULL AUTO_INCREMENT,
  title      VARCHAR(200) NOT NULL,
  content    TEXT         NOT NULL,
  status     TINYINT      NOT NULL DEFAULT 1 COMMENT '0 下架 1 启用',
  created_at DATETIME     DEFAULT NULL,
  updated_at DATETIME     DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='公告表';

-- ------------------------------------------------------------
-- 优惠券模板（管理员发布）
-- ------------------------------------------------------------
CREATE TABLE coupon (
  id                  BIGINT        NOT NULL AUTO_INCREMENT,
  name                VARCHAR(50)   NOT NULL COMMENT '优惠券名称',
  type                VARCHAR(16)   NOT NULL COMMENT 'CASH 代金券/FULL_REDUCE 满减券/DISCOUNT 折扣券',
  threshold           DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '使用门槛：订单满多少元可用，0 为无门槛',
  discount_amount     DECIMAL(10,2)          DEFAULT NULL COMMENT '代金/满减券的优惠金额',
  discount_percent    INT                    DEFAULT NULL COMMENT '折扣券折扣率：88 表示 8.8 折',
  max_discount_amount DECIMAL(10,2)          DEFAULT NULL COMMENT '折扣券优惠金额封顶，NULL 不封顶',
  total_count         INT           NOT NULL DEFAULT 0 COMMENT '发行总量，-1 表示不限量',
  received_count      INT           NOT NULL DEFAULT 0 COMMENT '累计已领取数量',
  used_count          INT           NOT NULL DEFAULT 0 COMMENT '累计已使用数量',
  per_limit           INT           NOT NULL DEFAULT 1 COMMENT '每人限领数量',
  valid_type          VARCHAR(8)    NOT NULL DEFAULT 'FIXED' COMMENT 'FIXED 固定有效期 / DAYS 领取后 N 天有效',
  valid_start_time    DATETIME               DEFAULT NULL COMMENT '固定有效期开始（FIXED）',
  valid_end_time      DATETIME               DEFAULT NULL COMMENT '固定有效期结束（FIXED）',
  valid_days          INT                    DEFAULT NULL COMMENT '领取后有效天数（DAYS）',
  status              TINYINT       NOT NULL DEFAULT 1 COMMENT '0 下架 1 上架',
  created_at          DATETIME               DEFAULT NULL,
  updated_at          DATETIME               DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_status (status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='优惠券模板';

-- ------------------------------------------------------------
-- 用户优惠券（领取记录 + 使用状态）
-- ------------------------------------------------------------
CREATE TABLE user_coupon (
  id               BIGINT      NOT NULL AUTO_INCREMENT,
  user_id          BIGINT      NOT NULL COMMENT '领取用户 ID',
  coupon_id        BIGINT      NOT NULL COMMENT '优惠券模板 ID',
  status           VARCHAR(8)  NOT NULL DEFAULT 'UNUSED' COMMENT 'UNUSED 未使用 / USED 已使用',
  order_id         BIGINT               DEFAULT NULL COMMENT '核销订单 ID',
  receive_time     DATETIME    NOT NULL COMMENT '领取时间',
  valid_start_time DATETIME    NOT NULL COMMENT '本券有效期开始（领取时按规则快照）',
  valid_end_time   DATETIME    NOT NULL COMMENT '本券有效期结束（领取时按规则快照）',
  used_time        DATETIME             DEFAULT NULL COMMENT '核销时间',
  created_at       DATETIME             DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_user_status (user_id, status),
  KEY idx_coupon (coupon_id),
  KEY idx_order (order_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='用户优惠券';
