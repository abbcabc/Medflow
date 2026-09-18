<template>
  <div class="login-shell">
    <div class="shell-card">
      <!-- 左侧品牌区 -->
      <div class="brand-panel">
        <div class="brand-logo">
          <span class="logo-icon"><i class="fa-solid fa-heart-pulse"></i></span>
          <span class="logo-text">MedFlow</span>
        </div>
        <h1 class="brand-title">智慧医疗<br />预约管理平台</h1>
        <p class="brand-slogan">AI 分导诊 · 智能挂号 · 全程伴诊</p>
        <ul class="brand-features">
          <li>
            <span class="feature-icon"><i class="fa-solid fa-robot"></i></span>
            <span>小智 AI 助手，全天候智能导诊</span>
          </li>
          <li>
            <span class="feature-icon"><i class="fa-solid fa-calendar-check"></i></span>
            <span>在线预约挂号，省时免排队</span>
          </li>
          <li>
            <span class="feature-icon"><i class="fa-solid fa-shield-heart"></i></span>
            <span>诊疗信息安全可靠有保障</span>
          </li>
        </ul>
        <span class="brand-deco deco-1"></span>
        <span class="brand-deco deco-2"></span>
        <span class="brand-deco deco-3"></span>
      </div>
      <!-- 右侧表单区 -->
      <div class="form-panel">
        <slot />
      </div>
    </div>
    <p class="shell-footer">MedFlow · 智慧医疗预约管理平台</p>
  </div>
</template>

<script>
export default {
  name: 'LoginShell'
}
</script>

<style scoped>
.login-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32px 16px;
  background:
    radial-gradient(600px 400px at 12% 8%, rgba(29, 111, 242, 0.10), transparent 60%),
    radial-gradient(520px 380px at 88% 90%, rgba(18, 179, 168, 0.12), transparent 60%),
    linear-gradient(135deg, #eef4ff 0%, #f7fbff 50%, #e9f7f5 100%);
  box-sizing: border-box;
}

.shell-card {
  display: flex;
  width: min(920px, 100%);
  min-height: 540px;
  border-radius: 20px;
  overflow: hidden;
  background: #fff;
  box-shadow:
    0 24px 60px rgba(23, 62, 129, 0.14),
    0 4px 14px rgba(23, 62, 129, 0.06);
  animation: card-in 0.5s ease both;
}

@keyframes card-in {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}

/* ---------- 左侧品牌区 ---------- */
.brand-panel {
  position: relative;
  flex: 1.05;
  padding: 48px 44px;
  color: #fff;
  background: linear-gradient(160deg, #1256c4 0%, #1d6ff2 55%, #14a5c4 100%);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 细网格纹理 */
.brand-panel::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(255, 255, 255, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.05) 1px, transparent 1px);
  background-size: 28px 28px;
  pointer-events: none;
}

.brand-deco {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
}
.deco-1 {
  width: 220px; height: 220px;
  right: -70px; top: -70px;
  background: rgba(255, 255, 255, 0.10);
}
.deco-2 {
  width: 140px; height: 140px;
  left: -50px; bottom: -40px;
  border: 2px solid rgba(255, 255, 255, 0.18);
}
.deco-3 {
  width: 70px; height: 70px;
  right: 60px; bottom: 70px;
  background: rgba(255, 255, 255, 0.08);
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 56px;
}
.logo-icon {
  width: 42px; height: 42px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.16);
  backdrop-filter: blur(4px);
  font-size: 20px;
}
.logo-text {
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.brand-title {
  margin: 0 0 14px;
  font-size: 30px;
  font-weight: 700;
  line-height: 1.4;
  letter-spacing: 1px;
}

.brand-slogan {
  margin: 0 0 44px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.78);
  letter-spacing: 2px;
}

.brand-features {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.brand-features li {
  display: flex;
  align-items: center;
  gap: 14px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.92);
}
.feature-icon {
  width: 36px; height: 36px;
  flex: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.14);
  font-size: 15px;
}

/* ---------- 右侧表单区 ---------- */
.form-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 48px 52px;
  background: #fff;
}

.shell-footer {
  margin-top: 22px;
  font-size: 12px;
  color: #93a5bd;
  letter-spacing: 1px;
}

