import request from '@/utils/request'

/** 发表评价 */
export const createReview = (data) => request.post('/reviews', data)
/** 我的评价分页 */
export const myReviews = (params) => request.get('/reviews/my', { params })
/** 房源详情页公开评价分页 */
export const homestayReviews = (homestayId, params) =>
  request.get(`/homestays/${homestayId}/reviews`, { params })
/** 房东评价分页（本人房源） */
export const hostReviews = (params) =>
  request.get('/host/reviews', { params })
/** 房东回复评价 */
export const replyReview = (id, content) =>
  request.put(`/host/reviews/${id}/reply`, { content })
/** 管理员评价分页 */
export const adminReviews = (params) =>
  request.get('/admin/reviews', { params })
/** 管理员删除评价 */
export const deleteReview = (id) => request.delete(`/admin/reviews/${id}`)
