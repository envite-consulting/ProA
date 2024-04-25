// Utilities
import { defineStore } from 'pinia'

interface PaperLayout {
  sx: number,
  tx: number,
  ty: number
}

export const useAppStore = defineStore('app', {
  state: () => {
    return {
      selectedProjectId: null as number | null,
      graphByProject: {} as { [key: number]: string }, // new Map<number, string>() // real map not working with persist plugin
      paperLayoutByProject: {} as { [key: number]: PaperLayout }
    }
  },
  actions: {
    setGraphForProject(id: number, graph: string) {
      this.graphByProject[id] = graph;
    },
    getGraphForProject(id: number): string {
      return this.graphByProject[id];
    },
    setPaperLayoutForProject(id: number, paperLayout: PaperLayout) {
      this.paperLayoutByProject[id] = paperLayout;
    },
    getPaperLayoutForProject(id: number): PaperLayout {
      return this.paperLayoutByProject[id];
    }
  },
  persist: {
    storage: sessionStorage,
    paths: ['selectedProjectId', 'graphByProject', 'paperLayoutByProject'],
  },
})
