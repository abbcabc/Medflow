<template>
  <div class="app-layout">
    <PatientSidebar>
      <button class="side-nav-btn primary-action" @click="goChat">
        <i class="fa-solid fa-comments"></i>返回对话
      </button>
      <button class="side-nav-btn" @click="goAppointment">
        <i class="fa-solid fa-calendar-check"></i>查看预约信息
      </button>
      <button class="side-nav-btn" @click="goChangePassword">
        <i class="fa-solid fa-key"></i>修改密码
      </button>
    </PatientSidebar>

    <div class="main-content">
      <div class="profile-page">
        <div class="profile-wrapper">
      <!-- 顶部栏 -->
      <div class="profile-header">
        <div class="header-left">
          <span class="header-badge"><i class="fa-solid fa-user-pen"></i></span>
          <div>
            <h2>个人信息</h2>
            <p>完善信息有助于小智为您提供更精准的导诊服务</p>
          </div>
        </div>
        <button
          class="ghost-btn"
          @click="goChat"
          :disabled="loading || !normalizedPatientId"
        >
          <i class="fa-solid fa-comments"></i>&nbsp;返回对话
        </button>
      </div>

      <!-- 提示信息 -->
      <div v-if="loading" class="tip tip-loading">正在加载个人信息...</div>
      <div v-if="error" class="tip tip-error">{{ error }}</div>
      <div v-if="successMessage" class="tip tip-success">{{ successMessage }}</div>
      <div v-if="!normalizedPatientId && !loading" class="tip tip-warning">
        未找到患者信息，请重新登录
      </div>

      <!-- 编辑模式：显示表单 -->
      <form v-if="isEditing && normalizedPatientId" @submit.prevent="handleSubmit" class="profile-card">
        <h3 class="card-title">编辑个人信息</h3>
        <div class="form-row">
          <div class="form-group">
            <label for="username">用户名</label>
            <input type="text" id="username" v-model="profileForm.username" required />
          </div>
          <div class="form-group">
            <label for="realName">真实姓名</label>
            <input type="text" id="realName" v-model="profileForm.realName" required />
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label for="age">年龄</label>
            <input type="number" id="age" v-model.number="profileForm.age" min="0" max="150" />
          </div>
          <div class="form-group">
            <label for="gender">性别</label>
            <select id="gender" v-model="profileForm.gender">
              <option value="">请选择性别</option>
              <option value="男">男</option>
              <option value="女">女</option>
              <option value="其他">其他</option>
            </select>
          </div>
        </div>

        <div class="form-group">
          <label for="idCard">身份证号</label>
          <input
            type="text"
            id="idCard"
            v-model="profileForm.idCard"
            maxlength="18"
            placeholder="请输入18位身份证号"
          />
        </div>

        <div class="form-group">
          <label for="email">邮箱</label>
          <input
            type="email"
            id="email"
            v-model="profileForm.email"
            placeholder="请输入邮箱地址"
          />
        </div>

        <!-- 操作按钮 -->
        <div class="form-actions">
          <button type="submit" :disabled="submitting" class="save-btn">
            {{ submitting ? '保存中...' : '保存' }}
          </button>
          <button type="button" @click="cancelEditing" :disabled="submitting" class="cancel-btn">
            取消
          </button>
        </div>
      </form>

      <!-- 查看模式：显示信息卡片 -->
      <div v-if="!isEditing && normalizedPatientId && !loading" class="profile-card">
        <div class="view-top">
          <span class="avatar-circle">{{ (profileForm.realName || profileForm.username || '患').charAt(0) }}</span>
          <div class="view-name">
            <strong>{{ profileForm.realName || profileForm.username || '未设置姓名' }}</strong>
            <span>{{ profileForm.gender || '性别未设置' }}<template v-if="profileForm.age"> · {{ profileForm.age }} 岁</template></span>
          </div>
          <button @click="startEditing" class="ghost-btn">
            <i class="fa-solid fa-pen-to-square"></i>&nbsp;修改信息
          </button>
        </div>
        <div class="info-content">
          <div class="info-item">
            <span class="label"><i class="fa-solid fa-id-badge"></i>用户名</span>
            <span class="value">{{ profileForm.username || '未设置' }}</span>
          </div>
          <div class="info-item">
            <span class="label"><i class="fa-solid fa-signature"></i>真实姓名</span>
            <span class="value">{{ profileForm.realName || '未设置' }}</span>
          </div>
          <div class="info-item">
            <span class="label"><i class="fa-solid fa-cake-candles"></i>年龄</span>
            <span class="value">{{ profileForm.age || '未设置' }}</span>
          </div>
          <div class="info-item">
            <span class="label"><i class="fa-solid fa-venus-mars"></i>性别</span>
            <span class="value">{{ profileForm.gender || '未设置' }}</span>
          </div>
          <div class="info-item">
            <span class="label"><i class="fa-solid fa-id-card"></i>身份证号</span>
            <span class="value">{{ profileForm.idCard || '未设置' }}</span>
          </div>
          <div class="info-item">
            <span class="label"><i class="fa-solid fa-envelope"></i>邮箱</span>
            <span class="value">{{ profileForm.email || '未设置' }}</span>
          </div>
        </div>
      </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import PatientSidebar from './PatientSidebar.vue'
