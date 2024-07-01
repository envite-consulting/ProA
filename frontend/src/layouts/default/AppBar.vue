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
        :disabled="!appStore.selectedProjectId && item.title !== 'Projektübersicht'"
      >
        {{ item.title }}
      </v-list-item>
    </v-list>
  </v-navigation-drawer>

  <SettingsDrawer />
</template>

<style scoped>

</style>

<script lang="ts">
import { defineComponent } from 'vue'
import { useAppStore } from "@/store/app";
import SettingsDrawer from "@/components/SettingsDrawer.vue";

export default defineComponent({
  components: { SettingsDrawer },
  data: () => ({
    appStore: useAppStore(),
    drawer: false as boolean,
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
    ]
  }),

  methods: {
    toggleSettings() {
      this.appStore.setAreSettingsOpened(!this.appStore.getAreSettingsOpened());
    }
  },

  watch: {
    group() {
      this.drawer = false;
    }
  }
})
</script>
