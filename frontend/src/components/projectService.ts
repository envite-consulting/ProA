import axios from 'axios';

const getProject = (projectId: number) => {
    return axios.get("/api/project/" + projectId)
}

export default getProject;