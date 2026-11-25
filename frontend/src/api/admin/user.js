import request from '@/utils/request'

export const userPage = (params) => request.get('/admin/users', { params })
export const updateUserStatus = (id, status) =>
  request.put(`/admin/users/${id}/status`, null, { params: { status } })
export const hostApplyPage = (params) => request.get('/admin/host-applies', { params })
export const auditHost = (id, approved) =>
  request.put(`/admin/host-applies/${id}/audit`, null, { params: { approved } })
