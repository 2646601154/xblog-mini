<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getUserList, updateUser, disableUser, enableUser, deleteUser } from '@/api'
import type { User, UserFormData } from '@/types'
import { ElMessage, ElMessageBox } from 'element-plus'

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

onMounted(fetchUsers)
</script>

<template>
  <div>
    <div class="flex items-center justify-between mb-4">
      <h2 class="text-2xl font-bold">用户管理</h2>
    </div>

    <el-card>
      <div class="flex gap-4 mb-4">
        <el-select v-model="filters.role" placeholder="角色筛选" clearable @change="handleFilter">
          <el-option label="全部" value="" />
          <el-option label="管理员" value="admin" />
          <el-option label="普通用户" value="user" />
        </el-select>
        <el-select v-model="filters.status" placeholder="状态筛选" clearable @change="handleFilter">
          <el-option label="全部" value="" />
          <el-option label="正常" value="normal" />
          <el-option label="禁用" value="disabled" />
        </el-select>
      </div>

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
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="openEditDialog(row)">编辑</el-button>
            <el-button v-if="row.status === 'normal'" type="danger" size="small" link @click="handleDisable(row)">禁用</el-button>
            <el-button v-else type="success" size="small" link @click="handleEnable(row)">启用</el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
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
  </div>
</template>