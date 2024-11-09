<template>
  <v-card class="pa-5">
    <v-card-title class="px-0 pt-0">
      <div class="d-flex align-center">
        <span>{{ $t('authentication.createAnAccount') }}</span>
        <v-btn variant="text" icon class="ms-auto" @click="closeDialog">
          <v-icon icon="mdi-close"></v-icon>
        </v-btn>
      </div>
    </v-card-title>

    <v-divider/>
    <v-card-text>
      <v-form ref="createAccountForm" @submit.prevent>
        <v-alert v-if="message.message !== ''"
                 closable
                 icon="mdi-alert-circle-outline"
                 :text="message.message"
                 :type="message.type"
                 class="mb-5"
                 @click:close="message.message = ''"
        ></v-alert>
        <v-text-field
          type="email"
          v-model="email"
          :label="$t('authentication.email')"
          required
          variant="outlined"
          class="my-2"
          :rules="emailRules"
        ></v-text-field>
        <v-text-field
          v-model="firstName"
          :label="$t('authentication.firstName')"
          required
          variant="outlined"
          class="my-2"
          :rules="firstNameRules"
        ></v-text-field>
        <v-text-field
          type="email"
          v-model="lastName"
          :label="$t('authentication.lastName')"
          required
          variant="outlined"
          class="my-2"
          :rules="lastNameRules"
        ></v-text-field>
        <v-text-field
          type="password"
          v-model="password"
          :label="$t('authentication.password')"
          required
          variant="outlined"
          :rules="passwordRules"
        ></v-text-field>
        <v-select
          v-model="selectedRole"
          :label="$t('authentication.role')"
          :items="localizedRoleOptions"
          item-title="label"
          item-value="value"
          required
          variant="outlined"
          class="my-2"
        ></v-select>
        <v-btn type="button" @click="createAccount" color="primary" block height="50">
          {{ $t('general.continue') }}
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
import {
  emailRules,
  firstNameRules,
  lastNameRules,
  newPasswordRules
} from "@/components/Authentication/formValidation";
import { VForm } from "vuetify/components";
import axios, { AxiosError } from "axios";
import { SelectedDialog, useAppStore } from "@/store/app";
import { Message } from "@/components/Authentication/AuthenticationDialog.vue";
import { authHeader } from "@/components/Authentication/authHeader";
import { Role } from "@/components/ProcessMap/types";

export default defineComponent({
  name: "CreateAccount",

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
      firstName: '' as string,
      lastName: '' as string,
      emailRules: emailRules,
      firstNameRules: firstNameRules,
      lastNameRules: lastNameRules,
      passwordRules: newPasswordRules,
      SelectedDialog: SelectedDialog,
      store: useAppStore(),
      selectedRole: Role.USER
    }
  },

  computed: {
    localizedRoleOptions() {
      const roles = Object.values(Role);
      return roles.map(role => ({
        value: role,
        label: this.$t(`authentication.${role.toLowerCase()}`)
      }));
    }
  },

  methods: {
    async createAccount() {
      this.$emit('showMessage', { message: '', type: 'error' } as Message);
      const form = this.$refs.createAccountForm as VForm;
      form.resetValidation();
      const { valid } = await form.validate();
      if (!valid) {
        return;
      }

      try {
        await axios.post('/api/authentication/register', {
          email: this.email,
          password: this.password,
          firstName: this.firstName,
          lastName: this.lastName,
          role: this.selectedRole
        }, { headers: { ...authHeader(), 'Content-Type': 'application/json' } });

        this.$emit('showMessage', {
          type: 'success',
          message: this.$t('authentication.successfullyCreatedAccount') as string
        });
      } catch (e) {
        if ((e as AxiosError).response?.status === 409) {
          this.$emit('showMessage', {
            type: 'error',
            message: this.$t('authentication.emailAlreadyRegistered') as string
          });
          return;
        }

        this.$emit('showMessage', {
          type: 'error',
          message: this.$t('authentication.accountCreationFailed') as string
        });
      }
    },
    closeDialog() {
      this.store.setSelectedDialog(SelectedDialog.NONE);
    }
  }
});
</script>
