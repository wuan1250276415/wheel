<template>
  <div class="backup-config">
    <div class="page-header">
      <h2>备份配置</h2>
    </div>

    <el-card shadow="never" v-loading="loading">
      <el-form :model="configForm" label-width="160px" style="max-width: 600px;">
        <!-- 定时任务配置 -->
        <el-divider content-position="left">定时任务</el-divider>
        
        <el-form-item label="每日备份时间">
          <el-input v-model="configForm.dailyCron" placeholder="Cron 表达式，如: 0 0 2 * * ?">
            <template #append>
              <el-tooltip :content="config?.dailyCronDescription || ''" placement="top">
                <el-icon><InfoFilled /></el-icon>
              </el-tooltip>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="每周备份时间">
          <el-input v-model="configForm.weeklyCron" placeholder="Cron 表达式，如: 0 0 3 ? * SUN">
            <template #append>
              <el-tooltip :content="config?.weeklyCronDescription || ''" placement="top">
                <el-icon><InfoFilled /></el-icon>
              </el-tooltip>
            </template>
          </el-input>
        </el-form-item>

        <!-- 保留策略 -->
        <el-divider content-position="left">保留策略</el-divider>

        <el-form-item label="保留天数">
          <el-input-number v-model="configForm.retentionDays" :min="1" :max="365" />
          <span class="form-tip">超过此天数的备份将被自动清理</span>
        </el-form-item>

        <el-form-item label="最少保留数量">
          <el-input-number v-model="configForm.minBackupCount" :min="1" :max="100" />
          <span class="form-tip">无论天数如何，至少保留这么多备份</span>
        </el-form-item>

        <!-- 存储配置 -->
        <el-divider content-position="left">存储配置</el-divider>

        <el-form-item label="存储路径">
          <el-input v-model="configForm.storagePath" placeholder="/data/backups" />
        </el-form-item>

        <el-form-item label="启用加密">
          <el-switch v-model="configForm.encryptionEnabled" />
          <span class="form-tip">使用 AES-256 加密备份文件</span>
        </el-form-item>

        <!-- 告警配置 -->
        <el-divider content-position="left">告警配置</el-divider>

        <el-form-item label="无备份告警阈值">
          <el-input-number v-model="configForm.alertThresholdHours" :min="1" :max="168" />
          <span class="form-tip">超过此小时数无成功备份将发送告警</span>
        </el-form-item>

        <el-form-item label="存储告警阈值">
          <el-input-number v-model="configForm.storageWarningThresholdGb" :min="1" :max="1000" />
          <span class="form-tip">GB，超过此使用量将发送告警</span>
        </el-form-item>

        <!-- 操作按钮 -->
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave">保存配置</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 配置信息 -->
      <div v-if="config?.lastUpdatedTime" class="config-info">
        <p>最后更新：{{ config.lastUpdatedTime }}</p>
        <p v-if="config.lastUpdatedBy">更新人：{{ config.lastUpdatedBy }}</p>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { InfoFilled } from '@element-plus/icons-vue'
import type { BackupConfig, BackupConfigDTO } from '@/types/backup'
import { getBackupConfig, updateBackupConfig } from '@/api/backup'

const loading = ref(false)
const saving = ref(false)
const config = ref<BackupConfig | null>(null)

const configForm = reactive<BackupConfigDTO>({
  dailyCron: '',
  weeklyCron: '',
  retentionDays: 30,
  minBackupCount: 7,
  storagePath: '',
  encryptionEnabled: true,
  alertThresholdHours: 48,
  storageWarningThresholdGb: 50
})

async function fetchConfig() {
  loading.value = true
  try {
    const res = await getBackupConfig()
    if (res.code === 200) {
      config.value = res.data
      // 填充表单
      configForm.dailyCron = res.data.dailyCron
      configForm.weeklyCron = res.data.weeklyCron
      configForm.retentionDays = res.data.retentionDays
      configForm.minBackupCount = res.data.minBackupCount
      configForm.storagePath = res.data.storagePath
      configForm.encryptionEnabled = res.data.encryptionEnabled
      configForm.alertThresholdHours = res.data.alertThresholdHours
      configForm.storageWarningThresholdGb = res.data.storageWarningThresholdGb
    } else {
      ElMessage.error(res.message || '获取配置失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '获取配置失败')
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  saving.value = true
  try {
    const res = await updateBackupConfig(configForm)
    if (res.code === 200) {
      ElMessage.success('配置保存成功')
      fetchConfig()
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

function handleReset() {
  if (config.value) {
    configForm.dailyCron = config.value.dailyCron
    configForm.weeklyCron = config.value.weeklyCron
    configForm.retentionDays = config.value.retentionDays
    configForm.minBackupCount = config.value.minBackupCount
    configForm.storagePath = config.value.storagePath
    configForm.encryptionEnabled = config.value.encryptionEnabled
    configForm.alertThresholdHours = config.value.alertThresholdHours
    configForm.storageWarningThresholdGb = config.value.storageWarningThresholdGb
  }
}

onMounted(() => {
  fetchConfig()
})
</script>

<style scoped>
.backup-config {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.form-tip {
  margin-left: 12px;
  color: #909399;
  font-size: 12px;
}

.config-info {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
  color: #909399;
  font-size: 12px;
}

.config-info p {
  margin: 4px 0;
}
</style>
