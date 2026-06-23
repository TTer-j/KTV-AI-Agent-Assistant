<template>
  <div class="ktv-shell">
    <aside class="side-panel glass-panel">
      <div class="brand">
        <div class="brand-mark">AI</div>
        <div>
          <strong>KTV AI</strong>
          <span>智能点歌助手</span>
        </div>
      </div>

      <nav class="side-nav" aria-label="主导航">
        <button class="nav-btn active" title="AI 点歌">⌘</button>
        <router-link class="nav-btn" to="/playlist" title="场景歌单">♪</router-link>
        <router-link class="nav-btn" to="/ai-chat" title="对话页">↗</router-link>
      </nav>

      <div class="mini-section">
        <p class="eyebrow">偏好画像</p>
        <div class="preference-tags">
          <span v-for="tag in preferenceTags" :key="tag">{{ tag }}</span>
        </div>
      </div>

      <div class="mini-section history">
        <p class="eyebrow">历史点歌</p>
        <button
          v-for="item in history.slice(0, 6)"
          :key="item.id"
          class="history-item"
          @click="quickAsk(item.text)"
        >
          <span>{{ item.text }}</span>
          <small>{{ item.time }}</small>
        </button>
        <p v-if="history.length === 0" class="empty-text">暂无记录</p>
      </div>
    </aside>

    <main class="workspace">
      <header class="topbar glass-panel">
        <div>
          <p class="eyebrow">Room A08 · Voice Ready</p>
          <h1>一句话点歌，AI 帮你配完整歌单</h1>
        </div>
        <div class="top-actions">
          <button class="icon-btn" title="清空对话" @click="resetChat">↺</button>
          <button class="primary-btn" @click="generatePersonalMix">为我推荐</button>
        </div>
      </header>

      <section class="command-card glass-panel">
        <div class="prompt-row">
          <button
            class="mic-button"
            :class="{ listening: isListening }"
            :title="isListening ? '停止语音' : '语音点歌'"
            @click="toggleListening"
          >
            {{ isListening ? '■' : '●' }}
          </button>
          <textarea
            v-model="inputMessage"
            name="ai-song-command"
            placeholder="来首周杰伦的慢歌 / 给我生日局歌单 / 我要适合告白的情歌"
            rows="2"
            @keydown.enter.exact.prevent="sendMessage"
          ></textarea>
          <button class="send-button" title="发送" @click="sendMessage">↑</button>
        </div>

        <div class="quick-prompts">
          <button v-for="prompt in quickPrompts" :key="prompt" @click="quickAsk(prompt)">
            {{ prompt }}
          </button>
        </div>
        <p class="voice-status">{{ voiceStatus }}</p>
      </section>

      <section class="content-grid">
        <div class="chat-panel glass-panel">
          <div class="panel-title">
            <h2>AI 多轮点歌</h2>
            <span>{{ currentIntent }}</span>
          </div>

          <div ref="messageListRef" class="messages">
            <article
              v-for="message in messages"
              :key="message.id"
              class="message"
              :class="message.role"
            >
              <div class="avatar">{{ message.role === 'user' ? '你' : 'AI' }}</div>
              <div class="bubble">
                <p>{{ message.content }}</p>

                <div v-if="message.songs?.length" class="song-results">
                  <div v-for="song in message.songs" :key="song.id" class="song-row">
                    <img :src="song.coverUrl" :alt="song.songName" @error="setImageFallback($event, song)" />
                    <div>
                      <strong>{{ song.songName }}</strong>
                      <span>{{ song.singer }} · {{ song.genre || 'KTV' }}</span>
                      <small class="audio-state" :class="{ muted: !hasAudio(song) }">
                        {{ hasAudio(song) ? '网易云可播' : '暂无可靠音源' }}
                      </small>
                    </div>
                    <button title="加入已点" @click="addToQueue(song)">＋</button>
                    <button title="收藏" @click="toggleFavorite(song)">
                      {{ isFavorite(song) ? '★' : '☆' }}
                    </button>
                  </div>
                </div>

                <div v-if="message.playlists?.length" class="playlist-results">
                  <div v-for="playlist in message.playlists" :key="playlist.playlistName" class="playlist-result">
                    <div>
                      <strong>{{ playlist.playlistName }}</strong>
                      <span>{{ playlist.description }}</span>
                    </div>
                    <button @click="addPlaylistSongs(playlist.songs || [])">一键添加</button>
                  </div>
                </div>
              </div>
            </article>
          </div>
        </div>

        <aside class="queue-panel glass-panel">
          <div class="panel-title">
            <h2>已点歌单</h2>
            <span>{{ queue.length }} 首</span>
          </div>

          <div class="queue-actions">
            <input v-model="playlistName" name="playlist-name" aria-label="歌单名" />
            <button title="保存歌单" @click="saveCurrentPlaylist">✓</button>
          </div>

          <div class="queue-list">
            <div
              v-for="(song, index) in queue"
              :key="`${song.id}-${index}`"
              class="queue-item"
              :class="{ playing: currentPlaying?.id === song.id, unavailable: !hasAudio(song) }"
            >
              <span class="queue-index">{{ index + 1 }}</span>
              <div>
                <strong>{{ song.songName }}</strong>
                <small>{{ song.singer }} · {{ hasAudio(song) ? '可播放' : '暂无音源' }}</small>
              </div>
              <button
                :title="hasAudio(song) ? '播放' : '暂无可靠音源'"
                :disabled="!hasAudio(song)"
                @click="playQueueSong(index)"
              >
                ▶
              </button>
              <button title="置顶" @click="moveToTop(index)">↑</button>
              <button title="删除" @click="removeFromQueue(index)">×</button>
            </div>
            <p v-if="queue.length === 0" class="empty-text">对 AI 说一句想唱什么，歌曲会出现在这里。</p>
          </div>

          <MusicPlayer
            ref="musicPlayerRef"
            @remove-song="removeSongFromQueue"
            @current-change="currentPlaying = $event"
          />

          <div class="saved-playlists">
            <div class="panel-title compact">
              <h2>我的歌单</h2>
              <button title="新建空歌单" @click="createEmptyPlaylist">＋</button>
            </div>
            <div v-for="playlist in savedPlaylists" :key="playlist.id" class="saved-card">
              <input v-model="playlist.name" name="saved-playlist-name" @change="persistSavedPlaylists" />
              <small>{{ playlist.songs.length }} 首 · {{ playlist.scene }}</small>
              <div>
                <button @click="loadSavedPlaylist(playlist)">载入</button>
                <button @click="deleteSavedPlaylist(playlist.id)">删除</button>
              </div>
            </div>
          </div>
        </aside>
      </section>

      <section class="scene-strip">
        <button
          v-for="scene in scenes"
          :key="scene.name"
          class="scene-chip glass-panel"
          @click="askScene(scene.name)"
        >
          <span>{{ scene.icon }}</span>
          <strong>{{ scene.name }}</strong>
          <small>{{ scene.tip }}</small>
        </button>
      </section>
    </main>

  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, ref } from 'vue'
