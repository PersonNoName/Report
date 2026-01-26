// API Types for Report System

export interface SectionDefinition {
    id: string;
    title: string;
    required?: boolean;
}

export interface TemplateDTO {
    id: number;
    name: string;
    sections: SectionDefinition[];
    createdAt: string;
    updatedAt: string;
}

export interface ReportDTO {
    id: number;
    title: string;
    status: number;
    startDate: string;
    endDate: string;
    sections: ReportContent[];
}

export interface ReportContent {
    id: number;
    reportId: number;
    sectionTitle: string;
    contentHtml: string;
    sortOrder: number;
    isReferenceable: boolean;
    version: number;
    createdAt: string;
    updatedAt: string;
}

export interface ReferenceFragment {
    id: number;
    sectionTitle: string;
    contentHtml: string;
    sourceReportId: number;
    createdAt: string;
}

export interface CreateReportRequest {
    templateId: number;
    userId: number;
    title: string;
    reportType: string;
    startDate: string;
    endDate: string;
}

export interface CreateTemplateRequest {
    name: string;
    sections: SectionDefinition[];
}

export interface ExcelDataDTO {
    sheetName: string;
    headers: string[];
    rows: Record<string, string>[];
}

export interface ExportWordRequest {
    title: string;
    reportId?: number;
    sectionIds?: number[];
}

// API Client
const API_BASE = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api';

async function handleResponse<T>(response: Response): Promise<T> {
    if (!response.ok) {
        const error = await response.json().catch(() => ({ error: 'Request failed' }));
        throw new Error(error.error || 'Request failed');
    }
    return response.json();
}

// Template APIs
export const templateApi = {
    getAll: async (): Promise<TemplateDTO[]> => {
        const res = await fetch(`${API_BASE}/templates`);
        return handleResponse(res);
    },

    getById: async (id: number): Promise<TemplateDTO> => {
        const res = await fetch(`${API_BASE}/templates/${id}`);
        return handleResponse(res);
    },

    create: async (data: CreateTemplateRequest): Promise<TemplateDTO> => {
        const res = await fetch(`${API_BASE}/templates`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data),
        });
        return handleResponse(res);
    },

    update: async (id: number, data: CreateTemplateRequest): Promise<TemplateDTO> => {
        const res = await fetch(`${API_BASE}/templates/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data),
        });
        return handleResponse(res);
    },

    delete: async (id: number): Promise<void> => {
        const res = await fetch(`${API_BASE}/templates/${id}`, { method: 'DELETE' });
        if (!res.ok) throw new Error('Failed to delete template');
    },
};

// Report APIs
export const reportApi = {
    create: async (data: CreateReportRequest): Promise<ReportDTO> => {
        const res = await fetch(`${API_BASE}/reports`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data),
        });
        return handleResponse(res);
    },

    getById: async (id: number): Promise<ReportDTO> => {
        const res = await fetch(`${API_BASE}/reports/${id}`);
        return handleResponse(res);
    },
};

// Content APIs
export const contentApi = {
    addSection: async (reportId: number, title: string): Promise<ReportContent> => {
        const res = await fetch(`${API_BASE}/contents/${reportId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ title }),
        });
        return handleResponse(res);
    },

    updateContent: async (id: number, html?: string, title?: string): Promise<ReportContent> => {
        const res = await fetch(`${API_BASE}/contents/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ html, title }),
        });
        return handleResponse(res);
    },

    deleteSection: async (id: number): Promise<void> => {
        const res = await fetch(`${API_BASE}/contents/${id}`, { method: 'DELETE' });
        if (!res.ok) throw new Error('Failed to delete section');
    },
};

// Reference APIs
export const referenceApi = {
    search: async (title: string): Promise<ReferenceFragment[]> => {
        const res = await fetch(`${API_BASE}/references/search?title=${encodeURIComponent(title)}`);
        return handleResponse(res);
    },
};

// File APIs
export const fileApi = {
    uploadExcel: async (file: File): Promise<ExcelDataDTO[]> => {
        const formData = new FormData();
        formData.append('file', file);

        const res = await fetch(`${API_BASE}/files/upload/excel`, {
            method: 'POST',
            body: formData,
        });
        return handleResponse(res);
    },

    uploadWord: async (file: File): Promise<any[]> => {
        const formData = new FormData();
        formData.append('file', file);
        const res = await fetch(`${API_BASE}/files/upload/word`, {
            method: 'POST',
            body: formData,
        });
        return handleResponse(res);
    },

    exportWord: async (request: ExportWordRequest): Promise<Blob> => {
        const res = await fetch(`${API_BASE}/files/export/word`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(request),
        });

        if (!res.ok) throw new Error('Export failed');
        return res.blob();
    },

    excelToMarkdown: async (file: File, sheetIndex = 0): Promise<string> => {
        const formData = new FormData();
        formData.append('file', file);

        const res = await fetch(`${API_BASE}/files/excel-to-markdown?sheetIndex=${sheetIndex}`, {
            method: 'POST',
            body: formData,
        });
        return res.text();
    },
};
