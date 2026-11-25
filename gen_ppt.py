# -*- coding: utf-8 -*-
"""生成毕业设计答辩PPT"""
from pptx import Presentation
from pptx.util import Inches, Pt, Emu
from pptx.dml.color import RGBColor
from pptx.enum.text import PP_ALIGN, MSO_ANCHOR
from pptx.enum.shapes import MSO_SHAPE
import os

IMG = r'C:\Users\liu\AppData\Local\Temp\doc_images'
OUT = r'D:\实训文件\第一小组-辛聪-辛聪\民宿管理系统答辩PPT.pptx'

# 颜色
C_PRIMARY = RGBColor(0x4A, 0x90, 0xD9)   # 蓝
C_DARK = RGBColor(0x1A, 0x3C, 0x5E)      # 深蓝
C_ACCENT = RGBColor(0x52, 0xC4, 0x1A)   # 绿
C_ORANGE = RGBColor(0xFA, 0x8C, 0x16)    # 橙
C_RED = RGBColor(0xF5, 0x22, 0x2D)       # 红
C_PURPLE = RGBColor(0x72, 0x2E, 0xD1)    # 紫
C_WHITE = RGBColor(0xFF, 0xFF, 0xFF)
C_BG = RGBColor(0xF0, 0xF5, 0xFF)       # 浅蓝底
C_TEXT = RGBColor(0x33, 0x33, 0x33)      # 正文
C_LIGHT = RGBColor(0x99, 0x99, 0x99)     # 辅助

prs = Presentation()
prs.slide_width = Inches(13.333)
prs.slide_height = Inches(7.5)

def add_slide():
    return prs.slides.add_slide(prs.slide_layouts[6])  # blank

def set_bg(slide, color):
    bg = slide.background
    fill = bg.fill
    fill.solid()
    fill.fore_color.rgb = color

def add_rect(slide, x, y, w, h, color, alpha=None):
    shape = slide.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(x), Inches(y), Inches(w), Inches(h))
    shape.fill.solid()
    shape.fill.fore_color.rgb = color
    shape.line.fill.background()
    if alpha is not None:
        shape.fill.fore_color.brightness = 0
    return shape

def add_round_rect(slide, x, y, w, h, color):
    shape = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(x), Inches(y), Inches(w), Inches(h))
    shape.fill.solid()
    shape.fill.fore_color.rgb = color
    shape.line.fill.background()
    return shape

def add_text(slide, x, y, w, h, text, size=24, color=C_TEXT, bold=False, align=PP_ALIGN.LEFT, font='Microsoft YaHei'):
    txBox = slide.shapes.add_textbox(Inches(x), Inches(y), Inches(w), Inches(h))
    tf = txBox.text_frame
    tf.word_wrap = True
    p = tf.paragraphs[0]
    p.text = text
    p.font.size = Pt(size)
    p.font.color.rgb = color
    p.font.bold = bold
    p.font.name = font
    p.alignment = align
    return txBox

def add_multi_text(slide, x, y, w, h, lines, size=22, color=C_TEXT, bold=False, align=PP_ALIGN.LEFT, spacing=1.5):
    txBox = slide.shapes.add_textbox(Inches(x), Inches(y), Inches(w), Inches(h))
    tf = txBox.text_frame
    tf.word_wrap = True
    for i, line in enumerate(lines):
        if i == 0:
            p = tf.paragraphs[0]
        else:
            p = tf.add_paragraph()
        p.text = line
        p.font.size = Pt(size)
        p.font.color.rgb = color
        p.font.bold = bold
        p.font.name = 'Microsoft YaHei'
        p.alignment = align
        p.space_after = Pt(size * spacing)
    return txBox

def add_title_bar(slide, title, subtitle=''):
    """每页顶部标题栏"""
    add_rect(slide, 0, 0, 13.333, 1.1, C_PRIMARY)
    add_text(slide, 0.6, 0.15, 10, 0.8, title, size=32, color=C_WHITE, bold=True)
    if subtitle:
        add_text(slide, 0.6, 0.65, 10, 0.4, subtitle, size=16, color=RGBColor(0xCC, 0xDD, 0xFF))
    # 右侧装饰
    add_rect(slide, 12.8, 0, 0.533, 1.1, C_DARK)

