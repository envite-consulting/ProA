<template>

  <v-container fluid style="margin: auto">
    <v-banner
      v-if="showLoggedInBanner && webVersion && isUserLoggedIn"
      color="success"
      icon="mdi-check"
      lines="one"
      :text="$t('projectOverview.welcomeBack') + store.getUser()?.firstName + '!'"
      :stacked="false"
      sticky
    >
      <template v-slot:actions>
        <v-btn icon @click="showLoggedInBanner = false">
          <v-icon>mdi-close</v-icon>
        </v-btn>
      </template>
    </v-banner>

    <v-card v-for="(group, index) in projectGroups" :key="index" width="300px" height="252px"
            style="float: left; margin: 16px"
            :class="{ 'active-card': activeProjectByGroup[group.name]?.id === store.selectedProjectId }">

      <div class="d-flex flex-row justify-space-between align-center">
        <v-card-title v-text="group.name"></v-card-title>

        <p v-if="activeProjectByGroup[group.name]?.id === store.selectedProjectId" class="active-text">
          {{ $t('projectOverview.active') }}</p>
      </div>

      <v-card-text class="pt-0">
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
          {{ $t('projectOverview.newVersion') }}
        </v-btn>
        <div class="text-dots">{{ $t('general.createdOn') }}: {{
            formatDate(activeProjectByGroup[group.name].createdAt)
          }}
        </div>
        <div class="text-dots">{{ $t('general.lastModifiedOn') }}: {{
            formatDate(activeProjectByGroup[group.name].modifiedAt)
          }}
        </div>
      </v-card-text>
      <v-divider></v-divider>
      <v-list-item append-icon="mdi-chevron-right" lines="two" :subtitle="$t('projectOverview.open')" link
                   @click="() => openProject(activeProjectByGroup[group.name].id)"></v-list-item>
    </v-card>

    <v-card width="300px" height="252px" style="float: left; margin: 16px" class="d-flex flex-column">

      <v-card-title>
        <div style="text-align: center; margin-top: 25px">
          <v-icon icon="mdi-plus" size="x-large">
          </v-icon>
        </div>
        <div style="text-align: center;">
          {{ $t('projectOverview.newProject') }}
        </div>
      </v-card-title>
      <v-spacer></v-spacer>
      <v-card-actions>
        <v-btn color="primary" :text="$t('projectOverview.create')" block @click="handleOpenNewProjectDialog"></v-btn>
      </v-card-actions>
    </v-card>
    <v-dialog v-model="projectDialog" persistent width="600">
      <v-card>
        <v-card-title>
          <span class="text-h5">{{ $t('projectOverview.createProject') }}</span>
        </v-card-title>
        <v-card-text>
          <v-container>
            <v-row>
              <v-col cols="12" sm="12" md="12">
                <v-text-field label="Name" v-model="newProjectName"
                              :rules="[() => !!newProjectName || $t('projectOverview.projectNameRequired')]"
                              ref="newProjectNameInput"></v-text-field>
              </v-col>
            </v-row>
            <v-row>
              <v-col cols="12" sm="12" md="12">
                <v-text-field label="Version" v-model="newVersionName" placeholder="1.0"
                              :rules="versionRules" ref="newVersionNameInput"></v-text-field>
              </v-col>
            </v-row>
          </v-container>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="blue-darken-1" variant="text" @click="closeNewProjectOrVersionDialog">
            {{ $t('general.cancel') }}
          </v-btn>
          <v-btn color="blue-darken-1" variant="text" @click="createProject()">
            {{ $t('general.save') }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
    <v-dialog v-model="showNewVersionDialog" persistent width="600">
      <v-card>
        <v-card-title class="pt-4 pb-0 px-5">
          {{ $t('projectOverview.newVersionFor') }} {{ newProjectName }}
        </v-card-title>
        <v-card-text>
          <v-container>
            <v-row>
              <v-col cols="12" sm="12" md="12">
                <v-text-field ref="newVersionInput" :label="$t('projectOverview.newVersion')" v-model="newVersionName"
                              :rules="versionRules"></v-text-field>
              </v-col>
            </v-row>
          </v-container>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="blue-darken-1" variant="text" @click="closeNewProjectOrVersionDialog">
            {{ $t('general.cancel') }}
          </v-btn>
          <v-btn color="blue-darken-1" variant="text" @click="createProject()">
            {{ $t('general.save') }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>
<style scoped>
.active-card {
  box-shadow: 0 0 10px 3px rgba(24, 103, 192, 0.5);
}

.active-text {
  padding: 8px 16px;
  color: #1867c0;
  font-weight: 500;
  font-size: 0.875rem;
}

.text-dots {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
<script lang="ts">
import { defineComponent } from 'vue'
import axios from 'axios';
import { SelectedDialog, useAppStore, UserData } from "@/store/app";
import { VTextField } from "vuetify/components";
import { authHeader } from "@/components/Authentication/authHeader";

export interface Project {
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

export interface ActiveProjectByGroup {
  [key: string]: Project
}

export default defineComponent({
  data: () => {
    const store = useAppStore();
    return {
      store: store,
      projects: [] as Project[],
      projectDialog: false as boolean,
      showNewVersionDialog: false as boolean,
      newProjectName: "" as string,
      activeProjectByGroup: {} as ActiveProjectByGroup,
      newVersionName: "" as string,
      newVersionInitialProject: {} as Project,
      showLoggedInBanner: false as boolean,
      webVersion: (import.meta.env.VITE_DESKTOP_OR_WEB != 'desktop') as boolean,
      user: store.getUser() as UserData
    }
  },

  computed: {
    isUserLoggedIn() {
      return this.store.getUser() != null;
    },
    versionRules() {
      return [
        (): boolean | string => !this.versionNameExists || this.$t('projectOverview.versionNameExists'),
        (): boolean | string => !!this.newVersionName || this.$t('projectOverview.versionNameRequired'),
      ]
    },
    versionNameExists() {
      return !!this.projects.find(project => project.version === this.newVersionName && project.name === this.newProjectName);
    },
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
        this.activeProjectByGroup[group.name] = persistedActiveProject ?? group.projects[0];
      }

      return projectGroups;
    }
  },

  watch: {
    isUserLoggedIn(newValue) {
      if (!newValue) {
        window.location.reload();
      }
    }
  },

  mounted: function () {
    const currentState = window.history.state || {};

    this.showLoggedInBanner = currentState.showLoggedInBanner;

    const updatedState = { ...currentState, showLoggedInBanner: false };
    window.history.replaceState(updatedState, document.title);

    this.fetchProjects();
  },
  methods: {
    openNewVersionDialog(projectGroup: ProjectGroup) {
      this.newVersionName = "";
      this.newProjectName = projectGroup.name;
      this.showNewVersionDialog = true;
      this.newVersionInitialProject = this.activeProjectByGroup[projectGroup.name];
    },
    handleOpenNewProjectDialog() {
      this.openNewProjectDialog();
    },
    openNewProjectDialog() {
      this.newProjectName = "";
      this.newVersionName = "";
      this.projectDialog = true;
    },
    setActiveProject(groupName: string, projectId: number) {
      this.activeProjectByGroup[groupName] = this.projects.find(project => project.id === projectId)!;
      this.store.setActiveProjectForGroup(groupName, this.activeProjectByGroup[groupName]);
    },
    formatDate(dateString: string) {
      const locales = useAppStore().getSelectedLanguage() === 'de' ? 'de-DE' : 'en-US';
      return new Date(dateString).toLocaleString(locales);
    },
    async fetchProjects() {
      if (this.webVersion && !this.isUserLoggedIn) {
        return;
      }

      if (this.webVersion && this.isUserLoggedIn) {
        await axios.get(`/api/project/${this.user.id}`, { headers: authHeader() })
          .then((result: { data: Project[] }) => {
            const sortProjectsByActiveFirst = (project: Project): number => {
              return project.id == this.store.selectedProjectId ? -1 : 0;
            };
            this.projects = result.data.sort(sortProjectsByActiveFirst);
          })
          .catch(error => {
            console.error(error);
            this.projects = [];
          });
        return;
      }

      axios.get("/api/project", { headers: authHeader() })
        .then((result: { data: Project[] }) => {
          const sortProjectsByActiveFirst = (project: Project): number => {
            return project.id == this.store.selectedProjectId ? -1 : 0;
          };
          this.projects = result.data.sort(sortProjectsByActiveFirst);
        })
        .catch(error => {
          console.error(error);
        });
    },
    async createProject() {
      const newProjectNameInput = this.$refs.newProjectNameInput as VTextField;
      const newVersionNameInput = this.$refs.newVersionNameInput as VTextField;
      const newVersionInput = this.$refs.newVersionInput as VTextField;

      let errors: string[] = [];
      if (this.showNewVersionDialog) {
        errors = errors.concat(await newVersionInput.validate());
      } else {
        errors = errors.concat(await newProjectNameInput.validate());
        errors = errors.concat(await newVersionNameInput.validate());
      }
      if (errors.length > 0) {
        return;
      }

      const projectName = this.newProjectName;
      const projectVersion = this.newVersionName;

      let formData = new FormData();
      formData.append("name", projectName);
      formData.append("version", projectVersion);

      await axios.post(`/api/project${this.user?.id ? ("/" + this.user.id) : ""}`, formData, { headers: authHeader() })
        .then(result => {
          this.projectDialog = false;
          this.showNewVersionDialog = false;
          this.setActiveProject(result.data.name, result.data.id);
          this.projects.push(result.data);
        });
    },
    openProject(id: number) {
      this.store.setSelectedProjectId(id);
      this.$router.push("/ProcessList")
    },
    closeNewProjectOrVersionDialog() {
      this.projectDialog = false;
      this.showNewVersionDialog = false;
    }
  }
});
</script>
