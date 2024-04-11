<template>
  <v-list lines="two" class="pa-6">
    <template v-for="(model, index) in processModels" :key="'process-'+model.id">
      <v-list-item>
        <v-list-item-title>{{ model.name }}</v-list-item-title>

        <v-list-item-subtitle>
          {{ new Date(model.created).toLocaleString("de-DE") }} - {{ model.updatedBy.email }}
        </v-list-item-subtitle>
        <template v-slot:prepend="{ isActive }">
          <v-list-item-action start>
            <v-checkbox v-model="selectedProcessModels" :value="model"></v-checkbox>
          </v-list-item-action>
        </template>
      </v-list-item>
      <v-divider v-if="index < processModels.length - 1" :key="`${index}-divider`"></v-divider>
    </template>
    <v-btn prepend-icon="mdi-import" @click="importProcessModels" v-if="selectedProcessModels.length > 0">
      Prozessmodelle importieren
    </v-btn>
  </v-list>
  <v-dialog v-model="camundaCloudDialog" persistent width="600">
    <v-card>
      <v-card-title>
        <span class="text-h5">Prozesse aus C8 importieren</span>
      </v-card-title>
      <v-card-text>
        <v-container>
          <v-row>
            <v-col v-if="!!token">
              <v-icon icon="mdi-check-circle" color="green"></v-icon>
              Mit Camunda 8 verbunden. Sie können Prozessmodelle abrufen.
            </v-col>
            <v-col v-if="!token" cols="12" sm="12" md="12">
              <v-text-field v-model="clientId" class="text-field__styled" dense color="#26376B"
                placeholder="Client ID"></v-text-field>
            </v-col>
            <v-col v-if="!token" cols="12" sm="12" md="12">
              <v-text-field v-model="clientSecret" class="text-field__styled" dense color="#26376B"
                placeholder="Client Secret"></v-text-field>
            </v-col>
            <v-col v-if="tokenError">
              Es ist ein Fehler aufgetreten!
            </v-col>
            <v-col v-if="!token">
              <v-btn color="blue-darken-1" @click="fetchToken">
                Verbinden
              </v-btn>
            </v-col>
            <v-col cols="12" sm="12" md="12">
              <v-text-field :disabled="!token" v-model="creatorEMail" class="text-field__styled" dense color="#26376B"
                placeholder="Ersteller E-Mail"></v-text-field>
            </v-col>
            <v-col>
              <v-btn :disabled="!token" color="blue-darken-1" @click="fetchProcessModels">
                Prozessmodelle abrufen
              </v-btn>
            </v-col>
          </v-row>
        </v-container>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn color="blue-darken-1" variant="text" @click="camundaCloudDialog = false">
          Schließen
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
  <v-dialog
      v-model="loadingDialog"
      max-width="320"
      persistent
    >
      <v-list
        class="py-2"
        color="primary"
        elevation="12"
        rounded="lg"
      >
        <v-list-item
          title="Laden ..."
        >
          <template v-slot:append>
            <v-progress-circular
              color="primary"
              indeterminate="disable-shrink"
              size="16"
              width="2"
            ></v-progress-circular>
          </template>
        </v-list-item>
      </v-list>
    </v-dialog>
  <div class="ma-4" style="position: absolute; bottom: 8px; right: 8px;">
    <v-fab-transition>
      <v-btn class="mt-auto pointer-events-initial" color="primary" elevation="8" icon="mdi-cloud-search"
        @click="camundaCloudDialog = true" size="large" />
    </v-fab-transition>
  </div>
</template>
<style scoped></style>
<script lang="ts">
import { defineComponent } from 'vue'
import axios from 'axios';
import { moduleExpression } from '@babel/types';

declare interface ProcessModel {
  id: string,
  name: string
  created: string
  updatedBy: {
    email: string
  }
}

export default defineComponent({
  data: () => ({
    camundaCloudDialog: false as boolean,
    loadingDialog: false as boolean,
    clientId: "" as string,
    clientSecret: "" as string,
    creatorEMail: "" as string,
    processModels: [] as ProcessModel[],
    selectedProcessModels: [] as ProcessModel[],
    tokenError: false,
    token: null,
  }),

  watch: {
  },
  mounted: function () {
  },
  methods: {
    fetchToken() {
      this.loadingDialog = true;
      axios.post("/api/camunda-cloud/token", {
        "client_id": this.clientId,
        "client_secret": this.clientSecret,
      }).then(result => {
        console.log(result);
        this.token = result.data;
        this.tokenError = false;
        this.loadingDialog = false;
      }).catch(error => {
        console.log(error);
        this.tokenError = true;
        this.loadingDialog = false;
      })
    },
    fetchProcessModels() {
      this.loadingDialog = true;
      axios.post("/api/camunda-cloud", {
        "token": this.token,
        "email": this.creatorEMail,
      }).then(result => {
        this.processModels = result.data.items;
        this.camundaCloudDialog=false;
        this.loadingDialog = false;
      }).catch(error => {
        console.log(error);
        this.tokenError = true;
        this.token = null;
        this.loadingDialog = false;
      })
    },
    importProcessModels() {

      this.loadingDialog = true;
      const selectedProcessModelIds: string[] = this.selectedProcessModels.map(model => {
        return model.id;
      });

      axios.post("/api/camunda-cloud/import", {
        token: this.token,
        selectedProcessModelIds: selectedProcessModelIds,
      }).then(() => {
        this.processModels = this.processModels.filter(model => !selectedProcessModelIds.includes(model.id));
        this.selectedProcessModels = [];
        this.loadingDialog = false;
      }).catch(error => {
        console.log(error);
        this.loadingDialog = false;
      });
    }
  }
})
</script>
