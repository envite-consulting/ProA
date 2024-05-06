// Utilities
import { defineStore } from 'pinia'

export interface PaperLayout {
  sx: number,
  tx: number,
  ty: number
}

export const useAppStore = defineStore('app', {
  state: () => {
    return {
      selectedProjectId: null as number | null,
      graphByProject: {} as { [key: number]: string }, // new Map<number, string>() // real map not working with persist plugin
      portsInformationByProject: {} as {[key: number]: { [key: string]: string[] }},
      paperLayoutByProject: {} as { [key: number]: string },
      filtersByProject: {} as { [key: number]: string },
      hiddenCellsByProject: {} as { [key: number]: string },
      hiddenPortsByProject: {} as { [key: number]: string },
      hiddenLinksByProject: {} as {[key: number]: { [key: string]: string }},
    }
  },
  actions: {
    setGraphForProject(id: number, graph: string) {
      this.graphByProject[id] = graph;
    },
    getGraphForProject(id: number): string {
      return this.graphByProject[id];
    },
    setPortsInformationByProject(id: number, portsInformation: { [key: string]: string[] }){
      this.portsInformationByProject[id] = portsInformation;
    },
    getPortsInformationByProject(id: number): { [key: string]: string[] } {
      return this.portsInformationByProject[id];
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
    },
    setHiddenLinksForProject(id: number, hiddenLinks: { [key: string]: string }) {
      this.hiddenLinksByProject[id] = hiddenLinks;
    },
    getHiddenLinksForProject(id: number): { [key: string]: string } {
      return this.hiddenLinksByProject[id];
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
      'hiddenPortsByProject',
      'portsInformationByProject',
      'hiddenLinksByProject',
    ]
  },
})
