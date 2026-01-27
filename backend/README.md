# Report Generation System Backend

Spring Boot 3 + MySQL + MyBatis-Plus

## 启动步骤

### 1. 创建数据库
```sql
CREATE DATABASE report_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 执行表结构
运行 `src/main/resources/db/schema.sql` 创建表和初始数据。

### 3. 配置数据库连接
修改 `src/main/resources/application.yml` 中的数据库连接信息：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/report_db
    username: your_username
    password: your_password
```

### 4. 启动后端
```bash
cd backend
mvn spring-boot:run
```

后端将在 `http://localhost:8080` 启动。

## API 端点

| 端点 | 方法 | 描述 |
|------|------|------|
| `/api/templates` | GET | 获取所有模板 |
| `/api/templates/{id}/sections` | GET | 获取模板章节 |
| `/api/reports` | GET, POST | 报告列表与创建 |
| `/api/reports/{id}` | GET, PUT | 报告详情与更新 |
| `/api/reports/{id}/contents/{key}` | PUT | 保存章节内容 |
| `/api/reports/{id}/export` | GET | 导出Word文档 |
| `/api/references` | GET, POST | 参考资料管理 |

## Word模板

将Word模板文件放置在 `src/main/resources/templates/template.docx`，模板中使用占位符如：
- `{{report_title}}` - 报告标题
- `{{weekly_summary}}` - 本周总结
- `{{next_week_plan}}` - 下周计划
