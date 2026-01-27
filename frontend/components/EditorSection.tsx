
import React from 'react';
import { ReportSection } from '../types';

interface EditorSectionProps {
  section: ReportSection;
  onUpdate: (content: string) => void;
  onDelete: () => void;
  isActive: boolean;
  onFocus: () => void;
}

export const EditorSection: React.FC<EditorSectionProps> = ({
  section,
  onUpdate,
  onDelete,
  isActive,
  onFocus
}) => {
  return (
    <div 
      className={`border-2 border-black transition-all ${isActive ? 'shadow-[8px_8px_0px_0px_rgba(0,0,0,1)]' : 'opacity-80 hover:opacity-100'}`}
      onClick={onFocus}
    >
      <div className={`bg-black text-white px-4 py-2 flex justify-between items-center ${!isActive && 'opacity-60'}`}>
        <span className="text-[10px] font-bold uppercase tracking-widest">
          原子单元: {section.title} / {section.id.toUpperCase()}.MD
        </span>
        {isActive && (
          <div className="flex gap-4 text-[10px] font-bold">
            <button className="hover:underline underline-offset-4">保存</button>
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
          <button className="px-3 py-1 border-r border-black hover:bg-black hover:text-white uppercase">加粗</button>
          <button className="px-3 py-1 border-r border-black hover:bg-black hover:text-white uppercase">斜体</button>
          <button className="px-3 py-1 border-r border-black hover:bg-black hover:text-white uppercase">H1</button>
          <button className="px-3 py-1 border-r border-black hover:bg-black hover:text-white uppercase">H2</button>
          <button className="px-3 py-1 border-r border-black hover:bg-black hover:text-white uppercase">引用</button>
          <button className="px-3 py-1 border-r border-black hover:bg-black hover:text-white uppercase">代码</button>
        </div>
      )}

      <textarea
        className={`w-full ${isActive ? 'h-64' : 'h-16'} p-6 font-mono text-sm border-none focus:ring-0 markdown-dot-bg leading-relaxed resize-none`}
        spellCheck={false}
        value={section.content}
        onChange={(e) => onUpdate(e.target.value)}
        placeholder={`正在编辑 ${section.title}...`}
      />
    </div>
  );
};
