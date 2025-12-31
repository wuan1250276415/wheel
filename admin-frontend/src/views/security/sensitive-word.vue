<template>
  <div class="sensitive-word-page">
    <!-- 搜索筛选区 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryForm" inline>
        <el-form-item label="关键词">
          <el-input
            v-model="queryForm.keyword"
            placeholder="请输入敏感词"
            clearable
            style="width: 150px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="queryForm.category" placeholder="全部" clearable style="width: 120px">
            <el-option v-for="(name, key) in SensitiveWordCategoryMap" :key="key" :label="name" :value="Number(key)" />
          </el-select>
        </el-form-item>
        <el-form-item label="风险等级">
          <el-select v-model="queryForm.level" placeholder="全部" clearable style="width: 100px">
            <el-option v-for="(name, key) in SensitiveWordLevelMap" :key="key" :label="name" :value="Number(key)" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 100px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <el-button type="primary" v-permission="['security:sensitive:add']" @click="handleAdd">
          <el-icon><Plus /></el-icon>添加敏感词
        </el-button>
        <el-button
          type="danger"
          v-permission="['security:sensitive:delete']"
          :disabled="selectedIds.length === 0"
          @click="handleBatchDelete"
        >
          批量删除（{{ selectedIds.length }}）
        </el-button>
        <el-button type="warning" v-permission="['security:sensitive:refresh']" @click="handleRefreshCache">
          <el-icon><Refresh /></el-icon>刷新缓存
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
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="word" label="敏感词" width="150" />
        <el-table-column label="分类" width="100">
          <template #default="{ row }">
            <el-tag :type="getCategoryTagType(row.category)">
              {{ SensitiveWordCategoryMap[row.category] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="风险等级" width="100">
          <template #default="{ row }">
            <el-tag :type="getLevelTagType(row.level)">
              {{ SensitiveWordLevelMap[row.level] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="变体词" min-width="200">
          <template #default="{ row }">
            <template v-if="row.variants && row.variants.length > 0">
              <el-tag v-for="v in row.variants.slice(0, 3)" :key="v" size="small" style="margin-right: 4px">
                {{ v }}
              </el-tag>
              <span v-if="row.variants.length > 3" style="color: #909399">
                +{{ row.variants.length - 3 }}
              </span>
            </template>
            <span v-else style="color: #909399">-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              v-permission="['security:sensitive:edit']"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" v-permission="['security:sensitive:edit']" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button link type="danger" size="small" v-permission="['security:sensitive:delete']" @click="handleDelete(row)">
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

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="敏感词" prop="word">
          <el-input v-model="formData.word" placeholder="请输入敏感词" maxlength="100" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="formData.category" placeholder="请选择分类" style="width: 100%">
            <el-option v-for="(name, key) in SensitiveWordCategoryMap" :key="key" :label="name" :value="Number(key)" />
          </el-select>
        </el-form-item>
        <el-form-item label="风险等级" prop="level">
          <el-radio-group v-model="formData.level">
            <el-radio :label="1">低</el-radio>
            <el-radio :label="2">中</el-radio>
            <el-radio :label="3">高</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="变体词">
          <el-select
            v-model="formData.variants"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="输入后按回车添加"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="formData.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import type { SensitiveWord, SensitiveWordQueryDTO, SensitiveWordCreateDTO } from '@/types/security'
import { SensitiveWordCategoryMap, SensitiveWordLevelMap } from '@/types/security'
import {
  getSensitiveWordList,
  addSensitiveWord,
  updateSensitiveWord,
  deleteSensitiveWord,
  batchDeleteSensitiveWords,
  refreshSensitiveWordCache
} from '@/api/security'

// 查询表单
const queryForm = reactive<SensitiveWordQueryDTO>({
  keyword: '',
  category: undefined,
  level: undefined,
  status: undefined
})

// 表格数据
const loading = ref(false)
const tableData = ref<SensitiveWord[]>([])
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 选中的ID列表
const selectedIds = ref<number[]>([])

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const submitLoading = ref(false)
const editingId = ref<number | null>(null)

const formData = reactive<SensitiveWordCreateDTO>({
  word: '',
  category: 5,
  level: 2,
  variants: [],
  status: 1
})

const formRules: FormRules = {
  word: [{ required: true, message: '请输入敏感词', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  level: [{ required: true, message: '请选择风险等级', trigger: 'change' }]
}

// 查询列表
async function handleQuery() {
  loading.value = true
  try {
    const params = {
      ...queryForm,
      current: pagination.current,
      size: pagination.size
    }
    const res = await getSensitiveWordList(params)
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
  queryForm.keyword = ''
  queryForm.category = undefined
  queryForm.level = undefined
  queryForm.status = undefined
  pagination.current = 1
  handleQuery()
}

// 选择变更
function handleSelectionChange(selection: SensitiveWord[]) {
  selectedIds.value = selection.map(item => item.id)
}

// 添加
function handleAdd() {
  dialogTitle.value = '添加敏感词'
  editingId.value = null
  formData.word = ''
  formData.category = 5
  formData.level = 2
  formData.variants = []
  formData.status = 1
  dialogVisible.value = true
}

// 编辑
function handleEdit(row: SensitiveWord) {
  dialogTitle.value = '编辑敏感词'
  editingId.value = row.id
  formData.word = row.word
  formData.category = row.category
  formData.level = row.level
  formData.variants = row.variants || []
  formData.status = row.status
  dialogVisible.value = true
}

// 提交表单
async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate()

  submitLoading.value = true
  try {
    if (editingId.value) {
      await updateSensitiveWord(editingId.value, formData)
      ElMessage.success('更新成功')
    } else {
      await addSensitiveWord(formData)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    handleQuery()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}

// 状态变更
async function handleStatusChange(row: SensitiveWord) {
  try {
    await updateSensitiveWord(row.id, {
      word: row.word,
      category: row.category,
      level: row.level,
      variants: row.variants,
      status: row.status
    })
    ElMessage.success('状态更新成功')
  } catch (error) {
    row.status = row.status === 1 ? 0 : 1
    ElMessage.error('状态更新失败')
  }
}

// 删除
function handleDelete(row: SensitiveWord) {
  ElMessageBox.confirm(`确定要删除敏感词"${row.word}"吗？`, '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteSensitiveWord(row.id)
      ElMessage.success('删除成功')
      handleQuery()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 批量删除
function handleBatchDelete() {
  ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 个敏感词吗？`, '批量删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await batchDeleteSensitiveWords(selectedIds.value)
      ElMessage.success('批量删除成功')
      handleQuery()
    } catch (error) {
      ElMessage.error('批量删除失败')
    }
  }).catch(() => {})
}

// 刷新缓存
async function handleRefreshCache() {
  try {
    await refreshSensitiveWordCache()
    ElMessage.success('缓存刷新成功')
  } catch (error) {
    ElMessage.error('缓存刷新失败')
  }
}

// 获取分类标签类型
function getCategoryTagType(category: number): string {
  const map: Record<number, string> = {
    1: 'danger',
    2: 'warning',
    3: 'info',
    4: '',
    5: 'info'
  }
  return map[category] || ''
}

// 获取等级标签类型
function getLevelTagType(level: number): string {
  const map: Record<number, string> = {
    1: 'info',
    2: 'warning',
    3: 'danger'
  }
  return map[level] || ''
}

onMounted(() => {
  handleQuery()
})
</script>

<style scoped lang="scss">
.sensitive-word-page {
  .search-card {
    margin-bottom: 16px;

    .action-buttons {
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
