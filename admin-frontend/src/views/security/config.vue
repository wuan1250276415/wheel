<template>
  <div class="audit-config-page">
    <!-- AI审核阈值配置 -->
    <el-card class="config-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>AI审核阈值配置</span>
          <el-tag type="info" size="small">风险分范围: 0-100</el-tag>
        </div>
      </template>

      <el-form :model="thresholdConfig" label-width="180px" class="config-form">
        <el-form-item label="自动通过阈值">
          <el-slider
            v-model="thresholdConfig.ai_pass_threshold"
            :min="0"
            :max="100"
            :marks="thresholdMarks"
            show-input
            style="width: 400px"
          />
          <div class="config-tip">
            风险分低于此值的内容将自动通过审核
          </div>
        </el-form-item>

        <el-form-item label="自动拒绝阈值">
          <el-slider
            v-model="thresholdConfig.ai_reject_threshold"
            :min="0"
            :max="100"
            :marks="thresholdMarks"
            show-input
            style="width: 400px"
          />
          <div class="config-tip">
            风险分高于此值的内容将自动拒绝
          </div>
        </el-form-item>

        <el-form-item>
          <div class="threshold-preview">
            <div class="preview-bar">
              <div class="zone pass" :style="{ width: thresholdConfig.ai_pass_threshold + '%' }">
                自动通过
              </div>
              <div
                class="zone review"
                :style="{
                  width: (thresholdConfig.ai_reject_threshold - thresholdConfig.ai_pass_threshold) + '%'
                }"
              >
                人工复审
              </div>
              <div class="zone reject" :style="{ width: (100 - thresholdConfig.ai_reject_threshold) + '%' }">
                自动拒绝
              </div>
            </div>
            <div class="preview-labels">
              <span>0</span>
              <span>{{ thresholdConfig.ai_pass_threshold }}</span>
              <span>{{ thresholdConfig.ai_reject_threshold }}</span>
              <span>100</span>
            </div>
          </div>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 举报优先级配置 -->
    <el-card class="config-card" shadow="never">
      <template #header>
        <span>举报优先级配置</span>
      </template>

      <el-form :model="reportConfig" label-width="180px" class="config-form">
        <el-form-item label="优先级提升阈值">
          <el-input-number
            v-model="reportConfig.report_priority_threshold"
            :min="1"
            :max="100"
          />
          <span class="config-unit">次举报</span>
          <div class="config-tip">
            同一内容被举报达到此次数时，自动提升审核优先级
          </div>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- VIP审核优先级配置 -->
    <el-card class="config-card" shadow="never">
      <template #header>
        <span>VIP审核优先级配置</span>
      </template>

      <el-form :model="vipConfig" label-width="180px" class="config-form">
        <el-form-item label="VIP用户优先级">
          <el-select v-model="vipConfig.vip_audit_priority" style="width: 200px">
            <el-option label="低优先级" :value="1" />
            <el-option label="中优先级" :value="2" />
            <el-option label="高优先级" :value="3" />
          </el-select>
          <div class="config-tip">
            VIP用户提交内容的审核优先级
          </div>
        </el-form-item>

        <el-form-item label="SVIP用户优先级">
          <el-select v-model="vipConfig.svip_audit_priority" style="width: 200px">
            <el-option label="低优先级" :value="1" />
            <el-option label="中优先级" :value="2" />
            <el-option label="高优先级" :value="3" />
          </el-select>
          <div class="config-tip">
            SVIP用户提交内容的审核优先级
          </div>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- AI服务配置 -->
    <el-card class="config-card" shadow="never">
      <template #header>
        <span>AI审核服务配置</span>
      </template>

      <el-form :model="aiServiceConfig" label-width="180px" class="config-form">
        <el-form-item label="主服务提供商">
          <el-select v-model="aiServiceConfig.primary_provider" style="width: 200px">
            <el-option label="阿里云内容安全" value="aliyun" />
            <el-option label="腾讯云天御" value="tencent" />
          </el-select>
          <div class="config-tip">
            优先使用的AI审核服务
          </div>
        </el-form-item>

        <el-form-item label="服务降级策略">
          <el-select v-model="aiServiceConfig.fallback_strategy" style="width: 200px">
            <el-option label="降级为本地敏感词过滤" value="local" />
            <el-option label="切换备用服务" value="backup" />
            <el-option label="进入人工审核队列" value="manual" />
          </el-select>
          <div class="config-tip">
            AI服务不可用时的处理策略
          </div>
        </el-form-item>

        <el-form-item label="超时时间">
          <el-input-number
            v-model="aiServiceConfig.timeout"
            :min="1000"
            :max="30000"
            :step="1000"
          />
          <span class="config-unit">毫秒</span>
          <div class="config-tip">
            AI服务调用超时时间
          </div>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 保存按钮 -->
    <div class="save-actions">
      <el-button type="primary" size="large" :loading="saving" @click="handleSave">
        保存配置
      </el-button>
      <el-button size="large" @click="handleReset">
        重置为默认值
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { AuditConfig } from '@/types/security'
import { getAuditConfigList, batchUpdateAuditConfig } from '@/api/security'

