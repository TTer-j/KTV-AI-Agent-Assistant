import axios from 'axios'

const baseURL = '/api/songs'

export const songApi = {
  search: (keyword) => axios.get(`${baseURL}/search`, { params: { keyword } }),
  hot: (limit = 10) => axios.get(`${baseURL}/hot`, { params: { limit } }),
  scene: (scene) => axios.get(`${baseURL}/scene/${scene}`),
  genre: (genre) => axios.get(`${baseURL}/genre/${genre}`),
  detail: (id) => axios.get(`${baseURL}/${id}`)
}