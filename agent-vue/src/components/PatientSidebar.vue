<template>
  <aside class="patient-sidebar">
    <div class="sidebar-logo">
      <span class="logo-badge"><i class="fa-solid fa-heart-pulse"></i></span>
      <div class="logo-meta">
        <span class="logo-name">MedFlow</span>
        <span class="logo-sub">智慧医疗预约平台</span>
      </div>
    </div>
    <nav class="sidebar-nav">
      <slot />
    </nav>
    <div class="sidebar-foot">
      <slot name="footer" />
      <p class="foot-text">小智 AI 伴诊 · 全程守护</p>
    </div>
  </aside>
</template>

<script>
export default {
  name: 'PatientSidebar'
}
</script>

<style>
/* 患者端侧边栏共享样式（全局，供对话页/预约页复用） */
.patient-sidebar {
  width: 232px;
  flex: none;
  display: flex;
  flex-direction: column;
  padding: 26px 18px;
  background: linear-gradient(180deg, #0f3a78 0%, #1450a3 60%, #1667c9 100%);
  box-sizing: border-box;
}

.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 6px 24px;
}
.logo-badge {
  width: 40px;
  height: 40px;
  flex: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.15);
  color: #fff;
  font-size: 18px;
}
.logo-meta {
  display: flex;
  flex-direction: column;
}
.logo-name {
  color: #fff;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0.5px;
}
.logo-sub {
  color: rgba(255, 255, 255, 0.62);
  font-size: 11px;
  margin-top: 2px;
  letter-spacing: 1px;
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
  gap: 10px;
  flex: 1;
}

.side-nav-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 11px 14px;
  border: 1px solid rgba(255, 255, 255, 0.16);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.88);
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s, border-color 0.2s, transform 0.15s;
  box-sizing: border-box;
}
.side-nav-btn i {
  width: 18px;
  text-align: center;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.75);
}
.side-nav-btn:hover {
  background: rgba(255, 255, 255, 0.16);
  border-color: rgba(255, 255, 255, 0.3);
  transform: translateX(2px);
}
.side-nav-btn.primary-action {
  background: linear-gradient(135deg, #1d6ff2 0%, #12b3a8 100%);
  border: none;
  color: #fff;
  font-weight: 600;
  box-shadow: 0 6px 16px rgba(18, 108, 200, 0.35);
}
.side-nav-btn.primary-action i { color: #fff; }
.side-nav-btn.primary-action:hover {
  filter: brightness(1.06);
  transform: translateX(0) translateY(-1px);
}

.sidebar-foot {
  padding-top: 18px;
}

.foot-text {
  margin: 10px 0 0;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.45);
  text-align: center;
  letter-spacing: 1px;
}

/* 退出登录按钮（footer 插槽内使用） */
.side-nav-btn.logout {
  border-color: rgba(255, 120, 120, 0.35);
  background: rgba(255, 90, 90, 0.12);
  color: rgba(255, 170, 170, 0.95);
}
.side-nav-btn.logout i {
  color: rgba(255, 140, 140, 0.85);
}
.side-nav-btn.logout:hover {
  background: rgba(255, 90, 90, 0.22);
  border-color: rgba(255, 120, 120, 0.55);
}

@media (max-width: 768px) {
  .patient-sidebar {
    width: 100%;
    flex-direction: row;
    align-items: center;
    padding: 10px 14px;
    gap: 12px;
  }
  .sidebar-logo { padding: 0; }
  .logo-sub { display: none; }
  .sidebar-nav {
    flex-direction: row;
    flex: 1;
    justify-content: flex-end;
    gap: 8px;
  }
  .side-nav-btn {
    width: auto;
    padding: 8px 12px;
    font-size: 13px;
    white-space: nowrap;
  }
  .sidebar-foot { display: none; }
}
</style>
