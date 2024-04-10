<template>
  <v-list lines="two" class="pa-6">
    <template v-for="(model, index) in processModels" :key="'process-'+model.id">
      <v-list-item>
        <v-list-item-title>{{ model.name }}</v-list-item-title>

        <v-list-item-subtitle>
          {{ new Date(model.created).toLocaleString("de-DE") }} - {{ model.updatedBy.email }}
        </v-list-item-subtitle>
      </v-list-item>
      <v-divider v-if="index < processModels.length - 1" :key="`${index}-divider`"></v-divider>
    </template>
  </v-list>
  <v-dialog v-model="camundaCloudDialog" persistent width="600">
    <v-card>
      <v-card-title>
        <span class="text-h5">Prozesse aus C8 importieren</span>
      </v-card-title>
      <v-card-text>
        <v-container>
          <v-row>
            <v-col cols="12" sm="12" md="12">
              <v-text-field v-model="clientId" class="text-field__styled" dense color="#26376B"
                placeholder="Client ID"></v-text-field>
            </v-col>
            <v-col cols="12" sm="12" md="12">
              <v-text-field v-model="clientSecret" class="text-field__styled" dense color="#26376B"
                placeholder="Client Secret"></v-text-field>
            </v-col>
            <v-col cols="12" sm="12" md="12">
              <v-text-field v-model="creatorEMail" class="text-field__styled" dense color="#26376B"
                placeholder="Ersteller E-Mail"></v-text-field>
            </v-col>
          </v-row>
        </v-container>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn color="blue-darken-1" variant="text" @click="camundaCloudDialog = false">
          Schließen
        </v-btn>
        <v-btn color="blue-darken-1" variant="text" @click="fetchProcessModels">
          Speichern
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
  <div class="ma-4" style="position: absolute; bottom: 8px; right: 8px;">
    <v-fab-transition>
      <v-btn class="mt-auto pointer-events-initial" color="primary" elevation="8" icon="mdi-plus"
        @click="camundaCloudDialog = true" size="large" />
    </v-fab-transition>
  </div>
</template>
<style scoped></style>
<script lang="ts">
import { defineComponent } from 'vue'
import axios from 'axios';

declare interface ProcessModel {
  id: number,
  name: string
  created: string
  updatedBy: {
    email: string
  }
}

export default defineComponent({
  data: () => ({
    camundaCloudDialog: false as boolean,
    clientId: "" as string,
    clientSecret: "" as string,
    creatorEMail: "" as string,
    processModels: [] as ProcessModel[],
  }),

  watch: {
  },
  mounted: function () {
  },
  methods: {
    fetchProcessModels() {
      axios.post("/api/camunda-cloud", {
        "client_id": this.clientId,
        "client_secret": this.clientSecret,
        "email": this.creatorEMail,
      }).then(result => {
        console.log(result);
        this.processModels = result.data.items;
      })
    }
  }
})
</script>
