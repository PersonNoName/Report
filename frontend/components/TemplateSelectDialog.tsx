import React from 'react';
import type { ReportTemplate, SectionNode } from '../types';
import { BrutalButton } from './BrutalButton';

import { parseDocx } from '../services/api';

interface TemplateSelectDialogProps {
    templates: ReportTemplate[];
    isOpen: boolean;
    onSelect: (templateId: number) => void;
    onCreate: (name: string, description: string, sections?: SectionNode[], baseDocxUrl?: string) => void;
    onClose: () => void;
}

const SectionTree: React.FC<{ nodes: SectionNode[], level?: number }> = ({ nodes, level = 0 }) => {
    return (
        <ul className="space-y-1">
            {nodes.map((node, idx) => (
                <li key={idx} className="text-xs font-mono">
                    <div className="flex gap-2 items-center" style={{ paddingLeft: `${level * 16}px` }}>
                        <span className="text-gray-400 select-none">
                            {level === 0 ? `${idx + 1}.` : '└─'}
                        </span>
                        <span>{node.title}</span>
                    </div>
                    {node.children && node.children.length > 0 && (
                        <SectionTree nodes={node.children} level={level + 1} />
                    )}
                </li>
            ))}
        </ul>
    );
};

export const TemplateSelectDialog: React.FC<TemplateSelectDialogProps> = ({
    templates,
    isOpen,
    onSelect,
    onCreate,
    onClose,
}) => {
    // Local state for creation mode
    const [isCreating, setIsCreating] = React.useState(false);
    const [newTemplateName, setNewTemplateName] = React.useState('');
    const [newTemplateDesc, setNewTemplateDesc] = React.useState('');
    const [parsedSections, setParsedSections] = React.useState<SectionNode[]>([]);
    const [parsedFileName, setParsedFileName] = React.useState<string>('');
    const [isParsing, setIsParsing] = React.useState(false);

    if (!isOpen) return null;

    const handleCreateSubmit = () => {
        if (!newTemplateName.trim()) {
            alert('请输入模板名称');
            return;
        }
        onCreate(newTemplateName, newTemplateDesc, parsedSections, parsedFileName);
        setIsCreating(false);
        setNewTemplateName('');
        setNewTemplateDesc('');
        setParsedSections([]);
        setParsedFileName('');
    };

    const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (!file) return;

        try {
            setIsParsing(true);
            const result = await parseDocx(file);
            setParsedSections(result.sections);
            setParsedFileName(result.fileName);
            if (!newTemplateName && file.name) {
                setNewTemplateName(file.name.replace(/\.[^/.]+$/, ""));
            }
        } catch (error) {
            console.error('解析失败:', error);
            alert('解析Word文档失败，请检查文件格式');
        } finally {
            setIsParsing(false);
        }
    };

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
            {/* Backdrop */}
            <div
                className="absolute inset-0 bg-black/50"
                onClick={onClose}
            />

            {/* Dialog */}
            <div className="relative bg-white border-4 border-black shadow-[8px_8px_0_0_rgba(0,0,0,1)] w-full max-w-2xl mx-4 max-h-[90vh] flex flex-col">
                {/* Header */}
                <div className="bg-black text-white p-4 flex justify-between items-center flex-none">
                    <h2 className="text-lg font-bold uppercase tracking-widest">
                        {isCreating ? '创建新模板' : '选择报告模板'}
                    </h2>
                    <button
                        onClick={onClose}
                        className="text-white hover:text-gray-300 transition-colors"
                    >
                        <span className="material-symbols-outlined">close</span>
                    </button>
                </div>

                {/* Content */}
                <div className="p-6 flex-1 overflow-y-auto">
                    {isCreating ? (
                        <div className="space-y-4">
                            <div>
                                <label className="block text-xs font-bold uppercase mb-1">模板名称</label>
                                <input
                                    className="w-full border-2 border-black p-2 font-mono text-sm focus:ring-0"
                                    placeholder="例如：通用周报模板"
                                    value={newTemplateName}
                                    onChange={e => setNewTemplateName(e.target.value)}
                                    autoFocus
                                />
                            </div>
                            <div>
                                <label className="block text-xs font-bold uppercase mb-1">描述 (可选)</label>
                                <textarea
                                    className="w-full border-2 border-black p-2 font-mono text-sm focus:ring-0 h-16 resize-none"
                                    placeholder="简要描述此模板的用途..."
                                    value={newTemplateDesc}
                                    onChange={e => setNewTemplateDesc(e.target.value)}
                                />
                            </div>

                            <div className="border-t-2 border-dashed border-gray-300 pt-4">
                                <label className="block text-xs font-bold uppercase mb-2">
                                    从Word导入章节 (可选)
                                </label>
                                <div className="flex items-center gap-2 mb-2">
                                    <label className="cursor-pointer bg-gray-100 border-2 border-black px-3 py-1 text-xs font-bold hover:bg-gray-200 transition-colors">
                                        上传 .docx 文件
                                        <input
                                            type="file"
                                            accept=".docx"
                                            className="hidden"
                                            onChange={handleFileUpload}
                                        />
                                    </label>
                                    {isParsing && <span className="text-xs text-gray-500 animate-pulse">解析中...</span>}
                                </div>
                                <p className="text-[10px] text-gray-500 mb-3">
                                    支持自动识别各级标题 (Heading 1-6)
                                </p>

                                {parsedSections.length > 0 && (
                                    <div className="bg-gray-50 border-2 border-black p-2">
                                        <div className="text-[10px] font-bold border-b border-gray-300 pb-1 mb-2 flex justify-between">
                                            <span>已识别到结构:</span>
                                            <button
                                                className="text-red-500 hover:underline"
                                                onClick={() => setParsedSections([])}
                                            >
                                                清除
                                            </button>
                                        </div>
                                        <div className="max-h-60 overflow-y-auto">
                                            <SectionTree nodes={parsedSections} />
                                        </div>
                                    </div>
                                )}
                            </div>
                        </div>
                    ) : (
                        templates.length === 0 ? (
                            <div className="text-center py-10 text-gray-500">
                                <span className="material-symbols-outlined text-4xl block mb-2">folder_off</span>
                                <p className="text-sm font-bold">暂无可用模板</p>
                                <button
                                    onClick={() => setIsCreating(true)}
                                    className="mt-4 text-blue-600 hover:underline font-bold text-xs uppercase"
                                >
                                    + 创建第一个模板
                                </button>
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
                                {/* Always show create button card */}
                                <div
                                    className="border-2 border-dashed border-gray-300 p-4 hover:border-black hover:bg-gray-50 transition-colors cursor-pointer flex flex-col items-center justify-center text-gray-400 hover:text-black min-h-[120px]"
                                    onClick={() => setIsCreating(true)}
                                >
                                    <span className="material-symbols-outlined text-3xl mb-1">add_circle</span>
                                    <span className="text-xs font-bold uppercase">创建新模板</span>
                                </div>
                            </div>
                        )
                    )}
                </div>

                {/* Footer */}
                <div className="border-t-2 border-black p-4 flex justify-end gap-2 flex-none">
                    {isCreating ? (
                        <>
                            <BrutalButton variant="secondary" onClick={() => setIsCreating(false)}>
                                返回
                            </BrutalButton>
                            <BrutalButton variant="primary" onClick={handleCreateSubmit}>
                                确认创建
                            </BrutalButton>
                        </>
                    ) : (
                        <BrutalButton variant="secondary" onClick={onClose}>
                            取消
                        </BrutalButton>
                    )}
                </div>
            </div>
        </div>
    );
};
