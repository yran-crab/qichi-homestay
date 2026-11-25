-- ============================================================
-- 民宿管理系统 bnbms 种子数据（可重复执行）
-- 所有演示账号密码统一为 123456（BCrypt 密文）
-- ============================================================

USE bnbms;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE user_coupon;
TRUNCATE TABLE coupon;
TRUNCATE TABLE notice;
TRUNCATE TABLE banner;
TRUNCATE TABLE review;
TRUNCATE TABLE booking_order;
TRUNCATE TABLE favorite;
TRUNCATE TABLE homestay_facility;
TRUNCATE TABLE facility;
TRUNCATE TABLE homestay;
TRUNCATE TABLE sys_user;
SET FOREIGN_KEY_CHECKS = 1;

-- 图片生成服务地址前缀
-- https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=xxx&image_size=xxx

-- ------------------------------------------------------------
-- 用户
-- ------------------------------------------------------------
INSERT INTO sys_user
(id, username, password, real_name, phone, email, avatar, role, host_status, status, created_at, updated_at)
VALUES
(1, 'admin',   '$2a$10$S4TXoggEGCA26y8SFUmnD.q9PCJUJzWah9ulNAeilMD4WtMbDwcBW', '系统管理员', '13800000001', 'admin@bnbms.com',
 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=professional%20hotel%20manager%20cartoon%20avatar&image_size=square',
 'ADMIN', 'NONE', 1, NOW(), NOW()),
(2, 'host1',   '$2a$10$S4TXoggEGCA26y8SFUmnD.q9PCJUJzWah9ulNAeilMD4WtMbDwcBW', '林栖晚', '13800000002', 'linqiwan@bnbms.com',
 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=gentle%20young%20chinese%20woman%20host%20cartoon%20avatar&image_size=square',
 'HOST', 'APPROVED', 1, DATE_SUB(NOW(), INTERVAL 120 DAY), NOW()),
(3, 'host2',   '$2a$10$S4TXoggEGCA26y8SFUmnD.q9PCJUJzWah9ulNAeilMD4WtMbDwcBW', '沈渡', '13800000003', 'shendu@bnbms.com',
 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=friendly%20young%20chinese%20man%20host%20cartoon%20avatar&image_size=square',
 'HOST', 'APPROVED', 1, DATE_SUB(NOW(), INTERVAL 100 DAY), NOW()),
(4, 'tenant1', '$2a$10$S4TXoggEGCA26y8SFUmnD.q9PCJUJzWah9ulNAeilMD4WtMbDwcBW', '苏晓', '13900000001', 'suxiao@example.com',
 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=cute%20traveler%20girl%20cartoon%20avatar&image_size=square',
 'TENANT', 'NONE', 1, DATE_SUB(NOW(), INTERVAL 60 DAY), NOW()),
(5, 'tenant2', '$2a$10$S4TXoggEGCA26y8SFUmnD.q9PCJUJzWah9ulNAeilMD4WtMbDwcBW', '陈默', '13900000002', 'chenmo@example.com',
 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=handsome%20traveler%20boy%20cartoon%20avatar&image_size=square',
 'TENANT', 'NONE', 1, DATE_SUB(NOW(), INTERVAL 45 DAY), NOW()),
(6, 'hopending','$2a$10$S4TXoggEGCA26y8SFUmnD.q9PCJUJzWah9ulNAeilMD4WtMbDwcBW', '周望', '13900000003', NULL,
 NULL, 'TENANT', 'PENDING', 1, DATE_SUB(NOW(), INTERVAL 5 DAY), NOW()),
(7, 'disabled','$2a$10$S4TXoggEGCA26y8SFUmnD.q9PCJUJzWah9ulNAeilMD4WtMbDwcBW', '吴禁用', '13900000004', NULL,
 NULL, 'TENANT', 'NONE', 0, DATE_SUB(NOW(), INTERVAL 30 DAY), NOW());

-- ------------------------------------------------------------
-- 设施字典
-- ------------------------------------------------------------
INSERT INTO facility (id, name, icon, sort, created_at) VALUES
(1,  '免费WiFi',    '📶', 1,  NOW()),
(2,  '全屋空调',    '❄️', 2,  NOW()),
(3,  '智能电视',    '📺', 3,  NOW()),
(4,  '洗衣机',      '👕', 4,  NOW()),
(5,  '厨房餐具',    '🍳', 5,  NOW()),
(6,  '免费停车位',  '🅿️', 6,  NOW()),
(7,  '独立电梯',    '🏢', 7,  NOW()),
(8,  '全屋暖气',    '🌡️', 8,  NOW()),
(9,  '吹风机',      '💇', 9,  NOW()),
(10, '洗漱用品',    '🧴', 10, NOW()),
(11, '24小时热水',  '🚿', 11, NOW()),
(12, '健身房',      '🏋️', 12, NOW()),
(13, '室外泳池',    '🏊', 13, NOW()),
(14, '可携带宠物',  '🐾', 14, NOW()),
(15, '行李寄存',    '🧳', 15, NOW()),
(16, '高清投影',    '🎬', 16, NOW());

