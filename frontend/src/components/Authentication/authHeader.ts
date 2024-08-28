import { useAppStore } from "@/store/app";

const store = useAppStore();

export const authHeader = () => {
  const user = store.getUser();
  if (user) {
    return { Authorization: `Bearer ${user.token}` };
  }
  return {};
}
