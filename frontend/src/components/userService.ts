import axios from "axios";
import { authHeader } from "./Authentication/authHeader";

const getUser = async () => {
  const response = await axios.get('/api/user', { headers: authHeader() });
  const { data } = response;
  return data;
}

export default getUser;
