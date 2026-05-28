<script setup lang="ts">
defineProps<{
  current: number
  pageSize: number
  total: number
}>()

const emit = defineEmits<{
  'update:current': [page: number]
  change: [page: number]
}>()

const handleCurrentChange = (page: number) => {
  emit('update:current', page)
  emit('change', page)
}
</script>

<template>
  <div class="app-pagination">
    <el-pagination
      :current-page="current"
      :page-size="pageSize"
      :total="total"
      layout="prev, pager, next"
      background
      @current-change="handleCurrentChange"
    />
  </div>
</template>

<style scoped>
.app-pagination {
  display: flex;
  justify-content: center;
  padding: 30px 0;
}
</style>
