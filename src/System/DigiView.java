/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package System;
import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.awt.Toolkit;
import javax.sound.sampled.*;
import java.io.File;
import java.awt.FlowLayout;
import java.awt.Cursor;
import java.awt.GridLayout;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.digiview.core.model.Question;
import com.digiview.core.model.Lesson;
import com.digiview.core.model.QuizRecord;
import com.digiview.core.model.User;
import com.digiview.core.model.Achievement;
import com.digiview.core.data.QuestionBank;
import com.digiview.core.data.LessonCurriculum;
import com.digiview.core.data.AchievementRegistry;
import com.digiview.core.logic.QuizProgressionRules;
/**
 *
 * @author Jacob
 */
public class DigiView {

    public static void log(String tag, String message) {
        String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new java.util.Date());
        System.out.printf("[%s] [%-8s] %s%n", timestamp, tag, message);
    }

    public static void logErr(String tag, String message, Throwable t) {
        String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new java.util.Date());
        System.err.printf("[%s] [ERROR   ] [%s] %s%n", timestamp, tag, message);
        if (t != null) {
            t.printStackTrace(System.err);
        }
    }

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            logErr("THREAD", "Uncaught exception on thread: " + thread.getName(), throwable);
        });

        log("SYSTEM", "==========================================================");
        log("SYSTEM", "  DigiView LMS Desktop v1.1.1 (Minor Update)");
        log("SYSTEM", "  Java Runtime : " + System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")");
        log("SYSTEM", "  OS Platform  : " + System.getProperty("os.name") + " " + System.getProperty("os.version") + " (" + System.getProperty("os.arch") + ")");
        log("SYSTEM", "  Working Dir  : " + new File(".").getAbsolutePath());
        log("SYSTEM", "  Cloud Server : " + ApiClient.getBaseUrl());
        log("SYSTEM", "==========================================================");

        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                log("UI", "Initializing DigiView LMS desktop application...");
                DigiViewLMS app = new DigiViewLMS();
                app.setVisible(true);
                log("UI", "Main window displayed successfully. Ready for user interaction.");
            } catch (Throwable t) {
                logErr("STARTUP", "Failed to initialize desktop application UI", t);
            }
        });
    }
}





class DigiViewLMS extends JFrame {
           

    private static class ScrollableGradientPanel extends GradientPanel implements Scrollable {
        ScrollableGradientPanel(LayoutManager layout) {
            super(layout);
        }

        @Override public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }
        @Override public int getScrollableUnitIncrement(Rectangle r, int orientation, int direction) {
            return 24;
        }
        @Override public int getScrollableBlockIncrement(Rectangle r, int orientation, int direction) {
            return 100;
        }
        @Override public boolean getScrollableTracksViewportWidth() {
            return true;
        }
        @Override public boolean getScrollableTracksViewportHeight() {
            Container p = getParent();
            return p instanceof JViewport && p.getHeight() > getPreferredSize().height;
        }
    }

    private static class ScrollableContentPanel extends JPanel implements Scrollable {
        ScrollableContentPanel() {
            super(new BorderLayout());
            setOpaque(false);
        }

        @Override public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }
        @Override public int getScrollableUnitIncrement(Rectangle r, int orientation, int direction) {
            return 24;
        }
        @Override public int getScrollableBlockIncrement(Rectangle r, int orientation, int direction) {
            return 96;
        }
        @Override public boolean getScrollableTracksViewportWidth() {
            return true;
        }
        @Override public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }


    private static class RoundedComboBox extends JComboBox<String> {
        RoundedComboBox(String[] items) {
            super(items);
            setOpaque(false);
            setForeground(new Color(248, 250, 252));
            setFont(new Font("SansSerif", Font.PLAIN, 15));
            setBorder(new EmptyBorder(0, 12, 0, 8));
            setPreferredSize(new Dimension(340, 52));
            setMinimumSize(new Dimension(100, 52));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
            setAlignmentX(Component.CENTER_ALIGNMENT);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
                @Override
                protected JButton createArrowButton() {
                    JButton b = new JButton("\u25BE");
                    b.setContentAreaFilled(false);
                    b.setBorderPainted(false);
                    b.setFocusPainted(false);
                    b.setForeground(new Color(120, 135, 160));
                    return b;
                }

                @Override
                public void paintCurrentValueBackground(
                        Graphics g, Rectangle bounds, boolean hasFocus) {

                }
            });

            setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(
                        JList<?> list, Object value, int index,
                        boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(
                            list, value, index, isSelected, cellHasFocus);
                    setForeground(new Color(248, 250, 252));
                    setBorder(new EmptyBorder(8, 14, 8, 14));
                    if (index == -1) {
                        setOpaque(false);
                    } else {
                        setOpaque(true);
                        setBackground(isSelected
                                ? new Color(30, 50, 80)
                                : new Color(16, 26, 43));
                    }
                    return this;
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            paintInputBackground(g, this);
            super.paintComponent(g);
        }

        @Override
        protected void paintBorder(Graphics g) {
            paintInputOutline(g, this);
        }
    }
    
    
    private static DigiViewLMS activeInstance;
    private static final int CLICK_POOL_SIZE = 6;
    private Clip[] clickClips = new Clip[CLICK_POOL_SIZE];
    private int currentClipIndex = 0;
    private byte[] clickAudioBytes;
    private Clip musicClip;
    private boolean musicEnabled = true;
    private long lastClickTime = 0;

    /**
     * Root Redirection & Path Resolver.
     * Allows using any symbol alias (like "@", "~", "^", "$", "/") to redirect to the project root folder.
     * Examples:
     * - "@/public/sounds/mixkit-select-click-1109.wav"
     * - "@/Icons/Splash-icon.png"
     * - "~/public/sounds/..."
     * - "@/sounds/..."
     */
    public static File resolveRoot(String path) {
        if (path == null) return null;
        String cleaned = redirectRootSymbol(path);
        
        File[] candidates = new File[] {
            new File(cleaned),
            new File("public/" + cleaned),
            new File("../" + cleaned),
            new File("../public/" + cleaned),
            new File("src/System/" + cleaned),
            new File("bin/" + cleaned)
        };
        for (File f : candidates) {
            if (f.exists()) return f;
        }
        return new File(cleaned);
    }

    /**
     * Strips leading root alias symbols like '@', '~', '^', '$' and leading slashes.
     */
    public static String redirectRootSymbol(String path) {
        if (path == null) return "";
        String p = path.trim();
        while (p.startsWith("@") || p.startsWith("~") || p.startsWith("^") || p.startsWith("$")) {
            p = p.substring(1).trim();
        }
        while (p.startsWith("/") || p.startsWith("\\")) {
            p = p.substring(1).trim();
        }
        return p;
    }

    public static InputStream resolveRootStream(String path) {
        try {
            File f = resolveRoot(path);
            if (f != null && f.exists() && f.isFile()) {
                return new FileInputStream(f);
            }
            String cleaned = redirectRootSymbol(path);
            InputStream is = DigiView.class.getResourceAsStream("/" + cleaned);
            if (is != null) return is;
            is = DigiView.class.getClassLoader().getResourceAsStream(cleaned);
            if (is != null) return is;
            is = DigiView.class.getResourceAsStream("/System/" + cleaned);
            if (is != null) return is;
            is = DigiView.class.getResourceAsStream("/sounds/" + cleaned);
            if (is != null) return is;
            is = DigiView.class.getResourceAsStream("/Icons/" + cleaned);
            if (is != null) return is;
        } catch (Exception ignored) {}
        return null;
    }

    private static InputStream getAudioResourceStream(String filename) {
        try {
            // Check with root alias symbols '@', '~', etc.
            InputStream is = resolveRootStream("@/public/sounds/" + filename);
            if (is != null) return is;
            is = resolveRootStream("@/sounds/" + filename);
            if (is != null) return is;
            is = resolveRootStream("@/" + filename);
            if (is != null) return is;

            File f = resolveRoot("public/sounds/" + filename);
            if (f.exists()) return new FileInputStream(f);
            f = resolveRoot("public/Sounds/" + filename);
            if (f.exists()) return new FileInputStream(f);
            is = DigiView.class.getResourceAsStream("/sounds/" + filename);
            if (is != null) return is;
            is = DigiView.class.getResourceAsStream("/System/sounds/" + filename);
            if (is != null) return is;
            is = DigiView.class.getResourceAsStream("sounds/" + filename);
            if (is != null) return is;
            is = DigiView.class.getClassLoader().getResourceAsStream("System/sounds/" + filename);
            if (is != null) return is;
            is = DigiView.class.getClassLoader().getResourceAsStream("sounds/" + filename);
            if (is != null) return is;
            f = resolveRoot("src/System/sounds/" + filename);
            if (f.exists()) return new FileInputStream(f);
            f = resolveRoot("bin/System/sounds/" + filename);
            if (f.exists()) return new FileInputStream(f);
            f = resolveRoot("sounds/" + filename);
            if (f.exists()) return new FileInputStream(f);
        } catch (Exception ignored) {}
        return null;
    }

    private void initAudio() {
        activeInstance = this;
        new Thread(() -> {
            DigiView.log("AUDIO", "Initializing audio subsystem and preloading sound clips...");
            // 1. Preload Click Sound into memory and pool of 6 clips
            int poolLoaded = 0;
            try {
                InputStream is = getAudioResourceStream("mixkit-select-click-1109.wav");
                if (is != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buf = new byte[8192];
                    int r;
                    while ((r = is.read(buf)) != -1) baos.write(buf, 0, r);
                    is.close();
                    clickAudioBytes = baos.toByteArray();

                    for (int i = 0; i < CLICK_POOL_SIZE; i++) {
                        try {
                            ByteArrayInputStream bais = new ByteArrayInputStream(clickAudioBytes);
                            AudioInputStream ais = AudioSystem.getAudioInputStream(bais);
                            Clip clip = AudioSystem.getClip();
                            clip.open(ais);
                            clickClips[i] = clip;
                            poolLoaded++;
                        } catch (Exception e) {
                            DigiView.logErr("AUDIO", "Click clip pool item " + i + " init failed: " + e.getMessage(), null);
                        }
                    }
                    DigiView.log("AUDIO", "Click audio pool ready: " + poolLoaded + "/" + CLICK_POOL_SIZE + " clips allocated.");
                } else {
                    DigiView.log("AUDIO", "Warning: mixkit-select-click-1109.wav not found.");
                }
            } catch (Exception e) {
                DigiView.logErr("AUDIO", "Failed to preload click audio: " + e.getMessage(), e);
            }

            // 2. Global AWT Event Listener for instant, 100% consistent click feedback
            try {
                Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
                    if (event.getID() == MouseEvent.MOUSE_PRESSED) {
                        Object src = event.getSource();
                        if (src instanceof AbstractButton || src instanceof JComboBox || src instanceof JTabbedPane) {
                            playClick();
                        }
                    }
                }, AWTEvent.MOUSE_EVENT_MASK);
            } catch (Exception ignored) {}

            // 3. Start ambient background study music automatically
            startBackgroundMusic();
        }, "DigiView-AudioEngine").start();
    }

    public static void playClick() {
        if (activeInstance != null) {
            activeInstance.playClickSound();
        }
    }

    public void playClickSound() {
        long now = System.currentTimeMillis();
        if (now - lastClickTime < 70) {
            return; // Throttle to prevent duplicate sound on rapid events
        }
        lastClickTime = now;

        try {
            if (clickClips != null && clickClips.length > 0) {
                synchronized (clickClips) {
                    Clip c = clickClips[currentClipIndex];
                    currentClipIndex = (currentClipIndex + 1) % clickClips.length;
                    if (c != null) {
                        if (c.isRunning()) c.stop();
                        c.setFramePosition(0);
                        c.start();
                        return;
                    }
                }
            }
            // Fallback if pool not yet ready
            if (clickAudioBytes != null) {
                AudioInputStream ais = AudioSystem.getAudioInputStream(new ByteArrayInputStream(clickAudioBytes));
                Clip c = AudioSystem.getClip();
                c.open(ais);
                c.addLineListener(ev -> {
                    if (ev.getType() == LineEvent.Type.STOP) c.close();
                });
                c.start();
            }
        } catch (Throwable ignored) {}
    }

    public synchronized void startBackgroundMusic() {
        if (!musicEnabled) return;
        try {
            if (musicClip != null && musicClip.isRunning()) return;

            if (musicClip == null) {
                InputStream is = getAudioResourceStream("freesound_community-relax-32200.wav");
                if (is == null) {
                    DigiView.log("AUDIO", "Background music file not found: freesound_community-relax-32200.wav");
                    return;
                }
                AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(is));
                musicClip = AudioSystem.getClip();
                musicClip.open(ais);

                try {
                    if (musicClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                        FloatControl gain = (FloatControl) musicClip.getControl(FloatControl.Type.MASTER_GAIN);
                        gain.setValue(-8.0f); // Pleasant background study music volume
                    }
                } catch (Exception ignored) {}
            }

            musicClip.setFramePosition(0);
            musicClip.loop(Clip.LOOP_CONTINUOUSLY);
            DigiView.log("AUDIO", "Background study music playing in continuous loop (freesound_community-relax-32200.wav).");
        } catch (Throwable t) {
            DigiView.logErr("AUDIO", "Background music start failed: " + t.getMessage(), t);
        }
    }

    public synchronized void stopBackgroundMusic() {
        try {
            if (musicClip != null && musicClip.isRunning()) {
                musicClip.stop();
                DigiView.log("AUDIO", "Background music paused.");
            }
        } catch (Exception ignored) {}
    }

    public synchronized void toggleBackgroundMusic() {
        musicEnabled = !musicEnabled;
        DigiView.log("AUDIO", "Background music toggled: " + (musicEnabled ? "ON" : "OFF"));
        if (musicEnabled) {
            startBackgroundMusic();
        } else {
            stopBackgroundMusic();
        }
    }

    public synchronized boolean isMusicPlaying() {
        return musicEnabled && musicClip != null && musicClip.isRunning();
    }
        
        private int quizHearts = 5;

private JLabel createHeartsLabel() {
    JLabel heartsLabel = new JLabel();
    heartsLabel.setFont(
        new Font("SansSerif", Font.BOLD, 20)
    );
    heartsLabel.setForeground(Color.RED);

    updateHeartsLabel(heartsLabel);

    return heartsLabel;
}

