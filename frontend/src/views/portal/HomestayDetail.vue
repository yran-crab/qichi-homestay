<template>
  <div class="page-container detail-page" v-loading="loading">
    <template v-if="detail">
      <!-- 标题区 -->
      <div class="detail-head">
        <div>
          <h2 class="detail-title">{{ detail.title }}</h2>
          <div class="detail-sub">
            <el-rate :model-value="Number(detail.rating)" disabled show-score score-template="{value}"
              :max="5" class="rate" />
            <span>{{ detail.reviewCount }} 条评价</span>
            <el-divider direction="vertical" />
            <el-icon><Location /></el-icon>
            <span>{{ detail.city }} · {{ detail.address }}</span>
            <el-divider direction="vertical" />
            <span class="view-text">浏览 {{ detail.viewCount }}</span>
          </div>
        </div>
        <el-button
          class="fav-toggle"
          :type="detail.favored ? 'warning' : 'default'"
          :plain="!detail.favored"
          round
          @click="onToggleFavorite">
          <el-icon class="fav-icon"><StarFilled v-if="detail.favored" /><Star v-else /></el-icon>
          {{ detail.favored ? '已收藏' : '收藏' }} {{ detail.favoriteCount }}
        </el-button>
      </div>

      <el-row :gutter="20" class="detail-main">
        <!-- 左侧主内容 -->
        <el-col :xs="24" :md="16">
          <!-- 图片轮播 -->
          <el-carousel height="380px" class="gallery" indicator-position="outside" arrow="hover">
            <el-carousel-item v-for="(img, i) in gallery" :key="i">
              <img :src="img" class="gallery-img" :alt="detail.title + i" />
            </el-carousel-item>
          </el-carousel>

          <!-- 核心信息 -->
          <el-card shadow="never" class="info-card">
            <div class="info-grid">
              <div class="info-item"><span class="info-label">房源类型</span>{{ detail.type }}</div>
              <div class="info-item"><span class="info-label">可住人数</span>{{ detail.maxGuests }} 人</div>
              <div class="info-item"><span class="info-label">卧室</span>{{ detail.bedrooms }} 间</div>
              <div class="info-item"><span class="info-label">客厅</span>{{ detail.livingRooms }} 间</div>
              <div class="info-item"><span class="info-label">卫生间</span>{{ detail.bathrooms }} 间</div>
              <div class="info-item"><span class="info-label">床位</span>{{ detail.beds }} 张</div>
              <div class="info-item" v-if="detail.area"><span class="info-label">面积</span>{{ detail.area }} ㎡</div>
              <div class="info-item" v-if="detail.floorInfo"><span class="info-label">楼层</span>{{ detail.floorInfo }}</div>
            </div>
          </el-card>

          <!-- 配套设施 -->
          <el-card shadow="never" class="info-card">
            <template #header><span class="card-title">配套设施</span></template>
            <div class="facility-grid">
              <div v-for="f in detail.facilities" :key="f.id" class="facility-item">
                <span class="facility-icon">{{ f.icon || '✓' }}</span>{{ f.name }}
              </div>
            </div>
          </el-card>

          <!-- 房源描述 -->
          <el-card shadow="never" class="info-card">
            <template #header><span class="card-title">房源介绍</span></template>
            <div class="description">{{ detail.description }}</div>
            <template v-if="detail.rules">
              <div class="rules-title">入住说明</div>
              <div class="rules-text">{{ detail.rules }}</div>
            </template>
          </el-card>

          <!-- 评价区 -->
          <el-card shadow="never" class="info-card">
            <template #header><span class="card-title">用户评价（{{ detail.reviewCount }}）</span></template>
            <div v-loading="reviewLoading">
              <el-empty v-if="!reviewLoading && reviews.length === 0" description="暂无评价" :image-size="80" />
              <div v-else class="review-list">
                <div v-for="r in reviews" :key="r.id" class="review-item">
                  <div class="review-head">
                    <el-avatar :size="36" :src="r.tenantAvatar">{{ (r.tenantName || '匿').charAt(0) }}</el-avatar>
                    <div class="review-meta">
                      <div class="review-name">{{ r.tenantName || '匿名用户' }}</div>
                      <el-rate :model-value="r.rating" disabled size="small" />
                    </div>
                    <span class="review-date">{{ fmtDate(r.createdAt) }}</span>
                  </div>
                  <div class="review-content">{{ r.content }}</div>
                  <div v-if="r.images && r.images.length" class="review-images">
                    <el-image
                      v-for="(img, i) in r.images"
                      :key="i"
                      :src="img"
                      :preview-src-list="r.images"
                      :initial-index="i"
                      fit="cover"
                      class="review-img" />
                  </div>
                  <div v-if="r.reply" class="review-reply">
                    <span class="reply-tag">房东回复：</span>{{ r.reply }}
                  </div>
                </div>
              </div>
              <div v-if="reviewTotal > reviewPageSize" class="review-pager">
                <el-pagination
                  background
                  layout="prev, pager, next"
                  :total="reviewTotal"
                  :page-size="reviewPageSize"
                  :current-page="reviewPage"
                  @current-change="onReviewPage" />
              </div>
            </div>
          </el-card>

          <!-- 房源可用性日历 -->
          <el-card shadow="never" class="info-card">
            <template #header>
              <div class="calendar-head">
                <span class="card-title">房源日历</span>
                <div class="calendar-legend">
                  <span class="legend-item"><i class="legend-dot booked"></i>已预订</span>
                  <span class="legend-item"><i class="legend-dot available"></i>可预订</span>
                </div>
              </div>
            </template>
            <div class="calendar-nav">
              <el-button text @click="calMonth--"><el-icon><ArrowLeft /></el-icon></el-button>
              <span class="cal-month-label">{{ calYear }} 年 {{ calMonth }} 月</span>
              <el-button text @click="calMonth++"><el-icon><ArrowRight /></el-icon></el-button>
            </div>
            <div class="mini-calendar">
              <div class="cal-week-row">
                <div v-for="w in ['日','一','二','三','四','五','六']" :key="w" class="cal-week-cell">{{ w }}</div>
              </div>
              <div class="cal-day-grid">
                <div
                  v-for="(d, i) in calCells"
                  :key="i"
                  class="cal-day-cell"
                  :class="{
                    'cal-other': !d.current,
                    'cal-booked': d.booked,
                    'cal-past': d.past,
                    'cal-today': d.today
                  }">
                  <span class="cal-day-num">{{ d.day }}</span>
                  <span v-if="d.booked" class="cal-day-tag">已订</span>
                  <span v-else-if="!d.past && d.current" class="cal-day-tag cal-avail-tag">可订</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :md="8">
          <div class="book-card card-shadow">
            <div class="book-price">
              <span class="price-num">¥{{ detail.price }}</span><span class="price-unit">/晚</span>
              <span v-if="detail.originalPrice" class="origin-price">¥{{ detail.originalPrice }}</span>
            </div>
            <div class="book-field">
              <label>入住 / 退房日期</label>
              <el-date-picker
                v-model="dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="入住"
                end-placeholder="退房"
                value-format="YYYY-MM-DD"
                :disabled-date="disableDate"
                style="width: 100%" />
            </div>
            <div class="book-field">
              <label>入住人数</label>
              <el-input-number v-model="guests" :min="1" :max="detail.maxGuests" style="width: 100%" />
            </div>
            <div class="book-summary" v-if="nights > 0">
              <span>共 {{ nights }} 晚</span>
              <span class="summary-price">¥{{ nights * Number(detail.price) }}</span>
            </div>
            <el-button type="primary" class="book-btn" @click="onBook">立即预订</el-button>

            <!-- 房东卡片 -->
            <div class="host-card" v-if="detail.host">
              <el-avatar :size="44" :src="detail.host.avatar">
                {{ (detail.host.realName || '房').charAt(0) }}
              </el-avatar>
              <div class="host-info">
                <div class="host-name">{{ detail.host.realName || '房东' }}</div>
                <div class="host-desc">认证房东 · 用心接待每位旅客</div>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Location, Star, StarFilled, ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { homestayDetail } from '@/api/homestay'