-- ------------------------------------------------------------
-- 房源（14 套，覆盖 8 个城市；10/11/12 分别为待审/驳回/下架）
-- ------------------------------------------------------------
INSERT INTO homestay
(id, host_id, title, city, address, longitude, latitude, cover_img, images, type, price, original_price,
 area, bedrooms, living_rooms, bathrooms, beds, max_guests, floor_info, description, rules,
 status, audit_remark, view_count, favorite_count, order_count, rating, review_count, created_at, updated_at)
VALUES
(1, 2, '成都宽窄巷子禅意一居整租', '成都', '成都市青羊区宽窄巷子景区旁泡桐树街18号', 104.066800, 30.672800,
 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=zen%20style%20cozy%20apartment%20living%20room%20chengdu&image_size=landscape_16_9',
 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=zen%20style%20cozy%20apartment%20living%20room%20chengdu&image_size=landscape_16_9,https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=minimal%20japanese%20bedroom%20warm%20light&image_size=landscape_16_9,https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=clean%20modern%20bathroom%20with%20bathtub&image_size=landscape_16_9,https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=chengdu%20old%20alley%20teahouse%20street%20view&image_size=landscape_16_9',
 '整租', 368.00, 428.00, 52.00, 1, 1, 1, 1, 2, '3/6层',
 '步行 3 分钟即达宽窄巷子，新中式禅意装修，全屋实木家具与智能家居。老成都烟火气与安静睡眠兼得，适合情侣或独自旅行。',
 '入住时间 14:00 后，退房 12:00 前；禁止吸烟、禁止派对；可接待来访但不允许额外留宿。',
 'APPROVED', NULL, 1286, 1, 2, 5.0, 1, DATE_SUB(NOW(), INTERVAL 55 DAY), NOW()),

(2, 2, '杭州西湖畔山景落地窗公寓', '杭州', '杭州市西湖区虎跑路近四眼井 66 号', 120.155100, 30.234100,
 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=hangzhou%20west%20lake%20mountain%20view%20apartment%20floor%20window&image_size=landscape_16_9',
 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=hangzhou%20west%20lake%20mountain%20view%20apartment%20floor%20window&image_size=landscape_16_9,https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=nordic%20bright%20living%20room%20with%20plants&image_size=landscape_16_9,https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=cozy%20double%20bedroom%20linen%20sheets&image_size=landscape_16_9,https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=west%20lake%20green%20hill%20teagarden%20aerial&image_size=landscape_16_9',
 '公寓', 458.00, 528.00, 68.00, 1, 1, 1, 2, 3, '5/8层',
 '直面山景的整面落地窗，清晨被鸟鸣唤醒。全屋地暖与胶囊咖啡机，楼下即网红咖啡店，步行 10 分钟到苏堤。',
 '入住 14:00 后，退房 12:00 前；需支付 200 元钥匙押金（退房无损退还）；不接待 65 岁以上老人单独入住。',
 'APPROVED', NULL, 2031, 1, 2, 5.0, 1, DATE_SUB(NOW(), INTERVAL 50 DAY), NOW()),

(3, 2, '厦门海边日出两居海景房', '厦门', '厦门市思明区环岛路曾厝垵对面海景大厦 12 层', 118.089400, 24.449800,
 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=xiamen%20seaside%20sunrise%20ocean%20view%20two%20bedroom&image_size=landscape_16_9',
 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=xiamen%20seaside%20sunrise%20ocean%20view%20two%20bedroom&image_size=landscape_16_9,https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=mediterranean%20style%20blue%20living%20room&image_size=landscape_16_9,https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=seaside%20white%20wooden%20bedroom%20balcony&image_size=landscape_16_9,https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=xiamen%20sandy%20beach%20palm%20sunrise&image_size=landscape_16_9',
 '整租', 528.00, 598.00, 76.00, 2, 1, 1, 2, 4, '12/22层',
 '躺在床上看海上日出，步行 2 分钟到沙滩。地中海风格两居室，配备儿童餐椅与沙滩玩具，亲子友好。',
 '入住 14:00 后，退房 11:00 前；请爱护沙滩玩具，离店时放回原处；海边请注意儿童安全。',
 'APPROVED', NULL, 1876, 1, 1, 5.0, 1, DATE_SUB(NOW(), INTERVAL 48 DAY), NOW()),

(4, 2, '丽江古城纳西小院阳光花房', '丽江', '丽江市古城区五一街文治巷 77 号', 100.233000, 26.872100,
 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=lijiang%20naxi%20ancient%20town%20courtyard%20sunny%20flower%20house&image_size=landscape_16_9',
 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=lijiang%20naxi%20ancient%20town%20courtyard%20sunny%20flower%20house&image_size=landscape_16_9,https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=rustic%20wooden%20lounge%20with%20fireplace&image_size=landscape_16_9,https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=vintage%20floral%20bedroom%20skylight&image_size=landscape_16_9,https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=lijiang%20ancient%20town%20roof%20sunset&image_size=landscape_16_9',
 '小院', 398.00, 468.00, 85.00, 1, 1, 1, 2, 2, '独栋2层',
 '百年纳西民居改造的独立小院，天井种满多肉与三角梅。提供免费普洱与手冲咖啡，晚上可在院子里看星空。',
 '入住 14:00 后，退房 12:00 前；古城内不通车，工作人员可协助行李搬运（免费）；夜晚 22:00 后保持安静。',
 'APPROVED', NULL, 2455, 1, 2, 4.0, 1, DATE_SUB(NOW(), INTERVAL 45 DAY), NOW()),

