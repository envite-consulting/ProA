<template>
  <v-list-item>
    <div class="d-flex flex-row align-center">
      <svg xmlns="http://www.w3.org/2000/svg" :width="width" :height="height" :viewBox="viewBox">
        <path :d="path" stroke="#000" :stroke-width="strokeWidth"
              fill="none"/>
      </svg>
      <p class="ms-4">{{ text }}</p>
    </div>
  </v-list-item>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
  name: 'LegendItem',
  props: {
    path: String,
    viewBox: String,
    text: String,
    width: String,
    height: String,
    strokeWidth: { type: String, default: "5" }
  }
});
</script>
