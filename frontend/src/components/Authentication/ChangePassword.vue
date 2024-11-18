<template>
  <v-card class="pa-5">
    <v-card-title class="px-0 pt-0">
      <div class="d-flex align-center justify-space-between">
        <span>{{ $t('authentication.changePassword') }}</span>
        <div class="d-flex align-center">
          <v-btn variant="text" icon @click="resetMessageAndOpenDialog(SelectedDialog.PROFILE)" class="me-2">
            <v-icon icon="mdi-arrow-left"></v-icon>
          </v-btn>
          <v-btn variant="text" icon @click="closeDialog">
            <v-icon icon="mdi-close"></v-icon>
          </v-btn>
        </div>
      </div>
    </v-card-title>

    <v-divider/>

    <v-card-text>
      <v-form ref="changePwForm" @submit.prevent>
        <v-alert v-if="message.message !== ''"
                 closable
                 icon="mdi-alert-circle-outline"
                 :text="message.message"
                 :type="message.type"
                 class="mb-5"
                 @click:close="message.message = ''"
        ></v-alert>
        <v-text-field
          type="password"
          v-model="currentPassword"
          :label="$t('authentication.currentPassword')"
          required
          variant="outlined"
          class="my-2"
          :rules="currentPasswordRules"
        ></v-text-field>
        <v-text-field
          type="password"
          v-model="newPassword"
          :label="$t('authentication.newPassword')"
          required
          variant="outlined"
          :rules="newPasswordRules"
          class="my-2"
        ></v-text-field>
        <v-btn type="button" color="primary" block class="mb-1" height="50" @click="changePassword">
          {{ $t('authentication.changePassword') }}
        </v-btn>
      </v-form>
    </v-card-text>
  </v-card>
</template>

<style scoped>
@import "authentication.css";
</style>

<script lang="ts">
import { defineComponent } from 'vue'

import { currentPasswordRules, newPasswordRules } from "@/components/Authentication/formValidation";
import { Message } from "@/components/Authentication/AuthenticationDialog.vue";
import { SelectedDialog, useAppStore } from "@/store/app";
import { VForm } from "vuetify/components";
import axios from "axios";
import { authHeader } from "@/components/Authentication/authHeader";
import { UserData } from "@/components/Home/ProjectOverview.vue";
import getUser from "@/components/userService";

export default defineComponent({
  name: "ChangePassword",

  props: {
    message: {
      type: Object,
      required: true
    }
  },

  data() {
    return {
      currentPasswordRules: currentPasswordRules,
      newPasswordRules: newPasswordRules,
      currentPassword: '' as string,
      newPassword: '' as string,
      SelectedDialog: SelectedDialog,
      user: {} as UserData,
      store: useAppStore()
    }
  },

  async mounted() {
    if (this.store.getUserToken() != null) this.user = await getUser();
  },

  methods: {
    resetMessageAndOpenDialog(selected: SelectedDialog) {
      this.$emit('showMessage', { message: '', type: 'error' } as Message);
      this.openDialog(selected);
    },
    closeDialog() {
      this.store.setSelectedDialog(SelectedDialog.NONE);
    },
    openDialog(dialog: SelectedDialog) {
      this.store.setSelectedDialog(dialog);
    },
    async changePassword() {
      this.$emit('showMessage', { message: '', type: 'error' } as Message);

      const form = this.$refs.changePwForm as VForm;
      form.resetValidation();
      const { valid } = await form.validate();
      if (!valid) {
        return;
      }

      const currPassword = this.currentPassword;
      const { email } = this.user;

      const isPasswordValid = await this.testSignIn(email, currPassword);
      if (!isPasswordValid) {
        const message = {
          message: this.$t('authentication.currentPwWrongError'),
          type: 'error'
        }
        this.$emit('showMessage', message);
        return;
      }

      if (currPassword === this.newPassword) {
        const message = {
          message: this.$t('authentication.samePasswordError'),
          type: 'error'
        }
        this.$emit('showMessage', message);
        return;
      }

      try {
        await axios.patch(
          '/api/user', { password: this.newPassword },
          { headers: { ...authHeader(), 'Content-Type': 'application/json' } }
        );

        const message: Message = {
          type: 'success',
          message: this.$t('authentication.passwordSuccessfullyChanged') as string
        }
        this.$emit('showMessage', message);
        this.openDialog(SelectedDialog.PROFILE);
      } catch (e) {
        const message: Message = {
          type: 'error',
          message: this.$t('authentication.passwordChangeErrorMsg') as string
        }
        this.$emit('showMessage', message);
      }
    },
    async testSignIn(email: string, currPassword: string): Promise<boolean> {
      try {
        await axios.post('/api/authentication/login', {
          email,
          password: currPassword
        }, { headers: { 'Content-Type': 'application/json' } });

        return true;
      } catch (e) {
        return false;
      }
    }
  }
})
</script>
