<template>
  <div class="page-container confirm-page" v-loading="loading">
    <el-page-header content="预订确认" @back="$router.back()" class="back-bar" />

    <el-row :gutter="20" class="confirm-main" v-if="detail">
      <el-col :xs="24" :md="16">
        <el-card shadow="never" class="block">
          <template #header><span class="block-title">房源信息</span></template>
          <div class="stay-line">
            <img :src="detail.coverImg" class="stay-cover" />
            <div class="stay-meta">
              <div class="stay-title">{{ detail.title }}</div>
              <div class="stay-sub">
                <el-icon><Location /></el-icon>{{ detail.city }} · {{ detail.address }}
              </div>
              <div class="stay-sub">可住 {{ detail.maxGuests }} 人 · {{ detail.bedrooms }} 卧 {{ detail.beds }} 床</div>
            </div>
          </div>
        </el-card>

        <el-card shadow="never" class="block">
          <template #header><span class="block-title">入住信息</span></template>
          <el-form :model="form" label-width="92px" class="book-form">
            <el-form-item label="入住日期">
              <el-date-picker
                v-model="dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="入住"
                end-placeholder="退房"
                value-format="YYYY-MM-DD"
                :disabled-date="disableDate"
                style="width: 100%" />
            </el-form-item>
            <el-form-item label="入住人数">
              <el-input-number v-model="form.guestCount" :min="1" :max="detail.maxGuests" />
              <span class="hint">最多 {{ detail.maxGuests }} 人</span>
            </el-form-item>
            <el-form-item label="入住人" required>
              <el-input v-model="form.contactName" maxlength="50" placeholder="请输入入住人姓名" />
            </el-form-item>
            <el-form-item label="联系手机" required>
              <el-input v-model="form.contactPhone" maxlength="11" placeholder="用于房东联系，请填写 11 位手机号" />
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="8">
        <div class="pay-bar card-shadow">
          <div class="price-head">费用明细</div>
          <div class="price-row">
            <span>¥{{ money(form2.unitPrice || detail.price) }} × {{ form2.nights || 0 }} 晚</span>
            <span>¥{{ money(form2.totalAmount) }}</span>
          </div>
          <div class="price-row" v-if="form2.checkInDate">
            <span class="muted">{{ form2.checkInDate }} 至 {{ form2.checkOutDate }}</span>
          </div>

          <div class="coupon-row">
            <span>优惠券</span>
            <el-select
              v-model="selectedCouponId"
              placeholder="不使用优惠券"
              clearable
              class="coupon-select"
              :loading="couponLoading"
              @change="onCouponChange">
              <el-option
                v-for="c in couponOptions"
                :key="c.id"
                :label="couponOptionLabel(c)"
                :value="c.id"
                :disabled="!c.usable">
                <span :class="{ 'option-disabled': !c.usable }">{{ couponOptionLabel(c) }}</span>
              </el-option>
            </el-select>
          </div>

          <div class="price-row discount-row" v-if="Number(form2.discountAmount) > 0">
            <span>优惠券抵扣（{{ form2.couponName }}）</span>
            <span class="discount-num">-¥{{ money(form2.discountAmount) }}</span>
          </div>

          <el-divider />
          <div class="price-total">
            <span>合计应付</span>
            <span class="total-num">¥{{ money(form2.payAmount || form2.totalAmount) }}</span>
          </div>
          <el-button type="primary" class="submit-btn" :loading="submitting" @click="submit">
            提交订单
          </el-button>
          <div class="tip">提交后请在 30 分钟内完成支付，超时订单自动取消</div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Location } from '@element-plus/icons-vue'
import { homestayDetail } from '@/api/homestay'
import { confirmBooking, createOrder, bookedDates } from '@/api/order'
import { usableCoupons } from '@/api/coupon'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const homestayId = Number(route.params.id)
const loading = ref(false)
const submitting = ref(false)
const detail = ref(null)
const bookedRanges = ref([])
const dateRange = ref([route.query.in, route.query.out])

const form = reactive({
  guestCount: Number(route.query.guests) || 1,
  contactName: userStore.userInfo?.realName || userStore.userInfo?.nickname || '',
  contactPhone: userStore.userInfo?.phone || ''
})
const form2 = reactive({
  nights: 0, unitPrice: 0, totalAmount: '0.00', payAmount: '0.00',
  discountAmount: '0.00', couponName: '', couponId: null,
  checkInDate: '', checkOutDate: ''
})

// 优惠券
const selectedCouponId = ref(null)
const couponOptions = ref([])
const couponLoading = ref(false)

/** 金额统一两位小数 */
function money(v) {
  const n = Number(v || 0)
  return n.toFixed(2)
}

function couponOptionLabel(c) {
  const benefit = c.type === 'DISCOUNT'
    ? `${(c.discountPercent / 10).toFixed(1)} 折`
    : `减 ¥${c.discountAmount}`
  return c.usable
    ? `${c.name}（${benefit}）`
    : `${c.name}（${c.unusableReason || '不可用'}）`
}