(5, 2, '大理洱海风琴别墅三居整栋', '大理', '大理白族自治州大理镇才村码头附近海景大道 9 号', 100.225700, 25.789400,
 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=dali%20erhai%20lakefront%20white%20villa%20three%20bedroom&image_size=landscape_16_9',
 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=dali%20erhai%20lakefront%20white%20villa%20three%20bedroom&image_size=landscape_16_9,https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=luxury%20villa%20living%20room%20panoramic%20lake%20view&image_size=landscape_16_9,https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=elegant%20hotel%20suite%20bedroom%20lakeview&image_size=landscape_16_9,https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=infinity%20pool%20overlooking%20erhai%20lake&image_size=landscape_16_9',
 '别墅', 1288.00, 1588.00, 220.00, 3, 2, 2, 4, 6, '独栋2层',
 '洱海东岸一线海景独栋别墅，270° 观海露台与无边泳池。含厨房、健身房、烧烤区，适合家庭聚会与小型团建。',
 '入住 14:00 后，退房 12:00 前；整栋出租最多 6 人；泳池开放时间 9:00-21:00；禁止室内吸烟。',
 'APPROVED', NULL, 3120, 1, 2, 5.0, 1, DATE_SUB(NOW(), INTERVAL 42 DAY), NOW()),

(6, 2, '西安钟鼓楼回民街精装公寓', '西安', '西安市莲湖区西大街回民街入口聚源小区 B 座', 108.939800, 34.341600,
 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=xian%20bell%20tower%20modern%20chinese%20apartment&image_size=landscape_16_9',
 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=xian%20bell%20tower%20modern%20chinese%20apartment&image_size=landscape_16_9,https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=modern%20grey%20wood%20living%20room&image_size=landscape_16_9,https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=compact%20warm%20bedroom%20reading%20lamp&image_size=landscape_16_9,https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=xian%20bell%20tower%20night%20illumination&image_size=landscape_16_9',
 '公寓', 328.00, 398.00, 45.00, 1, 1, 1, 1, 2, '7/18层',
 '出门即是回民街，步行 8 分钟到钟鼓楼。新中式精装公寓，配备智能门锁与高速 WiFi，吃货出行首选。',
 '入住 14:00 后，退房 12:00 前；小区门禁严格，入住人请携带身份证；夜间请勿大声喧哗。',
 'APPROVED', NULL, 968, 0, 1, 5.0, 1, DATE_SUB(NOW(), INTERVAL 38 DAY), NOW()),

(7, 2, '重庆洪崖洞江景 LOFT 复式', '重庆', '重庆市渝中区嘉陵江滨江路高盛创富中心', 106.551600, 29.563000,
 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=chongqing%20hongyadong%20river%20view%20loft%20night&image_size=landscape_16_9',
 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=chongqing%20hongyadong%20river%20view%20loft%20night&image_size=landscape_16_9,https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=industrial%20style%20loft%20living%20room%20projector&image_size=landscape_16_9,https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=loft%20upstairs%20bedroom%20river%20window&image_size=landscape_16_9,https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=chongqing%20night%20skyline%20yangtze%20river&image_size=landscape_16_9',
 '整租', 488.00, 568.00, 58.00, 1, 1, 1, 2, 3, '22/32层',
 '4.8 米挑高 LOFT，270° 落地窗正对洪崖洞与千厮门大桥夜景。120 寸高清投影与复古游戏机，氛围感拉满。',
 '入住 15:00 后，退房 12:00 前；投影仪使用后请关闭电源；高层房源请勿攀爬窗台。',
 'APPROVED', NULL, 2764, 0, 2, 4.0, 1, DATE_SUB(NOW(), INTERVAL 35 DAY), NOW()),

(8, 2, '苏州平江路苏式庭院民宿', '苏州', '苏州市姑苏区平江路历史街区钮家巷 12 号', 120.585300, 31.299000,
 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=suzhou%20pingjiang%20classical%20garden%20courtyard%20house&image_size=landscape_16_9',
 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=suzhou%20pingjiang%20classical%20garden%20courtyard%20house&image_size=landscape_16_9,https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=chinese%20tea%20room%20wood%20furniture&image_size=landscape_16_9,https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=su%20style%20silk%20bedding%20bedroom&image_size=landscape_16_9,https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=suzhou%20canal%20stone%20bridge%20rain&image_size=landscape_16_9',
 '小院', 568.00, 668.00, 96.00, 2, 1, 1, 2, 4, '独栋',
 '清代老宅修缮的苏式庭院，太湖石假山与锦鲤池。房间配备苏州丝绸床品，含一顿地道苏式早面，平江路出门即达。',
 '入住 14:00 后，退房 12:00 前；老宅木楼板，室内请穿软底拖鞋；庭院鱼池请勿投喂。',
 'APPROVED', NULL, 1543, 1, 2, 0.0, 0, DATE_SUB(NOW(), INTERVAL 30 DAY), NOW()),

