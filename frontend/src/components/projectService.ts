import axios from 'axios';
import { useAppStore } from "@/store/app";
import { authHeader } from "@/components/Authentication/authHeader";

const webVersion = import.meta.env.VITE_DESKTOP_OR_WEB == 'web';
const userId = useAppStore().getUser()?.id;

const getProject = (projectId: number) => {
  if (webVersion) {
    return axios.get(`/api/project/${userId}/${projectId}`, { headers: authHeader() })
  }
  return axios.get(`/api/project/project-id/${projectId}`)
}

export default getProject;
