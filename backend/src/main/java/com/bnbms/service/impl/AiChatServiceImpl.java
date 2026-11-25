package com.bnbms.service.impl;

import com.bnbms.service.AiChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 智能客服服务实现
 *
 * <p>架构说明（三级降级，保证客服始终可用）：
 * <ol>
 *   <li>若配置了 qwen.api-key，则调用通义千问（阿里云百炼 DashScope，OpenAI 兼容接口），
 *       携带民宿业务知识库系统提示词与多轮对话历史，回答最智能</li>
 *   <li>其次若配置了 coze.token，则调用 Coze 工作流 API（/v3/chat）</li>
 *   <li>均未配置或调用失败时，降级为基于关键词匹配的规则引擎兜底回复</li>
 * </ol>
 */
@Slf4j
@Service
public class AiChatServiceImpl implements AiChatService {

    // ==================== 通义千问（DashScope）配置 ====================

    @Value("${qwen.api-key:}")
    private String qwenApiKey;

    @Value("${qwen.model:qwen-plus}")
    private String qwenModel;

    @Value("${qwen.base-url:https://dashscope.aliyuncs.com/compatible-mode/v1}")
    private String qwenBaseUrl;

    @Value("${qwen.temperature:0.7}")
    private double qwenTemperature;

    @Value("${qwen.max-tokens:1024}")
    private int qwenMaxTokens;

    // ==================== Coze 配置 ====================

    @Value("${coze.token:}")
    private String cozeToken;

    @Value("${coze.bot-id:}")
    private String cozeBotId;

    @Value("${coze.api-url:https://api.coze.cn}")
    private String cozeApiUrl;

    /**
     * 携带多轮历史时，最多传给大模型的历史消息条数（user/assistant 各算一条），
     * 控制 Token 消耗。取最近的 10 条（约 5 轮对话）。
     */
    private static final int MAX_HISTORY_MESSAGES = 10;

    private final RestTemplate restTemplate;

