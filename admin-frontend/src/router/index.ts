import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { getToken } from '@/utils/auth'
import { useAuthStore } from '@/stores/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '仪表盘', icon: 'DataLine' }
      },
      {
        path: 'membership',
        name: 'Membership',
        meta: { title: '会员管理', icon: 'UserFilled' },
        children: [
          {
            path: 'plans',
            name: 'MembershipPlans',
            component: () => import('@/views/membership/plan/index.vue'),
            meta: { title: '套餐管理', permission: 'membership:plan:view' }
          },
          {
            path: 'users',
            name: 'MembershipUsers',
            component: () => import('@/views/membership/user/index.vue'),
            meta: { title: '用户会员', permission: 'membership:user:view' }
          },
          {
            path: 'orders',
            name: 'MembershipOrders',
            component: () => import('@/views/membership/order/index.vue'),
            meta: { title: '订单管理', permission: 'membership:order:view' }
          }
        ]
      },
      {
        path: 'audit',
        name: 'Audit',
        meta: { title: '内容审核', icon: 'Checked' },
        children: [
          {
            path: 'queue',
            name: 'AuditQueue',
            component: () => import('@/views/audit/queue.vue'),
            meta: { title: '审核队列', permission: 'audit:queue:view' }
          },
          {
            path: 'history',
            name: 'AuditHistory',
            component: () => import('@/views/audit/history.vue'),
            meta: { title: '审核历史', permission: 'audit:history:view' }
          }
        ]
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = getToken()

  if (to.meta.requiresAuth !== false) {
    // 需要登录
    if (!token) {
      next({ path: '/login', query: { redirect: to.fullPath } })
      return
    }

    // 检查权限
    if (to.meta.permission) {
      const authStore = useAuthStore()
      const hasPermission = authStore.permissions.includes(to.meta.permission as string)

      if (!hasPermission) {
        ElMessage.error('权限不足')
        next(false)
        return
      }
    }
  } else {
    // 已登录用户访问登录页，跳转到首页
    if (token && to.path === '/login') {
      next('/dashboard')
      return
    }
  }

  next()
})

export default router
