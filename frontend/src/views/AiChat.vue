<template>
  <div class="ai-chat">
    <header class="header">
      <router-link to="/" class="back-link">← 返回</router-link>
      <h1 class="title">AI 点歌助手</h1>
      <div class="placeholder"></div>
    </header>

    <main class="chat-main">
      <div class="chat-container card-glass">
        <div class="chat-messages">
          <div 
            v-for="(msg, index) in messages" 
            :key="index" 
            class="message"
            :class="{ user: msg.type === 'user', ai: msg.type === 'ai' }"
          >
            <div class="message-content">
              <span class="label">{{ msg.type === 'user' ? '你' : 'AI' }}</span>
              <p>{{ msg.content }}</p>
              <div v-if="msg.type === 'ai' && msg.songs && msg.songs.length > 0" class="song-list">
                <div 
                  v-for="song in msg.songs" 
                  :key="song.id" 
                  class="song-item"
                >
                  <span class="song-name">{{ song.songName }}</span>
                  <span class="song-singer">{{ song.singer }}</span>
                  <button class="add-btn btn-glass" @click="addToPlaylist(song)">+ 加入点歌</button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="chat-input glass">
          <button 
            class="voice-btn" 
            :class="{ active: isListening }"
            @mousedown="startListening"
            @mouseup="stopListening"
          >
            🎤
          </button>
          <input 
            v-model="inputMessage" 
            type="text" 
            placeholder="输入你想点的歌曲..."
            class="input-glass"
            @keyup.enter="sendMessage"
          />
          <button class="send-btn btn-glass" @click="sendMessage">发送</button>
        </div>
      </div>
    </main>

    <MusicPlayer ref="musicPlayerRef" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { chatApi } from '../api/chat'
import { speechRecognition } from '../utils/speech'
import MusicPlayer from '../components/MusicPlayer.vue'

const messages = ref([])
const inputMessage = ref('')
const isListening = ref(false)
const musicPlayerRef = ref(null)
let recognition = null
let mediaRecorder = null
let audioChunks = []

const sendMessage = async () => {
  if (!inputMessage.value.trim()) return
  
  const userMsg = {
    type: 'user',
    content: inputMessage.value.trim()
  }
  messages.value.push(userMsg)
  
  const originalInput = inputMessage.value.trim()
  inputMessage.value = ''
  
  try {
    const res = await chatApi.chat({
      sessionId: 'default',
      userInput: originalInput
    })
    
    const data = res.data.data
    const aiMsg = {
      type: 'ai',
      content: data.reply || '抱歉，我没有理解您的意思',
      songs: data.songs || []
    }
    messages.value.push(aiMsg)
    
    scrollToBottom()
  } catch (error) {
    messages.value.push({
      type: 'ai',
      content: '抱歉，我现在有点忙，请稍后再试'
    })
    scrollToBottom()
  }
}

const addToPlaylist = (song) => {
  if (musicPlayerRef.value) {
    musicPlayerRef.value.addToPlaylist(song)
  }
}

const scrollToBottom = () => {
  setTimeout(() => {
    const container = document.querySelector('.chat-messages')
    if (container) {
      container.scrollTop = container.scrollHeight
    }
  }, 100)
}

const startListening = async () => {
  if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
    alert('您的浏览器不支持麦克风录音')
    return
  }
  
  try {
    isListening.value = true
    const stream = await navigator.mediaDevices.getUserMedia({ 
      audio: {
        sampleRate: 16000,
        channelCount: 1,
        echoCancellation: true,
        noiseSuppression: true
      } 
    })
    
    mediaRecorder = new MediaRecorder(stream, {
      mimeType: 'audio/webm;codecs=opus'
    })
    audioChunks = []
    
    mediaRecorder.ondataavailable = (event) => {
      if (event.data.size > 0) {
        audioChunks.push(event.data)
      }
    }
    
    mediaRecorder.onstop = async () => {
      const audioBlob = new Blob(audioChunks, { type: 'audio/webm' })
      const reader = new FileReader()
      reader.onloadend = async () => {
        const base64Audio = reader.result.split(',')[1]
        try {
          const response = await fetch('http://localhost:8080/api/speech/recognize', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ audio: base64Audio })
          })
          const data = await response.json()
          if (data.code === 200 && data.data && data.data.text) {
            inputMessage.value = data.data.text
            sendMessage()
          } else {
            alert('语音识别失败：' + (data.message || '未知错误'))
          }
        } catch (err) {
          console.error('发送音频失败', err)
          alert('语音识别服务调用失败')
        }
      }
      reader.readAsDataURL(audioBlob)
      stream.getTracks().forEach(track => track.stop())
    }
    
    mediaRecorder.start()
  } catch (err) {
    console.error('麦克风权限错误', err)
    isListening.value = false
    alert('请允许浏览器使用麦克风权限')
  }
}

const stopListening = () => {
  isListening.value = false
  if (mediaRecorder && mediaRecorder.state !== 'inactive') {
    mediaRecorder.stop()
  }
}
</script>

<style scoped>
.ai-chat {
  padding: 20px 40px;
  max-width: 800px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  height: 100vh;
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

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 20px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
}

.message {
  margin-bottom: 20px;
}

.message.user {
  text-align: right;
}

.message.ai {
  text-align: left;
}

.message-content {
  display: inline-block;
  padding: 14px 20px;
  border-radius: 20px;
  max-width: 80%;
}

.user .message-content {
  background: rgba(0, 122, 255, 0.1);
  color: #007aff;
  border-bottom-right-radius: 6px;
}

.ai .message-content {
  background: rgba(0, 0, 0, 0.03);
  color: #1d1d1f;
  border-bottom-left-radius: 6px;
}

.label {
  display: block;
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 6px;
  opacity: 0.6;
}

.message-content p {
  margin: 0;
  font-size: 15px;
  line-height: 1.6;
}

.song-list {
  margin-top: 15px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.song-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 15px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 12px;
}

.song-name {
  font-size: 14px;
  font-weight: 600;
  color: #1d1d1f;
  min-width: 100px;
}

.song-singer {
  font-size: 13px;
  color: rgba(0, 0, 0, 0.5);
  flex: 1;
}

.add-btn {
  font-size: 12px;
  padding: 6px 12px;
}

.chat-input {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px;
}

.voice-btn {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  border: none;
  background: rgba(255, 255, 255, 0.9);
  font-size: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.voice-btn:hover {
  background: rgba(255, 255, 255, 1);
}

.voice-btn.active {
  background: rgba(0, 122, 255, 0.1);
  color: #007aff;
}

.chat-input input {
  flex: 1;
}

.send-btn {
  white-space: nowrap;
}
</style>