import { chatApi } from '../api/chat'
import { songApi } from '../api/song'
import { speechRecognition } from '../utils/speech'
import MusicPlayer from '../components/MusicPlayer.vue'

const STORAGE_KEYS = {
  queue: 'ktv-ai-queue',
  favorites: 'ktv-ai-favorites',
  history: 'ktv-ai-history',
  playlists: 'ktv-ai-playlists',
  session: 'ktv-ai-session',
  dataVersion: 'ktv-ai-data-version'
}

const DATA_VERSION = 'music-api-v5-strict-netease'

const inputMessage = ref('')
const isListening = ref(false)
const voiceStatus = ref('点击左侧圆点开始语音点歌')
const messages = ref([
  {
    id: Date.now(),
    role: 'ai',
    content: '今晚想唱什么？可以直接说歌手、情绪、场景，模糊一点也没关系，我会追问到能精准点歌。',
    songs: [],
    playlists: []
  }
])
const queue = ref([])
const favorites = ref([])
const history = ref([])
const savedPlaylists = ref([])
const playlistName = ref('今晚已点')
const currentIntent = ref('待命')
const currentPlaying = ref(null)
const musicPlayerRef = ref(null)
const messageListRef = ref(null)
const sessionId = ref(localStorage.getItem(STORAGE_KEYS.session) || `web-${Date.now()}`)
let recognition = null
let mediaRecorder = null
let audioChunks = []
let usingRecorderFallback = false
let recorderStopTimer = null

