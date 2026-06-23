package com.ktvai.assistant.controller;

import com.ktvai.assistant.agent.SceneRecommender;
import com.ktvai.assistant.dto.PlaylistDTO;
import com.ktvai.assistant.dto.Result;
import com.ktvai.assistant.entity.UserPlaylist;
import com.ktvai.assistant.mapper.UserPlaylistMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
@Tag(name = "歌单接口", description = "歌单管理和推荐接口")
public class PlayListController {
    
    private final UserPlaylistMapper userPlaylistMapper;
    private final SceneRecommender sceneRecommender;
    
    @GetMapping("/recommend")
    @Operation(summary = "场景歌单推荐", description = "根据场景推荐歌单")
    public Result<List<PlaylistDTO>> recommendPlaylists(@RequestParam(defaultValue = "") String scene) {
        List<PlaylistDTO> playlists = sceneRecommender.recommendPlaylists(scene);
        return Result.success(playlists);
    }
    
    @GetMapping("/user/{userId}")
    @Operation(summary = "用户歌单", description = "获取用户的歌单列表")
    public Result<List<PlaylistDTO>> getUserPlaylists(@PathVariable Long userId) {
        List<UserPlaylist> playlists = userPlaylistMapper.findByUserId(userId);
        List<PlaylistDTO> dtoList = playlists.stream().map(this::convertToDTO).collect(Collectors.toList());
        return Result.success(dtoList);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "歌单详情", description = "获取歌单详情")
    public Result<PlaylistDTO> getPlaylistById(@PathVariable Long id) {
        UserPlaylist playlist = userPlaylistMapper.selectById(id);
        if (playlist == null) {
            return Result.error(404, "歌单不存在");
        }
        return Result.success(convertToDTO(playlist));
    }
    
    @PostMapping
    @Operation(summary = "创建歌单", description = "创建新的歌单")
    public Result<PlaylistDTO> createPlaylist(@RequestBody UserPlaylist playlist) {
        userPlaylistMapper.insert(playlist);
        return Result.success("创建成功", convertToDTO(playlist));
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "更新歌单", description = "更新歌单信息")
    public Result<PlaylistDTO> updatePlaylist(@PathVariable Long id, @RequestBody UserPlaylist playlist) {
        playlist.setId(id);
        userPlaylistMapper.updateById(playlist);
        return Result.success("更新成功", convertToDTO(playlist));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除歌单", description = "删除歌单")
    public Result<Void> deletePlaylist(@PathVariable Long id) {
        userPlaylistMapper.deleteById(id);
        return Result.success(null);
    }
    
    private PlaylistDTO convertToDTO(UserPlaylist playlist) {
        PlaylistDTO dto = new PlaylistDTO();
        dto.setId(playlist.getId());
        dto.setPlaylistName(playlist.getPlaylistName());
        dto.setDescription(playlist.getDescription());
        dto.setPlayCount(playlist.getPlayCount());
        return dto;
    }
}