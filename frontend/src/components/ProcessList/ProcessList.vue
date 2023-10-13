<template>
  <v-list lines="one">
  <v-list-item
    v-for="model in processModels"
    :key="'process-'+model.id"
    :title="model.processName"
    :to="'/ProcessView/'+model.id"
  ></v-list-item>
</v-list>
  <div class="ma-4" style="position: absolute; bottom: 8px; right: 8px;">
    <v-fab-transition>
      <v-btn class="mt-auto pointer-events-initial" color="primary" elevation="8" icon="mdi-plus"
        @click="dialog = true" size="large" />
    </v-fab-transition>
  </div>

  <v-dialog v-model="dialog" persistent width="1024">
    <v-card>
      <v-card-title>
        <span class="text-h5">Prozesmodell hochladen</span>
      </v-card-title>
      <v-card-text>
        <v-container>
          <v-row>
            <v-col cols="12" sm="6" md="4">
              <v-file-input label="Prozessmodell" v-model="processModel"></v-file-input>
            </v-col>
            <v-col cols="12" sm="6" md="4">
              <v-text-field label="Beschreibung" hint="example of helper text only on focus"></v-text-field>
            </v-col>
          </v-row>
        </v-container>
        <small>*indicates required field</small>
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
}

export default defineComponent({
  data: () => ({
    dialog: false,
    processModel: [] as File[],
    processModels: [] as ProcessModel[],
  }),
  mounted: function () {
    this.fetchProcessModels();
  },
  methods: {

    fetchProcessModels(){
      axios.get("/api/process-model").then(result=>{
        this.processModels = result.data;
      })
    },

    uploadProcessModel() {
      if (this.processModel !== null && this.processModel.length>0) {

        let formData = new FormData();
        formData.append("processModel", this.processModel[0]);
        formData.append("fileName", this.processModel[0].name);

        console.log(this.processModel[0].name);

        axios.post("/api/process-model", formData)
          .then(response => {
            console.log(response);
            this.fetchProcessModels();
            this.processModel=[];
            this.dialog = false;
          }).catch(error => {
            console.log(error)
          });
      }
    }
  }


})
</script>