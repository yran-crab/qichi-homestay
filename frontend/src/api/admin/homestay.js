import request from '@/utils/request'

export const adminHomestayPage = (params) => request.get('/admin/homestays', { params })
export const auditHomestayPage = (params) => request.get('/admin/homestays/audits', { params })
export const auditHomestay = (id, data) => request.put(`/admin/homestays/${id}/audit`, data)
export const forceOfflineHomestay = (id) => request.put(`/admin/homestays/${id}/offline`)
