<template>
  <el-container class="console-layout">
    <el-aside width="220px" class="console-aside">
      <div class="aside-logo">
        <img src="/favicon.svg" alt="logo" />
        <span>{{ isAdmin ? '管理后台' : '房东中心' }}</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :router="true"
        class="aside-menu"
        background-color="transparent"
        text-color="#c9d4dd"
        active-text-color="#ffffff"
      >
        <template v-if="isAdmin">
          <el-menu-item :index="'/admin'">
            <el-icon><DataLine /></el-icon><span>平台看板</span>
          </el-menu-item>
          <el-menu-item :index="'/admin/users'">
            <el-icon><User /></el-icon><span>用户管理</span>
          </el-menu-item>
          <el-menu-item :index="'/admin/host-audit'">
            <el-icon><Avatar /></el-icon><span>房东审核</span>
          </el-menu-item>
          <el-menu-item :index="'/admin/homestay-audit'">
            <el-icon><CircleCheck /></el-icon><span>房源审核</span>
          </el-menu-item>
          <el-menu-item :index="'/admin/homestays'">
            <el-icon><House /></el-icon><span>房源管理</span>
          </el-menu-item>
          <el-menu-item :index="'/admin/orders'">
            <el-icon><List /></el-icon><span>订单管理</span>
          </el-menu-item>
          <el-menu-item :index="'/admin/reviews'">
            <el-icon><ChatDotRound /></el-icon><span>评价管理</span>
          </el-menu-item>
          <el-menu-item :index="'/admin/banners'">
            <el-icon><Picture /></el-icon><span>轮播管理</span>
          </el-menu-item>
          <el-menu-item :index="'/admin/notices'">
            <el-icon><Bell /></el-icon><span>公告管理</span>
          </el-menu-item>
          <el-menu-item :index="'/admin/coupons'">
            <el-icon><Ticket /></el-icon><span>优惠券管理</span>
          </el-menu-item>
          <el-menu-item :index="'/admin/facilities'">
            <el-icon><Grid /></el-icon><span>设施字典</span>
          </el-menu-item>
        </template>
        <template v-else>
          <el-menu-item :index="'/host'">
            <el-icon><DataLine /></el-icon><span>经营看板</span>
          </el-menu-item>
          <el-menu-item :index="'/host/homestays'">
            <el-icon><House /></el-icon><span>房源管理</span>
          </el-menu-item>
          <el-menu-item :index="'/host/orders'">
            <el-icon><List /></el-icon><span>订单管理</span>
          </el-menu-item>
          <el-menu-item :index="'/host/reviews'">
            <el-icon><ChatDotRound /></el-icon><span>评价管理</span>
          </el-menu-item>
          <el-menu-item :index="'/host/profile'">
            <el-icon><Setting /></el-icon><span>个人设置</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="console-header">
        <div class="header-title">{{ route.meta.title || '' }}</div>
        <div class="header-actions">
          <el-button text @click="$router.push('/')">
            <el-icon><Monitor /></el-icon> 返回门户
          </el-button>
          <el-dropdown trigger="click" @command="onCommand">
            <span class="user-trigger">
              <el-avatar :size="30" :src="userStore.userInfo?.avatar">
                {{ userStore.userInfo?.realName?.charAt(0) || '用' }}
              </el-avatar>
              <span>{{ userStore.userInfo?.realName || userStore.userInfo?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="home" :icon="Monitor">返回门户</el-dropdown-item>
                <el-dropdown-item command="logout" :icon="SwitchButton">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="console-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import {
  DataLine, User, Avatar, CircleCheck, House, List, ChatDotRound,
  Picture, Bell, Grid, Setting, Monitor, ArrowDown, SwitchButton, Ticket
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isAdmin = computed(() => route.path.startsWith('/admin'))
const activeMenu = computed(() => route.path)

function onCommand(command) {
  if (command === 'home') {
    router.push('/')
  } else if (command === 'logout') {
    ElMessageBox.confirm('确定退出登录吗？', '提示', { type: 'warning' }).then(() => {
      userStore.logout()
      router.push('/login')
    }).catch(() => {})
  }
}
</script>

<style scoped>
.console-layout {
  min-height: 100vh;
}
.console-aside {
  background: linear-gradient(180deg, #243447 0%, #1f2d3d 100%);
  overflow-x: hidden;
}
.aside-logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #fff;
  font-size: 17px;
  font-weight: 700;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}
.aside-logo img {
  width: 26px;
  height: 26px;
}
.aside-menu {
  border-right: none;
  padding: 8px 0;
}
.aside-menu :deep(.el-menu-item) {
  margin: 2px 10px;
  border-radius: 6px;
  height: 46px;
  line-height: 46px;
}
.aside-menu :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.08) !important;
}
.aside-menu :deep(.el-menu-item.is-active) {
  background: var(--bnb-primary) !important;
}
.console-header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 6px rgba(43, 51, 62, 0.06);
  padding: 0 24px;
}
.header-title {
  font-size: 16px;
  font-weight: 600;
}
.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}
.user-trigger {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  outline: none;
  font-size: 14px;
}
.console-main {
  background: var(--bnb-bg);
  padding: 20px;
}
</style>
