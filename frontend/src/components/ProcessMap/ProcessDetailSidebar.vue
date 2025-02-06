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
      <div v-if="isFetching" class="d-flex align-center justify-center w-100 h-75">
        <div class="d-flex flex-column align-center justify-center">
          <span class="mb-2 mx-5 text-center">{{ $t('processDetailSidebar.loadingData') }}</span>
          <v-progress-circular indeterminate/>
        </div>
      </div>
      <div :hidden="isFetching">
        <v-container class="flex-shrink-0 py-0">
          <p class="text-h6 font-weight-regular">{{ details.name }}</p>
        </v-container>
        <v-container class="flex-grow-1 overflow-auto py-0">
          <v-list dense>
            <div v-if="details.startEvents && details.startEvents.length > 0" class="mb-2">
              <v-list-subheader>{{ $t('processDetailSidebar.startEvents') }}</v-list-subheader>
              <v-list-item v-for="(start, index) in details.startEvents" :key="'startEvent-' + index">
                <v-chip @click="goToProcessModel(start.elementId)"
                        @mouseenter="model!.highlightPort(ProcessElementType.START_EVENT)"
                        @mouseleave="model!.unhighlightPort(ProcessElementType.START_EVENT)">{{ start.label || 'Start' }}
                </v-chip>
              </v-list-item>
            </div>

            <div v-if="details.endEvents && details.endEvents.length > 0" class="mb-2">
              <v-list-subheader>{{ $t('processDetailSidebar.endEvents') }}</v-list-subheader>
              <v-list-item v-for="(end, index) in details.endEvents" :key="'endEvent' + index">
                <v-chip @click="goToProcessModel(end.elementId)"
                        @mouseenter="model!.highlightPort(ProcessElementType.END_EVENT)"
                        @mouseleave="model!.unhighlightPort(ProcessElementType.END_EVENT)">{{
                    end.label || $t('general.end')
                  }}
                </v-chip>
              </v-list-item>
            </div>

            <div v-if="details.intermediateCatchEvents && details.intermediateCatchEvents.length > 0" class="mb-2">
              <v-list-subheader>{{ $t('general.intermediateCatchEvents') }}</v-list-subheader>
              <v-list-item v-for="(event, index) in details.intermediateCatchEvents" :key="'endEvent' + index">
                <v-chip @click="goToProcessModel(event.elementId)"
                        @mouseenter="model!.highlightPort(ProcessElementType.INTERMEDIATE_CATCH_EVENT)"
                        @mouseleave="model!.unhighlightPort(ProcessElementType.INTERMEDIATE_CATCH_EVENT)">
                  {{ event.label || $t('general.intermediateEvent') }}
                </v-chip>
              </v-list-item>
            </div>

            <div v-if="details.intermediateThrowEvents && details.intermediateThrowEvents.length > 0" class="mb-2">
              <v-list-subheader>{{ $t('general.intermediateThrowEvents') }}</v-list-subheader>
              <v-list-item v-for="(event, index) in details.intermediateThrowEvents"
                           :key="'intermediateThrowEvent' + index">
                <v-chip @click="goToProcessModel(event.elementId)"
                        @mouseenter="model!.highlightPort(ProcessElementType.INTERMEDIATE_THROW_EVENT)"
                        @mouseleave="model!.unhighlightPort(ProcessElementType.INTERMEDIATE_THROW_EVENT)">
                  {{ event.label || $t('general.intermediateEvent') }}
                </v-chip>
              </v-list-item>
            </div>

            <div v-if="details.activities && details.activities.length > 0" class="mb-2">
              <v-list-subheader>{{ $t('general.callActivities') }}</v-list-subheader>
              <v-list-item v-for="(activity, index) in details.activities" :key="'activity' + index">
                <v-chip @click="goToProcessModel(activity.elementId)"
                        @mouseenter="model!.highlightPort(ProcessElementType.CALL_ACTIVITY)"
                        @mouseleave="model!.unhighlightPort(ProcessElementType.CALL_ACTIVITY)">
                  {{ activity.label || $t('general.activity') }}
                </v-chip>
              </v-list-item>
            </div>

            <div v-if="details.description" class="mb-4">
              <v-list-subheader>{{ $t('general.description') }}</v-list-subheader>
              <v-list-item>
                {{ details.description }}
              </v-list-item>
            </div>
          </v-list>
        </v-container>
      </div>
        <v-container class="flex-shrink-0 text-center mb-4">
          <div id="process-model-viewer" class="mb-4"></div>
          <v-btn color="blue-darken-1" variant="text" @click="goToProcessModel(null)">
            {{ $t('processDetailSidebar.goToProcessModel') }}
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
import { ProcessElementType, RouteObject } from "./types";
import { authHeader } from "@/components/Authentication/authHeader";

export default defineComponent({
  name: 'ProcessDetailSidebar',
  computed: {
    ProcessElementType() {
      return ProcessElementType
    }
  },
  data: () => ({
    showSidebar: false as boolean,
    details: {} as Process,
    model: null as AbstractProcessShape | null,
    isFetching: false as boolean
  }),

  methods: {
    async open(model: AbstractProcessShape) {
      this.isFetching = true;
      this.showSidebar = true;
      await this.$nextTick();
      this.model = model;
      const modelId = model.id.toString();
      this.resetProcessModel();
      await this.fetchProcessModel(modelId);
      axios.get("/api/process-model/" + modelId + "/details", { headers: authHeader() }).then(result => {
        this.details = result.data;
        this.isFetching = false;
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
      const response = await axios.get(url, { headers: authHeader() });
      const xmlText = response.data;
      await viewer.importXML(xmlText);
      (viewer as any).get('canvas').zoom('fit-viewport', 'auto');
    },
    resetProcessModel() {
      document.getElementById('process-model-viewer')!.innerHTML = '';
    },
    unhighlightPorts() {
      if (this.model) {
        for (const elementType of Object.values(ProcessElementType)) {
          this.model.unhighlightPort(elementType);
        }
      }
    },
    saveGraphState() {
      this.$emit('saveGraphState');
    }
  }
});
</script>
