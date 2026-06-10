<template>
  <div class="login-container">
    <div class="login-box">
      <h2>宿舍报修管理系统</h2>
      
      <el-tabs v-model="activeTab" type="border-card">
        <el-tab-pane label="登录" name="login">
          <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" label-width="80px">
            <el-form-item label="账号" prop="username">
              <el-input 
                v-model="loginForm.username" 
                placeholder="请输入学号/工号" 
                :disabled="false"
                :readonly="false"
                @input="console.log('登录账号输入:', loginForm.username)"
              />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input 
                v-model="loginForm.password" 
                type="password" 
                placeholder="请输入密码" 
                :disabled="false"
                :readonly="false"
                @input="console.log('登录密码输入')"
              />
            </el-form-item>
            <el-form-item>
              <el-button 
                type="primary" 
                @click="handleLogin" 
                class="login-btn"
                :disabled="false"
                style="background: #409EFF; font-size: 16px; height: 45px;"
              >
                登录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="注册" name="register">
          <!-- 用户类型选择 -->
          <div class="user-type-selector">
            <el-radio-group v-model="registerForm.userType" @change="handleUserTypeChange">
              <el-radio value="STUDENT">学生注册</el-radio>
              <el-radio value="REPAIR">修理工注册</el-radio>
            </el-radio-group>
          </div>

          <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" label-width="80px">
            <!-- 学生注册字段 -->
            <template v-if="registerForm.userType === 'STUDENT'">
              <el-form-item label="学号" prop="username">
                <el-input 
                  v-model="registerForm.username" 
                  placeholder="请输入学号" 
                  :disabled="false"
                  :readonly="false"
                  maxlength="20"
                  show-word-limit
                  @input="console.log('注册学号输入:', registerForm.username)"
                />
              </el-form-item>
              <el-form-item label="楼栋" prop="building">
                <el-select 
                  v-model="registerForm.building" 
                  placeholder="请选择楼栋" 
                  :disabled="false"
                  @change="console.log('楼栋选择:', registerForm.building)"
                >
                  <el-option v-for="building in buildings" :key="building.id" :label="building.name" :value="building.name" />
                </el-select>
              </el-form-item>
              <el-form-item label="宿舍号" prop="dormNumber">
                <el-input 
                  v-model="registerForm.dormNumber" 
                  placeholder="请输入宿舍号" 
                  :disabled="false"
                  :readonly="false"
                  maxlength="20"
                  show-word-limit
                  @input="console.log('注册宿舍号输入:', registerForm.dormNumber)"
                />
              </el-form-item>
            </template>

            <!-- 修理工注册字段 -->
            <template v-else>
              <el-form-item label="工号">
                <el-input 
                  :value="generatedRepairmanCode || '系统将自动生成'"
                  disabled
                  placeholder="系统将自动生成"
                />
              </el-form-item>
              <el-form-item label="擅长类型">
                <el-select 
                  v-model="registerForm.specialtyIds" 
                  multiple
                  placeholder="请选择擅长修理类型（可多选）" 
                  style="width: 100%"
                >
                  <el-option v-for="type in faultTypes" :key="type.id" :label="type.name" :value="type.id" />
                </el-select>
              </el-form-item>
            </template>

            <!-- 通用字段 -->
            <el-form-item label="姓名" prop="realName">
              <el-input 
                v-model="registerForm.realName" 
                placeholder="请输入姓名" 
                :disabled="false"
                :readonly="false"
                maxlength="50"
                show-word-limit
                @input="console.log('注册姓名输入:', registerForm.realName)"
              />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input 
                v-model="registerForm.phone" 
                placeholder="请输入手机号" 
                :disabled="false"
                :readonly="false"
                maxlength="20"
                show-word-limit
                @input="console.log('注册手机号输入:', registerForm.phone)"
              />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input 
                v-model="registerForm.password" 
                type="password" 
                placeholder="请输入密码" 
                :disabled="false"
                :readonly="false"
                maxlength="50"
                show-word-limit
                @input="console.log('注册密码输入')"
              />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input 
                v-model="registerForm.confirmPassword" 
                type="password" 
                placeholder="请确认密码" 
                :disabled="false"
                :readonly="false"
                maxlength="50"
                show-word-limit
                @input="console.log('注册确认密码输入')"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleRegister" class="login-btn">注册</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import axios from '../utils/axios'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('login')
