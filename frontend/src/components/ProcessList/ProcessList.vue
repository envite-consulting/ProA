<template>
  <v-list lines="two" class="pa-6">
    <template v-for="(model, index) in processModels" :key="'process-'+model.id" >
    <v-list-item >
        <v-list-item-title>{{model.processName}}</v-list-item-title>

        <v-list-item-subtitle>
          {{new Date(model.createdAt).toLocaleString("de-DE")}} {{!!model.description ? '-' : ''}} {{model.description}}
        </v-list-item-subtitle>
        <template v-slot:append>
          <v-btn
            color="grey-lighten-1"
            icon="mdi-more"
            variant="text"
            :to="'/ProcessView/' + model.id"
          ></v-btn>
          <v-btn
            color="grey-lighten-1"
            icon="mdi-information"
            variant="text"
          ></v-btn>
        </template>
      </v-list-item>
      <v-divider v-if="index < processModels.length - 1" :key="`${index}-divider`"></v-divider>
    </template>
  </v-list>
  <div class="ma-4" style="position: absolute; bottom: 8px; right: 8px;">
    <v-fab-transition>
      <v-btn class="mt-auto pointer-events-initial" color="primary" elevation="8" icon="mdi-plus" @click="dialog = true"
        size="large" />
    </v-fab-transition>
  </div>

  <v-dialog v-model="dialog" persistent width="600">
    <v-card>
      <v-card-title>
        <span class="text-h5">Prozesmodell hochladen</span>
      </v-card-title>
      <v-card-text>
        <v-container>
          <v-row>
            <v-col cols="12" sm="12" md="12">
              <v-file-input label="Prozessmodell" v-model="processModel"></v-file-input>
            </v-col>
            <v-col cols="12" sm="12" md="12">
              <v-textarea label="Beschreibung" v-model="description"></v-textarea>
            </v-col>
          </v-row>
        </v-container>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn color="blue-darken-1" variant="text" @click="dialog = false">
          Close
        </v-btn>
        <v-btn color="blue-darken-1" variant="text" @click="uploadProcessModel">
          Save
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>
<style></style>
<script lang="ts">
import { defineComponent } from 'vue';
import axios from 'axios';

declare interface ProcessModel {
  id: number,
  processName: string
  descrption: string
  createdAt: string
}

export default defineComponent({
  data: () => ({
    dialog: false,
    description: '',
    processModel: [] as File[],
    processModels: [] as ProcessModel[],
  }),
  mounted: function () {
    this.fetchProcessModels();
  },
  methods: {

    fetchProcessModels() {
      axios.get("/api/process-model").then(result => {
        this.processModels = result.data;
      })
    },

    uploadProcessModel() {
      if (this.processModel !== null && this.processModel.length > 0) {

        let formData = new FormData();
        formData.append("processModel", this.processModel[0]);
        formData.append("fileName", this.processModel[0].name);
        formData.append("description", this.description);

        console.log(this.processModel[0].name);

        axios.post("/api/process-model", formData)
          .then(response => {
            console.log(response);
            this.fetchProcessModels();
            this.processModel = [];
            this.dialog = false;
          }).catch(error => {
            console.log(error)
          });
      }
    }
  }


})
</script>