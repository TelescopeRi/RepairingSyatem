<template>
  <div class="student-management">
    <el-card>
      <div class="filter-bar">
        <el-button type="primary" @click="showAddDialog">添加学生</el-button>
      </div>

      <el-table :data="students" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="学号" width="120" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="building" label="楼栋" width="100" />
        <el-table-column prop="dormNumber" label="宿舍号" width="100" />
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
            <el-button type="danger" link @click="deleteStudent(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加学生对话框 -->
    <el-dialog title="添加学生" v-model="showAddModal" width="500px">
      <el-form :model="form" :rules="formRules" ref="addFormRef" label-width="80px">
        <el-form-item label="学号" prop="username">
          <el-input 
            v-model="form.username" 
            placeholder="请输入学号"
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
        <el-form-item label="楼栋" prop="building">
          <el-select v-model="form.building" placeholder="请选择楼栋" style="width: 100%">
            <el-option v-for="building in buildings" :key="building.id" :label="building.name" :value="building.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="宿舍号" prop="dormNumber">
          <el-input 
            v-model="form.dormNumber" 
            placeholder="请输入宿舍号"
            maxlength="20"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showAddModal = false">取消</el-button>
        <el-button type="primary" @click="saveStudent">保存</el-button>
      </template>
    </el-dialog>

    <!-- 编辑学生对话框 -->
    <el-dialog title="编辑学生" v-model="showEditModal" width="500px">
      <el-form :model="form" :rules="formRules" ref="editFormRef" label-width="80px">
        <el-form-item label="学号">
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
        <el-form-item label="楼栋" prop="building">
          <el-select v-model="form.building" placeholder="请选择楼栋" style="width: 100%">
            <el-option v-for="building in buildings" :key="building.id" :label="building.name" :value="building.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="宿舍号" prop="dormNumber">
          <el-input 
            v-model="form.dormNumber" 
            placeholder="请输入宿舍号"
            maxlength="20"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showEditModal = false">取消</el-button>
        <el-button type="primary" @click="updateStudent">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from '../../utils/axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const students = ref([])
const buildings = ref([])

const showAddModal = ref(false)
const showEditModal = ref(false)
const addFormRef = ref(null)
const editFormRef = ref(null)

const form = ref({
  id: '',
  username: '',
  realName: '',
  phone: '',
  building: '',
  dormNumber: ''
})

// 表单验证规则
const formRules = {
  username: [
    { required: true, message: '请输入学号', trigger: 'blur' },
    { min: 2, max: 20, message: '学号长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 50, message: '姓名长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{0,18}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  building: [
    { required: true, message: '请选择楼栋', trigger: 'change' }
  ],
  dormNumber: [
    { required: true, message: '请输入宿舍号', trigger: 'blur' },
    { min: 1, max: 20, message: '宿舍号长度在 1 到 20 个字符', trigger: 'blur' }
  ]
}

// 显示添加对话框
const showAddDialog = () => {
  form.value = {
    id: '',
    username: '',
    realName: '',
    phone: '',
    building: '',
    dormNumber: ''
  }
  addFormRef.value?.clearValidate()
  showAddModal.value = true
}

// 显示编辑对话框
const showEditDialog = (student) => {
  form.value = { ...student }
  editFormRef.value?.clearValidate()
  showEditModal.value = true
}

// 保存学生
const saveStudent = async () => {
  if (!addFormRef.value) return
  
  await addFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      await axios.post('/api/admin/students', {
        username: form.value.username,
        realName: form.value.realName,
        phone: form.value.phone,
        building: form.value.building,
        dormNumber: form.value.dormNumber
      })

      ElMessage.success('添加成功')
      showAddModal.value = false
      loadStudents()
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '添加失败')
    }
  })
}

// 更新学生
const updateStudent = async () => {
  if (!editFormRef.value) return
  
  await editFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      await axios.put(`/api/admin/users/${form.value.id}`, {
        realName: form.value.realName,
        phone: form.value.phone,
        building: form.value.building,
        dormNumber: form.value.dormNumber
      })

      ElMessage.success('更新成功')
      showEditModal.value = false
      loadStudents()
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
      ElMessage.error(error.response?.data?.message || '操作失败')
    }
  }
}

// 切换状态（启用/禁用）
const toggleStatus = async (student) => {
  const newStatus = student.status === 1 ? 0 : 1
  try {
    await axios.put(`/api/admin/users/${student.id}/status`, null, {
      params: { status: newStatus }
    })
    student.status = newStatus
    ElMessage.success(newStatus === 1 ? '已启用' : '已禁用')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

// 删除学生
const deleteStudent = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该学生吗？', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await axios.delete(`/api/admin/users/${id}`)
    ElMessage.success('删除成功')
    loadStudents()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 加载学生列表
const loadStudents = async () => {
  try {
    const response = await axios.get('/api/admin/students')
    students.value = response.data
  } catch (error) {
    console.error('加载学生失败', error)
    ElMessage.error('加载学生失败')
  }
}

// 加载楼栋列表（去重）
const loadBuildings = async () => {
  try {
    const response = await axios.get('/api/admin/buildings')
    const data = response.data || []
    // 使用 Map 去重，保持原始顺序
    const uniqueData = [...new Map(data.map(item => [item.id, item])).values()]
    buildings.value = uniqueData
  } catch (error) {
    console.error('加载楼栋失败', error)
  }
}

onMounted(() => {
  loadStudents()
  loadBuildings()
})
</script>

<style scoped>
.student-management {
  padding: 20px;
}

.filter-bar {
  margin-bottom: 20px;
}
</style>