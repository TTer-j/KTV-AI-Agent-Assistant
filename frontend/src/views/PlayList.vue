<template>
  <div class="playlist">
    <header class="header">
      <router-link to="/" class="back-link">← 返回</router-link>
      <h1 class="title">场景歌单</h1>
      <div class="placeholder"></div>
    </header>

    <main class="main">
      <section class="scene-tags">
        <button 
          v-for="tag in sceneTags" 
          :key="tag" 
          class="tag-btn"
          :class="{ active: selectedTag === tag }"
          @click="selectScene(tag)"
        >
          {{ tag }}
        </button>
      </section>

      <section class="playlist-grid">
        <div 
          v-for="playlist in playlists" 
          :key="playlist.id" 
          class="playlist-card card-glass"
        >
          <div class="playlist-header">
            <h3 class="text-primary">{{ playlist.playlistName }}</h3>
          </div>
          <div class="playlist-content">
            <div class="song-preview">
              <div v-for="(song, index) in getPreviewSongs(playlist)" :key="index" class="preview-item">
                <span class="num">{{ index + 1 }}</span>
                <span class="text-secondary">{{ song.songName }}</span>
              </div>
            </div>
          </div>
          <div class="playlist-footer">
            <span class="song-count text-secondary">{{ playlist.songs?.length || 0 }} 首歌曲</span>
            <button class="add-all-btn btn-glass" @click="addAllToPlaylist(playlist)">一键添加</button>
          </div>
        </div>
      </section>
    </main>

    <MusicPlayer ref="musicPlayerRef" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { songApi } from '../api/song'
import MusicPlayer from '../components/MusicPlayer.vue'

const sceneTags = ['车载出行', '朋友聚会', '生日派对', '情侣对唱', '经典老歌', '动感舞曲']
const selectedTag = ref('车载出行')
const playlists = ref([])
const musicPlayerRef = ref(null)

onMounted(() => {
  loadPlaylists(selectedTag.value)
})

const selectScene = async (tag) => {
  selectedTag.value = tag
  await loadPlaylists(tag)
}

const loadPlaylists = async (scene) => {
  try {
    const res = await songApi.scene(scene)
    const songs = res.data.data || []
    
    playlists.value = [
      {
        id: 1,
        playlistName: `${scene}歌单`,
        description: `${scene}专属精选歌曲`,
        songs: songs.slice(0, 8)
      }
    ]
  } catch (error) {
    console.error('加载场景歌单失败', error)
  }
}

const getPreviewSongs = (playlist) => {
  if (!playlist.songs) return []
  return playlist.songs.slice(0, 3)
}

const addAllToPlaylist = (playlist) => {
  if (musicPlayerRef.value && playlist.songs) {
    playlist.songs.forEach(song => {
      musicPlayerRef.value.addToPlaylist(song)
    })
    alert(`已添加 ${playlist.songs.length} 首歌曲到播放列表`)
  }
}
</script>

<style scoped>
.playlist {
  padding: 20px 40px;
  max-width: 1000px;
  margin: 0 auto;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 0;
}

.back-link {
  text-decoration: none;
  color: #007aff;
  font-size: 15px;
  font-weight: 500;
}

.title {
  font-size: 24px;
  font-weight: 600;
  color: #1d1d1f;
}

.placeholder {
  width: 60px;
}

.main {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.scene-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  padding: 0 20px;
}

.tag-btn {
  padding: 10px 20px;
  background: rgba(255, 255, 255, 0.6);
  border: none;
  border-radius: 20px;
  font-size: 14px;
  color: rgba(0, 0, 0, 0.6);
  cursor: pointer;
  transition: all 0.2s;
}

.tag-btn:hover {
  background: rgba(0, 122, 255, 0.05);
  color: #007aff;
}

.tag-btn.active {
  background: rgba(0, 122, 255, 0.1);
  color: #007aff;
}

.playlist-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
  padding: 0 20px;
}

.playlist-card {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.playlist-header h3 {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
}

.playlist-content {
  flex: 1;
}

.song-preview {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.preview-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
}

.num {
  width: 20px;
  font-size: 12px;
  color: rgba(0, 0, 0, 0.3);
}

.playlist-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 10px;
  border-top: 1px solid rgba(0, 0, 0, 0.04);
}

.song-count {
  font-size: 13px;
}

.add-all-btn {
  font-size: 13px;
}
</style>