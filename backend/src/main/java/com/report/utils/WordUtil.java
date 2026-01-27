package com.report.utils;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class WordUtil {

    public static List<String> parseHeadings(MultipartFile file) throws IOException {
        List<String> headings = new ArrayList<>();
        try (XWPFDocument doc = new XWPFDocument(file.getInputStream())) {
            for (XWPFParagraph p : doc.getParagraphs()) {
                String text = p.getText().trim();
                if (text.isEmpty()) {
                    continue;
                }

                // Check by Style ID (Heading 1 usually has style ID "1" or "Heading1")
                String styleID = p.getStyleID();
                String styleName = "";

                // Try to get style name if possible
                if (styleID != null && doc.getStyles() != null && doc.getStyles().getStyle(styleID) != null) {
                    styleName = doc.getStyles().getStyle(styleID).getName();
                }

                System.out.println(
                        "DEBUG PARSE: Text=[" + text + "], StyleID=[" + styleID + "], StyleName=[" + styleName + "]");

                if (styleID != null && (styleID.equals("1") ||
                        styleID.toLowerCase().contains("heading1") ||
                        styleID.toLowerCase().contains("heading 1") ||
                        (styleName != null && styleName.toLowerCase().contains("heading 1")) ||
                        (styleName != null && styleName.contains("标题 1")))) {
                    headings.add(text);
                }
                // Fallback: Check if it looks very much like a main title if no style is used?
                // User explicitly said "not 1. or 一、", implies they want Structure.
                // But often users format manually.
                // Let's stick to Style ID "1" (Heading 1) as requested "outermost title".
                // We can also accept style ID "2" (Heading 2) if they want that, but
                // "outermost" implies H1.

                // For robustness, let's also check if it *looks* like a standard H1 if style is
                // missing?
                // No, user specifically rejected the regex approach.
            }
        }
        return headings;
    }
}
