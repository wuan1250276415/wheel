<template>
  <div class="blacklist-page">
    <!-- 搜索筛选区 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryForm" inline>
        <el-form-item label="类型">
          <el-select v-model="queryForm.type" placeholder="全部" clearable style="width: 100px">
            <el-option v-for="(name, key) in BlacklistTypeMap" :key="key" :label="name" :value="Number(key)" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标ID">
          <el-input
            v-model="queryForm.targetId"
            placeholder="用户ID/IP/设备ID"
            clearable
            style="width: 150px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 100px">
            <el-option v-for="(name, key) in BlacklistStatusMap" :key="key" :label="name" :value="Number(key)" />
          </el-select>
        </el-form-item>
        <el-form-item label="申诉状态">
          <el-select v-model="queryForm.appealStatus" placeholder="全部" clearable style="width: 120px">
            <el-option v-for="(name, key) in AppealStatusMap" :key="key" :label="name" :value="Number(key)" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <el-button type="primary" v-permission="['security:blacklist:add']" @click="handleAdd">
          <el-icon><Plus /></el-icon>添加黑名单
        </el-button>
        <el-button type="warning" @click="showAppeals = !showAppeals">
          {{ showAppeals ? '返回列表' : '查看申诉' }}
          <el-badge v-if="appealCount > 0" :value="appealCount" class="appeal-badge" />
        </el-button>
      </div>
    </el-card>

    <!-- 黑名单列表 -->
    <el-card v-if="!showAppeals" class="table-card" shadow="never">
      <el-table v-loading="loading" :data="tableData" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)">{{ BlacklistTypeMap[row.type] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetId" label="目标ID" width="150" />
        <el-table-column prop="targetName" label="目标名称" width="120" />
        <el-table-column prop="reason" label="封禁原因" min-width="200" show-overflow-tooltip />
        <el-table-column label="封禁时长" width="100">
          <template #default="{ row }">
            {{ row.duration === 0 ? '永久' : `${row.duration}分钟` }}
          </template>
        </el-table-column>
        <el-table-column label="过期时间" width="180">
          <template #default="{ row }">
            {{ row.expireAt || '永久' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'danger' : 'info'">
              {{ BlacklistStatusMap[row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申诉状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getAppealStatusType(row.appealStatus)">
              {{ AppealStatusMap[row.appealStatus] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operatorName" label="操作人" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 1"
              link
              type="warning"
              size="small"
              v-permission="['security:blacklist:remove']"
              @click="handleRemove(row)"
            >
              解除
            </el-button>
            <el-button
              v-if="row.appealStatus === 1"
              link
              type="primary"
              size="small"
              v-permission="['security:blacklist:appeal']"
              @click="handleAppeal(row)"
            >
              处理申诉
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

    <!-- 申诉列表 -->
    <el-card v-if="showAppeals" class="table-card" shadow="never">
      <template #header>
        <span>待处理申诉列表</span>
      </template>
      <el-table v-loading="appealLoading" :data="appealData" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)">{{ BlacklistTypeMap[row.type] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetId" label="目标ID" width="150" />
        <el-table-column prop="reason" label="封禁原因" width="200" show-overflow-tooltip />
        <el-table-column prop="appealReason" label="申诉理由" min-width="200" show-overflow-tooltip />
        <el-table-column prop="appealTime" label="申诉时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="success" size="small" @click="handleAppealApprove(row)">通过</el-button>
            <el-button link type="danger" size="small" @click="handleAppealReject(row)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加黑名单对话框 -->
    <el-dialog
      v-model="addDialogVisible"
      title="添加黑名单"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form ref="addFormRef" :model="addForm" :rules="addFormRules" label-width="100px">
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="addForm.type">
            <el-radio :label="1">用户</el-radio>
            <el-radio :label="2">IP</el-radio>
            <el-radio :label="3">设备</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="目标ID" prop="targetId">
          <el-input v-model="addForm.targetId" :placeholder="getTargetPlaceholder(addForm.type)" />
        </el-form-item>
        <el-form-item label="封禁原因" prop="reason">
          <el-input v-model="addForm.reason" type="textarea" :rows="3" placeholder="请输入封禁原因" maxlength="500" />
        </el-form-item>
        <el-form-item label="封禁时长" prop="duration">
          <el-radio-group v-model="addForm.durationType" @change="handleDurationTypeChange">
            <el-radio label="permanent">永久</el-radio>
            <el-radio label="temporary">临时</el-radio>
          </el-radio-group>
          <div v-if="addForm.durationType === 'temporary'" style="margin-top: 10px">
            <el-input-number v-model="addForm.duration" :min="1" :max="525600" />
            <span style="margin-left: 8px">分钟</span>
            <div style="margin-top: 8px; color: #909399; font-size: 12px">
              快捷选择：
              <el-button link size="small" @click="addForm.duration = 60">1小时</el-button>
              <el-button link size="small" @click="addForm.duration = 1440">1天</el-button>
              <el-button link size="small" @click="addForm.duration = 10080">7天</el-button>
              <el-button link size="small" @click="addForm.duration = 43200">30天</el-button>
            </div>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleAddSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 申诉处理对话框 -->
    <el-dialog
      v-model="appealDialogVisible"
      title="处理申诉"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="封禁原因">{{ currentAppeal?.reason }}</el-descriptions-item>
        <el-descriptions-item label="申诉理由">{{ currentAppeal?.appealReason }}</el-descriptions-item>
        <el-descriptions-item label="申诉时间">{{ currentAppeal?.appealTime }}</el-descriptions-item>
      </el-descriptions>
      <el-form :model="appealHandleForm" label-width="100px" style="margin-top: 20px">
        <el-form-item label="处理结果">
          <el-radio-group v-model="appealHandleForm.approved">
            <el-radio :label="true">通过申诉（解除封禁）</el-radio>
            <el-radio :label="false">驳回申诉</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理说明">
          <el-input v-model="appealHandleForm.reason" type="textarea" :rows="3" placeholder="请输入处理说明" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="appealDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="appealSubmitLoading" @click="handleAppealSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { Blacklist, BlacklistQueryDTO, BlacklistCreateDTO } from '@/types/security'
import { BlacklistTypeMap, BlacklistStatusMap, AppealStatusMap } from '@/types/security'
import {
  getBlacklistList,
  addBlacklist,
  removeBlacklist,
  getBlacklistAppeals,
  handleBlacklistAppeal
} from '@/api/security'

// 查询表单
const queryForm = reactive<BlacklistQueryDTO>({
  type: undefined,
  targetId: '',
  status: undefined,
  appealStatus: undefined
})

// 表格数据
const loading = ref(false)
const tableData = ref<Blacklist[]>([])
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 申诉相关
const showAppeals = ref(false)
const appealLoading = ref(false)
const appealData = ref<Blacklist[]>([])
const appealCount = ref(0)

// 添加对话框
const addDialogVisible = ref(false)
const addFormRef = ref<FormInstance>()
const submitLoading = ref(false)
const addForm = reactive({
  type: 1,
  targetId: '',
  reason: '',
  duration: 1440,
  durationType: 'temporary' as 'permanent' | 'temporary'
})

const addFormRules: FormRules = {
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  targetId: [{ required: true, message: '请输入目标ID', trigger: 'blur' }],
  reason: [{ required: true, message: '请输入封禁原因', trigger: 'blur' }]
}

// 申诉处理对话框
const appealDialogVisible = ref(false)
const currentAppeal = ref<Blacklist | null>(null)
const appealSubmitLoading = ref(false)
const appealHandleForm = reactive({
  approved: false,
  reason: ''
})

// 查询列表
async function handleQuery() {
  loading.value = true
  try {
    const params = {
      ...queryForm,
      current: pagination.current,
      size: pagination.size
    }
    const res = await getBlacklistList(params)
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
  queryForm.type = undefined
  queryForm.targetId = ''
  queryForm.status = undefined
  queryForm.appealStatus = undefined
  pagination.current = 1
  handleQuery()
}

// 加载申诉列表
async function loadAppeals() {
  appealLoading.value = true
  try {
    const res = await getBlacklistAppeals({ status: 1, current: 1, size: 100 })
    appealData.value = res.data.records
    appealCount.value = res.data.total
  } catch (error) {
    console.error('加载申诉列表失败', error)
  } finally {
    appealLoading.value = false
  }
}

// 添加黑名单
function handleAdd() {
  addForm.type = 1
  addForm.targetId = ''
  addForm.reason = ''
  addForm.duration = 1440
  addForm.durationType = 'temporary'
  addDialogVisible.value = true
}

// 封禁时长类型变更
function handleDurationTypeChange(val: string) {
  if (val === 'permanent') {
    addForm.duration = 0
  } else {
    addForm.duration = 1440
  }
}

// 获取目标ID占位符
function getTargetPlaceholder(type: number): string {
  const map: Record<number, string> = {
    1: '请输入用户ID',
    2: '请输入IP地址',
    3: '请输入设备ID'
  }
  return map[type] || '请输入目标ID'
}

// 提交添加
async function handleAddSubmit() {
  if (!addFormRef.value) return
  await addFormRef.value.validate()

  submitLoading.value = true
  try {
    const data: BlacklistCreateDTO = {
      type: addForm.type,
      targetId: addForm.targetId,
      reason: addForm.reason,
      duration: addForm.durationType === 'permanent' ? 0 : addForm.duration
    }
    await addBlacklist(data)
    ElMessage.success('添加成功')
    addDialogVisible.value = false
    handleQuery()
  } catch (error: any) {
    ElMessage.error(error.message || '添加失败')
  } finally {
    submitLoading.value = false
  }
}

// 解除黑名单
function handleRemove(row: Blacklist) {
  ElMessageBox.confirm(`确定要解除该黑名单吗？`, '解除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await removeBlacklist(row.id)
      ElMessage.success('解除成功')
      handleQuery()
    } catch (error) {
      ElMessage.error('解除失败')
    }
  }).catch(() => {})
}

// 处理申诉
function handleAppeal(row: Blacklist) {
  currentAppeal.value = row
  appealHandleForm.approved = false
  appealHandleForm.reason = ''
  appealDialogVisible.value = true
}

// 快速通过申诉
function handleAppealApprove(row: Blacklist) {
  currentAppeal.value = row
  appealHandleForm.approved = true
  appealHandleForm.reason = '申诉通过，解除封禁'
  handleAppealSubmit()
}

// 快速驳回申诉
function handleAppealReject(row: Blacklist) {
  ElMessageBox.prompt('请输入驳回原因', '驳回申诉', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPlaceholder: '请输入驳回原因'
  }).then(async ({ value }) => {
    try {
      await handleBlacklistAppeal(row.id, { approved: false, reason: value || '申诉驳回' })
      ElMessage.success('已驳回')
      loadAppeals()
    } catch (error) {
      ElMessage.error('操作失败')
    }
  }).catch(() => {})
}

// 提交申诉处理
async function handleAppealSubmit() {
  if (!currentAppeal.value) return

  appealSubmitLoading.value = true
  try {
    await handleBlacklistAppeal(currentAppeal.value.id, appealHandleForm)
    ElMessage.success(appealHandleForm.approved ? '申诉已通过' : '申诉已驳回')
    appealDialogVisible.value = false
    loadAppeals()
    handleQuery()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    appealSubmitLoading.value = false
  }
}

// 获取类型标签类型
function getTypeTagType(type: number): string {
  const map: Record<number, string> = {
    1: 'danger',
    2: 'warning',
    3: 'info'
  }
  return map[type] || ''
}

// 获取申诉状态标签类型
function getAppealStatusType(status: number): string {
  const map: Record<number, string> = {
    0: 'info',
    1: 'warning',
    2: 'success',
    3: 'danger'
  }
  return map[status] || ''
}

// 监听申诉列表显示
watch(showAppeals, (val) => {
  if (val) {
    loadAppeals()
  }
})

onMounted(() => {
  handleQuery()
  loadAppeals()
})
</script>

<style scoped lang="scss">
.blacklist-page {
  .search-card {
    margin-bottom: 16px;

    .action-buttons {
      margin-top: 12px;
      display: flex;
      align-items: center;
      gap: 12px;

      .appeal-badge {
        margin-left: 8px;
      }
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
