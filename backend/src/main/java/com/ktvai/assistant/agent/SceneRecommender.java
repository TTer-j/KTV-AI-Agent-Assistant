package com.ktvai.assistant.agent;

import com.ktvai.assistant.dto.PlaylistDTO;
import com.ktvai.assistant.dto.SongDTO;
import com.ktvai.assistant.entity.Song;
import com.ktvai.assistant.service.SongService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SceneRecommender {
    
    private final SongService songService;
    
    private static final Map<String, String> SCENE_DESCRIPTIONS = new HashMap<>();
    static {
        SCENE_DESCRIPTIONS.put("生日派对", "欢快热闹的生日庆祝歌曲");
        SCENE_DESCRIPTIONS.put("朋友聚会", "轻松愉悦的聚会氛围歌曲");
        SCENE_DESCRIPTIONS.put("车载路上听", "路上耐听、不抢话的华语流行");
        SCENE_DESCRIPTIONS.put("情侣对唱", "适合两个人一起唱的甜蜜对唱");
        SCENE_DESCRIPTIONS.put("公司团建暖场", "熟悉、好唱、能快速热场的歌曲");
        SCENE_DESCRIPTIONS.put("浪漫约会", "甜蜜温馨的情歌");
        SCENE_DESCRIPTIONS.put("毕业季", "青春回忆的校园歌曲");
        SCENE_DESCRIPTIONS.put("婚礼", "幸福甜蜜的婚礼歌曲");
        SCENE_DESCRIPTIONS.put("伤感", "抒情伤感的歌曲");
        SCENE_DESCRIPTIONS.put("动感", "动感十足的舞曲");
        SCENE_DESCRIPTIONS.put("经典", "经典老歌");
    }
    
    public PlaylistDTO genScenePlayList(String scene) {
        PlaylistDTO playlist = new PlaylistDTO();
        playlist.setPlaylistName(scene + "歌单");
        playlist.setDescription(SCENE_DESCRIPTIONS.getOrDefault(scene, scene + "专属歌单"));
        
        List<Song> songs = songService.getSongsByScene(scene);
        
        List<SongDTO> songDTOs = songs.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        playlist.setSongs(songDTOs);
        playlist.setPlayCount(0);
        
        return playlist;
    }
    
    public List<PlaylistDTO> recommendPlaylists(String userInput) {
        List<PlaylistDTO> playlists = new ArrayList<>();
        
        String normalized = userInput == null ? "" : userInput;

        if (normalized.contains("生日")) {
            playlists.add(genScenePlayList("生日派对"));
        }
        if (normalized.contains("聚会") || normalized.contains("朋友") || normalized.contains("蹦迪") || normalized.contains("嗨")) {
            playlists.add(genScenePlayList("朋友聚会"));
        }
        if (normalized.contains("车载") || normalized.contains("路上") || normalized.contains("开车")) {
            playlists.add(genScenePlayList("车载路上听"));
        }
        if (normalized.contains("情侣") || normalized.contains("对唱") || normalized.contains("合唱")) {
            playlists.add(genScenePlayList("情侣对唱"));
        }
        if (normalized.contains("公司") || normalized.contains("团建") || normalized.contains("暖场")) {
            playlists.add(genScenePlayList("公司团建暖场"));
        }
        if (normalized.contains("约会") || normalized.contains("浪漫") || normalized.contains("告白")) {
            playlists.add(genScenePlayList("浪漫约会"));
        }
        if (normalized.contains("毕业")) {
            playlists.add(genScenePlayList("毕业季"));
        }
        if (normalized.contains("婚礼") || normalized.contains("结婚")) {
            playlists.add(genScenePlayList("婚礼"));
        }
        
        if (playlists.isEmpty()) {
            playlists.add(genScenePlayList("KTV热门"));
            playlists.add(genScenePlayList("经典老歌"));
        }
        
        return playlists;
    }
    
    private SongDTO convertToDTO(Song song) {
        SongDTO dto = new SongDTO();
        dto.setId(song.getId());
        dto.setSongName(song.getSongName());
        dto.setSinger(song.getSinger());
        dto.setAlbum(song.getAlbum());
        dto.setGenre(song.getGenre());
        dto.setSceneTags(song.getSceneTags());
        dto.setCoverUrl(song.getCoverUrl());
        dto.setPopularity(song.getPopularity());
        dto.setDuration(song.getDuration());
        return dto;
    }
}
