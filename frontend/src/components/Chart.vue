<template>
  <div class="chart-wrap" :style="{ height }">
    <div ref="el" class="chart-canvas" />
    <div v-if="empty" class="chart-empty">
      <el-icon :size="40"><PieChart /></el-icon>
      <span>暂无数据</span>
    </div>
  </div>
</template>

<script setup>
import { ref, shallowRef, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { PieChart } from '@element-plus/icons-vue'

const props = defineProps({
  option: { type: Object, default: () => ({}) },
  height: { type: String, default: '320px' },
  loading: { type: Boolean, default: false }
})

const el = ref(null)
const empty = ref(false)
const chart = shallowRef(null)
let ro = null

// 统一配色（民宿风：青绿主色 + 暖色辅色）
const PALETTE = [
  '#2e9e8f', '#5cc1b3', '#ff7a45', '#ffc24b',
  '#6a8eff', '#9b5de6', '#f15bb5', '#00bbf9'
]

function baseOption() {
  return {
    color: PALETTE,
    textStyle: { color: '#2b333e', fontSize: 12 },
    grid: { left: 12, right: 20, top: 40, bottom: 12, containLabel: true },
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { type: 'scroll', top: 6, textStyle: { color: '#7a8699' } }
  }
}

function isEmpty(opt) {
  const series = opt && opt.series
  if (!series || !series.length) return true
  return series.every((s) => !s.data || !s.data.length)
}

function render() {
  if (!chart.value) return
  const merged = { ...baseOption(), ...props.option }
  // 合并 grid / tooltip / legend 等子对象，避免被浅覆盖丢失
  merged.grid = { ...baseOption().grid, ...(props.option.grid || {}) }
  merged.tooltip = { ...baseOption().tooltip, ...(props.option.tooltip || {}) }
  merged.legend = { ...baseOption().legend, ...(props.option.legend || {}) }
  empty.value = isEmpty(merged)
  chart.value.setOption(merged, true)
  chart.value.hideLoading()
  if (props.loading) chart.value.showLoading('default', { text: '', color: '#2e9e8f', textColor: '#7a8699', maskColor: 'rgba(255,255,255,0.6)' })
}

function resize() {
  chart.value && chart.value.resize()
}

onMounted(async () => {
  await nextTick()
  chart.value = echarts.init(el.value)
  ro = new ResizeObserver(() => resize())
  ro.observe(el.value)
  render()
  window.addEventListener('resize', resize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resize)
  if (ro) ro.disconnect()
  if (chart.value) {
    chart.value.dispose()
    chart.value = null
  }
})

watch(() => props.option, render, { deep: true })
watch(() => props.loading, () => {
  if (!chart.value) return
  if (props.loading) chart.value.showLoading('default', { text: '', color: '#2e9e8f', maskColor: 'rgba(255,255,255,0.6)' })
  else chart.value.hideLoading()
})
</script>

<style scoped>
.chart-wrap {
  position: relative;
  width: 100%;
}
.chart-canvas {
  width: 100%;
  height: 100%;
}
.chart-empty {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: var(--bnb-text-secondary);
  font-size: 13px;
  pointer-events: none;
}
</style>
