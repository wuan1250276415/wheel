<template>
  <div class="dashboard">
    <h1>欢迎使用情侣转盘管理后台</h1>

    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="待审核内容" :value="pendingCount.totalPending" />
          <div class="stat-footer">
            <span class="label">SVIP: {{ pendingCount.svipPending }}</span>
            <span class="label">VIP: {{ pendingCount.vipPending }}</span>
            <span class="label">普通: {{ pendingCount.normalPending }}</span>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="会员套餐数" :value="0" />
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="活跃会员" :value="0" />
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic title="今日订单" :value="0" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>快捷操作</span>
          </template>
          <div class="quick-actions">
            <el-button type="primary" @click="$router.push('/audit/queue')" v-permission="'audit:queue:view'">
              <el-icon><Checked /></el-icon>
              审核队列
            </el-button>
            <el-button type="success" @click="$router.push('/membership/plans')" v-permission="'membership:plan:view'">
              <el-icon><Grid /></el-icon>
              套餐管理
            </el-button>
            <el-button type="warning" @click="$router.push('/membership/orders')" v-permission="'membership:order:view'">
              <el-icon><Tickets /></el-icon>
              订单管理
            </el-button>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <span>系统信息</span>
          </template>
          <div class="system-info">
            <p><strong>管理员：</strong>{{ authStore.adminInfo?.realName || authStore.adminInfo?.username }}</p>
            <p><strong>角色：</strong>{{ authStore.roles.join(', ') }}</p>
            <p><strong>权限数量：</strong>{{ authStore.permissions.length }}</p>
            <p><strong>最后登录：</strong>{{ authStore.adminInfo?.lastLoginTime || '暂无' }}</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { getPendingCount } from '@/api/audit'

const authStore = useAuthStore()

const pendingCount = ref({
  svipPending: 0,
  vipPending: 0,
  normalPending: 0,
  totalPending: 0
})

onMounted(async () => {
  try {
    const res = await getPendingCount()
    pendingCount.value = res.data
  } catch (error) {
    console.error('获取待审核数量失败:', error)
  }
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

h1 {
  margin-bottom: 30px;
  color: #333;
}

.stat-card {
  text-align: center;
}

.stat-footer {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-around;
  font-size: 12px;
  color: #666;
}

.quick-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.system-info p {
  margin: 12px 0;
  line-height: 1.8;
}
</style>
