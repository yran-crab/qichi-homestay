<template>
  <div class="dash-page" v-loading="loading">
    <!-- 指标卡 -->
    <el-row :gutter="16">
      <el-col v-for="c in cards" :key="c.label" :xs="12" :sm="12" :md="6">
        <div class="metric-card card-shadow">
          <div class="metric-icon" :style="{ background: c.bg, color: c.color }">
            <el-icon :size="22"><component :is="c.icon" /></el-icon>
          </div>
          <div class="metric-body">
            <div class="metric-label">{{ c.label }}</div>
            <div class="metric-value">{{ c.value }}</div>
            <div class="metric-sub" :class="c.subClass">{{ c.sub }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 待办提醒 -->
    <div class="todo-bar card-shadow" v-if="data">
      <div class="todo-item">
        <el-icon class="todo-icon todo-warn"><WarningFilled /></el-icon>
        <span>待审核房东：<b>{{ data.pendingHosts }}</b> 人</span>
      </div>
      <div class="todo-item">
        <el-icon class="todo-icon todo-warn"><WarningFilled /></el-icon>
        <span>待审核房源：<b>{{ data.pendingHomestays }}</b> 套</span>
      </div>
      <el-button class="todo-btn" type="primary" plain size="small" @click="$router.push('/admin/host-audit')">前往审核</el-button>
    </div>

    <!-- 图表区 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :xs="24" :md="16">
        <div class="chart-card card-shadow">
          <div class="chart-title">近 30 天订单与交易额趋势</div>
          <Chart :option="trendOption" height="320px" :loading="loading" />
        </div>
      </el-col>
      <el-col :xs="24" :md="8">
        <div class="chart-card card-shadow">
          <div class="chart-title">房源类型占比</div>
          <Chart :option="typeOption" height="320px" :loading="loading" />
        </div>
      </el-col>
    </el-row>
    <el-row :gutter="16" class="chart-row">
      <el-col :xs="24" :md="12">
        <div class="chart-card card-shadow">
          <div class="chart-title">热销城市 TOP10</div>
          <Chart :option="cityOption" height="300px" :loading="loading" />
        </div>
      </el-col>
      <el-col :xs="24" :md="12">
        <div class="chart-card card-shadow">
          <div class="chart-title">本周新增概览</div>
          <Chart :option="weekOption" height="300px" :loading="loading" />
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { User, House, List, Money, WarningFilled } from '@element-plus/icons-vue'
import Chart from '@/components/Chart.vue'
import { getAdminDashboard } from '@/api/dashboard'

const data = ref(null)
const loading = ref(true)

/** 日期兼容：字符串/数组[年,月,日] -> 'YYYY-MM-DD' */
function normDate(d) {
  if (!d) return ''
  if (typeof d === 'string') return d.slice(0, 10)
  if (Array.isArray(d) && d.length >= 3) {
    return `${d[0]}-${String(d[1]).padStart(2, '0')}-${String(d[2]).padStart(2, '0')}`
  }
  return String(d)
}

const cards = computed(() => {
  if (!data.value) return []
  const d = data.value
  return [
    { label: '用户总数', value: d.totalUsers, sub: `本周新增 ${d.newUsersThisWeek}`, icon: User, bg: '#e6f5f3', color: '#2e9e8f', subClass: 'sub-up' },
    { label: '房源总数', value: d.totalHomestays, sub: `本周新增 ${d.newHomestaysThisWeek}`, icon: House, bg: '#fff1ea', color: '#ff7a45', subClass: 'sub-up' },
    { label: '订单总数', value: d.totalOrders, sub: `本周新增 ${d.newOrdersThisWeek}`, icon: List, bg: '#eef2ff', color: '#6a8eff', subClass: 'sub-up' },
    { label: '交易总额', value: `¥${fmtMoney(d.totalRevenue)}`, sub: `本周 ¥${fmtMoney(d.revenueThisWeek)}`, icon: Money, bg: '#fff8e6', color: '#ffc24b', subClass: 'sub-up' }
  ]
})

function fmtMoney(v) {
  const n = Number(v || 0)
  return n.toLocaleString('zh-CN', { maximumFractionDigits: 2 })
}

/* 图1：近30天订单量与交易额双折线 */
const trendOption = computed(() => {
  const list = data.value?.dailyStats || []
  const dates = list.map((i) => normDate(i.d))
  const counts = list.map((i) => Number(i.cnt || 0))
  const amts = list.map((i) => Number(i.amt || 0))
  return {
    tooltip: { trigger: 'axis' },
    legend: { data: ['订单量', '交易额'], top: 0 },
    grid: { left: 10, right: 20, top: 36, bottom: 24, containLabel: true },
    xAxis: { type: 'category', data: dates, boundaryGap: false, axisLabel: { color: '#7a8699', fontSize: 10, formatter: (v) => v.slice(5) } },
    yAxis: [
      { type: 'value', name: '订单量', axisLabel: { color: '#7a8699' }, splitLine: { lineStyle: { color: '#f0f2f5' } } },
      { type: 'value', name: '交易额', axisLabel: { color: '#7a8699', formatter: (v) => (v >= 1000 ? v / 1000 + 'k' : v) }, splitLine: { show: false } }
    ],
    series: [
      { name: '订单量', type: 'line', smooth: true, data: counts, itemStyle: { color: '#2e9e8f' }, areaStyle: { color: 'rgba(46,158,143,0.12)' } },
      { name: '交易额', type: 'line', smooth: true, yAxisIndex: 1, data: amts, itemStyle: { color: '#ff7a45' }, areaStyle: { color: 'rgba(255,122,69,0.12)' } }
    ]
  }
})

/* 图2：房源类型占比（环形饼） */
const typeOption = computed(() => {
  const list = data.value?.typeDist || []
  return {
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, type: 'scroll', textStyle: { color: '#7a8699' } },
    series: [{
      type: 'pie',
      radius: ['40%', '65%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: true,
      label: { show: true, formatter: '{b}\n{d}%', color: '#2b333e' },
      data: list.map((i) => ({ name: i.name, value: Number(i.cnt || 0) }))
    }]
  }
})

/* 图3：热销城市 TOP10 横向柱状 */
const cityOption = computed(() => {
  const list = (data.value?.topCities || []).slice().reverse()
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: 10, right: 30, top: 16, bottom: 10, containLabel: true },
    xAxis: { type: 'value', axisLabel: { color: '#7a8699' }, splitLine: { lineStyle: { color: '#f0f2f5' } } },
    yAxis: { type: 'category', data: list.map((i) => i.name), axisLabel: { color: '#2b333e' } },
    series: [{
      type: 'bar',
      data: list.map((i) => Number(i.cnt || 0)),
      barWidth: 14,
      itemStyle: {
        borderRadius: [0, 6, 6, 0],
        color: { type: 'linear', x: 0, y: 0, x2: 1, y2: 0, colorStops: [{ offset: 0, color: '#5cc1b3' }, { offset: 1, color: '#2e9e8f' }] }
      },
      label: { show: true, position: 'right', color: '#7a8699' }
    }]
  }
})

