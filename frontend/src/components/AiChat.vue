<template>
  <!-- 悬浮触发按钮 -->
  <div class="ai-chat-fab" :class="{ active: visible }" @click="toggle" v-if="!visible">
    <el-badge :value="'AI'" type="primary">
      <el-icon class="fab-icon"><ChatDotRound /></el-icon>
    </el-badge>
  </div>

  <!-- 聊天面板 -->
  <transition name="ai-slide">
    <div v-if="visible" class="ai-chat-panel">
      <!-- 头部 -->
      <div class="chat-header">
        <div class="chat-title">
          <el-icon class="chat-title-icon"><ChatDotRound /></el-icon>
          <span>AI 智能客服</span>
          <span class="chat-badge">千问</span>
        </div>
        <el-icon class="chat-close" @click="toggle"><Close /></el-icon>
      </div>

      <!-- 消息区 -->
      <div ref="msgBoxRef" class="chat-body">
        <div v-for="(m, i) in messages" :key="i" class="msg-item" :class="m.role">
          <el-avatar v-if="m.role === 'ai'" :size="32" class="msg-avatar">AI</el-avatar>
          <div class="msg-bubble" :class="m.role">{{ formatReply(m.content) }}</div>
        </div>
        <div v-if="loading" class="msg-item ai">
          <el-avatar :size="32" class="msg-avatar">AI</el-avatar>
          <div class="msg-bubble ai typing">
            <span class="dot"></span><span class="dot"></span><span class="dot"></span>
          </div>
        </div>
      </div>

      <!-- 快捷问题 -->
      <div v-if="messages.length <= 1" class="quick-questions">
        <span
          v-for="q in quickQuestions"
          :key="q"
          class="quick-chip"
          @click="sendQuick(q)">{{ q }}</span>
      </div>

      <!-- 输入区 -->
      <div class="chat-input">
        <el-input
          v-model="input"
          placeholder="输入您的问题..."
          :disabled="loading"
          @keyup.enter="onSend" />
        <el-button type="primary" :loading="loading" @click="onSend">
          <el-icon><Promotion /></el-icon>
        </el-button>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { nextTick, ref } from 'vue'
import { ChatDotRound, Close, Promotion } from '@element-plus/icons-vue'
import { aiChat } from '@/api/ai'

const visible = ref(false)
const input = ref('')
const loading = ref(false)
const msgBoxRef = ref(null)

const quickQuestions = [
  '怎么预订民宿？',
  '优惠券怎么用？',
  '如何取消订单？',
  '怎么当房东？',
  '支付方式是什么？',
  '怎么搜索民宿？',
]

const messages = ref([
  { role: 'ai', content: '您好！我是栖迟民宿的 AI 智能客服，有什么可以帮您的？\n\n您可以直接输入问题，或点击下方快捷提问。' }
])

function toggle() {
  visible.value = !visible.value
}

function sendQuick(q) {
  input.value = q
  onSend()
}

async function onSend() {
  const text = input.value.trim()
  if (!text || loading.value) return

  messages.value.push({ role: 'user', content: text })
  input.value = ''
  loading.value = true
  await scrollToBottom()

  // 组装多轮对话历史（排除刚加入的本条用户消息），ai 角色归一化为 assistant，
  // 让千问能够理解「那它呢 / 再说说取消」之类的上下文追问
  const history = messages.value
    .slice(0, -1)
    .slice(-10)
    .map((m) => ({ role: m.role === 'ai' ? 'assistant' : 'user', content: m.content }))

  try {
    const res = await aiChat(text, history)
    messages.value.push({ role: 'ai', content: res.reply })
  } catch (e) {
    messages.value.push({ role: 'ai', content: '抱歉，服务暂时不可用，请稍后再试。' })
  } finally {
    loading.value = false
    await scrollToBottom()
  }
}

async function scrollToBottom() {
  await nextTick()
  if (msgBoxRef.value) {
    msgBoxRef.value.scrollTop = msgBoxRef.value.scrollHeight
  }
}

function formatReply(text) {
  return text
}
</script>

<style scoped>
.ai-chat-fab {
  position: fixed;
  right: 24px;
  bottom: 32px;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 999;
  transition: transform 0.2s;
}
.ai-chat-fab:hover {
  transform: scale(1.08);
}
.fab-icon {
  font-size: 26px;
  color: #fff;
}
.ai-chat-panel {
  position: fixed;
  right: 24px;
  bottom: 32px;
  width: 380px;
  max-width: calc(100vw - 48px);
  height: 520px;
  max-height: calc(100vh - 100px);
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  z-index: 999;
  overflow: hidden;
}
.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 18px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}
.chat-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
}
.chat-title-icon {
  font-size: 20px;
}
.chat-badge {
  background: rgba(255, 255, 255, 0.25);
  font-size: 10px;
  padding: 1px 6px;
  border-radius: 8px;
}
.chat-close {
  font-size: 18px;
  cursor: pointer;
  opacity: 0.8;
  transition: opacity 0.2s;
}
.chat-close:hover {
  opacity: 1;
}
.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f7f8fa;
}
.msg-item {
  display: flex;
  gap: 8px;
  margin-bottom: 14px;
  align-items: flex-start;
}
.msg-item.user {
  flex-direction: row-reverse;
}
.msg-avatar {
  flex-shrink: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
}
.msg-bubble {
  max-width: 260px;
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 13.5px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}
.msg-bubble.ai {
  background: #fff;
  color: var(--bnb-text);
  border: 1px solid #eee;
  border-top-left-radius: 4px;
}
.msg-bubble.user {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-top-right-radius: 4px;
}
.typing {
  display: flex;
  gap: 4px;
  align-items: center;
  padding: 14px;
}
.typing .dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #aaa;
  animation: typing-bounce 1.4s infinite ease-in-out;
}
.typing .dot:nth-child(2) { animation-delay: 0.2s; }
.typing .dot:nth-child(3) { animation-delay: 0.4s; }
@keyframes typing-bounce {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.4; }
  30% { transform: translateY(-6px); opacity: 1; }
}
.quick-questions {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  padding: 8px 16px;
  border-top: 1px solid #eee;
  background: #fff;
}
.quick-chip {
  font-size: 12px;
  color: #667eea;
  background: rgba(102, 126, 234, 0.08);
  border: 1px solid rgba(102, 126, 234, 0.2);
  padding: 3px 10px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.15s;
}
.quick-chip:hover {
  background: rgba(102, 126, 234, 0.15);
}
.chat-input {
  display: flex;
  gap: 8px;
  padding: 12px 16px;
  border-top: 1px solid #eee;
  background: #fff;
}
.chat-input .el-button {
  flex-shrink: 0;
}

/* 滑入动画 */
.ai-slide-enter-active,
.ai-slide-leave-active {
  transition: all 0.3s ease;
}
.ai-slide-enter-from,
.ai-slide-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}

/* 滚动条美化 */
.chat-body::-webkit-scrollbar {
  width: 4px;
}
.chat-body::-webkit-scrollbar-thumb {
  background: #ddd;
  border-radius: 2px;
}

@media (max-width: 768px) {
  .ai-chat-panel {
    right: 0;
    bottom: 0;
    width: 100vw;
    height: 100vh;
    max-height: 100vh;
    border-radius: 0;
  }
}
</style>
