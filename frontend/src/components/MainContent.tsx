"use client";

import { useReport } from "@/lib/store";
import TiptapEditor from "./TiptapEditor";
import SectionTitleInput from "./SectionTitleInput";
import { useEffect, useRef } from "react";
import { cn } from "@/lib/utils";

export default function MainContent() {
  const { state, actions } = useReport();
  const sectionRefs = useRef<Record<number, HTMLDivElement | null>>({});

  // Scroll to active section
  useEffect(() => {
    if (state.activeSectionId && sectionRefs.current[state.activeSectionId]) {
      sectionRefs.current[state.activeSectionId]?.scrollIntoView({
        behavior: "smooth",
        block: "start",
      });
    }
  }, [state.activeSectionId]);

  return (
    <main className="flex-1 overflow-y-auto bg-white p-8 custom-scrollbar relative">
      <div className="max-w-3xl mx-auto space-y-12">

        {/* Report Metadata */}
        <div className="border-2 border-black p-4 bg-gray-50 mb-8">
          <div className="text-[10px] text-gray-500 mb-2">
            --- (YAML Metadata) ---
          </div>
          <div className="grid grid-cols-[100px_1fr] gap-2 text-xs">
            <span className="font-bold">Title:</span>
            <input
              className="bg-transparent border-none p-0 focus:ring-0 font-mono text-xs w-full"
              type="text"
              value={state.reportTitle}
              onChange={(e) => actions.setReportTitle(e.target.value)}
            />
            <span className="font-bold">Date:</span>
            <span>{new Date().toLocaleDateString()}</span>
            <span className="font-bold">Template:</span>
            <span>{state.currentTemplate?.name || "Generic_v1.0"}</span>
          </div>
          <div className="text-[10px] text-gray-500 mt-2">---</div>
        </div>

        {/* Dynamic Sections */}
        {state.sections.map((section, index) => (
          <div
            key={section.id}
            ref={el => { sectionRefs.current[section.id] = el }}
            className={cn(
              "transition-all duration-300",
              state.activeSectionId === section.id
                ? "border-2 border-black shadow-[8px_8px_0px_0px_rgba(0,0,0,1)]"
                : "border-2 border-black opacity-80 hover:opacity-100"
            )}
            onClick={() => actions.setActiveSection(section.id)}
          >
            {/* Editor Header */}
            <div className={cn(
              "px-4 py-2 flex justify-between items-center border-black",
              state.activeSectionId === section.id
                ? "bg-black text-white"
                : "bg-gray-200 border-b-2"
            )}>
              <div className="flex items-center gap-2 flex-1">
                <span className="text-xs font-bold whitespace-nowrap">
                  {index + 1}.
                </span>
                <SectionTitleInput
                  value={section.sectionTitle} // Start using sectionTitle from ReportContent
                  onUpdate={(val) => actions.updateSectionTitle(section.id, val)}
                  className={cn(
                    "text-xs font-bold w-full",
                    state.activeSectionId === section.id ? "text-white placeholder:text-white/50" : "text-black"
                  )}
                  placeholder="Section Title..."
                />
              </div>

              {state.activeSectionId === section.id && (
                <div className="flex gap-4 text-[10px] font-bold">
                  <span className="text-xs opacity-50">
                    {section.contentHtml?.length || 0} chars
                  </span>
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      // Assuming backend logic for 'save current as reference' is handled or we add 'Save to Library'
                      // state.actions.saveAsReference(section.id) is not implemented in new store yet
                      alert("Reference saving temporarily disabled");
                    }}
                    className="hover:underline underline-offset-4 text-green-400"
                  >
                    Save as Ref
                  </button>
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      if (confirm("Delete this section?")) {
                        actions.deleteSection(section.id);
                      }
                    }}
                    className="hover:text-red-400"
                  >
                    ✕
                  </button>
                </div>
              )}
            </div>

            {/* Formatting Toolbar (Only visible when active) */}
            {state.activeSectionId === section.id && (
              <div className="p-0 border-b-2 border-black flex items-center bg-gray-100 text-[10px] font-bold overflow-x-auto">
                <div className="px-3 py-1 border-r border-black hover:bg-black hover:text-white cursor-pointer">B</div>
                <div className="px-3 py-1 border-r border-black hover:bg-black hover:text-white cursor-pointer">H1</div>
                <div className="px-3 py-1 border-r border-black hover:bg-black hover:text-white cursor-pointer">H2</div>
                <div className="px-3 py-1 border-r border-black bg-ai-accent text-white hover:bg-blue-800 flex items-center gap-1 cursor-pointer">
                  <span className="material-symbols-outlined text-[10px]">auto_fix_high</span>
                  AI Polish
                </div>
              </div>
            )}

            {/* Editor Area */}
            <div className="w-full min-h-[16rem] bg-white text-black">
              <TiptapEditor
                content={section.contentHtml}
                onChange={(html) => actions.updateSection(section.id, html)}
                onFocus={() => actions.setActiveSection(section.id)}
              />
            </div>
          </div>
        ))}

        {/* Empty State / Add Button */}
        <div className="text-center p-8">
          <button
            onClick={() => actions.addSection("New Section")}
            className="bg-black text-white px-6 py-3 text-sm font-bold hover:bg-gray-800 shadow-[4px_4px_0px_0px_rgba(0,0,0,0.5)] active:shadow-none active:translate-x-[2px] active:translate-y-[2px] transition-all"
          >
            + Add Section
          </button>
        </div>

        {/* Excel Data Panel (Floating if data exists) */}
        {state.excelData && (
          <div className="border-2 border-black p-4 bg-gray-50 mt-8">
            <div className="flex justify-between items-center mb-4">
              <div className="text-[10px] font-bold uppercase tracking-tighter">
                Excel Data: {state.excelData[0]?.sheetName}
              </div>
              <button
                onClick={actions.clearExcelData}
                className="text-xs hover:text-red-500"
              >
                ✕ Close
              </button>
            </div>
            <div className="text-[12px] whitespace-pre font-mono bg-black text-green-400 p-4 border border-black overflow-x-auto max-h-64">
              <table className="w-full border-collapse">
                <thead>
                  <tr>
                    {state.excelData[0]?.headers.map((h, i) => (
                      <th key={i} className="border border-green-800 px-2 py-1 text-left">{h}</th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {(state.excelData && state.excelData[0]) ? state.excelData[0].rows.slice(0, 10).map((row, i) => (
                    <tr key={i}>
                      {state.excelData![0].headers.map((h, j) => (
                        <td key={j} className="border border-green-900 px-2 py-1">{row[h]}</td>
                      ))}
                    </tr>
                  )) : null}
                </tbody>
              </table>
            </div>
          </div>
        )}

      </div>
    </main>
  );
}
