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
        <el-form-item label="风险等级">
          <el-select v-model="queryForm.riskLevel" placeholder="全部" clearable style="width: 120px">
            <el-option label="高风险" value="high" />
            <el-option label="中风险" value="medium" />
            <el-option label="低风险" value="low" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容类型">
          <el-select v-model="queryForm.contentType" placeholder="全部" clearable style="width: 120px">
            <el-option label="文本" :value="1" />
            <el-option label="图片" :value="2" />
            <el-option label="混合" :value="3" />
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
        <el-table-column prop="title" label="标题" width="200" show-overflow-tooltip />
        <el-table-column label="分类" width="100">
          <template #default="{ row }">
            {{ getCategoryName(row.categoryId) }}
          </template>
        </el-table-column>
        <el-table-column label="优先级" width="80">
          <template #default="{ row }">
            <el-tag :type="getPriorityType(row.priority)" size="small">
              {{ getPriorityText(row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submitUser" label="提交用户" width="100" />
        <el-table-column label="AI预审" width="180">
          <template #default="{ row }">
            <div v-if="row.aiAuditResult" class="ai-result">
              <el-tag :type="getAiResultType(row.aiAuditResult)" size="small">
                {{ getAiResultText(row.aiAuditResult) }}
              </el-tag>
              <span v-if="row.riskScore !== undefined" class="risk-score" :class="getRiskScoreClass(row.riskScore)">
                风险分: {{ row.riskScore }}
              </span>
            </div>
            <span v-else style="color: #909399">-</span>
          </template>
        </el-table-column>
        <el-table-column label="敏感词检测" width="120">
          <template #default="{ row }">
            <template v-if="row.sensitiveWords && row.sensitiveWords.length > 0">
              <el-popover placement="top" :width="200" trigger="hover">
                <template #reference>
                  <el-tag type="danger" size="small">
                    检出{{ row.sensitiveWords.length }}个
                  </el-tag>
                </template>
                <div>
                  <el-tag v-for="word in row.sensitiveWords" :key="word" size="small" style="margin: 2px">
                    {{ word }}
                  </el-tag>
                </div>
              </el-popover>
            </template>
            <el-tag v-else type="success" size="small">无</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="160" />
        <el-table-column label="等待时长" width="90">
          <template #default="{ row }">
            <span :class="getWaitingTimeClass(row.submitTime)">
              {{ getWaitingTime(row.submitTime) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handlePreview(row)">
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
    <el-dialog v-model="previewDialogVisible" title="内容预览" width="800px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="内容ID">{{ previewData.id }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ getCategoryName(previewData.categoryId) }}</el-descriptions-item>
        <el-descriptions-item label="标题" :span="2">{{ previewData.title }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">
          {{ previewData.description }}
        </el-descriptions-item>
        <el-descriptions-item label="标签" :span="2">
          <el-tag v-for="tag in previewData.tags" :key="tag" style="margin-right: 8px">{{ tag }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="提交用户">{{ previewData.submitUser }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ previewData.submitTime }}</el-descriptions-item>
      </el-descriptions>

      <!-- AI预审结果详情 -->
      <el-card v-if="previewData.aiAuditDetail" class="ai-detail-card" shadow="never">
        <template #header>
          <span>AI预审结果</span>
        </template>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="审核决策">
            <el-tag :type="getAiResultType(previewData.aiAuditResult)">
              {{ getAiResultText(previewData.aiAuditResult) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="风险评分">
            <span :class="getRiskScoreClass(previewData.riskScore)">{{ previewData.riskScore }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="风险类型" :span="2">
            <el-tag v-for="risk in previewData.aiAuditDetail?.risks" :key="risk.type" type="warning" size="small" style="margin-right: 4px">
              {{ risk.label }} ({{ (risk.confidence * 100).toFixed(0) }}%)
            </el-tag>
            <span v-if="!previewData.aiAuditDetail?.risks?.length" style="color: #909399">无风险</span>
          </el-descriptions-item>
          <el-descriptions-item label="审核建议" :span="2">
            {{ previewData.aiAuditDetail?.suggestion || '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 敏感词检测结果 -->
      <el-card v-if="previewData.sensitiveWords?.length > 0" class="sensitive-card" shadow="never">
        <template #header>
          <span>敏感词检测结果</span>
        </template>
        <div class="sensitive-words">
          <el-tag v-for="word in previewData.sensitiveWords" :key="word" type="danger" style="margin: 4px">
            {{ word }}
          </el-tag>
        </div>
      </el-card>

      <template #footer>
        <el-button @click="previewDialogVisible = false">关闭</el-button>
        <el-button type="success" v-permission="['audit:queue:approve']" @click="handleApproveFromPreview">
          通过
        </el-button>
        <el-button type="danger" v-permission="['audit:queue:reject']" @click="handleRejectFromPreview">
          拒绝
        </el-button>
      </template>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog
      v-model="auditDialogVisible"
      :title="auditDialogTitle"
      width="550px"
      :close-on-click-modal="false"
    >
      <el-form ref="auditFormRef" :model="auditForm" :rules="auditRules" label-width="100px">
        <!-- 拒绝原因模板（仅拒绝时显示） -->
        <el-form-item v-if="auditForm.auditStatus === 2" label="拒绝原因">
          <el-select v-model="selectedRejectTemplate" placeholder="选择拒绝原因模板" style="width: 100%" @change="handleTemplateSelect">
            <el-option v-for="tpl in rejectTemplates" :key="tpl.id" :label="tpl.name" :value="tpl.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="审核意见" prop="auditComment">
          <el-input
            v-model="auditForm.auditComment"
            type="textarea"
            :rows="5"
            :placeholder="auditForm.auditStatus === 2 ? '请输入拒绝原因' : '请输入审核意见（选填）'"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleAuditSubmit">确定</el-button>
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

// 拒绝原因模板
const rejectTemplates = [
  { id: 1, name: '内容涉及色情低俗', content: '您提交的内容涉及色情低俗信息，违反平台规定，已被拒绝。' },
  { id: 2, name: '内容涉及暴力血腥', content: '您提交的内容涉及暴力血腥信息，违反平台规定，已被拒绝。' },
  { id: 3, name: '内容涉及政治敏感', content: '您提交的内容涉及政治敏感信息，违反平台规定，已被拒绝。' },
  { id: 4, name: '内容涉及广告推广', content: '您提交的内容涉及广告推广信息，违反平台规定，已被拒绝。' },
  { id: 5, name: '内容涉及侵权', content: '您提交的内容涉及侵权信息，违反平台规定，已被拒绝。' },
  { id: 6, name: '内容质量不符合要求', content: '您提交的内容质量不符合平台要求，请修改后重新提交。' },
  { id: 7, name: '内容与分类不符', content: '您提交的内容与所选分类不符，请选择正确分类后重新提交。' },
  { id: 8, name: '其他原因', content: '' }
]

// 查询表单
const queryForm = reactive<AuditContentQueryDTO & { riskLevel?: string; contentType?: number }>({
  priority: undefined,
  categoryId: undefined,
  riskLevel: undefined,
  contentType: undefined,
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
const selectedRejectTemplate = ref<number | null>(null)
const auditForm = reactive({
  contentId: 0,
  auditStatus: 1,
  auditComment: ''
})
const auditRules: FormRules = {
  auditComment: [
    { required: false, message: '请输入审核意见', trigger: 'blur' }
  ]
}

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
  queryForm.riskLevel = undefined
  queryForm.contentType = undefined
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
  selectedRejectTemplate.value = null
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
  selectedRejectTemplate.value = null
  auditDialogVisible.value = true
}

// 从预览对话框拒绝
function handleRejectFromPreview() {
  previewDialogVisible.value = false
  handleReject(previewData.value)
}

// 选择拒绝原因模板
function handleTemplateSelect(templateId: number) {
  const template = rejectTemplates.find(t => t.id === templateId)
  if (template) {
    auditForm.auditComment = template.content
  }
}

// 提交审核
async function handleAuditSubmit() {
  if (!auditFormRef.value) return

  // 拒绝时必须填写原因
  if (auditForm.auditStatus === 2 && !auditForm.auditComment.trim()) {
    ElMessage.warning('请填写拒绝原因')
    return
  }

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
      inputPlaceholder: '请输入拒绝原因',
      inputType: 'textarea'
    }
  ).then(async ({ value }) => {
    if (!value?.trim()) {
      ElMessage.warning('请输入拒绝原因')
      return
    }
    try {
      await batchRejectContent({
        contentIds: selectedIds.value,
        auditComment: value
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
  const map: Record<number, string> = { 1: '爱好娱乐', 2: '情感约会', 3: '生活日常', 4: '学习工作' }
  return map[categoryId] || ''
}

// 获取优先级类型
function getPriorityType(priority: number): string {
  const map: Record<number, string> = { 3: 'danger', 2: 'warning', 1: 'info' }
  return map[priority] || ''
}

// 获取优先级文本
function getPriorityText(priority: number): string {
  const map: Record<number, string> = { 3: '高', 2: '中', 1: '低' }
  return map[priority] || ''
}

// 获取AI结果类型
function getAiResultType(result: number): string {
  const map: Record<number, string> = { 1: 'success', 2: 'warning', 3: 'danger' }
  return map[result] || ''
}

// 获取AI结果文本
function getAiResultText(result: number): string {
  const map: Record<number, string> = { 1: '建议通过', 2: '需复审', 3: '建议拒绝' }
  return map[result] || '-'
}

// 获取风险分样式类
function getRiskScoreClass(score: number): string {
  if (score >= 80) return 'risk-high'
  if (score >= 30) return 'risk-medium'
  return 'risk-low'
}

// 获取等待时长
function getWaitingTime(submitTime: string): string {
  const now = dayjs()
  const submit = dayjs(submitTime)
  const hours = now.diff(submit, 'hour')
  const minutes = now.diff(submit, 'minute')

  if (hours >= 24) return `${Math.floor(hours / 24)}天`
  if (hours > 0) return `${hours}小时`
  return `${minutes}分钟`
}

// 获取等待时长样式类
function getWaitingTimeClass(submitTime: string): string {
  const hours = dayjs().diff(dayjs(submitTime), 'hour')
  if (hours >= 24) return 'waiting-danger'
  if (hours >= 4) return 'waiting-warning'
  return ''
}

onMounted(() => {
  handleQuery()
})
</script>

<style scoped lang="scss">
.audit-queue-page {
  .search-card {
    margin-bottom: 16px;
    .batch-actions { margin-top: 12px; }
  }

  .table-card {
    .pagination-wrapper {
      margin-top: 16px;
      display: flex;
      justify-content: flex-end;
    }
  }

  .ai-result {
    display: flex;
    flex-direction: column;
    gap: 4px;

    .risk-score {
      font-size: 12px;
      &.risk-high { color: #f56c6c; }
      &.risk-medium { color: #e6a23c; }
      &.risk-low { color: #67c23a; }
    }
  }

  .waiting-danger { color: #f56c6c; font-weight: 500; }
  .waiting-warning { color: #e6a23c; }

  .ai-detail-card, .sensitive-card {
    margin-top: 16px;
  }
}
</style>
