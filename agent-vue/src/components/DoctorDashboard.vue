<template>
  <div class="doctor-dashboard">
    <!-- 医生端侧边栏 -->
    <aside class="doc-sidebar">
      <div class="sidebar-logo">
        <span class="logo-badge"><i class="fa-solid fa-user-doctor"></i></span>
        <div class="logo-meta">
          <span class="logo-name">MedFlow</span>
          <span class="logo-sub">医生工作台</span>
        </div>
      </div>

      <nav class="sidebar-nav">
        <button
          v-for="item in menuItems"
          :key="item.key"
          class="side-nav-btn"
          :class="{ active: activeMenu === item.key }"
          @click="handleMenuClick(item)"
        >
          <i v-if="item.key === 'welcome'" class="fa-solid fa-house"></i>
          <i v-else-if="item.key === 'schedule'" class="fa-solid fa-calendar-days"></i>
          <i v-else-if="item.key === 'patients'" class="fa-solid fa-user-group"></i>
          <i v-else-if="item.key === 'records'" class="fa-solid fa-file-medical"></i>
          <i v-else class="fa-solid fa-id-card"></i>
          <span class="nav-text">{{ item.label }}</span>
        </button>
      </nav>

      <div class="sidebar-foot">
        <button class="side-nav-btn logout" @click="handleLogout">
          <i class="fa-solid fa-right-from-bracket"></i>
          <span class="nav-text">退出登录</span>
        </button>
        <p class="foot-text">MedFlow · 医生工作台</p>
      </div>
    </aside>

    <!-- 主内容区域 -->
    <main class="doc-main">
      <!-- 头部卡片 -->
      <div class="header-section">
        <div class="header-left">
          <div
            class="avatar-container"
            @mouseenter="showAvatarTooltip = true"
            @mouseleave="showAvatarTooltip = false"
          >
            <img
              :src="getAvatarUrl(doctorInfo.avatarUrl)"
              alt="医生头像"
              class="avatar"
              @click="toggleAvatarModal"
            />
            <div v-if="showAvatarTooltip" class="avatar-tooltip">点击修改头像</div>
          </div>
          <div class="header-meta">
            <h2 class="page-title">{{ doctorInfo.doctorName || '医生工作台' }}</h2>
            <p class="page-desc">{{ doctorInfo.title ? `${doctorInfo.title} · MedFlow 医生端工作台` : 'MedFlow 医生端工作台' }}</p>
          </div>
        </div>
        <span class="header-chip"><i class="fa-solid fa-stethoscope"></i>接诊工作台</span>
      </div>

      <!-- 欢迎区域 -->
      <section v-if="activeMenu === 'welcome'" class="welcome-section">
        <div class="welcome-intro">
          <h2 class="section-title"><i class="fa-solid fa-house"></i> 欢迎回来，{{ doctorInfo.doctorName || '医生' }}</h2>
          <p class="welcome-sub">今日工作概览</p>
        </div>

        <div class="stat-grid">
          <div class="stat-card">
            <span class="stat-badge b-blue"><i class="fa-solid fa-calendar-days"></i></span>
            <div class="stat-content">
              <div class="stat-number c-blue">{{ scheduleStats.totalSchedules || 0 }}</div>
              <div class="stat-label">总排班</div>
            </div>
          </div>
          <div class="stat-card">
            <span class="stat-badge b-green"><i class="fa-solid fa-clock"></i></span>
            <div class="stat-content">
              <div class="stat-number c-green">{{ scheduleStats.availableSlots || 0 }}</div>
              <div class="stat-label">可预约</div>
            </div>
          </div>
          <div class="stat-card">
            <span class="stat-badge b-amber"><i class="fa-solid fa-circle-check"></i></span>
            <div class="stat-content">
              <div class="stat-number c-amber">{{ scheduleStats.bookedSlots || 0 }}</div>
              <div class="stat-label">已预约</div>
            </div>
          </div>
          <div class="stat-card">
            <span class="stat-badge b-violet"><i class="fa-solid fa-table-cells"></i></span>
            <div class="stat-content">
              <div class="stat-number c-violet">{{ scheduleStats.totalSlots || 0 }}</div>
              <div class="stat-label">总号源</div>
            </div>
          </div>
        </div>

        <!-- 快捷操作 -->
        <div class="quick-actions">
          <h3><i class="fa-solid fa-bolt"></i> 快捷操作</h3>
          <div class="action-buttons">
            <button @click="handleQuickAction('schedule')" class="action-tile">
              <i class="fa-solid fa-calendar-check action-icon"></i>
              <span>查看排班</span>
            </button>
            <button @click="handleQuickAction('patients')" class="action-tile">
              <i class="fa-solid fa-user-group action-icon"></i>
              <span>患者管理</span>
            </button>
            <button @click="handleQuickAction('todaySchedule')" class="action-tile">
              <i class="fa-solid fa-file-pen action-icon"></i>
              <span>今日排班</span>
            </button>
            <button @click="handleQuickAction('records')" class="action-tile">
              <i class="fa-solid fa-pills action-icon"></i>
              <span>病历管理</span>
            </button>
          </div>
        </div>
      </section>

      <!-- 排班查看 -->
      <section v-if="activeMenu === 'schedule'" class="schedule-section">
        <div class="section-header">
          <h2 class="section-title"><i class="fa-solid fa-calendar-days"></i> 我的排班</h2>
          <div class="header-actions">
            <button @click="fetchSchedules" class="refresh-btn" :disabled="loading">
              <i class="fa-solid fa-rotate"></i>刷新
            </button>
          </div>
        </div>

        <div class="schedule-filters">
          <div class="filter-group">
            <label>日期范围：</label>
            <input
              type="date"
              v-model="filterParams.startDate"
              class="filter-input"
            >
            <span>至</span>
            <input
              type="date"
              v-model="filterParams.endDate"
              class="filter-input"
            >
            <button @click="applyDateFilter" class="filter-btn">筛选</button>
            <button @click="resetDateFilter" class="filter-btn secondary">重置</button>
          </div>
          <div class="filter-group">
            <label>显示选项：</label>
            <select v-model="filterParams.showType" class="filter-select" @change="fetchSchedules">
              <option value="all">全部排班</option>
              <option value="future">未来排班</option>
              <option value="today">今日排班</option>
            </select>
          </div>
        </div>

        <div class="section-content">
          <div v-if="loading" class="loading-state">
            <div class="spinner"></div>
            <span>加载中...</span>
          </div>

          <div v-else-if="schedules.length === 0" class="empty-state">
            <i class="fa-solid fa-calendar-xmark empty-icon"></i>
            <p>暂无排班记录</p>
            <p class="empty-tip">请联系管理员添加排班</p>
          </div>

          <div v-else class="schedules-list">
            <div class="schedule-item" v-for="schedule in schedules" :key="schedule.scheduleId">
              <div class="schedule-date">
                <div class="date-display">{{ formatDate(schedule.workDate) }}</div>
                <div class="week-day">{{ getWeekDay(schedule.workDate) }}</div>
                <div class="time-slot" :class="getTimeSlotClass(schedule.timeSlot)">
                  {{ schedule.timeSlot }}
                </div>
              </div>
              <div class="schedule-info">
                <div class="slots-info">
                  <span class="slots-text">
                    已预约: {{ schedule.bookedSlots }}/{{ schedule.totalSlots }}
                  </span>
                  <div class="progress-bar">
                    <div
                      class="progress-fill"
                      :style="{ width: getProgressWidth(schedule) }"
                      :class="getProgressClass(schedule)"
                    ></div>
                  </div>
                </div>
                <div class="availability" :class="getAvailabilityClass(schedule)">
                  {{ getAvailabilityText(schedule) }}
                </div>
                <div class="schedule-status" :class="getScheduleStatusClass(schedule)">
                  {{ getScheduleStatusText(schedule) }}
                </div>
              </div>
              <div class="schedule-actions">
                <button
                  @click="viewScheduleDetail(schedule)"
                  class="action-btn view"
                >
                  查看详情
                </button>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 患者管理模块 -->
      <section v-if="activeMenu === 'patients'" class="patients-section">
        <div class="section-header">
          <h2 class="section-title"><i class="fa-solid fa-user-group"></i> 患者管理</h2>
          <div class="header-actions">
            <button @click="fetchPatientList" class="refresh-btn" :disabled="patientLoading">
              <i class="fa-solid fa-rotate"></i>刷新患者列表
            </button>
          </div>
        </div>
        <div class="section-content">
          <div v-if="patientLoading" class="loading-state">
            <div class="spinner"></div>
            <span>加载患者列表中...</span>
          </div>
          <div v-else-if="patientList.length === 0" class="empty-state">
            <i class="fa-solid fa-user-group empty-icon"></i>
            <p>暂无接诊患者记录</p>
          </div>
          <div v-else class="patient-list">
            <div class="patient-card" v-for="patient in patientList" :key="patient.patientId">
              <div class="patient-basic">
                <h3>患者：{{ patient.patientName }}</h3>
                <p>患者ID：{{ patient.patientId }}</p>
                <p>联系电话：{{ patient.patientPhone || '未填写' }}</p>
              </div>
              <div class="patient-appointment-count">
                <span class="count-text">累计预约：{{ patient.appointmentRecords.length }} 次</span>
                <button
                  @click="handleViewPatientRecords(patient.patientId, patient.patientName)"
                  class="action-btn view"
                >
                  查看病历/预约记录
                </button>
              </div>
              <div class="appointment-brief">
                <h4>最新预约记录</h4>
                <div
                  class="appointment-item-brief"
                  v-for="(item, index) in patient.appointmentRecords.slice(0,2)"
                  :key="item.appointmentId"
                >
                  <span class="appointment-date">{{ formatDate(item.appointmentDate) }} {{ item.timeSlot }}</span>
                  <span class="appointment-status" :class="getStatusClass(item.status)">{{ getStatusText(item.status) }}</span>
                  <span class="appointment-symptom">{{ item.symptomsDescription || '无症状描述' }}</span>
                </div>
                <p v-if="patient.appointmentRecords.length > 2" class="more-record">
                  还有 {{ patient.appointmentRecords.length - 2 }} 条记录，点击查看全部
                </p>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 病历管理模块 -->
      <section v-if="activeMenu === 'records'" class="records-section">
        <div class="section-header">
          <h2 class="section-title"><i class="fa-solid fa-file-medical"></i> 病历管理 - {{ currentPatientName || '请选择患者' }}</h2>
          <div class="header-actions">
            <button @click="goBackToPatients" class="refresh-btn" v-if="currentPatientId">
              <i class="fa-solid fa-arrow-left"></i>返回患者列表
            </button>
          </div>
        </div>
        <div class="section-content">
          <div v-if="!currentPatientId" class="empty-state">
            <i class="fa-solid fa-folder-open empty-icon"></i>
            <p>请从【患者管理】中选择患者查看病历记录</p>
          </div>
          <div v-else-if="recordLoading" class="loading-state">
            <div class="spinner"></div>
            <span>加载病历记录中...</span>
          </div>
          <div v-else-if="patientRecords.length === 0" class="empty-state">
            <i class="fa-solid fa-folder-open empty-icon"></i>
            <p>该患者暂无病历/预约记录</p>
          </div>
          <div v-else class="record-list">
            <div class="record-card" v-for="record in patientRecords" :key="record.appointmentId">
              <div class="record-header">
                <div class="record-basic">
                  <span class="appointment-number">预约编号：{{ record.appointmentNumber }}</span>
                  <span class="record-date">{{ formatDate(record.appointmentDate) }} {{ record.timeSlot }}</span>
                </div>
                <span class="record-status" :class="getStatusClass(record.status)">{{ getStatusText(record.status) }}</span>
              </div>
              <div class="record-body">
                <div class="record-item">
                  <label>症状描述：</label>
                  <span>{{ record.symptomsDescription || '无' }}</span>
                </div>
                <div class="record-item" v-if="record.cancelReason">
                  <label>取消原因：</label>
                  <span>{{ record.cancelReason }}</span>
                </div>
                <div class="record-item">
                  <label>创建时间：</label>
                  <span>{{ record.createdAt }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 个人信息 -->
      <section v-if="activeMenu === 'profile'" class="profile-section">
        <div class="section-header">
          <h2 class="section-title"><i class="fa-solid fa-id-card"></i> 个人信息</h2>
        </div>
        <div class="profile-card">
          <div class="profile-header">
            <img
              :src="doctorInfo.avatarUrl || '/src/assets/images/avatars/default_avatar.png'"
              alt="医生头像"
              class="profile-avatar"
            />
            <div class="profile-basic">
              <h3>{{ doctorInfo.doctorName }}</h3>
              <p>{{ doctorInfo.title }}</p>
            </div>
          </div>

          <div class="profile-details">
            <div class="detail-item">
              <label>科室：</label>
              <span>{{ doctorInfo.deptId ? `科室 ${doctorInfo.deptId}` : '未设置' }}</span>
            </div>
            <div class="detail-item">
              <label>专长：</label>
              <span>{{ doctorInfo.specialty || '未设置' }}</span>
            </div>
            <div class="detail-item">
              <label>工作经验：</label>
              <span>{{ doctorInfo.yearsExperience ? `${doctorInfo.yearsExperience} 年` : '未设置' }}</span>
            </div>
            <div class="detail-item">
              <label>个人介绍：</label>
              <span>{{ doctorInfo.introduction || '暂无介绍' }}</span>
            </div>
          </div>
        </div>
      </section>
    </main>

    <!-- 头像修改模态框 -->
    <div
      v-if="showAvatarModal"
      class="modal-overlay"
      @click.self="closeAvatarModal"
    >
      <div class="modal-content avatar-modal">
        <div class="modal-header">
          <h3>修改头像</h3>
          <button @click="closeAvatarModal" class="close-btn"><i class="fa-solid fa-xmark"></i></button>
        </div>

        <div class="modal-body">
          <!-- 当前头像预览 -->
          <div class="current-avatar-section">
            <h4>当前头像</h4>
            <img
              :src="getAvatarUrl(doctorInfo.avatarUrl)"
              alt="当前头像"
              class="current-avatar"
            />
          </div>

          <!-- 上传新头像部分 -->
          <div class="upload-section">
            <h4>上传新头像</h4>
            <div
              class="upload-area"
              :class="{ 'drag-over': isDragOver }"
              @drop="handleDrop"
              @dragover.prevent="handleDragOver"
              @dragleave.prevent="handleDragLeave"
              @click="triggerFileInput"
            >
              <div class="upload-placeholder">
                <i class="fa-solid fa-images upload-icon"></i>
                <p>点击或拖拽图片到此处</p>
                <p class="upload-tip">支持 JPG、PNG 格式，大小不超过 2MB</p>
              </div>
              <input
                ref="fileInput"
                type="file"
                accept="image/*"
                @change="handleFileSelect"
                style="display: none"
              />
            </div>

            <!-- 图片预览 -->
            <div v-if="previewImage" class="preview-section">
              <h4>预览</h4>
              <div class="preview-container">
                <img :src="previewImage" alt="预览" class="preview-image" />
                <div class="preview-actions">
                  <button @click="confirmUpload" class="btn primary" :disabled="uploading">
                    {{ uploading ? '上传中...' : '确认使用' }}
                  </button>
                  <button @click="cancelPreview" class="btn secondary">取消</button>
                </div>
              </div>
            </div>
          </div>

          <!-- 默认头像选择 -->
          <div class="default-avatars-section">
            <h4>选择默认头像</h4>
            <div class="default-avatars">
              <div
                v-for="avatar in defaultAvatars"
                :key="avatar"
                class="default-avatar-item"
                :class="{ selected: selectedDefaultAvatar === avatar }"
                @click="selectDefaultAvatar(avatar)"
              >
                <img :src="avatar" alt="默认头像" />
              </div>
            </div>
            <button
              @click="useDefaultAvatar"
              class="btn primary"
              :disabled="!selectedDefaultAvatar"
            >
              使用此头像
            </button>
          </div>
        </div>

        <div class="modal-footer">
          <button @click="closeAvatarModal" class="btn secondary">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getDoctorCurrentPatients, getPatientRecords } from '@/api/doctor'
