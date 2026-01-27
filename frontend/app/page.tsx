
'use client';

import React, { useState, useMemo } from 'react';
import { ReportSection, ReportMetadata, HistoricalFragment, LinkedAsset } from '../types';
import { BrutalButton } from '../components/BrutalButton';
import { EditorSection } from '../components/EditorSection';
import { generateSectionContent } from '../services/gemini';

const INITIAL_SECTIONS: ReportSection[] = [
  {
    id: '01_rev',
    title: '收入概览',
    content: '# 1. 收入概览\n2023年 **第四季度** 总收入达到 `$45.2M`，同比增长 `15%`。\n- 企业级软件部门扩张显著\n- 贡献了总收入的 62%',
    type: 'markdown',
    lastModified: new Date().toISOString()
  },
  {
    id: '02_cost',
    title: '成本分析',
    content: '# 2. 成本分析\n由于供应链优化，本季度运营成本下降了 `4.5%`。',
    type: 'markdown',
    lastModified: new Date().toISOString()
  }
];

const HIST_FRAGS: HistoricalFragment[] = [
  { id: 'h1', title: '23Q3_存档', content: '上季度总收入为 $39.5M，主要受产品X驱动。', matchScore: 95, source: 'Git/Archive', relatedTo: '收入概览' },
  { id: 'h2', title: '22年报_最终', content: '2022年底企业级客户留存率为 88%。', matchScore: 40, source: 'ERP/Reports', relatedTo: '收入概览' },
  { id: 'h3', title: '23Q2_成本核算', content: '云服务托管费用环比增长 2%。', matchScore: 82, source: 'Fin/Audit', relatedTo: '成本分析' }
];

const ASSETS: LinkedAsset[] = [
  { name: 'Q4_财务报表.PDF', size: '1.2MB', type: 'pdf' },
  { name: '支出明细.CSV', size: '45KB', type: 'csv' }
];