const quickPrompts = [
  '来首周杰伦的慢歌',
  '我要唱适合告白的情歌',
  '来点嗨的，适合聚会蹦迪',
  '给我找最近很火的粤语歌'
]

const scenes = [
  { name: '生日局', icon: '🎂', tip: '开场到合唱' },
  { name: '朋友聚会', icon: '🍻', tip: '热闹不冷场' },
  { name: '车载路上听', icon: '🚗', tip: '轻松耐听' },
  { name: '情侣对唱', icon: '♡', tip: '甜歌合唱' },
  { name: '公司团建暖场', icon: '🏢', tip: '熟悉好唱' },
  { name: '经典老歌', icon: '📻', tip: '麦霸回忆杀' }
]

const preferenceTags = computed(() => {
  const singers = queue.value.concat(favorites.value).map(song => song.singer).filter(Boolean)
  const genres = queue.value.concat(favorites.value).map(song => song.genre).filter(Boolean)
  const top = [...new Set([...singers.slice(-4), ...genres.slice(-4)])]
  return top.length ? top.slice(0, 6) : ['华语流行', '情歌', 'KTV 热门']
})

onMounted(async () => {
  localStorage.setItem(STORAGE_KEYS.session, sessionId.value)
  if (localStorage.getItem(STORAGE_KEYS.dataVersion) !== DATA_VERSION) {
    localStorage.removeItem(STORAGE_KEYS.queue)
    localStorage.removeItem(STORAGE_KEYS.favorites)
    localStorage.removeItem(STORAGE_KEYS.playlists)
    localStorage.setItem(STORAGE_KEYS.dataVersion, DATA_VERSION)
  }
  queue.value = sanitizeSongs(readStorage(STORAGE_KEYS.queue, []))
  favorites.value = sanitizeSongs(readStorage(STORAGE_KEYS.favorites, []))
  history.value = readStorage(STORAGE_KEYS.history, [])
  savedPlaylists.value = readStorage(STORAGE_KEYS.playlists, []).map(playlist => ({
    ...playlist,
    songs: sanitizeSongs(playlist.songs || [])
  }))
  persistQueue()
  localStorage.setItem(STORAGE_KEYS.favorites, JSON.stringify(favorites.value))
  persistSavedPlaylists()
  syncPlayerQueue()

  if (queue.value.length === 0) {
    try {
      const res = await songApi.hot(4)
      queue.value = sanitizeSongs(res.data.data || [])
      persistQueue()
      syncPlayerQueue()
    } catch (error) {
      console.error('加载默认热门歌曲失败', error)
    }
  }
})

const sendMessage = async () => {
  const text = inputMessage.value.trim()
  if (!text) return
  inputMessage.value = ''
  pushHistory(text)
  messages.value.push({ id: Date.now(), role: 'user', content: text })
  await scrollMessages()

  try {
    const res = await chatApi.chat({ sessionId: sessionId.value, userInput: text })
    const data = res.data.data || {}
    currentIntent.value = `${intentLabel(data.intentType, data.clarification)} · ${data.aiProvider || 'AI'}`
    const songs = sanitizeSongs(data.songs || [])
    const playlists = sanitizePlaylists(data.playlists || [])
    messages.value.push({
      id: Date.now() + 1,
      role: 'ai',
      content: data.reply || '我先帮你找几首稳的。',
      songs,
      playlists
    })
    if (songs.length) {
      songs.slice(0, 3).forEach(addToQueue)
    }
  } catch (error) {
    console.error('AI 点歌失败，降级到本地搜索', error)
    await localFallback(text)
  }

  await scrollMessages()
}

