<template>
  <v-card height="100%">

    <div id="process-modelling" class="full-screen"></div>

  </v-card>
</template>
<style>
.full-screen{
  width: 100%;
  height: 100%;
}
</style>
<script lang="ts">
import { defineComponent } from 'vue';
import BpmnViewer from 'bpmn-js';
import axios from 'axios';

export default defineComponent({
  data: () => ({

  }),


  mounted: function () {

    var viewer = new BpmnViewer({
      container: '#process-modelling'
    });

    let url = '/api/process-model/'+this.$route.params.id;

    axios.get(url)
      .then(response => {
        var xmltext = response.data;
        viewer.importXML(xmltext).then(function (result: any) {

          const { warnings } = result;
          viewer.get('process-modelling').zoom('fit-viewport');
        }).catch(function (err: any) {

          const { warnings, message } = err;
          console.log('something went wrong:', warnings, message);
        });
      });

  }
})
</script>
