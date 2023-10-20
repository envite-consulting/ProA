<template>
  <v-card height="100%">

    <div id="cyto-graph" class="cyto-diagram"></div>

  </v-card>
  <v-dialog v-model="dialog" width="auto">
    <v-card>
      <v-card-title class="text-h5">
        Prozess: {{ selectedProcess.processName }}
      </v-card-title>
      <v-card-text>
        Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore
        magna aliqua.
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn color="green-darken-1" variant="text" @click="$router.push('/ProcessView/' + selectedProcess.id)">
          Prozess öffnen
        </v-btn>
        <v-btn color="primary" variant="text" @click="dialog = false">Schließen</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>
<style scoped>
.cyto-diagram {
  height: 100%;
  width: 100%;
}
</style>
<script lang="ts">
import { defineComponent } from 'vue'
import cytoscape, { ElementDefinition } from 'cytoscape';
import axios from 'axios';

interface Process {
  id: string
  processName: string
}

interface Connection {
  callingProcessid: number
  calledProcessid: number
}

export default defineComponent({
  data: () => ({
    cy: null,
    dialog: false,
    processMapElements: [] as ElementDefinition[],
    selectedProcess: {} as Process,
  }),

  watch: {
  },
  mounted: function () {
    this.fetchProcessModels();

  },
  methods: {

    fetchProcessModels() {
      axios.get("/api/process-map").then(result => {
        let processes = result.data.processes.map((process: Process) => { return { data: { id: process.id, name: process.processName } } });
        console.log(processes)
        this.processMapElements.push(...processes);
        let connections = result.data.connections.map((connection: Connection) => {
          return {
            data: {
              id: "edge-" + connection.callingProcessid + "-" + connection.calledProcessid,
              source: connection.callingProcessid,
              target: connection.calledProcessid,
            }
          }
        });
        this.processMapElements.push(...connections);
        this.createProcessMapGraph();
      })
    },

    createProcessMapGraph() {

      const component = this;

      var cy = cytoscape({

        container: document.getElementById('cyto-graph'), // container to render in
        elements: component.processMapElements,

        style: [ // the stylesheet for the graph
          {
            selector: 'node',
            style: {
              'background-color': '#666',
              'label': 'data(name)'
            }
          },

          {
            selector: 'edge',
            style: {
              'width': 3,
              'line-color': '#ccc',
              'target-arrow-color': '#ccc',
              'target-arrow-shape': 'triangle',
              'curve-style': 'bezier'
            }
          }
        ],

        layout: {
          name: 'grid',
          rows: 1
        }

      });

      cy.on('click', 'node', function (evt: any) {
        var evtTarget = evt.target;
        const selectedProcess = component.processMapElements.find(element => element.data.id === evtTarget.id());
        component.selectedProcess = {} as Process;
        component.selectedProcess = { id: selectedProcess!.data!.id!, processName: selectedProcess?.data.name };
        component.dialog = true;
      });
    }
  }
})
</script>
