<template>
  <div class="user-membership-page">
    <!-- 搜索筛选区 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryForm" inline>
        <el-form-item label="用户ID">
          <el-input
            v-model="queryForm.userId"
            placeholder="请输入用户ID"
            clearable
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="会员等级">
          <el-select v-model="queryForm.tier" placeholder="全部" clearable style="width: 120px">
            <el-option label="VIP" :value="1" />
            <el-option label="SVIP" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="会员状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="正常" :value="1" />
            <el-option label="已过期" :value="2" />
            <el-option label="已取消" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
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
    </el-card>

    <!-- 表格区域 -->
    <el-card class="table-card" shadow="never">
      <el-table
        v-loading="loading"
        :data="tableData"
        border
      >
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="nickname" label="用户昵称" width="150" />
        <el-table-column label="会员等级" width="100">
          <template #default="{ row }">
            <el-tag :type="row.tier === 2 ? 'warning' : 'success'">
              {{ row.tier === 2 ? 'SVIP' : 'VIP' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="planName" label="套餐名称" width="180" />
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="endTime" label="结束时间" width="180" />
        <el-table-column label="剩余天数" width="100">
          <template #default="{ row }">
            <span :class="{ 'text-danger': row.remainingDays < 7 }">
              {{ row.remainingDays }}天
            </span>
          </template>
        </el-table-column>
        <el-table-column label="自动续费" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.autoRenew"
              :active-value="true"
              :inactive-value="false"
              v-permission="['membership:user:update']"
              @change="handleAutoRenewChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              size="small"
              v-permission="['membership:user:view']"
              @click="handleViewHistory(row)"
            >
              历史记录
            </el-button>
            <el-button
              link
              type="primary"
              size="small"
              v-permission="['membership:user:extend']"
              @click="handleExtend(row)"
            >
              延期
            </el-button>
            <el-button
              link
              type="warning"
              size="small"
              v-permission="['membership:user:upgrade']"
              @click="handleUpgrade(row)"
            >
              升级
            </el-button>
            <el-button
              link
              type="danger"
              size="small"
              v-permission="['membership:user:update']"
              @click="handleCancel(row)"
            >
              取消
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

    <!-- 延期对话框 -->
    <el-dialog
      v-model="extendDialogVisible"
      title="延期会员"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="extendFormRef"
        :model="extendForm"
        :rules="extendRules"
        label-width="100px"
      >
        <el-form-item label="用户昵称">
          <span>{{ currentRow?.nickname }}</span>
        </el-form-item>
        <el-form-item label="当前到期">
          <span>{{ currentRow?.endTime }}</span>
        </el-form-item>
        <el-form-item label="延期天数" prop="days">
          <el-input-number
            v-model="extendForm.days"
            :min="1"
            :max="365"
            controls-position="right"
          />
          <span style="margin-left: 8px">天</span>
        </el-form-item>
        <el-form-item label="延期原因" prop="reason">
          <el-input
            v-model="extendForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入延期原因"
            maxlength="200"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="extendDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleExtendSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 升级对话框 -->
    <el-dialog
      v-model="upgradeDialogVisible"
      title="升级会员"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="upgradeFormRef"
        :model="upgradeForm"
        :rules="upgradeRules"
        label-width="100px"
      >
        <el-form-item label="用户昵称">
          <span>{{ currentRow?.nickname }}</span>
        </el-form-item>
        <el-form-item label="当前等级">
          <el-tag :type="currentRow?.tier === 2 ? 'warning' : 'success'">
            {{ currentRow?.tier === 2 ? 'SVIP' : 'VIP' }}
          </el-tag>
        </el-form-item>
        <el-form-item label="目标等级" prop="targetTier">
          <el-radio-group v-model="upgradeForm.targetTier">
            <el-radio :label="2" :disabled="currentRow?.tier === 2">SVIP</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="有效时长" prop="durationDays">
          <el-input-number
            v-model="upgradeForm.durationDays"
            :min="1"
            :max="3650"
            controls-position="right"
          />
          <span style="margin-left: 8px">天</span>
        </el-form-item>
        <el-form-item label="升级原因" prop="reason">
          <el-input
            v-model="upgradeForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入升级原因"
            maxlength="200"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="upgradeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleUpgradeSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 历史记录抽屉 -->
    <el-drawer
      v-model="historyDrawerVisible"
      title="会员历史记录"
      size="60%"
    >
      <el-timeline>
        <el-timeline-item
          v-for="item in historyList"
          :key="item.id"
          :timestamp="item.createTime"
          placement="top"
        >
          <el-card>
            <p>
              <strong>操作类型：</strong>
              <el-tag :type="getOperationTagType(item.operationType)">
                {{ item.operationType }}
              </el-tag>
            </p>
            <p><strong>会员等级：</strong>{{ item.tier === 2 ? 'SVIP' : 'VIP' }}</p>
            <p><strong>开始时间：</strong>{{ item.startTime }}</p>
            <p><strong>结束时间：</strong>{{ item.endTime }}</p>
            <p v-if="item.remark"><strong>备注：</strong>{{ item.remark }}</p>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import type { UserMembership, UserMembershipQueryDTO } from '@/types/api'
import {
  queryUserMemberships,
  getUserMembershipHistory,
  extendUserMembership,
  upgradeUserMembership,
  cancelUserMembership,
  updateAutoRenew
} from '@/api/membership'

// 查询表单
const queryForm = reactive<UserMembershipQueryDTO>({
  userId: undefined,
  tier: undefined,
  status: undefined,
  dateRange: []
})

// 表格数据
const loading = ref(false)
const tableData = ref<UserMembership[]>([])
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 当前操作行
const currentRow = ref<UserMembership | null>(null)

// 延期对话框
const extendDialogVisible = ref(false)
const extendFormRef = ref<FormInstance>()
const extendForm = reactive({
  days: 30,
  reason: ''
})
const extendRules: FormRules = {
  days: [{ required: true, message: '请输入延期天数', trigger: 'blur' }],
  reason: [{ required: true, message: '请输入延期原因', trigger: 'blur' }]
}

// 升级对话框
const upgradeDialogVisible = ref(false)
const upgradeFormRef = ref<FormInstance>()
const upgradeForm = reactive({
  targetTier: 2,
  durationDays: 365,
  reason: ''
})
const upgradeRules: FormRules = {
  targetTier: [{ required: true, message: '请选择目标等级', trigger: 'change' }],
  durationDays: [{ required: true, message: '请输入有效时长', trigger: 'blur' }],
  reason: [{ required: true, message: '请输入升级原因', trigger: 'blur' }]
}

// 历史记录抽屉
const historyDrawerVisible = ref(false)
const historyList = ref<any[]>([])

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

    const res = await queryUserMemberships(params)
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
  queryForm.userId = undefined
  queryForm.tier = undefined
  queryForm.status = undefined
  queryForm.dateRange = []
  pagination.current = 1
  handleQuery()
}

// 延期会员
function handleExtend(row: UserMembership) {
  currentRow.value = row
  extendForm.days = 30
  extendForm.reason = ''
  extendFormRef.value?.clearValidate()
  extendDialogVisible.value = true
}

async function handleExtendSubmit() {
  if (!extendFormRef.value || !currentRow.value) return

  await extendFormRef.value.validate(async (valid) => {
    if (!valid) return

    submitLoading.value = true
    try {
      await extendUserMembership(currentRow.value!.userId, extendForm)
      ElMessage.success('延期成功')
      extendDialogVisible.value = false
      handleQuery()
    } catch (error: any) {
      ElMessage.error(error.message || '延期失败')
    } finally {
      submitLoading.value = false
    }
  })
}

// 升级会员
function handleUpgrade(row: UserMembership) {
  currentRow.value = row
  upgradeForm.targetTier = 2
  upgradeForm.durationDays = 365
  upgradeForm.reason = ''
  upgradeFormRef.value?.clearValidate()
  upgradeDialogVisible.value = true
}

async function handleUpgradeSubmit() {
  if (!upgradeFormRef.value || !currentRow.value) return

  await upgradeFormRef.value.validate(async (valid) => {
    if (!valid) return

    submitLoading.value = true
    try {
      await upgradeUserMembership(currentRow.value!.userId, upgradeForm)
      ElMessage.success('升级成功')
      upgradeDialogVisible.value = false
      handleQuery()
    } catch (error: any) {
      ElMessage.error(error.message || '升级失败')
    } finally {
      submitLoading.value = false
    }
  })
}

// 取消会员
function handleCancel(row: UserMembership) {
  ElMessageBox.confirm(
    `确定要取消用户【${row.nickname}】的会员吗？`,
    '取消确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await cancelUserMembership(row.userId)
      ElMessage.success('取消成功')
      handleQuery()
    } catch (error) {
      ElMessage.error('取消失败')
    }
  }).catch(() => {})
}

