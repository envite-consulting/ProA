<template>
  <v-list>
    <v-list-item>
      <v-list-item-title class="font-weight-bold">{{ $t('processMap.legend') }}</v-list-item-title>
    </v-list-item>
    <v-divider></v-divider>
    <LegendItem :text="$t('processMap.process')" path="M 0 0 h 140 l 10 35 l -10 35 H 0 l 10 -35 l -10 -35 Z"
                width="30"
                height="30" view-box="-2 -2 154 74" stroke-width="8"></LegendItem>
    <LegendItem :text="$t('processMap.database')"
                path="M 1.5 9 C 15.5 19 75.5 19 90.5 9 l 0 90 c -15 10 -75 10 -89 0 l 0 -90 C 15.5 -1 75.5 -1 90.5 9 v 15 c -15 10 -75 10 -89 0"
                width="30"
                height="30" view-box="-0.5 -0.5 93 109" stroke-width="5"></LegendItem>
    <LegendItem :text="$t('processMap.startEvent')" path="M 10 -20 a 10 10 0 1 0 0.00001 0 Z" width="30"
                height="30" view-box="-1.99999 -22 24 24" stroke-width="1.5"></LegendItem>
    <LegendItem :text="$t('processMap.endEvent')" path="M 10 -20 a 10 10 0 1 0 0.00001 0 Z" width="30"
                height="30" view-box="-1.99999 -22 24 24" stroke-width="3"></LegendItem>
    <LegendItem :text="$t('general.intermediateEvent')"
                path="M -25 -10 a 10 10 0 1 0 0.00001 0 Z M -25 -7 a 7 7 0 1 0 0.00001 0 Z"
                width="30"
                height="30" view-box="-37 -12 24 24" stroke-width="2"></LegendItem>
    <LegendItem :text="$t('processMap.callActivity')"
                path="M 35 -10 a3,3 0 0 1 3,3 v15 a3,3 0 0 1 -3,3 h-25 a3,3 0 0 1 -3,-3 v-15 a3,3 0 0 1 3,-3 z"
                width="30"
                height="30" view-box="5 -12 35 25" stroke-width="2"></LegendItem>
  </v-list>
</template>

<style scoped>

</style>

<script lang="ts">
import { defineComponent } from 'vue'
import LegendItem from "@/components/ProcessMap/LegendItem.vue";

export default defineComponent({
  name: "Legend",
  components: { LegendItem }
})
</script>
