<template>
  <v-navigation-drawer
    location="right"
    permanent
    v-model="showSidebar"
  >
    <div class="d-flex flex-column h-100">
      <div class="btn-container">
        <v-btn icon="mdi-arrow-collapse-right" variant="text" class="ms-2" @click="close"></v-btn>
      </div>
      <v-container class="flex-shrink-0 py-0">
        <p class="text-h6 font-weight-regular">{{ details.name }}</p>
      </v-container>
      <v-container class="flex-grow-1 overflow-auto py-0">
        <v-list dense>
          <div v-if="details.startEvents && details.startEvents.length > 0" class="mb-2">
            <v-list-subheader>Startaktivitäten</v-list-subheader>
            <v-list-item v-for="(start, index) in details.startEvents" :key="'startEvent-' + index">
              <v-chip @click="goToProcessModel(start.elementId)" @mouseenter="model!.highlightStartPort()"
                      @mouseleave="model!.unhighlightStartPort()">{{ start.label || 'Start' }}
              </v-chip>
            </v-list-item>
          </div>

          <div class="mb-2">
            <v-list-subheader>Endaktivitäten</v-list-subheader>
            <v-list-item v-for="(end, index) in details.endEvents" :key="'endEvent' + index">
              <v-chip @click="goToProcessModel(end.elementId)" @mouseenter="model!.highlightEndPort()"
                      @mouseleave="model!.unhighlightEndPort()">{{ end.label || 'Ende' }}
              </v-chip>
            </v-list-item>
          </div>

          <div v-if="details.description" class="mb-4">
            <v-list-subheader>Beschreibung</v-list-subheader>
            <v-list-item>
              {{ details.description }}
            </v-list-item>
          </div>
        </v-list>
      </v-container>
      <v-container class="flex-shrink-0 text-center mb-4">
        <div id="process-model-viewer" class="mb-4"></div>
        <v-btn color="blue-darken-1" variant="text" @click="goToProcessModel(null)">
          Zum Prozessmodell
        </v-btn>
      </v-container>
    </div>
  </v-navigation-drawer>
</template>

<style scoped>
.v-list-item {
  padding: 0.25rem 0;
  padding-inline: 0 !important;
  min-height: 0;
}

.v-list-subheader {
  padding: 0.25rem 0;
  padding-inline: 0 !important;
  min-height: 0;
}

.v-list {
  padding: 0;
}

#process-model-viewer {
  height: 20vh;
}

.btn-container {
  min-height: 64px;
  height: 64px;
  display: flex;
  align-items: center;
}
</style>

<script lang="ts">
import { defineComponent } from 'vue';
import { dia } from '@joint/core';
import { Process } from '@/components/ProcessDetailDialog.vue';
import axios from 'axios';
import BpmnViewer from 'bpmn-js/lib/Viewer';
import { AbstractProcessShape } from "@/components/ProcessMap/jointjs/AbstractProcessElement";

interface RouteObject {
  path: string,
  query?: {
    portId: string
  }
}

export default defineComponent({
  name: 'ProcessDetailSidebar',
  data: () => ({
    showSidebar: false as boolean,
    details: {} as Process,
    model: null as AbstractProcessShape | null
  }),

  methods: {
    async open(model: AbstractProcessShape) {
      this.showSidebar = true;
      await this.$nextTick();
      this.model = model;
      const modelId = model.id.toString();
      this.resetProcessModel();
      await this.fetchProcessModel(modelId);
      axios.get("/api/process-model/" + modelId + "/details").then(result => {
        this.details = result.data;
      })
    },
    close() {
      this.showSidebar = false;
    },
    async goToProcessModel(portId: string | null) {
      this.unhighlightPorts();
      this.close();
      this.saveGraphState();
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
      (viewer as any).get('canvas').zoom('fit-viewport', 'auto');
    },
    resetProcessModel() {
      document.getElementById('process-model-viewer')!.innerHTML = '';
    },
    unhighlightPorts() {
      if (this.model) {
        this.model.unhighlightStartPort();
        this.model.unhighlightEndPort();
      }
    },
    saveGraphState() {
      this.$emit('saveGraphState');
    }
  }
});
</script>
