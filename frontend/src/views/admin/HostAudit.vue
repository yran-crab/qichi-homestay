<template>
  <div>
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="query">
        <el-form-item label="申请人">
          <el-input v-model="query.realName" placeholder="昵称关键词" clearable @keyup.enter="loadData(1)" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="loadData(1)">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column label="ID" prop="id" width="70" />
        <el-table-column label="申请人" min-width="150">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="36" :src="row.avatar">{{ (row.realName || row.username).charAt(0) }}</el-avatar>
              <div>
                <div class="user-name">{{ row.realName || '-' }}</div>
                <div class="user-sub">@{{ row.username }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="手机号" prop="phone" width="140" />
        <el-table-column label="提交时间" width="180">
          <template #default="{ row }">{{ formatTime(row.updatedAt || row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="success" size="small" @click="doAudit(row, true)">通过</el-button>
            <el-button type="danger" size="small" plain @click="doAudit(row, false)">驳回</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无待审核的房东申请" />
        </template>
      </el-table>

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
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { hostApplyPage, auditHost } from '@/api/admin/user'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ page: 1, pageSize: 10, realName: '' })

function formatTime(t) {
  return t ? t.replace('T', ' ').substring(0, 16) : '-'
}

async function loadData(page) {
  if (page) query.page = page
  loading.value = true
  try {
    const data = await hostApplyPage({
      page: query.page,
      pageSize: query.pageSize,
      realName: query.realName || undefined
    })
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

async function doAudit(row, approved) {
  try {
    await ElMessageBox.confirm(
      `确定${approved ? '通过' : '驳回'}「${row.realName || row.username}」的房东入驻申请？`,
      '审核确认',
      { type: approved ? 'success' : 'warning', confirmButtonText: '确定', cancelButtonText: '取消' }
    )
  } catch {
    return
  }
  await auditHost(row.id, approved)
  ElMessage.success(approved ? '已通过申请' : '已驳回申请')
  loadData()
}

onMounted(() => loadData(1))
</script>

<style scoped>
.filter-card {
  margin-bottom: 14px;
}
.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}
.user-name {
  font-weight: 600;
}
.user-sub {
  font-size: 12px;
  color: var(--bnb-text-secondary);
}
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
