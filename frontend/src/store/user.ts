import { defineStore } from 'pinia'
import { login, getCurrentUser } from '@/api/user'

interface User {
  id: number
  username: string
  realName: string
  tenantId: number
  roles: string[]
}

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: null as User | null
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    username: (state) => state.user?.username || '',
    realName: (state) => state.user?.realName || ''
  },

  actions: {
    async loginAction(username: string, password: string) {
      const res: any = await login({ username, password })
      this.token = res.data.token
      localStorage.setItem('token', res.data.token)
      await this.getUserInfo()
      return res
    },

    async getUserInfo() {
      const res: any = await getCurrentUser()
      this.user = res.data
    },

    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('token')
    }
  }
})