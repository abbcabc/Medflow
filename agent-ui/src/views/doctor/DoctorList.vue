<template>
  <div class="doctor-container">
    <div class="header-section">
      <div class="header-left">
        <span class="header-badge"><i class="fa-solid fa-user-doctor"></i></span>
        <div>
          <h2 class="page-title">医生管理</h2>
          <p class="page-desc">管理医生信息、职称与接诊状态</p>
        </div>
      </div>
      <div class="search-box">
        <el-input v-model="searchForm.doctorName" placeholder="医生姓名" style="width: 220px" />
        <el-button type="primary" @click="getList">搜索</el-button>
        <el-button type="success" @click="openAdd">新增医生</el-button>
      </div>
    </div>

    <div class="table-card">
      <el-table :data="pageData" border stripe style="width: 100%">
        <el-table-column type="index" :index="(i) => (pageNum - 1) * pageSize + i + 1" label="序号" width="64" align="center" />
        <el-table-column label="ID" prop="doctorId" width="80" />
        <el-table-column label="医生姓名" prop="doctorName" width="130" />
        <el-table-column label="科室" width="120">
          <template #default="scope">{{ deptMap[scope.row.deptId] || scope.row.deptId }}</template>
        </el-table-column>
        <el-table-column label="职称" prop="title" width="200" />
        <el-table-column label="擅长" prop="specialty" min-width="230" />
        <el-table-column label="从业年限" prop="yearsExperience" width="110" />

        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.isAvailable === 1 ? 'success' : 'danger'" class="status-pill">
              {{ scope.row.isAvailable === 1 ? '接诊中' : '停诊' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button type="primary" size="small" @click="openEdit(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row.doctorId)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 前端分页 -->
      <el-pagination
        v-model:current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next, jumper"
        @current-change="handlePageChange"
        class="table-pagination"
      />
    </div>

    <el-dialog v-model="dialogVisible" title="医生信息" width="680px">
      <el-form :model="form" label-width="120px">
        <el-form-item label="医生姓名"><el-input v-model="form.doctorName" /></el-form-item>
        <el-form-item label="科室">
          <el-select v-model="form.deptId" placeholder="请选择科室" style="width: 100%">
            <el-option v-for="d in deptList" :key="d.deptId" :label="d.deptName" :value="d.deptId" />
          </el-select>
        </el-form-item>
        <el-form-item label="职称"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="擅长领域"><el-input v-model="form.specialty" /></el-form-item>
        <el-form-item label="简介"><el-input v-model="form.introduction" type="textarea" rows="3" /></el-form-item>
        <el-form-item label="从业年限"><el-input v-model="form.yearsExperience" /></el-form-item>
        <el-form-item label="是否接诊">
          <el-radio-group v-model="form.isAvailable">
            <el-radio :label="1">接诊中</el-radio>
            <el-radio :label="0">停诊</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDoctorPage, addDoctor, updateDoctor, deleteDoctor } from '@/api/doctor'
import { getDeptPage } from '@/api/dept'

// 分页配置
const pageNum = ref(1)
const pageSize = ref(6)
const total = ref(0)
const allData = ref([])    // 所有医生数据
const pageData = ref([])   // 当前页数据

const dialogVisible = ref(false)
const isEdit = ref(false)

const searchForm = reactive({ doctorName: '' })

const form = reactive({
  doctorId: '',
  deptId: '',
  doctorName: '',
  title: '',
  specialty: '',
  introduction: '',
  yearsExperience: '',
  isAvailable: 1,
  avatarUrl: ''
})

// 获取列表（安全版，不会再报错）
const getList = async () => {
  try {
    const res = await getDoctorPage({
      doctorName: searchForm.doctorName
    })

    // ✅ 安全获取数组（兼容你的返回格式）
    let list = []
    if (res.data && Array.isArray(res.data)) {
      list = res.data
    } else if (res.data && res.data.records) {
      list = res.data.records
    }

    // ✅ 倒序排列（最新医生在前）
    // list.sort((a, b) => (b.doctorId || 0) - (a.doctorId || 0))

    allData.value = list
    total.value = list.length
    pageNum.value = 1 // 重置页码
    handlePageChange()
  } catch (err) {
    console.error(err)
    ElMessage.error('获取医生列表失败')
    allData.value = []
    pageData.value = []
  }
}

// 前端分页核心
const handlePageChange = () => {
  const start = (pageNum.value - 1) * pageSize.value
  const end = start + pageSize.value
  pageData.value = allData.value.slice(start, end)
}

// 打开新增
const openAdd = () => {
  isEdit.value = false
  Object.assign(form, {
    doctorId: '', deptId: '', doctorName: '', title: '',
    specialty: '', introduction: '', yearsExperience: '', isAvailable: 1
  })
  dialogVisible.value = true
}

// 打开编辑
const openEdit = (row) => {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

// 提交
const submitForm = async () => {
  try {
    isEdit.value ? await updateDoctor(form) : await addDoctor(form)
    ElMessage.success('操作成功')
    dialogVisible.value = false
    getList()
  } catch {
    ElMessage.error('操作失败')
  }
}

// 删除
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除？')
    await deleteDoctor(id)
    ElMessage.success('删除成功')
    getList()
  } catch {
    ElMessage.info('已取消')
  }
}

