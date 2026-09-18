<template>
  <LoginShell>
    <div class="auth-head">
      <h2>创建账号</h2>
      <p>注册患者账号，开启智能就医之旅</p>
    </div>
    <form @submit.prevent="handleRegister" class="auth-form">
      <div class="auth-field">
        <label for="phone">手机号</label>
        <input
          type="tel"
          id="phone"
          v-model="registerForm.phone"
          placeholder="请输入手机号"
          required
        />
      </div>

      <div class="auth-field">
        <label for="password">密码</label>
        <div class="pwd-wrap">
          <input
            :type="showPwd ? 'text' : 'password'"
            id="password"
            v-model="registerForm.password"
            placeholder="请设置登录密码"
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
        {{ loading ? '注册中...' : '注册并登录' }}
      </button>
    </form>

    <div v-if="message" :class="['auth-message', messageType]">
      {{ message }}
    </div>

    <div class="auth-links">
      <span>已有账号？</span>
      <a href="#" @click.prevent="goToLogin">返回登录</a>
    </div>
  </LoginShell>
</template>

<script>
  import axios from 'axios'
  import LoginShell from './LoginShell.vue'
  import CaptchaInput from './CaptchaInput.vue'
export default {
  name: 'PatientRegister',
  components: { LoginShell, CaptchaInput },
  data() {
    return {
      registerForm: {
        phone: '',
        password: ''
      },
      showPwd: false,
      captchaCode: '',
      loading: false,
      message: '',
      messageType: '' // 'success' or 'error'
    }
  },
  methods: {
    async handleRegister() {
      if (!this.$refs.captcha.verify()) {
        this.showMessage('验证码错误', 'error')
        return
      }
      if (!this.registerForm.phone || !this.registerForm.password) {
        this.showMessage('请填写完整信息', 'error')
        return
      }
      
      this.loading = true
      this.message = ''
      
      try {
        const response = await axios.post('/api/api/patient/register', this.registerForm)
        
        if (response.data.code === 200) {
          this.showMessage('注册成功', 'success')
          // this.registerForm.phone = ''
          // this.registerForm.password = ''
            const responseLogin = await axios.post(
              '/api/api/patient/login',
              this.registerForm
            );
            localStorage.setItem('patientToken', responseLogin.data.data.token || '');
            localStorage.setItem('patientId', responseLogin.data.data.patientId);

            this.$router.push('/chat');
        } else {
          this.showMessage(response.data.message || '注册失败', 'error')
        }
      } catch (error) {
        console.error('注册失败:', error)
        this.showMessage('网络错误，请稍后重试', 'error')
      } finally {
        this.loading = false
      }
    },
    goToLogin() {
      this.$router.push('/plogin');
    },
    showMessage(msg, type) {
      this.message = msg
      this.messageType = type
      // 3秒后自动清除消息
      setTimeout(() => {
        this.message = ''
      }, 3000)
    }
  }
}
</script>
