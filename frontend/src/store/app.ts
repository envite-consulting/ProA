// Utilities
import { defineStore } from 'pinia'

export const useAppStore = defineStore('app', {
  state: () => {
    return {
      selectedProjectId: null as number | null,
      graphByProject: {} as { [key: number]: string }, // new Map<number, string>() // real map not working with persist plugin
      paperLayoutByProject: {} as { [key: number]: string },
      filtersByProject: {} as { [key: number]: string },
      hiddenCellsByProject: {} as { [key: number]: string },
      hiddenPortsByProject: {} as { [key: number]: string },
    }
  },
  actions: {
    setGraphForProject(id: number, graph: string) {
      this.graphByProject[id] = graph;
    },
    getGraphForProject(id: number): string {
      return this.graphByProject[id];
    },
    setPaperLayoutForProject(id: number, string: string) {
      this.paperLayoutByProject[id] = string;
    },
    getPaperLayoutForProject(id: number): string {
      return this.paperLayoutByProject[id];
    },
    setFiltersForProject(id: number, filters: string) {
      this.filtersByProject[id] = filters;
    },
    getFiltersForProject(id: number): string {
      return this.filtersByProject[id];
    },
    setHiddenCellsForProject(id: number, hiddenCells: string) {
      this.hiddenCellsByProject[id] = hiddenCells;
    },
    getHiddenCellsForProject(id: number): string {
      return this.hiddenCellsByProject[id];
    },
    setHiddenPortsForProject(id: number, hiddenPorts: string) {
      this.hiddenPortsByProject[id] = hiddenPorts;
    },
    getHiddenPortsForProject(id: number): string {
      return this.hiddenPortsByProject[id];
    }
  },
  persist: {
    storage: sessionStorage,
    paths: [
      'selectedProjectId',
      'graphByProject',
      'paperLayoutByProject',
      'filtersByProject',
      'hiddenCellsByProject',
      'hiddenPortsByProject'
    ]
  },
})
