// Utilities
import { defineStore } from 'pinia'

export const useAppStore = defineStore('app', {
  state: () => {
    return {
      selectedProjectId: null as number | null,
      graphByProject: {} as { [key: number]: string } // new Map<number, string>() // real map not working with persist plugin
    }
  },
  actions: {
    setGraphForProject(id: number, graph: string) {
      this.graphByProject[id] = graph;
    },
  },
  persist: {
    storage: sessionStorage,
    paths: ['selectedProjectId', 'graphByProject'],
  },
})