def add_footer(slide, page_num):
    add_text(slide, 0.5, 7.0, 4, 0.4, '民宿管理系统毕业答辩', size=12, color=C_LIGHT)
    add_text(slide, 11.5, 7.0, 1.5, 0.4, str(page_num), size=12, color=C_LIGHT, align=PP_ALIGN.RIGHT)

def add_image(slide, img_name, x, y, w=None, h=None):
    path = os.path.join(IMG, img_name)
    if not os.path.exists(path):
        add_text(slide, x, y, 3, 0.5, f'[图片: {img_name}]', size=16, color=C_LIGHT)
        return
    if w and h:
        slide.shapes.add_picture(path, Inches(x), Inches(y), Inches(w), Inches(h))
    elif w:
        slide.shapes.add_picture(path, Inches(x), Inches(y), width=Inches(w))
    elif h:
        slide.shapes.add_picture(path, Inches(x), Inches(y), height=Inches(h))
    else:
        slide.shapes.add_picture(path, Inches(x), Inches(y))

def add_bullet_card(slide, x, y, w, h, title, items, color=C_PRIMARY):
    """带标题的卡片"""
    add_round_rect(slide, x, y, w, h, C_BG)
    # 标题条
    add_rect(slide, x, y, w, 0.5, color)
    add_text(slide, x+0.15, y+0.05, w-0.3, 0.4, title, size=18, color=C_WHITE, bold=True)
    # 内容
    lines = ['• ' + item for item in items]
    add_multi_text(slide, x+0.15, y+0.6, w-0.3, h-0.7, lines, size=15, color=C_TEXT, spacing=1.2)

# ==========================================
# Slide 1: 封面
# ==========================================
slide = add_slide()
set_bg(slide, C_DARK)
# 装饰条
add_rect(slide, 0, 3.0, 13.333, 0.06, C_PRIMARY)
add_text(slide, 1, 1.0, 11, 1.2, '基于 Spring Boot + Vue3 的', size=28, color=RGBColor(0xAA,0xCC,0xEE), align=PP_ALIGN.CENTER)
add_text(slide, 1, 1.8, 11, 1.5, '民宿预订管理系统', size=48, color=C_WHITE, bold=True, align=PP_ALIGN.CENTER)
add_text(slide, 1, 3.2, 11, 0.8, '—— 含 AI 智能推荐、在线支付、优惠券营销', size=22, color=C_PRIMARY, align=PP_ALIGN.CENTER)
add_text(slide, 1, 5.5, 11, 0.5, '答辩人：辛聪', size=24, color=C_WHITE, align=PP_ALIGN.CENTER)
add_text(slide, 1, 6.0, 11, 0.5, '指导教师：XXX', size=20, color=C_LIGHT, align=PP_ALIGN.CENTER)

# ==========================================
# Slide 2: 研究背景与意义
# ==========================================
slide = add_slide()
set_bg(slide, C_WHITE)
add_title_bar(slide, '一、研究背景与意义')
add_round_rect(slide, 0.8, 1.5, 5.5, 2.5, C_BG)
add_text(slide, 1.0, 1.6, 5.0, 0.5, '研究背景', size=22, color=C_PRIMARY, bold=True)
add_multi_text(slide, 1.0, 2.2, 5.0, 1.8, [
    '• 共享经济快速发展，民宿市场持续增长',
    '• 传统订房方式效率低、信息不透明',
    '• 缺少智能化推荐和精准营销手段',
    '• 在线支付闭环是核心需求',
], size=17, color=C_TEXT, spacing=1.5)

