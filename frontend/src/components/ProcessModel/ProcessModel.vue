<template>
  <v-card height="100%">
    <div
      v-if="isFetching"
      class="d-flex align-center justify-center w-100 h-75"
    >
      <div class="d-flex flex-column align-center justify-center">
        <span class="mb-2 mx-5 text-center">{{
          $t("processView.loadingProcessModel")
        }}</span>
        <v-progress-circular indeterminate />
      </div>
    </div>
    <div id="process-modelling" class="full-screen"></div>
    <div
      class="ma-4"
      style="
        position: absolute;
        top: 8px;
        right: 8px;
        display: flex;
        align-items: center;
        gap: 20px;
      "
    >
      <div>
        <v-chip
          v-if="!processType.PARTICIPANT"
          prepend-icon="mdi-file"
          class="ma-1"
          variant="tonal"
        >
          {{ processName }}
        </v-chip>
        <v-chip
          v-if="relatedProcessModels.length > 0"
          prepend-icon="mdi-layers"
          color="#EA9843"
          class="ma-1"
          variant="flat"
        >
          {{ level }}
        </v-chip>
      </div>
      <v-select
        v-if="relatedProcessModels.length > 0"
        density="compact"
        v-model="selectedProcessModel"
        :items="formattedProcessModels"
        item-title="displayTitle"
        item-value="relatedProcessModelId"
        @change="onProcessModelChange"
        style="width: 300px"
        variant="outlined"
      >
        <template #label>
          <v-icon style="margin-right: 5px">mdi-swap-vertical</v-icon>
          {{ $t("processView.changeLevel") }}
        </template>
      </v-select>
    </div>
    <div class="ma-4" style="position: absolute; bottom: 8px; right: 8px">
      <v-fab-transition style="margin-right: 5px">
        <v-btn
          class="mt-auto pointer-events-initial"
          color="primary"
          elevation="8"
          icon="mdi-chevron-left"
          @click="goLeft"
          size="large"
        />
      </v-fab-transition>
      <v-fab-transition style="margin-right: 5px">
        <v-btn
          class="mt-auto pointer-events-initial"
          color="primary"
          elevation="8"
          icon="mdi-chevron-right"
          @click="goRight"
          size="large"
        />
      </v-fab-transition>
      <v-fab-transition style="margin-right: 5px">
        <v-btn
          class="mt-auto pointer-events-initial"
          color="primary"
          elevation="8"
          icon="mdi-chevron-up"
          @click="goUp"
          size="large"
        />
      </v-fab-transition>
      <v-fab-transition style="margin-right: 5px">
        <v-btn
          class="mt-auto pointer-events-initial"
          color="primary"
          elevation="8"
          icon="mdi-chevron-down"
          @click="goDown"
          size="large"
        />
      </v-fab-transition>
      <v-fab-transition style="margin-right: 5px">
        <v-btn
          class="mt-auto pointer-events-initial"
          color="primary"
          elevation="8"
          icon="mdi-magnify-plus"
          @click="zoomIn"
          size="large"
        />
      </v-fab-transition>
      <v-fab-transition style="margin-right: 5px">
        <v-btn
          class="mt-auto pointer-events-initial"
          color="primary"
          elevation="8"
          icon="mdi-magnify-minus"
          @click="zoomOut"
          size="large"
        />
      </v-fab-transition>
      <v-fab-transition style="margin-right: 5px">
        <v-btn
          class="mt-auto pointer-events-initial"
          color="primary"
          elevation="8"
          icon="mdi-fit-to-screen"
          @click="fitToScreen"
          size="large"
        />
      </v-fab-transition>
    </div>
    <div class="ma-4" style="position: absolute; top: 8px; left: 8px">
      <v-btn prepend-icon="mdi-arrow-left" @click="goBack">
        {{ $t("processView.back") }}
      </v-btn>
    </div>
  </v-card>
</template>
<style>
.full-screen {
  width: 100%;
  height: 100%;
  padding-top: 100px;
  padding-bottom: 100px;
}
</style>
<script lang="ts">
import { defineComponent } from "vue";
import NavigatedViewer from "bpmn-js/lib/NavigatedViewer";
import ElementRegistry from "diagram-js/lib/core/ElementRegistry";
import axios from "axios";
import { Canvas } from "bpmn-js/lib/features/context-pad/ContextPadProvider";
import { useAppStore } from "@/store/app";
import { authHeader } from "@/components/Authentication/authHeader";

enum ProcessType {
  COLLABORATION = "COLLABORATION",
  PARTICIPANT = "PARTICIPANT",
  PROCESS = "PROCESS"
}