// 阈值配置
const thresholdConfig = reactive({
  ai_pass_threshold: 30,
  ai_reject_threshold: 80
})

// 举报配置
const reportConfig = reactive({
  report_priority_threshold: 3
})

// VIP配置
const vipConfig = reactive({
  vip_audit_priority: 2,
  svip_audit_priority: 3
})

// AI服务配置
const aiServiceConfig = reactive({
  primary_provider: 'aliyun',
  fallback_strategy: 'local',
  timeout: 5000
})

// 阈值标记
const thresholdMarks = {
  0: '0',
  30: '30',
  50: '50',
  80: '80',
  100: '100'
}

// 保存状态
const saving = ref(false)

// 原始配置列表
const configList = ref<AuditConfig[]>([])

// 加载配置
async function loadConfig() {
  try {
    const res = await getAuditConfigList()
    configList.value = res.data

    // 解析配置值
    res.data.forEach((config: AuditConfig) => {
      switch (config.configKey) {
        case 'ai_pass_threshold':
          thresholdConfig.ai_pass_threshold = parseInt(config.configValue)
          break
        case 'ai_reject_threshold':
          thresholdConfig.ai_reject_threshold = parseInt(config.configValue)
          break
        case 'report_priority_threshold':
          reportConfig.report_priority_threshold = parseInt(config.configValue)
          break
        case 'vip_audit_priority':
          vipConfig.vip_audit_priority = parseInt(config.configValue)
          break
        case 'svip_audit_priority':
          vipConfig.svip_audit_priority = parseInt(config.configValue)
          break
        case 'primary_provider':
          aiServiceConfig.primary_provider = config.configValue
          break
        case 'fallback_strategy':
          aiServiceConfig.fallback_strategy = config.configValue
          break
        case 'timeout':
          aiServiceConfig.timeout = parseInt(config.configValue)
          break
      }
    })
  } catch (error) {
    ElMessage.error('加载配置失败')
  }
}

// 保存配置
async function handleSave() {
  // 验证阈值
  if (thresholdConfig.ai_pass_threshold >= thresholdConfig.ai_reject_threshold) {
    ElMessage.warning('自动通过阈值必须小于自动拒绝阈值')
    return
  }

  saving.value = true
  try {
    const configs = [
      { configKey: 'ai_pass_threshold', configValue: String(thresholdConfig.ai_pass_threshold) },
      { configKey: 'ai_reject_threshold', configValue: String(thresholdConfig.ai_reject_threshold) },
      { configKey: 'report_priority_threshold', configValue: String(reportConfig.report_priority_threshold) },
      { configKey: 'vip_audit_priority', configValue: String(vipConfig.vip_audit_priority) },
      { configKey: 'svip_audit_priority', configValue: String(vipConfig.svip_audit_priority) },
      { configKey: 'primary_provider', configValue: aiServiceConfig.primary_provider },
      { configKey: 'fallback_strategy', configValue: aiServiceConfig.fallback_strategy },
      { configKey: 'timeout', configValue: String(aiServiceConfig.timeout) }
    ]

    await batchUpdateAuditConfig(configs)
    ElMessage.success('配置保存成功')
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

// 重置为默认值
function handleReset() {
  ElMessageBox.confirm('确定要重置所有配置为默认值吗？', '重置确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    thresholdConfig.ai_pass_threshold = 30
    thresholdConfig.ai_reject_threshold = 80
    reportConfig.report_priority_threshold = 3
    vipConfig.vip_audit_priority = 2
    vipConfig.svip_audit_priority = 3
    aiServiceConfig.primary_provider = 'aliyun'
    aiServiceConfig.fallback_strategy = 'local'
    aiServiceConfig.timeout = 5000
    ElMessage.success('已重置为默认值，请点击保存生效')
  }).catch(() => {})
}

onMounted(() => {
  loadConfig()
})
</script>

<style scoped lang="scss">
.audit-config-page {
  .config-card {
    margin-bottom: 16px;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }

  .config-form {
    max-width: 800px;

    .config-tip {
      font-size: 12px;
      color: #909399;
      margin-top: 4px;
    }

    .config-unit {
      margin-left: 8px;
      color: #606266;
    }
  }

  .threshold-preview {
    margin-top: 20px;
    width: 100%;
    max-width: 500px;

    .preview-bar {
      display: flex;
      height: 30px;
      border-radius: 4px;
      overflow: hidden;

      .zone {
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 12px;
        color: #fff;
        transition: width 0.3s;

        &.pass {
          background: #67c23a;
        }

        &.review {
          background: #e6a23c;
        }

        &.reject {
          background: #f56c6c;
        }
      }
    }

    .preview-labels {
      display: flex;
      justify-content: space-between;
      margin-top: 4px;
      font-size: 12px;
      color: #909399;
    }
  }

  .save-actions {
    margin-top: 24px;
    padding: 16px;
    background: #fff;
    border-radius: 4px;
    text-align: center;
  }
}
</style>
