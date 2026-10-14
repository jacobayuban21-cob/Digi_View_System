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
import androidx.appcompat.app.AppCompatActivity;

import com.digiview.lms.api.ApiClient;
import com.digiview.lms.model.ApiResponse;
import com.digiview.lms.model.QuizRecord;
import com.digiview.lms.model.RegisterRequest;
import com.digiview.lms.util.SoundManager;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
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
    private TextView tvLoginServerText, tvQuickDemoLogin;
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

    // Lessons Curriculum Model
    private static class LessonItem {
        final String title, icon, explanation, code;
        LessonItem(String title, String icon, String explanation, String code) {
            this.title = title;
            this.icon = icon;
            this.explanation = explanation;
            this.code = code;
        }
    }
    private final List<LessonItem> lessonsList = new ArrayList<>();

    // Quiz Engine State
    private static class Question {
        final String text;
        final List<String> options;
        final int answer;
        final int difficulty;
        Question(String text, List<String> options, int answer, int difficulty) {
            this.text = text;
            this.options = options;
            this.answer = answer;
            this.difficulty = difficulty;
        }
    }
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
            popup.getMenu().add(0, 2, 1, "☁️ Server & Sync");
            popup.getMenu().add(0, 3, 2, "🚪 Logout");
            popup.setOnMenuItemClickListener(item -> {
                SoundManager.getInstance(this).playClick();
                if (item.getItemId() == 1) {
                    switchPage(5);
                } else if (item.getItemId() == 2) {
                    showAuthModal();
                } else if (item.getItemId() == 3) {
                    performLogout();
                }
                return true;
            });
            popup.show();
        });

        View topCloud = findViewById(R.id.topCloudPill);
        if (topCloud != null) {
            topCloud.setOnClickListener(v -> {
                SoundManager.getInstance(this).playClick();
                showAuthModal();
            });
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
    // 4. LESSONS CURRICULUM
    // =========================================================================
    private void initLessonsData() {
        lessonsList.clear();
        lessonsList.add(new LessonItem("HTML Basics", "🌐",
                "HTML stands for HyperText Markup Language. It is used to create and structure the content of webpages. A basic HTML document contains <html>, <head>, <title>, and <body> elements.",
                "<!DOCTYPE html>\n<html>\n<head>\n  <title>My Page</title>\n</head>\n<body>\n  <h1>Hello World</h1>\n  <p>Welcome!</p>\n</body>\n</html>"));
        lessonsList.add(new LessonItem("Headings & Paragraphs", "📝",
                "HTML headings range from <h1> (highest importance) to <h6>. The <p> tag creates text paragraphs with default browser spacing.",
                "<h1>Main Heading</h1>\n<h2>Sub-Heading</h2>\n<p>This is a standard text paragraph.</p>"));
        lessonsList.add(new LessonItem("Links & Anchors", "🔗",
                "HTML links allow navigation across pages. The <a> tag creates anchors, and the 'href' attribute specifies the target URL destination.",
                "<a href=\"https://example.com\" target=\"_blank\">Visit Website</a>"));
        lessonsList.add(new LessonItem("Images & Media", "🖼",
                "The <img> tag embeds graphics. The 'src' attribute defines the file path, and 'alt' provides accessibility descriptions.",
                "<img src=\"photo.jpg\" alt=\"A scenic view\" width=\"400\">"));
        lessonsList.add(new LessonItem("Unordered & Ordered Lists", "📋",
                "HTML provides unordered bulleted lists (<ul>) and ordered numbered lists (<ol>). Each list item is placed inside an <li> element.",
                "<ul>\n  <li>HTML5</li>\n  <li>CSS3</li>\n</ul>\n\n<ol>\n  <li>Step 1</li>\n  <li>Step 2</li>\n</ol>"));
        lessonsList.add(new LessonItem("HTML Tables", "📊",
                "Tables organize data into rows (<tr>), headers (<th>), and standard data cells (<td>).",
                "<table>\n  <tr><th>Student</th><th>Grade</th></tr>\n  <tr><td>Juan</td><td>95</td></tr>\n</table>"));
        lessonsList.add(new LessonItem("HTML Forms", "📝",
                "Forms collect user input via <input>, <label>, and <button> elements. Crucial for logins and questionnaires.",
                "<form action=\"/submit\">\n  <label>Name:</label>\n  <input type=\"text\" name=\"user\">\n  <button type=\"submit\">Submit</button>\n</form>"));
    }

    private void refreshLessonsView() {
        if (viewLessons == null) return;
        LinearLayout container = viewLessons.findViewById(R.id.lessonsListContainer);
        container.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(this);
        for (LessonItem item : lessonsList) {
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

    private void showLessonDetailModal(LessonItem item) {
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
    // 5. QUIZ ENGINE (FULL 70 QUESTIONS MATCHING DESKTOP DIFFICULTY & HEARTS)
    // =========================================================================
    private static Question q(String text, String a, String b, String c, String d, int ans, int diff) {
        return new Question(text, Arrays.asList(a, b, c, d), ans, diff);
    }

    private void initQuestionBank() {
        questionBank.clear();

        // Difficulty 1: Easy / Beginner (25 questions matching desktop)
        questionBank.add(q("What does HTML stand for?", "HyperText Markup Language", "HighText Machine Language", "Hyperlink Text Management Language", "Home Tool Markup Language", 0, 1));
        questionBank.add(q("Which file extension is used for a webpage written in HTML?", ".java", ".html", ".docx", ".exe", 1, 1));
        questionBank.add(q("Which program is used to open and view a webpage?", "A calculator", "A spreadsheet", "A web browser", "A compiler", 2, 1));
        questionBank.add(q("Which element contains the visible content of a webpage?", "<head>", "<body>", "<title>", "<meta>", 1, 1));
        questionBank.add(q("Which element sets the title shown in the browser tab?", "<head>", "<h1>", "<title>", "<header>", 2, 1));
        questionBank.add(q("Which element creates the largest heading?", "<h6>", "<heading>", "<h1>", "<head>", 2, 1));
        questionBank.add(q("Which element creates a paragraph?", "<para>", "<p>", "<text>", "<paragraph>", 1, 1));
        questionBank.add(q("Which element creates a line break?", "<break>", "<lb>", "<br>", "<newline>", 2, 1));
        questionBank.add(q("Which element creates a hyperlink?", "<link>", "<a>", "<href>", "<url>", 1, 1));
        questionBank.add(q("Which element displays an image?", "<image>", "<picture>", "<src>", "<img>", 3, 1));
        questionBank.add(q("Which element creates a bulleted (unordered) list?", "<ol>", "<li>", "<ul>", "<list>", 2, 1));
        questionBank.add(q("Which element creates a numbered (ordered) list?", "<ul>", "<ol>", "<li>", "<order>", 1, 1));
        questionBank.add(q("Which element marks one item inside a list?", "<item>", "<ul>", "<li>", "<ol>", 2, 1));
        questionBank.add(q("Which element creates a table?", "<table>", "<tab>", "<grid>", "<data>", 0, 1));
        questionBank.add(q("Which element creates a table row?", "<td>", "<th>", "<tr>", "<row>", 2, 1));
        questionBank.add(q("Which element creates a table header cell?", "<td>", "<th>", "<tr>", "<thead-cell>", 1, 1));
        questionBank.add(q("Which element creates a table data cell?", "<cell>", "<data>", "<td>", "<tr>", 2, 1));
        questionBank.add(q("Which element creates a form?", "<input>", "<form>", "<field>", "<submit>", 1, 1));
        questionBank.add(q("Which element creates a clickable button?", "<click>", "<press>", "<button>", "<btn>", 2, 1));
        questionBank.add(q("Which element shows text with strong importance (usually bold)?", "<strong>", "<small>", "<mark-up>", "<heavy>", 0, 1));
        questionBank.add(q("Which element creates a horizontal line?", "<line>", "<hr>", "<br>", "<divider>", 1, 1));
        questionBank.add(q("Which declaration identifies an HTML5 document?", "<html5>", "<!DOCTYPE html>", "<doctype>", "<HTML5>", 1, 1));
        questionBank.add(q("Which element is the root of an HTML document?", "<body>", "<head>", "<html>", "<main>", 2, 1));
        questionBank.add(q("Which element contains information about the page that is not displayed?", "<footer>", "<main>", "<head>", "<section>", 2, 1));
        questionBank.add(q("Which element is a generic block container?", "<span>", "<div>", "<a>", "<strong>", 1, 1));

        // Difficulty 2: Medium / Intermediate (25 questions matching desktop)
        questionBank.add(q("Which is the correct way to write a paragraph?", "<p>Hello<p>", "<p>Hello</p>", "</p>Hello<p>", "<p/>Hello", 1, 2));
        questionBank.add(q("Which code creates a link to example.com?", "<a src=\"https://example.com\">Visit</a>", "<link>https://example.com</link>", "<a href=\"https://example.com\">Visit</a>", "<href=\"https://example.com\">Visit</href>", 2, 2));
        questionBank.add(q("Which attribute tells <img> which picture file to show?", "href", "src", "alt", "link", 1, 2));
        questionBank.add(q("Which code displays an image with alternative text?", "<img href=\"cat.jpg\" alt=\"Cat\">", "<image src=\"cat.jpg\">", "<img src=\"cat.jpg\" alt=\"Cat\">", "<img>cat.jpg</img>", 2, 2));
        questionBank.add(q("Which syntax creates an HTML comment?", "// comment", "/* comment */", "<!-- comment -->", "** comment **", 2, 2));
        questionBank.add(q("What is the correct closing tag for <h1>?", "<h1/>", "</h1>", "<\\h1>", "</heading>", 1, 2));
        questionBank.add(q("Which code correctly places a list item inside a bulleted list?", "<ul><li>HTML</li></ul>", "<li><ul>HTML</ul></li>", "<ul>HTML<li></ul>", "<ul><item>HTML</item></ul>", 0, 2));
        questionBank.add(q("Which code creates a numbered list with one item?", "<ul><li>One</li></ul>", "<ol><li>One</li></ol>", "<li><ol>One</ol></li>", "<list>One</list>", 1, 2));
        questionBank.add(q("Which code creates a table row with two header cells?", "<tr><td>Name</td><td>Age</td></tr>", "<table>Name Age</table>", "<tr><th>Name</th><th>Age</th></tr>", "<th><tr>Name</tr></th>", 2, 2));
        questionBank.add(q("Which attribute makes a cell span two columns?", "rowspan=\"2\"", "colspan=\"2\"", "columns=\"2\"", "span=\"2\"", 1, 2));
        questionBank.add(q("Which attribute makes a cell span two rows?", "colspan=\"2\"", "height=\"2\"", "rowspan=\"2\"", "rows=\"2\"", 2, 2));
        questionBank.add(q("Which code creates a single-line text box?", "<input type=\"text\">", "<text></text>", "<input text>", "<textbox>", 0, 2));
        questionBank.add(q("Which code creates a password field?", "<input type=\"text\">", "<input type=\"hidden\">", "<input type=\"password\">", "<password>", 2, 2));
        questionBank.add(q("Which attribute shows hint text inside an empty input?", "value", "label", "placeholder", "hint", 2, 2));
        questionBank.add(q("Which attribute makes a form field mandatory?", "required", "important", "validate", "must", 0, 2));
        questionBank.add(q("Which code correctly connects a label to an input?", "<label name=\"user\">Username</label><input id=\"user\">", "<label for=\"user\">Username</label><input id=\"user\">", "<label id=\"user\">Username</label><input for=\"user\">", "<label href=\"user\">Username</label><input name=\"label\">", 1, 2));
        questionBank.add(q("Which code creates a dropdown list with one option?", "<select><option>Red</option></select>", "<dropdown><item>Red</item></dropdown>", "<option><select>Red</select></option>", "<input type=\"dropdown\">", 0, 2));
        questionBank.add(q("Which attribute opens a link in a new tab?", "new", "window", "target=\"_blank\"", "open=\"new\"", 2, 2));
        questionBank.add(q("Which attribute uniquely identifies one element?", "class", "id", "style", "group", 1, 2));
        questionBank.add(q("Which attribute lets several elements share the same CSS class?", "id", "unique", "class", "single", 2, 2));
        questionBank.add(q("Which code sets the page language to English?", "<html language=\"English\">", "<html lang=\"en\">", "<language value=\"en\">", "<head lang=\"en\">", 1, 2));
        questionBank.add(q("Which code declares UTF-8 character encoding?", "<charset value=\"UTF-8\">", "<meta encoding=\"UTF-8\">", "<meta charset=\"UTF-8\">", "<head charset=\"UTF-8\">", 2, 2));
        questionBank.add(q("Which code nests tags correctly?", "<p><strong>Hi</p></strong>", "<p><strong>Hi</strong></p>", "<strong><p>Hi</strong></p>", "<p><strong>Hi</p>", 1, 2));
        questionBank.add(q("Which code loads an external stylesheet?", "<style src=\"styles.css\">", "<css href=\"styles.css\"></css>", "<link rel=\"stylesheet\" href=\"styles.css\">", "<script href=\"styles.css\"></script>", 2, 2));
        questionBank.add(q("Which code loads an external JavaScript file?", "<javascript src=\"app.js\">", "<script src=\"app.js\"></script>", "<link rel=\"script\" href=\"app.js\">", "<script href=\"app.js\"></script>", 1, 2));

        // Difficulty 3: Hard / Advanced (20 questions matching desktop)
        questionBank.add(q("Complete the code: <a ____=\"https://example.com\">Visit</a>", "src", "link", "href", "url", 2, 3));
        questionBank.add(q("Complete the code: <img ____=\"photo.jpg\" alt=\"Photo\">", "src", "href", "file", "path", 0, 3));
        questionBank.add(q("Complete the code: <img src=\"photo.jpg\" ____=\"A photo\">", "title", "alt", "name", "text", 1, 3));
        questionBank.add(q("Complete the code: <ul><____>HTML</____></ul>", "item", "ol", "li", "list", 2, 3));
        questionBank.add(q("Complete the code to make a numbered list: <____><li>First</li><li>Second</li></____>", "ul", "ol", "list", "nl", 1, 3));
        questionBank.add(q("Complete the code: <table><____><th>Name</th></____></table>", "td", "row", "th", "tr", 3, 3));
        questionBank.add(q("Complete the code so the cell spans two columns: <td ____=\"2\">Total</td>", "rowspan", "colspan", "span", "columns", 1, 3));
        questionBank.add(q("Complete the code so typed characters are hidden: <input type=\"____\">", "hidden", "secret", "password", "text", 2, 3));
        questionBank.add(q("Complete the code so the field must be filled in: <input type=\"email\" ____>", "needed", "required", "validate", "must", 1, 3));
        questionBank.add(q("Complete the code: <label ____=\"user\">Username</label><input id=\"user\">", "id", "name", "href", "for", 3, 3));
        questionBank.add(q("Complete the code: <select><____>Red</____><option>Blue</option></select>", "item", "choice", "option", "value", 2, 3));
        questionBank.add(q("Complete the code so the link opens in a new tab: <a href=\"https://example.com\" target=\"____\">Visit</a>", "_new", "_blank", "_tab", "_self", 1, 3));
        questionBank.add(q("Complete the code: <html ____=\"en\">", "language", "lang", "code", "locale", 1, 3));
        questionBank.add(q("Complete the code: <meta ____=\"UTF-8\">", "encoding", "type", "charset", "content", 2, 3));
        questionBank.add(q("Complete the code: <link rel=\"stylesheet\" ____=\"styles.css\">", "src", "href", "file", "style", 1, 3));
        questionBank.add(q("Complete the code: <script ____=\"app.js\"></script>", "href", "link", "file", "src", 3, 3));
        questionBank.add(q("Complete the code: <form ____=\"POST\" action=\"save.php\">...</form>", "type", "method", "submit", "send", 1, 3));
        questionBank.add(q("Complete the code: <____>Copyright 2026 Digi-View</____>", "bottom", "footer", "end", "foot", 1, 3));
        questionBank.add(q("Complete the code: <figure><img src=\"chart.png\" alt=\"Chart\"><____>Sales</____></figure>", "caption", "figcaption", "figuretitle", "label", 1, 3));
        questionBank.add(q("Complete the code so the video shows play and pause buttons: <video src=\"movie.mp4\" ____></video>", "buttons", "play", "controls", "show", 2, 3));
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
        });
    }

    private void promptStartQuiz(String diffName, int diffLevel) {
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
            activeOptionLayouts[q.answer].setBackgroundResource(R.drawable.bg_option_correct);
            if (activeOptionBadges[q.answer] != null) {
                activeOptionBadges[q.answer].setBackgroundResource(R.drawable.bg_badge_success);
                activeOptionBadges[q.answer].setTextColor(Color.WHITE);
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

        recordQuizAttempt(activeDifficultyName + " Quiz", activeScore, total, percent);
    }

    private void recordQuizAttempt(String title, int score, int total, int percent) {
        String formattedDate = new SimpleDateFormat("MMM dd, yyyy  •  h:mm a", Locale.ENGLISH).format(new Date());
        QuizRecord r = new QuizRecord(currentUsername, title, score, total, percent, formattedDate);
        historyRecords.add(0, r);
        saveUserData();

        // Background Cloud Sync to Spring Boot Backend
        try {
            ApiClient.getApi().saveQuizRecord(r).enqueue(new Callback<ApiResponse<QuizRecord>>() {
                @Override
                public void onResponse(Call<ApiResponse<QuizRecord>> call, Response<ApiResponse<QuizRecord>> response) {}
                @Override
                public void onFailure(Call<ApiResponse<QuizRecord>> call, Throwable t) {}
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

        viewProfile.findViewById(R.id.btnProfileAuth).setOnClickListener(v -> {
            SoundManager.getInstance(this).playClick();
            showAuthModal();
        });

        View btnLogout = viewProfile.findViewById(R.id.btnProfileLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                SoundManager.getInstance(this).playClick();
                performLogout();
            });
        }
    }

    // =========================================================================
    // 7. ACHIEVEMENTS PAGE (EXACT 9 DESKTOP ACHIEVEMENTS)
    // =========================================================================
    private static class AchievementItem {
        final String icon;
        final String title;
        final String description;
        final int current;
        final int target;

        AchievementItem(String icon, String title, String description, int current, int target) {
            this.icon = icon;
            this.title = title;
            this.description = description;
            this.current = current;
            this.target = target;
        }

        boolean isUnlocked() {
            return current >= target;
        }
    }

    private boolean hasPassedDifficulty(String diff) {
        for (QuizRecord r : historyRecords) {
            if (r.getQuiz().toLowerCase(Locale.ROOT).contains(diff.toLowerCase(Locale.ROOT))
                    && !r.getQuiz().contains("Exited")
                    && r.getPercent() >= 70) {
                return true;
            }
        }
        return false;
    }

    private void refreshAchievementsView() {
        if (viewAchievements == null) return;

        int taken = historyRecords.size();
        int xp = currentXp;
        boolean passedAny = false;
        boolean perfect = false;
        for (QuizRecord r : historyRecords) {
            if (!r.getQuiz().contains("Exited") && r.getPercent() >= 70) passedAny = true;
            if (r.getPercent() == 100) perfect = true;
        }

        AchievementItem[] items = new AchievementItem[] {
            new AchievementItem("🎯", "First Quiz", "Complete at least one quiz.", taken, 1),
            new AchievementItem("⭐", "HTML Quiz Pass", "Earn 70% or higher on a quiz.", passedAny ? 1 : 0, 1),
            new AchievementItem("🏆", "Perfect Score", "Get every question correct in a quiz.", perfect ? 1 : 0, 1),
            new AchievementItem("🌱", "Easy Master", "Pass the Easy quiz.", hasPassedDifficulty("Beginner") || hasPassedDifficulty("Easy") ? 1 : 0, 1),
            new AchievementItem("⚡", "Medium Master", "Pass the Medium quiz.", hasPassedDifficulty("Intermediate") || hasPassedDifficulty("Medium") ? 1 : 0, 1),
            new AchievementItem("🔥", "Hard Master", "Pass the Hard quiz.", hasPassedDifficulty("Advanced") || hasPassedDifficulty("Hard") ? 1 : 0, 1),
            new AchievementItem("📈", "Level Up", "Reach Level 2 (100 XP).", xp, 100),
            new AchievementItem("📚", "Persistent Learner", "Complete 5 quizzes.", taken, 5),
            new AchievementItem("💎", "XP Collector", "Earn 500 XP.", xp, 500)
        };

        int unlockedCount = 0;
        for (AchievementItem item : items) {
            if (item.isUnlocked()) unlockedCount++;
        }

        TextView tvSubtitle = viewAchievements.findViewById(R.id.tvAchievementsSubtitle);
        ProgressBar overallBar = viewAchievements.findViewById(R.id.achievementsOverallBar);
        if (tvSubtitle != null) {
            tvSubtitle.setText(unlockedCount + " of " + items.length + " unlocked — achievements are based on your quiz results.");
        }
        if (overallBar != null) {
            overallBar.setProgress((unlockedCount * 100) / items.length);
        }

        LinearLayout container = viewAchievements.findViewById(R.id.achievementsListContainer);
        if (container == null) return;
        container.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(this);
        for (AchievementItem item : items) {
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

            int shown = Math.min(item.current, item.target);
            int percent = Math.min(100, (item.current * 100) / item.target);
            tvProgText.setText(shown + " / " + item.target);
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
        btnLoginServerPill = findViewById(R.id.btnLoginServerPill);
        tvLoginServerText = findViewById(R.id.tvLoginServerText);
        btnDoLogin = findViewById(R.id.btnDoLogin);
        btnGoToRegister = findViewById(R.id.btnGoToRegister);
        tvQuickDemoLogin = findViewById(R.id.tvQuickDemoLogin);

        etRegFullName = findViewById(R.id.etRegFullName);
        etRegGrade = findViewById(R.id.etRegGrade);
        etRegSection = findViewById(R.id.etRegSection);
        spRegGender = findViewById(R.id.spRegGender);
        etRegPassword = findViewById(R.id.etRegPassword);
        etRegConfirmPassword = findViewById(R.id.etRegConfirmPassword);
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

        if (tvQuickDemoLogin != null) {
            tvQuickDemoLogin.setOnClickListener(v -> {
                SoundManager.getInstance(this).playClick();
                if (etLoginUsername != null) etLoginUsername.setText("student");
                if (etLoginPassword != null) etLoginPassword.setText("admin123");
                Toast.makeText(this, "Default credentials filled!", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnLoginServerPill != null) {
            btnLoginServerPill.setOnClickListener(v -> {
                SoundManager.getInstance(this).playClick();
                showAuthModal();
            });
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
            Toast.makeText(this, "Please enter both username and password.", Toast.LENGTH_SHORT).show();
            return;
        }

        UserEntry entry = usersMap.get(username.toLowerCase(Locale.ROOT));
        if (entry != null && entry.password.equals(password)) {
            currentUsername = entry.username;
            currentFullName = entry.fullName;
            currentSection = entry.gradeSection;
            currentGender = entry.gender;
            currentXp = entry.xp;
            isLoggedIn = true;
            saveUserData();

            hideAuthScreen();
            switchPage(0);
            Toast.makeText(this, "Welcome, " + currentFullName + "!", Toast.LENGTH_SHORT).show();

            try {
                ApiClient.getApi().login(new com.digiview.lms.model.AuthRequest(username, password))
                        .enqueue(new Callback<ApiResponse<com.digiview.lms.model.User>>() {
                            @Override
                            public void onResponse(Call<ApiResponse<com.digiview.lms.model.User>> call, Response<ApiResponse<com.digiview.lms.model.User>> response) {
                                if (response.code() == 401 || !response.isSuccessful() || response.body() == null || !response.body().success) {
                                    RegisterRequest reg = new RegisterRequest(currentFullName, 16, currentGender, "General", currentSection, currentUsername, password);
                                    ApiClient.getApi().register(reg).enqueue(new Callback<ApiResponse<com.digiview.lms.model.User>>() {
                                        @Override
                                        public void onResponse(Call<ApiResponse<com.digiview.lms.model.User>> call2, Response<ApiResponse<com.digiview.lms.model.User>> response2) {}
                                        @Override
                                        public void onFailure(Call<ApiResponse<com.digiview.lms.model.User>> call2, Throwable t) {}
                                    });
                                }
                            }
                            @Override
                            public void onFailure(Call<ApiResponse<com.digiview.lms.model.User>> call, Throwable t) {}
                        });
            } catch (Exception ignored) {}
            return;
        }

        Toast.makeText(this, "Authenticating with server...", Toast.LENGTH_SHORT).show();
        try {
            ApiClient.getApi().login(new com.digiview.lms.model.AuthRequest(username, password))
                    .enqueue(new Callback<ApiResponse<com.digiview.lms.model.User>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<com.digiview.lms.model.User>> call, Response<ApiResponse<com.digiview.lms.model.User>> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().success && response.body().data != null) {
                                com.digiview.lms.model.User u = response.body().data;
                                currentUsername = u.username != null ? u.username : username;
                                currentFullName = u.fullName != null ? u.fullName : username;
                                currentSection = u.gradeSection != null ? u.gradeSection : "Grade 11";
                                currentGender = u.gender != null ? u.gender : "Not specified";
                                currentXp = u.xp;
                                isLoggedIn = true;

                                UserEntry newEntry = new UserEntry(currentUsername, password, currentFullName, currentSection, currentGender, currentXp);
                                usersMap.put(currentUsername.toLowerCase(Locale.ROOT), newEntry);
                                saveUserData();

                                hideAuthScreen();
                                switchPage(0);
                                Toast.makeText(MainActivity.this, "Welcome, " + currentFullName + "!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this, "Invalid username or password.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<com.digiview.lms.model.User>> call, Throwable t) {
                            Toast.makeText(MainActivity.this, "Invalid username or password.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception ex) {
            Toast.makeText(this, "Invalid username or password.", Toast.LENGTH_SHORT).show();
        }
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

        String un = baseUsername;
        int suffix = 1;
        while (usersMap.containsKey(un.toLowerCase(Locale.ROOT))) {
            un = baseUsername + suffix;
            suffix++;
        }

        String fullSec = gr + " - " + sec;
        UserEntry newUser = new UserEntry(un, pw, name, fullSec, gender, 0);
        usersMap.put(un.toLowerCase(Locale.ROOT), newUser);

        currentUsername = un;
        currentFullName = name;
        currentSection = fullSec;
        currentGender = gender;
        currentXp = 0;
        isLoggedIn = true;
        saveUserData();

        final String regUn = un;
        final String regPw = pw;
        final String regName = name;
        final String regGender = gender;
        final String regSec = fullSec;
        try {
            RegisterRequest req = new RegisterRequest(regName, 16, regGender, "General", regSec, regUn, regPw);
            ApiClient.getApi().register(req).enqueue(new Callback<ApiResponse<com.digiview.lms.model.User>>() {
                @Override
                public void onResponse(Call<ApiResponse<com.digiview.lms.model.User>> call, Response<ApiResponse<com.digiview.lms.model.User>> response) {}
                @Override
                public void onFailure(Call<ApiResponse<com.digiview.lms.model.User>> call, Throwable t) {}
            });
        } catch (Exception ignored) {}

        showThemedAlertDialog(
                "🎉",
                "Account Created",
                "Account created successfully!\n\nYour username is: " + un + "\nPlease remember this username for login.",
                "CONTINUE TO APP",
                () -> {
                    hideAuthScreen();
                    switchPage(0);
                }
        );
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

            tvTitle.setText(r.getQuiz());
            tvDate.setText(r.getDate());
            tvScore.setText("Score: " + r.getScore() + " / " + r.getTotal());
            tvPercent.setText(r.getPercent() + "%");
            pb.setProgress(r.getPercent());

            if (r.getQuiz().contains("Beginner")) tvIcon.setText("🟢");
            else if (r.getQuiz().contains("Intermediate")) tvIcon.setText("🟡");
            else tvIcon.setText("🔴");

            if (r.getQuiz().contains("Exited")) {
                tvBadge.setText("EXITED");
                tvBadge.setTextColor(getResources().getColor(R.color.red, getTheme()));
                tvBadge.setBackgroundResource(R.drawable.bg_badge_danger);
            } else if (r.getPercent() == 100) {
                tvBadge.setText("PERFECT 🏆");
                tvBadge.setTextColor(getResources().getColor(R.color.gold, getTheme()));
                tvBadge.setBackgroundResource(R.drawable.bg_badge_gold);
            } else if (r.getPercent() >= 70) {
                tvBadge.setText("PASSED ✅");
                tvBadge.setTextColor(getResources().getColor(R.color.green, getTheme()));
                tvBadge.setBackgroundResource(R.drawable.bg_badge_success);
            } else {
                tvBadge.setText("FAILED");
                tvBadge.setTextColor(getResources().getColor(R.color.red, getTheme()));
                tvBadge.setBackgroundResource(R.drawable.bg_badge_danger);
            }

            container.addView(card);
        }
    }

    // =========================================================================
    // 8. AUTHENTICATION & SYNC MODAL
    // =========================================================================
    private void showAuthModal() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_auth, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        EditText etServerUrl = dialogView.findViewById(R.id.etServerUrl);
        MaterialButton btnPresetRender = dialogView.findViewById(R.id.btnPresetRender);
        MaterialButton btnPresetLocal = dialogView.findViewById(R.id.btnPresetLocal);
        MaterialButton btnTest = dialogView.findViewById(R.id.btnTestConnection);
        TextView tvStatus = dialogView.findViewById(R.id.tvConnectionStatus);

        EditText etName = dialogView.findViewById(R.id.etAuthFullName);
        EditText etSec = dialogView.findViewById(R.id.etAuthSection);
        EditText etUser = dialogView.findViewById(R.id.etAuthUsername);
        MaterialButton btnSave = dialogView.findViewById(R.id.btnAuthSave);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnAuthCancel);

        etServerUrl.setText(ApiClient.getBaseUrl());
        etName.setText(currentFullName);
        etSec.setText(currentSection);
        etUser.setText(currentUsername);

        btnPresetRender.setOnClickListener(v -> {
            SoundManager.getInstance(this).playClick();
            etServerUrl.setText(ApiClient.DEFAULT_CLOUD_URL);
        });

        btnPresetLocal.setOnClickListener(v -> {
            SoundManager.getInstance(this).playClick();
            etServerUrl.setText(ApiClient.DEFAULT_LOCAL_URL);
        });

        btnTest.setOnClickListener(v -> {
            SoundManager.getInstance(this).playClick();
            String testUrl = etServerUrl.getText().toString().trim();
            if (testUrl.isEmpty()) {
                tvStatus.setText("❌ Enter URL first");
                tvStatus.setTextColor(getResources().getColor(R.color.red, getTheme()));
                return;
            }
            tvStatus.setText("⏳ Testing server...");
            tvStatus.setTextColor(getResources().getColor(R.color.cyan, getTheme()));

            new Thread(() -> {
                String normalized = testUrl;
                if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
                    normalized = "http://" + normalized;
                }
                if (!normalized.endsWith("/")) {
                    normalized += "/";
                }
                final String finalUrl = normalized;
                long start = System.currentTimeMillis();
                try {
                    okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
                            .connectTimeout(5, TimeUnit.SECONDS)
                            .readTimeout(5, TimeUnit.SECONDS)
                            .build();
                    okhttp3.Request req = new okhttp3.Request.Builder()
                            .url(finalUrl + "api/health")
                            .build();
                    try (okhttp3.Response resp = client.newCall(req).execute()) {
                        long latency = System.currentTimeMillis() - start;
                        final String msg = "✅ Online (" + resp.code() + ", " + latency + "ms)";
                        runOnUiThread(() -> {
                            tvStatus.setText(msg);
                            tvStatus.setTextColor(getResources().getColor(R.color.green, getTheme()));
                        });
                        return;
                    }
                } catch (Exception ex) {
                    try {
                        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
                                .connectTimeout(5, TimeUnit.SECONDS)
                                .readTimeout(5, TimeUnit.SECONDS)
                                .build();
                        okhttp3.Request req = new okhttp3.Request.Builder()
                                .url(finalUrl)
                                .build();
                        try (okhttp3.Response resp = client.newCall(req).execute()) {
                            long latency = System.currentTimeMillis() - start;
                            final String msg = "✅ Online (" + resp.code() + ", " + latency + "ms)";
                            runOnUiThread(() -> {
                                tvStatus.setText(msg);
                                tvStatus.setTextColor(getResources().getColor(R.color.green, getTheme()));
                            });
                            return;
                        }
                    } catch (Exception ignored) {}

                    runOnUiThread(() -> {
                        tvStatus.setText("❌ Server Unreachable");
                        tvStatus.setTextColor(getResources().getColor(R.color.red, getTheme()));
                    });
                }
            }).start();
        });

        btnSave.setOnClickListener(v -> {
            SoundManager.getInstance(this).playClick();
            String newServer = etServerUrl.getText().toString().trim();
            if (!newServer.isEmpty()) {
                ApiClient.setBaseUrl(newServer);
                SharedPreferences sp = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                sp.edit().putString(KEY_SERVER_URL, ApiClient.getBaseUrl()).apply();
            }

            String newName = etName.getText().toString().trim();
            String newSec = etSec.getText().toString().trim();
            String newUser = etUser.getText().toString().trim();

            if (newName.isEmpty() || newUser.isEmpty()) {
                Toast.makeText(this, "Name and username are required.", Toast.LENGTH_SHORT).show();
                return;
            }

            currentFullName = newName;
            if (!newSec.isEmpty()) currentSection = newSec;
            currentUsername = newUser;
            saveUserData();

            // Background Registration Sync to Spring Boot & Supabase
            try {
                RegisterRequest req = new RegisterRequest(newName, 16, currentGender, "General", currentSection, newUser, "default123");
                ApiClient.getApi().register(req).enqueue(new Callback<ApiResponse<com.digiview.lms.model.User>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<com.digiview.lms.model.User>> call, Response<ApiResponse<com.digiview.lms.model.User>> response) {}
                    @Override
                    public void onFailure(Call<ApiResponse<com.digiview.lms.model.User>> call, Throwable t) {}
                });
            } catch (Exception ignored) {}

            Toast.makeText(this, "Profile and server settings saved!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            switchPage(activePageIndex);
        });

        btnCancel.setOnClickListener(v -> {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SoundManager.getInstance(this).pauseMusic();
    }
}
