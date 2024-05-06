<template>

  <v-container fluid style="margin: auto">

    <v-card v-for="(group, index) in projectGroups" :key="index" width="300px" height="252px"
            style="float: left; margin: 16px">

      <v-card-title v-text="group.name"></v-card-title>

      <v-card-text>
        <v-select
          label="Version"
          density="compact"
          v-model="activeProjectByGroup[group.name]"
          :items="group.projects"
          item-title="version"
          item-value="id"
          hide-details
          @update:model-value="setActiveProject(group.name, $event)"
        ></v-select>
        <v-btn variant="plain" class="pa-0" @click="openNewVersionDialog(group)">
          <v-icon icon="mdi-plus" size="large"></v-icon>
          Neue Version
        </v-btn>
        <div>Erstellt am: {{
            formatDate(activeProjectByGroup[group.name].createdAt)
          }}
        </div>
        <div>Zuletzt geändert am: {{
            formatDate(activeProjectByGroup[group.name].modifiedAt)
          }}
        </div>
      </v-card-text>
      <v-divider></v-divider>
      <v-list-item append-icon="mdi-chevron-right" lines="two" subtitle="Öffnen" link
                   @click="() => openProject(activeProjectByGroup[group.name].id)"></v-list-item>
    </v-card>

    <v-card width="300px" height="252px" style="float: left; margin: 16px" class="d-flex flex-column">

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
            <v-row>
              <v-col cols="12" sm="12" md="12">
                <v-text-field label="Version" v-model="newProjectVersionName"></v-text-field>
              </v-col>
            </v-row>
          </v-container>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="blue-darken-1" variant="text" @click="projectDialog = false">
            Schließen
          </v-btn>
          <v-btn color="blue-darken-1" variant="text" @click="createProject()">
            Speichern
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
    <v-dialog v-model="showNewVersionDialog" persistent width="600">
      <v-card>
        <v-card-title class="pt-4 pb-0 px-5">
          Neue Version für {{ newVersionForGroup.name }}
        </v-card-title>
        <v-card-text>
          <v-container>
            <!-- TODO: Add version select to copy from existing version -->
            <!--            <v-row>-->
            <!--              <v-col cols="12" sm="12" md="12">-->
            <!--                <v-select-->
            <!--                  label="Version"-->
            <!--                  v-model="newVersionInitialProject"-->
            <!--                  :items="newVersionForGroup.projects"-->
            <!--                  item-title="version"-->
            <!--                  item-value="id"-->
            <!--                  hide-details-->
            <!--                ></v-select>-->
            <!--              </v-col>-->
            <!--            </v-row>-->
            <!--            <v-row>-->
            <!--              <v-col cols="12" sm="12" md="12" class="py-0 ps-5">-->
            <!--                <v-icon icon="mdi-arrow-right" size="large"></v-icon>-->
            <!--              </v-col>-->
            <!--            </v-row>-->
            <v-row>
              <v-col cols="12" sm="12" md="12">
                <v-text-field label="Neue Version" v-model="newVersionName"></v-text-field>
              </v-col>
            </v-row>
          </v-container>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="blue-darken-1" variant="text" @click="showNewVersionDialog = false">
            Schließen
          </v-btn>
          <v-btn color="blue-darken-1" variant="text" @click="createProject(newVersionForGroup.name, newVersionName)">
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
import { useAppStore } from "@/store/app";

interface Project {
  id: number
  name: string
  version: string
  createdAt: string
  modifiedAt: string
}

interface ProjectGroup {
  name: string,
  projects: Project[]
}

interface ActiveProjectByGroup {
  [key: string]: Project
}

export default defineComponent({
  data: () => ({
    projects: [] as Project[],
    projectDialog: false as boolean,
    showNewVersionDialog: false as boolean,
    newProjectName: "" as string,
    newProjectVersionName: "1.0",
    store: useAppStore(),
    activeProjectByGroup: {} as ActiveProjectByGroup,
    newVersionForGroup: {} as ProjectGroup,
    newVersionName: "" as string,
    newVersionInitialProject: {} as Project
  }),

  computed: {
    projectGroups(): ProjectGroup[] {
      const groupedProjects: { [key: string]: Project[] } = {};
      for (const project of this.projects) {
        if (!groupedProjects[project.name]) {
          groupedProjects[project.name] = [];
        }
        groupedProjects[project.name].push(project);
      }

      const projectGroups: ProjectGroup[] = Object.keys(groupedProjects).map(name => {
        return {
          name: name,
          projects: groupedProjects[name].sort((a, b) => {
            return new Date(b.modifiedAt).getTime() - new Date(a.modifiedAt).getTime();
          })
        }
      });

      for (const group of projectGroups) {
        const persistedActiveProject = this.store.getActiveProjectForGroup(group.name);
        this.activeProjectByGroup[group.name] = persistedActiveProject ? JSON.parse(persistedActiveProject) : group.projects[0];
      }

      return projectGroups;
    }
  },

  watch: {},
  mounted: function () {
    this.fetchProjects();
  },
  methods: {
    openNewVersionDialog(projectGroup: ProjectGroup) {
      this.newVersionForGroup = projectGroup;
      this.showNewVersionDialog = true;
      this.newVersionInitialProject = this.activeProjectByGroup[projectGroup.name];
    },
    setActiveProject(groupName: string, projectId: number) {
      this.activeProjectByGroup[groupName] = this.projects.find(project => project.id === projectId)!;
      this.store.setActiveProjectForGroup(groupName, JSON.stringify(this.activeProjectByGroup[groupName]));
    },
    formatDate(dateString: string) {
      return new Date(dateString).toLocaleString("de-DE");
    },
    fetchProjects() {
      axios.get("/api/project").then(result => {
        this.projects = result.data;
      })
    },
    createProject(projectName: string = '', projectVersion: string = '') {
      let formData = new FormData();
      formData.append("name", projectName || this.newProjectName);
      formData.append("version", projectVersion || this.newProjectVersionName);

      axios.post("/api/project", formData).then(result => {
        this.projectDialog = false;
        this.showNewVersionDialog = false;
        this.newProjectName = "";
        this.newProjectVersionName = "1.0";
        this.newVersionForGroup = {} as ProjectGroup;
        this.newVersionName = "";
        this.setActiveProject(result.data.name, result.data.id);
        this.projects.push(result.data);
      });
    },
    openProject(id: number) {
      useAppStore().selectedProjectId = id;
      this.$router.push("/ProcessList")
    }
  }
});
</script>
