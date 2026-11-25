<template>
  <div class="page-container coupon-page">
    <el-card shadow="never" class="head-card">
      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <el-tab-pane label="领券中心" name="center" />
        <el-tab-pane label="我的优惠券" name="mine" />
      </el-tabs>
    </el-card>

    <!-- 领券中心 -->
    <div v-if="activeTab === 'center'" v-loading="loadingCenter">
      <el-empty v-if="centerList.length === 0" description="暂无可领取的优惠券" />
      <div class="ticket-grid">
        <div v-for="c in centerList" :key="c.id" class="ticket" :class="{ disabled: c.soldOut }">
          <div class="ticket-left">
            <template v-if="c.type === 'DISCOUNT'">
              <div class="big-num">{{ (c.discountPercent / 10).toFixed(1) }}<span class="unit">折</span></div>
            </template>
            <template v-else>
              <div class="currency">¥</div>
              <div class="big-num">{{ Number(c.discountAmount).toFixed(0) }}</div>
            </template>
            <div class="threshold">{{ thresholdText(c) }}</div>
          </div>
          <div class="ticket-right">
            <div class="t-name">{{ c.name }}</div>
            <div class="t-type">
              <el-tag size="small" :type="typeTag(c.type)" effect="plain">{{ typeText(c.type) }}</el-tag>
              <span class="t-valid">{{ validText(c) }}</span>
            </div>
            <div class="t-desc" v-if="c.type === 'DISCOUNT' && c.maxDiscountAmount">
              最高优惠 ¥{{ c.maxDiscountAmount }}
            </div>
            <div class="t-stock">
              已领 {{ c.receivedCount }}/{{ c.totalCount === -1 ? '不限' : c.totalCount }}
            </div>
            <div class="t-action">
              <el-button
                v-if="!c.received && !c.soldOut"
                type="primary" round size="small"
                :loading="receivingId === c.id"
                @click="onReceive(c)">立即领取</el-button>
              <el-tag v-else-if="c.received" type="success" effect="plain" round>已领取</el-tag>
              <el-tag v-else type="info" effect="plain" round>已领完</el-tag>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 我的优惠券 -->
    <div v-if="activeTab === 'mine'" v-loading="loadingMine">
      <div class="mine-filter">
        <el-radio-group v-model="mineStatus" @change="loadMine">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button label="UNUSED">未使用</el-radio-button>
          <el-radio-button label="USED">已使用</el-radio-button>
          <el-radio-button label="EXPIRED">已过期</el-radio-button>
        </el-radio-group>
      </div>
      <el-empty v-if="mineList.length === 0" description="暂无优惠券，去领券中心看看吧" />
      <div class="ticket-grid">
        <div
          v-for="uc in mineList"
          :key="uc.id"
          class="ticket mine-ticket"
          :class="{ used: uc.status === 'USED', expired: uc.status === 'EXPIRED' }">
          <div class="ticket-left">
            <template v-if="uc.type === 'DISCOUNT'">
              <div class="big-num">{{ (uc.discountPercent / 10).toFixed(1) }}<span class="unit">折</span></div>
            </template>
            <template v-else>
              <div class="currency">¥</div>
              <div class="big-num">{{ Number(uc.discountAmount).toFixed(0) }}</div>
            </template>
            <div class="threshold">{{ thresholdText(uc) }}</div>
          </div>
          <div class="ticket-right">
            <div class="t-name">{{ uc.name }}</div>
            <div class="t-type">
              <el-tag size="small" :type="typeTag(uc.type)" effect="plain">{{ typeText(uc.type) }}</el-tag>
            </div>
            <div class="t-valid">{{ fmt(uc.validStartTime) }} 至 {{ fmt(uc.validEndTime) }}</div>
            <div class="t-action">
              <el-tag v-if="uc.status === 'UNUSED'" type="success" effect="dark" round>未使用</el-tag>
              <el-tag v-else-if="uc.status === 'USED'" type="info" effect="plain" round>已使用</el-tag>
              <el-tag v-else type="info" effect="plain" round>已过期</el-tag>
            </div>
            <div v-if="uc.status === 'UNUSED'" class="watermark">可用</div>
            <div v-else-if="uc.status === 'USED'" class="watermark">已使用</div>
            <div v-else class="watermark">已过期</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { couponCenter, receiveCoupon, myCoupons } from '@/api/coupon'