// 添加 fileInput 的引用声明
const fileInput = ref(null)

// 头像相关数据
const showAvatarModal = ref(false)
const showAvatarTooltip = ref(false)
const isDragOver = ref(false)
const previewImage = ref('')
const selectedFile = ref(null)
const uploading = ref(false)
const selectedDefaultAvatar = ref('')

const patientList = ref([])       // 患者列表
const patientLoading = ref(false) // 患者列表加载状态
const patientRecords = ref([])    // 患者病历/预约记录
const recordLoading = ref(false)  // 病历记录加载状态
const currentPatientId = ref('')  // 当前选中的患者ID
const currentPatientName = ref('')// 当前选中的患者姓名

// 修改 resetUploadState 函数
const resetUploadState = () => {
  previewImage.value = ''
  selectedFile.value = null
  selectedDefaultAvatar.value = ''
  isDragOver.value = false
  // 安全地重置文件输入
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

// 修改 closeAvatarModal 函数
const closeAvatarModal = () => {
  showAvatarModal.value = false
  resetUploadState()
}

// 修改 toggleAvatarModal 函数
const toggleAvatarModal = () => {
  showAvatarModal.value = !showAvatarModal.value
  showAvatarTooltip.value = false // 关闭提示
  if (showAvatarModal.value) {
    resetUploadState()
  }
}

// 修改 triggerFileInput 函数
const triggerFileInput = () => {
  if (fileInput.value) {
    fileInput.value.click()
  }
}

// 修改 cancelPreview 函数
const cancelPreview = () => {
  previewImage.value = ''
  selectedFile.value = null
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}


// 修改默认头像路径
const defaultAvatars = ref([
  '/src/assets/images/avatars/default_avatar.png',
  '/src/assets/images/avatars/doctor1.png',
  '/src/assets/images/avatars/doctor2.png',
  '/src/assets/images/avatars/doctor3.png',
  '/src/assets/images/avatars/doctor4.png'
])

// 修改获取头像URL的函数
const getAvatarUrl = (avatarPath) => {
  if (!avatarPath) return '/src/assets/images/avatars/default_avatar.png'
  if (avatarPath.startsWith('http')) return avatarPath
  if (avatarPath.startsWith('/src/assets/')) return avatarPath
  return `http://localhost:1219/api${avatarPath}`
}

const createAuthHeaders = (isMultipart = false) => {
  const token = localStorage.getItem('doctorToken')
  const headers = {
    'Authorization': token || ''
  }
  
  // 如果是文件上传，不设置 Content-Type
  if (!isMultipart) {
    headers['Content-Type'] = 'application/json'
  }
  
  return headers
}

const authFetch = async (url, options = {}) => {
  const isMultipart = options.body instanceof FormData
  const headers = createAuthHeaders(isMultipart)
  const config = {
    ...options,
    headers: {
      ...headers,
      ...options.headers
    }
  }
  
  const response = await fetch(url, config)
  if (response.status === 401) {
    // token过期或无效，跳转到登录页
    localStorage.removeItem('doctorToken')
    localStorage.removeItem('doctorId')
    localStorage.removeItem('doctorInfo')
    router.push('/doctorLogin')
    throw new Error('登录已过期，请重新登录')
  }
  return response
}

const router = useRouter()

// 医生信息
const doctorInfo = reactive({
  doctorId: '',
  doctorName: '',
  title: '',
  specialty: '',
  deptId: '',
  yearsExperience: '',
  introduction: '',
  avatarUrl: ''
})

// 菜单状态
const activeMenu = ref('welcome')

// 排班相关数据
const schedules = ref([])
const loading = ref(false)
const scheduleStats = reactive({
  totalSchedules: 0,
  totalSlots: 0,
  bookedSlots: 0,
  availableSlots: 0
})

// 筛选参数
const filterParams = reactive({
  startDate: '',
  endDate: '',
  showType: 'future'
})

// 菜单项
const menuItems = [
  { key: 'welcome', label: '工作台', icon: '🏠' },
  { key: 'schedule', label: '我的排班', icon: '📅' },
  { key: 'patients', label: '患者管理', icon: '👥' },
  { key: 'records', label: '病历管理', icon: '📋' },
  { key: 'profile', label: '个人信息', icon: '👨‍⚕️' }
]

// 初始化医生信息
const initDoctorInfo = () => {
  const storedInfo = localStorage.getItem('doctorInfo')
  if (storedInfo) {
    const info = JSON.parse(storedInfo)
    Object.assign(doctorInfo, info)
    doctorInfo.doctorId = localStorage.getItem('doctorId')
  } else {
    fetchDoctorProfile()
  }
}

// 获取医生详细信息
const fetchDoctorProfile = async () => {
  const doctorId = localStorage.getItem('doctorId')
  if (!doctorId) return

  try {
    const response = await authFetch(`/api/api/doctor/profile?doctorId=${doctorId}`)
    const result = await response.json()
    
    if (result.code === 200) {
      Object.assign(doctorInfo, result.data)
      doctorInfo.doctorId = doctorId
      localStorage.setItem('doctorInfo', JSON.stringify(result.data))
    }
  } catch (error) {
    console.error('获取医生信息失败:', error)
    if (error.message.includes('登录已过期')) {
      alert('登录已过期，请重新登录')
    }
  }
}

// 获取排班统计信息
const fetchScheduleStats = async () => {
  const doctorId = localStorage.getItem('doctorId')
  if (!doctorId) return

  try {
    const response = await authFetch(`/api/api/doctor-schedule/doctor/${doctorId}/stats`)
    const result = await response.json()
    
    if (result.code === 200) {
      Object.assign(scheduleStats, result.data)
    }
  } catch (error) {
    console.error('获取排班统计失败:', error)
    if (error.message.includes('登录已过期')) {
      alert('登录已过期，请重新登录')
    }
  }
}

// 获取排班列表
const fetchSchedules = async () => {
  const doctorId = localStorage.getItem('doctorId')
  if (!doctorId) return

  loading.value = true
  try {
    let url = `/api/api/doctor-schedule/doctor/${doctorId}`
    
    if (filterParams.showType === 'future') {
      url = `/api/api/doctor-schedule/doctor/${doctorId}/future`
    } else if (filterParams.showType === 'today') {
      const today = new Date().toISOString().split('T')[0]
      url = `/api/api/doctor-schedule/doctor/${doctorId}/date/${today}`
    } else if (filterParams.startDate && filterParams.endDate) {
      url = `/api/api/doctor-schedule/doctor/${doctorId}/date-range?startDate=${filterParams.startDate}&endDate=${filterParams.endDate}`
    }

    const response = await authFetch(url)
    const result = await response.json()
    
    if (result.code === 200) {
      schedules.value = result.data
    } else {
      console.error('获取排班列表失败:', result.message)
    }
  } catch (error) {
    console.error('获取排班列表错误:', error)
    if (error.message.includes('登录已过期')) {
      alert('登录已过期，请重新登录')
    }
  } finally {
    loading.value = false
  }
}


// 查看排班详情
const viewScheduleDetail = (schedule) => {
  const detailMessage = `
排班详情：
📅 日期：${formatDate(schedule.workDate)} ${getWeekDay(schedule.workDate)}
⏰ 时间段：${schedule.timeSlot}
🎫 号源情况：${schedule.bookedSlots}/${schedule.totalSlots}
📊 预约率：${Math.round((schedule.bookedSlots / schedule.totalSlots) * 100)}%
  `.trim()
  
  alert(detailMessage)
}

// 工具函数
const formatDate = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

const getWeekDay = (dateString) => {
  const date = new Date(dateString)
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return weekDays[date.getDay()]
}

const getTimeSlotClass = (timeSlot) => {
  return timeSlot === '上午' ? 'morning' : 'afternoon'
}

const getProgressWidth = (schedule) => {
  const percentage = (schedule.bookedSlots / schedule.totalSlots) * 100
  return `${Math.min(percentage, 100)}%`
}

const getProgressClass = (schedule) => {
  const percentage = (schedule.bookedSlots / schedule.totalSlots) * 100
  if (percentage >= 90) return 'high'
  if (percentage >= 70) return 'medium'
  return 'low'
}

const getAvailabilityClass = (schedule) => {
  if (schedule.bookedSlots >= schedule.totalSlots) return 'full'
  if (schedule.bookedSlots === 0) return 'empty'
  return 'available'
}

const getAvailabilityText = (schedule) => {
  if (schedule.bookedSlots >= schedule.totalSlots) return '已满'
  if (schedule.bookedSlots === 0) return '空闲'
  return '可预约'
}

const getScheduleStatusClass = (schedule) => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const scheduleDate = new Date(schedule.workDate)
  
  if (scheduleDate < today) return 'past'
  if (scheduleDate.getTime() === today.getTime()) return 'today'
  return 'future'
}