const localFallback = async (text) => {
  const res = await songApi.search(text)
  const songs = sanitizeSongs(res.data.data || [])
  messages.value.push({
    id: Date.now() + 2,
    role: 'ai',
    content: songs.length ? '先用本地曲库给你顶上，这几首适合马上开唱。' : '本地曲库暂时没命中，换个歌手、语言或场景试试。',
    songs,
    playlists: []
  })
  songs.slice(0, 3).forEach(addToQueue)
}

const quickAsk = (text) => {
  inputMessage.value = text
  sendMessage()
}

const askScene = (scene) => {
  quickAsk(`生成一套${scene}场景歌单`)
}

const generatePersonalMix = () => {
  const tags = preferenceTags.value.join('、')
  quickAsk(`根据我喜欢的${tags}，推荐一套适合今晚 KTV 的歌单`)
}

const addToQueue = (song) => {
  const cleanSong = sanitizeSong(song)
  if (!cleanSong || queue.value.some(item => item.id === cleanSong.id)) return
  queue.value.push(cleanSong)
  persistQueue()
  musicPlayerRef.value?.addToPlaylist(cleanSong)
}

const addPlaylistSongs = (songs) => {
  songs.forEach(addToQueue)
}

const removeFromQueue = (index) => {
  queue.value.splice(index, 1)
  persistQueue()
  syncPlayerQueue()
}

const moveToTop = (index) => {
  const song = queue.value.splice(index, 1)[0]
  queue.value.unshift(song)
  persistQueue()
  syncPlayerQueue()
}

const playQueueSong = (index) => {
  syncPlayerQueue(index, true)
}

const syncPlayerQueue = (index = 0, autoplay = false) => {
  nextTick(() => {
    musicPlayerRef.value?.setPlaylist(queue.value, { index, autoplay })
  })
}

const removeSongFromQueue = (song) => {
  queue.value = queue.value.filter(item => String(item.id) !== String(song.id))
  persistQueue()
}

const toggleFavorite = (song) => {
  const index = favorites.value.findIndex(item => item.id === song.id)
  if (index >= 0) {
    favorites.value.splice(index, 1)
  } else {
    favorites.value.unshift(song)
  }
  localStorage.setItem(STORAGE_KEYS.favorites, JSON.stringify(favorites.value))
}

const isFavorite = (song) => favorites.value.some(item => item.id === song.id)

const hasAudio = (song) => Boolean(song?.audioUrl)

const saveCurrentPlaylist = () => {
  if (queue.value.length === 0) return
  savedPlaylists.value.unshift({
    id: Date.now(),
    name: playlistName.value || '未命名歌单',
    scene: currentIntent.value,
    songs: sanitizeSongs(queue.value)
  })
  persistSavedPlaylists()
}

const createEmptyPlaylist = () => {
  savedPlaylists.value.unshift({
    id: Date.now(),
    name: '新建歌单',
    scene: '手动创建',
    songs: []
  })
  persistSavedPlaylists()
}

const loadSavedPlaylist = (playlist) => {
  queue.value = sanitizeSongs(playlist.songs)
  playlistName.value = playlist.name
  persistQueue()
  syncPlayerQueue(0, false)
}

const deleteSavedPlaylist = (id) => {
  savedPlaylists.value = savedPlaylists.value.filter(item => item.id !== id)
  persistSavedPlaylists()
}

const resetChat = () => {
  messages.value = [{
    id: Date.now(),
    role: 'ai',
    content: '新的包间已准备好。说一句你想唱什么，我来配。',
    songs: [],
    playlists: []
  }]
  currentIntent.value = '待命'
}

