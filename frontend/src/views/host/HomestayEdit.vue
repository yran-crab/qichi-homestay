<template>
  <div>
    <el-page-header :content="isEdit ? '编辑房源' : '发布房源'" @back="$router.back()" class="page-head" />

    <el-form ref="formRef" :model="form" :rules="rules" label-position="top" v-loading="loading">
      <!-- 图片 -->
      <el-card shadow="never" class="form-card">
        <template #header><span class="card-title">房源图片</span></template>
        <ImageUpload v-model="form.images" multiple :max="10" />
        <div class="form-hint">第一张图片将作为房源封面展示，请将最满意的照片放在首位</div>
      </el-card>

      <!-- 基本信息 -->
      <el-card shadow="never" class="form-card">
        <template #header><span class="card-title">基本信息</span></template>
        <el-row :gutter="18">
          <el-col :span="24">
            <el-form-item label="房源标题" prop="title">
              <el-input v-model="form.title" maxlength="100" show-word-limit
                placeholder="例：西湖边原木风一居室 · 近地铁" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="8">
            <el-form-item label="房源类型" prop="type">
              <el-select v-model="form.type" placeholder="请选择" style="width: 100%">
                <el-option v-for="t in types" :key="t" :label="t" :value="t" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="8">
            <el-form-item label="所在城市" prop="city">
              <el-select v-model="form.city" filterable allow-create default-first-option
                placeholder="选择或输入城市" style="width: 100%">
                <el-option v-for="c in cities" :key="c" :label="c" :value="c" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="8">
            <el-form-item label="楼层（选填）">
              <el-input v-model="form.floorInfo" placeholder="例：12/32层" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="详细地址" prop="address">
              <el-input v-model="form.address" placeholder="街道、小区、楼栋等详细地址" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-card>

      <!-- 价格与规格 -->
      <el-card shadow="never" class="form-card">
        <template #header><span class="card-title">价格与规格</span></template>
        <el-row :gutter="18">
          <el-col :xs="12" :sm="6">
            <el-form-item label="每晚价格（元）" prop="price">
              <el-input-number v-model="form.price" :min="1" :max="99999" :precision="2"
                controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :xs="12" :sm="6">
            <el-form-item label="划线价（选填）">
              <el-input-number v-model="form.originalPrice" :min="0" :max="99999" :precision="2"
                controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :xs="12" :sm="6">
            <el-form-item label="面积（㎡）">
              <el-input-number v-model="form.area" :min="0" :max="9999" :precision="2"
                controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :xs="12" :sm="6">
            <el-form-item label="可住人数" prop="maxGuests">
              <el-input-number v-model="form.maxGuests" :min="1" :max="30"
                controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :xs="12" :sm="6">
            <el-form-item label="卧室" prop="bedrooms">
              <el-input-number v-model="form.bedrooms" :min="0" :max="20" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :xs="12" :sm="6">
            <el-form-item label="客厅" prop="livingRooms">
              <el-input-number v-model="form.livingRooms" :min="0" :max="20" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :xs="12" :sm="6">
            <el-form-item label="卫生间" prop="bathrooms">
              <el-input-number v-model="form.bathrooms" :min="1" :max="20" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :xs="12" :sm="6">
            <el-form-item label="床位" prop="beds">
              <el-input-number v-model="form.beds" :min="1" :max="40" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-card>

      <!-- 配套设施 -->
      <el-card shadow="never" class="form-card">
        <template #header><span class="card-title">配套设施</span></template>
        <el-form-item prop="facilityIds" class="facility-form-item">
          <el-checkbox-group v-model="form.facilityIds" class="facility-group">
            <el-checkbox v-for="f in facilities" :key="f.id" :label="f.id" border class="facility-check">
              <span class="facility-label">{{ f.icon }} {{ f.name }}</span>
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-card>

      <!-- 描述 -->
      <el-card shadow="never" class="form-card">
        <template #header><span class="card-title">房源介绍与入住说明</span></template>
        <el-form-item label="房源描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="5" maxlength="2000" show-word-limit
            placeholder="介绍房源特色、周边环境、交通等信息" />
        </el-form-item>
        <el-form-item label="入住说明（选填）">
          <el-input v-model="form.rules" type="textarea" :rows="3" maxlength="1000" show-word-limit
            placeholder="例：入住时间 14:00 后，退房时间 12:00 前；禁止吸烟、聚会" />
        </el-form-item>
      </el-card>

      <div class="form-actions">
        <el-button @click="$router.back()">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSubmit">{{ isEdit ? '保存修改' : '提交发布' }}</el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ImageUpload from '@/components/ImageUpload.vue'
