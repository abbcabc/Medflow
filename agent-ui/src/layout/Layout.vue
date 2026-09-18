<template>
  <div class="layout-container">
    <!-- 侧边栏 -->
    <el-aside width="200px" class="layout-aside">
      <div class="aside-logo">MedFlow管理系统</div>
      <el-menu
        default-active="/layout/dashboard"
        class="aside-menu"
        router
        unique-opened
        :default-active="activePath"
      >
        <el-menu-item index="/layout/dashboard">
          <el-icon><House /></el-icon>
          <span>首页仪表盘</span>
        </el-menu-item>
        <el-sub-menu index="2">
          <template #title>
            <el-icon><OfficeBuilding /></el-icon>
            <span>科室管理</span>
          </template>
          <el-menu-item index="/dept/list">科室列表</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="3">
          <template #title>
            <el-icon><UserFilled /></el-icon>
            <span>医生管理</span>
          </template>
          <el-menu-item index="/doctor/list">医生列表</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="4">
          <template #title>
            <el-icon><Calendar /></el-icon>
            <span>预约管理</span>
          </template>
          <el-menu-item index="/appointment/list">预约列表</el-menu-item>
        </el-sub-menu>
        <!-- 新增排班管理菜单 -->
        <el-sub-menu index="5">
          <template #title>
            <el-icon><Clock /></el-icon>
            <span>排班管理</span>
          </template>
          <el-menu-item index="/schedule/list">排班列表</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    <!-- 主内容 -->
    <el-container class="layout-main">
      <!-- 头部 -->
      <el-header class="layout-header">
        <div class="header-right">
          <el-dropdown>
            <span class="user-info">
              <el-icon><User /></el-icon>
              系统管理员
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <!-- 内容区域 -->
      <el-main class="layout-content">
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { useRouter, useRoute } from 'vue-router'
import jsCookie from 'js-cookie'
import { House, User, OfficeBuilding, UserFilled, Calendar,Clock  } from '@element-plus/icons-vue'
import { computed } from 'vue'

const router = useRouter()
const route = useRoute()

// 动态获取当前激活的路由
const activePath = computed(() => route.path)

// 退出登录
const logout = () => {
  jsCookie.remove('token')
  ElMessage.success('退出成功')
  router.push('/login')
}
</script>

<style scoped>
.layout-container {
  width: 100vw;
  height: 100vh;
  display: flex;
}

/* 侧边栏整体样式 - 统一背景色 */
.layout-aside {
  background-color: #2e3b4e;
  color: #fff;
  --el-menu-bg-color: #2e3b4e;          /* 菜单背景色统一 */
  --el-menu-text-color: #e5e9f0;       /* 菜单文字默认颜色 */
  --el-menu-active-text-color: #fff;   /* 激活项文字颜色 */
  --el-menu-hover-text-color: #fff;    /* 悬浮文字颜色 */
  --el-menu-border-color: #2e3b4e;     /* 菜单边框色统一 */
}

.aside-logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  font-size: 18px;
  border-bottom: 1px solid #404e62;
  background-color: #2e3b4e; /* 保证logo区域背景和导航一致 */
}

.aside-menu {
  height: calc(100vh - 60px);
  border-right: none;
  --el-sub-menu-title-color: #e5e9f0; /* 子菜单标题颜色 */
  --el-sub-menu-hover-bg-color: #1f2937; /* 子菜单悬浮背景 */
}

/* 激活项样式 */
:deep(.el-menu-item.is-active) {
  background-color: #165dff !important; /* 选中项背景色 */
  color: #fff !important;
}

/* 菜单项悬浮样式 */
:deep(.el-menu-item:hover) {
  background-color: #1f2937 !important;
}

/* 子菜单展开时的标题样式 */
:deep(.el-sub-menu.is-opened > .el-sub-menu__title) {
  background-color: #1f2937 !important;
}

/* 子菜单选项样式 */
:deep(.el-sub-menu .el-menu-item) {
  padding-left: 40px !important; /* 子菜单缩进 */
  background-color: #2e3b4e !important;
}

.layout-main {
  flex: 1;
}

.layout-header {
  height: 60px;
  line-height: 60px;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  justify-content: flex-end;
  padding: 0 20px;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  cursor: pointer;
  margin-right: 10px;
}

.layout-content {
  padding: 20px;
  background-color: #f5f7fa;
  height: calc(100vh - 60px);
  overflow: auto;
}
</style>