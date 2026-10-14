/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package System;
import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.border.*;
import java.awt.*;
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
/**
 *
 * @author Jacob
 */
public class DigiView {
public static void main(String[] args) {
    javax.swing.SwingUtilities.invokeLater(() -> {
        new DigiViewLMS().setVisible(true);
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
    
    
        private Clip clickClip;
        
private void playClickSound() {
    try {
        if (clickClip == null) {
            File soundFile = new File(
                "src/System/sounds/mixkit-select-click-1109.wav"
            );

            if (!soundFile.exists()) {
                System.out.println(
                    "Sound file not found: "
                    + soundFile.getAbsolutePath()
                );
                return;
            }

            AudioInputStream audioStream =
                AudioSystem.getAudioInputStream(soundFile);

            clickClip = AudioSystem.getClip();
            clickClip.open(audioStream);
            audioStream.close();
        }

        if (clickClip.isRunning()) {
            clickClip.stop();
        }

        clickClip.setFramePosition(0);
        clickClip.start();

    } catch (Exception e) {
        System.out.println(
            "Hindi ma-play ang click sound: " + e.getMessage()
        );
    }
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
    return currentUser.history.stream().anyMatch(
        record -> record.quiz.equals(quizName)
            && record.percent >= 70
    );
}

    private JPanel createLogo() {
    JPanel logoPanel = new JPanel(
        new FlowLayout(FlowLayout.CENTER, 12, 0)
    );

    logoPanel.setOpaque(false);
    logoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

    JLabel dv = new JLabel("DV", SwingConstants.CENTER);
    dv.setFont(new Font("SansSerif", Font.BOLD, 22));
    dv.setForeground(Color.WHITE);
    dv.setOpaque(true);
    dv.setBackground(new Color(8, 145, 178));
    dv.setPreferredSize(new Dimension(52, 52));

    JLabel title = new JLabel("Digi-View");
    title.setFont(new Font("SansSerif", Font.BOLD, 25));
    title.setForeground(TEXT);

    logoPanel.add(dv);
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


    private JPanel root;
    private JPanel content;
    private JLabel pageTitle;
    private JLabel userBadge;
    private final CardLayout authCards = new CardLayout();
    private JPanel authPanel;
    private JTextField loginUsername;
    private JPasswordField loginPassword;

    private final List<Lesson> lessons = List.of(
        new Lesson("HTML Basics", "🌐",
            "HTML stands for HyperText Markup Language. It is used to create and structure the content of webpages. A basic HTML document usually contains the &lt;html&gt;, &lt;head&gt;, &lt;title&gt;, and &lt;body&gt; elements.",
            "<!DOCTYPE html>\\n<html>\\n<head>\\n  <title>My Page</title>\\n</head>\\n<body>\\n  <h1>Hello World</h1>\\n  <p>Welcome!</p>\\n</body>\\n</html>"),
        new Lesson("Headings & Paragraphs", "📝",
            "HTML headings are used to create titles and subtitles. Heading elements range from &lt;h1&gt; to &lt;h6&gt;, while the &lt;p&gt; element is used to create paragraphs of text.",
            "<h1>My Website</h1>\\n<h2>About Me</h2>\\n<p>This is a paragraph.</p>"),
        new Lesson("Links", "🔗",
            "HTML links allow users to move from one webpage to another. The &lt;a&gt; element creates a link, while the href attribute specifies the destination of the link.",
            "<a href=\"https://example.com\">Visit Website</a>"),
        new Lesson("Images", "🖼",
            "The &lt;img&gt; element is used to display an image on a webpage. The src attribute specifies the image location, while the alt attribute provides alternative text.",
            "<img src=\"picture.jpg\" alt=\"My picture\">"),
        new Lesson("Lists", "📋",
            "HTML provides two common types of lists. The &lt;ul&gt; element creates an unordered list, while &lt;ol&gt; creates an ordered list. Each item is placed inside an &lt;li&gt; element.",
            "<ul>\\n  <li>HTML</li>\\n  <li>CSS</li>\\n</ul>\\n\\n<ol>\\n  <li>First</li>\\n  <li>Second</li>\\n</ol>"),
        new Lesson("HTML Tables", "📊",
            "HTML tables are used to organize information into rows and columns. The &lt;table&gt; element creates the table, &lt;tr&gt; creates a row, &lt;th&gt; creates a header cell, and &lt;td&gt; creates a data cell.",
            "<table>\\n  <tr><th>Name</th><th>Score</th></tr>\\n  <tr><td>Juan</td><td>90</td></tr>\\n</table>"),
        new Lesson("HTML Forms", "📝",
            "HTML forms are used to collect information from users. Common form elements include &lt;form&gt;, &lt;label&gt;, &lt;input&gt;, and &lt;button&gt;. Forms are useful for login pages, registration forms, and surveys.",
            "<form>\\n  <label>Username</label>\\n  <input type=\"text\" placeholder=\"Enter username\">\\n  <button>Submit</button>\\n</form>"),
        new Lesson("Block-Level Elements", "🧱",
            "Block-level elements normally start on a new line and take up the available width. Common examples include &lt;div&gt;, &lt;p&gt;, and heading elements such as &lt;h1&gt;. They are useful for organizing webpage content into sections.",
            "<div>\\n  <h2>Profile</h2>\\n  <p>This is a block-level section.</p>\\n</div>")
    );
    
   private final List<Question> easyQuestions = List.of(
    q("What does HTML stand for?", "HyperText Markup Language", "HighText Machine Language", "Hyperlink Text Management Language", "Home Tool Markup Language", 0, 1),
        q("Which file extension is used for a webpage written in HTML?", ".java", ".html", ".docx", ".exe", 1, 1),
        q("Which program is used to open and view a webpage?", "A calculator", "A spreadsheet", "A web browser", "A compiler", 2, 1),
        q("Which element contains the visible content of a webpage?", "<head>", "<body>", "<title>", "<meta>", 1, 1),
        q("Which element sets the title shown in the browser tab?", "<head>", "<h1>", "<title>", "<header>", 2, 1),
        q("Which element creates the largest heading?", "<h6>", "<heading>", "<h1>", "<head>", 2, 1),
        q("Which element creates a paragraph?", "<para>", "<p>", "<text>", "<paragraph>", 1, 1),
        q("Which element creates a line break?", "<break>", "<lb>", "<br>", "<newline>", 2, 1),
        q("Which element creates a hyperlink?", "<link>", "<a>", "<href>", "<url>", 1, 1),
        q("Which element displays an image?", "<image>", "<picture>", "<src>", "<img>", 3, 1),
        q("Which element creates a bulleted (unordered) list?", "<ol>", "<li>", "<ul>", "<list>", 2, 1),
        q("Which element creates a numbered (ordered) list?", "<ul>", "<ol>", "<li>", "<order>", 1, 1),
        q("Which element marks one item inside a list?", "<item>", "<ul>", "<li>", "<ol>", 2, 1),
        q("Which element creates a table?", "<table>", "<tab>", "<grid>", "<data>", 0, 1),
        q("Which element creates a table row?", "<td>", "<th>", "<tr>", "<row>", 2, 1),
        q("Which element creates a table header cell?", "<td>", "<th>", "<tr>", "<thead-cell>", 1, 1),
        q("Which element creates a table data cell?", "<cell>", "<data>", "<td>", "<tr>", 2, 1),
        q("Which element creates a form?", "<input>", "<form>", "<field>", "<submit>", 1, 1),
        q("Which element creates a clickable button?", "<click>", "<press>", "<button>", "<btn>", 2, 1),
        q("Which element shows text with strong importance (usually bold)?", "<strong>", "<small>", "<mark-up>", "<heavy>", 0, 1),
        q("Which element creates a horizontal line?", "<line>", "<hr>", "<br>", "<divider>", 1, 1),
        q("Which declaration identifies an HTML5 document?", "<html5>", "<!DOCTYPE html>", "<doctype>", "<HTML5>", 1, 1),
        q("Which element is the root of an HTML document?", "<body>", "<head>", "<html>", "<main>", 2, 1),
        q("Which element contains information about the page that is not displayed?", "<footer>", "<main>", "<head>", "<section>", 2, 1),
        q("Which element is a generic block container?", "<span>", "<div>", "<a>", "<strong>", 1, 1)
    );

    

    private final List<Question> mediumQuestions = List.of(
    q("Which is the correct way to write a paragraph?", "<p>Hello<p>", "<p>Hello</p>", "</p>Hello<p>", "<p/>Hello", 1, 2),
        q("Which code creates a link to example.com?", "<a src=\"https://example.com\">Visit</a>", "<link>https://example.com</link>", "<a href=\"https://example.com\">Visit</a>", "<href=\"https://example.com\">Visit</href>", 2, 2),
        q("Which attribute tells <img> which picture file to show?", "href", "src", "alt", "link", 1, 2),
        q("Which code displays an image with alternative text?", "<img href=\"cat.jpg\" alt=\"Cat\">", "<image src=\"cat.jpg\">", "<img src=\"cat.jpg\" alt=\"Cat\">", "<img>cat.jpg</img>", 2, 2),
        q("Which syntax creates an HTML comment?", "// comment", "/* comment */", "<!-- comment -->", "** comment **", 2, 2),
        q("What is the correct closing tag for <h1>?", "<h1/>", "</h1>", "<\\h1>", "</heading>", 1, 2),
        q("Which code correctly places a list item inside a bulleted list?", "<ul><li>HTML</li></ul>", "<li><ul>HTML</ul></li>", "<ul>HTML<li></ul>", "<ul><item>HTML</item></ul>", 0, 2),
        q("Which code creates a numbered list with one item?", "<ul><li>One</li></ul>", "<ol><li>One</li></ol>", "<li><ol>One</ol></li>", "<list>One</list>", 1, 2),
        q("Which code creates a table row with two header cells?", "<tr><td>Name</td><td>Age</td></tr>", "<table>Name Age</table>", "<tr><th>Name</th><th>Age</th></tr>", "<th><tr>Name</tr></th>", 2, 2),
        q("Which attribute makes a cell span two columns?", "rowspan=\"2\"", "colspan=\"2\"", "columns=\"2\"", "span=\"2\"", 1, 2),
        q("Which attribute makes a cell span two rows?", "colspan=\"2\"", "height=\"2\"", "rowspan=\"2\"", "rows=\"2\"", 2, 2),
        q("Which code creates a single-line text box?", "<input type=\"text\">", "<text></text>", "<input text>", "<textbox>", 0, 2),
        q("Which code creates a password field?", "<input type=\"text\">", "<input type=\"hidden\">", "<input type=\"password\">", "<password>", 2, 2),
        q("Which attribute shows hint text inside an empty input?", "value", "label", "placeholder", "hint", 2, 2),
        q("Which attribute makes a form field mandatory?", "required", "important", "validate", "must", 0, 2),
        q("Which code correctly connects a label to an input?", "<label name=\"user\">Username</label><input id=\"user\">", "<label for=\"user\">Username</label><input id=\"user\">", "<label id=\"user\">Username</label><input for=\"user\">", "<label href=\"user\">Username</label><input name=\"label\">", 1, 2),
        q("Which code creates a dropdown list with one option?", "<select><option>Red</option></select>", "<dropdown><item>Red</item></dropdown>", "<option><select>Red</select></option>", "<input type=\"dropdown\">", 0, 2),
        q("Which attribute opens a link in a new tab?", "new", "window", "target=\"_blank\"", "open=\"new\"", 2, 2),
        q("Which attribute uniquely identifies one element?", "class", "id", "style", "group", 1, 2),
        q("Which attribute lets several elements share the same CSS class?", "id", "unique", "class", "single", 2, 2),
        q("Which code sets the page language to English?", "<html language=\"English\">", "<html lang=\"en\">", "<language value=\"en\">", "<head lang=\"en\">", 1, 2),
        q("Which code declares UTF-8 character encoding?", "<charset value=\"UTF-8\">", "<meta encoding=\"UTF-8\">", "<meta charset=\"UTF-8\">", "<head charset=\"UTF-8\">", 2, 2),
        q("Which code nests tags correctly?", "<p><strong>Hi</p></strong>", "<p><strong>Hi</strong></p>", "<strong><p>Hi</strong></p>", "<p><strong>Hi</p>", 1, 2),
        q("Which code loads an external stylesheet?", "<style src=\"styles.css\">", "<css href=\"styles.css\"></css>", "<link rel=\"stylesheet\" href=\"styles.css\">", "<script href=\"styles.css\"></script>", 2, 2),
        q("Which code loads an external JavaScript file?", "<javascript src=\"app.js\">", "<script src=\"app.js\"></script>", "<link rel=\"script\" href=\"app.js\">", "<script href=\"app.js\"></script>", 1, 2)
    );





    private final List<Question> hardQuestions = List.of(
    q("Complete the code: <a ____=\"https://example.com\">Visit</a>", "src", "link", "href", "url", 2, 3),
        q("Complete the code: <img ____=\"photo.jpg\" alt=\"Photo\">", "src", "href", "file", "path", 0, 3),
        q("Complete the code: <img src=\"photo.jpg\" ____=\"A photo\">", "title", "alt", "name", "text", 1, 3),
        q("Complete the code: <ul><____>HTML</____></ul>", "item", "ol", "li", "list", 2, 3),
        q("Complete the code to make a numbered list: <____><li>First</li><li>Second</li></____>", "ul", "ol", "list", "nl", 1, 3),
        q("Complete the code: <table><____><th>Name</th></____></table>", "td", "row", "th", "tr", 3, 3),
        q("Complete the code so the cell spans two columns: <td ____=\"2\">Total</td>", "rowspan", "colspan", "span", "columns", 1, 3),
        q("Complete the code so typed characters are hidden: <input type=\"____\">", "hidden", "secret", "password", "text", 2, 3),
        q("Complete the code so the field must be filled in: <input type=\"email\" ____>", "needed", "required", "validate", "must", 1, 3),
        q("Complete the code: <label ____=\"user\">Username</label><input id=\"user\">", "id", "name", "href", "for", 3, 3),
        q("Complete the code: <select><____>Red</____><option>Blue</option></select>", "item", "choice", "option", "value", 2, 3),
        q("Complete the code so the link opens in a new tab: <a href=\"https://example.com\" target=\"____\">Visit</a>", "_new", "_blank", "_tab", "_self", 1, 3),
        q("Complete the code: <html ____=\"en\">", "language", "lang", "code", "locale", 1, 3),
        q("Complete the code: <meta ____=\"UTF-8\">", "encoding", "type", "charset", "content", 2, 3),
        q("Complete the code: <link rel=\"stylesheet\" ____=\"styles.css\">", "src", "href", "file", "style", 1, 3),
        q("Complete the code: <script ____=\"app.js\"></script>", "href", "link", "file", "src", 3, 3),
        q("Complete the code: <form ____=\"POST\" action=\"save.php\">...</form>", "type", "method", "submit", "send", 1, 3),
        q("Complete the code: <____>Copyright 2026 Digi-View</____>", "bottom", "footer", "end", "foot", 1, 3),
        q("Complete the code: <figure><img src=\"chart.png\" alt=\"Chart\"><____>Sales</____></figure>", "caption", "figcaption", "figuretitle", "label", 1, 3),
        q("Complete the code so the video shows play and pause buttons: <video src=\"movie.mp4\" ____></video>", "buttons", "play", "controls", "show", 2, 3)
    );
    
    private final List<Question> questionBank =
    java.util.stream.Stream.of(
        easyQuestions,
        mediumQuestions,
        hardQuestions
    )
    .flatMap(List::stream)
    .toList();
    
    
    
    public DigiViewLMS() {
        loadData();

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setMinimumSize(new Dimension(590, 700));

    setLocationRelativeTo(null);
    getContentPane().setBackground(PANEL);


    showAuth();
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
        if (!DATA_FILE.exists()) return;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
            Object data = in.readObject();
            if (data instanceof Map<?, ?> map) {
                for (Object value : map.values()) {
                    if (value instanceof User u) users.put(u.username.toLowerCase(Locale.ROOT), u);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Saved data could not be loaded. A new local data file will be used.",
                "Digi-View", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void saveData() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            out.writeObject(users);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Unable to save local data: " + ex.getMessage(),
                "Save Error", JOptionPane.ERROR_MESSAGE);
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

        JPanel outer = new GradientPanel(new GridBagLayout());

        RoundedPanel card = new RoundedPanel(34, CARD_BG, CARD_BORDER);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(40, 55, 28, 55));
        card.setPreferredSize(new Dimension(480, 660));


        JPanel logo = createLogo();
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        logo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        card.add(logo);
        card.add(Box.createVerticalStrut(45));


        JPanel heading = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        heading.setOpaque(false);
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);
        heading.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel signIn = new JLabel("SIGN IN");
        signIn.setFont(new Font("SansSerif", Font.PLAIN, 28));
        signIn.setForeground(TEXT);

        JLabel fill = new JLabel("/ FILL OUT THE FORM");
        fill.setFont(new Font("SansSerif", Font.PLAIN, 11));
        fill.setForeground(HINT);

        heading.add(signIn);
        heading.add(fill);
        card.add(heading);
        card.add(Box.createVerticalStrut(40));


        loginUsername = new RoundedTextField("Username");
        loginPassword = new RoundedPasswordField("Password");
        loginPassword.addActionListener(e -> login()); 

        card.add(loginUsername);
        card.add(Box.createVerticalStrut(16));
        card.add(loginPassword);
        card.add(Box.createVerticalStrut(10));

        RoundedButton loginBtn = new RoundedButton("SIGN IN", true);
        loginBtn.addActionListener(e -> {
            playClickSound();
            login();
        });
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(22));
        card.add(Box.createVerticalStrut(30));


        RoundedButton create = new RoundedButton("CREATE AN ACCOUNT", false);
        create.addActionListener(e -> {
            playClickSound();
            authCards.show(authPanel, "register");
        });
        card.add(create);

        card.add(Box.createVerticalGlue());


        JLabel version = new JLabel("V1.0.0");
        version.setFont(new Font("SansSerif", Font.PLAIN, 12));
        version.setForeground(HINT);
        version.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(version);

        outer.add(card);
        return outer;
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
        card.setBorder(new EmptyBorder(35, 55, 28, 55));


        JPanel logo = createLogo();
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        logo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        card.add(logo);
        card.add(Box.createVerticalStrut(30));


        JPanel heading = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        heading.setOpaque(false);
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);
        heading.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel title = new JLabel("SIGN UP");
        title.setFont(new Font("SansSerif", Font.PLAIN, 28));
        title.setForeground(TEXT);

        JLabel sub = new JLabel("/ STUDENT PROFILE DETAILS");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 11));
        sub.setForeground(HINT);