add_round_rect(slide, 7.0, 1.5, 5.5, 2.5, RGBColor(0xF6,0xFF,0xED))
add_text(slide, 7.2, 1.6, 5.0, 0.5, '研究意义', size=22, color=C_ACCENT, bold=True)
add_multi_text(slide, 7.2, 2.2, 5.0, 1.8, [
    '• 全流程在线预订，提升用户体验',
    '• AI 智能推荐提高转化率',
    '• 优惠券系统实现精准营销',
    '• 多角色权限管理，规范运营',
], size=17, color=C_TEXT, spacing=1.5)

# 技术指标
add_round_rect(slide, 0.8, 4.5, 11.7, 2.2, C_DARK)
add_text(slide, 1.0, 4.6, 11, 0.5, '系统关键指标', size=22, color=C_WHITE, bold=True)
metrics = [
    ('3 种角色', '租客/房东/管理员'),
    ('11 张数据表', '完整数据模型'),
    ('50+ 个页面', '全功能覆盖'),
    ('3 大 AI 亮点', '推荐/客服/日历'),
]
for i, (val, desc) in enumerate(metrics):
    x = 1.2 + i * 2.8
    add_text(slide, x, 5.2, 2.5, 0.5, val, size=24, color=C_PRIMARY, bold=True, align=PP_ALIGN.CENTER)
    add_text(slide, x, 5.7, 2.5, 0.4, desc, size=14, color=C_LIGHT, align=PP_ALIGN.CENTER)
add_footer(slide, 2)

# ==========================================
# Slide 3: 技术栈
# ==========================================
slide = add_slide()
set_bg(slide, C_WHITE)
add_title_bar(slide, '二、技术选型')
techs = [
    ('后端', ['Spring Boot 2.7', 'MyBatis-Plus 3.5', 'MySQL 8.0', 'Redis (可选降级)'], C_PRIMARY),
    ('前端', ['Vue 3 + Vite', 'Element Plus', 'Pinia 状态管理', 'Axios 请求封装'], C_ACCENT),
    ('第三方', ['支付宝沙箱支付', 'Coze AI 工作流', 'JWT 令牌鉴权'], C_ORANGE),
    ('工具', ['Trae AI IDE 开发', 'Maven 构建管理', 'Git 版本控制'], C_PURPLE),
]
for i, (cat, items, color) in enumerate(techs):
    x = 0.8 + i * 3.0
    add_round_rect(slide, x, 1.5, 2.7, 4.5, C_BG)
    add_rect(slide, x, 1.5, 2.7, 0.6, color)
    add_text(slide, x+0.1, 1.55, 2.5, 0.5, cat, size=20, color=C_WHITE, bold=True, align=PP_ALIGN.CENTER)
    add_multi_text(slide, x+0.15, 2.3, 2.4, 3.5, ['• '+t for t in items], size=16, color=C_TEXT, spacing=1.8)
add_footer(slide, 3)

# ==========================================
# Slide 4: 系统架构
# ==========================================
slide = add_slide()
set_bg(slide, C_WHITE)
add_title_bar(slide, '三、系统架构设计', '前后端分离 · 三层架构')
# 架构层
layers = [
    ('前端展示层', 'Vue3 + Element Plus + Vite\n租客端 / 房东端 / 管理端', C_PRIMARY, 1.5),
    ('API 接口层', 'RESTful API · JWT 鉴权 · @RequireRole 角色控制', C_ACCENT, 3.0),
    ('业务逻辑层', 'Spring Boot Service · 事务管理 · 规则引擎兜底', C_ORANGE, 4.2),
    ('数据持久层', 'MyBatis-Plus · MySQL 8 · Redis 缓存(可降级)', C_PURPLE, 5.4),
]
for name, desc, color, y in layers:
    add_round_rect(slide, 1.5, y, 10, 1.0, C_BG)
    add_rect(slide, 1.5, y, 0.15, 1.0, color)
    add_text(slide, 1.8, y+0.05, 3, 0.5, name, size=20, color=color, bold=True)
    add_text(slide, 4.8, y+0.1, 6.5, 0.8, desc, size=16, color=C_TEXT)
# 箭头
for i in range(3):
    add_text(slide, 6.3, 2.55+i*1.2, 0.5, 0.4, '↓', size=24, color=C_LIGHT, align=PP_ALIGN.CENTER)
