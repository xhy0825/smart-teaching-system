import request from '@/utils/request'
import type { AxiosResponse } from 'axios'

// AI 模型配置相关 API

/**
 * 获取供应商预设
 */
export const getProviderPresets = (): Promise<AxiosResponse<any>> => {
  return request.get('/api/ai/configs/presets')
}

/**
 * 获取配置列表
 * 租户ID从JWT token自动获取，无需传参
 */
export const listModelConfigs = (): Promise<AxiosResponse<any>> => {
  return request.get('/api/ai/configs')
}

/**
 * 保存配置
 * @param data 配置数据
 */
export const saveModelConfig = (data: any): Promise<AxiosResponse<any>> => {
  return request.post('/api/ai/configs', data)
}

/**
 * 更新配置
 * @param id 配置ID
 * @param data 配置数据
 */
export const updateModelConfig = (id: number, data: any): Promise<AxiosResponse<any>> => {
  return request.put(`/api/ai/configs/${id}`, data)
}

/**
 * 删除配置
 * @param id 配置ID
 * 租户ID从JWT token自动获取，无需传参
 */
export const deleteModelConfig = (id: number): Promise<AxiosResponse<any>> => {
  return request.delete(`/api/ai/configs/${id}`)
}

/**
 * 设为默认配置
 * @param id 配置ID
 * 租户ID从JWT token自动获取，无需传参
 */
export const setDefaultConfig = (id: number): Promise<AxiosResponse<any>> => {
  return request.post(`/api/ai/configs/${id}/set-default`)
}

/**
 * 测试连接
 * @param data 配置数据（不保存，直接测试）
 */
export const testConnection = (data: any): Promise<AxiosResponse<any>> => {
  return request.post('/api/ai/configs/test', data)
}