(9, 3, '成都春熙路太古里轻奢一居', '成都', '成都市锦江区红星路三段 IFS 国际金融中心旁', 104.081000, 30.653000,
 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=chengdu%20taikoo%20li%20light%20luxury%20apartment&image_size=landscape_16_9',
 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=chengdu%20taikoo%20li%20light%20luxury%20apartment&image_size=landscape_16_9,https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=modern%20light%20luxury%20living%20room%20marble&image_size=landscape_16_9,https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=hotel%20style%20bedroom%20ambient%20light&image_size=landscape_16_9,https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=chengdu%20taikoo%20li%20bustling%20night%20street&image_size=landscape_16_9',
 '公寓', 418.00, 498.00, 50.00, 1, 1, 1, 1, 2, '18/35层',
 '春熙路地铁口 100 米，IFS 爬墙熊猫对面。轻奢精装、乳胶床垫与雨淋花洒，楼下即商圈，购物觅食零通勤。',
 '入住 14:00 后，退房 12:00 前；公寓楼内禁止开派对；访客需在前台登记。',
 'APPROVED', NULL, 1752, 0, 2, 0.0, 0, DATE_SUB(NOW(), INTERVAL 28 DAY), NOW()),

(10, 2, '杭州西溪湿地原木风整居（新店待审）', '杭州', '杭州市西湖区西溪湿地东区蒋村街 5 号', 120.071000, 30.275000,
 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=hangzhou%20xixi%20wetland%20wooden%20cabin%20style%20house&image_size=landscape_16_9',
 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=hangzhou%20xixi%20wetland%20wooden%20cabin%20style%20house&image_size=landscape_16_9,https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=wooden%20japandi%20living%20room%20reed%20view&image_size=landscape_16_9,https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=tatami%20wood%20bedroom%20paper%20lamp&image_size=landscape_16_9,https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=xixi%20wetland%20reed%20marsh%20boat&image_size=landscape_16_9',
 '整租', 498.00, NULL, 72.00, 2, 1, 1, 2, 4, '2/3层',
 '西溪湿地旁的原木风住宅，推开窗就是芦苇荡。全套无印良品家居，配备黑胶唱片机与精品手冲器具。',
 '入住 14:00 后，退房 12:00 前；不接受 18 岁以下客人单独入住。',
 'PENDING', NULL, 0, 0, 0, 0.0, 0, DATE_SUB(NOW(), INTERVAL 1 DAY), NOW()),

(11, 3, '丽江束河古镇观景单间', '丽江', '丽江市玉龙县束河古镇茶马驿馆旁', 100.201000, 26.921000,
 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=lijiang%20shuhe%20old%20town%20single%20room&image_size=landscape_16_9',
 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=lijiang%20shuhe%20old%20town%20single%20room&image_size=landscape_16_9,https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=small%20tidy%20bedroom%20wood%20beam&image_size=landscape_16_9',
 '单间', 168.00, NULL, 22.00, 1, 0, 1, 1, 2, '2层',
 '束河古镇安静角落的单间，公共露台可观玉龙雪山。',
 '入住 14:00 后，退房 12:00 前。',
 'REJECTED', '图片不清晰且设施信息不完整，请补充实拍照片、房型参数与入住说明后重新提交', 0, 0, 0, 0.0, 0, DATE_SUB(NOW(), INTERVAL 3 DAY), NOW()),

(12, 2, '厦门曾厝垵文创小筑', '厦门', '厦门市思明区曾厝垵社国办街 221 号', 118.102000, 24.441000,
 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=xiamen%20zengcuoan%20art%20village%20guesthouse&image_size=landscape_16_9',
 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=xiamen%20zengcuoan%20art%20village%20guesthouse&image_size=landscape_16_9,https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=artistic%20handmade%20decor%20living%20room&image_size=landscape_16_9,https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=cozy%20attic%20bedroom%20skylight&image_size=landscape_16_9',
 '整租', 298.00, 358.00, 40.00, 1, 1, 1, 1, 2, '4层',
 '文创小渔村中的设计师小筑，满墙独立插画与手作器物。近期装修维护中，暂停预订。',
 '入住 14:00 后，退房 12:00 前。',
 'OFFLINE', NULL, 642, 0, 0, 0.0, 0, DATE_SUB(NOW(), INTERVAL 25 DAY), NOW()),

(13, 3, '杭州龙井茶园山景别墅', '杭州', '杭州市西湖区龙井村狮峰山下 18 号', 120.105000, 30.228000,
 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=hangzhou%20longjing%20tea%20plantation%20mountain%20villa&image_size=landscape_16_9',
 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=hangzhou%20longjing%20tea%20plantation%20mountain%20villa&image_size=landscape_16_9,https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=villa%20great%20room%20wood%20beam%20sofa&image_size=landscape_16_9,https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=bright%20master%20bedroom%20tea%20garden%20view&image_size=landscape_16_9,https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=longjing%20tea%20terraces%20misty%20morning&image_size=landscape_16_9',
 '别墅', 988.00, 1188.00, 180.00, 3, 2, 2, 4, 6, '独栋3层',
 '狮峰龙井核心产区内的茶山别墅，含私家健身房与露天泳池。提供明前龙井品鉴与采茶体验（季节性）。',
 '入住 14:00 后，退房 12:00 前；茶山区域夜间无路灯，建议白天抵达；可协助安排付费接送。',
 'APPROVED', NULL, 2210, 1, 2, 0.0, 0, DATE_SUB(NOW(), INTERVAL 22 DAY), NOW()),

