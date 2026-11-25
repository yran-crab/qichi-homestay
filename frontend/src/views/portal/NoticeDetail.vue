<template>
  <div class="page-container notice-detail" v-loading="loading">
    <template v-if="notice">
      <el-page-header @back="$router.back()">
        <template #content>
          <span class="page-header-title">公告详情</span>
        </template>
      </el-page-header>
      <el-card shadow="never" class="detail-card">
        <h1 class="notice-title">{{ notice.title }}</h1>
        <div class="notice-meta">
          <el-icon><Clock /></el-icon>
          <span>{{ fmt(notice.createdAt) }}</span>
        </div>
        <el-divider />
        <div class="notice-content">{{ notice.content }}</div>
      </el-card>
    </template>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { Clock } from '@element-plus/icons-vue'
import { noticeDetail } from '@/api/notice'

const route = useRoute()
const loading = ref(false)
const notice = ref(null)

const fmt = (t) => (t ? t.replace('T', ' ').substring(0, 19) : '-')

async function loadData() {
  loading.value = true
  try {
    notice.value = await noticeDetail(route.params.id)
  } finally {
    loading.value = false
  }
}
onMounted(loadData)
</script>

<style scoped>
.page-header-title {
  font-size: 16px;
}
.detail-card {
  margin-top: 16px;
  border-radius: 10px;
}
.notice-title {
  margin: 0 0 12px;
  font-size: 24px;
  font-weight: 700;
}
.notice-meta {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--bnb-text-secondary);
  font-size: 13px;
}
.notice-content {
  font-size: 15px;
  line-height: 1.9;
  color: var(--bnb-text-regular);
  white-space: pre-wrap;
}
</style>
