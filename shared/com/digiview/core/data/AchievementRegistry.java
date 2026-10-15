package com.digiview.core.data;

import com.digiview.core.logic.QuizProgressionRules;
import com.digiview.core.model.Achievement;
import com.digiview.core.model.QuizRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Single Authoritative Evaluator for Achievements in DigiView LMS (Desktop & Mobile).
 */
public class AchievementRegistry {

    public static List<Achievement> evaluateAchievements(List<? extends QuizRecord> history, int xp, Set<String> customUnlocked) {
        List<Achievement> list = new ArrayList<>();

        int taken = (history != null) ? history.size() : 0;
        boolean passedAny = false;
        boolean perfect = (customUnlocked != null && customUnlocked.contains("Perfect Score"));

        if (history != null) {
            for (QuizRecord r : history) {
                if (r == null) continue;
                if (r.percent >= QuizProgressionRules.PASSING_PERCENT) {
                    passedAny = true;
                }
                if (r.percent == 100) {
                    perfect = true;
                }
            }
        }

        boolean easyPassed = QuizProgressionRules.hasPassedTier(1, history);
        boolean medPassed = QuizProgressionRules.hasPassedTier(2, history);
        boolean hardPassed = QuizProgressionRules.hasPassedTier(3, history);

        list.add(new Achievement("🎯", "First Quiz", "Complete at least one quiz.", taken, 1));
        list.add(new Achievement("⭐", "HTML Quiz Pass", "Earn 70% or higher on a quiz.", passedAny ? 1 : 0, 1));
        list.add(new Achievement("🏆", "Perfect Score", "Get every question correct in a quiz.", perfect ? 1 : 0, 1));
        list.add(new Achievement("🌱", "Easy Master", "Pass the Easy quiz.", easyPassed ? 1 : 0, 1));
        list.add(new Achievement("⚡", "Medium Master", "Pass the Medium quiz.", medPassed ? 1 : 0, 1));
        list.add(new Achievement("🔥", "Hard Master", "Pass the Hard quiz.", hardPassed ? 1 : 0, 1));
        list.add(new Achievement("📈", "Level Up", "Reach Level 2 (100 XP).", xp, 100));
        list.add(new Achievement("📚", "Persistent Learner", "Complete 5 quizzes.", taken, 5));
        list.add(new Achievement("💎", "XP Collector", "Earn 500 XP.", xp, 500));

        return Collections.unmodifiableList(list);
    }
}
