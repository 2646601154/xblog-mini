<script setup lang="ts">
import { useConfigStore } from '@/stores/config'

const configStore = useConfigStore()
configStore.initConfig()
</script>

<template>
  <footer class="footer">
    <div class="footer-container">
      <p class="footer-text">{{ configStore.copyright }}</p>
      <p class="footer-icp">{{ configStore.icpNumber }}</p>
    </div>
  </footer>
</template>

<style scoped>
.footer {
  background: var(--bg-dark);
  color: var(--text-secondary);
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
  color: var(--text-secondary);
}

.footer-icp {
  font-size: 0.85rem;
  color: var(--text-secondary);
  opacity: 0.7;
}

@media (max-width: 768px) {
  .footer {
    padding: 30px 16px;
  }
}
</style>