const getScheduleStatusText = (schedule) => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const scheduleDate = new Date(schedule.workDate)
  
  if (scheduleDate < today) return '已过期'
  if (scheduleDate.getTime() === today.getTime()) return '今日'
  
  const diffDays = Math.ceil((scheduleDate - today) / (1000 * 60 * 60 * 24))
  return `${diffDays}天后`
}

const applyDateFilter = () => {
  if (filterParams.startDate && filterParams.endDate) {
    fetchSchedules()
  }
}

const resetDateFilter = () => {
  filterParams.startDate = ''
  filterParams.endDate = ''
  filterParams.showType = 'future'
  fetchSchedules()
}

// 菜单点击处理
const handleMenuClick = (menuItem) => {
  activeMenu.value = menuItem.key
  if (menuItem.key === 'schedule') {
    fetchSchedules()
  }
}

// 快捷操作处理
const handleQuickAction = (action) => {
  switch (action) {
    case 'schedule':
      activeMenu.value = 'schedule'
      fetchSchedules()
      break
    case 'patients':
      activeMenu.value = 'patients'
      break
    case 'todaySchedule':
      activeMenu.value = 'schedule'
      filterParams.showType = 'today'
      fetchSchedules()
      break
    case 'records':
      activeMenu.value = 'records'
      break
  }
}

