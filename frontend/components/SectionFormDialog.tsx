import React, { useState, useEffect } from 'react';
import type { TemplateSection } from '../types';
import { BrutalButton } from './BrutalButton';

interface SectionFormDialogProps {
    isOpen: boolean;
    section?: TemplateSection | null; // null = 新建模式
    onSave: (data: { title: string; sectionType: string; sectionKey: string }) => void;
    onClose: () => void;
}

const SECTION_TYPES = [
    { value: 'RICH_TEXT', label: '富文本' },
    { value: 'TABLE', label: '表格' },
    { value: 'CHART', label: '图表' },
];

export const SectionFormDialog: React.FC<SectionFormDialogProps> = ({
    isOpen,
    section,
    onSave,
    onClose,
}) => {
    const [title, setTitle] = useState('');
    const [sectionKey, setSectionKey] = useState('');
    const [sectionType, setSectionType] = useState('RICH_TEXT');
    const [errors, setErrors] = useState<{ title?: string; sectionKey?: string }>({});

    const isEditMode = !!section;

    // Reset form when dialog opens/closes or section changes
    useEffect(() => {
        if (isOpen) {
            if (section) {
                setTitle(section.title);
                setSectionKey(section.sectionKey);
                setSectionType(section.sectionType);
            } else {
                setTitle('');
                setSectionKey('');
                setSectionType('RICH_TEXT');
            }
            setErrors({});
        }
    }, [isOpen, section]);

    // Auto-generate sectionKey from title (only in create mode)
    const handleTitleChange = (value: string) => {
        setTitle(value);
        if (!isEditMode) {
            // Generate key from title: lowercase, replace spaces with underscores
            const generatedKey = value
                .toLowerCase()
                .replace(/[^a-z0-9\u4e00-\u9fa5]/g, '_')
                .replace(/_+/g, '_')
                .replace(/^_|_$/g, '');
            setSectionKey(generatedKey);
        }
    };

    const validate = (): boolean => {
        const newErrors: { title?: string; sectionKey?: string } = {};

        if (!title.trim()) {
            newErrors.title = '标题不能为空';
        }

        if (!sectionKey.trim()) {
            newErrors.sectionKey = '唯一标识不能为空';
        } else if (!/^[a-z0-9_\u4e00-\u9fa5]+$/.test(sectionKey)) {
            newErrors.sectionKey = '只能包含小写字母、数字、下划线或中文';
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (validate()) {
            onSave({ title, sectionKey, sectionType });
        }
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
            {/* Backdrop */}
            <div
                className="absolute inset-0 bg-black/50"
                onClick={onClose}
            />

            {/* Dialog */}
            <div className="relative bg-white border-4 border-black shadow-[8px_8px_0_0_rgba(0,0,0,1)] w-full max-w-md mx-4">
                {/* Header */}
                <div className="bg-black text-white p-4 flex justify-between items-center">
                    <h2 className="text-lg font-bold uppercase tracking-widest">
                        {isEditMode ? '编辑章节' : '添加章节'}
                    </h2>
                    <button
                        onClick={onClose}
                        className="text-white hover:text-gray-300 transition-colors"
                    >
                        <span className="material-symbols-outlined">close</span>
                    </button>
                </div>

                {/* Form */}
                <form onSubmit={handleSubmit} className="p-6 space-y-4">
                    {/* Title */}
                    <div>
                        <label className="block text-xs font-bold uppercase mb-2">
                            标题 <span className="text-red-500">*</span>
                        </label>
                        <input
                            type="text"
                            value={title}
                            onChange={(e) => handleTitleChange(e.target.value)}
                            className={`w-full border-2 p-2 text-sm font-mono focus:outline-none focus:ring-0 ${errors.title ? 'border-red-500' : 'border-black'
                                }`}
                            placeholder="例如：本周工作总结"
                        />
                        {errors.title && (
                            <p className="text-red-500 text-xs mt-1">{errors.title}</p>
                        )}
                    </div>

                    {/* Section Key */}
                    <div>
                        <label className="block text-xs font-bold uppercase mb-2">
                            唯一标识 <span className="text-red-500">*</span>
                            {isEditMode && <span className="text-gray-400 ml-2">(不可修改)</span>}
                        </label>
                        <input
                            type="text"
                            value={sectionKey}
                            onChange={(e) => setSectionKey(e.target.value)}
                            disabled={isEditMode}
                            className={`w-full border-2 p-2 text-sm font-mono focus:outline-none focus:ring-0 ${errors.sectionKey ? 'border-red-500' : 'border-black'
                                } ${isEditMode ? 'bg-gray-100 text-gray-500' : ''}`}
                            placeholder="例如：weekly_summary"
                        />
                        {errors.sectionKey && (
                            <p className="text-red-500 text-xs mt-1">{errors.sectionKey}</p>
                        )}
                        {!isEditMode && (
                            <p className="text-gray-400 text-[10px] mt-1">
                                用于标识章节，会自动根据标题生成
                            </p>
                        )}
                    </div>

                    {/* Section Type */}
                    <div>
                        <label className="block text-xs font-bold uppercase mb-2">
                            章节类型
                        </label>
                        <div className="flex gap-2">
                            {SECTION_TYPES.map((type) => (
                                <button
                                    key={type.value}
                                    type="button"
                                    onClick={() => setSectionType(type.value)}
                                    className={`flex-1 border-2 border-black p-2 text-xs font-bold transition-colors ${sectionType === type.value
                                            ? 'bg-black text-white'
                                            : 'bg-white text-black hover:bg-gray-100'
                                        }`}
                                >
                                    {type.label}
                                </button>
                            ))}
                        </div>
                    </div>

                    {/* Actions */}
                    <div className="flex gap-2 pt-4 border-t border-gray-200">
                        <BrutalButton
                            type="button"
                            variant="secondary"
                            onClick={onClose}
                            className="flex-1"
                        >
                            取消
                        </BrutalButton>
                        <BrutalButton
                            type="submit"
                            variant="primary"
                            className="flex-1"
                        >
                            {isEditMode ? '保存' : '添加'}
                        </BrutalButton>
                    </div>
                </form>
            </div>
        </div>
    );
};
