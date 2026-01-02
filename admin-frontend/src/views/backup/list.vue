<template>
  <div class="backup-list">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>备份管理</h2>
      <el-button type="primary" :loading="triggerLoading" @click="handleTriggerBackup">
        <el-icon><Plus /></el-icon>
        手动备份
      </el-button>
    </div>

    <!-- 搜索筛选 -->
    <el-card class="filter-card" shadow="never">
      <el-form :model="queryForm" inline>
        <el-form-item label="备份类型">
          <el-select v-model="queryForm.backupType" placeholder="全部" clearable style="width: 120px">
            <el-option label="每日备份" :value="1" />
            <el-option label="每周备份" :value="2" />
            <el-option label="手动备份" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 100px">
            <el-option label="进行中" :value="0" />
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 备份列表表格 -->
    <el-card shadow="never">
      <el-table :data="backupList" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="backupTypeName" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.backupType)">{{ row.backupTypeName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="filename" label="文件名" min-width="200" show-overflow-tooltip />
        <el-table-column prop="fileSizeFormatted" label="大小" width="100" />
        <el-table-column prop="statusName" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">{{ row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="durationFormatted" label="耗时" width="100" />
        <el-table-column prop="isEncrypted" label="加密" width="70">
          <template #default="{ row }">
            <el-icon v-if="row.isEncrypted" color="#67C23A"><Lock /></el-icon>
            <el-icon v-else color="#909399"><Unlock /></el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="150" show-overflow-tooltip />
        <el-table-column prop="createdTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleDownload(row)" :disabled="row.status !== 1">
              下载
            </el-button>
            <el-button link type="success" @click="handleRestore(row)" :disabled="row.status !== 1">
              恢复
            </el-button>
            <el-popconfirm title="确定删除此备份吗？" @confirm="handleDelete(row)">
              <template #reference>
                <el-button link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryForm.pageNum"
          v-model:page-size="queryForm.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleQuery"
          @current-change="handleQuery"
        />
      </div>
    </el-card>

    <!-- 手动备份对话框 -->
    <el-dialog v-model="triggerDialogVisible" title="手动备份" width="500px">
      <el-form :model="triggerForm" label-width="80px">
        <el-form-item label="备份描述">
          <el-input
            v-model="triggerForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入备份描述（可选）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="triggerDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="triggerLoading" @click="confirmTriggerBackup">
          开始备份
        </el-button>
      </template>
    </el-dialog>

    <!-- 恢复确认对话框 -->
    <el-dialog v-model="restoreDialogVisible" title="恢复确认" width="500px">
      <el-alert type="warning" :closable="false" show-icon>
        <template #title>
          <p><strong>警告：</strong>恢复操作将覆盖当前数据库数据！</p>
          <p>系统会在恢复前自动创建备份，以便在恢复失败时回滚。</p>
        </template>
      </el-alert>
      <div v-if="selectedBackup" style="margin-top: 16px;">
        <p><strong>备份文件：</strong>{{ selectedBackup.filename }}</p>
        <p><strong>备份时间：</strong>{{ selectedBackup.createdTime }}</p>
        <p><strong>文件大小：</strong>{{ selectedBackup.fileSizeFormatted }}</p>
      </div>
      <template #footer>
        <el-button @click="restoreDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="restoreLoading" @click="confirmRestore">
          确认恢复
        </el-button>
      </template>
    </el-dialog>

    <!-- 进度对话框 -->
    <el-dialog v-model="progressDialogVisible" :title="progressTitle" width="400px" :close-on-click-modal="false">
      <div class="progress-content">
        <el-progress :percentage="progressPercent" :status="progressStatus" :stroke-width="20" />
        <p class="progress-stage">{{ progressStage }}</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Lock, Unlock } from '@element-plus/icons-vue'
import type { BackupRecord, BackupQueryDTO, ManualBackupDTO } from '@/types/backup'
import {
  listBackups,
  triggerBackup,
  downloadBackup,
  deleteBackup,
  restoreFromBackup,
  getBackupProgress
} from '@/api/backup'

// 查询表单
const queryForm = reactive<BackupQueryDTO>({
  pageNum: 1,
  pageSize: 10,
  backupType: undefined,
  status: undefined,
  startTime: undefined,
  endTime: undefined
})
const dateRange = ref<string[]>([])

// 列表数据
const loading = ref(false)
const backupList = ref<BackupRecord[]>([])
const total = ref(0)

