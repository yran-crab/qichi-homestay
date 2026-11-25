<template>
  <div class="page-container favorites-page">
    <div class="page-head-bar">
      <h2 class="page-title">我的收藏</h2>
      <span class="page-sub">共收藏 {{ total }} 套民宿</span>
    </div>

    <div v-loading="loading">
      <el-row :gutter="20">
        <el-col v-for="item in list" :key="item.id" :xs="12" :sm="12" :md="8" :lg="6">
          <StayCard :item="item" @changed="onChanged" />
        </el-col>
      </el-row>

      <el-empty v-if="!loading && list.length === 0" description="还没有收藏任何民宿，去找找心仪的住处吧">
        <el-button type="primary" round @click="$router.push('/homestays')">去逛逛</el-button>
      </el-empty>

      <div v-if="total > query.pageSize" class="pager">
        <el-pagination
          v-model:current-page="query.page"
          :page-size="query.pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="loadData" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { myFavorites } from '@/api/favorite'
import StayCard from '@/components/StayCard.vue'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ page: 1, pageSize: 12 })

async function loadData() {
  loading.value = true
  try {
    const data = await myFavorites({ page: query.page, pageSize: query.pageSize })
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function onChanged({ favored }) {
  // 取消收藏后从当前页移除
  if (!favored) {
    loadData()
  }
}

onMounted(loadData)
</script>

<style scoped>
.page-head-bar {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin: 4px 2px 16px;
}
.page-title {
  margin: 0;
  font-size: 22px;
}
.page-sub {
  font-size: 13px;
  color: var(--bnb-text-secondary);
}
.pager {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
