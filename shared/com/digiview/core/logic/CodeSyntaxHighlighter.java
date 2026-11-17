package com.digiview.core.logic;

import com.digiview.core.model.CodeTheme;

import java.util.ArrayList;
import java.util.List;

/**
 * Minimal HTML/CSS-like tokenizer shared by Desktop and Android so lesson code
 * blocks can be highlighted with editor-style themes.
 */
public class CodeSyntaxHighlighter {

    public enum Role {
        COMMENT,
        DOCTYPE,
        TAG_PUNCTUATION,   // < > </ />
        TAG_NAME,
        ATTRIBUTE_NAME,
        ATTRIBUTE_VALUE,
        PLAIN_TEXT
    }

    public static class Token {
        public final int start;
        public final int end;
        public final Role role;

        public Token(int start, int end, Role role) {
            this.start = start;
            this.end = end;
            this.role = role;
        }
    }

    public static List<Token> tokenize(String code) {
        List<Token> tokens = new ArrayList<>();
        if (code == null || code.isEmpty()) return tokens;

        int n = code.length();
        int i = 0;
        boolean insideTag = false;

        while (i < n) {
            char c = code.charAt(i);

            if (!insideTag && i + 3 < n && c == '<' && code.charAt(i + 1) == '!'
                    && code.charAt(i + 2) == '-' && code.charAt(i + 3) == '-') {
                int end = code.indexOf("-->", i + 4);
                int stop = end >= 0 ? Math.min(n, end + 3) : n;
                tokens.add(new Token(i, stop, Role.COMMENT));
                i = stop;
                continue;
            }

            if (!insideTag && i + 8 < n && code.regionMatches(true, i, "<!doctype", 0, 9)) {
                int end = code.indexOf('>', i);
                int stop = end >= 0 ? Math.min(n, end + 1) : n;
                tokens.add(new Token(i, stop, Role.DOCTYPE));
                i = stop;
                continue;
            }

            if (c == '<' && !insideTag) {
                insideTag = true;
                int start = i;
                while (i < n) {
                    char d = code.charAt(i);
                    if (d == '<' || d == '/' || d == '!') i++;
                    else break;
                }
                if (i == start) i++;
                tokens.add(new Token(start, i, Role.TAG_PUNCTUATION));

                int nameStart = i;
                while (i < n) {
                    char d = code.charAt(i);
                    if (Character.isLetterOrDigit(d) || d == ':' || d == '-') i++;
                    else break;
                }
                if (i > nameStart) tokens.add(new Token(nameStart, i, Role.TAG_NAME));
                continue;
            }

            if (c == '>' && insideTag) {
                tokens.add(new Token(i, i + 1, Role.TAG_PUNCTUATION));
                insideTag = false;
                i++;
                continue;
            }

            if (insideTag) {
                if (Character.isWhitespace(c)) {
                    int start = i;
                    while (i < n && Character.isWhitespace(code.charAt(i))) i++;
                    tokens.add(new Token(start, i, Role.PLAIN_TEXT));
                    continue;
                }

                if (c == '"' || c == '\'') {
                    char quote = c;
                    int start = i;
                    i++;
                    while (i < n && code.charAt(i) != quote) i++;
                    if (i < n) i++;
                    tokens.add(new Token(start, i, Role.ATTRIBUTE_VALUE));
                    continue;
                }

                if (c == '=') {
                    tokens.add(new Token(i, i + 1, Role.TAG_PUNCTUATION));
                    i++;
                    continue;
                }

                if (c == '/') {
                    tokens.add(new Token(i, i + 1, Role.TAG_PUNCTUATION));
                    i++;
                    continue;
                }

                int start = i;
                while (i < n) {
                    char d = code.charAt(i);
                    if (Character.isWhitespace(d) || d == '=' || d == '>' || d == '"' || d == '\'' || d == '/') break;
                    i++;
                }
                if (i > start) tokens.add(new Token(start, i, Role.ATTRIBUTE_NAME));
                continue;
            }

            // outside any tag -> plain content
            int start = i;
            while (i < n && code.charAt(i) != '<') i++;
            if (i > start) tokens.add(new Token(start, i, Role.PLAIN_TEXT));
        }

        return tokens;
    }

    public static String colorHex(int rgb) {
        return String.format("#%06X", rgb & 0xFFFFFF);
    }

    public static String toHtml(String code, CodeTheme theme) {
        if (code == null) return "";
        List<Token> tokens = tokenize(code);
        StringBuilder sb = new StringBuilder();
        for (Token t : tokens) {
            String text = code.substring(t.start, t.end)
                    .replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;");
            String color;
            switch (t.role) {
                case COMMENT: color = colorHex(theme.comment); break;
                case DOCTYPE: color = colorHex(theme.doctype); break;
                case TAG_PUNCTUATION: color = colorHex(theme.tag); break;
                case TAG_NAME: color = colorHex(theme.tagName); break;
                case ATTRIBUTE_NAME: color = colorHex(theme.attributeName); break;
                case ATTRIBUTE_VALUE: color = colorHex(theme.attributeValue); break;
                default: color = colorHex(theme.plainText); break;
            }
            sb.append("<font color='").append(color).append("'>")
              .append(text)
              .append("</font>");
        }
        return "<html><body style='margin:0; padding:0; background-color:" + colorHex(theme.background) + ";'>"
                + sb.toString().replace("\n", "<br>")
                + "</body></html>";
    }
}