const toggleListening = () => {
  if (isListening.value) {
    speechRecognition.stop(recognition)
    stopRecorderFallback()
    isListening.value = false
    voiceStatus.value = '语音已停止'
    return
  }

  if (!speechRecognition.isSupported()) {
    startRecorderFallback()
    return
  }

  isListening.value = true
  voiceStatus.value = '正在听你说话...'
  recognition = speechRecognition.start(
    (result) => {
      voiceStatus.value = `识别到：${result}`
      inputMessage.value = result
      sendMessage()
    },
    (error) => {
      isListening.value = false
      startRecorderFallback()
    },
    () => {
      if (!usingRecorderFallback) {
        isListening.value = false
      }
    }
  )
}

const startRecorderFallback = async () => {
  if (!navigator.mediaDevices?.getUserMedia) {
    voiceStatus.value = '当前浏览器拿不到麦克风能力'
    messages.value.push({
      id: Date.now(),
      role: 'ai',
      content: '当前浏览器拿不到麦克风能力，可以先用文字告诉我想唱什么。',
      songs: [],
      playlists: []
    })
    return
  }

  try {
    usingRecorderFallback = true
    isListening.value = true
    voiceStatus.value = '浏览器语音识别不可用，正在录音 5 秒后自动识别...'
    const stream = await navigator.mediaDevices.getUserMedia({
      audio: {
        sampleRate: 16000,
        channelCount: 1,
        echoCancellation: true,
        noiseSuppression: true
      }
    })
    mediaRecorder = new MediaRecorder(stream)
    audioChunks = []

    mediaRecorder.ondataavailable = (event) => {
      if (event.data.size > 0) audioChunks.push(event.data)
    }

    mediaRecorder.onstop = async () => {
      isListening.value = false
      usingRecorderFallback = false
      clearTimeout(recorderStopTimer)
      voiceStatus.value = '语音录制完成，正在识别...'
      stream.getTracks().forEach(track => track.stop())
      if (audioChunks.length === 0) {
        voiceStatus.value = '没有录到声音'
        return
      }

      const audioBlob = new Blob(audioChunks, { type: mediaRecorder.mimeType || 'audio/webm' })
      const base64Audio = await blobToBase64(audioBlob)
      try {
        const response = await fetch('/api/speech/recognize', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ audio: base64Audio })
        })
        const data = await response.json()
        const text = data?.data?.text
        if (text) {
          voiceStatus.value = `识别到：${text}`
          inputMessage.value = text
          sendMessage()
        } else {
          voiceStatus.value = '没有听清，可以再试一次'
          messages.value.push({
            id: Date.now(),
            role: 'ai',
            content: '这段语音我没听清，可以再说一次，或者直接打字给我。',
            songs: [],
            playlists: []
          })
        }
      } catch (error) {
        voiceStatus.value = '语音识别服务暂时不可用'
        messages.value.push({
          id: Date.now(),
          role: 'ai',
          content: '语音识别服务暂时不可用，可以先用文字点歌。',
          songs: [],
          playlists: []
        })
      }
    }

    mediaRecorder.start()
    recorderStopTimer = setTimeout(() => stopRecorderFallback(), 5000)
  } catch (error) {
    isListening.value = false
    usingRecorderFallback = false
    voiceStatus.value = '麦克风权限未打开'
    messages.value.push({
      id: Date.now(),
      role: 'ai',
      content: '麦克风权限没有打开，可以在浏览器地址栏允许麦克风，或者先用文字点歌。',
      songs: [],
      playlists: []
    })
  }
}

const stopRecorderFallback = () => {
  clearTimeout(recorderStopTimer)
  if (mediaRecorder && mediaRecorder.state !== 'inactive') {
    mediaRecorder.stop()
  } else {
    usingRecorderFallback = false
  }
}

const blobToBase64 = (blob) => new Promise((resolve, reject) => {
  const reader = new FileReader()
  reader.onloadend = () => resolve(String(reader.result).split(',')[1] || '')
  reader.onerror = reject
  reader.readAsDataURL(blob)
})

const pushHistory = (text) => {
  history.value = [
    { id: Date.now(), text, time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }) },
    ...history.value.filter(item => item.text !== text)
  ].slice(0, 20)
  localStorage.setItem(STORAGE_KEYS.history, JSON.stringify(history.value))
}