export default {
  name: 'PatientProfile',
  components: { PatientSidebar },
  data() {
    return {
      profileForm: {
        patientId: null,
        username: '',
        realName: '',
        age: null,
        idCard: '',
        email: '',
        gender: ''
      },
      originalForm: {},
      loading: false,
      submitting: false,
      isEditing: false,
      error: '',
      successMessage: ''
    }
  },
  computed: {
    // 从 localStorage 读取并转换为整数
    normalizedPatientId() {
      try {
        const patientId = localStorage.getItem('patientId')
        if (!patientId) {
          console.warn('未找到 patientId')
          return null
        }
        
        // 转换为整数
        const patientIdInt = parseInt(patientId, 10)
        
        // 检查转换是否成功
        if (isNaN(patientIdInt)) {
          console.error('patientId 转换失败:', patientId)
          return null
        }
        
        console.log('从 localStorage 读取的 patientId:', patientId, '转换为:', patientIdInt)
        return patientIdInt
      } catch (error) {
        console.error('读取 patientId 失败:', error)
        return null
      }
    }
  },
  mounted() {
    this.fetchProfile()
  },
  methods: {
    async fetchProfile() {
      // 检查 patientId 是否存在
      if (!this.normalizedPatientId) {
        this.error = '未找到患者信息，请重新登录'
        return
      }
      
      this.loading = true
      this.error = ''
      
      try {
        console.log('发送请求，patientId:', this.normalizedPatientId, '类型:', typeof this.normalizedPatientId)
        
        const response = await axios.get('api/api/patient/profile', {
          params: {
            patientId: this.normalizedPatientId
          },
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('patientToken') || localStorage.getItem('token') || ''}`,
            'Content-Type': 'application/x-www-form-urlencoded'
          }
        })
        
        console.log('获取个人信息响应:', response.data)
        
        if (response.data.code === 200 && response.data.data) {
          const profileData = response.data.data
          this.profileForm = {
            patientId: this.normalizedPatientId,
            username: profileData.username || '',
            realName: profileData.realName || '',
            age: profileData.age || null,
            idCard: profileData.idCard || '',
            email: profileData.email || '',
            gender: profileData.gender || ''
          }
          this.originalForm = { ...this.profileForm }
          console.log('个人信息加载成功:', this.profileForm)
        } else {
          this.error = response.data.message || '获取个人信息失败'
        }
      } catch (error) {
        console.error('获取个人信息失败:', error)
        if (error.response) {
          console.error('错误响应:', error.response)
          if (error.response.status === 403) {
            this.error = '权限不足，请重新登录'
            this.$emit('auth-failed')
          } else if (error.response.status === 404) {
            this.error = '用户信息不存在'
          } else if (error.response.status === 400) {
            this.error = '请求参数错误，请检查患者ID'
          } else {
            this.error = `服务器错误: ${error.response.status}`
          }
        } else {
          this.error = '网络错误，请稍后重试'
        }
      } finally {
        this.loading = false
      }
    },

    async handleSubmit() {
      // 验证必填字段
      if (!this.profileForm.username.trim()) {
        this.error = '用户名不能为空'
        return
      }
      
      if (!this.profileForm.realName.trim()) {
        this.error = '真实姓名不能为空'
        return
      }

      // 再次验证 patientId
      if (!this.normalizedPatientId) {
        this.error = '患者ID无效，请重新登录'
        return
      }

      this.submitting = true
      this.error = ''

      try {
        const submitData = {
          ...this.profileForm,
          patientId: this.normalizedPatientId
        }

        console.log('提交更新数据:', submitData)

        const response = await axios.put('api/api/patient/profile', submitData, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('patientToken') || localStorage.getItem('token') || ''}`,
            'Content-Type': 'application/json'
          }
        })
        
        if (response.data.code === 200) {
          this.successMessage = '个人信息更新成功'
          this.isEditing = false
          this.originalForm = { ...this.profileForm }
          
          setTimeout(() => {
            this.successMessage = ''  
          }, 2000)
        } else {
          this.error = response.data.message || '更新失败'
        }
      } catch (error) {
        console.error('更新个人信息失败:', error)
        if (error.response && error.response.status === 403) {
          this.error = '权限不足，请重新登录'
          this.$emit('auth-failed')
        } else {
          this.error = '网络错误，请稍后重试'
        }
      } finally {
        this.submitting = false
      }
    },

    startEditing() {
      this.isEditing = true
      this.error = ''
      this.successMessage = ''
    },

    cancelEditing() {
      this.isEditing = false
      this.profileForm = { ...this.originalForm }
      this.error = ''
    },

    // 重新加载方法
    reloadProfile() {
      this.fetchProfile()
    },

    goChat(){
      this.$router.push('/chat')
    },

    goAppointment(){
      this.$router.push('/appointmentDetail')
    },

    goChangePassword(){
      this.$router.push('/changePassword')
    }
  }
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

