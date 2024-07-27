<template>
  <v-navigation-drawer
    v-model="appStore.areSettingsOpened"
    location="right"
    temporary
    width="400"
  >
    <div class="d-flex flex-column ma-3 ms-4">
      <div class="d-flex align-center">
        <p class="text-h6">{{ $t('general.settings') }}</p>
        <v-btn class="ms-auto" variant="text" icon @click="closeSettingsDrawer">
          <v-icon>mdi-close</v-icon>
        </v-btn>
      </div>
      <div class="mt-2">
        <p class="text-subtitle-1 text-grey-darken-2 mb-1">{{ $t('settingsDrawer.geminiApiKey') }}</p>
        <v-text-field :label="$t('settingsDrawer.apiKey')" v-model="settings.geminiApiKey"
                      :type="showApiKey ? 'text' : 'password'"
                      :append-inner-icon="showApiKey ? 'mdi-eye' : 'mdi-eye-off'"
                      @click:append-inner="showApiKey = !showApiKey"
                      :error-messages="apiKeyError" @input="apiKeyError = ''" :loading="isValidating"
                      :disabled="isValidating">
        </v-text-field>
      </div>
      <div class="mt-2">
        <p class="text-subtitle-1 text-grey-darken-2 mb-1">{{ $t('settingsDrawer.camundaModelerConnection') }}</p>
        <v-text-field :label="$t('general.clientId')" v-model="settings.modelerClientId" hide-details
                      class="mb-2"
                      :error-messages="modelerError" @input="modelerError = ''" :loading="isValidating"
                      :disabled="isValidating">
        </v-text-field>
        <v-text-field :loading="isValidating" :disabled="isValidating" :label="$t('general.clientSecret')"
                      v-model="settings.modelerClientSecret"
                      :type="showModelerClientSecret ? 'text' : 'password'"
                      :append-inner-icon="showModelerClientSecret ? 'mdi-eye' : 'mdi-eye-off'"
                      @click:append-inner="showModelerClientSecret = !showModelerClientSecret"
                      :error-messages="modelerError" @input="modelerError = ''">
        </v-text-field>
      </div>
      <div class="mt-2 mb-4">
        <p class="text-subtitle-1 text-grey-darken-2 mb-1">{{ $t('settingsDrawer.camundaOperateConnection') }}</p>
        <v-text-field :label="$t('general.clientId')" v-model="settings.operateClientId"
                      :error-messages="appStore.operateConnectionError" hide-details
                      class="mb-2" @input="appStore.setOperateConnectionError('')" :loading="isValidating"
                      :disabled="isValidating">
        </v-text-field>
        <v-text-field :label="$t('general.clientSecret')" v-model="settings.operateClientSecret"
                      :type="showOperateClientSecret ? 'text' : 'password'"
                      :append-inner-icon="showOperateClientSecret ? 'mdi-eye' : 'mdi-eye-off'"
                      @click:append-inner="showOperateClientSecret = !showOperateClientSecret"
                      :error-messages="appStore.operateConnectionError" @input="appStore.setOperateConnectionError('')"
                      :loading="isValidating"
                      :disabled="isValidating">
        </v-text-field>
        <v-text-field :label="$t('settingsDrawer.regionId')" v-model="settings.operateRegionId"
                      :error-messages="appStore.operateClusterError" @input="appStore.setOperateClusterError('')"
                      :loading="isValidating"
                      :disabled="isValidating" hide-details class="mb-2">
        </v-text-field>
        <v-text-field :label="$t('settingsDrawer.clusterId')" v-model="settings.operateClusterId"
                      :error-messages="appStore.operateClusterError" @input="appStore.setOperateClusterError('')"
                      :loading="isValidating"
                      :disabled="isValidating">
        </v-text-field>
      </div>
      <div class="d-flex">
        <div class="mt-3 me-3">
          <v-btn color="primary" @click="saveSettings">{{ $t('general.save') }}</v-btn>
        </div>
        <div class="mt-3">
          <v-tooltip :text="$t('settingsDrawer.resetToEnvVariables')" location="bottom">
            <template v-slot:activator="{ props }">
              <v-btn v-bind="props" color="grey" @click="resetSettings">{{ $t('settingsDrawer.reset') }}</v-btn>
            </template>
          </v-tooltip>
        </div>
      </div>
    </div>
  </v-navigation-drawer>
