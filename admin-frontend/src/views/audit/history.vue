<template>
  <div class="audit-history-page">
    <!-- 搜索筛选区 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryForm" inline>
        <el-form-item label="审核员">
          <el-input
            v-model="queryForm.auditorId"
            placeholder="请输入审核员ID"
            clearable
            style="width: 150px"
          />
        </el-form-item>
        <el-form-item label="审核结果">
          <el-select v-model="queryForm.auditStatus" placeholder="全部" clearable style="width: 120px">
            <el-option label="通过" :value="1" />
            <el-option label="拒绝" :value="2" />
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
            <div class="stat-label">今日审核</div>
            <div class="stat-value">{{ statistics.todayAuditCount }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">通过率</div>
            <div class="stat-value success">{{ statistics.approveRate }}%</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">平均处理时长</div>
            <div class="stat-value warning">{{ statistics.avgProcessTime }}秒</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">待审核数量</div>
            <div class="stat-value danger">{{ statistics.pendingCount }}</div>
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
        <el-table-column prop="contentId" label="内容ID" width="100" />
        <el-table-column prop="contentTitle" label="内容标题" width="200" show-overflow-tooltip />
        <el-table-column prop="auditorName" label="审核员" width="120" />
        <el-table-column label="审核结果" width="100">
          <template #default="{ row }">
            <el-tag :type="row.auditStatus === 1 ? 'success' : 'danger'">
              {{ row.auditStatus === 1 ? '通过' : '拒绝' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="auditComment" label="审核意见" width="200" show-overflow-tooltip />
        <el-table-column label="处理时长" width="100">
          <template #default="{ row }">
            {{ formatProcessTime(row.processTime) }}
          </template>
        </el-table-column>
        <el-table-column label="审核来源" width="100">
          <template #default="{ row }">
            {{ row.auditSource || 'WEB' }}
          </template>
        </el-table-column>
        <el-table-column prop="auditTime" label="审核时间" width="180" />
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

    <!-- 审核员工作量统计 -->
    <el-card class="chart-card" shadow="never">
      <div class="card-header">
        <span class="card-title">审核员工作量统计</span>
        <el-date-picker
          v-model="chartDateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          style="width: 240px"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          @change="loadAuditorWorkload"
        />
      </div>
      <div ref="auditorChartRef" class="chart-container"></div>
    </el-card>

    <!-- 审核趋势统计 -->
    <el-card class="chart-card" shadow="never">
      <div class="card-header">
        <span class="card-title">审核趋势统计</span>
      </div>
      <div ref="trendChartRef" class="chart-container"></div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import type { AuditHistory, AuditHistoryQueryDTO } from '@/types/api'
import {
  getAuditHistory,
  getAuditStatistics
} from '@/api/audit'
import * as echarts from 'echarts'
import type { EChartsOption } from 'echarts'
import dayjs from 'dayjs'

// 查询表单
const queryForm = reactive<AuditHistoryQueryDTO>({
  auditorId: undefined,
  auditStatus: undefined,
  dateRange: []
})

// 统计数据
const statistics = reactive({
  todayAuditCount: 0,
  approveRate: 0,
  avgProcessTime: 0,
  pendingCount: 0
})

// 表格数据
const loading = ref(false)
const tableData = ref<AuditHistory[]>([])
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 图表日期范围
const chartDateRange = ref<string[]>([
  dayjs().subtract(7, 'day').format('YYYY-MM-DD'),
  dayjs().format('YYYY-MM-DD')
])

// 图表引用
const auditorChartRef = ref<HTMLElement>()
const trendChartRef = ref<HTMLElement>()

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

    const res = await getAuditHistory(params)
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
  queryForm.auditorId = undefined
  queryForm.auditStatus = undefined
  queryForm.dateRange = []
  pagination.current = 1
  handleQuery()
}

// 加载统计数据
async function loadStatistics() {
  try {
    const res = await getAuditStatistics({})
    Object.assign(statistics, res.data)
  } catch (error) {
    console.error('加载统计数据失败', error)
  }
}

// 格式化处理时长
function formatProcessTime(milliseconds: number): string {
  if (!milliseconds) return '-'
  const seconds = Math.floor(milliseconds / 1000)
  if (seconds < 60) {
    return `${seconds}秒`
  } else if (seconds < 3600) {
    const minutes = Math.floor(seconds / 60)
    return `${minutes}分钟`
  } else {
    const hours = Math.floor(seconds / 3600)
    const minutes = Math.floor((seconds % 3600) / 60)
    return `${hours}小时${minutes}分钟`
  }
}

// 加载审核员工作量
async function loadAuditorWorkload() {
  if (!auditorChartRef.value) return

  try {
    const res = await getAuditStatistics({
      startDate: chartDateRange.value[0],
      endDate: chartDateRange.value[1]
    })

    const chart = echarts.init(auditorChartRef.value)
    const option: EChartsOption = {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        }
      },
      legend: {
        data: ['通过数量', '拒绝数量', '总数量']
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: res.data.auditorStats?.map(item => item.auditorName) || []
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: '通过数量',
          type: 'bar',
          data: res.data.auditorStats?.map(item => item.approveCount) || [],
          itemStyle: { color: '#67c23a' }
        },
        {
          name: '拒绝数量',
          type: 'bar',
          data: res.data.auditorStats?.map(item => item.rejectCount) || [],
          itemStyle: { color: '#f56c6c' }
        },
        {
          name: '总数量',
          type: 'line',
          data: res.data.auditorStats?.map(item => item.totalCount) || [],
          itemStyle: { color: '#409eff' }
        }
      ]
    }
    chart.setOption(option)
  } catch (error) {
    console.error('加载审核员工作量失败', error)
  }
}

