package com.digiview.core.logic;

import com.digiview.core.model.QuizRecord;

import java.util.List;
import java.util.Locale;

/**
 * Single Authoritative Business Rules Engine for Quiz Progression and Scoring.
 * Shared by Desktop Swing and Android Mobile LMS.
 */
public class QuizProgressionRules {

    public static final int PASSING_PERCENT = 70;
    public static final int INITIAL_HEARTS = 5;
    public static final int XP_PER_CORRECT_ANSWER = 10;
    public static final int XP_PER_LEVEL = 100;

    /**
     * Checks if the student has achieved at least 70% passing score in a specific difficulty.
     * @param difficultyLevel 1 = Easy/Beginner, 2 = Medium/Intermediate, 3 = Hard/Advanced
     * @param history List of past quiz attempts
     */
    public static boolean hasPassedTier(int difficultyLevel, List<? extends QuizRecord> history) {
        if (history == null || history.isEmpty()) return false;

        String targetKeyword;
        String synonymKeyword;
        switch (difficultyLevel) {
            case 1:
                targetKeyword = "easy";
                synonymKeyword = "beginner";
                break;
            case 2:
                targetKeyword = "medium";
                synonymKeyword = "intermediate";
                break;
            case 3:
                targetKeyword = "hard";
                synonymKeyword = "advanced";
                break;
            default:
                return false;
        }

        for (QuizRecord r : history) {
            if (r == null || r.quiz == null) continue;
            String title = r.quiz.toLowerCase(Locale.ROOT);
            if ((title.contains(targetKeyword) || title.contains(synonymKeyword)) && r.percent >= PASSING_PERCENT) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines whether a quiz difficulty tier is currently unlocked for the user.
     * Rule: Level 1 is always unlocked. Level 2 requires Level 1 >= 70%. Level 3 requires Level 2 >= 70%.
     */
    public static boolean isTierUnlocked(int difficultyLevel, List<? extends QuizRecord> history) {
        if (difficultyLevel <= 1) return true; // Level 1 is always open
        if (difficultyLevel == 2) {
            return hasPassedTier(1, history);
        }
        if (difficultyLevel == 3) {
            return hasPassedTier(2, history);
        }
        return true;
    }

    /**
     * Returns an informative lock message explaining the requirement to unlock a difficulty tier.
     */
    public static String getLockReason(int difficultyLevel) {
        if (difficultyLevel == 2) {
            return "Reach at least " + PASSING_PERCENT + "% in Beginner / Easy to unlock Intermediate!";
        }
        if (difficultyLevel == 3) {
            return "Reach at least " + PASSING_PERCENT + "% in Intermediate / Medium to unlock Advanced!";
        }
        return "";
    }

    /**
     * Calculates user level from accumulated XP.
     */
    public static int calculateLevel(int xp) {
        if (xp <= 0) return 1;
        return (xp / XP_PER_LEVEL) + 1;
    }

    /**
     * Calculates rounded percentage from score and total questions.
     */
    public static int calculatePercentage(int score, int total) {
        if (total <= 0) return 0;
        return (int) Math.round((score * 100.0) / total);
    }
}
