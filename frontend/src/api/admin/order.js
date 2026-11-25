import request from '@/utils/request'

export const adminOrders = (params) => request.get('/admin/orders', { params })
export const adminOrderDetail = (id) => request.get(`/admin/orders/${id}`)
