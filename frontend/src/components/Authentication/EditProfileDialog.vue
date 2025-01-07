<template>
  <v-card class="pa-5">
    <v-card-title class="px-0 pt-0">
      <div class="d-flex align-center justify-space-between">
        <span>{{ $t('authentication.editYourProfile') }}</span>
        <div class="d-flex align-center">
          <v-btn variant="text" icon @click="openDialog(SelectedDialog.PROFILE)" class="me-2">
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
      <v-form @submit.prevent ref="editProfileForm">
        <v-alert :type="message.type" closable class="mb-5" icon="mdi-alert-circle-outline"
                 v-if="message.message !== ''" :text="message.message" @click:close="removeMessage"></v-alert>
        <v-text-field
          v-model="newUserData.firstName"
          :label="$t('authentication.firstName')"
          variant="outlined"
          required
          :rules="firstNameRules"
          class="my-2"
        />

        <v-text-field
          v-model="newUserData.lastName"
          :label="$t('authentication.lastName')"
          variant="outlined"
          required
          :rules="lastNameRules"
          class="my-2"
        />

        <v-text-field
          type="email"
          v-model="newUserData.email"
          :label="$t('authentication.email')"
          variant="outlined"
          required
          :rules="emailRules"
          class="my-2"
        />

        <v-btn type="button" height="50" block color="primary" class="mb-1" @click="updateUser">
          {{ $t('general.save') }}
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
import { SelectedDialog, useAppStore } from "@/store/app";
import { emailRules, firstNameRules, lastNameRules } from "@/components/Authentication/formValidation";
import { VForm } from "vuetify/components";
import axios, { AxiosError } from "axios";
import { Message } from "@/components/Authentication/AuthenticationDialog.vue";
import { authHeader } from "@/components/Authentication/authHeader";
import getUser from "@/components/userService";
import { UserData } from "@/components/Home/ProjectOverview.vue";

export default defineComponent({
  name: "EditProfileDialog",

  data() {
    const store = useAppStore();
    const newUserData = {} as UserData;
    return {
      store,
      newUserData,
      emailRules: emailRules,
      firstNameRules: firstNameRules,
      lastNameRules: lastNameRules,
      SelectedDialog: SelectedDialog
    }
  },

  props: {
    message: {
      type: Object,
      required: true
    }
  },

  async mounted() {
    this.newUserData = await getUser();
  },

  methods: {
    closeDialog() {
      this.store.setSelectedDialog(SelectedDialog.NONE);
    },
    openDialog(dialog: SelectedDialog) {
      this.store.setSelectedDialog(dialog);
    },
    async updateUser() {
      this.$emit('showMessage', { message: '', type: 'error' } as Message);
      const form = this.$refs.editProfileForm as VForm;
      form.resetValidation();
      const { valid } = await form.validate();
      if (!valid) {
        return;
      }
      const currUserData = await getUser();
      if (JSON.stringify(this.newUserData) === JSON.stringify(currUserData)) {
        this.openDialog(SelectedDialog.PROFILE)
        return;
      }
      try {
        await axios.patch(
          '/api/user',
          this.newUserData,
          { headers: { ...authHeader(), 'Content-Type': 'application/json' } }
        );

        const message: Message = {
          type: 'success',
          message: this.$t('authentication.profileSuccessfullyEdited') as string
        }
        this.$emit('showMessage', message);
        this.openDialog(SelectedDialog.PROFILE);
      } catch (e) {

        if ((e as AxiosError).response?.status === 429) {
          const message: Message = {
            type: 'error',
            message: this.$t('authentication.tooManyRequests') as string
          }
          this.$emit('showMessage', message);
          return;
        }

        const message: Message = {
          type: 'error',
          message: this.$t('authentication.profileEditFailed') as string
        }
        this.$emit('showMessage', message);
      }
    },
    removeMessage() {
      this.$emit('removeMessage');
    }
  }
});
</script>



