package com.vocaloid.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "music")
public class MusicEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "title", nullable = false, length = 255)
    private String title;
    
    @Lob
    @Column(name = "metadata", columnDefinition = "LONGBLOB")
    private byte[] metadata;
    
    @Column(name = "upload_time", nullable = false)
    private LocalDateTime uploadTime;
    
    // 默认构造函数
    public MusicEntity() {
        this.uploadTime = LocalDateTime.now();
    }
    
    // 带参数的构造函数
    public MusicEntity(String title, byte[] metadata) {
        this.title = title;
        this.metadata = metadata;
        this.uploadTime = LocalDateTime.now();
    }
    
    // Getters and Setters
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
    
    public byte[] getMetadata() {
        return metadata;
    }
    
    public void setMetadata(byte[] metadata) {
        this.metadata = metadata;
    }
    
    public LocalDateTime getUploadTime() {
        return uploadTime;
    }
    
    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }
    
    @Override
    public String toString() {
        return "MusicEntity{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", uploadTime=" + uploadTime +
                '}';
    }
}
