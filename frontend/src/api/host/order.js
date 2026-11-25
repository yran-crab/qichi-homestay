import request from '@/utils/request'

export const hostOrders = (params) => request.get('/host/orders', { params })
export const hostOrderDetail = (id) => request.get(`/host/orders/${id}`)
export const confirmCheckIn = (id) => request.put(`/host/orders/${id}/check-in`)
export const confirmCheckOut = (id) => request.put(`/host/orders/${id}/check-out`)
