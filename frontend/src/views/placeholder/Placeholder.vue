<template>
  <div class="placeholder-page card-shadow">
    <el-empty description="模块建设中">
      <template #image>
        <el-icon :size="64" color="#c0c4cc"><Tools /></el-icon>
      </template>
      <p class="ph-title">{{ route.meta.title || '页面' }}</p>
      <p class="ph-desc">该模块将在后续任务中实现</p>
      <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
    </el-empty>
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router'
import { Tools } from '@element-plus/icons-vue'
const route = useRoute()
</script>

<style scoped>
.placeholder-page {
  min-height: 360px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.ph-title {
  font-size: 18px;
  font-weight: 600;
  margin: 8px 0 4px;
}
.ph-desc {
  color: var(--bnb-text-secondary);
  margin: 0 0 16px;
}
</style>
