<template>
  <v-dialog v-model="showProjectDetailDialog" width="600">
    <v-card>
      <v-container>
        <v-card-title>{{ project.name }}</v-card-title>
        <v-divider />
        <v-card-text>
          <div class="card-section mt-3">
            <span class="text-body-1 font-weight-bold">
              {{ $t("projectOverview.owner") + ": " }}
            </span>
            <span class="text-body-1">
              {{
                project.owner?.id === userId
                  ? `(${$t("projectOverview.you")})`
                  : `${project.owner?.firstName} ${project.owner?.lastName}`
              }}
            </span>
          </div>

          <div class="card-section">
            <p class="text-body-1 font-weight-bold">
              {{ $t("projectOverview.contributors") + ": " }}
            </p>
            <p
              v-for="contributor in project.contributors"
              class="text-body-1 deletable"
            >
              {{ contributor.firstName + " " + contributor.lastName }}
            </p>
            <v-text-field
              class="mt-2"
              v-model="newContributorEmail"
              :label="$t('authentication.email')"
              density="compact"
              ref="newContributorEmailInput"
              :rules="emailRules"
              :error-messages="newContributorErrorMsg"
              @input="newContributorErrorMsg = ''"
              @focusout="resetValidation"
            >
              <template v-slot:append>
                <v-btn
                  append-icon="mdi-plus"
                  :text="$t('projectOverview.addNew')"
                  @click="addContributor"
                  variant="tonal"
                ></v-btn>
              </template>
            </v-text-field>
          </div>

          <div class="card-section">
            <p class="text-body-1 font-weight-bold">
              {{ $t("projectOverview.versions") + ": " }}
            </p>
            <p
              v-for="version in project.versions"
              class="text-body-1 deletable"
              @click="deleteVersion(project, version)"
            >
              {{ version.name }}
            </p>
          </div>

          <div class="card-section mb-3">
            <div class="mb-1">
              <span class="text-body-1 font-weight-bold">
                {{ $t("general.createdOn") + ": " }}
              </span>
              <span class="text-body-1">
                {{ formatDate(project.createdAt) }}
              </span>
            </div>
            <div>
              <span class="text-body-1 font-weight-bold">
                {{ $t("general.lastModifiedOn") + ": " }}
              </span>
              <span class="text-body-1">
                {{ formatDate(project.modifiedAt) }}
              </span>
            </div>
          </div>
        </v-card-text>
        <v-divider />
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="blue-darken-1" variant="text" @click="closeDialog">
            {{ $t("general.close") }}
          </v-btn>
        </v-card-actions>
      </v-container>
    </v-card>
  </v-dialog>
</template>

<style scoped>
.card-section {
  margin: 2rem 0;
}

.deletable:hover {
  cursor: pointer;
  text-decoration: line-through;
}
</style>

<script lang="ts">
import { defineComponent } from "vue";
import { Project, ProjectVersion } from "@/components/Home/ProjectOverview.vue";
import axios, { AxiosError } from "axios";
import { authHeader } from "@/components/Authentication/authHeader";
import { useAppStore } from "@/store/app";
import { VTextField } from "vuetify/components";
import { emailRules } from "@/components/Authentication/formValidation";

export default defineComponent({
  name: "ProjectDetailDialog",

  props: {
    showProjectDetailDialog: {
      type: Boolean,
      required: true
    },
    projectDetailId: {
      type: Number,
      required: true
    },
    userId: {
      type: Number
    },
    projectChangedFlag: {
      type: Boolean,
      required: true
    }
  },

  data: () => ({
    emailRules: emailRules,
    newContributorEmail: "" as string,
    newContributorErrorMsg: "" as string,
    project: {} as Project
  }),

  watch: {
    showProjectDetailDialog(newVal) {
      if (newVal) {
        this.fetchProject();
      }
    },
    projectChangedFlag(newVal) {
      if (newVal) {
        this.fetchProject();
        this.$emit("resetProjectChangedFlag");
      }
    }
  },

  methods: {
    closeDialog() {
      this.$emit("close");
    },
    async fetchProject() {
      try {
        const { data } = await axios.get<Project>(
          `/api/project/${this.projectDetailId}`,
          {
            headers: authHeader()
          }
        );
        this.project = data;
      } catch (error) {
        console.error(error);
      }
    },
    formatDate(dateString: string) {
      const locales =
        useAppStore().getSelectedLanguage() === "de" ? "de-DE" : "en-US";
      return new Date(dateString).toLocaleString(locales);
    },
    async addContributor() {
      const newContributorEmailInput = this.$refs
        .newContributorEmailInput as VTextField;

      const errors = await newContributorEmailInput.validate();
      if (errors.length > 0) {
        return;
      }

      let formData = new FormData();
      formData.append("email", this.newContributorEmail);

      try {
        await axios.post(
          `/api/project/${this.projectDetailId}/contributor`,
          formData,
          {
            headers: authHeader()
          }
        );
        this.newContributorEmail = "";
        this.resetValidation();
        await this.fetchProject();
      } catch (error) {
        const e = error as AxiosError;
        if (e.response?.status === 404) {
          this.newContributorErrorMsg = this.$t(
            "projectOverview.emailNotFound"
          );
          return;
        }
        this.newContributorErrorMsg = this.$t("projectOverview.errorMessage");
      }
    },
    resetValidation() {
      this.newContributorErrorMsg = "";
      const newContributorEmailInput = this.$refs
        .newContributorEmailInput as VTextField;
      newContributorEmailInput.resetValidation();
    },
    deleteVersion(project: Project, version: ProjectVersion) {
      this.$emit("deleteVersion", project, version);
    }
  }
});
</script>
