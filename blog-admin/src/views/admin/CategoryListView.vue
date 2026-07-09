<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getCategoryList, createCategory, updateCategory, deleteCategory } from '@/api'
import type { Category, CategoryFormData } from '@/types'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const categories = ref<Category[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const editingId = ref<number | null>(null)
const form = ref<CategoryFormData>({
  name: '',
  slug: '',
  description: '',
  sortOrder: 0,
})
const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  slug: [{ required: true, message: '请输入URL标识', trigger: 'blur' }],
}

const categoryColors = [
  '#409EFF',
  '#67C23A',
  '#E6A23C',
  '#F56C6C',
  '#909399',
  '#C0A4F1',
  '#36D6C4',
  '#FF9F43',
]

function getCategoryColor(name: string) {
  let hash = 0
  for (let i = 0; i < name.length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash)
  }
  return categoryColors[Math.abs(hash) % categoryColors.length]
}

async function fetchCategories() {
  loading.value = true
  try {
    const res = await getCategoryList()
    categories.value = res.data.data
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function openCreateDialog() {
  editingId.value = null
  dialogTitle.value = '创建分类'
  form.value = { name: '', slug: '', description: '', sortOrder: 0 }
  dialogVisible.value = true
}

function openEditDialog(cat: Category) {
  editingId.value = cat.id
  dialogTitle.value = '编辑分类'
  form.value = {
    name: cat.name,
    slug: cat.slug,
    description: cat.description,
    sortOrder: cat.sortOrder,
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    try {
      if (editingId.value) {
        await updateCategory(editingId.value, form.value)
        ElMessage.success('更新成功')
      } else {
        await createCategory(form.value)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      fetchCategories()
    } catch (e) {
      console.error(e)
    }
  })
}

async function handleDelete(cat: Category) {
  try {
    await ElMessageBox.confirm(`确定删除分类「${cat.name}」吗？`, '警告', { type: 'warning' })
    await deleteCategory(cat.id)
    ElMessage.success('删除成功')
    fetchCategories()
  } catch (e: any) {
    if (e !== 'cancel') console.error(e)
  }
}

onMounted(fetchCategories)
</script>

<template>
  <div>
    <div class="header-section">
      <div class="title-area">
        <h2 class="text-2xl font-bold">分类管理</h2>
      </div>
      <el-button type="primary" @click="openCreateDialog">+ 创建分类</el-button>
    </div>

    <el-card v-loading="loading" class="category-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>分类列表</span>
          <span class="text-gray-400 text-sm">共 {{ categories.length }} 个分类</span>
        </div>
      </template>

      <el-table :data="categories" stripe class="category-table">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column label="名称" min-width="150">
          <template #default="{ row }">
            <div class="category-name-cell">
              <div
                class="category-icon"
                :style="{
                  backgroundColor: getCategoryColor(row.name) + '20',
                  color: getCategoryColor(row.name),
                }"
              >
                {{ row.name.charAt(0).toUpperCase() }}
              </div>
              <span class="font-medium">{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="slug" label="URL标识" width="150">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.slug }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200">
          <template #default="{ row }">
            <span class="text-gray-500">{{ row.description || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" align="center">
          <template #default="{ row }">
            <span class="sort-badge">{{ row.sortOrder }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="articleCount" label="文章数" width="100" align="center">
          <template #default="{ row }">
            <span class="article-count-badge">{{ row.articleCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="openEditDialog(row)"
              >编辑</el-button
            >
            <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="categories.length === 0 && !loading" description="暂无分类，创建一个吧" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" class="category-dialog">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="URL标识" prop="slug">
          <el-input v-model="form.slug" placeholder="如: tech" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="可选" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" />
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

.category-table :deep(.el-table__header th) {
  background-color: #f5f7fa;
  color: #606266;
  font-weight: 600;
}

.category-table :deep(.el-table__row) {
  transition: background-color 0.2s;
}

.category-table :deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}

.category-name-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.category-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 16px;
}

.sort-badge {
  background: #f0f2f5;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  color: #606266;
}

.article-count-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 24px;
  height: 24px;
  padding: 0 8px;
  background: #409eff;
  color: white;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.category-dialog :deep(.el-dialog__header) {
  border-bottom: 1px solid #f0f2f5;
  padding-bottom: 16px;
}
</style>
