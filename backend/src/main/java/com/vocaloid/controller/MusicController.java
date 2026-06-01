package com.vocaloid.controller;

import com.vocaloid.config.SecurityConfig;
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
    
    // 批量上传歌曲（支持文件夹）
    @PostMapping("/batch-upload")
    public ResponseEntity<?> batchUploadMusic(
            @RequestParam("files") MultipartFile[] files) {
        
        int successCount = 0;
        int failCount = 0;
        StringBuilder errorMsg = new StringBuilder();
        
        for (MultipartFile file : files) {
            try {
                // 获取原始文件名
                String originalFilename = file.getOriginalFilename();
                if (originalFilename == null) {
                    failCount++;
                    errorMsg.append("未知文件: 文件名为空\n");
                    continue;
                }
                
                // 从路径中提取纯文件名（去掉文件夹路径）
                // 处理 Windows 和 Unix 风格的路径分隔符
                String pureFilename = originalFilename;
                if (pureFilename.contains("\\")) {
                    // Windows 风格路径
                    pureFilename = pureFilename.substring(pureFilename.lastIndexOf("\\") + 1);
                } else if (pureFilename.contains("/")) {
                    // Unix 风格路径
                    pureFilename = pureFilename.substring(pureFilename.lastIndexOf("/") + 1);
                }
                
                // 检查文件大小（5MB限制）
                if (file.getSize() > 5 * 1024 * 1024) {
                    failCount++;
                    errorMsg.append(pureFilename).append(": 文件过大\n");
                    continue;
                }
                
                // 检查是否为音频文件
                if (!pureFilename.toLowerCase().endsWith(".mp3") && 
                    !pureFilename.toLowerCase().endsWith(".wav") && 
                    !pureFilename.toLowerCase().endsWith(".ogg")) {
                    failCount++;
                    errorMsg.append(pureFilename).append(": 不是支持的音频格式\n");
                    continue;
                }
                
                // 从文件名提取标题（去掉扩展名）
                String title = pureFilename.substring(0, pureFilename.lastIndexOf("."));
                
                // 将文件转换为字节数组
                byte[] fileBytes = file.getBytes();
                
                // 创建音乐实体
                MusicEntity music = new MusicEntity(title, fileBytes);
                musicRepository.save(music);
                successCount++;
                
            } catch (IOException e) {
                failCount++;
                String displayName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "未知文件";
                // 同样提取纯文件名用于错误提示
                if (displayName.contains("\\")) {
                    displayName = displayName.substring(displayName.lastIndexOf("\\") + 1);
                } else if (displayName.contains("/")) {
                    displayName = displayName.substring(displayName.lastIndexOf("/") + 1);
                }
                errorMsg.append(displayName).append(": ").append(e.getMessage()).append("\n");
            }
        }
        
        String result = String.format("批量上传完成！成功：%d首，失败：%d首", successCount, failCount);
        if (errorMsg.length() > 0) {
            result += "\n\n失败详情：\n" + errorMsg.toString();
        }
        
        return ResponseEntity.ok().body(result);
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
    
    // 检查密钥（用于前端验证）
    @PostMapping("/check")
    public ResponseEntity<?> checkSecretKey(@RequestBody Map<String, String> request) {
        String secretKey = request.get("secretKey");
        if (secretKey != null && secretKey.equals(SecurityConfig.ADMIN_SECRET_KEY)) {
            return ResponseEntity.ok().body("密钥验证成功");
        } else {
            return ResponseEntity.status(401).body("密钥错误");
        }
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
    
    // 删除所有歌曲
    @DeleteMapping("/all")
    public ResponseEntity<?> deleteAllMusic() {
        try {
            long count = musicRepository.count();
            musicRepository.deleteAll();
            return ResponseEntity.ok().body("已删除所有歌曲，共 " + count + " 首");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("删除失败：" + e.getMessage());
        }
    }
    
    // 修改歌曲名称
    @PutMapping("/{id}/title")
    public ResponseEntity<?> updateMusicTitle(
            @PathVariable Integer id,
            @RequestBody Map<String, String> request) {
        try {
            Optional<MusicEntity> musicOpt = musicRepository.findById(id);
            if (!musicOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            MusicEntity music = musicOpt.get();
            String newTitle = request.get("title");
            
            if (newTitle == null || newTitle.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("歌曲名称不能为空");
            }
            
            music.setTitle(newTitle.trim());
            musicRepository.save(music);
            
            return ResponseEntity.ok().body("歌曲名称修改成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("修改失败：" + e.getMessage());
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
