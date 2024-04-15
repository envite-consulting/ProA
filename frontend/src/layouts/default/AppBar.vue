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

export default defineComponent({
  data: () => ({
      drawer: false,
      group: null,
      items: [
        {
          title: 'Home',
          route: '/'
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
      group () {
        this.drawer = false
      },
    },
})
</script>
