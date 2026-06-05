import axios from 'axios'
import type { AxiosResponse } from 'axios'

// AI 模型配置相关 API

/**
 * 获取供应商预设
 */
export const getProviderPresets = (): Promise<AxiosResponse<any>> => {
  return axios.get('/api/ai/configs/presets')
}

/**
 * 获取配置列表
 * @param tenantId 租户ID，默认0
 */
export const listModelConfigs = (tenantId: number = 0): Promise<AxiosResponse<any>> => {
  return axios.get('/api/ai/configs', { params: { tenantId } })
}

/**
 * 保存配置
 * @param data 配置数据
 */
export const saveModelConfig = (data: any): Promise<AxiosResponse<any>> => {
  return axios.post('/api/ai/configs', data)
}

/**
 * 更新配置
 * @param id 配置ID
 * @param data 配置数据
 */
export const updateModelConfig = (id: number, data: any): Promise<AxiosResponse<any>> => {
  return axios.put(`/api/ai/configs/${id}`, data)
}

/**
 * 删除配置
 * @param id 配置ID
 * @param tenantId 租户ID
 */
export const deleteModelConfig = (id: number, tenantId: number = 0): Promise<AxiosResponse<any>> => {
  return axios.delete(`/api/ai/configs/${id}`, { params: { tenantId } })
}

/**
 * 设为默认配置
 * @param id 配置ID
 * @param tenantId 租户ID
 */
export const setDefaultConfig = (id: number, tenantId: number = 0): Promise<AxiosResponse<any>> => {
  return axios.post(`/api/ai/configs/${id}/set-default`, null, { params: { tenantId } })
}

/**
 * 测试连接
 * @param data 配置数据（不保存，直接测试）
 */
export const testConnection = (data: any): Promise<AxiosResponse<any>> => {
  return axios.post('/api/ai/configs/test', data)
}