const activeTab = ref('center')
const loadingCenter = ref(false)
const loadingMine = ref(false)
const centerList = ref([])
const mineList = ref([])
const mineStatus = ref('UNUSED')
const receivingId = ref(null)

function typeText(t) {
  return { CASH: '代金券', FULL_REDUCE: '满减券', DISCOUNT: '折扣券' }[t] || '优惠券'
}
function typeTag(t) {
  return { CASH: 'warning', FULL_REDUCE: 'danger', DISCOUNT: 'success' }[t] || ''
}
function thresholdText(c) {
  const th = Number(c.threshold || 0)
  return th > 0 ? `满 ¥${th} 可用` : '无门槛'
}
function validText(c) {
  if (c.validType === 'DAYS') return `领取后 ${c.validDays} 天内有效`
  return `${fmt(c.validStartTime)} 至 ${fmt(c.validEndTime)}`
}
function fmt(t) {
  return t ? t.replace('T', ' ').substring(0, 10) : '-'
}

async function loadCenter() {
  loadingCenter.value = true
  try {
    centerList.value = await couponCenter()
  } finally {
    loadingCenter.value = false
  }
}

async function loadMine() {
  loadingMine.value = true
  try {
    mineList.value = await myCoupons(mineStatus.value)
  } finally {
    loadingMine.value = false
  }
}

function onTabChange(name) {
  if (name === 'mine' && mineList.value.length === 0) loadMine()
  if (name === 'center' && centerList.value.length === 0) loadCenter()
}

async function onReceive(c) {
  receivingId.value = c.id
  try {
    await receiveCoupon(c.id)
    ElMessage.success('领取成功，可在"我的优惠券"中查看')
    c.received = true
    c.receivedCount += 1
  } catch (e) {} finally {
    receivingId.value = null
  }
}

onMounted(loadCenter)
</script>

<style scoped>
.coupon-page {
  min-height: 300px;
}
.head-card {
  border-radius: 10px;
  margin-bottom: 16px;
}
.head-card :deep(.el-card__body) {
  padding: 0 16px;
}
.mine-filter {
  margin-bottom: 16px;
}
.ticket-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 16px;
}
.ticket {
  display: flex;
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(43, 51, 62, 0.07);
  min-height: 120px;
  position: relative;
}
.ticket-left {
  width: 130px;
  flex-shrink: 0;
  background: linear-gradient(135deg, #ff7a45 0%, #f5502f 100%);
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
}
.ticket-left::after {
  content: '';
  position: absolute;
  right: -6px;
  top: 0;
  bottom: 0;
  width: 12px;
  background-image: radial-gradient(circle, var(--bnb-bg, #f5f7fa) 4px, transparent 5px);
  background-size: 12px 14px;
  background-position: center;
  background-repeat: repeat-y;
}
.currency {
  font-size: 16px;
  font-weight: 700;
  margin-top: 10px;
}
.big-num {
  font-size: 38px;
  font-weight: 800;
  line-height: 1.1;
}
.big-num .unit {
  font-size: 16px;
  font-weight: 600;
  margin-left: 2px;
}
.threshold {
  font-size: 12px;
  opacity: 0.95;
  margin-top: 4px;
  margin-bottom: 8px;
}
.ticket-right {
  flex: 1;
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  position: relative;
  overflow: hidden;
}
.t-name {
  font-size: 15px;
  font-weight: 700;
  color: var(--bnb-text);
  padding-right: 70px;
}
.t-type {
  display: flex;
  align-items: center;
  gap: 8px;
}
.t-valid,
.t-desc,
.t-stock {
  font-size: 12px;
  color: var(--bnb-text-secondary);
}
.t-action {
  margin-top: auto;
}
.ticket.disabled .ticket-left {
  background: linear-gradient(135deg, #b6bdc5 0%, #9aa3ad 100%);
}
.mine-ticket.used,
.mine-ticket.expired {
  filter: grayscale(0.7);
}
.watermark {
  position: absolute;
  right: 8px;
  top: 10px;
  font-size: 13px;
  font-weight: 700;
  color: #bbb;
  border: 1.5px solid #ccc;
  border-radius: 4px;
  padding: 1px 6px;
  transform: rotate(-12deg);
}
.mine-ticket:not(.used):not(.expired) .watermark {
  color: #ff7a45;
  border-color: #ffbb99;
}
</style>
