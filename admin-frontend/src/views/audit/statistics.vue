<template>
  <div class="audit-statistics-page">
    <!-- 时间范围选择 -->
    <el-card class="filter-card" shadow="never">
      <el-form inline>
        <el-form-item label="统计周期">
          <el-radio-group v-model="periodType" @change="handlePeriodChange">
            <el-radio-button label="today">今日</el-radio-button>
            <el-radio-button label="week">本周</el-radio-button>
            <el-radio-button label="month">本月</el-radio-button>
            <el-radio-button label="custom">自定义</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="periodType === 'custom'" label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 240px"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="loadStatistics"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleExport">
            <el-icon><Download /></el-icon>导出报表
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计概览卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-icon" style="background: #409eff">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.totalAuditCount }}</div>
              <div class="stat-label">审核总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-icon" style="background: #67c23a">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value success">{{ statistics.approveRate }}%</div>
              <div class="stat-label">通过率</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-icon" style="background: #e6a23c">
              <el-icon><Timer /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value warning">{{ formatTime(statistics.avgProcessTime) }}</div>
              <div class="stat-label">平均处理时长</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-icon" style="background: #f56c6c">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value danger">{{ statistics.pendingCount }}</div>
              <div class="stat-label">待审核数量</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="16">
      <!-- 审核趋势图 -->
      <el-col :span="16">
        <el-card class="chart-card" shadow="never">
          <template #header>
            <span>审核趋势</span>
          </template>
          <div ref="trendChartRef" class="chart-container"></div>
        </el-card>
      </el-col>

      <!-- 违规类型分布 -->
      <el-col :span="8">
        <el-card class="chart-card" shadow="never">
          <template #header>
            <span>违规类型分布</span>
          </template>
          <div ref="violationChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 审核员工作量统计 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <span>审核员工作量统计</span>
      </template>
      <el-table :data="auditorStats" border>
        <el-table-column prop="auditorName" label="审核员" width="120" />
        <el-table-column prop="totalCount" label="审核总数" width="100" sortable />
        <el-table-column prop="approveCount" label="通过数" width="100">
          <template #default="{ row }">
            <span style="color: #67c23a">{{ row.approveCount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="rejectCount" label="拒绝数" width="100">
          <template #default="{ row }">
            <span style="color: #f56c6c">{{ row.rejectCount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="通过率" width="120">
          <template #default="{ row }">
            <el-progress
              :percentage="row.approvalRate"
              :color="getProgressColor(row.approvalRate)"
              :stroke-width="10"
            />
          </template>
        </el-table-column>
        <el-table-column label="平均处理时长" width="120">
          <template #default="{ row }">
            {{ formatTime(row.avgProcessTime) }}
          </template>
        </el-table-column>
        <el-table-column label="工作量占比" min-width="150">
          <template #default="{ row }">
            <el-progress
              :percentage="getWorkloadPercentage(row.totalCount)"
              :stroke-width="10"
            />
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 导出对话框 -->
    <el-dialog v-model="exportDialogVisible" title="导出报表" width="400px">
      <el-form label-width="100px">
        <el-form-item label="导出格式">
          <el-radio-group v-model="exportFormat">
            <el-radio label="excel">Excel (.xlsx)</el-radio>
            <el-radio label="pdf">PDF</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="导出内容">
          <el-checkbox-group v-model="exportContent">
            <el-checkbox label="overview">统计概览</el-checkbox>
            <el-checkbox label="trend">审核趋势</el-checkbox>
            <el-checkbox label="violation">违规分布</el-checkbox>
            <el-checkbox label="auditor">审核员统计</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="exportDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="exportLoading" @click="confirmExport">确定导出</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, CircleCheck, Timer, Warning, Download } from '@element-plus/icons-vue'
import type { AuditStatistics, AuditorWorkload } from '@/types/api'
import { getAuditStatistics } from '@/api/audit'
import { exportAuditReport } from '@/api/security'
import * as echarts from 'echarts'
import type { EChartsOption } from 'echarts'
import dayjs from 'dayjs'

// 统计周期
const periodType = ref('today')
const dateRange = ref<string[]>([])

// 统计数据
const statistics = reactive<AuditStatistics>({
  todayAuditCount: 0,
  totalAuditCount: 0,
  approveRate: 0,
  avgProcessTime: 0,
  pendingCount: 0,
  auditorStats: [],
  trendStats: []
})

// 审核员统计
const auditorStats = ref<AuditorWorkload[]>([])

// 图表引用
const trendChartRef = ref<HTMLElement>()
const violationChartRef = ref<HTMLElement>()

// 导出相关
const exportDialogVisible = ref(false)
const exportFormat = ref('excel')
const exportContent = ref(['overview', 'trend', 'violation', 'auditor'])
const exportLoading = ref(false)

// 计算总工作量
const totalWorkload = computed(() => {
  return auditorStats.value.reduce((sum, item) => sum + item.totalCount, 0)
})

// 处理周期变更
function handlePeriodChange(type: string) {
  const now = dayjs()
  switch (type) {
    case 'today':
      dateRange.value = [now.format('YYYY-MM-DD'), now.format('YYYY-MM-DD')]
      break
    case 'week':
      dateRange.value = [now.startOf('week').format('YYYY-MM-DD'), now.format('YYYY-MM-DD')]
      break
    case 'month':
      dateRange.value = [now.startOf('month').format('YYYY-MM-DD'), now.format('YYYY-MM-DD')]
      break
    case 'custom':
      dateRange.value = []
      return
  }
  loadStatistics()
}

// 加载统计数据
async function loadStatistics() {
  if (periodType.value === 'custom' && dateRange.value.length !== 2) return

  try {
    const params: any = {}
    if (dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }

    const res = await getAuditStatistics(params)
    Object.assign(statistics, res.data)
    statistics.totalAuditCount = res.data.todayAuditCount || 0
    auditorStats.value = res.data.auditorStats || []

    await nextTick()
    renderTrendChart()
    renderViolationChart()
  } catch (error) {
    ElMessage.error('加载统计数据失败')
  }
}

// 渲染趋势图
function renderTrendChart() {
  if (!trendChartRef.value) return

  const chart = echarts.init(trendChartRef.value)
  const option: EChartsOption = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['审核总数', '通过数', '拒绝数']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: statistics.trendStats?.map(item => item.date) || []
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '审核总数',
        type: 'line',
        data: statistics.trendStats?.map(item => item.totalCount) || [],
        smooth: true,
        itemStyle: { color: '#409eff' },
        areaStyle: { color: 'rgba(64, 158, 255, 0.1)' }
      },
      {
        name: '通过数',
        type: 'line',
        data: statistics.trendStats?.map(item => item.approveCount) || [],
        smooth: true,
        itemStyle: { color: '#67c23a' }
      },
      {
        name: '拒绝数',
        type: 'line',
        data: statistics.trendStats?.map(item => item.rejectCount) || [],
        smooth: true,
        itemStyle: { color: '#f56c6c' }
      }
    ]
  }
  chart.setOption(option)
}

// 渲染违规类型分布图
function renderViolationChart() {
  if (!violationChartRef.value) return

  const chart = echarts.init(violationChartRef.value)
  // 模拟违规类型数据
  const violationData = [
    { name: '色情低俗', value: 35 },
    { name: '广告推广', value: 28 },
    { name: '暴力血腥', value: 15 },
    { name: '政治敏感', value: 12 },
    { name: '其他', value: 10 }
  ]

  const option: EChartsOption = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      top: 'center'
    },
    series: [
      {
        name: '违规类型',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['60%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold'
          }
        },
        data: violationData
      }
    ]
  }
  chart.setOption(option)
}

