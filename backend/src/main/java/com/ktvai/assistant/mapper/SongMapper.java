package com.ktvai.assistant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ktvai.assistant.entity.Song;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SongMapper extends BaseMapper<Song> {
    
    @Select("SELECT * FROM song WHERE song_name LIKE CONCAT('%', #{keyword}, '%') OR singer LIKE CONCAT('%', #{keyword}, '%')")
    List<Song> searchByKeyword(@Param("keyword") String keyword);
    
    @Select("SELECT * FROM song WHERE genre = #{genre} ORDER BY popularity DESC LIMIT #{limit}")
    List<Song> findByGenre(@Param("genre") String genre, @Param("limit") Integer limit);
    
    @Select("SELECT * FROM song WHERE scene_tags LIKE CONCAT('%', #{scene}, '%') ORDER BY popularity DESC LIMIT #{limit}")
    List<Song> findByScene(@Param("scene") String scene, @Param("limit") Integer limit);

    @Select("SELECT * FROM song WHERE external_id = #{externalId} LIMIT 1")
    Song findByExternalId(@Param("externalId") String externalId);
}