import { facilityList } from '@/api/homestay'
import { getHostHomestay, createHomestay, updateHomestay } from '@/api/host/homestay'

const route = useRoute()
const router = useRouter()

const types = ['整租', '单间', '公寓', '别墅', '小院']
const cities = ['成都', '杭州', '厦门', '丽江', '大理', '西安', '重庆', '苏州']

const formRef = ref()
const loading = ref(false)
const saving = ref(false)
const facilities = ref([])

const isEdit = computed(() => !!route.params.id)

const form = reactive({
  title: '', city: '', address: '', floorInfo: '',
  type: '整租', price: null, originalPrice: null, area: null,
  bedrooms: 1, livingRooms: 1, bathrooms: 1, beds: 1, maxGuests: 2,
  images: [], description: '', rules: '',
  facilityIds: []
})

const rules = {
  title: [{ required: true, message: '请输入房源标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择房源类型', trigger: 'change' }],
  city: [{ required: true, message: '请选择城市', trigger: 'change' }],
  address: [{ required: true, message: '请输入详细地址', trigger: 'blur' }],
  price: [{ required: true, message: '请输入每晚价格', trigger: 'blur' }],
  maxGuests: [{ required: true, message: '请填写可住人数', trigger: 'blur' }],
  bedrooms: [{ required: true, trigger: 'blur' }],
  livingRooms: [{ required: true, trigger: 'blur' }],
  bathrooms: [{ required: true, trigger: 'blur' }],
  beds: [{ required: true, trigger: 'blur' }],
  description: [{ required: true, message: '请填写房源描述', trigger: 'blur' }],
  facilityIds: [
    { type: 'array', required: true, message: '请至少选择一项配套设施', trigger: 'change' }
  ]
}

async function loadFacilities() {
  facilities.value = await facilityList()
}

async function loadHomestay() {
  loading.value = true
  try {
    const data = await getHostHomestay(route.params.id)
    Object.assign(form, {
      title: data.title,
      city: data.city,
      address: data.address,
      floorInfo: data.floorInfo || '',
      type: data.type,
      price: data.price,
      originalPrice: data.originalPrice,
      area: data.area,
      bedrooms: data.bedrooms,
      livingRooms: data.livingRooms,
      bathrooms: data.bathrooms,
      beds: data.beds,
      maxGuests: data.maxGuests,
      images: data.images || [],
      description: data.description || '',
      rules: data.rules || '',
      facilityIds: (data.facilities || []).map((f) => f.id)
    })
  } finally {
    loading.value = false
  }
}

async function onSubmit() {
  await formRef.value.validate()
  if (!form.images.length) {
    ElMessage.warning('请至少上传一张房源图片')
    return
  }
  saving.value = true
  try {
    const payload = { ...form, coverImg: form.images[0] }
    if (isEdit.value) {
      await updateHomestay(route.params.id, payload)
      ElMessage.success('保存成功')
    } else {
      await createHomestay(payload)
      ElMessage.success('发布成功，等待平台审核')
    }
    router.push('/host/homestays')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadFacilities()
  if (isEdit.value) loadHomestay()
})
</script>

<style scoped>
.page-head {
  margin-bottom: 14px;
}
.form-card {
  margin-bottom: 16px;
}
.card-title {
  font-weight: 600;
  font-size: 15.5px;
}
.form-hint {
  margin-top: 8px;
  font-size: 12.5px;
  color: var(--bnb-text-secondary);
}
.facility-form-item :deep(.el-form-item__content) {
  flex-wrap: wrap;
}
.facility-group {
  display: flex;
  flex-wrap: wrap;
  gap: 4px 10px;
}
.facility-check {
  margin-right: 0;
  margin-bottom: 8px;
}
.facility-label {
  font-size: 13.5px;
}
.form-actions {
  display: flex;
  justify-content: center;
  gap: 14px;
  padding: 10px 0 30px;
}
</style>
