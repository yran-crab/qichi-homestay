<template>
  <div class="page-container orders-page" v-loading="loading">
    <div class="page-head">
      <h2 class="page-title">我的订单</h2>
    </div>

    <el-tabs v-model="activeStatus" class="status-tabs" @tab-change="loadData">
      <el-tab-pane label="全部" name="" />
      <el-tab-pane label="待支付" name="PENDING_PAY" />
      <el-tab-pane label="待入住" name="PAID" />
      <el-tab-pane label="入住中" name="CHECKED_IN" />
      <el-tab-pane label="已完成" name="COMPLETED" />
      <el-tab-pane label="已取消" name="CANCELLED" />
    </el-tabs>

    <el-empty v-if="!loading && list.length === 0" description="暂无相关订单">
      <el-button type="primary" @click="$router.push('/homestays')">去发现民宿</el-button>
    </el-empty>

    <div class="order-list">
      <el-card v-for="o in list" :key="o.id" shadow="never" class="order-card">
        <div class="order-top">
          <span class="order-no">订单号 {{ o.orderNo }}</span>
          <el-tag :type="statusType(o.status)" effect="light" round>{{ statusText(o.status) }}</el-tag>
        </div>
        <div class="order-body" @click="openDetail(o)">
          <img :src="o.coverImg" class="order-cover" />
          <div class="order-meta">
            <div class="order-title">{{ o.homestayTitle }}</div>
            <div class="order-sub">{{ o.checkInDate }} 至 {{ o.checkOutDate }} · {{ o.nights }} 晚 · {{ o.guestCount }} 人</div>
            <div class="order-sub">入住人 {{ o.contactName }} · {{ o.contactPhone }}</div>
            <div class="order-sub" v-if="o.status === 'CANCELLED' && o.cancelReason">取消原因：{{ o.cancelReason }}</div>
          </div>
          <div class="order-amount">
            <div class="amount">¥{{ money(o.payAmount || o.totalAmount) }}</div>
            <div class="coupon-save" v-if="Number(o.discountAmount) > 0">已优惠 ¥{{ money(o.discountAmount) }}</div>
            <div class="countdown" v-if="o.status === 'PENDING_PAY'">
              <el-icon><AlarmClock /></el-icon>
              剩 {{ countdown(o) }}
            </div>
          </div>
        </div>
        <div class="order-actions">
          <el-button size="small" @click="openDetail(o)">订单详情</el-button>
          <template v-if="o.status === 'PENDING_PAY'">
            <el-button size="small" @click="onCancel(o)">取消订单</el-button>
            <el-button size="small" type="primary" @click="$router.push(`/orders/cashier/${o.id}`)">去支付</el-button>
          </template>
          <template v-else-if="o.status === 'PAID'">
            <el-button size="small" @click="onCancel(o)" :disabled="!canCancel(o)">取消订单</el-button>
          </template>
          <el-button
            v-if="o.status === 'COMPLETED' && o.reviewed === 0"
            size="small"
            type="warning"
            plain
            @click="openReview(o)">
            去评价
          </el-button>
          <el-button
            v-if="o.status === 'COMPLETED' && o.reviewed === 1"
            size="small"
            disabled>
            已评价
          </el-button>
        </div>
      </el-card>
    </div>

    <el-pagination
      v-if="total > pageSize"
      class="pager"
      background
      layout="prev, pager, next"
      :total="total"
      :page-size="pageSize"
      :current-page="page"
      @current-change="onPageChange" />

    <!-- 订单详情抽屉 -->
    <el-drawer v-model="drawer" title="订单详情" size="420px">
      <template v-if="current">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="订单状态">
            <el-tag :type="statusType(current.status)" size="small">{{ statusText(current.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="订单号">{{ current.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="民宿">{{ current.homestayTitle }}</el-descriptions-item>
          <el-descriptions-item label="地址">{{ current.city }} · {{ current.address }}</el-descriptions-item>
          <el-descriptions-item label="入住 / 退房">{{ current.checkInDate }} 至 {{ current.checkOutDate }}（{{ current.nights }} 晚）</el-descriptions-item>
          <el-descriptions-item label="入住人数">{{ current.guestCount }} 人</el-descriptions-item>
          <el-descriptions-item label="入住人">{{ current.contactName }} {{ current.contactPhone }}</el-descriptions-item>
          <el-descriptions-item label="单价">¥{{ current.unitPrice }} / 晚</el-descriptions-item>
          <el-descriptions-item label="订单金额">¥{{ money(current.totalAmount) }}</el-descriptions-item>
          <el-descriptions-item label="优惠券" v-if="Number(current.discountAmount) > 0">
            {{ current.couponName }}（-¥{{ money(current.discountAmount) }}）
          </el-descriptions-item>
          <el-descriptions-item label="实付金额"><span class="amount">¥{{ money(current.payAmount || current.totalAmount) }}</span></el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ fmt(current.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="支付时间" v-if="current.payTime">{{ fmt(current.payTime) }}</el-descriptions-item>
          <el-descriptions-item label="取消时间" v-if="current.cancelTime">{{ fmt(current.cancelTime) }}</el-descriptions-item>
          <el-descriptions-item label="取消原因" v-if="current.cancelReason">{{ current.cancelReason }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-drawer>

    <!-- 评价对话框 -->
    <el-dialog v-model="reviewDialog" title="发表评价" width="500px" :close-on-click-modal="false">
      <el-form :model="reviewForm" label-width="80px">
        <el-form-item label="民宿">
          <span>{{ reviewForm.homestayTitle }}</span>
        </el-form-item>
        <el-form-item label="评分" required>
          <el-rate v-model="reviewForm.rating" :max="5" show-text :texts="['很差', '较差', '一般', '好', '很好']" />
        </el-form-item>
        <el-form-item label="评价内容" required>
          <el-input
            v-model="reviewForm.content"
            type="textarea"
            :rows="4"
            maxlength="1000"
            show-word-limit
            placeholder="分享您的入住体验..." />
        </el-form-item>
        <el-form-item label="上传图片">
          <ImageUpload v-model="reviewForm.images" multiple :max="6" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialog = false">取消</el-button>
        <el-button type="primary" :loading="reviewSubmitting" @click="submitReview">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { AlarmClock } from '@element-plus/icons-vue'
import { myOrders, cancelOrder } from '@/api/order'
import { createReview } from '@/api/review'
import ImageUpload from '@/components/ImageUpload.vue'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const activeStatus = ref('')
const drawer = ref(false)
const current = ref(null)

const reviewDialog = ref(false)
const reviewSubmitting = ref(false)
const reviewForm = ref({
  orderId: null,
  homestayTitle: '',
  rating: 5,
  content: '',
  images: []
})

const STATUS = {
  PENDING_PAY: { text: '待支付', type: 'warning' },
  PAID: { text: '待入住', type: 'primary' },
  CHECKED_IN: { text: '入住中', type: 'success' },
  COMPLETED: { text: '已完成', type: 'info' },
  CANCELLED: { text: '已取消', type: 'danger' }
}
const statusText = (s) => STATUS[s]?.text || s
const statusType = (s) => STATUS[s]?.type || 'info'

function fmt(t) {
  return t ? t.replace('T', ' ').substring(0, 19) : '-'
}

function money(v) {
  return Number(v || 0).toFixed(2)
}

function countdown(o) {
  const created = new Date((o.createdAt || '').replace(' ', 'T')).getTime()
  const left = Math.max(0, 1800 - Math.floor((Date.now() - created) / 1000))
  const m = String(Math.floor(left / 60)).padStart(2, '0')
  const s = String(left % 60).padStart(2, '0')
  return `${m}:${s}`
}

function canCancel(o) {
  // 入住日晚于今天才可线上取消
  return new Date(o.checkInDate + 'T00:00:00').getTime() > new Date().setHours(0, 0, 0, 0)
}

async function loadData() {
  loading.value = true
  try {
    const res = await myOrders({ page: page.value, pageSize: pageSize.value, status: activeStatus.value })
    list.value = res.list || []
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function onPageChange(p) {
  page.value = p
  loadData()
}

function openDetail(o) {
  current.value = o
  drawer.value = true
}

async function onCancel(o) {
  let reason = ''
  try {
    const r = await ElMessageBox.prompt('请填写取消原因（选填）', '取消订单', {
      confirmButtonText: '确认取消',
      cancelButtonText: '再想想',
      inputValue: '行程变更',
      inputValidator: () => true
    })
    reason = r.value || ''
  } catch (e) {
    return
  }
  try {
    await cancelOrder(o.id, reason)
    ElMessage.success('订单已取消')
    loadData()
  } catch (e) {
    // 拦截器已提示
  }
}

function openReview(o) {
  reviewForm.value = {
    orderId: o.id,
    homestayTitle: o.homestayTitle,
    rating: 5,
    content: '',
    images: []
  }
  reviewDialog.value = true
}

async function submitReview() {
  if (!reviewForm.value.rating) {
    ElMessage.warning('请选择评分')
    return
  }
  if (!reviewForm.value.content || !reviewForm.value.content.trim()) {
    ElMessage.warning('请填写评价内容')
    return
  }
  reviewSubmitting.value = true
  try {
    await createReview({
      orderId: reviewForm.value.orderId,
      rating: reviewForm.value.rating,
      content: reviewForm.value.content.trim(),
      images: reviewForm.value.images
    })
    ElMessage.success('评价发表成功')
    reviewDialog.value = false
    loadData()
  } catch (e) {
    // 拦截器已提示
  } finally {
    reviewSubmitting.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.page-head {
  margin-bottom: 8px;
}
.page-title {
  margin: 0;
  font-size: 22px;
}
.status-tabs {
  margin-bottom: 8px;
}
.order-card {
  border-radius: 10px;
  margin-bottom: 12px;
}
.order-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 10px;
  border-bottom: 1px solid #f2f2f2;
}
.order-no {
  color: var(--bnb-text-secondary);
  font-size: 13px;
}
.order-body {
  display: flex;
  gap: 14px;
  padding: 14px 0;
  cursor: pointer;
}
.order-cover {
  width: 120px;
  height: 86px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
}
.order-meta {
  flex: 1;
  min-width: 0;
}
.order-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
}
.order-sub {
  color: var(--bnb-text-secondary);
  font-size: 13px;
  margin-top: 4px;
}
.order-amount {
  text-align: right;
  flex-shrink: 0;
}
.amount {
  color: var(--bnb-accent);
  font-size: 18px;
  font-weight: 700;
}
.countdown {
  margin-top: 8px;
  color: var(--bnb-accent);
  font-size: 12.5px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 3px;
}
.coupon-save {
  margin-top: 4px;
  font-size: 12px;
  color: var(--bnb-accent);
}
.order-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
.pager {
  margin-top: 18px;
  justify-content: center;
}
</style>
