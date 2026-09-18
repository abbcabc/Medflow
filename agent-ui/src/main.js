import { createApp } from 'vue';
import App from './App.vue';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import router from './router'
import pinia from './store'
import request from './utils/request'
import jsCookie from 'js-cookie'
import dayjs from 'dayjs'
import axios from 'axios'
import { handle401 } from '@/utils/auth401'

// 患者端直接使用 axios 的请求统一处理 401（管理端走 utils/request 封装，医生端 fetch 自行处理）
axios.interceptors.response.use(undefined, (error) => {
  if (error.response && error.response.status === 401) {
    handle401('patient')
  }
  return Promise.reject(error)
})

// 引入Element Plus样式
import 'element-plus/dist/index.css'

const app = createApp(App);

app.config.globalProperties.$http = request
app.config.globalProperties.$cookie = jsCookie
app.config.globalProperties.$dayjs = dayjs
app.use(pinia).use(router)
app.use(ElementPlus);
app.mount('#app');

