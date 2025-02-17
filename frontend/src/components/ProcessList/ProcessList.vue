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
  <ProcessDetailDialog ref="processDetailDialog" />
  <div v-if="isFetching" class="d-flex align-center justify-center w-100 h-75">
    <div class="d-flex flex-column align-center justify-center">
      <span class="mb-2">{{ $t("processList.fetchingProcessModels") }}</span>
      <v-progress-circular indeterminate />
    </div>
  </div>
  <v-list v-else lines="two" class="pa-6">
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

    <v-fab-transition>
      <v-btn
        class="mt-auto pointer-events-initial"
        color="primary"
        elevation="8"
        icon="mdi-plus"
        @click="openMultipleUploadDialog"
        size="large"
      />
    </v-fab-transition>
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
        <span class="text-h5" v-if="uploadDialogMode === 'multiple'">{{
          $t("processList.uploadProcessModels")
        }}</span>
        <span class="text-h5" v-if="uploadDialogMode === 'single'">{{
          $t("processList.replaceProcessModel")
        }}</span>
      </v-card-title>
      <v-card-text>
        <v-container>
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
                hide-details
                label="Name"
                v-model="file.name"
              ></v-text-field>
            </v-col>
            <v-col cols="12" sm="12" md="12" class="py-1">
              <v-row no-gutters align="center">
                <v-col style="position: relative">
                  <v-textarea
                    rows="3"
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
                    :text="$t('processList.generateDescriptionWithAI')"
                    location="bottom"
                  >
                    <template v-slot:activator="{ props }">
                      <v-icon
                        v-bind="props"
                        color="grey"
                        class="ms-2 hover-icon"
                        @click="generateDescription(file, index)"
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
        <v-btn color="blue-darken-1" variant="text" @click="closeUploadDialog">
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
        {{ $t('processList.uploadingProcessModel') }}: {{ currentlyUploadingProcessModel.name }}
        ({{ currentUploadStatus }})
        <v-progress-linear color="primary" :model-value="progress" :height="10"></v-progress-linear>
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

  <v-dialog v-model="errorDialog" width="auto">
    <v-card
      max-width="400"
      prepend-icon="mdi-alert-circle-outline"
      :title="$t('processList.error')"
    >
      <template v-slot:text>
        <span v-if="errorType = ErrorType.ALREADY_EXISTING">
          {{
            $t(
              "processList.alreadyExistsErrorMsg1." +
                (alreadyExistingBpmnProcessIds.length > 1
                  ? "plural"
                  : "singular")
            )
          }}
          <strong>{{ alreadyExistingBpmnProcessIds.join(", ") }}</strong>
          {{
            $t(
              "processList.alreadyExistsErrorMsg2." +
                (alreadyExistingBpmnProcessIds.length > 1
                  ? "plural"
                  : "singular")
            )
          }}
        </span>
        <span v-else-if="errorType === ErrorType.CANT_REPLACE_WITH_COLLABORATION">
          {{ $t('processList.cantReplaceWithCollaborationErrorMsg') }}
        </span>
        <span v-else-if="errorType === ErrorType.UNKNOWN">
          {{ $t("processList.unknownErrorMsg") }}
        </span>
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
import axios, { AxiosError } from 'axios';
import ProcessDetailDialog from '@/components/ProcessDetailDialog.vue';
import { defineComponent } from "vue";
import { useAppStore } from "@/store/app";
import getProject from "../projectService";
import { GoogleGenerativeAI } from "@google/generative-ai";
import { authHeader } from "@/components/Authentication/authHeader";
import i18n from "@/i18n";
import ProcessTreeNode from "@/components/ProcessList/ProcessTreeNode.vue";
import { Settings } from "@/components/SettingsDrawer.vue";

interface BPMNContent {
  name: string;
  description: string;
  isCollaboration: boolean;
}

export interface ProcessModelNode {
  id: number;
  bpmnProcessId: string;
  processName: string;
  description: string;
  createdAt: string;
  parentsBpmnProcessIds: string[];
  childrenBpmnProcessIds: string[];
  children: ProcessModelNode[];
}

