<template>
  <v-toolbar>
    <v-toolbar-title>
      <div class="d-flex align-center">
        <span>{{ selectedProjectName }}</span>
        <span class="text-body-2 text-grey-darken-1 ms-4">VERSION {{ selectedVersionName }}</span>
      </div>
    </v-toolbar-title>
  </v-toolbar>
  <ProcessDetailDialog ref="processDetailDialog"/>
  <v-list lines="two" class="pa-6">
    <template v-for="(model, index) in processModels" :key="'process-'+model.id">
      <v-list-item>
        <v-list-item-title>{{ model.processName }}</v-list-item-title>

        <v-list-item-subtitle>
          {{ getLocaleDate(model.createdAt) }} {{ !!model.description ? '-' : '' }} {{
            model.description
          }}
        </v-list-item-subtitle>
        <template v-slot:append>
          <v-btn color="grey-lighten-1" icon="mdi-delete" variant="text"
                 @click="async () => await deleteProcessModel(model.id)"></v-btn>
          <v-btn color="grey-lighten-1" icon="mdi-upload" variant="text"
                 @click="openSingleUploadDialog(model.id)"></v-btn>
          <v-btn color="grey-lighten-1" icon="mdi-more" variant="text" :to="'/ProcessView/' + model.id"></v-btn>
          <v-btn color="grey-lighten-1" icon="mdi-information" variant="text"
                 @click="() => showProcessInfoDialog(model.id)"></v-btn>
        </template>
      </v-list-item>
      <v-divider v-if="index < processModels.length - 1" :key="`${index}-divider`"></v-divider>
    </template>
  </v-list>
  <div class="ma-4" style="position: fixed; bottom: 8px; right: 8px;">
    <v-btn class="me-5" prepend-icon="mdi-cloud-search" @click="goToC8Import">
      {{ $t('processList.importFromC8') }}
    </v-btn>
    <v-fab-transition>
      <v-btn class="mt-auto pointer-events-initial" color="primary" elevation="8" icon="mdi-plus"
             @click="openMultipleUploadDialog" size="large"/>
    </v-fab-transition>
  </div>

  <v-dialog v-model="uploadDialog" persistent width="600" @after-leave="resetUploadDialog">
    <v-card>
      <v-card-title>
        <span class="text-h5" v-if="uploadDialogMode === 'multiple'">{{ $t('processList.uploadProcessModels') }}</span>
        <span class="text-h5" v-if="uploadDialogMode === 'single'">{{ $t('processList.replaceProcessModel') }}</span>
      </v-card-title>
      <v-card-text>
        <v-container>
          <v-row class="pb-5">
            <v-col cols="12" sm="12" md="12">
              <v-file-input v-if="uploadDialogMode === 'multiple'" :label="$t('processList.processModels')"
                            v-model="processModelFiles"
                            chips multiple @change="handleFileSelection"></v-file-input>
              <v-file-input v-if="uploadDialogMode === 'single'" :label="$t('general.processModel')"
                            v-model="processModelFiles" chips
                            @change="handleFileSelection"></v-file-input>
            </v-col>
          </v-row>
          <v-row v-for="(file, index) in processModelsToUpload" :key="'file-' + index" class="py-5 mt-0">
            <p class="px-3">{{ file.file.name }}</p>
            <v-col cols="12" sm="12" md="12" class="py-1">
              <v-text-field hide-details label="Name" v-model="file.name"></v-text-field>
            </v-col>
            <v-col cols="12" sm="12" md="12" class="py-1">
              <v-textarea hide-details rows="3" :label="$t('general.description')"
                          v-model="file.description"></v-textarea>
            </v-col>
          </v-row>
        </v-container>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn color="blue-darken-1" variant="text" @click="closeUploadDialog">
          {{ $t('general.cancel') }}
        </v-btn>
        <v-btn v-if="uploadDialogMode === 'multiple'" color="blue-darken-1" variant="text" @click="uploadProcessModels">
          {{ $t('general.save') }}
        </v-btn>
        <v-btn v-if="uploadDialogMode === 'single'" color="blue-darken-1" variant="text" @click="swapProcessModel">
          {{ $t('general.save') }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>

  <v-dialog v-model="progressDialog" max-width="600">
    <v-card title="Upload">
      <template v-slot:text>
        {{ $t('processList.uploadingProcessModels') }}
        <v-progress-linear color="primary" :model-value="progress" :height="10"></v-progress-linear>
      </template>

      <v-card-actions>
        <v-spacer></v-spacer>

        <v-btn :text="$t('general.cancel')" variant="text" @click="progressDialog = false"></v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>

</template>

<style></style>

<script lang="ts">
import { defineComponent } from 'vue';
import axios from 'axios';
import ProcessDetailDialog from '@/components/ProcessDetailDialog.vue';
import { useAppStore } from "@/store/app";
import getProject from "../projectService";

declare interface ProcessModel {
  id: number,
  processName: string
  description: string
  createdAt: string
}

enum UploadDialogMode {
  SINGLE = 'single',
  MULTIPLE = 'multiple'
}

interface ProcessModelToUpload {
  file: File
  name: string
  description: string
}

export default defineComponent({

  components: {
    ProcessDetailDialog
  },
  data: () => ({
    appStore: useAppStore(),
    uploadDialog: false as boolean,
    uploadDialogMode: UploadDialogMode.MULTIPLE as UploadDialogMode,
    replaceProcessModel: null as number | null,
    progressDialog: false,
    progress: 0,
    processModelFiles: [] as File[],
    processModelsToUpload: [] as ProcessModelToUpload[],
    processModels: [] as ProcessModel[],
    selectedProjectId: null as number | null,
    selectedProjectName: '' as string,
    selectedVersionName: '' as string,
    fileExtensionMatcher: /.[^/.]+$/,
  }),
  mounted: function () {
    this.selectedProjectId = this.appStore.selectedProjectId;
    if (!this.selectedProjectId) {
      this.$router.push("/");
      return;
    }
    getProject(this.selectedProjectId).then(result => {
      this.selectedProjectName = result.data.name;
      this.selectedVersionName = result.data.version;
    })
    this.fetchProcessModels();
  },
  watch: {},
  methods: {
    showProcessInfoDialog(processId: number) {
      (this.$refs.processDetailDialog as InstanceType<typeof ProcessDetailDialog>).showProcessInfoDialog(processId);
    },

    async deleteProcessModel(processId: number) {
      await axios.delete("/api/process-model/" + processId).then(() => {
        this.appStore.setProcessModelsChanged();
        this.fetchProcessModels();
      })
    },

    fetchProcessModels() {
      axios.get("/api/project/" + this.selectedProjectId + "/process-model").then(result => {
        this.processModels = result.data;
      })
    },

    openSingleUploadDialog(modelId: number) {
      this.uploadDialog = true;
      this.uploadDialogMode = UploadDialogMode.SINGLE;
      this.replaceProcessModel = modelId;
    },

    openMultipleUploadDialog() {
      this.uploadDialog = true;
      this.uploadDialogMode = UploadDialogMode.MULTIPLE;
    },

    async handleFileSelection() {
      this.processModelsToUpload = [];

      const readFileContent = (file: File): Promise<string> => {
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
      };

      const parseBPMNContent = (content: string): Omit<ProcessModelToUpload, "file"> => {
        const parser = new DOMParser();
        const xmlDoc = parser.parseFromString(content, "text/xml");
        const nameElement = xmlDoc.querySelector("bpmn\\:process") || xmlDoc.querySelector("process");
        const documentationElement = xmlDoc.querySelector("bpmn\\:documentation") || xmlDoc.querySelector("documentation");

        return {
          name: nameElement ? nameElement.getAttribute("name") || '' : '',
          description: documentationElement ? documentationElement.textContent || '' : '',
        };
      };

      for (const file of this.processModelFiles) {
        try {
          const content = await readFileContent(file);
          const { name, description } = parseBPMNContent(content);
          this.processModelsToUpload.push({
            file,
            name: name || file.name.replace(this.fileExtensionMatcher, ""),
            description,
          });
        } catch (error) {
          console.error("Error reading file content: ", error);
        }
      }
    },

    async uploadProcessModel(processModel: ProcessModelToUpload) {
      let formData = new FormData();
      const fileName = processModel.name || processModel.file.name.replace(this.fileExtensionMatcher, "");
      formData.append("processModel", processModel.file);
      formData.append("fileName", fileName);
      formData.append("description", processModel.description);

      await axios.post("/api/project/" + this.selectedProjectId + "/process-model", formData);
    },

    async uploadProcessModels() {
      if (this.processModelsToUpload.length > 0) {
        this.progressDialog = true;
        const progressSteps = 100 / (this.processModelsToUpload.length * 2);

        for (const processModel of this.processModelsToUpload) {
          this.progress += progressSteps;

          await this.uploadProcessModel(processModel);

          this.progress += progressSteps;
        }
        this.afterUploadActions();
      }
    },

    async swapProcessModel() {
      if (this.processModelsToUpload.length === 1) {
        this.progressDialog = true;
        const oldModelId = this.replaceProcessModel;
        await this.deleteProcessModel(oldModelId!);
        this.progress += 50;

        await this.uploadProcessModel(this.processModelsToUpload[0]);
        this.afterUploadActions();
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
      this.replaceProcessModel = null;
      this.progress = 0;
    },
    goToC8Import() {
      this.$router.push("CamundaCloudImport");
    },
    getLocaleDate(date: string): string {
      const locales = this.appStore.getSelectedLanguage() === 'de' ? 'de-DE' : 'en-US';
      return new Date(date).toLocaleString(locales);
    }
  }
})
</script>
