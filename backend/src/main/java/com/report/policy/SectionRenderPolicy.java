package com.report.policy;

import com.deepoove.poi.policy.AbstractRenderPolicy;
import com.deepoove.poi.render.RenderContext;
import com.deepoove.poi.xwpf.NiceXWPFDocument;
import com.report.dto.SectionData;
import com.report.entity.TemplateStyle;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlCursor;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSpacing;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STLineSpacingRule;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自定义渲染策略，用于动态生成树形章节结构
 * 在占位符位置插入内容
 */
public class SectionRenderPolicy extends AbstractRenderPolicy<List<SectionData>> {

    private final Map<String, TemplateStyle> styleConfig;

    public SectionRenderPolicy() {
        this.styleConfig = new HashMap<>();
    }

    public SectionRenderPolicy(Map<String, TemplateStyle> styleConfig) {
        this.styleConfig = styleConfig != null ? styleConfig : new HashMap<>();
    }

    @Override
    public void doRender(RenderContext<List<SectionData>> context) throws Exception {
        XWPFRun run = context.getRun();
        List<SectionData> sections = context.getData();

        if (sections == null || sections.isEmpty()) {
            clearPlaceholder(context, true);
            return;
        }

        // 获取当前段落
        XWPFParagraph currentPara = (XWPFParagraph) run.getParent();
        NiceXWPFDocument doc = (NiceXWPFDocument) context.getXWPFDocument();

        // 使用 XmlCursor 在当前段落位置插入新段落
        XmlCursor cursor = currentPara.getCTP().newCursor();

        // 渲染所有章节
        renderSectionsWithCursor(doc, sections, cursor);

        cursor.dispose();

        // 清除占位符所在的原段落
        clearPlaceholder(context, true);
    }

    /**
     * 使用 XmlCursor 在指定位置插入章节
     */
    private void renderSectionsWithCursor(NiceXWPFDocument doc, List<SectionData> sections, XmlCursor cursor) {
        for (SectionData section : sections) {
            // 1. 创建标题段落
            XWPFParagraph titlePara = doc.insertNewParagraph(cursor);
            if (titlePara != null) {
                XWPFRun titleRun = titlePara.createRun();
                titleRun.setText(section.getTitle());
                applyTitleStyle(titlePara, titleRun, section.getLevel());
                cursor.toNextToken(); // 移动光标到下一个位置
            }

            // 2. 如果有内容，创建内容段落
            if (section.getContent() != null && !section.getContent().trim().isEmpty()) {
                XWPFParagraph contentPara = doc.insertNewParagraph(cursor);
                if (contentPara != null) {
                    XWPFRun contentRun = contentPara.createRun();

                    String plainText = stripHtml(section.getContent());
                    String[] lines = plainText.split("\n");
                    boolean first = true;
                    for (String line : lines) {
                        String trimmedLine = line.trim();
                        if (!trimmedLine.isEmpty()) {
                            if (!first) {
                                contentRun.addBreak();
                            }
                            contentRun.setText(trimmedLine);
                            first = false;
                        }
                    }

                    applyBodyStyle(contentPara, contentRun);
                    cursor.toNextToken();
                }
            }

            // 3. 递归处理子章节
            if (section.getChildren() != null && !section.getChildren().isEmpty()) {
                renderSectionsWithCursor(doc, section.getChildren(), cursor);
            }
        }
    }

    private void applyTitleStyle(XWPFParagraph para, XWPFRun run, int level) {
        if (level < 1)
            level = 1;
        if (level > 4)
            level = 4;

        String styleType = "HEADING_" + level;
        TemplateStyle style = styleConfig.get(styleType);

        if (style != null) {
            applyStyle(para, run, style);
        } else {
            applyDefaultTitleStyle(para, run, level);
        }
    }

    private void applyBodyStyle(XWPFParagraph para, XWPFRun run) {
        TemplateStyle style = styleConfig.get("BODY");

        if (style != null) {
            applyStyle(para, run, style);
        } else {
            applyDefaultBodyStyle(para, run);
        }
    }

