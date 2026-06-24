package com.ktvai.assistant.service;

import com.ktvai.assistant.agent.DialogueContext;
import com.ktvai.assistant.agent.IntentParser;
import com.ktvai.assistant.agent.SceneRecommender;
import com.ktvai.assistant.agent.SongRetriever;
import com.ktvai.assistant.dto.*;
import com.ktvai.assistant.entity.Song;
import com.ktvai.assistant.external.DeepSeekClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {
    
    private final IntentParser intentParser;
    private final SongRetriever songRetriever;
    private final SceneRecommender sceneRecommender;
    private final DialogueContext dialogueContext;
    private final DeepSeekClient deepSeekClient;
    
    public com.ktvai.assistant.dto.ChatResponse chat(ChatQuery query) {
        String sessionId = dialogueContext.getOrCreateSessionId(query.getSessionId());
        String userInput = query.getUserInput();
        
        com.ktvai.assistant.dto.ChatResponse response = new com.ktvai.assistant.dto.ChatResponse();
        response.setSessionId(sessionId);
        response.setAiConfigured(deepSeekClient.isConfigured());
        response.setAiProvider(deepSeekClient.isConfigured() ? "DeepSeek 已配置" : "DeepSeek 未配置 · 本地规则兜底");
        
        try {
            IntentDTO intent = intentParser.parseIntent(userInput);
            response.setIntentType(intent.getType());
            
            if (intent.isNeedClarify()) {
                String clarifyQuestion = generateClarifyQuestion(userInput, intent.getClarificationQuestion());
                response.setReply(clarifyQuestion);
                response.setClarification(true);
                dialogueContext.saveContext(sessionId, userInput, response.getReply(), intent.getType());
                return response;
            }
            
            String userPreferences = dialogueContext.getUserPreferences(sessionId);
            
            switch (intent.getType()) {
                case "SONG_SEARCH":
                    handleSongSearch(response, intent, userPreferences);
                    break;
                    
                case "SCENE_PLAYLIST":
                    handleScenePlaylist(response, intent);
                    break;
                    
                case "GENRE_SEARCH":
                    handleGenreSearch(response, intent, userPreferences);
                    break;
                    
                case "CHAT":
                default:
                    handleChat(response, userInput);
                    break;
            }
            
            dialogueContext.saveContext(sessionId, userInput, response.getReply(), intent.getType());
            
        } catch (Exception e) {
            log.error("Chat service error", e);
            response.setReply("我刚才没接稳，但可以先按本地曲库给你推荐。试试说：周杰伦慢歌、生日歌单、粤语情歌。");
            response.setSongs(new ArrayList<>());
            response.setPlaylists(new ArrayList<>());
        }
        
        return response;
    }
    
    private String generateClarifyQuestion(String userInput, String fallbackQuestion) {
        String fallback = fallbackQuestion == null || fallbackQuestion.isEmpty() ? "请问你想点什么歌呢？" : fallbackQuestion;
        try {
            String reply = deepSeekClient.chat(
                    "你是一个KTV智能点歌助手。用户的需求不够明确，请用友好的语气追问用户，获取更多信息来帮他点歌。",
                    userInput
            );
            return reply.isEmpty() ? fallback : reply;
        } catch (Exception e) {
            return fallback;
        }
    }
    
    private void handleSongSearch(com.ktvai.assistant.dto.ChatResponse response, IntentDTO intent, String preferences) {
        List<Song> songs = songRetriever.retrieveWithRerank(intent, preferences);
        
        if (songs.isEmpty()) {
            response.setReply("没有找到相关歌曲，试试其他关键词吧~");
            return;
        }
        
        List<SongDTO> songDTOs = songs.stream()
                .map(this::convertToSongDTO)
                .collect(Collectors.toList());
        
        response.setSongs(songDTOs);
        
        StringBuilder songInfo = new StringBuilder();
        for (int i = 0; i < Math.min(5, songDTOs.size()); i++) {
            SongDTO song = songDTOs.get(i);
            songInfo.append(i + 1).append(". ").append(song.getSongName())
                    .append(" - ").append(song.getSinger()).append("\n");
        }
        
        try {
            String reply = deepSeekClient.chat(
                    "你是一个专业的KTV智能点歌助手。根据找到的歌曲列表，用友好、热情的语气向用户介绍，不要超过100字。",
                    "用户搜索了：" + intent.getSongName() + "\n找到的歌曲：\n" + songInfo
            );
            response.setReply(chooseAiReply(response, reply, generateFallbackSongReply(songDTOs)));
        } catch (Exception e) {
            response.setReply(generateFallbackSongReply(songDTOs));
        }
    }
    
    private void handleScenePlaylist(com.ktvai.assistant.dto.ChatResponse response, IntentDTO intent) {
        List<PlaylistDTO> playlists = sceneRecommender.recommendPlaylists(intent.getScene());
        
        response.setPlaylists(playlists);
        
        if (playlists.isEmpty()) {
            response.setReply("没有找到相关场景歌单");
        } else {
            StringBuilder playlistInfo = new StringBuilder();
            for (int i = 0; i < playlists.size(); i++) {
                PlaylistDTO playlist = playlists.get(i);
                playlistInfo.append(i + 1).append(". ").append(playlist.getPlaylistName())
                        .append(" - ").append(playlist.getDescription()).append("\n");
            }
            
            try {
                String reply = deepSeekClient.chat(
                        "你是一个专业的KTV智能点歌助手。根据场景推荐歌单，用热情、有感染力的语气向用户介绍，不要超过100字。",
                        "用户需要：" + intent.getScene() + "场景的歌单\n推荐的歌单：\n" + playlistInfo
                );
                response.setReply(chooseAiReply(response, reply, generateFallbackPlaylistReply(playlists)));
            } catch (Exception e) {
                response.setReply(generateFallbackPlaylistReply(playlists));
            }
        }
    }
    
    private void handleGenreSearch(com.ktvai.assistant.dto.ChatResponse response, IntentDTO intent, String preferences) {
        List<Song> songs = songRetriever.retrieveWithRerank(intent, preferences);
        
        if (songs.isEmpty()) {
            response.setReply("没有找到相关曲风的歌曲");
            return;
        }
        
        List<SongDTO> songDTOs = songs.stream()
                .map(this::convertToSongDTO)
                .collect(Collectors.toList());
        
        response.setSongs(songDTOs);
        
        StringBuilder songInfo = new StringBuilder();
        for (int i = 0; i < Math.min(5, songDTOs.size()); i++) {
            SongDTO song = songDTOs.get(i);
            songInfo.append(i + 1).append(". ").append(song.getSongName())
                    .append(" - ").append(song.getSinger()).append("\n");
        }
        
        try {
            String reply = deepSeekClient.chat(
                    "你是一个专业的KTV智能点歌助手。根据曲风推荐歌曲，用专业、热情的语气向用户介绍，不要超过100字。",
                    "用户需要：" + intent.getGenre() + "曲风的歌曲\n找到的歌曲：\n" + songInfo
            );
            response.setReply(chooseAiReply(response, reply, generateFallbackGenreReply(songDTOs, intent.getGenre())));
        } catch (Exception e) {
            response.setReply(generateFallbackGenreReply(songDTOs, intent.getGenre()));
        }
    }
    
    private void handleChat(com.ktvai.assistant.dto.ChatResponse response, String userInput) {
        try {
            String reply = deepSeekClient.chat(
                    "你是一个友好的KTV智能点歌助手，擅长聊天和帮助用户点歌。请用轻松、友好的语气回复用户的问候或闲聊，不要超过50字。",
                    userInput
            );
            response.setReply(chooseAiReply(response, reply, "你好！我是KTV智能点歌助手，你可以告诉我想唱什么歌。"));
        } catch (Exception e) {
            response.setReply("你好！我是KTV智能点歌助手，你可以告诉我想唱什么歌，或者需要什么场景的歌单。");
        }
        response.setSongs(new ArrayList<>());
        response.setPlaylists(new ArrayList<>());
    }
    
    private String generateFallbackSongReply(List<SongDTO> songs) {
        StringBuilder reply = new StringBuilder("为您找到以下歌曲：\n");
        for (int i = 0; i < Math.min(3, songs.size()); i++) {
            SongDTO song = songs.get(i);
            reply.append(i + 1).append(". ").append(song.getSongName())
                    .append(" - ").append(song.getSinger()).append("\n");
        }
        return reply.toString();
    }
    
    private String generateFallbackPlaylistReply(List<PlaylistDTO> playlists) {
        StringBuilder reply = new StringBuilder("为您推荐以下歌单：\n");
        for (int i = 0; i < playlists.size(); i++) {
            PlaylistDTO playlist = playlists.get(i);
            reply.append(i + 1).append(". ").append(playlist.getPlaylistName())
                    .append(" - ").append(playlist.getDescription()).append("\n");
        }
        return reply.toString();
    }
    
    private String generateFallbackGenreReply(List<SongDTO> songs, String genre) {
        StringBuilder reply = new StringBuilder("为您找到").append(genre).append("曲风的歌曲：\n");
        for (int i = 0; i < Math.min(3, songs.size()); i++) {
            SongDTO song = songs.get(i);
            reply.append(i + 1).append(". ").append(song.getSongName())
                    .append(" - ").append(song.getSinger()).append("\n");
        }
        return reply.toString();
    }
    
    private SongDTO convertToSongDTO(Song song) {
        SongDTO dto = new SongDTO();
        dto.setId(song.getId());
        dto.setSongName(song.getSongName());
        dto.setSinger(song.getSinger());
        dto.setAlbum(song.getAlbum());
        dto.setGenre(song.getGenre());
        dto.setSceneTags(song.getSceneTags());
        dto.setExternalId(song.getExternalId());
        dto.setCoverUrl(song.getCoverUrl());
        dto.setAudioUrl(song.getAudioUrl());
        dto.setPopularity(song.getPopularity());
        dto.setDuration(song.getDuration());
        return dto;
    }

    private String chooseAiReply(com.ktvai.assistant.dto.ChatResponse response, String aiReply, String fallback) {
        if (!deepSeekClient.isConfigured()) {
            response.setAiConfigured(false);
            response.setAiUsed(false);
            response.setAiProvider("DeepSeek 未配置 · 本地规则兜底");
            return fallback;
        }
        if (aiReply == null || aiReply.isBlank()) {
            response.setAiConfigured(true);
            response.setAiUsed(false);
            response.setAiProvider("DeepSeek 调用失败 · 本地规则兜底");
            return fallback;
        }
        response.setAiConfigured(true);
        response.setAiUsed(true);
        response.setAiProvider("DeepSeek 已调用");
        return aiReply;
    }
}
