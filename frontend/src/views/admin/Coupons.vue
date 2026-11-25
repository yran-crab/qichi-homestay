<template>
  <div class="admin-coupons" v-loading="loading">
    <el-card shadow="never">
      <div class="toolbar">
        <span class="page-title">优惠券管理</span>
        <el-button type="primary" @click="openCreate"><el-icon><Plus /></el-icon>发布优惠券</el-button>
      </div>

      <!-- 筛选 -->
      <el-form :inline="true" class="filter-form">
        <el-form-item label="名称">
          <el-input v-model="query.name" placeholder="优惠券名称" clearable @keyup.enter="load(1)" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="query.type" placeholder="全部类型" clearable style="width: 140px">
            <el-option label="代金券" value="CASH" />
            <el-option label="满减券" value="FULL_REDUCE" />
            <el-option label="折扣券" value="DISCOUNT" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 120px">
            <el-option label="上架中" :value="1" />
            <el-option label="已下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load(1)">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" stripe>
        <el-table-column label="ID" prop="id" width="60" />
        <el-table-column label="名称" prop="name" min-width="170" />
        <el-table-column label="类型" width="90">
          <template #default="{ row }">
            <el-tag :type="typeTag(row.type)" size="small">{{ typeText(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="优惠规则" min-width="170">
          <template #default="{ row }">
            <div>{{ ruleText(row) }}</div>
            <div class="sub-text" v-if="row.type === 'DISCOUNT' && row.maxDiscountAmount">
              最高减 ¥{{ Number(row.maxDiscountAmount).toFixed(2) }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="有效期" min-width="200">
          <template #default="{ row }">
            <div v-if="row.validType === 'FIXED'">
              {{ fmt(row.validStartTime) }} ~ {{ fmt(row.validEndTime) }}
            </div>
            <div v-else>领取后 {{ row.validDays }} 天内有效</div>
          </template>
        </el-table-column>
        <el-table-column label="领取/总量" width="110">
          <template #default="{ row }">
            {{ row.receivedCount }} / {{ row.totalCount === -1 ? '不限' : row.totalCount }}
          </template>
        </el-table-column>
        <el-table-column label="已用" prop="usedCount" width="70" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="190" fixed="right">
          <template #default="{ row }">
            <el-button
              link type="primary" size="small"
              :disabled="row.receivedCount > 0"
              @click="openEdit(row)">编辑</el-button>
            <el-button link :type="row.status === 1 ? 'warning' : 'success'" size="small" @click="toggleStatus(row)">
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button
              link type="danger" size="small"
              :disabled="row.receivedCount > 0"
              @click="onDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pager"
        background
        layout="total, prev, pager, next"
        :total="total"
        :page-size="query.pageSize"
        :current-page="query.page"
        @current-change="load" />
    </el-card>

    <!-- 新增 / 编辑弹窗 -->
    <el-dialog v-model="dialog" :title="form.id ? '编辑优惠券' : '发布优惠券'" width="620px" @closed="resetForm">
      <el-form :model="form" label-width="100px">
        <el-form-item label="券名称" required>
          <el-input v-model="form.name" maxlength="50" show-word-limit placeholder="如：新人立减20元券" />
        </el-form-item>
        <el-form-item label="券类型" required>
          <el-radio-group v-model="form.type">
            <el-radio label="CASH">代金券（无门槛立减）</el-radio>
            <el-radio label="FULL_REDUCE">满减券</el-radio>
            <el-radio label="DISCOUNT">折扣券</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item v-if="form.type === 'FULL_REDUCE'" label="使用门槛" required>
          <el-input-number v-model="form.threshold" :min="0" :precision="2" :step="10" />
          <span class="hint">订单满多少元可用</span>
        </el-form-item>
        <el-form-item v-if="form.type === 'CASH'" label="使用门槛">
          <el-input-number v-model="form.threshold" :min="0" :precision="2" :step="10" />
          <span class="hint">一般为 0（无门槛）</span>
        </el-form-item>

        <el-form-item v-if="form.type === 'DISCOUNT'" label="折扣率" required>
          <el-input-number v-model="form.discountPercent" :min="1" :max="99" :step="1" />
          <span class="hint">填 88 表示 8.8 折</span>
        </el-form-item>
        <el-form-item v-if="form.type === 'DISCOUNT'" label="封顶优惠">
          <el-input-number v-model="form.maxDiscountAmount" :min="0" :precision="2" :step="10" />
          <span class="hint">最多抵扣多少元，不填则不封顶</span>
        </el-form-item>

        <el-form-item v-if="form.type !== 'DISCOUNT'" label="优惠金额" required>
          <el-input-number v-model="form.discountAmount" :min="0.01" :precision="2" :step="10" />
          <span class="hint">元</span>
        </el-form-item>

        <el-form-item label="发行总量" required>
          <el-input-number v-model="form.totalCount" :min="-1" :step="100" />
          <span class="hint">-1 表示不限量</span>
        </el-form-item>
        <el-form-item label="每人限领" required>
          <el-input-number v-model="form.perLimit" :min="1" :max="10" />
          <span class="hint">张</span>
        </el-form-item>

        <el-form-item label="有效期类型" required>
          <el-radio-group v-model="form.validType">
            <el-radio label="FIXED">固定时间区间</el-radio>
            <el-radio label="DAYS">领取后 N 天</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.validType === 'FIXED'" label="有效区间" required>
          <el-date-picker
            v-model="fixedRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%" />
        </el-form-item>
        <el-form-item v-else label="有效天数" required>
          <el-input-number v-model="form.validDays" :min="1" :max="365" />
          <span class="hint">领取后多少天内有效</span>
        </el-form-item>

        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">立即上架</el-radio>
            <el-radio :label="0">暂不上架</el-radio>
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
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  adminCouponPage, createCoupon, updateCoupon, updateCouponStatus, deleteCoupon
} from '@/api/coupon'

const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const total = ref(0)
const dialog = ref(false)
const fixedRange = ref([])

const query = reactive({ page: 1, pageSize: 10, name: '', type: '', status: null })

const emptyForm = () => ({
  id: null,
  name: '',
  type: 'CASH',
  threshold: 0,
  discountAmount: 20,
  discountPercent: 88,
  maxDiscountAmount: null,
  totalCount: 1000,
  perLimit: 1,
  validType: 'FIXED',
  validDays: 30,
  status: 1
})
const form = reactive(emptyForm())

function typeText(t) {
  return { CASH: '代金券', FULL_REDUCE: '满减券', DISCOUNT: '折扣券' }[t] || t
}
function typeTag(t) {
  return { CASH: 'warning', FULL_REDUCE: 'danger', DISCOUNT: 'success' }[t] || ''
}
function ruleText(row) {
  const th = Number(row.threshold || 0)
  if (row.type === 'DISCOUNT') {
    return `${(row.discountPercent / 10).toFixed(1)} 折${th > 0 ? `（满 ¥${th.toFixed(2)} 可用）` : '（无门槛）'}`
  }
  return th > 0
    ? `满 ¥${th.toFixed(2)} 减 ¥${Number(row.discountAmount).toFixed(2)}`
    : `无门槛立减 ¥${Number(row.discountAmount).toFixed(2)}`
}
function fmt(t) {
  return t ? t.replace('T', ' ').substring(0, 16) : '-'
}

async function load(page) {
  if (page) query.page = page
  loading.value = true
  try {
    const data = await adminCouponPage({
      page: query.page,
      pageSize: query.pageSize,
      name: query.name || undefined,
      type: query.type || undefined,
      status: query.status === '' || query.status === null ? undefined : query.status
    })
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.name = ''
  query.type = ''
  query.status = null
  load(1)
}

function openCreate() {
  Object.assign(form, emptyForm())
  fixedRange.value = []
  dialog.value = true
}

function openEdit(row) {
  Object.assign(form, {
    id: row.id,
    name: row.name,
    type: row.type,
    threshold: Number(row.threshold),
    discountAmount: row.discountAmount != null ? Number(row.discountAmount) : null,
    discountPercent: row.discountPercent,
    maxDiscountAmount: row.maxDiscountAmount != null ? Number(row.maxDiscountAmount) : null,
    totalCount: row.totalCount,
    perLimit: row.perLimit,
    validType: row.validType,
    validDays: row.validDays,
    status: row.status
  })
  if (row.validType === 'FIXED') {
    fixedRange.value = [
      row.validStartTime.replace(' ', 'T').substring(0, 19),
      row.validEndTime.replace(' ', 'T').substring(0, 19)
    ]
  } else {
    fixedRange.value = []
  }
  dialog.value = true
}

function validateForm() {
  if (!form.name.trim()) return '请填写券名称'
  if (form.type === 'FULL_REDUCE' && !(form.threshold > 0)) return '满减券请填写大于 0 的使用门槛'
  if (form.type !== 'DISCOUNT' && !(form.discountAmount > 0)) return '请填写优惠金额'
  if (form.type === 'FULL_REDUCE' && form.discountAmount >= form.threshold) {
    return '满减券的门槛需大于优惠金额'
  }
  if (form.type === 'DISCOUNT' && !(form.discountPercent >= 1 && form.discountPercent <= 99)) {
    return '折扣率需在 1-99 之间'
  }
  if (form.validType === 'FIXED' && (!fixedRange.value || fixedRange.value.length !== 2)) {
    return '请选择固定有效期区间'
  }
  if (form.validType === 'DAYS' && !(form.validDays >= 1)) return '请填写有效天数'
  return null
}

async function submit() {
  const err = validateForm()
  if (err) {
    ElMessage.warning(err)
    return
  }
  const payload = { ...form }
  if (form.validType === 'FIXED') {
    payload.validStartTime = fixedRange.value[0]
    payload.validEndTime = fixedRange.value[1]
    payload.validDays = null
  } else {
    payload.validStartTime = null
    payload.validEndTime = null
  }
  if (form.type !== 'DISCOUNT') {
    payload.discountPercent = null
    payload.maxDiscountAmount = null
  } else {
    payload.discountAmount = null
  }
  submitting.value = true
  try {
    if (form.id) {
      await updateCoupon(form.id, payload)
      ElMessage.success('更新成功')
    } else {
      await createCoupon(payload)
      ElMessage.success('发布成功')
    }
    dialog.value = false
    load()
  } catch (e) {} finally {
    submitting.value = false
  }
}

async function toggleStatus(row) {
  const target = row.status === 1 ? 0 : 1
  try {
    await updateCouponStatus(row.id, target)
    ElMessage.success(target === 1 ? '已上架' : '已下架')
    load()
  } catch (e) {}
}

async function onDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除优惠券「${row.name}」吗？`, '删除确认', { type: 'warning' })
  } catch (e) { return }
  try {
    await deleteCoupon(row.id)
    ElMessage.success('删除成功')
    load()
  } catch (e) {}
}

function resetForm() {
  Object.assign(form, emptyForm())
  fixedRange.value = []
}

onMounted(() => load(1))
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
.filter-form {
  margin-top: 14px;
}
.filter-form :deep(.el-form-item) {
  margin-bottom: 8px;
}
.sub-text {
  color: var(--bnb-text-secondary);
  font-size: 12px;
}
.hint {
  margin-left: 10px;
  color: var(--bnb-text-secondary);
  font-size: 12.5px;
}
.pager {
  margin-top: 14px;
  justify-content: flex-end;
}
</style>
