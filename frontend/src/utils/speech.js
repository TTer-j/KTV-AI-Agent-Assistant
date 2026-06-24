export const speechRecognition = {
  isSupported: () => 'webkitSpeechRecognition' in window || 'SpeechRecognition' in window,
  
  start: (onResult, onError, onEnd) => {
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
    if (!SpeechRecognition) {
      return null
    }
    
    const recognition = new SpeechRecognition()
    recognition.continuous = false
    recognition.interimResults = true
    recognition.lang = 'zh-CN'
    recognition.maxAlternatives = 3
    
    recognition.onresult = (event) => {
      let finalText = ''
      let interimText = ''
      for (let i = event.resultIndex; i < event.results.length; i += 1) {
        const text = event.results[i][0]?.transcript || ''
        if (event.results[i].isFinal) {
          finalText += text
        } else {
          interimText += text
        }
      }
      if (onResult) onResult(finalText || interimText, Boolean(finalText))
    }

    recognition.onnomatch = () => {
      if (onError) onError({ error: 'no-match' })
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
