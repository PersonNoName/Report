-- Report Generation System Database Schema
-- MySQL 8.0+

-- 1. 报告模板定义表 (如：周报模板V1, 月报模板V2024)
CREATE TABLE IF NOT EXISTS report_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '模板名称，如"研发部通用周报"',
    description VARCHAR(255) COMMENT '模板描述',
    base_docx_url VARCHAR(255) COMMENT '对应的空白Word模板文件存储路径(含占位符)',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否激活',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报告模板定义表';

-- 2. 模板章节结构表 (核心扩展表)
CREATE TABLE IF NOT EXISTS template_section (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    template_id BIGINT NOT NULL COMMENT '关联report_template',
    section_key VARCHAR(50) NOT NULL COMMENT '唯一标识，对应Word模板里的占位符',
    title VARCHAR(100) NOT NULL COMMENT '标题名称',
    section_type VARCHAR(20) DEFAULT 'RICH_TEXT' COMMENT '类型：RICH_TEXT/TABLE/CHART',
    sort_order INT DEFAULT 0 COMMENT '排序权重',
    parent_id BIGINT DEFAULT NULL COMMENT '支持多级标题',
    is_active BOOLEAN DEFAULT TRUE COMMENT '软删除标记',
    INDEX idx_template_id (template_id),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模板章节结构表';

-- 3. 报告实例表 (每一次写报告就是生成一条记录)
CREATE TABLE IF NOT EXISTS report_instance (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    template_id BIGINT NOT NULL COMMENT '关联模板ID',
    user_id BIGINT COMMENT '创建用户ID',
    report_name VARCHAR(200) COMMENT '报告名称',
    start_date DATE COMMENT '报告周期开始日期',
    end_date DATE COMMENT '报告周期结束日期',
    status VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态: DRAFT/FINALIZED',
    source_excel_url VARCHAR(255) COMMENT '关联的Excel数据源路径',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_template_id (template_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报告实例表';

-- 4. 报告内容详情表 (纵表设计，解决扩展性)
CREATE TABLE IF NOT EXISTS report_content (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    report_instance_id BIGINT NOT NULL COMMENT '关联报告实例ID',
    section_key VARCHAR(50) NOT NULL COMMENT '对应template_section的key',
    content_html TEXT COMMENT '存储富文本HTML',
    content_json JSON COMMENT '表格或复杂数据，存JSON',
    version INT DEFAULT 1 COMMENT '版本号',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_report_section (report_instance_id, section_key),
    INDEX idx_report_instance_id (report_instance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报告内容详情表';

-- 5. 参考资料库/话术库
CREATE TABLE IF NOT EXISTS reference_material (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    section_key VARCHAR(50) NOT NULL COMMENT '属于哪个模块',
    content_text TEXT NOT NULL COMMENT '资料内容',
    tags VARCHAR(255) COMMENT '标签，逗号分隔',
    source_report_id BIGINT COMMENT '来源报告ID',
    is_standard BOOLEAN DEFAULT FALSE COMMENT '是否为标准话术',
    created_by BIGINT COMMENT '创建人ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_section_key (section_key),
    INDEX idx_is_standard (is_standard),
    FULLTEXT INDEX ft_content (content_text)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='参考资料库';

-- 初始数据：默认周报模板
INSERT INTO report_template (name, description, is_active) VALUES
('通用周报模板', '适用于各部门的通用周报格式', TRUE);

-- 默认章节结构
INSERT INTO template_section (template_id, section_key, title, section_type, sort_order, is_active) VALUES
(1, 'weekly_summary', '本周工作总结', 'RICH_TEXT', 1, TRUE),
(1, 'key_achievements', '重点成果', 'RICH_TEXT', 2, TRUE),
(1, 'issues_risks', '问题与风险', 'RICH_TEXT', 3, TRUE),
(1, 'next_week_plan', '下周工作计划', 'RICH_TEXT', 4, TRUE),
(1, 'resources_needed', '需要的资源支持', 'RICH_TEXT', 5, TRUE);
