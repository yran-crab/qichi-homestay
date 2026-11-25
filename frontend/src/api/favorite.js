import request from '@/utils/request'

/** 收藏 / 取消收藏，返回 true=已收藏，false=已取消 */
export const toggleFavorite = (homestayId) =>
  request.post(`/favorites/${homestayId}/toggle`)

/** 我的收藏分页 */
export const myFavorites = (params) => request.get('/favorites/my', { params })
