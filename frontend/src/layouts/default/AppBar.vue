<template>
  <v-app-bar color="primary" prominent>
    <v-app-bar-nav-icon variant="text" @click.stop="drawer = !drawer"></v-app-bar-nav-icon>

    <v-toolbar-title>ProA – {{ currentRouteName }}</v-toolbar-title>

    <v-spacer></v-spacer>

    <v-tooltip location="bottom" v-if="webVersion && isUserLoggedIn">
      <template v-slot:activator="{ props }">
        <v-btn v-bind="props" @click="openDialog(SelectedDialog.PROFILE)">
          <v-icon icon="mdi-account-circle"></v-icon>
        </v-btn>
      </template>
      {{ $t('general.myProfile') }}
    </v-tooltip>

    <v-tooltip location="bottom" v-if="webVersion && isUserLoggedIn && isUserAdmin">
      <template v-slot:activator="{ props }">
        <v-btn v-bind="props" @click="openDialog(SelectedDialog.CREATE_ACCOUNT)">
          <v-icon icon="mdi-account-plus"></v-icon>
        </v-btn>
      </template>
      {{ $t('navigation.createAccount') }}
    </v-tooltip>

    <v-tooltip location="bottom" v-if="webVersion && isUserLoggedIn">
      <template v-slot:activator="{ props }">
        <v-btn v-bind="props" @click="signOut">
          <v-icon icon="mdi-logout"></v-icon>
        </v-btn>
      </template>
      {{ $t('general.signOut') }}
    </v-tooltip>

    <v-menu>
      <template v-slot:activator="{ props }">
        <v-btn
          color="white"
          variant="text"
          v-bind="props"
        >
          {{ selectedLanguage.toUpperCase() }}
        </v-btn>
      </template>
      <v-list>
        <v-list-item
          v-for="language in availableLanguages.sort()"
          :key="language.code"
          :value="language.code"
          @click="changeLanguage(language.code)"
        >
          <v-list-item-title>{{ language.name }}</v-list-item-title>
        </v-list-item>
      </v-list>
    </v-menu>

    <v-tooltip :text="$t('general.settings')" location="bottom">
      <template v-slot:activator="{ props }">
        <v-btn icon v-bind="props" @click="toggleSettings">
          <v-icon>mdi-cog</v-icon>
        </v-btn>
      </template>
    </v-tooltip>
  </v-app-bar>

  <v-navigation-drawer v-model="drawer" location="left" temporary>
    <v-list
      flat dense nav class="py-1 px-0"
    >
      <v-list-item
        @click="$router.push({ path: item.route })"
        v-for="item in items"
        :key="item.title"
        dense
        :disabled="!store.getSelectedProjectId() && item.title !== $t('navigation.projectOverview')"
        class="px-2"
      >
        <v-list-item-title class="text-body-1 font-weight-regular">{{ item.title }}</v-list-item-title>
      </v-list-item>
    </v-list>
  </v-navigation-drawer>

  <SettingsDrawer/>

  <AuthenticationDialog v-if="webVersion"/>

</template>

<style scoped>
</style>

<script lang="ts">
import { defineComponent } from 'vue'
import { useAppStore } from "@/store/app";
import SettingsDrawer from "@/components/SettingsDrawer.vue";
import i18n from "@/i18n";
import AuthenticationDialog from "@/components/Authentication/AuthenticationDialog.vue";
import { SelectedDialog } from "@/store/app";
import { Role } from "@/components/ProcessMap/types";

export type LanguageCode = 'en' | 'de';

interface Language {
  code: LanguageCode
  name: string
}

export default defineComponent({
  components: { AuthenticationDialog, SettingsDrawer },
  methods: {
    changeLanguage(language: LanguageCode) {
      this.selectedLanguage = language;
      this.store.setSelectedLanguage(language);
      i18n.global.locale = language;
    },
    lowerFirstLetter(s: string | undefined) {
      if (!s) {
        return '';
      }
      return s.charAt(0).toLowerCase() + s.slice(1);
    },
    toggleSettings() {
      this.store.setAreSettingsOpened(!this.store.getAreSettingsOpened());
    },
    signOut() {
      this.store.setUser(null);
    },
    openDialog(dialog: SelectedDialog) {
      this.store.setSelectedDialog(dialog);
    }
  },

  data: () => {
    const store = useAppStore();

    return {
      store,
      drawer: false as boolean,
      group: null,
      selectedLanguage: store.getSelectedLanguage() as LanguageCode,
      webVersion: (import.meta.env.VITE_APP_MODE === 'web') as boolean,
      showEditDialog: false as boolean,
      showProfileDialog: false as boolean,
      showProfileSuccessMessage: false as boolean,
      SelectedDialog: SelectedDialog
    }
  },

  mounted() {
    i18n.global.locale = this.selectedLanguage;
  },

  computed: {
    selectedDialog() {
      return this.store.getSelectedDialog();
    },
    availableLanguages(): Language[] {
      const availableLanguages: Language[] = [
        {
          code: 'de',
          name: this.$t('general.german')
        },
        {
          code: 'en',
          name: this.$t('general.english')
        }
      ];
      return availableLanguages.sort((language1, language2) => language1.name.localeCompare(language2.name));
    },
    items() {
      return [
        {
          title: this.$t('navigation.projectOverview'),
          route: '/'
        },
        {
          title: this.$t('navigation.processList'),
          route: '/ProcessList'
        },
        {
          title: this.$t('navigation.c8Import'),
          route: '/CamundaCloudImport'
        },
        {
          title: this.$t('navigation.processMap'),
          route: '/ProcessMap'
        }
      ];
    },
    currentRouteName() {
      return this.$t('navigation.' + this.lowerFirstLetter(this.$route.name?.toString()));
    },
    isUserLoggedIn() {
      return this.store.getUser() != null;
    },
    isUserAdmin() {
      return this.store.getUser()?.role === Role.ADMIN;
    }
  },

  watch: {
    group() {
      this.drawer = false
    }
  }
})
</script>