(14, 3, '重庆解放碑城市夜景公寓', '重庆', '重庆市渝中区解放碑民权路合景聚融广场', 106.578000, 29.555000,
 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=chongqing%20jiefangbei%20city%20night%20view%20apartment&image_size=landscape_16_9',
 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=chongqing%20jiefangbei%20city%20night%20view%20apartment&image_size=landscape_16_9,https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=contemporary%20apartment%20living%20room%20neon%20city&image_size=landscape_16_9,https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=modern%20bedroom%20floor%20window%20night%20city&image_size=landscape_16_9,https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=chongqing%20cyberpunk%20city%20lights%20aerial&image_size=landscape_16_9',
 '公寓', 358.00, 428.00, 48.00, 1, 1, 1, 1, 2, '30/45层',
 '解放碑核心 30 层高空公寓，两江交汇夜景尽收眼底。地铁 1/2 号线换乘站旁，8D 魔幻都市出行枢纽。',
 '入住 14:00 后，退房 12:00 前；高层请勿在窗台放置物品；需要安静环境请提前告知。',
 'APPROVED', NULL, 1103, 1, 1, 0.0, 0, DATE_SUB(NOW(), INTERVAL 18 DAY), NOW());

-- ------------------------------------------------------------
-- 房源-设施 关联
-- ------------------------------------------------------------
INSERT INTO homestay_facility (homestay_id, facility_id) VALUES
(1,1),(1,2),(1,3),(1,4),(1,8),(1,9),(1,10),(1,11),(1,15),
(2,1),(2,2),(2,3),(2,4),(2,5),(2,6),(2,7),(2,9),(2,10),(2,11),(2,16),
(3,1),(3,2),(3,3),(3,5),(3,8),(3,9),(3,10),(3,11),(3,13),(3,15),
(4,1),(4,2),(4,3),(4,5),(4,6),(4,9),(4,10),(4,11),(4,14),(4,15),
(5,1),(5,2),(5,3),(5,4),(5,5),(5,6),(5,8),(5,9),(5,10),(5,11),(5,12),(5,13),(5,16),
(6,1),(6,2),(6,3),(6,4),(6,7),(6,9),(6,10),(6,11),(6,15),
(7,1),(7,2),(7,3),(7,4),(7,5),(7,6),(7,7),(7,9),(7,10),(7,11),(7,16),
(8,1),(8,2),(8,3),(8,5),(8,8),(8,9),(8,10),(8,11),(8,14),(8,15),
(9,1),(9,2),(9,3),(9,4),(9,5),(9,7),(9,9),(9,10),(9,11),(9,16),
(10,1),(10,2),(10,3),(10,4),(10,5),(10,9),(10,10),(10,11),(10,15),
(11,1),(11,2),(11,9),(11,10),(11,11),
(12,1),(12,2),(12,3),(12,5),(12,9),(12,10),(12,11),(12,15),
(13,1),(13,2),(13,3),(13,4),(13,5),(13,6),(13,8),(13,9),(13,10),(13,11),(13,12),(13,13),
(14,1),(14,2),(14,3),(14,4),(14,6),(14,7),(14,9),(14,10),(14,11),(14,16);

-- ------------------------------------------------------------
-- 收藏
-- ------------------------------------------------------------
INSERT INTO favorite (id, user_id, homestay_id, created_at) VALUES
(1, 4, 2, DATE_SUB(NOW(), INTERVAL 20 DAY)),
(2, 4, 3, DATE_SUB(NOW(), INTERVAL 18 DAY)),
(3, 4, 5, DATE_SUB(NOW(), INTERVAL 15 DAY)),
(4, 4, 8, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(5, 4, 13, DATE_SUB(NOW(), INTERVAL 8 DAY)),
(6, 5, 1, DATE_SUB(NOW(), INTERVAL 22 DAY)),
(7, 5, 4, DATE_SUB(NOW(), INTERVAL 14 DAY)),
(8, 5, 14, DATE_SUB(NOW(), INTERVAL 6 DAY));

-- ------------------------------------------------------------
-- 订单（日期相对当前日期动态生成，保证任何时候初始化都可演示）
-- ------------------------------------------------------------
INSERT INTO booking_order
(id, order_no, tenant_id, host_id, homestay_id, check_in_date, check_out_date, nights, guest_count,
 unit_price, total_amount, contact_name, contact_phone, status, pay_time, cancel_time, cancel_reason,
 reviewed, created_at, updated_at)
VALUES
(1,  'BN2026010001', 4, 2, 1,  DATE_SUB(CURDATE(), INTERVAL 25 DAY), DATE_SUB(CURDATE(), INTERVAL 22 DAY), 3, 2,
 368.00, 1104.00, '苏晓', '13900000001', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 26 DAY), NULL, NULL,
 1, DATE_SUB(NOW(), INTERVAL 26 DAY), DATE_SUB(NOW(), INTERVAL 22 DAY)),
