import request from '@/utils/request'

/**
 * AI 智能客服对话
 * @param {string} message 用户本次输入
 * @param {Array<{role: string, content: string}>} history 历史消息（role: user / assistant）
 */
export const aiChat = (message, history = []) =>
  request.post('/ai/chat', { message, history })
