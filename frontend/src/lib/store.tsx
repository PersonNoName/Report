"use client";

import { createContext, useContext, useReducer, useCallback, ReactNode } from 'react';
import {
    ReportContent,
    TemplateDTO,
    SectionDefinition,
    ExcelDataDTO,
    ReferenceFragment,
    templateApi,
    reportApi,
    contentApi,
    referenceApi,
    fileApi
} from './api';

// State Interface
interface ReportState {
    // Template
    currentTemplate: TemplateDTO | null;
    templates: TemplateDTO[];

    // Sections
    sections: ReportContent[];
    activeSectionId: number | null;

    // Reference Library
    historicalSections: ReferenceFragment[];
    searchQuery: string;

    // Uploaded Word References (Temporary)
    uploadedReferences: ReferenceFragment[];

    // Excel Data (for floating panel)
    excelData: ExcelDataDTO[] | null;

    // UI State
    isLoading: boolean;
    error: string | null;
    reportTitle: string;
    reportId: number | null;
}

// Action Types
type Action =
    | { type: 'SET_LOADING'; payload: boolean }
    | { type: 'SET_ERROR'; payload: string | null }
    | { type: 'SET_TEMPLATES'; payload: TemplateDTO[] }
    | { type: 'SET_CURRENT_TEMPLATE'; payload: TemplateDTO | null }
    | { type: 'SET_SECTIONS'; payload: ReportContent[] }
    | { type: 'ADD_SECTION'; payload: ReportContent }
    | { type: 'UPDATE_SECTION'; payload: ReportContent }
    | { type: 'REMOVE_SECTION'; payload: number }
    | { type: 'SET_ACTIVE_SECTION'; payload: number | null }
    | { type: 'SET_HISTORICAL_SECTIONS'; payload: ReferenceFragment[] }
    | { type: 'SET_SEARCH_QUERY'; payload: string }
    | { type: 'SET_EXCEL_DATA'; payload: ExcelDataDTO[] | null }
    | { type: 'SET_REPORT_TITLE'; payload: string }
    | { type: 'SET_REPORT_ID'; payload: number | null };

// Initial State
const initialState: ReportState = {
    currentTemplate: null,
    templates: [],
    sections: [],
    activeSectionId: null,
    historicalSections: [],
    uploadedReferences: [],
    searchQuery: '',
    excelData: null,
    isLoading: false,
    error: null,
    reportTitle: 'New Report',
    reportId: null,
};

// Reducer
function reportReducer(state: ReportState, action: Action): ReportState {
    switch (action.type) {
        case 'SET_LOADING':
            return { ...state, isLoading: action.payload };
        case 'SET_ERROR':
            return { ...state, error: action.payload };
        case 'SET_TEMPLATES':
            return { ...state, templates: action.payload };
        case 'SET_CURRENT_TEMPLATE':
            return { ...state, currentTemplate: action.payload };
        case 'SET_SECTIONS':
            return { ...state, sections: action.payload };
        case 'ADD_SECTION':
            return { ...state, sections: [...state.sections, action.payload] };
        case 'UPDATE_SECTION':
            return {
                ...state,
                sections: state.sections.map(s =>
                    s.id === action.payload.id ? action.payload : s
                ),
            };
        case 'REMOVE_SECTION':
            return {
                ...state,
                sections: state.sections.filter(s => s.id !== action.payload),
            };
        case 'SET_ACTIVE_SECTION':
            return { ...state, activeSectionId: action.payload };
        case 'SET_HISTORICAL_SECTIONS':
            return { ...state, historicalSections: action.payload };
        case 'SET_SEARCH_QUERY':
            return { ...state, searchQuery: action.payload };
        case 'SET_EXCEL_DATA':
            return { ...state, excelData: action.payload };
        case 'SET_REPORT_TITLE':
            return { ...state, reportTitle: action.payload };
        case 'SET_REPORT_ID':
            return { ...state, reportId: action.payload };
        default:
            return state;
    }
}

