<template>
  <v-app-bar color="primary" prominent>
    <v-app-bar-nav-icon variant="text" @click.stop="drawer = !drawer"></v-app-bar-nav-icon>

    <v-toolbar-title>ProA</v-toolbar-title>

    <v-spacer></v-spacer>


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
        <!-- <v-list-item-icon>
            <v-icon>{{ item.icon }}</v-icon>
        </v-list-item-icon> -->
        <v-list-item-content>
          <v-list-title>{{ item.title }}</v-list-title>
        </v-list-item-content>
      </v-list-item>
    </v-list>
  </v-navigation-drawer>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { useAppStore } from "@/store/app";

export interface Settings {
  geminiApiKey: string;
  modelerClientId: string;
  modelerClientSecret: string;
  operateClientId: string;
  operateClientSecret: string;
}

export default defineComponent({
  methods: { useAppStore },
  data: () => ({
    drawer: false,
    group: null,
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
    ],
  }),

  watch: {
    group() {
      this.drawer = false
    },
  },
})
</script>
