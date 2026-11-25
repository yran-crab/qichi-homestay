<template>
  <div class="dash-page" v-loading="loading">
    <!-- 指标卡 -->
    <el-row :gutter="16">
      <el-col v-for="c in cards" :key="c.label" :xs="12" :sm="8" :md="8">
        <div class="metric-card card-shadow">
          <div class="metric-icon" :style="{ background: c.bg, color: c.color }">
            <el-icon :size="22"><component :is="c.icon" /></el-icon>
          </div>
          <div class="metric-body">
            <div class="metric-label">{{ c.label }}</div>
            <div class="metric-value">{{ c.value }}</div>
            <div class="metric-sub">{{ c.sub }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :xs="24" :md="16">
        <div class="chart-card card-shadow">
          <div class="chart-title">近 30 天收入趋势</div>
          <Chart :option="revenueOption" height="320px" :loading="loading" />
        </div>
      </el-col>
      <el-col :xs="24" :md="8">
        <div class="chart-card card-shadow">
          <div class="chart-title">订单状态分布</div>
          <Chart :option="statusOption" height="320px" :loading="loading" />
        </div>
      </el-col>
    </el-row>
    <el-row :gutter="16" class="chart-row">
      <el-col :span="24">
        <div class="chart-card card-shadow">
          <div class="chart-title">房源热度排行 TOP10</div>
          <Chart :option="rankOption" height="300px" :loading="loading" />
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { House, List, Money } from '@element-plus/icons-vue'
import Chart from '@/components/Chart.vue'
import { getHostDashboard } from '@/api/dashboard'

const data = ref(null)
const loading = ref(true)

const STATUS_TEXT = {
  PENDING_PAY: '待支付',
  PAID: '待入住',
  CHECKED_IN: '入住中',
  COMPLETED: '已完成',
  CANCELLED: '已取消'
}
const STATUS_COLOR = {
  PENDING_PAY: '#ffc24b',
  PAID: '#2e9e8f',
  CHECKED_IN: '#6a8eff',
  COMPLETED: '#5cc1b3',
  CANCELLED: '#c0c4cc'
}

function fmtMoney(v) {
  const n = Number(v || 0)
  return n.toLocaleString('zh-CN', { maximumFractionDigits: 2 })
}

const cards = computed(() => {
  if (!data.value) return []
  const d = data.value
  return [
    { label: '房源总数', value: d.totalHomestays, sub: '累计上架', icon: House, bg: '#e6f5f3', color: '#2e9e8f' },
    { label: '订单总数', value: d.totalOrders, sub: `本月 ${d.ordersThisMonth} 单`, icon: List, bg: '#eef2ff', color: '#6a8eff' },
    { label: '累计收入', value: `¥${fmtMoney(d.totalRevenue)}`, sub: `本月 ¥${fmtMoney(d.revenueThisMonth)}`, icon: Money, bg: '#fff1ea', color: '#ff7a45' }
  ]
})

/* 图1：近30天收入趋势（面积折线） */
const revenueOption = computed(() => {
  const list = data.value?.dailyRevenue || []
  return {
    tooltip: { trigger: 'axis', formatter: (p) => `${p[0].axisValue}<br/>收入：¥${p[0].data}` },
    grid: { left: 10, right: 20, top: 24, bottom: 24, containLabel: true },
    xAxis: { type: 'category', data: list.map((i) => i.d), boundaryGap: false, axisLabel: { color: '#7a8699', fontSize: 10, formatter: (v) => (typeof v === 'string' ? v.slice(5) : v) } },
    yAxis: { type: 'value', axisLabel: { color: '#7a8699', formatter: (v) => (v >= 1000 ? v / 1000 + 'k' : v) }, splitLine: { lineStyle: { color: '#f0f2f5' } } },
    series: [{
      type: 'line',
      smooth: true,
      data: list.map((i) => Number(i.amt || 0)),
      itemStyle: { color: '#ff7a45' },
      areaStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: 'rgba(255,122,69,0.35)' }, { offset: 1, color: 'rgba(255,122,69,0.02)' }] } }
    }]
  }
})

/* 图2：订单状态分布（饼图） */
const statusOption = computed(() => {
  const list = data.value?.orderStatusDist || []
  return {
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, type: 'scroll', textStyle: { color: '#7a8699' } },
    series: [{
      type: 'pie',
      radius: ['40%', '65%'],
      center: ['50%', '45%'],
      label: { show: true, formatter: '{b}\n{d}%', color: '#2b333e' },
      data: list.map((i) => ({
        name: STATUS_TEXT[i.name] || i.name,
        value: Number(i.cnt || 0),
        itemStyle: { color: STATUS_COLOR[i.name] || '#5cc1b3' }
      }))
    }]
  }
})

/* 图3：房源热度排行 TOP10（横向柱状，双指标） */
const rankOption = computed(() => {
  const list = (data.value?.homestayRank || []).slice().reverse()
  return {
    tooltip: { trigger: 'axis' },
    legend: { top: 0, data: ['浏览量', '订单量'] },
    grid: { left: 10, right: 30, top: 36, bottom: 10, containLabel: true },
    xAxis: { type: 'value', axisLabel: { color: '#7a8699' }, splitLine: { lineStyle: { color: '#f0f2f5' } } },
    yAxis: { type: 'category', data: list.map((i) => i.name), axisLabel: { color: '#2b333e', width: 140, overflow: 'truncate' } },
    series: [
      { name: '浏览量', type: 'bar', data: list.map((i) => Number(i.views || 0)), barWidth: 12, itemStyle: { color: '#6a8eff', borderRadius: [0, 4, 4, 0] } },
      { name: '订单量', type: 'bar', data: list.map((i) => Number(i.orders || 0)), barWidth: 12, itemStyle: { color: '#2e9e8f', borderRadius: [0, 4, 4, 0] } }
    ]
  }
})

async function load() {
  loading.value = true
  try {
    data.value = await getHostDashboard()
  } finally {
    loading.value = false
  }
}
onMounted(load)
</script>

<style scoped>
.dash-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.metric-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 16px;
  border-radius: 10px;
  margin-bottom: 4px;
}
.metric-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.metric-label {
  font-size: 13px;
  color: var(--bnb-text-secondary);
}
.metric-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--bnb-text);
  line-height: 1.2;
  margin-top: 2px;
}
.metric-sub {
  font-size: 12px;
  color: var(--bnb-text-secondary);
  margin-top: 4px;
}
.chart-card {
  background: #fff;
  border-radius: 10px;
  padding: 16px 18px 12px;
}
.chart-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--bnb-text);
  margin-bottom: 8px;
}
</style>
