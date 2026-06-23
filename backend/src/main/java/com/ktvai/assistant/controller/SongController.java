package com.ktvai.assistant.controller;

import com.ktvai.assistant.dto.Result;
import com.ktvai.assistant.dto.SongDTO;
import com.ktvai.assistant.entity.Song;
import com.ktvai.assistant.service.SongService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/songs")
@RequiredArgsConstructor
@Tag(name = "歌曲接口", description = "歌曲检索和管理接口")
public class SongController {
    
    private final SongService songService;
    
    @GetMapping("/search")
    @Operation(summary = "搜索歌曲", description = "根据关键词搜索歌曲")
    public Result<List<SongDTO>> searchSongs(@RequestParam String keyword) {
        List<Song> songs = songService.searchSongs(keyword);
        List<SongDTO> dtoList = songs.stream().map(this::convertToDTO).collect(Collectors.toList());
        return Result.success(dtoList);
    }
    
    @GetMapping("/hot")
    @Operation(summary = "热门歌曲", description = "获取热门歌曲列表")
    public Result<List<SongDTO>> getHotSongs(@RequestParam(defaultValue = "10") int limit) {
        List<Song> songs = songService.getHotSongs(limit);
        List<SongDTO> dtoList = songs.stream().map(this::convertToDTO).collect(Collectors.toList());
        return Result.success(dtoList);
    }
    
    @GetMapping("/scene/{scene}")
    @Operation(summary = "场景歌曲", description = "根据场景获取推荐歌曲")
    public Result<List<SongDTO>> getSongsByScene(@PathVariable String scene) {
        List<Song> songs = songService.getSongsByScene(scene);
        List<SongDTO> dtoList = songs.stream().map(this::convertToDTO).collect(Collectors.toList());
        return Result.success(dtoList);
    }
    
    @GetMapping("/genre/{genre}")
    @Operation(summary = "曲风歌曲", description = "根据曲风获取推荐歌曲")
    public Result<List<SongDTO>> getSongsByGenre(@PathVariable String genre) {
        List<Song> songs = songService.getSongsByGenre(genre);
        List<SongDTO> dtoList = songs.stream().map(this::convertToDTO).collect(Collectors.toList());
        return Result.success(dtoList);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "歌曲详情", description = "根据ID获取歌曲详情")
    public Result<SongDTO> getSongById(@PathVariable Long id) {
        Song song = songService.getSongById(id);
        if (song == null) {
            return Result.error(404, "歌曲不存在");
        }
        return Result.success(convertToDTO(song));
    }
    
    @PostMapping
    @Operation(summary = "添加歌曲", description = "添加新歌曲")
    public Result<SongDTO> addSong(@RequestBody Song song) {
        Song saved = songService.saveSong(song);
        return Result.success("添加成功", convertToDTO(saved));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除歌曲", description = "删除歌曲")
    public Result<Void> deleteSong(@PathVariable Long id) {
        songService.deleteSong(id);
        return Result.success(null);
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
        dto.setAudioUrl(song.getAudioUrl());
        dto.setPopularity(song.getPopularity());
        dto.setDuration(song.getDuration());
        return dto;
    }
}