<template>
  <LoginShell>
    <div class="auth-head">
      <h2>欢迎回来</h2>
      <p>登录患者账号，继续您的就医之旅</p>
    </div>
    <form @submit.prevent="handleLogin" class="auth-form">
      <div class="auth-field">
        <label for="login-phone">手机号</label>
        <input
          type="tel"
          id="login-phone"
          v-model="loginForm.phone"
          placeholder="请输入手机号"
          required
        />
      </div>

      <div class="auth-field">
        <label for="login-password">密码</label>
        <div class="pwd-wrap">
          <input
            :type="showPwd ? 'text' : 'password'"
            id="login-password"
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

      <CaptchaInput ref="captcha" v-model="captchaCode" />

      <button type="submit" :disabled="loading" class="auth-submit">
        {{ loading ? '登录中...' : '登录' }}
      </button>
    </form>

    <div v-if="message" :class="['auth-message', messageType]">
      {{ message }}
    </div>

    <div class="auth-links">
      <span>还没有账号？</span>
      <a href="#" @click.prevent="goToRegister">立即注册</a>
      <a href="#" @click.prevent="goToDoctorLogin">医生登录</a>
    </div>
  </LoginShell>
</template>

<script>
import axios from 'axios'
import LoginShell from './LoginShell.vue'
import CaptchaInput from './CaptchaInput.vue'
export default {
  name: 'PatientLogin',
  components: { LoginShell, CaptchaInput },
  data() {
    return {
      loginForm: {
        phone: '',
        password: ''
      },
      showPwd: false,
      captchaCode: '',
      loading: false,
      message: '',
      messageType: '' 
    }
  },
  methods: {
  async handleLogin() {
    if (!this.$refs.captcha.verify()) {
      this.showMessage('验证码错误', 'error');
      return;
    }
    this.loading = true; // 添加loading状态
    this.message = ''; // 清空之前的信息
    
    try {
      
      const response = await axios.post(
        '/api/api/patient/login',
        this.loginForm
      );
      
      if (response.data.code === 200) {
        this.showMessage('登录成功', 'success');
        // console.log('登录响应:', response);
        // console.log("token",response.data.data.token)
        // console.log("用户id",response.data.data.patientId)
        // 保存登录状态（防御性处理：剥离后端可能携带的 Bearer 前缀，请求时统一拼接）
        localStorage.setItem('patientToken', (response.data.data.token || '').replace(/^Bearer\s+/i, ''));
        localStorage.setItem('patientId', response.data.data.patientId);
        
        
        // 清空表单
        //this.loginForm.phone = '';
        //this.loginForm.password = '';
        
        setTimeout(() => {
          this.$router.push('/chat')
        }, 2000);
        
        
      } else {
        this.showMessage(response.data.message || '登录失败', 'error');
      }
    } catch (error) {
      console.error('登录错误:', error);
      this.showMessage('网络错误，请稍后重试', 'error');
    } finally {
      this.loading = false;
    }
  },
  
  // 修改注册跳转方法
  goToRegister() {
    this.$router.push('/register');
  },
  goToDoctorLogin(){
    this.$router.push('/doctorLogin');
  },
  showMessage(msg, type) {
    this.message = msg;
    this.messageType = type;
    setTimeout(() => {
      this.message = '';    
    }, 2000);
    
  }
}
}
</script>

<style scoped>
/* 视觉样式统一由 LoginShell 提供 */
</style>