<template>
  <div class="chat-page">
    <PatientSidebar>
      <button class="side-nav-btn primary-action" @click="startNewSession">
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
        <!-- 多会话切换栏 -->
        <div class="session-bar">
          <el-select
            v-model="currentSessionId"
            class="session-select"
            placeholder="选择会话"
            :disabled="isSending"
            @change="onSessionChange"
          >
            <el-option
              v-for="s in sessionList"
              :key="s.id"
              :value="s.id"
              :label="s.title || '新对话'"
            >
              <div class="session-option">
                <span class="session-option-title">{{ s.title || '新对话' }}</span>
                <i
                  class="fa-solid fa-trash-can session-del"
                  title="删除会话"
                  @click.stop="removeSession(s)"
                ></i>
              </div>
            </el-option>
          </el-select>
          <el-button class="new-session-btn" :disabled="isSending" @click="startNewSession">
            <i class="fa-solid fa-plus"></i>&nbsp;新会话
          </el-button>
        </div>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import PatientSidebar from './PatientSidebar.vue'

// XSS 防护：v-html 渲染前净化（默认配置即可，禁止 script/事件属性）
const sanitizeContent = (content) => DOMPurify.sanitize(content || '')
const router = useRouter()

const messageListRef = ref()
const isSending = ref(false)
const abortController = ref(null)
const inputMessage = ref('')
const messages = ref([])

// 多会话状态：sessionId 即服务端聊天记忆的 memoryId，会话归属由服务端按登录态校验
const sessionList = ref([])
const currentSessionId = ref(null)
const LAST_SESSION_KEY = 'chat_last_session_id'

const authHeaders = () => {
  const token = localStorage.getItem('patientToken') || localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
}

onMounted(() => {
  watch(messages, () => scrollToBottom(), { deep: true })
  initSessions()
})

