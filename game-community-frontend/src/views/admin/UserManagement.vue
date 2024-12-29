<template>
  <div class="user-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" @click="handleCreateUser">
            新建用户
          </el-button>
        </div>
      </template>

      <!-- 搜索过滤 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input
              v-model="searchForm.username"
              placeholder="搜索用户名"
              clearable
          />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="searchForm.role" placeholder="选择角色" clearable>
            <el-option label="管理员" value="ADMIN" />
            <el-option label="普通用户" value="USER" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" clearable>
            <el-option label="正常" value="ACTIVE" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 用户列表 -->
      <el-table
          v-loading="loading"
          :data="userList"
          border
          style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="用户信息">
          <template #default="{ row }">
            <div class="user-info">
              <el-avatar :size="32" :src="row.avatar" />
              <div class="user-details">
                <div class="username">{{ row.username }}</div>
                <div class="nickname">{{ row.nickname }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" />
        <el-table-column label="角色">
          <template #default="{ row }">
            <el-tag
                v-for="role in row.roles"
                :key="role"
                :type="role === 'ADMIN' ? 'danger' : ''"
                class="role-tag"
            >
              {{ getRoleLabel(role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-switch
                v-model="row.status"
                :active-value="'ACTIVE'"
                :inactive-value="'DISABLED'"
                @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.lastLoginTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
                link
                type="primary"
                @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button
                link
                type="warning"
                @click="handleResetPassword(row)"
            >
              重置密码
            </el-button>
            <el-button
                link
                type="danger"
                @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>

      <!-- 用户编辑对话框 -->
      <el-dialog
          v-model="dialogVisible"
          :title="dialogTitle"
          width="500px"
      >
        <el-form
            ref="userForm"
            :model="formData"
            :rules="formRules"
            label-width="80px"
        >
          <el-form-item label="用户名" prop="username">
            <el-input
                v-model="formData.username"
                :disabled="isEdit"
            />
          </el-form-item>
          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="formData.nickname" />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="formData.email" />
          </el-form-item>
          <el-form-item label="角色" prop="roles">
            <el-checkbox-group v-model="formData.roles">
              <el-checkbox label="ADMIN">管理员</el-checkbox>
              <el-checkbox label="USER">普通用户</el-checkbox>
            </el-checkbox-group>
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-radio-group v-model="formData.status">
              <el-radio label="ACTIVE">正常</el-radio>
              <el-radio label="DISABLED">禁用</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitForm">确认</el-button>
          </span>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { format } from 'date-fns'
import {
  getUsers,
  createUser,
  updateUser,
  deleteUser,
  resetPassword
} from '@/api/user'

export default {
  name: 'UserManagement',

  setup() {
    const loading = ref(false)
    const dialogVisible = ref(false)
    const currentPage = ref(1)
    const pageSize = ref(20)
    const total = ref(0)
    const userList = ref([])
    const userForm = ref(null)

    const searchForm = reactive({
      username: '',
      role: '',
      status: ''
    })

    const formData = reactive({
      username: '',
      nickname: '',
      email: '',
      roles: [],
      status: 'ACTIVE'
    })

    const isEdit = ref(false)

    const dialogTitle = computed(() => {
      return isEdit.value ? '编辑用户' : '新建用户'
    })

    const formRules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
      ],
      nickname: [
        { required: true, message: '请输入昵称', trigger: 'blur' }
      ],
      email: [
        { required: true, message: '请输入邮箱', trigger: 'blur' },
        { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
      ],
      roles: [
        { required: true, message: '请选择角色', trigger: 'change' }
      ]
    }

    // 获取用户列表
    const fetchUserList = async () => {
      loading.value = true
      try {
        const params = {
          page: currentPage.value - 1,
          size: pageSize.value,
          ...searchForm
        }
        const res = await getUsers(params)
        userList.value = res.data.content
        total.value = res.data.totalElements
      } catch (error) {
        ElMessage.error('获取用户列表失败')
      } finally {
        loading.value = false
      }
    }

    // 创建/编辑用户
    const handleCreateUser = () => {
      isEdit.value = false
      Object.assign(formData, {
        username: '',
        nickname: '',
        email: '',
        roles: ['USER'],
        status: 'ACTIVE'
      })
      dialogVisible.value = true
    }

    const handleEdit = (row) => {
      isEdit.value = true
      Object.assign(formData, row)
      dialogVisible.value = true
    }

    // 提交表单
    const submitForm = async () => {
      if (!userForm.value) return

      await userForm.value.validate(async (valid) => {
        if (valid) {
          try {
            if (isEdit.value) {
              await updateUser(formData.id, formData)
              ElMessage.success('更新成功')
            } else {
              await createUser(formData)
              ElMessage.success('创建成功')
            }
            dialogVisible.value = false
            fetchUserList()
          } catch (error) {
            ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
          }
        }
      })
    }

    // 重置密码
    const handleResetPassword = async (row) => {
      try {
        await ElMessageBox.confirm('确定要重置该用户的密码吗？')
        await resetPassword(row.id)
        ElMessage.success('密码重置成功')
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('密码重置失败')
        }
      }
    }

    // 删除用户
    const handleDelete = async (row) => {
      try {
        await ElMessageBox.confirm('确定要删除该用户吗？此操作不可恢复！', '警告', {
          type: 'warning'
        })
        await deleteUser(row.id)
        ElMessage.success('删除成功')
        fetchUserList()
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('删除失败')
        }
      }
    }

    // 状态变更
    const handleStatusChange = async (row) => {
      try {
        await updateUser(row.id, { status: row.status })
        ElMessage.success('状态更新成功')
      } catch (error) {
        row.status = row.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
        ElMessage.error('状态更新失败')
      }
    }

    // 搜索
    const handleSearch = () => {
      currentPage.value = 1
      fetchUserList()
    }

    const resetSearch = () => {
      Object.assign(searchForm, {
        username: '',
        role: '',
        status: ''
      })
      handleSearch()
    }

    // 分页
    const handleSizeChange = (val) => {
      pageSize.value = val
      fetchUserList()
    }

    const handleCurrentChange = (val) => {
      currentPage.value = val
      fetchUserList()
    }

    // 角色标签显示
    const getRoleLabel = (role) => {
      const roleMap = {
        'ADMIN': '管理员',
        'USER': '普通用户'
      }
      return roleMap[role] || role
    }

    // 格式化时间
    const formatDateTime = (time) => {
      return time ? format(new Date(time), 'yyyy-MM-dd HH:mm:ss') : '-'
    }

    return {
      loading,
      dialogVisible,
      currentPage,
      pageSize,
      total,
      userList,
      userForm,
      searchForm,
      formData,
      isEdit,
      dialogTitle,
      formRules,
      handleCreateUser,
      handleEdit,
      submitForm,
      handleResetPassword,
      handleDelete,
      handleStatusChange,
      handleSearch,
      resetSearch,
      handleSizeChange,
      handleCurrentChange,
      getRoleLabel,
      formatDateTime
    }
  }
}
</script>

<style scoped>
.user-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-details {
  display: flex;
  flex-direction: column;
}

.username {
  font-weight: bold;
}

.nickname {
  font-size: 12px;
  color: #666;
}

.role-tag {
  margin-right: 5px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>