const persistQueue = () => localStorage.setItem(STORAGE_KEYS.queue, JSON.stringify(queue.value))
const persistSavedPlaylists = () => localStorage.setItem(STORAGE_KEYS.playlists, JSON.stringify(savedPlaylists.value))
const readStorage = (key, fallback) => {
  try {
    return JSON.parse(localStorage.getItem(key)) || fallback
  } catch {
    return fallback
  }
}

const sanitizePlaylists = (playlists) => playlists.map(playlist => ({
  ...playlist,
  songs: sanitizeSongs(playlist.songs || [])
}))

const sanitizeSongs = (songs) => songs.map(sanitizeSong).filter(Boolean)

const sanitizeSong = (song) => {
  if (!song) return null
  const safeSong = { ...song }
  if (!isSafeCover(safeSong.coverUrl)) {
    safeSong.coverUrl = coverFallback(safeSong)
  }
  return safeSong
}

const isSafeCover = (url) => {
  if (!url) return false
  return !url.includes('neeko-copilot') && !url.includes('text_to_image')
}

const coverFallback = (song) => {
  const title = encodeURIComponent((song?.songName || 'KTV').slice(0, 12))
  const artist = encodeURIComponent((song?.singer || 'AI').slice(0, 16))
  return `data:image/svg+xml;charset=UTF-8,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 300 300'%3E%3Cdefs%3E%3ClinearGradient id='g' x1='0' x2='1' y1='0' y2='1'%3E%3Cstop stop-color='%23ff5c7a'/%3E%3Cstop offset='1' stop-color='%2336d1dc'/%3E%3C/linearGradient%3E%3C/defs%3E%3Crect width='300' height='300' rx='32' fill='%23111318'/%3E%3Ccircle cx='220' cy='58' r='74' fill='url(%23g)' opacity='.55'/%3E%3Ccircle cx='68' cy='232' r='86' fill='url(%23g)' opacity='.25'/%3E%3Ctext x='28' y='142' fill='%23ffffff' font-size='30' font-family='Arial' font-weight='700'%3E${title}%3C/text%3E%3Ctext x='28' y='184' fill='%23ffffff' opacity='.68' font-size='20' font-family='Arial'%3E${artist}%3C/text%3E%3C/svg%3E`
}

const setImageFallback = (event, song) => {
  const fallback = coverFallback(song)
  if (event.target.src !== fallback) {
    event.target.src = fallback
  }
}

const intentLabel = (type, clarification) => {
  if (clarification) return '正在追问'
  const map = {
    SONG_SEARCH: '自然语言点歌',
    SCENE_PLAYLIST: '场景歌单',
    GENRE_SEARCH: '曲风推荐',
    CHAT: '闲聊'
  }
  return map[type] || 'AI 推荐'
}

const scrollMessages = async () => {
  await nextTick()
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}
</script>

<style scoped>
.ktv-shell {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 18px;
  padding: 18px;
  color: rgba(255, 255, 255, 0.92);
}

.glass-panel {
  background: rgba(30, 32, 38, 0.58);
  border: 1px solid rgba(255, 255, 255, 0.12);
  box-shadow: 0 24px 70px rgba(0, 0, 0, 0.34);
  backdrop-filter: blur(28px) saturate(135%);
  -webkit-backdrop-filter: blur(28px) saturate(135%);
  border-radius: 24px;
}