const buildings = ref([])
const faultTypes = ref([])
const loginFormRef = ref(null)
const registerFormRef = ref(null)
const generatedRepairmanCode = ref('')

const loginForm = ref({
  username: '',
  password: ''
})

const registerForm = ref({
  userType: 'STUDENT',
  username: '',
  realName: '',
  phone: '',
  building: '',
  dormNumber: '',
  specialtyIds: [],
  password: '',
  confirmPassword: ''
})

// 登录表单验证规则
const loginRules = {
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 2, max: 20, message: '账号长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 50, message: '密码长度在 6 到 50 个字符', trigger: 'blur' }
  ]
}

// 注册表单验证规则
const registerRules = {
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
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 50, message: '密码长度在 6 到 50 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.value.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    console.log('登录按钮被点击了!')
    console.log('登录表单数据:', loginForm.value)
    
    try {
      console.log('开始调用 userStore.login...')
      await userStore.login(loginForm.value.username, loginForm.value.password)
      console.log('登录成功，用户信息:', userStore.user)
      
      const role = userStore.user.role
      if (role === 'STUDENT') {
        router.push('/student/dashboard')
      } else if (role === 'REPAIR') {
        router.push('/repairman/pending')
      } else if (role === 'ADMIN') {
        router.push('/admin/statistics')
      }
    } catch (error) {
      console.error('登录出错:', error)
      ElMessage.error(error.response?.data?.message || '登录失败')
    }
  })
}

const handleUserTypeChange = () => {
  // 清空表单字段
  registerForm.value.username = ''
  registerForm.value.realName = ''
  registerForm.value.phone = ''
  registerForm.value.building = ''
  registerForm.value.dormNumber = ''
  registerForm.value.specialtyIds = []
  registerForm.value.password = ''
  registerForm.value.confirmPassword = ''
  generatedRepairmanCode.value = ''
  
  // 清除验证状态
  registerFormRef.value?.clearValidate()
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  await registerFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    // 修理工注册时不需要填写工号（自动生成）
    if (registerForm.value.userType === 'REPAIR') {
      registerForm.value.username = ''
    }
    
    try {
      const response = await axios.post('/api/auth/register', {
        username: registerForm.value.username,
        password: registerForm.value.password,
        realName: registerForm.value.realName,
        phone: registerForm.value.phone,
        building: registerForm.value.building,
        dormNumber: registerForm.value.dormNumber,
        specialtyIds: registerForm.value.specialtyIds.length > 0 ? registerForm.value.specialtyIds.join(',') : null,
        role: registerForm.value.userType
      })
      
      // 修理工注册成功后显示生成的工号
      if (registerForm.value.userType === 'REPAIR') {
        generatedRepairmanCode.value = response.data.username
        ElMessage.success(`注册成功！您的工号是：${response.data.username}，请使用工号登录`)
      } else {
        ElMessage.success('注册成功，请登录')
      }
      
      // 清空表单
      handleUserTypeChange()
      registerForm.value.userType = 'STUDENT'
      activeTab.value = 'login'
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '注册失败')
    }
  })
}

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

const loadFaultTypes = async () => {
  try {
    const response = await axios.get('/api/student/fault-types')
    faultTypes.value = response.data || []
  } catch (error) {
    console.error('加载故障类型失败', error)
  }
}

onMounted(() => {
  loadBuildings()
  loadFaultTypes()
})
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  z-index: 1;
}

.login-box {
  width: 450px;
  background: white;
  padding: 30px;
  border-radius: 10px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 10;
}

.login-box h2 {
  text-align: center;
  margin-bottom: 20px;
  color: #333;
}

.login-btn {
  width: 100%;
}

.user-type-selector {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
}

.user-type-selector :deep(.el-radio-group) {
  width: 100%;
  display: flex;
  justify-content: space-around;
}

:deep(.el-input.is-disabled .el-input__wrapper) {
  background-color: #f5f7fa;
  cursor: not-allowed;
}

:deep(.el-input.is-disabled .el-input__inner) {
  color: #909399;
}
</style>