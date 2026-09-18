<template>
  <div class="app-layout">
    <PatientSidebar>
      <button class="side-nav-btn primary-action" @click="loadAppointments">
        <i class="fa-solid fa-rotate"></i>刷新列表
      </button>
      <button class="side-nav-btn" @click="goBack">
        <i class="fa-solid fa-comments"></i>返回对话
      </button>
    </PatientSidebar>

    <div class="main-content">
      <div class="appointment-container">
        <!-- 标题区域 -->
        <div class="header-section">
          <div>
            <h2 class="page-title">我的预约记录</h2>
            <p class="page-desc">在此查看您通过小智助手完成的全部挂号预约</p>
          </div>
          <div class="stat-cards">
            <div class="stat-card">
              <span class="stat-num">{{ appointments.length }}</span>
              <span class="stat-label">全部预约</span>
            </div>
            <div class="stat-card stat-blue">
              <span class="stat-num">{{ getStatusCount('confirmed') }}</span>
              <span class="stat-label">进行中</span>
            </div>
            <div class="stat-card stat-green">
              <span class="stat-num">{{ getStatusCount('completed') }}</span>
              <span class="stat-label">已完成</span>
            </div>
          </div>
        </div>

        <!-- 加载状态 -->
        <div v-if="loading" class="loading-container">
          <div class="loading-message">
            <i class="fa-solid fa-spinner fa-spin loading-icon"></i>
            <span>正在加载预约信息...</span>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-else-if="appointments.length === 0" class="empty-container">
          <i class="fa-solid fa-calendar-xmark empty-icon"></i>
          <p class="empty-text">暂无预约记录</p>
          <el-button type="primary" @click="loadAppointments">
            <i class="fa-solid fa-refresh"></i>
            &nbsp;重新加载
          </el-button>
        </div>

        <!-- 预约列表 -->
        <div v-else class="appointment-list" ref="appointmentListRef">
          <div
            v-for="appointment in appointments"
            :key="appointment.appointmentId"
            :class="['appointment-card', getStatusClass(appointment.status)]"
          >
            <!-- 预约头部 -->
            <div class="appointment-header">
              <div class="appointment-number">
                <i class="fa-solid fa-hashtag"></i>
                {{ appointment.appointmentNumber }}
              </div>
              <el-tag :type="getStatusTagType(appointment.status)" class="status-tag">
                {{ getStatusText(appointment.status) }}
              </el-tag>
            </div>

            <!-- 预约内容 -->
            <div class="appointment-content">
              <div class="info-grid">
                <div class="info-item">
                  <i class="fa-solid fa-calendar-day info-icon"></i>
                  <span class="label">预约日期：</span>
                  <span class="value">{{ appointment.appointmentDate ? String(appointment.appointmentDate).split(/[ T]/)[0] : '-' }}</span>
                </div>
                <div class="info-item">
                  <i class="fa-solid fa-clock info-icon"></i>
                  <span class="label">时间段：</span>
                  <span class="value">{{ getTimeSlotText(appointment.timeSlot) }}</span>
                </div>
                <div class="info-item">
                  <i class="fa-solid fa-building info-icon"></i>
                  <span class="label">科室：</span>
                  <span class="value">{{ appointment.deptName || '待分配' }}</span>
                </div>
                <div class="info-item">
                  <i class="fa-solid fa-user-doctor info-icon"></i>
                  <span class="label">医生：</span>
                  <span class="value">{{ appointment.doctorName || '待分配' }}</span>
                </div>
              </div>

              <!-- 症状描述 -->
              <div v-if="appointment.symptomsDescription" class="symptoms-section">
                <i class="fa-solid fa-file-medical symptoms-icon"></i>
                <div class="symptoms-content">
                  <span class="label">症状描述：</span>
                  <span class="value">{{ appointment.symptomsDescription }}</span>
                </div>
              </div>

              <!-- 取消原因 -->
              <div v-if="appointment.status === 'cancelled' && appointment.cancelReason" 
                   class="cancel-section">
                <i class="fa-solid fa-ban cancel-icon"></i>
                <div class="cancel-content">
                  <span class="label cancel-label">取消原因：</span>
                  <span class="value">{{ appointment.cancelReason }}</span>
                </div>
              </div>

              <!-- 时间信息 -->
              <div class="time-section">
                <div v-if="appointment.cancelledAt" class="time-item">
                  <i class="fa-solid fa-times-circle time-icon"></i>
                  <span class="label">取消时间：</span>
                  <span class="value">{{ formatDate(appointment.cancelledAt) }}</span>
                </div>
                <div v-if="appointment.completedAt" class="time-item">
                  <i class="fa-solid fa-check-circle time-icon"></i>
                  <span class="label">完成时间：</span>
                  <span class="value">{{ formatDate(appointment.completed_at) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import PatientSidebar from './PatientSidebar.vue'
import { handle401 } from '@/utils/auth401'

const router = useRouter()
const appointmentListRef = ref()
const loading = ref(false)
const appointments = ref([])

// 获取用户ID
const getUserId = () => {
  return localStorage.getItem('patientId') // 默认使用ID 7
}

// 加载预约列表
const loadAppointments = async () => {
  const userId = getUserId()
  if (!userId) {
    ElMessage.warning('请先登录')
    return
  }

  loading.value = true
  try {
    const response = await fetch(`/api/api/appointments/patient?userId=${userId}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('patientToken') || localStorage.getItem('token') || ''}`
      }
    })
    if (response.status === 401) {
      // 未登录/令牌失效：清理并跳转登录页
      loading.value = false
      handle401('patient')
      return
    }
    if (!response.ok) {
      throw new Error('网络响应异常')
    }
    const data = await response.json()
    console.log(data)
    appointments.value = data
    ElMessage.success('预约列表加载成功')
  } catch (error) {
    console.error('加载预约列表失败:', error)
    ElMessage.error('加载预约列表失败')
  } finally {
    loading.value = false
  }
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 获取时间段文本
const getTimeSlotText = (timeSlot) => {
  const timeSlotMap = {
    'morning': '上午 (8:00-12:00)',
    'afternoon': '下午 (14:00-18:00)',
    'evening': '晚上 (18:00-22:00)'
  }
  return timeSlotMap[timeSlot] || timeSlot || '待安排'
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    'pending': '待确认',
    'confirmed': '已确认',
    'cancelled': '已取消',
    'completed': '已完成'
  }
  return statusMap[status] || status || '未知状态'
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  const typeMap = {
    'pending': 'warning',
    'confirmed': 'success',
    'cancelled': 'danger',
    'completed': 'info'
  }
  return typeMap[status] || 'info'
}

