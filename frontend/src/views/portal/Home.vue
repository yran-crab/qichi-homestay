<template>
  <div class="home-page" v-loading="loading">
    <!-- Hero 搜索区 -->
    <section class="hero">
      <div class="hero-mask">
        <h1>寻一处民宿，赴一段栖迟时光</h1>
        <p>精选城市特色民宿 · 在线预订 · 品质保障</p>
        <div class="hero-search">
          <el-input v-model="keyword" placeholder="搜索城市、地标或民宿名称" size="large" clearable
            @keyup.enter="goList">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-button type="primary" size="large" @click="goList">搜索民宿</el-button>
        </div>
      </div>
    </section>

    <template v-if="homeData">
      <!-- 轮播图 -->
      <section v-if="homeData.banners.length" class="banner-section">
        <el-carousel height="320px" :interval="5000" arrow="hover" indicator-position="outside">
          <el-carousel-item v-for="b in homeData.banners" :key="b.id">
            <a v-if="b.link" :href="b.link" class="banner-link" @click.prevent="goBanner(b)">
              <img :src="b.imageUrl" :alt="b.title" class="banner-img" />
              <div class="banner-title-overlay">{{ b.title }}</div>
            </a>
            <div v-else class="banner-link">
              <img :src="b.imageUrl" :alt="b.title" class="banner-img" />
              <div class="banner-title-overlay">{{ b.title }}</div>
            </div>
          </el-carousel-item>
        </el-carousel>
      </section>

      <!-- 公告条 -->
      <section v-if="homeData.notices.length" class="notice-bar">
        <div class="page-container">
          <div class="notice-inner" @click="$router.push('/notices')">
            <el-icon class="notice-icon"><Bell /></el-icon>
            <span class="notice-label">公告</span>
            <span class="notice-text">{{ homeData.notices[0].title }}</span>
            <span class="notice-more">查看全部 →</span>
          </div>
        </div>
      </section>

      <div class="page-container">
        <!-- 热门推荐 -->
        <section v-if="homeData.hotList.length" class="home-section">
          <div class="section-head">
            <h2 class="section-title">热门推荐</h2>
            <router-link to="/homestays" class="section-more">更多 →</router-link>
          </div>
          <el-row :gutter="20">
            <el-col v-for="item in homeData.hotList" :key="item.id" :xs="12" :sm="12" :md="8" :lg="6">
              <StayCard :item="item" />
            </el-col>
          </el-row>
        </section>

        <!-- 猜你喜欢（个性化推荐） -->
        <section v-if="recommendList.length" class="home-section recommend-section">
          <div class="section-head">
            <div class="title-wrap">
              <h2 class="section-title">猜你喜欢</h2>
              <span class="recommend-tag">AI 智能推荐</span>
            </div>
            <span class="recommend-hint">{{ recommendHint }}</span>
          </div>
          <el-row :gutter="20">
            <el-col v-for="item in recommendList" :key="item.id" :xs="12" :sm="12" :md="8" :lg="6">
              <StayCard :item="item" />
            </el-col>
          </el-row>
        </section>

        <!-- 最新上线 -->
        <section v-if="homeData.latestList.length" class="home-section">
          <div class="section-head">
            <h2 class="section-title">最新上线</h2>
            <router-link to="/homestays?sort=latest" class="section-more">更多 →</router-link>
          </div>
          <el-row :gutter="20">
            <el-col v-for="item in homeData.latestList" :key="item.id" :xs="12" :sm="12" :md="8" :lg="6">
              <StayCard :item="item" />
            </el-col>
          </el-row>
        </section>

        <!-- 城市快捷入口 -->
        <section class="home-section city-section">
          <div class="section-head">
            <h2 class="section-title">城市探索</h2>
          </div>
          <div class="city-grid">
            <div v-for="c in cities" :key="c.name" class="city-card" @click="goCity(c.name)">
              <img :src="c.img" :alt="c.name" class="city-img" />
              <div class="city-overlay">
                <span class="city-name">{{ c.name }}</span>
                <span class="city-count">{{ c.count }} 套民宿</span>
              </div>
            </div>
          </div>
        </section>
      </div>
    </template>
  </div>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Bell } from '@element-plus/icons-vue'
import { getHome, getRecommend } from '@/api/home'
import { useUserStore } from '@/stores/user'
import StayCard from '@/components/StayCard.vue'

