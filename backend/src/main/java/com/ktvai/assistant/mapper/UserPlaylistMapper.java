package com.ktvai.assistant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ktvai.assistant.entity.UserPlaylist;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserPlaylistMapper extends BaseMapper<UserPlaylist> {
    
    @Select("SELECT * FROM user_playlist WHERE user_id = #{userId} ORDER BY updated_at DESC")
    List<UserPlaylist> findByUserId(@Param("userId") Long userId);
}