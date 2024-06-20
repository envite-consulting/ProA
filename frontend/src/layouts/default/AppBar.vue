<template>
  <v-app-bar color="primary" prominent>
    <v-app-bar-nav-icon variant="text" @click.stop="drawer = !drawer"></v-app-bar-nav-icon>

    <v-toolbar-title>ProA</v-toolbar-title>

    <v-spacer></v-spacer>

    <v-tooltip text="Einstellungen">
      <template v-slot:activator="{ props }">
        <v-btn icon v-bind="props" @click="toggleSettings">
          <v-icon>mdi-cog</v-icon>
        </v-btn>
      </template>
    </v-tooltip>
  </v-app-bar>

  <v-navigation-drawer v-model="drawer" location="left" temporary>
    <v-list
      flat dense nav class="py-1"
    >
      <v-list-item
        @click="$router.push({ path: item.route })"
        v-for="item in items"
        :key="item.title"
        dense
        :disabled="!useAppStore().selectedProjectId && item.title !== 'Projektübersicht'"
      >
        {{ item.title }}
      </v-list-item>
    </v-list>
  </v-navigation-drawer>

  <v-navigation-drawer
    v-model="openSettings"
    location="right"
    temporary
    width="400"
  >
    <div class="d-flex flex-column ma-3 ms-4">
      <div class="d-flex align-center">
        <p class="text-h6">Einstellungen</p>
        <v-btn class="ms-auto" variant="text" icon @click="toggleSettings">
          <v-icon>mdi-close</v-icon>
        </v-btn>
      </div>
      <div class="mt-2">
        <p class="text-subtitle-1 text-grey-darken-2 mb-1">Gemini API Key</p>
        <v-text-field label="API Key" v-model="settings.geminiApiKey" :type="showApiKey ? 'text' : 'password'"
                      :append-inner-icon="showApiKey ? 'mdi-eye' : 'mdi-eye-off'"
                      @click:append-inner="showApiKey = !showApiKey"
                      :error-messages="apiKeyError" @input="apiKeyError = ''" :loading="isValidating"
                      :disabled="isValidating">
        </v-text-field>
      </div>
      <div class="mt-2">
        <p class="text-subtitle-1 text-grey-darken-2 mb-1">Camunda Modeler Verbindung</p>
        <v-text-field label="Client ID" v-model="settings.modelerClientId" hide-details class="mb-2"
                      :error-messages="modelerError" @input="modelerError = ''" :loading="isValidating"
                      :disabled="isValidating">
        </v-text-field>
        <v-text-field :loading="isValidating" :disabled="isValidating" label="Client Secret"
                      v-model="settings.modelerClientSecret"
                      :type="showModelerClientSecret ? 'text' : 'password'"
                      :append-inner-icon="showModelerClientSecret ? 'mdi-eye' : 'mdi-eye-off'"
                      @click:append-inner="showModelerClientSecret = !showModelerClientSecret"
                      :error-messages="modelerError" @input="modelerError = ''">
        </v-text-field>
      </div>
      <div class="mt-2 mb-4">
        <p class="text-subtitle-1 text-grey-darken-2 mb-1">Camunda Operate Verbindung</p>
        <v-text-field label="Client ID" v-model="settings.operateClientId" :error-messages="operateError" hide-details
                      class="mb-2" @input="operateError = ''" :loading="isValidating" :disabled="isValidating">
        </v-text-field>
        <v-text-field label="Client Secret" v-model="settings.operateClientSecret"
                      :type="showOperateClientSecret ? 'text' : 'password'"
                      :append-inner-icon="showOperateClientSecret ? 'mdi-eye' : 'mdi-eye-off'"
                      @click:append-inner="showOperateClientSecret = !showOperateClientSecret"
                      :error-messages="operateError" @input="operateError = ''" :loading="isValidating"
                      :disabled="isValidating">
        </v-text-field>
      </div>
      <div class="d-flex">
        <div class="mt-3 me-3">
          <v-btn color="primary" @click="saveSettings">Speichern</v-btn>
        </div>
        <div class="mt-3">
          <v-tooltip text="Werte auf Umgebungsvariablen zurücksetzen" location="bottom">
            <template v-slot:activator="{ props }">
              <v-btn v-bind="props" color="grey" @click="resetSettings">Zurücksetzen</v-btn>
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
import { GoogleGenerativeAI } from "@google/generative-ai";
import axios from "axios";

