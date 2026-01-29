-- ============================================
-- 模板样式配置表
-- 存储从上传的 Word 模板中提取的样式信息
-- ============================================

CREATE TABLE IF NOT EXISTS template_style (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_id BIGINT NOT NULL COMMENT '关联模板ID',
    
    -- 样式类型: HEADING_1, HEADING_2, HEADING_3, HEADING_4, BODY
    style_type VARCHAR(50) NOT NULL COMMENT '样式类型',
    
    -- 字体属性
    font_family VARCHAR(100) COMMENT '字体名称',
    font_size INT COMMENT '字号(磅)',
    bold TINYINT(1) DEFAULT 0 COMMENT '是否加粗',
    italic TINYINT(1) DEFAULT 0 COMMENT '是否斜体',
    font_color VARCHAR(20) COMMENT '字体颜色(十六进制)',
    
    -- 段落属性
    line_spacing DOUBLE COMMENT '行间距(倍数)',
    spacing_before DOUBLE COMMENT '段前间距(磅)',
    spacing_after DOUBLE COMMENT '段后间距(磅)',
    alignment VARCHAR(20) COMMENT '对齐方式: LEFT, CENTER, RIGHT, JUSTIFY',
    
    -- 首行缩进
    first_line_indent DOUBLE COMMENT '首行缩进(磅)',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_template_id (template_id),
    UNIQUE KEY uk_template_style (template_id, style_type),
    FOREIGN KEY (template_id) REFERENCES report_template(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板样式配置表';

-- ============================================
-- 默认样式数据（可选，用于没有样式配置的模板）
-- ============================================

-- 如需插入默认样式，取消下方注释并修改 template_id
/*
INSERT INTO template_style (template_id, style_type, font_family, font_size, bold, line_spacing, spacing_after) VALUES
(1, 'HEADING_1', '黑体', 22, 1, 1.5, 12),
(1, 'HEADING_2', '黑体', 18, 1, 1.5, 10),
(1, 'HEADING_3', '黑体', 16, 1, 1.5, 8),
(1, 'HEADING_4', '黑体', 14, 1, 1.5, 6),
(1, 'BODY', '宋体', 12, 0, 1.5, 6);
*/
