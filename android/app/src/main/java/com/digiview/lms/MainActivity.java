package com.digiview.lms;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
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

    // User State
    private String currentUsername = "student";
    private String currentFullName = "Student User";
    private String currentSection = "Grade 11 - STEM A";
    private String currentGender = "Not specified";
    private int currentXp = 0;

    // Top Bar Views
    private TextView tvTopXp, tvTopLevel, topAvatar;

    // Content Frame
    private FrameLayout contentContainer;

    // Bottom Navigation Views
    private LinearLayout[] navItems = new LinearLayout[5];
    private TextView[] navIcons = new TextView[5];
    private TextView[] navLabels = new TextView[5];
    private int activePageIndex = 0;

    // Cache of Inflated Views
    private View viewDashboard;
    private View viewLessons;
    private View viewQuizHub;
    private View viewProfile;
    private View viewHistory;

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

        // Start on Dashboard
        switchPage(0);
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
        currentGender = sp.getString(KEY_GENDER, "Not specified");
        currentXp = sp.getInt(KEY_XP, 0);

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
            showAuthModal();
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
        navItems[3] = findViewById(R.id.navProfile);
        navItems[4] = findViewById(R.id.navHistory);

        navIcons[0] = findViewById(R.id.iconNavDashboard);
        navIcons[1] = findViewById(R.id.iconNavLessons);
        navIcons[2] = findViewById(R.id.iconNavQuiz);
        navIcons[3] = findViewById(R.id.iconNavProfile);
        navIcons[4] = findViewById(R.id.iconNavHistory);

        navLabels[0] = findViewById(R.id.labelNavDashboard);
        navLabels[1] = findViewById(R.id.labelNavLessons);
        navLabels[2] = findViewById(R.id.labelNavQuiz);
        navLabels[3] = findViewById(R.id.labelNavProfile);
        navLabels[4] = findViewById(R.id.labelNavHistory);

        for (int i = 0; i < 5; i++) {
            final int page = i;
            navItems[i].setOnClickListener(v -> switchPage(page));
        }
    }

    private void switchPage(int index) {
        SoundManager.getInstance(this).playClick();
        activePageIndex = index;

        // Highlight Active Tab
        int cyanColor = getResources().getColor(R.color.cyan, getTheme());
        int mutedColor = getResources().getColor(R.color.muted, getTheme());

        for (int i = 0; i < 5; i++) {
            boolean isActive = (i == index);
            navLabels[i].setTextColor(isActive ? cyanColor : mutedColor);
            navLabels[i].setTextSize(isActive ? 11.5f : 11.0f);
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
        else if (index == 3) refreshProfileView();
        else if (index == 4) refreshHistoryView();
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
                if (viewProfile == null) viewProfile = inflater.inflate(R.layout.layout_profile, contentContainer, false);
                return viewProfile;
            case 4:
                if (viewHistory == null) viewHistory = inflater.inflate(R.layout.layout_history, contentContainer, false);
                return viewHistory;
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
    }

    // =========================================================================
    // 5. QUIZ ENGINE (MATCHING DESKTOP DIFFICULTY & HEARTS)
    // =========================================================================
    private void initQuestionBank() {
        questionBank.clear();
        // Difficulty 1: Beginner
        questionBank.add(new Question("What does HTML stand for?",
                Arrays.asList("HyperText Markup Language", "HighText Machine Language", "Hyperlink Text Management Language", "Home Tool Markup Language"), 0, 1));
        questionBank.add(new Question("Which file extension is used for HTML webpages?",
                Arrays.asList(".html", ".web", ".page", ".doc"), 0, 1));
        questionBank.add(new Question("Which HTML tag is used for the largest heading?",
                Arrays.asList("<h6>", "<h1>", "<head>", "<heading>"), 1, 1));
        questionBank.add(new Question("Which HTML tag creates a paragraph of text?",
                Arrays.asList("<text>", "<p>", "<para>", "<content>"), 1, 1));
        questionBank.add(new Question("Which element contains the visible content of a webpage?",
                Arrays.asList("<head>", "<body>", "<title>", "<meta>"), 1, 1));

        // Difficulty 2: Intermediate
        questionBank.add(new Question("Which code correctly creates a link to example.com?",
                Arrays.asList("<a src=\"https://example.com\">Visit</a>", "<a href=\"https://example.com\">Visit</a>", "<link>https://example.com</link>", "<href>example.com</href>"), 1, 2));
        questionBank.add(new Question("Which tag creates a bulleted unordered list?",
                Arrays.asList("<ol>", "<ul>", "<list>", "<dl>"), 1, 2));
        questionBank.add(new Question("Which attribute specifies alternative text for an image?",
                Arrays.asList("src", "title", "alt", "name"), 2, 2));
        questionBank.add(new Question("Which HTML tag defines a table row?",
                Arrays.asList("<td>", "<th>", "<tr>", "<table>"), 2, 2));
        questionBank.add(new Question("Which HTML tag creates a form to collect user input?",
                Arrays.asList("<input>", "<form>", "<fieldset>", "<submit>"), 1, 2));

        // Difficulty 3: Advanced
        questionBank.add(new Question("What is the correct HTML5 Doctype declaration?",
                Arrays.asList("<!DOCTYPE html>", "<!DOCTYPE HTML5>", "<?xml version='1.0'?>", "<html doctype='html5'>"), 0, 3));
        questionBank.add(new Question("Which tag indicates self-contained navigation links in HTML5?",
                Arrays.asList("<menu>", "<navigate>", "<nav>", "<links>"), 2, 3));
        questionBank.add(new Question("Which attribute opens a link in a new browser tab?",
                Arrays.asList("target=\"_blank\"", "target=\"_new\"", "window=\"new\"", "rel=\"external\""), 0, 3));
        questionBank.add(new Question("Which element is used to group related options in a drop-down list?",
                Arrays.asList("<group>", "<optgroup>", "<options>", "<selectgroup>"), 1, 3));
        questionBank.add(new Question("Which attribute makes an input field required before form submission?",
                Arrays.asList("validate", "mandatory", "required", "need"), 2, 3));
    }

    private void initQuizHubViews(View hub) {
        quizDifficultyView = hub.findViewById(R.id.quizDifficultyView);
        quizActiveView = hub.findViewById(R.id.quizActiveView);
        quizResultView = hub.findViewById(R.id.quizResultView);

        // Difficulty Selector Cards
        hub.findViewById(R.id.btnTierBeginner).setOnClickListener(v -> {
            SoundManager.getInstance(this).playClick();
            startQuiz("Beginner", 1);
        });
        hub.findViewById(R.id.btnTierIntermediate).setOnClickListener(v -> {
            SoundManager.getInstance(this).playClick();
            startQuiz("Intermediate", 2);
        });
        hub.findViewById(R.id.btnTierAdvanced).setOnClickListener(v -> {
            SoundManager.getInstance(this).playClick();
            startQuiz("Advanced", 3);
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
            activeOptionLayouts[i].setBackgroundResource(R.drawable.bg_option_normal);
            activeOptionLayouts[i].setEnabled(true);
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
                activeOptionLayouts[i].setBackgroundResource(R.drawable.bg_card);
                activeOptionLayouts[i].startAnimation(pop);
            } else {
                activeOptionLayouts[i].setBackgroundResource(R.drawable.bg_option_normal);
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
            tvActiveFeedback.setText("Correct answer! Great job! 🎉");
            tvActiveFeedback.setTextColor(getResources().getColor(R.color.green, getTheme()));
        } else {
            activeHearts--;
            activeOptionLayouts[selectedOptionIndex].setBackgroundResource(R.drawable.bg_option_wrong);
            activeOptionLayouts[q.answer].setBackgroundResource(R.drawable.bg_option_correct);
            tvActiveFeedback.setText("Incorrect. One heart deducted.");
            tvActiveFeedback.setTextColor(getResources().getColor(R.color.red, getTheme()));
        }

        updateHeartsText();

        if (activeHearts <= 0) {
            new AlertDialog.Builder(this)
                    .setTitle("Out of Hearts")
                    .setMessage("You ran out of hearts! Please review the lesson and try again.")
                    .setPositiveButton("Try Again", (dialog, which) -> {
                        recordQuizAttempt(activeDifficultyName + " (Failed)", activeScore, activeQuestions.size(), 0);
                        quizActiveView.setVisibility(View.GONE);
                        quizDifficultyView.setVisibility(View.VISIBLE);
                    })
                    .setCancelable(false)
                    .show();
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
        new AlertDialog.Builder(this)
                .setTitle("Exit Quiz?")
                .setMessage("Are you sure you want to exit? Your attempt will be recorded as exited.")
                .setPositiveButton("Exit", (dialog, which) -> {
                    recordQuizAttempt(activeDifficultyName + " Quiz (Exited)", activeScore, activeQuestions.size(), 0);
                    quizActiveView.setVisibility(View.GONE);
                    quizDifficultyView.setVisibility(View.VISIBLE);
                })
                .setNegativeButton("Cancel", null)
                .show();
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SoundManager.getInstance(this).pauseMusic();
    }
}
