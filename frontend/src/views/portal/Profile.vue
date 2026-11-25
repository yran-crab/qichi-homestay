<template>
  <div class="page-container">
    <div class="profile-card card-shadow">
      <el-tabs v-model="activeTab" class="profile-tabs">
        <!-- 基本资料 -->
        <el-tab-pane label="基本资料" name="info">
          <el-form :model="profileForm" label-width="90px" class="profile-form">
            <el-form-item label="头像">
              <ImageUpload v-model="profileForm.avatar" />
            </el-form-item>
            <el-form-item label="用户名">
              <el-input :model-value="profileForm.username" disabled class="readonly-input" />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="profileForm.realName" maxlength="20" show-word-limit />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="profileForm.phone" maxlength="11" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="profileForm.email" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="saveProfile">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 修改密码 -->
        <el-tab-pane label="修改密码" name="password">
          <el-form ref="pwdRef" :model="pwdForm" :rules="pwdRules" label-width="90px" class="profile-form">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="6-32 位" />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="savePassword">确认修改</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 房东入驻 -->
        <el-tab-pane label="房东入驻" name="host">
          <div class="host-apply">
            <el-result
              :icon="hostResultIcon"
              :title="hostResultTitle"
              :sub-title="hostResultSub">
              <template #extra>
                <el-button v-if="canApply" type="primary" :loading="applying" @click="onApplyHost">
                  {{ profileForm.hostStatus === 'REJECTED' ? '重新提交申请' : '申请成为房东' }}
                </el-button>
                <el-button v-if="profileForm.hostStatus === 'APPROVED' || profileForm.role === 'HOST'"
                  type="primary" @click="$router.push('/host')">
                  进入房东中心
                </el-button>
              </template>
            </el-result>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import ImageUpload from '@/components/ImageUpload.vue'
import { getProfile, updateProfile, changePassword, applyHost } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const userStore = useUserStore()
const activeTab = ref(route.query.tab || 'info')
const saving = ref(false)
const applying = ref(false)

const profileForm = reactive({
  username: '', realName: '', phone: '', email: '', avatar: '',
  role: 'TENANT', hostStatus: 'NONE'
})

const pwdRef = ref()
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度为 6-32 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, cb) =>
        value !== pwdForm.newPassword ? cb(new Error('两次输入的密码不一致')) : cb(),
      trigger: 'blur'
    }
  ]
}

const canApply = computed(() =>
  profileForm.role !== 'HOST' &&
  ['NONE', 'REJECTED'].includes(profileForm.hostStatus))

const hostResultIcon = computed(() => {
  if (profileForm.role === 'HOST' || profileForm.hostStatus === 'APPROVED') return 'success'
  if (profileForm.hostStatus === 'PENDING') return 'warning'
  if (profileForm.hostStatus === 'REJECTED') return 'error'
  return 'info'
})
const hostResultTitle = computed(() => {
  if (profileForm.role === 'HOST' || profileForm.hostStatus === 'APPROVED') return '您已是认证房东'
  if (profileForm.hostStatus === 'PENDING') return '入驻申请审核中'
  if (profileForm.hostStatus === 'REJECTED') return '上次申请未通过审核'
  return '成为房东，发布您的房源'
})
const hostResultSub = computed(() => {
  if (profileForm.role === 'HOST' || profileForm.hostStatus === 'APPROVED') return '前往房东中心发布房源、接待旅客吧'
  if (profileForm.hostStatus === 'PENDING') return '平台将在 1-3 个工作日内完成审核，请耐心等待'
  if (profileForm.hostStatus === 'REJECTED') return '您可以补充资料后重新提交申请'
  return '认证通过后即可发布房源并在平台接单'
})

async function loadProfile() {
  const data = await getProfile()
  Object.assign(profileForm, data)
}

async function saveProfile() {
  saving.value = true
  try {
    await updateProfile({
      realName: profileForm.realName,
      phone: profileForm.phone,
      email: profileForm.email,
      avatar: profileForm.avatar
    })
    await userStore.fetchProfile()
    ElMessage.success('资料已更新')
  } finally {
    saving.value = false
  }
}

async function savePassword() {
  await pwdRef.value.validate()
  saving.value = true
  try {
    await changePassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
    ElMessage.success('密码修改成功，请重新登录')
    setTimeout(() => {
      userStore.logout()
      location.href = '/login'
    }, 1200)
  } finally {
    saving.value = false
  }
}

async function onApplyHost() {
  applying.value = true
  try {
    await applyHost()
    ElMessage.success('申请已提交，请等待管理员审核')
    profileForm.hostStatus = 'PENDING'
  } finally {
    applying.value = false
  }
}

onMounted(loadProfile)
</script>

<style scoped>
.profile-card {
  padding: 8px 28px 28px;
}
.profile-form {
  max-width: 520px;
  margin-top: 10px;
}
.readonly-input {
  width: 280px;
}
.host-apply {
  padding: 20px 0;
}
</style>
