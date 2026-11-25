<template>
  <div>
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="query">
        <el-form-item label="角色">
          <el-select v-model="query.role" placeholder="全部" clearable style="width: 130px">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="房东" value="HOST" />
            <el-option label="租客" value="TENANT" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="用户名/昵称/手机号" clearable
            @keyup.enter="loadData(1)" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="loadData(1)">查询</el-button>
          <el-button :icon="RefreshLeft" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column label="ID" prop="id" width="70" />
        <el-table-column label="用户" min-width="170">
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
        <el-table-column label="手机号" prop="phone" width="130" />
        <el-table-column label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="roleTag(row.role)">{{ roleText(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="房东认证" width="110">
          <template #default="{ row }">{{ hostStatusText(row.hostStatus) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              :disabled="row.role === 'ADMIN'"
              @change="(val) => toggleStatus(row, val)" />
          </template>
        </el-table-column>
        <el-table-column label="注册时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, RefreshLeft } from '@element-plus/icons-vue'
import { userPage, updateUserStatus } from '@/api/admin/user'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ page: 1, pageSize: 10, role: '', status: null, keyword: '' })

function roleText(role) {
  return { ADMIN: '管理员', HOST: '房东', TENANT: '租客' }[role] || role
}
function roleTag(role) {
  return { ADMIN: 'danger', HOST: 'warning', TENANT: 'success' }[role] || ''
}
function hostStatusText(s) {
  return { NONE: '—', PENDING: '审核中', APPROVED: '已认证', REJECTED: '已驳回' }[s] || '—'
}
function formatTime(t) {
  return t ? t.replace('T', ' ').substring(0, 16) : '-'
}

async function loadData(page) {
  if (page) query.page = page
  loading.value = true
  try {
    const data = await userPage({
      page: query.page,
      pageSize: query.pageSize,
      role: query.role || undefined,
      status: query.status === '' || query.status === null ? undefined : query.status,
      keyword: query.keyword || undefined
    })
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.role = ''
  query.status = null
  query.keyword = ''
  loadData(1)
}

async function toggleStatus(row, enabled) {
  const action = enabled ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确定${action}用户「${row.realName || row.username}」吗？`, '提示', {
      type: 'warning'
    })
  } catch {
    return
  }
  await updateUserStatus(row.id, enabled ? 1 : 0)
  row.status = enabled ? 1 : 0
  ElMessage.success(`已${action}`)
}

onMounted(() => loadData(1))
</script>

<style scoped>
.filter-card {
  margin-bottom: 14px;
}
.table-card :deep(.el-card__body) {
  padding-top: 12px;
}
.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}
.user-name {
  font-weight: 600;
  line-height: 1.4;
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
