package com.vocaloid.service;

import com.vocaloid.entity.MusicEntity;
import com.vocaloid.repository.MusicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SmartRandomService {
    
    @Autowired
    private MusicRepository musicRepository;
    
    // 存储每个会话的已播放歌曲
    private final Map<String, Set<Integer>> sessionPlayedSongs = new ConcurrentHashMap<>();
    
    /**
     * 获取智能随机歌曲
     * @param sessionId 会话ID
     * @return 随机歌曲
     */
    public Optional<MusicEntity> getSmartRandomSong(String sessionId) {
        // 获取当前会话已播放的歌曲
        Set<Integer> playedSongs = sessionPlayedSongs.getOrDefault(sessionId, new HashSet<>());
        
        // 获取所有歌曲
        List<MusicEntity> allSongs = musicRepository.findAll();
        if (allSongs.isEmpty()) {
            return Optional.empty();
        }
        
        // 如果所有歌曲都已播放过，重置会话记录
        if (playedSongs.size() >= allSongs.size()) {
            playedSongs.clear();
            sessionPlayedSongs.put(sessionId, playedSongs);
        }
        
        // 获取未播放的歌曲
        List<MusicEntity> unplayedSongs = new ArrayList<>();
        for (MusicEntity song : allSongs) {
            if (!playedSongs.contains(song.getId())) {
                unplayedSongs.add(song);
            }
        }
        
        // 如果没有未播放的歌曲，返回随机歌曲
        if (unplayedSongs.isEmpty()) {
            return musicRepository.findRandomMusic();
        }
        
        // 随机选择一首未播放的歌曲
        Random random = new Random();
        MusicEntity selectedSong = unplayedSongs.get(random.nextInt(unplayedSongs.size()));
        
        // 更新记录
        playedSongs.add(selectedSong.getId());
        sessionPlayedSongs.put(sessionId, playedSongs);
        
        return Optional.of(selectedSong);
    }
    
    
    /**
     * 重置会话记录
     */
    public void resetSession(String sessionId) {
        sessionPlayedSongs.remove(sessionId);
    }
    
    /**
     * 获取会话统计信息
     */
    public Map<String, Object> getSessionStats(String sessionId) {
        Set<Integer> playedSongs = sessionPlayedSongs.getOrDefault(sessionId, new HashSet<>());
        long totalSongs = musicRepository.countAllMusic();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("playedCount", playedSongs.size());
        stats.put("totalSongs", totalSongs);
        stats.put("remainingSongs", totalSongs - playedSongs.size());
        stats.put("completionRate", totalSongs > 0 ? (double) playedSongs.size() / totalSongs : 0.0);
        
        return stats;
    }
    
}
