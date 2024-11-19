<template>
  <v-dialog v-model="showDialog" max-width="500" @click:outside="closeDialog" persistent no-click-animation>
    <ProfileDialog v-if="selectedDialog === SelectedDialog.PROFILE" :message="message" @showMessage="showMessage"/>
    <EditProfileDialog v-if="selectedDialog === SelectedDialog.EDIT_PROFILE" @showMessage="showMessage"
                       :message="message"/>
    <CreateAccount v-if="selectedDialog === SelectedDialog.CREATE_ACCOUNT" :message="message"
                   @showMessage="showMessage"/>
    <ChangePassword v-if="selectedDialog === SelectedDialog.CHANGE_PW" @showMessage="showMessage" :message="message"/>
  </v-dialog>
</template>

<style scoped>
@import "authentication.css";
</style>

<script lang="ts">
import { defineComponent } from 'vue'
import EditProfileDialog from "@/components/Authentication/EditProfileDialog.vue";
import ProfileDialog from "@/components/Authentication/ProfileDialog.vue";
import CreateAccount from "@/components/Authentication/CreateAccount.vue";
import ChangePassword from "@/components/Authentication/ChangePassword.vue";
import { useAppStore } from "@/store/app";
import { SelectedDialog } from "@/store/app";

export interface Message {
  message: string,
  type: 'error' | 'success'
}

export default defineComponent({
  name: "AuthenticationDialog",
  components: { ChangePassword, CreateAccount, ProfileDialog, EditProfileDialog },

  data() {
    return {
      SelectedDialog: SelectedDialog,
      message: { message: '', type: 'error' } as Message,
      store: useAppStore()
    }
  },

  computed: {
    showDialog() {
      return this.store.getSelectedDialog() != SelectedDialog.NONE;
    },
    selectedDialog(): SelectedDialog {
      return this.store.getSelectedDialog();
    }
  },

  methods: {
    showMessage(message: Message) {
      this.message = message;
    },
    resetMessage() {
      this.message = { message: '', type: 'error' };
    },
    closeDialog() {
      this.store.setSelectedDialog(SelectedDialog.NONE);
    }
  },

  watch: {
    showDialog() {
      if (!this.showDialog) {
        this.resetMessage();
      }
    }
  }
})
</script>