const scrollToBottom = () => {
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

// ==================== 会话管理 ====================

// 统一处理会话接口响应：业务码 401 走患者端登录跳转，其余返回响应体
const unwrap = (response) => {
  if (response.data && response.data.code === 401) {
    handle401('patient')
    return null
  }
  return response.data
}

// 进入页面：拉取会话列表并恢复上次会话（localStorage）；无会话则自动创建
const initSessions = async () => {
  try {
    const response = await axios.get('/api/agent/session/list', { headers: authHeaders() })
    const body = unwrap(response)
    if (!body) return
    sessionList.value = body.data || []
    if (sessionList.value.length > 0) {
      const saved = Number(localStorage.getItem(LAST_SESSION_KEY))
      const target = sessionList.value.find((s) => s.id === saved) || sessionList.value[0]
      currentSessionId.value = target.id
      localStorage.setItem(LAST_SESSION_KEY, String(target.id))
      await loadChatHistory(target.id)
    } else {
      await startNewSession()
    }
  } catch (error) {
    console.error('加载会话列表失败:', error)
    showWelcome()
  }
}

// 新建会话：清空消息区并发起欢迎语；当前会话还没产生对话时不重复创建
const startNewSession = async () => {
  if (isSending.value) stopGeneration()
  const hasContent = messages.value.some((m) => m.isUser)
  if (currentSessionId.value && !hasContent) return
  try {
    const response = await axios.post('/api/agent/session', null, { headers: authHeaders() })
    const body = unwrap(response)
    if (!body) return
    sessionList.value.unshift(body.data)
    currentSessionId.value = body.data.id
    localStorage.setItem(LAST_SESSION_KEY, String(body.data.id))
    messages.value = []
    showWelcome()
  } catch (error) {
    console.error('创建会话失败:', error)
  }
}

// 删除会话：连同服务端聊天记忆一起删除；删除当前会话时自动切换
const removeSession = async (session) => {
  try {
    await ElMessageBox.confirm('删除后该会话的聊天记录将无法恢复，确定删除？', '删除会话', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
  } catch {
    return
  }
  try {
    const response = await axios.delete(`/api/agent/session/${session.id}`, { headers: authHeaders() })
    if (!unwrap(response)) return
    sessionList.value = sessionList.value.filter((s) => s.id !== session.id)
    if (currentSessionId.value === session.id) {
      if (sessionList.value.length > 0) {
        currentSessionId.value = sessionList.value[0].id
        localStorage.setItem(LAST_SESSION_KEY, String(currentSessionId.value))
        await loadChatHistory(currentSessionId.value)
      } else {
        currentSessionId.value = null
        await startNewSession()
      }
    }
    ElMessage.success('会话已删除')
  } catch (error) {
    console.error('删除会话失败:', error)
  }
}

// 切换会话：中断进行中的生成，加载目标会话历史
const onSessionChange = async (id) => {
  if (isSending.value) stopGeneration()
  currentSessionId.value = id
  localStorage.setItem(LAST_SESSION_KEY, String(id))
  await loadChatHistory(id)
}

// 静默刷新会话列表（首条消息后标题在服务端已生成）
const refreshSessions = async () => {
  try {
    const response = await axios.get('/api/agent/session/list', { headers: authHeaders() })
    const body = unwrap(response)
    if (body) sessionList.value = body.data || []
  } catch {
    /* 静默失败，不影响聊天主流程 */
  }
}

// ==================== 消息与流式对话 ====================

// 加载指定会话的聊天历史
const loadChatHistory = async (sessionId) => {
  try {
    const response = await axios.get('/api/agent/chat/history', {
      params: { sessionId },
      headers: authHeaders(),
    })
    const list = unwrap(response) || []
    messages.value = list
      .filter((msg) => {
        // 只保留用户和AI消息
        if (msg.type !== 'USER' && msg.type !== 'AI') return false
        // 过滤掉历史版本遗留的系统自动消息
        if (msg.type === 'USER') {
          const content = msg.displayText || msg.text || ''
          if (
            content.includes('当前用户信息：用户ID=') &&
            content.trim() === '当前用户信息：用户ID=7,不要在对话中展示用户ID'
          ) {
            return false
          }
        }
        return true
      })
      .map((msg) => {
        let content = msg.displayText || msg.text || ''
        // 清理用户消息中的系统提示信息
        if (msg.type === 'USER') {
          const patterns = ['\n\nAnswer using', 'Answer using', '当前用户信息：用户ID=', '不要在对话中展示用户ID']
          patterns.forEach((pattern) => {
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
          isThinking: false,
        }
      })
      .filter((msg) => msg.content.length > 0)

    if (messages.value.length === 0) {
      showWelcome()
    }
  } catch (error) {
    console.error('加载聊天历史失败:', error)
    showWelcome()
  }
}

// 空会话的本地欢迎语：不调用模型、不写入记忆，避免把"你好"抢先占住会话标题，
// 让第一条真实提问成为标题来源
const showWelcome = () => {
  messages.value.push({
    isUser: false,
    content: '您好，我是您的AI伴诊助手小智，可以帮您智能导诊、查询号源、预约挂号、查询或取消预约。请问有什么可以帮您？',
    isTyping: false,
    isThinking: false,
  })
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
  if (isSending.value) return
  if (!currentSessionId.value) {
    ElMessage.warning('会话尚未就绪，请稍候重试')
    return
  }
  isSending.value = true
  const firstInSession = !messages.value.some((m) => m.isUser)

  // 添加用户消息
  const userMsg = {
    isUser: true,
    content: message,
    isTyping: false,
    isThinking: false,
  }
  messages.value.push(userMsg)

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

  const headers = { 'Content-Type': 'application/json', ...authHeaders() }

  fetch('/api/agent/chat', {
    method: 'POST',
    headers,
    body: JSON.stringify({ sessionId: currentSessionId.value, message }),
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
      // 真流式：SSE 协议解析。服务端把每个模型 token 包装为 "data: <token>" 事件，
      // 事件间以空行分隔；token 内部的换行会被拆成多条 data: 行，需按行累积还原
      const reader = response.body.getReader()
      const decoder = new TextDecoder('utf-8')
      let sseBuf = ''
      while (true) {
        const { value, done } = await reader.read()
        if (done) break
        sseBuf += decoder.decode(value, { stream: true }).replace(/\r\n/g, '\n')
        let sep
        while ((sep = sseBuf.indexOf('\n\n')) !== -1) {
          const event = sseBuf.slice(0, sep)
          sseBuf = sseBuf.slice(sep + 2)
          const text = event
            .split('\n')
            .filter((line) => line.startsWith('data:'))
            .map((line) => line.slice(5).replace(/^ /, ''))
            .join('\n')
          if (text) {
            lastMsg.content += text
            scrollToBottom() // 实时滚动
          }
        }
      }
    })
    .catch((error) => {
      if (error.name === 'AbortError') {
        // 用户主动停止：保留已生成内容，不提示错误
      } else {
        console.error('流式错误:', error)
        // 切换会话/新建会话可能已清空消息数组，需判空防止此处异常阻断 finally
        const last = messages.value.at(-1)
        if (last) last.content = '请求失败，请重试'
      }
    })
    .finally(() => {
      // 注意：以下语句必须在任何可能抛异常的取值之前完成，
      // 否则 isSending 复位失败会导致"停止"按钮永远变不回"发送"
      isSending.value = false
      abortController.value = null
      const last = messages.value.at(-1)
      if (last) {
        last.isTyping = false
        last.isThinking = false
      }
      // 本会话首条消息后服务端开始生成标题，先立即刷新一次，再延迟刷新拿到 LLM 生成的标题
      if (firstInSession) {
        refreshSessions()
        setTimeout(() => refreshSessions(), 5000)
      }
    })
}

// 退出登录：清除本地登录态与会话标识，回到患者登录页
const handleLogout = () => {
  localStorage.removeItem('patientToken')
  localStorage.removeItem('patientId')
  localStorage.removeItem('token')
  localStorage.removeItem(LAST_SESSION_KEY)
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

/* 会话切换栏 */
.session-bar {
  display: flex;
  align-items: center;
  gap: 10px;
}
.session-select {
  width: 240px;
}
.session-select :deep(.el-select__wrapper) {
  border-radius: 10px;
}
.session-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}
.session-option-title {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.session-del {
  color: #c0ccda;
  font-size: 12px;
  padding: 2px 4px;
}
.session-del:hover {
  color: #f2789f;
}
.new-session-btn {
  border-radius: 10px;
  font-weight: 600;
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
  .session-select { width: 150px; }
  .msg-row { max-width: 92%; }
}
</style>
