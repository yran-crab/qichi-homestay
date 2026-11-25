<template>
  <div class="page-container cashier-page" v-loading="loading">
    <div class="cashier-box card-shadow" v-if="order">
      <!-- 正在校验支付结果 -->
      <template v-if="verifying">
        <div class="cashier-head">
          <el-icon class="wallet-icon is-loading"><Loading /></el-icon>
          <h2>正在校验支付结果</h2>
          <p>请稍候，正在与支付宝确认支付状态...</p>
        </div>
      </template>

      <!-- 支付未完成（回跳后校验未通过） -->
      <template v-else-if="verifyFailed">
        <el-result icon="warning" title="支付未完成" sub-title="未检测到支付成功记录，可重新发起支付">
          <template #extra>
            <el-button type="primary" @click="retryPay">重新支付</el-button>
            <el-button @click="$router.push('/orders')">查看我的订单</el-button>
          </template>
        </el-result>
      </template>

      <!-- 待支付 -->
      <template v-else-if="order.status === 'PENDING_PAY'">
        <div class="cashier-head">
          <el-icon class="wallet-icon"><Wallet /></el-icon>
          <h2>支付宝沙箱收银台</h2>
          <p>本系统使用支付宝沙箱环境，请使用沙箱买家账号完成支付</p>
        </div>

        <div class="countdown" v-if="remainText">
          支付剩余时间 <span class="countdown-num">{{ remainText }}</span>
        </div>

        <div class="amount-box">
          <div class="amount-label">待支付金额</div>
          <div class="amount-num">¥{{ money(order.payAmount) }}</div>
          <div class="amount-meta">订单号 {{ order.orderNo }}</div>
        </div>

        <el-card shadow="never" class="stay-card">
          <div class="stay-line">
            <img :src="order.coverImg" class="cover" />
            <div>
              <div class="title">{{ order.homestayTitle }}</div>
              <div class="sub">{{ order.checkInDate }} 至 {{ order.checkOutDate }} · {{ order.nights }} 晚 · {{ order.guestCount }} 人</div>
            </div>
          </div>
        </el-card>

        <div class="fee-card">
          <div class="fee-row">
            <span>订单总额</span>
            <span>¥{{ money(order.totalAmount) }}</span>
          </div>
          <div class="fee-row" v-if="Number(order.discountAmount) > 0">
            <span>优惠券（{{ order.couponName }}）</span>
            <span class="fee-discount">-¥{{ money(order.discountAmount) }}</span>
          </div>
        </div>

        <div class="pay-way">
          <div class="pay-way-item alipay-way">
            <el-icon class="way-icon alipay"><CreditCard /></el-icon>
            <span>支付宝沙箱支付</span>
          </div>
        </div>

        <el-button type="primary" class="pay-btn" :loading="paying" @click="onPay">
          确认支付 ¥{{ money(order.payAmount) }}
        </el-button>
        <el-button text class="cancel-btn" @click="onCancel">取消订单</el-button>
      </template>

      <!-- 支付成功 -->
      <template v-else-if="paid || order.status === 'PAID'">
        <el-result icon="success" title="支付成功" sub-title="房东已收到您的订单，入住前请保持手机畅通">
          <template #extra>
            <el-button type="primary" @click="$router.push('/orders')">查看我的订单</el-button>
            <el-button @click="$router.push('/homestays')">继续逛逛</el-button>
          </template>
        </el-result>
      </template>

      <!-- 其他状态（已取消等） -->
      <template v-else>
        <el-result icon="warning" :title="statusText(order.status)" sub-title="该订单无需支付">
          <template #extra>
            <el-button type="primary" @click="$router.push('/orders')">查看我的订单</el-button>
          </template>
        </el-result>
      </template>
    </div>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Wallet, CreditCard, Loading } from '@element-plus/icons-vue'
import { orderDetail, alipayOrder, verifyAlipayPay, cancelOrder } from '@/api/order'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const paying = ref(false)
const verifying = ref(false)
const verifyFailed = ref(false)
const paid = ref(false)
const order = ref(null)
const remainSeconds = ref(0)
let timer = null

const remainText = computed(() => {
  if (remainSeconds.value <= 0) return ''
  const m = String(Math.floor(remainSeconds.value / 60)).padStart(2, '0')
  const s = String(remainSeconds.value % 60).padStart(2, '0')
  return `${m}:${s}`
})

function statusText(s) {
  return { PAID: '订单已支付', CHECKED_IN: '订单已入住', COMPLETED: '订单已完成', CANCELLED: '订单已取消' }[s] || s
}

function money(v) {
  return Number(v || 0).toFixed(2)
}

function startCountdown() {
  if (!order.value.createdAt) return
  const created = new Date(order.value.createdAt.replace(' ', 'T')).getTime()
  remainSeconds.value = Math.max(0, 1800 - Math.floor((Date.now() - created) / 1000))
  timer = setInterval(() => {
    remainSeconds.value -= 1
    if (remainSeconds.value <= 0) {
      clearInterval(timer)
      ElMessage.warning('订单已超时，请重新下单')
      load()
    }
  }, 1000)
}

