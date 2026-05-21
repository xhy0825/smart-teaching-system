<template>
  <div class="login-bg">
    <canvas ref="canvasRef" class="particles-canvas"></canvas>
    <div class="grid-overlay"></div>
    <div class="glow-overlay"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

const canvasRef = ref<HTMLCanvasElement>()
let animationId: number
let particles: Particle[] = []
let mouse = { x: -1000, y: -1000 }

interface Particle {
  x: number
  y: number
  vx: number
  vy: number
  size: number
  color: string
  alpha: number
}

const colors = ['#00d4ff', '#0099ff', '#8b5cf6', '#6366f1', '#06b6d4']

function createParticle(width: number, height: number): Particle {
  return {
    x: Math.random() * width,
    y: Math.random() * height,
    vx: (Math.random() - 0.5) * 0.8,
    vy: (Math.random() - 0.5) * 0.8,
    size: Math.random() * 2.5 + 1,
    color: colors[Math.floor(Math.random() * colors.length)],
    alpha: Math.random() * 0.5 + 0.3
  }
}

function initParticles(width: number, height: number, count: number = 90) {
  particles = []
  for (let i = 0; i < count; i++) {
    particles.push(createParticle(width, height))
  }
}

function drawParticles(ctx: CanvasRenderingContext2D, width: number, height: number) {
  ctx.clearRect(0, 0, width, height)

  // 绘制连线
  const maxDist = 150
  for (let i = 0; i < particles.length; i++) {
    for (let j = i + 1; j < particles.length; j++) {
      const dx = particles[i].x - particles[j].x
      const dy = particles[i].y - particles[j].y
      const dist = Math.sqrt(dx * dx + dy * dy)
      if (dist < maxDist) {
        const opacity = (1 - dist / maxDist) * 0.3
        ctx.beginPath()
        ctx.strokeStyle = `rgba(0, 212, 255, ${opacity})`
        ctx.lineWidth = 0.5
        ctx.moveTo(particles[i].x, particles[i].y)
        ctx.lineTo(particles[j].x, particles[j].y)
        ctx.stroke()
      }
    }
  }

  // 绘制粒子
  particles.forEach(p => {
    ctx.beginPath()
    ctx.arc(p.x, p.y, p.size, 0, Math.PI * 2)
    ctx.fillStyle = p.color
    ctx.globalAlpha = p.alpha
    ctx.fill()
    ctx.globalAlpha = 1
  })
}

function updateParticles(width: number, height: number) {
  const mouseRadius = 200
  particles.forEach(p => {
    p.x += p.vx
    p.y += p.vy

    // 边界反弹
    if (p.x < 0 || p.x > width) p.vx *= -1
    if (p.y < 0 || p.y > height) p.vy *= -1

    // 鼠标交互 - 轻微吸引
    const dx = mouse.x - p.x
    const dy = mouse.y - p.y
    const dist = Math.sqrt(dx * dx + dy * dy)
    if (dist < mouseRadius && dist > 0) {
      const force = (mouseRadius - dist) / mouseRadius * 0.02
      p.vx += dx / dist * force
      p.vy += dy / dist * force
    }

    // 速度衰减
    p.vx *= 0.999
    p.vy *= 0.999
  })
}

function animate() {
  const canvas = canvasRef.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  if (!ctx) return

  updateParticles(canvas.width, canvas.height)
  drawParticles(ctx, canvas.width, canvas.height)
  animationId = requestAnimationFrame(animate)
}

function handleResize() {
  const canvas = canvasRef.value
  if (!canvas) return
  canvas.width = window.innerWidth
  canvas.height = window.innerHeight
  // 重新初始化粒子位置
  initParticles(canvas.width, canvas.height)
}

function handleMouseMove(e: MouseEvent) {
  mouse.x = e.clientX
  mouse.y = e.clientY
}

function handleMouseLeave() {
  mouse.x = -1000
  mouse.y = -1000
}

onMounted(() => {
  const canvas = canvasRef.value
  if (!canvas) return

  canvas.width = window.innerWidth
  canvas.height = window.innerHeight
  initParticles(canvas.width, canvas.height, 90)
  animate()

  window.addEventListener('resize', handleResize)
  canvas.addEventListener('mousemove', handleMouseMove)
  canvas.addEventListener('mouseleave', handleMouseLeave)
})

onUnmounted(() => {
  cancelAnimationFrame(animationId)
  window.removeEventListener('resize', handleResize)
  const canvas = canvasRef.value
  if (canvas) {
    canvas.removeEventListener('mousemove', handleMouseMove)
    canvas.removeEventListener('mouseleave', handleMouseLeave)
  }
})
</script>

<style scoped>
.login-bg {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: #0a0e27;
  overflow: hidden;
  z-index: 0;
}

.particles-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.grid-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 200%;
  height: 200%;
  background-image:
    linear-gradient(rgba(0, 212, 255, 0.08) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 212, 255, 0.08) 1px, transparent 1px);
  background-size: 60px 60px;
  animation: gridMove 20s linear infinite;
  transform: translate(-25%, -25%);
}

.glow-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background:
    radial-gradient(circle at 20% 30%, rgba(0, 153, 255, 0.15), transparent 50%),
    radial-gradient(circle at 80% 70%, rgba(139, 92, 246, 0.15), transparent 50%),
    radial-gradient(circle at 50% 50%, rgba(0, 212, 255, 0.05), transparent 70%);
  animation: glowPulse 8s ease-in-out infinite alternate;
}

@keyframes gridMove {
  0% {
    transform: translate(-25%, -25%);
  }
  100% {
    transform: translate(-30%, -30%);
  }
}

@keyframes glowPulse {
  0% {
    opacity: 0.8;
  }
  100% {
    opacity: 1;
  }
}
</style>
