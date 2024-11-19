import { useAppStore } from "@/store/app";

const store = useAppStore();

export const authHeader = () => {
  const userToken = store.getUserToken();
  return userToken ? { Authorization: `Bearer ${userToken}` } : {};
}
