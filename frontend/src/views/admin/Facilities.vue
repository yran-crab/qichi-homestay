<template>
  <div>
    <el-card shadow="never">
      <div class="toolbar">
        <span class="toolbar-tip">房源配套设施字典，房东发布房源时勾选使用</span>
        <el-button type="primary" :icon="Plus" @click="openDialog()">新增设施</el-button>
      </div>

      <el-table v-loading="loading" :data="list" stripe style="margin-top: 14px">
        <el-table-column label="ID" prop="id" width="80" />
        <el-table-column label="图标" width="100">
          <template #default="{ row }">
            <span class="facility-icon">{{ row.icon || '✓' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="设施名称" prop="name" />
        <el-table-column label="排序" prop="sort" width="100" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openDialog(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="onRemove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editing.id ? '编辑设施' : '新增设施'" width="420px">
      <el-form :model="editing" label-width="80px">
        <el-form-item label="名称" required>
          <el-input v-model="editing.name" maxlength="32" placeholder="例：投影设备" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="editing.icon" maxlength="16" placeholder="emoji，例：📺" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="editing.sort" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  facilityAdminList, createFacility, updateFacility, deleteFacility
} from '@/api/admin/facility'

const loading = ref(false)
const saving = ref(false)
const list = ref([])
const dialogVisible = ref(false)
const editing = reactive({ id: null, name: '', icon: '', sort: 0 })

async function loadData() {
  loading.value = true
  try {
    list.value = await facilityAdminList()
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  if (row) {
    Object.assign(editing, { id: row.id, name: row.name, icon: row.icon || '', sort: row.sort || 0 })
  } else {
    Object.assign(editing, { id: null, name: '', icon: '', sort: 0 })
  }
  dialogVisible.value = true
}

async function onSave() {
  if (!editing.name.trim()) {
    ElMessage.warning('请输入设施名称')
    return
  }
  saving.value = true
  try {
    const payload = { name: editing.name.trim(), icon: editing.icon, sort: editing.sort }
    if (editing.id) {
      await updateFacility(editing.id, payload)
      ElMessage.success('修改成功')
    } else {
      await createFacility(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    saving.value = false
  }
}

async function onRemove(row) {
  try {
    await ElMessageBox.confirm(`删除设施「${row.name}」后，已发布房源中的该标签也会移除，确定删除？`, '提示', {
      type: 'warning'
    })
  } catch {
    return
  }
  await deleteFacility(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.toolbar-tip {
  font-size: 13.5px;
  color: var(--bnb-text-secondary);
}
.facility-icon {
  font-size: 18px;
}
</style>
