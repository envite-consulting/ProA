<template>
  <v-dialog v-model="dialogModel" max-width="500">
    <v-card class="pa-5">
      <v-card-title>
        <div class="d-flex align-center">
          <span>{{ $t("manageUsers.editProfile") }}</span>
          <v-btn variant="text" icon class="ms-auto" @click="closeDialog">
            <v-icon icon="mdi-close"></v-icon>
          </v-btn>
        </div>
      </v-card-title>
      <v-divider />
      <v-card-text class="mb-3">
        <v-form ref="editUserForm" @submit.prevent>
          <v-text-field
            v-model="localUserEmail"
            :label="$t('authentication.email')"
            variant="outlined"
            :rules="emailRules"
          />
          <v-text-field
            v-model="localUserFirstName"
            :label="$t('authentication.firstName')"
            variant="outlined"
            :rules="firstNameRules"
          />
          <v-text-field
            v-model="localUserLastName"
            :label="$t('authentication.lastName')"
            variant="outlined"
            :rules="lastNameRules"
          />
          <v-text-field
            v-model="newPassword"
            type="password"
            :label="$t('authentication.newPassword')"
            variant="outlined"
            :rules="newPasswordRules"
          />
        </v-form>
        <div class="d-flex align-center justify-space-between">
          <v-btn
            type="button"
            height="50"
            color="primary"
            @click="patchUser(userId)"
          >
            {{ $t("manageUsers.saveChanges") }}
          </v-btn>
          <v-btn
            v-if="ownUserId !== userId"
            type="button"
            height="50"
            variant="text"
            color="red"
            @click="deleteUser(userId)"
          >
            {{ "Delete User" }}
          </v-btn>
        </div>
      </v-card-text>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import axios from "axios";
import { authHeader } from "@/components/Authentication/authHeader";
import {
  emailRules,
  firstNameRules,
  lastNameRules,
  newPasswordRules,
} from "@/components/Authentication/formValidation";
import { VForm } from "vuetify/components";

export default defineComponent({
  name: "EditUserDialog",
  props: {
    showDialog: {
      type: Boolean,
      required: true,
      default: false,
    },
    userId: {
      type: Number,
      required: true,
    },
    userEmail: {
      type: String,
      required: true,
    },
    userFirstName: {
      type: String,
      required: true,
    },
    userLastName: {
      type: String,
      required: true,
    },
    userCreatedAt: {
      type: String,
      required: true,
    },
    userModifiedAt: {
      type: String,
      required: true,
    },
    ownUserId: {
      type: Number,
      required: true,
    },
  },

  data() {
    return {
      localUserEmail: "" as string,
      localUserFirstName: "" as string,
      localUserLastName: "" as string,
      newPassword: "" as string,
      emailRules: emailRules,
      firstNameRules: firstNameRules,
      lastNameRules: lastNameRules,
      newPasswordRules: newPasswordRules,
    };
  },

  computed: {
    dialogModel() {
      return this.showDialog;
    },
  },

  methods: {
    closeDialog() {
      this.$emit("close");
    },
    async deleteUser(id: number) {
      await axios.delete(`/api/user/${id}`, { headers: authHeader() });
      this.$emit("deleteUser", id);
      this.closeDialog();
    },
    async patchUser(id: number) {
      const form = this.$refs.editUserForm as VForm;
      const { valid } = await form.validate();
      if (!valid) {
        return;
      }

      const data = {
        email:
          this.localUserEmail != this.userEmail ? this.localUserEmail : null,
        firstName:
          this.localUserFirstName != this.userFirstName
            ? this.localUserFirstName
            : null,
        lastName:
          this.localUserLastName != this.userLastName
            ? this.localUserLastName
            : null,
        password: this.newPassword != "" ? this.newPassword : null,
      };
      await axios.patch(`/api/user/${id}`, data, { headers: authHeader() });
      this.$emit("fetchUsers");
      this.closeDialog();
    },
  },

  watch: {
    showDialog(newValue: boolean) {
      if (newValue) {
        this.localUserEmail = this.userEmail;
        this.localUserFirstName = this.userFirstName;
        this.localUserLastName = this.userLastName;
        this.newPassword = "";
      }
    },
  },
});
</script>
