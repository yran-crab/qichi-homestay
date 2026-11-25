package com.bnbms.service;

import java.util.List;
import java.util.Map;

/**
 * AI 智能客服服务
 * <p>调用优先级：通义千问（DashScope）→ Coze 工作流 → 本地规则引擎兜底。
 */
public interface AiChatService {

    /**
     * 发送用户消息，获取 AI 回复（单轮）。
     *
     * @param message 用户输入
     * @return AI 回复文本
     */
    String chat(String message);

    /**
     * 发送用户消息并携带对话历史，获取 AI 回复（多轮上下文）。
     *
     * @param message 用户本次输入
     * @param history 历史消息列表，每条包含 role（user / assistant）与 content；可为空
     * @return AI 回复文本
     */
    String chat(String message, List<Map<String, String>> history);
}
