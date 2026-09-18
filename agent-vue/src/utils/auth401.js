import { ElMessage } from 'element-plus'
import jsCookie from 'js-cookie'
import router from '@/router'

let redirecting = false

// 统一处理 401：清除本地登录态并跳回对应登录页
// scene: 'patient' -> /plogin；'admin' -> /adminlogin
export function handle401(scene = 'patient') {
  if (redirecting) return
  redirecting = true
  if (scene === 'admin') {
    jsCookie.remove('token')
  } else {
    localStorage.removeItem('patientToken')
    localStorage.removeItem('patientId')
    localStorage.removeItem('token')
  }
  ElMessage.warning('登录状态已失效，请重新登录')
  setTimeout(() => {
    redirecting = false
    router.push(scene === 'admin' ? '/adminlogin' : '/plogin')
  }, 600)
}