private void updateHeartsLabel(JLabel heartsLabel) {
     StringBuilder hearts = new StringBuilder();

    for (int i = 0; i < quizHearts; i++) {
        hearts.append("❤️ ");
    }

    if (quizHearts > 0) {
        heartsLabel.setText(hearts.toString().trim());
    } else {
        heartsLabel.setText("No hearts remaining");
    }

    heartsLabel.revalidate();
    heartsLabel.repaint();
}
        
    private boolean hasPassed(String quizName) {
        if (currentUser == null || currentUser.history == null) return false;
        String t = quizName.toLowerCase();
        int tier = (t.contains("easy") || t.contains("beginner")) ? 1 :
                   ((t.contains("medium") || t.contains("intermediate")) ? 2 : 3);
        return QuizProgressionRules.hasPassedTier(tier, currentUser.history);
    }

    private JPanel createLogo() {
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        logoPanel.setOpaque(false);
        logoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        logoPanel.add(new LogoBadge(52));

        JLabel title = new JLabel("Digi-View");
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setForeground(TEXT);

        logoPanel.add(title);
        return logoPanel;
    }
        private static class GradientPanel extends JPanel {
    GradientPanel(LayoutManager layout) {
        super(layout);
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(
            RenderingHints.KEY_RENDERING,
            RenderingHints.VALUE_RENDER_QUALITY
        );

        int w = getWidth();
        int h = getHeight();

        g2.setPaint(new GradientPaint(
            0, 0, new Color(5, 15, 35),
            w, h, new Color(18, 65, 130)
        ));
        g2.fillRect(0, 0, w, h);

        g2.setColor(new Color(37, 99, 235, 35));
        g2.fillOval(w - 320, -100, 420, 420);

        g2.setColor(new Color(56, 189, 248, 22));
        g2.fillOval(-160, h - 250, 420, 420);

        g2.dispose();
    }
}
    private static final Color BG = new Color(16, 31, 53);
    private static final Color PANEL = new Color(16, 31, 53);
    private static final Color PANEL2 = new Color(20, 41, 67);
    private static final Color BLUE = new Color(37, 99, 235);
    private static final Color CYAN = new Color(56, 189, 248);
    private static final Color TEXT = new Color(248, 250, 252);
    private static final Color MUTED = new Color(148, 163, 184);
    private static final Color GREEN = new Color(34, 197, 94);
    private static final Color RED = new Color(239, 68, 68);
    private static final File DATA_FILE = new File(System.getProperty("user.home"), "digiview-data.ser");
    
    private static final Color D_SIDEBAR = new Color(6, 15, 30);
    private static final Color D_TOP     = new Color(7, 17, 32);
    private static final Color D_CARD    = new Color(14, 27, 48);
    private static final Color D_LINE    = new Color(30, 48, 76);
    
    private final Map<String, NavButton> navButtons = new LinkedHashMap<>();
    private JLabel topXp, topHearts, sideLevel;

    private final Map<String, User> users = new LinkedHashMap<>();
    private User currentUser;
    private String currentPage = "Dashboard";
    private int lastResponsiveWidth = -1;

    private JPanel root;
    private JPanel mainPanel;
    private JPanel sidebarPanel;
    private JPanel content;
    private JLabel pageTitle;
    private JLabel userBadge;
    private final CardLayout authCards = new CardLayout();
    private JPanel authPanel;
    private JTextField loginUsername;
    private JPasswordField loginPassword;

    private javax.swing.Timer resizeDebounceTimer;
    private Lesson activeLesson = null;
    private boolean isQuizActive = false;

    private final List<Lesson> lessons = LessonCurriculum.getLessons();
    private final List<Question> easyQuestions = QuestionBank.getQuestionsForDifficulty(1);
    private final List<Question> mediumQuestions = QuestionBank.getQuestionsForDifficulty(2);
    private final List<Question> hardQuestions = QuestionBank.getQuestionsForDifficulty(3);
    private final List<Question> questionBank = QuestionBank.getAllQuestions();
    
    
    private void loadAppIcon() {
        try {
            String[] symbolPaths = new String[] {
                "@/public/Icons/Splash-icon.png",
                "@/public/Icons/Icon.jpg",
                "@/public/Icons/Icon.ico",
                "@/Icons/Splash-icon.png",
                "@/Icons/Icon.jpg",
                "public/Icons/Splash-icon.png",
                "Icons/Splash-icon.png"
            };
            for (String sp : symbolPaths) {
                File f = resolveRoot(sp);
                if (f != null && f.exists()) {
                    Image img = javax.imageio.ImageIO.read(f);
                    if (img != null) {
                        setIconImage(img);
                        return;
                    }
                }
                InputStream is = resolveRootStream(sp);
                if (is != null) {
                    Image img = javax.imageio.ImageIO.read(is);
                    if (img != null) {
                        setIconImage(img);
                        return;
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    public DigiViewLMS() {
        setTitle("DigiView - v1.1.1");
        loadAppIcon();
        loadData();
        initAudio();

        Rectangle maxBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int initialW = Math.min(1280, Math.max(900, maxBounds.width - 40));
        int initialH = Math.min(800, Math.max(600, maxBounds.height - 40));
        int minW = Math.min(860, maxBounds.width - 20);
        int minH = Math.min(580, maxBounds.height - 20);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(minW, minH));
        setSize(initialW, initialH);
        setLocationRelativeTo(null);
        getContentPane().setBackground(PANEL);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (resizeDebounceTimer == null) {
                    resizeDebounceTimer = new javax.swing.Timer(120, evt -> handleWindowResize());
                    resizeDebounceTimer.setRepeats(false);
                }
                resizeDebounceTimer.restart();
            }
        });

        showAuth();
    }

    private void handleWindowResize() {
        if (currentUser == null) {
            if (authPanel != null) {
                authPanel.revalidate();
                authPanel.repaint();
            }
            return;
        }

        if (content == null) return;

        if (sidebarPanel != null) {
            int targetSideW = getWidth() < 1060 ? 220 : 260;
            if (sidebarPanel.getPreferredSize().width != targetSideW) {
                sidebarPanel.setPreferredSize(new Dimension(targetSideW, 0));
                if (root != null) {
                    root.revalidate();
                }
            }
        }

        int currentW = getEffectiveContentWidth();
        if (Math.abs(currentW - lastResponsiveWidth) > 24) {
            lastResponsiveWidth = currentW;

            if (isQuizActive) {
                content.revalidate();
                content.repaint();
                return;
            }

            if (activeLesson != null) {
                showLesson(activeLesson);
                return;
            }

            if (currentPage != null) {
                showPage(currentPage);
            }
        }
    }
    
   

    private JLabel createDVLogo() {
    JLabel logo = new JLabel("DV", SwingConstants.CENTER);

    logo.setFont(new Font("SansSerif", Font.BOLD, 24));
    logo.setForeground(Color.WHITE);
    logo.setOpaque(true);
    logo.setBackground(new Color(37, 99, 235));
    logo.setPreferredSize(new Dimension(55, 55));
    logo.setMinimumSize(new Dimension(55, 55));
    logo.setMaximumSize(new Dimension(55, 55));

    logo.setBorder(BorderFactory.createLineBorder(
        new Color(56, 189, 248), 1, true
    ));

    return logo;
}
    
    private JButton createNavButton(
        String icon,
        String title,
        boolean active) {

    JButton b = new JButton(icon + "   " + title);
    b.addActionListener(e -> playClick());

    b.setUI(new BasicButtonUI());
    b.setFont(new Font("SansSerif", Font.BOLD, 14));
    b.setHorizontalAlignment(SwingConstants.LEFT);

    b.setForeground(active
        ? Color.WHITE
        : new Color(148, 163, 184));

    b.setBackground(active
        ? new Color(18, 48, 79)
        : new Color(8, 21, 38));

    b.setOpaque(true);
    b.setContentAreaFilled(true);
    b.setFocusPainted(false);
    b.setBorderPainted(false);

    b.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(
            0, active ? 4 : 4, 0, 0,
            active
                ? new Color(56, 189, 248)
                : new Color(8, 21, 38)
        ),
        BorderFactory.createEmptyBorder(14, 16, 14, 10)
    ));

    b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
    b.setAlignmentX(Component.LEFT_ALIGNMENT);
    b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));


    b.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
            if (!active) {
                b.setBackground(new Color(20, 41, 67));
                b.setForeground(Color.WHITE);
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            if (!active) {
                b.setBackground(new Color(8, 21, 38));
                b.setForeground(new Color(148, 163, 184));
            }
        }
    });

    return b;
}
    

    private static Question q(String text, String a, String b, String c, String d, int answer, int difficulty) {
        return new Question(text, Arrays.asList(a, b, c, d), answer, difficulty);
    }

    private void loadData() {
        if (!DATA_FILE.exists()) {
            DigiView.log("DATA", "Local data file not found (" + DATA_FILE.getAbsolutePath() + "). Starting with clean state.");
            return;
        }
        try (FileInputStream fis = new FileInputStream(DATA_FILE);
             ObjectInputStream in = new ObjectInputStream(fis) {
                 @Override
                 protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
                     ObjectStreamClass desc = super.readClassDescriptor();
                     if ("System.DigiViewLMS$User".equals(desc.getName()) || "System.DigiView$User".equals(desc.getName())) {
                         return ObjectStreamClass.lookup(User.class);
                     }
                     return desc;
                 }

                 @Override
                 protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
                     String name = desc.getName();
                     if ("System.DigiViewLMS$User".equals(name) || "System.DigiView$User".equals(name)) {
                         return User.class;
                     }
                     return super.resolveClass(desc);
                 }
             }) {
            Object data = in.readObject();
            if (data instanceof Map<?, ?>) {
                Map<?, ?> map = (Map<?, ?>) data;
                for (Object value : map.values()) {
                    if (value instanceof User) {
                        User u = (User) value;
                        users.put(u.username.toLowerCase(Locale.ROOT), u);
                    }
                }
            }
            DigiView.log("DATA", "Successfully loaded " + users.size() + " user account(s) from " + DATA_FILE.getAbsolutePath());
        } catch (Exception ex) {
            DigiView.log("DATA", "Cached data schema update required (" + ex.getMessage() + "). Initializing fresh local state.");
        }
    }

    private void saveData() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            out.writeObject(users);
            DigiView.log("DATA", "Saved " + users.size() + " user account(s) to " + DATA_FILE.getAbsolutePath());
        } catch (IOException ex) {
            DigiView.logErr("DATA", "Unable to save local data: " + ex.getMessage(), ex);
            showThemedMessage("❌", "Save Error", "Unable to save local data: " + ex.getMessage());
        }
    }
    
     private void showAuth() {
        getContentPane().removeAll();

            authPanel = new GradientPanel(authCards);
            authPanel.add(buildLogin(), "login");
            authPanel.add(buildRegister(), "register");

            getContentPane().add(authPanel);
            authCards.show(authPanel, "login");

            setExtendedState(JFrame.NORMAL);
            revalidate();
            repaint();
            setLocationRelativeTo(null);
    }

