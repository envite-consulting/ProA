// Utilities
import { defineStore } from 'pinia'
import {
  ActiveProjectByGroup,
  Project
} from "@/components/Home/ProjectOverview.vue";
import { LanguageCode } from "@/layouts/default/AppBar.vue";
import { Role } from "@/components/ProcessMap/types";
import { SnackbarType, SnackbarConfigs } from "@/utils/snackbar";

export enum SelectedDialog {
  NONE = -1,
  PROFILE = 0,
  EDIT_PROFILE = 1,
  CREATE_ACCOUNT = 3,
  CHANGE_PW = 4
}

export interface Snackbar {
  visible: boolean;
  message: string;
  type: SnackbarType;
  color: string;
  icon: string;
  timeout: number;
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
      hiddenLinksByProject: {} as { [key: number]: { [key: string]: string } },
      processModelsChangeFlag: false as boolean,
      selectedLanguage: 'en' as LanguageCode,
      areSettingsOpened: false as boolean,
      operateConnectionError: '' as string,
      operateClusterError: '' as string,
      userToken: null as string | null,
      userRole: null as Role | null,
      selectedDialog: SelectedDialog.NONE as SelectedDialog,
      snackbar: {
        visible: false,
        message: '',
        type: '' as SnackbarType,
        color: '',
        icon: '',
        timeout: 3000
      } as Snackbar
    }
  },
  actions: {
    setSelectedProjectId(id: number | null) {
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
    setUserToken(userToken: string | null) {
      this.userToken = userToken;
    },
    getUserToken(): string | null {
      return this.userToken;
    },
    setSelectedDialog(dialog: SelectedDialog) {
      this.selectedDialog = dialog;
    },
    getSelectedDialog(): SelectedDialog {
      return this.selectedDialog;
    },
    setUserRole(role: Role | null) {
      this.userRole = role;
    },
    getUserRole(): Role | null {
      return this.userRole;
    },
    async showSnackbar(message: string, type: SnackbarType) {
      this.snackbar.visible = false;
      await new Promise((resolve) => setTimeout(resolve, 0));
      this.snackbar.message = message;
      this.snackbar.type = type;
      this.snackbar.color = SnackbarConfigs[type].color;
      this.snackbar.icon = SnackbarConfigs[type].icon;
      this.snackbar.visible = true;
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
      'userToken',
      'userRole',
      'selectedDialog'
    ]
  },
})