(2,  'BN2026010002', 4, 2, 2,  DATE_SUB(CURDATE(), INTERVAL 20 DAY), DATE_SUB(CURDATE(), INTERVAL 18 DAY), 2, 2,
 458.00, 916.00,  '苏晓', '13900000001', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 21 DAY), NULL, NULL,
 1, DATE_SUB(NOW(), INTERVAL 21 DAY), DATE_SUB(NOW(), INTERVAL 18 DAY)),
(3,  'BN2026010003', 5, 2, 3,  DATE_SUB(CURDATE(), INTERVAL 18 DAY), DATE_SUB(CURDATE(), INTERVAL 15 DAY), 3, 3,
 528.00, 1584.00, '陈默', '13900000002', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 19 DAY), NULL, NULL,
 1, DATE_SUB(NOW(), INTERVAL 19 DAY), DATE_SUB(NOW(), INTERVAL 15 DAY)),
(4,  'BN2026010004', 4, 2, 4,  DATE_SUB(CURDATE(), INTERVAL 12 DAY), DATE_SUB(CURDATE(), INTERVAL 10 DAY), 2, 2,
 398.00, 796.00,  '苏晓', '13900000001', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 13 DAY), NULL, NULL,
 1, DATE_SUB(NOW(), INTERVAL 13 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY)),
(5,  'BN2026010005', 5, 2, 5,  DATE_SUB(CURDATE(), INTERVAL 8 DAY),  DATE_SUB(CURDATE(), INTERVAL 5 DAY),  3, 5,
 1288.00, 3864.00,'陈默', '13900000002', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 9 DAY),  NULL, NULL,
 1, DATE_SUB(NOW(), INTERVAL 9 DAY),  DATE_SUB(NOW(), INTERVAL 5 DAY)),
(6,  'BN2026010006', 4, 2, 6,  DATE_SUB(CURDATE(), INTERVAL 6 DAY),  DATE_SUB(CURDATE(), INTERVAL 5 DAY),  1, 2,
 328.00, 328.00,  '苏晓', '13900000001', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 7 DAY),  NULL, NULL,
 1, DATE_SUB(NOW(), INTERVAL 7 DAY),  DATE_SUB(NOW(), INTERVAL 5 DAY)),
(7,  'BN2026010007', 5, 2, 7,  DATE_SUB(CURDATE(), INTERVAL 3 DAY),  DATE_SUB(CURDATE(), INTERVAL 1 DAY),  2, 2,
 488.00, 976.00,  '陈默', '13900000002', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 4 DAY),  NULL, NULL,
 1, DATE_SUB(NOW(), INTERVAL 4 DAY),  DATE_SUB(NOW(), INTERVAL 1 DAY)),
(8,  'BN2026010008', 4, 3, 9,  DATE_ADD(CURDATE(), INTERVAL 2 DAY),   DATE_ADD(CURDATE(), INTERVAL 4 DAY),   2, 2,
 418.00, 836.00,  '苏晓', '13900000001', 'PAID', DATE_SUB(NOW(), INTERVAL 2 DAY), NULL, NULL,
 0, DATE_SUB(NOW(), INTERVAL 2 DAY), NOW()),
(9,  'BN2026010009', 5, 2, 2,  DATE_ADD(CURDATE(), INTERVAL 5 DAY),   DATE_ADD(CURDATE(), INTERVAL 7 DAY),   2, 2,
 458.00, 916.00,  '陈默', '13900000002', 'PAID', DATE_SUB(NOW(), INTERVAL 1 DAY), NULL, NULL,
 0, DATE_SUB(NOW(), INTERVAL 1 DAY), NOW()),
(10, 'BN2026010010', 4, 3, 13, DATE_SUB(CURDATE(), INTERVAL 1 DAY),  DATE_ADD(CURDATE(), INTERVAL 2 DAY),   3, 2,
 988.00, 2964.00, '苏晓', '13900000001', 'CHECKED_IN', DATE_SUB(NOW(), INTERVAL 3 DAY), NULL, NULL,
 0, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(11, 'BN2026010011', 5, 2, 1,  DATE_ADD(CURDATE(), INTERVAL 3 DAY),   DATE_ADD(CURDATE(), INTERVAL 4 DAY),   1, 2,
 368.00, 368.00,  '陈默', '13900000002', 'PENDING_PAY', NULL, NULL, NULL,
 0, DATE_SUB(NOW(), INTERVAL 10 MINUTE), NOW()),
(12, 'BN2026010012', 4, 2, 8,  DATE_ADD(CURDATE(), INTERVAL 10 DAY),  DATE_ADD(CURDATE(), INTERVAL 12 DAY),  2, 3,
 568.00, 1136.00, '苏晓', '13900000001', 'PENDING_PAY', NULL, NULL, NULL,
 0, DATE_SUB(NOW(), INTERVAL 30 MINUTE), NOW()),
(13, 'BN2026010013', 5, 2, 6,  DATE_SUB(CURDATE(), INTERVAL 15 DAY), DATE_SUB(CURDATE(), INTERVAL 13 DAY), 2, 2,
 328.00, 656.00,  '陈默', '13900000002', 'CANCELLED', NULL, DATE_SUB(NOW(), INTERVAL 16 DAY), '行程变更，主动取消',
 0, DATE_SUB(NOW(), INTERVAL 17 DAY), DATE_SUB(NOW(), INTERVAL 16 DAY)),
(14, 'BN2026010014', 4, 2, 7,  DATE_ADD(CURDATE(), INTERVAL 20 DAY),  DATE_ADD(CURDATE(), INTERVAL 22 DAY),  2, 2,
 488.00, 976.00,  '苏晓', '13900000001', 'PENDING_PAY', NULL, NULL, NULL,
 0, DATE_SUB(NOW(), INTERVAL 40 MINUTE), NOW()),
(15, 'BN2026010015', 4, 3, 14, DATE_SUB(CURDATE(), INTERVAL 15 DAY), DATE_SUB(CURDATE(), INTERVAL 14 DAY), 1, 2,
 358.00, 358.00,  '苏晓', '13900000001', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 16 DAY), NULL, NULL,
 0, DATE_SUB(NOW(), INTERVAL 16 DAY), DATE_SUB(NOW(), INTERVAL 14 DAY)),
