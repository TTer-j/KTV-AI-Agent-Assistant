package com.ktvai.assistant.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class VectorLoader {
    
    private static final String[] SCENE_DOCUMENTS = {
        "生日派对场景适合播放欢快热闹的歌曲",
        "朋友聚会场景适合播放轻松愉悦的流行歌曲",
        "浪漫约会场景适合播放甜蜜温馨的情歌",
        "毕业季场景适合播放青春回忆的校园歌曲",
        "婚礼场景适合播放幸福甜蜜的歌曲",
        "KTV热门场景适合播放大家都会唱的经典歌曲",
        "经典老歌场景适合播放8090年代的经典歌曲",
        "摇滚场景适合播放节奏感强的摇滚歌曲",
        "民谣场景适合播放清新抒情的民谣歌曲",
        "动感舞曲场景适合播放节奏感强的舞曲"
    };
    
    public void loadDocuments() {
        log.info("RAG knowledge base loaded successfully, {} documents", SCENE_DOCUMENTS.length);
    }
}