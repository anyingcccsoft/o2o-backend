-- ============================================================
-- 外卖订餐系统种子数据（MySQL 8.0 版，与 src/main/resources/data.sql 等价）
-- 执行方式：先执行 schema.sql，再执行 mysql -uroot -p o2o < init_data.sql
-- 账号：管理员 admin/123456，测试用户 test/123456（BCrypt 密文）
-- ============================================================

USE o2o;


-- 管理员（admin / 123456）
INSERT INTO test_employee (username, password_hash, real_name, phone, status) VALUES
    ('admin', '$2a$10$CBNleXeyivMS9lPEjzJjO.WjDY4/p84BPI9k4n5JSaexzNo6pySN2', '系统管理员', '13900000001', 1);

-- 测试用户（test / 123456）
INSERT INTO `test_user` (username, phone, password_hash, nickname) VALUES
    ('test', '13800138000', '$2a$10$hxtyUkuCQiGkLg8oCsATvuzkpqvYCDOmL4.XX8x7ZWz/.laUsypL.', '测试用户');

-- 菜品分类
INSERT INTO test_category (name, sort_order, status) VALUES
                                                         ('热菜', 1, 1),
                                                         ('凉菜', 2, 1),
                                                         ('主食', 3, 1),
                                                         ('饮品', 4, 1);

-- 菜品（13 道，库存 50-200，图片为内置 SVG 占位图）
INSERT INTO test_dish (category_id, name, description, price, image_url, stock, status, sales) VALUES
                                                                                                   (1, '宫保鸡丁', '经典川菜，鸡肉鲜嫩，花生香脆，微辣回甜', 28.00, '/images/dish/gongbaojiding.svg', 120, 1, 156),
                                                                                                   (1, '鱼香肉丝', '咸甜酸辣兼备，肉丝滑嫩，下饭首选', 26.00, '/images/dish/yuxiangrousi.svg', 100, 1, 132),
                                                                                                   (1, '麻婆豆腐', '麻辣鲜香，豆腐嫩滑，川味十足', 18.00, '/images/dish/mapodoufu.svg', 150, 1, 201),
                                                                                                   (1, '红烧肉', '肥而不腻，入口即化，浓油赤酱本帮风味', 38.00, '/images/dish/hongshaorou.svg', 80, 1, 98),
                                                                                                   (2, '凉拌黄瓜', '清脆爽口，蒜香开胃，夏日解腻小菜', 10.00, '/images/dish/liangbanhuanggua.svg', 200, 1, 76),
                                                                                                   (2, '口水鸡', '红油飘香，鸡肉嫩滑，麻辣过瘾', 24.00, '/images/dish/koushuiji.svg', 90, 1, 88),
                                                                                                   (2, '皮蛋豆腐', '皮蛋香浓配嫩豆腐，简单美味', 12.00, '/images/dish/pidandoufu.svg', 160, 1, 54),
                                                                                                   (3, '扬州炒饭', '粒粒分明，配料丰富，蛋香浓郁', 16.00, '/images/dish/yangzhouchaofan.svg', 180, 1, 245),
                                                                                                   (3, '牛肉拉面', '汤清面劲，牛肉大块，暖胃管饱', 20.00, '/images/dish/niuroulamian.svg', 140, 1, 187),
                                                                                                   (3, '鲜虾云吞', '整只鲜虾入馅，皮薄馅足，汤鲜味美', 22.00, '/images/dish/xianxiayuntun.svg', 110, 1, 121),
                                                                                                   (4, '冰爽柠檬水', '新鲜柠檬现切现泡，酸甜解渴', 8.00, '/images/dish/ningmengshui.svg', 200, 1, 310),
                                                                                                   (4, '珍珠奶茶', '香浓奶茶配 Q 弹珍珠，经典必点', 12.00, '/images/dish/zhenzhunaicha.svg', 190, 1, 268),
                                                                                                   (4, '鲜榨橙汁', '每日鲜榨，无添加，维 C 满满', 15.00, '/images/dish/xianzhachengzhi.svg', 150, 1, 175);

-- 公告
INSERT INTO test_notice (title, content, status) VALUES
                                                     ('新店开业，全场特惠', '本店线上订餐系统正式上线！新用户下单立享优惠，欢迎品尝招牌红烧肉与宫保鸡丁。', 1),
                                                     ('配送时间调整通知', '即日起配送时间为每日 10:00-21:00，超出时段的订单将于次日优先配送，敬请谅解。', 1);

-- 测试用户默认地址
INSERT INTO test_address (user_id, consignee, phone, region, detail, is_default) VALUES
    (1, '张小测', '13800138000', '上海市宝山区', '上大路99号上海大学东区宿舍1号楼', 1);


