<template>
  <v-toolbar>
    <v-toolbar-title>{{ selectedProjectName }}</v-toolbar-title>
    <v-spacer></v-spacer>
    <v-btn icon @click="fetchProcessModels">
      <v-icon>mdi-refresh</v-icon>
    </v-btn>
  </v-toolbar>
  <v-card class="full-screen-below-toolbar" @mouseup="saveGraphState">
    <ProcessDetailDialog ref="processDetailDialog"/>
    <div id="graph-container" class="full-screen"></div>
    <div class="ma-4" style="position: absolute; bottom: 8px; right: 8px;">
      <v-fab-transition style="margin-right: 5px">
        <v-btn class="mt-auto pointer-events-initial" color="primary" elevation="8" icon="mdi-chevron-left"
               @click="goLeft" size="large"/>
      </v-fab-transition>
      <v-fab-transition style="margin-right: 5px">
        <v-btn class="mt-auto pointer-events-initial" color="primary" elevation="8" icon="mdi-chevron-right"
               @click="goRight" size="large"/>
      </v-fab-transition>
      <v-fab-transition style="margin-right: 5px">
        <v-btn class="mt-auto pointer-events-initial" color="primary" elevation="8" icon="mdi-chevron-up" @click="goUp"
               size="large"/>
      </v-fab-transition>
      <v-fab-transition style="margin-right: 5px">
        <v-btn class="mt-auto pointer-events-initial" color="primary" elevation="8" icon="mdi-chevron-down"
               @click="goDown" size="large"/>
      </v-fab-transition>
      <v-fab-transition style="margin-right: 5px">
        <v-btn class="mt-auto pointer-events-initial" color="primary" elevation="8" icon="mdi-magnify-plus"
               @click="zoomIn" size="large"/>
      </v-fab-transition>
      <v-fab-transition style="margin-right: 5px">
        <v-btn class="mt-auto pointer-events-initial" color="primary" elevation="8" icon="mdi-magnify-minus"
               @click="zoomOut" size="large"/>
      </v-fab-transition>
      <v-fab-transition style="margin-right: 5px">
        <v-btn class="mt-auto pointer-events-initial" color="primary" elevation="8" icon="mdi-fit-to-screen"
               @click="fitToScreen" size="large"/>
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
</style>

<script lang="ts">
import { defineComponent, ref } from 'vue';
import { shapes } from '@joint/core';
import { graph, paper } from './jointjs/JointJSDiagram';
//MIT License
import { DirectedGraph } from '@joint/layout-directed-graph';

import createAbstractProcessElement from "./jointjs/AbstractProcessElement";
import createAbstractDataStoreElement from "./jointjs/AbstractDataStoreElement";

import axios from 'axios';
import ProcessDetailDialog from '@/components/ProcessDetailDialog.vue';

import { useAppStore } from "../../store/app";
import getProject from "../projectService";

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


export default defineComponent({
  components: {
    ProcessDetailDialog
  },
  data: () => ({
    selectedProjectId: null as number | null,
    selectedProjectName: '' as string,
    tooltipVisible: false,
    mouseX: '',
    mouseY: '',
    portsInformation: {} as { [key: string]: string[] },
    tooltipList: [] as string[],
    store: useAppStore(),
  }),
  setup() {
    const processDetailDialog = ref(null);
    return {
      processDetailDialog,
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
      graph.fromJSON(JSON.parse(persistedGraph));
    } else {
      this.fetchProcessModels();
    }
    const persistedLayout = this.store.getPaperLayoutForProject(this.store.selectedProjectId!);
    if (!!persistedLayout) {
      const { sx, tx, ty } = persistedLayout;
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
      this.store.setPaperLayoutForProject(this.store.selectedProjectId!, {
        sx: paper.scale().sx,
        tx: paper.translate().tx,
        ty: paper.translate().ty
      });
    },
    fetchProcessModels() {
      const component = this;
      graph.clear();
      axios.get("/api/project/" + this.selectedProjectId + "/process-map").then(result => {

        let abstracProcessShapes = result.data.processes.map((process: Process) => {

          const filterEmpty = (label: string) => label != undefined && label != null && label != '';

          this.portsInformation['start-' + process.id] = process.startEvents.filter(event => filterEmpty(event.label)).map(e => e.label);
          this.portsInformation['i-catch-event-' + process.id] = process.intermediateCatchEvents.filter(event => filterEmpty(event.label)).map(e => e.label);
          this.portsInformation['i-throw-event-' + process.id] = process.intermediateThrowEvents.filter(event => filterEmpty(event.label)).map(e => e.label);
          this.portsInformation['end-' + process.id] = process.endEvents.filter(event => filterEmpty(event.label)).map(e => e.label);
          this.portsInformation['call-' + process.id] = process.activities.filter(event => filterEmpty(event.label)).map(e => e.label);

          return createAbstractProcessElement(process.name, process.id);
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
    }
  }
})
</script>