// 获取状态对应的CSS类
const getStatusClass = (status) => {
  return `status-${status}`
}

// 获取状态数量统计
const getStatusCount = (status) => {
  return appointments.value.filter(app => app.status === status).length
}

// 返回聊天页面
const goBack = () => {
  router.push('/chat')
}

onMounted(() => {
  loadAppointments()
})
</script>

<style scoped>

/* 保持与 ChatWindow 相同的布局样式 */
.app-layout {
  display: flex;
  height: 100vh;
  background: linear-gradient(160deg, #eef4fb 0%, #f6fafd 60%, #eaf6f4 100%);
  overflow: hidden;
}

.main-content {
  flex: 1;
  padding: 18px 24px 22px;
  overflow-y: auto;
  box-sizing: border-box;
}

.appointment-container {
  display: flex;
  flex-direction: column;
  min-height: 100%;
  max-width: 1200px;
  margin: 0 auto;
}

/* 头部区域 */
.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
  margin-bottom: 22px;
  padding: 18px 22px;
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 2px 10px rgba(23, 62, 129, 0.06);
}

.page-title {
  margin: 0 0 4px;
  color: #0f2e5c;
  font-size: 22px;
  font-weight: 700;
}

.page-desc {
  margin: 0;
  font-size: 12px;
  color: #8aa0bd;
  letter-spacing: 0.5px;
}

/* 统计卡片 */
.stat-cards {
  display: flex;
  gap: 14px;
}

.stat-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 88px;
  padding: 10px 18px;
  border-radius: 12px;
  background: #f4f8fd;
  border: 1px solid #e3ecf7;
}
.stat-num {
  font-size: 22px;
  font-weight: 700;
  color: #0f2e5c;
  line-height: 1.2;
}
.stat-label {
  font-size: 12px;
  color: #8aa0bd;
  margin-top: 2px;
}
.stat-blue {
  background: #ecf3ff;
  border-color: #cfe0ff;
}
.stat-blue .stat-num { color: #1d6ff2; }
.stat-green {
  background: #e9f9f5;
  border-color: #c4ecdf;
}
.stat-green .stat-num { color: #0f9e8c; }

/* 加载状态 */
.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
}

.loading-message {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #606266;
  font-size: 16px;
}

.loading-icon {
  font-size: 20px;
  color: #409eff;
}

/* 空状态 */
.empty-container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 300px;
  gap: 16px;
}

.empty-icon {
  font-size: 64px;
  color: #c0c4cc;
  margin-bottom: 16px;
}

.empty-text {
  color: #909399;
  font-size: 16px;
  margin: 0;
}

/* 预约列表 */
.appointment-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.appointment-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  border-left: 4px solid #409eff;
}

.appointment-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

/* 状态边框颜色 */
.status-cancelled {
  border-left-color: #f56c6c;
}

.status-completed {
  border-left-color: #0f9e8c;
}

.status-confirmed {
  border-left-color: #1d6ff2;
}

.status-pending {
  border-left-color: #e6a23c;
}

/* 预约头部 */
.appointment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.appointment-number {
  font-size: 16px;
  font-weight: 600;
  color: #1d6ff2;
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-tag {
  font-weight: 600;
  padding: 12px;
  font-size: 15px;
}

/* 预约内容 */
.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-icon {
  color: #909399;
  width: 16px;
}

.label {
  color: #606266;
  font-weight: 500;
  min-width: 70px;
}

.value {
  color: #303133;
  flex: 1;
}

/* 症状描述区域 */
.symptoms-section {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 6px;
}

.symptoms-icon {
  color: #67c23a;
  margin-top: 2px;
}

.symptoms-content {
  flex: 1;
}

/* 取消原因区域 */
.cancel-section {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  padding: 12px;
  background: #fef0f0;
  border-radius: 6px;
}

.cancel-icon {
  color: #f56c6c;
  margin-top: 2px;
}

.cancel-label {
  color: #f56c6c;
}

/* 时间信息区域 */
.time-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px dashed #e0e0e0;
}

.time-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.time-icon {
  color: #909399;
  width: 16px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .app-layout {
    flex-direction: column;
  }

  .main-content {
    padding: 10px 12px 14px;
  }

  .header-section {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }

  .stat-cards {
    width: 100%;
    justify-content: space-around;
  }

  .info-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .appointment-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
}
</style>