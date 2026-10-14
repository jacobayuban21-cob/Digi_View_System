package com.digiview.repository;

import com.digiview.entity.QuizRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRecordRepository extends JpaRepository<QuizRecordEntity, Long> {
    List<QuizRecordEntity> findByUsernameIgnoreCaseOrderByIdDesc(String username);
}
