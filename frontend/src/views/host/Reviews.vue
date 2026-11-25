<template>
  <div class="host-reviews" v-loading="loading">
    <el-card shadow="never">
      <div class="toolbar">
        <el-input
          v-model="keyword"
          placeholder="房源/评价/租客"
          clearable
          style="width: 220px"
          @keyup.enter="reload"
          @clear="reload" />
        <el-checkbox v-model="onlyUnreplied" @change="reload">仅待回复</el-checkbox>
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
        <el-table-column label="回复状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.reply" type="success" size="small">已回复</el-tag>
            <el-tag v-else type="warning" size="small">待回复</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="100">
          <template #default="{ row }">{{ fmt(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openReply(row)">
              {{ row.reply ? '查看' : '回复' }}
            </el-button>
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

    <!-- 回复对话框 -->
    <el-dialog v-model="replyDialog" :title="replyForm.hasReply ? '查看回复' : '回复评价'" width="500px">
      <div class="reply-review-preview">
        <el-rate :model-value="replyForm.rating" disabled size="small" />
        <p>{{ replyForm.content }}</p>
      </div>
      <el-input
        v-model="replyForm.reply"
        type="textarea"
        :rows="4"
        maxlength="1000"
        show-word-limit
        :disabled="replyForm.hasReply"
        placeholder="感谢您的评价..." />
      <template #footer>
        <el-button @click="replyDialog = false">关闭</el-button>
        <el-button v-if="!replyForm.hasReply" type="primary" :loading="replying" @click="submitReply">提交回复</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { hostReviews, replyReview } from '@/api/review'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const onlyUnreplied = ref(false)

const replyDialog = ref(false)
const replying = ref(false)
const replyForm = ref({
  id: null,
  rating: 0,
  content: '',
  reply: '',
  hasReply: false
})

const fmt = (t) => (t ? t.replace('T', ' ').substring(0, 10) : '-')

async function load() {
  loading.value = true
  try {
    const res = await hostReviews({
      page: page.value,
      pageSize: pageSize.value,
      keyword: keyword.value,
      onlyUnreplied: onlyUnreplied.value
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

function openReply(row) {
  replyForm.value = {
    id: row.id,
    rating: row.rating,
    content: row.content,
    reply: row.reply || '',
    hasReply: !!row.reply
  }
  replyDialog.value = true
}

async function submitReply() {
  if (!replyForm.value.reply || !replyForm.value.reply.trim()) {
    ElMessage.warning('请填写回复内容')
    return
  }
  replying.value = true
  try {
    await replyReview(replyForm.value.id, replyForm.value.reply.trim())
    ElMessage.success('回复成功')
    replyDialog.value = false
    load()
  } catch (e) {
    // 拦截器已提示
  } finally {
    replying.value = false
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
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
.reply-review-preview {
  margin-bottom: 16px;
  padding: 12px;
  background: #f9f9f4;
  border-radius: 8px;
}
.reply-review-preview p {
  margin: 8px 0 0;
  font-size: 14px;
  line-height: 1.6;
}
</style>
