import React from 'react';
import type { ReportTemplate } from '../types';
import { BrutalButton } from './BrutalButton';

interface TemplateSelectDialogProps {
    templates: ReportTemplate[];
    isOpen: boolean;
    onSelect: (templateId: number) => void;
    onClose: () => void;
}

export const TemplateSelectDialog: React.FC<TemplateSelectDialogProps> = ({
    templates,
    isOpen,
    onSelect,
    onClose,
}) => {
    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
            {/* Backdrop */}
            <div
                className="absolute inset-0 bg-black/50"
                onClick={onClose}
            />

            {/* Dialog */}
            <div className="relative bg-white border-4 border-black shadow-[8px_8px_0_0_rgba(0,0,0,1)] w-full max-w-2xl mx-4">
                {/* Header */}
                <div className="bg-black text-white p-4 flex justify-between items-center">
                    <h2 className="text-lg font-bold uppercase tracking-widest">选择报告模板</h2>
                    <button
                        onClick={onClose}
                        className="text-white hover:text-gray-300 transition-colors"
                    >
                        <span className="material-symbols-outlined">close</span>
                    </button>
                </div>

                {/* Content */}
                <div className="p-6">
                    {templates.length === 0 ? (
                        <div className="text-center py-10 text-gray-500">
                            <span className="material-symbols-outlined text-4xl block mb-2">folder_off</span>
                            <p className="text-sm font-bold">暂无可用模板</p>
                        </div>
                    ) : (
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            {templates.map((template) => (
                                <div
                                    key={template.id}
                                    className="border-2 border-black p-4 hover:bg-gray-50 transition-colors cursor-pointer group"
                                    onClick={() => onSelect(template.id)}
                                >
                                    <div className="flex items-start justify-between mb-2">
                                        <h3 className="font-bold text-sm uppercase">{template.name}</h3>
                                        <span className="material-symbols-outlined text-gray-400 group-hover:text-black transition-colors">
                                            arrow_forward
                                        </span>
                                    </div>
                                    <p className="text-xs text-gray-600 line-clamp-2">
                                        {template.description || '暂无描述'}
                                    </p>
                                    <div className="mt-3 pt-3 border-t border-gray-200">
                                        <span className="text-[10px] text-gray-400 uppercase">
                                            创建于 {template.createdAt?.split('T')[0] || '-'}
                                        </span>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>

                {/* Footer */}
                <div className="border-t-2 border-black p-4 flex justify-end">
                    <BrutalButton variant="secondary" onClick={onClose}>
                        取消
                    </BrutalButton>
                </div>
            </div>
        </div>
    );
};
