<template>

  <v-container fluid style="margin: auto">

    <v-card v-for="project in projects" :key="'project-' + project.id" width="300px" height="170px"
      style="float: left; margin: 16px">

      <v-card-title v-text="project.name"></v-card-title>
      <v-card-text>
        <div>Erstellt am: {{ new Date(project.createdAt).toLocaleString("de-DE") }}</div>
        <div>Zuletzt geändert am: {{ new Date(project.modifiedAt).toLocaleString("de-DE") }}</div>
      </v-card-text>
      <v-divider></v-divider>
      <v-list-item append-icon="mdi-chevron-right" lines="two" subtitle="Öffnen" link @click="() => openProject(project.id)"></v-list-item>
    </v-card>

    <v-card width="300px" height="170px" style="float: left; margin: 16px" class="d-flex flex-column">

      <v-card-title>
        <div style="text-align: center; margin-top: 25px">
          <v-icon icon="mdi-plus" size="x-large">
          </v-icon>
        </div>
        <div style="text-align: center;">
          Neues Projekt
        </div>
      </v-card-title>
      <v-spacer></v-spacer>
      <v-card-actions>
        <v-btn color="primary" text="Erstellen" block @click="projectDialog = true"></v-btn>
      </v-card-actions>
    </v-card>
    <v-dialog v-model="projectDialog" persistent width="600">
      <v-card>
        <v-card-title>
          <span class="text-h5">Projekt anlegen</span>
        </v-card-title>
        <v-card-text>
          <v-container>
            <v-row>
              <v-col cols="12" sm="12" md="12">
                <v-text-field label="Name" v-model="newProjectName"></v-text-field>
              </v-col>
            </v-row>
          </v-container>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="blue-darken-1" variant="text" @click="projectDialog = false">
            Schließen
          </v-btn>
          <v-btn color="blue-darken-1" variant="text" @click="createProject">
            Speichern
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>
<style scoped></style>
<script lang="ts">
import { defineComponent } from 'vue'
import axios from 'axios';
import {useAppStore} from "../../store/app";
import { mapState } from 'pinia'

interface Project {
  id: number
  name: string
  createdAt: string
  modifiedAt: string
}

export default defineComponent({
  data: () => ({
    projects: [] as Project[],
    projectDialog: false as boolean,
    newProjectName: "" as string,
  }),

  watch: {
  },
  mounted: function () {
    this.fetchProjects();
  },
  methods: {
    fetchProjects() {
      axios.get("/api/project").then(result => {
        this.projects = result.data;
      })
    },
    createProject() {
      let formData = new FormData();
      formData.append("name", this.newProjectName);

      axios.post("/api/project", formData).then(result => {
        this.projectDialog = false;
        this.newProjectName = "";
        this.projects.push(result.data);
      });
    },
    openProject(id: number){
      useAppStore().selectedProjectId = id;
      this.$router.push("/ProcessList")
    }
  }
})
</script>