// 加载审核趋势
async function loadAuditTrend() {
  if (!trendChartRef.value) return

  try {
    const res = await getAuditStatistics({
      startDate: dayjs().subtract(30, 'day').format('YYYY-MM-DD'),
      endDate: dayjs().format('YYYY-MM-DD')
    })

    const chart = echarts.init(trendChartRef.value)
    const option: EChartsOption = {
      tooltip: {
        trigger: 'axis'
      },
      legend: {
        data: ['审核总数', '通过数', '拒绝数', '通过率']
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
        data: res.data.trendStats?.map(item => item.date) || []
      },
      yAxis: [
        {
          type: 'value',
          name: '数量',
          position: 'left'
        },
        {
          type: 'value',
          name: '通过率(%)',
          position: 'right',
          min: 0,
          max: 100
        }
      ],
      series: [
        {
          name: '审核总数',
          type: 'line',
          data: res.data.trendStats?.map(item => item.totalCount) || [],
          smooth: true,
          itemStyle: { color: '#409eff' }
        },
        {
          name: '通过数',
          type: 'line',
          data: res.data.trendStats?.map(item => item.approveCount) || [],
          smooth: true,
          itemStyle: { color: '#67c23a' }
        },
        {
          name: '拒绝数',
          type: 'line',
          data: res.data.trendStats?.map(item => item.rejectCount) || [],
          smooth: true,
          itemStyle: { color: '#f56c6c' }
        },
        {
          name: '通过率',
          type: 'line',
          yAxisIndex: 1,
          data: res.data.trendStats?.map(item => item.approveRate) || [],
          smooth: true,
          itemStyle: { color: '#e6a23c' }
        }
      ]
    }
    chart.setOption(option)
  } catch (error) {
    console.error('加载审核趋势失败', error)
  }
}

onMounted(async () => {
  await handleQuery()
  await loadStatistics()

  await nextTick()
  await loadAuditorWorkload()
  await loadAuditTrend()

  // 监听窗口大小变化
  window.addEventListener('resize', () => {
    if (auditorChartRef.value) {
      echarts.getInstanceByDom(auditorChartRef.value)?.resize()
    }
    if (trendChartRef.value) {
      echarts.getInstanceByDom(trendChartRef.value)?.resize()
    }
  })
})
</script>

<style scoped lang="scss">
.audit-history-page {
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
    margin-bottom: 16px;

    .pagination-wrapper {
      margin-top: 16px;
      display: flex;
      justify-content: flex-end;
    }
  }

  .chart-card {
    margin-bottom: 16px;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;

      .card-title {
        font-size: 16px;
        font-weight: 500;
        color: #303133;
      }
    }

    .chart-container {
      width: 100%;
      height: 400px;
    }
  }
}
</style>