export default function Home() {
  const [sections, setSections] = useState<ReportSection[]>(INITIAL_SECTIONS);
  const [activeId, setActiveId] = useState<string>(INITIAL_SECTIONS[0].id);
  const [metadata, setMetadata] = useState<ReportMetadata>({
    title: '2023第四季度财务分析',
    version: 'V1.0.2-FINAL',
    author: '分析团队',
    date: '2023-12-20'
  });
  const [isAiLoading, setIsAiLoading] = useState(false);

  const activeSection = useMemo(() => sections.find(s => s.id === activeId), [sections, activeId]);
  
  // 核心功能：根据当前主题筛选历史记录
  const filteredHistory = useMemo(() => {
    if (!activeSection) return [];
    return HIST_FRAGS.filter(f => f.relatedTo === activeSection.title);
  }, [activeSection]);

  const addSection = () => {
    const id = `sec_${Date.now()}`;
    setSections([...sections, { id, title: '新章节', content: '', type: 'markdown', lastModified: new Date().toISOString() }]);
    setActiveId(id);
  };

  const handleAiAssist = async () => {
    if (!activeSection) return;
    setIsAiLoading(true);
    const suggestion = await generateSectionContent(activeSection.title, activeSection.content);
    setSections(prev => prev.map(s => s.id === activeId ? { ...s, content: s.content + '\n\n' + suggestion } : s));
    setIsAiLoading(false);
  };

  return (
    <div className="flex flex-col h-full">
      {/* 顶部工具栏 */}
      <header className="h-12 border-b-4 border-black px-4 flex items-center justify-between bg-white z-10">
        <div className="flex items-center gap-6">
          <span className="font-bold text-lg">PROJ_{metadata.title.toUpperCase()}.MD</span>
          <div className="text-[10px] font-bold flex gap-4 text-gray-500">
            <span className="text-green-600">● 实时同步中</span>
            <span>版本: {metadata.version}</span>
            <span>编码: UTF-8</span>
          </div>
        </div>
        <div className="flex gap-px bg-black">
          <BrutalButton variant="secondary" className="border-r border-black">导入</BrutalButton>
          <BrutalButton>导出最终报告</BrutalButton>
        </div>
      </header>

      <div className="flex flex-1 overflow-hidden">
        {/* 左侧：结构导航 */}
        <aside className="w-60 border-r-4 border-black flex flex-col bg-white">
          <div className="p-3 border-b-2 border-black bg-black text-white text-xs font-bold tracking-widest uppercase">
            报告目录结构
          </div>
          <div className="flex-1 overflow-y-auto p-2">
            {sections.map(s => (
              <div 
                key={s.id}
                onClick={() => setActiveId(s.id)}
                className={`p-2 mb-1 cursor-pointer text-[10px] font-bold uppercase flex items-center gap-2 ${activeId === s.id ? 'bg-black text-white' : 'hover:bg-gray-100'}`}
              >
                <span className="material-symbols-outlined text-sm">sticky_note_2</span>
                {s.title}
              </div>
            ))}
            <div onClick={addSection} className="p-2 mt-4 border-2 border-dashed border-black text-[10px] font-bold text-center cursor-pointer hover:bg-gray-50">
              + 新增原子单元
            </div>
          </div>
          <div className="p-4 border-t-2 border-black bg-gray-50 text-[10px]">
            <div className="font-bold mb-1 underline">当前状态统计</div>
            <div>字符数: {sections.reduce((a, b) => a + b.content.length, 0)}</div>
            <div>单元数: {sections.length}</div>
          </div>
        </aside>

        {/* 中间：主编辑区 */}
        <main className="flex-1 overflow-y-auto p-12 bg-white">
          <div className="max-w-2xl mx-auto space-y-12">
            {/* YAML 配置区 */}
            <div className="border-2 border-black p-4 bg-gray-100 relative">
              <div className="absolute -top-3 left-4 bg-white px-2 border-2 border-black text-[10px] font-bold">YAML_METADATA</div>
              <div className="grid grid-cols-[80px_1fr] gap-y-2 text-xs font-mono">
                <span className="font-bold">TITLE:</span>
                <input className="bg-transparent border-none p-0 focus:ring-0 text-xs w-full" value={metadata.title} onChange={e => setMetadata({...metadata, title: e.target.value})} />
                <span className="font-bold">AUTHOR:</span>
                <input className="bg-transparent border-none p-0 focus:ring-0 text-xs w-full" value={metadata.author} onChange={e => setMetadata({...metadata, author: e.target.value})} />
                <span className="font-bold">VERSION:</span>
                <span>{metadata.version}</span>
              </div>
            </div>

            {/* 章节列表 */}
            {sections.map(s => (
              <EditorSection 
                key={s.id} 
                section={s} 
                isActive={activeId === s.id} 
                onFocus={() => setActiveId(s.id)}
                onUpdate={c => setSections(prev => prev.map(x => x.id === s.id ? {...x, content: c} : x))}
                onDelete={() => setSections(prev => prev.filter(x => x.id !== s.id))}
              />
            ))}
          </div>
        </main>

        {/* 右侧：历史对应主题记录 */}
        <aside className="w-80 border-l-4 border-black bg-white flex flex-col">
          <div className="p-3 border-b-2 border-black flex justify-between items-center bg-gray-50">
            <span className="text-xs font-bold uppercase tracking-widest">历史对应主题记录</span>
            <span className="material-symbols-outlined text-sm cursor-pointer hover:text-red-500" onClick={handleAiAssist}>psychology</span>
          </div>

          <div className="flex-1 overflow-y-auto p-4 space-y-4">
            <div className="text-[10px] font-bold text-gray-400 mb-2 uppercase">当前主题: {activeSection?.title || '未选择'}</div>
            
            {isAiLoading ? (
              <div className="border-2 border-black p-8 text-center animate-pulse text-[10px] font-bold">
                AI 正在检索历史档案...
              </div>
            ) : filteredHistory.length > 0 ? (
              filteredHistory.map(h => (
                <div key={h.id} className="border-2 border-black group">
                  <div className="bg-black text-white p-1 px-2 text-[9px] font-bold flex justify-between italic">
                    <span>{h.title}</span>
                    <span className="text-green-400">匹配: {h.matchScore}%</span>
                  </div>
                  <div className="p-2 text-[10px] leading-relaxed bg-gray-50 group-hover:bg-white transition-colors italic">
                    "{h.content}"
                  </div>
                  <div className="border-t border-black p-1 px-2 text-[8px] flex justify-between items-center bg-white font-bold">
                    <span>来源: {h.source}</span>
                    <button className="underline hover:no-underline">引用</button>
                  </div>
                </div>
              ))
            ) : (
              <div className="border-2 border-dashed border-gray-300 py-12 text-center">
                <span className="material-symbols-outlined text-gray-300 mb-2 block">history_toggle_off</span>
                <p className="text-[10px] text-gray-400 font-bold">暂无对应主题的历史记录</p>
              </div>
            )}

            <div className="pt-6 border-t-2 border-black mt-8">
              <div className="text-[10px] font-bold uppercase mb-3 flex items-center justify-between">
                <span>关联附件资产</span>
                <span className="material-symbols-outlined text-xs">attach_file</span>
              </div>
              {ASSETS.map(a => (
                <div key={a.name} className="border border-black p-2 mb-1 flex justify-between items-center hover:bg-black hover:text-white cursor-pointer group">
                  <span className="text-[10px] font-bold">{a.name}</span>
                  <span className="text-[8px] opacity-50 group-hover:opacity-100">{a.size}</span>
                </div>
              ))}
            </div>
          </div>

          <div className="p-2 bg-black text-white text-[9px] font-bold flex justify-between">
            <span>系统状态: 正常</span>
            <span className="animate-ping">_</span>
          </div>
        </aside>
      </div>
    </div>
  );
}
