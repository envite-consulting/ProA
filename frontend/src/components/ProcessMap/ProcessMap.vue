<template>
  <v-toolbar>
    <v-toolbar-title>{{ selectedProjectName }}</v-toolbar-title>
    <v-spacer></v-spacer>
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
  <v-card class="full-screen-below-toolbar" @mouseup="saveGraphState">
    <ProcessDetailDialog ref="processDetailDialog"/>
    <div id="graph-container" class="full-screen"></div>
    <div style="position: absolute; top: 0; right: 0;">
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
import { dia, shapes } from '@joint/core';
import { graph, paper } from './jointjs/JointJSDiagram';
//MIT License
import { DirectedGraph } from '@joint/layout-directed-graph';

import createAbstractProcessElement, { AbstractProcessShape } from "./jointjs/AbstractProcessElement";
import createAbstractDataStoreElement, { AbstractDataStoreShape } from "./jointjs/AbstractDataStoreElement";

import axios from 'axios';
import ProcessDetailDialog from '@/components/ProcessDetailDialog.vue';

import { useAppStore } from "@/store/app";
import getProject from "../projectService";

const scrollStep = 20;

interface Process {
  id: number
  processName: string
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
    ProcessDetailDialog
  },

  data: () => ({
    selectedProjectId: null as number | null,
    selectedProjectName: '' as string,
    store: useAppStore(),
  }),

  setup() {
    const appStore = useAppStore();
    const projectId = appStore.selectedProjectId;
    const processDetailDialog = ref(null);
    const showFilterMenu = ref(false);
    const persistedHiddenPorts = appStore.getHiddenPortsForProject(projectId);
    const hiddenPorts: {
      [key: string]: dia.Element.Port[]
    } = !!persistedHiddenPorts ? JSON.parse(persistedHiddenPorts!) : {};
    const persistedHiddenCells = appStore.getHiddenCellsForProject(projectId);
    const hiddenCells: dia.Cell[] = !!persistedHiddenCells ? JSON.parse(persistedHiddenCells!) : [];
    const persistedFilterGraphInput = appStore.getFiltersForProject(projectId);
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

    const persistedGraph = this.store.getGraphForProject(this.store.selectedProjectId!);
    if (!!persistedGraph) {
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
      this.store.setGraphForProject(this.store.selectedProjectId!, JSON.stringify(graph));
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
    },
    fetchProcessModels() {
      this.resetFilters();
      const component = this;
      graph.clear();
      axios.get("/api/project/" + this.selectedProjectId + "/process-map").then(result => {

        let abstracProcessShapes = result.data.processes.map((process: Process) => {
          return createAbstractProcessElement(process.processName, process.id);
        });

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
    toggleFilterMenu() {
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
</script>../ProcessMap/jointjs/JointJSDiagram
