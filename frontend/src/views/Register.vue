<template>
  <div class="auth-page">
    <div class="auth-card card-shadow">
      <div class="auth-brand">
        <img src="/favicon.svg" alt="logo" />
        <h2>注册栖迟账号</h2>
        <p>注册后即可预订心仪民宿</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" size="large">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名（4-20 位）" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="phone">
          <el-input v-model="form.phone" placeholder="手机号" :prefix-icon="Iphone" />
        </el-form-item>
        <el-form-item prop="realName">
          <el-input v-model="form.realName" placeholder="昵称 / 姓名" :prefix-icon="Avatar" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="设置密码（至少 6 位）"
            :prefix-icon="Lock" />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" show-password placeholder="确认密码"
            :prefix-icon="Lock" />
        </el-form-item>
        <el-button type="primary" class="submit-btn" :loading="loading" @click="onSubmit">注 册</el-button>
        <div class="auth-tip">
          已有账号？<router-link to="/login">返回登录</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Iphone, Avatar } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

const form = reactive({ username: '', phone: '', realName: '', password: '', confirmPassword: '' })

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 20, message: '长度为 4-20 个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  realName: [{ required: true, message: '请输入昵称或姓名', trigger: 'blur' }],
  password: [
    { required: true, message: '请设置密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== form.password) callback(new Error('两次输入的密码不一致'))
        else callback()
      },
      trigger: 'blur'
    }
  ]
}

async function onSubmit() {
  await formRef.value.validate()
  loading.value = true
  try {
    await request.post('/auth/register', {
      username: form.username,
      phone: form.phone,
      realName: form.realName,
      password: form.password
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e6f5f3 0%, #f6f8fa 60%);
  padding: 20px;
}
.auth-card {
  width: 420px;
  padding: 36px 40px 28px;
  border-radius: 14px;
}
.auth-brand {
  text-align: center;
  margin-bottom: 24px;
}
.auth-brand img {
  width: 48px;
  height: 48px;
}
.auth-brand h2 {
  margin: 10px 0 4px;
  color: var(--bnb-primary);
}
.auth-brand p {
  margin: 0;
  color: var(--bnb-text-secondary);
  font-size: 13px;
}
.submit-btn {
  width: 100%;
  margin-top: 4px;
}
.auth-tip {
  text-align: center;
  margin-top: 14px;
  font-size: 14px;
  color: var(--bnb-text-secondary);
}
.auth-tip a {
  color: var(--bnb-primary);
}
</style>