        heading.add(title);
        heading.add(sub);
        card.add(heading);
        card.add(Box.createVerticalStrut(28));


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
        card.add(Box.createVerticalStrut(14));
        card.add(confirm);
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

            String un = baseUsername;
            int suffix = 1;
            while (users.containsKey(un.toLowerCase(Locale.ROOT))) {
                un = baseUsername + suffix;
                suffix++;
            }

            currentUser = new User(name, 0, g, "N/A", gr + " - " + sec, un, pw);
            users.put(un.toLowerCase(Locale.ROOT), currentUser);
            saveData();
            showApp();

            message("Account created successfully!\n\n"
                    + "Your username is: " + un
                    + "\nPlease remember this username for login.",
                    JOptionPane.INFORMATION_MESSAGE);
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

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        outer.add(card, gbc);


        JScrollPane scrollPane = new JScrollPane(outer);
        scrollPane.setBorder(null);
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
        User u = users.get(username.toLowerCase(Locale.ROOT));
        if (u == null || !u.password.equals(password)) {
            message("Invalid username or password.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        currentUser = u;
        showApp();
    }

    private void showApp() {
         if (currentUser == null) {
            showAuth();
            return;
        }

        getContentPane().removeAll();

        root = new GradientPanel(new BorderLayout());

        root.add(buildSidebar(), BorderLayout.WEST);

        JPanel main = new GradientPanel(new BorderLayout());

        main.add(buildTopBar(), BorderLayout.NORTH);

        content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(28, 30, 28, 30));

        JScrollPane scroll = new JScrollPane(content);

        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getViewport().setBackground(BG);

        scroll.getVerticalScrollBar().setUnitIncrement(24);
        scroll.getHorizontalScrollBar().setUnitIncrement(24);

        scroll.getViewport().setScrollMode(JViewport.BLIT_SCROLL_MODE);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        main.add(scroll, BorderLayout.CENTER);

        root.add(main, BorderLayout.CENTER);

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
        sidebar.setPreferredSize(new Dimension(260, 0));


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
        sidebar.add(Box.createVerticalStrut(14));


        RoundedButton logout = new RoundedButton("\uD83D\uDEAA   Logout", false);
        logout.setForeground(new Color(200, 212, 230));
        logout.setPreferredSize(new Dimension(200, 48));
        logout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        logout.setAlignmentX(Component.LEFT_ALIGNMENT);
        logout.addActionListener(e -> {
            playClickSound();
            int answer = JOptionPane.showConfirmDialog(
                this, "Are you sure you want to log out?", "Logout",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE
            );
            if (answer == JOptionPane.YES_OPTION) {
                currentUser = null;
                showAuth();
            }
        });
        sidebar.add(logout);

        return sidebar;
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


        HeroPanel hero = new HeroPanel();

        JPanel heroContent = new JPanel();
        heroContent.setOpaque(false);
        heroContent.setLayout(new BoxLayout(heroContent, BoxLayout.Y_AXIS));
        heroContent.setBorder(new EmptyBorder(38, 46, 38, 46));

        JLabel welcome = new JLabel(
            "<html>Welcome, <font color='#22d3ee'>" + currentUser.username.toUpperCase()
            + "</font>!&nbsp; \uD83D\uDC4B</html>");
        welcome.setFont(new Font("SansSerif", Font.BOLD, 36));
        welcome.setForeground(TEXT);
        welcome.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel desc = new JLabel(
            "<html><div style='width:640px'>Keep learning, complete your HTML lessons, "
            + "and challenge yourself with the quiz. Your goal is not just to memorize "
            + "HTML\u2014it's to understand how it works.</div></html>");
        desc.setFont(new Font("SansSerif", Font.PLAIN, 15));
        desc.setForeground(new Color(190, 205, 225));
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
        p.add(fixHeight(hero, 290));
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

        JPanel stats = new JPanel(new GridLayout(1, 4, 18, 0));
        stats.setOpaque(false);

        for (String[] item : values) {
            RoundedPanel card = new RoundedPanel(26, D_CARD, D_LINE);
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBorder(new EmptyBorder(22, 26, 22, 26));

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
            card.add(Box.createVerticalStrut(16));
            card.add(title);
            card.add(Box.createVerticalStrut(4));
            card.add(value);
            stats.add(card);
        }
        p.add(fixHeight(stats, 180));
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

        JPanel cards = new JPanel(new GridLayout(0, 3, 16, 16));
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

        int rows = (lessons.size() + 2) / 3;
        p.add(fixHeight(cards, rows * 84 + (rows - 1) * 16));

        addToContent(p);
    }