private JPanel authCard(String heading, String subtitle) {
    JPanel outer = new GradientPanel(new GridBagLayout());

    JPanel card = panel();
    card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
    card.setBorder(new CompoundBorder(
        new LineBorder(new Color(43, 100, 180), 1, true),
        new EmptyBorder(25, 30, 25, 30)
    ));

    
    JPanel logo = createLogo();
    logo.setAlignmentX(Component.CENTER_ALIGNMENT);

    JLabel h = label(heading, 23, TEXT, true);
    h.setAlignmentX(Component.LEFT_ALIGNMENT);

    JLabel sub = label(subtitle, 13, MUTED, false);
    sub.setAlignmentX(Component.LEFT_ALIGNMENT);

    card.add(logo);
    card.add(Box.createVerticalStrut(25));
    card.add(h);
    card.add(Box.createVerticalStrut(7));
    card.add(sub);
    card.add(Box.createVerticalStrut(20));

    outer.add(card);
    outer.putClientProperty("card", card);

    return outer;
}

    private JPanel buildLogin() {
        final Color CARD_BG = new Color(13, 21, 36);
        final Color CARD_BORDER = new Color(33, 52, 80);
        final Color HINT = new Color(120, 135, 160);
        final Color ACCENT = new Color(56, 189, 248);

        JPanel outer = new ScrollableGradientPanel(new GridBagLayout());

        RoundedPanel card = new RoundedPanel(34, CARD_BG, CARD_BORDER);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(38, 42, 28, 42));
        card.setPreferredSize(new Dimension(480, 620));

        JPanel logo = createLogo();
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        logo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        card.add(logo);
        card.add(Box.createVerticalStrut(24));

        JPanel heading = new JPanel();
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.setOpaque(false);
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);
        heading.setPreferredSize(new Dimension(396, 60));
        heading.setMaximumSize(new Dimension(396, 60));

        JLabel signIn = new JLabel("SIGN IN", SwingConstants.CENTER);
        signIn.setFont(new Font("SansSerif", Font.BOLD, 26));
        signIn.setForeground(TEXT);
        signIn.setAlignmentX(Component.CENTER_ALIGNMENT);
        signIn.setPreferredSize(new Dimension(396, 32));
        signIn.setMaximumSize(new Dimension(396, 32));

        JLabel fill = new JLabel("Enter your credentials to access your account", SwingConstants.CENTER);
        fill.setFont(new Font("SansSerif", Font.PLAIN, 12));
        fill.setForeground(HINT);
        fill.setAlignmentX(Component.CENTER_ALIGNMENT);
        fill.setPreferredSize(new Dimension(396, 20));
        fill.setMaximumSize(new Dimension(396, 20));

        heading.add(signIn);
        heading.add(Box.createVerticalStrut(6));
        heading.add(fill);
        card.add(heading);
        card.add(Box.createVerticalStrut(26));

        loginUsername = new RoundedTextField("Username");
        loginPassword = new RoundedPasswordField("Password");
        loginPassword.addActionListener(e -> login()); 

        card.add(loginUsername);
        card.add(Box.createVerticalStrut(14));
        card.add(loginPassword);
        JCheckBox showLoginPassword = createPasswordToggle(loginPassword, "Show password");
        card.add(showLoginPassword);
        card.add(Box.createVerticalStrut(14));

        RoundedButton loginBtn = new RoundedButton("SIGN IN", true);
        loginBtn.addActionListener(e -> {
            playClickSound();
            login();
        });
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(18));

        RoundedButton create = new RoundedButton("CREATE AN ACCOUNT", false);
        create.addActionListener(e -> {
            playClickSound();
            authCards.show(authPanel, "register");
        });
        card.add(create);

        card.add(Box.createVerticalGlue());
        card.add(Box.createVerticalStrut(14));

        JLabel version = new JLabel("V1.1.1");
        version.setFont(new Font("SansSerif", Font.PLAIN, 12));
        version.setForeground(HINT);
        version.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(version);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(24, 24, 24, 24);
        outer.add(card, gbc);

        JScrollPane scrollPane = new JScrollPane(outer);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(24);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(scrollPane, BorderLayout.CENTER);
        return wrapper;
    }


    private static class RoundedPanel extends JPanel {
        private final int arc;
        private final Color fill, line;

        RoundedPanel(int arc, Color fill, Color line) {
            this.arc = arc;
            this.fill = fill;
            this.line = line;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            g2.setColor(line);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    
    private static void styleInput(JTextField f) {
        f.setOpaque(false);
        f.setForeground(new Color(248, 250, 252));
        f.setCaretColor(new Color(248, 250, 252));
        f.setFont(new Font("SansSerif", Font.PLAIN, 15));
        f.setBorder(new EmptyBorder(0, 20, 0, 20));
        f.setPreferredSize(new Dimension(340, 52));
        f.setMinimumSize(new Dimension(100, 52));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        f.setAlignmentX(Component.CENTER_ALIGNMENT);
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { f.repaint(); }
            @Override public void focusLost(FocusEvent e) { f.repaint(); }
        });
    }

    private static void paintInputBackground(Graphics g, JComponent c) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(16, 26, 43));
        g2.fillRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, 22, 22);
        g2.dispose();
    }

    private static void paintInputOutline(Graphics g, JComponent c) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(c.hasFocus()
                ? new Color(56, 189, 248)
                : new Color(38, 58, 88));
        g2.drawRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, 22, 22);
        g2.dispose();
    }

    private static void paintHint(Graphics g, JComponent c, String hint) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(c.getFont());
        g2.setColor(new Color(120, 135, 160));
        FontMetrics fm = g2.getFontMetrics();
        int y = (c.getHeight() + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(hint, c.getInsets().left, y);
        g2.dispose();
    }

    private static class RoundedTextField extends JTextField {
        private final String hint;

        RoundedTextField(String hint) {
            this.hint = hint;
            styleInput(this);
        }

        @Override
        protected void paintComponent(Graphics g) {
            paintInputBackground(g, this);
            super.paintComponent(g);
            if (getText().isEmpty()) paintHint(g, this, hint);
        }

        @Override
        protected void paintBorder(Graphics g) {
            paintInputOutline(g, this);
        }
    }

    private static class RoundedPasswordField extends JPasswordField {
        private final String hint;

        RoundedPasswordField(String hint) {
            this.hint = hint;
            styleInput(this);
            setEchoChar('\u2022');
        }

        @Override
        protected void paintComponent(Graphics g) {
            paintInputBackground(g, this);
            super.paintComponent(g);
            if (getPassword().length == 0) paintHint(g, this, hint);
        }

        @Override
        protected void paintBorder(Graphics g) {
            paintInputOutline(g, this);
        }
    }

    private JCheckBox createPasswordToggle(JPasswordField passwordField, String label) {
        JCheckBox toggle = new JCheckBox(label);
        toggle.setOpaque(false);
        toggle.setForeground(MUTED);
        toggle.setFont(new Font("SansSerif", Font.PLAIN, 11));
        toggle.setAlignmentX(Component.LEFT_ALIGNMENT);
        toggle.addActionListener(e -> passwordField.setEchoChar(toggle.isSelected() ? (char) 0 : '\u2022'));
        return toggle;
    }
    

    private static class AnswerOption extends JToggleButton {
        static final int NORMAL = 0, CORRECT = 1, WRONG = 2;

        final int optionIndex;
        private final String letter;
        private final String answerText;
        private int state = NORMAL;
        private boolean locked = false;
        private final HoverAnimator hoverAnim = new HoverAnimator(this);

        AnswerOption(String letter, String text, int optionIndex) {
            super(text);
            this.letter = letter;
            this.answerText = text;
            this.optionIndex = optionIndex;
            setActionCommand(String.valueOf(optionIndex));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setText("");   
            setFont(new Font("SansSerif", Font.PLAIN, 17));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        
        void lock(int resultState) {
            this.state = resultState;
            this.locked = true;
            setEnabled(false);
            setCursor(Cursor.getDefaultCursor());
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            float t = locked ? 0f : hoverAnim.get();
            boolean selected = isSelected();

            Color fill, line, badge;
            if (state == CORRECT) {
                fill = new Color(14, 62, 46);
                line = new Color(34, 197, 94);
                badge = new Color(34, 197, 94);
            } else if (state == WRONG) {
                fill = new Color(72, 22, 30);
                line = new Color(239, 68, 68);
                badge = new Color(239, 68, 68);
            } else if (selected) {
                fill = new Color(18, 52, 98);
                line = new Color(56, 189, 248);
                badge = new Color(37, 99, 235);
            } else {
                fill = mix(new Color(14, 27, 48), new Color(20, 41, 67), t);
                line = mix(new Color(30, 48, 76), new Color(56, 120, 190), t);
                badge = mix(new Color(26, 45, 82), new Color(37, 80, 150), t);
            }

            g2.setColor(fill);
            g2.fillRoundRect(0, 0, w - 1, h - 1, 20, 20);
            g2.setColor(line);
            g2.drawRoundRect(0, 0, w - 1, h - 1, 20, 20);

           
            int bs = 36, bx = 14, by = (h - bs) / 2;
            g2.setColor(badge);
            g2.fillRoundRect(bx, by, bs, bs, 12, 12);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 16));
            FontMetrics bfm = g2.getFontMetrics();
            g2.drawString(letter, bx + (bs - bfm.stringWidth(letter)) / 2,
                    by + (bs + bfm.getAscent() - bfm.getDescent()) / 2);

           
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int tx = bx + bs + 16;
            int maxW = w - tx - 50;
            String s = answerText;
            while (s.length() > 1 && fm.stringWidth(s) > maxW) {
                s = s.substring(0, s.length() - 1);
            }
            if (!s.equals(answerText)) s = s.substring(0, Math.max(1, s.length() - 1)) + "\u2026";
            g2.setColor(new Color(248, 250, 252));
            g2.drawString(s, tx, (h + fm.getAscent() - fm.getDescent()) / 2);

            
            if (state != NORMAL) {
                String mark = state == CORRECT ? "\u2713" : "\u2717";
                g2.setFont(new Font("SansSerif", Font.BOLD, 22));
                FontMetrics mfm = g2.getFontMetrics();
                g2.setColor(state == CORRECT ? new Color(74, 222, 128) : new Color(248, 113, 113));
                g2.drawString(mark, w - 20 - mfm.stringWidth(mark),
                        (h + mfm.getAscent() - mfm.getDescent()) / 2);
            }
            g2.dispose();
        }
    }
    
    
    
    private static Color mix(Color a, Color b, float t) {
        return new Color(
            Math.round(a.getRed()   + (b.getRed()   - a.getRed())   * t),
            Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
            Math.round(a.getBlue()  + (b.getBlue()  - a.getBlue())  * t),
            Math.round(a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t));
    }

    
    private static class HoverAnimator {
        private float progress = 0f;
        private boolean hovering = false;
        private final javax.swing.Timer timer;

        HoverAnimator(JComponent target) {
            timer = new javax.swing.Timer(15, e -> {
                float goal = hovering ? 1f : 0f;
                progress += (goal - progress) * 0.22f;
                if (Math.abs(goal - progress) < 0.01f) {
                    progress = goal;
                    ((javax.swing.Timer) e.getSource()).stop();
                }
                target.repaint();
            });
            target.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovering = true;  timer.start(); }
                @Override public void mouseExited(MouseEvent e)  { hovering = false; timer.start(); }
            });
        }

        float get() { return progress; }
    }

    
    private static class RoundedButton extends JButton {
        private final boolean filled;
        private final HoverAnimator hoverAnim = new HoverAnimator(this);

        RoundedButton(String text, boolean filled) {
            super(text);
            this.filled = filled;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setFont(new Font("SansSerif", Font.PLAIN, 14));
            setForeground(filled ? Color.WHITE : new Color(56, 189, 248));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(340, 52));
            setMinimumSize(new Dimension(100, 52));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
            setAlignmentX(Component.CENTER_ALIGNMENT);
            addActionListener(e -> playClick());
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            float t = hoverAnim.get();
            int w = getWidth() - 1, h = getHeight() - 1;

           
            float scale = 0.985f + 0.015f * t;
            if (getModel().isPressed()) scale -= 0.025f;
            g2.translate(getWidth() / 2.0, getHeight() / 2.0);
            g2.scale(scale, scale);
            g2.translate(-getWidth() / 2.0, -getHeight() / 2.0);

            if (filled) {
                Color c1 = mix(new Color(33, 150, 243), new Color(60, 165, 250), t);
                Color c2 = mix(new Color(52, 178, 240), new Color(75, 195, 250), t);
                g2.setPaint(new GradientPaint(0, 0, c1, w, 0, c2));
                g2.fillRoundRect(0, 0, w, h, 20, 20);
            } else {
                g2.setColor(new Color(56, 189, 248, Math.round(25 * t)));
                g2.fillRoundRect(0, 0, w, h, 20, 20);
                g2.setColor(mix(new Color(40, 100, 150), new Color(56, 189, 248), t));
                g2.drawRoundRect(0, 0, w, h, 20, 20);
            }
            super.paintComponent(g2);   
            g2.dispose();
        }
    }
    
     private JPanel buildRegister() {

    final Color CARD_BG = new Color(13, 21, 36);
        final Color CARD_BORDER = new Color(33, 52, 80);
        final Color HINT = new Color(120, 135, 160);
        final Color ACCENT = new Color(56, 189, 248);

        JPanel outer = new ScrollableGradientPanel(new GridBagLayout());

        RoundedPanel card = new RoundedPanel(34, CARD_BG, CARD_BORDER);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(32, 42, 24, 42));


        JPanel logo = createLogo();
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        logo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        card.add(logo);
        card.add(Box.createVerticalStrut(24));

        JPanel heading = new JPanel();
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.setOpaque(false);
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);
        heading.setPreferredSize(new Dimension(396, 60));
        heading.setMaximumSize(new Dimension(396, 60));

        JLabel title = new JLabel("SIGN UP", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setForeground(TEXT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setPreferredSize(new Dimension(396, 32));
        title.setMaximumSize(new Dimension(396, 32));

        JLabel sub = new JLabel("Fill out your student profile details to register", SwingConstants.CENTER);
        sub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        sub.setForeground(HINT);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        sub.setPreferredSize(new Dimension(396, 20));
        sub.setMaximumSize(new Dimension(396, 20));

        heading.add(title);
        heading.add(Box.createVerticalStrut(6));
        heading.add(sub);
        card.add(heading);
        card.add(Box.createVerticalStrut(22));


        RoundedTextField fullName = new RoundedTextField("Student Name");

        RoundedTextField grade = new RoundedTextField("Grade (e.g. 12)");
        grade.setPreferredSize(new Dimension(150, 52));
        RoundedTextField section = new RoundedTextField("Section");
        section.setPreferredSize(new Dimension(150, 52));

        JPanel gradeSection = new JPanel(new GridLayout(1, 2, 14, 0));
        gradeSection.setOpaque(false);
        gradeSection.setAlignmentX(Component.CENTER_ALIGNMENT);
        gradeSection.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        gradeSection.add(grade);
        gradeSection.add(section);

        RoundedComboBox gender = new RoundedComboBox(new String[] {
            "Select gender", "Male", "Female", "Prefer not to say"
        });

        RoundedPasswordField password = new RoundedPasswordField("Password");
        RoundedPasswordField confirm = new RoundedPasswordField("Confirm Password");

        card.add(fullName);
        card.add(Box.createVerticalStrut(14));
        card.add(gradeSection);
        card.add(Box.createVerticalStrut(14));
        card.add(gender);
        card.add(Box.createVerticalStrut(14));
        card.add(password);
        JCheckBox showRegisterPassword = createPasswordToggle(password, "Show password");
        card.add(showRegisterPassword);
        card.add(Box.createVerticalStrut(14));
        card.add(confirm);
        JCheckBox showConfirmPassword = createPasswordToggle(confirm, "Show confirmation");
        card.add(Box.createVerticalStrut(24));

        RoundedButton register = new RoundedButton("CREATE ACCOUNT", true);
        register.addActionListener(e -> {
            playClickSound();

            String name = fullName.getText().trim();
            String g = Objects.toString(gender.getSelectedItem(), "");
            String gr = grade.getText().trim();
            String sec = section.getText().trim();
            String pw = new String(password.getPassword());
            String cpw = new String(confirm.getPassword());

            if (name.length() < 2
                    || gr.isEmpty()
                    || sec.isEmpty()
                    || gender.getSelectedIndex() == 0
                    || pw.length() < 4) {
                message("Please complete all fields. "
                        + "Password must have at least 4 characters.",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!pw.equals(cpw)) {
                message("Passwords do not match.", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String baseUsername = name
                    .replaceAll("[^A-Za-z0-9]", "")
                    .toLowerCase(Locale.ROOT);
            if (baseUsername.isEmpty()) baseUsername = "student";

            final String regUn = baseUsername;
            final String regPw = pw;
            final String regName = name;
            final String regG = g;
            final String regSec = gr + " - " + sec;

            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            register.setEnabled(false);
            new Thread(() -> {
                ApiClient.AuthResult res = ApiClient.register(regUn, regPw, regName, 16, regG, "General", regSec);
                SwingUtilities.invokeLater(() -> {
                    setCursor(Cursor.getDefaultCursor());
                    register.setEnabled(true);
                    if (res.success) {
                        String finalUser = res.username != null ? res.username : regUn;
                        currentUser = new User(res.fullName != null ? res.fullName : regName, res.xp, res.gender != null ? res.gender : regG, "N/A", regSec, finalUser, regPw);
                        users.put(finalUser.toLowerCase(Locale.ROOT), currentUser);
                        saveData();
                        showApp();
                        message("Account created successfully on DigiView Server!\n\n"
                                + "Your username is: " + finalUser
                                + "\nPlease remember this username for login.",
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        message(res.message, JOptionPane.WARNING_MESSAGE);
                    }
                });
            }).start();
        });
        card.add(register);
        card.add(Box.createVerticalStrut(18));


        JButton back = new JButton("ALREADY REGISTERED? LOGIN");
        back.setContentAreaFilled(false);
        back.setBorderPainted(false);
        back.setFocusPainted(false);
        back.setOpaque(false);
        back.setForeground(ACCENT);
        back.setFont(new Font("SansSerif", Font.PLAIN, 13));
        back.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        back.setAlignmentX(Component.CENTER_ALIGNMENT);
        back.addActionListener(e -> {
            playClickSound();
            authCards.show(authPanel, "login");
        });
        card.add(back);

        card.add(Box.createVerticalStrut(14));
        JLabel regVersion = new JLabel("V1.1.1");
        regVersion.setFont(new Font("SansSerif", Font.PLAIN, 12));
        regVersion.setForeground(HINT);
        regVersion.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(regVersion);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(24, 24, 24, 24);
        outer.add(card, gbc);

        JScrollPane scrollPane = new JScrollPane(outer);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(24);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(scrollPane, BorderLayout.CENTER);
        return wrapper;
    }
     
       private void login() {
        String username = loginUsername.getText().trim();
        String password = new String(loginPassword.getPassword());
        if (username.isEmpty() || password.isEmpty()) {
            message("Please enter both username and password.", JOptionPane.WARNING_MESSAGE);
            return;
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new Thread(() -> {
            ApiClient.AuthResult res = ApiClient.login(username, password);
            SwingUtilities.invokeLater(() -> {
                setCursor(Cursor.getDefaultCursor());
                if (res.success) {
                    User existing = users.get(username.toLowerCase(Locale.ROOT));
                    User u = existing != null ? existing : new User(
                        res.fullName != null ? res.fullName : username,
                        16,
                        res.gender != null ? res.gender : "Not specified",
                        "N/A",
                        res.gradeSection != null ? res.gradeSection : "Grade 11",
                        res.username != null ? res.username : username,
                        password
                    );
                    u.fullName = res.fullName != null ? res.fullName : u.fullName;
                    u.xp = res.xp;
                    if (res.gender != null && !res.gender.isEmpty()) u.gender = res.gender;
                    if (res.gradeSection != null && !res.gradeSection.isEmpty()) u.gradeSection = res.gradeSection;
                    if (res.username != null && !res.username.isEmpty()) u.username = res.username;
                    u.password = password;
                    currentUser = u;
                    users.put(u.username.toLowerCase(Locale.ROOT), u);
                    saveData();
                    showApp();
                } else {
                    User cached = users.get(username.toLowerCase(Locale.ROOT));
                    if (cached != null && cached.password.equals(password)) {
                        currentUser = cached;
                        showApp();
                        message("Signed in (Offline Mode).\nCloud server was unreachable (" + (res.message != null ? res.message.split("\n")[0] : "Network offline") + "). Working with local student data.", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                    message(res.message, JOptionPane.WARNING_MESSAGE);
                }
            });
        }).start();
    }

    private void showApp() {
         if (currentUser == null) {
            showAuth();
            return;
        }

        getContentPane().removeAll();

        root = new GradientPanel(new BorderLayout());

        sidebarPanel = buildSidebar();
        root.add(sidebarPanel, BorderLayout.WEST);

        mainPanel = new GradientPanel(new BorderLayout());

        mainPanel.add(buildTopBar(), BorderLayout.NORTH);

        content = new ScrollableContentPanel();
        content.setBorder(new EmptyBorder(26, 32, 26, 20));

        JScrollPane scroll = new JScrollPane(content);

        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getViewport().setBackground(BG);

        scroll.getVerticalScrollBar().setUnitIncrement(24);
        scroll.getHorizontalScrollBar().setUnitIncrement(24);

        scroll.getViewport().setScrollMode(JViewport.BLIT_SCROLL_MODE);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        mainPanel.add(scroll, BorderLayout.CENTER);

        root.add(mainPanel, BorderLayout.CENTER);

        getContentPane().add(root, BorderLayout.CENTER);

        showPage("Dashboard");

        setExtendedState(JFrame.MAXIMIZED_BOTH);

        revalidate();
        repaint();
    }

    private JPanel buildSidebar() {
        navButtons.clear();

        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(D_SIDEBAR);
        sidebar.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 0, 1, new Color(24, 40, 64)),
            new EmptyBorder(24, 16, 18, 16)
        ));
        int sideW = getWidth() < 1060 ? 220 : 260;
        sidebar.setPreferredSize(new Dimension(sideW, 0));


        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        brand.setOpaque(false);
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        JPanel brandText = new JPanel();
        brandText.setOpaque(false);
        brandText.setLayout(new BoxLayout(brandText, BoxLayout.Y_AXIS));

        JLabel brandName = new JLabel(
            "<html><span style='color:#f8fafc'>Digi</span>"
            + "<span style='color:#38bdf8'>View</span></html>");
        brandName.setFont(new Font("SansSerif", Font.BOLD, 22));

        JLabel tagline = new JLabel("HTML LMS");
        tagline.setFont(new Font("SansSerif", Font.PLAIN, 11));
        tagline.setForeground(MUTED);

        brandText.add(brandName);
        brandText.add(tagline);

        brand.add(new LogoBadge(56));
        brand.add(brandText);
        sidebar.add(brand);
        sidebar.add(Box.createVerticalStrut(30));


        String[][] navItems = {
            {"\uD83C\uDFE0", "Dashboard", "Dashboard"},
            {"\uD83D\uDCDA", "Lessons", "Lessons"},
            {"\uD83E\uDDE0", "Quiz", "Quiz"},
            {"\uD83C\uDFC6", "Achievements", "Achievements"},
            {"\uD83D\uDCCA", "Quiz History", "History"},
            {"\uD83D\uDC64", "Profile", "Profile"}
        };
        for (String[] item : navItems) {
            NavButton nav = new NavButton(item[0], item[1]);
            nav.addActionListener(e -> {
                playClickSound();
                showPage(item[2]);
            });
            navButtons.put(item[2], nav);
            sidebar.add(nav);
            sidebar.add(Box.createVerticalStrut(6));
        }
        
        sidebar.add(Box.createVerticalGlue());


        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(28, 44, 68));
        sep.setBackground(new Color(28, 44, 68));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(sep);
        sidebar.add(Box.createVerticalStrut(14));

        JPanel userCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        userCard.setOpaque(false);
        userCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        userCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel nameLabel = new JLabel(currentUser.username);
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        nameLabel.setForeground(TEXT);

        sideLevel = new JLabel("Level " + (currentUser.xp / 100 + 1));
        sideLevel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        sideLevel.setForeground(MUTED);

        info.add(nameLabel);
        info.add(sideLevel);

        userCard.add(new AvatarBadge(currentUser.username.substring(0, 1), 42));
        userCard.add(info);
        sidebar.add(userCard);
        RoundedButton musicBtn = new RoundedButton(isMusicPlaying() ? "🎵   Music: ON" : "🔇   Music: OFF", false);
        musicBtn.setForeground(new Color(160, 185, 215));
        musicBtn.setPreferredSize(new Dimension(200, 42));
        musicBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        musicBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        musicBtn.addActionListener(e -> {
            toggleBackgroundMusic();
            musicBtn.setText(isMusicPlaying() ? "🎵   Music: ON" : "🔇   Music: OFF");
        });
        sidebar.add(musicBtn);
        sidebar.add(Box.createVerticalStrut(8));

        RoundedButton logout = new RoundedButton("\uD83D\uDEAA   Logout", false);
        logout.setForeground(new Color(200, 212, 230));
        logout.setPreferredSize(new Dimension(200, 48));
        logout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        logout.setAlignmentX(Component.LEFT_ALIGNMENT);
        logout.addActionListener(e -> {
            playClickSound();
            boolean ok = showThemedConfirm(
                "🚪", "Log Out", "Are you sure you want to log out of your DigiView account?",
                "LOG OUT", "CANCEL"
            );
            if (ok) {
                currentUser = null;
                showAuth();
            }
        });
        sidebar.add(logout);
        sidebar.add(Box.createVerticalStrut(10));

        JLabel verLabel = new JLabel("DigiView v1.1.1");
        verLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        verLabel.setForeground(new Color(100, 116, 139));
        verLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        verLabel.setBorder(new EmptyBorder(0, 10, 0, 0));
        sidebar.add(verLabel);
        sidebar.add(Box.createVerticalStrut(6));

        return sidebar;
    }
        


    private int getEffectiveContentWidth() {
        int sideW = (getWidth() < 1060 ? 220 : 260);
        int w = getWidth() - sideW;
        int reservedH = 66; // 32 left margin + 20 right margin + 14 scrollbar allowance
        return Math.max(340, w - reservedH);
    }

    private <T extends JComponent> T fixHeight(T c, int h) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.setPreferredSize(new Dimension(100, h));
        c.setMinimumSize(new Dimension(0, h));
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
        return c;
    }

    private void renderDashboard() {
        JPanel p = verticalPanel();
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 10, 0));

        int effW = getEffectiveContentWidth();

        HeroPanel hero = new HeroPanel();

        JPanel heroContent = new JPanel();
        heroContent.setOpaque(false);
        heroContent.setLayout(new BoxLayout(heroContent, BoxLayout.Y_AXIS));
        heroContent.setBorder(new EmptyBorder(34, 40, 34, 40));

        JLabel welcome = new JLabel(
            "<html>Welcome, <font color='#22d3ee'>" + currentUser.username.toUpperCase()
            + "</font>!&nbsp; \uD83D\uDC4B</html>");
        welcome.setFont(new Font("SansSerif", Font.BOLD, 36));
        welcome.setForeground(TEXT);
        welcome.setAlignmentX(Component.LEFT_ALIGNMENT);

        int descW = Math.max(260, effW - 140);
        JLabel desc = new JLabel(
            "<html><table width='" + descW + "' cellpadding='0' cellspacing='0'><tr><td style='color:#becde1; font-family:SansSerif; font-size:15px;'>"
            + "Keep learning, complete your HTML lessons, and challenge yourself with the quiz. "
            + "Your goal is not just to memorize HTML\u2014it's to understand how it works."
            + "</td></tr></table></html>");
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);

        RoundedButton startBtn = new RoundedButton("\uD83D\uDE80  Start Learning", true);
        startBtn.setPreferredSize(new Dimension(200, 52));
        startBtn.addActionListener(e -> {
            playClickSound();
            showPage("Lessons");
        });

        RoundedButton lessonsBtn = new RoundedButton("\uD83D\uDCDA  View Lessons", false);
        lessonsBtn.setForeground(Color.WHITE);
        lessonsBtn.setPreferredSize(new Dimension(190, 52));
        lessonsBtn.addActionListener(e -> {
            playClickSound();
            showPage("Lessons");
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        actions.setOpaque(false);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);
        actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        actions.add(startBtn);
        actions.add(Box.createHorizontalStrut(12));
        actions.add(lessonsBtn);

        heroContent.add(welcome);
        heroContent.add(Box.createVerticalStrut(14));
        heroContent.add(desc);
        heroContent.add(Box.createVerticalStrut(24));
        heroContent.add(actions);

        hero.add(heroContent, BorderLayout.CENTER);
        int heroH = effW < 750 ? 320 : 280;
        p.add(fixHeight(hero, heroH));
        p.add(Box.createVerticalStrut(24));


        int quizzesTaken = currentUser.history.size();
        double averageScore = quizzesTaken == 0 ? 0
            : currentUser.history.stream().mapToInt(r -> r.percent).average().orElse(0);
        long quizzesPassed = currentUser.history.stream()
            .filter(r -> r.percent >= 70).count();
        int level = currentUser.xp / 100 + 1;
        int progress = currentUser.xp % 100;

        String[][] values = {
            {"\u26A1", "Total XP", String.valueOf(currentUser.xp)},
            {"\uD83C\uDFAF", "Average Score", String.format(Locale.US, "%.0f%%", averageScore)},
            {"\uD83D\uDCDD", "Quizzes Taken", String.valueOf(quizzesTaken)},
            {"\uD83C\uDFC6", "Quizzes Passed", String.valueOf(quizzesPassed)}
        };

        int statCols = (effW >= 960) ? 4 : (effW >= 560 ? 2 : 1);
        int statRows = (values.length + statCols - 1) / statCols;
        JPanel stats = new JPanel(new GridLayout(statRows, statCols, 16, 16));
        stats.setOpaque(false);

        for (String[] item : values) {
            RoundedPanel card = new RoundedPanel(26, D_CARD, D_LINE);
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBorder(new EmptyBorder(20, 24, 20, 24));

            IconBox icon = new IconBox(item[0]);
            icon.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel title = new JLabel(item[1]);
            title.setFont(new Font("SansSerif", Font.PLAIN, 14));
            title.setForeground(MUTED);
            title.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel value = new JLabel(item[2]);
            value.setFont(new Font("SansSerif", Font.BOLD, 30));
            value.setForeground(TEXT);
            value.setAlignmentX(Component.LEFT_ALIGNMENT);

            card.add(icon);
            card.add(Box.createVerticalStrut(14));
            card.add(title);
            card.add(Box.createVerticalStrut(4));
            card.add(value);
            stats.add(card);
        }
        int cardH = statCols == 1 ? 140 : 175;
        p.add(fixHeight(stats, statRows * cardH + (statRows - 1) * 16));
        p.add(Box.createVerticalStrut(32));

        
        JPanel levelHeading = new JPanel(new BorderLayout());
        levelHeading.setOpaque(false);

        JLabel levelTitle = new JLabel("Your Learning Level");
        levelTitle.setFont(new Font("SansSerif", Font.BOLD, 24));
        levelTitle.setForeground(TEXT);

        JLabel levelNumber = new JLabel("Level " + level);
        levelNumber.setFont(new Font("SansSerif", Font.PLAIN, 13));
        levelNumber.setForeground(MUTED);

        levelHeading.add(levelTitle, BorderLayout.WEST);
        levelHeading.add(levelNumber, BorderLayout.EAST);
        p.add(fixHeight(levelHeading, 40));
        p.add(Box.createVerticalStrut(12));

        RoundedPanel progressCard = new RoundedPanel(26, D_CARD, D_LINE);
        progressCard.setLayout(new BoxLayout(progressCard, BoxLayout.Y_AXIS));
        progressCard.setBorder(new EmptyBorder(22, 26, 22, 26));

        JPanel progressLabels = new JPanel(new BorderLayout());
        progressLabels.setOpaque(false);

        JLabel progressText = new JLabel("Progress to Level " + (level + 1));
        progressText.setFont(new Font("SansSerif", Font.PLAIN, 14));
        progressText.setForeground(MUTED);

        JLabel xpText = new JLabel(currentUser.xp + "/" + (level * 100) + " XP");
        xpText.setFont(new Font("SansSerif", Font.BOLD, 14));
        xpText.setForeground(TEXT);

        progressLabels.add(progressText, BorderLayout.WEST);
        progressLabels.add(xpText, BorderLayout.EAST);

        progressCard.add(fixHeight(progressLabels, 22));
        progressCard.add(Box.createVerticalStrut(14));
        progressCard.add(fixHeight(new ProgressPill(progress), 14));

        p.add(fixHeight(progressCard, 110));
        p.add(Box.createVerticalStrut(32));

        JLabel continueTitle = new JLabel("Continue Learning");
        continueTitle.setFont(new Font("SansSerif", Font.BOLD, 24));
        continueTitle.setForeground(TEXT);
        continueTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel continueDesc = new JLabel("Choose a lesson to review");
        continueDesc.setFont(new Font("SansSerif", Font.PLAIN, 13));
        continueDesc.setForeground(MUTED);
        continueDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(continueTitle);
        p.add(Box.createVerticalStrut(5));
        p.add(continueDesc);
        p.add(Box.createVerticalStrut(16));

        int lessonCols = (effW >= 920) ? 3 : (effW >= 580 ? 2 : 1);
        JPanel cards = new JPanel(new GridLayout(0, lessonCols, 16, 16));
        cards.setOpaque(false);

        for (final Lesson lesson : lessons) {
            RoundedButton b = new RoundedButton(lesson.icon + "    " + lesson.title, false);
            b.setForeground(Color.WHITE);
            b.setFont(new Font("SansSerif", Font.BOLD, 15));
            b.setHorizontalAlignment(SwingConstants.LEFT);
            b.setBorder(new EmptyBorder(0, 24, 0, 10));
            b.addActionListener(e -> {
                playClickSound();
                showLesson(lesson);
            });
            cards.add(b);
        }

        int rows = (lessons.size() + lessonCols - 1) / lessonCols;
        p.add(fixHeight(cards, rows * 84 + (rows - 1) * 16));

        addToContent(p);
    }

    private void showPage(String page) {
        if (currentUser == null) {
            DigiView.log("NAV", "showPage('" + page + "') invoked with null user -> redirecting to showAuth()");
            showAuth();
            return;
        }
        activeLesson = null;
        isQuizActive = false;
        lastResponsiveWidth = getEffectiveContentWidth();
        DigiView.log("NAV", "Page view switched to: " + page + " (User: " + currentUser.username + ", XP: " + currentUser.xp + ")");
        currentPage = page;
        if (pageTitle != null) pageTitle.setText(page);
        refreshHeaderStats();
        navButtons.forEach((key, btn) -> btn.setActive(key.equals(page)));

        switch (page) {
            case "Dashboard": renderDashboard(); break;
            case "Lessons": renderLessons(); break;
            case "Quiz": renderQuizSelection(); break;
            case "Achievements": renderAchievements(); break;
            case "History": renderHistory(); break;
            case "Profile": renderProfile(); break;
            default: renderDashboard(); break;
        }
    }

    private void refreshHeaderStats() {
        if (topXp != null) topXp.setText("\u26A1 " + currentUser.xp + " XP");
        if (topHearts != null) {
            topHearts.setText("<html><font color='#ef4444'>\u2764</font> " + quizHearts + "</html>");
        }
        if (sideLevel != null) sideLevel.setText("Level " + (currentUser.xp / 100 + 1));
    }
    
    private JPanel buildTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(D_TOP);
        top.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, new Color(24, 40, 64)),
            new EmptyBorder(14, 32, 14, 32)
        ));

        pageTitle = new JLabel("Dashboard");
        pageTitle.setFont(new Font("SansSerif", Font.PLAIN, 15));
        pageTitle.setForeground(new Color(190, 205, 225));

        topXp = new JLabel();
        topXp.setFont(new Font("SansSerif", Font.BOLD, 14));
        topXp.setForeground(TEXT);

        AvatarBadge avatar = new AvatarBadge(currentUser.username.substring(0, 1), 44);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        right.setOpaque(false);
        right.add(topXp);
        right.add(avatar);

        top.add(pageTitle, BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);
        return top;
    }

    private void showServerConnectionDialog() {
        JDialog dlg = new JDialog(this, "Cloud Server Connection Settings", true);
        dlg.getContentPane().setBackground(D_TOP);
        dlg.setLayout(new BorderLayout());

        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel title = new JLabel("Cloud Backend Settings");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Configure your deployed Render or local Spring Boot server.");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13));
        sub.setForeground(MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(title);
        p.add(Box.createVerticalStrut(4));
        p.add(sub);
        p.add(Box.createVerticalStrut(18));

        JLabel urlLabel = new JLabel("Server Base URL:");
        urlLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        urlLabel.setForeground(CYAN);
        urlLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(urlLabel);
        p.add(Box.createVerticalStrut(6));

        final JTextField urlField = new JTextField(ApiClient.getBaseUrl());
        urlField.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        urlField.setBackground(new Color(14, 27, 48));
        urlField.setForeground(TEXT);
        urlField.setCaretColor(CYAN);
        urlField.setBorder(new CompoundBorder(new LineBorder(D_LINE, 1, true), new EmptyBorder(8, 12, 8, 12)));
        urlField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        urlField.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(urlField);
        p.add(Box.createVerticalStrut(10));

        JPanel presetRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        presetRow.setOpaque(false);
        presetRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton renderPreset = new JButton("☁️ Render Cloud");
        renderPreset.setBackground(new Color(24, 42, 68));
        renderPreset.setForeground(CYAN);
        renderPreset.setFocusPainted(false);
        renderPreset.setFont(new Font("SansSerif", Font.PLAIN, 12));
        renderPreset.addActionListener(e -> urlField.setText(ApiClient.DEFAULT_CLOUD_URL));

        JButton localPreset = new JButton("💻 Localhost (:8085)");
        localPreset.setBackground(new Color(24, 42, 68));
        localPreset.setForeground(TEXT);
        localPreset.setFocusPainted(false);
        localPreset.setFont(new Font("SansSerif", Font.PLAIN, 12));
        localPreset.addActionListener(e -> urlField.setText(ApiClient.DEFAULT_LOCAL_URL));

        JButton lanPreset = new JButton("📡 LAN (:8085)");
        lanPreset.setBackground(new Color(24, 42, 68));
        lanPreset.setForeground(TEXT);
        lanPreset.setFocusPainted(false);
        lanPreset.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lanPreset.addActionListener(e -> urlField.setText(ApiClient.getLocalLanUrl()));

        presetRow.add(renderPreset);
        presetRow.add(localPreset);
        presetRow.add(lanPreset);
        p.add(presetRow);
        p.add(Box.createVerticalStrut(16));

        final JLabel statusFeedback = new JLabel("Status: Ready to test connection.");
        statusFeedback.setFont(new Font("SansSerif", Font.PLAIN, 13));
        statusFeedback.setForeground(MUTED);
        statusFeedback.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(statusFeedback);
        p.add(Box.createVerticalStrut(18));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton testBtn = new JButton("⚡ Test Connection");
        testBtn.setBackground(new Color(30, 50, 80));
        testBtn.setForeground(CYAN);
        testBtn.setFocusPainted(false);
        testBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        testBtn.addActionListener(e -> {
            final String testUrl = urlField.getText().trim();
            statusFeedback.setText("Testing " + testUrl + "...");
            statusFeedback.setForeground(CYAN);
            new Thread(() -> {
                final ApiClient.ConnectionTestResult res = ApiClient.testConnection(testUrl);
                SwingUtilities.invokeLater(() -> {
                    if (res.success) {
                        statusFeedback.setText("✅ Online! HTTP " + res.httpCode + " (" + res.latencyMs + "ms latency)");
                        statusFeedback.setForeground(GREEN);
                    } else {
                        statusFeedback.setText("❌ " + res.message);
                        statusFeedback.setForeground(RED);
                    }
                });
            }).start();
        });

        JButton saveBtn = new JButton("Save & Connect");
        saveBtn.setBackground(BLUE);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        saveBtn.addActionListener(e -> {
            String newUrl = urlField.getText().trim();
            ApiClient.saveConfigUrl(newUrl);
            showThemedMessage("☁️", "Server Connected", "Server URL configured and saved!\nConnected to: " + ApiClient.getBaseUrl());
            dlg.dispose();
        });

        btnRow.add(testBtn);
        btnRow.add(saveBtn);
        p.add(btnRow);

        dlg.getContentPane().add(p, BorderLayout.CENTER);
        dlg.pack();
        dlg.setSize(560, 360);
        dlg.setLocationRelativeTo(this != null && this.isShowing() ? this : null);
        dlg.setVisible(true);
    }
    
    private void makeFullWidth(JComponent component) {
    component.setAlignmentX(Component.LEFT_ALIGNMENT);
    component.setMaximumSize(
        new Dimension(Integer.MAX_VALUE,
                      component.getPreferredSize().height)
    );
}
    
    
    private String codeSnippet(String code, int max) {
        String oneLine = code.replaceAll("\\s+", " ").trim();
        return oneLine.length() > max ? oneLine.substring(0, max).trim() : oneLine;
    }

    private void renderLessons() {
        JPanel p = verticalPanel();
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 10, 0));

        
        JLabel heading = new JLabel("HTML Learning Center");
        heading.setFont(new Font("SansSerif", Font.BOLD, 28));
        heading.setForeground(TEXT);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        heading.setBorder(new EmptyBorder(0, 0, 0, 12));

        JLabel sub = new JLabel("Learn the concept before taking the challenge.");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 14));
        sub.setForeground(MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(heading);
        p.add(Box.createVerticalStrut(6));
        p.add(sub);
        p.add(Box.createVerticalStrut(24));

        
        String[] summaries = {
            "Learn what HTML is and understand the basic structure of a webpage.",
            "Learn how headings and paragraphs organize webpage content.",
            "Learn how to connect webpages using hyperlinks.",
            "Learn how to display images using the img element.",
            "Learn how to create ordered and unordered lists.",
            "Learn how rows, headers, and data cells work in tables.",
            "Learn how forms collect information from users.",
            "Learn how block-level elements structure page sections."
        };

        
        int effW = getEffectiveContentWidth();
        int lCols = (effW >= 960) ? 3 : (effW >= 600 ? 2 : 1);
        JPanel grid = new JPanel(new GridLayout(0, lCols, 18, 18));
        grid.setOpaque(false);

        for (int i = 0; i < lessons.size(); i++) {
            final Lesson l = lessons.get(i);
            String summary = i < summaries.length ? summaries[i] : l.explanation;

            RoundedPanel card = new RoundedPanel(26, D_CARD, D_LINE);
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBorder(new EmptyBorder(24, 26, 24, 26));

            JLabel number = new JLabel(String.format("LESSON %02d", i + 1));
            number.setFont(new Font("SansSerif", Font.BOLD, 12));
            number.setForeground(CYAN);
            number.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel icon = new JLabel(l.icon);
            icon.setFont(new Font("SansSerif", Font.PLAIN, 30));
            icon.setForeground(TEXT);
            icon.setAlignmentX(Component.LEFT_ALIGNMENT);

            int titleW = Math.max(160, (effW / lCols) - 76);
            JLabel title = new JLabel("<html><div style='width:" + titleW + "px; font-weight:bold; font-size:20px; color:#f8fafc'>" + l.title + "</div></html>");
            title.setAlignmentX(Component.LEFT_ALIGNMENT);

            JTextArea desc = new JTextArea(summary);
            desc.setLineWrap(true);
            desc.setWrapStyleWord(true);
            desc.setEditable(false);
            desc.setFocusable(false);
            desc.setOpaque(false);
            desc.setBorder(null);
            desc.setForeground(MUTED);
            desc.setFont(new Font("SansSerif", Font.PLAIN, 14));
            desc.setAlignmentX(Component.LEFT_ALIGNMENT);
            desc.setPreferredSize(new Dimension(100, 48));
            desc.setMinimumSize(new Dimension(0, 48));
            desc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));


            RoundedPanel pill = new RoundedPanel(14, new Color(3, 9, 18), new Color(12, 24, 40));
            pill.setLayout(new BorderLayout());
            pill.setBorder(new EmptyBorder(0, 16, 0, 16));
            JLabel codeLabel = new JLabel(codeSnippet(l.code, 70));
            codeLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            codeLabel.setForeground(new Color(125, 211, 252));
            pill.add(codeLabel, BorderLayout.CENTER);


            RoundedButton learn = new RoundedButton("Learn More  \u2192", true);
            learn.setPreferredSize(new Dimension(150, 42));
            learn.setFont(new Font("SansSerif", Font.BOLD, 13));
            learn.addActionListener(e -> {
                playClickSound();
                showLesson(l);
            });

            JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            btnRow.setOpaque(false);
            btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            btnRow.add(learn);

            card.add(number);
            card.add(Box.createVerticalStrut(10));
            card.add(icon);
            card.add(Box.createVerticalStrut(8));
            card.add(title);
            card.add(Box.createVerticalStrut(6));
            card.add(desc);
            card.add(Box.createVerticalStrut(14));
            card.add(fixHeight(pill, 44));
            card.add(Box.createVerticalGlue());
            card.add(Box.createVerticalStrut(16));
            card.add(btnRow);

            grid.add(card);
        }

        int rows = (lessons.size() + lCols - 1) / lCols;
        p.add(fixHeight(grid, rows * 340 + (rows - 1) * 18));

        addToContent(p);
    }

    private void showLesson(Lesson l) {
        activeLesson = l;
        JPanel p = verticalPanel();
        p.setOpaque(false);

        RoundedButton back = new RoundedButton("\u2190  Back to Lessons", false);
        back.setForeground(Color.WHITE);
        back.setPreferredSize(new Dimension(190, 42));
        back.setMaximumSize(new Dimension(190, 42));
        back.setAlignmentX(Component.LEFT_ALIGNMENT);
        back.addActionListener(e -> {
            playClickSound();
            activeLesson = null;
            showPage("Lessons");
        });
        p.add(back);
        p.add(Box.createVerticalStrut(18));

        int effW = getEffectiveContentWidth();
        
        RoundedPanel card = new RoundedPanel(26, D_CARD, D_LINE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(28, 32, 28, 32));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel(l.icon + "  " + l.title);
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.setBorder(new EmptyBorder(0, 0, 0, 10));

        JLabel sub = new JLabel("Lesson explanation and example");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 14));
        sub.setForeground(MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        int descW = Math.max(260, effW - 84);
        JLabel explanation = new JLabel(
            "<html><table width='" + descW + "' cellpadding='0' cellspacing='0'><tr><td style='color:#d7e1f0; font-family:SansSerif; font-size:16px;'>"
            + l.explanation
            + "</td></tr></table></html>");
        explanation.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel codeTitle = new JLabel("Code Example");
        codeTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        codeTitle.setForeground(CYAN);
        codeTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea code = codeArea(l.code);
        code.setOpaque(false);
        code.setLineWrap(true);

        RoundedPanel codeBox = new RoundedPanel(16, new Color(3, 9, 18), new Color(20, 36, 58));
        codeBox.setLayout(new BorderLayout());
        codeBox.setBorder(new EmptyBorder(12, 14, 12, 14));
        codeBox.add(code, BorderLayout.CENTER);

        int lines = l.code.split("\n").length;
        int boxHeight = lines * 20 + 44;
        int boxW = Math.max(260, effW - 84);
        Dimension boxSize = new Dimension(boxW, boxHeight);
        codeBox.setPreferredSize(boxSize);
        codeBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, boxHeight));
        codeBox.setMinimumSize(new Dimension(200, boxHeight));
        codeBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        RoundedButton quiz = new RoundedButton("Take a Quiz  \u2192", true);
        quiz.setPreferredSize(new Dimension(170, 46));
        quiz.setMaximumSize(new Dimension(170, 46));
        quiz.setAlignmentX(Component.LEFT_ALIGNMENT);
        quiz.addActionListener(e -> {
            playClickSound();
            activeLesson = null;
            showPage("Quiz");
        });

        card.add(title);
        card.add(Box.createVerticalStrut(6));
        card.add(sub);
        card.add(Box.createVerticalStrut(24));
        card.add(explanation);
        card.add(Box.createVerticalStrut(28));
        card.add(codeTitle);
        card.add(Box.createVerticalStrut(10));
        card.add(codeBox);
        card.add(Box.createVerticalStrut(26));
        card.add(quiz);

        p.add(card);
        addToContent(p);
    }

    private void renderQuizSelection() {
    JPanel p = verticalPanel();
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 10, 0));


        HeroPanel hero = new HeroPanel();

        JPanel heroContent = new JPanel();
        heroContent.setOpaque(false);
        heroContent.setLayout(new BoxLayout(heroContent, BoxLayout.Y_AXIS));
        heroContent.setBorder(new EmptyBorder(40, 46, 40, 46));

        JLabel heroTitle = new JLabel(
            "<html>Ready for the <font color='#22d3ee'>HTML Challenge?</font></html>");
        heroTitle.setFont(new Font("SansSerif", Font.BOLD, 36));
        heroTitle.setForeground(TEXT);
        heroTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        int effW = getEffectiveContentWidth();
        int heroDescW = Math.max(260, effW - 140);

        JLabel heroDesc = new JLabel(
            "<html><table width='" + heroDescW + "' cellpadding='0' cellspacing='0'><tr><td style='color:#becde1; font-family:SansSerif; font-size:16px;'>"
            + "Choose a difficulty and test your knowledge. Be careful\u2014wrong answers cost one heart."
            + "</td></tr></table></html>");
        heroDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        heroContent.add(heroTitle);
        heroContent.add(Box.createVerticalStrut(14));
        heroContent.add(heroDesc);

        hero.add(heroContent, BorderLayout.CENTER);
        int heroH = effW < 700 ? 230 : 190;
        p.add(fixHeight(hero, heroH));
        p.add(Box.createVerticalStrut(24));


        String[] names = {"Easy", "Medium", "Hard"};
        String[] icons = {"\uD83C\uDF31", "\u26A1", "\uD83D\uDD25"};
        String[] tags = {"HTML Basics", "Syntax & Code", "Complete the Code"};
        
        int[] counts = {25, 25, 20};

        boolean easyPassed = hasPassed("Easy");

        int qCols = (effW >= 860) ? 3 : (effW >= 560 ? 2 : 1);
        int qRows = (names.length + qCols - 1) / qCols;
        JPanel grid = new JPanel(new GridLayout(qRows, qCols, 18, 18));
        grid.setOpaque(false);

        for (int i = 0; i < names.length; i++) {
            final int level = i + 1;

            boolean unlocked = (level == 1) || easyPassed;

            RoundedPanel card = new RoundedPanel(26, D_CARD, D_LINE);
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBorder(new EmptyBorder(22, 26, 24, 26));


            JPanel topRow = new JPanel(new BorderLayout());
            topRow.setOpaque(false);

            JLabel icon = new JLabel(icons[i]);
            icon.setFont(new Font("SansSerif", Font.PLAIN, 34));
            icon.setForeground(TEXT);
            topRow.add(icon, BorderLayout.WEST);

            if (!unlocked) {
                JLabel lock = new JLabel("\uD83D\uDD12 Locked");
                lock.setFont(new Font("SansSerif", Font.PLAIN, 13));
                lock.setForeground(new Color(180, 140, 40));
                topRow.add(lock, BorderLayout.NORTH);
                lock.setHorizontalAlignment(SwingConstants.RIGHT);
            }


            JLabel title = new JLabel(names[i]);
            title.setFont(new Font("SansSerif", Font.BOLD, 24));
            title.setForeground(unlocked ? TEXT : MUTED);
            title.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel info = new JLabel(counts[i] + " questions \u2022 " + tags[i]);
            info.setFont(new Font("SansSerif", Font.PLAIN, 14));
            info.setForeground(unlocked ? new Color(170, 185, 205) : new Color(95, 110, 135));
            info.setAlignmentX(Component.LEFT_ALIGNMENT);


            JComponent action;
            if (unlocked) {
                RoundedButton start = new RoundedButton("Start " + names[i] + "  \u2192", true);
                start.setFont(new Font("SansSerif", Font.BOLD, 14));
                start.addActionListener(e -> {
                    playClickSound();
                    startQuiz(level);
                });
                action = start;
            } else {
                RoundedPanel locked = new RoundedPanel(16, new Color(16, 28, 48), D_LINE);
                locked.setLayout(new BorderLayout());
                String reqText = "🔒 Reach 70% in Easy";
                JLabel lockedText = new JLabel(reqText, SwingConstants.CENTER);
                lockedText.setFont(new Font("SansSerif", Font.BOLD, 13));
                lockedText.setForeground(new Color(245, 158, 11));
                locked.add(lockedText, BorderLayout.CENTER);
                locked.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                locked.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        playClickSound();
                        startQuiz(level);
                    }
                });
                action = locked;
            }

            JLabel hearts = new JLabel(
                "<html><font color='" + (unlocked ? "#ef4444" : "#5b6477") + "'>"
                + "\u2764 \u2764 \u2764 \u2764 \u2764</font></html>");
            hearts.setFont(new Font("SansSerif", Font.PLAIN, 16));

            JPanel titleRow = new JPanel(new BorderLayout());
            titleRow.setOpaque(false);
            titleRow.add(title, BorderLayout.WEST);
            titleRow.add(hearts, BorderLayout.EAST);

            card.add(fixHeight(topRow, 50));
            card.add(Box.createVerticalStrut(10));
            card.add(fixHeight(titleRow, 34));
            card.add(Box.createVerticalStrut(6));
            card.add(info);
            card.add(Box.createVerticalGlue());
            card.add(Box.createVerticalStrut(18));
            card.add(fixHeight(action, 52));

            grid.add(card);
        }

        p.add(fixHeight(grid, qRows * 265 + (qRows - 1) * 18));
        p.add(Box.createVerticalStrut(24));


        RoundedPanel passing = new RoundedPanel(26, D_CARD, D_LINE);
        passing.setLayout(new BoxLayout(passing, BoxLayout.Y_AXIS));
        passing.setBorder(new EmptyBorder(22, 28, 22, 28));

        JLabel passTitle = new JLabel("\uD83C\uDFAF  Passing Score");
        passTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
        passTitle.setForeground(TEXT);
        passTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel passDesc = new JLabel(
            "<html>You need at least <b>70%</b> in Easy mode to unlock Intermediate and Hard difficulties.</html>");
        passDesc.setFont(new Font("SansSerif", Font.PLAIN, 16));
        passDesc.setForeground(new Color(170, 185, 205));
        passDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        passing.add(passTitle);
        passing.add(Box.createVerticalStrut(10));
        passing.add(passDesc);

        p.add(fixHeight(passing, 120));

        addToContent(p);
}

    private void startQuiz(int difficulty) {
        if (currentUser == null) {
            message("Please log in first.", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if ((difficulty == 2 || difficulty == 3) && !hasPassed("Easy")) {
            showThemedMessage(
                "🔒",
                "Difficulty Locked",
                "You must reach at least 70% in the Easy Quiz to unlock " + difficultyName(difficulty) + "."
            );
            return;
        }

    

    boolean ready = showThemedConfirm(
        "🚀",
        "Start " + difficultyName(difficulty) + " Quiz",
        "Are you ready to take this quiz?\nYou will have 5 hearts. Each wrong answer deducts 1 heart.",
        "START QUIZ",
        "CANCEL"
    );

    if (!ready) {
        return;
    }

    List<Question> pool = new ArrayList<>();

    for (Question question : questionBank) {
        if (question.difficulty == difficulty) {
            pool.add(question);
        }
    }

    if (pool.isEmpty()) {
        message(
            "No questions found for "
                + difficultyName(difficulty) + " Quiz.",
            JOptionPane.ERROR_MESSAGE
        );
        return;
    }

    Collections.shuffle(pool);

    int limit;
    switch (difficulty) {
        case 1: limit = 25; break;   
        case 2: limit = 25; break; 
        case 3: limit = 20; break;   
        default: limit = 10; break;
    }

    if (pool.size() > limit) {
        pool = new ArrayList<>(pool.subList(0, limit));
    }

    quizHearts = 5;

    runQuiz(pool, 0, 0, difficulty);
}
    
    private void runQuiz(List<Question> questions, int index, int score, int difficulty) {
        isQuizActive = true;
        if (index >= questions.size()) {
            isQuizActive = false;
            int percent = (int)Math.round(score * 100.0 / questions.size());
            currentUser.xp += score * 5;
            String qName = (percent < 70) ? difficultyName(difficulty) + " (Failed)" : difficultyName(difficulty);
            currentUser.history.add(new QuizRecord(qName, score, questions.size(), percent, new Date().toString()));
            final String uName = currentUser.username;
            if (percent >= 70) {
                currentUser.achievements.add("HTML Quiz Pass");
                new Thread(() -> ApiClient.unlockAchievement(uName, "HTML Quiz Pass")).start();
            }
            if (percent == 100) {
                currentUser.achievements.add("Perfect Score");
                new Thread(() -> ApiClient.unlockAchievement(uName, "Perfect Score")).start();
            }
            saveData();
            final int sScore = score;
            final int tTotal = questions.size();
            final int pPercent = percent;
            final String recordName = qName;
            new Thread(() -> ApiClient.submitQuizRecord(uName, recordName, sScore, tTotal, pPercent, new Date().toString())).start();
            showQuizResult(score, questions.size(), percent, difficulty);
            return;
        }
        
        Question q = questions.get(index);
        JPanel p = verticalPanel();
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 10, 0));

        
        JLabel quizTitle = new JLabel(difficultyName(difficulty) + " Quiz");
        quizTitle.setFont(new Font("SansSerif", Font.BOLD, 30));
        quizTitle.setForeground(TEXT);
        quizTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel progressLabel = new JLabel("Question " + (index + 1) + " of " + questions.size());
        progressLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        progressLabel.setForeground(CYAN);
        progressLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.add(quizTitle);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(progressLabel);

        int[] liveScore = {score};
        RoundedButton exitQuiz = new RoundedButton("Exit Quiz", false);
        exitQuiz.setForeground(new Color(252, 165, 165));
        exitQuiz.setPreferredSize(new Dimension(130, 44));
        exitQuiz.addActionListener(e -> {
            playClickSound();
            boolean exit = showThemedConfirm(
                "⚠️",
                "Exit Quiz",
                "Are you sure you want to exit this quiz?\nYour current attempt will not be saved.",
                "EXIT",
                "CANCEL"
            );

            if (exit) {
                isQuizActive = false;
                String qName = difficultyName(difficulty) + " (Exited)";
                currentUser.history.add(new QuizRecord(
                    qName,
                    liveScore[0], questions.size(), 0, new Date().toString()
                ));
                saveData();
                final String uName = currentUser.username;
                final int lScore = liveScore[0];
                final int tTotal = questions.size();
                new Thread(() -> ApiClient.submitQuizRecord(uName, qName, lScore, tTotal, 0, new Date().toString())).start();
                showPage("Quiz");
            }
        });

        JPanel exitWrap = new JPanel(new GridBagLayout());
        exitWrap.setOpaque(false);
        exitWrap.add(exitQuiz);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(titleBox, BorderLayout.WEST);
        header.add(exitWrap, BorderLayout.EAST);
        p.add(fixHeight(header, 80));
        p.add(Box.createVerticalStrut(16));

        
        RoundedPanel card = new RoundedPanel(26, D_CARD, D_LINE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(24, 30, 26, 30));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel heartsLabel = createHeartsLabel();
        heartsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel number = label(String.format("QUESTION %02d", index + 1), 13, CYAN, true);
        number.setAlignmentX(Component.LEFT_ALIGNMENT);

        
        int effW = getEffectiveContentWidth();
        int qW = Math.max(260, effW - 84);
        String safeText = q.text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        JLabel question = new JLabel(
            "<html><table width='" + qW + "' cellpadding='0' cellspacing='0'><tr><td style='color:#f8fafc; font-family:SansSerif; font-size:24px; font-weight:bold;'>"
            + safeText
            + "</td></tr></table></html>");
        question.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(heartsLabel);
        card.add(Box.createVerticalStrut(14));
        card.add(number);
        card.add(Box.createVerticalStrut(8));
        card.add(question);

        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));
        p.add(card);
        p.add(Box.createVerticalStrut(20));

        
        List<Integer> order = new ArrayList<>(Arrays.asList(0, 1, 2, 3));
        Collections.shuffle(order);

        String[] letters = {"A", "B", "C", "D"};
        ButtonGroup group = new ButtonGroup();
        List<AnswerOption> options = new ArrayList<>();

        for (int pos = 0; pos < order.size(); pos++) {
            int oi = order.get(pos);
            AnswerOption opt = new AnswerOption(letters[pos], q.options.get(oi), oi);
            opt.addActionListener(e -> playClickSound());
            group.add(opt);
            options.add(opt);
            p.add(fixHeight(opt, 58));
            p.add(Box.createVerticalStrut(12));
        }

        
        JLabel feedback = label("Choose the best answer.", 15, MUTED, false);
        feedback.setAlignmentX(Component.LEFT_ALIGNMENT);

        RoundedButton submit = new RoundedButton("Submit Answer", true);
        submit.setPreferredSize(new Dimension(220, 50));
        submit.setMaximumSize(new Dimension(220, 50));
        submit.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(Box.createVerticalStrut(4));
        p.add(feedback);
        p.add(Box.createVerticalStrut(12));
        p.add(submit);

        submit.addActionListener(e -> {
            playClickSound();

            if (group.getSelection() == null) {
                message(
                    "Please select an answer first.",
                    JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            int chosen = Integer.parseInt(
                group.getSelection().getActionCommand()
            );

            boolean correct = chosen == q.answer;
            
            liveScore[0] = score + (correct ? 1 : 0);

            submit.setEnabled(false);

            for (AnswerOption o : options) {
                if (o.optionIndex == chosen) {
                    o.lock(correct ? AnswerOption.CORRECT : AnswerOption.WRONG);
                } else {
                    o.lock(AnswerOption.NORMAL);
                }
            }

            if (!correct) {
                quizHearts--;
            }

            DigiView.log("QUIZ", "Q" + (index + 1) + "/" + questions.size() + " Answer Submitted: Chosen=" + chosen + " (" + (correct ? "CORRECT" : "WRONG") + ") | Hearts: " + quizHearts + " | Current Score: " + liveScore[0]);

            updateHeartsLabel(heartsLabel);

            if (quizHearts <= 0) {
                isQuizActive = false;
                int currentPercent = (int)Math.round(liveScore[0] * 100.0 / questions.size());
                String qName = difficultyName(difficulty) + " (Failed)";
                currentUser.history.add(new QuizRecord(qName, liveScore[0], questions.size(), currentPercent, new Date().toString()));
                saveData();
                final String uName = currentUser.username;
                final int lScore = liveScore[0];
                final int tTotal = questions.size();
                final int cPercent = currentPercent;
                new Thread(() -> ApiClient.submitQuizRecord(uName, qName, lScore, tTotal, cPercent, new Date().toString())).start();

                message(
                    "You ran out of hearts! Please review the lessons and try again.\nThis attempt has been marked as 'Needs Review' in your Quiz History.",
                    JOptionPane.WARNING_MESSAGE
                );

                showPage("Quiz");
                return;
            }

            feedback.setText(
                correct
                    ? "Correct answer!"
                    : "Incorrect answer. One heart was deducted."
            );

            feedback.setForeground(correct ? GREEN : RED);

            RoundedButton next = new RoundedButton(
                index == questions.size() - 1
                    ? "View Results \u2192"
                    : "Next Question \u2192",
                true
            );
            next.setPreferredSize(new Dimension(220, 50));
            next.setMaximumSize(new Dimension(220, 50));
            next.setAlignmentX(Component.LEFT_ALIGNMENT);

            next.addActionListener(ev -> {
                playClickSound();
                runQuiz(
                    questions,
                    index + 1,
                    score + (correct ? 1 : 0),
                    difficulty
                );
            });

            p.add(Box.createVerticalStrut(12));
            p.add(next);

            p.revalidate();
            p.repaint();
        });

        addToContent(p);
    }
    
    private void showQuizResult( int score, int total, int percent, int difficulty ) {
        DigiView.log("QUIZ", "Quiz Finished: Level=" + (difficulty == 1 ? "Easy" : difficulty == 2 ? "Medium" : "Hard") + " | Score=" + score + "/" + total + " (" + percent + "%) | Status=" + (percent >= 70 ? "PASSED" : "FAILED"));
        
        JPanel p = verticalPanel();
        JPanel card = panel(); card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS)); 
        card.setBorder(new EmptyBorder(30, 30, 30, 30));
        card.add(label("Quiz Completed!", 28, CYAN, true)); 
        card.add(Box.createVerticalStrut(12));
        card.add(label("Your score: " + score + " / " + total, 22, TEXT, true)); 
        card.add(Box.createVerticalStrut(8));
        card.add(label("Percentage: " + percent + "%", 18, percent >= 70 ? GREEN : new Color(251, 191, 36), true));
        card.add(Box.createVerticalStrut(15));
        String resultMessage; 

