package com.digiview.repository;

import com.digiview.entity.AchievementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AchievementRepository extends JpaRepository<AchievementEntity, Long> {
    List<AchievementEntity> findByUsernameIgnoreCase(String username);
    boolean existsByUsernameIgnoreCaseAndAchievementName(String username, String achievementName);
}
