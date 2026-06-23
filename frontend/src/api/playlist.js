import axios from 'axios'

const baseURL = '/api/playlists'

export const playlistApi = {
  recommend: (scene = '') => axios.get(`${baseURL}/recommend`, { params: { scene } }),
  userPlaylists: (userId) => axios.get(`${baseURL}/user/${userId}`),
  detail: (id) => axios.get(`${baseURL}/${id}`),
  create: (data) => axios.post(baseURL, data),
  update: (id, data) => axios.put(`${baseURL}/${id}`, data),
  delete: (id) => axios.delete(`${baseURL}/${id}`)
}