<template>
  <v-card height="100%">

    <div id="process-modelling" class="full-screen"></div>
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
    <div class="ma-4" style="position: absolute; top: 8px; left: 8px;">
      <v-btn prepend-icon="mdi-arrow-left"
             @click="goBack">
        Zurück
      </v-btn>
    </div>
  </v-card>
</template>
<style>
.full-screen {
  width: 100%;
  height: 100%;
}
</style>
<script lang="ts">
import { defineComponent } from 'vue';
import NavigatedViewer from "bpmn-js/lib/NavigatedViewer";
import ElementRegistry from 'diagram-js/lib/core/ElementRegistry';
import axios from 'axios';
import { Canvas } from "bpmn-js/lib/features/context-pad/ContextPadProvider";

const scrollStep = 20;

export default defineComponent({
  data: () => ({
    canvas: null as Canvas | null
  }),

  async mounted() {
    const container = document.querySelector('#process-modelling') as HTMLElement;
    const viewer = new NavigatedViewer({
      container
    });

    this.canvas = viewer.get('canvas') as Canvas;

    const url = '/api/process-model/' + this.$route.params.id;
    try {
      const response = await axios.get(url);
      const xmlText = response.data;
      await viewer.importXML(xmlText);
    } catch (error) {
      console.log(error);
    }

    this.canvas.zoom('fit-viewport', 'auto');
    const portId = this.$route.query.portId as string;
    if (portId) {
      const elementRegistry = viewer.get<ElementRegistry>('elementRegistry');
      const port = elementRegistry.get(portId);
      if (port) {
        this.translateToCenter(port);
      }
      this.removeQueryParams();
    }
  },

  methods: {
    translateToCenter(port: any) {
      const viewbox = this.canvas.viewbox();
      const elementBounds = port.width && port.height ? port : this.canvas.getBBox(port);
      const elementCenter = {
        x: elementBounds.x + elementBounds.width / 2,
        y: elementBounds.y + elementBounds.height / 2
      };
      const newViewbox = {
        x: elementCenter.x - viewbox.width / 2,
        y: elementCenter.y - viewbox.height / 2,
        width: viewbox.width,
        height: viewbox.height
      };
      this.canvas.viewbox(newViewbox);
    },
    zoomIn() {
      const currScale = this.canvas.viewbox().scale;
      this.canvas.zoom(currScale * 1.1, 'auto');
    },
    zoomOut() {
      const currScale = this.canvas.viewbox().scale;
      this.canvas.zoom(currScale / 1.1, 'auto');
    },
    fitToScreen() {
      this.canvas.zoom('fit-viewport', 'auto');
    },
    goRight() {
      const { x, y, width, height } = this.canvas.viewbox();
      this.canvas.viewbox({ x: x + scrollStep, y, width, height });
    },
    goLeft() {
      const { x, y, width, height } = this.canvas.viewbox();
      this.canvas.viewbox({ x: x - scrollStep, y, width, height });
    },
    goUp() {
      const { x, y, width, height } = this.canvas.viewbox();
      this.canvas.viewbox({ x, y: y - scrollStep, width, height });
    },
    goDown() {
      const { x, y, width, height } = this.canvas.viewbox();
      this.canvas.viewbox({ x, y: y + scrollStep, width, height });
    },
    removeQueryParams() {
      const url = new URL(window.location.href);
      url.searchParams.delete('portId');
      window.history.replaceState({}, '', url.pathname + url.search);
    },
    goBack() {
      this.removeQueryParams();
      this.$router.go(-1);
    }
  }
})
</script>