const router = useRouter()
const userStore = useUserStore()
const keyword = ref('')
const loading = ref(false)
const homeData = ref(null)
const recommendList = ref([])

const recommendHint = computed(() =>
  userStore.isLogin
    ? '基于你的收藏和订单偏好生成'
    : '登录后获取专属个性化推荐'
)

const cities = [
  { name: '成都', img: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=chengdu%20panda%20base%20city%20scape&image_size=square', count: 2 },
  { name: '杭州', img: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=hangzhou%20west%20lake%20city%20scape&image_size=square', count: 3 },
  { name: '厦门', img: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=xiamen%20seaside%20city%20scape&image_size=square', count: 2 },
  { name: '丽江', img: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=lijiang%20ancient%20town%20city%20scape&image_size=square', count: 2 },
  { name: '大理', img: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=dali%20erhai%20lake%20city%20scape&image_size=square', count: 1 },
  { name: '重庆', img: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=chongqing%20night%20cityscape%20city%20scape&image_size=square', count: 1 }
]

function goList() {
  router.push({ path: '/homestays', query: keyword.value ? { keyword: keyword.value } : {} })
}

function goBanner(b) {
  if (b.link) router.push(b.link)
}

function goCity(city) {
  router.push({ path: '/homestays', query: { city } })
}

async function loadData() {
  loading.value = true
  try {
    const [home, recommend] = await Promise.all([getHome(), getRecommend(8)])
    homeData.value = home
    recommendList.value = recommend
  } catch (e) {
    // 静默处理
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.hero {
  height: 380px;
  background: linear-gradient(135deg, #2e9e8f 0%, #56c0b1 55%, #8fd8cf 100%);
  border-radius: 0 0 24px 24px;
}
.hero-mask {
  max-width: 1200px;
  margin: 0 auto;
  padding: 90px 16px 0;
  color: #fff;
}
.hero-mask h1 {
  font-size: 38px;
  margin: 0 0 12px;
  letter-spacing: 2px;
}
.hero-mask p {
  font-size: 17px;
  opacity: 0.92;
  margin: 0 0 32px;
}
.hero-search {
  display: flex;
  gap: 12px;
  max-width: 640px;
}
.hero-search :deep(.el-input__wrapper) {
  border-radius: 24px;
  padding: 4px 20px;
}
.hero-search .el-button {
  border-radius: 24px;
  padding: 0 32px;
}
.banner-section {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 16px;
  border-radius: 12px;
  overflow: hidden;
}
.banner-link {
  display: block;
  width: 100%;
  height: 100%;
  position: relative;
  cursor: pointer;
}
.banner-img {
  width: 100%;
  height: 320px;
  object-fit: cover;
  border-radius: 12px;
}
.banner-title-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 30px 24px 16px;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.55));
  color: #fff;
  font-size: 20px;
  font-weight: 600;
  border-radius: 0 0 12px 12px;
}
.notice-bar {
  max-width: 1200px;
  margin: 0 auto 20px;
  padding: 0 16px;
}
.notice-inner {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: #fff;
  border-radius: 8px;
  cursor: pointer;
  transition: box-shadow 0.2s;
}
.notice-inner:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}
.notice-icon {
  color: var(--bnb-accent);
  font-size: 18px;
}
.notice-label {
  background: var(--bnb-accent);
  color: #fff;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  flex-shrink: 0;
}
.notice-text {
  flex: 1;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.notice-more {
  color: var(--bnb-text-secondary);
  font-size: 13px;
  flex-shrink: 0;
}
.home-section {
  margin-bottom: 36px;
}
.recommend-section {
  position: relative;
}
.title-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
}
.recommend-tag {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
  white-space: nowrap;
}
.recommend-hint {
  color: var(--bnb-text-secondary);
  font-size: 13px;
}
.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.section-title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
}
.section-more {
  color: var(--bnb-primary);
  font-size: 14px;
  text-decoration: none;
}
.section-more:hover {
  text-decoration: underline;
}
.city-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
}
.city-card {
  position: relative;
  height: 120px;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s;
}
.city-card:hover {
  transform: translateY(-4px);
}
.city-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.city-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #fff;
}
.city-name {
  font-size: 18px;
  font-weight: 700;
}
.city-count {
  font-size: 12px;
  opacity: 0.85;
  margin-top: 2px;
}
@media (max-width: 768px) {
  .city-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>
