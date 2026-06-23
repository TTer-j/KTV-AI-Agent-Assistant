export const speechRecognition = {
  isSupported: () => 'webkitSpeechRecognition' in window || 'SpeechRecognition' in window,
  
  start: (onResult, onError, onEnd) => {
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
    if (!SpeechRecognition) {
      return null
    }
    
    const recognition = new SpeechRecognition()
    recognition.continuous = false
    recognition.interimResults = false
    recognition.lang = 'zh-CN'
    
    recognition.onresult = (event) => {
      const result = event.results[0][0].transcript
      if (onResult) onResult(result)
    }
    
    recognition.onerror = (event) => {
      if (onError) onError(event)
    }
    
    recognition.onend = () => {
      if (onEnd) onEnd()
    }
    
    recognition.start()
    return recognition
  },
  
  stop: (recognition) => {
    if (recognition) {
      recognition.stop()
    }
  }
}