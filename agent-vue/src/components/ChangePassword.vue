<template>
  <div class="app-layout">
    <PatientSidebar>
      <button class="side-nav-btn primary-action" @click="goBack">
        <i class="fa-solid fa-arrow-left"></i>返回
      </button>
      <button class="side-nav-btn" @click="goProfile">
        <i class="fa-solid fa-user"></i>个人信息
      </button>
    </PatientSidebar>
    <div class="main-content">
      <div class="change-password-container">
        <!-- 标题区域 -->
        <div class="header-section">
          <div class="header-left">
            <span class="header-badge"><i class="fa-solid fa-shield-halved"></i></span>
            <div>
              <h2 class="page-title">修改密码</h2>
              <p class="page-desc">请定期修改密码以确保账户安全</p>
            </div>
          </div>
        </div>

        <!-- 修改密码表单 -->
        <div class="password-form-container">
          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-width="120px"
            class="password-form"
          >
            <!-- 当前密码 -->
            <el-form-item label="当前密码" prop="oldPassword">
              <el-input
                v-model="passwordForm.oldPassword"
                type="password"
                placeholder="请输入当前密码"
                show-password
                clearable
              >
                <template #prefix>
                  <i class="fa-solid fa-lock"></i>
                </template>
              </el-input>
            </el-form-item>

            <!-- 新密码 -->
            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                placeholder="请输入新密码"
                show-password
                clearable
              >
                <template #prefix>
                  <i class="fa-solid fa-key"></i>
                </template>
              </el-input>
              <div class="password-tips">
                <span>密码长度6-20位，包含字母和数字</span>
              </div>
            </el-form-item>

            <!-- 确认新密码 -->
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                placeholder="请再次输入新密码"
                show-password
                clearable
              >
                <template #prefix>
                  <i class="fa-solid fa-key"></i>
                </template>
              </el-input>
            </el-form-item>

            <!-- 操作按钮 -->
            <el-form-item class="form-actions">
              <el-button 
                type="primary" 
                @click="handleChangePassword"
                :loading="loading"
                class="submit-button"
              >
                <i class="fa-solid fa-check"></i>
                &nbsp;确认修改
              </el-button>
              <el-button @click="handleReset">
                <i class="fa-solid fa-rotate-left"></i>
                &nbsp;重置
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 安全提示 -->
        <div class="security-tips">
          <div class="tips-header">
            <i class="fa-solid fa-lightbulb tips-icon"></i>
            <span class="tips-title">安全提示</span>
          </div>
          <ul class="tips-list">
            <li class="tip-item">
              <i class="fa-solid fa-circle-check"></i>
              不要使用过于简单的密码，如"123456"、"password"等
            </li>
            <li class="tip-item">
              <i class="fa-solid fa-circle-check"></i>
              建议使用字母、数字和特殊字符的组合
            </li>
            <li class="tip-item">
              <i class="fa-solid fa-circle-check"></i>
              定期更换密码，建议每3个月更换一次
            </li>
            <li class="tip-item">
              <i class="fa-solid fa-circle-check"></i>
              不要在公共场所输入密码
            </li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import PatientSidebar from './PatientSidebar.vue'
import { handle401 } from '@/utils/auth401'

const router = useRouter()
const passwordFormRef = ref()
const loading = ref(false)

