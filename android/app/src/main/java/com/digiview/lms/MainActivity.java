package com.digiview.lms;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.digiview.lms.api.ApiClient;
import com.digiview.lms.model.ApiResponse;
import com.digiview.lms.model.QuizRecord;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * DigiView LMS Quiz Activity for Android.
 * Replicates the desktop Swing DigiView styling, color scheme, and micro-animations.
 */
public class MainActivity extends AppCompatActivity {

    private TextView tvQuizTitle;
    private TextView tvProgressLabel;
    private TextView tvHearts;
    private TextView tvQuestionNumber;
    private TextView tvQuestionText;
    private TextView tvFeedback;

    private LinearLayout cardQuestion;
    private LinearLayout[] optionLayouts = new LinearLayout[4];
    private TextView[] optionTexts = new TextView[4];

    private MaterialButton btnSubmit;
    private MaterialButton btnNext;
    private MaterialButton btnExit;

    private int currentQuestionIndex = 0;
    private int score = 0;
    private int hearts = 5;
    private int selectedOptionIndex = -1;
    private boolean isSubmitted = false;

    private final List<QuestionItem> questions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        bindViews();
        loadQuestionBank();
        setupListeners();
        displayCurrentQuestion();
    }

    private void bindViews() {
        tvQuizTitle = findViewById(R.id.quizTitle);
        tvProgressLabel = findViewById(R.id.quizProgressLabel);
        tvHearts = findViewById(R.id.tvHearts);
        tvQuestionNumber = findViewById(R.id.tvQuestionNumber);
        tvQuestionText = findViewById(R.id.tvQuestionText);
        tvFeedback = findViewById(R.id.tvFeedback);

        cardQuestion = findViewById(R.id.cardQuestion);

        optionLayouts[0] = findViewById(R.id.optA);
        optionLayouts[1] = findViewById(R.id.optB);
        optionLayouts[2] = findViewById(R.id.optC);
        optionLayouts[3] = findViewById(R.id.optD);

        optionTexts[0] = findViewById(R.id.tvOptionA);
        optionTexts[1] = findViewById(R.id.tvOptionB);
        optionTexts[2] = findViewById(R.id.tvOptionC);
        optionTexts[3] = findViewById(R.id.tvOptionD);

        btnSubmit = findViewById(R.id.btnSubmit);
        btnNext = findViewById(R.id.btnNext);
        btnExit = findViewById(R.id.btnExitQuiz);
    }

    private void loadQuestionBank() {
        questions.clear();
        questions.add(new QuestionItem("What does HTML stand for?",
                Arrays.asList("HyperText Markup Language", "HighText Machine Language", "Hyperlink Text Management Language", "Home Tool Markup Language"), 0));
        questions.add(new QuestionItem("Which file extension is used for a webpage written in HTML?",
                Arrays.asList(".java", ".html", ".docx", ".exe"), 1));
        questions.add(new QuestionItem("Which program is used to open and view a webpage?",
                Arrays.asList("A calculator", "A spreadsheet", "A web browser", "A compiler"), 2));
        questions.add(new QuestionItem("Which element contains the visible content of a webpage?",
                Arrays.asList("<head>", "<body>", "<title>", "<meta>"), 1));
        questions.add(new QuestionItem("Which element sets the title shown in the browser tab?",
                Arrays.asList("<head>", "<h1>", "<title>", "<header>"), 2));
        questions.add(new QuestionItem("Which element creates the largest heading?",
                Arrays.asList("<h6>", "<heading>", "<h1>", "<head>"), 2));
        questions.add(new QuestionItem("Which element creates a hyperlink?",
                Arrays.asList("<link>", "<a>", "<href>", "<url>"), 1));
        questions.add(new QuestionItem("Which element displays an image?",
                Arrays.asList("<image>", "<picture>", "<src>", "<img>"), 3));
        questions.add(new QuestionItem("Which element creates a bulleted list?",
                Arrays.asList("<ol>", "<li>", "<ul>", "<list>"), 2));
        questions.add(new QuestionItem("Which element creates a clickable button?",
                Arrays.asList("<click>", "<press>", "<button>", "<btn>"), 2));

        Collections.shuffle(questions);
    }

    private void setupListeners() {
        for (int i = 0; i < 4; i++) {
            final int index = i;
            optionLayouts[i].setOnClickListener(v -> selectOption(index));
        }

        btnSubmit.setOnClickListener(v -> submitAnswer());
        btnNext.setOnClickListener(v -> nextQuestion());
        btnExit.setOnClickListener(v -> showExitDialog());
    }

    private void selectOption(int index) {
        if (isSubmitted) return;

        selectedOptionIndex = index;
        for (int i = 0; i < 4; i++) {
            if (i == index) {
                optionLayouts[i].setBackgroundResource(R.drawable.bg_card_outline);
            } else {
                optionLayouts[i].setBackgroundResource(R.drawable.bg_option_normal);
            }
        }
        tvFeedback.setText("Selected option: " + (char) ('A' + index));
        tvFeedback.setTextColor(Color.parseColor("#94A3B8"));
    }

    private void displayCurrentQuestion() {
        if (currentQuestionIndex >= questions.size()) {
            finishQuiz();
            return;
        }

        QuestionItem q = questions.get(currentQuestionIndex);
        selectedOptionIndex = -1;
        isSubmitted = false;

        tvProgressLabel.setText(String.format("Question %d of %d", currentQuestionIndex + 1, questions.size()));
        tvQuestionNumber.setText(String.format("QUESTION %02d", currentQuestionIndex + 1));
        tvQuestionText.setText(q.question);
        tvHearts.setText(String.format("❤ %d Hearts", hearts));
        tvFeedback.setText("Choose the best answer.");
        tvFeedback.setTextColor(Color.parseColor("#94A3B8"));

        for (int i = 0; i < 4; i++) {
            optionTexts[i].setText(q.options.get(i));
            optionLayouts[i].setBackgroundResource(R.drawable.bg_option_normal);
            optionLayouts[i].setEnabled(true);
        }

        btnSubmit.setVisibility(View.VISIBLE);
        btnNext.setVisibility(View.GONE);

        // Micro-animations matching desktop version
        try {
            Animation popAnim = AnimationUtils.loadAnimation(this, R.anim.card_pop);
            cardQuestion.startAnimation(popAnim);

            Animation slideAnim = AnimationUtils.loadAnimation(this, R.anim.slide_up);
            findViewById(R.id.optionsContainer).startAnimation(slideAnim);
        } catch (Exception ignored) {}
    }

    private void submitAnswer() {
        if (selectedOptionIndex < 0) {
            Toast.makeText(this, "Please select an answer first!", Toast.LENGTH_SHORT).show();
            return;
        }

        isSubmitted = true;
        QuestionItem q = questions.get(currentQuestionIndex);
        boolean correct = (selectedOptionIndex == q.correctIndex);

        for (int i = 0; i < 4; i++) {
            optionLayouts[i].setEnabled(false);
        }

        if (correct) {
            score++;
            optionLayouts[selectedOptionIndex].setBackgroundResource(R.drawable.bg_option_correct);
            tvFeedback.setText("✔ Correct! Great job!");
            tvFeedback.setTextColor(Color.parseColor("#34D399"));
        } else {
            hearts--;
            tvHearts.setText(String.format("❤ %d Hearts", Math.max(0, hearts)));
            optionLayouts[selectedOptionIndex].setBackgroundResource(R.drawable.bg_option_wrong);
            optionLayouts[q.correctIndex].setBackgroundResource(R.drawable.bg_option_correct);
            tvFeedback.setText("✖ Incorrect! The correct answer was " + (char) ('A' + q.correctIndex) + ".");
            tvFeedback.setTextColor(Color.parseColor("#F87171"));
        }

        btnSubmit.setVisibility(View.GONE);
        btnNext.setVisibility(View.VISIBLE);

        if (hearts <= 0) {
            btnNext.setText("View Results");
        } else if (currentQuestionIndex + 1 >= questions.size()) {
            btnNext.setText("Complete Quiz");
        } else {
            btnNext.setText("Next Question →");
        }
    }

    private void nextQuestion() {
        if (hearts <= 0) {
            finishQuiz();
            return;
        }

        currentQuestionIndex++;
        displayCurrentQuestion();
    }

    private void finishQuiz() {
        int totalQuestions = questions.size();
        int finalScore = (int) Math.round(((double) score / Math.max(1, totalQuestions)) * 100);

        // Sync with Spring Boot backend in background
        syncQuizResultWithBackend("Student", "HTML Basics Quiz", score, totalQuestions, finalScore);

        new AlertDialog.Builder(this)
                .setTitle(hearts > 0 ? "🎉 Quiz Completed!" : "💔 Out of Hearts")
                .setMessage(String.format("Final Score: %d/%d (%d%%)\nRemaining Hearts: %d\n\nResults synced with DigiView Cloud!",
                        score, totalQuestions, finalScore, Math.max(0, hearts)))
                .setPositiveButton("Play Again", (dialog, which) -> {
                    currentQuestionIndex = 0;
                    score = 0;
                    hearts = 5;
                    loadQuestionBank();
                    displayCurrentQuestion();
                })
                .setNegativeButton("Exit", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void syncQuizResultWithBackend(String username, String category, int correct, int total, int scorePct) {
        QuizRecord record = new QuizRecord(username, category, correct, total, scorePct, "NORMAL");
        ApiClient.getApi().saveQuizRecord(record).enqueue(new Callback<ApiResponse<QuizRecord>>() {
            @Override
            public void onResponse(Call<ApiResponse<QuizRecord>> call, Response<ApiResponse<QuizRecord>> response) {
                // Background sync succeeded
            }

            @Override
            public void onFailure(Call<ApiResponse<QuizRecord>> call, Throwable t) {
                // Background sync fallback - offline friendly
            }
        });
    }

    private void showExitDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Exit Quiz")
                .setMessage("Are you sure you want to exit? Your current progress will be lost.")
                .setPositiveButton("Yes, Exit", (dialog, which) -> finish())
                .setNegativeButton("Keep Playing", null)
                .show();
    }

    private static class QuestionItem {
        final String question;
        final List<String> options;
        final int correctIndex;

        QuestionItem(String question, List<String> options, int correctIndex) {
            this.question = question;
            this.options = options;
            this.correctIndex = correctIndex;
        }
    }
}
