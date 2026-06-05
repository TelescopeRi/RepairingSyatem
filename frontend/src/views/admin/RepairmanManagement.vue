<template>
  <div class="repairman-management">
    <el-card>
      <div class="filter-bar">
        <el-button type="primary" @click="showAddDialog">添加维修工</el-button>
      </div>

      <el-table :data="repairmen" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="工号" width="120" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="specialtyNames" label="擅长类型" width="200">
          <template #default="scope">
            <span v-if="scope.row.specialtyNames" class="specialty-tags">
              <el-tag 
                v-for="name in scope.row.specialtyNames.split(',')" 
                :key="name.trim()"
                size="small"
                type="success"
              >
                {{ name.trim() }}
              </el-tag>
            </span>
            <span v-else class="no-specialty">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="scope">
            <el-button type="primary" link @click="showEditDialog(scope.row)">编辑</el-button>
            <el-button type="warning" link @click="resetPassword(scope.row.id)">重置密码</el-button>
            <el-button
                :type="scope.row.status === 1 ? 'danger' : 'success'"
                link
                @click="toggleStatus(scope.row)"
            >
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="danger" link @click="deleteRepairman(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加维修工对话框 -->
    <el-dialog title="添加维修工" v-model="showAddModal" width="500px">
      <el-form :model="form" :rules="formRules" ref="addFormRef" label-width="80px">
        <el-form-item label="工号" prop="username">
          <el-input 
            v-model="form.username" 
            placeholder="请输入工号"
            maxlength="20"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input 
            v-model="form.realName" 
            placeholder="请输入姓名"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input 
            v-model="form.phone" 
            placeholder="请输入手机号"
            maxlength="20"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="擅长类型">
          <el-select 
            v-model="form.specialtyIds" 
            multiple 
            placeholder="请选择擅长修理类型（可多选）"
            style="width: 100%"
          >
            <el-option 
              v-for="type in faultTypes" 
              :key="type.id" 
              :label="type.name" 
              :value="type.id"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showAddModal = false">取消</el-button>
        <el-button type="primary" @click="saveRepairman">保存</el-button>
      </template>
    </el-dialog>

    <!-- 编辑维修工对话框 -->
    <el-dialog title="编辑维修工" v-model="showEditModal" width="500px">
      <el-form :model="form" :rules="formRules" ref="editFormRef" label-width="80px">
        <el-form-item label="工号">
          <el-input v-model="form.username" disabled />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input 
            v-model="form.realName" 
            placeholder="请输入姓名"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input 
            v-model="form.phone" 
            placeholder="请输入手机号"
            maxlength="20"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="擅长类型">
          <el-select 
            v-model="form.specialtyIds" 
            multiple 
            placeholder="请选择擅长修理类型（可多选）"
            style="width: 100%"
          >
            <el-option 
              v-for="type in faultTypes" 
              :key="type.id" 
              :label="type.name" 
              :value="type.id"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showEditModal = false">取消</el-button>
        <el-button type="primary" @click="updateRepairman">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from '../../utils/axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const repairmen = ref([])
const faultTypes = ref([])

const showAddModal = ref(false)
const showEditModal = ref(false)
const addFormRef = ref(null)
const editFormRef = ref(null)

const form = ref({
  id: '',
  username: '',
  realName: '',
  phone: '',
  specialtyIds: []
})

// 表单验证规则
const formRules = {
  username: [
    { required: true, message: '请输入工号', trigger: 'blur' },
    { min: 2, max: 20, message: '工号长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 50, message: '姓名长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{0,18}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

// 显示添加对话框
const showAddDialog = () => {
  form.value = {
    id: '',
    username: '',
    realName: '',
    phone: '',
    specialtyIds: []
  }
  addFormRef.value?.clearValidate()
  showAddModal.value = true
}

// 显示编辑对话框
const showEditDialog = (repairman) => {
  // 将specialtyIds字符串转换为数组
  const specialtyIds = repairman.specialtyIds ? repairman.specialtyIds.split(',').map(id => Number(id.trim())) : []
  form.value = { 
    ...repairman,
    specialtyIds: specialtyIds
  }
  editFormRef.value?.clearValidate()
  showEditModal.value = true
}

// 保存维修工
const saveRepairman = async () => {
  if (!addFormRef.value) return
  
  await addFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      // 将数组转换为逗号分隔的字符串
      const specialtyIdsStr = form.value.specialtyIds.join(',')
      
      await axios.post('/api/admin/repairmen', {
        ...form.value,
        role: 'REPAIR',
        status: 1,
        specialtyIds: specialtyIdsStr || null
      })

      ElMessage.success('添加成功')
      showAddModal.value = false
      loadRepairmen()
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '添加失败')
    }
  })
}

// 更新维修工
const updateRepairman = async () => {
  if (!editFormRef.value) return
  
  await editFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      // 将数组转换为逗号分隔的字符串
      const specialtyIdsStr = form.value.specialtyIds.join(',')
      
      await axios.put(`/api/admin/users/${form.value.id}`, {
        realName: form.value.realName,
        phone: form.value.phone,
        specialtyIds: specialtyIdsStr || null
      })

      ElMessage.success('更新成功')
      showEditModal.value = false
      loadRepairmen()
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '更新失败')
    }
  })
}

// 重置密码
const resetPassword = async (id) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入新密码', '重置密码', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputType: 'password',
      inputPattern: /^.{6,}$/,
      inputErrorMessage: '密码长度不能小于6位'
    })

    await axios.put(`/api/admin/users/${id}/password`, null, {
      params: { password: value }
    })
    ElMessage.success('密码重置成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

// 切换状态（启用/禁用）
const toggleStatus = async (repairman) => {
  const newStatus = repairman.status === 1 ? 0 : 1
  try {
    await axios.put(`/api/admin/users/${repairman.id}/status`, null, {
      params: { status: newStatus }
    })
    repairman.status = newStatus
    ElMessage.success(newStatus === 1 ? '已启用' : '已禁用')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

// 删除维修工
const deleteRepairman = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该维修工吗？', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await axios.delete(`/api/admin/users/${id}`)
    ElMessage.success('删除成功')
    loadRepairmen()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 加载维修工列表
const loadRepairmen = async () => {
  try {
    const response = await axios.get('/api/admin/repairmen')
    repairmen.value = response.data
  } catch (error) {
    console.error('加载维修工失败', error)
    ElMessage.error('加载维修工失败')
  }
}

// 加载故障类型
const loadFaultTypes = async () => {
  try {
    const response = await axios.get('/api/admin/fault-types')
    faultTypes.value = response.data
  } catch (error) {
    console.error('加载故障类型失败', error)
  }
}

onMounted(() => {
  loadRepairmen()
  loadFaultTypes()
})
</script>

<style scoped>
.repairman-management {
  padding: 20px;
}

.filter-bar {
  margin-bottom: 20px;
}
</style>