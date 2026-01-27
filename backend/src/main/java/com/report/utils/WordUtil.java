package com.report.utils;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class WordUtil {

    @Data
    @NoArgsConstructor
    public static class SectionNode {
        private String title;
        private int level; // 1 for Heading 1, 2 for Heading 2, etc.
        private List<SectionNode> children = new ArrayList<>();

        public SectionNode(String title, int level) {
            this.title = title;
            this.level = level;
        }
    }

    public static List<SectionNode> parseStructure(MultipartFile file) throws IOException {
        List<SectionNode> roots = new ArrayList<>();
        Stack<SectionNode> stack = new Stack<>();

        try (XWPFDocument doc = new XWPFDocument(file.getInputStream())) {
            for (XWPFParagraph p : doc.getParagraphs()) {
                String text = p.getText().trim();
                if (text.isEmpty()) {
                    continue;
                }

                int level = getHeadingLevel(doc, p);
                if (level > 0) {
                    SectionNode newNode = new SectionNode(text, level);

                    while (!stack.isEmpty() && stack.peek().getLevel() >= level) {
                        stack.pop();
                    }

                    if (stack.isEmpty()) {
                        roots.add(newNode);
                    } else {
                        stack.peek().getChildren().add(newNode);
                    }
                    stack.push(newNode);
                }
            }
        }
        return roots;
    }

    private static int getHeadingLevel(XWPFDocument doc, XWPFParagraph p) {
        String styleID = p.getStyleID();
        if (styleID == null)
            return 0;

        // 1. Check style ID
        int level = matchLevel(styleID);
        if (level > 0)
            return level;

        // 2. Check style Name (if available)
        if (doc.getStyles() != null) {
            org.apache.poi.xwpf.usermodel.XWPFStyle style = doc.getStyles().getStyle(styleID);
            if (style != null && style.getName() != null) {
                level = matchLevel(style.getName());
                if (level > 0)
                    return level;
            }
        }

        return 0;
    }

    private static int matchLevel(String text) {
        if (text == null)
            return 0;
        String lower = text.trim().toLowerCase();

        // Exact digit match (e.g. "1", "2")
        if (lower.matches("^[1-6]$")) {
            return Integer.parseInt(lower);
        }

        // Regex for "Heading 1", "标题 1", "Head 1" etc.
        // Matches typical patterns like "heading 1", "heading1", "标题1", "title 1"
        // Also handling potential extra chars if safe, but restricted to ensure it's a
        // heading.
        // We match: (any start) + (heading|title|标题) + (optional space) + (1-6) + (any
        // end)
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(".*(heading|title|标题)\\s*([1-6]).*");
        java.util.regex.Matcher matcher = pattern.matcher(lower);
        if (matcher.matches()) {
            return Integer.parseInt(matcher.group(2));
        }

        return 0;
    }

    // Maintain old method for backward compatibility if needed, or update
    // controller to use new one.
    // The requirement implies replacing or upgrading the functionality.
    public static List<String> parseHeadings(MultipartFile file) throws IOException {
        List<SectionNode> nodes = parseStructure(file);
        // Flatten for compatibility if any other code uses it?
        // Current controller uses it. I will update controller to use parseStructure.
        // So I can remove this or make it call parseStructure and flatten.
        List<String> validTitles = new ArrayList<>();
        flattenTitles(nodes, validTitles);
        return validTitles;
    }

    private static void flattenTitles(List<SectionNode> nodes, List<String> result) {
        for (SectionNode node : nodes) {
            result.add(node.getTitle());
            flattenTitles(node.getChildren(), result);
        }
    }
}
