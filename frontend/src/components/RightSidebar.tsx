"use client";

import { useReport } from "@/lib/store";
import { useState } from "react";

export default function RightSidebar() {
  const { state, actions } = useReport();
  const [searchInput, setSearchInput] = useState("");

  const handleSearch = (e: React.KeyboardEvent) => {
    if (e.key === "Enter") {
      actions.searchReferences(searchInput);
    }
  };

  return (
    <aside className="w-80 flex-none border-l-4 border-black flex flex-col bg-white overflow-hidden">
      <div className="p-3 border-b-2 border-black bg-white flex justify-between items-center">
        <span className="text-xs font-bold uppercase tracking-widest">
          历史与AI辅助
        </span>
      </div>

      <div className="flex-1 overflow-y-auto p-4 space-y-6 custom-scrollbar">

        {/* AI Suggestion (Mock for now) */}
        <div className="space-y-2">
          <div className="text-[10px] font-bold uppercase text-ai-accent flex items-center justify-between">
            <span>AI_建议 // 智能补全</span>
            <span className="material-symbols-outlined text-[12px] animate-pulse">
              psychology
            </span>
          </div>
          <div className="border-2 border-ai-accent p-0 relative">
            <div className="bg-ai-accent text-white p-1 px-2 text-[10px] font-bold flex justify-between">
              <span>风格统一建议</span>
              <span>置信度: 98%</span>
            </div>
            <div className="p-2 text-[10px] leading-tight font-mono bg-blue-50">
              <div className="mb-1 text-gray-500">
                // 基于当前内容
              </div>
              <div className="text-black">
                检测到您使用了非正式用语。建议修改为：
              </div>
              <div className="mt-2 p-2 border border-blue-200 bg-white text-ai-accent font-bold">
                "本季度营收稳步增长，环比上升15%。"
              </div>
            </div>
          </div>
        </div>

        {/* Reference Search */}
        <div className="space-y-2 pt-4 border-t-2 border-gray-200">
          <div className="text-[10px] font-bold uppercase text-gray-500">
            搜索归档资料
          </div>
          <div className="flex gap-2">
            <input
              className="w-full border-2 border-black p-2 text-xs focus:ring-0 uppercase font-bold placeholder-gray-400"
              placeholder="按标题搜索..."
              type="text"
              value={searchInput}
              onChange={(e) => setSearchInput(e.target.value)}
              onKeyDown={handleSearch}
            />
            <button
              onClick={() => actions.searchReferences(searchInput)}
              className="px-2 bg-black text-white"
            >
              <span className="material-symbols-outlined text-sm">search</span>
            </button>
          </div>
        </div>

        {/* Search Results */}
        <div className="space-y-4">
          {state.historicalSections.map((ref) => (
            <div key={ref.id} className="border-2 border-black p-0 opacity-80 hover:opacity-100 transition-opacity">
              <div className="bg-gray-200 p-1 px-2 text-[10px] font-bold flex justify-between border-b border-black">
                <span className="truncate max-w-[150px]">{ref.sectionTitle}</span>
                <span>{new Date(ref.createdAt).toLocaleDateString()}</span>
              </div>
              <div
                className="p-2 text-[10px] leading-tight font-mono whitespace-pre-wrap max-h-24 overflow-hidden"
                dangerouslySetInnerHTML={{ __html: ref.contentHtml }}
              />
              <button
                onClick={() => {
                  if (state.activeSectionId) {
                    actions.applyReference(ref.id, state.activeSectionId);
                  } else {
                    alert('Please select a section on the left first');
                  }
                }}
                className="w-full border-t border-black p-1 text-[10px] font-bold hover:bg-black hover:text-white transition-colors"
              >
                [ Apply to Current ]
              </button>
            </div>
          ))}

          {state.historicalSections.length === 0 && searchInput && (
            <div className="text-center text-xs text-gray-400 py-4">
              No references found
            </div>
          )}
        </div>

        {/* Resources Link */}
        <div className="pt-4 border-t-2 border-black">
          <div className="text-[10px] font-bold uppercase mb-3">Resources</div>
          <div className="space-y-1">
            <div className="border border-black p-2 flex items-center justify-between text-[10px] hover:bg-gray-100 cursor-pointer">
              <span className="font-bold">Q4_Chart.PNG</span>
              <span>[2.4MB]</span>
            </div>
          </div>
        </div>

      </div>

      <div className="p-3 bg-black text-white text-[9px] font-mono border-t-4 border-black flex justify-between">
        <span>TERMINAL CONNECTED // LOCALHOST:8080</span>
        <span className="text-green-400 animate-pulse">●</span>
      </div>
    </aside>
  );
}
