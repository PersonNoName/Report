"use client";

import { useReport } from "@/lib/store";
import { cn } from "@/lib/utils";

export default function LeftSidebar() {
  const { state, actions } = useReport();

  return (
    <aside className="w-64 flex-none border-r-4 border-black flex flex-col bg-white">
      <div className="p-3 border-b-2 border-black bg-black text-white flex justify-between items-center">
        <span className="text-xs font-bold uppercase tracking-widest">
          大纲_树状图
        </span>
        <span className="material-symbols-outlined text-sm">folder_open</span>
      </div>

      <div className="flex-1 overflow-y-auto p-2 text-xs font-medium uppercase leading-loose custom-scrollbar">
        {/* Sections List */}
        {state.sections.map((section) => (
          <div
            key={section.id}
            onClick={() => actions.setActiveSection(section.id)}
            className={cn(
              "flex items-center gap-2 px-2 py-1 cursor-pointer transition-colors",
              state.activeSectionId === section.id
                ? "bg-black text-white"
                : "hover:bg-gray-200"
            )}
          >
            <span>└─</span>
            <span className="material-symbols-outlined text-[14px]">
              {section.isReference ? "bookmark" : "description"}
            </span>
            <span className="truncate">{section.title || "未命名章节"}</span>
            {state.activeSectionId === section.id && (
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  if (confirm('确定删除此章节吗？')) actions.deleteSection(section.id);
                }}
                className="ml-auto hover:text-red-500"
              >
                ✕
              </button>
            )}
          </div>
        ))}

        {/* Add New Section */}
        <div
          onClick={() => actions.addSection("新章节")}
          className="flex items-center gap-2 px-2 py-1 hover:bg-gray-200 cursor-pointer opacity-50 mt-2"
        >
          <span>└─</span>
          <span className="material-symbols-outlined text-[14px]">add_box</span>
          <span>新建章节</span>
        </div>
      </div>

      <div className="p-4 border-t-2 border-black">
        <div className="bg-gray-100 p-2 border border-black text-[10px]">
          <div className="font-bold border-b border-black mb-1">系统信息</div>
          <div>用户: S_JENKINS</div>
          <div>模板: {state.currentTemplate?.name || "自定义"}</div>
          <div>章节: {state.sections.length}</div>
          <div className="mt-1 text-ai-accent font-bold">
            API: {state.error ? "离线" : "已连接"}
          </div>
        </div>
      </div>
    </aside>
  );
}
