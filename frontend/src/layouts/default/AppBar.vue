<template>
  <v-app-bar color="primary" prominent>
    <v-app-bar-nav-icon variant="text" @click.stop="drawer = !drawer"></v-app-bar-nav-icon>

    <v-toolbar-title>ProA – {{ getCurrentRouteName }}</v-toolbar-title>

    <v-spacer></v-spacer>

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
        :disabled="!useAppStore().selectedProjectId && item.title !== $t('navigation.projectOverview')"
        class="px-2"
      >
        <v-list-item-title class="text-body-1 font-weight-regular">{{ item.title }}</v-list-item-title>
      </v-list-item>
    </v-list>
  </v-navigation-drawer>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { useAppStore } from "@/store/app";
import i18n from "@/i18n";

export type LanguageCode = 'en' | 'de';

interface Language {
  code: LanguageCode
  name: string
}

enum AppRoutes {
  ProjectOverview = 'ProjectOverview',
  ProcessList = 'ProcessList',
  ProcessView = 'ProcessView',
  CamundaCloudImport = 'CamundaCloudImport',
  ProcessMap = 'ProcessMap',
}

enum AppRoutesGerman {
  ProjectOverview = 'Projektübersicht',
  ProcessList = 'Prozessliste',
  ProcessView = 'Prozessansicht',
  CamundaCloudImport = 'C8 Import',
  ProcessMap = 'Prozesskarte',
}

export default defineComponent({
  methods: {
    useAppStore,
    getGermanRouteName(routeName: string): string {
      switch (routeName) {
        case AppRoutes.ProjectOverview:
          return AppRoutesGerman.ProjectOverview
        case AppRoutes.ProcessList:
          return AppRoutesGerman.ProcessList
        case AppRoutes.ProcessView:
          return AppRoutesGerman.ProcessView
        case AppRoutes.CamundaCloudImport:
          return AppRoutesGerman.CamundaCloudImport
        case AppRoutes.ProcessMap:
          return AppRoutesGerman.ProcessMap
        default:
          return routeName
      }
    }
  },
  data: () => ({
    drawer: false as boolean,
    group: null,
    items: [
      {
        title: 'Projektübersicht',
        route: '/'
      },
      {
        title: 'Prozessliste',
        route: '/ProcessList'
      },
      {
        title: 'C8 Import',
        route: '/CamundaCloudImport'
      },
      {
        title: 'Prozesskarte',
        route: '/ProcessMap'
      },
    ],
    selectedLanguage: useAppStore().getSelectedLanguage() as LanguageCode,
  }),
  mounted() {
    i18n.global.locale = this.selectedLanguage;
  },
  computed: {
    getCurrentRouteName() {
      const routeName = (this.$router.currentRoute.value.name || '').toString();
      return this.getGermanRouteName(routeName);
    },
    changeLanguage(language: LanguageCode) {
      this.selectedLanguage = language;
      useAppStore().setSelectedLanguage(language);
      i18n.global.locale = language;
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
        },
      ];
    }
  },

  watch: {
    group() {
      this.drawer = false
    },
  },
})
</script>
