import request from '@/utils/request'

/** 公开公告列表 */
export const noticeList = () => request.get('/notices')
/** 公开公告详情 */
export const noticeDetail = (id) => request.get(`/notices/${id}`)

/** 管理端公告列表 */
export const adminNoticeList = () => request.get('/admin/notices')
export const createNotice = (data) => request.post('/admin/notices', data)
export const updateNotice = (id, data) => request.put(`/admin/notices/${id}`, data)
export const deleteNotice = (id) => request.delete(`/admin/notices/${id}`)