add_footer(slide, 4)

# ==========================================
# Slide 5: 功能模块图
# ==========================================
slide = add_slide()
set_bg(slide, C_WHITE)
add_title_bar(slide, '四、系统功能模块', '三种角色 · 全功能覆盖')
add_image(slide, '4.1.1_module.png', 0.8, 1.3, w=11.7)
add_footer(slide, 5)

# ==========================================
# Slide 6: E-R图
# ==========================================
slide = add_slide()
set_bg(slide, C_WHITE)
add_title_bar(slide, '五、数据库 E-R 设计', '8 大核心实体 · 11 张数据表')
add_image(slide, '4.2.1_er_overall.png', 0.8, 1.3, w=11.7)
add_footer(slide, 6)

# ==========================================
# Slide 7: 核心流程
# ==========================================
slide = add_slide()
set_bg(slide, C_WHITE)
add_title_bar(slide, '六、核心业务流程', '预订 → 选券 → 支付 → 核销')
add_image(slide, '4.3.5_flow_booking.png', 0.5, 1.3, w=6.0)
add_image(slide, '4.3.6_flow_pay.png', 6.8, 1.3, w=6.0)
add_text(slide, 0.8, 6.0, 5.5, 0.5, '预订下单流程', size=16, color=C_PRIMARY, bold=True, align=PP_ALIGN.CENTER)
add_text(slide, 7.0, 6.0, 5.5, 0.5, '支付宝支付流程', size=16, color=C_PRIMARY, bold=True, align=PP_ALIGN.CENTER)
add_footer(slide, 7)

# ==========================================
# Slide 8: 租客端功能(1)
# ==========================================
slide = add_slide()
set_bg(slide, C_WHITE)
add_title_bar(slide, '七、租客端 — 首页与搜索', '含 AI 智能推荐')
# 左图
add_text(slide, 0.8, 1.3, 5.5, 0.4, '首页（热门推荐 + 猜你喜欢）', size=18, color=C_PRIMARY, bold=True)
add_image(slide, '5.3.1_home.png', 0.8, 1.8, w=5.8)
# 右图
add_text(slide, 7.0, 1.3, 5.5, 0.4, '民宿搜索列表（多条件筛选）', size=18, color=C_PRIMARY, bold=True)
add_image(slide, '5.4.1_search.png', 7.0, 1.8, w=5.8)
add_footer(slide, 8)

# ==========================================
# Slide 9: 租客端功能(2)
# ==========================================
slide = add_slide()
set_bg(slide, C_WHITE)
add_title_bar(slide, '七、租客端 — 详情与预订', '房源日历 + 优惠券 + 支付')
add_text(slide, 0.8, 1.3, 3.5, 0.4, '民宿详情页（含日历）', size=16, color=C_PRIMARY, bold=True)
add_image(slide, '5.5.1_detail.png', 0.5, 1.8, w=4.1)
add_text(slide, 4.8, 1.3, 3.5, 0.4, '预订确认（选券）', size=16, color=C_PRIMARY, bold=True)
add_image(slide, '5.6.1_booking_confirm.png', 4.6, 1.8, w=4.1)
add_text(slide, 9.0, 1.3, 3.5, 0.4, '收银台（支付宝）', size=16, color=C_PRIMARY, bold=True)
add_image(slide, '5.7.1_cashier.png', 8.8, 1.8, w=4.1)
add_footer(slide, 9)

# ==========================================
# Slide 10: 租客端功能(3)
# ==========================================
slide = add_slide()
set_bg(slide, C_WHITE)
add_title_bar(slide, '七、租客端 — 订单与优惠券')
add_text(slide, 0.8, 1.3, 4, 0.4, '优惠券中心', size=18, color=C_PRIMARY, bold=True)
add_image(slide, '5.8.1_coupon_center.png', 0.5, 1.8, w=5.8)
add_text(slide, 7.0, 1.3, 4, 0.4, '我的订单', size=18, color=C_PRIMARY, bold=True)
add_image(slide, '5.9.1_orders.png', 6.8, 1.8, w=5.8)
add_footer(slide, 10)

