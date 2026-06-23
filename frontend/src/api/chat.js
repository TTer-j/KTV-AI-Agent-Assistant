import axios from 'axios'

const baseURL = '/api/ai'

export const chatApi = {
  chat: (data) => axios.post(`${baseURL}/chat`, data)
}