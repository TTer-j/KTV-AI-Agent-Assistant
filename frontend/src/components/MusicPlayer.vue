<template>
  <section class="player-panel">
    <div class="player-head">
      <div>
        <p class="eyebrow">Music Player</p>
        <h3>网易云音乐源</h3>
      </div>
      <span class="source-badge">NETEASE</span>
    </div>

    <div class="now-playing">
      <img
        class="cover"
        :src="currentSong?.coverUrl || emptyCover"
        :alt="currentSong?.songName || '未播放'"
        @error="setImageFallback"
      />
      <div class="song-meta">
        <h3>{{ currentSong?.songName || '未选择歌曲' }}</h3>
        <span>{{ currentSong?.singer || '从已点歌单选择一首歌播放' }}</span>
        <small>{{ currentSong?.album || '点击队列歌曲开始播放' }}</small>
      </div>
    </div>

    <div class="progress-wrap">
      <span>{{ formatTime(currentTime) }}</span>
      <div class="progress-bar" @click="seek">
        <div class="progress" :style="{ width: progressPercent + '%' }"></div>
      </div>
      <span>{{ formatTime(duration) }}</span>
    </div>

    <div class="control-deck">
      <button title="上一首" @click="playPrev">⏮</button>
      <button class="play-toggle" title="播放/暂停" @click="togglePlay">
        {{ isPlaying ? '⏸' : '▶' }}
      </button>
      <button title="下一首" @click="playNext">⏭</button>
      <label class="volume-row">
        <span>音量</span>
        <input
          type="range"
          min="0"
          max="100"
          :value="volume * 100"
          @input="changeVolume"
        />
      </label>
    </div>

    <p v-if="playerNotice" class="notice">{{ playerNotice }}</p>

    <div class="queue-header">
      <h3>播放队列</h3>
      <span>{{ playlist.length }} 首</span>
    </div>

    <div class="playlist-content">
      <button
        v-for="(song, index) in playlist"
        :key="song.id || `${song.songName}-${index}`"
        class="playlist-item"
        :class="{ active: currentIndex === index, unavailable: !hasAudio(song) }"
        @click="playSong(index)"
      >
        <span class="index">{{ index + 1 }}</span>
        <span class="track-name">{{ song.songName }}</span>
        <span class="track-singer">{{ hasAudio(song) ? song.singer : '暂无音源' }}</span>
        <span class="remove-btn" @click.stop="removeFromPlaylist(index)">×</span>
      </button>
      <p v-if="playlist.length === 0" class="empty">还没有歌曲，先让 AI 推荐几首。</p>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'

const emit = defineEmits(['remove-song', 'current-change'])

const currentSong = ref(null)
const isPlaying = ref(false)
const currentTime = ref(0)
const duration = ref(0)
const volume = ref(0.7)
const currentIndex = ref(-1)
const playlist = ref([])
const playerNotice = ref('')
let playRequestId = 0

const audio = new Audio()
const emptyCover = coverFallback({ songName: 'KTV AI', singer: '智能点歌' })

const progressPercent = computed(() => {
  if (!duration.value) return 0
  return Math.min(100, (currentTime.value / duration.value) * 100)
})

const formatTime = (seconds) => {
  const safeSeconds = Number.isFinite(seconds) ? seconds : 0
  const mins = Math.floor(safeSeconds / 60)
  const secs = Math.floor(safeSeconds % 60)
  return `${mins}:${secs.toString().padStart(2, '0')}`
}

const playSong = async (index) => {
  if (index < 0 || index >= playlist.value.length) return
  const requestId = ++playRequestId
  currentIndex.value = index
  currentSong.value = playlist.value[index]
  playerNotice.value = ''
  currentTime.value = 0
  emit('current-change', currentSong.value)

  if (!currentSong.value.audioUrl) {
    audio.pause()
    audio.removeAttribute('src')
    isPlaying.value = false
    playerNotice.value = '这首歌暂无可播放音频'
    return
  }

  audio.pause()
  if (audio.src !== currentSong.value.audioUrl) {
    audio.src = currentSong.value.audioUrl
    audio.load()
  }

  try {
    await audio.play()
    if (requestId !== playRequestId) return
    isPlaying.value = true
  } catch (err) {
    if (requestId !== playRequestId || err?.name === 'AbortError') return
    isPlaying.value = false
    playerNotice.value = '播放失败，这个音频地址可能不可用'
  }
}

const togglePlay = () => {
  if (!currentSong.value) {
    playSong(playlist.value.length ? 0 : -1)
    return
  }

  if (isPlaying.value) {
    audio.pause()
    isPlaying.value = false
    return
  }

  if (!audio.src && currentIndex.value >= 0) {
    playSong(currentIndex.value)
    return
  }

  audio.play().then(() => {
    isPlaying.value = true
  }).catch(err => {
    if (err?.name === 'AbortError') return
    isPlaying.value = false
    playerNotice.value = '播放失败，换一首试试'
  })
}