# ==========================================
# Slide 11: 房东端
# ==========================================
slide = add_slide()
set_bg(slide, C_WHITE)
add_title_bar(slide, '八、房东端 — 经营管理', '看板 · 房源 · 订单 · 评价')
add_text(slide, 0.8, 1.3, 4, 0.4, '经营看板', size=18, color=C_ORANGE, bold=True)
add_image(slide, '5.12.1_host_dashboard.png', 0.5, 1.8, w=5.8)
add_text(slide, 7.0, 1.3, 4, 0.4, '房源管理 + 发布', size=18, color=C_ORANGE, bold=True)
add_image(slide, '5.13.1_host_homestays.png', 6.8, 1.8, w=5.8)
add_footer(slide, 11)

# ==========================================
# Slide 12: 管理端
# ==========================================
slide = add_slide()
set_bg(slide, C_WHITE)
add_title_bar(slide, '九、管理端 — 平台管控', '审核 · CRUD · 优惠券管理')
# 四宫格
imgs = [
    ('5.15.1_admin_dashboard.png', '平台看板', 0.5, 1.3),
    ('5.16.1_users.png', '用户管理', 6.8, 1.3),
    ('5.20.1_coupon_mgmt.png', '优惠券管理', 0.5, 4.3),
    ('5.17.2_homestay_mgmt.png', '房源管理', 6.8, 4.3),
]
for img, title, x, y in imgs:
    add_text(slide, x, y, 3, 0.35, title, size=16, color=C_PURPLE, bold=True)
    add_image(slide, img, x, y+0.35, w=5.8)
add_footer(slide, 12)

# ==========================================
# Slide 13: 亮点1 - AI智能推荐
# ==========================================
slide = add_slide()
set_bg(slide, C_WHITE)
add_title_bar(slide, '十、系统亮点 (1)', 'AI 智能推荐 — 协同过滤')
# 左：算法流程
add_text(slide, 0.8, 1.3, 6, 0.4, '推荐算法流程', size=18, color=C_PURPLE, bold=True)
add_image(slide, '7.1.1_recommend_flow.png', 0.5, 1.8, w=6.5)
# 右：效果
add_text(slide, 7.5, 1.3, 5, 0.4, '首页推荐效果', size=18, color=C_PURPLE, bold=True)
add_image(slide, '7.1.2_recommend_home.png', 7.5, 1.8, w=5.3)
add_round_rect(slide, 7.5, 4.5, 5.3, 2.0, C_BG)
add_multi_text(slide, 7.7, 4.6, 4.9, 1.8, [
    '① 收集偏好种子（收藏+订单）',
    '② 构建画像（城市/房型/价格）',
    '③ 候选匹配（SQL排除已看）',
    '④ 不足则热门补齐',
    '⑤ 未登录直接返回热门',
], size=15, color=C_TEXT, spacing=1.2)
add_footer(slide, 13)

# ==========================================
# Slide 14: 亮点2 - AI客服
# ==========================================
slide = add_slide()
set_bg(slide, C_WHITE)
add_title_bar(slide, '十、系统亮点 (2)', 'AI 智能客服 — Coze 双层架构')
# 左：客服面板
add_text(slide, 0.8, 1.3, 5, 0.4, 'AI 客服聊天面板', size=18, color=C_PURPLE, bold=True)
add_image(slide, '7.3.2_ai_chat_panel.png', 1.5, 1.8, h=4.5)
# 右：架构说明
add_round_rect(slide, 6.5, 1.5, 6.0, 4.5, C_BG)
add_text(slide, 6.7, 1.6, 5.5, 0.5, '双层架构设计', size=20, color=C_PURPLE, bold=True)
add_multi_text(slide, 6.7, 2.3, 5.5, 3.5, [
    '• 优先调用 Coze /v3/chat API',
    '  → 云端大模型智能对话',
    '',
    '• 未配置 Token 时自动降级',
    '  → 本地规则引擎兜底',
    '  → 覆盖 12 个业务 FAQ',
    '',
    '• 全局浮窗挂载',
    '  → 所有门户页面可见',
    '  → 快捷问题芯片引导',
], size=16, color=C_TEXT, spacing=1.0)
add_footer(slide, 14)

