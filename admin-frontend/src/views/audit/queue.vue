<template>
  <div class="audit-queue-page">
    <!-- 筛选区 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryForm" inline>
        <el-form-item label="优先级">
          <el-select v-model="queryForm.priority" placeholder="全部" clearable style="width: 120px">
            <el-option label="高" :value="3" />
            <el-option label="中" :value="2" />
            <el-option label="低" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="queryForm.categoryId" placeholder="全部" clearable style="width: 150px">
            <el-option label="爱好娱乐" :value="1" />
            <el-option label="情感约会" :value="2" />
            <el-option label="生活日常" :value="3" />
            <el-option label="学习工作" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="提交时间">
          <el-date-picker
            v-model="queryForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 240px"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 批量操作按钮 -->
      <div class="batch-actions">
        <el-button
          type="success"
          v-permission="['audit:queue:approve']"
          :disabled="selectedIds.length === 0"
          @click="handleBatchApprove"
        >
          批量通过（{{ selectedIds.length }}）
        </el-button>
        <el-button
          type="danger"
          v-permission="['audit:queue:reject']"
          :disabled="selectedIds.length === 0"
          @click="handleBatchReject"
        >
          批量拒绝（{{ selectedIds.length }}）
        </el-button>
      </div>
    </el-card>

    <!-- 表格区域 -->
    <el-card class="table-card" shadow="never">
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="内容ID" width="80" />
        <el-table-column prop="title" label="标题" width="200" />
        <el-table-column label="分类" width="120">
          <template #default="{ row }">
            {{ getCategoryName(row.categoryId) }}
          </template>
        </el-table-column>
        <el-table-column label="优先级" width="100">
          <template #default="{ row }">
            <el-tag :type="getPriorityType(row.priority)">
              {{ getPriorityText(row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submitUser" label="提交用户" width="120" />
        <el-table-column prop="submitTime" label="提交时间" width="180" />
        <el-table-column label="AI预审" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.aiAuditResult" :type="row.aiAuditResult === 1 ? 'success' : 'danger'">
              {{ row.aiAuditResult === 1 ? '建议通过' : '建议拒绝' }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="等待时长" width="100">
          <template #default="{ row }">
            {{ getWaitingTime(row.submitTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              size="small"
              @click="handlePreview(row)"
            >
              预览
            </el-button>
            <el-button
              link
              type="success"
              size="small"
              v-permission="['audit:queue:approve']"
              @click="handleApprove(row)"
            >
              通过
            </el-button>
            <el-button
              link
              type="danger"
              size="small"
              v-permission="['audit:queue:reject']"
              @click="handleReject(row)"
            >
              拒绝
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleQuery"
          @current-change="handleQuery"
        />
      </div>
    </el-card>

    <!-- 内容预览对话框 -->
    <el-dialog
      v-model="previewDialogVisible"
      title="内容预览"
      width="800px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="内容ID">{{ previewData.id }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ getCategoryName(previewData.categoryId) }}</el-descriptions-item>
        <el-descriptions-item label="标题" :span="2">{{ previewData.title }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">
          {{ previewData.description }}
        </el-descriptions-item>
        <el-descriptions-item label="标签" :span="2">
          <el-tag v-for="tag in previewData.tags" :key="tag" style="margin-right: 8px">
            {{ tag }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="提交用户">{{ previewData.submitUser }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ previewData.submitTime }}</el-descriptions-item>
        <el-descriptions-item label="AI预审结果" :span="2">
          <el-tag v-if="previewData.aiAuditResult" :type="previewData.aiAuditResult === 1 ? 'success' : 'danger'">
            {{ previewData.aiAuditResult === 1 ? '建议通过' : '建议拒绝' }}
          </el-tag>
          <span v-else>-</span>
          <div v-if="previewData.aiAuditReason" style="margin-top: 8px; color: #909399">
            原因：{{ previewData.aiAuditReason }}
          </div>
        </el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <el-button @click="previewDialogVisible = false">关闭</el-button>
        <el-button
          type="success"
          v-permission="['audit:queue:approve']"
          @click="handleApproveFromPreview"
        >
          通过
        </el-button>
        <el-button
          type="danger"
          v-permission="['audit:queue:reject']"
          @click="handleRejectFromPreview"
        >
          拒绝
        </el-button>
      </template>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog
      v-model="auditDialogVisible"
      :title="auditDialogTitle"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="auditFormRef"
        :model="auditForm"
        :rules="auditRules"
        label-width="100px"
      >
        <el-form-item label="审核意见" prop="auditComment">
          <el-input
            v-model="auditForm.auditComment"
            type="textarea"
            :rows="5"
            placeholder="请输入审核意见（选填）"
            maxlength="200"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleAuditSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import type { AuditContent, AuditContentQueryDTO } from '@/types/api'
import {
  getAuditQueue,
  approveContent,
  rejectContent,
  batchApproveContent,
  batchRejectContent
} from '@/api/audit'
import dayjs from 'dayjs'

// 查询表单
const queryForm = reactive<AuditContentQueryDTO>({
  priority: undefined,
  categoryId: undefined,
  dateRange: []
})

// 表格数据
const loading = ref(false)
const tableData = ref<AuditContent[]>([])
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 选中的ID列表
const selectedIds = ref<number[]>([])

// 预览对话框
const previewDialogVisible = ref(false)
const previewData = ref<any>({})

// 审核对话框
const auditDialogVisible = ref(false)
const auditDialogTitle = ref('')
const auditFormRef = ref<FormInstance>()
const auditForm = reactive({
  contentId: 0,
  auditStatus: 1,
  auditComment: ''
})
const auditRules: FormRules = {}

// 提交loading
const submitLoading = ref(false)

// 查询列表
async function handleQuery() {
  loading.value = true
  try {
    const params: any = {
      ...queryForm,
      current: pagination.current,
      size: pagination.size
    }

    if (queryForm.dateRange && queryForm.dateRange.length === 2) {
      params.startDate = queryForm.dateRange[0]
      params.endDate = queryForm.dateRange[1]
    }
    delete params.dateRange

    const res = await getAuditQueue(params)
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

// 重置查询
function handleReset() {
  queryForm.priority = undefined
  queryForm.categoryId = undefined
  queryForm.dateRange = []
  pagination.current = 1
  handleQuery()
}

// 选择变更
function handleSelectionChange(selection: AuditContent[]) {
  selectedIds.value = selection.map(item => item.id)
}

// 预览内容
function handlePreview(row: AuditContent) {
  previewData.value = row
  previewDialogVisible.value = true
}

// 通过审核
function handleApprove(row: AuditContent) {
  auditDialogTitle.value = '通过审核'
  auditForm.contentId = row.id
  auditForm.auditStatus = 1
  auditForm.auditComment = ''
  auditDialogVisible.value = true
}

// 从预览对话框通过
function handleApproveFromPreview() {
  previewDialogVisible.value = false
  handleApprove(previewData.value)
}

// 拒绝审核
function handleReject(row: AuditContent) {
  auditDialogTitle.value = '拒绝审核'
  auditForm.contentId = row.id
  auditForm.auditStatus = 2
  auditForm.auditComment = ''
  auditDialogVisible.value = true
}

// 从预览对话框拒绝
function handleRejectFromPreview() {
  previewDialogVisible.value = false
  handleReject(previewData.value)
}

// 提交审核
async function handleAuditSubmit() {
  if (!auditFormRef.value) return

  submitLoading.value = true
  try {
    if (auditForm.auditStatus === 1) {
      await approveContent(auditForm.contentId, auditForm.auditComment)
      ElMessage.success('审核通过')
    } else {
      await rejectContent(auditForm.contentId, auditForm.auditComment)
      ElMessage.success('已拒绝')
    }
    auditDialogVisible.value = false
    handleQuery()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}

// 批量通过
function handleBatchApprove() {
  ElMessageBox.prompt(
    `确定要批量通过选中的 ${selectedIds.value.length} 条内容吗？`,
    '批量通过',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '请输入审核意见（选填）',
      inputType: 'textarea'
    }
  ).then(async ({ value }) => {
    try {
      await batchApproveContent({
        contentIds: selectedIds.value,
        auditComment: value || ''
      })
      ElMessage.success('批量通过成功')
      handleQuery()
    } catch (error) {
      ElMessage.error('批量通过失败')
    }
  }).catch(() => {})
}

// 批量拒绝
function handleBatchReject() {
  ElMessageBox.prompt(
    `确定要批量拒绝选中的 ${selectedIds.value.length} 条内容吗？`,
    '批量拒绝',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '请输入拒绝原因（选填）',
      inputType: 'textarea'
    }
  ).then(async ({ value }) => {
    try {
      await batchRejectContent({
        contentIds: selectedIds.value,
        auditComment: value || ''
      })
      ElMessage.success('批量拒绝成功')
      handleQuery()
    } catch (error) {
      ElMessage.error('批量拒绝失败')
    }
  }).catch(() => {})
}

// 获取分类名称
function getCategoryName(categoryId: number): string {
  const map: Record<number, string> = {
    1: '爱好娱乐',
    2: '情感约会',
    3: '生活日常',
    4: '学习工作'
  }
  return map[categoryId] || ''
}

// 获取优先级类型
function getPriorityType(priority: number): string {
  const map: Record<number, string> = {
    3: 'danger',
    2: 'warning',
    1: 'info'
  }
  return map[priority] || ''
}

// 获取优先级文本
function getPriorityText(priority: number): string {
  const map: Record<number, string> = {
    3: '高',
    2: '中',
    1: '低'
  }
  return map[priority] || ''
}

// 获取等待时长
function getWaitingTime(submitTime: string): string {
  const now = dayjs()
  const submit = dayjs(submitTime)
  const hours = now.diff(submit, 'hour')
  const minutes = now.diff(submit, 'minute')

  if (hours >= 24) {
    return `${Math.floor(hours / 24)}天`
  } else if (hours > 0) {
    return `${hours}小时`
  } else {
    return `${minutes}分钟`
  }
}

onMounted(() => {
  handleQuery()
})
</script>

<style scoped lang="scss">
.audit-queue-page {
  .search-card {
    margin-bottom: 16px;

    .batch-actions {
      margin-top: 12px;
    }
  }

  .table-card {
    .pagination-wrapper {
      margin-top: 16px;
      display: flex;
      justify-content: flex-end;
    }
  }
}
</style>
