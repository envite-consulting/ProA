<template>
  <v-container fluid style="margin: auto">
    <v-banner
      v-if="showLoggedInBanner && webVersion && isUserLoggedIn"
      color="success"
      icon="mdi-check"
      lines="one"
      :text="$t('projectOverview.welcomeBack') + user.firstName + '!'"
      :stacked="false"
      sticky
    >
      <template v-slot:actions>
        <v-btn icon @click="showLoggedInBanner = false">
          <v-icon>mdi-close</v-icon>
        </v-btn>
      </template>
    </v-banner>

    <v-snackbar
      v-model="store.snackbar.visible"
      :color="store.snackbar.color"
      :timeout="store.snackbar.timeout"
      centered
    >
      <v-icon left large class="snackbar-icon">
        {{ store.snackbar.icon }}
      </v-icon>
      <span class="snackbar-text">{{ store.snackbar.message }}</span>
    </v-snackbar>

    <v-card
      v-for="(group, index) in projectGroups"
      :key="index"
      width="310px"
      height="310px"
      style="float: left; margin: 16px"
      :class="{
        'active-card':
          activeProjectByGroup[group.name]?.id === store.selectedProjectId
      }"
    >
      <div class="d-flex flex-row justify-space-between align-center">
        <v-card-title>
          {{ group.name }}
        </v-card-title>

        <p
          v-if="
            activeProjectByGroup[group.name]?.id === store.selectedProjectId
          "
          class="active-text"
        >
          {{ $t("projectOverview.active") }}
        </p>
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
        <v-btn
          variant="plain"
          class="pa-0"
          @click="openNewVersionDialog(group)"
        >
          <v-icon icon="mdi-plus" size="large"></v-icon>
          {{ $t("projectOverview.newVersion") }}
        </v-btn>
        <div class="text-dots">
          {{ $t("general.createdOn") }}:
          {{ formatDate(activeProjectByGroup[group.name].createdAt) }}
        </div>
        <div class="text-dots">
          {{ $t("general.lastModifiedOn") }}:
          {{ formatDate(activeProjectByGroup[group.name].modifiedAt) }}
        </div>
      </v-card-text>
      <v-divider></v-divider>
      <v-list-item
        append-icon="mdi-chevron-right"
        lines="two"
        :subtitle="$t('projectOverview.open')"
        link
        @click="() => openProject(activeProjectByGroup[group.name].id)"
      ></v-list-item>
      <v-card-actions class="justify-end">
        <v-btn
          icon
          color="grey"
          @click="openDeleteDialog(activeProjectByGroup[group.name])"
        >
          <v-icon>mdi-delete</v-icon>
        </v-btn>
      </v-card-actions>
    </v-card>

    <v-dialog v-model="confirmDeleteDialog" max-width="400">
      <v-card
        prepend-icon="mdi-delete"
        :title="$t('projectOverview.confirmDeletion')"
      >
        <template v-slot:text>
          {{ $t("projectOverview.confirmDeletionText1")
          }}<strong>{{ projectToBeDeleted?.name }}</strong
          >{{ $t("projectOverview.confirmDeletionText2")
          }}<strong>{{ projectToBeDeleted?.version }}</strong
          >{{ $t("projectOverview.confirmDeletionText3") }}
        </template>
        <template v-slot:actions>
          <div class="ms-auto">
            <v-btn
              :text="$t('general.cancel')"
              @click="confirmDeleteDialog = false"
            ></v-btn>
            <v-btn
              :text="$t('projectOverview.confirm')"
              @click="confirmDelete"
            ></v-btn>
          </div>
        </template>
      </v-card>
    </v-dialog>

    <v-card
      width="310px"
      height="310px"
      style="float: left; margin: 16px"
      class="d-flex flex-column"
    >
      <v-card-title>
        <div style="text-align: center; margin-top: 25px">
          <v-icon icon="mdi-plus" size="x-large"> </v-icon>
        </div>
        <div style="text-align: center">
          {{ $t("projectOverview.newProject") }}
        </div>
      </v-card-title>
      <v-spacer></v-spacer>
      <v-card-actions>
        <v-btn
          color="primary"
          :text="$t('projectOverview.create')"
          block
          @click="handleOpenNewProjectDialog"
        ></v-btn>
      </v-card-actions>
    </v-card>
    <v-dialog v-model="projectDialog" persistent width="600">
      <v-card>
        <v-card-title>
          <span class="text-h5">{{ $t("projectOverview.createProject") }}</span>
        </v-card-title>
        <v-card-text>
          <v-container>
            <v-row>
              <v-col cols="12" sm="12" md="12">
                <v-text-field
                  label="Name"
                  v-model="newProjectName"
                  :rules="[
                    () =>
                      !!newProjectName ||
                      $t('projectOverview.projectNameRequired')
                  ]"
                  ref="newProjectNameInput"
                ></v-text-field>
              </v-col>
            </v-row>
            <v-row>
              <v-col cols="12" sm="12" md="12">
                <v-text-field
                  label="Version"
                  v-model="newVersionName"
                  placeholder="1.0"
                  :rules="versionRules"
                  ref="newVersionNameInput"
                ></v-text-field>
              </v-col>
            </v-row>
          </v-container>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn
            color="blue-darken-1"
            variant="text"
            @click="closeNewProjectOrVersionDialog"
          >
            {{ $t("general.cancel") }}
          </v-btn>
          <v-btn color="blue-darken-1" variant="text" @click="createProject()">
            {{ $t("general.save") }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
    <v-dialog v-model="showNewVersionDialog" persistent width="600">
      <v-card>
        <v-card-title class="pt-4 pb-0 px-5">
          {{ $t("projectOverview.newVersionFor") }} {{ newProjectName }}
        </v-card-title>
        <v-card-text>
          <v-container>
            <v-row>
              <v-col cols="12" sm="12" md="12">
                <v-text-field
                  ref="newVersionInput"
                  :label="$t('projectOverview.newVersion')"
                  v-model="newVersionName"
                  :rules="versionRules"
                ></v-text-field>
              </v-col>
            </v-row>
          </v-container>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn
            color="blue-darken-1"
            variant="text"
            @click="closeNewProjectOrVersionDialog"
          >
            {{ $t("general.cancel") }}
          </v-btn>
          <v-btn color="blue-darken-1" variant="text" @click="createProject()">
            {{ $t("general.save") }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>
<style scoped>
@import "@/styles/global.css";

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
import { defineComponent } from "vue";
import axios from "axios";
import { SnackbarType } from "@/utils/snackbar";
import { useAppStore } from "@/store/app";
import { VTextField } from "vuetify/components";
import { authHeader } from "@/components/Authentication/authHeader";
import getUser from "@/components/userService";

export interface Project {
  id: number;
  name: string;
  version: string;
  createdAt: string;
  modifiedAt: string;
}

interface ProjectGroup {
  name: string;
  projects: Project[];
}

export interface ActiveProjectByGroup {
  [key: string]: Project;
}

export interface UserData {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  role: string;
  createdAt: string;
  modifiedAt: string;
}

export default defineComponent({
  data: () => {
    const store = useAppStore();
    return {
      store: store,
      projects: [] as Project[],
      projectDialog: false as boolean,
      confirmDeleteDialog: false,
      projectToBeDeleted: null as Project | null,
      showNewVersionDialog: false as boolean,
      newProjectName: "" as string,
      activeProjectByGroup: {} as ActiveProjectByGroup,
      newVersionName: "" as string,
      newVersionInitialProject: {} as Project,
      showLoggedInBanner: false as boolean,
      webVersion: (import.meta.env.VITE_APP_MODE === "web") as boolean,
      user: {} as UserData
    };
  },

  computed: {
    isUserLoggedIn() {
      return this.store.getUserToken() != null;
    },
    versionRules() {
      return [
        (): boolean | string =>
          !this.versionNameExists ||
          this.$t("projectOverview.versionNameExists"),
        (): boolean | string =>
          !!this.newVersionName ||
          this.$t("projectOverview.versionNameRequired")
      ];
    },
    versionNameExists() {
      return !!this.projects.find(
        (project) =>
          project.version === this.newVersionName &&
          project.name === this.newProjectName
      );
    },
    projectGroups(): ProjectGroup[] {
      const groupedProjects: { [key: string]: Project[] } = {};
      for (const project of this.projects) {
        if (!groupedProjects[project.name]) {
          groupedProjects[project.name] = [];
        }
        groupedProjects[project.name].push(project);
      }

      return Object.keys(groupedProjects).map((name) => {
        return {
          name: name,
          projects: groupedProjects[name].sort((a, b) => {
            return (
              new Date(b.modifiedAt).getTime() -
              new Date(a.modifiedAt).getTime()
            );
          })
        };
      });
    }
  },

  watch: {
    isUserLoggedIn(newValue) {
      if (!newValue) {
        window.location.reload();
      }
    },
    projectGroups(newGroups) {
      for (const group of newGroups) {
        const persistedActiveProject = this.store.getActiveProjectForGroup(
          group.name
        );
        this.activeProjectByGroup[group.name] =
          persistedActiveProject ?? group.projects[0];
      }
    }
  },

  mounted: async function () {
    if (this.store.getUserToken() != null) this.user = await getUser();

    const currentState = window.history.state || {};

    this.showLoggedInBanner = currentState.showLoggedInBanner;

    const updatedState = { ...currentState, showLoggedInBanner: false };
    window.history.replaceState(updatedState, document.title);

    await this.fetchProjects();
  },
  methods: {
    openNewVersionDialog(projectGroup: ProjectGroup) {
      this.newVersionName = "";
      this.newProjectName = projectGroup.name;
      this.showNewVersionDialog = true;
      this.newVersionInitialProject =
        this.activeProjectByGroup[projectGroup.name];
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
      this.activeProjectByGroup[groupName] = this.projects.find(
        (project) => project.id === projectId
      )!;
      this.store.setActiveProjectForGroup(
        groupName,
        this.activeProjectByGroup[groupName]
      );
    },
    formatDate(dateString: string) {
      const locales =
        useAppStore().getSelectedLanguage() === "de" ? "de-DE" : "en-US";
      return new Date(dateString).toLocaleString(locales);
    },
    async fetchProjects() {
      if (this.webVersion && !this.isUserLoggedIn) {
        return;
      }

      if (this.webVersion && this.isUserLoggedIn) {
        try {
          const result = await axios.get("/api/project", {
            headers: authHeader()
          });
          this.projects = result.data.sort((project: Project) => {
            return project.id === this.store.selectedProjectId ? -1 : 0;
          });
        } catch (error) {
          this.projects = [];
        }
        return;
      }

      try {
        const result = await axios.get("/api/project", {
          headers: authHeader()
        });
        this.projects = result.data.sort((project: Project) => {
          return project.id === this.store.selectedProjectId ? -1 : 0;
        });
      } catch (error) {
        this.projects = [];
      }
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

      try {
        const result = await axios.post("api/project", formData, {
          headers: authHeader()
        });

        this.projectDialog = false;
        this.showNewVersionDialog = false;
        this.setActiveProject(result.data.name, result.data.id);
        this.projects.push(result.data);

        this.store.showSnackbar(
          this.$t("projectOverview.projectSuccessfullyCreated"),
          SnackbarType.SUCCESS
        );
      } catch (error) {
        this.store.showSnackbar(
          this.$t("projectOverview.errorMessage"),
          SnackbarType.ERROR
        );
      }
    },
    openDeleteDialog(project: Project) {
      this.projectToBeDeleted = project;
      this.confirmDeleteDialog = true;
    },
    async confirmDelete() {
      if (this.projectToBeDeleted) {
        try {
          await this.deleteProject(this.projectToBeDeleted.id);

          this.projects = this.projects.filter(
            (project) => project.id !== this.projectToBeDeleted?.id
          );
          this.updateProjects();
          this.handleSelectedProjectAfterDelete();
          this.confirmDeleteDialog = false;
          this.projectToBeDeleted = null;

          this.projects = this.projects.sort((project: Project) => {
            return project.id === this.store.selectedProjectId ? -1 : 0;
          });

          this.store.showSnackbar(
            this.$t("projectOverview.projectSuccessfullyDeleted"),
            SnackbarType.SUCCESS
          );
        } catch (error) {
          this.store.showSnackbar(
            this.$t("projectOverview.errorMessage"),
            SnackbarType.ERROR
          );
        }
      }
    },
    async deleteProject(projectId: number) {
      await axios.delete(`/api/project/${projectId}`, {
        headers: authHeader()
      });
    },
    openProject(id: number) {
      this.store.setSelectedProjectId(id);
      this.$router.push("/ProcessList");
    },
    closeNewProjectOrVersionDialog() {
      this.projectDialog = false;
      this.showNewVersionDialog = false;
    },
    updateProjects() {
      this.projectGroups.forEach((group) => {
        const persistedActiveProject = this.store.getActiveProjectForGroup(
          group.name
        );

        const validProject = group.projects.find(
          (project) => project.id === persistedActiveProject?.id
        );

        if (validProject) {
          this.activeProjectByGroup[group.name] = validProject;
        } else {
          this.activeProjectByGroup[group.name] = group.projects[0] || null;
        }

        if (this.activeProjectByGroup[group.name]) {
          this.setActiveProject(
            group.name,
            this.activeProjectByGroup[group.name].id
          );
        }
      });
    },
    handleSelectedProjectAfterDelete() {
      if (this.projectToBeDeleted) {
        const groupName = this.projectToBeDeleted.name;
        const remainingProjects = this.projects
          .filter((project) => project.name === groupName)
          .sort(
            (a, b) =>
              new Date(b.modifiedAt).getTime() -
              new Date(a.modifiedAt).getTime()
          );
        const nextActiveProject =
          remainingProjects.length > 0 ? remainingProjects[0] : null;
        const currentSelectedProjectId = this.store.getSelectedProjectId();
        const belongsToGroup =
          this.projectToBeDeleted &&
          this.projectToBeDeleted.id === currentSelectedProjectId;

        if (currentSelectedProjectId && nextActiveProject && belongsToGroup) {
          this.setActiveProject(groupName, nextActiveProject.id);
          this.store.setSelectedProjectId(nextActiveProject.id);
        } else if (currentSelectedProjectId && !belongsToGroup) {
          this.store.setSelectedProjectId(currentSelectedProjectId);
        } else {
          this.store.setSelectedProjectId(null);
        }
      }
    }
  }
});
</script>