    public AiChatServiceImpl() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(30_000);
        this.restTemplate = new RestTemplate(factory);
    }

    @Override
    public String chat(String message) {
        return chat(message, null);
    }

    @Override
    public String chat(String message, List<Map<String, String>> history) {
        if (message == null || message.trim().isEmpty()) {
            return "您好，请问有什么可以帮您？";
        }
        String userMessage = message.trim();

        // 1. 优先调用通义千问
        if (qwenApiKey != null && !qwenApiKey.trim().isEmpty()) {
            try {
                return callQwen(userMessage, history);
            } catch (Exception e) {
                log.warn("通义千问调用失败，尝试降级: {}", e.getMessage());
            }
        }

        // 2. 其次尝试 Coze AI
        if (cozeToken != null && !cozeToken.trim().isEmpty()
                && cozeBotId != null && !cozeBotId.trim().isEmpty()) {
            try {
                return callCoze(userMessage);
            } catch (Exception e) {
                log.warn("Coze AI 调用失败，降级到规则引擎: {}", e.getMessage());
            }
        }

        // 3. 规则兜底
        return ruleBasedReply(userMessage);
    }

    // ================================================================
    // 通义千问（DashScope OpenAI 兼容接口）
    // 文档：https://help.aliyun.com/zh/model-studio/developer-reference/use-qwen-by-calling-api
    // ================================================================

    /**
     * 调用通义千问 chat/completions 接口。
     * <p>请求 / 响应结构与 OpenAI 完全一致：
     * 响应 choices[0].message.content 即为模型回复。
     */
    @SuppressWarnings("unchecked")
    private String callQwen(String message, List<Map<String, String>> history) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(qwenApiKey.trim());

        Map<String, Object> body = new HashMap<>();
        body.put("model", qwenModel);
        body.put("temperature", qwenTemperature);
        body.put("max_tokens", qwenMaxTokens);

        List<Map<String, String>> messages = new ArrayList<>();
        // system：注入民宿业务知识库，约束模型以专业客服身份回答
        Map<String, String> systemMsg = new HashMap<>(2);
        systemMsg.put("role", "system");
        systemMsg.put("content", SYSTEM_PROMPT);
        messages.add(systemMsg);

        // history：最近若干轮多轮对话，支持上下文追问
        if (history != null && !history.isEmpty()) {
            int from = Math.max(0, history.size() - MAX_HISTORY_MESSAGES);
            for (int i = from; i < history.size(); i++) {
                Map<String, String> h = history.get(i);
                if (h == null) {
                    continue;
                }
                String role = h.get("role");
                String content = h.get("content");
                if (content == null || content.trim().isEmpty()) {
                    continue;
                }
                // 仅接受合法角色，前端的 ai 角色归一化为 assistant
                if ("user".equals(role) || "assistant".equals(role) || "ai".equals(role)) {
                    Map<String, String> m = new HashMap<>(2);
                    m.put("role", "ai".equals(role) ? "assistant" : role);
                    m.put("content", content.trim());
                    messages.add(m);
                }
            }
        }

        // user：本轮用户输入
        Map<String, String> userMsg = new HashMap<>(2);
        userMsg.put("role", "user");
        userMsg.put("content", message);
        messages.add(userMsg);
        body.put("messages", messages);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        String url = qwenBaseUrl + "/chat/completions";

        Map<String, Object> resp = restTemplate.postForObject(url, entity, Map.class);
        if (resp == null) {
            throw new RuntimeException("千问返回为空");
        }
        List<Map<String, Object>> choices = (List<Map<String, Object>>) resp.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new RuntimeException("千问返回无 choices: " + resp);
        }
        Map<String, Object> choice = choices.get(0);
        Map<String, Object> respMessage = (Map<String, Object>) choice.get("message");
        if (respMessage == null) {
            throw new RuntimeException("千问返回无 message: " + resp);
        }
        String content = (String) respMessage.get("content");
        if (content == null || content.trim().isEmpty()) {
            // 部分场景（如触发安全策略）内容可能为空，给出可识别的错误以触发降级
            throw new RuntimeException("千问返回内容为空: " + resp);
        }
        return content.trim();
    }

    /**
     * 系统提示词：栖迟民宿业务知识库 + 客服行为规范。
     * <p>大模型据此获得平台的全部业务规则，回答准确、风格统一，且不会编造平台没有的功能。
     */
    private static final String SYSTEM_PROMPT = ""
            + "你是「栖迟民宿」预订平台的 AI 智能客服，名字叫小栖，7×24 小时在线，用简体中文为用户提供咨询服务。\n\n"
            + "【平台业务知识】\n"
            + "1. 预订流程：在房源详情页选择入住和退房日期、填写入住人数、可选用优惠券抵扣、点击「立即预订」提交订单，"
            + "然后在支付宝收银台完成支付。订单提交后需在 30 分钟内支付，超时系统自动取消。\n"
            + "2. 支付方式：平台集成支付宝沙箱在线支付，点击「确认支付」跳转支付宝收银台付款，成功后订单状态变为「已支付」，"
            + "可在「我的订单」查看。\n"
            + "3. 优惠券：分三类——代金券（无门槛直减）、满减券（订单满指定金额后抵扣）、折扣券（按折扣率打折，可设封顶金额）。"
            + "在「优惠券中心」领取，预订确认页的优惠券下拉框中选用。规则：待支付订单取消后券自动退回，已支付订单取消不退券。\n"
            + "4. 取消订单：待支付状态可随时取消且优惠券退回；已支付状态入住前可取消但优惠券不退；超时未支付 30 分钟后自动取消。"
            + "入口：我的订单 → 对应订单 → 取消订单。已支付订单退款一般 1-3 个工作日原路退回支付宝。\n"
            + "5. 成为房东：注册登录后在「个人中心」提交房东申请，管理员通常 1-2 个工作日内审核；通过后在房东中心发布房源"
            + "（标题、城市、价格、户型、设施等），房源经管理员审核后上架。房东还可管理房源、查看订单、回复评价。\n"
            + "6. 收藏：在房源详情页点击右上角「收藏」，可在「我的收藏」查看；收藏与订单历史用于「猜你喜欢」协同过滤个性化推荐，"
            + "收藏和预订越多推荐越精准。\n"
            + "7. 评价：仅已完成（已退房）的订单可评价，支持 1-5 星评分和文字、图片评价，房东可回复。"
            + "入口：我的订单 → 已完成订单 → 发表评价。\n"
            + "8. 房源日历：房源详情页「房源日历」可查看月度可订日期，红色为已订、绿色为可订、灰色为过去日期，可左右切换月份。\n"
            + "9. 注册登录：注册需填写用户名、密码、确认密码和手机号，默认角色为租客。登录后按角色自动跳转："
            + "租客→首页，房东→房东中心，管理员→管理后台。演示账号：tenant1/123456（租客）、host1/123456（房东）、"
            + "admin/123456（管理员）。忘记密码可在登录页通过手机号验证重置。\n"
            + "10. 搜索筛选：在「找民宿」页可按城市、价格区间、房型、入住人数等条件筛选，支持标题/描述/地址关键词搜索，"
            + "可按最新发布或价格排序。\n"
            + "11. 费用计算：房费 = 每晚价格 × 入住天数 - 优惠券抵扣金额，最终实付金额在预订确认页自动计算展示。\n"
            + "12. 个人中心可修改真实姓名、手机号、邮箱，也可提交房东申请、查看收藏与订单。\n\n"
            + "【回答规范】\n"
            + "- 语气亲切、耐心、专业；回答简洁有条理，复杂问题用编号分步骤说明，可适度使用 emoji 辅助排版。\n"
            + "- 优先依据上述业务知识作答；涉及操作路径时给出明确的菜单入口。\n"
            + "- 只回答与本民宿预订平台相关的问题。与平台无关的请求（如写代码、创作、闲聊时事等），"
            + "礼貌说明你只负责民宿客服并引导用户提出业务问题。\n"
            + "- 知识中没有或信息不足时不要编造功能、政策或金额，可如实说明并建议用户联系人工客服。\n"
            + "- 单次回答尽量控制在 200 字以内，重点突出，不要长篇大论。";

    /**
     * 调用 Coze 工作流 API（POST /v3/chat）
     */
    @SuppressWarnings("unchecked")
    private String callCoze(String message) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(cozeToken);

        Map<String, Object> body = new HashMap<>();
        body.put("bot_id", cozeBotId);
        body.put("user_id", "bnbms-portal-user");
        body.put("stream", false);
        body.put("auto_save_history", true);

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> msg = new HashMap<>();
        msg.put("role", "user");
        msg.put("content", message);
        msg.put("content_type", "text");
        messages.add(msg);
        body.put("additional_messages", messages);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        String url = cozeApiUrl + "/v3/chat";

        Map<String, Object> resp = restTemplate.postForObject(url, entity, Map.class);
        if (resp != null) {
            Integer code = (Integer) resp.get("code");
            if (code != null && code == 0) {
                Map<String, Object> data = (Map<String, Object>) resp.get("data");
                if (data != null) {
                    List<Map<String, Object>> answerList = (List<Map<String, Object>>) data.get("answer_list");
                    if (answerList != null && !answerList.isEmpty()) {
                        String content = (String) answerList.get(0).get("content");
                        if (content != null && !content.isEmpty()) {
                            return content;
                        }
                    }
                }
            }
        }
        throw new RuntimeException("Coze 返回无效");
    }

    // ================================================================
    // 知识库：每个主题包含同义词组 + 回答模板
    // ================================================================
    private static final TopicRule[] TOPIC_RULES = {
        new TopicRule("greeting",
            new String[]{"你好","hello","hi","嗨","在吗","在不在","有人吗","哈喽","早上好","下午好","晚上好"},
            "您好！我是栖迟民宿的 AI 智能客服，可以为您解答关于预订、支付、优惠券、取消订单、房东申请等方面的问题。请问有什么可以帮您的？\n\n您也可以直接问我：\n• 怎么预订民宿\n• 优惠券怎么用\n• 怎么取消订单\n• 怎么当房东"),

        new TopicRule("booking",
            new String[]{"预订","预订","下单","怎么订","如何预订","订房","订民宿","怎么住","想住","想订","怎么租","租民宿","book","预订流程","订单流程","怎么选房","怎么选日期","入住日期","退房日期","住几晚","几天","选日期","预定","约民宿","约房","想预定","想预订"},
            "预订流程：\n1. 在房源详情页选择入住和退房日期\n2. 填写入住人数\n3. 可选择优惠券抵扣（可选）\n4. 点击「立即预订」提交订单\n5. 在收银台通过支付宝完成支付\n\n💡 订单提交后需在 30 分钟内完成支付，超时将自动取消。\n💡 可以在预订确认页选用优惠券享受优惠。"),

        new TopicRule("payment",
            new String[]{"支付","付款","怎么付","支付宝","pay","付钱","结账","买单","怎么充值","付款方式","支付方式","在线支付","网上支付","沙箱","沙盒","付款流程","缴费","交钱","如何支付","怎么给钱","怎么结账"},
            "本系统集成了支付宝沙箱支付。下单后点击「确认支付」会跳转至支付宝收银台完成付款，支付成功后自动返回并更新订单状态为「已支付」。\n\n💡 支付成功后即可在「我的订单」查看订单详情。\n💡 如遇支付问题，请检查网络连接或稍后重试。"),

        new TopicRule("coupon",
            new String[]{"优惠券","券","折扣","满减","代金券","折扣券","满减券","优惠","减免","抵扣","coupon","领券","抢券","怎么用券","券怎么用","有什么优惠","优惠活动","打折","促销","红包","代金","满多少减","优惠码","有什么券"},
            "优惠券使用说明：\n• 代金券：无门槛直接抵扣（如减 20 元）\n• 满减券：订单满指定金额后抵扣（如满 300 减 50）\n• 折扣券：按折扣率打折（如 8.8 折），可设封顶金额\n\n📥 领取方式：在「优惠券中心」点击领取\n🎯 使用方式：预订确认页的优惠券下拉框选择即可\n🔄 退券规则：待支付订单取消后券自动退回，已支付订单取消不退券"),

        new TopicRule("cancel",
            new String[]{"取消","退款","退订","退","cancel","不要了","不订了","怎么退","退订单","取消订单","如何取消","能不能退","可以退吗","退款规则","退房","退钱","取消预订","不想住了","取消下单","撤单","退单"},
            "取消订单规则：\n• 🅿️ 待支付状态：可随时取消，优惠券自动退回\n• ✅ 已支付状态：入住前可取消，但优惠券不退回\n• ⏰ 超时未支付：系统 30 分钟后自动取消\n\n取消入口：我的订单 → 对应订单 → 取消订单\n\n💡 取消后订单状态变为「已取消」，待支付的取消会退还优惠券。"),

        new TopicRule("host",
            new String[]{"房东","发布房源","怎么当房东","房东申请","当房东","房东端","我要出租","我要发布","怎么出租","出租房子","上架房源","怎么上架","发布","怎么挂","做房东","成为房东","申请房东","我要当房东","怎么才能当房东","房东怎么弄","房源怎么发","发布民宿","挂房源"},
            "成为房东：\n1. 注册并登录账号\n2. 在「个人中心」提交房东申请\n3. 等待管理员审核通过\n4. 在房东中心发布房源（填写标题、城市、价格、户型、设施等）\n5. 房源经管理员审核后上架展示\n\n💡 审核通常在 1-2 个工作日内完成。\n💡 房东可在房东中心管理房源、查看订单和回复评价。"),

        new TopicRule("favorite",
            new String[]{"收藏","怎么收藏","favorite","收藏夹","想收藏","加收藏","存起来","收藏房源","我的收藏","关注","喜欢","想收藏","收藏功能","怎么收藏房","怎么关注","心愿单","心愿单","收藏列表"},
            "在房源详情页点击右上角的「收藏」按钮即可收藏房源。\n\n💡 已收藏的房源可在「我的收藏」中随时查看。\n💡 收藏越多，「猜你喜欢」推荐越精准！"),

        new TopicRule("review",
            new String[]{"评价","评分","怎么评论","review","点评","打分","评论","给好评","给差评","写评价","评价规则","怎么评","打星","几星","星级","评价功能","评价怎么弄","评价怎么写","评价入口","评价怎么发","评价流程"},
            "评价规则：\n• 仅已完成（已退房）的订单可发表评价\n• 评分范围 1-5 星\n• 支持文字和图片评价\n• 房东可回复评价\n\n评价入口：我的订单 → 已完成订单 → 发表评价\n\n💡 您的评价可以帮助其他用户做出选择，也能帮助房东改进服务。"),

        new TopicRule("recommend",
            new String[]{"推荐","猜你喜欢","猜我喜欢","recommend","个性化","推荐算法","推荐系统","给我推荐","有什么推荐","帮我推荐","推荐什么","推荐机制","怎么推荐的","为啥推荐","智能推荐","推荐是怎么算的","推荐什么意思"},
            "「猜你喜欢」是系统基于您的收藏和订单历史，通过协同过滤算法生成的个性化推荐。\n\n🔍 推荐流程：\n1. 收集偏好种子（收藏 + 订单）\n2. 构建偏好画像（城市 / 房型 / 价格区间）\n3. 候选匹配查询（排除已看过的）\n4. 不足则用热门房源补齐\n\n💡 您收藏和预订的房源越多，推荐越精准！\n💡 未登录用户也会展示热门推荐。"),

        new TopicRule("calendar",
            new String[]{"日历","可订日期","空房","available","availability","日历视图","有没有空","哪天有空","哪天可订","档期","哪天能住","日期可用","日历怎么看","看日期","有没有房","有房吗","空闲","可用日期","日历功能","订房日历"},
            "在房源详情页的「房源日历」卡片中，可以查看该房源的月度可用性日历。\n\n🔴 红色标记为已订日期\n🟢 绿色标记为可订日期\n⚪ 灰色标记为过去日期\n\n💡 可以通过左右箭头切换月份查看。\n💡 建议提前查看日历再选择入住日期。"),

        new TopicRule("register",
            new String[]{"注册","sign up","register","注册账号","怎么注册","如何注册","新用户","创建账号","申请账号","注册流程","没有账号","没注册","怎么建账号","怎么注册账号"},
            "注册流程：\n1. 点击右上角「注册」按钮\n2. 填写用户名、密码、确认密码和手机号\n3. 点击注册完成\n4. 默认角色为租客，可浏览和预订民宿\n\n💡 注册后如需成为房东，请在个人中心提交申请。\n💡 用户名和密码请妥善保管。"),

        new TopicRule("login",
            new String[]{"登录","login","登入","怎么登录","如何登录","登录不了","登不上","登不进","登录失败","登录入口","怎么登","账号登录","密码错误","登不上怎么办","登入不了","登录不上","无法登录","不能登录"},
            "登录方式：输入用户名和密码即可登录。\n\n💡 系统会根据您的角色自动跳转：\n• 租客 → 首页\n• 房东 → 房东中心\n• 管理员 → 管理后台\n\n💡 演示账号：tenant1/123456（租客）、host1/123456（房东）、admin/123456（管理员）\n💡 如果登录不上，请检查用户名和密码是否正确。"),

        new TopicRule("price",
            new String[]{"价格","多少钱","怎么收费","价格多少","房价","多少钱一晚","每晚多少钱","费用","收费","价位","价格区间","贵不贵","多少钱一天","住一晚多少钱","价格怎么样","房价多少","一晚多少钱","住宿费","房费","价格怎么算","多少钱一晚","每晚价格"},
            "民宿价格由房东在发布房源时设定，不同房源价格不同。\n\n💡 您可以在搜索页面通过价格区间筛选合适价位的房源。\n💡 使用优惠券可以享受额外折扣（代金 / 满减 / 折扣）。\n💡 最终价格在预订确认页会自动计算，包含优惠抵扣后的实付金额。\n\n💡 预订费用 = 每晚价格 × 入住天数 - 优惠券抵扣金额。"),

        new TopicRule("search",
            new String[]{"搜索","怎么搜","查找","怎么找","找民宿","找房子","搜索功能","筛选","怎么筛选","怎么查","怎么找民宿","搜索条件","筛选条件","怎么过滤","搜索民宿","查民宿","怎么找房","找房","找住的地方","搜民宿"},
            "搜索民宿方式：\n1. 在首页或导航栏点击「找民宿」\n2. 可以按城市、价格区间、房型、入住人数等条件筛选\n3. 支持关键词搜索（标题、描述、地址）\n4. 可按最新发布、价格排序\n\n💡 多条件组合筛选可以快速找到心仪的房源。\n💡 点击房源卡片可进入详情页查看更多信息。"),

        new TopicRule("refund",
            new String[]{"退款","退钱","退费","怎么退钱","钱退回来","退款多久","退款到账","退到哪","退款流程","退多少","钱退吗","怎么退款","退回","退回来"},
            "退款说明：\n• 待支付订单取消后无需退款（尚未付款）\n• 已支付订单取消后，系统会在 1-3 个工作日内原路退回至支付宝账户\n• 优惠券退回规则：待支付取消退券，已支付取消不退券\n\n💡 退款到账时间取决于支付渠道，通常较快。\n💡 如有退款问题请联系客服。"),

        new TopicRule("password",
            new String[]{"密码","忘记密码","忘了密码","密码忘了","密码不对","密码错误","找回密码","重置密码","改密码","修改密码","不记得密码","密码不记得","怎么改密码","怎么重置"},
            "如果您忘记密码：\n1. 在登录页面点击「忘记密码」\n2. 输入注册时的手机号\n3. 验证后重置新密码\n\n💡 建议使用容易记住但安全的密码。\n💡 演示账号密码统一为：123456"),

        new TopicRule("account",
            new String[]{"账号","个人信息","我的信息","改资料","修改信息","改手机号","换手机","改名字","资料修改","个人资料","修改个人","改账号","改信息","怎么改资料","编辑资料","个人中心","改资料"},
            "修改个人信息：\n1. 登录后点击右上角头像 →「个人中心」\n2. 可以修改真实姓名、手机号、邮箱等信息\n3. 点击「保存修改」即可\n\n💡 个人中心还可以提交房东申请、查看收藏和订单。"),

        new TopicRule("thanks",
            new String[]{"谢谢","感谢","thanks","thank","多谢","辛苦了","谢谢了","感谢感谢","太感谢","太好了","很有用","很有帮助","谢谢客服","感谢你"},
            "不客气！很高兴能帮到您。😊\n\n如果还有其他问题，随时可以问我。祝您旅途愉快！"),

        new TopicRule("goodbye",
            new String[]{"再见","拜拜","拜","bye","goodbye","走了","下了","先走了","下次见","回头见","88","拜"},
            "感谢您的使用！再见！👋\n\n如果还有问题，随时欢迎回来找我。祝您有愉快的入住体验！"),

        new TopicRule("service",
            new String[]{"客服","人工","联系","真人","转人工","人工客服","联系客服","人工服务","在线客服","找客服","客服电话","怎么联系","人工接"},
            "我是 AI 智能客服，7×24 小时在线，可以解答大部分常见问题。\n\n💡 您可以直接问我关于预订、支付、优惠券、取消、房东等方面的问题。\n💡 试试这些常见问题：\n• 「怎么预订民宿」\n• 「优惠券怎么用」\n• 「怎么取消订单」\n• 「怎么当房东」"),
    };

    /**
     * 规则引擎：基于评分的智能匹配
     * <p>对用户输入与知识库中每个主题的同义词组进行逐词匹配评分，
     * 取最高分主题的回答返回。分数相同时按知识库顺序优先。
     */
    private String ruleBasedReply(String msg) {
        String lower = msg.toLowerCase().trim();
        if (lower.isEmpty()) {
            return TOPIC_RULES[0].answer;
        }

        // 对每个主题评分
        int bestScore = 0;
        TopicRule bestTopic = null;

        for (TopicRule rule : TOPIC_RULES) {
            int score = rule.matchScore(lower);
            if (score > bestScore) {
                bestScore = score;
                bestTopic = rule;
            }
        }

        if (bestTopic != null && bestScore > 0) {
            return bestTopic.answer;
        }

        // 模糊兜底：如果没有任何匹配，分析用户意图给出引导
        return smartFallback(lower);
    }

    /**
     * 智能兜底回复：当无主题匹配时，根据用户输入特征给出引导性回复
     */
    private String smartFallback(String msg) {
        // 检查是否是问句
        boolean isQuestion = msg.contains("？") || msg.contains("?") ||
                msg.contains("怎么") || msg.contains("如何") ||
                msg.contains("为什么") || msg.contains("为什么") ||
                msg.contains("能不能") || msg.contains("可以") ||
                msg.contains("是不是") || msg.contains("有没有") ||
                msg.contains("多少") || msg.contains("哪里") ||
                msg.contains("什么") || msg.contains("啥");

        StringBuilder sb = new StringBuilder();
        sb.append("抱歉，我暂时没有完全理解您的问题。\n\n");
        sb.append("我是栖迟民宿的 AI 智能客服，可以帮您解答以下方面的问题：\n");
        sb.append("• 🏠 预订民宿（怎么订房、选日期）\n");
        sb.append("• 💰 支付方式（支付宝怎么付）\n");
        sb.append("• 🎫 优惠券（怎么领、怎么用）\n");
        sb.append("• ❌ 取消订单（怎么退、退款规则）\n");
        sb.append("• 🏘️ 成为房东（怎么出租房子）\n");
        sb.append("• ⭐ 评价功能（怎么写评价）\n");
        sb.append("• 🔍 搜索筛选（怎么找民宿）\n");
        sb.append("• 📅 房源日历（查看可订日期）\n");
        sb.append("• 💡 智能推荐（猜你喜欢）\n");
        sb.append("• 👤 账号问题（注册、登录、改密码）\n\n");

        if (isQuestion) {
            sb.append("您可以换个方式问，比如：\n");
            sb.append("• 「怎么预订民宿？」\n");
            sb.append("• 「优惠券怎么用？」\n");
            sb.append("• 「如何取消订单？」\n");
            sb.append("• 「怎么当房东？」");
        } else {
            sb.append("请直接输入您的问题，我会尽力为您解答！");
        }

        return sb.toString();
    }

    /**
     * 知识库规则定义
     */
    private static class TopicRule {
        final String id;
        final String[] synonyms;
        final String answer;

        TopicRule(String id, String[] synonyms, String answer) {
            this.id = id;
            this.synonyms = synonyms;
            this.answer = answer;
        }

        /**
         * 计算用户输入与本主题的匹配分
         * 完整词匹配 +3，部分包含 +1
         */
        int matchScore(String lower) {
            int score = 0;
            for (String syn : synonyms) {
                if (lower.contains(syn.toLowerCase())) {
                    // 越长的关键词权重越高
                    score += syn.length() > 2 ? 3 : 2;
                }
            }
            return score;
        }
    }
}
