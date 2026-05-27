<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getMediaList, deleteMediaImage } from '@/api'
import type { MediaImage } from '@/types'
import { ElMessage, ElMessageBox } from 'element-plus'

const images = ref<MediaImage[]>([])
const loading = ref(false)
const page = ref(1)
const size = ref(20)
const total = ref(0)
const typeFilter = ref<'all' | 'cover' | 'avatar'>('all')
const ossFilter = ref<'all' | 'oss' | 'url'>('all')
const previewUrl = ref<string | null>(null)
const previewVisible = ref(false)

function loadImages() {
  loading.value = true
  getMediaList({ page: page.value, size: size.value })
    .then((res) => {
      images.value = res.data.data.records
      total.value = res.data.data.total
    })
    .catch(() => {
      ElMessage.error('加载图片列表失败')
    })
    .finally(() => {
      loading.value = false
    })
}

function handleDelete(image: MediaImage) {
  ElMessageBox.confirm(
    `确定要删除图片吗？\n来源：${image.sourceName}`,
    '删除确认',
    { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' }
  ).then(() => {
    deleteMediaImage(image.url)
      .then(() => {
        ElMessage.success('删除成功')
        loadImages()
      })
      .catch((err) => {
        ElMessage.error(err?.response?.data?.message || '删除失败')
      })
  }).catch(() => {})
}

function preview(image: MediaImage) {
  previewUrl.value = image.url
  previewVisible.value = true
}

function onPageChange(p: number) {
  page.value = p
  loadImages()
}

const filteredImages = computed(() => {
  return images.value.filter((img) => {
    if (typeFilter.value !== 'all' && img.type !== typeFilter.value) return false
    if (ossFilter.value === 'oss' && !img.oss) return false
    if (ossFilter.value === 'url' && img.oss) return false
    return true
  })
})

onMounted(loadImages)
</script>

<template>
  <div class="p-6">
    <div class="mb-4 flex gap-3 items-center">
      <h2 class="text-xl font-bold">图片管理</h2>
      <el-select v-model="typeFilter" style="width: 120px" size="small">
        <el-option label="全部类型" value="all" />
        <el-option label="封面图" value="cover" />
        <el-option label="头像" value="avatar" />
      </el-select>
      <el-select v-model="ossFilter" style="width: 120px" size="small">
        <el-option label="全部来源" value="all" />
        <el-option label="OSS" value="oss" />
        <el-option label="外部URL" value="url" />
      </el-select>
    </div>

    <div v-loading="loading" class="min-h-[400px]">
      <div v-if="filteredImages.length === 0 && !loading" class="text-center text-gray-400 py-20">
        暂无图片
      </div>
      <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4">
        <el-card
          v-for="img in filteredImages"
          :key="img.url"
          :body-style="{ padding: '0' }"
          shadow="hover"
          class="overflow-hidden cursor-pointer"
        >
          <div class="aspect-square bg-gray-100 flex items-center justify-center overflow-hidden" @click="preview(img)">
            <el-image
              :src="img.url"
              fit="cover"
              class="w-full h-full"
              lazy
            >
              <template #error>
                <div class="text-gray-400 text-sm">加载失败</div>
              </template>
            </el-image>
          </div>
          <div class="p-3">
            <div class="text-xs text-gray-500 truncate" :title="img.sourceName">
              {{ img.sourceName }}
            </div>
            <div class="flex items-center justify-between mt-1">
              <div class="flex gap-1">
                <el-tag :type="img.type === 'cover' ? 'primary' : 'success'" size="small">
                  {{ img.type === 'cover' ? '封面' : '头像' }}
                </el-tag>
                <el-tag :type="img.oss ? 'warning' : 'info'" size="small">
                  {{ img.oss ? 'OSS' : 'URL' }}
                </el-tag>
              </div>
              <el-button
                v-if="img.oss"
                type="danger"
                size="small"
                :icon="'Delete'"
                text
                @click.stop="handleDelete(img)"
              />
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <div class="flex justify-center mt-6">
      <el-pagination
        :current-page="page"
        :page-size="size"
        :total="total"
        layout="prev, pager, next"
        @current-change="onPageChange"
      />
    </div>

    <el-dialog v-model="previewVisible" title="图片预览" width="80%">
      <div class="flex justify-center">
        <img v-if="previewUrl" :src="previewUrl" class="max-w-full max-h-[70vh] object-contain" />
      </div>
    </el-dialog>
  </div>
</template>
