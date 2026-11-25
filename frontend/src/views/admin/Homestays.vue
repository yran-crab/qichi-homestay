<template>
  <div>
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="query">
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 130px">
            <el-option label="审核中" value="PENDING" />
            <el-option label="已上架" value="APPROVED" />
            <el-option label="已驳回" value="REJECTED" />
            <el-option label="已下架" value="OFFLINE" />
          </el-select>
        </el-form-item>
        <el-form-item label="城市">
          <el-select v-model="query.city" placeholder="全部" clearable filterable style="width: 130px">
            <el-option v-for="c in cities" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="标题/城市" clearable @keyup.enter="loadData(1)" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="loadData(1)">查询</el-button>
          <el-button :icon="RefreshLeft" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column label="ID" prop="id" width="70" />
        <el-table-column label="房源" min-width="240">
          <template #default="{ row }">
            <div class="stay-cell">
              <img :src="row.coverImg" class="stay-thumb" />
              <div>
                <div class="stay-name">{{ row.title }}</div>
                <div class="stay-sub">房东ID:{{ row.hostId }} · {{ row.city }} · {{ row.type }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="价格" width="100">
          <template #default="{ row }"><span class="price">¥{{ row.price }}</span></template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审核备注" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">{{ row.auditRemark || '—' }}</template>
        </el-table-column>
        <el-table-column label="更新时间" width="160">
          <template #default="{ row }">{{ formatTime(row.updatedAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="view(row)">查看</el-button>
            <el-button v-if="row.status === 'APPROVED'" link type="danger" size="small"
              @click="onForceOffline(row)">强制下架</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="loadData()"
          @size-change="loadData(1)" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, RefreshLeft } from '@element-plus/icons-vue'
import { adminHomestayPage, forceOfflineHomestay } from '@/api/admin/homestay'

const router = useRouter()
const cities = ['成都', '杭州', '厦门', '丽江', '大理', '西安', '重庆', '苏州']

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ page: 1, pageSize: 10, status: '', city: '', keyword: '' })

function statusText(s) {
  return { PENDING: '审核中', APPROVED: '已上架', REJECTED: '已驳回', OFFLINE: '已下架' }[s] || s
}
function statusTag(s) {
  return { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger', OFFLINE: 'info' }[s] || ''
}
function formatTime(t) {
  return t ? t.replace('T', ' ').substring(0, 16) : '-'
}

async function loadData(page) {
  if (page) query.page = page
  loading.value = true
  try {
    const data = await adminHomestayPage({
      page: query.page,
      pageSize: query.pageSize,
      status: query.status || undefined,
      city: query.city || undefined,
      keyword: query.keyword || undefined
    })
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.status = ''
  query.city = ''
  query.keyword = ''
  loadData(1)
}

function view(row) {
  if (row.status === 'APPROVED') {
    window.open(`/homestay/${row.id}`, '_blank')
  } else {
    ElMessage.info('该房源未上架，门户暂不可见')
  }
}

async function onForceOffline(row) {
  try {
    await ElMessageBox.confirm(
      `强制下架「${row.title}」后租客将无法浏览和预订，确定继续？`,
      '强制下架确认',
      { type: 'warning', confirmButtonText: '强制下架', cancelButtonText: '取消' }
    )
  } catch {
    return
  }
  await forceOfflineHomestay(row.id)
  ElMessage.success('已强制下架')
  loadData()
}

onMounted(() => loadData(1))
</script>

<style scoped>
.filter-card {
  margin-bottom: 14px;
}
.stay-cell {
  display: flex;
  gap: 10px;
  align-items: center;
}
.stay-thumb {
  width: 72px;
  height: 54px;
  border-radius: 6px;
  object-fit: cover;
}
.stay-name {
  font-weight: 600;
  max-width: 260px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.stay-sub {
  font-size: 12px;
  color: var(--bnb-text-secondary);
  margin-top: 2px;
}
.price {
  color: var(--bnb-accent);
  font-weight: 700;
}
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
