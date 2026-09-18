<template>
  <div class="schedule-list-container">
    <div class="header-section">
      <div class="header-left">
        <span class="header-badge"><i class="fa-solid fa-calendar-days"></i></span>
        <div>
          <h2 class="page-title">排班管理</h2>
          <p class="page-desc">管理医生排班与出诊安排</p>
        </div>
      </div>
      <div class="search-box">
        <el-input
          v-model="searchForm.keyword"
          placeholder="请输入医生姓名/科室名称"
          style="width: 240px"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-date-picker
          v-model="searchForm.workDate"
          type="date"
          placeholder="排班日期"
          value-format="YYYY-MM-DD"
          clearable
          style="width: 150px"
        />
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button type="success" @click="handleAdd">新增排班</el-button>
        <el-button type="warning" @click="openOneKey">一键排班</el-button>
        <template v-if="!selectionMode">
          <el-button type="danger" plain @click="enterSelectionMode">批量删除</el-button>
        </template>
        <template v-else>
          <el-button type="danger" :disabled="selectedIds.length === 0" @click="confirmBatchDelete">
            删除所选({{ selectedIds.length }})
          </el-button>
          <el-button @click="exitSelectionMode">取消</el-button>
        </template>
      </div>
    </div>

    <el-card shadow="hover">

      <!-- 排班列表 -->
      <el-table
        v-loading="loading"
        :data="finalScheduleList"
        border
        stripe
        style="width: 100%; margin-top: 20px"
        @selection-change="handleSelectionChange"
      >
        <el-table-column v-if="selectionMode" type="selection" width="55" />
        <el-table-column type="index" :index="(i) => (pagination.pageNum - 1) * pagination.pageSize + i + 1" label="序号" width="64" align="center" />
        <el-table-column prop="scheduleId" label="排班ID" width="80" align="center" />
        <el-table-column prop="doctorName" label="医生姓名" min-width="120" />
        <!-- <el-table-column prop="deptName" label="所属科室" min-width="120" /> -->
        <el-table-column prop="workDate" label="排班日期" min-width="120" />
        <el-table-column prop="timeSlot" label="班次类型" min-width="100">
          <template #default="scope">
            <el-tag :type="scope.row.timeSlot === '上午' ? 'success' : 'warning'">
              {{ scope.row.timeSlot === '上午' ? '上午' : '下午' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" min-width="100">
          <template #default="scope">
            <el-tag :type="scope.row.isAvailable === 1 ? 'primary' : 'danger'">
              {{ scope.row.isAvailable === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="180" align="center">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button
              type="danger"
              size="small"
              @click="handleDelete(scope.row.scheduleId)"
              icon="Delete"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.pageNum"
        :page-size="pagination.pageSize"
        :total="filteredTotal"
        layout="total, prev, pager, next, jumper"
        @current-change="handleCurrentChange"
        class="table-pagination"
      />
    </el-card>

    <!-- 新增/编辑排班弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑排班' : '新增排班'"
      width="600px"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        style="margin-top: 20px"
      >
        <el-form-item label="医生姓名" prop="doctorId">
          <el-select v-model="form.doctorId" placeholder="请选择医生">
            <el-option
              v-for="doctor in doctorList.records"
              :key="doctor.doctorId"
              :label="doctor.doctorName"
              :value="doctor.doctorId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排班日期" prop="workDate">
          <el-date-picker
            v-model="form.workDate"
            type="date"
            placeholder="请选择排班日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="班次类型" prop="timeSlot">
          <el-radio-group v-model="form.timeSlot">
            <el-radio label="上午">上午</el-radio>
            <el-radio label="下午">下午</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch
            v-model="form.status"
            active-value="enabled"
            inactive-value="disabled"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 一键排班弹窗 -->
    <el-dialog v-model="oneKeyVisible" title="一键排班" width="480px">
      <el-form :model="oneKeyForm" label-width="90px">
        <el-form-item label="排班对象">
          <el-radio-group v-model="oneKeyForm.target">
            <el-radio label="all">全部医生</el-radio>
            <el-radio label="one">指定医生</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="oneKeyForm.target === 'one'" label="选择医生">
          <el-select v-model="oneKeyForm.doctorId" placeholder="请选择医生" style="width: 100%">
            <el-option v-for="d in doctorList.records" :key="d.doctorId" :label="d.doctorName" :value="d.doctorId" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="oneKeyForm.dateRange"
            type="daterange"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="时间段">
          <el-checkbox-group v-model="oneKeyForm.timeSlots">
            <el-checkbox label="上午">上午</el-checkbox>
            <el-checkbox label="下午">下午</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="总号源">
          <el-input-number v-model="oneKeyForm.totalSlots" :min="1" :max="100" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="oneKeyVisible = false">取消</el-button>
        <el-button type="primary" :loading="oneKeyLoading" @click="submitOneKey">生成排班</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Delete } from '@element-plus/icons-vue'
import {
  getScheduleList,
  addSchedule,
  editSchedule,
  deleteSchedule,
  oneKeySchedule,
  batchDeleteSchedule
} from '@/api/schedule'
import { getDoctorPage } from '@/api/doctor'
import { getDeptPage } from '@/api/dept'

// 加载状态
const loading = ref(false)
// 弹窗显示状态
const dialogVisible = ref(false)
// 是否编辑
const isEdit = ref(false)
// 表单引用
const formRef = ref(null)

// 今日日期（本地时区，格式 yyyy-MM-dd）
const todayStr = () => {
  const d = new Date()
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`
}

// 搜索表单（默认查当天）
const searchForm = reactive({
  keyword: '',
  workDate: todayStr()
})

// 分页参数
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 排班列表
const scheduleList = ref([])
// 医生列表
const doctorList = ref([])

// 表单数据
const form = reactive({
  id: '',
  doctorId: '',
  scheduleDate: '',
  shiftType: 'morning',
  status: 'enabled'
})

// 表单校验规则
const rules = reactive({
  doctorId: [{ required: true, message: '请选择医生', trigger: 'change' }],
  scheduleDate: [{ required: true, message: '请选择排班日期', trigger: 'change' }],
  shiftType: [{ required: true, message: '请选择班次类型', trigger: 'change' }]
})

// 日期格式化（只保留 YYYY-MM-DD）
const formatDateOnly = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const time = date.getTime() + date.getTimezoneOffset() * 60000
  return new Date(time).toISOString().split('T')[0]
}

// 获取排班列表
const getList = async () => {
  loading.value = true
  try {
    const res = await getScheduleList({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword
    })
    scheduleList.value = res.data
  } catch (error) {
    ElMessage.error('获取排班列表失败')
  } finally {
    loading.value = false
  }
}

// 处理后最终给表格用的排班列表（关键字/日期过滤 + 按当前页切片 + 自动加医生姓名）
const filteredScheduleList = computed(() => {
  const kw = searchForm.keyword.trim()
  const date = searchForm.workDate
  let list = scheduleList.value
  if (kw) {
    // 前端过滤：医生姓名 / 医生所属科室名称
    list = list.filter((item) => {
      const doctor = doctorList.value.records.find((d) => d.doctorId === item.doctorId)
      const doctorName = doctor?.doctorName || ''
      const deptName = deptMap.value[doctor?.deptId] || ''
      return doctorName.includes(kw) || deptName.includes(kw)
    })
  }
  if (date) {
    // 按排班日期过滤（workDate 已是 yyyy-MM-dd 字符串）
    list = list.filter((item) => item.workDate === date)
  }
  return list
})

// 过滤后的总条数（分页用）
const filteredTotal = computed(() => filteredScheduleList.value.length)

const finalScheduleList = computed(() => {
  // 前端分页切片：表格只展示当前页数据
  const start = (pagination.pageNum - 1) * pagination.pageSize
  const pageRows = filteredScheduleList.value.slice(start, start + pagination.pageSize)
  // 遍历当前页排班表
  return pageRows.map(item => {
    // 用 doctorId 匹配医生（完全不修改 doctorList）
    const doctor = doctorList.value.records.find(d => d.doctorId === item.doctorId)
    return {
      ...item,
      workDate: item.workDate, // 时间只保留日期
      doctorName: doctor?.doctorName || '未知医生' // 新增姓名
    }
  })
})

// 科室 ID -> 名称映射（搜索科室名称用）
const deptMap = ref({})
const loadDeptMap = async () => {
  try {
    const res = await getDeptPage()
    const list = Array.isArray(res.data) ? res.data : (res.data?.records || [])
    const map = {}
    list.forEach((d) => { map[d.deptId] = d.deptName })
    deptMap.value = map
  } catch (err) {
    console.error('获取科室列表失败', err)
  }
}

// 获取医生列表
const getDoctorOptions = async () => {
  
  try {
    const res = await getDoctorPage({
      doctorName: ""
    })
    // console.log("111111111111")
    // console.log(res.data)
    doctorList.value = res.data
  } catch (error) {
    ElMessage.error('获取医生列表失败')
  }
}


// 新增排班
const handleAdd = () => {
  resetForm()
  isEdit.value = false
  dialogVisible.value = true
}

// ============ 一键排班 ============
const oneKeyVisible = ref(false)
const oneKeyLoading = ref(false)
const oneKeyForm = reactive({
  target: 'all',        // all=全部医生 / one=指定医生
  doctorId: '',
  dateRange: [],
  timeSlots: ['上午', '下午'],
  totalSlots: 10
})

const openOneKey = () => {
  oneKeyVisible.value = true
}

const submitOneKey = async () => {
  if (!oneKeyForm.dateRange || oneKeyForm.dateRange.length !== 2) {
    ElMessage.warning('请选择日期范围')
    return
  }
  if (oneKeyForm.target === 'one' && !oneKeyForm.doctorId) {
    ElMessage.warning('请选择医生')
    return
  }
  if (oneKeyForm.timeSlots.length === 0) {
    ElMessage.warning('请至少选择一个时间段')
    return
  }
  oneKeyLoading.value = true
  try {
    const res = await oneKeySchedule({
      doctorId: oneKeyForm.target === 'one' ? oneKeyForm.doctorId : null,
      startDate: oneKeyForm.dateRange[0],
      endDate: oneKeyForm.dateRange[1],
      timeSlots: oneKeyForm.timeSlots.join(','),
      totalSlots: oneKeyForm.totalSlots
    })
    ElMessage.success(`一键排班成功，共生成 ${res.data} 条排班记录`)
    oneKeyVisible.value = false
    getList()
  } catch (err) {
    console.error('一键排班失败', err)
  } finally {
    oneKeyLoading.value = false
  }
}

// ============ 批量删除（选择模式） ============
const selectionMode = ref(false)
const selectedIds = ref([])

// 进入/退出选择模式
const enterSelectionMode = () => {
  selectedIds.value = []
  selectionMode.value = true
}
const exitSelectionMode = () => {
  selectionMode.value = false
  selectedIds.value = []
}

// 表格勾选变化（仅记录排班ID）
const handleSelectionChange = (rows) => {
  selectedIds.value = rows.map((r) => r.scheduleId)
}

// 确认批量删除
const confirmBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 条排班吗？`, '批量删除', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await batchDeleteSchedule(selectedIds.value)
    ElMessage.success(res.data === true ? '批量删除成功' : '批量删除完成')
    exitSelectionMode()
    getList()
  } catch (err) {
    if (err !== 'cancel' && err?.message !== 'cancel') {
      console.error('批量删除失败', err)
    } else {
      ElMessage.info('已取消删除')
    }
  }
}

// 编辑排班
const handleEdit = (row) => {
  resetForm()
  isEdit.value = true
  // 复制行数据到表单
  Object.assign(form, {
    ...row,
    scheduleDate: row.scheduleDate ? new Date(row.scheduleDate) : ''
  })
  dialogVisible.value = true
}

// 删除排班
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该排班吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteSchedule(id)
    ElMessage.success('删除成功')
    getList()
  } catch (error) {
    ElMessage.info('已取消删除')
  }
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    if (isEdit.value) {
      await editSchedule(form)
      ElMessage.success('编辑成功')
    } else {
      await addSchedule(form)
      form.id = ''
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    getList()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 搜索
const handleSearch = () => {
  pagination.pageNum = 1
  getList()
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  Object.assign(form, {
    id: '',
    doctorId: '',
    scheduleDate: '',
    shiftType: 'morning',
    status: 'enabled'
  })
}

// 当前页改变
const handleCurrentChange = (val) => {
  pagination.pageNum = val
}

// 初始化
onMounted(() => {
  getList()
  getDoctorOptions()
  loadDeptMap()
})


</script>

<style scoped>
.schedule-list-container {
  padding: 20px 24px;
}

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