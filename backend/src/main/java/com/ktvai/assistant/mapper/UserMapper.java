package com.ktvai.assistant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ktvai.assistant.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}