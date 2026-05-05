<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getArticleDetail, createArticle, updateArticle, bindArticleTags, getCategoryList, getTagList } from '@/api'
import type { ArticleFormData, Category, Tag } from '@/types'
import { ElMessage } from 'element-plus'
import RichEditor from '@/components/rich-editor/RichEditor.vue'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => !!route.params.id)
const articleId = computed(() => Number(route.params.id))

const loading = ref(false)
const submitting = ref(false)
const formRef = ref()
const editorRef = ref()
const isEditorFullscreen = ref(false)
const categories = ref<Category[]>([])
const allTags = ref<Tag[]>([])
const form = ref<ArticleFormData>({
  title: '',
  summary: '',
  content: '',
  coverImage: '',
  categoryId: 0,
  tagIds: [],
  status: 'draft',
})
const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
}

async function fetchCategories() {
  try {
    const res = await getCategoryList()
    categories.value = res.data.data
  } catch (e) {
    console.error(e)
  }
}

async function fetchTags() {
  try {
    const res = await getTagList()
    allTags.value = res.data.data
  } catch (e) {
    console.error(e)
  }
}

async function fetchArticle() {
  if (!isEdit.value) return
  loading.value = true
  try {
    const res = await getArticleDetail(articleId.value)
    const data = res.data.data
    form.value = {
      title: data.title,
      summary: data.summary,
      content: data.content,
      coverImage: data.coverImage,
      categoryId: data.category?.id,
      tagIds: data.tags?.map((t: any) => t.id) || [],
      status: data.status,
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    submitting.value = true
    try {
      if (isEdit.value) {
        await updateArticle(articleId.value, form.value)
        if (form.value.tagIds) {
          await bindArticleTags(articleId.value, form.value.tagIds)
        }
        ElMessage.success('更新成功')
      } else {
        const res = await createArticle(form.value)
        if (form.value.tagIds && form.value.tagIds.length > 0) {
          await bindArticleTags(res.data.data.id, form.value.tagIds)
        }
        ElMessage.success('创建成功')
      }
      router.push('/admin/articles')
    } catch (e) {
      console.error(e)
    } finally {
      submitting.value = false
    }
  })
}

function handleCancel() {
  router.back()
}

function handleFullscreenChange(isFullscreen: boolean) {
  isEditorFullscreen.value = isFullscreen
}

onMounted(() => {
  fetchCategories()
  fetchTags()
  fetchArticle()
})
</script>

<template>
  <div>
    <div class="flex items-center justify-between mb-4">
      <h2 class="text-2xl font-bold">{{ isEdit ? '编辑文章' : '创建文章' }}</h2>
    </div>

    <el-card v-loading="loading">
      <!-- 左栏：富文本编辑器 | 右栏：其他字段 -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6" :class="{ 'fullscreen-mode': isEditorFullscreen }">
        <!-- 左：内容编辑器 -->
        <div class="lg:col-span-2">
          <el-form-item label="内容" prop="content">
            <RichEditor ref="editorRef" v-model="form.content" :height="600" @fullscreen-change="handleFullscreenChange" />
          </el-form-item>
        </div>

        <!-- 右：其他字段 -->
        <div v-show="!isEditorFullscreen" class="lg:col-span-1">
          <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
            <el-form-item label="标题" prop="title">
              <el-input v-model="form.title" placeholder="请输入文章标题" />
            </el-form-item>
            <el-form-item label="摘要" prop="summary">
              <el-input v-model="form.summary" type="textarea" :rows="2" placeholder="请输入文章摘要" />
            </el-form-item>
            <el-form-item label="封面图" prop="coverImage">
              <el-input v-model="form.coverImage" placeholder="请输入封面图URL" />
            </el-form-item>
            <el-form-item label="分类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
                <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="标签">
              <el-select v-model="form.tagIds" multiple placeholder="请选择标签" style="width: 100%">
                <el-option v-for="tag in allTags" :key="tag.id" :label="tag.name" :value="tag.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio value="draft">草稿</el-radio>
                <el-radio value="published">发布</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="submitting" @click="handleSubmit">
                {{ isEdit ? '更新' : '创建' }}
              </el-button>
              <el-button @click="handleCancel">取消</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.fullscreen-mode {
  grid-template-columns: 1fr !important;
}
</style>