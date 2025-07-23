<template>
  <v-toolbar>
    <v-toolbar-title>
      <div class="d-flex align-center">
        <span>{{ selectedProjectName }}</span>
        <span class="text-body-2 text-grey-darken-1 ms-4"
          >VERSION {{ selectedVersionName }}</span
        >
      </div>
    </v-toolbar-title>
  </v-toolbar>
  <ProcessDetailDialog
    ref="processDetailDialog"
    @closed="fetchProcessModels(true)"
  />
  <div v-if="isFetching" class="d-flex align-center justify-center w-100 h-75">
    <div class="d-flex flex-column align-center justify-center">
      <span class="mb-2">{{ $t("processList.fetchingProcessModels") }}</span>
      <v-progress-circular indeterminate />
    </div>
  </div>
  <v-list v-else lines="two" class="pa-6">
    <v-select
      v-if="showFilter"
      v-model="selectedLevels"
      :items="availableLevels"
      density="compact"
      multiple
      clearable
      @update:modelValue="fetchProcessModels(true)"
      style="width: 300px"
      variant="outlined"
    >
      <template #label>
        <v-icon style="margin-right: 5px">mdi-filter</v-icon>
        {{ $t("processList.filterByLevels") }}
      </template>
    </v-select>
    <v-list-item v-if="rootProcessModels.length == 0"
      >{{ $t("processList.noProcessModelsFound") }}
    </v-list-item>
    <template
      v-for="(model, index) in rootProcessModels"
      :key="'process-' + model.id"
    >
      <ProcessTreeNode
        :model="model"
        :index="index"
        @delete-process="deleteProcessModel"
        @upload-process="openSingleUploadDialog"
        @more-info="showProcessInfoDialog"
      />
      <v-divider
        v-if="index < rootProcessModels.length - 1"
        :key="`${index}-divider`"
      ></v-divider>
    </template>
  </v-list>
  <div
    class="ma-4"
    style="position: fixed; bottom: 8px; right: 8px; z-index: 1"
  >
    <v-tooltip location="top">
      <template v-slot:activator="{ props }">
        <v-fab-transition>
          <v-btn
            class="mt-auto pointer-events-initial me-4"
            color="primary"
            elevation="8"
            icon="mdi-cloud"
            @click="goToC8Import"
            size="large"
            v-bind="props"
          ></v-btn>
        </v-fab-transition>
      </template>
      <span>{{ $t("processList.navigateToC8Import") }}</span>
    </v-tooltip>

    <v-tooltip location="top">
      <template v-slot:activator="{ props }">
        <v-fab-transition>
          <v-btn
            class="mt-auto pointer-events-initial"
            color="primary"
            elevation="8"
            icon="mdi-plus"
            @click="openMultipleUploadDialog"
            size="large"
            v-bind="props"
          />
        </v-fab-transition>
      </template>
      <span>{{ $t("processList.uploadProcessModels") }}</span>
    </v-tooltip>
  </div>
  <div
    v-if="rootProcessModels.length > 0"
    class="d-flex align-center justify-center ma-4"
    style="position: fixed; bottom: 8px; right: 8px; left: 8px; height: 56px"
  >
    <v-btn prepend-icon="mdi-map" @click="goToProcessMap">
      {{ $t("processList.toTheProcessMap") }}
    </v-btn>
  </div>

  <v-dialog
    v-model="uploadDialog"
    persistent
    width="600"
    @after-leave="resetUploadDialog"
  >
    <v-card>
      <v-card-title>
        <span class="text-h5" v-if="uploadDialogMode === 'multiple'"
          ><strong>{{ $t("processList.uploadProcessModels") }}</strong></span
        >
        <span class="text-h5 text-wrap" v-if="uploadDialogMode === 'single'">
          <strong>{{ $t("processList.replaceProcessModel") }}:</strong>
          {{ processName }}
        </span>
      </v-card-title>
      <v-card-text>
        <v-container>
          <v-row v-if="uploadDialogMode === 'single'">
            <v-col
              v-if="showProcessModelChangeAnalysisCheckbox"
              cols="12"
              sm="12"
              md="12"
              class="pt-0"
            >
              <v-checkbox
                v-model="performProcessModelChangeAnalysis"
                color="primary"
                hide-details
                :disabled="isPerformingProcessModelChangeAnalysis"
              >
                <template #label>
                  <span style="display: inline-flex; align-items: center">
                    {{
                      $t(
                        "processModelChangeAnalysis.performProcessModelChangeAnalysis"
                      )
                    }}
                    <v-tooltip
                      location="top"
                      :disabled="isPerformingProcessModelChangeAnalysis"
                      :text="$t('processModelChangeAnalysis.info')"
                      max-width="600"
                    >
                      <template #activator="{ props }">
                        <v-icon
                          v-bind="props"
                          icon="mdi-information-outline"
                          class="ms-2"
                        />
                      </template>
                    </v-tooltip>
                  </span>
                </template>
              </v-checkbox>
            </v-col>
          </v-row>
          <v-row class="pb-5">
            <v-col cols="12" sm="12" md="12" class="pt-0">
              <v-file-input
                v-if="uploadDialogMode === 'multiple'"
                :label="$t('processList.processModels')"
                v-model="processModelFiles"
                chips
                multiple
                @change="handleFileSelection"
                hide-details
              ></v-file-input>
              <v-file-input
                v-if="uploadDialogMode === 'single'"
                :disabled="isPerformingProcessModelChangeAnalysis"
                :label="$t('general.processModel')"
                v-model="processModelFiles"
                chips
                @change="handleFileSelection"
                hide-details
              ></v-file-input>
            </v-col>
          </v-row>
          <v-row
            v-for="(file, index) in processModelsToUpload"
            :key="'file-' + index"
            class="py-5 mt-0"
          >
            <div class="d-flex align-center">
              <p class="px-3">{{ file.file.name }}</p>
              <span
                v-if="file.isCollaboration"
                class="text-grey-darken-1 text-body-2"
                >{{ $t("processList.collaboration") }}</span
              >
            </div>
            <v-col cols="12" sm="12" md="12" class="py-1">
              <v-text-field
                :disabled="isPerformingProcessModelChangeAnalysis"
                hide-details
                label="Name"
                v-model="file.name"
              ></v-text-field>
            </v-col>
            <v-col cols="12" sm="12" md="12" class="py-1">
              <v-row no-gutters align-items="center">
                <v-col style="position: relative">
                  <v-textarea
                    rows="3"
                    :disabled="isPerformingProcessModelChangeAnalysis"
                    :label="$t('general.description')"
                    v-model="file.description"
                    :error-messages="descriptionErrors[index]"
                    @input="resetDescriptionErrors"
                  />
                  <v-overlay
                    class="align-center justify-center"
                    :model-value="file.aiLoading"
                    contained
                    persistent
                    scrim="grey"
                  >
                    <v-progress-circular
                      color="primary"
                      size="24"
                      indeterminate
                    ></v-progress-circular>
                  </v-overlay>
                </v-col>
                <v-col class="d-flex justify-end" cols="auto">
                  <v-tooltip
                    :disabled="isPerformingProcessModelChangeAnalysis"
                    :text="$t('processList.generateDescriptionWithAI')"
                    location="bottom"
                  >
                    <template v-slot:activator="{ props }">
                      <v-icon
                        v-bind="props"
                        color="grey"
                        class="ms-2 hover-icon"
                        @click="
                          isPerformingProcessModelChangeAnalysis
                            ? null
                            : generateDescription(file, index)
                        "
                      >
                        mdi-auto-fix
                      </v-icon>
                    </template>
                  </v-tooltip>
                </v-col>
              </v-row>
            </v-col>
          </v-row>
        </v-container>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-container>
          <v-row>
            <v-col class="mx-auto">
              <v-alert
                v-if="isPerformingProcessModelChangeAnalysis"
                type="info"
                variant="tonal"
              >
                <v-progress-circular
                  color="primary"
                  size="24"
                  class="mr-2"
                  indeterminate
                ></v-progress-circular>
                {{
                  $t(
                    "processModelChangeAnalysis.isPerformingProcessModelChangeAnalysis"
                  )
                }}
              </v-alert>
            </v-col>
          </v-row>
        </v-container>
        <v-btn
          color="blue-darken-1"
          variant="text"
          :disabled="isPerformingProcessModelChangeAnalysis"
          @click="closeUploadDialog"
        >
          {{ $t("general.cancel") }}
        </v-btn>
        <v-btn
          v-if="uploadDialogMode === 'multiple'"
          color="blue-darken-1"
          variant="text"
          @click="uploadProcessModels"
        >
          {{ $t("general.save") }}
        </v-btn>
        <v-btn
          v-if="uploadDialogMode === 'single'"
          color="blue-darken-1"
          variant="text"
          :disabled="isPerformingProcessModelChangeAnalysis"
          @click="replaceProcessModel"
        >
          {{ $t("general.save") }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>

  <v-dialog v-model="progressDialog" max-width="600">
    <v-card title="Upload">
      <template v-slot:text>
        {{ $t("processList.uploadingProcessModel") }}:
        {{ currentlyUploadingProcessModel.name }} ({{ currentUploadStatus }})
        <v-progress-linear
          color="primary"
          :model-value="progress"
          :height="10"
        ></v-progress-linear>
      </template>

      <v-card-actions>
        <v-spacer></v-spacer>

        <v-btn
          :text="$t('general.cancel')"
          variant="text"
          @click="progressDialog = false"
        ></v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>

  <v-dialog v-model="confirmDeleteDialog" width="auto">
    <v-card
      max-width="400"
      prepend-icon="mdi-delete"
      :title="$t('processList.confirmDeletion')"
    >
      <template v-slot:text>
        {{ $t("processList.confirmDeletionText1")
        }}<strong>{{ processModelToBeDeleted?.processName }}</strong
        >{{ $t("processList.confirmDeletionText2") }}
      </template>
      <template v-slot:actions>
        <div class="ms-auto">
          <v-btn
            :text="$t('general.cancel')"
            @click="confirmDeleteDialog = false"
          ></v-btn>
          <v-btn
            :text="$t('processList.confirm')"
            @click="deleteProcessModel(processModelToBeDeleted!, true)"
          ></v-btn>
        </div>
      </template>
    </v-card>
  </v-dialog>

  <v-dialog v-model="processModelChangeResultsDialog" width="auto">
    <v-card max-width="600">
      <v-card-title class="d-flex align-center">
        <v-icon class="me-2">mdi-check-circle-outline</v-icon>
        {{ $t("processModelChangeAnalysis.processModelChangeResults") }}
      </v-card-title>
      <v-card-text>
        <v-list density="compact">
          <template
            v-for="(result, index) in processModelChangeResults"
            :key="index"
          >
            <v-list-item class="flex-column align-start">
              <div class="d-flex" v-if="result.processModelName">
                <v-icon class="me-2" color="grey">mdi-file</v-icon>
                <span>{{ result.processModelName }}</span>
              </div>

              <div class="d-flex mt-1" v-if="result.processModelLevel">
                <v-icon class="me-2">mdi-layers</v-icon>
                <span>{{
                  $t("general.level") + " " + result.processModelLevel
                }}</span>
              </div>

              <div class="d-flex mt-1">
                <v-icon class="me-2" color="green">mdi-file-check</v-icon>
                <span>{{ formatMessage(result.message) }}</span>
              </div>
            </v-list-item>
            <v-divider
              v-if="index < processModelChangeResults.length - 1"
              class="my-3"
            />
          </template>
        </v-list>
      </v-card-text>
      <v-card-actions>
        <v-spacer />
        <v-btn
          :text="$t('general.close')"
          @click="processModelChangeResultsDialog = false"
        />
      </v-card-actions>
    </v-card>
  </v-dialog>

  <v-dialog v-model="errorDialog" width="auto">
    <v-card
      max-width="400"
      prepend-icon="mdi-alert-circle-outline"
      :title="$t('processList.error')"
    >
      <template v-slot:text>
        {{ errorMessage }}
      </template>
      <template v-slot:actions>
        <div class="ms-auto">
          <v-btn
            :text="$t('general.close')"
            @click="errorDialog = false"
          ></v-btn>
        </div>
      </template>
    </v-card>
  </v-dialog>
</template>

<style scoped>
.hover-icon {
  transition: color 0.3s ease-in-out;
}

.hover-icon:hover {
  color: #757575 !important;
}
</style>

<script lang="ts">
import axios from "axios";
import ProcessDetailDialog from "@/components/ProcessDetailDialog.vue";
import { defineComponent } from "vue";
import { useAppStore } from "@/store/app";
import getProject from "../projectService";
import { GoogleGenerativeAI } from "@google/generative-ai";
import { authHeader } from "@/components/Authentication/authHeader";
import i18n from "@/i18n";
import ProcessTreeNode from "@/components/ProcessList/ProcessTreeNode.vue";
import { Settings } from "@/components/SettingsDrawer.vue";
import { getErrorMessage } from "@/services/axiosErrorHandler";

interface BPMNContent {
  name: string;
  description: string;
  isCollaboration: boolean;
}

interface ProcessModelInformation {
  id: number;
  bpmnProcessId: string;
  processName: string;
  description: string;
  createdAt: string;
  level: number;
  childrenIds: number[];
  processType: string;
}

export interface ProcessModelNode extends ProcessModelInformation {
  children: ProcessModelNode[];
}

interface RelatedProcessModel {
  relatedProcessModelId: number;
  processName: string;
  level: number;
}

enum UploadDialogMode {
  SINGLE = "single",
  MULTIPLE = "multiple"
}

interface ProcessModelToUpload {
  file: File;
  name: string;
  description: string;
  content: string;
  aiLoading: boolean;
  isCollaboration: boolean;
}

type ProcessResult = {
  processModelName: string;
  processModelLevel: number;
  message: string;
};

export default defineComponent({
  components: {
    ProcessTreeNode,
    ProcessDetailDialog
  },
  data: () => ({
    appStore: useAppStore(),
    processModelChangeResults: [] as ProcessResult[],
    processModelChangeResultsDialog: false as boolean,
    availableLevels: [] as number[],
    confirmDeleteDialog: false as boolean,
    errorDialog: false as boolean,
    errorMessage: "" as string,
    uploadDialog: false as boolean,
    uploadDialogMode: UploadDialogMode.MULTIPLE as UploadDialogMode,
    processModelToBeReplacedId: null as number | null,
    processModelToBeDeleted: null as ProcessModelNode | null,
    progressDialog: false,
    progress: 0,
    processModelFiles: [] as File[],
    processModelsToUpload: [] as ProcessModelToUpload[],
    processName: "" as string,
    relatedProcessModels: [] as RelatedProcessModel[],
    rootProcessModels: [] as ProcessModelNode[],
    selectedLevels: [] as number[],
    selectedProjectId: null as number | null,
    selectedProjectName: "" as string,
    selectedVersionName: "" as string,
    fileExtensionMatcher: /.[^/.]+$/,
    isFetching: false as boolean,
    isPerformingProcessModelChangeAnalysis: false as boolean,
    descriptionErrors: {} as { [key: number]: string },
    currentlyUploadingProcessModel: {} as ProcessModelToUpload,
    currentUploadStatus: "" as string,
    performProcessModelChangeAnalysis: false,
    showProcessModelChangeAnalysisCheckbox: false
  }),
  mounted: function () {
    this.selectedProjectId = this.appStore.selectedProjectId;
    if (!this.selectedProjectId) {
      this.$router.push("/");
      return;
    }
    getProject(this.selectedProjectId).then((result) => {
      this.selectedProjectName = result.data.name;
      this.selectedVersionName = result.data.version;
    });
    this.fetchProcessModels();
  },
  computed: {
    isUserLoggedIn(): boolean {
      return this.appStore.getUserToken() != null;
    },
    showFilter() {
      return this.availableLevels.some((level) => level > 0);
    }
  },
  watch: {
    isUserLoggedIn(newValue) {
      if (!newValue) {
        this.$router.push("/");
      }
    }
  },
  methods: {
    showProcessInfoDialog(processId: number) {
      (
        this.$refs.processDetailDialog as InstanceType<
          typeof ProcessDetailDialog
        >
      ).showProcessInfoDialog(processId);
    },

    async deleteProcessModel(
      processModelNode: ProcessModelNode,
      skipConfirm: boolean = false
    ) {
      const isParticipant = processModelNode.processType === "PARTICIPANT";
      if (!skipConfirm && isParticipant) {
        this.processModelToBeDeleted = processModelNode;
        this.confirmDeleteDialog = true;
        return;
      }
      const processId = processModelNode.id;
      await axios
        .delete("/api/process-model/" + processId, { headers: authHeader() })
        .then(() => {
          this.confirmDeleteDialog = false;
          this.processModelToBeDeleted = null;
          this.appStore.setProcessModelsChanged();
          this.fetchProcessModels(true);
        });
    },

    async fetchProcessModels(skipLoading = false) {
      if (!skipLoading) {
        this.isFetching = true;
      }

      const response = await axios.get(
        "/api/project/" + this.selectedProjectId + "/process-model",
        { headers: authHeader() }
      );

      const levelsSet = new Set<number>();
      response.data.forEach((model: ProcessModelInformation) => {
        if (model.level !== null) {
          levelsSet.add(model.level);
        }
      });
      this.availableLevels = Array.from(levelsSet).sort((a, b) => a - b);
      this.rootProcessModels = this.collectRoots(response.data);

      const levelsQuery =
        this.selectedLevels.length > 0
          ? "?levels=" + this.selectedLevels.join(",")
          : "";
      const levelsResponse = levelsQuery
        ? await axios.get(
            "/api/project/" +
              this.selectedProjectId +
              "/process-model" +
              levelsQuery,
            { headers: authHeader() }
          )
        : response;

      this.rootProcessModels = this.collectRoots(levelsResponse.data);
      this.isFetching = false;
    },

    async fetchProcessModel(processModelId: number) {
      const response = await axios.get(
        "/api/project/" +
          this.selectedProjectId +
          "/process-model/" +
          processModelId,
        { headers: authHeader() }
      );

      this.processName = response.data.processName;
      this.relatedProcessModels = response.data.relatedProcessModels;
    },

    collectRoots(
      processModelInformation: ProcessModelInformation[]
    ): ProcessModelNode[] {
      const modelMap = new Map<number, ProcessModelInformation>();
      processModelInformation.forEach((model) => modelMap.set(model.id, model));
      return processModelInformation
        .filter((model) => model.processType !== "PARTICIPANT")
        .map((model) => {
          if (model.processType === "COLLABORATION") {
            const children = model.childrenIds.map((id) => modelMap.get(id)!);
            const childrenAsNodes = children.map((child) => ({
              ...child,
              children: []
            }));
            return { ...model, children: childrenAsNodes };
          } else {
            return { ...model, children: [] };
          }
        });
    },

    async openSingleUploadDialog(modelId: number) {
      this.uploadDialog = true;
      this.uploadDialogMode = UploadDialogMode.SINGLE;
      this.processModelToBeReplacedId = modelId;

      await this.fetchProcessModel(modelId);

      const settings = (
        await axios.get("api/settings", { headers: authHeader() })
      ).data;
      const validApiKey =
        (settings.geminiApiKey?.startsWith("AIza") ||
          import.meta.env.VITE_GEMINI_API_KEY.startsWith("AIza")) &&
        (settings.geminiApiKey?.length === 39 ||
          import.meta.env.VITE_GEMINI_API_KEY.length === 39);
      this.showProcessModelChangeAnalysisCheckbox =
        this.relatedProcessModels.length > 0 && validApiKey;
    },

    openMultipleUploadDialog() {
      this.uploadDialog = true;
      this.uploadDialogMode = UploadDialogMode.MULTIPLE;
    },

    async readFileContent(file: File): Promise<string> {
      return new Promise((resolve, reject) => {
        const reader = new FileReader();

        reader.onload = () => {
          const result = reader.result;
          if (typeof result === "string") {
            resolve(result);
          } else {
            reject(new Error("File content is not a string"));
          }
        };

        reader.onerror = () => {
          reject(new Error("Error reading file"));
        };

        reader.readAsText(file);
      });
    },

    parseBPMNContent(content: string): BPMNContent {
      const parser = new DOMParser();
      const xmlDoc = parser.parseFromString(content, "text/xml");

      const isCollaboration =
        xmlDoc.getElementsByTagName("bpmn:participant")?.length > 1 ||
        xmlDoc.getElementsByTagName("semantic:participant")?.length > 1 ||
        xmlDoc.getElementsByTagName("participant")?.length > 1;

      if (isCollaboration) {
        const collaboration =
          xmlDoc.querySelector("bpmn\\:collaboration") ||
          xmlDoc.querySelector("semantic\\:collaboration") ||
          xmlDoc.querySelector("collaboration");

        const name = collaboration?.getAttribute("name") || "";
        const documentation =
          collaboration?.querySelector("bpmn\\:documentation") ||
          collaboration?.querySelector("semantic\\:documentation") ||
          collaboration?.querySelector("documentation");

        const description = documentation?.getAttribute("textContent") || "";
        return { name, description, isCollaboration };
      }

      const process =
        xmlDoc.querySelector("bpmn\\:process") ||
        xmlDoc.querySelector("semantic\\:process") ||
        xmlDoc.querySelector("process");

      const name = process?.getAttribute("name") || "";

      const documentation =
        process?.querySelector("bpmn\\:documentation") ||
        process?.querySelector("semantic\\:documentation") ||
        process?.querySelector("documentation");

      const description = documentation?.getAttribute("textContent") || "";

      return { name, description, isCollaboration };
    },

    async handleFileSelection() {
      this.processModelsToUpload = [];

      for (const file of this.processModelFiles) {
        try {
          this.processModelsToUpload.push(
            await this.getProcessModelToUpload(file)
          );
        } catch (error) {
          console.error("Error reading file content: ", error);
        }
      }
    },

    async getProcessModelToUpload(file: File): Promise<ProcessModelToUpload> {
      const content = await this.readFileContent(file);

      const { name, description, isCollaboration } =
        this.parseBPMNContent(content);
      return {
        file,
        name: name || file.name.replace(this.fileExtensionMatcher, ""),
        description,
        content,
        aiLoading: false,
        isCollaboration
      };
    },

    async uploadProcessModel(
      processModel: ProcessModelToUpload
    ): Promise<number> {
      const formData = this.createProcessModelFormData(processModel);
      const { data } = await axios.post(
        "/api/project/" + this.selectedProjectId + "/process-model",
        formData,
        {
          headers: authHeader()
        }
      );
      return data;
    },

    createProcessModelFormData(processModel: ProcessModelToUpload): FormData {
      let formData = new FormData();
      const fileName =
        processModel.name ||
        processModel.file.name.replace(this.fileExtensionMatcher, "");
      const apiKey = import.meta.env.VITE_GEMINI_API_KEY;
      const selectedLanguage = this.appStore.getSelectedLanguage();

      formData.append("processModel", processModel.file);
      formData.append("fileName", fileName);
      formData.append("description", processModel.description);
      formData.append(
        "isCollaboration",
        processModel.isCollaboration ? "true" : "false"
      );
      if (this.performProcessModelChangeAnalysis) {
        formData.append("startProcessModelChangeAnalysis", "true");
      }
      if (
        this.performProcessModelChangeAnalysis &&
        apiKey.startsWith("AIza") &&
        apiKey.length === 39
      ) {
        formData.append("geminiApiKey", apiKey);
        formData.append("selectedLanguage", selectedLanguage);
      }

      return formData;
    },

    async uploadProcessModels() {
      if (this.processModelsToUpload.length > 0) {
        this.progressDialog = true;
        const progressSteps = 100 / (this.processModelsToUpload.length * 2);

        const numProcessModels = this.processModelsToUpload.length;
        let i = 0;
        for (const processModel of this.processModelsToUpload) {
          this.progress += progressSteps;
          this.currentlyUploadingProcessModel = processModel;
          i += 1;
          this.currentUploadStatus = `${i}/${numProcessModels}`;

          try {
            await this.uploadProcessModel(processModel);
          } catch (error) {
            this.afterUploadActions();
            this.errorMessage = getErrorMessage(error);
            this.errorDialog = true;
            return;
          }

          this.progress += progressSteps;
        }

        this.afterUploadActions();
      }
    },

    async replaceProcessModel() {
      if (this.processModelsToUpload.length !== 1) {
        return;
      }

      const formData = this.createProcessModelFormData(
        this.processModelsToUpload[0]
      );

      try {
        if (this.performProcessModelChangeAnalysis) {
          this.isPerformingProcessModelChangeAnalysis = true;
        }

        const response = await axios.post(
          "/api/project/" +
            this.selectedProjectId +
            "/process-model/" +
            this.processModelToBeReplacedId,
          formData,
          { headers: authHeader() }
        );

        if (this.performProcessModelChangeAnalysis) {
          this.processModelChangeResults =
            response.data.processModelChangeResults;

          this.isPerformingProcessModelChangeAnalysis = false;
          this.performProcessModelChangeAnalysis = false;
          this.processModelChangeResultsDialog = true;
        }
      } catch (error) {
        this.afterUploadActions();
        this.errorMessage = getErrorMessage(error);
        this.errorDialog = true;
        return;
      }

      this.afterUploadActions();
    },

    formatMessage(message: string): string {
      const translations: Record<string, string> = {
        "Process model updated": this.$t(
          "processModelChangeAnalysis.processModelUpdated"
        ) as string,
        "No change necessary": this.$t(
          "processModelChangeAnalysis.noChangeNecessary"
        ) as string,
        "Same content": this.$t(
          "processModelChangeAnalysis.sameContent"
        ) as string
      };
      return translations[message] || message;
    },

    afterUploadActions() {
      this.fetchProcessModels();
      this.isPerformingProcessModelChangeAnalysis = false;
      this.performProcessModelChangeAnalysis = false;
      this.progressDialog = false;
      this.progress = 0;
      this.closeUploadDialog();
      this.appStore.setProcessModelsChanged();
    },

    closeUploadDialog() {
      this.uploadDialog = false;
      this.performProcessModelChangeAnalysis = false;
      this.resetDescriptionErrors();
    },

    resetUploadDialog() {
      this.processModelFiles = [];
      this.processModelsToUpload = [];
      this.progressDialog = false;
      this.processModelToBeReplacedId = null;
      this.progress = 0;
    },

    async generateDescription(
      processModelToUpload: ProcessModelToUpload,
      index: number
    ) {
      processModelToUpload.aiLoading = true;
      const content = processModelToUpload.content;

      function validApiKey(apiKey: string): boolean {
        return apiKey.startsWith("AIza") && apiKey.length === 39;
      }

      const settings: Settings = (
        await axios.get("api/settings", { headers: authHeader() })
      ).data;
      const apiKey =
        settings.geminiApiKey || import.meta.env.VITE_GEMINI_API_KEY;
      if (!apiKey) {
        this.descriptionErrors[index] = this.$t("processList.noApiKeyError");
        processModelToUpload.aiLoading = false;
        return;
      }
      if (!validApiKey(apiKey)) {
        this.descriptionErrors[index] = this.$t(
          "processList.invalidApiKeyError"
        );
        processModelToUpload.aiLoading = false;
        return;
      }
      const genAi = new GoogleGenerativeAI(apiKey);
      const model = genAi.getGenerativeModel({ model: "gemini-1.5-flash" });

      const promptInstructionsDe =
        "Ich möchte, dass du aus dem folgenden XML-Dokument " +
        "eine kurze Beschreibung für einen Geschäftsprozess generierst. " +
        "Die Beschreibung soll maximal 1-3 Sätze lang sein. " +
        "Bitte achte genaustens darauf, dass die Beschreibung nicht länger als " +
        "255 Zeichen lang ist. Sie darf unter keinen Umständen länger sein!\n\n";

      const promptInstructionsEn =
        "Please generate a short description for the business process from " +
        "the following XML document. The description should be a maximum of 1-3 sentences long. " +
        "Ensure that the description is no longer than 255 characters. It must not exceed this " +
        "length under any circumstances!\n\n";

      const promptInstructions =
        i18n.global.locale === "de"
          ? promptInstructionsDe
          : promptInstructionsEn;
      const prompt = promptInstructions + content;

      const result = await model.generateContent(prompt);
      const response = result.response;
      processModelToUpload.description = response.text().trim();

      processModelToUpload.aiLoading = false;
    },
    goToC8Import() {
      this.$router.push("CamundaCloudImport");
    },
    getLocaleDate(date: string): string {
      const locales =
        this.appStore.getSelectedLanguage() === "de" ? "de-DE" : "en-US";
      return new Date(date).toLocaleString(locales);
    },
    goToProcessMap() {
      this.$router.push("ProcessMap");
    },
    resetDescriptionErrors() {
      this.descriptionErrors = {};
    }
  }
});
</script>
