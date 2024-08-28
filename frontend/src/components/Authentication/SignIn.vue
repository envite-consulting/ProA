<template>
  <v-card class="pa-5">
    <v-card-title class="px-0 pt-0">
      <div class="d-flex align-center">
        <span>{{ $t('authentication.welcomeBack') }}</span>
        <v-btn variant="text" icon class="ms-auto" @click="closeDialog">
          <v-icon icon="mdi-close"></v-icon>
        </v-btn>
      </div>
    </v-card-title>

    <v-divider/>

    <v-card-text>
      <v-form ref="signInForm" @submit.prevent>
        <v-alert v-if="message.message !== ''"
                 closable
                 icon="mdi-alert-circle-outline"
                 :text="message.message"
                 :type="message.type"
                 class="mb-5"
                 @click:close="message.message = ''"
        ></v-alert>
        <v-text-field
          ref="emailTextField"
          type="email"
          v-model="email"
          :label="$t('authentication.email')"
          required
          variant="outlined"
          class="my-2"
          :rules="emailRules"
        ></v-text-field>
        <v-text-field
          type="password"
          v-model="password"
          :label="$t('authentication.password')"
          required
          variant="outlined"
          :rules="passwordRules"
        ></v-text-field>
        <p class="pa-1 mb-1 clickable-link d-inline-block" @click="handleForgotPw">
          {{ $t('authentication.forgotPwQuestion') }}
        </p>
        <v-btn type="button" color="primary" block height="50" @click="signIn">
          {{ $t('navigation.signIn') }}
        </v-btn>
        <p class="my-1 pa-1 clickable-link d-inline-block"
           @click="resetMessageAndOpenDialog(SelectedDialog.CREATE_ACCOUNT)">
          {{ $t('authentication.createAccountQuestion') }}</p>
      </v-form>
    </v-card-text>
  </v-card>
</template>

<style scoped>
@import "authentication.css";
</style>

<script lang="ts">
import { defineComponent } from 'vue'
import { currentPasswordRules, emailRules } from "@/components/Authentication/formValidation";
import { VForm, VTextField } from "vuetify/components";
import axios from "axios";
import { SelectedDialog, useAppStore } from "@/store/app";
import { Message } from "@/components/Authentication/AuthenticationDialog.vue";

export default defineComponent({
  name: "SignIn",

  props: {
    message: {
      type: Object,
      required: true
    }
  },

  data() {
    return {
      email: '' as string,
      password: '' as string,
      emailRules: emailRules,
      passwordRules: currentPasswordRules,
      store: useAppStore(),
      SelectedDialog: SelectedDialog,
    }
  },

  methods: {
    resetMessageAndOpenDialog(dialog: SelectedDialog) {
      this.$emit('showMessage', {
        type: 'error',
        message: '' as string
      });
      this.openDialog(dialog);
    },
    openDialog(dialog: SelectedDialog) {
      this.store.setSelectedDialog(dialog);
    },
    closeDialog() {
      this.store.setSelectedDialog(SelectedDialog.NONE);
    },
    async signIn() {
      this.$emit('showMessage', { message: '', type: 'error' } as Message);
      const form = this.$refs.signInForm as VForm;
      form.resetValidation();
      const { valid } = await form.validate();
      if (!valid) {
        return;
      }

      try {
        const response = await axios.post('/api/authentication/login', {
          email: this.email,
          password: this.password
        }, { headers: { 'Content-Type': 'application/json' } });

        const { password, ...user } = response.data;
        this.store.setUser(user);

        this.closeDialog();
        if (window.location.pathname === '/') {
          const currentState = window.history.state || {};
          const updatedState = { ...currentState, showLoggedInBanner: true };
          window.history.replaceState(updatedState, document.title);
          window.location.reload();
        } else {
          this.$router.push({ path: "/", state: { showLoggedInBanner: true } });
        }
      } catch
        (e) {
        this.$emit('showMessage', {
          type: 'error',
          message: this.$t('authentication.signInFailed') as string
        });
      }
    },
    async handleForgotPw() {
      (this.$refs.signInForm as VForm).resetValidation();

      const valid = (await (this.$refs.emailTextField as VTextField).validate()).length === 0;

      if (!valid) {
        return;
      }

      this.$emit('showMessage', {
        type: 'success',
        message: this.$t('authentication.resetPwEmailSent') as string
      });
    }
  }
});
</script>
