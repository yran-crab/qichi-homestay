<template>
  <div class="admin-reviews" v-loading="loading">
    <el-card shadow="never">
      <div class="toolbar">
        <el-input
          v-model="keyword"
          placeholder="房源/评价/租客/订单号"
          clearable
          style="width: 240px"
          @keyup.enter="reload"
          @clear="reload" />
        <el-button @click="reload"><el-icon><Refresh /></el-icon>刷新</el-button>
      </div>

      <el-table :data="list" stripe style="margin-top: 12px">
        <el-table-column label="房源" prop="homestayTitle" min-width="140" />
        <el-table-column label="租客" width="100">
          <template #default="{ row }">
            <div class="cell-tenant">
              <el-avatar :size="24" :src="row.tenantAvatar">{{ (row.tenantName || '匿').charAt(0) }}</el-avatar>
              <span>{{ row.tenantName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="评分" width="80">
          <template #default="{ row }">
            <el-rate :model-value="row.rating" disabled size="small" />
          </template>
        </el-table-column>
        <el-table-column label="评价内容" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">{{ row.content }}</template>
        </el-table-column>
        <el-table-column label="回复" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.reply">{{ row.reply }}</span>
            <span v-else class="cell-sub">-</span>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="100">
          <template #default="{ row }">{{ fmt(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button link type="danger" size="small" @click="onDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pager"
        background
        layout="total, prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page="page"
        @current-change="onPage" />
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { adminReviews, deleteReview } from '@/api/review'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const keyword = ref('')

const fmt = (t) => (t ? t.replace('T', ' ').substring(0, 10) : '-')

async function load() {
  loading.value = true
  try {
    const res = await adminReviews({
      page: page.value,
      pageSize: pageSize.value,
      keyword: keyword.value
    })
    list.value = res.list || []
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function reload() {
  page.value = 1
  load()
}

function onPage(p) {
  page.value = p
  load()
}

async function onDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除该评价吗？删除后不可恢复。', '删除确认', {
      type: 'warning',
      confirmButtonText: '确认删除'
    })
  } catch (e) {
    return
  }
  try {
    await deleteReview(row.id)
    ElMessage.success('删除成功')
    load()
  } catch (e) {
    // 拦截器已提示
  }
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
}
.cell-tenant {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}
.cell-sub {
  color: var(--bnb-text-secondary);
}
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