export default defineComponent({
  data: () => ({
    canvas: null as Canvas | null,
    scrollStep: 20,
    store: useAppStore(),
    zoomInMultiplier: 1.1,
    zoomOutMultiplier: 0.9,
    isFetching: false as boolean,
    level: 0 as number,
    processName: "" as string,
    processType: ProcessType,
    relatedProcessModels: [] as Array<{
      relatedProcessModelId: number;
      processName: string;
      level: number;
    }>,
    selectedProcessModel: null as { displayTitle: string } | null
  }),

  async mounted() {
    this.isFetching = true;
    this.addKeydownListener();

    const container = document.querySelector(
      "#process-modelling"
    ) as HTMLElement;
    const viewer = new NavigatedViewer({
      container
    });

    this.canvas = viewer.get("canvas") as Canvas;

    const processXmlUrl = "/api/process-model/" + this.$route.params.id;
    const processInformationUrl =
      "/api/project/" +
      this.store.getSelectedProjectId() +
      "/process-model/" +
      this.$route.params.id;

    try {
      const xmlResponse = await axios.get(processXmlUrl, {
        headers: authHeader()
      });
      const xmlText = xmlResponse.data;
      await viewer.importXML(xmlText);

      const processModelResponse = await axios.get(processInformationUrl, {
        headers: authHeader()
      });
      const processModel = processModelResponse.data;

      this.processName = processModel.processName;
      this.level = processModel.level;
      this.processType = processModel.processType;
      this.relatedProcessModels = processModel.relatedProcessModels.map(
        (relatedProcessModel: any) => ({
          relatedProcessModelId: relatedProcessModel.relatedProcessModelId,
          processName: relatedProcessModel.processName,
          level: relatedProcessModel.level
        })
      );
    } catch (error) {
      console.log(error);
    }

    this.canvas.zoom("fit-viewport", "auto");
    const portId = this.$route.query.portId as string;
    if (portId) {
      const elementRegistry = viewer.get<ElementRegistry>("elementRegistry");
      const port = elementRegistry.get(portId);
      if (port) {
        this.translateToCenter(port);
      }
      this.removeQueryParams();
    }

    this.isFetching = false;
  },

  beforeUnmount() {
    this.removeKeydownListener();
  },

  computed: {
    isUserLoggedIn() {
      return this.store.getUserToken() != null;
    },
    formattedProcessModels() {
      return this.relatedProcessModels.map((relatedProcessModel) => ({
        ...relatedProcessModel,
        displayTitle: `${this.$t("processView.level")} ${relatedProcessModel.level} – ${relatedProcessModel.processName}`
      }));
    }
  },

  watch: {
    isUserLoggedIn(newValue) {
      if (!newValue) {
        this.$router.push("/");
      }
    },
    selectedProcessModel(newValue) {
      if (newValue) {
        this.onProcessModelChange(newValue);
      }
    }
  },

  methods: {
    onProcessModelChange(selectedProcessModelId: number) {
      if (selectedProcessModelId) {
        this.$router.push("/ProcessView/" + selectedProcessModelId);
      }
    },
    translateToCenter(port: any) {
      const viewbox = this.canvas.viewbox();
      const elementBounds =
        port.width && port.height ? port : this.canvas.getBBox(port);
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
      this.canvas.zoom(currScale * this.zoomInMultiplier, "auto");
    },
    zoomOut() {
      const currScale = this.canvas.viewbox().scale;
      this.canvas.zoom(currScale * this.zoomOutMultiplier, "auto");
    },
    fitToScreen() {
      this.canvas.zoom("fit-viewport", "auto");
    },
    goRight() {
      const { x, y, width, height } = this.canvas.viewbox();
      this.canvas.viewbox({ x: x - this.scrollStep, y, width, height });
    },
    goLeft() {
      const { x, y, width, height } = this.canvas.viewbox();
      this.canvas.viewbox({ x: x + this.scrollStep, y, width, height });
    },
    goUp() {
      const { x, y, width, height } = this.canvas.viewbox();
      this.canvas.viewbox({ x, y: y + this.scrollStep, width, height });
    },
    goDown() {
      const { x, y, width, height } = this.canvas.viewbox();
      this.canvas.viewbox({ x, y: y - this.scrollStep, width, height });
    },
    removeQueryParams() {
      const url = new URL(window.location.href);
      url.searchParams.delete("portId");
      window.history.replaceState({}, "", url.pathname + url.search);
    },
    goBack() {
      this.removeQueryParams();
      this.$router.go(-1);
    },
    addKeydownListener() {
      window.addEventListener("keydown", this.onKeyDown);
    },
    removeKeydownListener() {
      window.removeEventListener("keydown", this.onKeyDown);
    },
    onKeyDown(evt: KeyboardEvent) {
      switch (evt.key) {
        case "ArrowLeft":
          this.goLeft();
          break;
        case "ArrowRight":
          this.goRight();
          break;
        case "ArrowUp":
          this.goUp();
          break;
        case "ArrowDown":
          this.goDown();
          break;
        case "+":
          this.zoomIn();
          break;
        case "-":
          this.zoomOut();
          break;
        case "f":
          this.fitToScreen();
          break;
      }
    }
  }
});
</script>
