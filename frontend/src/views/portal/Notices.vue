<template>
  <div class="page-container notices-page" v-loading="loading">
    <div class="page-head">
      <h2 class="page-title">平台公告</h2>
    </div>
    <el-empty v-if="!loading && list.length === 0" description="暂无公告" />
    <div class="notice-list">
      <el-card v-for="n in list" :key="n.id" shadow="never" class="notice-card"
        @click="$router.push(`/notices/${n.id}`)">
        <div class="notice-card-head">
          <el-icon class="notice-icon"><Bell /></el-icon>
          <span class="notice-title">{{ n.title }}</span>
          <span class="notice-date">{{ fmt(n.createdAt) }}</span>
        </div>
        <div class="notice-preview">{{ n.content.substring(0, 80) }}...</div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { Bell } from '@element-plus/icons-vue'
import { noticeList } from '@/api/notice'

const loading = ref(false)
const list = ref([])

const fmt = (t) => (t ? t.replace('T', ' ').substring(0, 10) : '')

async function loadData() {
  loading.value = true
  try {
    list.value = await noticeList()
  } finally {
    loading.value = false
  }
}
onMounted(loadData)
</script>

<style scoped>
.page-head {
  margin-bottom: 16px;
}
.page-title {
  margin: 0;
  font-size: 22px;
}
.notice-card {
  border-radius: 10px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: box-shadow 0.2s;
}
.notice-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}
.notice-card-head {
  display: flex;
  align-items: center;
  gap: 8px;
}
.notice-icon {
  color: var(--bnb-accent);
  font-size: 18px;
}
.notice-title {
  flex: 1;
  font-size: 16px;
  font-weight: 600;
}
.notice-date {
  color: var(--bnb-text-secondary);
  font-size: 13px;
}
.notice-preview {
  margin-top: 8px;
  color: var(--bnb-text-secondary);
  font-size: 14px;
  line-height: 1.6;
}
</style>