import { bookedDates } from '@/api/order'
import { homestayReviews } from '@/api/review'
import { toggleFavorite } from '@/api/favorite'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const detail = ref(null)
const dateRange = ref(null)
const guests = ref(1)
const bookedRanges = ref([])

const reviews = ref([])
const reviewLoading = ref(false)
const reviewTotal = ref(0)
const reviewPage = ref(1)
const reviewPageSize = ref(5)

// 日历
const now = new Date()
const calYear = ref(now.getFullYear())
const calMonth = ref(now.getMonth() + 1)
const bookedDateSet = computed(() => {
  const set = new Set()
  for (const r of bookedRanges.value) {
    const s = new Date(r.checkInDate + 'T00:00:00')
    const e = new Date(r.checkOutDate + 'T00:00:00')
    for (let t = s.getTime(); t < e.getTime(); t += 86400000) {
      const d = new Date(t)
      set.add(`${d.getFullYear()}-${d.getMonth() + 1}-${d.getDate()}`)
    }
  }
  return set
})
const calCells = computed(() => {
  const y = calYear.value
  const m = calMonth.value
  const firstDay = new Date(y, m - 1, 1).getDay()
  const daysInMonth = new Date(y, m, 0).getDate()
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const cells = []
  for (let i = 0; i < firstDay; i++) {
    cells.push({ day: '', current: false })
  }
  for (let d = 1; d <= daysInMonth; d++) {
    const date = new Date(y, m - 1, d)
    const key = `${y}-${m}-${d}`
    cells.push({
      day: d,
      current: true,
      booked: bookedDateSet.value.has(key),
      past: date.getTime() < today.getTime(),
      today: date.getTime() === today.getTime()
    })
  }
  const tail = (7 - cells.length % 7) % 7
  for (let i = 0; i < tail; i++) {
    cells.push({ day: '', current: false })
  }
  return cells
})

