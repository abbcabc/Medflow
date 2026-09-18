<template>
  <div class="chat-page">
    <PatientSidebar>
      <button class="side-nav-btn primary-action" @click="newChat">
        <i class="fa-solid fa-plus"></i>新会话
      </button>
      <button class="side-nav-btn" @click="goProfile">
        <i class="fa-solid fa-user-pen"></i>完善个人信息
      </button>
      <button class="side-nav-btn" @click="goAppoinment">
        <i class="fa-solid fa-calendar-check"></i>查看预约信息
      </button>
      <button class="side-nav-btn" @click="goChangePassword">
        <i class="fa-solid fa-key"></i>修改密码
      </button>
      <template #footer>
        <button class="side-nav-btn logout" @click="handleLogout">
          <i class="fa-solid fa-arrow-right-from-bracket"></i>退出登录
        </button>
      </template>
    </PatientSidebar>

    <main class="chat-main">
      <header class="chat-header">
        <div class="chat-title">
          <span class="title-badge"><i class="fa-solid fa-robot"></i></span>
          <span>小智 AI 伴诊助手</span>
        </div>
        <span class="chat-sub">智能导诊 · 号源查询 · 预约挂号</span>
      </header>

      <div class="message-list" ref="messageListRef">
        <div
          v-for="(message, index) in messages"
          :key="index"
          :class="['msg-row', message.isUser ? 'from-user' : 'from-bot']"
        >
          <span class="msg-avatar" :class="message.isUser ? 'avatar-user' : 'avatar-bot'">
            <i :class="message.isUser ? 'fa-solid fa-user' : 'fa-solid fa-robot'"></i>
          </span>
          <div class="msg-bubble" :class="message.isUser ? 'bubble-user' : 'bubble-bot'">
            <span v-html="sanitizeContent(message.content)"></span>
            <span class="loading-dots" v-if="message.isThinking || message.isTyping">
              <span class="dot"></span>
              <span class="dot"></span>
            </span>
          </div>
        </div>
      </div>

      <div class="input-bar">
        <el-input
          v-model="inputMessage"
          placeholder="请描述您的症状或想咨询的问题，例如：最近胃不舒服，应该挂哪个科？"
          @keyup.enter="sendMessage"
          size="large"
        ></el-input>
        <el-button
          @click="isSending ? stopGeneration() : sendMessage()"
          type="primary"
          size="large"
          class="send-btn"
        >
          <i :class="isSending ? 'fa-solid fa-stop' : 'fa-solid fa-paper-plane'"></i>&nbsp;{{ isSending ? '停止' : '发送' }}
        </el-button>
      </div>
    </main>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import DOMPurify from 'dompurify'
import { handle401 } from '@/utils/auth401'
import PatientSidebar from './PatientSidebar.vue'
import { v4 as uuidv4 } from 'uuid'

// XSS 防护：v-html 渲染前净化（默认配置即可，禁止 script/事件属性）
const sanitizeContent = (content) => DOMPurify.sanitize(content || '')
const router = useRouter()

const messageListRef = ref()
const isSending = ref(false)
const abortController = ref(null)
const inputMessage = ref('')
const messages = ref([])

// 获取用户ID（这里假设用户ID存储在localStorage中）
const getUserId = () => {
  let userId = localStorage.getItem('patientId')
  if (!userId) {
    userId = uuidv4()
    localStorage.setItem('patientId', userId)
  }
  return userId
}

onMounted(() => {
  watch(messages, () => scrollToBottom(), { deep: true })
  loadChatHistory()
})

