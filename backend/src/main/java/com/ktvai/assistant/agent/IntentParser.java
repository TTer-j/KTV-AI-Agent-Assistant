package com.ktvai.assistant.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final String INTENT_SYSTEM_PROMPT = """
            你是 KTV 智能点歌系统的意图解析器。请把用户口语化点歌需求解析成 JSON，只返回 JSON，不要解释。
            type 只能是 SONG_SEARCH、SCENE_PLAYLIST、GENRE_SEARCH、CHAT、CLARIFICATION。
            字段：
            - type: 意图类型
            - songName: 明确歌曲名，没有则空字符串
            - singer: 明确歌手名，没有则空字符串
            - genre: 曲风、语种、情绪关键词，如 慢歌/情歌/粤语/告白/嗨歌，没有则空字符串
            - scene: 场景，如 生日局/朋友聚会/情侣对唱/车载，没有则空字符串
            - clarificationQuestion: 需要追问时的问题，否则空字符串
            例子：
            用户：来首周杰伦的慢歌
            {"type":"SONG_SEARCH","songName":"","singer":"周杰伦","genre":"慢歌","scene":"","clarificationQuestion":""}
            用户：我要唱适合告白的情歌
            {"type":"GENRE_SEARCH","songName":"","singer":"","genre":"告白 情歌","scene":"","clarificationQuestion":""}
            用户：生日局歌单
            {"type":"SCENE_PLAYLIST","songName":"","singer":"","genre":"","scene":"生日局","clarificationQuestion":""}
            用户：我要唱那首很火的歌
            {"type":"CLARIFICATION","songName":"","singer":"","genre":"热门","scene":"","clarificationQuestion":"你记得是男歌手还是女歌手？国语还是粤语？"}
            """;
    
    public IntentDTO parseIntent(String userInput) {
        IntentDTO fallback = parseFallback(userInput);

        if (!deepSeekClient.isConfigured()) {
            return fallback;
        }

        try {
            String aiJson = deepSeekClient.chat(INTENT_SYSTEM_PROMPT, userInput);
            
            if (aiJson == null || aiJson.isEmpty()) {
                return fallback;
            }

            IntentDTO aiIntent = parseAiIntent(aiJson, userInput);
            return aiIntent == null ? fallback : aiIntent;
        } catch (Exception e) {
            log.warn("AI intent parsing failed, fallback to rule-based: {}", e.getMessage());
            return fallback;
        }
    }

    private IntentDTO parseAiIntent(String aiJson, String userInput) {
        try {
            String json = aiJson
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();
            JsonNode root = objectMapper.readTree(json);
            IntentDTO intent = new IntentDTO();
            intent.setType(normalizeIntentType(root.path("type").asText("")));
            intent.setSongName(blankToNull(root.path("songName").asText("")));
            intent.setSinger(blankToNull(root.path("singer").asText("")));
            intent.setGenre(blankToNull(root.path("genre").asText("")));
            intent.setScene(blankToNull(root.path("scene").asText("")));
            intent.setClarificationQuestion(blankToNull(root.path("clarificationQuestion").asText("")));
            intent.setKeywords(java.util.Arrays.asList(userInput.trim().split("\\s+")));
            intent.setNeedClarify("CLARIFICATION".equals(intent.getType()));
            if (intent.isNeedClarify() && intent.getClarificationQuestion() == null) {
                intent.setClarificationQuestion("你想偏男声还是女声？国语、粤语，还是某个年代的歌？");
            }
            if ("SONG_SEARCH".equals(intent.getType()) && intent.getSongName() == null && intent.getSinger() == null) {
                intent.setSongName(userInput);
            }
            if ("GENRE_SEARCH".equals(intent.getType()) && intent.getGenre() == null) {
                intent.setGenre(userInput);
            }
            if ("SCENE_PLAYLIST".equals(intent.getType()) && intent.getScene() == null) {
                intent.setScene(userInput);
            }
            return intent;
        } catch (Exception e) {
            log.warn("AI intent JSON parse failed: {}", aiJson);
            return null;
        }
    }

    private String blankToNull(String text) {
        String value = text == null ? "" : text.trim();
        return value.isEmpty() ? null : value;
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
            intent.setSinger(extractKnownSinger(normalized));
            intent.setNeedClarify(false);
        } else if (normalized.contains("你好") || normalized.contains("在吗") ||
                   normalized.contains("谢谢") || normalized.contains("你是谁")) {
            intent.setType("CHAT");
            intent.setNeedClarify(false);
        } else {
            intent.setType("SONG_SEARCH");
            intent.setSongName(normalized);
            intent.setSinger(extractKnownSinger(normalized));
            intent.setNeedClarify(false);
        }
        
        return intent;
    }

    private String extractKnownSinger(String text) {
        String[] singers = {"周杰伦", "陈奕迅", "张学友", "林俊杰", "王菲", "邓丽君", "刘德华", "五月天", "孙燕姿", "梁静茹", "Beyond", "谭咏麟", "邓紫棋"};
        for (String singer : singers) {
            if (text != null && text.contains(singer)) {
                return singer;
            }
        }
        return null;
    }
}
