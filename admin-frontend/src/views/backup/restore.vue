<template>
  <div class="backup-restore">
    <div class="page-header">
      <h2>恢复管理</h2>
    </div>

    <!-- 上传恢复 -->
    <el-card class="upload-card" shadow="never">
      <template #header>
        <span>从文件恢复</span>
      </template>
      
      <el-alert type="warning" :closable="false" show-icon style="margin-bottom: 20px;">
        <template #title>
          <p><strong>警告：</strong>恢复操作将覆盖当前数据库数据！请确保您了解此操作的后果。</p>
          <p>系统会在恢复前自动创建备份，以便在恢复失败时回滚。</p>
        </template>
      </el-alert>

      <el-upload
        ref="uploadRef"
        class="upload-area"
        drag
        :auto-upload="false"
        :limit="1"
        :on-change="handleFileChange"
        :on-exceed="handleExceed"
        accept=".sql,.gz,.enc"
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">
          将备份文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            支持 .sql、.gz、.enc 格式的备份文件
          </div>
        </template>
      </el-upload>

      <div v-if="selectedFile" class="selected-file">
        <p><strong>已选择文件：</strong>{{ selectedFile.name }}</p>
        <p><strong>文件大小：</strong>{{ formatFileSize(selectedFile.size) }}</p>
        <el-button type="danger" :loading="restoreLoading" @click="confirmUploadRestore">
          开始恢复
        </el-button>
      </div>
    </el-card>

    <!-- 恢复历史（占位） -->
    <el-card shadow="never" style="margin-top: 20px;">
      <template #header>
        <span>恢复历史</span>
      </template>
      
      <el-table :data="restoreHistory" v-loading="historyLoading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="sourceFilename" label="源文件" min-width="200" show-overflow-tooltip />
        <el-table-column prop="statusName" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">{{ row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="durationMs" label="耗时" width="100">
          <template #default="{ row }">
            {{ formatDuration(row.durationMs) }}
          </template>
        </el-table-column>
        <el-table-column prop="createdByName" label="操作人" width="120" />
        <el-table-column prop="createdTime" label="恢复时间" width="170" />
        <el-table-column prop="errorMessage" label="错误信息" min-width="200" show-overflow-tooltip />
      </el-table>

      <el-empty v-if="restoreHistory.length === 0 && !historyLoading" description="暂无恢复记录" />
    </el-card>

    <!-- 进度对话框 -->
    <el-dialog v-model="progressDialogVisible" title="恢复进度" width="400px" :close-on-click-modal="false">
      <div class="progress-content">
        <el-progress :percentage="progressPercent" :status="progressStatus" :stroke-width="20" />
        <p class="progress-stage">{{ progressStage }}</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import type { UploadFile, UploadInstance } from 'element-plus'
import type { RestoreRecord } from '@/types/backup'
import { restoreFromUpload, getRestoreProgress } from '@/api/backup'

const uploadRef = ref<UploadInstance>()
const selectedFile = ref<File | null>(null)
const restoreLoading = ref(false)

// 恢复历史
const historyLoading = ref(false)
const restoreHistory = ref<RestoreRecord[]>([])

// 进度
const progressDialogVisible = ref(false)
const progressPercent = ref(0)
const progressStage = ref('')
const progressStatus = ref<'' | 'success' | 'exception'>('')

function handleFileChange(file: UploadFile) {
  if (file.raw) {
    selectedFile.value = file.raw
  }
}

function handleExceed() {
  ElMessage.warning('只能上传一个文件')
}

function formatFileSize(bytes: number): string {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

function formatDuration(ms: number): string {
  if (!ms) return '-'
  if (ms < 1000) return ms + ' ms'
  if (ms < 60000) return (ms / 1000).toFixed(1) + ' s'
  return (ms / 60000).toFixed(1) + ' min'
}

function getStatusTagType(status: number): string {
  const map: Record<number, string> = { 0: '', 1: 'success', 2: 'danger', 3: 'warning' }
  return map[status] || ''
}

async function confirmUploadRestore() {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择备份文件')
    return
  }

  restoreLoading.value = true
  try {
    const res = await restoreFromUpload(selectedFile.value)
    if (res.code === 200) {
      ElMessage.success('恢复任务已启动')
      
      // 显示进度
      progressPercent.value = 0
      progressStage.value = '验证中...'
      progressStatus.value = ''
      progressDialogVisible.value = true
      
      // 轮询进度
      pollProgress(res.data.restoreId)
      
      // 清除文件
      selectedFile.value = null
      uploadRef.value?.clearFiles()
    } else {
      ElMessage.error(res.message || '恢复失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '恢复失败')
  } finally {
    restoreLoading.value = false
  }
}

async function pollProgress(restoreId: number) {
  const maxAttempts = 120
  let attempts = 0
  
  const poll = async () => {
    if (attempts >= maxAttempts) {
      progressStatus.value = 'exception'
      progressStage.value = '超时'
      return
    }
    
    try {
      const res = await getRestoreProgress(restoreId)
      if (res.code === 200) {
        progressPercent.value = res.data.progress
        progressStage.value = res.data.stage
        
        if (res.data.progress >= 100) {
          progressStatus.value = 'success'
          setTimeout(() => {
            progressDialogVisible.value = false
            // 刷新历史
          }, 1000)
          return
        }
      }
    } catch (error) {
      // 继续轮询
    }
    
    attempts++
    setTimeout(poll, 2000)
  }
  
  poll()
}

onMounted(() => {
  // 可以加载恢复历史
})
</script>

<style scoped>
.backup-restore {
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

.upload-card :deep(.el-card__body) {
  padding: 20px;
}

.upload-area {
  width: 100%;
}

.upload-area :deep(.el-upload-dragger) {
  width: 100%;
}

.selected-file {
  margin-top: 20px;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.selected-file p {
  margin: 8px 0;
}

.progress-content {
  text-align: center;
  padding: 20px 0;
}

.progress-stage {
  margin-top: 16px;
  color: #606266;
}
</style>
