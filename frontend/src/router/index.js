import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import AIChat from '../views/AiChat.vue'
import Playlist from '../views/PlayList.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/ai-chat',
    name: 'AIChat',
    component: AIChat
  },
  {
    path: '/playlist',
    name: 'Playlist',
    component: Playlist
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
