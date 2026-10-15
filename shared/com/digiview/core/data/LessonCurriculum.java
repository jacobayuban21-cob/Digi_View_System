package com.digiview.core.data;

import com.digiview.core.model.Lesson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Single Authoritative Lesson Curriculum for DigiView LMS (Desktop & Mobile).
 */
public class LessonCurriculum {

    private static final List<Lesson> LESSONS = initLessons();

    private static List<Lesson> initLessons() {
        List<Lesson> list = new ArrayList<>();

        list.add(new Lesson(
            "HTML Basics",
            "🌐",
            "HTML stands for HyperText Markup Language. It is used to create and structure the content of webpages. A basic HTML document contains the <html>, <head>, <title>, and <body> elements.",
            "<!DOCTYPE html>\n<html>\n<head>\n  <title>My Page</title>\n</head>\n<body>\n  <h1>Hello World</h1>\n  <p>Welcome!</p>\n</body>\n</html>"
        ));

        list.add(new Lesson(
            "Headings & Paragraphs",
            "📝",
            "HTML headings are used to create titles and subtitles. Heading elements range from <h1> (largest) to <h6>, while the <p> element is used to create paragraphs of text.",
            "<h1>My Website</h1>\n<h2>About Me</h2>\n<p>This is a standard text paragraph.</p>"
        ));

        list.add(new Lesson(
            "Links & Anchors",
            "🔗",
            "HTML links allow users to navigate between webpages. The <a> tag creates hyperlinks, and the 'href' attribute specifies the destination URL.",
            "<a href=\"https://example.com\" target=\"_blank\">Visit Website</a>"
        ));

        list.add(new Lesson(
            "Images & Media",
            "🖼",
            "The <img> element is used to display graphics and photos. The 'src' attribute defines the file path, while 'alt' provides accessibility descriptions.",
            "<img src=\"photo.jpg\" alt=\"A scenic view\" width=\"400\">"
        ));

        list.add(new Lesson(
            "Unordered & Ordered Lists",
            "📋",
            "HTML provides bulleted unordered lists (<ul>) and numbered ordered lists (<ol>). Each individual item is placed inside an <li> element.",
            "<ul>\n  <li>HTML5</li>\n  <li>CSS3</li>\n</ul>\n\n<ol>\n  <li>Step 1</li>\n  <li>Step 2</li>\n</ol>"
        ));

        list.add(new Lesson(
            "HTML Tables",
            "📊",
            "HTML tables organize data into rows and columns. The <table> element defines the container, <tr> creates rows, <th> creates header cells, and <td> creates data cells.",
            "<table>\n  <tr><th>Student</th><th>Grade</th></tr>\n  <tr><td>Juan</td><td>95</td></tr>\n</table>"
        ));

        list.add(new Lesson(
            "HTML Forms",
            "📝",
            "HTML forms collect user input. Common form elements include <form>, <label>, <input>, and <button>. Essential for logins, questionnaires, and account registration.",
            "<form action=\"/submit\">\n  <label>Username:</label>\n  <input type=\"text\" placeholder=\"Enter username\">\n  <button type=\"submit\">Submit</button>\n</form>"
        ));

        list.add(new Lesson(
            "Block-Level Elements",
            "🧱",
            "Block-level elements normally start on a new line and expand across the available width. Common examples include <div>, <p>, and heading elements like <h1>.",
            "<div>\n  <h2>Profile</h2>\n  <p>This is a block-level section.</p>\n</div>"
        ));

        return Collections.unmodifiableList(list);
    }

    public static List<Lesson> getLessons() {
        return LESSONS;
    }
}
