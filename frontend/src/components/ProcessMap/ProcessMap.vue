<template>
  <ProcessDetailDialog ref="processDetailDialog" />
  <v-card height="100%">

    <div id="cyto-graph" class="cyto-diagram"></div>

  </v-card>
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
import ProcessDetailDialog from '@/components/ProcessDetailDialog.vue';

interface Process {
  id: string
  processName: string
}

interface Connection {
  callingProcessid: number
  calledProcessid: number
}

export default defineComponent({
  components: {
    ProcessDetailDialog
  },
  data: () => ({
    cy: null,
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

      cy.layout({ name: 'breadthfirst' }).run();

      cy.on('click', 'node', function (evt: any) {
        var evtTarget = evt.target;
        (component.$refs.processDetailDialog as InstanceType<typeof ProcessDetailDialog>).showProcessInfoDialog(evtTarget.id());
      });
    }
  }
})
</script>
