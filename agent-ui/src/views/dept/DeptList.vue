<template>
  <div class="dept-container">
    <div class="header-section">
      <div class="header-left">
        <span class="header-badge"><i class="fa-solid fa-hospital"></i></span>
        <div>
          <h2 class="page-title">科室管理</h2>
          <p class="page-desc">管理科室信息、联系电话与启用状态</p>
        </div>
      </div>
      <div class="search-box">
        <el-input v-model="searchForm.deptName" placeholder="科室名称" style="width: 220px" />
        <el-button type="primary" @click="getList">搜索</el-button>
        <el-button type="success" @click="openAdd">新增科室</el-button>
      </div>
    </div>

    <el-table
      :data="tableData || []"
      border
      stripe
      style="width: 100%; margin-top: 20px"
    >
      <el-table-column type="index" :index="(i) => (pageNum - 1) * pageSize + i + 1" label="序号" width="64" align="center" />
      <el-table-column label="科室ID" prop="deptId" width="100" />
      <el-table-column label="科室名称" prop="deptName" width="150" />
      
      <!-- 科室描述列加宽 -->
      <el-table-column label="科室描述" prop="deptDescription" min-width="350" />
      
      <el-table-column label="上级科室ID" prop="parentDeptId" width="120" />
      <el-table-column label="联系电话" prop="contactPhone" width="150" />
      
      <el-table-column label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row?.status === 1 ? 'success' : 'danger'">
            {{ scope.row?.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      
      <!-- 创建时间只显示日期 -->
      <el-table-column label="创建时间" width="130">
        <template #default="scope">
          {{ scope.row?.createTime ? scope.row.createTime.split('T')[0] : '' }}
        </template>
      </el-table-column>

      <el-table-column label="操作" width="180">
        <template #default="scope">
          <el-button v-if="scope.row" type="primary" size="small" @click="openEdit(scope.row)">编辑</el-button>
          <el-button v-if="scope.row" type="danger" size="small" @click="handleDelete(scope.row.deptId)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="pageNum"
      :page-size="pageSize"
      :total="total"
      layout="total, prev, pager, next, jumper"
      @current-change="getList"
      class="table-pagination"
    />

    <el-dialog v-model="dialogVisible" title="科室信息" width="550px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="科室名称">
          <el-input v-model="form.deptName" placeholder="请输入科室名称" />
        </el-form-item>
        <el-form-item label="科室描述">
          <el-input v-model="form.deptDescription" type="textarea" rows="3" />
        </el-form-item>
        <el-form-item label="上级科室ID">
          <el-input v-model="form.parentDeptId" placeholder="0=一级科室" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.contactPhone" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
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
import { getDeptPage, addDept, updateDept, deleteDept } from '@/api/dept'

const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)

const searchForm = reactive({ deptName: '' })

const form = reactive({
  deptId: '',
  deptName: '',
  deptDescription: '',
  parentDeptId: 0,
  contactPhone: '',
  status: 1
})

// 获取列表
const getList = async () => {
  try {
    const res = await getDeptPage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      deptName: searchForm.deptName
    })

    if (res?.code === 200 && res.data) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    } else {
      tableData.value = []
      total.value = 0
    }
  } catch (err) {
    console.error('获取数据失败', err)
    tableData.value = []
    total.value = 0
    ElMessage.error('获取科室列表失败')
  }
}

// 打开新增
const openAdd = () => {
  isEdit.value = false
  Object.assign(form, {
    deptId: '',
    deptName: '',
    deptDescription: '',
    parentDeptId: 0,
    contactPhone: '',
    status: 1
  })
  dialogVisible.value = true
}

// 打开编辑
const openEdit = (row) => {
  if (!row) return
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

// 提交
const submitForm = async () => {
  try {
    if (isEdit.value) {
      await updateDept(form)
      ElMessage.success('修改成功')
    } else {
      await addDept(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    getList()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

// 删除
const handleDelete = async (id) => {
  if (!id) return
  try {
    await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
    await deleteDept(id)
    ElMessage.success('删除成功')
    getList()
  } catch {
    ElMessage.info('已取消')
  }
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.dept-container {
  padding: 20px;
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