import request from '@/utils/request'

export const login = (data) => request.post('/auth/login', data)
export const registerApi = (data) => request.post('/auth/register', data)
export const logout = () => request.post('/auth/logout')
export const getProfile = () => request.get('/auth/profile')
export const updateProfile = (data) => request.put('/auth/profile', data)
export const changePassword = (data) => request.put('/auth/password', data)
export const applyHost = () => request.post('/auth/host-apply')