.profile-page {
  min-height: 100%;
  padding: 0;
  box-sizing: border-box;
}

.profile-wrapper {
  max-width: 760px;
  margin: 0 auto;
}

/* 顶部栏 */
.profile-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 22px;
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
.profile-header h2 {
  margin: 0 0 2px;
  font-size: 20px;
  color: #0f2e5c;
}
.profile-header p {
  margin: 0;
  font-size: 12px;
  color: #8aa0bd;
}

.ghost-btn {
  padding: 9px 16px;
  border: 1px solid #cfe0ff;
  border-radius: 10px;
  background: #f2f7ff;
  color: #1d6ff2;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s, transform 0.15s;
  white-space: nowrap;
}
.ghost-btn:hover:not(:disabled) {
  background: #e5efff;
  transform: translateY(-1px);
}
.ghost-btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

/* 提示信息 */
.tip {
  padding: 12px 16px;
  border-radius: 10px;
  margin-bottom: 16px;
  text-align: center;
  font-size: 14px;
}
.tip-loading { background: #ecf3ff; color: #1d6ff2; }
.tip-error {
  background: #fdecec;
  color: #c0392b;
  border: 1px solid #f5c6c6;
}
.tip-success {
  background: #e6f7ef;
  color: #0f7a4d;
  border: 1px solid #b9e8d0;
}
.tip-warning {
  background: #fff7e6;
  color: #a3620a;
  border: 1px solid #ffe3ad;
}

/* 卡片（表单/查看共用） */
.profile-card {
  background: #fff;
  padding: 26px 28px;
  border-radius: 16px;
  box-shadow: 0 6px 24px rgba(23, 62, 129, 0.07);
}
.card-title {
  margin: 0 0 22px;
  font-size: 17px;
  color: #0f2e5c;
}

/* 表单样式 */
.form-row {
  display: flex;
  gap: 18px;
}
.form-row .form-group {
  flex: 1;
}
.form-group {
  margin-bottom: 18px;
}
label {
  display: block;
  margin-bottom: 6px;
  font-size: 13px;
  font-weight: 600;
  color: #4a5d7a;
}
input, select {
  width: 100%;
  padding: 11px 14px;
  border: 1.5px solid #dbe4f0;
  border-radius: 10px;
  font-size: 14px;
  color: #243b5a;
  background: #f8fafd;
  box-sizing: border-box;
  transition: border-color 0.2s, box-shadow 0.2s, background 0.2s;
}
input::placeholder { color: #a8b8cd; }
input:focus, select:focus {
  outline: none;
  border-color: #1d6ff2;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(29, 111, 242, 0.12);
}

.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #edf2f9;
}
.save-btn {
  padding: 11px 28px;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, #1d6ff2 0%, #12b3a8 100%);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}
.save-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 8px 20px rgba(29, 111, 242, 0.32);
}
.save-btn:disabled {
  background: #b7c6da;
  cursor: not-allowed;
}
.cancel-btn {
  padding: 11px 24px;
  border: 1px solid #dbe4f0;
  border-radius: 10px;
  background: #fff;
  color: #5b7191;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s;
}
.cancel-btn:hover:not(:disabled) {
  background: #f4f8fd;
}
.cancel-btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

/* 查看模式 */
.view-top {
  display: flex;
  align-items: center;
  gap: 16px;
  padding-bottom: 20px;
  margin-bottom: 6px;
  border-bottom: 1px solid #edf2f9;
}
.avatar-circle {
  width: 56px;
  height: 56px;
  flex: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: linear-gradient(135deg, #1d6ff2 0%, #12b3a8 100%);
  color: #fff;
  font-size: 22px;
  font-weight: 700;
  box-shadow: 0 6px 16px rgba(29, 111, 242, 0.28);
}
.view-name {
  display: flex;
  flex-direction: column;
  gap: 3px;
  flex: 1;
}
.view-name strong {
  font-size: 17px;
  color: #0f2e5c;
}
.view-name span {
  font-size: 13px;
  color: #8aa0bd;
}

.info-content {
  padding-top: 12px;
}
.info-item {
  display: flex;
  align-items: center;
  padding: 13px 0;
  border-bottom: 1px dashed #eef2f9;
}
.info-item:last-child {
  border-bottom: none;
}
.info-item .label {
  display: inline-flex;
  align-items: center;
  gap: 9px;
  font-size: 13px;
  font-weight: 600;
  color: #64789a;
  min-width: 130px;
}
.info-item .label i {
  width: 16px;
  text-align: center;
  color: #9db4d4;
}
.info-item .value {
  color: #243b5a;
  font-size: 14px;
  flex: 1;
}

@media (max-width: 768px) {
  .app-layout {
    flex-direction: column;
  }
  .main-content {
    padding: 10px 12px 14px;
  }
}

@media (max-width: 640px) {
  .form-row {
    flex-direction: column;
    gap: 0;
  }
  .view-top {
    flex-wrap: wrap;
  }
}
</style>