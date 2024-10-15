<template>
  <div class="d-flex align-center justify-center" style="height: 100%; width: 100%">
    <v-card class="pa-5" width="500">
      <v-card-title class="px-0 pt-0">
        {{ $t('authentication.welcomeBack') }}
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
          />
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
          <v-btn type="button" color="primary" block height="50" @click="signIn">
            {{ $t('navigation.signIn') }}
          </v-btn>
        </v-form>
      </v-card-text>
    </v-card>
  </div>
</template>

<style scoped>
@import "../Authentication/authentication.css";
</style>

<script lang="ts">
import { defineComponent } from 'vue'
import { currentPasswordRules, emailRules } from "@/components/Authentication/formValidation";
import { VForm } from "vuetify/components";
import axios from "axios";
import { SelectedDialog, useAppStore } from "@/store/app";
import { Message } from "@/components/Authentication/AuthenticationDialog.vue";


export default defineComponent({
  name: "SignIn",

  data() {
    return {
      email: '' as string,
      password: '' as string,
      emailRules: emailRules,
      passwordRules: currentPasswordRules,
      store: useAppStore(),
      SelectedDialog: SelectedDialog,
      message: { message: '', type: 'error' } as Message,
      defaultMessage: { message: '', type: 'error' } as Message
    }
  },

  methods: {
    async signIn() {
      this.message = { ...this.defaultMessage };
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

        this.$router.push({ path: "/", state: { showLoggedInBanner: true } });
      } catch
        (e) {
        this.message = {
          type: 'error',
          message: this.$t('authentication.signInFailed') as string
        };
      }
    }
  }
})
;
</script>