// 表单数据
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 表单验证规则
const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6到20个字符', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6到20个字符', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (value === passwordForm.oldPassword) {
          callback(new Error('新密码不能与旧密码相同'))
        } else if (!/^(?=.*[a-zA-Z])(?=.*\d).{6,20}$/.test(value)) {
          callback(new Error('密码必须包含字母和数字'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 获取用户ID
const getUserId = () => {
  return localStorage.getItem('patientId') // 默认使用ID 7
}

// 处理修改密码
const handleChangePassword = async () => {
  // 表单验证
  if (!passwordFormRef.value) return
  
  const valid = await passwordFormRef.value.validate()
  if (!valid) return

  // 确认对话框 - 移除 icon 配置
  try {
    await ElMessageBox.confirm(
      '确定要修改密码吗？修改后需要重新登录。',
      '确认修改',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
        // 移除了 icon 配置
      }
    )
  } catch {
    return // 用户取消操作
  }

  loading.value = true
  try {
    const userId = getUserId()
    const requestData = {
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    }

    const response = await fetch(`/api/api/patient/changePassword/${userId}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('patientToken') || localStorage.getItem('token') || ''}`
      },
      body: JSON.stringify(requestData)
    })

    if (response.status === 401) {
      // 未登录/令牌失效：清理并跳转登录页
      handle401('patient')
      return
    }
    if (!response.ok) {
      throw new Error('网络响应异常')
    }

    const result = await response.json()
    
    if (result.code === 200) {
      ElMessage.success('密码修改成功，请重新登录')
      
      // 清空本地存储并跳转到登录页
      localStorage.removeItem('patientId')
      setTimeout(() => {
        router.push('/')
      }, 1500)
    } else {
      throw new Error(result.message || '密码修改失败')
    }
  } catch (error) {
    console.error('修改密码失败:', error)
    ElMessage.error(error.message || '修改密码失败，请检查当前密码是否正确')
  } finally {
    loading.value = false
  }
}

// 重置表单
const handleReset = () => {
  passwordFormRef.value?.resetFields()
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 跳转到个人信息页面
const goProfile = () => {
  router.push('/profile')
}
</script>

<style scoped>
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

.change-password-container {
  max-width: 640px;
  margin: 0 auto;
}

/* 头部区域 */
.header-section {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  padding: 18px 22px;
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 2px 10px rgba(23, 62, 129, 0.06);
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
  font-size: 20px;
  color: #0f2e5c;
}
.page-desc {
  margin: 0;
  font-size: 12px;
  color: #8aa0bd;
}

/* 密码表单 */
.password-form-container {
  background: #fff;
  padding: 28px 32px;
  border-radius: 16px;
  box-shadow: 0 6px 24px rgba(23, 62, 129, 0.07);
  margin-bottom: 20px;
}

.password-form {
  max-width: 420px;
  margin: 0 auto;
}

.password-form :deep(.el-form-item__label) {
  font-weight: 600;
  color: #4a5d7a;
}

.password-form :deep(.el-input__wrapper) {
  border-radius: 10px;
}

.password-tips {
  margin-top: 6px;
  font-size: 12px;
  color: #8aa0bd;
}

.form-actions :deep(.el-form-item__content) {
  justify-content: center;
  margin-top: 14px;
}

.submit-button {
  min-width: 140px;
  border: none;
  background: linear-gradient(135deg, #1d6ff2 0%, #12b3a8 100%);
  font-weight: 600;
  transition: transform 0.2s, box-shadow 0.2s, filter 0.2s;
}
.submit-button:hover:not(:disabled) {
  filter: brightness(1.06);
  transform: translateY(-1px);
  box-shadow: 0 8px 20px rgba(29, 111, 242, 0.32);
}

/* 安全提示 */
.security-tips {
  background: #fff;
  padding: 20px 24px;
  border-radius: 14px;
  box-shadow: 0 2px 10px rgba(23, 62, 129, 0.06);
  border-left: 4px solid #12b3a8;
}

.tips-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
}

.tips-icon {
  color: #e6a23c;
  font-size: 17px;
}

.tips-title {
  font-size: 15px;
  font-weight: 700;
  color: #0f2e5c;
}

.tips-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.tip-item {
  display: flex;
  align-items: flex-start;
  gap: 9px;
  margin-bottom: 10px;
  color: #5b7191;
  font-size: 13px;
  line-height: 1.6;
}

.tip-item i {
  color: #0f9e8c;
  margin-top: 3px;
  flex-shrink: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .app-layout {
    flex-direction: column;
  }

  .main-content {
    padding: 10px 12px 14px;
  }

  .password-form-container {
    padding: 20px;
  }
}

/* 表单样式调整 */
:deep(.el-input__prefix) {
  display: flex;
  align-items: center;
}
</style>