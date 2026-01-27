
// Backend API Types - matching Spring Boot entities

export interface ReportTemplate {
  id: number;
  name: string;
  description: string;
  baseDocxUrl: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface TemplateSection {
  id: number;
  templateId: number;
  sectionKey: string;
  title: string;
  sectionType: 'RICH_TEXT' | 'TABLE' | 'CHART';
  sortOrder: number;
  parentId: number | null;
  isActive: boolean;
}

export interface SectionNode {
  title: string;
  level: number;
  children: SectionNode[];
}

export interface ReportInstance {
  id: number;
  templateId: number;
  userId: number;
  reportName: string;
  startDate: string;
  endDate: string;
  status: 'DRAFT' | 'FINALIZED';
  sourceExcelUrl: string;
  createdAt: string;
  updatedAt: string;
}

export interface ReportContent {
  id: number;
  reportInstanceId: number;
  sectionKey: string;
  contentHtml: string;
  contentJson: Record<string, unknown>;
  version: number;
  updatedAt: string;
}

export interface ReferenceMaterial {
  id: number;
  sectionKey: string;
  contentText: string;
  tags: string;
  sourceReportId: number | null;
  isStandard: boolean;
  createdBy: number | null;
  createdAt: string;
}

// API Response wrapper
export interface ApiResult<T> {
  code: number;
  message: string;
  data: T;
}

// Report detail response
export interface ReportDetail {
  report: ReportInstance;
  sections: TemplateSection[];
  contents: Record<string, ReportContent>;
}

// Legacy types for backward compatibility
export interface ReportSection {
  id: string;
  title: string;
  content: string;
  type: 'markdown' | 'table' | 'chart';
  lastModified: string;
}

export interface ReportMetadata {
  title: string;
  version: string;
  author: string;
  date: string;
}

export interface HistoricalFragment {
  id: string;
  title: string;
  content: string;
  matchScore: number;
  source: string;
  relatedTo: string;
}

export interface LinkedAsset {
  name: string;
  size: string;
  type: 'image' | 'csv' | 'pdf';
}