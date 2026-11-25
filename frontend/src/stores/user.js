import { defineStore } from 'pinia'
import request from '@/utils/request'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('bnbms_token') || '',
    userInfo: null
  }),
  getters: {
    isLogin: (state) => !!state.token,
    role: (state) => state.userInfo?.role || '',
    userId: (state) => state.userInfo?.id || null
  },
  actions: {
    setToken(token) {
      this.token = token
      localStorage.setItem('bnbms_token', token)
    },
    async login(payload) {
      const data = await request.post('/auth/login', payload)
      this.setToken(data.token)
      this.userInfo = data.userInfo
      return data
    },
    async fetchProfile() {
      if (!this.token) return null
      const data = await request.get('/auth/profile')
      this.userInfo = data
      return data
    },
    logout() {
      request.post('/auth/logout').catch(() => {})
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('bnbms_token')
    }
  }
})
