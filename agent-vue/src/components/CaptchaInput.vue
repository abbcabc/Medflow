<template>
  <div class="captcha-row">
    <input
      class="captcha-input"
      type="text"
      :value="modelValue"
      @input="$emit('update:modelValue', $event.target.value)"
      placeholder="验证码"
      maxlength="4"
      autocomplete="off"
    />
    <canvas
      ref="canvas"
      width="90"
      height="36"
      class="captcha-canvas"
      title="看不清？点击刷新"
      @click="refresh"
    ></canvas>
  </div>
</template>

<script>
// 字符集去掉易混淆的 0/O、1/I/L
const CHARS = 'ABCDEFGHJKMNPQRSTUVWXYZ23456789'

// 纯前端图形验证码：canvas 绘制 4 位码，点击刷新，父组件通过 $refs 调 verify() 校验
export default {
  name: 'CaptchaInput',
  props: {
    modelValue: { type: String, default: '' }
  },
  emits: ['update:modelValue'],
  data() {
    return {
      code: ''
    }
  },
  mounted() {
    this.refresh()
  },
  methods: {
    refresh() {
      let code = ''
      for (let i = 0; i < 4; i++) {
        code += CHARS[Math.floor(Math.random() * CHARS.length)]
      }
      this.code = code
      this.$emit('update:modelValue', '')
      this.draw()
    },
    draw() {
      const canvas = this.$refs.canvas
      if (!canvas) return
      const ctx = canvas.getContext('2d')
      const w = canvas.width
      const h = canvas.height
      ctx.clearRect(0, 0, w, h)

      // 背景渐变
      const bg = ctx.createLinearGradient(0, 0, w, h)
      bg.addColorStop(0, '#eef4fb')
      bg.addColorStop(1, '#e6f5f2')
      ctx.fillStyle = bg
      ctx.fillRect(0, 0, w, h)

      // 干扰线
      ctx.lineWidth = 1
      for (let i = 0; i < 4; i++) {
        ctx.strokeStyle = `rgba(29,111,242,${0.12 + Math.random() * 0.15})`
        ctx.beginPath()
        ctx.moveTo(Math.random() * w, Math.random() * h)
        ctx.bezierCurveTo(
          Math.random() * w, Math.random() * h,
          Math.random() * w, Math.random() * h,
          Math.random() * w, Math.random() * h
        )
        ctx.stroke()
      }

      // 噪点
      for (let i = 0; i < 26; i++) {
        ctx.fillStyle = `rgba(20,80,120,${0.12 + Math.random() * 0.2})`
        ctx.fillRect(Math.random() * w, Math.random() * h, 2, 2)
      }

      // 字符
      const colors = ['#1d6ff2', '#12b3a8', '#4a5d7a', '#e0731d']
      for (let i = 0; i < this.code.length; i++) {
        ctx.save()
        ctx.font = `bold ${17 + Math.floor(Math.random() * 5)}px Georgia, serif`
        ctx.fillStyle = colors[Math.floor(Math.random() * colors.length)]
        ctx.translate(11 + i * 19, h / 2 + 6 + (Math.random() * 6 - 3))
        ctx.rotate(Math.random() * 0.5 - 0.25)
        ctx.fillText(this.code[i], 0, 0)
        ctx.restore()
      }
    },
    // 供父组件校验：与图形一致返回 true；校验后无论成败都刷新并清空输入，防重放
    verify() {
      const ok = (this.modelValue || '').trim().toUpperCase() === this.code
      this.refresh()
      return ok
    }
  }
}
</script>
