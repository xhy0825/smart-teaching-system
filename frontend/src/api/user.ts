import request from '@/utils/request'

// 登录
export function login(data: { username: string; password: string }) {
  return request.post('/auth/login', data, {
    headers: {
      'X-Tenant-Id': '1'
    }
  })
}

// 注册
export function register(data: { username: string; password: string; realName: string }) {
  return request.post('/auth/register', data)
}

// 获取当前用户信息
export function getCurrentUser() {
  return request.get('/user/me')
}

// 获取用户列表
export function getUserList() {
  return request.get('/user/list')
}