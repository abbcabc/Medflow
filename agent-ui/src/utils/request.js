import axios from 'axios'
import { ElMessage } from 'element-plus'
import jsCookie from 'js-cookie'
import { handle401 } from '@/utils/auth401'

// 创建Axios实例
const service = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API,
  timeout: 5000,
  headers: {
    'Content-Type': 'application/json;charset=utf-8'
  }
})
service.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      // 业务码 401：登录态失效，统一清理并跳转登录页
      if (res.code === 401) {
        handle401('admin')
        return Promise.reject(new Error(res.msg || '请重新登录'))
      }
      ElMessage.error(res.msg || '请求失败')
      return Promise.reject(new Error(res.msg || '请求失败'))
    } else {
      return res
    }
  },
  (error) => {
    // HTTP 401：未登录/令牌无效/过期
    if (error.response && error.response.status === 401) {
      handle401('admin')
      return Promise.reject(error)
    }
    ElMessage.error(error.message || '网络异常')
    return Promise.reject(error)
  }
)
// 请求拦截器：携带Token（管理端，token 存于 Cookie）
service.interceptors.request.use(
  (config) => {
    const token = jsCookie.get('token') || localStorage.getItem('token') || localStorage.getItem('patientToken')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// // 响应拦截器：处理异常和令牌过期
// service.interceptors.response.use(
//   (response) => {
//     const res = response.data
//     // 状态码不是200则为异常
//     if (res.code !== 200) {
//       ElMessage.error(res.msg || '请求失败')
//       // 401：令牌无效/过期，跳转到登录页
//       if (res.code === 401) {
//         ElMessageBox.confirm('登录状态已过期，请重新登录', '提示', {
//           confirmButtonText: '重新登录',
//           cancelButtonText: '取消',
//           type: 'warning'
//         }).then(() => {
//           jsCookie.remove('token')
//           window.location.href = '/login'
//         })
//       }
//       return Promise.reject(new Error(res.msg || '请求失败'))
//     } else {
//       return res
//     }
//   },
//   (error) => {
//     ElMessage.error(error.message || '网络异常')
//     return Promise.reject(error)
//   }
// )

export default service