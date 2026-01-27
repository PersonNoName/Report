package com.report.policy;

import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.policy.AbstractRenderPolicy;
import com.deepoove.poi.render.RenderContext;
import com.deepoove.poi.xwpf.NiceXWPFDocument;
import com.report.dto.SectionData;
import org.apache.poi.xwpf.usermodel.*;

import java.util.List;

/**
 * 自定义渲染策略，用于动态生成树形章节结构
 * 保留模板中定义的标题样式（Heading 1, Heading 2 等）
 */
public class SectionRenderPolicy extends AbstractRenderPolicy<List<SectionData>> {

    /**
     * 标题样式映射：level -> Word 样式 ID
     * 中文 Word 默认使用数字作为标题样式 ID
     */
    private static final String[] HEADING_STYLES = { "1", "2", "3", "4", "5", "6" };

    @Override
    public void doRender(RenderContext<List<SectionData>> context) throws Exception {
        XWPFRun run = context.getRun();
        List<SectionData> sections = context.getData();

        if (sections == null || sections.isEmpty()) {
            // 清除占位符
            clearPlaceholder(context, true);
            return;
        }

        // 获取文档
        NiceXWPFDocument doc = (NiceXWPFDocument) context.getXWPFDocument();

        // 获取当前段落，用于确定插入位置
        XWPFParagraph currentParagraph = (XWPFParagraph) run.getParent();

        // 递归渲染所有章节
        renderSections(doc, currentParagraph, sections);

        // 清除占位符
        clearPlaceholder(context, true);
    }

    /**
     * 递归渲染章节
     */
    private void renderSections(NiceXWPFDocument doc, XWPFParagraph afterParagraph,
            List<SectionData> sections) {
        for (SectionData section : sections) {
            // 1. 创建标题段落
            XWPFParagraph titlePara = doc.createParagraph();

            // 设置标题样式
            String styleId = getHeadingStyle(section.getLevel());
            try {
                titlePara.setStyle(styleId);
            } catch (Exception e) {
                // 样式不存在时，使用备用格式
                applyFallbackTitleStyle(titlePara, section.getLevel());
            }

            XWPFRun titleRun = titlePara.createRun();
            titleRun.setText(section.getTitle());

            // 2. 如果有内容，创建内容段落
            if (section.getContent() != null && !section.getContent().trim().isEmpty()) {
                XWPFParagraph contentPara = doc.createParagraph();
                XWPFRun contentRun = contentPara.createRun();

                // 处理 HTML 内容：简单去除标签，保留换行
                String plainText = stripHtml(section.getContent());

                // 处理换行
                String[] lines = plainText.split("\n");
                for (int i = 0; i < lines.length; i++) {
                    contentRun.setText(lines[i]);
                    if (i < lines.length - 1) {
                        contentRun.addBreak();
                    }
                }
            }

            // 3. 递归处理子章节
            if (section.getChildren() != null && !section.getChildren().isEmpty()) {
                renderSections(doc, titlePara, section.getChildren());
            }
        }
    }

    /**
     * 获取标题样式 ID
     */
    private String getHeadingStyle(int level) {
        if (level < 1)
            level = 1;
        if (level > HEADING_STYLES.length)
            level = HEADING_STYLES.length;
        return HEADING_STYLES[level - 1];
    }

    /**
     * 当模板中没有定义标题样式时，使用备用格式
     */
    private void applyFallbackTitleStyle(XWPFParagraph para, int level) {
        XWPFRun run = para.getRuns().isEmpty() ? para.createRun() : para.getRuns().get(0);
        run.setBold(true);

        // 根据层级设置字号
        switch (level) {
            case 1:
                run.setFontSize(18);
                run.setFontFamily("黑体");
                break;
            case 2:
                run.setFontSize(16);
                run.setFontFamily("黑体");
                break;
            case 3:
                run.setFontSize(14);
                run.setFontFamily("黑体");
                break;
            default:
                run.setFontSize(12);
                run.setFontFamily("宋体");
                break;
        }
    }

    /**
     * 去除 HTML 标签，保留基本格式
     */
    private String stripHtml(String html) {
        if (html == null)
            return "";

        // 将 <br>, <p>, </p> 转换为换行
        String text = html.replaceAll("(?i)<br\\s*/?>", "\n");
        text = text.replaceAll("(?i)</p>", "\n");
        text = text.replaceAll("(?i)<p[^>]*>", "");

        // 去除其他 HTML 标签
        text = text.replaceAll("<[^>]*>", "");

        // 解码 HTML 实体
        text = text.replace("&nbsp;", " ");
        text = text.replace("&lt;", "<");
        text = text.replace("&gt;", ">");
        text = text.replace("&amp;", "&");
        text = text.replace("&quot;", "\"");

        return text.trim();
    }
}
