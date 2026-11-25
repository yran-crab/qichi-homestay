<template>
  <div class="stay-card card-shadow" :class="{ 'is-offline': offline }" @click="onCardClick">
    <div class="stay-cover">
      <img :src="item.coverImg" :alt="item.title" loading="lazy" />
      <span class="stay-type">{{ item.type }}</span>
      <!-- 收藏角标 -->
      <span
        v-if="!offline"
        class="fav-btn"
        :class="{ active: item.favored }"
        title="收藏 / 取消收藏"
        @click.stop="onToggleFavorite">
        <el-icon><StarFilled v-if="item.favored" /><Star v-else /></el-icon>
      </span>
      <div v-if="offline" class="offline-mask">已下架</div>
    </div>
    <div class="stay-body">
      <div class="stay-title">{{ item.title }}</div>
      <div class="stay-meta">
        <el-icon><Location /></el-icon>{{ item.city }} · {{ item.bedrooms }}室 · 可住{{ item.maxGuests }}人
      </div>
      <div class="stay-foot">
        <div class="stay-price">
          <span class="price-num">¥{{ item.price }}</span><span class="price-unit">/晚</span>
        </div>
        <div class="stay-rating">
          <el-icon><StarFilled /></el-icon>
          {{ Number(item.rating) > 0 ? Number(item.rating).toFixed(1) : '暂无' }}
          <span v-if="item.reviewCount > 0" class="review-count">({{ item.reviewCount }})</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Location, Star, StarFilled } from '@element-plus/icons-vue'
import { toggleFavorite } from '@/api/favorite'
import { useUserStore } from '@/stores/user'

const props = defineProps({
  item: { type: Object, required: true }
})
const emit = defineEmits(['changed'])

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const offline = computed(() => props.item.status && props.item.status !== 'APPROVED')

function onCardClick() {
  if (offline.value) {
    ElMessage.info('该房源已下架')
    return
  }
  router.push(`/homestay/${props.item.id}`)
}

async function onToggleFavorite() {
  if (!userStore.isLogin) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  try {
    const favored = await toggleFavorite(props.item.id)
    props.item.favored = favored
    props.item.favoriteCount = Math.max(0, (props.item.favoriteCount || 0) + (favored ? 1 : -1))
    ElMessage.success(favored ? '已加入收藏' : '已取消收藏')
    emit('changed', { id: props.item.id, favored })
  } catch (e) {
    // 全局拦截器已提示
  }
}
</script>

<style scoped>
.stay-card {
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
  margin-bottom: 20px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}
.stay-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 26px rgba(46, 158, 143, 0.15);
}
.stay-cover {
  position: relative;
  height: 180px;
  overflow: hidden;
}
.stay-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}
.stay-card:hover .stay-cover img {
  transform: scale(1.05);
}
.stay-type {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 2px 10px;
  border-radius: 10px;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  font-size: 12px;
}
.fav-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.35);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  transition: all 0.2s;
  z-index: 2;
}
.fav-btn:hover {
  background: rgba(0, 0, 0, 0.55);
  transform: scale(1.1);
}
.fav-btn.active {
  color: #ffb400;
  background: rgba(255, 255, 255, 0.92);
}
.offline-mask {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 17px;
  font-weight: 600;
  letter-spacing: 3px;
}
.is-offline {
  cursor: default;
}
.is-offline:hover {
  transform: none;
  box-shadow: none;
}
.stay-body {
  padding: 12px 14px 14px;
}
.stay-title {
  font-size: 15px;
  font-weight: 600;
  line-height: 1.4;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 6px;
}
.stay-meta {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12.5px;
  color: var(--bnb-text-secondary);
  margin-bottom: 10px;
}
.stay-foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.price-num {
  color: var(--bnb-accent);
  font-size: 19px;
  font-weight: 700;
}
.price-unit {
  color: var(--bnb-text-secondary);
  font-size: 12px;
}
.stay-rating {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 13px;
  color: #faad14;
}
.review-count {
  color: var(--bnb-text-secondary);
}
</style>
