<template>
  <div class="ktv-index">
    <header class="header">
      <div class="logo">
        <span class="logo-icon">🎤</span>
        <span class="logo-text">KTV AI智能点歌</span>
      </div>
      <div class="header-right">
        <button class="btn-secondary" @click="$router.push('/playlist')">
          <span>🎵</span> 我的歌单
        </button>
        <button class="btn-primary" @click="showAiChat = true">
          <span>💬</span> AI助手
        </button>
      </div>
    </header>

    <section class="hero-section">
      <div class="hero-content">
        <h1>欢迎来到KTV智能点歌系统</h1>
        <p>说出你想唱的歌，AI帮你快速找到</p>
        <div class="search-box">
          <input 
            v-model="searchKeyword" 
            type="text" 
            placeholder="输入歌曲名或歌手..."
            @keyup.enter="searchSongs"
          />
          <button class="search-btn" @click="searchSongs">
            <span>🔍</span>
          </button>
        </div>
        <button 
          class="voice-btn" 
          :class="{ active: isListening }"
          @mousedown="startListening"
          @mouseup="stopListening"
          @touchstart="startListening"
          @touchend="stopListening"
        >
          <span>{{ isListening ? '🛑' : '🎙️' }}</span>
          <span>{{ isListening ? '停止' : '语音点歌' }}</span>
        </button>
      </div>
    </section>

    <section class="hot-section">
      <div class="section-header">
        <h2>🔥 热门歌曲</h2>
        <button class="more-btn" @click="loadMoreHot">查看更多</button>
      </div>
      <div class="song-grid">
        <div 
          v-for="song in hotSongs" 
          :key="song.id" 
          class="song-card"
          @click="selectSong(song)"
        >
          <img :src="song.coverUrl" :alt="song.songName" class="song-cover" />
          <div class="song-info">
            <h3>{{ song.songName }}</h3>
            <p>{{ song.singer }}</p>
          </div>
          <button class="add-btn">+</button>
        </div>
      </div>
    </section>

    <section class="scene-section">
      <div class="section-header">
        <h2>🎵 场景歌单推荐</h2>
      </div>
      <div class="scene-grid">
        <div 
          v-for="(scene, index) in scenes" 
          :key="index" 
          class="scene-card"
          @click="loadSceneSongs(scene.name)"
        >
          <span class="scene-icon">{{ scene.icon }}</span>
          <span class="scene-name">{{ scene.name }}</span>
        </div>
      </div>
    </section>

    <footer class="footer">
      <p>KTV AI智能点歌助手 - 让唱歌更简单</p>
    </footer>

    <AiChat 
      v-if="showAiChat" 
      @close="showAiChat = false" 
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { songApi } from '../api/song'
import { speechRecognition } from '../utils/speech'
import AiChat from './AiChat.vue'

const searchKeyword = ref('')
const hotSongs = ref([])
const showAiChat = ref(false)
const isListening = ref(false)
let recognition = null

const scenes = [
  { name: '生日派对', icon: '🎂' },
  { name: '朋友聚会', icon: '👥' },
  { name: '浪漫约会', icon: '💕' },
  { name: '毕业季', icon: '🎓' },
  { name: '经典老歌', icon: '📻' },
  { name: '动感舞曲', icon: '💃' },
]

onMounted(() => {
  loadHotSongs()
})

const loadHotSongs = async () => {
  try {
    const res = await songApi.hot(6)
    hotSongs.value = res.data.data
  } catch (error) {
    console.error('加载热门歌曲失败', error)
  }
}

const searchSongs = async () => {
  if (!searchKeyword.value.trim()) return
  try {
    const res = await songApi.search(searchKeyword.value)
    hotSongs.value = res.data.data
  } catch (error) {
    console.error('搜索失败', error)
  }
}

const loadMoreHot = async () => {
  try {
    const res = await songApi.hot(12)
    hotSongs.value = res.data.data
  } catch (error) {
    console.error('加载更多失败', error)
  }
}

const loadSceneSongs = async (scene) => {
  try {
    const res = await songApi.scene(scene)
    hotSongs.value = res.data.data
  } catch (error) {
    console.error('加载场景歌曲失败', error)
  }
}

