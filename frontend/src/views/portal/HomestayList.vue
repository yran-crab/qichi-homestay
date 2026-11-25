<template>
  <div class="page-container listing-page">
    <!-- 筛选区 -->
    <el-card shadow="never" class="filter-card">
      <div class="filter-line">
        <el-input
          v-model="query.keyword"
          placeholder="搜索城市、地标或民宿名称"
          clearable
          class="keyword-input"
          :prefix-icon="Search"
          @keyup.enter="loadData(1)"
          @clear="loadData(1)" />
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="入住日期"
          end-placeholder="退房日期"
          value-format="YYYY-MM-DD"
          :disabled-date="disablePast"
          class="date-picker"
          @change="onDateChange" />
        <el-button type="primary" :icon="Search" @click="loadData(1)">搜索</el-button>
      </div>

      <div class="filter-line chips-line">
        <span class="filter-label">城市</span>
        <div class="chips">
          <span class="chip" :class="{ active: !query.city }" @click="setCity('')">不限</span>
          <span v-for="c in cityOptions" :key="c" class="chip"
            :class="{ active: query.city === c }" @click="setCity(c)">{{ c }}</span>
        </div>
      </div>

      <div class="filter-line chips-line">
        <span class="filter-label">类型</span>
        <div class="chips">
          <span class="chip" :class="{ active: !query.type }" @click="query.type = ''; loadData(1)">不限</span>
          <span v-for="t in typeOptions" :key="t" class="chip"
            :class="{ active: query.type === t }" @click="query.type = t; loadData(1)">{{ t }}</span>
        </div>
        <span class="filter-label price-label">价格</span>
        <el-select v-model="priceRange" class="price-select" @change="onPriceChange">
          <el-option label="不限" value="" />
          <el-option label="¥200 以下" value="0-200" />
          <el-option label="¥200 - ¥400" value="200-400" />
          <el-option label="¥400 - ¥700" value="400-700" />
          <el-option label="¥700 以上" value="700-" />
        </el-select>
        <span class="filter-label">可住</span>
        <el-input-number v-model="query.minGuests" :min="1" :max="20" :controls="false"
          class="guests-input" placeholder="人数" @change="loadData(1)" />
        <el-button text :icon="RefreshLeft" @click="resetAll">重置</el-button>
      </div>
    </el-card>

    <!-- 排序与统计 -->
    <div class="result-bar">
      <span class="result-total">共找到 <b>{{ total }}</b> 套民宿</span>
      <div class="sorts">
        <span v-for="s in sortOptions" :key="s.value" class="sort-item"
          :class="{ active: query.sort === s.value }" @click="changeSort(s.value)">{{ s.label }}</span>
      </div>
    </div>

    <!-- 房源卡片 -->
    <div v-loading="loading">
      <el-row :gutter="20">
        <el-col v-for="item in list" :key="item.id" :xs="12" :sm="12" :md="8" :lg="6">
          <StayCard :item="item" />
        </el-col>
      </el-row>

      <el-empty v-if="!loading && list.length === 0" description="没有找到符合条件的民宿，换个筛选试试吧" />

      <div v-if="total > query.pageSize" class="pager">
        <el-pagination
          v-model:current-page="query.page"
          :page-size="query.pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="loadData()" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { Search, RefreshLeft } from '@element-plus/icons-vue'
import { homestayPage } from '@/api/homestay'
import StayCard from '@/components/StayCard.vue'

const route = useRoute()

const cityOptions = ['成都', '杭州', '厦门', '丽江', '大理', '西安', '重庆', '苏州']
const typeOptions = ['整租', '单间', '公寓', '别墅', '小院']
const sortOptions = [
  { value: 'newest', label: '最新发布' },
  { value: 'price_asc', label: '价格从低到高' },
  { value: 'price_desc', label: '价格从高到低' }
]

const loading = ref(false)
const list = ref([])
const total = ref(0)
const dateRange = ref(null)
const priceRange = ref('')

const query = reactive({
  keyword: '', city: '', type: '', minPrice: null, maxPrice: null,
  minGuests: null, checkIn: null, checkOut: null,
  sort: 'newest', page: 1, pageSize: 12
})

function disablePast(date) {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return date.getTime() < today.getTime()
}

function onDateChange(val) {
  if (val && val.length === 2) {
    query.checkIn = val[0]
    query.checkOut = val[1]
  } else {
    query.checkIn = null
    query.checkOut = null
  }
  loadData(1)
}

function onPriceChange(val) {
  if (!val) {
    query.minPrice = null
    query.maxPrice = null
  } else {
    const [min, max] = val.split('-')
    query.minPrice = min ? Number(min) : null
    query.maxPrice = max ? Number(max) : null
  }
  loadData(1)
}

function setCity(city) {
  query.city = city
  loadData(1)
}

function changeSort(sort) {
  query.sort = sort
  loadData(1)
}

function resetAll() {
  Object.assign(query, {
    keyword: '', city: '', type: '', minPrice: null, maxPrice: null,
    minGuests: null, checkIn: null, checkOut: null, sort: 'newest', page: 1
  })
  dateRange.value = null
  priceRange.value = ''
  loadData(1)
}

async function loadData(page) {
  if (page) query.page = page
  loading.value = true
  try {
    const data = await homestayPage({
      keyword: query.keyword || undefined,
      city: query.city || undefined,
      type: query.type || undefined,
      minPrice: query.minPrice ?? undefined,
      maxPrice: query.maxPrice ?? undefined,
      minGuests: query.minGuests ?? undefined,
      checkIn: query.checkIn ?? undefined,
      checkOut: query.checkOut ?? undefined,
      sort: query.sort,
      page: query.page,
      pageSize: query.pageSize
    })
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  // 接收首页搜索条件
  if (route.query.keyword) query.keyword = String(route.query.keyword)
  if (route.query.city) query.city = String(route.query.city)
  if (route.query.checkIn) {
    query.checkIn = String(route.query.checkIn)
    query.checkOut = route.query.checkOut ? String(route.query.checkOut) : null
    dateRange.value = [query.checkIn, query.checkOut]
  }
  loadData(1)
})
</script>

<style scoped>
.filter-card {
  margin-bottom: 16px;
}
.filter-card :deep(.el-card__body) {
  padding: 16px 20px 8px;
}
.filter-line {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}
.keyword-input {
  width: 280px;
}
.date-picker {
  width: 280px;
}
.chips-line {
  gap: 8px;
}
.filter-label {
  font-size: 13px;
  color: var(--bnb-text-secondary);
  flex-shrink: 0;
}
.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.chip {
  padding: 4px 14px;
  border-radius: 14px;
  font-size: 13px;
  background: #f4f6f8;
  color: var(--bnb-text-regular);
  cursor: pointer;
  transition: all 0.2s;
}
.chip:hover {
  color: var(--bnb-primary);
}
.chip.active {
  background: var(--bnb-primary);
  color: #fff;
}
.price-select {
  width: 150px;
}
.guests-input {
  width: 90px;
}
.price-label {
  margin-left: 8px;
}
.result-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 4px 2px 14px;
}
.result-total {
  font-size: 14px;
  color: var(--bnb-text-secondary);
}
.result-total b {
  color: var(--bnb-primary);
  font-size: 16px;
}
.sorts {
  display: flex;
  gap: 18px;
}
.sort-item {
  font-size: 14px;
  color: var(--bnb-text-regular);
  cursor: pointer;
}
.sort-item.active {
  color: var(--bnb-primary);
  font-weight: 600;
}
.pager {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