/* 图4：本周新增概览柱状 */
const weekOption = computed(() => {
  const d = data.value || {}
  return {
    tooltip: { trigger: 'axis' },
    legend: { top: 0, data: ['本周新增'] },
    grid: { left: 10, right: 20, top: 36, bottom: 20, containLabel: true },
    xAxis: { type: 'category', data: ['用户', '房源', '订单'], axisLabel: { color: '#2b333e' } },
    yAxis: [{ type: 'value', minInterval: 1, axisLabel: { color: '#7a8699' }, splitLine: { lineStyle: { color: '#f0f2f5' } } }],
    series: [{
      name: '本周新增',
      type: 'bar',
      data: [
        { value: d.newUsersThisWeek || 0, itemStyle: { color: '#2e9e8f' } },
        { value: d.newHomestaysThisWeek || 0, itemStyle: { color: '#ff7a45' } },
        { value: d.newOrdersThisWeek || 0, itemStyle: { color: '#6a8eff' } }
      ],
      barWidth: 32,
      itemStyle: { borderRadius: [6, 6, 0, 0] },
      label: { show: true, position: 'top', color: '#2b333e' }
    }]
  }
})

async function load() {
  loading.value = true
  try {
    data.value = await getAdminDashboard()
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
.sub-up {
  color: var(--bnb-primary);
}
.todo-bar {
  display: flex;
  align-items: center;
  gap: 28px;
  padding: 14px 20px;
  border-radius: 10px;
  background: #fff;
}
.todo-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: var(--bnb-text);
}
.todo-icon {
  font-size: 18px;
}
.todo-warn {
  color: #ff7a45;
}
.todo-btn {
  margin-left: auto;
}
.chart-row {
  margin-top: 0 !important;
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
