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
    <div class="flex items-center justify-between mb-4">
      <h2 class="text-2xl font-bold">标签管理</h2>
      <el-button type="primary" @click="openCreateDialog">创建标签</el-button>
    </div>

    <el-card v-loading="loading">
      <el-table :data="tags" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="名称" min-width="150">
          <template #default="{ row }">
            <span class="font-medium">{{ row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="slug" label="URL标识" width="150" />
        <el-table-column prop="articleCount" label="文章数" width="100" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="openEditDialog(row)">编辑</el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="URL标识" prop="slug">
          <el-input v-model="form.slug" placeholder="如: java" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>