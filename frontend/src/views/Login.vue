<template>
  <div class="auth-page">
    <div class="auth-card card-shadow">
      <div class="auth-brand">
        <img src="/favicon.svg" alt="logo" />
        <h2>栖迟民宿</h2>
        <p>欢迎登录，开启美好旅程</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" size="large" @submit.prevent="onSubmit">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名 / 手机号" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="密码"
            :prefix-icon="Lock" @keyup.enter="onSubmit" />
        </el-form-item>
        <el-form-item prop="captcha">
          <div class="captcha-row">
            <el-input v-model="form.captcha" placeholder="图形验证码" :prefix-icon="Key" />
            <img class="captcha-img" :src="captchaUrl" title="点击刷新" @click="refreshCaptcha" alt="captcha" />
          </div>
        </el-form-item>
        <el-button type="primary" class="submit-btn" :loading="loading" @click="onSubmit">登 录</el-button>
        <div class="auth-tip">
          还没有账号？<router-link to="/register">立即注册</router-link>
        </div>
        <el-divider>演示账号（密码均为 123456）</el-divider>
        <div class="demo-accounts">
          <el-tag v-for="acc in demoAccounts" :key="acc.username" class="demo-tag" :type="acc.type"
            @click="fillAccount(acc.username)">
            {{ acc.label }}：{{ acc.username }}
          </el-tag>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Key } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)
const captchaKey = ref('')
const captchaUrl = ref('')

const form = reactive({ username: '', password: '', captcha: '', captchaKey: '' })

const rules = {
  username: [{ required: true, message: '请输入用户名或手机号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captcha: [{ required: true, message: '请输入图形验证码', trigger: 'blur' }]
}

const demoAccounts = [
  { username: 'admin', label: '管理员', type: 'danger' },
  { username: 'host1', label: '房东', type: 'warning' },
  { username: 'tenant1', label: '租客', type: 'success' }
]

function refreshCaptcha() {
  captchaKey.value = Date.now() + '-' + Math.random().toString(36).slice(2)
  form.captchaKey = captchaKey.value
  captchaUrl.value = `/api/auth/captcha?key=${captchaKey.value}`
}

function fillAccount(username) {
  form.username = username
  form.password = '123456'
}

async function onSubmit() {
  await formRef.value.validate()
  loading.value = true
  try {
    const data = await userStore.login(form)
    ElMessage.success('登录成功')
    const role = data.userInfo.role
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : ''
    // redirect 必须与当前角色兼容，避免跨角色登录后进入 403
    const redirectOk =
      redirect &&
      ((role === 'ADMIN' && redirect.startsWith('/admin')) ||
        (role === 'HOST' && redirect.startsWith('/host')) ||
        (role === 'TENANT' && !redirect.startsWith('/admin') && !redirect.startsWith('/host')))
    if (redirectOk) {
      router.push(redirect)
    } else if (role === 'ADMIN') {
      router.push('/admin')
    } else if (role === 'HOST') {
      router.push('/host')
    } else {
      router.push('/')
    }
  } catch (e) {
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}

onMounted(refreshCaptcha)
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
.captcha-row {
  display: flex;
  gap: 10px;
  width: 100%;
}
.captcha-img {
  width: 120px;
  height: 40px;
  border-radius: 6px;
  cursor: pointer;
  border: 1px solid var(--el-border-color);
  background: #f5f7fa;
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
.demo-accounts {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}
.demo-tag {
  cursor: pointer;
}
</style>