// 科室 ID -> 名称映射
const deptList = ref([])
const deptMap = ref({})

// 加载科室列表并构建 id->name 映射
const loadDeptList = async () => {
  try {
    const res = await getDeptPage()
    const list = Array.isArray(res.data) ? res.data : (res.data?.records || [])
    deptList.value = list
    const map = {}
    list.forEach((d) => { map[d.deptId] = d.deptName })
    deptMap.value = map
  } catch (err) {
    console.error('获取科室列表失败', err)
  }
}

onMounted(() => {
  getList()
  loadDeptList()
})
</script>

<style scoped>
.doctor-container {
  min-height: 100vh;
  box-sizing: border-box;
  padding: 20px 24px;
  background: #f2f6fb;
}

/* 头部卡片 */
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

/* 表格卡片 */
.table-card {
  background: #fff;
  border: 1px solid #e6edf5;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(23, 58, 99, 0.06);
  padding: 18px 20px;
}

/* el-table 美化 */
.table-card :deep(.el-table) {
  --el-table-header-bg-color: #f4f8fc;
  --el-table-border-color: #eef2f8;
  --el-table-row-hover-bg-color: #f7fafd;
  border-radius: 12px;
  overflow: hidden;
}
.table-card :deep(.el-table th.el-table__cell) {
  background: #f4f8fc;
  color: #4a5d7a;
  font-weight: 600;
  border-bottom: 1px solid #eef2f8;
}
.table-card :deep(.el-table td.el-table__cell) {
  border-bottom: 1px solid #eef2f8;
}
.table-card :deep(.el-table--border .el-table__cell) {
  border-right: 1px solid #eef2f8;
}
.table-card :deep(.el-table--border::after),
.table-card :deep(.el-table--border::before),
.table-card :deep(.el-table__inner-wrapper::before) {
  display: none;
}
.table-card :deep(.el-table .cell) {
  color: #4a5d7a;
}

/* 状态标签 pill 化 */
.table-card :deep(.el-tag.status-pill) {
  border: none;
  border-radius: 999px;
  padding: 2px 12px;
  font-weight: 600;
}

/* 分页 */
.table-pagination {
  margin-top: 16px;
  justify-content: flex-end;
}

/* 按钮风格 */
:deep(.el-button) {
  border-radius: 10px;
}
:deep(.el-button--primary) {
  background: linear-gradient(135deg, #1d6ff2 0%, #12b3a8 100%);
  border: none;
}
:deep(.el-button--primary:hover) {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(29, 111, 242, 0.32);
}

/* 弹窗 */
:deep(.el-dialog) {
  border-radius: 16px;
}
:deep(.el-dialog__title) {
  font-weight: 700;
  color: #243b5a;
}
:deep(.el-form-item__label) {
  color: #4a5d7a;
  font-weight: 600;
}

/* 响应式 */
@media (max-width: 900px) {
  .doctor-container { padding: 12px 14px; }
  .header-section { flex-direction: column; align-items: stretch; }
}
</style>
