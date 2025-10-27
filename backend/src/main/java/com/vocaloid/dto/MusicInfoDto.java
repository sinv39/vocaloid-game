package com.vocaloid.dto;

import java.time.LocalDateTime;

public class MusicInfoDto {
    private Integer id;
    private String title;
    private LocalDateTime uploadTime;
    
    public MusicInfoDto() {}
    
    public MusicInfoDto(Integer id, String title, LocalDateTime uploadTime) {
        this.id = id;
        this.title = title;
        this.uploadTime = uploadTime;
    }
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public LocalDateTime getUploadTime() {
        return uploadTime;
    }
    
    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }
}
