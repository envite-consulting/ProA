<template>
  <v-toolbar>
    <v-toolbar-title>
      <div class="d-flex align-center">
        <span>{{ selectedProjectName }}</span>
        <span class="text-body-2 text-grey-darken-1 ms-4">VERSION {{ selectedVersionName }}</span>
      </div>
    </v-toolbar-title>
    <v-spacer></v-spacer>
    <v-btn icon @click="toggleLegend">
      <v-icon>mdi-map-legend</v-icon>
    </v-btn>
    <v-btn v-if="filtersCount > 0" @click="toggleFilterMenu" icon style="margin-right: 5px">
      <v-badge color="white" :bordered="true" :content="filtersCount">
        <v-icon>mdi-filter-outline</v-icon>
      </v-badge>
    </v-btn>
    <v-btn v-else @click="toggleFilterMenu" icon style="margin-right: 5px">
      <v-icon>mdi-filter-outline</v-icon>
    </v-btn>
    <v-btn icon @click="fetchProcessModels">
      <v-icon>mdi-refresh</v-icon>
    </v-btn>
  </v-toolbar>
  <v-card :class="selectedProjectName ? 'full-screen-below-toolbar' : 'full-screen'" @mouseup="saveGraphState">
    <ProcessDetailDialog ref="processDetailDialog"/>
    <div id="graph-container" class="full-screen"></div>
    <div style="position: absolute; top: 0; right: 0;">
      <v-list v-if="showLegend">
        <v-list-item>
          <v-list-item-title class="font-weight-bold">Legende</v-list-item-title>
        </v-list-item>
        <v-divider></v-divider>
        <LegendItem text="Prozess" path="M 0 0 h 140 l 10 35 l -10 35 H 0 l 10 -35 l -10 -35 Z" width="30"
                    height="30" view-box="-2 -2 154 74" stroke-width="8"></LegendItem>
        <LegendItem text="Datenbank"
                    path="M 1.5 9 C 15.5 19 75.5 19 90.5 9 l 0 90 c -15 10 -75 10 -89 0 l 0 -90 C 15.5 -1 75.5 -1 90.5 9 v 15 c -15 10 -75 10 -89 0"
                    width="30"
                    height="30" view-box="-0.5 -0.5 93 109" stroke-width="5"></LegendItem>
        <LegendItem text="Startereignis" path="M 10 -20 a 10 10 0 1 0 0.00001 0 Z" width="30"
                    height="30" view-box="-1.99999 -22 24 24" stroke-width="1.5"></LegendItem>
        <LegendItem text="Endereignis" path="M 10 -20 a 10 10 0 1 0 0.00001 0 Z" width="30"
                    height="30" view-box="-1.99999 -22 24 24" stroke-width="3"></LegendItem>
        <LegendItem text="Zwischenereignis" path="M -25 -10 a 10 10 0 1 0 0.00001 0 Z M -25 -7 a 7 7 0 1 0 0.00001 0 Z"
                    width="30"
                    height="30" view-box="-37 -12 24 24" stroke-width="2"></LegendItem>
        <LegendItem text="Aufrufaktivität"
                    path="M 35 -10 a3,3 0 0 1 3,3 v15 a3,3 0 0 1 -3,3 h-25 a3,3 0 0 1 -3,-3 v-15 a3,3 0 0 1 3,-3 z"
                    width="30"
                    height="30" view-box="5 -12 35 25" stroke-width="2"></LegendItem>
      </v-list>
      <v-list v-if="showFilterMenu">
        <v-list-item>
          <v-list-item-title class="font-weight-bold">Ausblenden:</v-list-item-title>
        </v-list-item>
        <v-divider></v-divider>
        <v-list-item class="filter-item" v-for="(label, filterOption) in filterOptions" :key="filterOption">
          <v-checkbox v-model="filterGraphInput[filterOption]" :label="label" color="primary"
                      @change="filterGraph" hide-details></v-checkbox>
        </v-list-item>
      </v-list>
    </div>
    <div class="ma-4" style="position: absolute; bottom: 8px; right: 8px;">
      <v-fab-transition style="margin-right: 5px">
        <v-btn class="mt-auto pointer-events-initial" color="primary" elevation="8" icon="mdi-chevron-left"
               @click="goLeft"
               size="large"/>
      </v-fab-transition>
      <v-fab-transition style="margin-right: 5px">
        <v-btn class="mt-auto pointer-events-initial" color="primary" elevation="8" icon="mdi-chevron-right"
               @click="goRight"
               size="large"/>
      </v-fab-transition>
      <v-fab-transition style="margin-right: 5px">
        <v-btn class="mt-auto pointer-events-initial" color="primary" elevation="8" icon="mdi-chevron-up" @click="goUp"
               size="large"/>
      </v-fab-transition>
      <v-fab-transition style="margin-right: 5px">
        <v-btn class="mt-auto pointer-events-initial" color="primary" elevation="8" icon="mdi-chevron-down"
               @click="goDown"
               size="large"/>
      </v-fab-transition>
      <v-fab-transition style="margin-right: 5px">
        <v-btn class="mt-auto pointer-events-initial" color="primary" elevation="8" icon="mdi-magnify-plus"
               @click="zoomIn"
               size="large"/>
      </v-fab-transition>
      <v-fab-transition style="margin-right: 5px">
        <v-btn class="mt-auto pointer-events-initial" color="primary" elevation="8" icon="mdi-magnify-minus"
               @click="zoomOut"
               size="large"/>
      </v-fab-transition>
      <v-fab-transition style="margin-right: 5px">
        <v-btn class="mt-auto pointer-events-initial" color="primary" elevation="8" icon="mdi-fit-to-screen"
               @click="fitToScreen"
               size="large"/>
      </v-fab-transition>
    </div>
  </v-card>
  <v-tooltip id="tool-tip" v-model="tooltipVisible" :style="{ position: 'fixed', top: mouseY, left: mouseX }">
    <ul v-if="tooltipList.length > 0">
      <li v-for="item in tooltipList">{{ item }}</li>
    </ul>
    <span v-if="tooltipList.length === 0">Keine Informationen vorhanden</span>
  </v-tooltip>
