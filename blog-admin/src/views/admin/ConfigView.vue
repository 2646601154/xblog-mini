<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getConfigList, updateConfig } from '@/api'
import type { ConfigItem, ConfigFormData } from '@/types'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const submitting = ref(false)
const configs = ref<ConfigItem[]>([])
const form = ref<ConfigFormData[]>([])

async function fetchConfigs() {
  loading.value = true
  try {
    const res = await getConfigList()
    configs.value = res.data.data
    form.value = configs.value.map((c) => ({
      configKey: c.configKey,
      configValue: c.configValue,
    }))
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  submitting.value = true
  try {
    await updateConfig(form.value)
    ElMessage.success('保存成功')
    fetchConfigs()
  } catch (e) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}

function getDescription(key: string) {
  const descriptions: Record<string, string> = {
    icp_number: '备案号',
    copyright: '版权信息',
  }
  return descriptions[key] || key
}

onMounted(fetchConfigs)
</script>

<template>
  <div>
    <div class="flex items-center justify-between mb-4">
      <h2 class="text-2xl font-bold">系统配置</h2>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">保存配置</el-button>
    </div>

    <el-card v-loading="loading">
      <el-form label-width="140px">
        <el-form-item
          v-for="(item, index) in form"
          :key="item.configKey"
          :label="getDescription(item.configKey)"
        >
          <el-input
            v-model="item.configValue"
            :placeholder="`请输入${getDescription(item.configKey)}`"
          />
          <template #label>
            <span>{{ getDescription(item.configKey) }}</span>
          </template>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>
