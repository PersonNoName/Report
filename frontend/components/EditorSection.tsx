import React, { useState } from 'react';
import { ReportSection } from '../types';
import { polishContent } from '../services/api';

interface EditorSectionProps {
  section: ReportSection;
  onUpdate: (content: string) => void;
  onDelete: () => void;
  onSave?: () => void;
  isActive: boolean;
  onFocus: () => void;
}

export const EditorSection: React.FC<EditorSectionProps> = ({
  section,
  onUpdate,
  onDelete,
  onSave,
  isActive,
  onFocus
}) => {
  const containerRef = React.useRef<HTMLDivElement>(null);
  const textareaRef = React.useRef<HTMLTextAreaElement>(null);
  const [isPolishing, setIsPolishing] = useState(false);

  // Auto-scroll to center when active
  React.useEffect(() => {
    if (isActive && containerRef.current) {
      containerRef.current.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
  }, [isActive]);

  // Auto-resize textarea
  const adjustHeight = React.useCallback(() => {
    const el = textareaRef.current;
    if (el && isActive) {
      el.style.height = 'auto';
      el.style.height = el.scrollHeight + 'px';
    }
  }, [isActive]);

  // Adjust height on content change and active state change
  React.useLayoutEffect(() => {
    adjustHeight();
  }, [section.content, isActive, adjustHeight]);

  const handlePolish = async () => {
    if (!section.content.trim()) return;

    setIsPolishing(true);
    try {
      const polished = await polishContent(section.content);
      onUpdate(polished);
    } catch (error) {
      console.error('Failed to polish content:', error);
      alert('润色失败，请稍后重试');
    } finally {
      setIsPolishing(false);
    }
  };

  return (
    <div
      ref={containerRef}
      className={`border-2 border-black transition-all ${isActive ? 'shadow-[8px_8px_0px_0px_rgba(0,0,0,1)]' : 'opacity-80 hover:opacity-100'}`}
      onClick={onFocus}
    >
      <div className={`bg-black text-white px-4 py-2 flex justify-between items-center ${!isActive && 'opacity-60'}`}>
        <span className="text-[10px] font-bold uppercase tracking-widest">
          原子单元: {section.title} / {section.id.toUpperCase()}.MD
        </span>
        {isActive && (
          <div className="flex gap-4 text-[10px] font-bold">
            <button
              className="hover:underline underline-offset-4"
              onClick={(e) => { e.stopPropagation(); onSave?.(); }}
            >
              保存
            </button>
            <button
              className="hover:underline underline-offset-4 text-red-500"
              onClick={(e) => { e.stopPropagation(); onDelete(); }}
            >
              移除章节
            </button>
          </div>
        )}
      </div>

      {isActive && (
        <div className="p-0 border-b-2 border-black flex items-center bg-gray-100 text-[10px] font-bold overflow-x-auto">
          <button
            className={`px-3 py-1 border-r border-black hover:bg-black hover:text-white uppercase flex items-center gap-1 ${isPolishing ? 'cursor-wait opacity-50' : ''}`}
            onClick={handlePolish}
            disabled={isPolishing}
          >
            {isPolishing ? (
              <span className="animate-spin text-[10px]">◐</span>
            ) : (
              <span className="material-symbols-outlined text-[10px]">auto_awesome</span>
            )}
            AI 润色
          </button>
          <button className="px-3 py-1 border-r border-black hover:bg-black hover:text-white uppercase">加粗</button>
          <button className="px-3 py-1 border-r border-black hover:bg-black hover:text-white uppercase">斜体</button>
          <button className="px-3 py-1 border-r border-black hover:bg-black hover:text-white uppercase">H1</button>
          <button className="px-3 py-1 border-r border-black hover:bg-black hover:text-white uppercase">H2</button>
          <button className="px-3 py-1 border-r border-black hover:bg-black hover:text-white uppercase">引用</button>
          <button className="px-3 py-1 border-r border-black hover:bg-black hover:text-white uppercase">代码</button>
        </div>
      )}

      <textarea
        ref={textareaRef}
        className={`w-full ${isActive ? 'min-h-[16rem]' : 'h-16 cursor-pointer'} p-6 font-mono text-sm border-none focus:ring-0 markdown-dot-bg leading-relaxed resize-none overflow-hidden ${isPolishing ? 'opacity-50' : ''}`}
        spellCheck={false}
        value={section.content}
        onChange={(e) => {
          onUpdate(e.target.value);
          adjustHeight();
        }}
        placeholder={`正在编辑 ${section.title}...`}
        disabled={isPolishing}
      />
    </div>
  );
};
