import { createRouter, createWebHashHistory } from 'vue-router'
import jsCookie from 'js-cookie'

// 静态导入：高频核心页面
import PatientLogin from '../components/PatientLogin.vue'
import PatientRegister from '../components/PatientRegister.vue'
import PatientProfile from '../components/PatientProfile.vue'
import ChangePassword from '../components/ChangePassword.vue'
import DoctorLogin from '../components/DoctorLogin.vue'

// 懒加载：低频/大体积页面
const ChatWindow = () => import('../components/ChatWindow.vue')
const AppointmentDetail = () => import('../components/AppointmentDetail.vue')
const DoctorDashboard = () => import('../components/DoctorDashboard.vue')
const AdminLogin = () => import('@/views/login/Login.vue')
const Layout = () => import('@/layout/Layout.vue')
const Dashboard = () => import('@/views/dashboard/Dashboard.vue')

const routes = [
  // 根路径：默认跳转患者登录页（避免 404 兜底重定向回 / 造成无限循环）
  {
    path: '/',
    redirect: '/plogin'
  },
  // 管理员模块
  {
    path: '/adminlogin',
    name: 'adminLogin',
    component: AdminLogin,
    meta: { title: '管理员登录' }
  },
  {
    path: '/layout',
    name: 'Layout',
    component: Layout,
    redirect: '/layout/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: Dashboard,
        meta: { title: '首页仪表盘' }
      },
      // 以下管理页作为 /layout 子路由渲染，共享左侧导航栏（绝对路径，侧边栏链接无需改动）
      {
        path: '/dept/list',
        name: 'DeptList',
        component: () => import('@/views/dept/DeptList.vue'),
        meta: { title: '科室管理' }
      },
      {
        path: '/appointment/list',
        name: 'AppointmentList',
        component: () => import('@/views/appointment/AppointmentList.vue'),
        meta: { title: '预约管理' }
      },
      {
        path: '/doctor/list',
        name: 'DoctorList',
        component: () => import('@/views/doctor/DoctorList.vue'),
        meta: { title: '医生管理' }
      },
      {
        path: '/schedule/list',
        name: 'ScheduleList',
        component: () => import('@/views/schedule/ScheduleList.vue'),
        meta: { title: '排班管理' }
      }
    ]
  },
  // 患者模块
  {
    path: '/plogin',
    name: 'Login',
    component: PatientLogin
  },
  {
    path: '/register',
    name: 'Register',
    component: PatientRegister
  },
  {
    path: '/chat',
    name: 'Chat',
    component: ChatWindow
  },
  {
    path: '/profile',
    name: 'Profile', // 规范命名
    component: PatientProfile
  },
  {
    path: '/appointmentDetail',
    name: 'AppointmentDetail',
    component: AppointmentDetail
  },
  {
    path: '/changePassword',
    name: 'ChangePassword',
    component: ChangePassword
  },
  // 医生模块
  {
    path: '/doctorLogin',
    name: 'DoctorLogin',
    component: DoctorLogin
  },
  {
    path: '/doctor/dashboard',
    name: 'DoctorDashboard',
    component: DoctorDashboard
  },
  // 404页面
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// 启用修正后的路由守卫
// router.beforeEach((to, from, next) => {
//   document.title = to.meta.title || '医疗预约管理系统';
//   const token = jsCookie.get('token');
//   const whiteList = ['/', '/adminlogin', '/doctorLogin'];
  
//   if (!token && !whiteList.includes(to.path)) {
//     if (to.path.startsWith('/layout')) {
//       next('/adminlogin');
//     } else if (to.path.startsWith('/doctor')) {
//       next('/doctorLogin');
//     } else {
//       next('/');
//     }
//   } else {
//     next();
//   }
// });

export default router