# ==========================================
# Slide 15: 亮点3 - 房源日历
# ==========================================
slide = add_slide()
set_bg(slide, C_WHITE)
add_title_bar(slide, '十、系统亮点 (3)', '房源 availability 日历视图')
add_text(slide, 0.8, 1.3, 5, 0.4, '日历视图效果', size=18, color=C_PURPLE, bold=True)
add_image(slide, '7.2.1_calendar.png', 1.0, 1.8, w=5.0)
# 右侧说明
add_round_rect(slide, 6.5, 1.5, 6.0, 4.5, C_BG)
add_text(slide, 6.7, 1.6, 5.5, 0.5, '日历功能说明', size=20, color=C_PURPLE, bold=True)
add_multi_text(slide, 6.7, 2.3, 5.5, 3.5, [
    '• 月度网格视图展示房源可订状态',
    '',
    '• 红色标记已订日期',
    '• 绿色标记可订日期',
    '• 灰色标记过去日期',
    '',
    '• 复用已有 booked-dates API',
    '• 左右箭头切换月份',
    '',
    '• 直观展示档期，提升预订体验',
], size=16, color=C_TEXT, spacing=1.0)
add_footer(slide, 15)

# ==========================================
# Slide 16: 优惠券系统设计
# ==========================================
slide = add_slide()
set_bg(slide, C_WHITE)
add_title_bar(slide, '十一、优惠券营销系统', '代金券 · 满减券 · 折扣券')
# 三种券类型
types = [
    ('代金券 CASH', '直接抵扣\n如：无门槛减20', C_RED, 0.8),
    ('满减券 FULL_REDUCE', '满额抵扣\n如：满300减50', C_ORANGE, 4.6),
    ('折扣券 DISCOUNT', '比例折扣\n如：8.8折封顶100', C_PURPLE, 8.4),
]
for name, desc, color, x in types:
    add_round_rect(slide, x, 1.5, 3.5, 1.8, C_BG)
    add_rect(slide, x, 1.5, 3.5, 0.5, color)
    add_text(slide, x+0.1, 1.55, 3.3, 0.4, name, size=16, color=C_WHITE, bold=True, align=PP_ALIGN.CENTER)
    add_text(slide, x+0.2, 2.1, 3.1, 1.0, desc, size=15, color=C_TEXT, align=PP_ALIGN.CENTER)

# 核销闭环
add_round_rect(slide, 0.8, 3.8, 11.5, 2.8, RGBColor(0xF6,0xFF,0xED))
add_text(slide, 1.0, 3.9, 10, 0.5, '核销闭环设计（事务内原子操作）', size=20, color=C_ACCENT, bold=True)
add_multi_text(slide, 1.0, 4.5, 11, 2.0, [
    '① 下单事务内 → 校验券状态(UNUSED) + 校验门槛 → 重算金额 → insert 订单',
    '② markUsed 原子更新 (WHERE user_id + status=UNUSED) → 失败则整体回滚',
    '③ 待支付取消（含30分钟超时）→ 自动退券回 UNUSED + used_count-1',
    '④ 已支付取消不退券  ⑤ 优惠后最低支付 0.01 元  ⑥ 并发下单不会超发',
], size=16, color=C_TEXT, spacing=1.5)
add_footer(slide, 16)