(16, 'BN2026010016', 5, 3, 9,  DATE_SUB(CURDATE(), INTERVAL 22 DAY), DATE_SUB(CURDATE(), INTERVAL 21 DAY), 1, 1,
 418.00, 418.00,  '陈默', '13900000002', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 23 DAY), NULL, NULL,
 0, DATE_SUB(NOW(), INTERVAL 23 DAY), DATE_SUB(NOW(), INTERVAL 21 DAY)),
(17, 'BN2026010017', 4, 2, 5,  DATE_SUB(CURDATE(), INTERVAL 28 DAY), DATE_SUB(CURDATE(), INTERVAL 26 DAY), 2, 4,
 1288.00, 2576.00,'苏晓', '13900000001', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 29 DAY), NULL, NULL,
 0, DATE_SUB(NOW(), INTERVAL 29 DAY), DATE_SUB(NOW(), INTERVAL 26 DAY)),
(18, 'BN2026010018', 5, 3, 13, DATE_SUB(CURDATE(), INTERVAL 10 DAY), DATE_SUB(CURDATE(), INTERVAL 8 DAY),  2, 3,
 988.00, 1976.00, '陈默', '13900000002', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 11 DAY), NULL, NULL,
 0, DATE_SUB(NOW(), INTERVAL 11 DAY), DATE_SUB(NOW(), INTERVAL 8 DAY)),
(19, 'BN2026010019', 5, 2, 4,  DATE_ADD(CURDATE(), INTERVAL 8 DAY),   DATE_ADD(CURDATE(), INTERVAL 10 DAY),  2, 2,
 398.00, 796.00,  '陈默', '13900000002', 'PAID', NOW(), NULL, NULL,
 0, NOW(), NOW()),
(20, 'BN2026010020', 5, 2, 8,  DATE_SUB(CURDATE(), INTERVAL 2 DAY),  CURDATE(),                          2, 2,
 568.00, 1136.00, '陈默', '13900000002', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 3 DAY), NULL, NULL,
 0, DATE_SUB(NOW(), INTERVAL 3 DAY), NOW());

-- ------------------------------------------------------------
-- 评价（对应订单 1-7）
-- ------------------------------------------------------------
INSERT INTO review
(id, order_id, homestay_id, tenant_id, rating, content, images, reply, reply_at, created_at)
VALUES
(1, 1, 1, 4, 5,
 '位置太好了，走路几分钟就是宽窄巷子。房间是很舒服的禅意风格，床品干净，房东回复特别及时，还推荐了很多本地人去的小吃店，强烈推荐！',
 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=clean%20cozy%20zen%20room%20interior%20photo&image_size=square',
 NULL, NULL, DATE_SUB(NOW(), INTERVAL 21 DAY)),
(2, 2, 2, 4, 5,
 '落地窗山景绝了，早上在阳台喝茶看山非常治愈。地暖很舒服，小区安静，打车到苏堤起步价。下次来杭州还住这里。',
 NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 17 DAY)),
(3, 3, 3, 5, 5,
 '带着爸妈和孩子一起住的，三室够住，躺在床上真的能看到日出！厨房设备齐全，房东还准备了沙滩玩具和儿童餐椅，细节满分。',
 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=ocean%20sunrise%20view%20from%20bedroom%20window&image_size=square',
 '谢谢认可！下次来厦门可以提前说，给你们安排赶海工具和抓蟹攻略，期待再次光临～', DATE_SUB(NOW(), INTERVAL 14 DAY),
 DATE_SUB(NOW(), INTERVAL 15 DAY)),
(4, 4, 4, 4, 4,
 '小院很漂亮，花花草草打理得很好，晚上看星星很惬意。唯一不足是隔音一般，隔壁房间说话隐约能听到，整体还是满意的。',
 NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 9 DAY)),
