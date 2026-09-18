<template>
  <div class="dashboard-container">
    <!-- 页面标题 -->
    <div class="dashboard-header">
      <h2>控制台首页</h2>
      <p>欢迎使用医疗预约管理系统，今天是 {{ today }}</p>
    </div>
    <!-- 数据统计卡片：替换总管理员数为总患者数 -->
    <div class="stat-card-group">
      <el-card class="stat-card" shadow="hover" v-loading="statLoading">
        <div class="card-content">
          <div class="left">
            <p class="label">总患者数</p>
            <p class="num">{{ statData.patientCount }}</p>
          </div>
          <div class="right blue">
            <el-icon><User /></el-icon>
          </div>
        </div>
      </el-card>
      <el-card class="stat-card" shadow="hover" v-loading="statLoading">
        <div class="card-content">
          <div class="left">
            <p class="label">总科室数</p>
            <p class="num">{{ statData.deptCount }}</p>
          </div>
          <div class="right green">
            <el-icon><OfficeBuilding /></el-icon>
          </div>
        </div>
      </el-card>
      <el-card class="stat-card" shadow="hover" v-loading="statLoading">
        <div class="card-content">
          <div class="left">
            <p class="label">总医生数</p>
            <p class="num">{{ statData.doctorCount }}</p>
          </div>
          <div class="right orange">
            <el-icon><UserFilled /></el-icon>
          </div>
        </div>
      </el-card>
      <el-card class="stat-card" shadow="hover" v-loading="statLoading">
        <div class="card-content">
          <div class="left">
            <p class="label">今日预约数</p>
            <p class="num">{{ statData.todayAppointCount }}</p>
          </div>
          <div class="right purple">
            <el-icon><Calendar /></el-icon>
          </div>
        </div>
      </el-card>
    </div>
    <!-- 图表 + 最新预约 -->
    <div class="main-content">
      <!-- 左侧：预约趋势图 -->
      <el-card class="chart-card" shadow="hover" v-loading="chartLoading">
        <div class="card-title">近7天就诊趋势</div>
        <div ref="chartRef" class="chart-area"></div>
      </el-card>
      <!-- 右侧：最新预约列表 -->
      <el-card class="list-card" shadow="hover">
        <div class="card-title-row">
          <div class="card-title">最新预约记录</div>
          <el-button type="primary" link @click="goAppointmentList">查看更多</el-button>
        </div>
        <el-table :data="appointList" border stripe v-loading="listLoading">
          <el-table-column label="患者姓名" prop="patientName" />
          <el-table-column label="科室" prop="deptName" />
          <el-table-column label="医生" prop="doctorName" />
          <el-table-column label="就诊日期" prop="appointTime" width="120" />
          <el-table-column label="就诊时段" prop="timeSlot" width="90" />
          <el-table-column label="状态" prop="statusText" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.statusType">
                {{ scope.row.statusText }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import { User, OfficeBuilding, UserFilled, Calendar } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import {
  getDashboardStat,
  getDashboardTrend,
  getLatestAppointments
} from '@/api/dashboard'

import {
  getAdminAppointmentList,
} from '@/api/appointment'

// 今日日期格式化
const today = dayjs().format('YYYY年MM月DD日')
const router = useRouter()
// 查看更多：跳转预约列表
const goAppointmentList = () => router.push('/appointment/list')
// 图表实例
const chartRef = ref(null)
let myChart = null

// 拆分加载状态：每个模块独立控制
const statLoading = ref(false)   // 统计卡片加载
const chartLoading = ref(false)  // 图表加载
const listLoading = ref(false)   // 预约列表加载

// 统计数据（初始值兜底）
const statData = reactive({
  patientCount: 0,
  deptCount: 0,
  doctorCount: 0,
  todayAppointCount: 0
})

// 最新预约列表（空数组兜底）
const appointList = ref([])

// 状态枚举映射：匹配后端返回的cancelled/confirmed
const statusMap = {
  cancelled: { text: '已取消', type: 'info' },
  confirmed: { text: '已确认', type: 'warning' },
  completed: { text: '已完成', type: 'success' },
  default: { text: '未知', type: 'default' }
}

// 格式化预约数据：适配后端字段 + 状态映射 + 时间格式化
const formatAppointData = (list) => {
  if (!Array.isArray(list)) return []
  return list.map(item => {
    // 状态映射
    const statusInfo = statusMap[item.status] || statusMap.default
    // 预约时间格式化（处理后端的UTC时间）
    const appointTime = item.appointmentDate ? String(item.appointmentDate).split(/[ T]/)[0] : '未设置'
    return {
      ...item,
      patientName: item.realName || '未知患者', // 后端字段realName
      appointTime: appointTime, // 格式化后的预约时间
      statusText: statusInfo.text, // 中文状态
      statusType: statusInfo.type // 标签类型
    }
  })
}

// 初始化图表（增强兜底）
const initChart = (trendData) => {
  // console.log("初始化图标接收到数据"+trendData)
  if (!chartRef.value) return; // 防止DOM未加载
  if (myChart) myChart.dispose() // 销毁旧实例
  myChart = echarts.init(chartRef.value)
  const defaultDates = ['3月9日', '3月10日', '3月11日', '3月12日', '3月13日', '3月14日', '3月15日']
  const defaultCounts = [12, 15, 18, 14, 20, 25, 30]
  
  // 强制兜底为数组，避免reduce报错
  const dates = trendData?.dates ? (Array.isArray(trendData.dates) ? trendData.dates : defaultDates) : defaultDates
  const counts = trendData?.counts ? (Array.isArray(trendData.counts) ? trendData.counts : defaultCounts) : defaultCounts
  const option = {
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value', min: 0 }, // y轴从0开始，更贴合实际
    series: [
      {
        name: '预约量',
        type: 'line',
        data: counts,
        smooth: true,
        itemStyle: { color: '#1890ff' },
        lineStyle: { width: 2 },
        symbol: 'circle',
        symbolSize: 6
      }
    ],
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true }
  }
  myChart.setOption(option)
  // 自适应窗口大小
  window.addEventListener('resize', () => myChart?.resize())
}