const gallery = computed(() => {
  if (!detail.value) return []
  const imgs = detail.value.images || []
  return imgs.length ? imgs : [detail.value.coverImg]
})

const nights = computed(() => {
  if (!dateRange.value || dateRange.value.length !== 2) return 0
  const [inDate, outDate] = dateRange.value
  return Math.round((new Date(outDate) - new Date(inDate)) / 86400000)
})

function disableDate(date) {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  if (date.getTime() < today.getTime()) return true
  // 占用区间 [入住日, 退房日) 内的日期不可选（退房当天可入住）
  return bookedRanges.value.some((r) => {
    const s = new Date(r.checkInDate + 'T00:00:00')
    const e = new Date(r.checkOutDate + 'T00:00:00')
    return date.getTime() >= s.getTime() && date.getTime() < e.getTime()
  })
}

async function loadDetail() {
  loading.value = true
  try {
    const [d, ranges] = await Promise.all([
      homestayDetail(route.params.id),
      bookedDates(route.params.id).catch(() => [])
    ])
    detail.value = d
    bookedRanges.value = ranges || []
    if (route.query.in && route.query.out) {
      dateRange.value = [route.query.in, route.query.out]
      guests.value = Number(route.query.guests) || 1
    }
    loadReviews()
  } finally {
    loading.value = false
  }
}

async function loadReviews() {
  reviewLoading.value = true
  try {
    const res = await homestayReviews(route.params.id, {
      page: reviewPage.value,
      pageSize: reviewPageSize.value
    })
    reviews.value = res.list || []
    reviewTotal.value = res.total
  } catch (e) {
    // 静默处理
  } finally {
    reviewLoading.value = false
  }
}

function onReviewPage(p) {
  reviewPage.value = p
  loadReviews()
}

function fmtDate(t) {
  return t ? t.replace('T', ' ').substring(0, 10) : ''
}

