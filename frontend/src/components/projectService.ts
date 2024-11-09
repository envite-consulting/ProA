import axios from "axios";
import { authHeader } from "./Authentication/authHeader";

const getProject = (projectId: number) => {
    return axios.get(`/api/project/${projectId}`, { headers: authHeader() })
}

export default getProject;
