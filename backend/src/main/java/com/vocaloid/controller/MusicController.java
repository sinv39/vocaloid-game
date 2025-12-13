package com.vocaloid.controller;

import com.vocaloid.dto.MusicInfoDto;
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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
            
            // 清除缓存
            smartRandomService.clearCache();
            
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
    public ResponseEntity<List<MusicInfoDto>> getAllMusic() {
        // 使用缓存获取歌曲信息，只查询必要字段
        List<Object[]> results = smartRandomService.getMusicInfoForManage();
        List<MusicInfoDto> musicInfoList = results.stream()
                .map(row -> {
                    Integer id = (Integer) row[0];
                    String title = (String) row[1];
                    java.sql.Timestamp timestamp = (java.sql.Timestamp) row[2];
                    java.time.LocalDateTime uploadTime = timestamp.toLocalDateTime();
                    return new MusicInfoDto(id, title, uploadTime);
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(musicInfoList);
    }
    
    // 删除歌曲
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMusic(@PathVariable Integer id) {
        try {
            if (musicRepository.existsById(id)) {
                musicRepository.deleteById(id);
                // 清除缓存
                smartRandomService.clearCache();
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
    
    // 批量上传歌曲（ZIP压缩包）
    @PostMapping("/batch-upload")
    public ResponseEntity<?> batchUploadMusic(
            @RequestParam("file") MultipartFile zipFile) {
        
        try {
            // 检查文件是否为ZIP格式
            String originalFilename = zipFile.getOriginalFilename();
            if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".zip")) {
                return ResponseEntity.badRequest().body("压缩包格式错误，仅支持ZIP格式");
            }
            
            // 支持的音频文件扩展名
            Set<String> audioExtensions = Set.of(
                "mp3", "wav", "m4a", "flac", "ogg", "aac", "wma"
            );
            
            List<MusicEntity> musicList = new ArrayList<>();
            
            // 解压ZIP文件
            try (InputStream inputStream = zipFile.getInputStream();
                 ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
                
                ZipEntry entry;
                while ((entry = zipInputStream.getNextEntry()) != null) {
                    String entryName = entry.getName();
                    
                    // 检查是否为文件夹
                    if (entry.isDirectory()) {
                        return ResponseEntity.badRequest()
                            .body("压缩包内不能包含文件夹，请确保所有文件都在根目录");
                    }
                    
                    // 检查是否在子目录中（包含路径分隔符）
                    if (entryName.contains("/") || entryName.contains("\\")) {
                        return ResponseEntity.badRequest()
                            .body("压缩包内不能包含文件夹，请确保所有文件都在根目录");
                    }
                    
                    // 获取文件扩展名
                    String extension = getFileExtension(entryName);
                    if (extension == null || !audioExtensions.contains(extension.toLowerCase())) {
                        return ResponseEntity.badRequest()
                            .body("压缩包内包含非音频文件：" + entryName + "，请确保所有文件都是音频格式");
                    }
                    
                    // 读取文件内容
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    long totalBytes = 0;
                    final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
                    
                    while ((bytesRead = zipInputStream.read(buffer)) != -1) {
                        totalBytes += bytesRead;
                        if (totalBytes > MAX_FILE_SIZE) {
                            return ResponseEntity.badRequest()
                                .body("文件 " + entryName + " 大小超过5MB限制");
                        }
                        baos.write(buffer, 0, bytesRead);
                    }
                    
                    byte[] fileBytes = baos.toByteArray();
                    
                    // 提取文件名（去除扩展名）作为歌曲标题
                    String title = entryName.substring(0, entryName.lastIndexOf('.'));
                    if (title.isEmpty()) {
                        title = entryName; // 如果没有扩展名，使用完整文件名
                    }
                    
                    // 创建音乐实体
                    MusicEntity music = new MusicEntity(title, fileBytes);
                    musicList.add(music);
                    
                    zipInputStream.closeEntry();
                }
            }
            
            // 批量保存到数据库
            if (!musicList.isEmpty()) {
                musicRepository.saveAll(musicList);
                // 清除缓存
                smartRandomService.clearCache();
                return ResponseEntity.ok()
                    .body("批量上传成功！共上传 " + musicList.size() + " 首歌曲");
            } else {
                return ResponseEntity.badRequest().body("压缩包为空或未包含有效的音频文件");
            }
            
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("文件处理失败：" + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("批量上传失败：" + e.getMessage());
        }
    }
    
    // 获取文件扩展名
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return null;
        }
        return filename.substring(lastDotIndex + 1);
    }
    
    // 生成会话ID
    private String generateSessionId(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        return UUID.nameUUIDFromBytes((ip + userAgent).getBytes()).toString();
    }
}
