<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { Search, Close } from '@element-plus/icons-vue'

const props = withDefaults(
  defineProps<{
    modelValue?: string
    placeholder?: string
  }>(),
  {
    modelValue: '',
    placeholder: '搜索文章...',
  },
)

const emit = defineEmits<{
  'update:modelValue': [value: string]
  search: [value: string]
}>()

const inputRef = ref<HTMLInputElement | null>(null)
const isFocused = ref(false)

const isMobile = ref(false)

const checkMobile = () => {
  isMobile.value = window.innerWidth < 768
}

const handleInput = (event: Event) => {
  const target = event.target as HTMLInputElement
  emit('update:modelValue', target.value)
}

const handleClear = () => {
  emit('update:modelValue', '')
  inputRef.value?.focus()
}

const handleKeydown = (event: KeyboardEvent) => {
  if (event.key === 'Enter') {
    emit('search', props.modelValue)
  }
}

const handleShortcut = (event: KeyboardEvent) => {
  if ((event.ctrlKey || event.metaKey) && event.key === 'k') {
    event.preventDefault()
    inputRef.value?.focus()
  }
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
  document.addEventListener('keydown', handleShortcut)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
  document.removeEventListener('keydown', handleShortcut)
})

const showClearButton = computed(() => props.modelValue.length > 0)
</script>

<template>
  <div class="search-input-wrapper" :class="{ 'is-mobile': isMobile }">
    <div v-if="!isMobile" class="search-container" :class="{ 'is-focused': isFocused }">
      <el-icon class="search-icon">
        <Search />
      </el-icon>
      <input
        ref="inputRef"
        type="text"
        class="search-input"
        :value="modelValue"
        :placeholder="placeholder"
        @input="handleInput"
        @focus="isFocused = true"
        @blur="isFocused = false"
        @keydown="handleKeydown"
      />
      <button
        v-if="showClearButton"
        class="clear-button"
        @click="handleClear"
        type="button"
        aria-label="清除搜索"
      >
        <el-icon><Close /></el-icon>
      </button>
    </div>
    <button
      v-else
      class="mobile-search-btn"
      @click="inputRef?.focus()"
      type="button"
      aria-label="搜索"
    >
      <el-icon class="search-icon">
        <Search />
      </el-icon>
    </button>
  </div>
</template>

<style scoped>
.search-input-wrapper {
  display: inline-block;
}

.search-container {
  position: relative;
  display: flex;
  align-items: center;
  width: 220px;
  height: 40px;
  background: var(--bg-surface);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-pill);
  transition: all 0.2s ease;
  overflow: hidden;
}

.search-container:hover {
  border-color: var(--border-medium);
}

.search-container.is-focused {
  border: 2px solid var(--color-primary);
  box-shadow: 0 0 0 3px rgba(139, 92, 246, 0.1);
  width: 260px;
}

.search-icon {
  position: absolute;
  left: 14px;
  color: var(--text-muted);
  font-size: 16px;
  pointer-events: none;
  transition: color 0.2s ease;
}

.search-container.is-focused .search-icon {
  color: var(--color-primary);
}

.search-input {
  width: 100%;
  height: 100%;
  padding: 0 40px 0 40px;
  border: none;
  outline: none;
  background: transparent;
  font-size: var(--text-sm);
  color: var(--text-primary);
}

.search-input::placeholder {
  color: var(--text-muted);
}

.clear-button {
  position: absolute;
  right: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border: none;
  background: var(--bg-elevated);
  border-radius: 50%;
  cursor: pointer;
  color: var(--text-secondary);
  transition: all 0.2s ease;
}

.clear-button:hover {
  background: var(--color-primary-100);
  color: var(--color-primary);
}

/* Mobile styles */
.mobile-search-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: var(--bg-surface);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-pill);
  cursor: pointer;
  transition: all 0.2s ease;
}

.mobile-search-btn:hover {
  border-color: var(--color-primary);
  background: var(--color-primary-50);
}

.mobile-search-btn .search-icon {
  position: static;
  color: var(--text-muted);
  font-size: 18px;
}

.mobile-search-btn:hover .search-icon {
  color: var(--color-primary);
}
</style>
