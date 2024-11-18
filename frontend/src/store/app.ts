// Utilities
import { defineStore } from 'pinia'
import {
  ActiveProjectByGroup,
  Project
} from "@/components/Home/ProjectOverview.vue";
import { LanguageCode } from "@/layouts/default/AppBar.vue";

export interface PaperLayout {
  sx: number,
  tx: number,
  ty: number
}

export interface UserData {
  id: number
  email: string,
  firstName: string,
  lastName: string,
  createdAt: string,
  modifiedAt: string,
  token: string,
  role: string
}

export enum SelectedDialog {
  NONE = -1,
  PROFILE = 0,
  EDIT_PROFILE = 1,
  CREATE_ACCOUNT = 3,
  CHANGE_PW = 4
}

export const useAppStore = defineStore('app', {
  state: () => {
    return {
      selectedProjectId: null as number | null,
      activeProjectByGroup: {} as ActiveProjectByGroup,
      graphByProject: {} as { [key: number]: string }, // new Map<number, string>() // real map not working with persist plugin
      portsInformationByProject: {} as {
        [key: number]: { [key: string]: string[] }
      },
      paperLayoutByProject: {} as { [key: number]: string },
      filtersByProject: {} as { [key: number]: string },
      hiddenCellsByProject: {} as { [key: number]: string },
      hiddenPortsByProject: {} as { [key: number]: string },
      hiddenLinksByProject: {} as {[key: number]: { [key: string]: string }},
      processModelsChangeFlag: false as boolean,
      selectedLanguage: 'en' as LanguageCode,
      areSettingsOpened: false as boolean,
      operateConnectionError: '' as string,
      operateClusterError: '' as string,
      user: null as UserData | null,
      selectedDialog: SelectedDialog.NONE as SelectedDialog
    }
  },
  actions: {
    setSelectedProjectId(id: number) {
      this.selectedProjectId = id;
    },
    getSelectedProjectId(): number | null {
      return this.selectedProjectId;
    },
    setActiveProjectForGroup(projectGroupName: string, project: Project) {
      this.activeProjectByGroup[projectGroupName] = project;
    },
    getActiveProjectForGroup(projectGroupName: string): Project {
      return this.activeProjectByGroup[projectGroupName];
    },
    setGraphForProject(id: number, graph: string) {
      this.graphByProject[id] = graph;
    },
    getGraphForProject(id: number): string {
      return this.graphByProject[id];
    },
    setPortsInformationByProject(id: number, portsInformation: {
      [key: string]: string[]
    }) {
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
    },
    setProcessModelsChanged() {
      this.processModelsChangeFlag = true;
    },
    unsetProcessModelsChanged() {
      this.processModelsChangeFlag = false;
    },
    getProcessModelsChangeFlag() {
      return this.processModelsChangeFlag
    },
    setSelectedLanguage(language: LanguageCode) {
      this.selectedLanguage = language;
    },
    getSelectedLanguage(): LanguageCode {
      return this.selectedLanguage;
    },
    setAreSettingsOpened(opened: boolean) {
      this.areSettingsOpened = opened;
    },
    getAreSettingsOpened() {
      return this.areSettingsOpened
    },
    setOperateConnectionError(error: string) {
      this.operateConnectionError = error
    },
    getOperateConnectionError() {
      return this.operateConnectionError
    },
    setOperateClusterError(error: string) {
      this.operateClusterError = error
    },
    getOperateClusterError() {
      return this.operateClusterError
    },
    setUser(user: UserData | null) {
      this.user = user;
    },
    getUser(): UserData | null {
      return this.user;
    },
    setSelectedDialog(dialog: SelectedDialog) {
      this.selectedDialog = dialog;
    },
    getSelectedDialog(): SelectedDialog {
      return this.selectedDialog;
    }
  },
  persist: {
    storage: sessionStorage,
    paths: [
      'selectedProjectId',
      'activeProjectByGroup',
      'graphByProject',
      'paperLayoutByProject',
      'filtersByProject',
      'hiddenCellsByProject',
      'hiddenPortsByProject',
      'portsInformationByProject',
      'hiddenLinksByProject',
      'processModelsChangeFlag',
      'selectedLanguage',
      'areSettingsOpened',
      'operateConnectionError',
      'operateClusterError',
      'user',
      'selectedDialog'
    ]
  },
})