.side-panel {
  position: sticky;
  top: 18px;
  height: calc(100vh - 36px);
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-mark {
  width: 42px;
  height: 42px;
  display: grid;
  place-items: center;
  border-radius: 14px;
  background: linear-gradient(135deg, #ff5c7a, #36d1dc);
  font-weight: 800;
}

.brand strong,
.brand span {
  display: block;
}

.brand span,
.eyebrow,
.empty-text,
.history-item small,
.scene-chip small,
.queue-item small,
.saved-card small {
  color: rgba(255, 255, 255, 0.52);
}

.side-nav {
  display: flex;
  gap: 10px;
}

.nav-btn,
.icon-btn,
.mic-button,
.send-button,
.song-row button,
.queue-item button,
.saved-card button,
.compact button,
.queue-actions button {
  border: 1px solid rgba(255, 255, 255, 0.12);
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
  cursor: pointer;
  transition: transform 0.2s ease, background 0.2s ease;
}

.nav-btn {
  width: 42px;
  height: 42px;
  display: grid;
  place-items: center;
  border-radius: 14px;
  text-decoration: none;
}

.nav-btn.active,
.nav-btn:hover,
.icon-btn:hover,
.send-button:hover,
.queue-actions button:hover {
  background: rgba(255, 255, 255, 0.18);
  transform: translateY(-1px);
}

.mini-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.eyebrow {
  margin: 0;
  font-size: 12px;
  text-transform: uppercase;
}

.preference-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.preference-tags span,
.quick-prompts button {
  border-radius: 999px;
  padding: 8px 11px;
  background: rgba(255, 255, 255, 0.09);
  color: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.history {
  overflow: hidden;
}

.history-item {
  text-align: left;
  border: 0;
  background: rgba(255, 255, 255, 0.05);
  color: #fff;
  border-radius: 14px;
  padding: 10px;
  cursor: pointer;
}

.history-item span,
.history-item small {
  display: block;
}

.workspace {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.topbar {
  min-height: 112px;
  padding: 24px 28px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.topbar h1 {
  margin: 6px 0 0;
  font-size: 34px;
  font-weight: 750;
  letter-spacing: 0;
}

.top-actions {
  display: flex;
  gap: 10px;
}

.icon-btn {
  width: 42px;
  height: 42px;
  border-radius: 50%;
}

.primary-btn {
  border: 0;
  border-radius: 14px;
  padding: 0 18px;
  color: #111318;
  font-weight: 700;
  background: linear-gradient(135deg, #fff, #8be9fd);
  cursor: pointer;
}

.command-card {
  padding: 14px;
}

.prompt-row {
  display: grid;
  grid-template-columns: 54px 1fr 54px;
  gap: 12px;
  align-items: center;
}

.mic-button,
.send-button {
  width: 54px;
  height: 54px;
  border-radius: 50%;
  font-size: 18px;
}

.mic-button.listening {
  background: rgba(255, 92, 122, 0.82);
  box-shadow: 0 0 0 8px rgba(255, 92, 122, 0.16);
}

textarea {
  width: 100%;
  resize: none;
  border: 0;
  outline: 0;
  min-height: 54px;
  padding: 15px 4px;
  background: transparent;
  color: #fff;
  font-size: 17px;
  line-height: 1.45;
}

textarea::placeholder {
  color: rgba(255, 255, 255, 0.38);
}

.quick-prompts {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 10px 66px 0;
}

.quick-prompts button {
  cursor: pointer;
}

.voice-status {
  margin: 10px 66px 0;
  color: rgba(255, 255, 255, 0.5);
  font-size: 13px;
}

.content-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 18px;
}

.chat-panel,
.queue-panel {
  min-height: 550px;
  padding: 18px;
}

.panel-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.panel-title h2 {
  margin: 0;
  font-size: 18px;
  letter-spacing: 0;
}

.panel-title span {
  color: rgba(139, 233, 253, 0.9);
  font-size: 13px;
}

.messages {
  height: 488px;
  overflow-y: auto;
  padding-right: 4px;
}

.message {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.message.user {
  flex-direction: row-reverse;
}

.avatar {
  flex: 0 0 38px;
  height: 38px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  font-size: 13px;
}

.bubble {
  max-width: min(680px, 86%);
  padding: 14px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.08);
}

.user .bubble {
  background: rgba(139, 233, 253, 0.16);
}

.bubble p {
  margin: 0;
  line-height: 1.7;
  white-space: pre-line;
}

.song-results,
.playlist-results,
.queue-list,
.saved-playlists {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 12px;
}

.song-row,
.queue-item {
  display: grid;
  grid-template-columns: 44px 1fr 34px 34px;
  gap: 10px;
  align-items: center;
  padding: 10px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.07);
}

.song-row img {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  object-fit: cover;
}

.song-row strong,
.song-row span,
.queue-item strong,
.queue-item small {
  display: block;
}

.song-row span {
  margin-top: 3px;
  color: rgba(255, 255, 255, 0.55);
  font-size: 12px;
}

.song-row .audio-state {
  display: inline-flex;
  width: fit-content;
  margin-top: 6px;
  padding: 3px 7px;
  border-radius: 999px;
  color: #a9f3ff;
  background: rgba(139, 233, 253, 0.12);
  font-size: 11px;
  font-weight: 700;
}

.song-row .audio-state.muted {
  color: rgba(255, 255, 255, 0.52);
  background: rgba(255, 255, 255, 0.08);
}

.song-row button,
.queue-item button,
.saved-card button,
.compact button,
.queue-actions button {
  height: 32px;
  border-radius: 50%;
}

.playlist-result,
.saved-card {
  padding: 12px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.07);
}

.playlist-result {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.playlist-result span {
  display: block;
  margin-top: 4px;
  color: rgba(255, 255, 255, 0.55);
  font-size: 13px;
}

.playlist-result button {
  border: 0;
  border-radius: 12px;
  padding: 9px 12px;
  white-space: nowrap;
  color: #111318;
  background: #8be9fd;
  cursor: pointer;
}

.queue-actions {
  display: grid;
  grid-template-columns: 1fr 38px;
  gap: 8px;
  margin-bottom: 12px;
}

.queue-actions input,
.saved-card input {
  width: 100%;
  border: 0;
  border-radius: 12px;
  padding: 10px 12px;
  outline: 0;
  color: #fff;
  background: rgba(255, 255, 255, 0.08);
}

.queue-list {
  max-height: 276px;
  overflow-y: auto;
  margin-top: 0;
}

.queue-item {
  grid-template-columns: 28px minmax(0, 1fr) 32px 32px 32px;
}

.queue-item.playing {
  background: rgba(139, 233, 253, 0.15);
}

.queue-item.unavailable {
  opacity: 0.72;
}

.queue-item button:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}

.queue-index {
  color: rgba(255, 255, 255, 0.46);
}

.compact {
  margin-top: 18px;
}

.saved-card {
  display: grid;
  gap: 8px;
}

.saved-card div {
  display: flex;
  gap: 8px;
}

.saved-card button {
  width: auto;
  padding: 0 12px;
  border-radius: 12px;
}

.scene-strip {
  display: grid;
  grid-template-columns: repeat(6, minmax(120px, 1fr));
  gap: 12px;
  padding-bottom: 104px;
}

.scene-chip {
  min-height: 118px;
  padding: 16px;
  text-align: left;
  color: #fff;
  cursor: pointer;
}

.scene-chip span,
.scene-chip strong,
.scene-chip small {
  display: block;
}

.scene-chip span {
  font-size: 28px;
  margin-bottom: 14px;
}

.scene-chip strong {
  font-size: 15px;
  margin-bottom: 5px;
}

@media (max-width: 1100px) {
  .ktv-shell,
  .content-grid {
    grid-template-columns: 1fr;
  }

  .side-panel {
    position: static;
    height: auto;
  }

  .scene-strip {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 720px) {
  .ktv-shell {
    padding: 10px;
  }

  .topbar,
  .prompt-row,
  .playlist-result {
    align-items: stretch;
  }

  .topbar {
    flex-direction: column;
    gap: 14px;
  }

  .topbar h1 {
    font-size: 26px;
  }

  .prompt-row {
    grid-template-columns: 44px 1fr 44px;
  }

  .mic-button,
  .send-button {
    width: 44px;
    height: 44px;
  }

  .quick-prompts {
    padding: 10px 0 0;
  }

  .scene-strip {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