</template>

<style scoped>

</style>

<script lang="ts">
import { defineComponent } from 'vue'
import { useAppStore } from "@/store/app";
import axios from "axios";
import { GoogleGenerativeAI } from "@google/generative-ai";

export interface Settings {
  geminiApiKey: string;
  modelerClientId: string;
  modelerClientSecret: string;
  operateClientId: string;
  operateClientSecret: string;
  operateRegionId: string;
  operateClusterId: string;
}

export default defineComponent({
  name: "SettingsDrawer.vue",

  data: () => ({
    appStore: useAppStore(),
    settings: {} as Settings,
    showApiKey: false,
    showModelerClientSecret: false,
    showOperateClientSecret: false,
    apiKeyError: "",
    modelerError: "",
    isValidating: false
  }),

  mounted() {
    if (Object.keys(this.settings).length === 0) this.handleAfterToggle()
  },

  methods: {
    async handleAfterToggle() {
      if (!this.appStore.getAreSettingsOpened()) {
        await this.resetSettingsBar();
      } else {
        await this.fetchSettings();
        await this.validateSettings();
      }
    },
    resetSettings() {
      this.settings = {
        geminiApiKey: import.meta.env.VITE_GEMINI_API_KEY || "",
        modelerClientId: import.meta.env.VITE_MODELER_CLIENT_ID || "",
        modelerClientSecret: import.meta.env.VITE_MODELER_CLIENT_SECRET || "",
        operateClientId: import.meta.env.VITE_OPERATE_CLIENT_ID || "",
        operateClientSecret: import.meta.env.VITE_OPERATE_CLIENT_SECRET || "",
        operateRegionId: import.meta.env.VITE_OPERATE_REGION || "",
        operateClusterId: import.meta.env.VITE_OPERATE_CLUSTER_ID || ""
      }
    },
    async saveSettings() {
      const doSettingsExist = async () => {
        try {
          await axios.get("/api/settings");
          return true;
        } catch (error) {
          return false;
        }
      }

      const areSettingsValid = await this.validateSettings();
      if (!areSettingsValid) {
        return;
      }

      if (await doSettingsExist()) {
        await axios.patch("/api/settings", this.settings, { headers: { 'Content-Type': 'application/json' } });
      } else {
        await axios.post("/api/settings", this.settings, { headers: { 'Content-Type': 'application/json' } });
      }

      this.closeSettingsDrawer();
    },
    async validateSettings(): Promise<boolean> {
      const languages = ['en', 'de']
      this.isValidating = true;
      const operateConnectionInvalidMsgs =
        languages.map(lang => this.$t(`${lang}.settingsDrawer.operateConnectionInvalidMsg`));
      const operateConnectionError = this.appStore.getOperateConnectionError();
      const operateClusterInvalidMsgs =
        languages.map(lang => this.$t(`${lang}.settingsDrawer.operateClusterInvalidMsg`));
      const operateClusterError = this.appStore.getOperateClusterError();
      this.resetValidation();

      const [isAPIKeyValid, isModelerConnectionValid, {
        valid: isOperateConnectionValid,
        token: operateToken
      }] = await Promise.all([
        this.validateAPIKey(),
        this.validateModelerConnection(),
        this.validateOperateConnection()
      ]);
      const isOperateClusterValid = await this.validateOperateClusterId(operateToken || '');
      this.isValidating = false;

      if (!operateConnectionInvalidMsgs.includes(operateConnectionError)) {
        this.appStore.setOperateConnectionError(operateConnectionError);
      }

      if (!operateClusterInvalidMsgs.includes(operateClusterError)) {
        this.appStore.setOperateClusterError(operateClusterError);
      }

      if (!isAPIKeyValid || !isModelerConnectionValid || !isOperateConnectionValid || !isOperateClusterValid) {
        if (!isAPIKeyValid) this.apiKeyError = this.$t('settingsDrawer.apiKeyInvalidMsg');
        if (!isModelerConnectionValid) this.modelerError = this.$t('settingsDrawer.modelerConnectionInvalidMsg');
        if (!isOperateConnectionValid) {
          this.appStore.setOperateConnectionError(this.$t('settingsDrawer.operateConnectionInvalidMsg'));
        }
        if (!isOperateClusterValid) {
          this.appStore.setOperateClusterError(this.$t('settingsDrawer.operateClusterInvalidMsg'));
        }
        return false;
      }
      return true;
    },
    async validateAPIKey(): Promise<boolean> {
      const { geminiApiKey } = this.settings;
      if (!geminiApiKey) {
        return true;
      }
      const genAI = new GoogleGenerativeAI(geminiApiKey);
      const model = genAI.getGenerativeModel({ model: "gemini-1.5-flash" });

      try {
        await model.generateContent("write yes")
        return true;
      } catch (error) {
        return false;
      }
    },
    async validateModelerConnection(): Promise<boolean> {
      const { modelerClientId, modelerClientSecret } = this.settings;
      if (!modelerClientId && !modelerClientSecret) return true;
      if (!modelerClientId || !modelerClientSecret) return false;
      try {
        await axios.post("/api/camunda-cloud/token", {
          "client_id": modelerClientId,
          "client_secret": modelerClientSecret,
        });
        return true;
      } catch (error) {
        return false;
      }
    },
    async validateOperateConnection(): Promise<{ valid: boolean, token: string | null }> {
      const { operateClientId, operateClientSecret } = this.settings;
      if (!operateClientId && !operateClientSecret) return { valid: true, token: null };
      if (!operateClientId || !operateClientSecret) return { valid: false, token: null };
      try {
        const result = await axios.post("/api/camunda-cloud/token", {
          "client_id": operateClientId,
          "client_secret": operateClientSecret,
          "audience": "operate.camunda.io"
        });
        return { valid: true, token: result.data };
      } catch (error) {
        return { valid: false, token: null };
      }
    },
    async validateOperateClusterId(operateToken: string): Promise<boolean> {
      const { operateRegionId, operateClusterId } = this.settings;
      if (!operateRegionId && !operateClusterId) return true;
      if (!operateRegionId || !operateClusterId) return false;
      try {
        await axios.post("/api/camunda-cloud/process-instances", {
          "token": operateToken,
          "regionId": this.settings.operateRegionId,
          "clusterId": this.settings.operateClusterId
        });
        return true;
      } catch (error) {
        return false;
      }
    },
    async resetSettingsBar() {
      await this.fetchSettings();
      this.resetValidation();
      this.showApiKey = false;
      this.showModelerClientSecret = false;
      this.showOperateClientSecret = false;
    },
    resetValidation() {
      this.apiKeyError = "";
      this.modelerError = "";
      this.appStore.setOperateClusterError('');
      this.appStore.setOperateConnectionError("");
    },
    async fetchSettings() {
      try {
        await axios.get("/api/settings").then(result => {
          this.settings = result.data;
        });
      } catch (error) {
        this.settings = {} as Settings;
      }

      this.settings.geminiApiKey = this.settings.geminiApiKey || import.meta.env.VITE_GEMINI_API_KEY;
      this.settings.modelerClientId = this.settings.modelerClientId || import.meta.env.VITE_MODELER_CLIENT_ID;
      this.settings.modelerClientSecret = this.settings.modelerClientSecret || import.meta.env.VITE_MODELER_CLIENT_SECRET;
      this.settings.operateClientId = this.settings.operateClientId || import.meta.env.VITE_OPERATE_CLIENT_ID;
      this.settings.operateClientSecret = this.settings.operateClientSecret || import.meta.env.VITE_OPERATE_CLIENT_SECRET;
      this.settings.operateRegionId = this.settings.operateRegionId || import.meta.env.VITE_OPERATE_REGION_ID;
      this.settings.operateClusterId = this.settings.operateClusterId || import.meta.env.VITE_OPERATE_CLUSTER_ID;
    },
    closeSettingsDrawer() {
      this.appStore.setAreSettingsOpened(false);
    }
  },

  watch: {
    'appStore.areSettingsOpened'() {
      this.handleAfterToggle();
    }
  }
});
</script>
