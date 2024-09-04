<template>
  <v-toolbar>
    <v-toolbar-title>
      <div class="d-flex align-center">
        <span>{{ selectedProjectName }}</span>
        <span class="text-body-2 text-grey-darken-1 ms-4">VERSION {{ selectedVersionName }}</span>
      </div>
    </v-toolbar-title>
  </v-toolbar>
  <v-list lines="two" class="pa-6">
    <div v-if="importedProcessModels.length > 0">
      <v-list-item>
        <v-checkbox class="d-inline-block pe-3" hide-details @change="toggleSelectAll" v-model="selectAll">
          <template v-slot:label>
            <span class="ms-3">{{ $t('c8Import.selectAll') }}</span>
          </template>
        </v-checkbox>
      </v-list-item>
      <v-divider></v-divider>
    </div>
    <template v-for="(model, index) in processModels" :key="'process-'+model.id">
      <v-list-item>
        <v-list-item-title>{{ model.name }}</v-list-item-title>
        <v-list-item-subtitle>
          {{ getLocaleDate(model.created) }} - {{ model.updatedBy.email }}
        </v-list-item-subtitle>
        <template v-slot:prepend="{ isActive }">
          <v-list-item-action start>
            <v-checkbox v-model="selectedProcessModels" :value="model"></v-checkbox>
          </v-list-item-action>
        </template>
      </v-list-item>
      <v-divider v-if="index < processModels.length - 1" :key="`${index}-divider`"></v-divider>
    </template>
  </v-list>
  <v-dialog v-model="camundaCloudDialog" persistent width="600">
    <v-card>
      <v-card-title>
        <span class="text-h5">{{ $t('c8Import.importFromC8') }}</span>
      </v-card-title>
      <v-card-text>
        <v-container>
          <v-row>
            <v-col v-if="!!token">
              <v-icon icon="mdi-check-circle" color="green"></v-icon>
              {{ $t('c8Import.camundaConnectionSuccessMessage') }}
            </v-col>
            <v-col v-if="!token" cols="12" sm="12" md="12">
              <v-text-field v-model="settings.modelerClientId" class="text-field__styled" dense color="#26376B"
                            :placeholder="$t('general.clientId')"></v-text-field>
            </v-col>
            <v-col v-if="!token" cols="12" sm="12" md="12">
              <v-text-field v-model="settings.modelerClientSecret" class="text-field__styled" dense color="#26376B"
                            :placeholder="$t('general.clientSecret')"
                            :type="showOperateClientSecret ? 'text' : 'password'"
                            :append-inner-icon="showOperateClientSecret ? 'mdi-eye' : 'mdi-eye-off'"
                            @click:append-inner="showOperateClientSecret = !showOperateClientSecret"></v-text-field>
            </v-col>
            <v-col v-if="!token" cols="12" sm="12" md="12">
              <v-checkbox v-model="saveClientInformation" :label="$t('c8Import.saveInformationQuestion')"
                          color="primary"
                          hide-details></v-checkbox>
            </v-col>
            <v-col v-if="tokenError">
              {{ $t('c8Import.errorMessage') }}
            </v-col>
            <v-col v-if="!token">
              <v-btn color="blue-darken-1" @click="fetchToken">
                {{ $t('c8Import.connect') }}
              </v-btn>
            </v-col>
            <v-col cols="12" sm="12" md="12">
              <v-text-field :disabled="!token" v-model="creatorEmail" class="text-field__styled"
                            dense color="#26376B" :placeholder="$t('c8Import.creatorEmail')"
                            :hint="$t('c8Import.creatorEmailHint')">
              </v-text-field>
            </v-col>
            <v-col>
              <v-btn :disabled="!token" color="blue-darken-1" @click="fetchProcessModels">
                {{ $t('c8Import.retrieveProcessModels') }}
              </v-btn>
            </v-col>
          </v-row>
        </v-container>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn color="blue-darken-1" variant="text" @click="camundaCloudDialog = false">
          {{ $t('general.cancel') }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
  <v-dialog v-model="loadingDialog" max-width="320" persistent>
    <v-list class="py-2" color="primary" elevation="12" rounded="lg">
      <v-list-item :title="$t('c8Import.loading')">
        <template v-slot:append>
          <v-progress-circular color="primary" indeterminate="disable-shrink" size="16" width="2"></v-progress-circular>
        </template>
      </v-list-item>
    </v-list>
  </v-dialog>
  <div class="ma-4" style="position: fixed; z-index: 1; right: 8px; bottom: 8px">
    <v-fab-transition>
      <v-btn class="mt-auto pointer-events-initial" color="primary" elevation="8" icon="mdi-cloud-search"
             @click="openDialog" size="large"/>
    </v-fab-transition>
  </div>
  <div class="ma-4 d-flex align-center justify-center"
       style="position: fixed; bottom: 8px; right: 8px; left: 8px; height: 56px"
       v-if="selectedProcessModels.length > 0">
    <v-btn prepend-icon="mdi-import" @click="importProcessModels">
      {{ $t('c8Import.importProcessModels') }}
    </v-btn>
  </div>
  <div class="ma-4" v-if="importedProcessModels.length > 0"
       :style="{ position: 'fixed', right: '8px', top: stickyButtonTop + 'px' }">
    <v-menu :close-on-content-click="false">
      <template v-slot:activator="{ props }">
        <v-badge v-if="emailSelections.filter(emailSelection => emailSelection.selected).length > 0"
                 :content="emailSelections.filter(emailSelection => emailSelection.selected).length">
          <v-fab-transition>
            <v-btn class="mt-auto pointer-events-initial" elevation="8" icon="mdi-filter-outline"
                   v-bind="props" size="large"/>
          </v-fab-transition>
        </v-badge>
        <v-fab-transition v-else>
          <v-btn class="mt-auto pointer-events-initial" elevation="8" icon="mdi-filter-outline"
                 v-bind="props" size="large"/>
        </v-fab-transition>
      </template>
      <v-list density="compact" class="pa-2">
        <v-list-subheader>{{ $t('c8Import.creatorEmail') }}</v-list-subheader>
        <v-list-item
          v-for="(emailSelection, index) in emailSelections"
          :key="'imported-email-' + index"
        >
          <v-checkbox v-model="emailSelection.selected" hide-details density="compact"
                      @update:model-value="filterSelectedModels">
            <template v-slot:label>
              <span class="ms-1">{{ emailSelection.email }}</span>
            </template>
          </v-checkbox>
        </v-list-item>
      </v-list>
    </v-menu>
  </div>
</template>
<style scoped></style>
<script lang="ts">
import { defineComponent } from 'vue'
import axios from 'axios';
import { useAppStore } from "@/store/app";
import getProject from "../projectService";
import { Settings } from "../SettingsDrawer.vue"
import { authHeader } from "@/components/Authentication/authHeader";

declare interface ProcessModel {
  id: string,
  name: string
  created: string
  updatedBy: {
    email: string
  }
}

interface EmailSelection {
  email: string,
  selected: boolean
}

const minStickyOffset = 72;
const maxStickyOffset = 136;

export default defineComponent({
  data: () => ({
    store: useAppStore(),
    showOperateClientSecret: false as boolean,
    camundaCloudDialog: false as boolean,
    loadingDialog: false as boolean,
    settings: {} as Settings,
    saveClientInformation: true as boolean,
    creatorEmail: null as string | null,
    clientId: "" as string,
    clientSecret: "" as string,
    importedProcessModels: [] as ProcessModel[],
    processModels: [] as ProcessModel[],
    selectedProcessModels: [] as ProcessModel[],
    tokenError: false as boolean,
    token: null as string | null,
    selectedProjectId: null as number | null,
    selectedProjectName: '' as string,
    selectedVersionName: '' as string,
    selectAll: false as boolean,
    emailSelections: [] as EmailSelection[],
    stickyButtonTop: Math.max(minStickyOffset, maxStickyOffset - scrollY),
  }),

  computed: {
    isUserLoggedIn(): boolean {
      return this.store.getUser() != null;
    }
  },

  watch: {
    isUserLoggedIn(newValue) {
      if (!newValue) {
        this.$router.push("/");
      }
    }
  },

  mounted: function () {
    this.selectedProjectId = this.store.getSelectedProjectId();
    if (!this.selectedProjectId) {
      this.$router.push("/");
      return;
    }
    getProject(this.selectedProjectId).then(result => {
      this.selectedProjectName = result.data.name;
      this.selectedVersionName = result.data.version;
    });
    window.addEventListener('scroll', this.updateStickyButton);
    this.fetchSettings();
  },
  beforeUnmount() {
    window.removeEventListener('scroll', this.updateStickyButton);
  },
  methods: {
    async openDialog() {
      this.showOperateClientSecret = false;
      await this.fetchSettings();
      this.camundaCloudDialog = true;
    },
    fetchToken() {
      this.loadingDialog = true;
      axios.post("/api/camunda-cloud/token", {
        "client_id": this.settings.modelerClientId,
        "client_secret": this.settings.modelerClientSecret,
      }, { headers: authHeader() }).then(result => {
        this.token = result.data;
        this.tokenError = false;
        this.loadingDialog = false;
      }).catch(error => {
        this.tokenError = true;
        this.loadingDialog = false;
      })
    },
    async fetchProcessModels() {
      this.loadingDialog = true;
      axios.post("/api/camunda-cloud", {
        "token": this.token,
        "email": this.isBlank(this.creatorEmail) ? null : this.creatorEmail,
        "regionId": null,
        "clusterId": null
      }, { headers: authHeader() }).then(async result => {
        if (this.saveClientInformation) {
          await this.saveSettings();
        }
        const processModels: ProcessModel[] = result.data.items;
        this.processModels = processModels;
        this.importedProcessModels = processModels;
        this.emailSelections = [...new Set(processModels.map(processModel => processModel.updatedBy.email))]
          .sort()
          .map(email => ({
            email,
            selected: false
          }));
        this.camundaCloudDialog = false;
        this.loadingDialog = false;
      }).catch(error => {
        console.log(error);
        this.tokenError = true;
        this.token = null;
        this.loadingDialog = false;
      })
    },
    importProcessModels() {
      this.loadingDialog = true;
      const selectedProcessModelIds: string[] = this.selectedProcessModels.map(model => {
        return model.id;
      });

      axios.post("/api/camunda-cloud/project/" + this.selectedProjectId + "/import", {
        token: this.token,
        selectedProcessModelIds: selectedProcessModelIds,
      }, { headers: authHeader() }).then(() => {
        this.processModels = this.processModels.filter(model => !selectedProcessModelIds.includes(model.id));
        this.selectedProcessModels = [];
        this.loadingDialog = false;
        this.$router.push("/ProcessList");
      }).catch(error => {
        console.log(error);
        this.loadingDialog = false;
      });
    },
    async fetchSettings() {
      try {
        await axios.get("/api/settings", { headers: authHeader() }).then(result => {
          this.settings = result.data;
        });
      } catch (error) {
        this.settings = {} as Settings;
      }

      this.settings.modelerClientId = this.settings.modelerClientId || import.meta.env.VITE_MODELER_CLIENT_ID;
      this.settings.modelerClientSecret = this.settings.modelerClientSecret || import.meta.env.VITE_MODELER_CLIENT_SECRET;
    },
    async saveSettings() {
      const doSettingsExist = async () => {
        try {
          await axios.get("/api/settings", { headers: authHeader() });
          return true;
        } catch (error) {
          return false;
        }
      }

      if (await doSettingsExist()) {
        await axios.patch("/api/settings", this.settings, { headers: { ...authHeader(), 'Content-Type': 'application/json' } });
      } else {
        await axios.post("/api/settings", this.settings, { headers: { ...authHeader(), 'Content-Type': 'application/json' } });
      }
    },
    getLocaleDate(date: string): string {
      const locales = this.store.getSelectedLanguage() === 'de' ? 'de-DE' : 'en-US';
      return new Date(date).toLocaleString(locales);
    },
    toggleSelectAll() {
      if (this.selectAll) {
        this.selectedProcessModels = this.processModels;
      } else {
        this.selectedProcessModels = [];
      }
    },
    filterSelectedModels() {
      const selectedEmails = this.emailSelections
        .filter(emailSelection => emailSelection.selected)
        .map(emailSelection => emailSelection.email);
      if (selectedEmails.length == 0) {
        this.processModels = this.importedProcessModels;
        this.updateSelectAll();
        return;
      }
      this.processModels = this.importedProcessModels
        .filter(model => selectedEmails.includes(model.updatedBy.email));
      this.selectedProcessModels = this.selectedProcessModels
        .filter(model => selectedEmails.includes(model.updatedBy.email));
      this.updateSelectAll();
    },
    updateSelectAll() {
      this.selectAll = this.selectedProcessModels.length == this.processModels.length;
    },
    updateStickyButton() {
      this.stickyButtonTop = Math.max(minStickyOffset, maxStickyOffset - scrollY);
    },
    isBlank(s: string | null) {
      return !s || /^\s*$/.test(s);
    }
  }
})
</script>
