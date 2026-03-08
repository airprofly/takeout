-- 初始化外卖系统数据
USE `takeout`;

-- ==================== 员工数据 ====================
INSERT INTO `employee` (`id`, `name`, `username`, `password`, `phone`, `sex`, `id_number`, `status`, `create_time`, `update_time`) VALUES
(1, '管理员', 'admin', 'e10adc3949ba59abbe56e057f20f883e', '13800138000', '1', '110101199001011234', 1, NOW(), NOW()),
(2, '张经理', 'manager', 'e10adc3949ba59abbe56e057f20f883e', '13900139000', '1', '110101199001011235', 1, NOW(), NOW()),
(3, '李厨师', 'cook', 'e10adc3949ba59abbe56e057f20f883e', '13700137000', '1', '110101199001011236', 1, NOW(), NOW()),
(4, '王收银', 'cashier', 'e10adc3949ba59abbe56e057f20f883e', '13600136000', '2', '110101199001011237', 1, NOW(), NOW()),
(5, '赵配送', 'delivery', 'e10adc3949ba59abbe56e057f20f883e', '13500135000', '1', '110101199001011238', 1, NOW(), NOW());

-- ==================== 分类数据 ====================
INSERT INTO `category` (`id`, `type`, `name`, `sort`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES
-- 菜品分类 (type=1)
(1, 1, '热销菜品', 1, 1, NOW(), NOW(), 1, 1),
(2, 1, '主食套餐', 2, 1, NOW(), NOW(), 1, 1),
(3, 1, '凉菜', 3, 1, NOW(), NOW(), 1, 1),
(4, 1, '汤类', 4, 1, NOW(), NOW(), 1, 1),
(5, 1, '饮料', 5, 1, NOW(), NOW(), 1, 1),
(6, 1, '小吃', 6, 1, NOW(), NOW(), 1, 1),
(7, 1, '甜品', 7, 1, NOW(), NOW(), 1, 1),
-- 套餐分类 (type=2)
(11, 2, '商务套餐', 1, 1, NOW(), NOW(), 1, 1),
(12, 2, '情侣套餐', 2, 1, NOW(), NOW(), 1, 1),
(13, 2, '家庭套餐', 3, 1, NOW(), NOW(), 1, 1),
(14, 2, '单人套餐', 4, 1, NOW(), NOW(), 1, 1);

-- ==================== 菜品数据 ====================
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES
-- 热销菜品
(1, '宫保鸡丁', 1, 38.00, 'https://example.com/images/dish1.jpg', '经典川菜,鸡肉嫩滑,花生香脆', 1, NOW(), NOW(), 1, 1),
(2, '鱼香肉丝', 1, 32.00, 'https://example.com/images/dish2.jpg', '酸甜可口,下饭神器', 1, NOW(), NOW(), 1, 1),
(3, '麻婆豆腐', 1, 28.00, 'https://example.com/images/dish3.jpg', '麻辣鲜香,豆腐嫩滑', 1, NOW(), NOW(), 1, 1),
(4, '水煮鱼', 1, 58.00, 'https://example.com/images/dish4.jpg', '鱼肉鲜嫩,麻辣过瘾', 1, NOW(), NOW(), 1, 1),
(5, '回锅肉', 1, 42.00, 'https://example.com/images/dish5.jpg', '四川名菜,肥而不腻', 1, NOW(), NOW(), 1, 1),
-- 主食套餐
(6, '扬州炒饭', 2, 22.00, 'https://example.com/images/dish6.jpg', '粒粒分明,配料丰富', 1, NOW(), NOW(), 1, 1),
(7, '牛肉炒河粉', 2, 26.00, 'https://example.com/images/dish7.jpg', '河粉滑嫩,牛肉鲜美', 1, NOW(), NOW(), 1, 1),
(8, '红烧肉盖饭', 2, 32.00, 'https://example.com/images/dish8.jpg', '肥瘦相间,入口即化', 1, NOW(), NOW(), 1, 1),
-- 凉菜
(11, '拍黄瓜', 3, 12.00, 'https://example.com/images/dish11.jpg', '清爽解腻,开胃小菜', 1, NOW(), NOW(), 1, 1),
(12, '凉拌木耳', 3, 16.00, 'https://example.com/images/dish12.jpg', '口感爽脆,营养丰富', 1, NOW(), NOW(), 1, 1),
(13, '口水鸡', 3, 28.00, 'https://example.com/images/dish13.jpg', '麻辣鲜香,嫩滑爽口', 1, NOW(), NOW(), 1, 1),
-- 汤类
(14, '番茄鸡蛋汤', 4, 12.00, 'https://example.com/images/dish14.jpg', '酸甜开胃,老少皆宜', 1, NOW(), NOW(), 1, 1),
(15, '冬瓜排骨汤', 4, 38.00, 'https://example.com/images/dish15.jpg', '清淡营养,清热解暑', 1, NOW(), NOW(), 1, 1),
(16, '紫菜蛋花汤', 4, 10.00, 'https://example.com/images/dish16.jpg', '简单美味,营养健康', 1, NOW(), NOW(), 1, 1),
-- 饮料
(17, '鲜榨橙汁', 5, 18.00, 'https://example.com/images/dish17.jpg', '100%鲜榨,维C丰富', 1, NOW(), NOW(), 1, 1),
(18, '柠檬蜂蜜茶', 5, 15.00, 'https://example.com/images/dish18.jpg', '酸甜可口,清热润喉', 1, NOW(), NOW(), 1, 1),
(19, '酸梅汤', 5, 8.00, 'https://example.com/images/dish19.jpg', '传统解暑饮品', 1, NOW(), NOW(), 1, 1),
(20, '可乐', 5, 5.00, 'https://example.com/images/dish20.jpg', '冰爽快乐水', 1, NOW(), NOW(), 1, 1),
-- 小吃
(21, '炸鸡翅', 6, 18.00, 'https://example.com/images/dish21.jpg', '外酥里嫩,香辣可口', 1, NOW(), NOW(), 1, 1),
(22, '薯条', 6, 12.00, 'https://example.com/images/dish22.jpg', '金黄酥脆', 1, NOW(), NOW(), 1, 1),
(23, '鸡米花', 6, 15.00, 'https://example.com/images/dish23.jpg', '一口一个,美味无穷', 1, NOW(), NOW(), 1, 1),
-- 甜品
(24, '红糖冰粉', 7, 12.00, 'https://example.com/images/dish24.jpg', '清凉解暑,口感爽滑', 1, NOW(), NOW(), 1, 1),
(25, '芒果布丁', 7, 16.00, 'https://example.com/images/dish25.jpg', '香滑爽口,芒果香甜', 1, NOW(), NOW(), 1, 1),
(26, '双皮奶', 7, 14.00, 'https://example.com/images/dish26.jpg', '奶香浓郁,口感顺滑', 1, NOW(), NOW(), 1, 1);

-- ==================== 菜品口味数据 ====================
INSERT INTO `dish_flavor` (`id`, `dish_id`, `name`, `value`) VALUES
-- 宫保鸡丁口味
(1, 1, '辣度', '["不辣","微辣","中辣","特辣"]'),
(2, 1, '份量', '["小份","标准份","大份"]'),
-- 鱼香肉丝口味
(3, 2, '辣度', '["不辣","微辣","中辣"]'),
(4, 2, '份量', '["小份","标准份","大份"]'),
-- 麻婆豆腐口味
(5, 3, '辣度', '["中辣","特辣"]'),
(6, 3, '份量', '["小份","标准份","大份"]'),
-- 水煮鱼口味
(7, 4, '辣度', '["中辣","特辣"]'),
(8, 4, '份量', '["标准份","大份"]'),
-- 扬州炒饭口味
(9, 6, '份量', '["小份","标准份","大份"]'),
-- 牛肉炒河粉口味
(10, 7, '辣度', '["不辣","微辣"]'),
(11, 7, '份量', '["小份","标准份","大份"]'),
-- 红烧肉盖饭口味
(12, 8, '份量', '["标准份","大份"]'),
-- 饮料温度
(13, 17, '温度', '["冰","常温","热"]'),
(14, 18, '温度', '["冰","常温","热"]'),
(15, 19, '温度', '["冰","常温"]'),
(16, 20, '温度', '["冰","常温"]');

-- ==================== 套餐数据 ====================
INSERT INTO `setmeal` (`id`, `category_id`, `name`, `price`, `status`, `description`, `image`, `create_time`, `update_time`, `create_user`, `update_user`) VALUES
(1, 11, '商务A套餐', 68.00, 1, '适合商务人士的营养套餐', 'https://example.com/images/setmeal1.jpg', NOW(), NOW(), 1, 1),
(2, 11, '商务B套餐', 78.00, 1, '高端商务套餐,彰显品味', 'https://example.com/images/setmeal2.jpg', NOW(), NOW(), 1, 1),
(3, 12, '情侣甜蜜套餐', 98.00, 1, '浪漫双人套餐', 'https://example.com/images/setmeal3.jpg', NOW(), NOW(), 1, 1),
(4, 13, '家庭欢乐套餐', 168.00, 1, '适合3-4人享用的家庭套餐', 'https://example.com/images/setmeal4.jpg', NOW(), NOW(), 1, 1),
(5, 14, '单人工作套餐', 28.00, 1, '经济实惠的一人套餐', 'https://example.com/images/setmeal5.jpg', NOW(), NOW(), 1, 1),
(6, 14, '单人豪华套餐', 45.00, 1, '丰富营养的单人套餐', 'https://example.com/images/setmeal6.jpg', NOW(), NOW(), 1, 1);

-- ==================== 套餐菜品关系数据 ====================
INSERT INTO `setmeal_dish` (`id`, `setmeal_id`, `dish_id`, `name`, `price`, `copies`) VALUES
-- 商务A套餐 (宫保鸡丁 + 紫菜蛋花汤 + 米饭 + 酸梅汤)
(1, 1, 1, '宫保鸡丁', 38.00, 1),
(2, 1, 16, '紫菜蛋花汤', 10.00, 1),
(3, 1, 19, '酸梅汤', 8.00, 1),
-- 商务B套餐 (水煮鱼 + 冬瓜排骨汤 + 米饭 + 鲜榨橙汁)
(4, 2, 4, '水煮鱼', 58.00, 1),
(5, 2, 15, '冬瓜排骨汤', 38.00, 1),
(6, 2, 17, '鲜榨橙汁', 18.00, 1),
-- 情侣甜蜜套餐 (鱼香肉丝 + 麻婆豆腐 + 拍黄瓜 + 番茄鸡蛋汤 + 2杯柠檬蜂蜜茶)
(7, 3, 2, '鱼香肉丝', 32.00, 1),
(8, 3, 3, '麻婆豆腐', 28.00, 1),
(9, 3, 11, '拍黄瓜', 12.00, 1),
(10, 3, 14, '番茄鸡蛋汤', 12.00, 1),
(11, 3, 18, '柠檬蜂蜜茶', 15.00, 2),
-- 家庭欢乐套餐 (回锅肉 + 红烧肉盖饭 + 扬州炒饭 + 凉拌木耳 + 口水鸡 + 冬瓜排骨汤 + 4杯可乐)
(12, 4, 5, '回锅肉', 42.00, 1),
(13, 4, 8, '红烧肉盖饭', 32.00, 1),
(14, 4, 6, '扬州炒饭', 22.00, 1),
(15, 4, 12, '凉拌木耳', 16.00, 1),
(16, 4, 13, '口水鸡', 28.00, 1),
(17, 4, 15, '冬瓜排骨汤', 38.00, 1),
(18, 4, 20, '可乐', 5.00, 4),
-- 单人工作套餐 (麻婆豆腐 + 紫菜蛋花汤 + 米饭)
(19, 5, 3, '麻婆豆腐', 28.00, 1),
(20, 5, 16, '紫菜蛋花汤', 10.00, 1),
-- 单人豪华套餐 (宫保鸡丁 + 拍黄瓜 + 番茄鸡蛋汤 + 鲜榨橙汁)
(21, 6, 1, '宫保鸡丁', 38.00, 1),
(22, 6, 11, '拍黄瓜', 12.00, 1),
(23, 6, 14, '番茄鸡蛋汤', 12.00, 1),
(24, 6, 17, '鲜榨橙汁', 18.00, 1);

-- ==================== 用户数据 ====================
INSERT INTO `user` (`id`, `openid`, `name`, `phone`, `sex`, `id_number`, `avatar`, `create_time`) VALUES
(1, 'wx_openid_001', '张三', '13800000001', '1', '110101199001011111', 'https://example.com/avatars/user1.jpg', NOW()),
(2, 'wx_openid_002', '李四', '13800000002', '2', '110101199001011112', 'https://example.com/avatars/user2.jpg', NOW()),
(3, 'wx_openid_003', '王五', '13800000003', '1', '110101199001011113', 'https://example.com/avatars/user3.jpg', NOW()),
(4, 'wx_openid_004', '赵六', '13800000004', '2', '110101199001011114', 'https://example.com/avatars/user4.jpg', NOW());

-- ==================== 地址簿数据 ====================
INSERT INTO `address_book` (`id`, `user_id`, `consignee`, `sex`, `phone`, `province_code`, `province_name`, `city_code`, `city_name`, `district_code`, `district_name`, `detail`, `label`, `is_default`) VALUES
(1, 1, '张三', '1', '13800000001', '110000', '北京市', '110100', '北京市', '110101', '东城区', '东长安街1号', '家', 1),
(2, 1, '张三', '1', '13800000001', '110000', '北京市', '110100', '北京市', '110102', '西城区', '西单北大街1号', '公司', 0),
(3, 2, '李四', '2', '13800000002', '310000', '上海市', '310100', '上海市', '310104', '徐汇区', '漕河泾开发区1号', '公司', 1),
(4, 2, '李四', '2', '13800000002', '310000', '上海市', '310100', '上海市', '310115', '浦东新区', '世纪大道1号', '家', 0),
(5, 3, '王五', '1', '13800000003', '440000', '广东省', '440100', '广州市', '440103', '荔湾区', '中山一路1号', '家', 1),
(6, 4, '赵六', '2', '13800000004', '440000', '广东省', '440300', '深圳市', '440304', '福田区', '深南大道1号', '公司', 1);

-- ==================== 购物车数据 ====================
INSERT INTO `shopping_cart` (`id`, `name`, `image`, `user_id`, `dish_id`, `setmeal_id`, `dish_flavor`, `number`, `amount`, `create_time`) VALUES
(1, '宫保鸡丁', 'https://example.com/images/dish1.jpg', 1, 1, NULL, '中辣', 2, 76.00, NOW()),
(2, '番茄鸡蛋汤', 'https://example.com/images/dish14.jpg', 1, 14, NULL, NULL, 1, 12.00, NOW()),
(3, '商务A套餐', 'https://example.com/images/setmeal1.jpg', 2, NULL, 1, NULL, 1, 68.00, NOW()),
(4, '麻婆豆腐', 'https://example.com/images/dish3.jpg', 3, 3, NULL, '特辣', 1, 28.00, NOW()),
(5, '鲜榨橙汁', 'https://example.com/images/dish17.jpg', 3, 17, NULL, '冰', 2, 36.00, NOW()),
(6, '情侣甜蜜套餐', 'https://example.com/images/setmeal3.jpg', 4, NULL, 3, NULL, 1, 98.00, NOW()),
(7, '炸鸡翅', 'https://example.com/images/dish21.jpg', 1, 21, NULL, NULL, 3, 54.00, NOW()),
(8, '可乐', 'https://example.com/images/dish20.jpg', 2, 20, NULL, '冰', 4, 20.00, NOW());

-- ==================== 订单数据 ====================
INSERT INTO `orders` (`id`, `number`, `status`, `user_id`, `address_book_id`, `order_time`, `checkout_time`, `pay_method`, `pay_status`, `amount`, `remark`, `phone`, `address`, `user_name`, `consignee`, `estimated_delivery_time`, `delivery_status`, `pack_amount`, `tableware_number`, `tableware_status`) VALUES
(1, 'ORD20260308001', 5, 1, 1, '2026-03-08 11:30:00', '2026-03-08 11:35:00', 1, 1, 88.00, '少放辣', '13800000001', '北京市东城区东长安街1号', '张三', '张三', '2026-03-08 12:30:00', 1, 2, 2, 1),
(2, 'ORD20260308002', 4, 2, 3, '2026-03-08 12:00:00', '2026-03-08 12:05:00', 2, 1, 68.00, NULL, '13800000002', '上海市徐汇区漕河泾开发区1号', '李四', '李四', '2026-03-08 13:00:00', 1, 0, 1, 1),
(3, 'ORD20260308003', 3, 3, 5, '2026-03-08 12:15:00', NULL, 1, 1, 64.00, '不要香菜', '13800000003', '广东省广州市荔湾区中山一路1号', '王五', '王五', '2026-03-08 13:15:00', 1, 3, 1, 1);

-- ==================== 订单明细数据 ====================
INSERT INTO `order_detail` (`id`, `name`, `image`, `order_id`, `dish_id`, `setmeal_id`, `dish_flavor`, `number`, `amount`) VALUES
-- 订单1的明细: 2份宫保鸡丁 + 1份番茄鸡蛋汤
(1, '宫保鸡丁', 'https://example.com/images/dish1.jpg', 1, 1, NULL, '中辣', 2, 76.00),
(2, '番茄鸡蛋汤', 'https://example.com/images/dish14.jpg', 1, 14, NULL, NULL, 1, 12.00),
-- 订单2的明细: 1份商务A套餐
(3, '商务A套餐', 'https://example.com/images/setmeal1.jpg', 2, NULL, 1, NULL, 1, 68.00),
-- 订单3的明细: 1份麻婆豆腐 + 2份鲜榨橙汁
(4, '麻婆豆腐', 'https://example.com/images/dish3.jpg', 3, 3, NULL, '特辣', 1, 28.00),
(5, '鲜榨橙汁', 'https://example.com/images/dish17.jpg', 3, 17, NULL, '冰', 2, 36.00);

-- ==================== 数据初始化完成 ====================
-- 说明:
-- 1. 员工密码默认为: 123456 (MD5加密后: e10adc3949ba59abbe56e057f20f883e)
-- 2. 订单状态: 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消 7退款
-- 3. 支付方式: 1微信 2支付宝
-- 4. 支付状态: 0未支付 1已支付 2退款
-- 5. 图片URL使用示例地址,实际使用时需要替换为真实图片地址