// 编辑个人信息
const handleEditProfile = () => {
  alert('编辑个人信息功能开发中...')
}

// 退出登录
const handleLogout = () => {
  if (confirm('确定要退出登录吗？')) {
    localStorage.removeItem('doctorToken')
    localStorage.removeItem('doctorId')
    localStorage.removeItem('doctorInfo')
    router.push('/doctorLogin')
  }
}

// 组件挂载时初始化
onMounted(() => {
  initDoctorInfo()
  fetchScheduleStats()
  fetchPatientList()
  // 设置默认日期范围（未来30天）
  const today = new Date()
  const nextMonth = new Date(today)
  nextMonth.setDate(today.getDate() + 30)
  
  filterParams.startDate = today.toISOString().split('T')[0]
  filterParams.endDate = nextMonth.toISOString().split('T')[0]
})



// 处理文件选择
const handleFileSelect = (event) => {
  const file = event.target.files[0]
  if (file) {
    validateAndPreviewFile(file)
  }
}

// 处理拖拽事件
const handleDragOver = (event) => {
  event.preventDefault()
  isDragOver.value = true
}

const handleDragLeave = (event) => {
  event.preventDefault()
  isDragOver.value = false
}

const handleDrop = (event) => {
  event.preventDefault()
  isDragOver.value = false
  
  const files = event.dataTransfer.files
  if (files.length > 0) {
    validateAndPreviewFile(files[0])
  }
}

