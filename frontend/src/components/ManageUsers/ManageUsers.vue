<template>
  <v-container>
    <div class="d-flex align-center justify-start my-2">
      <v-text-field
        :label="$t('general.search')" prepend-inner-icon="mdi-magnify" clearable density="compact"
        variant="solo-filled" class="search-bar-width py-1" hide-details
        v-model="searchValue">
      </v-text-field>
    </div>
    <v-table>
      <thead>
      <tr>
        <th class="text-left">
          {{ $t('authentication.id') }}
        </th>
        <th class="text-left">
          {{ $t('authentication.email') }}
        </th>
        <th class="text-left">
          {{ $t('authentication.firstName') }}
        </th>
        <th class="text-left">
          {{ $t('authentication.lastName') }}
        </th>
        <th class="text-left">
          {{ $t('authentication.role') }}
        </th>
        <th class="text-left">
          {{ $t('general.createdOn') }}
        </th>
        <th class="text-left">
          {{ $t('general.lastModifiedOn') }}
        </th>
        <th/>
      </tr>
      </thead>
      <tbody>
      <tr
        v-for="item in usersShown"
        :key="item.id"
      >
        <td>{{ item.id }}</td>
        <td>{{ item.email }}</td>
        <td>{{ item.firstName }}</td>
        <td>{{ item.lastName }}</td>
        <td>{{ $t(`authentication.${item.role.toLowerCase()}`) }}</td>
        <td>{{ getLocaleDate(item.createdAt) }}</td>
        <td>{{ getLocaleDate(item.modifiedAt) }}</td>
        <td>
          <v-btn @click="deleteUser(item.id)" icon="mdi-delete" variant="text" color="grey-lighten-1"/>
        </td>
      </tr>
      </tbody>
    </v-table>
  </v-container>
</template>

<style scoped>
.search-bar-width {
  max-width: 50%;
}
</style>

<script lang="ts">
import { defineComponent } from 'vue'
import { UserData } from "@/components/Home/ProjectOverview.vue";
import axios from "axios";
import { authHeader } from "@/components/Authentication/authHeader";
import { useAppStore } from "@/store/app";

export default defineComponent({
  name: "ManageUsers",

  data() {
    return {
      user: {} as UserData,
      users: [] as UserData[],
      usersShown: [] as UserData[],
      store: useAppStore(),
      searchValue: "" as string,
    }
  },

  computed: {
    isUserLoggedIn() {
      return this.store.getUserToken() !== null;
    }
  },

  async mounted() {
    const users = (await axios.get('/api/user/all', { headers: authHeader() })).data;
    this.users = users.sort((user1: UserData, user2: UserData) => user1.id - user2.id);
    this.usersShown = [...this.users];

    this.user = (await axios.get('/api/user', { headers: authHeader() })).data;
  },

  methods: {
    async deleteUser(id: number) {
      await axios.delete(`/api/user/${id}`, { headers: authHeader() });
      this.users = this.users.filter(user => user.id !== id);
      this.usersShown = this.usersShown.filter(user => user.id !== id);
      if (this.user.id === id) {
        this.store.setUserToken(null);
        this.$router.push("/");
      }
    },

    getLocaleDate(date: string): string {
      const locales = this.store.getSelectedLanguage() === 'de' ? 'de-DE' : 'en-US';
      return new Date(date).toLocaleDateString(locales);
    }
  },

  watch: {
    isUserLoggedIn(newValue) {
      if (!newValue) {
        this.$router.push("/");
      }
    },

    searchValue(newValue) {
      if (!newValue) {
        this.usersShown = [...this.users];
        return;
      }

      this.usersShown = this.users.filter(user =>
        user.email.toLowerCase().includes(newValue.toLowerCase())
        || user.firstName?.toLowerCase().includes(newValue.toLowerCase())
        || user.lastName?.toLowerCase().includes(newValue.toLowerCase())
        || this.$t(`authentication.${user.role.toLowerCase()}`).toLowerCase().includes(newValue.toLowerCase()));
    }
  }

});
</script>