(5, 5, 5, 5, 5,
 '别墅品质对得起价格，泳池正对洱海，拍照超出片。管家服务到位，烧烤和早餐都安排得很好，公司团建大家都很满意！',
 NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(6, 6, 6, 4, 5,
 '下楼就是回民街，吃东西太方便了。房间虽然不大但布置温馨，洗澡水很热，适合短途旅行，性价比高。',
 NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(7, 7, 7, 5, 4,
 '洪崖洞夜景确实震撼，投影效果不错。入住当天热水不稳定，联系后很快处理了。整体推荐给喜欢夜景的朋友。',
 NULL, NULL, NULL, CURDATE());

-- ------------------------------------------------------------
-- 轮播图（3 启用 + 1 禁用）
-- ------------------------------------------------------------
INSERT INTO banner (id, title, image_url, link, sort, status, created_at, updated_at)
VALUES
(1, '丽江古城 · 住进晨光里',
 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=lijiang%20ancient%20town%20golden%20morning%20rooftop%20banner&image_size=landscape_16_9',
 '/homestays?city=丽江', 1, 1, NOW(), NOW()),
(2, '大理洱海 · 一线海景别墅',
 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=erhai%20lake%20villa%20pool%20blue%20sky%20wide%20banner&image_size=landscape_16_9',
 '/homestay/5', 2, 1, NOW(), NOW()),
(3, '城市夜游 · 重庆江景房',
 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=chongqing%20night%20river%20skyline%20neon%20wide%20banner&image_size=landscape_16_9',
 '/homestays?city=重庆', 3, 1, NOW(), NOW()),
(4, '【已下架】春季早鸟优惠活动',
 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=spring%20travel%20discount%20poster%20banner&image_size=landscape_16_9',
 NULL, 4, 0, NOW(), NOW());

-- ------------------------------------------------------------
-- 公告
-- ------------------------------------------------------------
INSERT INTO notice (id, title, content, status, created_at, updated_at)
VALUES
(1, '国庆黄金周房源预订提醒',
 '国庆假期（10 月 1 日-7 日）为预订高峰，建议至少提前 15 天预订心仪房源。旺季房源支持免费取消的时间以订单页提示为准，请规划好行程后再下单。祝大家旅途愉快！',
 1, DATE_SUB(NOW(), INTERVAL 10 DAY), NOW()),
(2, '房东入驻审核规则说明',
 '欢迎加入栖迟民宿房东大家庭！提交入驻申请后，平台将在 1-3 个工作日内完成审核。发布房源需上传真实实拍图片、完整房型参数与入住说明，审核通过后方可上架接单。',
 1, DATE_SUB(NOW(), INTERVAL 7 DAY), NOW()),
(3, '演示系统：支付宝沙箱支付流程使用指南',
 '本系统采用支付宝沙箱环境进行支付演示：在收银台点击「确认支付」后将跳转至支付宝沙箱收银台，请使用沙箱买家账号完成付款，支付成功后自动返回并更新订单状态。沙箱环境不会产生真实交易。',
 1, DATE_SUB(NOW(), INTERVAL 2 DAY), NOW());

-- ------------------------------------------------------------
-- 优惠券（5 张：无门槛代金/满减/折扣/领后N天）
-- ------------------------------------------------------------
INSERT INTO coupon
(id, name, type, threshold, discount_amount, discount_percent, max_discount_amount,
 total_count, received_count, used_count, per_limit, valid_type,
 valid_start_time, valid_end_time, valid_days, status, created_at, updated_at)
VALUES
(1, '新人立减20元券', 'CASH', 0.00, 20.00, NULL, NULL,
 1000, 2, 0, 1, 'FIXED',
 DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 83 DAY), NULL, 1, NOW(), NOW()),
(2, '满300减50元券', 'FULL_REDUCE', 300.00, 50.00, NULL, NULL,
 1000, 2, 0, 1, 'FIXED',
 DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 83 DAY), NULL, 1, NOW(), NOW()),
(3, '满600减120元券', 'FULL_REDUCE', 600.00, 120.00, NULL, NULL,
 500, 1, 0, 1, 'FIXED',
 DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 83 DAY), NULL, 1, NOW(), NOW()),
(4, '8.8折入住券（最高减150）', 'DISCOUNT', 0.00, NULL, 88, 150.00,
 500, 1, 0, 1, 'FIXED',
 DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 83 DAY), NULL, 1, NOW(), NOW()),
(5, '满500减100限时券', 'FULL_REDUCE', 500.00, 100.00, NULL, NULL,
 300, 0, 0, 1, 'DAYS',
 NULL, NULL, 30, 1, NOW(), NOW());

-- ------------------------------------------------------------
-- 用户优惠券（tenant1 三张有效；tenant2 一张有效 + 一张已过期）
-- ------------------------------------------------------------
INSERT INTO user_coupon
(id, user_id, coupon_id, status, order_id, receive_time, valid_start_time, valid_end_time, used_time, created_at)
VALUES
(1, 4, 1, 'UNUSED', NULL, DATE_SUB(NOW(), INTERVAL 2 DAY),
 DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 83 DAY), NULL, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(2, 4, 2, 'UNUSED', NULL, DATE_SUB(NOW(), INTERVAL 2 DAY),
 DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 83 DAY), NULL, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(3, 4, 4, 'UNUSED', NULL, DATE_SUB(NOW(), INTERVAL 1 DAY),
 DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 83 DAY), NULL, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(4, 5, 3, 'UNUSED', NULL, DATE_SUB(NOW(), INTERVAL 3 DAY),
 DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 83 DAY), NULL, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(5, 5, 2, 'UNUSED', NULL, DATE_SUB(NOW(), INTERVAL 15 DAY),
 DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), NULL, DATE_SUB(NOW(), INTERVAL 15 DAY));
