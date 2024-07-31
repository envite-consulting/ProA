<template>
  <v-toolbar>
    <v-toolbar-title>
      <div class="d-flex align-center">
        <span>{{ selectedProjectName }}</span>
        <span class="text-body-2 text-grey-darken-1 ms-4">VERSION {{ selectedVersionName }}</span>
      </div>
    </v-toolbar-title>

    <v-spacer></v-spacer>

    <v-tooltip :text="$t('processMap.retrieveProcessInstances')" location="bottom">
      <template v-slot:activator="{ props }">
        <v-btn icon v-bind="props" @click="handleFetchProcessInstances">
          <v-icon>mdi-play</v-icon>
        </v-btn>
      </template>
    </v-tooltip>

    <v-menu location="bottom" :close-on-content-click="false">
      <template v-slot:activator="{ props }">
        <v-btn icon v-bind="props" class="me-5px">
          <v-icon>mdi-map-legend</v-icon>
        </v-btn>
      </template>

      <Legend/>
    </v-menu>

    <v-menu location="bottom" :close-on-content-click="false">
      <template v-slot:activator="{ props }">
        <v-btn v-if="filtersCount > 0" v-bind="props" icon class="me-5px">
          <v-badge color="white" :bordered="true" :content="filtersCount">
            <v-icon>mdi-filter-outline</v-icon>
          </v-badge>
        </v-btn>

        <v-btn v-else v-bind="props" icon class="me-5px">
          <v-icon>mdi-filter-outline</v-icon>
        </v-btn>
      </template>

      <v-list>
        <v-list-item>
          <v-list-item-title class="font-weight-bold d-flex align-center justify-space-between">
            <div>{{ $t('processMap.hide') }}:</div>
            <div>
              <v-btn @click="clearFilters" variant="text" :text="$t('processMap.clear')" color="grey"/>
            </div>
          </v-list-item-title>
        </v-list-item>
        <v-divider></v-divider>
        <v-list-item class="filter-item" v-for="(label, filterOption) in filterOptions" :key="filterOption">
          <v-checkbox v-model="filterGraphInput[filterOption]" :label="label" color="primary"
                      @change="filterGraph" hide-details></v-checkbox>
        </v-list-item>
      </v-list>
    </v-menu>

    <v-btn icon @click="fetchProcessModels">
      <v-icon>mdi-refresh</v-icon>
    </v-btn>
  </v-toolbar>
</template>

<style scoped>
.me-5px {
  margin-right: 5px;
}

.filter-item {
  height: 1rem;
}
</style>

<script lang="ts">
import { defineComponent } from 'vue'

import getProject from "@/components/projectService";
import Legend from "@/components/ProcessMap/Legend.vue";
import LegendItem from "@/components/ProcessMap/LegendItem.vue";
import { useAppStore } from "@/store/app";

export default defineComponent({
  name: "ProcessMapToolbar",
  components: { Legend, LegendItem },

  props: {
    selectedProjectId: {
      type: Number,
      required: true
    }
  },

  data() {
    const store = useAppStore();
    const persistedFilterGraphInput = store.getFiltersForProject(this.selectedProjectId!);

    const defaultFilterGraphInput = {
      hideAbstractDataStores: false,
      hideCallActivities: false,
      hideConnectionLabels: false,
      hideIntermediateEvents: false,
      hideProcessesWithoutConnections: false,
      hideStartEndEvents: false
    }

    let filterGraphInput = { ...defaultFilterGraphInput };

    if (!!persistedFilterGraphInput) {
      filterGraphInput = JSON.parse(persistedFilterGraphInput);
    }

    return {
      selectedProjectName: '' as string,
      selectedVersionName: '' as string,
      defaultFilterGraphInput,
      filterGraphInput,
      store
    }
  },

  computed: {
    filterOptions() {
      return {
        hideAbstractDataStores: this.$t('processMap.resources'),
        hideCallActivities: this.$t('general.callActivities'),
        hideConnectionLabels: this.$t('processMap.connectionLabels'),
        hideIntermediateEvents: this.$t('processMap.intermediateEvents'),
        hideProcessesWithoutConnections: this.$t('processMap.processesWithoutConnections'),
        hideStartEndEvents: this.$t('processMap.endToStartConnections')
      }
    },
    filtersCount(): number {
      return Object.values(this.filterGraphInput).filter(value => value).length
    }
  },

  mounted() {
    getProject(this.selectedProjectId!).then(result => {
      this.selectedProjectName = result.data.name;
      this.selectedVersionName = result.data.version;
    });
  },

  methods: {
    clearFilters() {
      this.filterGraphInput = { ...this.defaultFilterGraphInput };
      this.$emit('filterGraph', this.filterGraphInput);
    },
    fetchProcessModels() {
      this.$emit('fetchProcessModels');
    },
    filterGraph() {
      this.$emit('filterGraph', this.filterGraphInput);
    },
    handleFetchProcessInstances() {
      this.$emit('handleFetchProcessInstances');
    },
    saveFilters() {
      this.store.setFiltersForProject(this.selectedProjectId!, JSON.stringify(this.filterGraphInput));
    }
  }
});
</script>