const playPrev = () => {
  if (!playlist.value.length) return
  const newIndex = findPlayableIndex(-1)
  playSong(newIndex)
}

const playNext = () => {
  if (!playlist.value.length) return
  const newIndex = findPlayableIndex(1)
  playSong(newIndex)
}

const findPlayableIndex = (direction) => {
  const total = playlist.value.length
  const startIndex = currentIndex.value < 0 ? 0 : currentIndex.value
  for (let step = 1; step <= total; step += 1) {
    const candidate = (startIndex + direction * step + total) % total
    if (hasAudio(playlist.value[candidate])) {
      return candidate
    }
  }
  return currentIndex.value < 0 ? 0 : currentIndex.value
}

const seek = (event) => {
  if (!duration.value) return
  const rect = event.currentTarget.getBoundingClientRect()
  const percent = Math.min(1, Math.max(0, (event.clientX - rect.left) / rect.width))
  audio.currentTime = percent * duration.value
}

const changeVolume = (event) => {
  volume.value = Number(event.target.value) / 100
  audio.volume = volume.value
}

const addToPlaylist = (song) => {
  const safeSong = sanitizeSong(song)
  const existsIndex = playlist.value.findIndex(item => String(item.id) === String(safeSong.id))
  if (existsIndex >= 0) {
    currentIndex.value = existsIndex
    currentSong.value = playlist.value[existsIndex]
    emit('current-change', currentSong.value)
    return
  }

  playlist.value.push(safeSong)
  if (!currentSong.value) {
    currentIndex.value = 0
    currentSong.value = playlist.value[0]
    emit('current-change', currentSong.value)
  }
}

const setPlaylist = (songs, options = {}) => {
  playlist.value = songs.map(sanitizeSong)
  if (playlist.value.length === 0) {
    clearPlaylist()
    return
  }

  const nextIndex = Math.min(Math.max(options.index ?? currentIndex.value, 0), playlist.value.length - 1)
  currentIndex.value = nextIndex
  currentSong.value = playlist.value[nextIndex]
  emit('current-change', currentSong.value)

  if (options.autoplay) {
    playSong(nextIndex)
  }
}

const removeFromPlaylist = (index) => {
  if (index < 0 || index >= playlist.value.length) return
  const removed = playlist.value[index]
  playlist.value.splice(index, 1)
  emit('remove-song', removed)

  if (!playlist.value.length) {
    clearPlaylist()
    return
  }

  if (index === currentIndex.value) {
    const nextIndex = Math.min(index, playlist.value.length - 1)
    playSong(nextIndex)
  } else if (index < currentIndex.value) {
    currentIndex.value -= 1
  }
}

const clearPlaylist = () => {
  playlist.value = []
  currentSong.value = null
  currentIndex.value = -1
  currentTime.value = 0
  duration.value = 0
  isPlaying.value = false
  playerNotice.value = ''
  audio.pause()
  audio.removeAttribute('src')
  emit('current-change', null)
}

const sanitizeSong = (song) => {
  const safeSong = { ...song }
  if (!safeSong.coverUrl || safeSong.coverUrl.includes('neeko-copilot') || safeSong.coverUrl.includes('text_to_image')) {
    safeSong.coverUrl = coverFallback(safeSong)
  }
  return safeSong
}

const hasAudio = (song) => Boolean(song?.audioUrl)

function coverFallback(song) {
  const title = encodeURIComponent((song?.songName || 'KTV').slice(0, 12))
  const artist = encodeURIComponent((song?.singer || 'AI').slice(0, 16))
  return `data:image/svg+xml;charset=UTF-8,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 300 300'%3E%3Cdefs%3E%3ClinearGradient id='g' x1='0' x2='1' y1='0' y2='1'%3E%3Cstop stop-color='%23ff5c7a'/%3E%3Cstop offset='1' stop-color='%2336d1dc'/%3E%3C/linearGradient%3E%3C/defs%3E%3Crect width='300' height='300' rx='32' fill='%23111318'/%3E%3Ccircle cx='220' cy='58' r='74' fill='url(%23g)' opacity='.55'/%3E%3Ccircle cx='68' cy='232' r='86' fill='url(%23g)' opacity='.25'/%3E%3Ctext x='28' y='142' fill='%23ffffff' font-size='30' font-family='Arial' font-weight='700'%3E${title}%3C/text%3E%3Ctext x='28' y='184' fill='%23ffffff' opacity='.68' font-size='20' font-family='Arial'%3E${artist}%3C/text%3E%3C/svg%3E`
}

const setImageFallback = (event) => {
  const fallback = coverFallback(currentSong.value)
  if (event.target.src !== fallback) {
    event.target.src = fallback
  }
}

