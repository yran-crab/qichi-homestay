package com.bnbms.controller.portal;

import com.bnbms.common.Result;
import com.bnbms.service.AiChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 智能客服接口（匿名可访问）
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;

    /**
     * 客服对话接口。
     *
     * @param body message：用户本次输入；history：可选的历史消息（role 为 user / assistant），
     *             用于多轮上下文理解
     */
    @PostMapping("/chat")
    @SuppressWarnings("unchecked")
    public Result<Map<String, String>> chat(@RequestBody Map<String, Object> body) {
        String message = body.get("message") == null ? "" : String.valueOf(body.get("message"));

        List<Map<String, String>> history = new ArrayList<>();
        Object rawHistory = body.get("history");
        if (rawHistory instanceof List) {
            for (Object item : (List<Object>) rawHistory) {
                if (item instanceof Map) {
                    Map<String, String> pair = new HashMap<>(2);
                    Object role = ((Map<String, Object>) item).get("role");
                    Object content = ((Map<String, Object>) item).get("content");
                    if (role != null && content != null) {
                        pair.put("role", String.valueOf(role));
                        pair.put("content", String.valueOf(content));
                        history.add(pair);
                    }
                }
            }
        }

        String reply = aiChatService.chat(message, history);
        Map<String, String> result = new HashMap<>();
        result.put("reply", reply);
        return Result.ok(result);
    }
}
