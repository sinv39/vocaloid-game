package com.vocaloid.repository;

import com.vocaloid.entity.MusicEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MusicRepository extends JpaRepository<MusicEntity, Integer> {
    
    @Query(value = "SELECT * FROM music ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<MusicEntity> findRandomMusic();
    
    @Query(value = "SELECT * FROM music WHERE id NOT IN :excludedIds ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<MusicEntity> findRandomMusicExcluding(@Param("excludedIds") List<Integer> excludedIds);
    
    @Query(value = "SELECT COUNT(*) FROM music", nativeQuery = true)
    long countAllMusic();
    
    List<MusicEntity> findByTitleContainingIgnoreCase(String title);
    
    List<MusicEntity> findAllByOrderByUploadTimeDesc();
}