interface RawProcessModel {
  id: number;
  bpmnProcessId: string;
  processName: string;
  description: string;
  createdAt: string;
  parentsBpmnProcessIds: string[];
  childrenBpmnProcessIds: string[];
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

interface HttpError {
  response: {
    data: {
      data: string;
      message: string;
    };
  };
}

enum ErrorType {
  ALREADY_EXISTING = "ALREADY_EXISTING",
  CANT_REPLACE_WITH_COLLABORATION = "CANT_REPLACE_WITH_COLLABORATION",
  UNKNOWN = "UNKNOWN",
}

export default defineComponent({
  components: {
    ProcessTreeNode,
    ProcessDetailDialog
  },
  data: () => ({
    appStore: useAppStore(),
    confirmDeleteDialog: false as boolean,
    errorDialog: false as boolean,
    errorType: ErrorType.ALREADY_EXISTING as ErrorType,
    ErrorType: ErrorType,
    alreadyExistingBpmnProcessIds: [] as string[],
    uploadDialog: false as boolean,
    uploadDialogMode: UploadDialogMode.MULTIPLE as UploadDialogMode,
    processModelToBeReplacedId: null as number | null,
    processModelToBeDeleted: null as ProcessModelNode | null,
    progressDialog: false,
    progress: 0,
    processModelFiles: [] as File[],
    processModelsToUpload: [] as ProcessModelToUpload[],
    rootProcessModels: [] as ProcessModelNode[],
    selectedProjectId: null as number | null,
    selectedProjectName: "" as string,
    selectedVersionName: "" as string,
    fileExtensionMatcher: /.[^/.]+$/,
    isFetching: false as boolean,
    descriptionErrors: {} as { [key: number]: string },
    currentlyUploadingProcessModel: {} as ProcessModelToUpload,
    currentUploadStatus: "" as string,
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
      const isPartOfCollaboration =
        processModelNode.parentsBpmnProcessIds.length > 0 ||
        processModelNode.childrenBpmnProcessIds.length > 0;
      if (!skipConfirm && isPartOfCollaboration) {
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
          this.fetchProcessModels();
        });
    },

    fetchProcessModels() {
      this.isFetching = true;
      axios
        .get("/api/project/" + this.selectedProjectId + "/process-model", {
          headers: authHeader()
        })
        .then((result) => {
          this.rootProcessModels = this.collectRoots(result.data);
          this.isFetching = false;
        });
    },

    collectRoots(rawProcessModels: RawProcessModel[]): ProcessModelNode[] {
      const modelMap = new Map<string, ProcessModelNode>();
      rawProcessModels.forEach((model) =>
        modelMap.set(model.bpmnProcessId, { ...model, children: [] })
      );

      const roots = [] as ProcessModelNode[];

      rawProcessModels.forEach((model) => {
        if (model.parentsBpmnProcessIds.length === 0) {
          roots.push(modelMap.get(model.bpmnProcessId)!);
        } else {
          model.parentsBpmnProcessIds.forEach((parentId) => {
            const parent = modelMap.get(parentId);
            if (parent) {
              parent.children.push(modelMap.get(model.bpmnProcessId)!);
            }
          });
        }
      });

      return roots;
    },

    openSingleUploadDialog(modelId: number) {
      this.uploadDialog = true;
      this.uploadDialogMode = UploadDialogMode.SINGLE;
      this.processModelToBeReplacedId = modelId;
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

      const semanticParticipants = xmlDoc.getElementsByTagName("participant");
      const bpmnParticipants = xmlDoc.getElementsByTagName("bpmn:participant");
      const isCollaboration =
        semanticParticipants.length > 1 || bpmnParticipants.length > 1;

      const name =
        xmlDoc.querySelector("bpmn\\:process")?.getAttribute("name") ||
        xmlDoc.querySelector("process")?.getAttribute("name");
      const documentation =
        xmlDoc
          .querySelector("bpmn\\:documentation")
          ?.getAttribute("textContent") ||
        xmlDoc.querySelector("documentation")?.getAttribute("textContent");

      return {
        name: name || "",
        description: documentation || "",
        isCollaboration
      };
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
    ): Promise<number | string> {
      const formData = this.createProcessModelFormData(processModel);
      const { data } = await axios.post(
        "/api/project/" + this.selectedProjectId + "/process-model",
        formData,
        { headers: authHeader() }
      );
      return data;
    },

    createProcessModelFormData(processModel: ProcessModelToUpload): FormData {
      let formData = new FormData();
      const fileName =
        processModel.name ||
        processModel.file.name.replace(this.fileExtensionMatcher, "");
      formData.append("processModel", processModel.file);
      formData.append("fileName", fileName);
      formData.append("description", processModel.description);
      formData.append(
        "isCollaboration",
        processModel.isCollaboration ? "true" : "false"
      );

      return formData;
    },

    async uploadProcessModels() {
      if (this.processModelsToUpload.length > 0) {
        this.progressDialog = true;
        const progressSteps = 100 / (this.processModelsToUpload.length * 2);

        let error = false;
        let errorType = ErrorType.ALREADY_EXISTING;
        this.alreadyExistingBpmnProcessIds = [];
        const numProcessModels = this.processModelsToUpload.length;
        let i = 0;
        for (const processModel of this.processModelsToUpload) {
          this.progress += progressSteps;
          this.currentlyUploadingProcessModel = processModel;
          i += 1;
          this.currentUploadStatus = `${i}/${numProcessModels}`;

          try {
            await this.uploadProcessModel(processModel);
          } catch (e) {
            if ((e as AxiosError).response?.status === 400) {
              error = true;
              this.alreadyExistingBpmnProcessIds.push(
                (e as HttpError).response.data.data,
              );
            } else {
              error = true;
              errorType = ErrorType.UNKNOWN;
            }
          }

          this.progress += progressSteps;
        }
        this.afterUploadActions();
        if (error) {
          this.errorType = errorType;
          this.errorDialog = true;
        }
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
        await axios.post(
          "/api/project/" +
            this.selectedProjectId +
            "/process-model/" +
            this.processModelToBeReplacedId,
          formData,
          { headers: authHeader() }
        );
        this.afterUploadActions();
      } catch (e) {
        this.afterUploadActions();
        this.errorType = ErrorType.CANT_REPLACE_WITH_COLLABORATION;
        this.errorDialog = true;
      }
    },

    afterUploadActions() {
      this.fetchProcessModels();
      this.progressDialog = false;
      this.progress = 0;
      this.closeUploadDialog();
      this.appStore.setProcessModelsChanged();
    },

    closeUploadDialog() {
      this.uploadDialog = false;
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