/* ---------- 响应式 ---------- */
@media (max-width: 760px) {
  .brand-panel { display: none; }
  .shell-card { min-height: auto; }
  .form-panel { padding: 40px 28px; }
}
</style>

<style>
/* ===== 登录页共享表单样式（全局，供三个登录页复用） ===== */
.auth-head h2 {
  margin: 0 0 8px;
  font-size: 26px;
  color: #0f2e5c;
  letter-spacing: 1px;
}
.auth-head p {
  margin: 0 0 28px;
  font-size: 14px;
  color: #7d93b2;
}

.auth-form { display: block; }

.auth-field { margin-bottom: 18px; }
.auth-field label {
  display: block;
  margin-bottom: 6px;
  font-size: 13px;
  font-weight: 600;
  color: #4a5d7a;
}
.auth-field input,
.auth-field select {
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
.auth-field input::placeholder { color: #a8b8cd; }
.auth-field input:focus,
.auth-field select:focus {
  outline: none;
  border-color: #1d6ff2;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(29, 111, 242, 0.12);
}
.auth-field input:disabled,
.auth-field select:disabled {
  background: #eef2f8;
  cursor: not-allowed;
  opacity: 0.7;
}

.auth-submit {
  width: 100%;
  margin-top: 8px;
  padding: 12px;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, #1d6ff2 0%, #12b3a8 100%);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 4px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s, opacity 0.2s;
}
.auth-submit:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 8px 20px rgba(29, 111, 242, 0.32);
}
.auth-submit:disabled {
  background: #b7c6da;
  cursor: not-allowed;
}

.auth-message {
  margin-top: 16px;
  padding: 10px 12px;
  border-radius: 8px;
  text-align: center;
  font-size: 13px;
}
.auth-message.success {
  background: #e6f7ef;
  color: #0f7a4d;
  border: 1px solid #b9e8d0;
}
.auth-message.error {
  background: #fdecec;
  color: #c0392b;
  border: 1px solid #f5c6c6;
}

.auth-links {
  margin-top: 24px;
  text-align: center;
  font-size: 14px;
  color: #7d93b2;
}
.auth-links a {
  margin-left: 14px;
  color: #1d6ff2;
  font-weight: 500;
  text-decoration: none;
}
.auth-links a:hover { text-decoration: underline; }
.auth-links a:first-of-type { margin-left: 6px; }

/* 密码可见性切换（各登录/注册页共用，Element Plus 同款线性图标） */
.pwd-wrap {
  position: relative;
}
.pwd-wrap input {
  width: 100%;
  padding-right: 44px;
  box-sizing: border-box;
}
.pwd-eye {
  position: absolute;
  right: 13px;
  top: 50%;
  transform: translateY(-50%);
  width: 26px;
  height: 26px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  color: #a8b8cd;
  cursor: pointer;
  border-radius: 6px;
  padding: 0;
  transition: color 0.15s, background 0.15s;
}
.pwd-eye:hover {
  color: #1d6ff2;
  background: rgba(29, 111, 242, 0.08);
}
.pwd-eye svg {
  width: 17px;
  height: 17px;
}

/* 验证码行：输入框完全对齐 .auth-field input 风格 */
.auth-form .captcha-row {
  margin-bottom: 18px;
}
.captcha-row .captcha-input {
  flex: 1;
  min-width: 0;
  width: 100%;
  padding: 11px 14px;
  border: 1.5px solid #dbe4f0;
  border-radius: 10px;
  font-size: 14px;
  letter-spacing: 4px;
  text-transform: uppercase;
  color: #243b5a;
  background: #f8fafd;
  box-sizing: border-box;
  transition: border-color 0.2s, box-shadow 0.2s, background 0.2s;
}
.captcha-row .captcha-input::placeholder {
  color: #a8b8cd;
}
.captcha-row .captcha-input:focus {
  outline: none;
  border-color: #1d6ff2;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(29, 111, 242, 0.12);
}
.captcha-canvas {
  height: 36px;
  border-radius: 10px;
  cursor: pointer;
  flex-shrink: 0;
  align-self: center;
}
</style>
