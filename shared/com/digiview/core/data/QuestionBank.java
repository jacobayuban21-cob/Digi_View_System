package com.digiview.core.data;

import com.digiview.core.model.Question;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Single Authoritative Question Bank for DigiView LMS (Desktop & Mobile).
 * Eliminates duplicate question banks between desktop and mobile.
 */
public class QuestionBank {

    private static final List<Question> ALL_QUESTIONS = initQuestions();

    private static Question q(String text, String a, String b, String c, String d, int ans, int diff) {
        return new Question(text, Arrays.asList(a, b, c, d), ans, diff);
    }

    private static List<Question> initQuestions() {
        List<Question> list = new ArrayList<>();

        // Difficulty 1: Easy / Beginner (25 questions)
        list.add(q("What does HTML stand for?", "HyperText Markup Language", "HighText Machine Language", "Hyperlink Text Management Language", "Home Tool Markup Language", 0, 1));
        list.add(q("Which file extension is used for a webpage written in HTML?", ".java", ".html", ".docx", ".exe", 1, 1));
        list.add(q("Which program is used to open and view a webpage?", "A calculator", "A spreadsheet", "A web browser", "A compiler", 2, 1));
        list.add(q("Which element contains the visible content of a webpage?", "<head>", "<body>", "<title>", "<meta>", 1, 1));
        list.add(q("Which element sets the title shown in the browser tab?", "<head>", "<h1>", "<title>", "<header>", 2, 1));
        list.add(q("Which element creates the largest heading?", "<h6>", "<heading>", "<h1>", "<head>", 2, 1));
        list.add(q("Which element creates a paragraph?", "<para>", "<p>", "<text>", "<paragraph>", 1, 1));
        list.add(q("Which element creates a line break?", "<break>", "<lb>", "<br>", "<newline>", 2, 1));
        list.add(q("Which element creates a hyperlink?", "<link>", "<a>", "<href>", "<url>", 1, 1));
        list.add(q("Which element displays an image?", "<image>", "<picture>", "<src>", "<img>", 3, 1));
        list.add(q("Which element creates a bulleted (unordered) list?", "<ol>", "<li>", "<ul>", "<list>", 2, 1));
        list.add(q("Which element creates a numbered (ordered) list?", "<ul>", "<ol>", "<li>", "<order>", 1, 1));
        list.add(q("Which element marks one item inside a list?", "<item>", "<ul>", "<li>", "<ol>", 2, 1));
        list.add(q("Which element creates a table?", "<table>", "<tab>", "<grid>", "<data>", 0, 1));
        list.add(q("Which element creates a table row?", "<td>", "<th>", "<tr>", "<row>", 2, 1));
        list.add(q("Which element creates a table header cell?", "<td>", "<th>", "<tr>", "<thead-cell>", 1, 1));
        list.add(q("Which element creates a table data cell?", "<cell>", "<data>", "<td>", "<tr>", 2, 1));
        list.add(q("Which element creates a form?", "<input>", "<form>", "<field>", "<submit>", 1, 1));
        list.add(q("Which element creates a clickable button?", "<click>", "<press>", "<button>", "<btn>", 2, 1));
        list.add(q("Which element shows text with strong importance (usually bold)?", "<strong>", "<small>", "<mark-up>", "<heavy>", 0, 1));
        list.add(q("Which element creates a horizontal line?", "<line>", "<hr>", "<br>", "<divider>", 1, 1));
        list.add(q("Which declaration identifies an HTML5 document?", "<html5>", "<!DOCTYPE html>", "<doctype>", "<HTML5>", 1, 1));
        list.add(q("Which element is the root of an HTML document?", "<body>", "<head>", "<html>", "<main>", 2, 1));
        list.add(q("Which element contains information about the page that is not displayed?", "<footer>", "<main>", "<head>", "<section>", 2, 1));
        list.add(q("Which element is a generic block container?", "<span>", "<div>", "<a>", "<strong>", 1, 1));

        // Difficulty 2: Medium / Intermediate (25 questions)
        list.add(q("Which is the correct way to write a paragraph?", "<p>Hello<p>", "<p>Hello</p>", "</p>Hello<p>", "<p/>Hello", 1, 2));
        list.add(q("Which code creates a link to example.com?", "<a src=\"https://example.com\">Visit</a>", "<link>https://example.com</link>", "<a href=\"https://example.com\">Visit</a>", "<href=\"https://example.com\">Visit</href>", 2, 2));
        list.add(q("Which attribute tells <img> which picture file to show?", "href", "src", "alt", "link", 1, 2));
        list.add(q("Which code displays an image with alternative text?", "<img href=\"cat.jpg\" alt=\"Cat\">", "<image src=\"cat.jpg\">", "<img src=\"cat.jpg\" alt=\"Cat\">", "<img>cat.jpg</img>", 2, 2));
        list.add(q("Which syntax creates an HTML comment?", "// comment", "/* comment */", "<!-- comment -->", "** comment **", 2, 2));
        list.add(q("What is the correct closing tag for <h1>?", "<h1/>", "</h1>", "<\\h1>", "</heading>", 1, 2));
        list.add(q("Which code correctly places a list item inside a bulleted list?", "<ul><li>HTML</li></ul>", "<li><ul>HTML</ul></li>", "<ul>HTML<li></ul>", "<ul><item>HTML</item></ul>", 0, 2));
        list.add(q("Which code creates a numbered list with one item?", "<ul><li>One</li></ul>", "<ol><li>One</li></ol>", "<li><ol>One</ol></li>", "<list>One</list>", 1, 2));
        list.add(q("Which code creates a table row with two header cells?", "<tr><td>Name</td><td>Age</td></tr>", "<table>Name Age</table>", "<tr><th>Name</th><th>Age</th></tr>", "<th><tr>Name</tr></th>", 2, 2));
        list.add(q("Which attribute makes a cell span two columns?", "rowspan=\"2\"", "colspan=\"2\"", "columns=\"2\"", "span=\"2\"", 1, 2));
        list.add(q("Which attribute makes a cell span two rows?", "colspan=\"2\"", "height=\"2\"", "rowspan=\"2\"", "rows=\"2\"", 2, 2));
        list.add(q("Which code creates a single-line text box?", "<input type=\"text\">", "<text></text>", "<input text>", "<textbox>", 0, 2));
        list.add(q("Which code creates a password field?", "<input type=\"text\">", "<input type=\"hidden\">", "<input type=\"password\">", "<password>", 2, 2));
        list.add(q("Which attribute shows hint text inside an empty input?", "value", "label", "placeholder", "hint", 2, 2));
        list.add(q("Which attribute makes a form field mandatory?", "required", "important", "validate", "must", 0, 2));
        list.add(q("Which code correctly connects a label to an input?", "<label name=\"user\">Username</label><input id=\"user\">", "<label for=\"user\">Username</label><input id=\"user\">", "<label id=\"user\">Username</label><input for=\"user\">", "<label href=\"user\">Username</label><input name=\"label\">", 1, 2));
        list.add(q("Which code creates a dropdown list with one option?", "<select><option>Red</option></select>", "<dropdown><item>Red</item></dropdown>", "<option><select>Red</select></option>", "<input type=\"dropdown\">", 0, 2));
        list.add(q("Which attribute opens a link in a new tab?", "new", "window", "target=\"_blank\"", "open=\"new\"", 2, 2));
        list.add(q("Which attribute uniquely identifies one element?", "class", "id", "style", "group", 1, 2));
        list.add(q("Which attribute lets several elements share the same CSS class?", "id", "unique", "class", "single", 2, 2));
        list.add(q("Which code sets the page language to English?", "<html language=\"English\">", "<html lang=\"en\">", "<language value=\"en\">", "<head lang=\"en\">", 1, 2));
        list.add(q("Which code declares UTF-8 character encoding?", "<charset value=\"UTF-8\">", "<meta encoding=\"UTF-8\">", "<meta charset=\"UTF-8\">", "<head charset=\"UTF-8\">", 2, 2));
        list.add(q("Which code nests tags correctly?", "<p><strong>Hi</p></strong>", "<p><strong>Hi</strong></p>", "<strong><p>Hi</strong></p>", "<p><strong>Hi</p>", 1, 2));
        list.add(q("Which code loads an external stylesheet?", "<style src=\"styles.css\">", "<css href=\"styles.css\"></css>", "<link rel=\"stylesheet\" href=\"styles.css\">", "<script href=\"styles.css\"></script>", 2, 2));
        list.add(q("Which code loads an external JavaScript file?", "<javascript src=\"app.js\">", "<script src=\"app.js\"></script>", "<link rel=\"script\" href=\"app.js\">", "<script href=\"app.js\"></script>", 1, 2));

        // Difficulty 3: Hard / Advanced (20 questions)
        list.add(q("Complete the code: <a ____=\"https://example.com\">Visit</a>", "src", "link", "href", "url", 2, 3));
        list.add(q("Complete the code: <img ____=\"photo.jpg\" alt=\"Photo\">", "src", "href", "file", "path", 0, 3));
        list.add(q("Complete the code: <img src=\"photo.jpg\" ____=\"A photo\">", "title", "alt", "name", "text", 1, 3));
        list.add(q("Complete the code: <ul><____>HTML</____></ul>", "item", "ol", "li", "list", 2, 3));
        list.add(q("Complete the code to make a numbered list: <____><li>First</li><li>Second</li></____>", "ul", "ol", "list", "nl", 1, 3));
        list.add(q("Complete the code: <table><____><th>Name</th></____></table>", "td", "row", "th", "tr", 3, 3));
        list.add(q("Complete the code so the cell spans two columns: <td ____=\"2\">Total</td>", "rowspan", "colspan", "span", "columns", 1, 3));
        list.add(q("Complete the code so typed characters are hidden: <input type=\"____\">", "hidden", "secret", "password", "text", 2, 3));
        list.add(q("Complete the code so the field must be filled in: <input type=\"email\" ____>", "needed", "required", "validate", "must", 1, 3));
        list.add(q("Complete the code: <label ____=\"user\">Username</label><input id=\"user\">", "id", "name", "href", "for", 3, 3));
        list.add(q("Complete the code: <select><____>Red</____><option>Blue</option></select>", "item", "choice", "option", "value", 2, 3));
        list.add(q("Complete the code so the link opens in a new tab: <a href=\"https://example.com\" target=\"____\">Visit</a>", "_new", "_blank", "_tab", "_self", 1, 3));
        list.add(q("Complete the code: <html ____=\"en\">", "language", "lang", "code", "locale", 1, 3));
        list.add(q("Complete the code: <meta ____=\"UTF-8\">", "encoding", "type", "charset", "content", 2, 3));
        list.add(q("Complete the code: <link rel=\"stylesheet\" ____=\"styles.css\">", "src", "href", "file", "style", 1, 3));
        list.add(q("Complete the code: <script ____=\"app.js\"></script>", "href", "link", "file", "src", 3, 3));
        list.add(q("Complete the code: <form ____=\"POST\" action=\"save.php\">...</form>", "type", "method", "submit", "send", 1, 3));
        list.add(q("Complete the code: <____>Copyright 2026 Digi-View</____>", "bottom", "footer", "end", "foot", 1, 3));
        list.add(q("Complete the code: <figure><img src=\"chart.png\" alt=\"Chart\"><____>Sales</____></figure>", "caption", "figcaption", "figuretitle", "label", 1, 3));
        list.add(q("Complete the code so the video shows play and pause buttons: <video src=\"movie.mp4\" ____></video>", "buttons", "play", "controls", "show", 2, 3));

        return Collections.unmodifiableList(list);
    }

    public static List<Question> getAllQuestions() {
        return ALL_QUESTIONS;
    }

    public static List<Question> getQuestionsForDifficulty(int difficulty) {
        List<Question> filtered = new ArrayList<>();
        for (Question q : ALL_QUESTIONS) {
            if (q.difficulty == difficulty) {
                filtered.add(q);
            }
        }
        return filtered;
    }
}
