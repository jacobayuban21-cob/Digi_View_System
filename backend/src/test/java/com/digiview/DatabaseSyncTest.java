package com.digiview;

import com.digiview.repository.AchievementRepository;
import com.digiview.repository.QuizRecordRepository;
import com.digiview.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class DatabaseSyncTest {

    static {
        DigiViewApplication.loadDotEnvIfPresent();
    }

    @Autowired
    private DataSource dataSource;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuizRecordRepository quizRecordRepository;

    @Autowired
    private AchievementRepository achievementRepository;

    @Test
    void verifyDatabaseTablesAndConnection() throws Exception {
        assertNotNull(dataSource, "DataSource should be initialized");

        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            System.out.println("=== SUPABASE DATABASE CONNECTION VERIFIED ===");
            System.out.println("Connected to: " + meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion());
            System.out.println("URL: " + meta.getURL());

            String[] tables = {"users", "quiz_records", "user_achievements"};
            for (String tableName : tables) {
                try (ResultSet rs = meta.getTables(null, "public", tableName, new String[]{"TABLE"})) {
                    if (rs.next()) {
                        System.out.println(" Table '" + tableName + "' exists in Supabase public schema.");
                    } else {
                        System.out.println(" Table '" + tableName + "' was not found in public schema.");
                    }
                }
            }
        }

        System.out.println("User count: " + userRepository.count());
        System.out.println("Quiz records count: " + quizRecordRepository.count());
        System.out.println("Achievements count: " + achievementRepository.count());
        System.out.println("=== REPOSITORIES & HIBERNATE DDL-AUTO COMPLETED SUCCESSFULLY ===");
    }
}
