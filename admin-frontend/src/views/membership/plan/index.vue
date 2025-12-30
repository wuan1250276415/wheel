<template>
  <div class="membership-plan-page">
    <!-- 搜索和筛选区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryForm" inline>
        <el-form-item label="套餐等级">
          <el-select v-model="queryForm.tier" placeholder="全部" clearable style="width: 150px">
            <el-option label="VIP" :value="1" />
            <el-option label="SVIP" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input
            v-model="queryForm.keyword"
            placeholder="套餐名称/标识"
            clearable
            style="width: 200px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button
            type="primary"
            v-permission="['membership:plan:create']"
            @click="handleCreate"
          >
            新建套餐
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格区域 -->
    <el-card class="table-card" shadow="never">
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        row-key="id"
      >
        <el-table-column prop="planName" label="套餐名称" width="180" />
        <el-table-column prop="planKey" label="套餐标识" width="150" />
        <el-table-column label="等级" width="100">
          <template #default="{ row }">
            <el-tag :type="row.tier === 2 ? 'warning' : 'success'">
              {{ row.tier === 2 ? 'SVIP' : 'VIP' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="时长" width="100">
          <template #default="{ row }">
            {{ row.durationDays }}天
          </template>
        </el-table-column>
        <el-table-column label="价格" width="120">
          <template #default="{ row }">
            <div>
              <span class="price">¥{{ row.price }}</span>
              <span v-if="row.originalPrice > row.price" class="original-price">
                ¥{{ row.originalPrice }}
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="折扣" width="80">
          <template #default="{ row }">
            <span v-if="row.originalPrice > row.price">
              {{ ((row.price / row.originalPrice) * 10).toFixed(1) }}折
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="推荐" width="80" align="center">
          <template #default="{ row }">
            <el-icon v-if="row.isRecommended" color="#f56c6c" :size="18">
              <Star />
            </el-icon>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              v-permission="['membership:plan:update']"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              size="small"
              v-permission="['membership:plan:view']"
              @click="handleView(row)"
            >
              查看
            </el-button>
            <el-button
              link
              type="primary"
              size="small"
              v-permission="['membership:plan:update']"
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button
              link
              type="danger"
              size="small"
              v-permission="['membership:plan:delete']"
              @click="handleDelete(row)"
            >
              删除
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

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="套餐名称" prop="planName">
          <el-input v-model="formData.planName" placeholder="请输入套餐名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="套餐标识" prop="planKey">
          <el-input
            v-model="formData.planKey"
            placeholder="请输入套餐标识（英文+数字）"
            maxlength="50"
            :disabled="isEdit"
          />
        </el-form-item>
        <el-form-item label="会员等级" prop="tier">
          <el-radio-group v-model="formData.tier">
            <el-radio :label="1">VIP</el-radio>
            <el-radio :label="2">SVIP</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="有效时长" prop="durationDays">
          <el-input-number
            v-model="formData.durationDays"
            :min="1"
            :max="3650"
            controls-position="right"
          />
          <span style="margin-left: 8px">天</span>
        </el-form-item>
        <el-form-item label="现价" prop="price">
          <el-input-number
            v-model="formData.price"
            :min="0.01"
            :precision="2"
            controls-position="right"
          />
          <span style="margin-left: 8px">元</span>
        </el-form-item>
        <el-form-item label="原价" prop="originalPrice">
          <el-input-number
            v-model="formData.originalPrice"
            :min="0.01"
            :precision="2"
            controls-position="right"
          />
          <span style="margin-left: 8px">元</span>
        </el-form-item>
        <el-form-item label="会员权益" prop="benefits">
          <el-input
            v-model="formData.benefits"
            type="textarea"
            :rows="3"
            placeholder="请输入会员权益（每行一个）"
          />
        </el-form-item>
        <el-form-item label="套餐描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入套餐描述"
            maxlength="500"
          />
        </el-form-item>
        <el-form-item label="推荐标识" prop="isRecommended">
          <el-switch v-model="formData.isRecommended" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number
            v-model="formData.sortOrder"
            :min="0"
            controls-position="right"
          />
          <span style="margin-left: 8px; color: #909399; font-size: 12px">
            数字越小越靠前
          </span>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="套餐详情"
      width="600px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="套餐名称">{{ viewData.planName }}</el-descriptions-item>
        <el-descriptions-item label="套餐标识">{{ viewData.planKey }}</el-descriptions-item>
        <el-descriptions-item label="会员等级">
          <el-tag :type="viewData.tier === 2 ? 'warning' : 'success'">
            {{ viewData.tier === 2 ? 'SVIP' : 'VIP' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="有效时长">{{ viewData.durationDays }}天</el-descriptions-item>
        <el-descriptions-item label="现价">¥{{ viewData.price }}</el-descriptions-item>
        <el-descriptions-item label="原价">¥{{ viewData.originalPrice }}</el-descriptions-item>
        <el-descriptions-item label="推荐标识">
          {{ viewData.isRecommended ? '是' : '否' }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="viewData.status === 1 ? 'success' : 'info'">
            {{ viewData.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="排序">{{ viewData.sortOrder }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ viewData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="会员权益" :span="2">
          <div v-if="viewData.benefitsList && viewData.benefitsList.length > 0">
            <div v-for="(benefit, index) in viewData.benefitsList" :key="index">
              • {{ benefit }}
            </div>
          </div>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="套餐描述" :span="2">
          {{ viewData.description || '-' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Star } from '@element-plus/icons-vue'
import type { MembershipPlan, MembershipPlanQueryDTO, MembershipPlanCreateDTO } from '@/types/api'
import {
  queryPlans,
  getPlanDetail,
  createPlan,
  updatePlan,
  deletePlan,
  updatePlanStatus
} from '@/api/membership'

// 查询表单
const queryForm = reactive<MembershipPlanQueryDTO>({
  tier: undefined,
  status: undefined,
  keyword: ''
})

// 表格数据
const loading = ref(false)
const tableData = ref<MembershipPlan[]>([])
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()

// 表单数据
const formData = reactive<MembershipPlanCreateDTO>({
  planName: '',
  planKey: '',
  tier: 1,
  durationDays: 30,
  price: 0,
  originalPrice: 0,
  benefits: '',
  description: '',
  isRecommended: false,
  sortOrder: 0
})

// 表单验证规则
const formRules: FormRules = {
  planName: [
    { required: true, message: '请输入套餐名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  planKey: [
    { required: true, message: '请输入套餐标识', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  tier: [
    { required: true, message: '请选择会员等级', trigger: 'change' }
  ],
  durationDays: [
    { required: true, message: '请输入有效时长', trigger: 'blur' }
  ],
  price: [
    { required: true, message: '请输入现价', trigger: 'blur' }
  ],
  originalPrice: [
    { required: true, message: '请输入原价', trigger: 'blur' }
  ]
}

// 查看详情
const viewDialogVisible = ref(false)
const viewData = ref<any>({})

// 查询列表
async function handleQuery() {
  loading.value = true
  try {
    const res = await queryPlans({
      ...queryForm,
      current: pagination.current,
      size: pagination.size
    })
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
  queryForm.tier = undefined
  queryForm.status = undefined
  queryForm.keyword = ''
  pagination.current = 1
  handleQuery()
}

// 新建套餐
function handleCreate() {
  isEdit.value = false
  dialogTitle.value = '新建套餐'
  resetForm()
  dialogVisible.value = true
}

// 编辑套餐
async function handleEdit(row: MembershipPlan) {
  isEdit.value = true
  dialogTitle.value = '编辑套餐'

  try {
    const res = await getPlanDetail(row.id)
    Object.assign(formData, res.data)
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取套餐详情失败')
  }
}

// 查看详情
async function handleView(row: MembershipPlan) {
  try {
    const res = await getPlanDetail(row.id)
    viewData.value = {
      ...res.data,
      benefitsList: res.data.benefits ? JSON.parse(res.data.benefits) : []
    }
    viewDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取套餐详情失败')
  }
}

// 删除套餐
function handleDelete(row: MembershipPlan) {
  ElMessageBox.confirm(
    `确定要删除套餐【${row.planName}】吗？删除后不可恢复。`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deletePlan(row.id)
      ElMessage.success('删除成功')
      handleQuery()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 状态变更
async function handleStatusChange(row: MembershipPlan) {
  try {
    await updatePlanStatus(row.id, row.status)
    ElMessage.success(row.status === 1 ? '已启用' : '已禁用')
  } catch (error) {
    row.status = row.status === 1 ? 0 : 1
    ElMessage.error('状态更新失败')
  }
}

// 提交表单
async function handleSubmit() {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitLoading.value = true
    try {
      if (isEdit.value) {
        await updatePlan((formData as any).id, formData)
        ElMessage.success('更新成功')
      } else {
        await createPlan(formData)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      handleQuery()
    } catch (error: any) {
      ElMessage.error(error.message || '操作失败')
    } finally {
      submitLoading.value = false
    }
  })
}

// 重置表单
function resetForm() {
  formData.planName = ''
  formData.planKey = ''
  formData.tier = 1
  formData.durationDays = 30
  formData.price = 0
  formData.originalPrice = 0
  formData.benefits = ''
  formData.description = ''
  formData.isRecommended = false
  formData.sortOrder = 0
  formRef.value?.clearValidate()
}

onMounted(() => {
  handleQuery()
})
</script>

<style scoped lang="scss">
.membership-plan-page {
  .search-card {
    margin-bottom: 16px;
  }

  .table-card {
    .price {
      color: #f56c6c;
      font-weight: 500;
      font-size: 14px;
    }

    .original-price {
      margin-left: 8px;
      color: #909399;
      font-size: 12px;
      text-decoration: line-through;
    }

    .pagination-wrapper {
      margin-top: 16px;
      display: flex;
      justify-content: flex-end;
    }
  }
}
</style>
