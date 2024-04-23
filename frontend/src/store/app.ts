// Utilities
import { defineStore } from 'pinia'

export const useAppStore = defineStore('app', {
  state: () => ({
    selectedProjectId: null as number | null,
  }),
})
