import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

/**
 * 路由表：后续任务将 Placeholder 占位页逐步替换为真实页面
 */
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/',
    component: () => import('@/layout/PortalLayout.vue'),
    children: [
      { path: '', name: 'Home', component: () => import('@/views/portal/Home.vue'), meta: { title: '首页' } },
      {
        path: 'homestays',
        name: 'HomestayList',
        component: () => import('@/views/portal/HomestayList.vue'),
        meta: { title: '找民宿' }
      },
      {
        path: 'homestay/:id',
        name: 'HomestayDetail',
        component: () => import('@/views/portal/HomestayDetail.vue'),
        meta: { title: '民宿详情' }
      },
      {
        path: 'booking/confirm/:id',
        name: 'BookingConfirm',
        component: () => import('@/views/portal/BookingConfirm.vue'),
        meta: { title: '预订确认', requiresAuth: true }
      },
      {
        path: 'orders/cashier/:id',
        name: 'Cashier',
        component: () => import('@/views/portal/Cashier.vue'),
        meta: { title: '订单支付', requiresAuth: true }
      },
      {
        path: 'orders',
        name: 'MyOrders',
        component: () => import('@/views/portal/Orders.vue'),
        meta: { title: '我的订单', requiresAuth: true }
      },
      {
        path: 'favorites',
        component: () => import('@/views/portal/Favorites.vue'),
        meta: { title: '我的收藏', requiresAuth: true }
      },
      {
        path: 'coupons',
        component: () => import('@/views/portal/Coupons.vue'),
        meta: { title: '优惠券中心', requiresAuth: true }
      },
      {
        path: 'profile',
        component: () => import('@/views/portal/Profile.vue'),
        meta: { title: '个人中心', requiresAuth: true }
      },
      {
        path: 'notices',
        component: () => import('@/views/portal/Notices.vue'),
        meta: { title: '平台公告' }
      },
      {
        path: 'notices/:id',
        component: () => import('@/views/portal/NoticeDetail.vue'),
        meta: { title: '公告详情' }
      }
    ]
  },
  {
    path: '/host',
    component: () => import('@/layout/ConsoleLayout.vue'),
    meta: { requiresAuth: true, roles: ['HOST'] },
    children: [
      { path: '', component: () => import('@/views/host/Dashboard.vue'), meta: { title: '经营看板', console: 'host' } },
      { path: 'homestays', component: () => import('@/views/host/Homestays.vue'), meta: { title: '房源管理', console: 'host' } },
      { path: 'homestay/new', component: () => import('@/views/host/HomestayEdit.vue'), meta: { title: '发布房源', console: 'host' } },
      { path: 'homestay/edit/:id', component: () => import('@/views/host/HomestayEdit.vue'), meta: { title: '编辑房源', console: 'host' } },
      { path: 'orders', component: () => import('@/views/host/Orders.vue'), meta: { title: '订单管理', console: 'host' } },
      { path: 'reviews', component: () => import('@/views/host/Reviews.vue'), meta: { title: '评价管理', console: 'host' } },
      { path: 'profile', component: () => import('@/views/portal/Profile.vue'), meta: { title: '个人设置', console: 'host' } }
    ]
  },
  {
    path: '/admin',
    component: () => import('@/layout/ConsoleLayout.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN'] },
    children: [
      { path: '', component: () => import('@/views/admin/Dashboard.vue'), meta: { title: '平台看板', console: 'admin' } },
      { path: 'users', component: () => import('@/views/admin/Users.vue'), meta: { title: '用户管理', console: 'admin' } },
      { path: 'host-audit', component: () => import('@/views/admin/HostAudit.vue'), meta: { title: '房东审核', console: 'admin' } },
      { path: 'homestay-audit', component: () => import('@/views/admin/HomestayAudit.vue'), meta: { title: '房源审核', console: 'admin' } },
      { path: 'homestays', component: () => import('@/views/admin/Homestays.vue'), meta: { title: '房源管理', console: 'admin' } },
      { path: 'orders', component: () => import('@/views/admin/Orders.vue'), meta: { title: '订单管理', console: 'admin' } },
      { path: 'reviews', component: () => import('@/views/admin/Reviews.vue'), meta: { title: '评价管理', console: 'admin' } },
      { path: 'banners', component: () => import('@/views/admin/Banners.vue'), meta: { title: '轮播管理', console: 'admin' } },
      { path: 'notices', component: () => import('@/views/admin/Notices.vue'), meta: { title: '公告管理', console: 'admin' } },
      { path: 'coupons', component: () => import('@/views/admin/Coupons.vue'), meta: { title: '优惠券管理', console: 'admin' } },
      { path: 'facilities', component: () => import('@/views/admin/Facilities.vue'), meta: { title: '设施字典', console: 'admin' } }
    ]
  },
  { path: '/403', name: 'Forbidden', component: () => import('@/views/error/403.vue'), meta: { title: '无权限' } },
  { path: '/:pathMatch(.*)*', name: 'NotFound', component: () => import('@/views/error/404.vue'), meta: { title: '页面不存在' } }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

const WHITE_LIST = ['/login', '/register', '/403']

router.beforeEach(async (to) => {
  document.title = to.meta.title ? `${to.meta.title} · 栖迟民宿` : '栖迟民宿'
  const userStore = useUserStore()

  if (userStore.token && !userStore.userInfo) {
    try {
      await userStore.fetchProfile()
    } catch (e) {
      userStore.token = ''
      localStorage.removeItem('bnbms_token')
    }
  }

  if (to.meta.requiresAuth && !userStore.token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  if (['/login', '/register'].includes(to.path) && userStore.token) {
    const role = userStore.role
    if (role === 'ADMIN') return { path: '/admin' }
    if (role === 'HOST') return { path: '/host' }
    return { path: '/' }
  }

  const roles = to.matched.reduce((acc, r) => (r.meta.roles ? acc.concat(r.meta.roles) : acc), [])
  if (roles.length && userStore.userInfo && !roles.includes(userStore.role)) {
    return { path: '/403' }
  }
  return true
})

export default router
