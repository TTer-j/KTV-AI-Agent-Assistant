package com.ktvai.assistant.service;

import com.ktvai.assistant.entity.User;
import com.ktvai.assistant.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserPrefService {
    
    private final UserMapper userMapper;
    
    public User getUserById(Long userId) {
        return userMapper.selectById(userId);
    }
    
    public User createUser(User user) {
        userMapper.insert(user);
        return user;
    }
    
    public User updateUser(User user) {
        userMapper.updateById(user);
        return user;
    }
    
    public void updateUserPreferences(Long userId, String tags) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setPreferenceTags(tags);
            userMapper.updateById(user);
        }
    }
    
    public String getUserPreferences(Long userId) {
        User user = userMapper.selectById(userId);
        return user != null ? user.getPreferenceTags() : "";
    }
}