<template>
  <ProcessMapToolbar ref="toolbar" :selectedProjectId="selectedProjectId"
                     @fetchProcessModels="fetchProcessModels" @filterGraph="filterGraph"/>
  <v-card class="full-screen-below-toolbar" @mouseup="saveGraphState">
    <ProcessDetailSidebar ref="processDetailSidebar" @saveGraphState="saveGraphState"/>
    <div id="graph-container" class="full-screen"></div>
    <NavigationButtons ref="navigationButtons" :selectedProjectId="selectedProjectId"/>
  </v-card>
  <v-tooltip id="tool-tip" v-model="tooltipVisible" :style="{ position: 'fixed', top: mouseY, left: mouseX }">
    <ul v-if="tooltipList.length > 0">
      <li v-for="item in tooltipList">{{ item }}</li>
    </ul>
    <span v-if="tooltipList.length === 0">{{ $t('processMap.noInformationAvailable') }}</span>
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
import axios from 'axios';
import { defineComponent } from 'vue';
import { dia, shapes } from '@joint/core';
import { DirectedGraph } from '@joint/layout-directed-graph';

import { graph, paper } from './jointjs/JointJSDiagram';
import createAbstractProcessElement, { AbstractProcessShape } from "./jointjs/AbstractProcessElement";
import createAbstractDataStoreElement, { AbstractDataStoreShape } from "./jointjs/AbstractDataStoreElement";
import { PortTargetArrowhead } from "./jointjs/PortTargetArrowHead";
import createLinkRemoveButton from "@/components/ProcessMap/jointjs/createLinkRemoveButton";
import {
  Connection,
  DataStore,
  DataStoreConnection,
  FilterGraphInput,
  HiddenLinks,
  HiddenPorts,
  PortsInformation,
  Process,
  ProcessElementType
} from "./types";

import ProcessDetailDialog from '@/components/ProcessDetailDialog.vue';
import ProcessDetailSidebar from "@/components/ProcessMap/ProcessDetailSidebar.vue";
import ProcessMapToolbar from "@/components/ProcessMap/ProcessMapToolbar.vue";
import NavigationButtons from "@/components/ProcessMap/NavigationButtons.vue";
import LegendItem from "@/components/ProcessMap/LegendItem.vue";

import { useAppStore } from "@/store/app";

export const getPortPrefix = (elementType: ProcessElementType): string => {
  switch (elementType) {
    case ProcessElementType.START_EVENT:
      return 'start-';
    case ProcessElementType.INTERMEDIATE_CATCH_EVENT:
      return 'i-catch-event-';
    case ProcessElementType.INTERMEDIATE_THROW_EVENT:
      return 'i-throw-event-';
    case ProcessElementType.END_EVENT:
      return 'end-'
    case ProcessElementType.CALL_ACTIVITY:
      return 'call-'
    default:
      return '';
  }
}