</template>

<style>
.full-screen-below-toolbar {
  width: 100%;
  height: calc(100% - 64px) !important;
}

.full-screen {
  width: 100%;
  height: 100%;
}

.filter-item {
  height: 1rem;
}
</style>

<script lang="ts">
import { computed, defineComponent, reactive, ref } from 'vue';
import { dia, linkTools, shapes } from '@joint/core';
import { graph, paper } from './jointjs/JointJSDiagram';
//MIT License
import { DirectedGraph } from '@joint/layout-directed-graph';

import createAbstractProcessElement, { AbstractProcessShape } from "./jointjs/AbstractProcessElement";
import createAbstractDataStoreElement, { AbstractDataStoreShape } from "./jointjs/AbstractDataStoreElement";

import axios from 'axios';
import ProcessDetailDialog from '@/components/ProcessDetailDialog.vue';

import { useAppStore } from "@/store/app";
import getProject from "../projectService";
import LegendItem from "@/components/ProcessMap/LegendItem.vue";

import { PortTargetArrowhead } from "./jointjs/PortTargetArrowHead";

const scrollStep = 20;

interface Event {
  elementId: string
  label: string
}

interface Activity {
  elementId: string
  label: string
}

interface Process {
  id: number
  name: string
  startEvents: Event[]
  intermediateCatchEvents: Event[]
  intermediateThrowEvents: Event[]
  endEvents: Event[]
  activities: Activity[]
}

type ProcessElementType =
  "START_EVENT"
  | "INTERMEDIATE_CATCH_EVENT"
  | "INTERMEDIATE_THROW_EVENT"
  | "END_EVENT"
  | "CALL_ACTIVITY";

interface Connection {
  callingProcessid: number
  callingElementType: ProcessElementType

  calledProcessid: number
  calledElementType: ProcessElementType
}

interface DataStore {
  id: number
  name: string
}

type DataAccess = "READ" | "WRITE" | "READ_WRITE" | "NONE;";

interface DataStoreConnection {
  processid: number
  dataStoreId: number
  access: DataAccess
}

