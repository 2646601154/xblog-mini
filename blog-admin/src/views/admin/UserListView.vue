<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  getUserList,
  createUser,
  updateUser,
  disableUser,
  enableUser,
  deleteUser,
  resetPassword,
} from '@/api'
import type { User, UserFormData, CreateUserFormData, ResetPasswordFormData } from '@/types'
import { useAuthStore } from '@/stores/auth'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()
const currentUserId = computed(() => authStore.userInfo?.id)

const loading = ref(false)
const users = ref<User[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const filters = ref({
  role: '',
  status: '',
})

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const editingUser = ref<User | null>(null)
const form = ref<UserFormData>({
  nickname: '',
  email: '',
  avatar: '',
  role: 'user',
  status: 'normal',
})

const createDialogVisible = ref(false)
const createFormRef = ref()
const createForm = ref<CreateUserFormData>({
  username: '',
  password: '',
  nickname: '',
  email: '',
  role: 'user',
})
const createRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    {
      pattern: /^[a-zA-Z][a-zA-Z0-9]{2,19}$/,
      message: '用户名需3-20位，字母开头',
      trigger: 'blur',
    },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码最少6位', trigger: 'blur' },
  ],
  email: [{ type: 'email' as const, message: '邮箱格式不正确', trigger: 'blur' }],
}

const resetPwdDialogVisible = ref(false)
const resetPwdFormRef = ref()
const resetPwdUser = ref<User | null>(null)
const resetPwdForm = ref<ResetPasswordFormData>({
  newPassword: '',
})
const resetPwdRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码最少6位', trigger: 'blur' },
  ],
}

async function fetchUsers() {
  loading.value = true
  try {
    const res = await getUserList({
      page: page.value,
      size: size.value,
      role: filters.value.role || undefined,
      status: filters.value.status || undefined,
    })
    users.value = res.data.data.records
    total.value = res.data.data.total
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function handleFilter() {
  page.value = 1
  fetchUsers()
}

function handlePageChange(p: number) {
  page.value = p
  fetchUsers()
}

function handleSizeChange(s: number) {
  size.value = s
  fetchUsers()
}

function openCreateDialog() {
  createForm.value = {
    username: '',
    password: '',
    nickname: '',
    email: '',
    role: 'user',
  }
  createDialogVisible.value = true
}

async function handleCreateSubmit() {
  if (!createFormRef.value) return
  await createFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    try {
      await createUser(createForm.value)
      ElMessage.success('创建成功')
      createDialogVisible.value = false
      fetchUsers()
    } catch (e) {
      console.error(e)
    }
  })
}

function openEditDialog(user: User) {
  editingUser.value = user
  dialogTitle.value = '编辑用户'
  form.value = {
    nickname: user.nickname,
    email: user.email,
    avatar: user.avatar,
    role: user.role,
    status: user.status,
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value || !editingUser.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    try {
      await updateUser(editingUser.value!.id, form.value)
      ElMessage.success('更新成功')
      dialogVisible.value = false
      fetchUsers()
    } catch (e) {
      console.error(e)
    }
  })
}

async function handleDisable(user: User) {
  try {
    await ElMessageBox.confirm(`确定禁用用户「${user.nickname}」吗？`, '提示')
    await disableUser(user.id)
    ElMessage.success('禁用成功')
    fetchUsers()
  } catch (e: any) {
    if (e !== 'cancel') console.error(e)
  }
}

async function handleEnable(user: User) {
  try {
    await enableUser(user.id)
    ElMessage.success('启用成功')
    fetchUsers()
  } catch (e) {
    console.error(e)
  }
}

async function handleDelete(user: User) {
  try {
    await ElMessageBox.confirm(`确定删除用户「${user.nickname}」吗？`, '警告', { type: 'warning' })
    await deleteUser(user.id)
    ElMessage.success('删除成功')
    fetchUsers()
  } catch (e: any) {
    if (e !== 'cancel') console.error(e)
  }
}

function openResetPasswordDialog(user: User) {
  resetPwdUser.value = user
  resetPwdForm.value = { newPassword: '' }
  resetPwdDialogVisible.value = true
}

