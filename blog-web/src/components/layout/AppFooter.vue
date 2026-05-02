<script setup lang="ts">
const currentYear = new Date().getFullYear()
</script>

<template>
  <footer class="footer">
    <div class="footer-container">
      <p class="footer-text">© {{ currentYear }} Xblog-mini · 用心记录每一天</p>
      <p class="footer-icp">皖ICP备2026012345号-1</p>
    </div>
  </footer>
</template>

<style scoped>
.footer {
  background: var(--bg-dark);
  color: rgba(255, 255, 255, 0.7);
  padding: 40px 20px;
  margin-top: auto;
}

.footer-container {
  max-width: 1200px;
  margin: 0 auto;
  text-align: center;
}

.footer-text {
  font-size: 0.95rem;
  margin-bottom: 8px;
}

.footer-icp {
  font-size: 0.85rem;
  color: rgba(255, 255, 255, 0.5);
}

@media (max-width: 768px) {
  .footer {
    padding: 30px 16px;
  }
}
</style>
