import request from '@/utils/request'

/** 管理员看板数据 */
export const getAdminDashboard = () => request.get('/admin/dashboard')

/** 房东看板数据 */
export const getHostDashboard = () => request.get('/host/dashboard')
