<template>
  <div class="host-orders" v-loading="loading">
    <el-card shadow="never">
      <div class="toolbar">
        <el-select v-model="status" placeholder="全部状态" clearable style="width: 150px" @change="reload">
          <el-option label="待支付" value="PENDING_PAY" />
          <el-option label="待入住" value="PAID" />
          <el-option label="入住中" value="CHECKED_IN" />
          <el-option label="已完成" value="COMPLETED" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
        <el-button @click="reload"><el-icon><Refresh /></el-icon>刷新</el-button>
      </div>

      <el-table :data="list" stripe style="margin-top: 12px">
        <el-table-column label="订单号" prop="orderNo" width="180" />
        <el-table-column label="房源" min-width="160">
          <template #default="{ row }">
            <div class="cell-stay">
              <img :src="row.coverImg" class="mini-cover" />
              <span>{{ row.homestayTitle }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="入住人" width="150">
          <template #default="{ row }">{{ row.contactName }} {{ row.contactPhone }}</template>
        </el-table-column>
        <el-table-column label="入住日期" width="200">
          <template #default="{ row }">
            <div>{{ row.checkInDate }} 至 {{ row.checkOutDate }}</div>
            <div class="cell-sub">{{ row.nights }} 晚 · {{ row.guestCount }} 人</div>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="100">
          <template #default="{ row }"><span class="cell-amount">¥{{ row.totalAmount }}</span></template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openDetail(row)">详情</el-button>
            <el-button
              v-if="row.status === 'PAID'"
              link type="success" size="small"
              @click="doAction(row, 'check-in')">确认入住</el-button>
            <el-button
              v-if="row.status === 'CHECKED_IN'"
              link type="warning" size="small"
              @click="doAction(row, 'check-out')">办理退房</el-button>
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

    <el-drawer v-model="drawer" title="订单详情" size="420px">
      <el-descriptions v-if="current" :column="1" border>
        <el-descriptions-item label="订单状态">
          <el-tag :type="statusType(current.status)" size="small">{{ statusText(current.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="订单号">{{ current.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="房源">{{ current.homestayTitle }}</el-descriptions-item>
        <el-descriptions-item label="地址">{{ current.city }} · {{ current.address }}</el-descriptions-item>
        <el-descriptions-item label="租客">{{ current.tenantName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="入住人">{{ current.contactName }} {{ current.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="入住 / 退房">{{ current.checkInDate }} 至 {{ current.checkOutDate }}（{{ current.nights }} 晚）</el-descriptions-item>
        <el-descriptions-item label="人数">{{ current.guestCount }} 人</el-descriptions-item>
        <el-descriptions-item label="金额"><span class="cell-amount">¥{{ current.totalAmount }}</span></el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ fmt(current.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="支付时间" v-if="current.payTime">{{ fmt(current.payTime) }}</el-descriptions-item>
        <el-descriptions-item label="取消原因" v-if="current.cancelReason">{{ current.cancelReason }}</el-descriptions-item>
      </el-descriptions>
      <div class="drawer-actions" v-if="current">
        <el-button v-if="current.status === 'PAID'" type="success" @click="doAction(current, 'check-in')">确认入住</el-button>
        <el-button v-if="current.status === 'CHECKED_IN'" type="warning" @click="doAction(current, 'check-out')">办理退房</el-button>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { hostOrders, hostOrderDetail, confirmCheckIn, confirmCheckOut } from '@/api/host/order'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const status = ref('')
const drawer = ref(false)
const current = ref(null)

const STATUS = {
  PENDING_PAY: { text: '待支付', type: 'warning' },
  PAID: { text: '待入住', type: 'primary' },
  CHECKED_IN: { text: '入住中', type: 'success' },
  COMPLETED: { text: '已完成', type: 'info' },
  CANCELLED: { text: '已取消', type: 'danger' }
}
const statusText = (s) => STATUS[s]?.text || s
const statusType = (s) => STATUS[s]?.type || 'info'
const fmt = (t) => (t ? t.replace('T', ' ').substring(0, 19) : '-')

async function load() {
  loading.value = true
  try {
    const res = await hostOrders({ page: page.value, pageSize: pageSize.value, status: status.value })
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

async function openDetail(row) {
  current.value = await hostOrderDetail(row.id)
  drawer.value = true
}

async function doAction(row, action) {
  const isIn = action === 'check-in'
  try {
    await ElMessageBox.confirm(isIn ? '确认租客已到店入住？' : '确认办理退房并结算订单？', '操作确认', {
      type: 'warning',
      confirmButtonText: '确认'
    })
  } catch (e) {
    return
  }
  if (isIn) {
    await confirmCheckIn(row.id)
    ElMessage.success('已确认入住')
  } else {
    await confirmCheckOut(row.id)
    ElMessage.success('已办理退房')
  }
  drawer.value = false
  load()
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 10px;
}
.cell-stay {
  display: flex;
  align-items: center;
  gap: 8px;
}
.mini-cover {
  width: 44px;
  height: 32px;
  border-radius: 4px;
  object-fit: cover;
}
.cell-sub {
  color: var(--bnb-text-secondary);
  font-size: 12px;
}
.cell-amount {
  color: var(--bnb-accent);
  font-weight: 700;
}
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
.drawer-actions {
  margin-top: 20px;
  text-align: center;
}
</style>