# ==========================================
# Slide 17: 测试与验证
# ==========================================
slide = add_slide()
set_bg(slide, C_WHITE)
add_title_bar(slide, '十二、系统测试与验证', '42 项端到端断言全部通过')
add_round_rect(slide, 0.8, 1.5, 11.5, 2.0, C_BG)
add_text(slide, 1.0, 1.6, 10, 0.5, '测试覆盖范围', size=20, color=C_PRIMARY, bold=True)
add_multi_text(slide, 1.0, 2.2, 11, 1.2, [
    '鉴权测试 · 领取限领/库存 · 有效期快照 · 三券型试算（¥1056单分别减20/50/126.72/100）',
    '门槛拦截 · 下单核销 · 重复用券拦截 · 取消退券 · 管理端CRUD权限',
], size=16, color=C_TEXT, spacing=1.5)

add_round_rect(slide, 0.8, 3.8, 5.5, 2.8, RGBColor(0xF6,0xFF,0xED))
add_text(slide, 1.0, 3.9, 5, 0.5, '后端验证', size=20, color=C_ACCENT, bold=True)
add_multi_text(slide, 1.0, 4.5, 5, 2.0, [
    '• mvn compile 通过',
    '• 临时 8090 端口启动',
    '• 42 项断言全通过',
    '• 测试数据已清理',
], size=16, color=C_TEXT, spacing=1.3)

add_round_rect(slide, 6.8, 3.8, 5.5, 2.8, RGBColor(0xFF,0xF7,0xE6))
add_text(slide, 7.0, 3.9, 5, 0.5, '前端验证', size=20, color=C_ORANGE, bold=True)
add_multi_text(slide, 7.0, 4.5, 5, 2.0, [
    '• npm run build 通过',
    '• Vite 代理 /api → 8080',
    '• 三端页面全部渲染正常',
    '• AI 客服浮窗全局可用',
], size=16, color=C_TEXT, spacing=1.3)
add_footer(slide, 17)

# ==========================================
# Slide 18: 总结
# ==========================================
slide = add_slide()
set_bg(slide, C_WHITE)
add_title_bar(slide, '十三、总结')
add_round_rect(slide, 0.8, 1.5, 11.5, 2.5, C_BG)
add_text(slide, 1.0, 1.6, 10, 0.5, '项目成果', size=22, color=C_PRIMARY, bold=True)
add_multi_text(slide, 1.0, 2.2, 11, 1.7, [
    '✓ 完整的民宿预订闭环：浏览 → 预订 → 支付 → 评价',
    '✓ 三种角色权限隔离：租客 / 房东 / 管理员',
    '✓ 支付宝沙箱真实支付集成',
    '✓ 优惠券营销系统：代金/满减/折扣三种 + 原子核销',
], size=18, color=C_TEXT, spacing=1.5)

add_round_rect(slide, 0.8, 4.3, 11.5, 2.3, RGBColor(0xF6,0xFF,0xED))
add_text(slide, 1.0, 4.4, 10, 0.5, '三大 AI 亮点', size=22, color=C_ACCENT, bold=True)
add_multi_text(slide, 1.0, 5.0, 11, 1.5, [
    '★ AI 智能推荐：基于收藏/订单的协同过滤，首页"猜你喜欢"',
    '★ AI 智能客服：Coze 双层架构，配 Token 走云端，不配走本地规则',
    '★ 房源日历视图：月度网格直观展示可订/已订日期',
], size=18, color=C_TEXT, spacing=1.3)
add_footer(slide, 18)

# ==========================================
# Slide 19: 致谢
# ==========================================
slide = add_slide()
set_bg(slide, C_DARK)
add_rect(slide, 0, 3.5, 13.333, 0.06, C_PRIMARY)
add_text(slide, 1, 2.0, 11, 1.5, '谢谢各位老师！', size=54, color=C_WHITE, bold=True, align=PP_ALIGN.CENTER)
add_text(slide, 1, 4.0, 11, 0.8, '恳请老师批评指正', size=28, color=C_PRIMARY, align=PP_ALIGN.CENTER)
add_text(slide, 1, 5.5, 11, 0.5, '答辩人：辛聪', size=22, color=C_LIGHT, align=PP_ALIGN.CENTER)

# ==========================================
# 保存
# ==========================================
prs.save(OUT)
print(f'PPT 已生成: {OUT}')
print(f'共 {len(prs.slides)} 页')
