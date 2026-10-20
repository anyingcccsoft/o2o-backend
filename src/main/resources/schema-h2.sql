-- ============================================================
-- 外卖订餐系统 H2 建表脚本（MODE=MySQL，dev profile 启动时自动执行）
-- 与 docs/sql/schema.sql（MySQL 8.0 版）保持结构一致
-- ============================================================

DROP TABLE IF EXISTS notice;
DROP TABLE IF EXISTS cart;
DROP TABLE IF EXISTS stock_log;
DROP TABLE IF EXISTS order_item;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS address;
DROP TABLE IF EXISTS dish;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS employee;
DROP TABLE IF EXISTS `user`;

-- 用户表
CREATE TABLE `user` (
    id            BIGINT       AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(32)  NOT NULL,
    phone         VARCHAR(20),
    password_hash VARCHAR(100) NOT NULL,
    nickname      VARCHAR(32),
    created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_username UNIQUE (username),
    CONSTRAINT uk_user_phone UNIQUE (phone)
);

-- 员工（管理端）表
CREATE TABLE employee (
    id            BIGINT       AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(32)  NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    real_name     VARCHAR(32),
    phone         VARCHAR(20),
    status        TINYINT      NOT NULL DEFAULT 1,
    CONSTRAINT uk_employee_username UNIQUE (username)
);

-- 菜品分类表
CREATE TABLE category (
    id         BIGINT      AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(32) NOT NULL,
    sort_order INT         NOT NULL DEFAULT 0,
    status     TINYINT     NOT NULL DEFAULT 1,
    CONSTRAINT uk_category_name UNIQUE (name)
);

-- 菜品表
CREATE TABLE dish (
    id          BIGINT        AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT        NOT NULL,
    name        VARCHAR(64)   NOT NULL,
    description VARCHAR(255),
    price       DECIMAL(10,2) NOT NULL,
    image_url   VARCHAR(255),
    stock       INT           NOT NULL DEFAULT 0,
    status      TINYINT       NOT NULL DEFAULT 1,
    sales       INT           NOT NULL DEFAULT 0
);
CREATE INDEX idx_dish_category_status ON dish (category_id, status);
CREATE INDEX idx_dish_name ON dish (name);

-- 收货地址表
CREATE TABLE address (
    id         BIGINT       AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT       NOT NULL,
    consignee  VARCHAR(32)  NOT NULL,
    phone      VARCHAR(20)  NOT NULL,
    region     VARCHAR(64),
    detail     VARCHAR(255) NOT NULL,
    is_default TINYINT      NOT NULL DEFAULT 0
);
CREATE INDEX idx_address_user ON address (user_id);

-- 订单表
CREATE TABLE orders (
    id               BIGINT        AUTO_INCREMENT PRIMARY KEY,
    order_no         VARCHAR(32)   NOT NULL,
    user_id          BIGINT        NOT NULL,
    total_amount     DECIMAL(10,2) NOT NULL,
    pay_method       TINYINT,
    pay_status       TINYINT       NOT NULL DEFAULT 0,
    order_status     TINYINT       NOT NULL DEFAULT 0,
    address_snapshot VARCHAR(500),
    remark           VARCHAR(255),
    created_at       DATETIME      DEFAULT CURRENT_TIMESTAMP,
    completed_at     DATETIME,
    CONSTRAINT uk_orders_order_no UNIQUE (order_no)
);
CREATE INDEX idx_orders_user_created ON orders (user_id, created_at);
CREATE INDEX idx_orders_status ON orders (order_status);

-- 订单明细表
CREATE TABLE order_item (
    id         BIGINT        AUTO_INCREMENT PRIMARY KEY,
    order_id   BIGINT        NOT NULL,
    dish_id    BIGINT        NOT NULL,
    dish_name  VARCHAR(64)   NOT NULL,
    dish_image VARCHAR(255),
    price      DECIMAL(10,2) NOT NULL,
    quantity   INT           NOT NULL,
    subtotal   DECIMAL(10,2) NOT NULL
);
CREATE INDEX idx_order_item_order ON order_item (order_id);

-- 库存流水表（1下单扣减 / 2取消回补 / 3后台调整）
CREATE TABLE stock_log (
    id             BIGINT      AUTO_INCREMENT PRIMARY KEY,
    dish_id        BIGINT      NOT NULL,
    change_type    TINYINT     NOT NULL,
    quantity_delta INT         NOT NULL,
    order_no       VARCHAR(32),
    operator       VARCHAR(32),
    created_at     DATETIME    DEFAULT CURRENT_TIMESTAMP
);

-- 购物车表
CREATE TABLE cart (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id  BIGINT NOT NULL,
    dish_id  BIGINT NOT NULL,
    quantity INT    NOT NULL DEFAULT 1,
    CONSTRAINT uk_cart_user_dish UNIQUE (user_id, dish_id)
);

-- 公告表
CREATE TABLE notice (
    id         BIGINT       AUTO_INCREMENT PRIMARY KEY,
    title      VARCHAR(100) NOT NULL,
    content    VARCHAR(1000),
    status     TINYINT      NOT NULL DEFAULT 1,
    created_at DATETIME     DEFAULT CURRENT_TIMESTAMP
);
