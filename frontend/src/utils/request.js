import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const service = axios.create({
  baseURL: '/api',
  timeout: 15000
})

service.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('bnbms_token')
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  (error) => Promise.reject(error)
)

service.interceptors.response.use(
  (response) => {
    const res = response.data
    // 文件流等非标准响应直接返回
    if (response.config.responseType === 'blob') {
      return response
    }
    if (res.code === 200) {
      return res.data
    }
    if (res.code === 401) {
      handleUnauthorized(res.message)
      return Promise.reject(new Error(res.message || '未登录'))
    }
    if (res.code === 403) {
      ElMessage.error(res.message || '没有访问权限')
      return Promise.reject(new Error(res.message || '没有访问权限'))
    }
    ElMessage.error(res.message || '操作失败')
    return Promise.reject(new Error(res.message || '操作失败'))
  },
  (error) => {
    const status = error.response && error.response.status
    if (status === 401) {
      handleUnauthorized()
    } else if (status === 403) {
      ElMessage.error('没有访问权限')
    } else {
      ElMessage.error(error.response?.data?.message || '网络异常，请稍后再试')
    }
    return Promise.reject(error)
  }
)

let unauthorizedRedirected = false
function handleUnauthorized() {
  localStorage.removeItem('bnbms_token')
  ElMessage.error('登录状态已过期，请重新登录')
  if (!unauthorizedRedirected) {
    unauthorizedRedirected = true
    const current = router.currentRoute.value.fullPath
    router.replace({ path: '/login', query: { redirect: current } }).finally(() => {
      setTimeout(() => (unauthorizedRedirected = false), 1000)
    })
  }
}

export default service
