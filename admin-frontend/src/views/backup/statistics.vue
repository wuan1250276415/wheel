<template>
  <div class="backup-statistics">
    <div class="page-header">
      <h2>备份统计</h2>
      <el-button @click="fetchStatistics" :loading="loading">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
            <el-icon><Document /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics?.totalBackupCount || 0 }}</div>
            <div class="stat-label">总备份数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);">
            <el-icon><SuccessFilled /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ formatPercent(statistics?.successRate) }}</div>
            <div class="stat-label">成功率</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
            <el-icon><Folder /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics?.totalStorageUsedFormatted || '0 B' }}</div>
            <div class="stat-label">存储使用</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card" :class="healthClass">
          <div class="stat-icon" :style="healthIconStyle">
            <el-icon><component :is="healthIcon" /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ healthStatusLabel }}</div>
            <div class="stat-label">健康状态</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 详细信息 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span>备份概览</span>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="最后备份时间">
              {{ statistics?.lastBackupTime || '无' }}
            </el-descriptions-item>
            <el-descriptions-item label="最后备份大小">
              {{ formatSize(statistics?.lastBackupSize) }}
            </el-descriptions-item>
            <el-descriptions-item label="成功备份">
              {{ statistics?.successCount || 0 }} 次
            </el-descriptions-item>
            <el-descriptions-item label="失败备份">
              {{ statistics?.failureCount || 0 }} 次
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span>健康检查</span>
          </template>
          <el-alert
            :type="healthAlertType"
            :title="statistics?.healthMessage || '系统运行正常'"
            :closable="false"
            show-icon
          />
          <div class="health-tips" style="margin-top: 16px;">
            <p><strong>健康检查说明：</strong></p>
            <ul>
              <li><el-tag type="success" size="small">健康</el-tag> 备份正常运行，无告警</li>
              <li><el-tag type="warning" size="small">警告</el-tag> 存在潜在问题，需关注</li>
              <li><el-tag type="danger" size="small">危险</el-tag> 存在严重问题，需立即处理</li>
            </ul>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Refresh, 
  Document, 
  SuccessFilled, 
  Folder, 
  CircleCheckFilled, 
  WarningFilled, 
  CircleCloseFilled 
} from '@element-plus/icons-vue'
import type { BackupStatistics } from '@/types/backup'
import { getBackupStatistics } from '@/api/backup'

const loading = ref(false)
const statistics = ref<BackupStatistics | null>(null)

async function fetchStatistics() {
  loading.value = true
  try {
    const res = await getBackupStatistics()
    if (res.code === 200) {
      statistics.value = res.data
    } else {
      ElMessage.error(res.message || '获取统计失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '获取统计失败')
  } finally {
    loading.value = false
  }
}

function formatPercent(rate?: number): string {
  if (rate === undefined || rate === null) return '0%'
  return (rate * 100).toFixed(1) + '%'
}

function formatSize(bytes?: number): string {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const healthClass = computed(() => {
  const status = statistics.value?.healthStatus
  if (status === 'CRITICAL') return 'health-critical'
  if (status === 'WARNING') return 'health-warning'
  return 'health-healthy'
})

const healthIconStyle = computed(() => {
  const status = statistics.value?.healthStatus
  if (status === 'CRITICAL') return 'background: linear-gradient(135deg, #ff416c 0%, #ff4b2b 100%);'
  if (status === 'WARNING') return 'background: linear-gradient(135deg, #f7971e 0%, #ffd200 100%);'
  return 'background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);'
})

const healthIcon = computed(() => {
  const status = statistics.value?.healthStatus
  if (status === 'CRITICAL') return CircleCloseFilled
  if (status === 'WARNING') return WarningFilled
  return CircleCheckFilled
})

const healthStatusLabel = computed(() => {
  const status = statistics.value?.healthStatus
  if (status === 'CRITICAL') return '危险'
  if (status === 'WARNING') return '警告'
  return '健康'
})

const healthAlertType = computed(() => {
  const status = statistics.value?.healthStatus
  if (status === 'CRITICAL') return 'error'
  if (status === 'WARNING') return 'warning'
  return 'success'
})

onMounted(() => {
  fetchStatistics()
})
</script>

<style scoped>
.backup-statistics {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.stat-cards {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
}

.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  width: 100%;
  padding: 0;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 28px;
  flex-shrink: 0;
}

.stat-content {
  margin-left: 16px;
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.health-tips ul {
  margin: 8px 0;
  padding-left: 20px;
}

.health-tips li {
  margin: 8px 0;
  color: #606266;
}
</style>
