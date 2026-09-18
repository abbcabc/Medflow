<template>
  <LoginShell>
    <div class="auth-head">
      <h2>医生工作台</h2>
      <p>登录后查看排班与接诊信息</p>
    </div>

    <form @submit.prevent="handleLogin" class="auth-form">
      <!-- 部门选择 -->
      <div class="auth-field">
        <label for="department" class="form-label">选择部门</label>
        <select
          id="department"
          v-model="loginForm.deptId"
          required
          :disabled="loading"
        >
          <option value="">请选择部门</option>
          <option
            v-for="dept in departmentList"
            :key="dept.deptId"
            :value="dept.deptId"
          >
            {{ dept.deptName }}
          </option>
        </select>
      </div>

      <!-- 医生姓名 -->
      <div class="auth-field">
        <label for="doctorName">医生姓名</label>
        <input
          id="doctorName"
          v-model="loginForm.doctorName"
          type="text"
          placeholder="请输入医生姓名"
          required
          :disabled="loading"
        />
      </div>

      <!-- 密码 -->
      <div class="auth-field">
        <label for="password">密码</label>
        <div class="pwd-wrap">
          <input
            id="password"
            v-model="loginForm.password"
            :type="showPwd ? 'text' : 'password'"
            placeholder="请输入密码"
            required
            :disabled="loading"
          />
          <button type="button" class="pwd-eye" @click="showPwd = !showPwd" :title="showPwd ? '隐藏密码' : '显示密码'">
            <svg v-if="showPwd" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>
            <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
          </button>
        </div>
      </div>

      <CaptchaInput ref="captchaRef" v-model="captchaCode" />

      <!-- 登录按钮 -->
      <button
        type="submit"
        class="auth-submit"
        :disabled="loading || !isFormValid"
      >
        <span v-if="loading" class="loading-text">
          <span class="spinner"></span>
          登录中...
        </span>
        <span v-else>登录</span>
      </button>
    </form>

    <!-- 错误提示 -->
    <div v-if="errorMessage" class="auth-message error">
      {{ errorMessage }}
    </div>

    <div class="auth-links">
      <span>不是医生？</span>
      <a href="#" @click.prevent="goPatientLogin">患者登录</a>
    </div>
  </LoginShell>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import LoginShell from './LoginShell.vue'
import CaptchaInput from './CaptchaInput.vue'

// 响应式数据
const departmentList = ref([])
const loading = ref(false)
const errorMessage = ref('')
const showPwd = ref(false)
const captchaCode = ref('')
const captchaRef = ref(null)

// 登录表单数据
const loginForm = reactive({
  deptId: '',
  doctorName: '',
  password: ''
})

const router = useRouter()

// 计算属性：表单是否有效
const isFormValid = computed(() => {
  return loginForm.deptId && loginForm.doctorName.trim() && loginForm.password.trim()
})

// 获取部门列表
const fetchDepartments = async () => {
  try {
    const response = await fetch('/api/dept/list')
    const result = await response.json()
    
    if (result.code === 200) {
      departmentList.value = result.data.records
      console.log(departmentList.value)
    } else {
      errorMessage.value = '获取部门列表失败: ' + result.message
    }
  } catch (error) {
    console.error('获取部门列表错误:', error)
    errorMessage.value = '网络错误，请检查连接'
  }
}

// 处理登录
const handleLogin = async () => {
  if (!captchaRef.value || !captchaRef.value.verify()) {
    errorMessage.value = '验证码错误'
    return
  }
  if (!isFormValid.value) {
    errorMessage.value = '请填写完整信息'
    return
  }

  loading.value = true
  errorMessage.value = ''

  try {
    const response = await fetch('/api/api/doctor/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        deptId: parseInt(loginForm.deptId),
        doctorName: loginForm.doctorName.trim(),
        password: loginForm.password
      })
    })

    const result = await response.json()

    if (result.code === 200) {
      // 登录成功，保存token和用户信息
      localStorage.setItem('doctorToken', result.data.token)
      localStorage.setItem('doctorId', result.data.doctorId)
      // localStorage.setItem('doctorInfo', JSON.stringify(result.data))
      
      // 显示成功消息
      alert('登录成功！')
      
      // 跳转到医生主页
      router.push('/doctor/dashboard')
    } else {
      errorMessage.value = result.message || '登录失败'
    }
  } catch (error) {
    console.error('登录错误:', error)
    errorMessage.value = '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}

// 组件挂载时获取部门列表
onMounted(() => {
  fetchDepartments()
})

// 返回患者登录
const goPatientLogin = () => {
  router.push('/plogin')
}
</script>

<style scoped>
.loading-text {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.spinner {
  width: 16px;
  height: 16px;
  border: 2px solid transparent;
  border-top: 2px solid white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>