async function handleResetPasswordSubmit() {
  if (!resetPwdFormRef.value || !resetPwdUser.value) return
  await resetPwdFormRef.value.validate(async (valid: boolean) => {
    if (!valid) return
    try {
      await resetPassword(resetPwdUser.value!.id, resetPwdForm.value)
      ElMessage.success('密码重置成功')
      resetPwdDialogVisible.value = false
    } catch (e) {
      console.error(e)
    }
  })
}

onMounted(fetchUsers)
</script>

<template>
  <div>
    <div class="flex items-center justify-between mb-4">
      <h2 class="text-2xl font-bold">用户管理</h2>
      <div class="flex gap-3 items-center">
        <el-select v-model="filters.role" placeholder="角色筛选" clearable @change="handleFilter" style="width: 140px">
          <el-option label="全部" value="" />
          <el-option label="管理员" value="admin" />
          <el-option label="普通用户" value="user" />
        </el-select>
        <el-select v-model="filters.status" placeholder="状态筛选" clearable @change="handleFilter" style="width: 140px">
          <el-option label="全部" value="" />
          <el-option label="正常" value="normal" />
          <el-option label="禁用" value="disabled" />
        </el-select>
        <el-button type="primary" @click="openCreateDialog">添加用户</el-button>
      </div>
    </div>

    <el-card shadow="never">
      <el-table :data="users" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="用户" min-width="200">
          <template #default="{ row }">
            <div class="flex items-center gap-3">
              <el-avatar :size="40" :src="row.avatar">{{ row.nickname?.charAt(0) }}</el-avatar>
              <div>
                <p class="font-medium">{{ row.nickname }}</p>
                <p class="text-gray-400 text-xs">@{{ row.username }}</p>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.role === 'admin' ? 'danger' : 'info'" size="small">
              {{ row.role === 'admin' ? '管理员' : '用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'normal' ? 'success' : 'danger'" size="small">
              {{ row.status === 'normal' ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" width="180">
          <template #default="{ row }">
            {{ new Date(row.createdAt).toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="290" fixed="right">
          <template #default="{ row }">
            <template v-if="row.id !== currentUserId">
              <el-button type="primary" size="small" link @click="openEditDialog(row)"
                >编辑</el-button
              >
              <el-button type="warning" size="small" link @click="openResetPasswordDialog(row)"
                >重置密码</el-button
              >
              <el-button
                v-if="row.status === 'normal'"
                type="danger"
                size="small"
                link
                @click="handleDisable(row)"
                >禁用</el-button
              >
              <el-button v-else type="success" size="small" link @click="handleEnable(row)"
                >启用</el-button
              >
              <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
            </template>
            <el-button v-else type="primary" size="small" link @click="router.push('/admin/profile')"
              >编辑自己请前往个人中心</el-button
            >
          </template>
        </el-table-column>
      </el-table>

      <div class="mt-4 flex justify-end">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="form" label-width="80px">
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="头像" prop="avatar">
          <el-input v-model="form.avatar" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role">
            <el-option label="管理员" value="admin" />
            <el-option label="用户" value="user" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status">
            <el-option label="正常" value="normal" />
            <el-option label="禁用" value="disabled" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="createDialogVisible" title="添加用户" width="500px">
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="createForm.username" placeholder="3-20位，字母开头" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="createForm.password"
            type="password"
            show-password
            placeholder="至少6位"
          />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="createForm.nickname" placeholder="选填，默认为用户名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="createForm.email" placeholder="选填" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="createForm.role">
            <el-option label="用户" value="user" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="resetPwdDialogVisible" title="重置密码" width="400px">
      <el-form
        ref="resetPwdFormRef"
        :model="resetPwdForm"
        :rules="resetPwdRules"
        label-width="80px"
      >
        <el-form-item label="用户名">
          <span class="text-gray-500">{{ resetPwdUser?.username }}</span>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="resetPwdForm.newPassword"
            type="password"
            show-password
            placeholder="至少6位"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetPwdDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleResetPasswordSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>
