<template>
  <v-list-group
    v-if="modelNode.children.length > 0"
    v-bind="{ value: modelNode.id }"
  >
    <template v-slot:activator="{ props }">
      <v-list-item v-bind="props" class="no-select">
        <v-list-item-title>
          {{ modelNode.processName }}
          <span class="text-body-2 text-grey-darken-1">
            {{ $t("processList.collaboration") }}
          </span>
        </v-list-item-title>

        <v-list-item-subtitle>
          {{ getLocaleDate(modelNode.createdAt) }}
          {{ !!modelNode.description ? "-" : "" }} {{ modelNode.description }}
        </v-list-item-subtitle>

        <template v-slot:append>
          <v-btn
            color="grey-lighten-1"
            icon="mdi-delete"
            variant="text"
            @click.stop="$emit('delete-process', modelNode)"
          >
          </v-btn>
          <v-btn
            color="grey-lighten-1"
            icon="mdi-more"
            variant="text"
            :to="'/ProcessView/' + modelNode.id"
          ></v-btn>
          <v-btn
            color="grey-lighten-1"
            icon="mdi-information"
            variant="text"
            @click.stop="$emit('more-info', modelNode.id)"
          ></v-btn>
        </template>
      </v-list-item>
    </template>

    <v-list-item
      v-for="(child, index) in modelNode.children"
      :key="'child-' + child.id"
      density="compact"
      class="no-select"
    >
      <ProcessTreeNode
        :model="child"
        :key="'child-node-' + child.id"
        @delete-process="$emit('delete-process', child)"
        @upload-process="$emit('upload-process', child.id)"
        @more-info="$emit('more-info', child.id)"
      />
      <v-divider v-if="index < modelNode.children.length - 1"></v-divider>
    </v-list-item>
  </v-list-group>

  <v-list-item v-else class="no-select">
    <v-list-item-title>
      {{ modelNode.processName }}
      <span class="text-body-2 text-grey-darken-1">
        {{
          modelNode.processType === "PARTICIPANT"
            ? $t("processList.participant")
            : ""
        }}
      </span>
    </v-list-item-title>

    <v-list-item-subtitle>
      {{ getLocaleDate(modelNode.createdAt) }}
      {{ !!modelNode.description ? "-" : "" }} {{ modelNode.description }}
    </v-list-item-subtitle>

    <template v-slot:append>
      <v-btn
        color="grey-lighten-1"
        icon="mdi-delete"
        variant="text"
        @click="$emit('delete-process', modelNode)"
      ></v-btn>
      <v-btn
        v-if="modelNode.processType !== 'PARTICIPANT'"
        color="grey-lighten-1"
        icon="mdi-upload"
        variant="text"
        @click="$emit('upload-process', modelNode.id)"
      ></v-btn>
      <v-btn
        color="grey-lighten-1"
        icon="mdi-more"
        variant="text"
        :to="'/ProcessView/' + modelNode.id"
      ></v-btn>
      <v-btn
        color="grey-lighten-1"
        icon="mdi-information"
        variant="text"
        @click="$emit('more-info', modelNode.id)"
      ></v-btn>
    </template>
  </v-list-item>
</template>

<style scoped>
.no-select {
  user-select: none;
}
</style>

<script lang="ts">
import { ProcessModelNode } from "@/components/ProcessList/ProcessList.vue";
import { useAppStore } from "@/store/app";

export default {
  name: "ProcessTreeNode",
  props: {
    model: Object
  },
  data() {
    return {
      appStore: useAppStore(),
      modelNode: this.model as ProcessModelNode
    };
  },
  methods: {
    getLocaleDate(date: string): string {
      const locales =
        this.appStore.getSelectedLanguage() === "de" ? "de-DE" : "en-US";
      return new Date(date).toLocaleString(locales);
    }
  }
};
</script>