// 格式化时间
function formatTime(milliseconds: number): string {
  if (!milliseconds) return '-'
  const seconds = Math.floor(milliseconds / 1000)
  if (seconds < 60) return `${seconds}秒`
  if (seconds < 3600) return `${Math.floor(seconds / 60)}分钟`
  return `${Math.floor(seconds / 3600)}小时`
}

// 获取进度条颜色
function getProgressColor(percentage: number): string {
  if (percentage >= 80) return '#67c23a'
  if (percentage >= 60) return '#e6a23c'
  return '#f56c6c'
}

// 获取工作量百分比
function getWorkloadPercentage(count: number): number {
  if (totalWorkload.value === 0) return 0
  return Math.round((count / totalWorkload.value) * 100)
}

// 导出报表
function handleExport() {
  exportDialogVisible.value = true
}

// 确认导出
async function confirmExport() {
  exportLoading.value = true
  try {
    const params = {
      format: exportFormat.value,
      content: exportContent.value,
      startDate: dateRange.value[0],
      endDate: dateRange.value[1]
    }
    await exportAuditReport(params)
    ElMessage.success('导出成功，请查看下载文件')
    exportDialogVisible.value = false
  } catch (error) {
    ElMessage.error('导出失败')
  } finally {
    exportLoading.value = false
  }
}

onMounted(() => {
  // 默认加载今日数据
  handlePeriodChange('today')

  // 监听窗口大小变化
  window.addEventListener('resize', () => {
    if (trendChartRef.value) {
      echarts.getInstanceByDom(trendChartRef.value)?.resize()
    }
    if (violationChartRef.value) {
      echarts.getInstanceByDom(violationChartRef.value)?.resize()
    }
  })
})
</script>

<style scoped lang="scss">
.audit-statistics-page {
  .filter-card {
    margin-bottom: 16px;
  }

  .stat-row {
    margin-bottom: 16px;

    .stat-item {
      display: flex;
      align-items: center;
      padding: 10px 0;

      .stat-icon {
        width: 48px;
        height: 48px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 16px;

        .el-icon {
          font-size: 24px;
          color: #fff;
        }
      }

      .stat-content {
        .stat-value {
          font-size: 24px;
          font-weight: 600;
          color: #303133;

          &.success { color: #67c23a; }
          &.warning { color: #e6a23c; }
          &.danger { color: #f56c6c; }
        }

        .stat-label {
          font-size: 14px;
          color: #909399;
          margin-top: 4px;
        }
      }
    }
  }

  .chart-card {
    margin-bottom: 16px;

    .chart-container {
      width: 100%;
      height: 350px;
    }
  }

  .table-card {
    margin-bottom: 16px;
  }
}
</style>
