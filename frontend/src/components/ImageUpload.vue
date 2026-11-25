<template>
  <div class="image-upload" :class="{ 'is-single': !multiple }">
    <!-- 单图模式 -->
    <template v-if="!multiple">
      <div v-if="modelValue" class="single-preview">
        <img :src="modelValue" alt="preview" />
        <div class="mask">
          <el-icon @click.stop="removeSingle"><Delete /></el-icon>
        </div>
      </div>
      <el-upload
        v-else
        action="/api/file/upload"
        :headers="headers"
        name="file"
        :show-file-list="false"
        :before-upload="beforeUpload"
        :on-success="onSuccessSingle"
        :on-error="onError"
        accept="image/*"
        class="single-uploader"
      >
        <el-icon class="uploader-icon"><Plus /></el-icon>
      </el-upload>
    </template>

    <!-- 多图模式 -->
    <template v-else>
      <div v-for="(url, index) in modelValue" :key="url" class="multi-item">
        <img :src="url" alt="image" />
        <div class="mask">
          <el-icon @click.stop="removeMulti(index)"><Delete /></el-icon>
        </div>
      </div>
      <el-upload
        v-if="modelValue.length < max"
        action="/api/file/upload"
        :headers="headers"
        name="file"
        multiple
        :show-file-list="false"
        :before-upload="beforeUpload"
        :on-success="onSuccessMulti"
        :on-error="onError"
        accept="image/*"
        class="multi-uploader"
      >
        <el-icon class="uploader-icon"><Plus /></el-icon>
      </el-upload>
    </template>

    <div class="tip">
      <slot name="tip">支持 jpg/png/webp，单张不超过 5MB<template v-if="multiple">，最多 {{ max }} 张</template></slot>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: { type: [String, Array], default: () => '' },
  multiple: { type: Boolean, default: false },
  max: { type: Number, default: 8 }
})
const emit = defineEmits(['update:modelValue'])

const headers = computed(() => ({
  Authorization: 'Bearer ' + (localStorage.getItem('bnbms_token') || '')
}))

function beforeUpload(file) {
  const isImage = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'].includes(file.type)
  if (!isImage) {
    ElMessage.error('仅支持 jpg/png/webp/gif 格式图片')
    return false
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  return true
}

function onSuccessSingle(response) {
  if (response.code === 200) {
    emit('update:modelValue', response.data.url)
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

function onSuccessMulti(response) {
  if (response.code === 200) {
    const list = Array.isArray(props.modelValue) ? [...props.modelValue] : []
    if (list.length >= props.max) {
      ElMessage.warning(`最多上传 ${props.max} 张`)
      return
    }
    list.push(response.data.url)
    emit('update:modelValue', list)
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

function onError() {
  ElMessage.error('上传失败，请检查网络或重新登录')
}

function removeSingle() {
  emit('update:modelValue', '')
}

function removeMulti(index) {
  const list = [...props.modelValue]
  list.splice(index, 1)
  emit('update:modelValue', list)
}
</script>

<style scoped>
.single-uploader :deep(.el-upload) {
  width: 140px;
  height: 140px;
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: border-color 0.2s;
}
.single-uploader :deep(.el-upload:hover) {
  border-color: var(--bnb-primary);
}
.single-preview {
  position: relative;
  width: 140px;
  height: 140px;
  border-radius: 8px;
  overflow: hidden;
}
.single-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.mask {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 20px;
  opacity: 0;
  transition: opacity 0.2s;
  cursor: pointer;
}
.single-preview:hover .mask,
.multi-item:hover .mask {
  opacity: 1;
}
.image-upload {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  flex-wrap: wrap;
}
.multi-item,
.multi-uploader :deep(.el-upload) {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 8px;
  overflow: hidden;
}
.multi-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.multi-uploader :deep(.el-upload) {
  border: 1px dashed #d9d9d9;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}
.multi-uploader :deep(.el-upload:hover) {
  border-color: var(--bnb-primary);
}
.uploader-icon {
  font-size: 24px;
  color: #8c939d;
}
.tip {
  flex-basis: 100%;
  font-size: 12px;
  color: var(--bnb-text-secondary);
}
</style>
