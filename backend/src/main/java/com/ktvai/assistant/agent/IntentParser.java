package com.ktvai.assistant.agent;

import com.ktvai.assistant.dto.IntentDTO;
import com.ktvai.assistant.external.DeepSeekClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class IntentParser {
    
    private final DeepSeekClient deepSeekClient;
    
    private static final String INTENT_SYSTEM_PROMPT = "你是一个专业的KTV智能点歌助手，擅长理解用户的口语化点歌需求。请根据用户输入分析意图：\n" +
            "1. SONG_SEARCH - 用户想点具体的歌曲，包含歌曲名或歌手名\n" +
            "2. SCENE_PLAYLIST - 用户想要场景歌单，如生日派对、朋友聚会、浪漫约会等\n" +
            "3. GENRE_SEARCH - 用户想要特定曲风的歌曲，如摇滚、民谣、流行等\n" +
            "4. CHAT - 用户在闲聊或打招呼\n" +
            "5. CLARIFICATION - 用户需求模糊，需要追问更多信息\n" +
            "请只返回意图类型，不要返回其他内容。";
    
    public IntentDTO parseIntent(String userInput) {
        IntentDTO fallback = parseFallback(userInput);
        if (!"SONG_SEARCH".equals(fallback.getType()) || fallback.isNeedClarify()) {
            return fallback;
        }

        try {
            String intentType = deepSeekClient.chat(INTENT_SYSTEM_PROMPT, userInput);
            
            if (intentType == null || intentType.isEmpty()) {
                return fallback;
            }
            
            return buildIntent(normalizeIntentType(intentType), userInput);
        } catch (Exception e) {
            log.warn("AI intent parsing failed, fallback to rule-based: {}", e.getMessage());
            return fallback;
        }
    }

    private String normalizeIntentType(String rawIntent) {
        String text = rawIntent == null ? "" : rawIntent.toUpperCase();
        if (text.contains("SCENE_PLAYLIST")) return "SCENE_PLAYLIST";
        if (text.contains("GENRE_SEARCH")) return "GENRE_SEARCH";
        if (text.contains("CLARIFICATION")) return "CLARIFICATION";
        if (text.contains("CHAT")) return "CHAT";
        if (text.contains("SONG_SEARCH")) return "SONG_SEARCH";
        return "SONG_SEARCH";
    }
    
    private IntentDTO buildIntent(String intentType, String userInput) {
        IntentDTO intent = new IntentDTO();
        intent.setKeywords(java.util.Arrays.asList(userInput.split(" ")));
        intent.setType(intentType.trim().toUpperCase());
        
        switch (intent.getType()) {
            case "SCENE_PLAYLIST":
                intent.setScene(userInput);
                intent.setNeedClarify(false);
                break;
            case "GENRE_SEARCH":
                intent.setGenre(userInput);
                intent.setNeedClarify(false);
                break;
            case "SONG_SEARCH":
                intent.setSongName(userInput);
                intent.setNeedClarify(false);
                break;
            case "CLARIFICATION":
                intent.setClarificationQuestion("请问你想点什么歌呢？");
                intent.setNeedClarify(true);
                break;
            case "CHAT":
            default:
                intent.setType("CHAT");
                intent.setNeedClarify(false);
        }
        
        return intent;
    }
    
    private IntentDTO parseFallback(String userInput) {
        IntentDTO intent = new IntentDTO();
        String normalized = userInput == null ? "" : userInput.trim();
        intent.setKeywords(java.util.Arrays.asList(normalized.split("\\s+")));

        if (normalized.isEmpty() || normalized.matches("[?？]+")) {
            intent.setType("CLARIFICATION");
            intent.setClarificationQuestion("请问你想点什么歌呢？");
            intent.setNeedClarify(true);
        } else if (normalized.contains("那首") || normalized.contains("那种") ||
                   normalized.equals("老歌") || normalized.equals("很火的歌") ||
                   normalized.equals("热门") || normalized.equals("随便") ||
                   normalized.equals("来一首")) {
            intent.setType("CLARIFICATION");
            intent.setClarificationQuestion("你想偏男声还是女声？国语、粤语，还是某个年代的歌？");
            intent.setNeedClarify(true);
        } else if (normalized.contains("歌单") || normalized.contains("推荐") ||
            normalized.contains("场景") || normalized.contains("派对") ||
            normalized.contains("聚会") || normalized.contains("气氛") ||
            normalized.contains("生日") || normalized.contains("车载") ||
            normalized.contains("路上") || normalized.contains("情侣") ||
            normalized.contains("对唱") || normalized.contains("团建") ||
            normalized.contains("暖场") || normalized.contains("公司")) {
            intent.setType("SCENE_PLAYLIST");
            intent.setScene(normalized);
            intent.setNeedClarify(false);
        } else if (normalized.contains("摇滚") || normalized.contains("民谣") ||
                   normalized.contains("流行") || normalized.contains("电子") ||
                   normalized.contains("爵士") || normalized.contains("慢歌") ||
                   normalized.contains("情歌") || normalized.contains("嗨") ||
                   normalized.contains("蹦迪") || normalized.contains("粤语") ||
                   normalized.contains("国语") || normalized.contains("告白") ||
                   normalized.contains("老歌")) {
            intent.setType("GENRE_SEARCH");
            intent.setGenre(normalized);
            intent.setNeedClarify(false);
        } else if (normalized.contains("你好") || normalized.contains("在吗") ||
                   normalized.contains("谢谢") || normalized.contains("你是谁")) {
            intent.setType("CHAT");
            intent.setNeedClarify(false);
        } else {
            intent.setType("SONG_SEARCH");
            intent.setSongName(normalized);
            intent.setNeedClarify(false);
        }
        
        return intent;
    }
}