// 1. 获取统计数据（独立函数）
const fetchStatData = async () => {
  statLoading.value = true
  try {
    const res = await getDashboardStat()
    if (res) {
      Object.assign(statData, res.data)
    }
  } catch (e) {
    console.error('获取统计数据失败：', e)
    ElMessage.warning('统计数据加载失败，显示默认值')
    // 保留初始默认值，不覆盖
  } finally {
    statLoading.value = false
  }
}

// 2. 获取趋势数据（独立函数）
const fetchTrendData = async () => {
  chartLoading.value = true
  try {
    const res = await getDashboardTrend()
    initChart(res.data)
    console.log("获取图表数据成功"+res.data)
  } catch (e) {
    console.error('获取趋势数据失败：', e)
    ElMessage.warning('趋势图表加载失败，显示默认数据')
    initChart() // 加载默认图表
  } finally {
    chartLoading.value = false
  }
}

// 3. 获取最新预约（独立函数）
const fetchAppointData = async () => {
  listLoading.value = true
  try {
    const res = await getAdminAppointmentList()
    appointList.value = formatAppointData(res.data).slice(0, 8) // 只保留最新8条
  } catch (e) {
    console.error('获取预约列表失败：', e)
    ElMessage.warning('预约列表加载失败，显示示例数据')
    // 兜底预约列表
    appointList.value = [
      { patientName: '张三', deptName: '内科', doctorName: '李医生', appointTime: '2026-03-15', timeSlot: '上午', statusText: '待确认', statusType: 'warning' },
      { patientName: '李四', deptName: '外科', doctorName: '王医生', appointTime: '2026-03-15', timeSlot: '上午', statusText: '已完成', statusType: 'success' },
      { patientName: '王五', deptName: '儿科', doctorName: '赵医生', appointTime: '2026-03-15', timeSlot: '下午', statusText: '已取消', statusType: 'info' },
      { patientName: '陈六', deptName: '骨科', doctorName: '刘医生', appointTime: '2026-03-15', timeSlot: '下午', statusText: '待确认', statusType: 'warning' }
    ]
  } finally {
    listLoading.value = false
  }
}

const getList = async () => {
  
  try {
    const res = await getAdminAppointmentList()
    console.log(res)
    let list = res.data || []
    if (!Array.isArray(list)) list = []

    // ✅ 按预约ID从小到大排序
    list.sort((b, a) => (a.appointmentId || 0) - (b.appointmentId || 0))

  } catch (err) {
    ElMessage.error('获取失败')
  } finally {
    
  }
}

// 并行加载所有数据（互不阻塞）
const loadAllData = () => {
  fetchStatData()
  fetchTrendData()
  fetchAppointData()
  getList()
}

// 页面生命周期
onMounted(() => {
  loadAllData()
})

onBeforeUnmount(() => {
  // 销毁图表实例和事件监听
  if (myChart) {
    myChart.dispose()
    myChart = null
  }
  window.removeEventListener('resize', () => myChart?.resize())
})
</script>

<style scoped>
.dashboard-container {
  width: 100%;
  height: 100%;
  padding: 0 20px 20px;
  box-sizing: border-box;
}
.dashboard-header {
  margin-bottom: 20px;
  padding-top: 20px;
}
.dashboard-header h2 {
  font-size: 22px;
  margin: 0 0 8px 0;
  color: #333;
}
.dashboard-header p {
  margin: 0;
  color: #999;
  font-size: 14px;
}
/* 统计卡片 */
.stat-card-group {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}
.stat-card {
  flex: 1;
  min-width: 220px;
}
.card-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.left .label {
  font-size: 14px;
  color: #999;
  margin: 0 0 5px 0;
}
.left .num {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin: 0;
}
.right {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
}
.right.blue {
  background: #1890ff;
}
.right.green {
  background: #52c41a;
}
.right.orange {
  background: #faad14;
}
.right.purple {
  background: #722ed1;
}
/* 主体内容 */
.main-content {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
  height: calc(100% - 200px);
}
.chart-card {
  flex: 2;
  min-width: 500px;
  height: 100%;
  display: flex;
  flex-direction: column;
}
.list-card {
  flex: 1;
  min-width: 450px;
  height: 100%;
  display: flex;
  flex-direction: column;
}
.card-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 15px;
  color: #333;
}
.card-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}
.card-title-row .card-title {
  margin-bottom: 0;
}
:deep(.el-table) {
  flex: 1;
  overflow: auto;
}
:deep(.el-loading-mask) {
  border-radius: 4px;
}
.chart-area {
  flex: 1;
  width: 100%;
  min-height: 400px; /* 新增：最小高度兜底 */
  height: 100%;
}
</style>