interface FilterGraphInput {
  hideAbstractDataStores: boolean;
  hideCallActivities: boolean;
  hideIntermediateEvents: boolean;
  hideStartEndEvents: boolean;
  hideProcessesWithoutConnections: boolean;
}

export default defineComponent({
  components: {
    LegendItem,
    ProcessDetailDialog
  },

  data: () => ({
    selectedProjectId: null as number | null,
    selectedProjectName: '' as string,
    selectedVersionName: '' as string,
    tooltipVisible: false,
    mouseX: '',
    mouseY: '',
    portsInformation: {} as { [key: string]: string[] },
    tooltipList: [] as string[],
    store: useAppStore(),
    showLegend: false
  }),
  setup() {
    const appStore = useAppStore();
    const projectId = appStore.selectedProjectId;
    const processDetailDialog = ref(null);
    const showFilterMenu = ref(false);
    const persistedHiddenPorts = appStore.getHiddenPortsForProject(projectId!);
    const hiddenPorts: {
      [key: string]: dia.Element.Port[]
    } = !!persistedHiddenPorts ? JSON.parse(persistedHiddenPorts!) : {};
    const persistedHiddenCells = appStore.getHiddenCellsForProject(projectId!);
    const hiddenCells: dia.Cell[] = !!persistedHiddenCells ? JSON.parse(persistedHiddenCells!) : [];
    const persistedFilterGraphInput = appStore.getFiltersForProject(projectId!);
    const filterGraphInput: FilterGraphInput = reactive(
      !!persistedFilterGraphInput ?
        JSON.parse(persistedFilterGraphInput) :
        {
          hideAbstractDataStores: false,
          hideCallActivities: false,
          hideIntermediateEvents: false,
          hideStartEndEvents: false,
          hideProcessesWithoutConnections: false
        });
    const filterOptions = {
      hideAbstractDataStores: 'Ressourcen',
      hideCallActivities: 'Aufrufaktivitäten',
      hideIntermediateEvents: 'Zwischenereignisse',
      hideStartEndEvents: 'End- zu Start-Verbindungen',
      hideProcessesWithoutConnections: 'Prozesse ohne Verbindungen'
    };
    const filtersCount = computed(() => {
      return Object.values(filterGraphInput).filter(value => value === true).length;
    });
    return {
      processDetailDialog,
      showFilterMenu,
      hiddenPorts,
      hiddenCells,
      filterGraphInput,
      filterOptions,
      filtersCount
    };
  },
  mounted: function () {
    this.selectedProjectId = this.store.selectedProjectId;
    if (!this.selectedProjectId) {
      this.$router.push("/");
      return;
    }
    getProject(this.selectedProjectId).then(result => {
      this.selectedProjectName = result.data.name;
      this.selectedVersionName = result.data.version;
    })
    const paperContainer = document.getElementById("graph-container");
    paperContainer!.appendChild(paper.el);

    const showProcessInfoDialog = (this.processDetailDialog! as InstanceType<typeof ProcessDetailDialog>).showProcessInfoDialog;
    paper.on('cell:pointerdblclick',
      function (cellView, evt, x, y) {
        if (!cellView.model.id.toString().startsWith('ds')) {
          showProcessInfoDialog(+cellView.model.id);
        }
      }
    );

    paper.on('paper:pinch', function (evt, x, y, sx) {
      evt.preventDefault();
      const { sx: sx0 } = paper.scale();
      paper.scaleUniformAtPoint(sx0 * sx, { x, y });
    });

    paper.on('paper:pan', function (evt, tx, ty) {
      evt.preventDefault();
      evt.stopPropagation();
      const { tx: tx0, ty: ty0 } = paper.translate();
      paper.translate(tx0 - tx, ty0 - ty);
    });

    paper.on('element:mouseover', (view, evt) => {
      var port = view.findAttribute('port', evt.target);
      if (port) {

        this.tooltipList = this.portsInformation[port];
        this.tooltipVisible = true;

        this.mouseX = evt.clientX! + "px !important";
        this.mouseY = evt.clientY! + "px !important";
      }
    });

    paper.on('element:mouseout', (view, evt) => {
      var port = view.findAttribute('port', evt.target);
      if (port) {
        this.tooltipVisible = false;
      }
    });

    const persistedGraph = this.store.getGraphForProject(this.store.selectedProjectId!);
    if (!!persistedGraph) {
      Object.assign(this.portsInformation, this.store.getPortsInformationByProject(this.store.selectedProjectId!));
      graph.fromJSON(JSON.parse(persistedGraph));
    } else {
      this.fetchProcessModels();
    }
    const persistedLayout = this.store.getPaperLayoutForProject(this.store.selectedProjectId!);
    if (!!persistedLayout) {
      const { sx, tx, ty } = JSON.parse(persistedLayout);
      paper.scale(sx);
      paper.translate(tx, ty);
    }

    const clearTools = () => {
      if (!lastView) return;
      lastView.removeTools();
    }
    let timer: NodeJS.Timeout;
    let lastView: dia.LinkView;
    paper.on("link:mouseenter", (linkView) => {
      if (linkView.model.get("source").id.toString().startsWith('ds')) {
        return;
      }
      clearTimeout(timer);
      clearTools();
      lastView = linkView;
      linkView.addTools(
        new dia.ToolsView({
          name: "onhover",
          tools: [
            new PortTargetArrowhead(),
            new linkTools.Remove({
              distance: -60,
              action: removeLink,
              markup: [
                {
                  tagName: "circle",
                  selector: "button",
                  attributes: {
                    r: 10,
                    fill: "#FFD5E8",
                    stroke: "#FD0B88",
                    "stroke-width": 2,
                    cursor: "pointer"
                  }
                },
                {
                  tagName: "path",
                  selector: "icon",
                  attributes: {
                    d: "M -4 -4 4 4 M -4 4 4 -4",
                    fill: "none",
                    stroke: "#333",
                    "stroke-width": 3,
                    "pointer-events": "none"
                  }
                }
              ]
            })
          ]
        })
      );
    });

    paper.on("link:mouseleave", () => {
      timer = setTimeout(() => clearTools(), 500);
    });

    paper.on("link:connect", async (linkView) => {
      const link = linkView.model;
      const callingProcessid = link.source().id;
      const calledProcessid = link.target().id;
      const callingElementType = this.getProcessElementType(link.source().port || '');
      const calledElementType = this.getProcessElementType(link.target().port || '');

      if (!callingElementType || !calledElementType) {
        return;
      }

      try {
        await axios.post(`/api/project/${this.selectedProjectId}/process-map/connection`, {
          callingProcessid,
          calledProcessid,
          callingElementType,
          calledElementType
        }, {
          headers: {
            'Content-Type': 'application/json'
          }
        });
      } catch (error) {
        console.error("Error while adding connection:", error);
      }
    });

    paper.on("link:disconnect", async (linkView, evt, prevElementView, prevMagnet) => {
      const link = linkView.model;
      const callingProcessid = link?.source()?.id?.toString();
      const calledProcessid = prevElementView.model.id.toString();
      const callingElementType = this.getProcessElementType(link?.source()?.port || '');
      const calledElementType = this.getProcessElementType(prevMagnet.getAttribute('port') || '');
      await removeLinkHelper(callingProcessid, callingElementType, calledProcessid, calledElementType);
    });

    const removeProcessLink = async (callingProcessid: string, callingElementType: string, calledProcessid: string, calledElementType: string) => {
      try {
        await axios.patch(`/api/project/${this.selectedProjectId}/process-map/connection`, {
          callingProcessid,
          callingElementType,
          calledProcessid,
          calledElementType
        }, {
          headers: {
            'Content-Type': 'application/json'
          }
        });
      } catch (error) {
        console.error("Error while removing connection:", error);
      }
    }

    const removeDataStoreLink = async (processid: string, dataStoreId: string) => {
      dataStoreId = dataStoreId.split('-')[1];
      try {
        await axios.patch(`/api/project/${this.selectedProjectId}/process-map/datastore-connection`, {
          processid,
          dataStoreId
        }, {
          headers: {
            'Content-Type': 'application/json'
          }
        });
      } catch (error) {
        console.error("Error while removing connection:", error);
      }
    }

    const removeLink = async (evt: dia.Event, linkView: dia.LinkView, toolView: dia.ToolView) => {
      linkView.model.remove({ ui: true, tool: toolView.cid });
      const link = linkView.model;
      const callingProcessid = link?.source()?.id?.toString();
      const calledProcessid = link?.target()?.id?.toString();
      const callingElementType = this.getProcessElementType(link.source().port!);
      const calledElementType = this.getProcessElementType(link.target().port!);
      await removeLinkHelper(callingProcessid, callingElementType, calledProcessid, calledElementType);
    }

    const removeLinkHelper = async (callingProcessid: string | undefined, callingElementType: string | null, calledProcessid: string | undefined, calledElementType: string | null) => {
      if (!callingProcessid || !calledProcessid) {
        return;
      }
      if (callingProcessid.startsWith('ds')) {
        await removeDataStoreLink(calledProcessid, callingProcessid);
        return;
      }

      if (calledProcessid.startsWith('ds')) {
        await removeDataStoreLink(callingProcessid, calledProcessid);
        return;
      }
      if (!callingElementType || !calledElementType) {
        return;
      }

      await removeProcessLink(callingProcessid, callingElementType, calledProcessid, calledElementType);
    }
  },
  methods: {
    zoomIn() {
      const { sx: sx0 } = paper.scale();
      paper.scale(sx0 * 1.3);
      this.savePaperLayout();
    },
    zoomOut() {
      const { sx: sx0 } = paper.scale();
      paper.scale(sx0 * 0.7);
      this.savePaperLayout();
    },
    fitToScreen() {
      paper.transformToFitContent();
      this.savePaperLayout();
    },
    goRight() {
      const { tx: tx0, ty: ty0 } = paper.translate();
      paper.translate(tx0 - scrollStep, ty0);
      this.savePaperLayout();
    },
    goLeft() {
      const { tx: tx0, ty: ty0 } = paper.translate();
      paper.translate(tx0 + scrollStep, ty0);
      this.savePaperLayout();
    },
    goUp() {
      const { tx: tx0, ty: ty0 } = paper.translate();
      paper.translate(tx0, ty0 + scrollStep);
      this.savePaperLayout();
    },
    goDown() {
      const { tx: tx0, ty: ty0 } = paper.translate();
      paper.translate(tx0, ty0 - scrollStep);
      this.savePaperLayout();
    },
    saveGraphState() {
      setTimeout(() => {
        this.store.setGraphForProject(this.store.selectedProjectId!, JSON.stringify(graph));
      }, 50);
    },
    savePaperLayout() {
      this.store.setPaperLayoutForProject(this.store.selectedProjectId!, JSON.stringify({
        sx: paper.scale().sx,
        tx: paper.translate().tx,
        ty: paper.translate().ty
      }));
    },
    saveFilters() {
      this.store.setFiltersForProject(this.store.selectedProjectId!, JSON.stringify(this.filterGraphInput));
    },
    saveHiddenCells() {
      this.store.setHiddenCellsForProject(this.store.selectedProjectId!, JSON.stringify(this.hiddenCells));
    },
    saveHiddenPorts() {
      this.store.setHiddenPortsForProject(this.store.selectedProjectId!, JSON.stringify(this.hiddenPorts));
    },
    closeMenus() {
      this.showFilterMenu = false;
      this.showLegend = false;
    },
    resetFilters() {
      this.filterGraphInput['hideIntermediateEvents'] = false;
      this.filterGraphInput['hideStartEndEvents'] = false;
      this.filterGraphInput['hideCallActivities'] = false;
      this.filterGraphInput['hideProcessesWithoutConnections'] = false;
      this.filterGraphInput['hideAbstractDataStores'] = false;
      this.hiddenCells = [];
      this.hiddenPorts = {};
      this.saveFilters();
      this.saveHiddenCells();
      this.saveHiddenPorts();
      this.closeMenus();
    },
    fetchProcessModels() {
      this.resetFilters();
      const component = this;
      graph.clear();
      axios.get("/api/project/" + this.selectedProjectId + "/process-map").then(result => {

        let abstracProcessShapes = result.data.processes.map((process: Process) => {

          const filterEmpty = (label: string) => !!label;

          this.portsInformation['start-' + process.id] = process.startEvents.filter(event => filterEmpty(event.label)).map(e => e.label);
          this.portsInformation['i-catch-event-' + process.id] = process.intermediateCatchEvents.filter(event => filterEmpty(event.label)).map(e => e.label);
          this.portsInformation['i-throw-event-' + process.id] = process.intermediateThrowEvents.filter(event => filterEmpty(event.label)).map(e => e.label);
          this.portsInformation['end-' + process.id] = process.endEvents.filter(event => filterEmpty(event.label)).map(e => e.label);
          this.portsInformation['call-' + process.id] = process.activities.filter(event => filterEmpty(event.label)).map(e => e.label);

          return createAbstractProcessElement(process.name, process.id);
        });

        this.store.setPortsInformationByProject(this.store.selectedProjectId!, this.portsInformation);

        graph.addCell(abstracProcessShapes);

        let connectionsShapes = result.data.connections.map((connection: Connection) => {

          const link = new shapes.standard.Link();

          const callingPortPrefix = component.getPortPrefix(connection.callingElementType);
          const calledPortPrefix = component.getPortPrefix(connection.calledElementType);

          link.set({
            source: { id: connection.callingProcessid, port: callingPortPrefix + connection.callingProcessid },
            target: { id: connection.calledProcessid, port: calledPortPrefix + connection.calledProcessid }
          })
          return link;
        });

        graph.addCell(connectionsShapes);

        let abstractDataStores = result.data.dataStores.map((dataStore: DataStore) => {
          return createAbstractDataStoreElement(dataStore.name, dataStore.id);
        })

        graph.addCell(abstractDataStores);

        let dataStoreConnectionShapes = result.data.dataStoreConnections.map((connection: DataStoreConnection) => {

          const link = new shapes.standard.Link();

          if (connection.access === "READ_WRITE") {
            link.attr({
              line: {
                sourceMarker: {
                  'type': 'path',
                  'stroke': 'black',
                  'fill': 'black',
                  'd': 'M 10 -5 0 0 10 5 Z'
                },
                targetMarker: {
                  'type': 'path',
                  'stroke': 'black',

                }
              }
            });

            link.set({
              source: { id: connection.processid, port: "call-" + connection.processid },
              target: {
                id: "ds-" + connection.dataStoreId,
                anchor: {
                  name: 'midSide',
                  args: {
                    rotate: true,
                  }

                }
              }
            })
          } else if (connection.access === "WRITE") {

            link.set({
              source: { id: connection.processid, port: "call-" + connection.processid },
              target: {
                id: "ds-" + connection.dataStoreId,
                anchor: {
                  name: 'midSide',
                  args: {
                    rotate: true,
                  }

                }
              }
            })
          } else if (connection.access === "READ") {
            link.set({
              target: { id: connection.processid, port: "call-" + connection.processid },
              source: {
                id: "ds-" + connection.dataStoreId,
                anchor: {
                  name: 'midSide',
                  args: {
                    rotate: true,
                  }

                }
              }
            })
          }

          return link;
        });

        graph.addCell(dataStoreConnectionShapes);
        paper.freeze();
        var graphBBox = DirectedGraph.layout(graph, {
          nodeSep: 150,
          edgeSep: 80,
          rankDir: "TB",
          marginX: 10,
          marginY: 10,
        });

        paper.transformToFitContent();
        paper.unfreeze();
        this.saveGraphState();
      })
    },
    getPortPrefix(elementType: ProcessElementType) {
      switch (elementType) {
        case 'START_EVENT':
          return 'start-';
        case 'INTERMEDIATE_CATCH_EVENT':
          return 'i-catch-event-';
        case 'INTERMEDIATE_THROW_EVENT':
          return 'i-throw-event-';
        case 'END_EVENT':
          return 'end-'
        case 'CALL_ACTIVITY':
          return 'call-'
        default:
          return '';
      }
    },
    getProcessElementType(portId: string): ProcessElementType | null {
      const mappings: { [key: string]: ProcessElementType } = {
        'start-': 'START_EVENT',
        'i-catch-event-': 'INTERMEDIATE_CATCH_EVENT',
        'i-throw-event-': 'INTERMEDIATE_THROW_EVENT',
        'end-': 'END_EVENT',
        'call-': 'CALL_ACTIVITY'
      };

      for (const prefix in mappings) {
        if (portId.startsWith(prefix)) {
          return mappings[prefix];
        }
      }

      return null;
    },
    toggleLegend() {
      if (!this.showLegend) {
        this.closeMenus();
      }
      this.showLegend = !this.showLegend;
    },
    toggleFilterMenu() {
      if (!this.showFilterMenu) {
        this.closeMenus();
      }
      this.showFilterMenu = !this.showFilterMenu;
    },
    filterGraph() {
      const {
        hideAbstractDataStores,
        hideCallActivities,
        hideIntermediateEvents,
        hideStartEndEvents,
        hideProcessesWithoutConnections
      } = this.filterGraphInput;

      graph.addCells(this.hiddenCells);
      this.hiddenCells = [];
      for (const [cellId, ports] of Object.entries(this.hiddenPorts)) {
        const cell = graph.getCell(cellId);
        if (cell instanceof AbstractProcessShape) {
          cell.addPorts(ports);
        }
      }
      this.hiddenPorts = {};

      const cellsToHide: dia.Cell[] = [];

      for (const link of graph.getLinks()) {
        const sourceCell = link.getSourceCell();
        const targetCell = link.getTargetCell();
        const sourcePort = link?.attributes?.source?.port;
        const targetPort = link?.attributes?.target?.port;

        if (
          hideAbstractDataStores &&
          (sourceCell instanceof AbstractDataStoreShape || targetCell instanceof AbstractDataStoreShape)
        ) {
          cellsToHide.push(link);

          if (sourceCell instanceof AbstractDataStoreShape && !cellsToHide.includes(sourceCell)) {
            cellsToHide.push(sourceCell);
          }
          if (targetCell instanceof AbstractDataStoreShape && !cellsToHide.includes(targetCell)) {
            cellsToHide.push(targetCell);
          }

        } else if (hideCallActivities && (sourcePort?.startsWith('call') || targetPort?.startsWith('call'))) {
          cellsToHide.push(link);

        } else if (hideIntermediateEvents && (sourcePort?.startsWith('i-') || targetPort?.startsWith('i-'))) {
          cellsToHide.push(link);

        } else if (hideStartEndEvents && sourcePort?.startsWith('end') && targetPort?.startsWith('start')) {
          cellsToHide.push(link);
        }
      }

      this.hiddenCells = cellsToHide;
      graph.removeCells(cellsToHide);

      const processesWithoutConnections: dia.Cell[] = [];
      for (const cell of graph.getCells()) {
        if (
          hideProcessesWithoutConnections &&
          (cell instanceof AbstractProcessShape || cell instanceof AbstractDataStoreShape) &&
          graph.getConnectedLinks(cell).length === 0
        ) {
          processesWithoutConnections.push(cell);
        } else if (cell instanceof AbstractProcessShape) {
          if (hideCallActivities) {
            const portId = 'call-' + cell.id;
            this.hiddenPorts[cell.id] = this.hiddenPorts[cell.id] || [];
            this.hiddenPorts[cell.id].push(cell.getPort(portId));
            cell.removePort(portId);
          }
          if (hideIntermediateEvents) {
            const portIds = ['i-catch-event-' + cell.id, 'i-throw-event-' + cell.id];
            this.hiddenPorts[cell.id] = this.hiddenPorts[cell.id] || [];
            this.hiddenPorts[cell.id].push(cell.getPort(portIds[0]));
            this.hiddenPorts[cell.id].push(cell.getPort(portIds[1]));
            cell.removePorts(portIds);
          }
        }

      }
      this.hiddenCells.push(...processesWithoutConnections);
      graph.removeCells(processesWithoutConnections);

      this.saveGraphState();
      this.saveFilters();
      this.saveHiddenCells();
      this.saveHiddenPorts();
    }
  }
})
</script>
