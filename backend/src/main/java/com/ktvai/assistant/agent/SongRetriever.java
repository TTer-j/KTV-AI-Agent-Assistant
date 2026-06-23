package com.ktvai.assistant.agent;

import com.ktvai.assistant.dto.IntentDTO;
import com.ktvai.assistant.entity.Song;
import com.ktvai.assistant.service.SongService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SongRetriever {
    
    private final SongService songService;
    
    public List<Song> retrieveSong(IntentDTO intent) {
        List<Song> results = new ArrayList<>();
        
        switch (intent.getType()) {
            case "SONG_SEARCH":
                if (intent.getSongName() != null && !intent.getSongName().isEmpty()) {
                    results = songService.searchSongs(intent.getSongName());
                }
                if (results.isEmpty() && intent.getSinger() != null) {
                    results = songService.searchSongs(intent.getSinger());
                }
                break;
                
            case "SCENE_PLAYLIST":
                if (intent.getScene() != null) {
                    results = songService.getSongsByScene(intent.getScene());
                }
                break;
                
            case "GENRE_SEARCH":
                if (intent.getGenre() != null) {
                    results = songService.getSongsByGenre(intent.getGenre());
                }
                break;
                
            default:
                results = songService.getHotSongs(10);
        }
        
        return results;
    }
    
    public List<Song> retrieveWithRerank(IntentDTO intent, String userPreferences) {
        List<Song> songs = retrieveSong(intent);
        
        if (userPreferences == null || userPreferences.isEmpty()) {
            return songs;
        }
        
        return songs.stream()
                .map(song -> {
                    int score = calculateScore(song, userPreferences);
                    return new RankedSong(song, score);
                })
                .sorted((a, b) -> b.score - a.score)
                .map(RankedSong::song)
                .collect(Collectors.toList());
    }
    
    private int calculateScore(Song song, String preferences) {
        int score = song.getPopularity() / 100;
        
        if (preferences.contains(song.getGenre())) {
            score += 50;
        }
        if (preferences.contains(song.getSinger())) {
            score += 30;
        }
        if (preferences.contains(song.getSceneTags())) {
            score += 20;
        }
        
        return score;
    }
    
    private record RankedSong(Song song, int score) {}
}