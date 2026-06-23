<template>
  <div class="app-container">
    <router-view />
  </div>
</template>

<script setup>
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  background:
    radial-gradient(circle at 18% 12%, rgba(255, 92, 122, 0.28), transparent 28%),
    radial-gradient(circle at 78% 8%, rgba(54, 209, 220, 0.24), transparent 26%),
    linear-gradient(135deg, #111318 0%, #1a1d25 46%, #08090d 100%);
  min-height: 100vh;
  color: #fff;
  overflow-x: hidden;
}

button,
input,
textarea {
  font: inherit;
}

button:focus-visible,
input:focus-visible,
textarea:focus-visible,
a:focus-visible {
  outline: 2px solid rgba(139, 233, 253, 0.8);
  outline-offset: 2px;
}

.app-container {
  width: 100%;
  min-height: 100vh;
}

.glass {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.05);
}

.btn-glass {
  background: rgba(0, 122, 255, 0.1);
  border: none;
  border-radius: 12px;
  color: #007aff;
  padding: 10px 20px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-glass:hover {
  background: rgba(0, 122, 255, 0.15);
  transform: translateY(-1px);
}

.btn-glass:active {
  transform: translateY(0);
}

.input-glass {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 16px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  padding: 14px 20px;
  font-size: 15px;
  color: #1d1d1f;
  outline: none;
  transition: all 0.2s;
}

.input-glass:focus {
  border-color: rgba(0, 122, 255, 0.3);
  box-shadow: 0 0 0 4px rgba(0, 122, 255, 0.05);
}

.input-glass::placeholder {
  color: rgba(0, 0, 0, 0.3);
}

.card-glass {
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
}

.text-primary {
  color: #1d1d1f;
}

.text-secondary {
  color: rgba(0, 0, 0, 0.5);
}

.text-blue {
  color: #007aff;
}
</style>
