<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getTagList, createTag, updateTag, deleteTag } from '@/api'
import type { Tag, TagFormData } from '@/types'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tags = ref<Tag[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const editingId = ref<number | null>(null)
const form = ref<TagFormData>({ name: '', slug: '' })
const rules = {
  name: [{ required: true, message: '请输入标签名称', trigger: 'blur' }],
  slug: [{ required: true, message: '请输入URL标识', trigger: 'blur' }],
}

const tagColors = [
  '#409EFF',
  '#67C23A',
  '#E6A23C',
  '#F56C6C',
  '#909399',
  '#C0A4F1',
  '#36D6C4',
  '#FF9F43',
]

function getTagColor(name: string) {
  let hash = 0
  for (let i = 0; i < name.length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash)
  }
  return tagColors[Math.abs(hash) % tagColors.length]
}

async function fetchTags() {
  loading.value = true
  try {
    const res = await getTagList()
    tags.value = res.data.data
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function openCreateDialog() {
  editingId.value = null
  dialogTitle.value = '创建标签'
  form.value = { name: '', slug: '' }
  dialogVisible.value = true
}

function openEditDialog(tag: Tag) {
  editingId.value = tag.id
  dialogTitle.value = '编辑标签'
  form.value = { name: tag.name, slug: tag.slug }
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    try {
      if (editingId.value) {
        await updateTag(editingId.value, form.value)
        ElMessage.success('更新成功')
      } else {
        await createTag(form.value)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      fetchTags()
    } catch (e) {
      console.error(e)
    }
  })
}

async function handleDelete(tag: Tag) {
  try {
    await ElMessageBox.confirm(`确定删除标签「${tag.name}」吗？`, '警告', { type: 'warning' })
    await deleteTag(tag.id)
    ElMessage.success('删除成功')
    fetchTags()
  } catch (e: any) {
    if (e !== 'cancel') console.error(e)
  }
}

onMounted(fetchTags)
</script>

<template>
  <div>
    <div class="header-section">
      <div class="title-area">
        <h2 class="text-2xl font-bold">标签管理</h2>
      </div>
      <el-button type="primary" @click="openCreateDialog">
        <el-icon class="mr-1"><Plus /></el-icon>
        创建标签
      </el-button>
    </div>

    <el-card v-loading="loading" class="tag-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>标签列表</span>
          <span class="text-gray-400 text-sm">共 {{ tags.length }} 个标签</span>
        </div>
      </template>

      <div class="tag-grid">
        <div v-for="tag in tags" :key="tag.id" class="tag-item">
          <div class="tag-content">
            <div
              class="tag-icon"
              :style="{
                backgroundColor: getTagColor(tag.name) + '20',
                color: getTagColor(tag.name),
              }"
            >
              #{{ tag.name.charAt(0).toUpperCase() }}
            </div>
            <div class="tag-info">
              <span class="tag-name">{{ tag.name }}</span>
              <el-tag size="small" type="info" class="tag-slug">/{{ tag.slug }}/</el-tag>
            </div>
          </div>
          <div class="tag-actions">
            <el-tag size="small" type="success" class="article-tag">
              {{ tag.articleCount || 0 }} 篇文章
            </el-tag>
            <div class="action-buttons">
              <el-button type="primary" size="small" link @click="openEditDialog(tag)"
                >编辑</el-button
              >
              <el-button type="danger" size="small" link @click="handleDelete(tag)">删除</el-button>
            </div>
          </div>
        </div>
      </div>

      <el-empty v-if="tags.length === 0 && !loading" description="暂无标签，创建一个吧" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="450px" class="tag-dialog">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入标签名称" />
        </el-form-item>
        <el-form-item label="URL标识" prop="slug">
          <el-input v-model="form.slug" placeholder="如: java">
            <template #prepend>/tag/</template>
          </el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.header-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.title-area {
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 500;
}

.tag-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.tag-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border: 1px solid #f0f2f5;
  border-radius: 10px;
  transition: all 0.3s ease;
}

.tag-item:hover {
  border-color: #409eff;
}

.tag-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.tag-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 14px;
}

.tag-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.tag-name {
  font-weight: 600;
  color: #303133;
}

.tag-slug {
  font-size: 11px;
}

.tag-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.article-tag {
  background: rgba(103, 194, 58, 0.1);
  border-color: rgba(103, 194, 58, 0.3);
  color: #67c23a;
}

.action-buttons {
  display: flex;
  gap: 4px;
}

.tag-dialog :deep(.el-dialog__header) {
  border-bottom: 1px solid #f0f2f5;
  padding-bottom: 16px;
}
</style>
