import request from '@/utils/request'

export const hostHomestayPage = (params) => request.get('/host/homestays', { params })
export const getHostHomestay = (id) => request.get(`/host/homestays/${id}`)
export const createHomestay = (data) => request.post('/host/homestays', data)
export const updateHomestay = (id, data) => request.put(`/host/homestays/${id}`, data)
export const submitHomestay = (id) => request.put(`/host/homestays/${id}/submit`)
export const offlineHomestay = (id) => request.put(`/host/homestays/${id}/offline`)
export const onlineHomestay = (id) => request.put(`/host/homestays/${id}/online`)
export const deleteHomestay = (id) => request.delete(`/host/homestays/${id}`)
