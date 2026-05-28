<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  getArticleDetail,
  createArticle,
  updateArticle,
  bindArticleTags,
  getCategoryList,
  getTagList,
  uploadFile,
} from '@/api'
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
const coverUploading = ref(false)
const coverPreviewUrl = computed(() => form.value.coverImage || '')
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

// ========== 封面图上传 ==========
async function handleCoverUpload(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.error('仅支持 JPG、PNG、GIF、WebP 格式')
    input.value = ''
    return
  }

  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 5MB')
    input.value = ''
    return
  }

  coverUploading.value = true
  try {
    const res = await uploadFile(file, 'article')
    form.value.coverImage = res.data.data
    ElMessage.success('封面上传成功')
  } catch {
    ElMessage.error('封面上传失败，请重试')
  } finally {
    coverUploading.value = false
    input.value = ''
  }
}

function removeCover() {
  form.value.coverImage = ''
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
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <!-- 左栏：富文本编辑器 | 右栏：其他字段 -->
        <div
          class="grid grid-cols-1 lg:grid-cols-3 gap-6"
          :class="{ 'fullscreen-mode': isEditorFullscreen }"
        >
          <!-- 左：内容编辑器 -->
          <div class="lg:col-span-2">
            <el-form-item label="内容" prop="content">
              <RichEditor
                ref="editorRef"
                v-model="form.content"
                :height="600"
                @fullscreen-change="handleFullscreenChange"
              />
            </el-form-item>
          </div>

          <!-- 右：其他字段 -->
          <div v-show="!isEditorFullscreen" class="lg:col-span-1">
            <el-form-item label="标题" prop="title">
              <el-input v-model="form.title" placeholder="请输入文章标题" />
            </el-form-item>
            <el-form-item label="摘要" prop="summary">
              <el-input
                v-model="form.summary"
                type="textarea"
                :rows="2"
                placeholder="请输入文章摘要"
              />
            </el-form-item>
            <el-form-item label="封面图" prop="coverImage">
              <div class="space-y-2 w-full">
                <!-- 预览图 -->
                <div v-if="coverPreviewUrl" class="relative inline-block">
                  <img :src="coverPreviewUrl" class="h-24 rounded object-cover" alt="封面预览" />
                  <el-button
                    type="danger"
                    size="small"
                    circle
                    class="absolute -top-2 -right-2"
                    @click="removeCover"
                  >
                    ✕
                  </el-button>
                </div>
                <!-- 上传区域 -->
                <div class="flex gap-2">
                  <label
                    class="cursor-pointer inline-flex items-center px-3 py-1.5 border border-gray-300 rounded text-sm text-gray-600 hover:border-blue-400 hover:text-blue-500 transition-colors"
                  >
                    <el-icon v-if="coverUploading" class="is-loading mr-1" :size="14"
                      ><Loading
                    /></el-icon>
                    <span>{{ coverUploading ? '上传中...' : '选择图片' }}</span>
                    <input
                      type="file"
                      accept="image/jpeg,image/png,image/gif,image/webp"
                      hidden
                      @change="handleCoverUpload"
                    />
                  </label>
                </div>
                <el-input v-model="form.coverImage" placeholder="或直接输入封面图URL" />
              </div>
            </el-form-item>
            <el-form-item label="分类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
                <el-option
                  v-for="cat in categories"
                  :key="cat.id"
                  :label="cat.name"
                  :value="cat.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="标签">
              <el-select
                v-model="form.tagIds"
                multiple
                placeholder="请选择标签"
                style="width: 100%"
              >
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
          </div>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.fullscreen-mode {
  grid-template-columns: 1fr !important;
}
</style>
