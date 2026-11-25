<template>
  <div>
    <el-card shadow="never" class="filter-card">
      <div class="toolbar">
        <el-radio-group v-model="query.status" @change="loadData(1)">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button label="PENDING">审核中</el-radio-button>
          <el-radio-button label="APPROVED">已上架</el-radio-button>
          <el-radio-button label="REJECTED">已驳回</el-radio-button>
          <el-radio-button label="OFFLINE">已下架</el-radio-button>
        </el-radio-group>
        <div class="toolbar-right">
          <el-input v-model="query.keyword" placeholder="标题/城市" clearable class="search-input"
            @keyup.enter="loadData(1)" @clear="loadData(1)" />
          <el-button type="primary" :icon="Plus" @click="$router.push('/host/homestay/new')">发布房源</el-button>
        </div>
      </div>
    </el-card>

    <el-card shadow="never">
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column label="房源" min-width="240">
          <template #default="{ row }">
            <div class="stay-cell">
              <img :src="row.coverImg" class="stay-thumb" />
              <div>
                <div class="stay-name">{{ row.title }}</div>
                <div class="stay-sub">{{ row.city }} · {{ row.type }} · {{ row.maxGuests }}人</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="每晚价格" width="110">
          <template #default="{ row }">
            <span class="price">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="数据" width="150">
          <template #default="{ row }">
            <div class="data-line">浏览 {{ row.viewCount }} · 收藏 {{ row.favoriteCount }}</div>
            <div class="data-line">订单 {{ row.orderCount }} · 评分 {{ Number(row.rating).toFixed(1) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="更新时间" width="160">
          <template #default="{ row }">{{ formatTime(row.updatedAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small"
              @click="$router.push(`/host/homestay/edit/${row.id}`)">编辑</el-button>
            <el-button v-if="row.status === 'REJECTED' || row.status === 'OFFLINE'"
              link type="warning" size="small" @click="onSubmit(row)">提交审核</el-button>
            <el-button v-if="row.status === 'APPROVED'"
              link type="info" size="small" @click="onOffline(row)">下架</el-button>
            <el-button v-if="row.status === 'OFFLINE'"
              link type="success" size="small" @click="onOnline(row)">上架</el-button>
            <el-button v-if="row.status !== 'APPROVED'"
              link type="danger" size="small" @click="onRemove(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #append v-if="false" />
      </el-table>

      <!-- 驳回原因 -->
      <el-alert
        v-for="item in rejectTips"
        :key="item.id"
        :title="`《${item.title}》被驳回：${item.auditRemark}`"
        type="error"
        :closable="false"
        show-icon
        class="reject-alert" />

      <div class="pager">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadData()" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  hostHomestayPage, submitHomestay, offlineHomestay, onlineHomestay, deleteHomestay
} from '@/api/host/homestay'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ page: 1, pageSize: 10, status: '', keyword: '' })

const rejectTips = computed(() => list.value.filter((i) => i.status === 'REJECTED' && i.auditRemark))

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
    const data = await hostHomestayPage({
      page: query.page,
      pageSize: query.pageSize,
      status: query.status || undefined,
      keyword: query.keyword || undefined
    })
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

async function confirmAction(row, action, fn, successMsg) {
  try {
    await ElMessageBox.confirm(`确定${action}房源「${row.title}」吗？`, '提示', { type: 'warning' })
  } catch {
    return
  }
  await fn(row.id)
  ElMessage.success(successMsg)
  loadData()
}

const onSubmit = (row) => confirmAction(row, '提交审核', submitHomestay, '已提交，等待平台审核')
const onOffline = (row) => confirmAction(row, '下架', offlineHomestay, '房源已下架')
const onOnline = (row) => confirmAction(row, '重新上架', onlineHomestay, '房源已上架')
const onRemove = (row) => confirmAction(row, '删除', deleteHomestay, '删除成功')

onMounted(() => loadData(1))
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}
.toolbar-right {
  display: flex;
  gap: 10px;
}
.search-input {
  width: 200px;
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
.data-line {
  font-size: 12.5px;
  color: var(--bnb-text-secondary);
  line-height: 1.7;
}
.reject-alert {
  margin-top: 12px;
}
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
