<template>
  <v-card class="pa-5">
    <v-card-title class="px-0 pt-0">
      <div class="d-flex align-center">
        <span>{{ $t("general.myProfile") }}</span>
        <v-btn variant="text" icon class="ms-auto" @click="closeDialog">
          <v-icon icon="mdi-close"></v-icon>
        </v-btn>
      </div>
    </v-card-title>

    <v-divider />

    <v-alert
      v-if="message.message !== ''"
      :type="message.type"
      closable
      icon="mdi-check-circle-outline"
      :text="message.message"
      @click:close="removeMessage"
    ></v-alert>
    <v-card-title class="px-0">
      <div class="d-flex align-center">
        <span>{{ user.firstName }} {{ user.lastName }}</span>
        <v-btn
          variant="text"
          class="ms-auto"
          color="primary"
          @click="resetMessageAndOpenDialog(SelectedDialog.EDIT_PROFILE)"
        >
          {{ $t("authentication.edit") }}
        </v-btn>
      </div>
    </v-card-title>

    <v-card-text class="pa-0 mb-3">
      <div class="d-flex align-center mb-3">
        <v-icon icon="mdi-email" class="me-2"></v-icon>
        <div class="d-flex flex-column">
          <span class="text-caption">{{ $t("authentication.email") }}</span>
          <span>{{ user.email }}</span>
        </div>
      </div>
      <div class="d-flex align-center justify-space-between">
        <div class="d-flex align-center">
          <v-icon icon="mdi-key" class="me-2"></v-icon>
          <div class="d-flex flex-column">
            <span class="text-caption">{{
              $t("authentication.password")
            }}</span>
            <span>************</span>
          </div>
        </div>
        <div class="d-flex align-center justify-end">
          <v-btn
            variant="text"
            color="primary"
            @click="resetMessageAndOpenDialog(SelectedDialog.CHANGE_PW)"
          >
            {{ $t("authentication.changePassword") }}
          </v-btn>
        </div>
      </div>
    </v-card-text>

    <v-card-subtitle class="px-0 mb-1">
      {{ $t("general.createdOn") }}: {{ getLocaleDate(user.createdAt) }}
    </v-card-subtitle>
    <v-card-subtitle class="px-0 mb-1">
      {{ $t("general.lastModifiedOn") }}: {{ getLocaleDate(user.modifiedAt) }}
    </v-card-subtitle>

    <div v-if="projects.length > 0">
      <v-divider class="mt-2" />
      <v-card-title class="px-0">
        {{ $t("authentication.projects") }}
      </v-card-title>
      <v-table density="compact">
        <thead>
          <tr>
            <th class="text-left">
              {{ $t("authentication.name") }}
            </th>
            <th class="text-left">
              {{ $t("authentication.version") }}
            </th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="project in projects" :key="'project-' + project.id">
            <td>
              <v-tooltip location="left">
                <template v-slot:activator="{ props }">
                  <v-btn
                    variant="plain"
                    color="primary"
                    v-bind="props"
                    class="text-none table-btn"
                    prepend-icon="mdi-folder"
                    @click="openProject(project.id)"
                  >
                    {{ project.name }}
                  </v-btn>
                </template>
                {{ $t("authentication.openProject") }}
              </v-tooltip>
            </td>
            <td class="text-no-wrap">
              {{ store.getActiveVersionForProject(project.id).name }}
            </td>
          </tr>
        </tbody>
      </v-table>
    </div>
  </v-card>
</template>

<style scoped>
@import "authentication.css";
</style>

<script lang="ts">
import { defineComponent } from "vue";
import { SelectedDialog, useAppStore } from "@/store/app";
import { Project, UserData } from "@/components/Home/ProjectOverview.vue";
import axios from "axios";
import { Message } from "@/components/Authentication/AuthenticationDialog.vue";
import { authHeader } from "@/components/Authentication/authHeader";
import getUser from "@/components/userService";

export default defineComponent({
  name: "ProfileDialog",

  props: {
    message: {
      type: Object,
      required: true
    }
  },

  data() {
    return {
      store: useAppStore(),
      projects: [] as Project[],
      SelectedDialog: SelectedDialog,
      user: {} as UserData
    };
  },

  methods: {
    resetMessageAndOpenDialog(selected: SelectedDialog) {
      this.$emit("showMessage", { message: "", type: "error" } as Message);
      this.openDialog(selected);
    },
    closeDialog() {
      this.store.setSelectedDialog(SelectedDialog.NONE);
    },
    openDialog(dialog: SelectedDialog) {
      this.store.setSelectedDialog(dialog);
    },
    getLocaleDate(date: string): string {
      const locales =
        this.store.getSelectedLanguage() === "de" ? "de-DE" : "en-US";
      return new Date(date).toLocaleDateString(locales);
    },
    openProject(id: number) {
      this.closeDialog();
      this.store.setSelectedProjectId(id);
      this.$router.push("/ProcessList").then(() => window.location.reload());
    },
    fetchProjects() {
      axios
        .get("/api/project", { headers: authHeader() })
        .then((result: { data: Project[] }) => {
          const sortProjectsByActiveFirstThenAlphabetically = (
            project1: Project,
            project2: Project
          ): number => {
            if (project1.id === this.store.selectedProjectId) return -1;
            if (project2.id === this.store.selectedProjectId) return 1;

            return project1.name.localeCompare(project2.name);
          };

          this.projects = result.data.sort(
            sortProjectsByActiveFirstThenAlphabetically
          );
        });
    },
    removeMessage() {
      this.$emit("removeMessage");
    }
  },

  async mounted() {
    this.fetchProjects();
    if (this.store.getUserToken() != null) this.user = await getUser();
  }
});
</script>
