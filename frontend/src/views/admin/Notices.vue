<template>
  <div class="admin-notices" v-loading="loading">
    <el-card shadow="never">
      <div class="toolbar">
        <span class="page-title">公告管理</span>
        <el-button type="primary" @click="openCreate"><el-icon><Plus /></el-icon>新增公告</el-button>
      </div>
      <el-table :data="list" stripe style="margin-top: 12px">
        <el-table-column label="ID" prop="id" width="60" />
        <el-table-column label="标题" prop="title" min-width="200" />
        <el-table-column label="内容" min-width="240" show-overflow-tooltip>
          <template #default="{ row }">{{ row.content.substring(0, 60) }}...</template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="120">
          <template #default="{ row }">{{ fmt(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="onDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialog" :title="editForm.id ? '编辑公告' : '新增公告'" width="640px">
      <el-form :model="editForm" label-width="70px">
        <el-form-item label="标题" required>
          <el-input v-model="editForm.title" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="内容" required>
          <el-input v-model="editForm.content" type="textarea" :rows="8" placeholder="公告正文..." />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="editForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { adminNoticeList, createNotice, updateNotice, deleteNotice } from '@/api/notice'

const loading = ref(false)
const list = ref([])
const dialog = ref(false)
const submitting = ref(false)
const editForm = ref({ id: null, title: '', content: '', status: 1 })

const fmt = (t) => (t ? t.replace('T', ' ').substring(0, 10) : '-')

async function load() {
  loading.value = true
  try {
    list.value = await adminNoticeList()
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editForm.value = { id: null, title: '', content: '', status: 1 }
  dialog.value = true
}

function openEdit(row) {
  editForm.value = { ...row }
  dialog.value = true
}

async function submit() {
  if (!editForm.value.title || !editForm.value.content) {
    ElMessage.warning('请填写标题和内容')
    return
  }
  submitting.value = true
  try {
    if (editForm.value.id) {
      await updateNotice(editForm.value.id, editForm.value)
    } else {
      await createNotice(editForm.value)
    }
    ElMessage.success(editForm.value.id ? '更新成功' : '创建成功')
    dialog.value = false
    load()
  } catch (e) {} finally {
    submitting.value = false
  }
}

async function onDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除该公告吗？', '删除确认', { type: 'warning' })
  } catch (e) { return }
  try {
    await deleteNotice(row.id)
    ElMessage.success('删除成功')
    load()
  } catch (e) {}
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.page-title {
  font-size: 18px;
  font-weight: 600;
}
</style>
