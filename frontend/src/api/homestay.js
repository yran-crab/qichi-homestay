import request from '@/utils/request'

export const homestayPage = (params) => request.get('/homestays', { params })
export const hotHomestays = (limit = 8) => request.get('/homestays/hot', { params: { limit } })
export const homestayDetail = (id) => request.get(`/homestays/${id}`)
export const facilityList = () => request.get('/facilities')
