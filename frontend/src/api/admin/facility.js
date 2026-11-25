import request from '@/utils/request'

export const facilityAdminList = () => request.get('/admin/facilities')
export const createFacility = (data) => request.post('/admin/facilities', data)
export const updateFacility = (id, data) => request.put(`/admin/facilities/${id}`, data)
export const deleteFacility = (id) => request.delete(`/admin/facilities/${id}`)