// 手动备份
const triggerDialogVisible = ref(false)
const triggerLoading = ref(false)
const triggerForm = reactive<ManualBackupDTO>({
  description: ''
})

// 恢复
const restoreDialogVisible = ref(false)
const restoreLoading = ref(false)
const selectedBackup = ref<BackupRecord | null>(null)

// 进度
const progressDialogVisible = ref(false)
const progressTitle = ref('')
const progressPercent = ref(0)
const progressStage = ref('')
const progressStatus = ref<'' | 'success' | 'exception'>('')

// 获取备份列表
async function fetchBackups() {
  loading.value = true
  try {
    if (dateRange.value && dateRange.value.length === 2) {
      queryForm.startTime = dateRange.value[0]
      queryForm.endTime = dateRange.value[1]
    } else {
      queryForm.startTime = undefined
      queryForm.endTime = undefined
    }
    
    const res = await listBackups(queryForm)
    if (res.code === 200) {
      backupList.value = res.data.records
      total.value = res.data.total
    } else {
      ElMessage.error(res.message || '查询失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '查询失败')
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  queryForm.pageNum = 1
  fetchBackups()
}

function handleReset() {
  queryForm.backupType = undefined
  queryForm.status = undefined
  dateRange.value = []
  handleQuery()
}

// 类型标签颜色
function getTypeTagType(type: number) {
  const map: Record<number, string> = { 1: 'info', 2: 'warning', 3: 'primary' }
  return map[type] || 'info'
}

// 状态标签颜色
function getStatusTagType(status: number) {
  const map: Record<number, string> = { 0: '', 1: 'success', 2: 'danger' }
  return map[status] || ''
}

// 手动备份
function handleTriggerBackup() {
  triggerForm.description = ''
  triggerDialogVisible.value = true
}

async function confirmTriggerBackup() {
  triggerLoading.value = true
  try {
    const res = await triggerBackup(triggerForm)
    if (res.code === 200) {
      ElMessage.success('备份任务已启动')
      triggerDialogVisible.value = false
      
      // 显示进度
      progressTitle.value = '备份进度'
      progressPercent.value = 0
      progressStage.value = '准备中...'
      progressStatus.value = ''
      progressDialogVisible.value = true
      
      // 轮询进度
      pollProgress(res.data.backupId, 'backup')
    } else {
      ElMessage.error(res.message || '备份失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '备份失败')
  } finally {
    triggerLoading.value = false
  }
}

// 下载备份
async function handleDownload(row: BackupRecord) {
  try {
    const blob = await downloadBackup(row.id)
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = row.filename
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('下载成功')
  } catch (error: any) {
    ElMessage.error(error.message || '下载失败')
  }
}

// 删除备份
async function handleDelete(row: BackupRecord) {
  try {
    const res = await deleteBackup(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchBackups()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '删除失败')
  }
}

// 恢复
function handleRestore(row: BackupRecord) {
  selectedBackup.value = row
  restoreDialogVisible.value = true
}

async function confirmRestore() {
  if (!selectedBackup.value) return
  
  restoreLoading.value = true
  try {
    const res = await restoreFromBackup(selectedBackup.value.id)
    if (res.code === 200) {
      ElMessage.success('恢复任务已启动')
      restoreDialogVisible.value = false
      
      // 显示进度
      progressTitle.value = '恢复进度'
      progressPercent.value = 0
      progressStage.value = '验证中...'
      progressStatus.value = ''
      progressDialogVisible.value = true
      
      // 可以添加进度轮询
    } else {
      ElMessage.error(res.message || '恢复失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '恢复失败')
  } finally {
    restoreLoading.value = false
  }
}

// 轮询进度
async function pollProgress(id: number, type: 'backup' | 'restore') {
  const maxAttempts = 120
  let attempts = 0
  
  const poll = async () => {
    if (attempts >= maxAttempts) {
      progressStatus.value = 'exception'
      progressStage.value = '超时'
      return
    }
    
    try {
      const res = await getBackupProgress(id)
      if (res.code === 200) {
        progressPercent.value = res.data.progress
        progressStage.value = res.data.stage
        
        if (res.data.progress >= 100) {
          progressStatus.value = 'success'
          setTimeout(() => {
            progressDialogVisible.value = false
            fetchBackups()
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
  fetchBackups()
})
</script>

<style scoped>
.backup-list {
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

.filter-card {
  margin-bottom: 20px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
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
