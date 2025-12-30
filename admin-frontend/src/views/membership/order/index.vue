<template>
  <div class="payment-order-page">
    <!-- 搜索筛选区 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryForm" inline>
        <el-form-item label="订单号">
          <el-input
            v-model="queryForm.orderNo"
            placeholder="请输入订单号"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="用户ID">
          <el-input
            v-model="queryForm.userId"
            placeholder="请输入用户ID"
            clearable
            style="width: 150px"
          />
        </el-form-item>
        <el-form-item label="支付状态">
          <el-select v-model="queryForm.paymentStatus" placeholder="全部" clearable style="width: 120px">
            <el-option label="待支付" :value="0" />
            <el-option label="已支付" :value="1" />
            <el-option label="已退款" :value="2" />
            <el-option label="已取消" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="queryForm.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            style="width: 360px"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">今日订单</div>
            <div class="stat-value">{{ statistics.todayOrders }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">今日收入（元）</div>
            <div class="stat-value success">{{ statistics.todayIncome }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">本月收入（元）</div>
            <div class="stat-value warning">{{ statistics.monthIncome }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">退款率</div>
            <div class="stat-value danger">{{ statistics.refundRate }}%</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 表格区域 -->
    <el-card class="table-card" shadow="never">
      <el-table
        v-loading="loading"
        :data="tableData"
        border
      >
        <el-table-column prop="orderNo" label="订单号" width="200" fixed="left" />
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="planName" label="套餐名称" width="180" />
        <el-table-column label="订单金额" width="120">
          <template #default="{ row }">
            <span class="amount">¥{{ row.orderAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="支付方式" width="100">
          <template #default="{ row }">
            {{ getPaymentMethodText(row.paymentMethod) }}
          </template>
        </el-table-column>
        <el-table-column label="支付状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getPaymentStatusType(row.paymentStatus)">
              {{ getPaymentStatusText(row.paymentStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paymentTime" label="支付时间" width="180" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              size="small"
              v-permission="['membership:order:view']"
              @click="handleViewDetail(row)"
            >
              详情
            </el-button>
            <el-button
              link
              type="danger"
              size="small"
              v-permission="['membership:order:refund']"
              v-if="row.paymentStatus === 1"
              @click="handleRefund(row)"
            >
              退款
            </el-button>
            <el-button
              link
              type="primary"
              size="small"
              v-permission="['membership:order:view']"
              @click="handleRemark(row)"
            >
              备注
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

    <!-- 订单详情抽屉 -->
    <el-drawer
      v-model="detailDrawerVisible"
      title="订单详情"
      size="50%"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单号" :span="2">{{ detailData.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ detailData.userId }}</el-descriptions-item>
        <el-descriptions-item label="用户昵称">{{ detailData.nickname }}</el-descriptions-item>
        <el-descriptions-item label="套餐名称" :span="2">{{ detailData.planName }}</el-descriptions-item>
        <el-descriptions-item label="订单金额">
          <span class="amount">¥{{ detailData.orderAmount }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="实付金额">
          <span class="amount">¥{{ detailData.paidAmount }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="支付方式">
          {{ getPaymentMethodText(detailData.paymentMethod) }}
        </el-descriptions-item>
        <el-descriptions-item label="支付状态">
          <el-tag :type="getPaymentStatusType(detailData.paymentStatus)">
            {{ getPaymentStatusText(detailData.paymentStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="交易流水号" :span="2">
          {{ detailData.transactionId || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ detailData.paymentTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="退款时间">{{ detailData.refundTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="退款金额">
          {{ detailData.refundAmount ? `¥${detailData.refundAmount}` : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="退款原因" :span="2">
          {{ detailData.refundReason || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">
          {{ detailData.remark || '-' }}
        </el-descriptions-item>
      </el-descriptions>

      <div v-if="detailData.callbackData" style="margin-top: 20px">
        <el-divider>回调数据</el-divider>
        <el-input
          type="textarea"
          :value="JSON.stringify(JSON.parse(detailData.callbackData), null, 2)"
          :rows="10"
          readonly
        />
      </div>
    </el-drawer>

    <!-- 退款对话框 -->
    <el-dialog
      v-model="refundDialogVisible"
      title="订单退款"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="refundFormRef"
        :model="refundForm"
        :rules="refundRules"
        label-width="100px"
      >
        <el-form-item label="订单号">
          <span>{{ currentRow?.orderNo }}</span>
        </el-form-item>
        <el-form-item label="订单金额">
          <span class="amount">¥{{ currentRow?.orderAmount }}</span>
        </el-form-item>
        <el-form-item label="退款金额" prop="refundAmount">
          <el-input-number
            v-model="refundForm.refundAmount"
            :min="0.01"
            :max="currentRow?.orderAmount || 0"
            :precision="2"
            controls-position="right"
          />
          <span style="margin-left: 8px">元</span>
        </el-form-item>
        <el-form-item label="退款原因" prop="refundReason">
          <el-input
            v-model="refundForm.refundReason"
            type="textarea"
            :rows="3"
            placeholder="请输入退款原因"
            maxlength="200"
          />
        </el-form-item>
        <el-alert
          title="退款后会员权益将被取消，请谨慎操作"
          type="warning"
          :closable="false"
          show-icon
        />
      </el-form>

      <template #footer>
        <el-button @click="refundDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="submitLoading" @click="handleRefundSubmit">
          确认退款
        </el-button>
      </template>
    </el-dialog>

    <!-- 备注对话框 -->
    <el-dialog
      v-model="remarkDialogVisible"
      title="添加备注"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-input
        v-model="remarkForm.remark"
        type="textarea"
        :rows="5"
        placeholder="请输入备注信息"
        maxlength="500"
      />

      <template #footer>
        <el-button @click="remarkDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleRemarkSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import type { PaymentOrder, PaymentOrderQueryDTO } from '@/types/api'
import {
  queryOrders,
  getOrderDetail,
  refundOrder,
  updateOrderRemark,
  getOrderStatistics
} from '@/api/membership'

// 查询表单
const queryForm = reactive<PaymentOrderQueryDTO>({
  orderNo: '',
  userId: undefined,
  paymentStatus: undefined,
  dateRange: []
})

// 统计数据
const statistics = reactive({
  todayOrders: 0,
  todayIncome: 0,
  monthIncome: 0,
  refundRate: 0
})

// 表格数据
const loading = ref(false)
const tableData = ref<PaymentOrder[]>([])
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 当前操作行
const currentRow = ref<PaymentOrder | null>(null)

// 详情抽屉
const detailDrawerVisible = ref(false)
const detailData = ref<any>({})

// 退款对话框
const refundDialogVisible = ref(false)
const refundFormRef = ref<FormInstance>()
const refundForm = reactive({
  refundAmount: 0,
  refundReason: ''
})
const refundRules: FormRules = {
  refundAmount: [{ required: true, message: '请输入退款金额', trigger: 'blur' }],
  refundReason: [
    { required: true, message: '请输入退款原因', trigger: 'blur' },
    { min: 5, max: 200, message: '长度在 5 到 200 个字符', trigger: 'blur' }
  ]
}

// 备注对话框
const remarkDialogVisible = ref(false)
const remarkForm = reactive({
  remark: ''
})

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
      params.startTime = queryForm.dateRange[0]
      params.endTime = queryForm.dateRange[1]
    }
    delete params.dateRange

    const res = await queryOrders(params)
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
  queryForm.orderNo = ''
  queryForm.userId = undefined
  queryForm.paymentStatus = undefined
  queryForm.dateRange = []
  pagination.current = 1
  handleQuery()
}

// 加载统计数据
async function loadStatistics() {
  try {
    const res = await getOrderStatistics({})
    Object.assign(statistics, res.data)
  } catch (error) {
    console.error('加载统计数据失败', error)
  }
}

// 查看详情
async function handleViewDetail(row: PaymentOrder) {
  try {
    const res = await getOrderDetail(row.id)
    detailData.value = res.data
    detailDrawerVisible.value = true
  } catch (error) {
    ElMessage.error('获取订单详情失败')
  }
}

// 退款
function handleRefund(row: PaymentOrder) {
  currentRow.value = row
  refundForm.refundAmount = row.orderAmount
  refundForm.refundReason = ''
  refundFormRef.value?.clearValidate()
  refundDialogVisible.value = true
}

async function handleRefundSubmit() {
  if (!refundFormRef.value || !currentRow.value) return

  await refundFormRef.value.validate(async (valid) => {
    if (!valid) return

    ElMessageBox.confirm(
      '退款操作不可逆，确定要退款吗？',
      '退款确认',
      {
        confirmButtonText: '确定退款',
        cancelButtonText: '取消',
        type: 'warning'
      }
    ).then(async () => {
      submitLoading.value = true
      try {
        await refundOrder(currentRow.value!.id, refundForm)
        ElMessage.success('退款成功')
        refundDialogVisible.value = false
        handleQuery()
        loadStatistics()
      } catch (error: any) {
        ElMessage.error(error.message || '退款失败')
      } finally {
        submitLoading.value = false
      }
    }).catch(() => {})
  })
}

// 添加备注
function handleRemark(row: PaymentOrder) {
  currentRow.value = row
  remarkForm.remark = row.remark || ''
  remarkDialogVisible.value = true
}

async function handleRemarkSubmit() {
  if (!currentRow.value) return

  submitLoading.value = true
  try {
    await updateOrderRemark(currentRow.value.id, remarkForm.remark)
    ElMessage.success('备注已更新')
    remarkDialogVisible.value = false
    handleQuery()
  } catch (error) {
    ElMessage.error('更新备注失败')
  } finally {
    submitLoading.value = false
  }
}

// 获取支付方式文本
function getPaymentMethodText(method: number): string {
  const map: Record<number, string> = {
    1: '微信支付',
    2: '支付宝',
    3: 'Apple Pay'
  }
  return map[method] || '未知'
}

// 获取支付状态类型
function getPaymentStatusType(status: number): string {
  const map: Record<number, string> = {
    0: 'info',
    1: 'success',
    2: 'warning',
    3: 'danger'
  }
  return map[status] || ''
}

// 获取支付状态文本
function getPaymentStatusText(status: number): string {
  const map: Record<number, string> = {
    0: '待支付',
    1: '已支付',
    2: '已退款',
    3: '已取消'
  }
  return map[status] || ''
}

onMounted(() => {
  handleQuery()
  loadStatistics()
})
</script>

<style scoped lang="scss">
.payment-order-page {
  .search-card {
    margin-bottom: 16px;
  }

  .stat-row {
    margin-bottom: 16px;

    .stat-item {
      text-align: center;

      .stat-label {
        font-size: 14px;
        color: #909399;
        margin-bottom: 8px;
      }

      .stat-value {
        font-size: 24px;
        font-weight: 500;
        color: #303133;

        &.success {
          color: #67c23a;
        }

        &.warning {
          color: #e6a23c;
        }

        &.danger {
          color: #f56c6c;
        }
      }
    }
  }

  .table-card {
    .amount {
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
