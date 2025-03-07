<template>
  <v-dialog
    v-model="infoDialog"
    persistent
    width="600"
    @after-leave="$emit('closed')"
  >
    <v-card>
      <v-card-title class="d-flex align-center">
        <span class="text-h5 text-wrap">
          <strong>
            {{
              currentProcessModel.processType === processType.COLLABORATION
                ? $t("processList.collaboration")
                : currentProcessModel.processType === processType.PARTICIPANT
                  ? $t("processList.participant")
                  : $t("general.processModel")
            }}:
          </strong>
          {{ currentProcessModel.processName }}
        </span>
        <v-spacer></v-spacer>
        <template
          v-if="
            currentProcessModel.processType &&
            currentProcessModel.processType !== processType.PARTICIPANT &&
            currentProcessModel.relatedProcessModels.length === 0 &&
            availableProcessModelsToAdd.length > 0
          "
        >
          <v-tooltip location="top">
            <template v-slot:activator="{ props }">
              <v-btn
                color="primary"
                icon="mdi-plus"
                size="small"
                v-bind="props"
                @click="openAddDialog"
              ></v-btn>
            </template>
            <span>{{ $t("general.addToRelatedProcessModels") }}</span>
          </v-tooltip>
        </template>
      </v-card-title>
      <v-card-text>
        <v-container>
          <v-row>
            <v-col v-if="relatedProcessModels.length > 0" cols="12">
              <v-switch
                v-model="showProcessLevels"
                :label="$t('general.showProcessLevels')"
                color="primary"
                hide-details
                density="comfortable"
              ></v-switch>
            </v-col>
            <v-col
              v-if="details.startEvents && details.startEvents.length > 0"
              cols="12"
              sm="6"
              md="6"
            >
              <b class="mb-2">{{ $t("general.startEvents") }}</b>
              <ul class="mt-1">
                <li
                  v-for="(start, index) in showProcessLevels
                    ? aggregateDetails.startEvents
                    : details.startEvents"
                  :key="'startEvent-' + index"
                  class="mb-2"
                >
                  <template v-if="showProcessLevels">
                    <v-badge
                      v-if="start.count > 1"
                      :content="start.count + 'x'"
                    >
                      <span class="badge-content">{{
                        start.label || "Start"
                      }}</span>
                    </v-badge>
                    <span v-else class="no-badge">{{
                      start.label || "Start"
                    }}</span>
                  </template>
                  <template v-else>
                    <v-chip @click="goToProcessModel(start.elementId)">
                      {{ start.label || "Start" }}
                    </v-chip>
                  </template>
                </li>
              </ul>
            </v-col>
            <v-col
              v-if="details.endEvents && details.endEvents.length > 0"
              cols="12"
              sm="6"
              md="6"
            >
              <b class="mb-2">{{ $t("general.endEvents") }}</b>
              <ul class="mt-1">
                <li
                  v-for="(end, index) in showProcessLevels
                    ? aggregateDetails.endEvents
                    : details.endEvents"
                  :key="'endEvent-' + index"
                  class="mb-2"
                >
                  <template v-if="showProcessLevels">
                    <v-badge v-if="end.count > 1" :content="end.count + 'x'">
                      <span class="badge-content">{{
                        end.label || $t("general.end")
                      }}</span>
                    </v-badge>
                    <span v-else class="no-badge">{{
                      end.label || $t("general.end")
                    }}</span>
                  </template>
                  <template v-else>
                    <v-chip @click="goToProcessModel(end.elementId)">
                      {{ end.label || $t("general.end") }}
                    </v-chip>
                  </template>
                </li>
              </ul>
            </v-col>
            <v-col
              v-if="
                details.intermediateCatchEvents &&
                details.intermediateCatchEvents.length > 0
              "
              cols="12"
              sm="6"
              md="6"
            >
              <b class="mb-2">{{ $t("general.intermediateCatchEvents") }}</b>
              <ul class="mt-1">
                <li
                  v-for="(event, index) in showProcessLevels
                    ? aggregateDetails.intermediateCatchEvents
                    : details.intermediateCatchEvents"
                  :key="'intermediateCatchEvent-' + index"
                  class="mb-2"
                >
                  <template v-if="showProcessLevels">
                    <v-badge
                      v-if="event.count > 1"
                      :content="event.count + 'x'"
                    >
                      <span class="badge-content">{{
                        event.label || $t("general.intermediateEvent")
                      }}</span>
                    </v-badge>
                    <span v-else class="no-badge">{{
                      event.label || $t("general.intermediateEvent")
                    }}</span>
                  </template>
                  <template v-else>
                    <v-chip @click="goToProcessModel(event.elementId)">
                      {{ event.label || $t("general.intermediateEvent") }}
                    </v-chip>
                  </template>
                </li>
              </ul>
            </v-col>
            <v-col
              v-if="
                details.intermediateThrowEvents &&
                details.intermediateThrowEvents.length > 0
              "
              cols="12"
              sm="6"
              md="6"
            >
              <b class="mb-2">{{ $t("general.intermediateThrowEvents") }}</b>
              <ul class="mt-1">
                <li
                  v-for="(event, index) in showProcessLevels
                    ? aggregateDetails.intermediateThrowEvents
                    : details.intermediateThrowEvents"
                  :key="'intermediateThrowEvent-' + index"
                  class="mb-2"
                >
                  <template v-if="showProcessLevels">
                    <v-badge
                      v-if="event.count > 1"
                      :content="event.count + 'x'"
                    >
                      <span class="badge-content">{{
                        event.label || $t("general.intermediateEvent")
                      }}</span>
                    </v-badge>
                    <span v-else class="no-badge">{{
                      event.label || $t("general.intermediateEvent")
                    }}</span>
                  </template>
                  <template v-else>
                    <v-chip @click="goToProcessModel(event.elementId)">
                      {{ event.label || $t("general.intermediateEvent") }}
                    </v-chip>
                  </template>
                </li>
              </ul>
            </v-col>
            <v-col
              v-if="details.activities && details.activities.length > 0"
              cols="12"
              sm="6"
              md="6"
            >
              <b class="mb-2">{{ $t("general.callActivities") }}</b>
              <ul class="mt-1">
                <li
                  v-for="(activity, index) in showProcessLevels
                    ? aggregateDetails.activities
                    : details.activities"
                  :key="'activity-' + index"
                  class="mb-2"
                >
                  <template v-if="showProcessLevels">
                    <v-badge
                      v-if="activity.count > 1"
                      :content="activity.count + 'x'"
                    >
                      <span class="badge-content">{{
                        activity.label || $t("general.activity")
                      }}</span>
                    </v-badge>
                    <span v-else class="no-badge">{{
                      activity.label || $t("general.activity")
                    }}</span>
                  </template>
                  <template v-else>
                    <v-chip @click="goToProcessModel(activity.elementId)">
                      {{ activity.label || $t("general.activity") }}
                    </v-chip>
                  </template>
                </li>
              </ul>
            </v-col>
            <v-col v-if="showProcessLevels" cols="12">
              <b class="mb-2">
                <v-icon>mdi-layers</v-icon>
                {{ $t("general.processLevels") }}
              </b>
              <v-radio-group v-model="selectedProcessModel" class="mt-1">
                <v-radio
                  v-for="(model, index) in sortedProcessModels"
                  :key="'relatedProcess-' + index"
                  :label="
                    $t('general.level') +
                    ' ' +
                    model.level +
                    ' – ' +
                    model.processName
                  "
                  :value="model.id"
                  :disabled="model.isCurrent"
                  @change="switchToRelatedModel(model.id)"
                  color="primary"
                ></v-radio>
              </v-radio-group>
            </v-col>
          </v-row>
          <div v-if="currentProcessModel.description" class="px-3 pb-3 pt-6">
            <v-row>
              <b>{{ $t("general.description") }}</b>
            </v-row>
            <v-row>
              <p class="description-text">
                {{ currentProcessModel.description }}
              </p>
            </v-row>
          </div>
          <div id="process-model-viewer" class="mt-4"></div>
        </v-container>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn
          color="blue-darken-1"
          variant="text"
          @click="goToProcessModel(null)"
        >
          {{ $t("general.processModel") }}
        </v-btn>
        <v-btn
          color="blue-darken-1"
          variant="text"
          @click="closeProcessInfoDialog"
        >
          {{ $t("general.cancel") }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>

  <v-dialog
    v-model="addDialog"
    persistent
    width="500"
    @after-leave="resetAddDialog"
  >
    <v-card>
      <v-card-title>{{ $t("general.addToRelatedProcessModels") }}</v-card-title>
      <v-card-text>
        <v-select
          v-model="selectedProcessesToAdd"
          :items="availableProcessModelsToAdd"
          :label="$t('processList.processModels')"
          multiple
          chips
          clearable
          item-title="processName"
          item-value="id"
        ></v-select>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn
          color="blue-darken-1"
          variant="text"
          @click="addDialog = false"
          >{{ $t("general.cancel") }}</v-btn
        >
        <v-btn color="blue-darken-1" @click="addRelatedProcessModel">{{
          $t("general.save")
        }}</v-btn>
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

.badge-content {
  background-color: #e0e0e0;
  padding: 8px 12px;
  margin-bottom: 6px;
  border-radius: 16px;
  font-size: 14px;
}

.no-badge {
  background-color: #e0e0e0;
  padding: 5px 12px;
  margin-bottom: 6px;
  border-radius: 16px;
  font-size: 14px;
  display: inline-block;
}
</style>
<script lang="ts">
import { defineComponent } from "vue";
import axios from "axios";
import { dia } from "@joint/core";
import BpmnViewer from "bpmn-js";
import { useAppStore } from "@/store/app";
import { authHeader } from "@/components/Authentication/authHeader";

export interface Process {
  id: number;
  name: string;
  description: string;
  startEvents: Event[];
  endEvents: Event[];
  intermediateCatchEvents: Event[];
  intermediateThrowEvents: Event[];
  activities: Event[];
}

declare interface Event {
  elementId: string;
  label: string;
  count: number;
}

interface ProcessModel {
  id: number;
  processName: string;
  description: string;
  level: number;
  processType: ProcessType;
  relatedProcessModels: {
    relatedProcessModelId: number;
    processName: string;
    level: number;
  }[];
}

enum ProcessType {
  COLLABORATION = "COLLABORATION",
  PARTICIPANT = "PARTICIPANT",
  PROCESS = "PROCESS"
}

interface RelatedProcessModel {
  relatedProcessModelId: number;
  processName: string;
  level: number;
}

interface RouteObject {
  path: string;
  query?: {
    portId: string;
  };
}

export default defineComponent({
  emits: ["closed"],
  data: () => ({
    infoDialog: false,
    showProcessLevels: false,
    store: useAppStore(),
    currentProcessModel: {} as ProcessModel,
    processType: ProcessType,
    details: {} as Process,
    relatedProcessModels: [] as RelatedProcessModel[],
    selectedProcessModel: null as number | null,
    addDialog: false,
    availableProcessModelsToAdd: [] as ProcessModel[],
    selectedProcessesToAdd: null as number | null
  }),

  watch: {
    showProcessLevels(newValue) {
      this.fetchProcessDetails(this.details.id);
      if (!newValue) {
        if (this.selectedProcessModel !== this.details.id) {
          this.resetProcessModel();
          this.fetchProcessModel(this.details.id);
        }
      }
    }
  },

  computed: {
    sortedProcessModels() {
      return [
        ...(this.currentProcessModel
          ? [
              {
                ...this.currentProcessModel,
                id: this.currentProcessModel.id,
                isCurrent: true
              }
            ]
          : []),
        ...this.relatedProcessModels.map((model) => ({
          ...model,
          id: model.relatedProcessModelId,
          isCurrent: false
        }))
      ].sort((a, b) => a.level - b.level);
    },
    aggregateDetails(): {
      startEvents: { elementId: string; label: string; count: number }[];
      endEvents: { elementId: string; label: string; count: number }[];
      intermediateCatchEvents: {
        elementId: string;
        label: string;
        count: number;
      }[];
      intermediateThrowEvents: {
        elementId: string;
        label: string;
        count: number;
      }[];
      activities: { elementId: string; label: string; count: number }[];
    } {
      const filterUnique = (
        items: { elementId: string; label: string; count: number }[]
      ) => {
        const seenLabels = new Map();

        items.forEach((item) => {
          if (!seenLabels.has(item.label)) {
            seenLabels.set(item.label, { ...item, count: 1 });
          } else {
            seenLabels.get(item.label).count++;
          }
        });

        return Array.from(seenLabels.values());
      };

      return {
        startEvents: filterUnique(this.details.startEvents || []),
        endEvents: filterUnique(this.details.endEvents || []),
        intermediateCatchEvents: filterUnique(
          this.details.intermediateCatchEvents || []
        ),
        intermediateThrowEvents: filterUnique(
          this.details.intermediateThrowEvents || []
        ),
        activities: filterUnique(this.details.activities || [])
      };
    }
  },

  methods: {
    async showProcessInfoDialog(processId: number) {
      this.infoDialog = true;
      await this.$nextTick();
      this.resetProcessModel();
      await this.fetchProcessModel(processId);
      await this.fetchProcessDetails(processId);
      await this.fetchAvailableProcessModelsToAdd();
    },
    async openAddDialog() {
      this.addDialog = true;
    },
    async fetchAvailableProcessModelsToAdd() {
      const url =
        "/api/project/" + this.store.getSelectedProjectId() + "/process-model";
      const response = await axios.get(url, { headers: authHeader() });
      this.availableProcessModelsToAdd = response.data.filter(
        (process: ProcessModel) =>
          process.processType !== ProcessType.PARTICIPANT &&
          process.id !== this.currentProcessModel.id &&
          !this.currentProcessModel.relatedProcessModels.some(
            (related) => related.relatedProcessModelId === process.id
          )
      );
    },
    async addRelatedProcessModel() {
      if (!this.selectedProcessesToAdd) return;

      const payload = { relatedProcessModelIds: this.selectedProcessesToAdd };
      await axios.post(
        "/api/project/" +
          this.store.getSelectedProjectId() +
          "/process-model/" +
          this.currentProcessModel.id +
          "/related-process-model",
        payload,
        { headers: authHeader() }
      );

      this.addDialog = false;
      this.resetProcessModel();
      await this.fetchProcessModel(this.currentProcessModel.id);
      await this.fetchAvailableProcessModelsToAdd();
    },
    async fetchProcessDetails(processId: number) {
      const url = this.showProcessLevels
        ? "/api/process-model/" + processId + "/details?aggregate=true"
        : "/api/process-model/" + processId + "/details";

      const response = await axios.get(url, { headers: authHeader() });
      this.details = response.data;
    },
    async goToProcessModel(portId: string | null) {
      const routeObject: RouteObject = {
        path: "/ProcessView/" + this.selectedProcessModel
      };
      if (portId) {
        routeObject.query = { portId };
      }
      this.$router.push(routeObject);
    },
    async fetchProcessModel(modelId: dia.Cell.ID) {
      const viewer = new BpmnViewer({
        container: "#process-model-viewer"
      });

      const processXmlUrl = "/api/process-model/" + modelId;
      const xmlResponse = await axios.get(processXmlUrl, {
        headers: authHeader()
      });
      const xmlText = xmlResponse.data;
      await viewer.importXML(xmlText);
      (viewer.get("canvas") as any).zoom("fit-viewport", "auto");

      const processInformationUrl =
        "api/project/" +
        this.store.getSelectedProjectId() +
        "/process-model/" +
        modelId;
      const processModelResponse = await axios.get(processInformationUrl, {
        headers: authHeader()
      });
      this.currentProcessModel = processModelResponse.data;
      this.selectedProcessModel = processModelResponse.data.id;
      this.relatedProcessModels =
        processModelResponse.data.relatedProcessModels;
    },
    async switchToRelatedModel(relatedProcessModelId: number) {
      this.resetProcessModel();
      await this.fetchProcessModel(relatedProcessModelId);
    },
    closeProcessInfoDialog() {
      this.infoDialog = false;
      this.showProcessLevels = false;
    },
    resetProcessModel() {
      document.getElementById("process-model-viewer")!.innerHTML = "";
    },
    resetAddDialog() {
      this.selectedProcessesToAdd = null;
    }
  }
});
</script>
