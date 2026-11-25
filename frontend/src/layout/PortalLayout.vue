<template>
  <div class="portal-layout">
    <header class="portal-header">
      <div class="header-inner">
        <router-link to="/" class="logo">
          <img src="/favicon.svg" alt="logo" />
          <span>栖迟民宿</span>
        </router-link>
        <nav class="nav-menu">
          <router-link to="/">首页</router-link>
          <router-link to="/homestays">找民宿</router-link>
          <router-link to="/coupons">优惠券</router-link>
          <router-link to="/notices">公告</router-link>
        </nav>
        <div class="header-right">
          <template v-if="userStore.isLogin">
            <el-dropdown trigger="click" @command="onCommand">
              <span class="user-trigger">
                <el-avatar :size="30" :src="userStore.userInfo?.avatar">
                  {{ userStore.userInfo?.realName?.charAt(0) || '客' }}
                </el-avatar>
                <span class="username">{{ userStore.userInfo?.realName || userStore.userInfo?.username }}</span>
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="orders" :icon="List">我的订单</el-dropdown-item>
                  <el-dropdown-item command="favorites" :icon="Star">我的收藏</el-dropdown-item>
                  <el-dropdown-item command="coupons" :icon="Ticket">我的优惠券</el-dropdown-item>
                  <el-dropdown-item command="profile" :icon="User">个人中心</el-dropdown-item>
                  <el-dropdown-item v-if="isHostOrAdmin" command="console" :icon="Monitor">
                    {{ userStore.role === 'ADMIN' ? '管理后台' : '房东中心' }}
                  </el-dropdown-item>
                  <el-dropdown-item command="logout" :icon="SwitchButton" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button text @click="$router.push('/login')">登录</el-button>
            <el-button type="primary" round @click="$router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </header>

    <main class="portal-main">
      <router-view />
    </main>

    <footer class="portal-footer">
      <p>栖迟民宿管理系统 · 毕业设计演示项目</p>
      <p>Spring Boot + Vue3 + MySQL + Redis + Element Plus</p>
    </footer>

    <!-- AI 智能客服浮窗 -->
    <AiChat />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { ArrowDown, List, Star, User, Monitor, SwitchButton, Ticket } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import AiChat from '@/components/AiChat.vue'

const router = useRouter()
const userStore = useUserStore()

const isHostOrAdmin = computed(() => ['HOST', 'ADMIN'].includes(userStore.role))

function onCommand(command) {
  if (command === 'logout') {
    ElMessageBox.confirm('确定退出登录吗？', '提示', { type: 'warning' }).then(() => {
      userStore.logout()
      router.push('/login')
    }).catch(() => {})
    return
  }
  if (command === 'console') {
    router.push(userStore.role === 'ADMIN' ? '/admin' : '/host')
    return
  }
  router.push('/' + command)
}
</script>

<style scoped>
.portal-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}
.portal-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #fff;
  box-shadow: 0 1px 8px rgba(43, 51, 62, 0.06);
}
.header-inner {
  max-width: 1200px;
  height: 64px;
  margin: 0 auto;
  padding: 0 16px;
  display: flex;
  align-items: center;
}
.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: 700;
  color: var(--bnb-primary);
}
.logo img {
  width: 30px;
  height: 30px;
}
.nav-menu {
  margin-left: 48px;
  display: flex;
  gap: 32px;
  font-size: 15px;
}
.nav-menu a {
  color: var(--bnb-text);
  line-height: 64px;
  position: relative;
}
.nav-menu a:hover,
.nav-menu a.router-link-exact-active {
  color: var(--bnb-primary);
}
.nav-menu a.router-link-exact-active::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 3px;
  border-radius: 2px;
  background: var(--bnb-primary);
}
.header-right {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 12px;
}
.user-trigger {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  outline: none;
}
.username {
  font-size: 14px;
  color: var(--bnb-text);
}
.portal-main {
  flex: 1;
}
.portal-footer {
  background: #fff;
  border-top: 1px solid var(--bnb-border);
  text-align: center;
  padding: 24px 0;
  color: var(--bnb-text-secondary);
  font-size: 13px;
  line-height: 1.9;
}
.portal-footer p {
  margin: 0;
}
</style>
