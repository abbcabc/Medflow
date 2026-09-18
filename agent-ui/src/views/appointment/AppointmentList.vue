<template>
  <div class="appointment-container">
    <div class="header-section">
      <div class="header-left">
        <span class="header-badge"><i class="fa-solid fa-calendar-check"></i></span>
        <div>
          <h2 class="page-title">预约管理</h2>
          <p class="page-desc">查看与管理患者预约记录</p>
        </div>
      </div>
      <div class="search-box">
        <el-input
          v-model="searchForm.keyword"
          placeholder="预约编号/患者名/医生名/科室名"
          style="width: 260px"
        />
        <el-select
          v-model="searchForm.status"
          placeholder="预约状态"
          style="width: 150px"
        >
          <el-option label="全部" value="" />
          <el-option label="已确认" value="confirmed" />
          <el-option label="已完成" value="completed" />
          <el-option label="已取消" value="cancelled" />
          <el-option label="爽约" value="no_show" />
        </el-select>
        <el-button type="primary" @click="getList">查询</el-button>
        <el-button @click="resetSearch">重置</el-button>
        <el-button type="info" plain @click="handleGenerateTest">生成测试数据</el-button>
      </div>
    </div>

    <!-- 预约列表表格 -->
    <el-table
      :data="pageData"
      border
      stripe
      style="width: 100%; margin-top: 20px"
      v-loading="loading"
    >
      <el-table-column type="index" :index="(i) => (pageNum - 1) * pageSize + i + 1" label="序号" width="64" align="center" />
      <el-table-column label="预约ID" prop="appointmentId" width="80" />
      <el-table-column label="预约编号" prop="appointmentNumber" width="160" />
      <el-table-column label="患者姓名" prop="realName" width="120" />
      <el-table-column label="预约科室" prop="deptName" width="150" />
      <el-table-column label="就诊医生" prop="doctorName" width="150" />
      <!-- <el-table-column label="排班ID" prop="scheduleId" width="80" /> -->
      
      <!-- 就诊日期：只显示日期 -->
      <el-table-column label="就诊日期" width="120">
        <template #default="scope">
          {{ scope.row.appointmentDate ? String(scope.row.appointmentDate).split(/[ T]/)[0] : '' }}
        </template>
      </el-table-column>

      <el-table-column label="就诊时段" prop="timeSlot" width="100" />
      <el-table-column label="症状描述" prop="symptomsDescription" min-width="200" />
      <el-table-column label="预约状态" prop="status" width="120">
        <template #default="scope">
          <el-tag :type="getStatusTagType(scope.row.status)" size="small">
            {{ getStatusCn(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button type="primary" size="small" @click="openDetail(scope.row)">详情</el-button>
          <el-button type="warning" size="small" @click="openStatusEdit(scope.row)">改状态</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      v-model:current-page="pageNum"
      :page-size="pageSize"
      :total="total"
      layout="total, prev, pager, next, jumper"
      @current-change="handlePageChange"
      class="table-pagination"
    />

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="预约详情" width="700px" destroy-on-close>
      <el-descriptions :data="currentDetail" border column="2">
        <el-descriptions-item label="预约编号">{{currentDetail.appointmentNumber}}</el-descriptions-item>
        <el-descriptions-item label="患者姓名" prop="realName">{{currentDetail.realName}}</el-descriptions-item>
        <el-descriptions-item label="科室" prop="deptName">{{currentDetail.deptName}}</el-descriptions-item>
        <el-descriptions-item label="医生" prop="doctorName">{{currentDetail.doctorName}}</el-descriptions-item>
        <el-descriptions-item label="排班ID" prop="scheduleId">{{currentDetail.scheduleId}}</el-descriptions-item>
        <el-descriptions-item label="就诊日期">
          {{ currentDetail.appointmentDate ? String(currentDetail.appointmentDate).split(/[ T]/)[0] : '' }}
        </el-descriptions-item>
        <el-descriptions-item label="时段" prop="timeSlot" >{{currentDetail.timeSlot}}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusTagType(currentDetail.status)">{{ getStatusCn(currentDetail.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="症状" prop="symptomsDescription" span="2" >{{currentDetail.symptomsDescription}}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 状态修改 -->
    <el-dialog v-model="statusEditDialogVisible" title="修改状态" width="400px">
      <el-form :model="statusForm" label-width="100px" style="padding-top:20px">
        <el-form-item label="当前状态">
          <el-tag :type="getStatusTagType(statusForm.oldStatus)">{{ getStatusCn(statusForm.oldStatus) }}</el-tag>
        </el-form-item>
        <el-form-item label="新状态" required>
          <el-select v-model="statusForm.newStatus">
            <el-option label="已确认" value="confirmed" />
            <el-option label="已完成" value="completed" />
            <el-option label="已取消" value="cancelled" />
            <el-option label="爽约" value="no_show" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusEditDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitStatusEdit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getAdminAppointmentList,
  updateAppointmentStatus,
  getAppointmentDetail,
  generateTestAppointments
} from '@/api/appointment'

const pageNum = ref(1)
const pageSize = ref(6)
const total = ref(0)
const allData = ref([])
const pageData = ref([])
const loading = ref(false)

const searchForm = reactive({ keyword: '', status: '' })
const detailDialogVisible = ref(false)
const statusEditDialogVisible = ref(false)
const currentDetail = ref({})

const statusForm = reactive({
  appointmentId: '',
  oldStatus: '',
  newStatus: ''
})

onMounted(() => { getList() })

// 获取列表 + ID 正序排序
const getList = async () => {
  loading.value = true
  try {
    const res = await getAdminAppointmentList(searchForm)
    let list = res.data || []
    if (!Array.isArray(list)) list = []

    // ✅ 按预约ID从小到大排序
    list.sort((b, a) => (a.appointmentId || 0) - (b.appointmentId || 0))

    allData.value = list
    total.value = list.length
    pageNum.value = 1
    handlePageChange()
  } catch (err) {
    ElMessage.error('获取失败')
  } finally {
    loading.value = false
  }
}

const handlePageChange = () => {
  const start = (pageNum.value - 1) * pageSize.value
  const end = start + pageSize.value
  pageData.value = allData.value.slice(start, end)
}

const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.status = ''
  getList()
}

// 生成测试预约数据
const handleGenerateTest = async () => {
  try {
    await ElMessageBox.confirm('将随机生成 10 条测试预约记录（最近一周内、随机患者/医生/时段/状态），是否继续？', '生成测试数据', {
      confirmButtonText: '生成',
      cancelButtonText: '取消',
      type: 'info'
    })
  } catch {
    return
  }
  try {
    const res = await generateTestAppointments(10)
    ElMessage.success(res.data || '生成成功')
    getList()
  } catch (e) {
    console.error('生成测试数据失败', e)
  }
}

const getStatusCn = (status) => {
  const map = { confirmed: '已确认', completed: '已完成', cancelled: '已取消', no_show: '爽约' }
  return map[status] || '未知'
}

const getStatusTagType = (status) => {
  const map = { confirmed: 'primary', completed: 'success', cancelled: 'danger', no_show: 'warning' }
  return map[status] || 'info'
}

const formatDate = (str) => str ? str.replace('T', ' ').substring(0, 19) : ''

const openDetail = async (row) => {
  // console.log(row.appointmentId)
  const res = await getAppointmentDetail(row.appointmentId)
  
  currentDetail.value = res.data || {}
  // console.log("res.data"+res.data)
  detailDialogVisible.value = true
}

const openStatusEdit = (row) => {
  statusForm.appointmentId = row.appointmentId
  statusForm.oldStatus = row.status
  statusForm.newStatus = ''
  statusEditDialogVisible.value = true
}

const submitStatusEdit = async () => {
  if (!statusForm.newStatus) return ElMessage.warning('请选择状态')
  await updateAppointmentStatus(statusForm.appointmentId, statusForm.newStatus)
  ElMessage.success('修改成功')
  statusEditDialogVisible.value = false
  getList()
}
</script>

<style scoped>
.appointment-container { padding: 20px; }

/* 头部搜索栏（与医生管理统一） */
.header-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  padding: 16px 22px;
  margin-bottom: 16px;
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
.header-badge {
  width: 42px;
  height: 42px;
  flex: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: linear-gradient(135deg, #1d6ff2 0%, #12b3a8 100%);
  color: #fff;
  font-size: 17px;
}
.page-title {
  margin: 0 0 2px;
  font-size: 19px;
  color: #243b5a;
}
.page-desc {
  margin: 0;
  font-size: 12px;
  color: #7d93b2;
}
.search-box {
  display: flex;
  gap: 10px;
  align-items: center;
}

/* 统一表格与分页样式 */
:deep(.el-table) {
  --el-table-header-bg-color: #f4f8fc;
  --el-table-header-text-color: #4a5d7a;
  --el-table-row-hover-bg-color: #f7fafd;
  --el-table-border-color: #eef2f8;
}
:deep(.el-table th.el-table__cell) {
  font-weight: 600;
}
.table-pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>