export interface Settings {
  geminiApiKey: string;
  modelerClientId: string;
  modelerClientSecret: string;
  operateClientId: string;
  operateClientSecret: string;
}

export interface Settings {
  geminiApiKey: string;
  modelerClientId: string;
  modelerClientSecret: string;
  operateClientId: string;
  operateClientSecret: string;
}

export default defineComponent({
  data: () => ({
    drawer: false as boolean,
    settings: {} as Settings,
    openSettings: false as boolean,
    showApiKey: false as boolean,
    apiKeyError: "" as string,
    apiKeySuccess: false as boolean,
    showModelerClientSecret: false as boolean,
    modelerError: "" as string,
    modelerSuccess: false as boolean,
    showOperateClientSecret: false as boolean,
    operateError: "" as string,
    operateSuccess: false as boolean,
    group: null,
    isValidating: false as boolean,
    items: [
      {
        title: 'Projektübersicht',
        route: '/'
      },
      {
        title: 'C8 Import',
        route: '/CamundaCloudImport'
      },
      {
        title: 'Prozessliste',
        route: '/ProcessList'
      },
      {
        title: 'Prozesskarte',
        route: '/ProcessMap'
      },
    ]
  }),

  methods: {
    useAppStore,
    async toggleSettings() {
      this.openSettings = !this.openSettings;
      if (!this.openSettings) {
        await this.resetSettingsBar();
      } else {
        await this.fetchSettings();
        await this.validateSettings();
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

      await this.toggleSettings();
    },
    async validateSettings(): Promise<boolean> {
      this.isValidating = true;
      this.resetValidation();

      const [isAPIKeyValid, isModelerConnectionValid, isOperateConnectionValid] = await Promise.all([
        this.validateAPIKey(),
        this.validateModelerConnection(),
        this.validateOperateConnection()
      ]);
      this.isValidating = false;

      const {
        geminiApiKey,
        modelerClientId,
        modelerClientSecret,
        operateClientId,
        operateClientSecret
      } = this.settings;
      if (!isAPIKeyValid || !isModelerConnectionValid || !isOperateConnectionValid) {
        if (!isAPIKeyValid) this.apiKeyError = "API Key ungültig";
        if (geminiApiKey && isAPIKeyValid) this.apiKeySuccess = true;
        if (!isModelerConnectionValid) this.modelerError = "Camunda Modeler Verbindung ungültig";
        if (modelerClientId && modelerClientSecret && isModelerConnectionValid) this.modelerSuccess = true;
        if (!isOperateConnectionValid) this.operateError = "Camunda Operate Verbindung ungültig";
        if (operateClientId && operateClientSecret && isOperateConnectionValid) this.operateSuccess = true;
        return false;
      }
      return true;
    },
    async validateAPIKey() {
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
    async validateModelerConnection() {
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
    async validateOperateConnection() {
      const { operateClientId, operateClientSecret } = this.settings;
      if (!operateClientId && !operateClientSecret) return true;
      if (!operateClientId || !operateClientSecret) return false;
      try {
        await axios.post("/api/camunda-cloud/token", {
          "client_id": operateClientId,
          "client_secret": operateClientSecret,
          "audience": "operate.camunda.io"
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
      this.operateError = "";
      this.apiKeySuccess = false;
      this.modelerSuccess = false;
      this.operateSuccess = false;
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
    },
    resetSettings() {
      this.settings = {
        geminiApiKey: import.meta.env.VITE_GEMINI_API_KEY || "",
        modelerClientId: import.meta.env.VITE_MODELER_CLIENT_ID || "",
        modelerClientSecret: import.meta.env.VITE_MODELER_CLIENT_SECRET || "",
        operateClientId: import.meta.env.VITE_OPERATE_CLIENT_ID || "",
        operateClientSecret: import.meta.env.VITE_OPERATE_CLIENT_SECRET || ""
      }
    }
  },

  watch: {
    group() {
      this.drawer = false;
    },
  },
})
</script>