export default defineComponent({
  components: {
    NavigationButtons,
    LegendItem,
    ProcessDetailSidebar,
    ProcessDetailDialog,
    ProcessMapToolbar
  },

  data() {
    const appStore = useAppStore();
    const selectedProjectId: number = appStore.getSelectedProjectId()!;
    const persistedHiddenCells = appStore.getHiddenCellsForProject(selectedProjectId);
    const persistedHiddenLinks = appStore.getHiddenLinksForProject(selectedProjectId);
    const persistedHiddenPorts = appStore.getHiddenPortsForProject(selectedProjectId);

    const hiddenCells: dia.Cell[] = !!persistedHiddenCells ? JSON.parse(persistedHiddenCells!) : [];
    const hiddenLinks: HiddenLinks = persistedHiddenLinks ?? {};
    const hiddenPorts: HiddenPorts = persistedHiddenPorts ? JSON.parse(persistedHiddenPorts) : {};
    const portsInformation: PortsInformation = {};

    return {
      mouseX: '' as string,
      mouseY: '' as string,
      tooltipList: [] as string[],
      tooltipVisible: false as boolean,
      appStore,
      hiddenCells,
      hiddenLinks,
      hiddenPorts,
      portsInformation,
      selectedProjectId,
    }
  },

  computed: {
    toolbar() {
      return this.$refs.toolbar as InstanceType<typeof ProcessMapToolbar>;
    },
    navigationButtons() {
      return this.$refs.navigationButtons as InstanceType<typeof NavigationButtons>;
    }
  },

  mounted() {
    if (!this.selectedProjectId) {
      this.$router.push("/");
      return;
    }

    const paperContainer = document.getElementById("graph-container");
    paperContainer!.appendChild(paper.el);

    const processDetailSidebar = this.$refs.processDetailSidebar as InstanceType<typeof ProcessDetailSidebar>;

    paper.on('cell:pointerdblclick',
      function (cellView) {
        const model = cellView.model;
        if (model instanceof AbstractProcessShape) {
          processDetailSidebar.open(model as AbstractProcessShape);
        }
      }
    );

    paper.on('element:mouseover', (view, evt) => {
      const port = view.findAttribute('port', evt.target);
      if (port) {

        this.tooltipList = this.portsInformation[port];
        this.tooltipVisible = true;

        this.mouseX = evt.clientX! + "px !important";
        this.mouseY = evt.clientY! + "px !important";
      }
    });

    paper.on('element:mouseout', (view, evt) => {
      const port = view.findAttribute('port', evt.target);
      if (port) {
        this.tooltipVisible = false;
      }
    });

    const store = useAppStore();
    if (store.getProcessModelsChangeFlag()) {
      this.fetchProcessModels();
      store.unsetProcessModelsChanged();
      return;
    }

    const persistedGraph = this.appStore.getGraphForProject(this.selectedProjectId);
    if (!!persistedGraph) {
      Object.assign(this.portsInformation, this.appStore.getPortsInformationByProject(this.selectedProjectId));
      graph.fromJSON(JSON.parse(persistedGraph));
    } else {
      this.fetchProcessModels();
      return;
    }
    const persistedLayout = this.appStore.getPaperLayoutForProject(this.selectedProjectId);
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
            createLinkRemoveButton(removeLink)
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

      try {
        await axios.post(`/api/project/${this.selectedProjectId}/process-map/connection`, {
          callingProcessid,
          calledProcessid,
          callingElementType,
          calledElementType,
          userCreated: true
        }, {
          headers: {
            'Content-Type': 'application/json'
          }
        });
      } catch (error) {
        console.error("Error while adding connection:", error);
      }
    });

    paper.on("link:disconnect", async (linkView, _evt, prevElementView) => {
      const link = linkView.model;
      const connectionId = link?.attributes?.connectionId;
      const callingProcessid = link?.source()?.id?.toString();
      const calledProcessid = prevElementView.model.id.toString();
      if (!callingProcessid || !calledProcessid || !connectionId) {
        console.error("Error while removing connection");
        return;
      }
      await removeLinkHelper(connectionId, callingProcessid, calledProcessid);
    });

    const removeProcessLink = async (connectionId: number): Promise<boolean> => {
      try {
        await axios.delete(`/api/project/process-map/process-connection/${connectionId}`);
        return true;
      } catch (error) {
        console.error("Error while removing process connection:", error);
        return false;
      }
    }

    const removeDataStoreLink = async (connectionId: number): Promise<boolean> => {
      try {
        await axios.delete(`/api/project/process-map/datastore-connection/${connectionId}`);
        return true;
      } catch (error) {
        console.error("Error while removing datastore connection:", error);
        return false;
      }
    }

    const removeLink = async (_evt: dia.Event, linkView: dia.LinkView, toolView: dia.ToolView): Promise<void> => {
      const link = linkView.model;
      const connectionId = link?.attributes?.connectionId;
      const callingProcessid = link?.source()?.id?.toString();
      const calledProcessid = link?.target()?.id?.toString();
      if (!callingProcessid || !calledProcessid || !connectionId) {
        console.error("Error while removing connection");
        return;
      }
      const wasLinkRemoved = await removeLinkHelper(connectionId, callingProcessid, calledProcessid);
      if (!wasLinkRemoved) {
        return;
      }
      linkView.model.remove({ ui: true, tool: toolView.cid });
    }

    const removeLinkHelper = async (connectionId: number, callingProcessid: string, calledProcessid: string): Promise<boolean> => {
      if (callingProcessid.startsWith('ds') || calledProcessid.startsWith('ds')) {
        return await removeDataStoreLink(connectionId);
      }

      return await removeProcessLink(connectionId);
    }
  },
  methods: {
    saveGraphState() {
      setTimeout(() => {
        this.appStore.setGraphForProject(this.selectedProjectId, JSON.stringify(graph));
      }, 50);
    },
    saveFilters() {
      this.toolbar.saveFilters();
    },
    saveHiddenElements() {
      this.appStore.setHiddenCellsForProject(this.selectedProjectId, JSON.stringify(this.hiddenCells));
      this.appStore.setHiddenLinksForProject(this.selectedProjectId, this.hiddenLinks);
    },
    saveHiddenPorts() {
      this.appStore.setHiddenPortsForProject(this.selectedProjectId, JSON.stringify(this.hiddenPorts));
    },
    resetFilters() {
      this.toolbar.clearFilters();
      this.hiddenCells = [];
      this.hiddenPorts = {};
      this.saveFilters();
      this.saveHiddenElements();
      this.saveHiddenPorts();
    },
    fetchProcessModels() {
      this.resetFilters();
      graph.clear();
      axios.get("/api/project/" + this.selectedProjectId + "/process-map").then(result => {

        let abstractProcessShapes = result.data.processes.map((process: Process) => {

          const filterEmpty = (label: string) => !!label;

          this.portsInformation['start-' + process.id] = process.startEvents.filter(event => filterEmpty(event.label)).map(e => e.label);
          this.portsInformation['i-catch-event-' + process.id] = process.intermediateCatchEvents.filter(event => filterEmpty(event.label)).map(e => e.label);
          this.portsInformation['i-throw-event-' + process.id] = process.intermediateThrowEvents.filter(event => filterEmpty(event.label)).map(e => e.label);
          this.portsInformation['end-' + process.id] = process.endEvents.filter(event => filterEmpty(event.label)).map(e => e.label);
          this.portsInformation['call-' + process.id] = process.activities.filter(event => filterEmpty(event.label)).map(e => e.label);

          return createAbstractProcessElement(process.name, process.id);
        });

        this.appStore.setPortsInformationByProject(this.selectedProjectId, this.portsInformation);

        graph.addCell(abstractProcessShapes);

        let connectionsShapes = result.data.connections.map((connection: Connection) => {

          const link = new shapes.standard.Link();

          const callingPortPrefix = getPortPrefix(connection.callingElementType);
          const calledPortPrefix = getPortPrefix(connection.calledElementType);

          link.set({
            connectionId: connection.id,
            source: { id: connection.callingProcessid, port: callingPortPrefix + connection.callingProcessid },
            target: { id: connection.calledProcessid, port: calledPortPrefix + connection.calledProcessid }
          })

          if (!!connection.label) {
            link.appendLabel({
              attrs: {
                text: {
                  text: connection.label
                }
              }
            });
          }

          return link;
        });

        graph.addCell(connectionsShapes);

        let abstractDataStores = result.data.dataStores.map((dataStore: DataStore) => {
          return createAbstractDataStoreElement(dataStore.name, dataStore.id);
        })

        graph.addCell(abstractDataStores);

        let dataStoreConnectionShapes = result.data.dataStoreConnections.map((connection: DataStoreConnection) => {

          const link = new shapes.standard.Link();
          const source = { id: connection.processid, port: "call-" + connection.processid };
          const target = { id: "ds-" + connection.dataStoreId, anchor: { name: 'midSide', args: { rotate: true, } } };

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

            link.set({ connectionId: connection.id, source, target });
          } else if (connection.access === "WRITE") {
            link.set({ connectionId: connection.id, source, target });
          } else if (connection.access === "READ") {
            link.set({ connectionId: connection.id, source: target, target: source });
          }

          return link;
        });

        graph.addCell(dataStoreConnectionShapes);



        DirectedGraph.layout(graph, {
          nodeSep: 80,
          edgeSep: 100,
          rankSep: 80,
          rankDir: "LR",
        });

        setTimeout(this.fitToScreen, 1);

        this.saveGraphState();
      });
    },
    getProcessElementType(portId: string): ProcessElementType | null {
      const mappings: { [key: string]: ProcessElementType } = {
        'start-': ProcessElementType.START_EVENT,
        'i-catch-event-': ProcessElementType.INTERMEDIATE_CATCH_EVENT,
        'i-throw-event-': ProcessElementType.INTERMEDIATE_THROW_EVENT,
        'end-': ProcessElementType.END_EVENT,
        'call-': ProcessElementType.CALL_ACTIVITY
      };

      for (const prefix in mappings) {
        if (portId.startsWith(prefix)) {
          return mappings[prefix];
        }
      }

      return null;
    },
    filterGraph({
                  hideAbstractDataStores,
                  hideCallActivities,
                  hideIntermediateEvents,
                  hideStartEndEvents,
                  hideProcessesWithoutConnections,
                  hideConnectionLabels
                }: FilterGraphInput) {

      if (!hideConnectionLabels) {
        for (const link of graph.getLinks()) {
          const label = this.hiddenLinks[link.id];
          if (!!label) {
            link.appendLabel({
              attrs: {
                text: {
                  text: label
                }
              }
            });
          }
        }
      }

      graph.addCells(this.hiddenCells.map(cell => cell?.attributes ? cell.attributes : cell) as dia.Cell[]);
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

      if (hideConnectionLabels) {
        for (const link of graph.getLinks()) {
          const labelText = link.labels()[0]?.attrs!.text!.text;
          if (!!labelText) {
            this.hiddenLinks[link.id] = labelText;
          }
          link.removeLabel();
        }
      }

      this.saveGraphState();
      this.saveFilters();
      this.saveHiddenElements();
      this.saveHiddenPorts();
    },
    fitToScreen() {
      this.navigationButtons.fitToScreen();
    }
  }
})
</script>
