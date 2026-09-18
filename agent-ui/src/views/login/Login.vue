<template>
  <LoginShell>
    <div class="auth-head">
      <h2>管理后台</h2>
      <p>医疗预约管理系统 · 管理员登录</p>
    </div>
    <form @submit.prevent="login" class="auth-form">
      <div class="auth-field">
        <label for="admin-username">用户名</label>
        <input
          id="admin-username"
          type="text"
          v-model="loginForm.username"
          placeholder="请输入用户名"
          required
        />
      </div>
      <div class="auth-field">
        <label for="admin-password">密码</label>
        <div class="pwd-wrap">
          <input
            id="admin-password"
            :type="showPwd ? 'text' : 'password'"
            v-model="loginForm.password"
            placeholder="请输入密码"
            required
          />
          <button type="button" class="pwd-eye" @click="showPwd = !showPwd" :title="showPwd ? '隐藏密码' : '显示密码'">
            <svg v-if="showPwd" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>
            <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
          </button>
        </div>
      </div>
      <div class="auth-field">
        <label>验证码</label>
        <CaptchaInput ref="captchaRef" v-model="captchaCode" />
      </div>
      <button type="submit" class="admin-submit-btn">登 录</button>
    </form>
  </LoginShell>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import jsCookie from 'js-cookie'
import LoginShell from '@/components/LoginShell.vue'
import CaptchaInput from '@/components/CaptchaInput.vue'

const router = useRouter()
const captchaRef = ref(null)
const captchaCode = ref('')
const showPwd = ref(false)
// 登录表单
const loginForm = reactive({
  username: '',
  password: ''
})

// 登录方法
const login = async () => {
  if (!loginForm.username || !loginForm.password) {
    ElMessage.error('请输入用户名和密码')
    return
  }
  if (!captchaRef.value || !captchaRef.value.verify()) {
    ElMessage.error('验证码错误')
    return
  }
  try {
    const res = await request.post('/api/admin/login', {
      username: loginForm.username,
      password: loginForm.password
    })
    // 保存Token到Cookie
    jsCookie.set('token', res.data, { expires: 7 })
    ElMessage.success('登录成功')
    // 跳转到首页
    router.push('/layout/dashboard')
  } catch (error) {
    console.log(error)
  }
}
</script>

<style scoped>
.admin-submit-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  letter-spacing: 4px;
  color: #fff;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  background: linear-gradient(135deg, #1d6ff2 0%, #12b3a8 100%);
  transition: transform 0.2s, box-shadow 0.2s;
}
.admin-submit-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 20px rgba(29, 111, 242, 0.32);
}
</style>
