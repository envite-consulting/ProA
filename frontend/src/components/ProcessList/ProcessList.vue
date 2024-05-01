<template>
  <v-toolbar v-if="selectedProjectName">
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

  <v-dialog v-model="uploadDialog" persistent width="600">
    <v-card>
      <v-card-title>
        <span class="text-h5">Prozessmodell hochladen</span>
      </v-card-title>
      <v-card-text>
        <v-container>
          <v-row>
            <v-col cols="12" sm="12" md="12">
              <v-file-input label="Prozessmodell" v-model="processModel" chips multiple></v-file-input>
            </v-col>
            <v-col cols="12" sm="12" md="12">
              <v-textarea label="Beschreibung" v-model="description"></v-textarea>
            </v-col>
          </v-row>
        </v-container>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn color="blue-darken-1" variant="text" @click="uploadDialog = false">
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
import { defineComponent } from 'vue';
import axios from 'axios';
import ProcessDetailDialog from '@/components/ProcessDetailDialog.vue';
import { useAppStore } from "../../store/app";
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
    description: '',
    processModel: [] as File[],
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
  watch: {},
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

    async uploadProcessModel() {
      if (this.processModel !== null && this.processModel.length > 0) {

        this.progressDialog = true;
        const progressSteps = 100 / (this.processModel.length * 2);

        for (const processModel of this.processModel) {
          this.progress += progressSteps;

          let formData = new FormData();
          formData.append("processModel", processModel);
          formData.append("fileName", processModel.name);
          formData.append("description", this.description);

          await axios.post("/api/project/" + this.selectedProjectId + "/process-model", formData);

          this.progress += progressSteps;
        }
        this.fetchProcessModels();
        this.processModel = [];
        this.description = '';
        this.progressDialog = false;
        this.progress = 0;
        this.uploadDialog = false;
      }
    },

  }
})
</script>
