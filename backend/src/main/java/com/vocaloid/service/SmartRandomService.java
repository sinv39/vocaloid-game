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
    
    // 缓存所有歌曲ID列表，避免重复查询
    private volatile List<Integer> cachedSongIds = null;
    private volatile long lastCacheUpdate = 0;
    private static final long CACHE_DURATION = 5 * 60 * 1000; // 5分钟缓存
    
    // 缓存管理页面歌曲信息列表
    private volatile List<Object[]> cachedMusicInfo = null;
    private volatile long lastMusicInfoCacheUpdate = 0;
    
    /**
     * 获取智能随机歌曲
     * @param sessionId 会话ID
     * @return 随机歌曲
     */
    public Optional<MusicEntity> getSmartRandomSong(String sessionId) {
        // 获取当前会话已播放的歌曲
        Set<Integer> playedSongs = sessionPlayedSongs.getOrDefault(sessionId, new HashSet<>());
        
        // 获取所有歌曲ID（使用缓存）
        List<Integer> allSongIds = getAllSongIds();
        if (allSongIds.isEmpty()) {
            return Optional.empty();
        }
        
        // 如果所有歌曲都已播放过，重置会话记录
        if (playedSongs.size() >= allSongIds.size()) {
            playedSongs.clear();
            sessionPlayedSongs.put(sessionId, playedSongs);
        }
        
        // 获取未播放的歌曲ID
        List<Integer> unplayedSongIds = new ArrayList<>();
        for (Integer songId : allSongIds) {
            if (!playedSongs.contains(songId)) {
                unplayedSongIds.add(songId);
            }
        }
        
        // 如果没有未播放的歌曲，返回随机歌曲
        if (unplayedSongIds.isEmpty()) {
            return musicRepository.findRandomMusic();
        }
        
        // 随机选择一首未播放的歌曲ID
        Random random = new Random();
        Integer selectedSongId = unplayedSongIds.get(random.nextInt(unplayedSongIds.size()));
        
        // 只查询选中的歌曲（不包含音频数据）
        Optional<MusicEntity> selectedSong = musicRepository.findById(selectedSongId);
        if (!selectedSong.isPresent()) {
            return Optional.empty();
        }
        
        // 更新记录
        playedSongs.add(selectedSongId);
        sessionPlayedSongs.put(sessionId, playedSongs);
        
        return selectedSong;
    }
    
    
    /**
     * 获取所有歌曲ID（带缓存）
     */
    private List<Integer> getAllSongIds() {
        long currentTime = System.currentTimeMillis();
        
        // 检查缓存是否有效
        if (cachedSongIds != null && (currentTime - lastCacheUpdate) < CACHE_DURATION) {
            return cachedSongIds;
        }
        
        // 缓存过期或不存在，重新查询
        synchronized (this) {
            // 双重检查锁定
            if (cachedSongIds != null && (currentTime - lastCacheUpdate) < CACHE_DURATION) {
                return cachedSongIds;
            }
            
            // 只查询ID，不查询音频数据
            List<Integer> songIds = musicRepository.findAllSongIds();
            cachedSongIds = new ArrayList<>(songIds);
            lastCacheUpdate = currentTime;
            
            return cachedSongIds;
        }
    }
    
    /**
     * 获取管理页面歌曲信息（带缓存）
     */
    public List<Object[]> getMusicInfoForManage() {
        long currentTime = System.currentTimeMillis();
        
        // 检查缓存是否有效
        if (cachedMusicInfo != null && (currentTime - lastMusicInfoCacheUpdate) < CACHE_DURATION) {
            return cachedMusicInfo;
        }
        
        // 缓存过期或不存在，重新查询
        synchronized (this) {
            // 双重检查锁定
            if (cachedMusicInfo != null && (currentTime - lastMusicInfoCacheUpdate) < CACHE_DURATION) {
                return cachedMusicInfo;
            }
            
            // 只查询必要字段，不查询音频数据
            List<Object[]> musicInfo = musicRepository.findAllMusicInfo();
            cachedMusicInfo = new ArrayList<>(musicInfo);
            lastMusicInfoCacheUpdate = currentTime;
            
            return cachedMusicInfo;
        }
    }
    
    /**
     * 清除缓存（当歌曲被添加或删除时调用）
     */
    public void clearCache() {
        synchronized (this) {
            cachedSongIds = null;
            lastCacheUpdate = 0;
            cachedMusicInfo = null;
            lastMusicInfoCacheUpdate = 0;
        }
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
        List<Integer> allSongIds = getAllSongIds();
        int totalSongs = allSongIds.size();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("playedCount", playedSongs.size());
        stats.put("totalSongs", totalSongs);
        stats.put("remainingSongs", totalSongs - playedSongs.size());
        stats.put("completionRate", totalSongs > 0 ? (double) playedSongs.size() / totalSongs : 0.0);
        
        return stats;
    }
    
}