// 验证并预览文件
const validateAndPreviewFile = (file) => {
  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    alert('请选择图片文件')
    return
  }

  // 验证文件大小（2MB）
  if (file.size > 2 * 1024 * 1024) {
    alert('文件大小不能超过2MB')
    return
  }

  selectedFile.value = file
  
  // 创建预览
  const reader = new FileReader()
  reader.onload = (e) => {
    previewImage.value = e.target.result
  }
  reader.readAsDataURL(file)
}

// 修改确认上传函数，确保使用正确的路径
const confirmUpload = async () => {
  if (!selectedFile.value) return

  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', selectedFile.value)
    formData.append('doctorId', doctorInfo.doctorId)

    console.log('开始上传头像...', {
      doctorId: doctorInfo.doctorId,
      fileName: selectedFile.value.name,
      fileSize: selectedFile.value.size
    })

    // 使用现有的上传接口
    const response = await authFetch('/api/api/doctor/upload-avatar', {
      method: 'POST',
      body: formData
      // 注意：不要设置 Content-Type，浏览器会自动设置
    })

    const result = await response.json()
    console.log('上传响应:', result)
    
    if (result.code === 200) {
      // 更新本地头像信息
      doctorInfo.avatarUrl = result.data.avatarUrl || result.data
      localStorage.setItem('doctorInfo', JSON.stringify(doctorInfo))
      
      alert('头像上传成功！')
      showAvatarModal.value = false
      resetUploadState()
      
      // 刷新页面以更新头像显示
      setTimeout(() => {
        window.location.reload()
      }, 1000)
    } else {
      throw new Error(result.message || '上传失败')
    }
  } catch (error) {
    console.error('上传头像失败:', error)
    
    // 如果后端上传失败，使用本地存储作为降级方案
    if (confirm('服务器上传失败，是否先保存到本地？')) {
      await saveAvatarToLocal()
    }
  } finally {
    uploading.value = false
  }
}

// 本地存储降级方案
const saveAvatarToLocal = async () => {
  try {
    const base64Image = await fileToBase64(selectedFile.value)
    const fullBase64 = `data:${selectedFile.value.type};base64,${base64Image}`
    
    doctorInfo.avatarUrl = fullBase64
    localStorage.setItem('doctorInfo', JSON.stringify(doctorInfo))
    localStorage.setItem('doctorAvatar', fullBase64) // 单独存储头像
    
    alert('头像已保存到本地！')
    showAvatarModal.value = false
    resetUploadState()
    
    // 刷新显示
    window.location.reload()
  } catch (localError) {
    console.error('本地存储失败:', localError)
    alert('保存失败，请重试')
  }
}

// Base64 转换工具
const fileToBase64 = (file) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = (e) => resolve(e.target.result)
    reader.onerror = reject
    reader.readAsDataURL(file)
  })
}


// 选择默认头像
const selectDefaultAvatar = (avatar) => {
  selectedDefaultAvatar.value = avatar
}

// 使用默认头像
const useDefaultAvatar = async () => {
  if (!selectedDefaultAvatar.value) return

  try {
    // 修改请求体，确保参数名称明确
    const requestBody = {
      doctorId: doctorInfo.doctorId,
      avatarUrl: selectedDefaultAvatar.value,
      updateTime: new Date().toISOString() // 添加时间参数
    }

    const response = await authFetch('/api/api/doctor/avatar', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(requestBody)
    })

    const result = await response.json()
    
    if (result.code === 200) {
      // 更新本地头像信息
      doctorInfo.avatarUrl = selectedDefaultAvatar.value
      localStorage.setItem('doctorInfo', JSON.stringify(doctorInfo))
      
      alert('头像更新成功！')
      showAvatarModal.value = false
      selectedDefaultAvatar.value = ''
    } else {
      alert('更新失败: ' + result.message)
    }
  } catch (error) {
    console.error('更新头像失败:', error)
    alert('更新失败，请重试')
  }
}



// 在个人信息页面也添加头像编辑功能
const handleEditProfile1 = () => {
  showAvatarModal.value = false
}

// 🔥 核心新增：患者列表获取方法
const fetchPatientList = async () => {
  const doctorId = localStorage.getItem('doctorId')
  if (!doctorId) return
  patientLoading.value = true
  try {
    const res = await getDoctorCurrentPatients(doctorId)
    if (res.code === 200) {
      patientList.value = res.data
    }
  } catch (error) {
    console.error('获取患者列表失败:', error)
    alert('获取患者列表失败，请重试')
  } finally {
    patientLoading.value = false
  }
}

// 🔥 核心新增：查看患者病历记录
const handleViewPatientRecords = async (patientId, patientName) => {
  currentPatientId.value = patientId
  currentPatientName.value = patientName
  activeMenu.value = 'records' // 跳转到病历管理页面
  await fetchPatientRecordList() // 加载该患者的记录
}

// 🔥 核心新增：获取指定患者的病历/预约记录
const fetchPatientRecordList = async () => {
  const doctorId = localStorage.getItem('doctorId')
  if (!doctorId || !currentPatientId.value) return
  recordLoading.value = true
  try {
    const res = await getPatientRecords(doctorId, currentPatientId.value)
    if (res.code === 200) {
      patientRecords.value = res.data
    }
  } catch (error) {
    console.error('获取患者病历记录失败:', error)
    alert('获取患者病历记录失败，请重试')
  } finally {
    recordLoading.value = false
  }
}

// 🔥 核心新增：返回患者列表
const goBackToPatients = () => {
  activeMenu.value = 'patients'
  currentPatientId.value = ''
  currentPatientName.value = ''
  patientRecords.value = []
}

