import request from '@/utils/request'

/* ---------- 租客端 ---------- */

/** 领券中心 */
export const couponCenter = () => request.get('/coupons/center')
/** 领取优惠券 */
export const receiveCoupon = (id) => request.post(`/coupons/${id}/receive`)
/** 我的优惠券 status: UNUSED/USED/EXPIRED */
export const myCoupons = (status) => request.get('/coupons/mine', { params: { status } })
/** 预订页可用券试算 */
export const usableCoupons = (params) => request.get('/coupons/usable', { params })

/* ---------- 管理端 ---------- */

export const adminCouponPage = (params) => request.get('/admin/coupons', { params })
export const createCoupon = (data) => request.post('/admin/coupons', data)
export const updateCoupon = (id, data) => request.put(`/admin/coupons/${id}`, data)
export const updateCouponStatus = (id, status) =>
  request.put(`/admin/coupons/${id}/status`, { status })
export const deleteCoupon = (id) => request.delete(`/admin/coupons/${id}`)
