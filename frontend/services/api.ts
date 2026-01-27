import type {
    ApiResult,
    ReportTemplate,
    TemplateSection,
    ReportInstance,
    ReportContent,
    ReportDetail,
    ReferenceMaterial,
    SectionNode
} from '../types';

const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

async function request<T>(url: string, options: RequestInit = {}): Promise<T> {
    const response = await fetch(`${API_BASE}${url}`, {
        headers: {
            'Content-Type': 'application/json',
            ...options.headers,
        },
        ...options,
    });

    if (!response.ok) {
        throw new Error(`API Error: ${response.status}`);
    }

    const result: ApiResult<T> = await response.json();

    if (result.code !== 200) {
        throw new Error(result.message);
    }

    return result.data;
}

// ============ Template API ============

export async function getTemplates(): Promise<ReportTemplate[]> {
    return request<ReportTemplate[]>('/templates');
}

export async function getTemplate(id: number): Promise<ReportTemplate> {
    return request<ReportTemplate>(`/templates/${id}`);
}

export async function createTemplate(template: Partial<ReportTemplate>): Promise<ReportTemplate> {
    return request<ReportTemplate>('/templates', {
        method: 'POST',
        body: JSON.stringify(template),
    });
}

export async function getTemplateSections(templateId: number): Promise<TemplateSection[]> {
    return request<TemplateSection[]>(`/templates/${templateId}/sections`);
}

export async function addSection(templateId: number, section: Partial<TemplateSection>): Promise<TemplateSection> {
    return request<TemplateSection>(`/templates/${templateId}/sections`, {
        method: 'POST',
        body: JSON.stringify(section),
    });
}

export async function updateSection(sectionId: number, section: Partial<TemplateSection>): Promise<TemplateSection> {
    return request<TemplateSection>(`/templates/sections/${sectionId}`, {
        method: 'PUT',
        body: JSON.stringify(section),
    });
}

export async function deleteSection(sectionId: number): Promise<void> {
    return request<void>(`/templates/sections/${sectionId}`, {
        method: 'DELETE',
    });
}

interface ParseResult {
    sections: SectionNode[];
    fileName: string;
}

export async function parseDocx(file: File): Promise<ParseResult> {
    const formData = new FormData();
    formData.append('file', file);

    const response = await fetch(`${API_BASE}/templates/parse`, {
        method: 'POST',
        body: formData,
    });

    if (!response.ok) {
        throw new Error(`API Error: ${response.status}`);
    }

    const result: ApiResult<ParseResult> = await response.json();
    if (result.code !== 200) {
        throw new Error(result.message);
    }
    return result.data;
}

export async function createTemplateWithSections(
    template: Partial<ReportTemplate>,
    sections: SectionNode[]
): Promise<ReportTemplate> {
    const response = await fetch(`${API_BASE}/templates/with-sections`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ template, sections }),
    });

    if (!response.ok) {
        throw new Error(`API Error: ${response.status}`);
    }

    const result: ApiResult<ReportTemplate> = await response.json();
    if (result.code !== 200) {
        throw new Error(result.message);
    }
    return result.data;
}

// ============ Report API ============

export async function getReports(userId?: number): Promise<ReportInstance[]> {
    const query = userId ? `?userId=${userId}` : '';
    return request<ReportInstance[]>(`/reports${query}`);
}

export async function createReport(report: Partial<ReportInstance>): Promise<ReportInstance> {
    return request<ReportInstance>('/reports', {
        method: 'POST',
        body: JSON.stringify(report),
    });
}

export async function getReportDetail(reportId: number): Promise<ReportDetail> {
    return request<ReportDetail>(`/reports/${reportId}`);
}

export async function updateReport(reportId: number, report: Partial<ReportInstance>): Promise<ReportInstance> {
    return request<ReportInstance>(`/reports/${reportId}`, {
        method: 'PUT',
        body: JSON.stringify(report),
    });
}

export async function saveContent(
    reportId: number,
    sectionKey: string,
    contentHtml: string
): Promise<ReportContent> {
    return request<ReportContent>(`/reports/${reportId}/contents/${sectionKey}`, {
        method: 'PUT',
        body: JSON.stringify({ contentHtml }),
    });
}

export async function finalizeReport(reportId: number): Promise<void> {
    return request<void>(`/reports/${reportId}/finalize`, {
        method: 'POST',
    });
}

export function getExportUrl(reportId: number): string {
    return `${API_BASE}/reports/${reportId}/export`;
}

// ============ Reference API ============

export async function searchReferences(
    sectionKey: string,
    keyword?: string
): Promise<ReferenceMaterial[]> {
    const query = keyword ? `&keyword=${encodeURIComponent(keyword)}` : '';
    return request<ReferenceMaterial[]>(`/references?sectionKey=${sectionKey}${query}`);
}

export async function saveAsStandard(
    sectionKey: string,
    contentText: string,
    tags?: string,
    userId?: number
): Promise<ReferenceMaterial> {
    return request<ReferenceMaterial>('/references', {
        method: 'POST',
        body: JSON.stringify({ sectionKey, contentText, tags, userId }),
    });
}

export async function deleteReference(id: number): Promise<void> {
    return request<void>(`/references/${id}`, {
        method: 'DELETE',
    });
}

// ============ AI API ============
export async function polishContent(content: string): Promise<string> {
    return request<string>('/ai/polish', {
        method: 'POST',
        body: JSON.stringify({ content }),
    });
}
