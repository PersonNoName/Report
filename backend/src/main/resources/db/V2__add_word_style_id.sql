-- ============================================
-- 数据库迁移：添加 Word 样式 ID 字段
-- 用于支持通过 paragraph.setStyle() 设置样式
-- ============================================

-- 添加 word_style_id 字段
ALTER TABLE template_style 
ADD COLUMN word_style_id VARCHAR(100) COMMENT 'Word内置样式ID，如Heading1、1、标题1等';

-- 更新现有数据的默认样式 ID
UPDATE template_style SET word_style_id = 'Heading1' WHERE style_type = 'HEADING_1' AND word_style_id IS NULL;
UPDATE template_style SET word_style_id = 'Heading2' WHERE style_type = 'HEADING_2' AND word_style_id IS NULL;
UPDATE template_style SET word_style_id = 'Heading3' WHERE style_type = 'HEADING_3' AND word_style_id IS NULL;
UPDATE template_style SET word_style_id = 'Heading4' WHERE style_type = 'HEADING_4' AND word_style_id IS NULL;
UPDATE template_style SET word_style_id = 'Normal' WHERE style_type = 'BODY' AND word_style_id IS NULL;