const scrollToBottom = () => {
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

// 加载聊天历史记录
const loadChatHistory = async () => {
  const userId = getUserId()
  try {
    const response = await axios.get(`/api/agent/chat/history`, {
      params: { userId },
      headers: {
        Authorization: `Bearer ${localStorage.getItem('patientToken') || localStorage.getItem('token') || ''}`
      }
    })
    
    // console.log('后端返回的完整数据:', response.data)
    
    if (response.data && response.data.length > 0) {
      // 过滤并转换消息
      messages.value = response.data
        .filter(msg => {
          // 只保留用户和AI消息
          if (msg.type !== 'USER' && msg.type !== 'AI') return false
          
          // 过滤掉系统自动消息
          if (msg.type === 'USER') {
            const content = msg.displayText || msg.text || ''
            if (content.includes('当前用户信息：用户ID=') && 
                content.trim() === '当前用户信息：用户ID=7,不要在对话中展示用户ID') {
              return false
            }
          }
          return true
        })
        .map(msg => {
          let content = msg.displayText || msg.text || ''
          
          // 清理用户消息中的系统提示信息
          if (msg.type === 'USER') {
            // 删除各种系统提示信息
            const patterns = [
              '\n\nAnswer using',
              'Answer using',
              '当前用户信息：用户ID=',
              '不要在对话中展示用户ID',
            ]
            
            patterns.forEach(pattern => {
              const index = content.indexOf(pattern)
              if (index !== -1) {
                content = content.substring(0, index)
              }
            })
          }
          
          return {
            isUser: msg.type === 'USER',
            content: content.trim(),
            isTyping: false,
            isThinking: false
          }
        })
        .filter(msg => msg.content.length > 0) // 确保内容不为空
      
      console.log('转换后的消息列表:', messages.value)
      
      if (messages.value.length === 0) {
        hello()
      }
    } else {
      hello()
    }
  } catch (error) {
    console.error('加载聊天历史失败:', error)
    hello()
  }
}

const hello = () => {
  const sessionId = getUserId()
  sendRequest('你好,当前会话id是'+sessionId)
}

const sendMessage = () => {
  if (isSending.value) return
  if (inputMessage.value.trim()) {
    sendRequest(inputMessage.value.trim())
    inputMessage.value = ''
  }
}

// 停止生成：中止当前流式请求，已生成的部分内容保留
const stopGeneration = () => {
  if (abortController.value) {
    abortController.value.abort()
  }
}

const sendRequest = (message) => {
  isSending.value = true
  const userId = getUserId()


  // 添加用户消息
  const userMsg = {
    isUser: true,
    content: message,
    isTyping: false,
    isThinking: false,
  }

  // 第一条默认发送的用户消息"你好"不放入会话列表
  if(messages.value.length > 0){
    messages.value.push(userMsg)
  }

  // 添加机器人加载消息
  const botMsg = {
    isUser: false,
    content: '', // 增量填充
    isTyping: true, // 显示加载动画
    isThinking: false,
  }
  messages.value.push(botMsg)
  const lastMsg = messages.value[messages.value.length - 1]
  scrollToBottom()

  const controller = new AbortController()
  abortController.value = controller

  const headers = { 'Content-Type': 'application/json' }
  const token = localStorage.getItem('patientToken') || localStorage.getItem('token')
  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }

  fetch('/api/agent/chat', {
    method: 'POST',
    headers,
    body: JSON.stringify({ memoryId: userId, message }),
    signal: controller.signal,
  })
    .then(async (response) => {
      if (response.status === 401) {
        // 未登录/令牌失效：清理并跳转登录页
        lastMsg.isThinking = false
        lastMsg.isTyping = false
        lastMsg.content = ''
        handle401('patient')
        return
      }
      if (!response.ok || !response.body) {
        throw new Error(`HTTP ${response.status}`)
      }
      // 真流式：逐块读取并增量渲染
      const reader = response.body.getReader()
      const decoder = new TextDecoder('utf-8')
      while (true) {
        const { value, done } = await reader.read()
        if (done) break
        const chunk = decoder.decode(value, { stream: true })
        if (chunk) {
          lastMsg.content += chunk // 增量更新
          scrollToBottom() // 实时滚动
        }
      }
      const tail = decoder.decode()
      if (tail) {
        lastMsg.content += tail
        scrollToBottom()
      }
    })
    .catch((error) => {
      if (error.name === 'AbortError') {
        // 用户主动停止：保留已生成内容，不提示错误
      } else {
        console.error('流式错误:', error)
        messages.value.at(-1).content = '请求失败，请重试'
      }
    })
    .finally(() => {
      // 流结束后隐藏加载动画
      messages.value.at(-1).isTyping = false
      isSending.value = false
      abortController.value = null
    })
}

const newChat = () => {
  // 清除本地存储的用户ID和消息
  // localStorage.removeItem('patientId')
  messages.value = []
  // 重新生成用户ID并发送欢迎消息
  hello()
}

