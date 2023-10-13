<template>
  <v-card height="100%">

    <div id="cyto-graph" class="example"></div>

  </v-card>
  <v-dialog v-model="dialog" width="auto">
    <v-card>
      <v-card-title class="text-h5">
        Prozess: Bewerbungs-Wf
      </v-card-title>
      <v-card-text>
        Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore
        magna aliqua.
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn color="green-darken-1" variant="text" @click="$router.push('ProcessView')">Prozess öffnen
        </v-btn>
        <v-btn color="primary" variant="text" @click="dialog = false">Schließen</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>
<style scoped>
.example {
  /*background-color: red;*/
  height: 100%;
  width: 100%;
}
</style>
<script lang="ts">
import { defineComponent } from 'vue'
import cytoscape from 'cytoscape';

export default defineComponent({
  data: () => ({
    cy: null,
    dialog: false,
  }),

  watch: {
  },
  mounted: function () {
    console.log("mounted");
    var cy = cytoscape({

      container: document.getElementById('cyto-graph'), // container to render in

      elements: [ // list of graph elements to start with
        { // node a
          data: { id: 'a', name: "hello1" }
        },
        { // node b
          data: { id: 'b', name: "hello2" }
        },
        { // edge ab
          data: { id: 'ab', source: 'a', target: 'b' }
        },
        { // edge ab
          data: { id: 'ab2', source: 'a', target: 'b' }
        },
        { // edge ab
          data: { id: 'ba', source: 'b', target: 'a' }
        }
      ],

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

    const component = this;
    cy.on('click', 'node', function (evt: any) {
      console.log('clicked ' + this.id());
      component.dialog = true;
    });
  }
})
</script>
