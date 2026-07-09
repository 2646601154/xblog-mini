<script setup lang="ts">
import { useConfigStore } from '@/stores/config'

const configStore = useConfigStore()
configStore.initConfig()
</script>

<template>
  <footer class="footer">
    <div class="footer-container">
      <div class="footer-decor"></div>
      <p class="footer-text">{{ configStore.copyright }}</p>
      <p class="footer-icp">{{ configStore.icpNumber }}</p>
    </div>
  </footer>
</template>

<style scoped>
.footer {
  background: var(--bg-surface);
  border-top: 1px solid var(--border-light);
  color: var(--text-secondary);
  padding: var(--space-3xl) var(--space-lg);
  margin-top: auto;
}

.footer-container {
  max-width: 1200px;
  margin: 0 auto;
  text-align: center;
}

.footer-decor {
  width: 60px;
  height: 2px;
  background: var(--color-primary);
  border-radius: var(--radius-sm);
  margin: 0 auto var(--space-lg);
}

.footer-text {
  font-size: var(--text-sm);
  margin-bottom: var(--space-sm);
  color: var(--text-secondary);
}

.footer-icp {
  font-size: var(--text-xs);
  color: var(--text-muted);
  opacity: 0.7;
}

@media (max-width: 768px) {
  .footer {
    padding: var(--space-xl) var(--space-lg);
  }
}
</style>