const selectSong = (song) => {
  console.log('选择歌曲:', song)
}

const startListening = () => {
  if (!speechRecognition.isSupported()) {
    alert('您的浏览器不支持语音识别')
    return
  }
  isListening.value = true
  recognition = speechRecognition.start(
    (result) => {
      searchKeyword.value = result
      searchSongs()
    },
    (error) => {
      console.error('语音识别错误', error)
      isListening.value = false
    },
    () => {
      isListening.value = false
    }
  )
}

const stopListening = () => {
  speechRecognition.stop(recognition)
  isListening.value = false
}
</script>

<style scoped>
.ktv-index {
  min-height: 100vh;
  color: #fff;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 40px;
  background: rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(10px);
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-icon {
  font-size: 32px;
}

.logo-text {
  font-size: 24px;
  font-weight: bold;
  background: linear-gradient(90deg, #ff6b6b, #feca57);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.btn-primary {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: linear-gradient(90deg, #ff6b6b, #feca57);
  border: none;
  border-radius: 25px;
  color: #fff;
  font-size: 16px;
  cursor: pointer;
  transition: transform 0.2s;
}

.btn-primary:hover {
  transform: scale(1.05);
}

.btn-secondary {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 25px;
  color: #fff;
  font-size: 16px;
  cursor: pointer;
  margin-right: 10px;
  transition: all 0.2s;
}

.btn-secondary:hover {
  background: rgba(255, 255, 255, 0.2);
}

.hero-section {
  padding: 80px 40px;
  text-align: center;
}

.hero-content h1 {
  font-size: 48px;
  margin-bottom: 20px;
  background: linear-gradient(90deg, #ff6b6b, #feca57, #48dbfb);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.hero-content p {
  font-size: 20px;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 40px;
}

.search-box {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-bottom: 20px;
}

.search-box input {
  width: 500px;
  padding: 15px 20px;
  font-size: 18px;
  border: none;
  border-radius: 30px;
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

.search-box input::placeholder {
  color: rgba(255, 255, 255, 0.5);
}

.search-btn {
  padding: 15px 25px;
  background: linear-gradient(90deg, #ff6b6b, #feca57);
  border: none;
  border-radius: 30px;
  font-size: 20px;
  cursor: pointer;
  transition: transform 0.2s;
}

.search-btn:hover {
  transform: scale(1.05);
}

.voice-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 30px;
  background: rgba(255, 255, 255, 0.1);
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 30px;
  color: #fff;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.2s;
}

.voice-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.voice-btn.active {
  background: linear-gradient(90deg, #ff6b6b, #feca57);
  border-color: transparent;
}

.hot-section, .scene-section {
  padding: 40px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.section-header h2 {
  font-size: 28px;
}

.more-btn {
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 20px;
  color: #fff;
  font-size: 14px;
  cursor: pointer;
}

.song-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.song-card {
  position: relative;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 15px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
}

.song-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 30px rgba(255, 107, 107, 0.3);
}

.song-cover {
  width: 100%;
  height: 180px;
  object-fit: cover;
}

.song-info {
  padding: 15px;
}

.song-info h3 {
  font-size: 16px;
  margin-bottom: 5px;
}

.song-info p {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
}

.add-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  width: 35px;
  height: 35px;
  background: rgba(0, 0, 0, 0.5);
  border: none;
  border-radius: 50%;
  color: #fff;
  font-size: 20px;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.2s;
}

.song-card:hover .add-btn {
  opacity: 1;
}

.scene-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 20px;
}

.scene-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 30px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 15px;
  cursor: pointer;
  transition: transform 0.3s, background 0.3s;
}

.scene-card:hover {
  transform: translateY(-5px);
  background: rgba(255, 255, 255, 0.1);
}

.scene-icon {
  font-size: 40px;
  margin-bottom: 10px;
}

.scene-name {
  font-size: 16px;
}

.footer {
  padding: 30px;
  text-align: center;
  background: rgba(0, 0, 0, 0.3);
  color: rgba(255, 255, 255, 0.5);
}
</style>