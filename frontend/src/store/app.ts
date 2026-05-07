import { defineStore } from 'pinia'

interface AppState {
  sidebarCollapsed: boolean
  currentSubject: string
}

export const useAppStore = defineStore('app', {
  state: (): AppState => ({
    sidebarCollapsed: false,
    currentSubject: 'MATH'
  }),

  actions: {
    toggleSidebar() {
      this.sidebarCollapsed = !this.sidebarCollapsed
    },

    setSubject(subject: string) {
      this.currentSubject = subject
    }
  }
})