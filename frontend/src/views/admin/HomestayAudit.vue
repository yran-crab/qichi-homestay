<template>
  <div>
    <el-card shadow="never">
      <div class="page-tip">
        <el-icon><BellFilled /></el-icon>
        共 <b>{{ total }}</b> 套房源等待审核，审核通过后将在门户展示并接受预订
      </div>

      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column label="房源" min-width="260">
          <template #default="{ row }">
            <div class="stay-cell">
              <img :src="row.coverImg" class="stay-thumb" />
              <div>
                <div class="stay-name">{{ row.title }}</div>
                <div class="stay-sub">{{ row.city }} · {{ row.address }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="类型" prop="type" width="90" />
        <el-table-column label="价格" width="100">
          <template #default="{ row }"><span class="price">¥{{ row.price }}</span></template>
        </el-table-column>
        <el-table-column label="规格" width="150">
          <template #default="{ row }">{{ row.bedrooms }}室{{ row.livingRooms }}厅 · {{ row.maxGuests }}人</template>
        </el-table-column>
        <el-table-column label="提交时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="success" size="small" @click="openAudit(row, true)">通过</el-button>
            <el-button type="danger" size="small" plain @click="openAudit(row, false)">驳回</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无待审核房源" />
        </template>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="query.page"
          :page-size="query.pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadData()" />
      </div>
    </el-card>

    <!-- 审核弹窗 -->
    <el-dialog v-model="dialogVisible" :title="auditApproved ? '通过审核' : '驳回房源'" width="560px">
      <div v-if="current" class="audit-preview">
        <img :src="current.coverImg" class="preview-img" />
        <div class="preview-info">
          <div class="preview-title">{{ current.title }}</div>
          <div class="preview-sub">{{ current.city }} · {{ current.type }} · ¥{{ current.price }}/晚</div>
          <div class="preview-sub">{{ current.bedrooms }}室{{ current.livingRooms }}厅{{ current.bathrooms }}卫 · 可住{{ current.maxGuests }}人</div>
        </div>
      </div>
      <el-form v-if="!auditApproved" class="reject-form">
        <el-input v-model="remark" type="textarea" :rows="3" maxlength="255" show-word-limit
          placeholder="请填写驳回原因（必填，将展示给房东）" />
      </el-form>
      <div v-else class="approve-tip">审核通过后该房源将立即在门户上架展示</div>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button :type="auditApproved ? 'success' : 'danger'" :loading="submitting" @click="doAudit">
          确认{{ auditApproved ? '通过' : '驳回' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { BellFilled } from '@element-plus/icons-vue'
import { auditHomestayPage, auditHomestay } from '@/api/admin/homestay'

const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ page: 1, pageSize: 10 })

const dialogVisible = ref(false)
const current = ref(null)
const auditApproved = ref(true)
const remark = ref('')

function formatTime(t) {
  return t ? t.replace('T', ' ').substring(0, 16) : '-'
}

async function loadData(page) {
  if (page) query.page = page
  loading.value = true
  try {
    const data = await auditHomestayPage({ page: query.page, pageSize: query.pageSize })
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function openAudit(row, approved) {
  current.value = row
  auditApproved.value = approved
  remark.value = ''
  dialogVisible.value = true
}

async function doAudit() {
  if (!auditApproved.value && !remark.value.trim()) {
    ElMessage.warning('请填写驳回原因')
    return
  }
  submitting.value = true
  try {
    await auditHomestay(current.value.id, {
      approved: auditApproved.value,
      remark: auditApproved.value ? null : remark.value.trim()
    })
    ElMessage.success(auditApproved.value ? '已通过审核' : '已驳回')
    dialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

onMounted(() => loadData(1))
</script>

<style scoped>
.page-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 14px;
  font-size: 13.5px;
  color: var(--bnb-text-secondary);
}
.page-tip b {
  color: var(--bnb-accent);
}
.stay-cell {
  display: flex;
  gap: 10px;
  align-items: center;
}
.stay-thumb {
  width: 80px;
  height: 60px;
  border-radius: 6px;
  object-fit: cover;
}
.stay-name {
  font-weight: 600;
  max-width: 300px;
}
.stay-sub {
  font-size: 12px;
  color: var(--bnb-text-secondary);
  margin-top: 3px;
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
.audit-preview {
  display: flex;
  gap: 14px;
  margin-bottom: 16px;
}
.preview-img {
  width: 140px;
  height: 100px;
  border-radius: 8px;
  object-fit: cover;
}
.preview-title {
  font-weight: 600;
  font-size: 15px;
  margin-bottom: 8px;
}
.preview-sub {
  font-size: 13px;
  color: var(--bnb-text-secondary);
  line-height: 1.8;
}
.reject-form {
  margin-top: 4px;
}
.approve-tip {
  color: var(--bnb-text-secondary);
  font-size: 13.5px;
}
</style>
