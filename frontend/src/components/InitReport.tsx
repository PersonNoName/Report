"use client";

import { useEffect, useRef } from 'react';
import { useReport } from '@/lib/store';

export default function InitReport() {
    const { state, actions } = useReport();
    const initializedTemplates = useRef(false);
    const creatingReport = useRef(false);

    // 1. Load Templates on Mount
    useEffect(() => {
        if (!initializedTemplates.current) {
            initializedTemplates.current = true;
            actions.loadTemplates();
        }
    }, [actions]);

    // 2. Auto-create Report if none exists and templates are loaded
    useEffect(() => {
        if (state.templates.length > 0 && !state.reportId && !state.isLoading && !creatingReport.current) {
            creatingReport.current = true;

            // Prefer "Weekly Market Report" request by user, otherwise first available
            const template = state.templates.find(t => t.name === 'Weekly Market Report') || state.templates[0];

            if (template) {
                console.log("Auto-creating report from template:", template.name);
                actions.createReport(template.id, 'My First Report')
                    .catch(err => console.error("Auto-creation failed", err))
                    .finally(() => {
                        creatingReport.current = false;
                    });
            } else {
                creatingReport.current = false;
            }
        }
    }, [state.templates, state.reportId, state.isLoading, actions]);

    return null;
}