// 🔥 核心新增：预约状态格式化（匹配后端返回的status）
const getStatusText = (status) => {
  const statusMap = {
    confirmed: '已确认',
    completed: '已完成',
    cancelled: '已取消'
  }
  return statusMap[status] || status
}

// 🔥 核心新增：预约状态样式类
const getStatusClass = (status) => {
  const classMap = {
    confirmed: 'confirmed',
    completed: 'completed',
    cancelled: 'cancelled'
  }
  return classMap[status] || status
}

</script>

<style scoped>
/* 医生工作台 · 医疗蓝主题 */
.doctor-dashboard {
  display: flex;
  height: 100vh;
  background: #f2f6fb;
  overflow: hidden;
}

/* ===== 侧边栏 ===== */
.doc-sidebar {
  width: 232px;
  flex: none;
  display: flex;
  flex-direction: column;
  padding: 26px 18px;
  box-sizing: border-box;
  background: linear-gradient(180deg, #0f2542 0%, #173a63 100%);
}

.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 6px 24px;
}
.logo-badge {
  width: 40px;
  height: 40px;
  flex: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: linear-gradient(135deg, #1d6ff2 0%, #12b3a8 100%);
  color: #fff;
  font-size: 17px;
}
.logo-meta {
  display: flex;
  flex-direction: column;
}
.logo-name {
  color: #fff;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0.5px;
}
.logo-sub {
  color: rgba(255, 255, 255, 0.62);
  font-size: 11px;
  margin-top: 2px;
  letter-spacing: 1px;
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
  gap: 10px;
  flex: 1;
}

.side-nav-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 11px 14px;
  box-sizing: border-box;
  border: 1px solid transparent;
  border-radius: 10px;
  background: transparent;
  color: rgba(255, 255, 255, 0.88);
  font-size: 14px;
  font-family: inherit;
  text-align: left;
  cursor: pointer;
  transition: background 0.2s, border-color 0.2s, transform 0.15s;
}
.side-nav-btn i {
  width: 18px;
  text-align: center;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.75);
}
.side-nav-btn:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 255, 255, 0.12);
  transform: translateX(2px);
}
.side-nav-btn.active {
  background: linear-gradient(135deg, #1d6ff2 0%, #12b3a8 100%);
  border-color: transparent;
  color: #fff;
  font-weight: 600;
  box-shadow: 0 6px 16px rgba(18, 108, 200, 0.35);
}
.side-nav-btn.active i { color: #fff; }

.side-nav-btn.logout {
  border: 1px solid rgba(231, 76, 60, 0.4);
  background: rgba(231, 76, 60, 0.12);
  color: #f0a9a2;
}
.side-nav-btn.logout i { color: #e78c82; }
.side-nav-btn.logout:hover {
  background: rgba(231, 76, 60, 0.22);
  border-color: rgba(231, 76, 60, 0.6);
  transform: translateX(0);
}

.sidebar-foot { padding-top: 18px; }
.foot-text {
  margin: 12px 0 0;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.45);
  text-align: center;
  letter-spacing: 1px;
}

/* ===== 主内容区域 ===== */
.doc-main {
  flex: 1;
  overflow-y: auto;
  padding: 18px 24px 26px;
  box-sizing: border-box;
  min-width: 0;
}

/* 头部卡片 */
.header-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
  padding: 16px 22px;
  background: #fff;
  border: 1px solid #e6edf5;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(23, 58, 99, 0.06);
}
.header-left {
  display: flex;
  align-items: center;
  gap: 14px;
}
.avatar-container {
  position: relative;
  cursor: pointer;
  flex: none;
}
.avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #eaf2fe;
  box-shadow: 0 0 0 2px rgba(29, 111, 242, 0.15);
}
.avatar-tooltip {
  position: absolute;
  top: 100%;
  left: 0;
  margin-top: 6px;
  background: #243b5a;
  color: #fff;
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 12px;
  white-space: nowrap;
  z-index: 10;
}
.header-meta {
  display: flex;
  flex-direction: column;
}
.page-title {
  margin: 0 0 2px;
  font-size: 20px;
  color: #243b5a;
}
.page-desc {
  margin: 0;
  font-size: 12px;
  color: #7d93b2;
}
.header-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border-radius: 999px;
  background: #eaf2fe;
  color: #1d6ff2;
  font-size: 13px;
  font-weight: 600;
  flex: none;
}

