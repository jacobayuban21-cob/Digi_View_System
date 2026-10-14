package com.digiview.config;

import com.digiview.entity.AchievementEntity;
import com.digiview.entity.QuizRecordEntity;
import com.digiview.entity.UserEntity;
import com.digiview.repository.AchievementRepository;
import com.digiview.repository.QuizRecordRepository;
import com.digiview.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.util.Date;

/**
 * Automatically seeds the database with the default 'student' user and initial quiz record
 * as soon as the server connects to Supabase PostgreSQL, ensuring the tables are never empty.
 */
@Configuration
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final QuizRecordRepository quizRecordRepository;
    private final AchievementRepository achievementRepository;

    public DataInitializer(UserRepository userRepository,
                           QuizRecordRepository quizRecordRepository,
                           AchievementRepository achievementRepository) {
        this.userRepository = userRepository;
        this.quizRecordRepository = quizRecordRepository;
        this.achievementRepository = achievementRepository;
    }

    @Override
    public void run(String... args) {
        try {
            if (!userRepository.existsByUsernameIgnoreCase("student")) {
                UserEntity student = new UserEntity(
                        "Student User",
                        16,
                        "Male",
                        "STEM",
                        "Grade 11 - STEM A",
                        "student",
                        "admin123"
                );
                student.setXp(120);
                userRepository.save(student);
                log.info("✅ Database seeded with default student user: student / admin123");

                QuizRecordEntity sample = new QuizRecordEntity(
                        "student",
                        "Beginner HTML Quiz",
                        5,
                        5,
                        100,
                        new Date().toString()
                );
                quizRecordRepository.save(sample);
                log.info("✅ Database seeded with initial quiz record for student");

                if (!achievementRepository.existsByUsernameIgnoreCaseAndAchievementName("student", "First Quiz")) {
                    achievementRepository.save(new AchievementEntity("student", "First Quiz"));
                }
                if (!achievementRepository.existsByUsernameIgnoreCaseAndAchievementName("student", "HTML Quiz Pass")) {
                    achievementRepository.save(new AchievementEntity("student", "HTML Quiz Pass"));
                }
                log.info("✅ Database seeded with initial achievements for student");
            } else {
                log.info("ℹ️ Default student user already exists in database.");
            }
        } catch (Exception ex) {
            log.error("⚠️ Failed to seed initial database data: {}", ex.getMessage());
        }
    }
}
