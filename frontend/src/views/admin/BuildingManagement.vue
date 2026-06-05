<template>
  <div class="building-management">
    <el-card>
      <div class="filter-bar">
        <el-button type="primary" @click="showAddDialog">添加楼栋</el-button>
      </div>

      <el-table :data="buildings" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="楼栋名称" width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button type="primary" link @click="showEditDialog(scope.row)">编辑</el-button>
            <el-button
                type="danger"
                link
                @click="toggleStatus(scope.row)"
            >
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加楼栋对话框 -->
    <el-dialog title="添加楼栋" v-model="showAddModal" width="400px">
      <el-form :model="form" :rules="formRules" ref="addFormRef">
        <el-form-item label="楼栋名称" prop="name">
          <el-input 
            v-model="form.name" 
            placeholder="请输入楼栋名称"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showAddModal = false">取消</el-button>
        <el-button type="primary" @click="saveBuilding">保存</el-button>
      </template>
    </el-dialog>

    <!-- 编辑楼栋对话框 -->
    <el-dialog title="编辑楼栋" v-model="showEditModal" width="400px">
      <el-form :model="form" :rules="formRules" ref="editFormRef">
        <el-form-item label="楼栋名称" prop="name">
          <el-input 
            v-model="form.name" 
            placeholder="请输入楼栋名称"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showEditModal = false">取消</el-button>
        <el-button type="primary" @click="updateBuilding">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from '../../utils/axios'
import { ElMessage } from 'element-plus'

const buildings = ref([])

const showAddModal = ref(false)
const showEditModal = ref(false)
const addFormRef = ref(null)
const editFormRef = ref(null)

const form = ref({
  id: '',
  name: ''
})

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入楼栋名称', trigger: 'blur' },
    { min: 2, max: 50, message: '楼栋名称长度在 2 到 50 个字符', trigger: 'blur' }
  ]
}

// 显示添加对话框
const showAddDialog = () => {
  form.value = {
    id: '',
    name: ''
  }
  addFormRef.value?.clearValidate()
  showAddModal.value = true
}

// 显示编辑对话框
const showEditDialog = (building) => {
  form.value = { ...building }
  editFormRef.value?.clearValidate()
  showEditModal.value = true
}

// 保存楼栋
const saveBuilding = async () => {
  if (!addFormRef.value) return
  
  await addFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      await axios.post('/api/admin/buildings', {
        name: form.value.name,
        status: 1
      })

      ElMessage.success('添加成功')
      showAddModal.value = false
      loadBuildings()
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '添加失败')
    }
  })
}

// 更新楼栋
const updateBuilding = async () => {
  if (!editFormRef.value) return
  
  await editFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      await axios.put(`/api/admin/buildings/${form.value.id}`, {
        name: form.value.name
      })

      ElMessage.success('更新成功')
      showEditModal.value = false
      loadBuildings()
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '更新失败')
    }
  })
}

// 切换楼栋状态（启用/禁用）
const toggleStatus = async (building) => {
  const newStatus = building.status === 1 ? 0 : 1
  try {
    await axios.put(`/api/admin/buildings/${building.id}`, {
      ...building,
      status: newStatus
    })
    building.status = newStatus
    ElMessage.success(newStatus === 1 ? '已启用' : '已禁用')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 加载楼栋列表
const loadBuildings = async () => {
  try {
    const response = await axios.get('/api/admin/buildings')
    buildings.value = response.data
  } catch (error) {
    console.error('加载楼栋失败', error)
    ElMessage.error('加载楼栋失败')
  }
}

onMounted(() => {
  loadBuildings()
})
</script>

<style scoped>
.building-management {
  padding: 20px;
}

.filter-bar {
  margin-bottom: 20px;
}
</style>