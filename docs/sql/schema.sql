-- ============================================================
-- 外卖订餐系统 MySQL 8.0 建库建表脚本（生产环境）
-- 执行方式：mysql -uroot -p < schema.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS o2o DEFAULT CHARACTER SET utf8mb4 DEFAULT COLLATE utf8mb4_unicode_ci;
USE o2o;

DROP TABLE IF EXISTS test_notice;
DROP TABLE IF EXISTS test_cart;
DROP TABLE IF EXISTS test_stock_log;
DROP TABLE IF EXISTS test_order_item;
DROP TABLE IF EXISTS test_orders;
DROP TABLE IF EXISTS test_address;
DROP TABLE IF EXISTS test_dish;
DROP TABLE IF EXISTS test_category;
DROP TABLE IF EXISTS test_employee;
DROP TABLE IF EXISTS test_user;

-- 用户表
CREATE TABLE test_user (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username      VARCHAR(32)  NOT NULL COMMENT '用户名',
    phone         VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    password_hash VARCHAR(100) NOT NULL COMMENT 'BCrypt 密码密文',
    nickname      VARCHAR(32)  DEFAULT NULL COMMENT '昵称',
    created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_username (username),
    UNIQUE KEY uk_user_phone (phone)
) COMMENT='用户表';

-- 员工（管理端）表
CREATE TABLE test_employee (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username      VARCHAR(32)  NOT NULL COMMENT '登录账号',
    password_hash VARCHAR(100) NOT NULL COMMENT 'BCrypt 密码密文',
    real_name     VARCHAR(32)  DEFAULT NULL COMMENT '真实姓名',
    phone         VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    status        TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 1启用/0禁用',
    PRIMARY KEY (id),
    UNIQUE KEY uk_test_employee_username (username)
) COMMENT='员工表';

-- 菜品分类表
CREATE TABLE test_category (
    id         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    name       VARCHAR(32) NOT NULL COMMENT '分类名称',
    sort_order INT         NOT NULL DEFAULT 0 COMMENT '排序值（小的在前）',
    status     TINYINT     NOT NULL DEFAULT 1 COMMENT '状态 1启用/0禁用',
    PRIMARY KEY (id),
    UNIQUE KEY uk_test_category_name (name)
) COMMENT='菜品分类表';

-- 菜品表
CREATE TABLE test_dish (
    id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    category_id BIGINT        NOT NULL COMMENT '分类ID',
    name        VARCHAR(64)   NOT NULL COMMENT '菜品名称',
    description VARCHAR(255)  DEFAULT NULL COMMENT '菜品描述',
    price       DECIMAL(10,2) NOT NULL COMMENT '价格（元）',
    image_url   VARCHAR(255)  DEFAULT NULL COMMENT '图片地址',
    stock       INT           NOT NULL DEFAULT 0 COMMENT '库存',
    status      TINYINT       NOT NULL DEFAULT 1 COMMENT '状态 1上架/0下架',
    sales       INT           NOT NULL DEFAULT 0 COMMENT '累计销量',
    PRIMARY KEY (id),
    KEY idx_test_dish_category_status (category_id, status),
    KEY idx_test_dish_name (name)
) COMMENT='菜品表';

-- 收货地址表
CREATE TABLE test_address (
    id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id    BIGINT       NOT NULL COMMENT '用户ID',
    consignee  VARCHAR(32)  NOT NULL COMMENT '收货人',
    phone      VARCHAR(20)  NOT NULL COMMENT '收货电话',
    region     VARCHAR(64)  DEFAULT NULL COMMENT '省市区',
    detail     VARCHAR(255) NOT NULL COMMENT '详细地址',
    is_default TINYINT      NOT NULL DEFAULT 0 COMMENT '默认地址 1是/0否',
    PRIMARY KEY (id),
    KEY idx_test_address_user (user_id)
) COMMENT='收货地址表';

-- 订单表
CREATE TABLE test_orders (
    id               BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    order_no         VARCHAR(32)   NOT NULL COMMENT '订单号',
    user_id          BIGINT        NOT NULL COMMENT '下单用户ID',
    total_amount     DECIMAL(10,2) NOT NULL COMMENT '订单总额（元）',
    pay_method       TINYINT       DEFAULT NULL COMMENT '支付方式 1微信/2支付宝/3货到付款',
    pay_status       TINYINT       NOT NULL DEFAULT 0 COMMENT '支付状态 0待支付/1已支付',
    order_status     TINYINT       NOT NULL DEFAULT 0 COMMENT '订单状态 0待接单/1制作中/2配送中/3已完成/4已取消',
    address_snapshot VARCHAR(500)  DEFAULT NULL COMMENT '收货地址快照',
    remark           VARCHAR(255)  DEFAULT NULL COMMENT '备注',
    created_at       DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    completed_at     DATETIME      DEFAULT NULL COMMENT '完成时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_test_orders_order_no (order_no),
    KEY idx_test_orders_user_created (user_id, created_at),
    KEY idx_test_orders_status (order_status)
) COMMENT='订单表';

-- 订单明细表
CREATE TABLE test_order_item (
    id         BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    order_id   BIGINT        NOT NULL COMMENT '订单ID',
    dish_id    BIGINT        NOT NULL COMMENT '菜品ID',
    dish_name  VARCHAR(64)   NOT NULL COMMENT '菜品名称快照',
    dish_image VARCHAR(255)  DEFAULT NULL COMMENT '菜品图片快照',
    price      DECIMAL(10,2) NOT NULL COMMENT '成交单价（元）',
    quantity   INT           NOT NULL COMMENT '数量',
    subtotal   DECIMAL(10,2) NOT NULL COMMENT '小计（元）',
    PRIMARY KEY (id),
    KEY idx_order_item_order (order_id)
) COMMENT='订单明细表';

-- 库存流水表
CREATE TABLE test_stock_log (
    id             BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    dish_id        BIGINT      NOT NULL COMMENT '菜品ID',
    change_type    TINYINT     NOT NULL COMMENT '类型 1下单扣减/2取消回补/3后台调整',
    quantity_delta INT         NOT NULL COMMENT '变动数量（负为扣减）',
    order_no       VARCHAR(32) DEFAULT NULL COMMENT '关联订单号',
    operator       VARCHAR(32) DEFAULT NULL COMMENT '操作人',
    created_at     DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '发生时间',
    PRIMARY KEY (id)
) COMMENT='库存流水表';

-- 购物车表
CREATE TABLE test_cart (
    id       BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id  BIGINT NOT NULL COMMENT '用户ID',
    dish_id  BIGINT NOT NULL COMMENT '菜品ID',
    quantity INT    NOT NULL DEFAULT 1 COMMENT '数量',
    PRIMARY KEY (id),
    UNIQUE KEY uk_cart_user_dish (user_id, dish_id)
) COMMENT='购物车表';

-- 公告表
CREATE TABLE test_notice (
    id         BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    title      VARCHAR(100)  NOT NULL COMMENT '标题',
    content    VARCHAR(1000) DEFAULT NULL COMMENT '内容',
    status     TINYINT       NOT NULL DEFAULT 1 COMMENT '状态 1发布/0下线',
    created_at DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    PRIMARY KEY (id)
) COMMENT='公告表';
