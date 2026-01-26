"use client";

import { useReport } from "@/lib/store";
import { useRef } from "react";

export default function Header() {
  const { state, actions } = useReport();
  const excelInputRef = useRef<HTMLInputElement>(null);
  const wordInputRef = useRef<HTMLInputElement>(null);



  const handleExcelUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      await actions.uploadExcel(file);
    }
    e.target.value = "";
  };

  const handleWordUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      await actions.uploadWord(file);
    }
    e.target.value = "";
  };

  const handleExport = async () => {
    await actions.exportWord();
  };

  return (
    <header className="h-12 flex-none border-b-4 border-black px-4 flex items-center justify-between z-20 bg-white">
      <div className="flex items-center gap-4">
        <input
          type="text"
          value={state.reportTitle}
          onChange={(e) => actions.setReportTitle(e.target.value)}
          className="font-bold text-lg tracking-tighter bg-transparent border-none focus:outline-none focus:ring-0 w-64"
        />
        <div className="h-4 w-px bg-black"></div>
        <div className="text-[10px] flex gap-4 uppercase font-bold">
          <span>状态: {state.isLoading ? "加载中..." : "已保存"}</span>
          <span>编码: UTF-8</span>
          <span className="text-accent">模式: 原始编辑</span>
          <span className="text-ai-accent flex items-center gap-1">
            <span className="material-symbols-outlined text-[10px]">
              smart_toy
            </span>{" "}
            AI: 在线
          </span>
        </div>
      </div>

      {/* Hidden file inputs */}
      <input
        ref={excelInputRef}
        type="file"
        accept=".xlsx,.xls"
        onChange={handleExcelUpload}
        className="hidden"
      />
      <input
        ref={wordInputRef}
        type="file"
        accept=".docx,.doc"
        onChange={handleWordUpload}
        className="hidden"
      />

      <div className="flex items-center gap-px bg-black border border-black">
        <button
          onClick={() => excelInputRef.current?.click()}
          disabled={state.isLoading}
          className="bg-white hover:bg-black hover:text-white px-3 py-1 text-xs font-bold transition-colors border-r border-black disabled:opacity-50"
        >
          [ Import Excel ]
        </button>
        <button
          onClick={() => wordInputRef.current?.click()}
          disabled={state.isLoading}
          className="bg-white hover:bg-black hover:text-white px-3 py-1 text-xs font-bold transition-colors border-r border-black disabled:opacity-50"
        >
          [ Import Word Ref ]
        </button>
        <button
          onClick={handleExport}
          disabled={state.isLoading || state.sections.length === 0}
          className="bg-black text-white hover:bg-white hover:text-black px-4 py-1 text-xs font-bold transition-colors disabled:opacity-50"
        >
          [ Export DOCX ]
        </button>
      </div>

      {/* Error toast */}
      {state.error && (
        <div className="absolute top-14 right-4 bg-red-500 text-white px-4 py-2 text-xs font-bold flex items-center gap-2">
          <span>{state.error}</span>
          <button onClick={actions.clearError} className="hover:underline">
            ✕
          </button>
        </div>
      )}
    </header>
  );
}