function onBook() {
  if (!dateRange.value || dateRange.value.length !== 2) {
    ElMessage.warning('请选择入住和退房日期')
    return
  }
  if (!userStore.token) {
    ElMessage.info('请先登录后再预订')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  router.push({
    path: `/booking/confirm/${detail.value.id}`,
    query: {
      in: dateRange.value[0],
      out: dateRange.value[1],
      guests: guests.value
    }
  })
}

async function onToggleFavorite() {
  if (!userStore.token) {
    ElMessage.info('请先登录后再收藏')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  try {
    const favored = await toggleFavorite(detail.value.id)
    detail.value.favored = favored
    detail.value.favoriteCount = Math.max(0, (detail.value.favoriteCount || 0) + (favored ? 1 : -1))
    ElMessage.success(favored ? '已加入收藏' : '已取消收藏')
  } catch (e) {
    // 全局拦截器已提示
  }
}

onMounted(loadDetail)
</script>

<style scoped>
.detail-head {
  margin-bottom: 16px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}
.fav-toggle {
  flex-shrink: 0;
  margin-top: 2px;
}
.fav-icon {
  margin-right: 4px;
}
.detail-title {
  margin: 0 0 8px;
  font-size: 24px;
}
.detail-sub {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--bnb-text-secondary);
  font-size: 13.5px;
}
.rate {
  height: 22px;
}
.view-text {
  color: var(--bnb-text-secondary);
}
.gallery {
  border-radius: 10px;
  overflow: hidden;
  margin-bottom: 16px;
}
.gallery-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.info-card {
  margin-bottom: 16px;
}
.card-title {
  font-weight: 600;
  font-size: 16px;
}
.info-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px 10px;
}
.info-item {
  font-size: 14px;
}
.info-label {
  display: block;
  color: var(--bnb-text-secondary);
  font-size: 12.5px;
  margin-bottom: 2px;
}
.facility-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}
.facility-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: var(--bnb-text-regular);
}
.facility-icon {
  color: var(--bnb-primary);
  width: 20px;
  text-align: center;
}
.description {
  white-space: pre-wrap;
  line-height: 1.8;
  font-size: 14px;
  color: var(--bnb-text-regular);
}
.rules-title {
  margin-top: 14px;
  font-weight: 600;
  margin-bottom: 6px;
}
.rules-text {
  font-size: 14px;
  color: var(--bnb-text-regular);
  white-space: pre-wrap;
}
.book-card {
  background: #fff;
  border-radius: 10px;
  padding: 20px;
  position: sticky;
  top: 16px;
}
.book-price {
  margin-bottom: 16px;
}
.book-price .price-num {
  color: var(--bnb-accent);
  font-size: 26px;
  font-weight: 700;
}
.book-price .price-unit {
  color: var(--bnb-text-secondary);
  font-size: 13px;
}
.origin-price {
  margin-left: 8px;
  color: var(--bnb-text-secondary);
  text-decoration: line-through;
  font-size: 13px;
}
.book-field {
  margin-bottom: 14px;
}
.book-field label {
  display: block;
  font-size: 13px;
  color: var(--bnb-text-secondary);
  margin-bottom: 6px;
}
.book-summary {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  font-size: 14px;
}
.summary-price {
  color: var(--bnb-accent);
  font-weight: 700;
  font-size: 17px;
}
.book-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
}
.host-card {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 18px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}
.host-name {
  font-weight: 600;
  font-size: 14.5px;
}
.host-desc {
  font-size: 12.5px;
  color: var(--bnb-text-secondary);
  margin-top: 2px;
}
.review-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.review-item {
  padding-bottom: 16px;
  border-bottom: 1px solid #f2f2f2;
}
.review-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}
.review-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}
.review-meta {
  flex: 1;
}
.review-name {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 2px;
}
.review-date {
  color: var(--bnb-text-secondary);
  font-size: 12.5px;
}
.review-content {
  font-size: 14px;
  line-height: 1.7;
  color: var(--bnb-text-regular);
  white-space: pre-wrap;
}
.review-images {
  display: flex;
  gap: 8px;
  margin-top: 10px;
  flex-wrap: wrap;
}
.review-img {
  width: 80px;
  height: 80px;
  border-radius: 6px;
}
.review-reply {
  margin-top: 10px;
  padding: 10px 12px;
  background: #f9f9f4;
  border-radius: 6px;
  font-size: 13px;
  color: var(--bnb-text-regular);
  line-height: 1.6;
}
.reply-tag {
  color: var(--bnb-primary);
  font-weight: 600;
}
.review-pager {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}
@media (max-width: 768px) {
  .info-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .facility-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

/* ===== 房源日历 ===== */
.calendar-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.calendar-legend {
  display: flex;
  gap: 14px;
}
.legend-item {
  font-size: 12px;
  color: var(--bnb-text-secondary);
  display: flex;
  align-items: center;
  gap: 4px;
}
.legend-dot {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 2px;
}
.legend-dot.booked {
  background: #f56c6c;
}
.legend-dot.available {
  background: #67c23a;
}
.calendar-nav {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-bottom: 12px;
}
.cal-month-label {
  font-size: 15px;
  font-weight: 600;
  min-width: 100px;
  text-align: center;
}
.mini-calendar {
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  overflow: hidden;
}
.cal-week-row {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  background: var(--bnb-bg, #f5f7fa);
}
.cal-week-cell {
  text-align: center;
  padding: 6px 0;
  font-size: 12px;
  font-weight: 600;
  color: var(--bnb-text-secondary);
}
.cal-day-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
}
.cal-day-cell {
  min-height: 52px;
  border: 1px solid var(--el-border-color-lighter);
  border-top: none;
  border-left: none;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
}
.cal-day-cell:nth-child(7n) {
  border-right: none;
}
.cal-day-cell:nth-last-child(-n+7) {
  border-bottom: none;
}
.cal-other {
  background: var(--bnb-bg, #f5f7fa);
}
.cal-day-num {
  font-size: 13px;
  font-weight: 500;
}
.cal-day-tag {
  font-size: 10px;
  padding: 1px 4px;
  border-radius: 3px;
}
.cal-booked {
  background: rgba(245, 108, 108, 0.08);
}
.cal-booked .cal-day-tag {
  background: #f56c6c;
  color: #fff;
}
.cal-past {
  color: #ccc;
}
.cal-past .cal-day-num {
  color: #ccc;
}
.cal-today .cal-day-num {
  color: var(--bnb-primary);
  font-weight: 700;
}
.cal-avail-tag {
  background: #e1f3d8;
  color: #67c23a;
}
</style>
