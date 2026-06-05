<template>
  <div class="repair-form">
    <el-card header="提交报修">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="故障类型" prop="faultTypeId" required>
          <el-select v-model="form.faultTypeId" placeholder="请选择故障类型" style="width: 100%">
            <el-option v-for="type in faultTypes" :key="type.id" :label="type.name" :value="type.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="故障描述" prop="description" required>
          <el-input
              v-model="form.description"
              type="textarea"
              :maxlength="200"
              placeholder="请详细描述故障情况（最多200字）"
              :rows="4"
              show-word-limit
          />
        </el-form-item>

        <el-form-item label="图片上传">
          <div class="upload-area">
            <el-upload
                :action="uploadUrl"
                :headers="uploadHeaders"
                :file-list="imageFiles"
                :limit="5"
                :on-success="handleUploadSuccess"
                :on-remove="handleUploadRemove"
                :before-upload="beforeUpload"
                list-type="picture-card"
            >
              <el-icon :size="30">
                <Plus />
              </el-icon>
            </el-upload>
          </div>
          <div class="upload-tip">可上传最多5张图片，支持jpg/png格式，单张不超过5MB</div>
        </el-form-item>

        <el-form-item label="紧急程度">
          <el-radio-group v-model="form.urgency">
            <el-radio value="NORMAL">普通</el-radio>
            <el-radio value="URGENT">紧急</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="楼栋" prop="building" required>
          <el-select v-model="form.building" placeholder="请选择楼栋" style="width: 100%">
            <el-option v-for="building in buildings" :key="building.id" :label="building.name" :value="building.name" />
          </el-select>
        </el-form-item>

        <el-form-item label="宿舍号" prop="dormNumber" required>
          <el-input 
            v-model="form.dormNumber" 
            placeholder="请输入宿舍号，如：101"
            maxlength="20"
            show-word-limit
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="submitting">
            提交报修
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import axios from '../../utils/axios'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const submitting = ref(false)

const form = reactive({
  faultTypeId: '',
  description: '',
  images: [],
  urgency: 'NORMAL',
  building: '',
  dormNumber: ''
})

const imageFiles = ref([])
const faultTypes = ref([])
const buildings = ref([])

// 上传配置
const uploadUrl = '/api/student/upload'
const uploadHeaders = {
  Authorization: `Bearer ${localStorage.getItem('token')}`
}

// 表单验证规则
const rules = {
  faultTypeId: [
    { required: true, message: '请选择故障类型', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入故障描述', trigger: 'blur' },
    { min: 5, max: 200, message: '故障描述长度在 5 到 200 个字符', trigger: 'blur' }
  ],
  building: [
    { required: true, message: '请选择楼栋', trigger: 'change' }
  ],
  dormNumber: [
    { required: true, message: '请输入宿舍号', trigger: 'blur' },
    { min: 1, max: 20, message: '宿舍号长度在 1 到 20 个字符', trigger: 'blur' }
  ]
}

// 上传前验证
const beforeUpload = (file) => {
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png'
  if (!isImage) {
    ElMessage.error('只能上传 jpg/png 格式的图片')
    return false
  }
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过5MB')
    return false
  }
  return true
}

// 上传成功回调
const handleUploadSuccess = (response, file) => {
  if (response.code === 200) {
    form.images.push(response.data.url)
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

// 移除图片
const handleUploadRemove = (file) => {
  const url = file.response?.data?.url
  if (url) {
    const index = form.images.indexOf(url)
    if (index > -1) {
      form.images.splice(index, 1)
    }
  }
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      await axios.post('/api/student/orders', {
        faultTypeId: form.faultTypeId,
        description: form.description,
        images: form.images,
        urgency: form.urgency,
        building: form.building,
        dormNumber: form.dormNumber
      })

      ElMessage.success('报修提交成功')
      router.push('/student/history')
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '提交失败')
    } finally {
      submitting.value = false
    }
  })
}

// 重置表单
const resetForm = () => {
  form.faultTypeId = ''
  form.description = ''
  form.images = []
  form.urgency = 'NORMAL'
  form.building = userStore.user?.building || ''
  form.dormNumber = userStore.user?.dormNumber || ''
  imageFiles.value = []
  formRef.value?.clearValidate()
}

// 加载故障类型（去重）
const loadFaultTypes = async () => {
  try {
    const response = await axios.get('/api/student/fault-types')
    const data = response.data || []
    // 使用 Map 去重，保持原始顺序
    const uniqueData = [...new Map(data.map(item => [item.id, item])).values()]
    faultTypes.value = uniqueData
  } catch (error) {
    console.error('加载故障类型失败', error)
    ElMessage.error('加载故障类型失败')
  }
}

// 加载楼栋列表（去重）
const loadBuildings = async () => {
  try {
    const response = await axios.get('/api/student/buildings')
    const data = response.data || []
    // 使用 Map 去重，保持原始顺序
    const uniqueData = [...new Map(data.map(item => [item.id, item])).values()]
    buildings.value = uniqueData
  } catch (error) {
    console.error('加载楼栋失败', error)
  }
}

onMounted(() => {
  loadFaultTypes()
  loadBuildings()
  // 从用户信息中获取默认值
  form.building = userStore.user?.building || ''
  form.dormNumber = userStore.user?.dormNumber || ''
})
</script>

<style scoped>
.repair-form {
  max-width: 700px;
  margin: 0 auto;
  padding: 20px;
}

.upload-area {
  width: 100%;
}

.upload-tip {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

:deep(.el-upload--picture-card) {
  width: 100px;
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 100px;
  height: 100px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .repair-form {
    padding: 10px;
  }

  :deep(.el-form-item__label) {
    width: 80px !important;
  }
}
</style>