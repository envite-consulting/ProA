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
          {{ new Date(model.createdAt).toLocaleString("de-DE") }} {{ !!model.description ? '-' : '' }} {{
            model.description
          }}
        </v-list-item-subtitle>
        <template v-slot:append>
          <v-btn color="grey-lighten-1" icon="mdi-delete" variant="text"
                 @click="() => deleteProcessModel(model.id)"></v-btn>
          <v-btn color="grey-lighten-1" icon="mdi-more" variant="text" :to="'/ProcessView/' + model.id"></v-btn>
          <v-btn color="grey-lighten-1" icon="mdi-information" variant="text"
                 @click="() => showProcessInfoDialog(model.id)"></v-btn>
        </template>
      </v-list-item>
      <v-divider v-if="index < processModels.length - 1" :key="`${index}-divider`"></v-divider>
    </template>
  </v-list>
  <div class="ma-4" style="position: absolute; bottom: 8px; right: 8px;">
    <v-fab-transition>
      <v-btn class="mt-auto pointer-events-initial" color="primary" elevation="8" icon="mdi-plus"
             @click="uploadDialog = true" size="large"/>
    </v-fab-transition>
  </div>

  <v-dialog v-model="uploadDialog" persistent width="600" @after-leave="resetUploadDialog">
    <v-card>
      <v-card-title>
        <span class="text-h5">Prozessmodelle hochladen</span>
      </v-card-title>
      <v-card-text>
        <v-container>
          <v-row class="pb-5">
            <v-col cols="12" sm="12" md="12">
              <v-file-input hide-details label="Prozessmodelle" v-model="processModelFiles" chips multiple
                            @change="handleFileSelection"></v-file-input>
            </v-col>
          </v-row>
          <v-row v-for="(file, index) in processModelsToUpload" :key="'file-' + index" class="py-5 mt-0">
            <p class="px-3">{{ file.file.name }}</p>
            <v-col cols="12" sm="12" md="12" class="py-1">
              <v-text-field hide-details label="Name" v-model="file.name"></v-text-field>
            </v-col>
            <v-col cols="12" sm="12" md="12" class="py-1">
              <v-textarea hide-details rows="3" label="Beschreibung" v-model="file.description"></v-textarea>
            </v-col>
          </v-row>
        </v-container>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn color="blue-darken-1" variant="text" @click="closeUploadDialog">
          Schließen
        </v-btn>
        <v-btn color="blue-darken-1" variant="text" @click="uploadProcessModel">
          Speichern
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>

  <v-dialog v-model="progressDialog" max-width="600">
    <v-card title="Upload">
      <template v-slot:text>
        Lade Prozessmodelle hoch...
        <v-progress-linear color="primary" :model-value="progress" :height="10"></v-progress-linear>
      </template>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn text="Close" variant="text" @click="progressDialog = false"></v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<style></style>

<script lang="ts">
import {defineComponent} from 'vue';
import axios from 'axios';
import ProcessDetailDialog from '@/components/ProcessDetailDialog.vue';
import {useAppStore} from "@/store/app";
import getProject from "../projectService";

declare interface ProcessModel {
  id: number,
  processName: string
  description: string
  createdAt: string
}

export default defineComponent({
  components: {
    ProcessDetailDialog
  },
  data: () => ({
    uploadDialog: false,
    progressDialog: false,
    progress: 0,
    processModelFiles: [] as File[],
    processModelsToUpload: [] as { file: File, name: string, description: string }[],
    processModels: [] as ProcessModel[],
    selectedProjectId: null as number | null,
    selectedProjectName: '' as string,
    selectedVersionName: '' as string,
  }),
  mounted: function () {
    this.selectedProjectId = useAppStore().selectedProjectId;
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
  methods: {
    showProcessInfoDialog(processId: number) {
      (this.$refs.processDetailDialog as InstanceType<typeof ProcessDetailDialog>).showProcessInfoDialog(processId);
    },
    deleteProcessModel(processId: number) {
      axios.delete("/api/process-model/" + processId).then(() => {
        this.fetchProcessModels();
      })
    },
    fetchProcessModels() {
      axios.get("/api/project/" + this.selectedProjectId + "/process-model").then(result => {
        this.processModels = result.data;
      })
    },
    handleFileSelection() {
      this.processModelsToUpload = [];

      const readFileContent = (file: File) => {
        return new Promise<string>((resolve, reject) => {
          const reader = new FileReader();
          reader.onload = (event) => {
            resolve(event.target?.result as string);
          };
          reader.onerror = (error) => {
            reject(error);
          };
          reader.readAsText(file);
        });
      };

      const parseBPMNContent = (content: string) => {
        const parser = new DOMParser();
        const xmlDoc = parser.parseFromString(content, "text/xml");
        const nameElement = xmlDoc.querySelector("bpmn\\:process") || xmlDoc.querySelector("process");
        const documentationElement = xmlDoc.querySelector("bpmn\\:documentation") || xmlDoc.querySelector("documentation");

        return {
          name: nameElement ? nameElement.getAttribute("name") || '' : '',
          description: documentationElement ? documentationElement.textContent || '' : '',
        };
      };

      this.processModelFiles.forEach(async (file) => {
        try {
          const content = await readFileContent(file);
          const {name, description} = parseBPMNContent(content);
          const fileExtensionMatcher = /\.[^/.]+$/;
          this.processModelsToUpload.push({
            file,
            name: name || file.name.replace(fileExtensionMatcher, ""),
            description,
          });
        } catch (error) {
          console.error("Error reading file content: ", error);
        }
      });
    },
    async uploadProcessModel() {
      if (this.processModelsToUpload.length > 0) {
        this.progressDialog = true;
        const progressSteps = 100 / (this.processModelsToUpload.length * 2);

        for (const processModel of this.processModelsToUpload) {
          this.progress += progressSteps;

          let formData = new FormData();
          formData.append("processModel", processModel.file);
          formData.append("fileName", processModel.name);
          formData.append("description", processModel.description);

          await axios.post("/api/project/" + this.selectedProjectId + "/process-model", formData);

          this.progress += progressSteps;
        }
        this.fetchProcessModels();
        this.closeUploadDialog();
      }
    },
    closeUploadDialog() {
      this.uploadDialog = false;
    },
    resetUploadDialog() {
      this.processModelFiles = [];
      this.processModelsToUpload = [];
      this.progressDialog = false;
      this.progress = 0;
    }
  }
})
</script>