// Context
interface ReportContextType {
    state: ReportState;
    actions: {
        // Template & Report actions
        loadTemplates: () => Promise<void>;
        createReport: (templateId: number, title: string) => Promise<void>;
        loadReport: (id: number) => Promise<void>;

        // Section actions
        addSection: (title: string) => Promise<void>;
        updateSection: (id: number, contentHtml: string) => Promise<void>;
        updateSectionTitle: (id: number, title: string) => Promise<void>;
        deleteSection: (id: number) => Promise<void>;
        setActiveSection: (id: number | null) => void;

        // Search actions
        searchReferences: (query: string) => Promise<void>;
        applyReference: (referenceId: number, targetId: number) => Promise<void>;

        // File actions
        uploadExcel: (file: File) => Promise<void>;
        uploadWord: (file: File) => Promise<void>;
        exportWord: () => Promise<void>;
        clearExcelData: () => void;

        // UI actions
        setReportTitle: (title: string) => void;
        clearError: () => void;
    };
}

const ReportContext = createContext<ReportContextType | null>(null);

// Provider Component
export function ReportProvider({ children }: { children: ReactNode }) {
    const [state, dispatch] = useReducer(reportReducer, initialState);

    // Template Actions
    const loadTemplates = useCallback(async () => {
        dispatch({ type: 'SET_LOADING', payload: true });
        try {
            const templates = await templateApi.getAll();
            dispatch({ type: 'SET_TEMPLATES', payload: templates });
        } catch (err) {
            dispatch({ type: 'SET_ERROR', payload: (err as Error).message });
        } finally {
            dispatch({ type: 'SET_LOADING', payload: false });
        }
    }, []);

    const createReport = useCallback(async (templateId: number, title: string) => {
        dispatch({ type: 'SET_LOADING', payload: true });
        try {
            // Hardcoded userId and dates for now
            const report = await reportApi.create({
                templateId,
                userId: 1,
                title,
                reportType: 'WEEKLY',
                startDate: new Date().toISOString().split('T')[0],
                endDate: new Date().toISOString().split('T')[0]
            });

            dispatch({ type: 'SET_REPORT_ID', payload: report.id });
            dispatch({ type: 'SET_REPORT_TITLE', payload: report.title });
            dispatch({ type: 'SET_SECTIONS', payload: report.sections });
        } catch (err) {
            dispatch({ type: 'SET_ERROR', payload: (err as Error).message });
        } finally {
            dispatch({ type: 'SET_LOADING', payload: false });
        }
    }, []);

    const loadReport = useCallback(async (id: number) => {
        dispatch({ type: 'SET_LOADING', payload: true });
        try {
            const report = await reportApi.getById(id);
            dispatch({ type: 'SET_REPORT_ID', payload: report.id });
            dispatch({ type: 'SET_REPORT_TITLE', payload: report.title });
            dispatch({ type: 'SET_SECTIONS', payload: report.sections });
        } catch (err) {
            dispatch({ type: 'SET_ERROR', payload: (err as Error).message });
        } finally {
            dispatch({ type: 'SET_LOADING', payload: false });
        }
    }, []);

    // Section Actions
    const addSection = useCallback(async (title: string) => {
        if (!state.reportId) {
            dispatch({ type: 'SET_ERROR', payload: 'No active report' });
            return;
        }
        try {
            const newSection = await contentApi.addSection(state.reportId, title);
            dispatch({ type: 'ADD_SECTION', payload: newSection });
        } catch (err) {
            dispatch({ type: 'SET_ERROR', payload: (err as Error).message });
        }
    }, [state.reportId]);

    const updateSection = useCallback(async (id: number, contentHtml: string) => {
        try {
            const updated = await contentApi.updateContent(id, contentHtml, undefined);
            dispatch({ type: 'UPDATE_SECTION', payload: updated });
        } catch (err) {
            dispatch({ type: 'SET_ERROR', payload: (err as Error).message });
        }
    }, []);

    const updateSectionTitle = useCallback(async (id: number, title: string) => {
        try {
            const updated = await contentApi.updateContent(id, undefined, title);
            dispatch({ type: 'UPDATE_SECTION', payload: updated });
        } catch (err) {
            dispatch({ type: 'SET_ERROR', payload: (err as Error).message });
        }
    }, []);

    const deleteSection = useCallback(async (id: number) => {
        try {
            await contentApi.deleteSection(id);
            dispatch({ type: 'REMOVE_SECTION', payload: id });
        } catch (err) {
            dispatch({ type: 'SET_ERROR', payload: (err as Error).message });
        }
    }, []);

    const setActiveSection = useCallback((id: number | null) => {
        dispatch({ type: 'SET_ACTIVE_SECTION', payload: id });
    }, []);

    // Search Actions
    const searchReferences = useCallback(async (query: string) => {
        dispatch({ type: 'SET_SEARCH_QUERY', payload: query });
        if (!query.trim()) {
            dispatch({ type: 'SET_HISTORICAL_SECTIONS', payload: [] });
            return;
        }
        try {
            const results = await referenceApi.search(query);
            dispatch({ type: 'SET_HISTORICAL_SECTIONS', payload: results });
        } catch (err) {
            dispatch({ type: 'SET_ERROR', payload: (err as Error).message });
        }
    }, []);

    const applyReference = useCallback(async (referenceId: number, targetId: number) => {
        const reference = state.historicalSections.find(s => s.id === referenceId);
        if (!reference) return;

        const target = state.sections.find(s => s.id === targetId);
        if (!target) return;

        // Call update API
        await updateSection(targetId, reference.contentHtml);
    }, [state.historicalSections, state.sections, updateSection]);

    // File Actions
    const uploadExcel = useCallback(async (file: File) => {
        dispatch({ type: 'SET_LOADING', payload: true });
        try {
            const data = await fileApi.uploadExcel(file);
            dispatch({ type: 'SET_EXCEL_DATA', payload: data });
        } catch (err) {
            dispatch({ type: 'SET_ERROR', payload: (err as Error).message });
        } finally {
            dispatch({ type: 'SET_LOADING', payload: false });
        }
    }, []);

    const uploadWord = useCallback(async (file: File) => {
        dispatch({ type: 'SET_LOADING', payload: true });
        try {
            // Uploads to reference library, returns "SectionDTOs" (ReferenceFragments)
            const refs = await fileApi.uploadWord(file);
            // We could show them in historical sections or a separate list
            // For now, assume they are available for search, or if we want to show immediately:
            // dispatch({ type: 'SET_HISTORICAL_SECTIONS', payload: refs });
            // But type mismatch might occur if API returns SectionDTO but we expect ReferenceFragment
            // Let's assume FileController returns ReferenceFragment-like objects.
            // Updated API to return any[] for now or need to align types.
        } catch (err) {
            dispatch({ type: 'SET_ERROR', payload: (err as Error).message });
        } finally {
            dispatch({ type: 'SET_LOADING', payload: false });
        }
    }, []);

    const exportWord = useCallback(async () => {
        if (!state.reportId) return;
        dispatch({ type: 'SET_LOADING', payload: true });
        try {
            const blob = await fileApi.exportWord({
                title: state.reportTitle,
                reportId: state.reportId,
            });

            // Download file
            const url = URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `${state.reportTitle}.docx`;
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            URL.revokeObjectURL(url);
        } catch (err) {
            dispatch({ type: 'SET_ERROR', payload: (err as Error).message });
        } finally {
            dispatch({ type: 'SET_LOADING', payload: false });
        }
    }, [state.reportTitle, state.reportId]);

    const clearExcelData = useCallback(() => {
        dispatch({ type: 'SET_EXCEL_DATA', payload: null });
    }, []);

    // UI Actions
    const setReportTitle = useCallback((title: string) => {
        dispatch({ type: 'SET_REPORT_TITLE', payload: title });
    }, []);

    const clearError = useCallback(() => {
        dispatch({ type: 'SET_ERROR', payload: null });
    }, []);

    const value: ReportContextType = {
        state,
        actions: {
            loadTemplates,
            createReport,
            loadReport,
            addSection,
            updateSection,
            updateSectionTitle,
            deleteSection,
            setActiveSection,
            searchReferences,
            applyReference,
            uploadExcel,
            uploadWord,
            exportWord,
            clearExcelData,
            setReportTitle,
            clearError,
        },
    };

    return (
        <ReportContext.Provider value={value}>
            {children}
        </ReportContext.Provider>
    );
}

// Hook
export function useReport() {
    const context = useContext(ReportContext);
    if (!context) {
        throw new Error('useReport must be used within a ReportProvider');
    }
    return context;
}