async function load() {
  loading.value = true
  try {
    order.value = await orderDetail(route.params.id)
    verifyFailed.value = false
    // 若从支付宝回跳，触发支付结果校验
    const flag = sessionStorage.getItem('alipay_return_' + route.params.id)
    if (flag) {
      sessionStorage.removeItem('alipay_return_' + route.params.id)
      if (order.value.status === 'PENDING_PAY') {
        await doVerify()
        return
      }
    }
    if (order.value.status === 'PAID') {
      paid.value = true
    }
    if (order.value.status === 'PENDING_PAY') {
      startCountdown()
    }
  } finally {
    loading.value = false
  }
}

async function onPay() {
  if (remainSeconds.value <= 0) {
    ElMessage.warning('订单已超时，请重新下单')
    return
  }
  paying.value = true
  try {
    const formHtml = await alipayOrder(route.params.id)
    // 标记正在等待支付宝回跳
    sessionStorage.setItem('alipay_return_' + route.params.id, '1')
    // 将支付宝返回的表单写入页面并提交，浏览器跳转到支付宝沙箱收银台
    const div = document.createElement('div')
    div.style.display = 'none'
    div.innerHTML = formHtml
    document.body.appendChild(div)
    const form = div.querySelector('form')
    if (form) {
      form.submit()
    } else {
      sessionStorage.removeItem('alipay_return_' + route.params.id)
      ElMessage.error('支付表单解析失败，请重试')
    }
  } catch (e) {
    sessionStorage.removeItem('alipay_return_' + route.params.id)
  } finally {
    paying.value = false
  }
}

async function doVerify() {
  verifying.value = true
  try {
    const result = await verifyAlipayPay(route.params.id)
    if (result === true) {
      paid.value = true
      order.value.status = 'PAID'
      ElMessage.success('支付成功')
    } else {
      verifyFailed.value = true
    }
  } catch (e) {
    verifyFailed.value = true
  } finally {
    verifying.value = false
    loading.value = false
  }
}

function retryPay() {
  verifyFailed.value = false
  paid.value = false
  order.value.status = 'PENDING_PAY'
  startCountdown()
}

async function onCancel() {
  try {
    await ElMessageBox.confirm('确定取消该订单吗？', '取消订单', { type: 'warning' })
  } catch (e) {
    return
  }
  await cancelOrder(route.params.id, '租客主动取消')
  ElMessage.success('订单已取消')
  router.replace('/orders')
}

onMounted(load)
onBeforeUnmount(() => timer && clearInterval(timer))
</script>

<style scoped>
.cashier-page {
  max-width: 640px;
}
.cashier-box {
  background: #fff;
  border-radius: 12px;
  padding: 32px 36px;
  margin-top: 20px;
}
.cashier-head {
  text-align: center;
}
.wallet-icon {
  font-size: 44px;
  color: var(--bnb-primary);
}
.wallet-icon.is-loading {
  animation: rotate 1.2s linear infinite;
}
@keyframes rotate {
  to { transform: rotate(360deg); }
}
.cashier-head h2 {
  margin: 10px 0 6px;
}
.cashier-head p {
  margin: 0;
  color: var(--bnb-text-secondary);
  font-size: 13px;
}
.countdown {
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
}
.countdown-num {
  color: var(--bnb-accent);
  font-weight: 700;
  font-size: 18px;
  margin-left: 6px;
}
.amount-box {
  text-align: center;
  margin: 24px 0 18px;
}
.amount-label {
  color: var(--bnb-text-secondary);
  font-size: 13px;
}
.amount-num {
  color: var(--bnb-accent);
  font-size: 40px;
  font-weight: 700;
  margin: 6px 0;
}
.amount-meta {
  color: var(--bnb-text-secondary);
  font-size: 12.5px;
}
.stay-card {
  border-radius: 8px;
}
.stay-line {
  display: flex;
  gap: 12px;
  align-items: center;
}
.cover {
  width: 84px;
  height: 60px;
  border-radius: 6px;
  object-fit: cover;
}
.title {
  font-weight: 600;
  font-size: 14.5px;
}
.sub {
  color: var(--bnb-text-secondary);
  font-size: 13px;
  margin-top: 4px;
}
.fee-card {
  margin-top: 12px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  padding: 10px 14px;
  font-size: 13.5px;
}
.fee-row {
  display: flex;
  justify-content: space-between;
  line-height: 26px;
  color: var(--bnb-text-secondary);
}
.fee-discount {
  color: var(--bnb-accent);
  font-weight: 600;
}
.pay-way {
  display: flex;
  justify-content: center;
  margin: 20px 0 8px;
}
.pay-way-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 24px;
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
  font-size: 14px;
}
.way-icon {
  font-size: 18px;
}
.alipay {
  color: #1677ff;
}
.pay-btn {
  width: 100%;
  height: 46px;
  font-size: 16px;
  margin-top: 14px;
}
.cancel-btn {
  width: 100%;
  margin-top: 6px;
}
</style>