    private void applyStyle(XWPFParagraph para, XWPFRun run, TemplateStyle style) {
        // 字体
        if (style.getFontFamily() != null && !style.getFontFamily().isEmpty()) {
            run.setFontFamily(style.getFontFamily());
        }

        // 字号
        if (style.getFontSize() != null && style.getFontSize() > 0) {
            run.setFontSize(style.getFontSize());
        }

        // 加粗
        if (style.getBold() != null) {
            run.setBold(style.getBold());
        }

        // 斜体
        if (style.getItalic() != null) {
            run.setItalic(style.getItalic());
        }

        // 颜色
        if (style.getFontColor() != null && !style.getFontColor().isEmpty()
                && !style.getFontColor().equals("auto")) {
            run.setColor(style.getFontColor());
        }

        // 行间距
        if (style.getLineSpacing() != null && style.getLineSpacing() > 0) {
            setLineSpacing(para, style.getLineSpacing());
        }

        // 段前间距
        if (style.getSpacingBefore() != null && style.getSpacingBefore() > 0) {
            para.setSpacingBefore(style.getSpacingBefore().intValue() * 20);
        }

        // 段后间距
        if (style.getSpacingAfter() != null && style.getSpacingAfter() > 0) {
            para.setSpacingAfter(style.getSpacingAfter().intValue() * 20);
        }

        // 对齐方式 - 默认左对齐
        String alignment = style.getAlignment();
        if (alignment != null) {
            switch (alignment) {
                case "CENTER":
                    para.setAlignment(ParagraphAlignment.CENTER);
                    break;
                case "RIGHT":
                    para.setAlignment(ParagraphAlignment.RIGHT);
                    break;
                case "JUSTIFY":
                    para.setAlignment(ParagraphAlignment.BOTH);
                    break;
                default:
                    para.setAlignment(ParagraphAlignment.LEFT);
            }
        } else {
            para.setAlignment(ParagraphAlignment.LEFT);
        }

        // 首行缩进
        if (style.getFirstLineIndent() != null && style.getFirstLineIndent() > 0) {
            para.setFirstLineIndent(style.getFirstLineIndent().intValue() * 20);
        }
    }

    private void setLineSpacing(XWPFParagraph para, double lineSpacing) {
        try {
            if (para.getCTP().getPPr() == null) {
                para.getCTP().addNewPPr();
            }
            CTSpacing spacing = para.getCTP().getPPr().getSpacing();
            if (spacing == null) {
                spacing = para.getCTP().getPPr().addNewSpacing();
            }
            spacing.setLine(BigInteger.valueOf((long) (lineSpacing * 240)));
            spacing.setLineRule(STLineSpacingRule.AUTO);
        } catch (Exception e) {
            // 忽略
        }
    }

    private void applyDefaultTitleStyle(XWPFParagraph para, XWPFRun run, int level) {
        run.setBold(true);
        run.setFontFamily("黑体");
        para.setAlignment(ParagraphAlignment.LEFT);

        switch (level) {
            case 1:
                run.setFontSize(22);
                para.setSpacingAfter(240);
                break;
            case 2:
                run.setFontSize(18);
                para.setSpacingAfter(200);
                break;
            case 3:
                run.setFontSize(16);
                para.setSpacingAfter(160);
                break;
            case 4:
                run.setFontSize(14);
                para.setSpacingAfter(120);
                break;
            default:
                run.setFontSize(12);
                para.setSpacingAfter(120);
        }

        setLineSpacing(para, 1.5);
    }

    private void applyDefaultBodyStyle(XWPFParagraph para, XWPFRun run) {
        run.setFontFamily("宋体");
        run.setFontSize(12);
        run.setBold(false);

        para.setAlignment(ParagraphAlignment.LEFT);
        para.setSpacingAfter(120);
        para.setFirstLineIndent(480); // 首行缩进约两字符

        setLineSpacing(para, 1.5);
    }

    private String stripHtml(String html) {
        if (html == null)
            return "";

        String text = html.replaceAll("(?i)<br\\s*/?>", "\n");
        text = text.replaceAll("(?i)</p>", "\n");
        text = text.replaceAll("(?i)<p[^>]*>", "");
        text = text.replaceAll("<[^>]*>", "");

        text = text.replace("&nbsp;", " ");
        text = text.replace("&lt;", "<");
        text = text.replace("&gt;", ">");
        text = text.replace("&amp;", "&");
        text = text.replace("&quot;", "\"");

        return text.trim();
    }
}