audio.addEventListener('timeupdate', () => {
  currentTime.value = audio.currentTime
})

audio.addEventListener('loadedmetadata', () => {
  duration.value = audio.duration || 0
})

audio.addEventListener('ended', () => {
  playNext()
})

audio.addEventListener('error', () => {
  isPlaying.value = false
  playerNotice.value = '音频地址不可用'
})

onMounted(() => {
  audio.volume = volume.value
})

onUnmounted(() => {
  audio.pause()
  audio.removeAttribute('src')
})

defineExpose({
  addToPlaylist,
  setPlaylist,
  playSong,
  togglePlay,
  playPrev,
  playNext,
  clearPlaylist,
  playlist,
  currentSong,
  currentIndex
})
</script>

<style scoped>
.player-panel {
  margin-top: 18px;
  padding: 16px;
  border-radius: 18px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.105), rgba(255, 255, 255, 0.055));
  border: 1px solid rgba(255, 255, 255, 0.12);
}

.player-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.player-head h3 {
  margin: 0;
  font-size: 16px;
  color: #fff;
}

.source-badge {
  padding: 5px 9px;
  border-radius: 999px;
  background: rgba(255, 77, 79, 0.16);
  color: #ff8f91;
  font-size: 11px;
  font-weight: 800;
}

.now-playing {
  display: grid;
  grid-template-columns: 76px 1fr;
  gap: 12px;
  align-items: center;
}

.cover {
  width: 76px;
  height: 76px;
  border-radius: 12px;
  object-fit: cover;
  box-shadow: 0 14px 34px rgba(0, 0, 0, 0.3);
}

.song-meta {
  min-width: 0;
}

.eyebrow {
  margin: 0 0 4px;
  color: rgba(139, 233, 253, 0.88);
  font-size: 12px;
  text-transform: uppercase;
}

.song-meta h3 {
  margin: 0;
  font-size: 18px;
  color: #fff;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.song-meta span {
  display: block;
  margin-top: 6px;
  color: rgba(255, 255, 255, 0.56);
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.song-meta small {
  display: block;
  margin-top: 6px;
  color: rgba(255, 255, 255, 0.38);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.progress-wrap {
  display: grid;
  grid-template-columns: 42px 1fr 42px;
  gap: 10px;
  align-items: center;
  margin-top: 18px;
  color: rgba(255, 255, 255, 0.46);
  font-size: 12px;
}

.progress-bar {
  height: 6px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  cursor: pointer;
  overflow: hidden;
}

.progress {
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #ff5c7a, #8be9fd);
}

.control-deck {
  display: grid;
  grid-template-columns: 42px 52px 42px minmax(0, 1fr);
  gap: 10px;
  align-items: center;
  margin-top: 14px;
}

.control-deck button,
.remove-btn {
  border: 1px solid rgba(255, 255, 255, 0.12);
  color: #fff;
  background: rgba(255, 255, 255, 0.08);
  cursor: pointer;
}

.control-deck button {
  width: 42px;
  height: 42px;
  border-radius: 50%;
}

.control-deck .play-toggle {
  width: 52px;
  height: 52px;
  background: #ff4d4f;
  color: #fff;
  font-weight: 800;
}

.volume-row {
  display: grid;
  grid-template-columns: 34px 1fr;
  gap: 8px;
  align-items: center;
  color: rgba(255, 255, 255, 0.48);
  font-size: 12px;
}

.volume-row input {
  width: 100%;
  accent-color: #8be9fd;
}

.notice {
  margin: 10px 0 0;
  color: rgba(255, 255, 255, 0.58);
  font-size: 12px;
  text-align: center;
}

.queue-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 18px;
}

.queue-header h3 {
  margin: 0;
  font-size: 16px;
}

.queue-header span {
  color: rgba(139, 233, 253, 0.9);
  font-size: 13px;
}

.playlist-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 12px;
  max-height: 220px;
  overflow-y: auto;
}

.playlist-item {
  display: grid;
  grid-template-columns: 28px minmax(0, 1fr) minmax(72px, 96px) 28px;
  gap: 8px;
  align-items: center;
  width: 100%;
  padding: 9px;
  border: 0;
  border-radius: 14px;
  color: #fff;
  background: rgba(255, 255, 255, 0.06);
  text-align: left;
  cursor: pointer;
}

.playlist-item.active {
  background: rgba(255, 77, 79, 0.16);
}

.playlist-item.unavailable {
  opacity: 0.64;
}

.index,
.track-singer {
  color: rgba(255, 255, 255, 0.48);
}

.track-name,
.track-singer {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13px;
}

.remove-btn {
  width: 26px;
  height: 26px;
  border-radius: 50%;
}

.empty {
  margin: 0;
  padding: 18px 0;
  color: rgba(255, 255, 255, 0.42);
  text-align: center;
}
</style>
