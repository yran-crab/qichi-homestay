import request from '@/utils/request'

/** 首页聚合数据 */
export const getHome = () => request.get('/home')

/** 个性化推荐（基于收藏/订单的协同过滤，未登录返回热门） */
export const getRecommend = (limit = 8) => request.get('/homestays/recommend', { params: { limit } })
