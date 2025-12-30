<template>
  <div class="main-layout">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside :width="appStore.sidebarCollapsed ? '64px' : '200px'" class="sidebar">
        <div class="logo">
          <h2 v-if="!appStore.sidebarCollapsed">情侣转盘</h2>
          <h2 v-else>转</h2>
        </div>
        <el-menu
          :default-active="activeMenu"
          :collapse="appStore.sidebarCollapsed"
          router
        >
          <el-menu-item index="/dashboard">
            <el-icon><DataLine /></el-icon>
            <template #title>仪表盘</template>
          </el-menu-item>

          <el-sub-menu index="membership" v-if="hasPermission(['membership:plan:view', 'membership:user:view', 'membership:order:view'])">
            <template #title>
              <el-icon><UserFilled /></el-icon>
              <span>会员管理</span>
            </template>
            <el-menu-item index="/membership/plans" v-permission="'membership:plan:view'">套餐管理</el-menu-item>
            <el-menu-item index="/membership/users" v-permission="'membership:user:view'">用户会员</el-menu-item>
            <el-menu-item index="/membership/orders" v-permission="'membership:order:view'">订单管理</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="audit" v-if="hasPermission(['audit:queue:view', 'audit:history:view'])">
            <template #title>
              <el-icon><Checked /></el-icon>
              <span>内容审核</span>
            </template>
            <el-menu-item index="/audit/queue" v-permission="'audit:queue:view'">审核队列</el-menu-item>
            <el-menu-item index="/audit/history" v-permission="'audit:history:view'">审核历史</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-aside>

      <el-container>
        <!-- 顶部导航 -->
        <el-header class="header">
          <div class="header-left">
            <el-icon @click="appStore.toggleSidebar" class="toggle-icon">
              <Fold v-if="!appStore.sidebarCollapsed" />
              <Expand v-else />
            </el-icon>
          </div>
          <div class="header-right">
            <el-dropdown>
              <span class="user-info">
                <el-avatar :size="32" :src="authStore.adminInfo?.avatarUrl" />
                <span class="username">{{ authStore.adminInfo?.realName || authStore.adminInfo?.username }}</span>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="handleLogout">
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>

        <!-- 主内容区 -->
        <el-main class="main-content">
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { useAppStore } from '@/stores/app'
import { hasPermission } from '@/utils/permission'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const appStore = useAppStore()

const activeMenu = computed(() => route.path)

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await authStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  } catch (error) {
    // 用户取消
  }
}
</script>

<style scoped>
.main-layout {
  width: 100%;
  height: 100vh;
}

.sidebar {
  background-color: #001529;
  transition: width 0.3s;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  border-bottom: 1px solid #1a1a1a;
}

.el-menu {
  border-right: none;
  background-color: #001529;
}

:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  color: rgba(255, 255, 255, 0.65);
}

:deep(.el-menu-item:hover),
:deep(.el-sub-menu__title:hover) {
  background-color: rgba(255, 255, 255, 0.08);
  color: #fff;
}

:deep(.el-menu-item.is-active) {
  background-color: #1890ff !important;
  color: #fff;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #fff;
  border-bottom: 1px solid #f0f0f0;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
}

.toggle-icon {
  font-size: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.toggle-icon:hover {
  color: #1890ff;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.username {
  font-size: 14px;
}

.main-content {
  background-color: #f0f2f5;
  padding: 20px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
