package com.digiview.lms;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import androidx.appcompat.app.AppCompatActivity;

import com.digiview.lms.api.ApiClient;
import com.digiview.lms.model.*;
import com.digiview.lms.util.SoundManager;
import com.google.android.material.button.MaterialButton;

import com.digiview.core.model.Question;
import com.digiview.core.model.Lesson;
import com.digiview.core.model.Achievement;
import com.digiview.core.data.QuestionBank;
import com.digiview.core.data.LessonCurriculum;
import com.digiview.core.data.AchievementRegistry;
import com.digiview.core.logic.QuizProgressionRules;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * DigiView LMS - Full Native Android Client
 * Replicates the complete Dark-themed desktop Java Swing application:
 * Dashboard, Lessons curriculum, Interactive Quiz engine, Profile, and Quiz History
 * with offline-first persistence and live Spring Boot / Supabase cloud sync.
 */
public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "DigiViewPrefs";
    private static final String KEY_SERVER_URL = "server_url";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_FULL_NAME = "fullName";
    private static final String KEY_SECTION = "gradeSection";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_XP = "xp";
    private static final String KEY_HISTORY = "history_json";
    private static final String KEY_USERS_JSON = "users_map_json";

    // User Entry for Multi-User Local Persistence
    private static class UserEntry {
        final String username;
        final String password;
        final String fullName;
        final String gradeSection;
        final String gender;
        int xp;

        UserEntry(String username, String password, String fullName, String gradeSection, String gender, int xp) {
            this.username = username;
            this.password = password;
            this.fullName = fullName;
            this.gradeSection = gradeSection;
            this.gender = gender;
            this.xp = xp;
        }
    }
    private final Map<String, UserEntry> usersMap = new HashMap<>();

    // User State
    private String currentUsername = "student";
    private String currentFullName = "Student User";
    private String currentSection = "Grade 11 - STEM A";
    private String currentGender = "Male";
    private int currentXp = 0;
    private boolean isLoggedIn = false;

    // Top Bar Views
    private TextView tvTopXp, tvTopLevel, topAvatar;

    // Fullscreen Auth Overlay Views
    private FrameLayout authScreenContainer;
    private View authLoginCard, authRegisterCard;
    private EditText etLoginUsername, etLoginPassword;
    private TextView tvLoginServerText;
    private View btnLoginServerPill;
    private MaterialButton btnDoLogin, btnGoToRegister;
    private EditText etRegFullName, etRegGrade, etRegSection, etRegPassword, etRegConfirmPassword;
    private Spinner spRegGender;
    private MaterialButton btnDoRegister;
    private TextView btnGoToLogin;

    // Content Frame
    private FrameLayout contentContainer;

    // Bottom Navigation Views (6 Desktop Sections)
    private static final int NUM_PAGES = 6;
    private LinearLayout[] navItems = new LinearLayout[NUM_PAGES];
    private TextView[] navIcons = new TextView[NUM_PAGES];
    private TextView[] navLabels = new TextView[NUM_PAGES];
    private int activePageIndex = 0;

    // Cache of Inflated Views
    private View viewDashboard;
    private View viewLessons;
    private View viewQuizHub;
    private View viewAchievements;
    private View viewHistory;
    private View viewProfile;

    // Lessons Curriculum Model (Shared Blueprint)
    private final List<Lesson> lessonsList = new ArrayList<>();

    // Quiz Engine State (Shared Blueprint)
    private final List<Question> questionBank = new ArrayList<>();
    private final List<Question> activeQuestions = new ArrayList<>();
    private int activeQuestionIndex = 0;
    private int activeScore = 0;
    private int activeHearts = 5;
    private int selectedOptionIndex = -1;
    private boolean isSubmitted = false;
    private String activeDifficultyName = "Beginner";
    private int activeDifficultyLevel = 1;

    // Quiz Hub View References
    private View quizDifficultyView, quizActiveView, quizResultView;
    private TextView tvActiveHearts, tvActiveQuestionNumber, tvActiveQuestionText, tvActiveFeedback;
    private TextView activeQuizTitle, activeQuizProgress;
    private LinearLayout[] activeOptionLayouts = new LinearLayout[4];
    private TextView[] activeOptionTexts = new TextView[4];
    private TextView[] activeOptionBadges = new TextView[4];
    private MaterialButton btnActiveSubmit, btnActiveNext, btnActiveExitQuiz;

    // History Records Cache
    private final List<QuizRecord> historyRecords = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadUserData();
        initLessonsData();
        initQuestionBank();

        bindTopBar();
        bindBottomNav();
        bindAuthScreen();

        // Start on Dashboard underneath
        switchPage(0);

        // Greet user with Desktop-matching Login screen initially
        showAuthScreen();

        // Start study background music automatically
        SoundManager.getInstance(this).startRelaxMusic(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SoundManager.getInstance(this).startRelaxMusic(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            SoundManager.getInstance(this).startRelaxMusic(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        SoundManager.getInstance(this).pauseMusic();
    }

    @Override
    public boolean dispatchTouchEvent(android.view.MotionEvent ev) {
        if (ev.getActionMasked() == android.view.MotionEvent.ACTION_DOWN) {
            View target = findClickableViewAt(getWindow().getDecorView(), (int) ev.getRawX(), (int) ev.getRawY());
            if (target != null) {
                SoundManager.getInstance(this).playClick();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private View findClickableViewAt(View root, int rawX, int rawY) {
        if (root == null || root.getVisibility() != View.VISIBLE) return null;
        int[] loc = new int[2];
        root.getLocationOnScreen(loc);
        int x = loc[0], y = loc[1];
        int w = root.getWidth(), h = root.getHeight();
        if (rawX < x || rawX > x + w || rawY < y || rawY > y + h) {
            return null;
        }
        if (root instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) root;
            for (int i = group.getChildCount() - 1; i >= 0; i--) {
                View childTarget = findClickableViewAt(group.getChildAt(i), rawX, rawY);
                if (childTarget != null) return childTarget;
            }
        }
        if (root.isClickable() || root.isFocusable() || root.hasOnClickListeners()
                || root instanceof android.widget.Checkable
                || root instanceof android.widget.Button
                || root instanceof android.widget.EditText
                || root instanceof android.widget.Spinner) {
            return root;
        }
        return null;
    }

    // =========================================================================
    // 1. DATA PERSISTENCE & CLOUD SYNC
    // =========================================================================
    private void loadUserData() {
        SharedPreferences sp = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedServer = sp.getString(KEY_SERVER_URL, ApiClient.DEFAULT_CLOUD_URL);
        if (savedServer == null || savedServer.contains("digiview-backend.onrender.com")) {
            savedServer = ApiClient.DEFAULT_CLOUD_URL;
            sp.edit().putString(KEY_SERVER_URL, savedServer).apply();
        }
        ApiClient.setBaseUrl(savedServer);

        currentUsername = sp.getString(KEY_USERNAME, "student");
        currentFullName = sp.getString(KEY_FULL_NAME, "Student User");
        currentSection = sp.getString(KEY_SECTION, "Grade 11 - STEM A");
        currentGender = sp.getString(KEY_GENDER, "Male");
        currentXp = sp.getInt(KEY_XP, 0);

        // Load Local Users Map
        usersMap.clear();
        String usersJson = sp.getString(KEY_USERS_JSON, "");
        if (!usersJson.isEmpty()) {
            try {
                JSONObject obj = new JSONObject(usersJson);
                Iterator<String> keys = obj.keys();
                while (keys.hasNext()) {
                    String k = keys.next();
                    JSONObject u = obj.getJSONObject(k);
                    usersMap.put(k.toLowerCase(Locale.ROOT), new UserEntry(
                            u.optString("username", k),
                            u.optString("password", "admin123"),
                            u.optString("fullName", "Student User"),
                            u.optString("gradeSection", "Grade 11 - STEM A"),
                            u.optString("gender", "Male"),
                            u.optInt("xp", 0)
                    ));
                }
            } catch (Exception ignored) {}
        }
        // Seed default demo account matching Desktop
        if (!usersMap.containsKey("student")) {
            usersMap.put("student", new UserEntry(
                    "student",
                    "admin123",
                    "Student User",
                    "Grade 11 - STEM A",
                    "Male",
                    120
            ));
        }

        historyRecords.clear();
        String jsonHistory = sp.getString(KEY_HISTORY, "[]");
        try {
            JSONArray arr = new JSONArray(jsonHistory);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                QuizRecord r = new QuizRecord(
                        obj.optString("username", currentUsername),
                        obj.optString("quiz", "Quiz"),
                        obj.optInt("score", 0),
                        obj.optInt("total", 5),
                        obj.optInt("percent", 0),
                        obj.optString("date", "")
                );
                historyRecords.add(r);
            }
        } catch (Exception ignored) {}
    }

    private void saveUserData() {
        SharedPreferences sp = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putString(KEY_USERNAME, currentUsername);
        ed.putString(KEY_FULL_NAME, currentFullName);
        ed.putString(KEY_SECTION, currentSection);
        ed.putString(KEY_GENDER, currentGender);
        ed.putInt(KEY_XP, currentXp);

        // Update active user in local map
        if (!currentUsername.isEmpty()) {
            UserEntry cur = usersMap.get(currentUsername.toLowerCase(Locale.ROOT));
            String pw = cur != null ? cur.password : "admin123";
            usersMap.put(currentUsername.toLowerCase(Locale.ROOT),
                    new UserEntry(currentUsername, pw, currentFullName, currentSection, currentGender, currentXp));
        }

        JSONObject usersObj = new JSONObject();
        for (Map.Entry<String, UserEntry> entry : usersMap.entrySet()) {
            try {
                JSONObject u = new JSONObject();
                u.put("username", entry.getValue().username);
                u.put("password", entry.getValue().password);
                u.put("fullName", entry.getValue().fullName);
                u.put("gradeSection", entry.getValue().gradeSection);
                u.put("gender", entry.getValue().gender);
                u.put("xp", entry.getValue().xp);
                usersObj.put(entry.getKey(), u);
            } catch (Exception ignored) {}
        }
        ed.putString(KEY_USERS_JSON, usersObj.toString());

        JSONArray arr = new JSONArray();
        for (QuizRecord r : historyRecords) {
            try {
                JSONObject obj = new JSONObject();
                obj.put("username", r.getUsername());
                obj.put("quiz", r.getQuiz());
                obj.put("score", r.getScore());
                obj.put("total", r.getTotal());
                obj.put("percent", r.getPercent());
                obj.put("date", r.getDate());
                arr.put(obj);
            } catch (Exception ignored) {}
        }
        ed.putString(KEY_HISTORY, arr.toString());
        ed.apply();

        updateTopBar();
    }

    // =========================================================================
    // 2. TOP BAR & NAVIGATION
    // =========================================================================
    private void bindTopBar() {
        tvTopXp = findViewById(R.id.tvTopXp);
        tvTopLevel = findViewById(R.id.tvTopLevel);
        topAvatar = findViewById(R.id.topAvatar);

        topAvatar.setOnClickListener(v -> {
            SoundManager.getInstance(this).playClick();
            PopupMenu popup = new PopupMenu(this, topAvatar);
            popup.getMenu().add(0, 1, 0, "👤 View Profile");
            popup.getMenu().add(0, 2, 1, "🚪 Logout");
            popup.setOnMenuItemClickListener(item -> {
                SoundManager.getInstance(this).playClick();
                if (item.getItemId() == 1) {
                    switchPage(5);
                } else if (item.getItemId() == 2) {
                    performLogout();
                }
                return true;
            });
            popup.show();
        });

        View topCloud = findViewById(R.id.topCloudPill);
        if (topCloud != null) {
            topCloud.setVisibility(View.GONE);
        }
        updateTopBar();
    }

    private void updateTopBar() {
        int level = (currentXp / 100) + 1;
        tvTopXp.setText("⚡ " + currentXp + " XP");
        tvTopLevel.setText("Level " + level);
        if (!currentUsername.isEmpty()) {
            topAvatar.setText(currentUsername.substring(0, 1).toUpperCase(Locale.ROOT));
        }
    }

    private void bindBottomNav() {
        contentContainer = findViewById(R.id.contentContainer);

        navItems[0] = findViewById(R.id.navDashboard);
        navItems[1] = findViewById(R.id.navLessons);
        navItems[2] = findViewById(R.id.navQuiz);
        navItems[3] = findViewById(R.id.navAchievements);
        navItems[4] = findViewById(R.id.navHistory);
        navItems[5] = findViewById(R.id.navProfile);

        navIcons[0] = findViewById(R.id.iconNavDashboard);
        navIcons[1] = findViewById(R.id.iconNavLessons);
        navIcons[2] = findViewById(R.id.iconNavQuiz);
        navIcons[3] = findViewById(R.id.iconNavAchievements);
        navIcons[4] = findViewById(R.id.iconNavHistory);
        navIcons[5] = findViewById(R.id.iconNavProfile);

        navLabels[0] = findViewById(R.id.labelNavDashboard);
        navLabels[1] = findViewById(R.id.labelNavLessons);
        navLabels[2] = findViewById(R.id.labelNavQuiz);
        navLabels[3] = findViewById(R.id.labelNavAchievements);
        navLabels[4] = findViewById(R.id.labelNavHistory);
        navLabels[5] = findViewById(R.id.labelNavProfile);

        for (int i = 0; i < NUM_PAGES; i++) {
            final int page = i;
            if (navItems[i] != null) {
                navItems[i].setOnClickListener(v -> switchPage(page));
            }
        }
    }

    private void switchPage(int index) {
        SoundManager.getInstance(this).playClick();
        activePageIndex = index;

        // Highlight Active Tab
        int cyanColor = getResources().getColor(R.color.cyan, getTheme());
        int mutedColor = getResources().getColor(R.color.muted, getTheme());

        for (int i = 0; i < NUM_PAGES; i++) {
            if (navLabels[i] != null) {
                boolean isActive = (i == index);
                navLabels[i].setTextColor(isActive ? cyanColor : mutedColor);
                navLabels[i].setTextSize(isActive ? 11.0f : 10.5f);
            }
        }

        // Animate Page Transition
        contentContainer.removeAllViews();
        View pageView = getOrCreatePageView(index);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        pageView.startAnimation(fadeIn);
        contentContainer.addView(pageView);

        // Refresh dynamic view contents
        if (index == 0) refreshDashboardView();
        else if (index == 1) refreshLessonsView();
        else if (index == 3) refreshAchievementsView();
        else if (index == 4) refreshHistoryView();
        else if (index == 5) refreshProfileView();
    }

    private View getOrCreatePageView(int index) {
        LayoutInflater inflater = LayoutInflater.from(this);
        switch (index) {
            case 0:
                if (viewDashboard == null) viewDashboard = inflater.inflate(R.layout.layout_dashboard, contentContainer, false);
                return viewDashboard;
            case 1:
                if (viewLessons == null) viewLessons = inflater.inflate(R.layout.layout_lessons, contentContainer, false);
                return viewLessons;
            case 2:
                if (viewQuizHub == null) {
                    viewQuizHub = inflater.inflate(R.layout.layout_quiz_hub, contentContainer, false);
                    initQuizHubViews(viewQuizHub);
                }
                return viewQuizHub;
            case 3:
                if (viewAchievements == null) viewAchievements = inflater.inflate(R.layout.layout_achievements, contentContainer, false);
                return viewAchievements;
            case 4:
                if (viewHistory == null) viewHistory = inflater.inflate(R.layout.layout_history, contentContainer, false);
                return viewHistory;
            case 5:
                if (viewProfile == null) viewProfile = inflater.inflate(R.layout.layout_profile, contentContainer, false);
                return viewProfile;
            default:
                return new View(this);
        }
    }

    // =========================================================================
    // 3. DASHBOARD PAGE
    // =========================================================================
    private void refreshDashboardView() {
        if (viewDashboard == null) return;

        TextView tvTitle = viewDashboard.findViewById(R.id.dashWelcomeTitle);
        TextView tvAvatar = viewDashboard.findViewById(R.id.dashAvatarBadge);
        TextView statXp = viewDashboard.findViewById(R.id.dashStatXp);
        TextView statLevel = viewDashboard.findViewById(R.id.dashStatLevel);
        TextView statQuizzes = viewDashboard.findViewById(R.id.dashStatQuizzes);

        tvTitle.setText("Welcome, " + currentFullName + "!");
        if (!currentUsername.isEmpty()) {
            tvAvatar.setText(currentUsername.substring(0, 1).toUpperCase(Locale.ROOT));
        }

        int level = (currentXp / 100) + 1;
        statXp.setText(currentXp + " XP");
        statLevel.setText("Level " + level);
        statQuizzes.setText(historyRecords.size() + " Completed");

        viewDashboard.findViewById(R.id.dashActionLessons).setOnClickListener(v -> switchPage(1));
        viewDashboard.findViewById(R.id.dashActionQuiz).setOnClickListener(v -> switchPage(2));
    }

    // =========================================================================
    // 4. LESSONS CURRICULUM (Single Source of Truth from Shared Core)
    // =========================================================================
    private void initLessonsData() {
        lessonsList.clear();
        lessonsList.addAll(LessonCurriculum.getLessons());
    }

    private void refreshLessonsView() {
        if (viewLessons == null) return;
        LinearLayout container = viewLessons.findViewById(R.id.lessonsListContainer);
        container.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(this);
        for (Lesson item : lessonsList) {
            View card = inflater.inflate(R.layout.item_lesson_card, container, false);
            TextView tvIcon = card.findViewById(R.id.tvLessonItemIcon);
            TextView tvTitle = card.findViewById(R.id.tvLessonItemTitle);
            TextView tvSummary = card.findViewById(R.id.tvLessonItemSummary);

            tvIcon.setText(item.icon);
            tvTitle.setText(item.title);
            tvSummary.setText(item.explanation);

            card.setOnClickListener(v -> {
                SoundManager.getInstance(this).playClick();
                showLessonDetailModal(item);
            });
            container.addView(card);
        }
    }

    private void showLessonDetailModal(Lesson item) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_lesson_detail, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvIcon = dialogView.findViewById(R.id.tvLessonModalIcon);
        TextView tvTitle = dialogView.findViewById(R.id.tvLessonModalTitle);
        TextView tvExp = dialogView.findViewById(R.id.tvLessonModalExplanation);
        TextView tvCode = dialogView.findViewById(R.id.tvLessonModalCode);
        MaterialButton btnClose = dialogView.findViewById(R.id.btnLessonModalClose);

        tvIcon.setText(item.icon);
        tvTitle.setText(item.title);
        tvExp.setText(item.explanation);
        tvCode.setText(item.code);

        btnClose.setOnClickListener(v -> {
            SoundManager.getInstance(this).playClick();
            dialog.dismiss();
        });
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setGravity(Gravity.CENTER);
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.92f);
            int maxWidth = (int) (420 * getResources().getDisplayMetrics().density + 0.5f);
            dialog.getWindow().setLayout(Math.min(width, maxWidth), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    // =========================================================================
    // 5. QUIZ ENGINE (Single Source of Truth from Shared QuestionBank)
    // =========================================================================
    private void initQuestionBank() {
        questionBank.clear();
        questionBank.addAll(QuestionBank.getAllQuestions());
    }

    private void initQuizHubViews(View hub) {
        quizDifficultyView = hub.findViewById(R.id.quizDifficultyView);
        quizActiveView = hub.findViewById(R.id.quizActiveView);
        quizResultView = hub.findViewById(R.id.quizResultView);

        // Difficulty Selector Cards
        hub.findViewById(R.id.btnTierBeginner).setOnClickListener(v -> {
            SoundManager.getInstance(this).playClick();
            promptStartQuiz("Beginner", 1);
        });
        hub.findViewById(R.id.btnTierIntermediate).setOnClickListener(v -> {
            SoundManager.getInstance(this).playClick();
            promptStartQuiz("Intermediate", 2);
        });
        hub.findViewById(R.id.btnTierAdvanced).setOnClickListener(v -> {
            SoundManager.getInstance(this).playClick();
            promptStartQuiz("Advanced", 3);
        });

        // Active Quiz Controls
        activeQuizTitle = hub.findViewById(R.id.activeQuizTitle);
        activeQuizProgress = hub.findViewById(R.id.activeQuizProgress);
        tvActiveHearts = hub.findViewById(R.id.tvActiveHearts);
        tvActiveQuestionNumber = hub.findViewById(R.id.tvActiveQuestionNumber);
        tvActiveQuestionText = hub.findViewById(R.id.tvActiveQuestionText);
        tvActiveFeedback = hub.findViewById(R.id.tvActiveFeedback);

        activeOptionLayouts[0] = hub.findViewById(R.id.activeOptA);
        activeOptionLayouts[1] = hub.findViewById(R.id.activeOptB);
        activeOptionLayouts[2] = hub.findViewById(R.id.activeOptC);
        activeOptionLayouts[3] = hub.findViewById(R.id.activeOptD);

        activeOptionTexts[0] = hub.findViewById(R.id.tvActiveOptionA);
        activeOptionTexts[1] = hub.findViewById(R.id.tvActiveOptionB);
        activeOptionTexts[2] = hub.findViewById(R.id.tvActiveOptionC);
        activeOptionTexts[3] = hub.findViewById(R.id.tvActiveOptionD);

        activeOptionBadges[0] = hub.findViewById(R.id.tvActiveOptionBadgeA);
        activeOptionBadges[1] = hub.findViewById(R.id.tvActiveOptionBadgeB);
        activeOptionBadges[2] = hub.findViewById(R.id.tvActiveOptionBadgeC);
        activeOptionBadges[3] = hub.findViewById(R.id.tvActiveOptionBadgeD);

        btnActiveSubmit = hub.findViewById(R.id.btnActiveSubmit);
        btnActiveNext = hub.findViewById(R.id.btnActiveNext);
        btnActiveExitQuiz = hub.findViewById(R.id.btnActiveExitQuiz);

        for (int i = 0; i < 4; i++) {
            final int optIndex = i;
            activeOptionLayouts[i].setOnClickListener(v -> selectQuizOption(optIndex));
        }

        btnActiveSubmit.setOnClickListener(v -> {
            SoundManager.getInstance(this).playClick();
            submitQuizAnswer();
        });
        btnActiveNext.setOnClickListener(v -> {
            SoundManager.getInstance(this).playClick();
            advanceQuizQuestion();
        });
        btnActiveExitQuiz.setOnClickListener(v -> {
            SoundManager.getInstance(this).playClick();
            exitQuizWithRecord();
        });

        // Result Button
        hub.findViewById(R.id.btnResultReturn).setOnClickListener(v -> {
            SoundManager.getInstance(this).playClick();
            quizResultView.setVisibility(View.GONE);
            quizDifficultyView.setVisibility(View.VISIBLE);
            refreshQuizTierLocks();
        });

        refreshQuizTierLocks();
    }

    private void refreshQuizTierLocks() {
        if (quizDifficultyView == null) return;
        boolean intermediateUnlocked = QuizProgressionRules.isTierUnlocked(2, historyRecords);
        boolean advancedUnlocked = QuizProgressionRules.isTierUnlocked(3, historyRecords);

        View cardInt = quizDifficultyView.findViewById(R.id.btnTierIntermediate);
        TextView tvLockInt = quizDifficultyView.findViewById(R.id.tvLockIntermediate);
        if (cardInt != null) {
            cardInt.setAlpha(intermediateUnlocked ? 1.0f : 0.65f);
        }
        if (tvLockInt != null) {
            tvLockInt.setText(intermediateUnlocked ? "✓ Unlocked" : "🔒 Reach 70% in Beginner");
            tvLockInt.setTextColor(intermediateUnlocked ? Color.parseColor("#34D399") : Color.parseColor("#F59E0B"));
        }

        View cardAdv = quizDifficultyView.findViewById(R.id.btnTierAdvanced);
        TextView tvLockAdv = quizDifficultyView.findViewById(R.id.tvLockAdvanced);
        if (cardAdv != null) {
            cardAdv.setAlpha(advancedUnlocked ? 1.0f : 0.65f);
        }
        if (tvLockAdv != null) {
            tvLockAdv.setText(advancedUnlocked ? "✓ Unlocked" : "🔒 Reach 70% in Beginner");
            tvLockAdv.setTextColor(advancedUnlocked ? Color.parseColor("#34D399") : Color.parseColor("#F59E0B"));
        }
    }

    private void promptStartQuiz(String diffName, int diffLevel) {
        if (!QuizProgressionRules.isTierUnlocked(diffLevel, historyRecords)) {
            showThemedConfirmDialog(
                    "🔒",
                    "Difficulty Locked",
                    QuizProgressionRules.getLockReason(diffLevel),
                    "GOT IT",
                    null,
                    null
            );
            return;
        }

        showThemedConfirmDialog(
                "🚀",
                "Start " + diffName + " Quiz",
                "Are you ready to take the " + diffName + " Quiz?\nYou will have 5 hearts. Each wrong answer deducts 1 heart.",
                "START QUIZ",
                "CANCEL",
                () -> startQuiz(diffName, diffLevel)
        );
    }

    private void startQuiz(String diffName, int diffLevel) {
        activeDifficultyName = diffName;
        activeDifficultyLevel = diffLevel;

        activeQuestions.clear();
        for (Question q : questionBank) {
            if (q.difficulty == diffLevel) {
                activeQuestions.add(q);
            }
        }
        Collections.shuffle(activeQuestions);

        int limit;
        switch (diffLevel) {
            case 1: limit = 25; break;
            case 2: limit = 25; break;
            case 3: limit = 20; break;
            default: limit = 10; break;
        }

        if (activeQuestions.size() > limit) {
            List<Question> trimmed = new ArrayList<>(activeQuestions.subList(0, limit));
            activeQuestions.clear();
            activeQuestions.addAll(trimmed);
        }

        activeQuestionIndex = 0;
        activeScore = 0;
        activeHearts = 5;

        quizDifficultyView.setVisibility(View.GONE);
        quizResultView.setVisibility(View.GONE);
        quizActiveView.setVisibility(View.VISIBLE);

        renderActiveQuestion();
    }

    private void renderActiveQuestion() {
        if (activeQuestionIndex >= activeQuestions.size()) {
            finishQuizSuccess();
            return;
        }

        isSubmitted = false;
        selectedOptionIndex = -1;

        Question q = activeQuestions.get(activeQuestionIndex);
        activeQuizTitle.setText(activeDifficultyName + " Quiz");
        activeQuizProgress.setText("Question " + (activeQuestionIndex + 1) + " of " + activeQuestions.size());
        tvActiveQuestionNumber.setText(String.format("QUESTION %02d", activeQuestionIndex + 1));
        tvActiveQuestionText.setText(q.text);

        updateHeartsText();

        for (int i = 0; i < 4; i++) {
            activeOptionTexts[i].setText(q.options.get(i));
            activeOptionTexts[i].setTextColor(getResources().getColor(R.color.text_white, getTheme()));
            activeOptionTexts[i].setTypeface(null, android.graphics.Typeface.NORMAL);
            activeOptionLayouts[i].setBackgroundResource(R.drawable.bg_option_normal);
            activeOptionLayouts[i].setEnabled(true);
            if (activeOptionBadges[i] != null) {
                activeOptionBadges[i].setBackgroundResource(R.drawable.bg_pill);
                activeOptionBadges[i].setTextColor(getResources().getColor(R.color.cyan, getTheme()));
            }
        }

        tvActiveFeedback.setText("Choose the best answer.");
        tvActiveFeedback.setTextColor(getResources().getColor(R.color.muted, getTheme()));

        btnActiveSubmit.setVisibility(View.VISIBLE);
        btnActiveSubmit.setEnabled(true);
        btnActiveNext.setVisibility(View.GONE);
    }

    private void updateHeartsText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < activeHearts; i++) sb.append("❤️");
        for (int i = activeHearts; i < 5; i++) sb.append("🖤");
        tvActiveHearts.setText(sb.toString());
    }

    private void selectQuizOption(int index) {
        if (isSubmitted) return;
        SoundManager.getInstance(this).playClick();
        selectedOptionIndex = index;

        Animation pop = AnimationUtils.loadAnimation(this, R.anim.card_pop);
        for (int i = 0; i < 4; i++) {
            if (i == index) {
                activeOptionLayouts[i].setBackgroundResource(R.drawable.bg_option_selected);
                activeOptionLayouts[i].startAnimation(pop);
                if (activeOptionBadges[i] != null) {
                    activeOptionBadges[i].setBackgroundResource(R.drawable.bg_badge_selected);
                    activeOptionBadges[i].setTextColor(Color.WHITE);
                }
                activeOptionTexts[i].setTextColor(Color.WHITE);
                activeOptionTexts[i].setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                activeOptionLayouts[i].setBackgroundResource(R.drawable.bg_option_normal);
                if (activeOptionBadges[i] != null) {
                    activeOptionBadges[i].setBackgroundResource(R.drawable.bg_pill);
                    activeOptionBadges[i].setTextColor(getResources().getColor(R.color.cyan, getTheme()));
                }
                activeOptionTexts[i].setTextColor(getResources().getColor(R.color.text_white, getTheme()));
                activeOptionTexts[i].setTypeface(null, android.graphics.Typeface.NORMAL);
            }
        }
    }

    private void submitQuizAnswer() {
        if (selectedOptionIndex == -1) {
            Toast.makeText(this, "Please select an answer first.", Toast.LENGTH_SHORT).show();
            return;
        }

        isSubmitted = true;
        btnActiveSubmit.setEnabled(false);

        Question q = activeQuestions.get(activeQuestionIndex);
        boolean isCorrect = (selectedOptionIndex == q.answer);

        if (isCorrect) {
            activeScore++;
            activeOptionLayouts[selectedOptionIndex].setBackgroundResource(R.drawable.bg_option_correct);
            if (activeOptionBadges[selectedOptionIndex] != null) {
                activeOptionBadges[selectedOptionIndex].setBackgroundResource(R.drawable.bg_badge_success);
                activeOptionBadges[selectedOptionIndex].setTextColor(Color.WHITE);
            }
            tvActiveFeedback.setText("Correct answer! Great job! 🎉");
            tvActiveFeedback.setTextColor(getResources().getColor(R.color.green, getTheme()));
        } else {
            activeHearts--;
            activeOptionLayouts[selectedOptionIndex].setBackgroundResource(R.drawable.bg_option_wrong);
            if (activeOptionBadges[selectedOptionIndex] != null) {
                activeOptionBadges[selectedOptionIndex].setBackgroundResource(R.drawable.bg_badge_danger);
                activeOptionBadges[selectedOptionIndex].setTextColor(Color.WHITE);
            }
            tvActiveFeedback.setText("Incorrect. One heart deducted.");
            tvActiveFeedback.setTextColor(getResources().getColor(R.color.red, getTheme()));
        }

        updateHeartsText();

        if (activeHearts <= 0) {
            showThemedAlertDialog(
                    "💔",
                    "Out of Hearts",
                    "You ran out of hearts! Review the lessons and try again.",
                    "TRY AGAIN",
                    () -> {
                        recordQuizAttempt(activeDifficultyName + " Quiz (Failed)", activeScore, activeQuestions.size(), 0);
                        quizActiveView.setVisibility(View.GONE);
                        quizDifficultyView.setVisibility(View.VISIBLE);
                    }
            );
            return;
        }

        btnActiveSubmit.setVisibility(View.GONE);
        btnActiveNext.setVisibility(View.VISIBLE);
        btnActiveNext.setText(activeQuestionIndex == activeQuestions.size() - 1 ? "View Results →" : "Next Question →");
    }

    private void advanceQuizQuestion() {
        activeQuestionIndex++;
        renderActiveQuestion();
    }

    private void exitQuizWithRecord() {
        showThemedConfirmDialog(
                "⚠️",
                "Exit Quiz?",
                "Are you sure you want to exit? Your attempt will be recorded as exited.",
                "EXIT",
                "CANCEL",
                () -> {
                    recordQuizAttempt(activeDifficultyName + " Quiz (Exited)", activeScore, activeQuestions.size(), 0);
                    quizActiveView.setVisibility(View.GONE);
                    quizDifficultyView.setVisibility(View.VISIBLE);
                }
        );
    }

    private void finishQuizSuccess() {
        quizActiveView.setVisibility(View.GONE);
        quizResultView.setVisibility(View.VISIBLE);

        int total = activeQuestions.size();
        int percent = (int) Math.round((activeScore * 100.0) / total);
        int xpGained = activeScore * 10;
        currentXp += xpGained;

        // Populate Result Card
        TextView tvScore = quizResultView.findViewById(R.id.tvResultScore);
        TextView tvPercent = quizResultView.findViewById(R.id.tvResultPercent);
        ProgressBar pb = quizResultView.findViewById(R.id.resultProgressBar);
        TextView tvBadge = quizResultView.findViewById(R.id.tvResultBadge);
        TextView tvXp = quizResultView.findViewById(R.id.tvResultXpGained);

        tvScore.setText(activeScore + " / " + total);
        tvPercent.setText(percent + "%");
        pb.setProgress(percent);
        tvXp.setText("⚡ +" + xpGained + " XP Earned");

        if (percent == 100) {
            tvBadge.setText("PERFECT SCORE 🏆");
            tvBadge.setTextColor(getResources().getColor(R.color.gold, getTheme()));
            tvBadge.setBackgroundResource(R.drawable.bg_badge_gold);
        } else if (percent >= 70) {
            tvBadge.setText("PASSED ✅");
            tvBadge.setTextColor(getResources().getColor(R.color.green, getTheme()));
            tvBadge.setBackgroundResource(R.drawable.bg_badge_success);
        } else {
            tvBadge.setText("TRY AGAIN ❌");
            tvBadge.setTextColor(getResources().getColor(R.color.red, getTheme()));
            tvBadge.setBackgroundResource(R.drawable.bg_badge_danger);
        }

        if (percent < 70) {
            recordQuizAttempt(activeDifficultyName + " Quiz (Failed)", activeScore, total, percent);
        } else {
            recordQuizAttempt(activeDifficultyName + " Quiz", activeScore, total, percent);
        }
    }

    private void recordQuizAttempt(String title, int score, int total, int percent) {
        String formattedDate = new SimpleDateFormat("MMM dd, yyyy  •  h:mm a", Locale.ENGLISH).format(new Date());
        QuizRecord r = new QuizRecord(currentUsername, title, score, total, percent, formattedDate);
        historyRecords.add(0, r);
        saveUserData();
        refreshQuizTierLocks();

        // Automatic Server Submission and Authoritative Progress Sync
        try {
            ApiClient.getApi().saveQuizRecord(r).enqueue(new Callback<ApiResponse<QuizRecord>>() {
                @Override
                public void onResponse(Call<ApiResponse<QuizRecord>> call, Response<ApiResponse<QuizRecord>> response) {
                    if (response.isSuccessful()) {
                        syncUserFromServer(currentUsername);
                    }
                }
                @Override
                public void onFailure(Call<ApiResponse<QuizRecord>> call, Throwable t) {}
            });
        } catch (Exception ignored) {}
    }

    private void syncUserFromServer(String username) {
        if (username == null || username.trim().isEmpty()) return;
        try {
            ApiClient.getApi().getProfile(username.trim()).enqueue(new Callback<ApiResponse<UserProfile>>() {
                @Override
                public void onResponse(Call<ApiResponse<UserProfile>> call, Response<ApiResponse<UserProfile>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                        UserProfile p = response.body().data;
                        if (p.xp > currentXp) {
                            currentXp = p.xp;
                        }
                        if (p.fullName != null && !p.fullName.isEmpty()) currentFullName = p.fullName;
                        if (p.gradeSection != null && !p.gradeSection.isEmpty()) currentSection = p.gradeSection;
                        if (p.gender != null && !p.gender.isEmpty()) currentGender = p.gender;

                        if (p.history != null && !p.history.isEmpty()) {
                            for (QuizRecord qr : p.history) {
                                boolean exists = false;
                                for (QuizRecord local : historyRecords) {
                                    if (local.getQuiz().equals(qr.getQuiz()) && local.getDate().equals(qr.getDate())) {
                                        exists = true;
                                        break;
                                    }
                                }
                                if (!exists) {
                                    historyRecords.add(qr);
                                }
                            }
                        }

                        saveUserData();
                        runOnUiThread(() -> {
                            updateTopBar();
                            if (activePageIndex == 4) refreshHistoryView();
                            if (activePageIndex == 5) refreshProfileView();
                        });
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<UserProfile>> call, Throwable t) {}
            });
        } catch (Exception ignored) {}
    }

    // =========================================================================
    // 6. PROFILE PAGE
    // =========================================================================
    private void refreshProfileView() {
        if (viewProfile == null) return;

        TextView tvAvatar = viewProfile.findViewById(R.id.profileAvatar);
        TextView tvFullName = viewProfile.findViewById(R.id.profileFullName);
        TextView tvHandle = viewProfile.findViewById(R.id.profileUsername);
        TextView chipLevel = viewProfile.findViewById(R.id.profileChipLevel);
        TextView chipXp = viewProfile.findViewById(R.id.profileChipXp);
        TextView chipSec = viewProfile.findViewById(R.id.profileChipSection);

        TextView tileName = viewProfile.findViewById(R.id.profileTileFullName);
        TextView tileUser = viewProfile.findViewById(R.id.profileTileUsername);
        TextView tileGender = viewProfile.findViewById(R.id.profileTileGender);
        TextView tileSec = viewProfile.findViewById(R.id.profileTileSection);

        TextView tvNextLvl = viewProfile.findViewById(R.id.profileNextLevelTitle);
        TextView tvRatio = viewProfile.findViewById(R.id.profileProgressRatio);
        ProgressBar pbLevel = viewProfile.findViewById(R.id.profileLevelProgressBar);

        int level = (currentXp / 100) + 1;
        int progress = currentXp % 100;

        if (!currentUsername.isEmpty()) {
            tvAvatar.setText(currentUsername.substring(0, 1).toUpperCase(Locale.ROOT));
        }
        tvFullName.setText(currentFullName);
        tvHandle.setText("@" + currentUsername);

        chipLevel.setText("⭐ Level " + level);
        chipXp.setText("⚡ " + currentXp + " XP");
        chipSec.setText("🏫 " + currentSection);

        tileName.setText(currentFullName);
        tileUser.setText(currentUsername);
        tileGender.setText(currentGender);
        tileSec.setText(currentSection);

        tvNextLvl.setText("Progress to Level " + (level + 1));
        tvRatio.setText(currentXp + " / " + (level * 100) + " XP");
        pbLevel.setProgress(progress);

        MaterialButton btnMusic = viewProfile.findViewById(R.id.btnToggleRelaxMusic);
        if (btnMusic != null) {
            boolean isPlaying = SoundManager.getInstance(this).isMusicPlaying();
            btnMusic.setText(isPlaying ? "🎵 Playing Relax Music: ON (Tap to Pause)" : "🎵 Play Relaxing Music: OFF");
            btnMusic.setOnClickListener(v -> {
                SoundManager.getInstance(this).playClick();
                boolean nowPlaying = SoundManager.getInstance(this).toggleRelaxMusic(this);
                btnMusic.setText(nowPlaying ? "🎵 Playing Relax Music: ON (Tap to Pause)" : "🎵 Play Relaxing Music: OFF");
            });
        }

        View btnProfileAuth = viewProfile.findViewById(R.id.btnProfileAuth);
        if (btnProfileAuth != null) {
            btnProfileAuth.setVisibility(View.GONE);
        }

        View btnLogout = viewProfile.findViewById(R.id.btnProfileLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                SoundManager.getInstance(this).playClick();
                performLogout();
            });
        }
    }

    // =========================================================================
    // 7. ACHIEVEMENTS PAGE (SHARED CANONICAL BLUEPRINTS)
    // =========================================================================
    private void refreshAchievementsView() {
        if (viewAchievements == null) return;

        List<Achievement> items = AchievementRegistry.evaluateAchievements(historyRecords, currentXp, null);

        int unlockedCount = 0;
        for (Achievement item : items) {
            if (item.isUnlocked()) unlockedCount++;
        }

        TextView tvSubtitle = viewAchievements.findViewById(R.id.tvAchievementsSubtitle);
        ProgressBar overallBar = viewAchievements.findViewById(R.id.achievementsOverallBar);
        if (tvSubtitle != null) {
            tvSubtitle.setText(unlockedCount + " of " + items.size() + " unlocked — achievements are based on your quiz results.");
        }
        if (overallBar != null) {
            overallBar.setProgress((unlockedCount * 100) / items.size());
        }

        LinearLayout container = viewAchievements.findViewById(R.id.achievementsListContainer);
        if (container == null) return;
        container.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(this);
        for (Achievement item : items) {
            View card = inflater.inflate(R.layout.item_achievement_card, container, false);
            TextView tvIcon = card.findViewById(R.id.tvAchIcon);
            TextView tvStatus = card.findViewById(R.id.tvAchStatus);
            TextView tvTitle = card.findViewById(R.id.tvAchTitle);
            TextView tvDesc = card.findViewById(R.id.tvAchDesc);
            TextView tvProgText = card.findViewById(R.id.tvAchProgressText);
            ProgressBar pb = card.findViewById(R.id.achProgressBar);

            tvIcon.setText(item.icon);
            tvTitle.setText(item.title);
            tvDesc.setText(item.description);

            int shown = Math.min(item.currentProgress, item.targetProgress);
            int percent = item.getProgressPercent();
            tvProgText.setText(shown + " / " + item.targetProgress);
            pb.setProgress(percent);

            if (item.isUnlocked()) {
                tvStatus.setText("✓ Unlocked");
                tvStatus.setTextColor(getResources().getColor(R.color.green, getTheme()));
                tvTitle.setTextColor(getResources().getColor(R.color.text_white, getTheme()));
                tvProgText.setTextColor(getResources().getColor(R.color.cyan, getTheme()));
            } else {
                tvStatus.setText("🔒 Locked");
                tvStatus.setTextColor(getResources().getColor(R.color.amber, getTheme()));
                tvTitle.setTextColor(getResources().getColor(R.color.muted, getTheme()));
                tvProgText.setTextColor(getResources().getColor(R.color.muted, getTheme()));
            }

            container.addView(card);
        }
    }

    // =========================================================================
    // 8. AUTHENTICATION SCREEN (DESKTOP REPLICA LOGIN & REGISTER)
    // =========================================================================
    private void bindAuthScreen() {
        authScreenContainer = findViewById(R.id.authScreenContainer);
        authLoginCard = findViewById(R.id.authLoginCard);
        authRegisterCard = findViewById(R.id.authRegisterCard);

        etLoginUsername = findViewById(R.id.etLoginUsername);
        etLoginPassword = findViewById(R.id.etLoginPassword);
        bindPasswordToggle(R.id.etLoginPassword, R.id.cbShowLoginPassword);
        btnLoginServerPill = findViewById(R.id.btnLoginServerPill);
        tvLoginServerText = findViewById(R.id.tvLoginServerText);
        btnDoLogin = findViewById(R.id.btnDoLogin);
        btnGoToRegister = findViewById(R.id.btnGoToRegister);

        etRegFullName = findViewById(R.id.etRegFullName);
        etRegGrade = findViewById(R.id.etRegGrade);
        etRegSection = findViewById(R.id.etRegSection);
        spRegGender = findViewById(R.id.spRegGender);
        etRegPassword = findViewById(R.id.etRegPassword);
        etRegConfirmPassword = findViewById(R.id.etRegConfirmPassword);
        bindPasswordToggle(R.id.etRegPassword, R.id.cbShowRegPassword);
        bindPasswordToggle(R.id.etRegConfirmPassword, R.id.cbShowRegConfirmPassword);
        btnDoRegister = findViewById(R.id.btnDoRegister);
        btnGoToLogin = findViewById(R.id.btnGoToLogin);

        if (spRegGender != null) {
            ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_dropdown_item,
                    new String[]{"Select gender", "Male", "Female", "Prefer not to say"});
            spRegGender.setAdapter(genderAdapter);
        }

        if (btnGoToRegister != null) {
            btnGoToRegister.setOnClickListener(v -> {
                SoundManager.getInstance(this).playClick();
                authLoginCard.setVisibility(View.GONE);
                authRegisterCard.setVisibility(View.VISIBLE);
                Animation anim = AnimationUtils.loadAnimation(this, R.anim.fade_in);
                authRegisterCard.startAnimation(anim);
            });
        }

        if (btnGoToLogin != null) {
            btnGoToLogin.setOnClickListener(v -> {
                SoundManager.getInstance(this).playClick();
                authRegisterCard.setVisibility(View.GONE);
                authLoginCard.setVisibility(View.VISIBLE);
                Animation anim = AnimationUtils.loadAnimation(this, R.anim.fade_in);
                authLoginCard.startAnimation(anim);
            });
        }

        if (btnLoginServerPill != null) {
            btnLoginServerPill.setVisibility(View.VISIBLE);
            updateServerPillText();
            btnLoginServerPill.setOnClickListener(v -> showServerSettingsDialog());
        }

        if (btnDoLogin != null) {
            btnDoLogin.setOnClickListener(v -> {
                SoundManager.getInstance(this).playClick();
                String u = etLoginUsername != null ? etLoginUsername.getText().toString().trim() : "";
                String p = etLoginPassword != null ? etLoginPassword.getText().toString().trim() : "";
                performLogin(u, p);
            });
        }

        if (btnDoRegister != null) {
            btnDoRegister.setOnClickListener(v -> {
                SoundManager.getInstance(this).playClick();
                performRegister();
            });
        }
    }

    private void showAuthScreen() {
        if (authScreenContainer != null) {
            authScreenContainer.setVisibility(View.VISIBLE);
            if (authLoginCard != null) {
                authLoginCard.setVisibility(View.VISIBLE);
                Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
                authLoginCard.startAnimation(fadeIn);
            }

            if (authRegisterCard != null) {
                authRegisterCard.setVisibility(View.GONE);
            }
        }
    }

    private void updateServerPillText() {
        if (tvLoginServerText != null) {
            tvLoginServerText.setText("Server: " + ApiClient.getBaseUrl() + " (Tap to change)");
        }
    }

    private void showServerSettingsDialog() {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        content.setPadding(padding, 0, padding, 0);

        EditText urlInput = new EditText(this);
        urlInput.setSingleLine(true);
        urlInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_URI);
        urlInput.setHint("http://192.168.1.10:8085/");
        urlInput.setText(ApiClient.getBaseUrl());
        content.addView(urlInput);

        TextView help = new TextView(this);
        help.setText("Use http://10.0.2.2:8085/ for an emulator, or the LAN URL printed by digiview server for a physical phone.");
        help.setTextColor(getResources().getColor(R.color.muted, getTheme()));
        help.setTextSize(12);
        content.addView(help);

        Button testButton = new Button(this);
        testButton.setText("Test Server");
        content.addView(testButton);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("DigiView Server")
                .setView(content)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", null)
                .create();

        testButton.setOnClickListener(v -> testServerUrl(urlInput.getText().toString().trim()));
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String url = urlInput.getText().toString().trim();
            if (url.isEmpty()) {
                urlInput.setError("Enter a server URL");
                return;
            }
            ApiClient.setBaseUrl(url);
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                    .putString(KEY_SERVER_URL, ApiClient.getBaseUrl())
                    .apply();
            updateServerPillText();
            dialog.dismiss();
        }));
        dialog.show();
    }

    private void testServerUrl(String rawUrl) {
        String target = rawUrl;
        if (!target.endsWith("/")) target += "/";
        final String healthUrl = target + "health";
        Toast.makeText(this, "Testing " + healthUrl, Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(healthUrl).openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                int status = connection.getResponseCode();
                final int responseCode = status;
                runOnUiThread(() -> Toast.makeText(this,
                        responseCode >= 200 && responseCode < 400 ? "Server is online (HTTP " + responseCode + ")" : "Server returned HTTP " + responseCode,
                        Toast.LENGTH_LONG).show());
            } catch (Exception e) {
                final String error = e.getMessage();
                runOnUiThread(() -> Toast.makeText(this,
                        "Could not reach server: " + error,
                        Toast.LENGTH_LONG).show());
            } finally {
                if (connection != null) connection.disconnect();
            }
        }).start();
    }

    private void hideAuthScreen() {
        if (authScreenContainer != null) {
            Animation fadeOut = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
            fadeOut.setDuration(250);
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override public void onAnimationStart(Animation animation) {}
                @Override
                public void onAnimationEnd(Animation animation) {
                    authScreenContainer.setVisibility(View.GONE);
                }
                @Override public void onAnimationRepeat(Animation animation) {}
            });
            authScreenContainer.startAnimation(fadeOut);
        }
    }

    private void performLogin(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            showThemedAlertDialog("⚠️", "Missing Information", "Please enter both username and password.", "OK", null);
            return;
        }

        if (btnDoLogin != null) {
            btnDoLogin.setEnabled(false);
            btnDoLogin.setText("SIGNING IN...");
        }

        ApiClient.getApi().login(new AuthRequest(username, password))
                .enqueue(new Callback<ApiResponse<User>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                        if (btnDoLogin != null) {
                            btnDoLogin.setEnabled(true);
                            btnDoLogin.setText("SIGN IN");
                        }

                        if (response.isSuccessful() && response.body() != null && response.body().success && response.body().data != null) {
                            User u = response.body().data;
                            currentUsername = u.username != null ? u.username : username;
                            currentFullName = u.fullName != null ? u.fullName : username;
                            currentSection = u.gradeSection != null ? u.gradeSection : "Grade 11";
                            currentGender = u.gender != null ? u.gender : "Not specified";
                            currentXp = u.xp;
                            isLoggedIn = true;

                            UserEntry newEntry = new UserEntry(currentUsername, password, currentFullName, currentSection, currentGender, currentXp);
                            usersMap.put(currentUsername.toLowerCase(Locale.ROOT), newEntry);
                            saveUserData();

                            // Automatically fetch latest profile & history from server
                            syncUserFromServer(currentUsername);

                            hideAuthScreen();
                            switchPage(0);
                            Toast.makeText(MainActivity.this, "Welcome, " + currentFullName + "!", Toast.LENGTH_SHORT).show();
                        } else {
                            String errorMsg = "Invalid username or password.";
                            if (response.body() != null && response.body().message != null && !response.body().message.isEmpty()) {
                                errorMsg = response.body().message;
                            } else if (response.errorBody() != null) {
                                try {
                                    String errStr = response.errorBody().string();
                                    int mIdx = errStr.indexOf("\"message\":\"");
                                    if (mIdx != -1) {
                                        int end = errStr.indexOf("\"", mIdx + 11);
                                        if (end != -1) errorMsg = errStr.substring(mIdx + 11, end);
                                    }
                                } catch (Exception ignored) {}
                            }
                            showThemedAlertDialog("❌", "Sign In Failed", errorMsg, "TRY AGAIN", null);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                        if (btnDoLogin != null) {
                            btnDoLogin.setEnabled(true);
                            btnDoLogin.setText("SIGN IN");
                        }

                        // Allow offline cached login if local credentials match
                        UserEntry cached = usersMap.get(username.toLowerCase(Locale.ROOT));
                        if (cached != null && cached.password.equals(password)) {
                            currentUsername = cached.username;
                            currentFullName = cached.fullName;
                            currentSection = cached.gradeSection;
                            currentGender = cached.gender;
                            currentXp = cached.xp;
                            isLoggedIn = true;
                            saveUserData();

                            hideAuthScreen();
                            switchPage(0);
                            Toast.makeText(MainActivity.this, "Signed in (Offline Mode).\nServer was unreachable.", Toast.LENGTH_LONG).show();
                            return;
                        }

                        showThemedAlertDialog(
                                "⚠️",
                                "Connection Failed",
                                "Could not connect to DigiView server at " + ApiClient.getBaseUrl() + ".\nPlease check your connection.",
                                "TRY AGAIN",
                                null
                        );
                    }
                });
    }

    private void bindPasswordToggle(int passwordId, int toggleId) {
        EditText password = findViewById(passwordId);
        CheckBox toggle = findViewById(toggleId);
        if (password == null || toggle == null) return;
        toggle.setOnCheckedChangeListener((button, checked) -> {
            int selection = password.getSelectionStart();
            password.setTransformationMethod(checked
                    ? HideReturnsTransformationMethod.getInstance()
                    : PasswordTransformationMethod.getInstance());
            password.setSelection(Math.max(0, selection));
        });
    }

    private void performRegister() {
        String name = etRegFullName != null ? etRegFullName.getText().toString().trim() : "";
        String gr = etRegGrade != null ? etRegGrade.getText().toString().trim() : "";
        String sec = etRegSection != null ? etRegSection.getText().toString().trim() : "";
        String gender = spRegGender != null && spRegGender.getSelectedItem() != null ? spRegGender.getSelectedItem().toString() : "";
        int genderIndex = spRegGender != null ? spRegGender.getSelectedItemPosition() : 0;
        String pw = etRegPassword != null ? etRegPassword.getText().toString().trim() : "";
        String cpw = etRegConfirmPassword != null ? etRegConfirmPassword.getText().toString().trim() : "";

        if (name.length() < 2 || gr.isEmpty() || sec.isEmpty() || genderIndex == 0 || pw.length() < 4) {
            showThemedAlertDialog("⚠️", "Incomplete Form", "Please complete all fields. Password must have at least 4 characters.", "GOT IT", null);
            return;
        }

        if (!pw.equals(cpw)) {
            showThemedAlertDialog("⚠️", "Password Mismatch", "Passwords do not match. Please re-enter your passwords.", "TRY AGAIN", null);
            return;
        }

        String baseUsername = name.replaceAll("[^A-Za-z0-9]", "").toLowerCase(Locale.ROOT);
        if (baseUsername.isEmpty()) baseUsername = "student";

        String fullSec = gr + " - " + sec;

        if (btnDoRegister != null) {
            btnDoRegister.setEnabled(false);
            btnDoRegister.setText("CREATING ACCOUNT...");
        }

        attemptRegisterOnServer(baseUsername, name, gender, fullSec, pw);
    }

    private void attemptRegisterOnServer(String un, String name, String gender, String fullSec, String pw) {
        RegisterRequest req = new RegisterRequest(name, 16, gender, "General", fullSec, un, pw);
        ApiClient.getApi().register(req).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success && response.body().data != null) {
                    if (btnDoRegister != null) {
                        btnDoRegister.setEnabled(true);
                        btnDoRegister.setText("CREATE ACCOUNT");
                    }
                    User u = response.body().data;
                    currentUsername = u.username != null ? u.username : un;
                    currentFullName = u.fullName != null ? u.fullName : name;
                    currentSection = u.gradeSection != null ? u.gradeSection : fullSec;
                    currentGender = u.gender != null ? u.gender : gender;
                    currentXp = u.xp;
                    isLoggedIn = true;

                    UserEntry newUser = new UserEntry(currentUsername, pw, currentFullName, currentSection, currentGender, currentXp);
                    usersMap.put(currentUsername.toLowerCase(Locale.ROOT), newUser);
                    saveUserData();

                    showThemedAlertDialog(
                            "🎉",
                            "Account Created",
                            "Account created successfully on DigiView Server!\n\nYour username is: " + currentUsername + "\nPlease remember this username for login.",
                            "CONTINUE TO APP",
                            () -> {
                                hideAuthScreen();
                                switchPage(0);
                            }
                    );
                } else {
                    if (btnDoRegister != null) {
                        btnDoRegister.setEnabled(true);
                        btnDoRegister.setText("CREATE ACCOUNT");
                    }
                    String err = "Registration failed.";
                    if (response.body() != null && response.body().message != null && !response.body().message.isEmpty()) {
                        err = response.body().message;
                    } else if (response.errorBody() != null) {
                        try {
                            String errorJson = response.errorBody().string();
                            int messageStart = errorJson.indexOf("\"message\":\"");
                            if (messageStart >= 0) {
                                int valueStart = messageStart + 11;
                                int valueEnd = errorJson.indexOf('"', valueStart);
                                if (valueEnd > valueStart) err = errorJson.substring(valueStart, valueEnd);
                            }
                        } catch (Exception ignored) {}
                    }
                    showThemedAlertDialog("⚠️", "Registration Failed", err, "TRY AGAIN", null);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                if (btnDoRegister != null) {
                    btnDoRegister.setEnabled(true);
                    btnDoRegister.setText("CREATE ACCOUNT");
                }
                showThemedAlertDialog(
                        "⚠️",
                        "Connection Error",
                        "Could not connect to DigiView server at " + ApiClient.getBaseUrl() + ".\nPlease check your connection.",
                        "OK",
                        null
                );
            }
        });
    }

    private void performLogout() {
        showThemedConfirmDialog(
                "🚪",
                "Log Out",
                "Are you sure you want to log out of your DigiView account?",
                "LOG OUT",
                "CANCEL",
                () -> {
                    isLoggedIn = false;
                    showAuthScreen();
                }
        );
    }

    // =========================================================================
    // THEMED CONFIRMATION & ALERT DIALOGS
    // =========================================================================
    public void showThemedConfirmDialog(String icon, String title, String message,
                                         String confirmText, String cancelText,
                                         Runnable onConfirm) {
        showThemedDialogInternal(icon, title, message, confirmText, cancelText, onConfirm, true);
    }

    public void showThemedAlertDialog(String icon, String title, String message,
                                       String buttonText, Runnable onDismiss) {
        showThemedDialogInternal(icon, title, message, buttonText, null, onDismiss, false);
    }

    private void showThemedDialogInternal(String icon, String title, String message,
                                          String confirmText, String cancelText,
                                          Runnable onAction, boolean cancelable) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirm, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(cancelable)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvIcon = dialogView.findViewById(R.id.tvConfirmIcon);
        TextView tvTitle = dialogView.findViewById(R.id.tvConfirmTitle);
        TextView tvMsg = dialogView.findViewById(R.id.tvConfirmMessage);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnConfirmCancel);
        MaterialButton btnOk = dialogView.findViewById(R.id.btnConfirmOk);

        if (icon != null && !icon.isEmpty()) {
            tvIcon.setText(icon);
            tvIcon.setVisibility(View.VISIBLE);
        } else {
            tvIcon.setVisibility(View.GONE);
        }

        tvTitle.setText(title != null ? title : "DigiView");
        tvMsg.setText(message != null ? message : "");

        if (confirmText != null && !confirmText.isEmpty()) {
            btnOk.setText(confirmText);
        }

        if (cancelText != null && !cancelText.isEmpty()) {
            btnCancel.setVisibility(View.VISIBLE);
            btnCancel.setText(cancelText);
            btnCancel.setOnClickListener(v -> {
                SoundManager.getInstance(this).playClick();
                dialog.dismiss();
            });
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) btnOk.getLayoutParams();
            if (lp != null) {
                int marginPx = (int) (6 * getResources().getDisplayMetrics().density + 0.5f);
                lp.setMarginStart(marginPx);
                btnOk.setLayoutParams(lp);
            }
        } else {
            btnCancel.setVisibility(View.GONE);
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) btnOk.getLayoutParams();
            if (lp != null) {
                lp.setMarginStart(0);
                btnOk.setLayoutParams(lp);
            }
        }

        btnOk.setOnClickListener(v -> {
            SoundManager.getInstance(this).playClick();
            dialog.dismiss();
            if (onAction != null) {
                onAction.run();
            }
        });

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setGravity(Gravity.CENTER);
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90f);
            int maxWidth = (int) (380 * getResources().getDisplayMetrics().density + 0.5f);
            dialog.getWindow().setLayout(Math.min(width, maxWidth), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    // =========================================================================
    // 7. HISTORY PAGE
    // =========================================================================
    private void refreshHistoryView() {
        if (viewHistory == null) return;

        LinearLayout container = viewHistory.findViewById(R.id.historyCardsContainer);
        TextView tvEmpty = viewHistory.findViewById(R.id.tvHistoryEmpty);
        container.removeAllViews();

        if (historyRecords.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            return;
        }
        tvEmpty.setVisibility(View.GONE);

        LayoutInflater inflater = LayoutInflater.from(this);
        for (QuizRecord r : historyRecords) {
            View card = inflater.inflate(R.layout.item_history_card, container, false);

            TextView tvIcon = card.findViewById(R.id.tvHistIcon);
            TextView tvTitle = card.findViewById(R.id.tvHistTitle);
            TextView tvDate = card.findViewById(R.id.tvHistDate);
            TextView tvScore = card.findViewById(R.id.tvHistScore);
            TextView tvPercent = card.findViewById(R.id.tvHistPercent);
            ProgressBar pb = card.findViewById(R.id.histProgressBar);
            TextView tvBadge = card.findViewById(R.id.tvHistBadge);

            String titleClean = r.getQuiz()
                    .replace(" (Exited)", "")
                    .replace("(Exited)", "")
                    .replace(" (Failed)", "")
                    .replace("(Failed)", "")
                    .replace(" (0 Hearts)", "")
                    .replace("(0 Hearts)", "")
                    .replace(" Quiz", "")
                    .trim();
            tvTitle.setText(titleClean + " Quiz");
            tvDate.setText(r.getDate());
            tvScore.setText("Score: " + r.getScore() + " / " + r.getTotal());
            tvPercent.setText(r.getPercent() + "%");
            pb.setProgress(r.getPercent());

            if (r.getQuiz().contains("Beginner") || r.getQuiz().contains("Easy")) tvIcon.setText("🟢");
            else if (r.getQuiz().contains("Intermediate") || r.getQuiz().contains("Medium")) tvIcon.setText("🟡");
            else tvIcon.setText("🔴");

            boolean exited = r.getQuiz().contains("Exited");
            boolean failed = r.getQuiz().contains("Failed") || r.getQuiz().contains("0 Hearts") || (!exited && r.getPercent() < 70);

            if (exited) {
                tvBadge.setText("EXITED");
                tvBadge.setTextColor(getResources().getColor(R.color.muted, getTheme()));
                tvBadge.setBackgroundResource(R.drawable.bg_badge_selected);
            } else if (!failed && r.getPercent() == 100) {
                tvBadge.setText("PERFECT 🏆");
                tvBadge.setTextColor(getResources().getColor(R.color.gold, getTheme()));
                tvBadge.setBackgroundResource(R.drawable.bg_badge_gold);
            } else if (!failed && r.getPercent() >= 70) {
                tvBadge.setText("PASSED ✅");
                tvBadge.setTextColor(getResources().getColor(R.color.green, getTheme()));
                tvBadge.setBackgroundResource(R.drawable.bg_badge_success);
            } else {
                tvBadge.setText("NEEDS REVIEW");
                tvBadge.setTextColor(getResources().getColor(R.color.amber, getTheme()));
                tvBadge.setBackgroundResource(R.drawable.bg_badge_gold);
            }

            container.addView(card);
        }
    }

    // =========================================================================
    // LIFECYCLE
    // =========================================================================

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SoundManager.getInstance(this).pauseMusic();
    }
}
