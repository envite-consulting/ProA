<template>
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
      <v-btn class="mt-auto pointer-events-initial" color="primary" elevation="8" icon="mdi-chevron-up"
             @click="goUp"
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
</template>

<style scoped>

</style>

<script lang="ts">
import { defineComponent } from 'vue'

import { paper } from "@/components/ProcessMap/jointjs/JointJSDiagram";
import { useAppStore } from "@/store/app";
import { dia } from "@joint/core";
import TransformToFitContentOptions = dia.Paper.TransformToFitContentOptions;

const transformFitToContentOptions: TransformToFitContentOptions = {
  horizontalAlign: 'middle',
  padding: 20,
  verticalAlign: 'middle'
}

export default defineComponent({
  name: "NavigationButtons",

  props: {
    selectedProjectId: {
      type: Number,
      required: true
    }
  },

  data() {
    return {
      scrollStep: 20,
      zoomInMultiplier: 1.3,
      zoomOutMultiplier: 0.7
    }
  },

  mounted() {
    const savePaperLayout = this.savePaperLayout;

    paper.on('paper:pinch', function (evt, x, y, sx) {
      evt.preventDefault();
      const { sx: sx0 } = paper.scale();
      paper.scaleUniformAtPoint(sx0 * sx, { x, y });
      savePaperLayout();
    });

    paper.on('paper:pan', function (evt, tx, ty) {
      evt.preventDefault();
      evt.stopPropagation();
      const { tx: tx0, ty: ty0 } = paper.translate();
      paper.translate(tx0 - tx, ty0 - ty);
      savePaperLayout();
    });
  },

  methods: {
    goLeft() {
      const { tx: tx0, ty: ty0 } = paper.translate();
      paper.translate(tx0 + this.scrollStep, ty0);
      this.savePaperLayout();
    },
    goRight() {
      const { tx: tx0, ty: ty0 } = paper.translate();
      paper.translate(tx0 - this.scrollStep, ty0);
      this.savePaperLayout();
    },
    goUp() {
      const { tx: tx0, ty: ty0 } = paper.translate();
      paper.translate(tx0, ty0 + this.scrollStep);
      this.savePaperLayout();
    },
    goDown() {
      const { tx: tx0, ty: ty0 } = paper.translate();
      paper.translate(tx0, ty0 - this.scrollStep);
      this.savePaperLayout();
    },
    zoomIn() {
      const { sx: sx0 } = paper.scale();
      paper.scale(sx0 * this.zoomInMultiplier);
      this.savePaperLayout();
    },
    zoomOut() {
      const { sx: sx0 } = paper.scale();
      paper.scale(sx0 * this.zoomOutMultiplier);
      this.savePaperLayout();
    },
    fitToScreen() {
      paper.transformToFitContent(transformFitToContentOptions);
      this.savePaperLayout();
    },
    savePaperLayout() {
      useAppStore().setPaperLayoutForProject(this.selectedProjectId, JSON.stringify({
        sx: paper.scale().sx,
        tx: paper.translate().tx,
        ty: paper.translate().ty
      }));
    },
  }
});
</script>
