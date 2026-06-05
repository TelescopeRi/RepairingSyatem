<template>
  <div class="fault-type-management">
    <el-card>
      <div class="filter-bar">
        <el-button type="primary" @click="showAddDialog">添加故障类型</el-button>
      </div>

      <el-table :data="faultTypes" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="类型名称" width="150" />
        <el-table-column prop="sortOrder" label="排序" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button type="primary" link @click="showEditDialog(scope.row)" size="small">编辑</el-button>
            <el-button
                :type="scope.row.status === 1 ? 'warning' : 'success'"
                link
                @click="toggleStatus(scope.row)"
                size="small"
            >
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="danger" link @click="deleteFaultType(scope.row.id)" size="small">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加故障类型对话框 -->
    <el-dialog title="添加故障类型" v-model="showAddModal" width="400px">
      <el-form :model="form" :rules="formRules" ref="addFormRef">
        <el-form-item label="类型名称" prop="name">
          <el-input 
            v-model="form.name" 
            placeholder="请输入类型名称"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number 
            v-model="form.sortOrder" 
            :min="0" 
            :max="9999" 
            placeholder="请输入排序数字"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showAddModal = false">取消</el-button>
        <el-button type="primary" @click="saveFaultType">保存</el-button>
      </template>
    </el-dialog>

    <!-- 编辑故障类型对话框 -->
    <el-dialog title="编辑故障类型" v-model="showEditModal" width="400px">
      <el-form :model="form" :rules="formRules" ref="editFormRef">
        <el-form-item label="类型名称" prop="name">
          <el-input 
            v-model="form.name" 
            placeholder="请输入类型名称"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number 
            v-model="form.sortOrder" 
            :min="0" 
            :max="9999" 
            placeholder="请输入排序数字"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showEditModal = false">取消</el-button>
        <el-button type="primary" @click="updateFaultType">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from '../../utils/axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const faultTypes = ref([])

const showAddModal = ref(false)
const showEditModal = ref(false)
const addFormRef = ref(null)
const editFormRef = ref(null)

const form = ref({
  id: '',
  name: '',
  sortOrder: 0
})

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入类型名称', trigger: 'blur' },
    { min: 2, max: 50, message: '类型名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  sortOrder: [
    { type: 'number', message: '排序必须是数字', trigger: 'blur' }
  ]
}

// 显示添加对话框
const showAddDialog = () => {
  form.value = {
    id: '',
    name: '',
    sortOrder: 0
  }
  addFormRef.value?.clearValidate()
  showAddModal.value = true
}

// 显示编辑对话框
const showEditDialog = (faultType) => {
  form.value = { ...faultType }
  editFormRef.value?.clearValidate()
  showEditModal.value = true
}

// 保存故障类型
const saveFaultType = async () => {
  if (!addFormRef.value) return
  
  await addFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      await axios.post('/api/admin/fault-types', {
        name: form.value.name,
        sortOrder: form.value.sortOrder || 0,
        status: 1
      })

      ElMessage.success('添加成功')
      showAddModal.value = false
      loadFaultTypes()
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '添加失败')
    }
  })
}

// 更新故障类型
const updateFaultType = async () => {
  if (!editFormRef.value) return
  
  await editFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      await axios.put(`/api/admin/fault-types/${form.value.id}`, {
        name: form.value.name,
        sortOrder: form.value.sortOrder || 0
      })

      ElMessage.success('更新成功')
      showEditModal.value = false
      loadFaultTypes()
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '更新失败')
    }
  })
}

// 切换故障类型状态（启用/禁用）
const toggleStatus = async (faultType) => {
  const newStatus = faultType.status === 1 ? 0 : 1
  try {
    await axios.put(`/api/admin/fault-types/${faultType.id}`, {
      ...faultType,
      status: newStatus
    })
    faultType.status = newStatus
    ElMessage.success(newStatus === 1 ? '已启用' : '已禁用')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 删除故障类型
const deleteFaultType = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该故障类型吗？删除后无法恢复', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await axios.delete(`/api/admin/fault-types/${id}`)
    ElMessage.success('删除成功')
    loadFaultTypes()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

// 加载故障类型列表
const loadFaultTypes = async () => {
  try {
    const response = await axios.get('/api/admin/fault-types')
    faultTypes.value = response.data
  } catch (error) {
    console.error('加载故障类型失败', error)
    ElMessage.error('加载故障类型失败')
  }
}

onMounted(() => {
  loadFaultTypes()
})
</script>

<style scoped>
.fault-type-management {
  padding: 20px;
}

.filter-bar {
  margin-bottom: 20px;
}
</style>