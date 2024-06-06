<template>
  <v-dialog v-model="infoDialog" persistent width="600">
    <v-card>
      <v-card-title>
        <span class="text-h5">Prozessmodell: {{ details.name }}</span>
      </v-card-title>
      <v-card-text>
        <v-container>
          <v-row>
            <v-col cols="12" sm="6" md="6">
              <b>Start Events</b>
              <ul class="mt-1">
                <li v-for="(start, index) in details.startEvents" :key="'startEvent-' + index" class="mb-2">
                  <v-chip @click="goToProcessModel(start.elementId)">{{ start.label || 'Start' }}
                  </v-chip>
                </li>
              </ul>
            </v-col>
            <v-col cols="12" sm="6" md="6">
              <b class="mb-2">End Events</b>
              <ul class="mt-1">
                <li v-for="(end, index) in details.endEvents" :key="'endEvent-' + index" class="mb-2">
                  <v-chip @click="goToProcessModel(end.elementId)">{{ end.label || 'Ende' }}
                  </v-chip>
                </li>
              </ul>
            </v-col>
          </v-row>
          <div v-if="details.description" class="px-3 pb-3">
            <v-row>
              <b>Beschreibung:</b>
            </v-row>
            <v-row>
              <p class="description-text">{{ details.description }}</p>
            </v-row>
          </div>
          <div id="process-model-viewer" class="mt-4"></div>
        </v-container>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn color="blue-darken-1" variant="text" @click="goToProcessModel(null)">
          Prozessmodell
        </v-btn>
        <v-btn color="blue-darken-1" variant="text" @click="infoDialog = false">
          Schließen
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>
<style scoped>
li {
  list-style-type: none;
}

#process-model-viewer {
  height: 20vh;
}

.description-text {
  word-break: break-word;
  white-space: pre-wrap;
}
</style>
<script lang="ts">
import { defineComponent } from 'vue';
import axios from 'axios';
import { dia } from "@joint/core";
import BpmnViewer from "bpmn-js";

export interface Process {
  id: number,
  name: string
  description: string
  startEvents: Event[]
  endEvents: Event[]
}

declare interface Event {
  elementId: string,
  label: string
}

interface RouteObject {
  path: string,
  query?: {
    portId: string
  }
}

export default defineComponent({
  data: () => ({
    infoDialog: false,
    details: {} as Process
  }),

  methods: {
    async showProcessInfoDialog(processId: number) {
      this.infoDialog = true;
      await this.$nextTick();
      this.resetProcessModel();
      await this.fetchProcessModel(processId);
      axios.get("/api/process-model/" + processId + "/details").then(result => {
        this.details = result.data;
      })
    },
    async goToProcessModel(portId: string | null) {
      const routeObject: RouteObject = { path: '/ProcessView/' + this.details.id };
      if (portId) {
        routeObject.query = { portId };
      }
      this.$router.push(routeObject);
    },
    async fetchProcessModel(modelId: dia.Cell.ID) {
      const viewer = new BpmnViewer({
        container: '#process-model-viewer'
      });

      const url = '/api/process-model/' + modelId;
      const response = await axios.get(url);
      const xmlText = response.data;
      await viewer.importXML(xmlText);
      (viewer.get('canvas') as any).zoom('fit-viewport', 'auto');
    },
    resetProcessModel() {
      document.getElementById('process-model-viewer')!.innerHTML = '';
    }
  },
})

</script>
