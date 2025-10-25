package com.vocaloid.controller;

import com.vocaloid.entity.MusicEntity;
import com.vocaloid.repository.MusicRepository;
import com.vocaloid.service.SmartRandomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/music")
@CrossOrigin(origins = "*")
public class MusicController {
    
    @Autowired
    private MusicRepository musicRepository;
    
    @Autowired
    private SmartRandomService smartRandomService;
    
    // 上传歌曲
    @PostMapping("/upload")
    public ResponseEntity<?> uploadMusic(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title) {
        
        try {
            // 检查文件大小（5MB限制）
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest().body("文件大小不能超过5MB");
            }
            
            // 将文件转换为BASE64编码的字节数组
            byte[] fileBytes = file.getBytes();
            
            // 创建音乐实体
            MusicEntity music = new MusicEntity(title, fileBytes);
            musicRepository.save(music);
            
            return ResponseEntity.ok().body("歌曲上传成功！");
            
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("文件上传失败：" + e.getMessage());
        }
    }
    
    // 获取智能随机歌曲
    @GetMapping("/random")
    public ResponseEntity<?> getRandomMusic(HttpServletRequest request) {
        // 生成会话ID（基于IP和User-Agent）
        String sessionId = generateSessionId(request);
        
        Optional<MusicEntity> music = smartRandomService.getSmartRandomSong(sessionId);
        if (music.isPresent()) {
            return ResponseEntity.ok(music.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // 获取会话统计信息
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSessionStats(HttpServletRequest request) {
        String sessionId = generateSessionId(request);
        Map<String, Object> stats = smartRandomService.getSessionStats(sessionId);
        return ResponseEntity.ok(stats);
    }
    
    // 重置会话
    @PostMapping("/reset")
    public ResponseEntity<?> resetSession(HttpServletRequest request) {
        String sessionId = generateSessionId(request);
        smartRandomService.resetSession(sessionId);
        return ResponseEntity.ok().body("会话已重置");
    }
    
    // 获取所有歌曲列表（管理页面用）
    @GetMapping("/list")
    public ResponseEntity<List<MusicEntity>> getAllMusic() {
        List<MusicEntity> musicList = musicRepository.findAllByOrderByUploadTimeDesc();
        return ResponseEntity.ok(musicList);
    }
    
    // 删除歌曲
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMusic(@PathVariable Integer id) {
        try {
            if (musicRepository.existsById(id)) {
                musicRepository.deleteById(id);
                return ResponseEntity.ok().body("歌曲删除成功");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("删除失败：" + e.getMessage());
        }
    }
    
    // 播放歌曲文件
    @GetMapping("/play/{id}")
    public ResponseEntity<byte[]> playMusic(@PathVariable Integer id) {
        try {
            Optional<MusicEntity> musicOpt = musicRepository.findById(id);
            if (!musicOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            MusicEntity music = musicOpt.get();
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            // 设置为audio/mpeg以支持浏览器直接播放
            headers.setContentType(MediaType.valueOf("audio/mpeg"));
            headers.setContentDispositionFormData("inline", music.getTitle() + ".mp3");
            // 添加跨域支持
            headers.setAccessControlAllowOrigin("*");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(music.getMetadata());
                    
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // 生成会话ID
    private String generateSessionId(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        return UUID.nameUUIDFromBytes((ip + userAgent).getBytes()).toString();
    }
}