if (percent < 70) {
    resultMessage =
        "You need 70% to pass. Review the lessons and try again.";
} else if (difficulty == 1) {
    resultMessage =
        "Easy passed! Medium Quiz is now unlocked.";
} else if (difficulty == 2) {
    resultMessage =
        "Medium passed! Hard Quiz is now unlocked.";
} else {
    resultMessage =
        "Hard passed! Congratulations on mastering all levels.";
}

card.add(label(resultMessage, 14, MUTED, false));
        card.add(Box.createVerticalStrut(20));
        JButton again = button("Try Another Quiz", BLUE); again.addActionListener(e -> showPage("Quiz"));
        JButton hist = button("View History", PANEL2); hist.addActionListener(e -> showPage("History"));
        JButton dashboard = button("Back to Dashboard", PANEL2); dashboard.addActionListener(e -> showPage("Dashboard"));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT)); actions.setOpaque(false); actions.add(again); actions.add(hist); actions.add(dashboard); card.add(actions);
        p.add(card); addToContent(p);
    }

    private void renderAchievements() {
        JPanel p = verticalPanel();
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 10, 0));

        List<Achievement> items = AchievementRegistry.evaluateAchievements(
            currentUser.history,
            currentUser.xp,
            currentUser.achievements
        );

        int unlockedCount = 0;
        for (Achievement it : items) {
            if (it.isUnlocked()) unlockedCount++;
        }


        HeroPanel hero = new HeroPanel();

        JPanel heroContent = new JPanel();
        heroContent.setOpaque(false);
        heroContent.setLayout(new BoxLayout(heroContent, BoxLayout.Y_AXIS));
        heroContent.setBorder(new EmptyBorder(30, 46, 30, 46));

        JLabel heroTitle = new JLabel(
            "<html>Your <font color='#22d3ee'>Achievements</font>&nbsp; \uD83C\uDFC6</html>");
        heroTitle.setFont(new Font("SansSerif", Font.BOLD, 34));
        heroTitle.setForeground(TEXT);
        heroTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        int effW = getEffectiveContentWidth();
        int heroDescW = Math.max(260, effW - 140);
        JLabel heroDesc = new JLabel("<html><table width='" + heroDescW + "' cellpadding='0' cellspacing='0'><tr><td style='color:#becde1; font-family:SansSerif; font-size:15px;'>"
                + unlockedCount + " of " + items.size()
                + " unlocked \u2014 achievements are based on your quiz results.</td></tr></table></html>");
        heroDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        ProgressPill overall = new ProgressPill(unlockedCount * 100 / items.size());
        overall.setAlignmentX(Component.LEFT_ALIGNMENT);
        int barW = Math.min(500, Math.max(200, effW - 120));
        overall.setPreferredSize(new Dimension(barW, 14));
        overall.setMaximumSize(new Dimension(500, 14));

        heroContent.add(heroTitle);
        heroContent.add(Box.createVerticalStrut(10));
        heroContent.add(heroDesc);
        heroContent.add(Box.createVerticalStrut(18));
        heroContent.add(overall);

        hero.add(heroContent, BorderLayout.CENTER);
        p.add(fixHeight(hero, 180));
        p.add(Box.createVerticalStrut(24));


        int aCols = (effW >= 960) ? 3 : (effW >= 600 ? 2 : 1);
        int aRows = (items.size() + aCols - 1) / aCols;
        JPanel grid = new JPanel(new GridLayout(0, aCols, 18, 18));
        grid.setOpaque(false);

        for (Achievement it : items) {
            String icon = it.icon;
            String name = it.title;
            String desc = it.description;
            int current = it.currentProgress;
            int target = it.targetProgress;
            boolean unlocked = it.isUnlocked();

            RoundedPanel card = new RoundedPanel(26,
                unlocked ? D_CARD : new Color(11, 21, 38),
                unlocked ? new Color(40, 110, 160) : D_LINE);
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBorder(new EmptyBorder(22, 26, 22, 26));


            JPanel topRow = new JPanel(new BorderLayout());
            topRow.setOpaque(false);
            topRow.add(new IconBox(icon), BorderLayout.WEST);

            JLabel status = new JLabel(unlocked ? "\u2713 Unlocked" : "\uD83D\uDD12 Locked");
            status.setFont(new Font("SansSerif", Font.BOLD, 12));
            status.setForeground(unlocked ? GREEN : new Color(180, 140, 40));
            topRow.add(status, BorderLayout.EAST);


            int cardW = Math.max(160, (effW - (aCols - 1) * 18) / aCols - 56);
            JLabel title = new JLabel("<html><div style='width:" + cardW + "px'>" + name + "</div></html>");
            title.setFont(new Font("SansSerif", Font.BOLD, 18));
            title.setForeground(unlocked ? TEXT : MUTED);
            title.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel description = new JLabel("<html><div style='width:" + cardW + "px'>" + desc + "</div></html>");
            description.setFont(new Font("SansSerif", Font.PLAIN, 13));
            description.setForeground(unlocked ? new Color(170, 185, 205) : new Color(110, 125, 150));
            description.setAlignmentX(Component.LEFT_ALIGNMENT);

      
            int shown = Math.min(current, target);
            int percent = it.getProgressPercent();

            JLabel progressText = new JLabel(shown + " / " + target);
            progressText.setFont(new Font("SansSerif", Font.BOLD, 12));
            progressText.setForeground(unlocked ? CYAN : MUTED);
            progressText.setAlignmentX(Component.LEFT_ALIGNMENT);

            card.add(fixHeight(topRow, 52));
            card.add(Box.createVerticalStrut(14));
            card.add(title);
            card.add(Box.createVerticalStrut(6));
            card.add(description);
            card.add(Box.createVerticalGlue());
            card.add(Box.createVerticalStrut(16));
            card.add(progressText);
            card.add(Box.createVerticalStrut(6));
            card.add(fixHeight(new ProgressPill(percent), 10));

            grid.add(card);
        }

        p.add(fixHeight(grid, aRows * 230 + (aRows - 1) * 18));

        addToContent(p);
    }
    
    private void renderHistory() {
        JPanel p = verticalPanel();
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 10, 0));
        p.add(sectionHeading("Quiz History", "Your quiz results are saved locally on this computer."));
        p.add(Box.createVerticalStrut(18));

        if (currentUser.history.isEmpty()) {
            p.add(emptyPanel("No quizzes completed yet. Start a quiz to see your results here."));
            addToContent(p);
            return;
        }

        
        List<QuizRecord> records = new ArrayList<>(currentUser.history);
        Collections.reverse(records);

        List<QuizRecord> finished = new ArrayList<>();
        for (QuizRecord r : records) {
            if (!r.quiz.endsWith("(Exited)") && !r.quiz.contains("(Exited)")) finished.add(r);
        }
        long passed = finished.stream().filter(r -> r.percent >= 70 && !r.quiz.contains("(Failed)")).count();
        long needsReview = finished.stream().filter(r -> r.percent < 70 || r.quiz.contains("(Failed)")).count();
        int best = finished.stream().mapToInt(r -> r.percent).max().orElse(0);

        String[][] summary = {
            {"\uD83D\uDCDD", "Completed", String.valueOf(finished.size())},
            {"\uD83C\uDFC6", "Passed", String.valueOf(passed)},
            {"\u26A0\uFE0F", "Needs Review", String.valueOf(needsReview)},
            {"\uD83C\uDFAF", "Best Score", best + "%"}
        };

        int effW = getEffectiveContentWidth();
        int hCols = (effW >= 920) ? 4 : (effW >= 540 ? 2 : 1);
        int hRows = (summary.length + hCols - 1) / hCols;
        JPanel stats = new JPanel(new GridLayout(hRows, hCols, 18, 18));
        stats.setOpaque(false);

        for (String[] item : summary) {
            RoundedPanel card = new RoundedPanel(22, D_CARD, D_LINE);
            card.setLayout(new BorderLayout(16, 0));
            card.setBorder(new EmptyBorder(16, 20, 16, 20));

            JPanel iconWrap = new JPanel(new GridBagLayout());
            iconWrap.setOpaque(false);
            iconWrap.add(new IconBox(item[0]));

            JLabel title = new JLabel(item[1]);
            title.setFont(new Font("SansSerif", Font.PLAIN, 13));
            title.setForeground(MUTED);
            title.setBorder(new EmptyBorder(0, 0, 0, 8));

            JLabel value = new JLabel(item[2]);
            value.setFont(new Font("SansSerif", Font.BOLD, 26));
            value.setForeground(TEXT);
            value.setBorder(new EmptyBorder(0, 0, 0, 8));

            JPanel text = new JPanel();
            text.setOpaque(false);
            text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
            text.add(Box.createVerticalGlue());
            text.add(title);
            text.add(value);
            text.add(Box.createVerticalGlue());

            card.add(iconWrap, BorderLayout.WEST);
            card.add(text, BorderLayout.CENTER);
            stats.add(card);
        }

        p.add(fixHeight(stats, hRows * 96 + (hRows - 1) * 18));
        p.add(Box.createVerticalStrut(24));

        
        JLabel listTitle = new JLabel("All Attempts");
        listTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
        listTitle.setForeground(TEXT);
        listTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(listTitle);
        p.add(Box.createVerticalStrut(14));

        for (QuizRecord r : records) {
            p.add(fixHeight(buildHistoryRow(r), 88));
            p.add(Box.createVerticalStrut(12));
        }

        addToContent(p);
    }

    
    private JPanel buildHistoryRow(QuizRecord r) {
        boolean exited = r.quiz.contains("(Exited)");
        boolean failed = r.quiz.contains("(Failed)") || r.quiz.contains("(0 Hearts)") || (!exited && r.percent < 70);
        String level = r.quiz
                .replace(" (Exited)", "")
                .replace("(Exited)", "")
                .replace(" (Failed)", "")
                .replace("(Failed)", "")
                .replace(" (0 Hearts)", "")
                .replace("(0 Hearts)", "")
                .replace(" Quiz", "")
                .trim();
        String icon = (level.startsWith("Easy") || level.startsWith("Beginner")) ? "\uD83C\uDF31"
                    : (level.startsWith("Medium") || level.startsWith("Intermediate")) ? "\u26A1" : "\uD83D\uDD25";

        RoundedPanel row = new RoundedPanel(22, D_CARD, D_LINE);
        row.setLayout(new BorderLayout(18, 0));
        row.setBorder(new EmptyBorder(14, 22, 14, 22));

        
        JPanel iconWrap = new JPanel(new GridBagLayout());
        iconWrap.setOpaque(false);
        iconWrap.add(new IconBox(icon));
        row.add(iconWrap, BorderLayout.WEST);

        
        JLabel title = new JLabel(level + " Quiz");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(TEXT);
        title.setBorder(new EmptyBorder(0, 0, 0, 8));

        JLabel date = new JLabel(formatHistoryDate(r.date));
        date.setFont(new Font("SansSerif", Font.PLAIN, 13));
        date.setForeground(MUTED);
        date.setBorder(new EmptyBorder(0, 0, 0, 8));

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.add(Box.createVerticalGlue());
        info.add(title);
        info.add(Box.createVerticalStrut(4));
        info.add(date);
        info.add(Box.createVerticalGlue());
        row.add(info, BorderLayout.CENTER);

        
        JLabel score = new JLabel(r.score + " / " + r.total);
        score.setFont(new Font("SansSerif", Font.BOLD, 18));
        score.setForeground(TEXT);

        JLabel percent = new JLabel(exited ? "-" : r.percent + "%");
        percent.setFont(new Font("SansSerif", Font.BOLD, 14));
        percent.setForeground(failed ? new Color(251, 191, 36) : MUTED);

        JPanel scoreLine = new JPanel(new BorderLayout());
        scoreLine.setOpaque(false);
        scoreLine.add(score, BorderLayout.WEST);
        scoreLine.add(percent, BorderLayout.EAST);

        ProgressPill bar = new ProgressPill(exited ? 0 : r.percent);
        bar.setPreferredSize(new Dimension(190, 8));

        JPanel scoreBox = new JPanel(new BorderLayout(0, 6));
        scoreBox.setOpaque(false);
        scoreBox.add(scoreLine, BorderLayout.NORTH);
        scoreBox.add(bar, BorderLayout.SOUTH);
        scoreBox.setPreferredSize(new Dimension(190, 46));

        
        JComponent badge;
        if (exited) {
            badge = statusBadge("Exited", new Color(148, 163, 184));
        } else if (!failed && r.percent >= 70) {
            badge = statusBadge("Passed", GREEN);
        } else {
            badge = statusBadge("Needs Review", new Color(251, 191, 36));
        }

        JPanel right = new JPanel(new GridBagLayout());
        right.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        right.add(scoreBox, gbc);
        gbc.insets = new Insets(0, 24, 0, 0);
        gbc.ipadx = 0;
        right.add(badge, gbc);
        row.add(right, BorderLayout.EAST);

        return row;
    }

    
    private JComponent statusBadge(String text, Color color) {
        RoundedPanel badge = new RoundedPanel(16,
            new Color(color.getRed(), color.getGreen(), color.getBlue(), 40),
            new Color(color.getRed(), color.getGreen(), color.getBlue(), 150));
        badge.setLayout(new GridBagLayout());
        badge.setBorder(new EmptyBorder(7, 14, 7, 14));
        badge.setPreferredSize(new Dimension(140, 34));

        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        label.setForeground(color);
        badge.add(label);
        return badge;
    }

    
    private String formatHistoryDate(String raw) {
        try {
            java.util.Date d = new java.text.SimpleDateFormat(
                "EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH).parse(raw);
            return new java.text.SimpleDateFormat(
                "MMM dd, yyyy  \u2022  h:mm a", Locale.ENGLISH).format(d);
        } catch (Exception e) {
            return raw;
        }
    }

    private void renderProfile() {
        JPanel p = verticalPanel();
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 10, 0));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(sectionHeading("My Profile", "Your registration information."));
        p.add(Box.createVerticalStrut(18));

        int level = currentUser.xp / 100 + 1;
        int progress = currentUser.xp % 100;

        
        HeroPanel hero = new HeroPanel();
        hero.setBorder(new EmptyBorder(30, 40, 30, 40));

        JPanel avatarWrap = new JPanel(new GridBagLayout());
        avatarWrap.setOpaque(false);
        avatarWrap.setBorder(new EmptyBorder(0, 0, 0, 30));
        avatarWrap.add(new AvatarBadge(currentUser.username.substring(0, 1), 110));

        JLabel name = new JLabel(currentUser.fullName);
        name.setFont(new Font("SansSerif", Font.BOLD, 32));
        name.setForeground(TEXT);
        name.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel handle = new JLabel("@" + currentUser.username);
        handle.setFont(new Font("SansSerif", Font.PLAIN, 16));
        handle.setForeground(new Color(34, 211, 238));
        handle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        chips.setOpaque(false);
        chips.setAlignmentX(Component.LEFT_ALIGNMENT);
        chips.add(profileChip("\u2B50  Level " + level, new Color(56, 189, 248)));
        chips.add(Box.createHorizontalStrut(10));
        chips.add(profileChip("\u26A1  " + currentUser.xp + " XP", new Color(250, 204, 21)));
        chips.add(Box.createHorizontalStrut(10));
        chips.add(profileChip("\uD83C\uDFEB  " + currentUser.gradeSection, new Color(148, 163, 184)));

        JPanel nameBox = new JPanel();
        nameBox.setOpaque(false);
        nameBox.setLayout(new BoxLayout(nameBox, BoxLayout.Y_AXIS));
        nameBox.add(Box.createVerticalGlue());
        nameBox.add(name);
        nameBox.add(Box.createVerticalStrut(4));
        nameBox.add(handle);
        nameBox.add(Box.createVerticalStrut(16));
        nameBox.add(chips);
        nameBox.add(Box.createVerticalGlue());

        int effW = getEffectiveContentWidth();

        hero.add(avatarWrap, BorderLayout.WEST);
        hero.add(nameBox, BorderLayout.CENTER);
        int heroH = effW < 650 ? 250 : 200;
        p.add(fixHeight(hero, heroH));
        p.add(Box.createVerticalStrut(22));

        
        int pCols = (effW >= 650) ? 2 : 1;
        int pRows = (4 + pCols - 1) / pCols;
        JPanel grid = new JPanel(new GridLayout(pRows, pCols, 18, 18));
        grid.setOpaque(false);
        grid.add(profileTile("\uD83D\uDC64", "Full Name", currentUser.fullName));
        grid.add(profileTile("\uD83C\uDFF7", "Username", currentUser.username));
        grid.add(profileTile("\u26A5", "Gender", currentUser.gender));
        grid.add(profileTile("\uD83C\uDFEB", "Grade & Section", currentUser.gradeSection));
        p.add(fixHeight(grid, pRows * 86 + (pRows - 1) * 18));
        p.add(Box.createVerticalStrut(22));

        
        RoundedPanel levelCard = new RoundedPanel(22, new Color(0, 0, 0, 0), D_LINE);
        levelCard.setLayout(new BorderLayout(0, 14));
        levelCard.setBorder(new EmptyBorder(20, 26, 22, 26));

        JLabel levelTitle = new JLabel("Progress to Level " + (level + 1));
        levelTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        levelTitle.setForeground(TEXT);

        JLabel levelXp = new JLabel(currentUser.xp + " / " + (level * 100) + " XP");
        levelXp.setFont(new Font("SansSerif", Font.BOLD, 14));
        levelXp.setForeground(MUTED);

        JPanel levelTop = new JPanel(new BorderLayout());
        levelTop.setOpaque(false);
        levelTop.add(levelTitle, BorderLayout.WEST);
        levelTop.add(levelXp, BorderLayout.EAST);

        ProgressPill bar = new ProgressPill(progress);
        bar.setPreferredSize(new Dimension(100, 12));

        levelCard.add(levelTop, BorderLayout.NORTH);
        levelCard.add(bar, BorderLayout.CENTER);
        p.add(fixHeight(levelCard, 100));

        addToContent(p);
    }

    
    private JPanel profileTile(String icon, String name, String value) {
        RoundedPanel tile = new RoundedPanel(22, new Color(0, 0, 0, 0), D_LINE);
        tile.setLayout(new BorderLayout(18, 0));
        tile.setBorder(new EmptyBorder(16, 22, 16, 22));

        JPanel iconWrap = new JPanel(new GridBagLayout());
        iconWrap.setOpaque(false);
        iconWrap.add(new IconBox(icon));

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        nameLabel.setForeground(MUTED);
        nameLabel.setBorder(new EmptyBorder(0, 0, 0, 4));

        JLabel valueLabel = new JLabel("<html>" + value + "</html>");
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        valueLabel.setForeground(TEXT);
        valueLabel.setBorder(new EmptyBorder(0, 0, 0, 4));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(Box.createVerticalGlue());
        text.add(nameLabel);
        text.add(Box.createVerticalStrut(3));
        text.add(valueLabel);
        text.add(Box.createVerticalGlue());

        tile.add(iconWrap, BorderLayout.WEST);
        tile.add(text, BorderLayout.CENTER);
        return tile;
    }

    
    private JComponent profileChip(String text, Color color) {
        RoundedPanel chip = new RoundedPanel(16,
            new Color(color.getRed(), color.getGreen(), color.getBlue(), 35),
            new Color(color.getRed(), color.getGreen(), color.getBlue(), 140));
        chip.setLayout(new GridBagLayout());
        chip.setBorder(new EmptyBorder(6, 16, 6, 16));

        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        label.setForeground(new Color(235, 242, 252));
        label.setBorder(new EmptyBorder(0, 0, 0, 6));
        chip.add(label);
        return chip;
    }
    

    private void addProfileRow(JPanel p, String name, String value) {
        p.add(label(name, 13, MUTED, true)); p.add(label(value, 14, TEXT, false));
    }
    private void addField(JPanel p, String name, JComponent field) {
        JLabel title = fieldLabel(name);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(title);
        p.add(field);
        p.add(Box.createVerticalStrut(5));
}
    private JLabel fieldLabel(String text) {
        JLabel l = label(text, 12, new Color(203, 213, 225), true); l.setBorder(new EmptyBorder(6, 0, 5, 0)); l.setAlignmentX(Component.LEFT_ALIGNMENT); return l;
    }
    private JTextField field(String placeholder) {
        JTextField f = new JTextField(); styleField(f); f.putClientProperty("JTextField.placeholderText", placeholder); return f;
    }
    private void styleField(JTextField f) {
        f.setBackground(new Color(8, 22, 41));
        f.setForeground(TEXT);
        f.setCaretColor(TEXT);
        f.setFont(new Font("SansSerif", Font.PLAIN, 14));

        f.setBorder(new CompoundBorder(
            new LineBorder(new Color(48, 68, 92)),
            new EmptyBorder(8, 10, 8, 10)
    ));

        f.setPreferredSize(new Dimension(340, 40));
        f.setMinimumSize(new Dimension(100, 40));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);

        if (f instanceof JPasswordField) {
            ((JPasswordField) f).setEchoChar('\u2022');
    }
}

    private void styleCombo(JComboBox<String> c) {
    c.setBackground(new Color(8, 22, 41));
    c.setForeground(TEXT);
    c.setFont(new Font("SansSerif", Font.PLAIN, 14));

    c.setPreferredSize(new Dimension(340, 40));
    c.setMinimumSize(new Dimension(100, 40));
    c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
    c.setAlignmentX(Component.LEFT_ALIGNMENT);
}
    private JButton button(String text, Color color) {
        JButton b = new JButton(text);

        b.setUI(new BasicButtonUI());
        b.setBackground(color); b.setForeground(Color.WHITE); b.setOpaque(true);
        b.setContentAreaFilled(true); b.setBorderPainted(true);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setBorder(new CompoundBorder(new LineBorder(color.darker()), new EmptyBorder(11, 16, 11, 16)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); b.addActionListener(e -> playClickSound()); return b;
    }
    private JButton textButton(String text) {
        JButton b = new JButton(text); 
        b.setUI(new BasicButtonUI());
        b.setForeground(CYAN); 
        b.setBackground(PANEL); 
        b.setOpaque(true); 
        b.setContentAreaFilled(true); 
        b.setBorderPainted(false); 
        b.setFocusPainted(false); 
        b.addActionListener(e -> playClickSound()); return b;
    }
    private JLabel label(String text, int size, Color color, boolean bold) {
        JLabel l = new JLabel("<html>" + text.replace("&", "&amp;").replace("\n", "<br>") + "</html>");
        l.setFont(new Font("SansSerif", bold ? Font.BOLD : Font.PLAIN, size)); l.setForeground(color); return l;
    }
    private JPanel panel() {
        JPanel p = new JPanel(); 
        p.setBackground(PANEL); 
        p.setBorder(new LineBorder(new Color(34, 52, 74))); return p;
    }
    private JPanel verticalPanel() {
        JPanel p = new JPanel(); 
        p.setBackground(BG); 
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); 
        p.setAlignmentX(Component.LEFT_ALIGNMENT); return p;
    }
    private JPanel sectionHeading(String title, String subtitle) {
        JPanel p = new JPanel(); 
        p.setOpaque(false); 
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel t = label(title, 23, TEXT, true); 
        JLabel s = label(subtitle, 13, MUTED, false);
        p.add(t); 
        p.add(Box.createVerticalStrut(7));
        p.add(s); p.add(Box.createVerticalStrut(16)); return p;
    }
    
    private JPanel statRow(String[][] stats) {
        JPanel p = new JPanel(new GridLayout(1, stats.length, 12, 0)); 
        p.setOpaque(false);
        
        for (String[] stat : stats) {
            JPanel c = panel(); 
            c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
            c.setBorder(new EmptyBorder(17, 18, 17, 18));
            c.add(label(stat[0], 12, MUTED, false)); 
            c.add(Box.createVerticalStrut(8)); 
            c.add(label(stat[1], 24, CYAN, true)); p.add(c);
        }
        return p;
    }
    
    private JPanel emptyPanel(String text) {
        JPanel c = panel(); 
        c.setBorder(new EmptyBorder(22,22,22,22)); 
        c.add(label(text, 14, MUTED, false)); return c;
    }
    
    private JTextArea codeArea(String code) {
        JTextArea a = new JTextArea(code); 
        a.setEditable(false); 
        a.setBackground(new Color(5, 11, 20)); 
        a.setForeground(new Color(125, 211, 252));
        a.setCaretColor(TEXT); 
        a.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13)); 
        a.setBorder(new EmptyBorder(12,12,12,12)); 
        a.setLineWrap(false); return a;
    }
    private void addToContent(JComponent component) {
    content.removeAll();

    component.setAlignmentX(Component.LEFT_ALIGNMENT);
    content.add(component, BorderLayout.CENTER);

    content.revalidate();
    content.repaint();
}
    public boolean showThemedConfirm(String icon, String title, String message, String confirmText, String cancelText) {
        playClickSound();
        final boolean[] result = {false};
        final JDialog dlg = new JDialog(this, true);
        dlg.setUndecorated(true);
        dlg.setBackground(new Color(0, 0, 0, 0));

        final boolean hasParent = this != null && this.isShowing();
        if (hasParent) {
            dlg.setBounds(this.getBounds());
        } else {
            dlg.setSize(500, 360);
            dlg.setLocationRelativeTo(null);
        }

        JPanel scrim = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                if (hasParent) {
                    g2.setColor(new Color(5, 12, 24, 205)); // Modern deep translucent scrim
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        scrim.setOpaque(false);

        RoundedPanel card = new RoundedPanel(28, new Color(13, 21, 36), new Color(33, 52, 80));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(30, 36, 26, 36));
        card.setMaximumSize(new Dimension(460, 480));

        // Top Icon Badge with custom glowing vector emblems for Notice, Error, Success, and Info
        if (icon != null && !icon.isEmpty()) {
            final String badgeIcon = icon;
            final String dlgTitle = title != null ? title : "";
            JPanel iconBadge = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                    int w = getWidth(), h = getHeight();

                    String lowerTitle = dlgTitle.toLowerCase();
                    boolean isWarning = badgeIcon.contains("⚠") || lowerTitle.contains("notice") || lowerTitle.contains("warning") || lowerTitle.contains("alert");
                    boolean isError = badgeIcon.contains("❌") || badgeIcon.contains("✖") || lowerTitle.contains("error") || lowerTitle.contains("fail");
                    boolean isSuccess = badgeIcon.contains("✨") || badgeIcon.contains("✅") || lowerTitle.contains("success") || lowerTitle.contains("pass");
                    boolean isLock = badgeIcon.contains("🔒") || lowerTitle.contains("lock");
                    boolean isExit = badgeIcon.contains("🚪") || lowerTitle.contains("log out") || lowerTitle.contains("exit");
                    boolean isRocket = badgeIcon.contains("🚀") || lowerTitle.contains("quiz") || lowerTitle.contains("start");
                    boolean isCloud = badgeIcon.contains("☁") || lowerTitle.contains("server") || lowerTitle.contains("connect");

                    Color bgTop, bgBtm, borderCol, glowCol;
                    if (isWarning) {
                        bgTop = new Color(52, 33, 11);
                        bgBtm = new Color(26, 16, 6);
                        borderCol = new Color(245, 158, 11, 220);
                        glowCol = new Color(245, 158, 11, 55);
                    } else if (isError) {
                        bgTop = new Color(56, 17, 24);
                        bgBtm = new Color(28, 8, 12);
                        borderCol = new Color(239, 68, 68, 220);
                        glowCol = new Color(239, 68, 68, 55);
                    } else if (isSuccess) {
                        bgTop = new Color(14, 46, 28);
                        bgBtm = new Color(7, 24, 14);
                        borderCol = new Color(34, 197, 94, 220);
                        glowCol = new Color(34, 197, 94, 55);
                    } else if (isLock) {
                        bgTop = new Color(50, 36, 12);
                        bgBtm = new Color(24, 18, 6);
                        borderCol = new Color(234, 179, 8, 220);
                        glowCol = new Color(234, 179, 8, 55);
                    } else if (isExit) {
                        bgTop = new Color(54, 20, 24);
                        bgBtm = new Color(26, 10, 14);
                        borderCol = new Color(244, 63, 94, 220);
                        glowCol = new Color(244, 63, 94, 55);
                    } else if (isRocket) {
                        bgTop = new Color(36, 22, 62);
                        bgBtm = new Color(18, 11, 32);
                        borderCol = new Color(168, 85, 247, 220);
                        glowCol = new Color(168, 85, 247, 55);
                    } else if (isCloud) {
                        bgTop = new Color(12, 38, 62);
                        bgBtm = new Color(6, 19, 32);
                        borderCol = new Color(14, 165, 233, 220);
                        glowCol = new Color(14, 165, 233, 55);
                    } else {
                        bgTop = new Color(16, 38, 68);
                        bgBtm = new Color(8, 20, 38);
                        borderCol = new Color(56, 189, 248, 220);
                        glowCol = new Color(56, 189, 248, 55);
                    }

                    // Soft outer glow halo
                    g2.setColor(glowCol);
                    g2.fillRoundRect(2, 2, w - 5, h - 5, 26, 26);

                    // Badge container
                    g2.setPaint(new GradientPaint(0, 4, bgTop, 0, h - 4, bgBtm));
                    g2.fillRoundRect(4, 4, w - 9, h - 9, 22, 22);
                    g2.setColor(borderCol);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(4, 4, w - 9, h - 9, 22, 22);

                    double cx = w / 2.0;
                    double cy = h / 2.0;

                    if (isWarning) {
                        // High-fidelity vector warning triangle with smooth rounded apex and amber gradient
                        Path2D.Double tri = new Path2D.Double();
                        tri.moveTo(cx - 2.5, cy - 11.5);
                        tri.quadTo(cx, cy - 15.5, cx + 2.5, cy - 11.5);
                        tri.lineTo(cx + 15.0, cy + 10.0);
                        tri.quadTo(cx + 17.0, cy + 13.5, cx + 13.0, cy + 13.5);
                        tri.lineTo(cx - 13.0, cy + 13.5);
                        tri.quadTo(cx - 17.0, cy + 13.5, cx - 15.0, cy + 10.0);
                        tri.closePath();

                        // Fill with vibrant golden amber gradient
                        g2.setPaint(new GradientPaint((float)cx, (float)(cy - 16), new Color(254, 240, 138), (float)cx, (float)(cy + 14), new Color(217, 119, 6)));
                        g2.fill(tri);

                        // Triangle outline for crisp contrast
                        g2.setColor(new Color(255, 251, 235, 220));
                        g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        g2.draw(tri);

                        // Bold exclamation mark in dark slate
                        g2.setColor(new Color(15, 23, 42));
                        g2.fillRoundRect((int)Math.round(cx - 1.8), (int)Math.round(cy - 6.5), 4, 9, 2, 2);
                        g2.fillOval((int)Math.round(cx - 1.8), (int)Math.round(cy + 5.5), 4, 4);

                    } else if (isError) {
                        // High-fidelity vector error circle with crimson gradient and white cross
                        double rad = 15.0;
                        g2.setPaint(new GradientPaint((float)cx, (float)(cy - rad), new Color(248, 113, 113), (float)cx, (float)(cy + rad), new Color(220, 38, 38)));
                        g2.fill(new Ellipse2D.Double(cx - rad, cy - rad, rad * 2, rad * 2));
                        g2.setColor(new Color(254, 202, 202));
                        g2.setStroke(new BasicStroke(1.0f));
                        g2.draw(new Ellipse2D.Double(cx - rad, cy - rad, rad * 2, rad * 2));

                        g2.setColor(Color.WHITE);
                        g2.setStroke(new BasicStroke(2.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        float cr = 6.0f;
                        g2.drawLine((int)(cx - cr), (int)(cy - cr), (int)(cx + cr), (int)(cy + cr));
                        g2.drawLine((int)(cx + cr), (int)(cy - cr), (int)(cx - cr), (int)(cy + cr));

                    } else if (isSuccess) {
                        // High-fidelity vector success circle with emerald gradient and white checkmark
                        double rad = 15.0;
                        g2.setPaint(new GradientPaint((float)cx, (float)(cy - rad), new Color(74, 222, 128), (float)cx, (float)(cy + rad), new Color(22, 163, 74)));
                        g2.fill(new Ellipse2D.Double(cx - rad, cy - rad, rad * 2, rad * 2));
                        g2.setColor(new Color(187, 247, 208));
                        g2.setStroke(new BasicStroke(1.0f));
                        g2.draw(new Ellipse2D.Double(cx - rad, cy - rad, rad * 2, rad * 2));

                        g2.setColor(Color.WHITE);
                        g2.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        Path2D.Double check = new Path2D.Double();
                        check.moveTo(cx - 6.5, cy + 0.5);
                        check.lineTo(cx - 1.5, cy + 5.5);
                        check.lineTo(cx + 6.5, cy - 4.5);
                        g2.draw(check);

                    } else if (isLock) {
                        // Vector padlock
                        g2.setColor(new Color(254, 240, 138));
                        g2.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        g2.drawArc((int)(cx - 6.5), (int)(cy - 12.0), 13, 14, 0, 180);

                        g2.setPaint(new GradientPaint((float)cx, (float)(cy - 2), new Color(250, 204, 21), (float)cx, (float)(cy + 12), new Color(202, 138, 4)));
                        g2.fillRoundRect((int)(cx - 10), (int)(cy - 2), 20, 15, 6, 6);
                        g2.setColor(new Color(254, 240, 138));
                        g2.setStroke(new BasicStroke(1.0f));
                        g2.drawRoundRect((int)(cx - 10), (int)(cy - 2), 20, 15, 6, 6);

                        g2.setColor(new Color(15, 23, 42));
                        g2.fillOval((int)(cx - 2), (int)(cy + 2), 4, 4);
                        g2.fillRect((int)(cx - 1.5), (int)(cy + 4), 3, 4);

                    } else if (isExit) {
                        // Vector Doorway & Exit Arrow
                        g2.setColor(new Color(251, 113, 133));
                        g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        g2.drawRect((int)(cx - 10), (int)(cy - 10), 12, 20);

                        g2.setColor(Color.WHITE);
                        g2.drawLine((int)(cx - 2), (int)cy, (int)(cx + 10), (int)cy);
                        g2.drawLine((int)(cx + 6), (int)(cy - 4), (int)(cx + 10), (int)cy);
                        g2.drawLine((int)(cx + 6), (int)(cy + 4), (int)(cx + 10), (int)cy);

                    } else if (isCloud) {
                        // Vector Cloud
                        g2.setPaint(new GradientPaint((float)cx, (float)(cy - 10), new Color(56, 189, 248), (float)cx, (float)(cy + 10), new Color(2, 132, 199)));
                        g2.fillOval((int)(cx - 12), (int)(cy - 4), 14, 14);
                        g2.fillOval((int)(cx - 5), (int)(cy - 10), 16, 16);
                        g2.fillOval((int)(cx + 4), (int)(cy - 4), 12, 12);
                        g2.fillRoundRect((int)(cx - 12), (int)(cy + 1), 26, 9, 6, 6);
                        g2.setColor(new Color(224, 242, 254));
                        g2.setStroke(new BasicStroke(1.0f));
                        g2.drawRoundRect((int)(cx - 12), (int)(cy + 1), 26, 9, 6, 6);

                    } else {
                        // Cyan Info Emblem
                        double rad = 15.0;
                        g2.setPaint(new GradientPaint((float)cx, (float)(cy - rad), new Color(56, 189, 248), (float)cx, (float)(cy + rad), new Color(2, 132, 199)));
                        g2.fill(new Ellipse2D.Double(cx - rad, cy - rad, rad * 2, rad * 2));
                        g2.setColor(new Color(186, 230, 253));
                        g2.setStroke(new BasicStroke(1.0f));
                        g2.draw(new Ellipse2D.Double(cx - rad, cy - rad, rad * 2, rad * 2));

                        g2.setColor(Color.WHITE);
                        g2.fillOval((int)Math.round(cx - 2.0), (int)Math.round(cy - 8.0), 4, 4);
                        g2.fillRoundRect((int)Math.round(cx - 2.0), (int)Math.round(cy - 2.0), 4, 10, 2, 2);
                    }

                    g2.dispose();
                }
            };
            iconBadge.setOpaque(false);
            Dimension bd = new Dimension(68, 68);
            iconBadge.setPreferredSize(bd);
            iconBadge.setMaximumSize(bd);
            iconBadge.setMinimumSize(bd);
            iconBadge.setAlignmentX(Component.CENTER_ALIGNMENT);

            card.add(iconBadge);
            card.add(Box.createVerticalStrut(14));
        }

        // Title
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setForeground(TEXT);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(10));

        // Message
        String safeMsg = (message == null ? "" : message)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\n", "<br/>");
        JLabel msgLabel = new JLabel("<html><div style='text-align: center; width: 350px; color: #94A3B8; font-family: SansSerif; font-size: 13px; line-height: 1.45;'>"
                + safeMsg + "</div></html>", SwingConstants.CENTER);
        msgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(msgLabel);
        card.add(Box.createVerticalStrut(24));

        // Button row
        JPanel btnRow = new JPanel();
        btnRow.setLayout(new BoxLayout(btnRow, BoxLayout.X_AXIS));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRow.setMaximumSize(new Dimension(380, 48));

        btnRow.add(Box.createHorizontalGlue());
        if (cancelText != null && !cancelText.isEmpty()) {
            RoundedButton btnCancel = new RoundedButton(cancelText, false);
            btnCancel.setFont(new Font("SansSerif", Font.BOLD, 13));
            btnCancel.setPreferredSize(new Dimension(140, 44));
            btnCancel.setMaximumSize(new Dimension(140, 44));
            btnCancel.addActionListener(e -> {
                playClickSound();
                result[0] = false;
                dlg.dispose();
            });
            btnRow.add(btnCancel);
            btnRow.add(Box.createHorizontalStrut(14));
        }

        RoundedButton btnConfirm = new RoundedButton(confirmText != null ? confirmText : "OK", true);
        btnConfirm.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnConfirm.setPreferredSize(new Dimension(cancelText != null ? 140 : 170, 44));
        btnConfirm.setMaximumSize(new Dimension(cancelText != null ? 140 : 170, 44));
        btnConfirm.addActionListener(e -> {
            playClickSound();
            result[0] = true;
            dlg.dispose();
        });
        btnRow.add(btnConfirm);
        btnRow.add(Box.createHorizontalGlue());

        card.add(btnRow);
        scrim.add(card);
        dlg.setContentPane(scrim);

        dlg.getRootPane().registerKeyboardAction(e -> {
            result[0] = false;
            dlg.dispose();
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        dlg.getRootPane().registerKeyboardAction(e -> {
            result[0] = true;
            dlg.dispose();
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        ComponentListener resizeListener = null;
        if (hasParent) {
            resizeListener = new ComponentAdapter() {
                @Override public void componentMoved(ComponentEvent e) { dlg.setBounds(getBounds()); }
                @Override public void componentResized(ComponentEvent e) { dlg.setBounds(getBounds()); }
            };
            this.addComponentListener(resizeListener);
        }

        final ComponentListener fResizeListener = resizeListener;
        dlg.addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) {
                if (hasParent && fResizeListener != null) {
                    DigiViewLMS.this.removeComponentListener(fResizeListener);
                }
            }
        });

        if (!hasParent) {
            dlg.pack();
            dlg.setSize(480, dlg.getPreferredSize().height);
            dlg.setLocationRelativeTo(null);
        }

        dlg.setVisible(true);
        return result[0];
    }

    public void showThemedMessage(String icon, String title, String message) {
        showThemedConfirm(icon, title, message, "OK", null);
    }

    private void message(String text, int type) {
        String icon = "ℹ️";
        String title = "Digi-View";
        if (type == JOptionPane.ERROR_MESSAGE) {
            icon = "❌";
            title = "Error";
        } else if (type == JOptionPane.WARNING_MESSAGE) {
            icon = "⚠️";
            title = "Notice";
        } else if (type == JOptionPane.INFORMATION_MESSAGE) {
            icon = "✨";
            title = "Success";
        }
        showThemedMessage(icon, title, text);
    }

    private String difficultyName(int d) {
        switch (d) {
            case 1: return "Easy";
            case 2: return "Medium";
            case 3: return "Hard";
            default: return "Mixed";
        }
    }

    private static class HeroPanel extends JPanel {
        HeroPanel() {
            super(new BorderLayout());
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth() - 1, h = getHeight() - 1;

            Shape shape = new java.awt.geom.RoundRectangle2D.Float(0, 0, w, h, 36, 36);
            g2.setPaint(new GradientPaint(0, 0, new Color(19, 52, 96),
                    w, h, new Color(9, 24, 48)));
            g2.fill(shape);

            g2.setClip(shape);
            g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, 170));
            g2.setColor(new Color(255, 255, 255, 14));
            FontMetrics fm = g2.getFontMetrics();
            String mark = "</>";
            g2.drawString(mark, w - fm.stringWidth(mark) - 30, h + 10);
            g2.setClip(null);

            g2.setColor(new Color(40, 70, 115));
            g2.drawRoundRect(0, 0, w, h, 36, 36);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    
    private static class LogoBadge extends JComponent {
        private static Image cachedLogo;
        private static boolean logoChecked = false;

        LogoBadge(int size) {
            Dimension d = new Dimension(size, size);
            setPreferredSize(d);
            setMinimumSize(d);
            setMaximumSize(d);
            ensureLogoLoaded();
        }

        private static synchronized void ensureLogoLoaded() {
            if (logoChecked && cachedLogo != null) return;
            logoChecked = true;
            try {
                String[] fileCandidates = new String[] {
                    "public/Icons/Splash-icon.png",
                    "public/Icons/Icon.jpg",
                    "Icons/Splash-icon.png",
                    "Icons/Icon.jpg",
                    "@/public/Icons/Splash-icon.png",
                    "@/public/Icons/Icon.jpg",
                    "@/Icons/Splash-icon.png",
                    "../public/Icons/Splash-icon.png",
                    "../public/Icons/Icon.jpg"
                };
                for (String c : fileCandidates) {
                    File f = resolveRoot(c);
                    if (f != null && f.exists() && f.isFile()) {
                        Image img = javax.imageio.ImageIO.read(f);
                        if (img != null) {
                            cachedLogo = img;
                            return;
                        }
                    }
                    InputStream is = resolveRootStream(c);
                    if (is != null) {
                        try {
                            Image img = javax.imageio.ImageIO.read(is);
                            if (img != null) {
                                cachedLogo = img;
                                return;
                            }
                        } finally {
                            try { is.close(); } catch (Exception ignored) {}
                        }
                    }
                }

                // Direct ClassLoader & Resource lookups
                String[] resPaths = new String[] {
                    "/Icons/Splash-icon.png",
                    "/public/Icons/Splash-icon.png",
                    "/Icons/Icon.jpg",
                    "/Splash-icon.png",
                    "Icons/Splash-icon.png",
                    "public/Icons/Splash-icon.png"
                };
                for (String r : resPaths) {
                    InputStream is = DigiView.class.getResourceAsStream(r);
                    if (is == null && DigiView.class.getClassLoader() != null) {
                        is = DigiView.class.getClassLoader().getResourceAsStream(r.startsWith("/") ? r.substring(1) : r);
                    }
                    if (is != null) {
                        try {
                            Image img = javax.imageio.ImageIO.read(is);
                            if (img != null) {
                                cachedLogo = img;
                                return;
                            }
                        } finally {
                            try { is.close(); } catch (Exception ignored) {}
                        }
                    }
                }
            } catch (Exception ignored) {}
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            int w = getWidth(), h = getHeight();

            if (cachedLogo != null) {
                java.awt.geom.RoundRectangle2D round = new java.awt.geom.RoundRectangle2D.Float(0, 0, w, h, 18, 18);
                g2.setClip(round);
                g2.drawImage(cachedLogo, 0, 0, w, h, null);
                g2.setClip(null);
                g2.setColor(new Color(56, 189, 248, 100));
                g2.draw(round);
            } else {
                g2.setPaint(new GradientPaint(0, 0, new Color(37, 99, 235),
                        w, h, new Color(56, 189, 248)));
                g2.fillRoundRect(0, 0, w - 1, h - 1, 18, 18);
                g2.setColor(new Color(255, 255, 255, 60));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 18, 18);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, (int) (w * 0.38)));
                FontMetrics fm = g2.getFontMetrics();
                String s = "DV";
                g2.drawString(s, (w - fm.stringWidth(s)) / 2,
                        (h + fm.getAscent() - fm.getDescent()) / 2);
            }
            g2.dispose();
        }
    }

    
    private static class AvatarBadge extends JComponent {
        private final String letter;

        AvatarBadge(String letter, int size) {
            this.letter = letter.toUpperCase();
            Dimension d = new Dimension(size, size);
            setPreferredSize(d);
            setMinimumSize(d);
            setMaximumSize(d);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g2.setPaint(new GradientPaint(0, 0, new Color(56, 189, 248),
                    w, h, new Color(37, 99, 235)));
            g2.fillOval(0, 0, w - 1, h - 1);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, (int) (w * 0.42)));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(letter, (w - fm.stringWidth(letter)) / 2,
                    (h + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        }
    }

    
    private static class IconBox extends JComponent {
        private final String icon;

        IconBox(String icon) {
            this.icon = icon;
            Dimension d = new Dimension(52, 52);
            setPreferredSize(d);
            setMinimumSize(d);
            setMaximumSize(d);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g2.setColor(new Color(26, 45, 82));
            g2.fillRoundRect(0, 0, w - 1, h - 1, 16, 16);

            g2.setFont(new Font("SansSerif", Font.PLAIN, 22));
            g2.setColor(Color.WHITE);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(icon, (w - fm.stringWidth(icon)) / 2,
                    (h + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        }
    }

    
    private static class ProgressPill extends JComponent {
        private final int value; 

        ProgressPill(int value) {
            this.value = Math.max(0, Math.min(100, value));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();

            g2.setColor(new Color(12, 26, 46));
            g2.fillRoundRect(0, 0, w, h, h, h);

            int fillW = (int) (w * value / 100.0);
            if (fillW > 0) {
                fillW = Math.max(fillW, h);
                g2.setPaint(new GradientPaint(0, 0, new Color(37, 99, 235),
                        w, 0, new Color(34, 211, 238)));
                g2.fillRoundRect(0, 0, fillW, h, h, h);
            }
            g2.dispose();
        }
    }

   
    private static class NavButton extends JButton {
        private boolean active;
        
        private final HoverAnimator hoverAnim = new HoverAnimator(this);
        
        NavButton(String icon, String title) {
            super(icon + "    " + title);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setHorizontalAlignment(SwingConstants.LEFT);
            setFont(new Font("SansSerif", Font.BOLD, 14));
            setForeground(new Color(160, 175, 198));
            setBorder(new EmptyBorder(0, 26, 0, 10));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(200, 54));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
            setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        void setActive(boolean active) {
            this.active = active;
            setForeground(active ? Color.WHITE : new Color(160, 175, 198));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();

            if (active) {
                g2.setPaint(new GradientPaint(0, 0, new Color(20, 56, 100),
                        w, 0, new Color(11, 30, 56)));
                g2.fillRoundRect(0, 0, w, h, 20, 20);
                g2.setColor(new Color(56, 189, 248));
                g2.fillRoundRect(0, 8, 4, h - 16, 4, 4);
            } float t = hoverAnim.get();
            if (!active && t > 0f) {
                g2.setColor(new Color(16, 32, 56, Math.round(255 * t)));
                g2.fillRoundRect(0, 0, w, h, 20, 20);
            }
            
            g2.translate(Math.round(6 * t), 0);
            super.paintComponent(g2);
            g2.dispose();
        }
    }
}