async function loadUsableCoupons() {
  if (!dateRange.value || dateRange.value.length !== 2) return
  couponLoading.value = true
  try {
    couponOptions.value = await usableCoupons({
      homestayId,
      checkInDate: dateRange.value[0],
      checkOutDate: dateRange.value[1]
    })
    // 已选券在新列表中若已不可用则清除选择
    if (selectedCouponId.value) {
      const hit = couponOptions.value.find((c) => c.id === selectedCouponId.value)
      if (!hit || !hit.usable) selectedCouponId.value = null
    }
  } catch (e) {
    couponOptions.value = []
  } finally {
    couponLoading.value = false
  }
}

function onCouponChange() {
  refreshQuote()
}

function disableDate(date) {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  if (date.getTime() < today.getTime()) return true
  return bookedRanges.value.some((r) => {
    const s = new Date(r.checkInDate + 'T00:00:00')
    const e = new Date(r.checkOutDate + 'T00:00:00')
    return date.getTime() >= s.getTime() && date.getTime() < e.getTime()
  })
}

async function refreshQuote() {
  if (!dateRange.value || dateRange.value.length !== 2) return
  const payload = {
    homestayId,
    checkInDate: dateRange.value[0],
    checkOutDate: dateRange.value[1],
    guestCount: form.guestCount,
    contactName: form.contactName || '占位',
    contactPhone: form.contactPhone || '13900000001',
    couponId: selectedCouponId.value || null
  }
  try {
    const data = await confirmBooking(payload)
    Object.assign(form2, data)
  } catch (e) {
    // 试算失败（档期/人数/券不可用）保留提示，不允许提交
    form2.nights = 0
    form2.totalAmount = '0.00'
    form2.payAmount = '0.00'
    form2.discountAmount = '0.00'
    form2.couponName = ''
    form2.couponId = null
  }
}

/** 日期变化：重新拉取可用券并试算 */
async function onDateChange() {
  selectedCouponId.value = null
  await Promise.all([loadUsableCoupons(), refreshQuote()])
}

async function submit() {
  if (!dateRange.value || dateRange.value.length !== 2) {
    ElMessage.warning('请选择入住和退房日期')
    return
  }
  if (!form.contactName.trim()) {
    ElMessage.warning('请填写入住人姓名')
    return
  }
  if (!/^1[3-9]\d{9}$/.test(form.contactPhone)) {
    ElMessage.warning('请填写正确的 11 位手机号')
    return
  }
  submitting.value = true
  try {
    const order = await createOrder({
      homestayId,
      checkInDate: dateRange.value[0],
      checkOutDate: dateRange.value[1],
      guestCount: form.guestCount,
      contactName: form.contactName.trim(),
      contactPhone: form.contactPhone.trim(),
      couponId: selectedCouponId.value || null
    })
    ElMessage.success('订单已创建，请尽快支付')
    router.replace(`/orders/cashier/${order.id}`)
  } finally {
    submitting.value = false
  }
}

watch(dateRange, onDateChange)
watch(() => form.guestCount, refreshQuote)

onMounted(async () => {
  loading.value = true
  try {
    const [d, ranges] = await Promise.all([
      homestayDetail(homestayId),
      bookedDates(homestayId).catch(() => [])
    ])
    detail.value = d
    bookedRanges.value = ranges || []
    if (!userStore.userInfo) await userStore.fetchProfile().catch(() => null)
    if (!form.contactName) form.contactName = userStore.userInfo?.realName || ''
    if (!form.contactPhone) form.contactPhone = userStore.userInfo?.phone || ''
    if (dateRange.value && dateRange.value.length === 2) {
      await Promise.all([loadUsableCoupons(), refreshQuote()])
    }
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.back-bar {
  margin-bottom: 14px;
}
.confirm-main {
  margin-top: 12px;
}
.block {
  margin-bottom: 16px;
  border-radius: 10px;
}
.block-title {
  font-weight: 600;
  font-size: 16px;
}
.stay-line {
  display: flex;
  gap: 14px;
}
.stay-cover {
  width: 132px;
  height: 96px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
}
.stay-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
}
.stay-sub {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--bnb-text-secondary);
  font-size: 13px;
  margin-top: 4px;
}
.hint {
  margin-left: 12px;
  color: var(--bnb-text-secondary);
  font-size: 13px;
}
.pay-bar {
  background: #fff;
  border-radius: 10px;
  padding: 20px;
  position: sticky;
  top: 16px;
}
.price-head {
  font-weight: 600;
  font-size: 16px;
  margin-bottom: 14px;
}
.price-row {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  margin-bottom: 10px;
}
.muted {
  color: var(--bnb-text-secondary);
  font-size: 13px;
}
.coupon-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 14px 0 6px;
  font-size: 14px;
}
.coupon-select {
  width: 200px;
}
.option-disabled {
  color: #bbb;
}
.discount-row {
  color: var(--bnb-accent);
}
.discount-num {
  font-weight: 600;
}
.price-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.total-num {
  color: var(--bnb-accent);
  font-size: 24px;
  font-weight: 700;
}
.submit-btn {
  width: 100%;
  height: 44px;
  margin-top: 16px;
  font-size: 16px;
}
.tip {
  margin-top: 10px;
  font-size: 12px;
  color: var(--bnb-text-secondary);
  text-align: center;
}
</style>
