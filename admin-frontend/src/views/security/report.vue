<template>
  <div class="report-page">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">今日举报</div>
            <div class="stat-value">{{ statistics.todayCount }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">待处理</div>
            <div class="stat-value danger">{{ statistics.pendingCount }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">已处理</div>
            <div class="stat-value success">{{ statistics.handledCount }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">有效率</div>
            <div class="stat-value warning">{{ statistics.validRate }}%</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索筛选区 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryForm" inline>
        <el-form-item label="举报类型">
          <el-select v-model="queryForm.reportType" placeholder="全部" clearable style="width: 120px">
            <el-option v-for="(name, key) in ReportTypeMap" :key="key" :label="name" :value="Number(key)" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option v-for="(name, key) in ReportStatusMap" :key="key" :label="name" :value="Number(key)" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="queryForm.priority" placeholder="全部" clearable style="width: 100px">
            <el-option v-for="(name, key) in ReportPriorityMap" :key="key" :label="name" :value="Number(key)" />
          </el-select>
        </el-form-item>
        <el-form-item label="举报时间">
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
      <el-table v-loading="loading" :data="tableData" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="举报类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getReportTypeTagType(row.reportType)">
              {{ ReportTypeMap[row.reportType] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="contentTitle" label="被举报内容" width="200" show-overflow-tooltip />
        <el-table-column prop="reporterName" label="举报人" width="100" />
        <el-table-column label="举报人信誉" width="100">
          <template #default="{ row }">
            <el-tag :type="getCredibilityType(row.reporterCredibility)" size="small">
              {{ row.reporterCredibility }}分
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="举报描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="证据" width="80">
          <template #default="{ row }">
            <el-button
              v-if="row.evidenceUrls && row.evidenceUrls.length > 0"
              link
              type="primary"
              size="small"
              @click="showEvidence(row)"
            >
              查看({{ row.evidenceUrls.length }})
            </el-button>
            <span v-else style="color: #909399">-</span>
          </template>
        </el-table-column>
        <el-table-column label="优先级" width="80">
          <template #default="{ row }">
            <el-tag :type="getPriorityTagType(row.priority)" size="small">
              {{ ReportPriorityMap[row.priority] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ ReportStatusMap[row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="举报时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button link type="success" size="small" v-permission="['security:report:handle']" @click="handleValid(row)">
                有效
              </el-button>
              <el-button link type="warning" size="small" v-permission="['security:report:handle']" @click="handleInvalid(row)">
                无效
              </el-button>
              <el-button link type="info" size="small" v-permission="['security:report:handle']" @click="handleIgnore(row)">
                忽略
              </el-button>
            </template>
            <el-button v-else link type="primary" size="small" @click="showDetail(row)">
              详情
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

    <!-- 举报类型分布图表 -->
    <el-card class="chart-card" shadow="never">
      <template #header>
        <span>举报类型分布</span>
      </template>
      <div ref="typeChartRef" class="chart-container"></div>
    </el-card>

    <!-- 证据查看对话框 -->
    <el-dialog v-model="evidenceDialogVisible" title="证据截图" width="800px">
      <div class="evidence-gallery">
        <el-image
          v-for="(url, index) in currentEvidence"
          :key="index"
          :src="url"
          :preview-src-list="currentEvidence"
          :initial-index="index"
          fit="contain"
          style="width: 200px; height: 200px; margin: 8px"
        />
      </div>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="举报详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="举报ID">{{ currentReport?.id }}</el-descriptions-item>
        <el-descriptions-item label="举报类型">
          <el-tag :type="getReportTypeTagType(currentReport?.reportType || 0)">
            {{ ReportTypeMap[currentReport?.reportType || 0] }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="被举报内容" :span="2">{{ currentReport?.contentTitle }}</el-descriptions-item>
        <el-descriptions-item label="举报人">{{ currentReport?.reporterName }}</el-descriptions-item>
        <el-descriptions-item label="举报人信誉">{{ currentReport?.reporterCredibility }}分</el-descriptions-item>
        <el-descriptions-item label="举报描述" :span="2">{{ currentReport?.description }}</el-descriptions-item>
        <el-descriptions-item label="举报时间">{{ currentReport?.createTime }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusTagType(currentReport?.status || 0)">
            {{ ReportStatusMap[currentReport?.status || 0] }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="处理人">{{ currentReport?.handlerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处理时间">{{ currentReport?.handleTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处理结果" :span="2">{{ currentReport?.handleResult || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 处理对话框 -->
    <el-dialog
      v-model="handleDialogVisible"
      :title="handleDialogTitle"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="handleForm" label-width="100px">
        <el-form-item label="处理结果">
          <el-input v-model="handleForm.handleResult" type="textarea" :rows="4" placeholder="请输入处理结果说明" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="handleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitHandle">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { ContentReport, ContentReportQueryDTO, ReportStatistics } from '@/types/security'
import { ReportTypeMap, ReportStatusMap, ReportPriorityMap } from '@/types/security'
import { getReportList, handleReport, getReportStatistics } from '@/api/security'
import * as echarts from 'echarts'
import type { EChartsOption } from 'echarts'

// 统计数据
const statistics = reactive<ReportStatistics>({
  todayCount: 0,
  pendingCount: 0,
  handledCount: 0,
  validRate: 0,
  typeDistribution: [],
  trendData: []
})

// 查询表单
const queryForm = reactive<ContentReportQueryDTO>({
  reportType: undefined,
  status: undefined,
  priority: undefined,
  dateRange: []
})

// 表格数据
const loading = ref(false)
const tableData = ref<ContentReport[]>([])
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 图表
const typeChartRef = ref<HTMLElement>()

// 证据对话框
const evidenceDialogVisible = ref(false)
const currentEvidence = ref<string[]>([])

// 详情对话框
const detailDialogVisible = ref(false)
const currentReport = ref<ContentReport | null>(null)

// 处理对话框
const handleDialogVisible = ref(false)
const handleDialogTitle = ref('')
const submitLoading = ref(false)
const handleForm = reactive({
  reportId: 0,
  status: 1,
  handleResult: ''
})

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

    const res = await getReportList(params)
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
  queryForm.reportType = undefined
  queryForm.status = undefined
  queryForm.priority = undefined
  queryForm.dateRange = []
  pagination.current = 1
  handleQuery()
}

// 加载统计数据
async function loadStatistics() {
  try {
    const res = await getReportStatistics()
    Object.assign(statistics, res.data)
    await nextTick()
    renderTypeChart()
  } catch (error) {
    console.error('加载统计数据失败', error)
  }
}

// 渲染类型分布图表
function renderTypeChart() {
  if (!typeChartRef.value) return

  const chart = echarts.init(typeChartRef.value)
  const option: EChartsOption = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '举报类型',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}: {c}'
        },
        data: statistics.typeDistribution.map(item => ({
          name: item.type,
          value: item.count
        }))
      }
    ]
  }
  chart.setOption(option)
}

// 查看证据
function showEvidence(row: ContentReport) {
  currentEvidence.value = row.evidenceUrls || []
  evidenceDialogVisible.value = true
}

// 查看详情
function showDetail(row: ContentReport) {
  currentReport.value = row
  detailDialogVisible.value = true
}

// 标记为有效
function handleValid(row: ContentReport) {
  handleDialogTitle.value = '标记为有效举报'
  handleForm.reportId = row.id
  handleForm.status = 1
  handleForm.handleResult = ''
  handleDialogVisible.value = true
}

// 标记为无效
function handleInvalid(row: ContentReport) {
  handleDialogTitle.value = '标记为无效举报'
  handleForm.reportId = row.id
  handleForm.status = 2
  handleForm.handleResult = ''
  handleDialogVisible.value = true
}

// 忽略举报
function handleIgnore(row: ContentReport) {
  ElMessageBox.confirm('确定要忽略该举报吗？', '忽略确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await handleReport(row.id, { status: 3, handleResult: '已忽略' })
      ElMessage.success('已忽略')
      handleQuery()
      loadStatistics()
    } catch (error) {
      ElMessage.error('操作失败')
    }
  }).catch(() => {})
}

// 提交处理
async function submitHandle() {
  submitLoading.value = true
  try {
    await handleReport(handleForm.reportId, {
      status: handleForm.status,
      handleResult: handleForm.handleResult
    })
    ElMessage.success('处理成功')
    handleDialogVisible.value = false
    handleQuery()
    loadStatistics()
  } catch (error: any) {
    ElMessage.error(error.message || '处理失败')
  } finally {
    submitLoading.value = false
  }
}

// 获取举报类型标签类型
function getReportTypeTagType(type: number): string {
  const map: Record<number, string> = {
    1: 'danger',
    2: 'danger',
    3: 'warning',
    4: '',
    5: 'info',
    6: 'info'
  }
  return map[type] || ''
}

// 获取信誉分标签类型
function getCredibilityType(score: number): string {
  if (score >= 80) return 'success'
  if (score >= 60) return 'warning'
  return 'danger'
}

// 获取优先级标签类型
function getPriorityTagType(priority: number): string {
  const map: Record<number, string> = {
    1: 'info',
    2: 'warning',
    3: 'danger'
  }
  return map[priority] || ''
}

// 获取状态标签类型
function getStatusTagType(status: number): string {
  const map: Record<number, string> = {
    0: 'warning',
    1: 'success',
    2: 'danger',
    3: 'info'
  }
  return map[status] || ''
}

onMounted(() => {
  handleQuery()
  loadStatistics()

  window.addEventListener('resize', () => {
    if (typeChartRef.value) {
      echarts.getInstanceByDom(typeChartRef.value)?.resize()
    }
  })
})
</script>

<style scoped lang="scss">
.report-page {
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

        &.success { color: #67c23a; }
        &.warning { color: #e6a23c; }
        &.danger { color: #f56c6c; }
      }
    }
  }

  .search-card {
    margin-bottom: 16px;
  }

  .table-card {
    margin-bottom: 16px;

    .pagination-wrapper {
      margin-top: 16px;
      display: flex;
      justify-content: flex-end;
    }
  }

  .chart-card {
    .chart-container {
      width: 100%;
      height: 300px;
    }
  }

  .evidence-gallery {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
  }
}
</style>
