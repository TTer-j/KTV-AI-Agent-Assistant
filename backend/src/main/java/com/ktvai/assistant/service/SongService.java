package com.ktvai.assistant.service;

import com.ktvai.assistant.entity.Song;
import com.ktvai.assistant.external.MusicApiClient;
import com.ktvai.assistant.mapper.SongMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SongService {
    
    private final SongMapper songMapper;
    private final MusicApiClient musicApiClient;
    
    public List<Song> searchSongs(String keyword) {
        List<Song> apiSongs = musicApiClient.searchSongs(keyword);
        for (Song song : apiSongs) {
            safeInsertMockSong(song);
        }
        if (!apiSongs.isEmpty()) {
            return apiSongs;
        }

        List<Song> localSongs = songMapper.searchByKeyword(keyword);
        return localSongs;
    }
    
    public List<Song> getSongsByScene(String scene) {
        List<Song> localSongs = songMapper.findByScene(scene, 10);
        if (!localSongs.isEmpty()) {
            return localSongs;
        }
        return musicApiClient.getSongsByScene(scene);
    }
    
    public List<Song> getSongsByGenre(String genre) {
        List<Song> localSongs = songMapper.findByGenre(genre, 10);
        if (!localSongs.isEmpty()) {
            return localSongs;
        }
        return musicApiClient.getSongsByGenre(genre);
    }
    
    public Song getSongById(Long id) {
        return songMapper.selectById(id);
    }
    
    public List<Song> getHotSongs(int limit) {
        List<Song> localSongs = songMapper.selectList(null);
        if (localSongs.isEmpty()) {
            List<Song> apiSongs = musicApiClient.searchSongs("");
            for (Song song : apiSongs) {
                safeInsertMockSong(song);
            }
            return apiSongs.stream()
                    .sorted(this::compareHotSong)
                    .limit(limit)
                    .collect(Collectors.toList());
        }
        return localSongs.stream()
                .sorted(this::compareHotSong)
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    public Song saveSong(Song song) {
        songMapper.insert(song);
        return song;
    }
    
    public void deleteSong(Long id) {
        songMapper.deleteById(id);
    }

    private void safeInsertMockSong(Song song) {
        try {
            if (song.getId() != null && songMapper.selectById(song.getId()) != null) {
                return;
            }
            if (song.getExternalId() != null) {
                Song existing = songMapper.findByExternalId(song.getExternalId());
                if (existing != null) {
                    song.setId(existing.getId());
                    return;
                }
            }
            songMapper.insert(song);
        } catch (Exception e) {
            log.warn("Skip caching song {} - {}: {}", song.getSongName(), song.getSinger(), e.getMessage());
        }
    }

    private int compareHotSong(Song a, Song b) {
        int playableCompare = Boolean.compare(hasAudio(b), hasAudio(a));
        if (playableCompare != 0) {
            return playableCompare;
        }
        return Integer.compare(safePopularity(b), safePopularity(a));
    }

    private boolean hasAudio(Song song) {
        return song != null && song.getAudioUrl() != null && !song.getAudioUrl().isBlank();
    }

    private int safePopularity(Song song) {
        return song == null || song.getPopularity() == null ? 0 : song.getPopularity();
    }
}