// 退出登录：清除本地登录态与会话标识，回到患者登录页
const handleLogout = () => {
  localStorage.removeItem('patientToken')
  localStorage.removeItem('patientId')
  localStorage.removeItem('token')
  messages.value = []
  inputMessage.value = ''
  router.push('/plogin')
}
</script>

<script>
export default {
  methods: {
    goProfile() {
      this.$router.push('/profile');
    },
    goAppoinment(){
      this.$router.push('/appointmentDetail');
    },
    goChangePassword(){
      this.$router.push('/changePassword');
    }
  }
}
</script>

<style scoped>
.chat-page {
  display: flex;
  height: 100vh;
  background: linear-gradient(160deg, #eef4fb 0%, #f6fafd 60%, #eaf6f4 100%);
  overflow: hidden;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  padding: 18px 24px 22px;
  box-sizing: border-box;
}

/* 顶部标题栏 */
.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  margin-bottom: 16px;
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 2px 10px rgba(23, 62, 129, 0.06);
}
.chat-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 700;
  color: #0f2e5c;
}
.title-badge {
  width: 32px;
  height: 32px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  background: linear-gradient(135deg, #1d6ff2 0%, #12b3a8 100%);
  color: #fff;
  font-size: 14px;
}
.chat-sub {
  font-size: 12px;
  color: #8aa0bd;
  letter-spacing: 1px;
}

/* 消息列表 */
.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px 8px 12px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  scrollbar-width: thin;
  scrollbar-color: #c5d4e8 transparent;
}

.msg-row {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  max-width: 82%;
}
.msg-row.from-user {
  align-self: flex-end;
  flex-direction: row-reverse;
}
.msg-row.from-bot {
  align-self: flex-start;
}

.msg-avatar {
  width: 34px;
  height: 34px;
  flex: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  font-size: 14px;
  color: #fff;
  box-shadow: 0 2px 6px rgba(23, 62, 129, 0.18);
}
.avatar-bot {
  background: linear-gradient(135deg, #12b3a8 0%, #1d6ff2 100%);
}
.avatar-user {
  background: linear-gradient(135deg, #f2994a 0%, #f2789f 100%);
}

.msg-bubble {
  padding: 12px 16px;
  font-size: 14px;
  line-height: 1.75;
  word-break: break-word;
  border-radius: 14px;
}
.bubble-bot {
  background: #fff;
  color: #2c4464;
  border: 1px solid #e3ecf7;
  border-top-left-radius: 4px;
  box-shadow: 0 2px 8px rgba(23, 62, 129, 0.05);
}
.bubble-user {
  background: linear-gradient(135deg, #1d6ff2 0%, #3f8cff 100%);
  color: #fff;
  border-top-right-radius: 4px;
  box-shadow: 0 4px 12px rgba(29, 111, 242, 0.25);
}

/* 加载动画 */
.loading-dots {
  display: inline-flex;
  align-items: center;
  padding-left: 6px;
}
.dot {
  display: inline-block;
  margin-left: 5px;
  width: 7px;
  height: 7px;
  background-color: #7fa8d9;
  border-radius: 50%;
  animation: pulse 1.2s infinite ease-in-out both;
}
.dot:nth-child(2) {
  animation-delay: -0.6s;
}
@keyframes pulse {
  0%, 100% { transform: scale(0.6); opacity: 0.4; }
  50% { transform: scale(1); opacity: 1; }
}

/* 底部输入栏 */
.input-bar {
  display: flex;
  gap: 12px;
  margin-top: 14px;
  padding: 12px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 6px 20px rgba(23, 62, 129, 0.08);
  align-items: center;
}
.input-bar :deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 0 0 1.5px #dbe4f0 inset;
}
.input-bar :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1.5px #1d6ff2 inset;
}
.send-btn {
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, #1d6ff2 0%, #12b3a8 100%);
  font-weight: 600;
}
.send-btn:hover:not(:disabled) {
  filter: brightness(1.06);
}

@media (max-width: 768px) {
  .chat-page { flex-direction: column; }
  .chat-main { padding: 10px 12px 14px; }
  .chat-sub { display: none; }
  .msg-row { max-width: 92%; }
}
</style>