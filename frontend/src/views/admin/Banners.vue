<template>
  <div class="admin-banners" v-loading="loading">
    <el-card shadow="never">
      <div class="toolbar">
        <span class="page-title">轮播图管理</span>
        <el-button type="primary" @click="openCreate"><el-icon><Plus /></el-icon>新增轮播</el-button>
      </div>
      <el-table :data="list" stripe style="margin-top: 12px">
        <el-table-column label="ID" prop="id" width="60" />
        <el-table-column label="图片" width="120">
          <template #default="{ row }">
            <img :src="row.imageUrl" class="banner-thumb" />
          </template>
        </el-table-column>
        <el-table-column label="标题" prop="title" min-width="160" />
        <el-table-column label="跳转链接" prop="link" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ row.link || '-' }}</template>
        </el-table-column>
        <el-table-column label="排序" prop="sort" width="80" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="onDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 弹窗 -->
    <el-dialog v-model="dialog" :title="editForm.id ? '编辑轮播' : '新增轮播'" width="520px">
      <el-form :model="editForm" label-width="90px">
        <el-form-item label="标题" required>
          <el-input v-model="editForm.title" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="图片 URL" required>
          <el-input v-model="editForm.imageUrl" placeholder="https://..." />
          <img v-if="editForm.imageUrl" :src="editForm.imageUrl" class="preview-img" />
        </el-form-item>
        <el-form-item label="跳转链接">
          <el-input v-model="editForm.link" placeholder="/homestays?city=成都 或 /homestay/5" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="editForm.sort" :min="0" :max="999" />
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
import { bannerList, createBanner, updateBanner, deleteBanner } from '@/api/banner'

const loading = ref(false)
const list = ref([])
const dialog = ref(false)
const submitting = ref(false)
const editForm = ref({ id: null, title: '', imageUrl: '', link: '', sort: 0, status: 1 })

async function load() {
  loading.value = true
  try {
    list.value = await bannerList()
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editForm.value = { id: null, title: '', imageUrl: '', link: '', sort: 0, status: 1 }
  dialog.value = true
}

function openEdit(row) {
  editForm.value = { ...row }
  dialog.value = true
}

async function submit() {
  if (!editForm.value.title || !editForm.value.imageUrl) {
    ElMessage.warning('请填写标题和图片 URL')
    return
  }
  submitting.value = true
  try {
    if (editForm.value.id) {
      await updateBanner(editForm.value.id, editForm.value)
    } else {
      await createBanner(editForm.value)
    }
    ElMessage.success(editForm.value.id ? '更新成功' : '创建成功')
    dialog.value = false
    load()
  } catch (e) {
    // 拦截器已提示
  } finally {
    submitting.value = false
  }
}

async function onDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除该轮播图吗？', '删除确认', { type: 'warning' })
  } catch (e) { return }
  try {
    await deleteBanner(row.id)
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
.banner-thumb {
  width: 100px;
  height: 56px;
  border-radius: 4px;
  object-fit: cover;
}
.preview-img {
  margin-top: 8px;
  width: 100%;
  max-height: 120px;
  border-radius: 6px;
  object-fit: cover;
}
</style>