/* 通用区块 */
.welcome-section,
.schedule-section,
.patients-section,
.records-section,
.profile-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}
.section-title {
  margin: 0;
  font-size: 17px;
  color: #243b5a;
  display: flex;
  align-items: center;
  gap: 8px;
}
.section-title i { color: #1d6ff2; }
.header-actions {
  display: flex;
  gap: 10px;
}
.refresh-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border: 1px solid #e6edf5;
  border-radius: 10px;
  background: #fff;
  color: #4a5d7a;
  padding: 8px 14px;
  font-size: 13px;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.2s;
}
.refresh-btn:hover:not(:disabled) {
  color: #fff;
  background: linear-gradient(135deg, #1d6ff2 0%, #12b3a8 100%);
  border-color: transparent;
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(29, 111, 242, 0.32);
}
.refresh-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.section-content {
  background: #fff;
  border: 1px solid #e6edf5;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(23, 58, 99, 0.06);
  padding: 20px;
}

/* 加载状态 & 空状态 */
.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 50px 20px;
  text-align: center;
}
.spinner {
  width: 38px;
  height: 38px;
  border: 4px solid #e6edf5;
  border-top-color: #1d6ff2;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 14px;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}
.empty-icon {
  font-size: 44px;
  margin-bottom: 14px;
  color: #b7c6da;
}
.empty-state p {
  margin: 0;
  color: #7d93b2;
  font-size: 15px;
}
.empty-tip {
  font-size: 13px;
  margin-top: 8px;
  color: #a9bacf;
}

/* 欢迎页 · 统计数字卡 */
.welcome-intro {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.welcome-sub {
  margin: 0;
  font-size: 13px;
  color: #7d93b2;
}
.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}
.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px;
  background: #fff;
  border: 1px solid #e6edf5;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(23, 58, 99, 0.06);
  transition: transform 0.2s, box-shadow 0.2s;
}
.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 22px rgba(23, 58, 99, 0.1);
}
.stat-badge {
  width: 42px;
  height: 42px;
  flex: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  font-size: 17px;
}
.stat-badge.b-blue { background: #eaf2fe; color: #1d6ff2; }
.stat-badge.b-green { background: #e6f7ef; color: #12b3a8; }
.stat-badge.b-amber { background: #fdf0e3; color: #e0731d; }
.stat-badge.b-violet { background: #eef0fe; color: #5a67d8; }
.stat-content { flex: 1; }
.stat-number {
  font-size: 26px;
  font-weight: 700;
  line-height: 1.2;
}
.stat-number.c-blue { color: #1d6ff2; }
.stat-number.c-green { color: #12b3a8; }
.stat-number.c-amber { color: #e0731d; }
.stat-number.c-violet { color: #5a67d8; }
.stat-label {
  font-size: 13px;
  color: #7d93b2;
  margin-top: 2px;
}

/* 快捷操作 */
.quick-actions {
  background: #fff;
  border: 1px solid #e6edf5;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(23, 58, 99, 0.06);
  padding: 20px 22px;
}
.quick-actions h3 {
  margin: 0 0 15px;
  font-size: 15px;
  color: #243b5a;
  display: flex;
  align-items: center;
  gap: 8px;
}
.quick-actions h3 i { color: #1d6ff2; }
.action-buttons {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(130px, 1fr));
  gap: 12px;
}
.action-tile {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 10px;
  background: #f7fafd;
  border: 1px solid #e6edf5;
  border-radius: 12px;
  cursor: pointer;
  font-size: 14px;
  color: #4a5d7a;
  font-family: inherit;
  transition: all 0.2s;
}
.action-tile:hover {
  background: #eaf2fe;
  border-color: rgba(29, 111, 242, 0.35);
  color: #1d6ff2;
  transform: translateY(-1px);
}
.action-icon {
  font-size: 19px;
  color: #1d6ff2;
}

/* 排班筛选 */
.schedule-filters {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  padding: 16px 20px;
  background: #fff;
  border: 1px solid #e6edf5;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(23, 58, 99, 0.06);
}
.filter-group {
  display: flex;
  align-items: center;
  gap: 10px;
}
.filter-group label {
  font-size: 13px;
  color: #4a5d7a;
  font-weight: 600;
}
.filter-input,
.filter-select {
  padding: 7px 10px;
  border: 1px solid #dfe7f2;
  border-radius: 10px;
  font-size: 13px;
  color: #4a5d7a;
  background: #fff;
  outline: none;
}
.filter-input:focus,
.filter-select:focus {
  border-color: #1d6ff2;
}
.filter-btn {
  padding: 7px 16px;
  border-radius: 10px;
  border: none;
  cursor: pointer;
  background: linear-gradient(135deg, #1d6ff2 0%, #12b3a8 100%);
  color: #fff;
  font-size: 13px;
  font-family: inherit;
  transition: all 0.2s;
}
.filter-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(29, 111, 242, 0.32);
}
.filter-btn.secondary {
  background: #eef2f8;
  color: #4a5d7a;
}
.filter-btn.secondary:hover {
  background: #e3eaf4;
  transform: none;
  box-shadow: none;
}

/* 排班列表 */
.schedules-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.schedule-item {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 14px 16px;
  background: #fff;
  border: 1px solid #e6edf5;
  border-radius: 14px;
  transition: box-shadow 0.2s;
}
.schedule-item:hover {
  box-shadow: 0 6px 18px rgba(23, 58, 99, 0.08);
}
.schedule-date {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 120px;
  gap: 5px;
}
.date-display {
  font-size: 15px;
  font-weight: 700;
  color: #243b5a;
}
.week-day {
  font-size: 12px;
  color: #7d93b2;
}
.time-slot {
  padding: 3px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}
.time-slot.morning {
  background: #eaf2fe;
  color: #1d6ff2;
}
.time-slot.afternoon {
  background: #fdf0e3;
  color: #e0731d;
}
.schedule-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 0;
}
.slots-text {
  font-size: 13px;
  color: #4a5d7a;
}
.progress-bar {
  height: 8px;
  background: #eef2f8;
  border-radius: 999px;
  overflow: hidden;
}
.progress-fill {
  height: 100%;
  border-radius: 999px;
  transition: width 0.3s;
}
.progress-fill.high { background: #e74c3c; }
.progress-fill.medium { background: #e0731d; }
.progress-fill.low { background: #12b3a8; }
.availability {
  font-size: 12px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: 999px;
  display: inline-block;
  width: fit-content;
}
.availability.full {
  background: #fdeaea;
  color: #c0392b;
}
.availability.empty {
  background: #eaf2fe;
  color: #1d6ff2;
}
.availability.available {
  background: #e6f7ef;
  color: #0f7a4d;
}
.schedule-status {
  font-size: 12px;
  color: #7d93b2;
  padding: 2px 10px;
  border-radius: 999px;
  display: inline-block;
  width: fit-content;
  background: #eef2f8;
}
.schedule-status.past {
  background: #eef2f8;
  color: #7d93b2;
}
.schedule-status.today {
  background: #fdf0e3;
  color: #e0731d;
}
.schedule-status.future {
  background: #eaf2fe;
  color: #1d6ff2;
}
.schedule-actions {
  min-width: 100px;
  text-align: right;
}
.action-btn.view {
  background: linear-gradient(135deg, #1d6ff2 0%, #12b3a8 100%);
  color: #fff;
  border: none;
  border-radius: 10px;
  padding: 7px 14px;
  cursor: pointer;
  font-size: 13px;
  font-family: inherit;
  transition: transform 0.2s, box-shadow 0.2s;
}
.action-btn.view:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(29, 111, 242, 0.32);
}

/* 患者管理 */
.patient-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(560px, 1fr));
  gap: 16px;
}
.patient-card {
  background: #fff;
  border: 1px solid #e6edf5;
  border-radius: 14px;
  padding: 18px 20px;
  transition: box-shadow 0.2s;
}
.patient-card:hover {
  box-shadow: 0 8px 22px rgba(23, 58, 99, 0.1);
}
.patient-basic {
  border-bottom: 1px solid #eef2f8;
  padding-bottom: 14px;
  margin-bottom: 14px;
}
.patient-basic h3 {
  margin: 0 0 8px;
  font-size: 16px;
  color: #243b5a;
}
.patient-basic p {
  margin: 4px 0;
  color: #7d93b2;
  font-size: 13px;
}
.patient-appointment-count {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}
.count-text {
  color: #4a5d7a;
  font-size: 13px;
  font-weight: 600;
}
.appointment-brief {
  border-top: 1px dashed #e6edf5;
  padding-top: 12px;
}
.appointment-brief h4 {
  margin: 0 0 8px;
  font-size: 13px;
  color: #4a5d7a;
}
.appointment-item-brief {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  padding: 8px 0;
  border-bottom: 1px dashed #eef2f8;
  font-size: 13px;
  align-items: center;
}
.appointment-date {
  color: #243b5a;
  font-weight: 600;
  min-width: 130px;
}
.appointment-status {
  padding: 3px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}
.appointment-status.confirmed {
  background: #eaf2fe;
  color: #1d6ff2;
}
.appointment-status.completed {
  background: #e6f7ef;
  color: #0f7a4d;
}
.appointment-status.cancelled {
  background: #fdeaea;
  color: #c0392b;
}
.appointment-symptom {
  color: #7d93b2;
  flex: 1;
  min-width: 160px;
}
.more-record {
  margin: 8px 0 0;
  color: #1d6ff2;
  font-size: 12px;
}

/* 病历管理 */
.record-list {
  display: grid;
  grid-template-columns: 1fr;
  gap: 14px;
}
.record-card {
  background: #fff;
  border: 1px solid #e6edf5;
  border-radius: 14px;
  padding: 16px 20px;
}
.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  border-bottom: 1px solid #eef2f8;
  padding-bottom: 12px;
  margin-bottom: 12px;
}
.record-basic {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.appointment-number {
  font-size: 14px;
  color: #243b5a;
  font-weight: 700;
}
.record-date {
  font-size: 12px;
  color: #7d93b2;
}
.record-status {
  padding: 3px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}
.record-status.confirmed {
  background: #eaf2fe;
  color: #1d6ff2;
}
.record-status.completed {
  background: #e6f7ef;
  color: #0f7a4d;
}
.record-status.cancelled {
  background: #fdeaea;
  color: #c0392b;
}
.record-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.record-item {
  display: flex;
  align-items: flex-start;
  font-size: 13px;
}
.record-item label {
  min-width: 80px;
  color: #7d93b2;
  font-weight: 600;
}
.record-item span {
  color: #4a5d7a;
  flex: 1;
  line-height: 1.6;
}

/* 个人信息 */
.profile-card {
  background: #fff;
  border: 1px solid #e6edf5;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(23, 58, 99, 0.06);
  padding: 22px;
}
.profile-header {
  display: flex;
  align-items: center;
  gap: 20px;
  padding-bottom: 18px;
  border-bottom: 1px solid #eef2f8;
  margin-bottom: 18px;
}
.profile-avatar {
  width: 84px;
  height: 84px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #eaf2fe;
}
.profile-basic {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.profile-basic h3 {
  margin: 0;
  font-size: 19px;
  color: #243b5a;
}
.profile-basic p {
  margin: 0;
  font-size: 14px;
  color: #7d93b2;
}
.profile-details {
  display: flex;
  flex-direction: column;
  gap: 13px;
}
.detail-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  font-size: 14px;
}
.detail-item label {
  min-width: 84px;
  font-weight: 600;
  color: #7d93b2;
}
.detail-item span {
  flex: 1;
  color: #4a5d7a;
  line-height: 1.6;
}

/* 头像修改模态框 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(15, 37, 66, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.modal-content {
  background: #fff;
  border-radius: 16px;
  width: 90%;
  max-width: 520px;
  max-height: 88vh;
  overflow-y: auto;
  box-shadow: 0 20px 50px rgba(15, 37, 66, 0.25);
}
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 22px;
  border-bottom: 1px solid #eef2f8;
}
.modal-header h3 {
  margin: 0;
  font-size: 16px;
  color: #243b5a;
  font-weight: 700;
}
.close-btn {
  background: transparent;
  border: none;
  font-size: 16px;
  cursor: pointer;
  color: #7d93b2;
  width: 30px;
  height: 30px;
  border-radius: 8px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}
.close-btn:hover {
  background: #eef2f8;
  color: #243b5a;
}
.modal-body {
  padding: 20px 22px;
}
.modal-footer {
  padding: 14px 22px;
  border-top: 1px solid #eef2f8;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
.btn {
  padding: 8px 18px;
  border-radius: 10px;
  cursor: pointer;
  font-size: 13px;
  border: none;
  font-family: inherit;
  transition: all 0.2s;
}
.btn.primary {
  background: linear-gradient(135deg, #1d6ff2 0%, #12b3a8 100%);
  color: #fff;
  font-weight: 600;
}
.btn.primary:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(29, 111, 242, 0.32);
}
.btn.primary:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}
.btn.secondary {
  background: #eef2f8;
  color: #4a5d7a;
}
.btn.secondary:hover {
  background: #e3eaf4;
}
.avatar-modal .modal-body {
  display: flex;
  flex-direction: column;
  gap: 18px;
}
.current-avatar-section h4,
.upload-section h4,
.default-avatars-section h4 {
  margin: 0 0 10px;
  font-size: 13px;
  color: #4a5d7a;
}
.current-avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #eaf2fe;
}
.upload-area {
  border: 2px dashed #cdd9ea;
  border-radius: 14px;
  padding: 26px;
  text-align: center;
  cursor: pointer;
  transition: border-color 0.2s, background 0.2s;
}
.upload-area:hover,
.upload-area.drag-over {
  border-color: #1d6ff2;
  background: #f7fafd;
}
.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}
.upload-icon {
  font-size: 26px;
  color: #1d6ff2;
}
.upload-placeholder p {
  margin: 0;
  color: #4a5d7a;
  font-size: 13px;
}
.upload-tip {
  font-size: 12px;
  color: #a9bacf !important;
}
.preview-section {
  margin-top: 14px;
}
.preview-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}
.preview-image {
  width: 90px;
  height: 90px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #eaf2fe;
}
.preview-actions {
  display: flex;
  gap: 10px;
}
.default-avatars {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin: 10px 0 14px;
}
.default-avatar-item {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid transparent;
  cursor: pointer;
  transition: border-color 0.2s;
}
.default-avatar-item.selected {
  border-color: #1d6ff2;
}
.default-avatar-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 响应式：<900px 侧边栏变顶部横排条 */
@media (max-width: 900px) {
  .doctor-dashboard {
    flex-direction: column;
  }
  .doc-sidebar {
    width: 100%;
    flex-direction: row;
    align-items: center;
    padding: 10px 14px;
    gap: 12px;
  }
  .sidebar-logo { padding: 0; }
  .logo-sub { display: none; }
  .sidebar-nav {
    flex-direction: row;
    flex: 1;
    justify-content: flex-end;
    gap: 8px;
    overflow-x: auto;
  }
  .side-nav-btn {
    width: auto;
    padding: 8px 12px;
    font-size: 13px;
    white-space: nowrap;
  }
  .side-nav-btn:hover { transform: none; }
  .sidebar-foot { display: none; }
  .doc-main { padding: 12px 14px 20px; }
  .stat-grid { grid-template-columns: repeat(2, 1fr); }
  .patient-list { grid-template-columns: 1fr; }
  .schedule-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  .schedule-date {
    align-items: flex-start;
    min-width: unset;
  }
  .schedule-actions {
    text-align: left;
    min-width: unset;
    width: 100%;
  }
  .patient-appointment-count {
    flex-direction: column;
    align-items: flex-start;
  }
  .appointment-item-brief {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
}
</style>
