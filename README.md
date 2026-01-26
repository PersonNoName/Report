# Report Generation System

An atomized content management system for generating consistent, high-fidelity Word reports.

## Architecture

- **Frontend**: Next.js 14, Tiptap, TailwindCSS (Port 3000)
- **Backend**: Spring Boot 3, H2 Database, poi-tl (Port 8080)

## Prerequisites

- Node.js 18+
- JDK 17+
- Maven 3.6+

## Getting Started

### 1. Start the Backend

```bash
cd backend
mvn spring-boot:run
```

The backend server will start on `http://localhost:8080`.
- H2 Console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:file:./data/reportdb`)
- API Docs: `http://localhost:8080/api/templates`

### 2. Start the Frontend

```bash
cd frontend
npm install
npm run dev
```

The application will be available at `http://localhost:3000`.

## Features

- **Atomized Editing**: Break down reports into independent sections.
- **Dynamic Templates**: Configure report structure via JSON templates.
- **Reference Library**: Search and reuse content from historical reports.
- **Word Import**: Slice existing Word documents into reusable sections.
- **Excel View**: Overlay reference data from Excel files while editing.
- **Word Export**: Generate perfectly formatted Word documents using `poi-tl` templates.

## Configuration

- **Backend Port**: `8080` (Configurable in `backend/src/main/resources/application.yml`)
- **Frontend API URL**: Default `http://localhost:8080/api` (Configurable via `NEXT_PUBLIC_API_URL`)