    private void showPage(String page) {
        if (currentUser == null) {
            showAuth();
            return;
        }
        if (pageTitle != null) pageTitle.setText(page);
        refreshHeaderStats();
        navButtons.forEach((key, btn) -> btn.setActive(key.equals(page)));

        switch (page) {
            case "Dashboard" -> renderDashboard();
            case "Lessons" -> renderLessons();
            case "Quiz" -> renderQuizSelection();
            case "Achievements" -> renderAchievements();
            case "History" -> renderHistory();
            case "Profile" -> renderProfile();
            default -> renderDashboard();
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
            new EmptyBorder(14, 30, 14, 30)
        ));

        pageTitle = new JLabel("Dashboard");
        pageTitle.setFont(new Font("SansSerif", Font.PLAIN, 15));
        pageTitle.setForeground(new Color(190, 205, 225));

        topXp = new JLabel();
        topXp.setFont(new Font("SansSerif", Font.BOLD, 14));
        topXp.setForeground(TEXT);

        

        AvatarBadge avatar = new AvatarBadge(currentUser.username.substring(0, 1), 44);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 22, 0));
        right.setOpaque(false);
        right.add(topXp);
        right.add(avatar);

        top.add(pageTitle, BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);
        return top;
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

        
        JPanel grid = new JPanel(new GridLayout(0, 3, 18, 18));
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

            JLabel title = new JLabel(l.title);
            title.setFont(new Font("SansSerif", Font.BOLD, 22));
            title.setForeground(TEXT);
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

        int rows = (lessons.size() + 2) / 3;
        p.add(fixHeight(grid, rows * 340 + (rows - 1) * 18));

        addToContent(p);
    }

    private void showLesson(Lesson l) {
        JPanel p = verticalPanel();
        p.setOpaque(false);

       
        RoundedButton back = new RoundedButton("\u2190  Back to Lessons", false);
        back.setForeground(Color.WHITE);
        back.setPreferredSize(new Dimension(190, 42));
        back.setMaximumSize(new Dimension(190, 42));
        back.setAlignmentX(Component.LEFT_ALIGNMENT);
        back.addActionListener(e -> {
            playClickSound();
            showPage("Lessons");
        });
        p.add(back);
        p.add(Box.createVerticalStrut(18));

        
        RoundedPanel card = new RoundedPanel(26, D_CARD, D_LINE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(28, 32, 28, 32));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel(l.icon + "  " + l.title);
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Lesson explanation and example");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 14));
        sub.setForeground(MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel explanation = new JLabel(
            "<html><div style='width:700px'>" + l.explanation + "</div></html>");
        explanation.setFont(new Font("SansSerif", Font.PLAIN, 16));
        explanation.setForeground(new Color(215, 225, 240));
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
        codeBox.setBorder(new EmptyBorder(10, 10, 10, 10));
        codeBox.add(code, BorderLayout.CENTER);

        int lines = l.code.split("\n").length;
        int boxHeight = lines * 20 + 44;
        Dimension boxSize = new Dimension(720, boxHeight);
        codeBox.setPreferredSize(boxSize);
        codeBox.setMaximumSize(boxSize);
        codeBox.setMinimumSize(new Dimension(200, boxHeight));
        codeBox.setAlignmentX(Component.LEFT_ALIGNMENT);

       
        RoundedButton quiz = new RoundedButton("Take a Quiz  \u2192", true);
        quiz.setPreferredSize(new Dimension(170, 46));
        quiz.setMaximumSize(new Dimension(170, 46));
        quiz.setAlignmentX(Component.LEFT_ALIGNMENT);
        quiz.addActionListener(e -> {
            playClickSound();
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
            "<html>Ready for the <font color='#22d3ee'>HTML Challenge?");
        heroTitle.setFont(new Font("SansSerif", Font.BOLD, 36));
        heroTitle.setForeground(TEXT);
        heroTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel heroDesc = new JLabel(
            "<html><div style='width:700px'>Choose a difficulty and test your knowledge. "
            + "Be careful\u2014wrong answers cost one heart.</div></html>");
        heroDesc.setFont(new Font("SansSerif", Font.PLAIN, 16));
        heroDesc.setForeground(new Color(190, 205, 225));
        heroDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        heroContent.add(heroTitle);
        heroContent.add(Box.createVerticalStrut(14));
        heroContent.add(heroDesc);

        hero.add(heroContent, BorderLayout.CENTER);
        p.add(fixHeight(hero, 200));
        p.add(Box.createVerticalStrut(24));


        String[] names = {"Easy", "Medium", "Hard"};
        String[] icons = {"\uD83C\uDF31", "\u26A1", "\uD83D\uDD25"};
        String[] tags = {"HTML Basics", "Syntax & Code", "Complete the Code"};
        
        int[] counts = {25, 25, 20};

        boolean easyPassed = hasPassed("Easy");

        JPanel grid = new JPanel(new GridLayout(1, 3, 18, 0));
        grid.setOpaque(false);

        for (int i = 0; i < names.length; i++) {
            final int level = i + 1;

            boolean unlocked = level == 1
                || (level == 2 && easyPassed)
                || (level == 3 && easyPassed);

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
                JLabel lockedText = new JLabel("Pass previous level", SwingConstants.CENTER);
                lockedText.setFont(new Font("SansSerif", Font.BOLD, 14));
                lockedText.setForeground(new Color(150, 165, 190));
                locked.add(lockedText, BorderLayout.CENTER);
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

        p.add(fixHeight(grid, 270));
        p.add(Box.createVerticalStrut(24));


        RoundedPanel passing = new RoundedPanel(26, D_CARD, D_LINE);
        passing.setLayout(new BoxLayout(passing, BoxLayout.Y_AXIS));
        passing.setBorder(new EmptyBorder(22, 28, 22, 28));

        JLabel passTitle = new JLabel("\uD83C\uDFAF  Passing Score");
        passTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
        passTitle.setForeground(TEXT);
        passTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel passDesc = new JLabel(
            "<html>You need at least <b>70%</b> to pass a level and unlock the next difficulty.</html>");
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

    if (difficulty == 2 && !hasPassed("Easy")) {
        message(
            "Pass the Easy Quiz first to unlock Medium.",
            JOptionPane.WARNING_MESSAGE
        );
        return;
    }

    

    int choice = JOptionPane.showConfirmDialog(
        this,
        "Are you ready to take this quiz?",
        "Start Quiz",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE
    );

    if (choice != JOptionPane.YES_OPTION) {
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

    int limit = switch (difficulty) {
        case 1 -> 25;   
        case 2 -> 25; 
        case 3 -> 20;   
        default -> 10;
    };

    if (pool.size() > limit) {
        pool = new ArrayList<>(pool.subList(0, limit));
    }

    quizHearts = 5;

    runQuiz(pool, 0, 0, difficulty);
}
    
    private void runQuiz(List<Question> questions, int index, int score, int difficulty) {
        
        if (index >= questions.size()) {
            int percent = (int)Math.round(score * 100.0 / questions.size());
            currentUser.xp += score * 5;
            currentUser.history.add(new QuizRecord(difficultyName(difficulty), score, questions.size(), percent, new Date().toString()));
            if (percent >= 70) currentUser.achievements.add("HTML Quiz Pass");
            if (percent == 100) currentUser.achievements.add("Perfect Score");
            saveData();
            showQuizResult(score, questions.size(), percent, difficulty);
            return;
        }
        Question q = questions.get(index);
        JPanel p = verticalPanel();
        p.add(sectionHeading(difficultyName(difficulty) + " Quiz","Question " + (index + 1) + " of " + questions.size()));
        JPanel card = panel(); card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS)); card.setBorder(new EmptyBorder(25, 25, 25, 25));
        JLabel heartsLabel = createHeartsLabel();
        heartsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(heartsLabel);
        card.add(Box.createVerticalStrut(15));
        JLabel number = label(String.format("QUESTION %02d", index + 1), 12, CYAN, true);
        JTextArea question = new JTextArea(q.text);
        question.setAlignmentX(Component.LEFT_ALIGNMENT);
        question.setLineWrap(true); 
        question.setWrapStyleWord(true); 
        question.setEditable(false);
        question.setOpaque(false); 
        question.setForeground(TEXT); 
        question.setFont(new Font("SansSerif", Font.BOLD, 21)); 
        question.setBorder(new EmptyBorder(15, 0, 18, 0));
                card.add(number); card.add(question);
                JButton exitQuiz = button("Exit Quiz", new Color(153, 27, 27));

        exitQuiz.setAlignmentX(Component.LEFT_ALIGNMENT);

        exitQuiz.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to exit this quiz?\n"
                    + "Your current attempt will not be saved.",
                "Exit Quiz",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

            if (choice == JOptionPane.YES_OPTION) {
                showPage("Quiz");
            }
        });

        card.add(exitQuiz);
        card.add(Box.createVerticalStrut(15));
        List<Integer> order = new ArrayList<>(List.of(0,1,2,3)); Collections.shuffle(order);
        ButtonGroup group = new ButtonGroup(); List<JRadioButton> radios = new ArrayList<>();
        for (int oi : order) {
        JRadioButton rb = new JRadioButton(q.options.get(oi));
        rb.setActionCommand(String.valueOf(oi));
        rb.setOpaque(true);
        rb.setBackground(PANEL2);
        rb.setForeground(TEXT);
        rb.setFont(new Font("SansSerif", Font.PLAIN, 15));
        rb.setBorder(new EmptyBorder(12, 12, 12, 12));
        rb.setAlignmentX(Component.LEFT_ALIGNMENT);
        rb.setHorizontalAlignment(SwingConstants.LEFT);
        rb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        group.add(rb);
        radios.add(rb);
        card.add(rb);
        card.add(Box.createVerticalStrut(8));
}
        JLabel feedback = label("Choose the best answer.", 13, MUTED, false);
        JButton submit = button("Submit Answer", BLUE);
        submit.setAlignmentX(Component.LEFT_ALIGNMENT);

            card.add(feedback);
            card.add(Box.createVerticalStrut(10));
            card.add(submit);
       submit.addActionListener(e -> {
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


    submit.setEnabled(false);


    if (!correct) {
        quizHearts--;
    }


    updateHeartsLabel(heartsLabel);


    if (quizHearts <= 0) {
        message(
            "You ran out of hearts! Please try the quiz again.",
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

    JButton next = button(
        index == questions.size() - 1
            ? "View Results →"
            : "Next Question →",
        new Color(8, 145, 178)
    );

    next.addActionListener(ev -> {
        runQuiz(
            questions,
            index + 1,
            score + (correct ? 1 : 0),
            difficulty
        );
    });

    card.add(Box.createVerticalStrut(10));
    card.add(feedback);
    card.add(Box.createVerticalStrut(10));
    card.add(next);

    card.revalidate();
    card.repaint();
    });
       
     p.add(card);
    addToContent(p);
}
    
    private void showQuizResult
        ( int score, int total, int percent, int difficulty ) {
        
        
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
        "Easy passed! Medium and Hard Quizzes are now unlocked.";
} else if (difficulty == 2) {
    resultMessage =
        "Medium passed! Great job!";
} else {
    resultMessage =
        "Hard passed! Congratulations on completing all levels.";
}

card.add(label(resultMessage, 14, MUTED, false));
        card.add(Box.createVerticalStrut(20));
        JButton again = button("Try Another Quiz", BLUE); again.addActionListener(e -> showPage("Quiz"));
        JButton dashboard = button("Back to Dashboard", PANEL2); dashboard.addActionListener(e -> showPage("Dashboard"));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT)); actions.setOpaque(false); actions.add(again); actions.add(dashboard); card.add(actions);
        p.add(card); addToContent(p);
    }

    private void renderAchievements() {
        JPanel p = verticalPanel();
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 10, 0));

        int taken = currentUser.history.size();
        int xp = currentUser.xp;
        boolean passedAny = currentUser.history.stream().anyMatch(r -> r.percent >= 70);
        boolean perfect = currentUser.history.stream().anyMatch(r -> r.percent == 100)
                || currentUser.achievements.contains("Perfect Score");


        Object[][] items = {
            {"\uD83C\uDFAF", "First Quiz", "Complete at least one quiz.", taken, 1},
            {"\u2B50", "HTML Quiz Pass", "Earn 70% or higher on a quiz.", passedAny ? 1 : 0, 1},
            {"\uD83C\uDFC6", "Perfect Score", "Get every question correct in a quiz.", perfect ? 1 : 0, 1},
            {"\uD83C\uDF31", "Easy Master", "Pass the Easy quiz.", hasPassed("Easy") ? 1 : 0, 1},
            {"\u26A1", "Medium Master", "Pass the Medium quiz.", hasPassed("Medium") ? 1 : 0, 1},
            {"\uD83D\uDD25", "Hard Master", "Pass the Hard quiz.", hasPassed("Hard") ? 1 : 0, 1},
            {"\uD83D\uDCC8", "Level Up", "Reach Level 2 (100 XP).", xp, 100},
            {"\uD83D\uDCDA", "Persistent Learner", "Complete 5 quizzes.", taken, 5},
            {"\uD83D\uDC8E", "XP Collector", "Earn 500 XP.", xp, 500}
        };

        int unlockedCount = 0;
        for (Object[] it : items) {
            if ((int) it[3] >= (int) it[4]) unlockedCount++;
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

        JLabel heroDesc = new JLabel(unlockedCount + " of " + items.length
                + " unlocked \u2014 achievements are based on your quiz results.");
        heroDesc.setFont(new Font("SansSerif", Font.PLAIN, 15));
        heroDesc.setForeground(new Color(190, 205, 225));
        heroDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        ProgressPill overall = new ProgressPill(unlockedCount * 100 / items.length);
        overall.setAlignmentX(Component.LEFT_ALIGNMENT);
        overall.setPreferredSize(new Dimension(500, 14));
        overall.setMaximumSize(new Dimension(500, 14));

        heroContent.add(heroTitle);
        heroContent.add(Box.createVerticalStrut(10));
        heroContent.add(heroDesc);
        heroContent.add(Box.createVerticalStrut(18));
        heroContent.add(overall);

        hero.add(heroContent, BorderLayout.CENTER);
        p.add(fixHeight(hero, 180));
        p.add(Box.createVerticalStrut(24));


        JPanel grid = new JPanel(new GridLayout(0, 3, 18, 18));
        grid.setOpaque(false);

        for (Object[] it : items) {
            String icon = (String) it[0];
            String name = (String) it[1];
            String desc = (String) it[2];
            int current = (int) it[3];
            int target = (int) it[4];
            boolean unlocked = current >= target;

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


            JLabel title = new JLabel(name);
            title.setFont(new Font("SansSerif", Font.BOLD, 20));
            title.setForeground(unlocked ? TEXT : MUTED);
            title.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel description = new JLabel(desc);
            description.setFont(new Font("SansSerif", Font.PLAIN, 14));
            description.setForeground(unlocked ? new Color(170, 185, 205) : new Color(110, 125, 150));
            description.setAlignmentX(Component.LEFT_ALIGNMENT);

      
            int shown = Math.min(current, target);
            int percent = Math.min(100, current * 100 / target);

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

        int rows = (items.length + 2) / 3;
        p.add(fixHeight(grid, rows * 230 + (rows - 1) * 18));

        addToContent(p);
    }
    
    private void renderHistory() {
        JPanel p = verticalPanel();
        p.add(sectionHeading("Quiz History", "Your quiz results are saved locally on this computer."));
        if (currentUser.history.isEmpty()) {
            p.add(emptyPanel("No quizzes completed yet. Start a quiz to see your results here."));
        } else {
            String[] cols = {"Date", "Quiz", "Score", "Percentage", "Result"};
            Object[][] rows = new Object[currentUser.history.size()][5];
            List<QuizRecord> records = new ArrayList<>(currentUser.history); Collections.reverse(records);
            for (int i=0; i<records.size(); i++) {
                QuizRecord r = records.get(i);
                rows[i] = new Object[]{r.date, r.quiz, r.score + "/" + r.total, r.percent + "%", r.percent >= 70 ? "Passed" : "Needs Review"};
            }
            JTable table = new JTable(rows, cols); table.setBackground(PANEL); table.setForeground(TEXT); table.setGridColor(new Color(43, 65, 91));
            table.setRowHeight(32); table.getTableHeader().setBackground(PANEL2); table.getTableHeader().setForeground(CYAN);
            JScrollPane sp = new JScrollPane(table); sp.getViewport().setBackground(PANEL); sp.setBorder(new LineBorder(new Color(43,65,91)));
            p.add(sp);
        }
        addToContent(p);
    }

    private void renderProfile() {
        JPanel p = verticalPanel();
        p.setOpaque(false);
        p.add(sectionHeading("My Profile", "Your registration information."));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        RoundedPanel card = new RoundedPanel(26, new Color(0, 0, 0, 0), D_LINE);
        card.setLayout(new GridLayout(0, 2, 10, 14));
        card.setBorder(new EmptyBorder(25, 25, 25, 25));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        addProfileRow(card, "Full Name", currentUser.fullName); 
        addProfileRow(card, "Username", currentUser.username);
        addProfileRow(card, "Age", String.valueOf(currentUser.age)); 
        addProfileRow(card, "Gender", currentUser.gender);
        addProfileRow(card, "Strand", currentUser.strand); 
        addProfileRow(card, "Grade & Section", currentUser.gradeSection);
        addProfileRow(card, "Level", String.valueOf(currentUser.xp / 100 + 1));         addProfileRow(card, "Experience Points", String.valueOf(currentUser.xp));
        p.add(fixHeight(card, 520));
        addToContent(p);
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
    private void message(String text, int type) { JOptionPane.showMessageDialog(this, text, "Digi-View LMS", type); }

    private String difficultyName(int d) 
    { return switch (d) { case 1 -> "Easy"; case 2 -> "Medium"; case 3 -> "Hard"; default -> "Mixed"; }; }

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
        LogoBadge(int size) {
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
            g2.setPaint(new GradientPaint(0, 0, new Color(37, 99, 235),
                    w, h, new Color(34, 190, 240)));
            g2.fillRoundRect(0, 0, w, h, 20, 20);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, (int) (w * 0.34)));
            FontMetrics fm = g2.getFontMetrics();
            String s = "DV";
            g2.drawString(s, (w - fm.stringWidth(s)) / 2,
                    (h + fm.getAscent() - fm.getDescent()) / 2);
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
    
    
    private static class Lesson {
        String title, icon, explanation, code;
        Lesson(String title, String icon, String explanation, String code) {
            this.title=title; this.icon=icon; this.explanation=explanation; this.code=code.replace("\\n", "\n");
        }
    }
    private static class Question {
        String text; List<String> options; int answer, difficulty;
        Question(String text, List<String> options, int answer, int difficulty) {
            this.text=text; this.options=options; this.answer=answer; this.difficulty=difficulty;
        }
    }
    private static class QuizRecord implements Serializable {
        private static final long serialVersionUID = 1L;
        String quiz, date; int score, total, percent;
        QuizRecord(String quiz, int score, int total, int percent, String date) {
            this.quiz=quiz; this.score=score; this.total=total; this.percent=percent; this.date=date;
        }
    }
    private static class User implements Serializable {
        private static final long serialVersionUID = 1L;
        String fullName, gender, strand, gradeSection, username, password;
        int age, xp;
        List<QuizRecord> history = new ArrayList<>();
        Set<String> achievements = new LinkedHashSet<>();
        User(String fullName, int age, String gender, String strand, String gradeSection, String username, String password) {
            this.fullName=fullName; this.age=age; this.gender=gender; this.strand=strand; this.gradeSection=gradeSection;
            this.username=username; this.password=password;
        }
    }
}