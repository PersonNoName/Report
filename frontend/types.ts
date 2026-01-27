
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
  // Added relatedTo property to fix type errors in components that import from this file (e.g., app/page.tsx)
  relatedTo: string;
}

export interface LinkedAsset {
  name: string;
  size: string;
  type: 'image' | 'csv' | 'pdf';
}