// 自动续费切换
async function handleAutoRenewChange(row: UserMembership) {
  try {
    await updateAutoRenew(row.userId, row.autoRenew)
    ElMessage.success(row.autoRenew ? '已开启自动续费' : '已关闭自动续费')
  } catch (error) {
    row.autoRenew = !row.autoRenew
    ElMessage.error('设置失败')
  }
}

// 查看历史记录
async function handleViewHistory(row: UserMembership) {
  try {
    const res = await getUserMembershipHistory(row.userId)
    historyList.value = res.data
    historyDrawerVisible.value = true
  } catch (error) {
    ElMessage.error('获取历史记录失败')
  }
}

// 获取状态类型
function getStatusType(status: number): string {
  const map: Record<number, string> = {
    1: 'success',
    2: 'info',
    3: 'danger'
  }
  return map[status] || ''
}

// 获取状态文本
function getStatusText(status: number): string {
  const map: Record<number, string> = {
    1: '正常',
    2: '已过期',
    3: '已取消'
  }
  return map[status] || ''
}

// 获取操作标签类型
function getOperationTagType(operation: string): string {
  const map: Record<string, string> = {
    '开通': 'success',
    '续费': 'primary',
    '升级': 'warning',
    '延期': 'info',
    '取消': 'danger'
  }
  return map[operation] || ''
}

onMounted(() => {
  handleQuery()
})
</script>

<style scoped lang="scss">
.user-membership-page {
  .search-card {
    margin-bottom: 16px;
  }

  .table-card {
    .text-danger {
      color: #f56c6c;
      font-weight: 500;
    }

    .pagination-wrapper {
      margin-top: 16px;
      display: flex;
      justify-content: flex-end;
    }
  }
}
</style>
