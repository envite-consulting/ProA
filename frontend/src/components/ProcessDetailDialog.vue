<template>
  <v-dialog v-model="infoDialog" persistent width="600">
    <v-card>
      <v-card-title>
        <span class="text-h5">Prozesmodell: {{details.name}}</span>
      </v-card-title>
      <v-card-text>
        <v-container>
          <v-row>
            <v-col cols="12" sm="6" md="6">
              <b>Start Events</b>
              <ul>
                <li v-for="(event, index) in details.startEvents" :key="index+'-startevent'">{{ event.label }}</li>
              </ul>
            </v-col>
            <v-col cols="12" sm="6" md="6">
              <b>End Events</b>
              <ul>
                <li v-for="(event, index) in details.endEvents" :key="index+'-endevent'">{{ event.label }}</li>
              </ul>
            </v-col>
          </v-row>
          <v-row>
            <b>Beschreibung:</b>
          </v-row>
          <v-row>
            <p>{{ details.description }}</p>
          </v-row>
        </v-container>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn color="blue-darken-1" variant="text" :to="'/ProcessView/' + details.id">
          Prozessmodell
        </v-btn>
        <v-btn color="blue-darken-1" variant="text" @click="infoDialog = false">
          Schließen
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>
<style></style>
<script lang="ts">
import { defineComponent } from 'vue';
import axios from 'axios';

declare interface Process {
  id: number,
  processName: string
  description: string
  startEvents: Event[]
  endEvents: Event[]
}

declare interface Event{
  label: string
}

export default defineComponent({
  data: () => ({

    infoDialog: false,
    details: {} as Process

  }),
  methods: {

    showProcessInfoDialog(processId: number) {
      axios.get("/api/process-model/" + processId + "/details").then(result => {
        this.details = result.data;
        this.infoDialog=true;
      })
    },
  },
})

</script>