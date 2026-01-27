
import React, { useState, useMemo } from 'react';
import { ReportSection, ReportMetadata, HistoricalFragment, LinkedAsset } from './types';
import { BrutalButton } from './components/BrutalButton';
import { EditorSection } from './components/EditorSection';
import { generateSectionContent } from './services/geminiService';

const INITIAL_SECTIONS: ReportSection[] = [
  {
    id: '01_revenue_overview',
    title: '收入概览',
    content: '# 1. 收入概览\n2023年 **第四季度** 总收入达到 `$45.2M`，同比增长 `15%`。\n- 企业级软件部门扩张\n- 贡献了总收入的 60%\n> EBITDA 利润率保持在 22% 稳定水平，符合财务预期。',
    type: 'markdown',
    lastModified: new Date().toISOString()
  },
  {
    id: '02_cost_analysis',
    title: '成本分析',
    content: '# 2. 成本分析\n运营成本及战略性研发支出的详细分析。',
    type: 'markdown',
    lastModified: new Date().toISOString()
  }
];

// Simplified the type definition since HistoricalFragment now includes relatedTo
const ALL_HISTORICAL_FRAGMENTS: HistoricalFragment[] = [
  {
    id: 'h1',
    title: '2023_Q3_收入快照.MD',
    content: '...上季度收入为 $39.5M。主要增长动力是产品 X 的发布...',
    matchScore: 92,
    source: 'Git: main/archive/q3',
    relatedTo: '收入概览'
  },
  {
    id: 'h2',
    title: '2022_年度报告_最终版.TXT',
    content: '年终收入总计超出预期 5%。企业级市场仍是我们最强的垂直领域...',
    matchScore: 45,
    source: 'ERP: reports/2022',
    relatedTo: '收入概览'
  },
  {
    id: 'h3',
    title: '2023_Q3_成本审计.MD',
    content: '服务器托管成本由于云迁移下降了 12%...',
    matchScore: 88,
    source: 'Confluence: IT/Costs',
    relatedTo: '成本分析'
  }
];

const LINKED_ASSETS: LinkedAsset[] = [
  { name: 'Q4_财务图表.PNG', size: '2.4MB', type: 'image' },
  { name: '支出明细表.CSV', size: '14KB', type: 'csv' }
];

const App: React.FC = () => {
  const [sections, setSections] = useState<ReportSection[]>(INITIAL_SECTIONS);
  const [metadata, setMetadata] = useState<ReportMetadata>({
    title: '2023年第四季度财务分析报告',
    version: '1.0.4-正式版',
    author: '詹金斯',
    date: '2023-12-15'
  });
  const [activeSectionId, setActiveSectionId] = useState<string>(INITIAL_SECTIONS[0].id);
  const [isAiLoading, setIsAiLoading] = useState(false);

  const activeSection = useMemo(() => sections.find(s => s.id === activeSectionId), [sections, activeSectionId]);

  // 根据当前激活的章节标题过滤历史参考
  const filteredFragments = useMemo(() => {
    if (!activeSection) return [];
    return ALL_HISTORICAL_FRAGMENTS.filter(frag => frag.relatedTo === activeSection.title);
  }, [activeSection]);

  const updateSection = (id: string, newContent: string) => {
    setSections(prev => prev.map(s => s.id === id ? { ...s, content: newContent, lastModified: new Date().toISOString() } : s));
  };

  const deleteSection = (id: string) => {
    if (sections.length <= 1) return;
    setSections(prev => prev.filter(s => s.id !== id));
    setActiveSectionId(sections[0].id === id ? sections[1].id : sections[0].id);
  };

  const addSection = () => {
    const newId = `section_${Date.now()}`;
    const newSection: ReportSection = {
      id: newId,
      title: '新增章节',
      content: '',
      type: 'markdown',
      lastModified: new Date().toISOString()
    };
    setSections(prev => [...prev, newSection]);
    setActiveSectionId(newId);
  };

  const handleAiAssist = async () => {
    if (!activeSection) return;
    setIsAiLoading(true);
    const suggestion = await generateSectionContent(activeSection.title, activeSection.content);
    updateSection(activeSectionId, activeSection.content + '\n\n' + suggestion);
    setIsAiLoading(false);
  };

  return (
    <div className="flex flex-col h-full overflow-hidden font-mono">
      {/* 顶部标题栏 */}
      <header className="h-12 flex-none border-b-4 border-black px-4 flex items-center justify-between z-20 bg-white">
        <div className="flex items-center gap-4">
          <span className="font-bold text-lg tracking-tighter">项目_{metadata.title.replace(/\s+/g, '_').toUpperCase()}.MD</span>
          <div className="h-4 w-px bg-black"></div>
          <div className="text-[10px] flex gap-4 uppercase font-bold">
            <span className="text-green-600">状态: 已保存</span>
            <span>编码: UTF-8</span>
            <span className="text-accent">模式: 原子化编辑</span>
          </div>
        </div>
        <div className="flex items-center gap-px bg-black border border-black h-8">
          <BrutalButton variant="secondary" className="border-r border-black h-full">导入源码</BrutalButton>
          <BrutalButton variant="primary" className="h-full">导出 Word 文档</BrutalButton>
        </div>
      </header>

      <div className="flex flex-1 overflow-hidden">
        {/* 左侧：目录树 */}
        <aside className="w-64 flex-none border-r-4 border-black flex flex-col bg-white">
          <div className="p-3 border-b-2 border-black bg-black text-white flex justify-between items-center">
            <span className="text-xs font-bold uppercase tracking-widest">目录结构</span>
            <span className="material-symbols-outlined text-sm">folder_open</span>
          </div>
          <div className="flex-1 overflow-y-auto p-2 text-[10px] font-bold uppercase leading-loose">
            {sections.map(section => (
              <div 
                key={section.id}
                onClick={() => setActiveSectionId(section.id)}
                className={`flex items-center gap-2 px-2 py-1 mb-1 cursor-pointer transition-colors ${activeSectionId === section.id ? 'bg-black text-white' : 'hover:bg-gray-200'}`}
              >
                <span>└─</span>
                <span className="material-symbols-outlined text-[14px]">description</span>
                <span className="truncate">{section.title}</span>
              </div>
            ))}
            <div 
              onClick={addSection}
              className="flex items-center gap-2 px-2 py-1 hover:bg-gray-200 cursor-pointer opacity-50 mt-4 border border-dashed border-black"
            >
              <span>└─</span>
              <span className="material-symbols-outlined text-[14px]">add_box</span>
              <span>新增章节</span>
            </div>
          </div>
          <div className="p-4 border-t-2 border-black">
            <div className="bg-gray-100 p-2 border border-black text-[10px]">
              <div className="font-bold border-b border-black mb-1">会话统计</div>
              <div>总字数: {sections.reduce((acc, s) => acc + s.content.length, 0)}</div>
              <div>片段数: {sections.length}</div>
            </div>
          </div>
        </aside>

        {/* 主编辑区 */}
        <main className="flex-1 overflow-y-auto bg-white p-8 space-y-12">
          <div className="max-w-3xl mx-auto space-y-12">
            {/* 元数据区块 */}
            <div className="border-2 border-black p-4 bg-gray-50 mb-8">
              <div className="text-[10px] text-gray-500 mb-2 font-bold italic">--- (YAML 元数据) ---</div>
              <div className="grid grid-cols-[100px_1fr] gap-2 text-xs">
                <span className="font-bold">标题 (TITLE):</span>
                <input 
                  className="bg-transparent border-none p-0 focus:ring-0 font-mono text-xs w-full font-bold" 
                  value={metadata.title}
                  onChange={(e) => setMetadata({...metadata, title: e.target.value})}
                />
                <span className="font-bold">版本 (VERSION):</span>
                <span>{metadata.version}</span>
                <span className="font-bold">作者 (AUTHOR):</span>
                <span>{metadata.author}</span>
              </div>
              <div className="text-[10px] text-gray-500 mt-2 font-bold italic">---</div>
            </div>

            {/* 章节编辑器列表 */}
            {sections.map(section => (
              <EditorSection
                key={section.id}
                section={section}
                isActive={activeSectionId === section.id}
                onUpdate={(content) => updateSection(section.id, content)}
                onDelete={() => deleteSection(section.id)}
                onFocus={() => setActiveSectionId(section.id)}
              />
            ))}
          </div>
        </main>

        {/* 右侧：历史参考与资产 */}
        <aside className="w-80 flex-none border-l-4 border-black flex flex-col bg-white overflow-hidden">
          <div className="p-3 border-b-2 border-black bg-white flex justify-between items-center">
            <span className="text-xs font-bold uppercase tracking-widest">历史记录参考</span>
            <div className="flex gap-2">
              <span 
                className="material-symbols-outlined text-sm cursor-pointer hover:text-accent" 
                onClick={handleAiAssist}
                title="AI 辅助生成"
              >
                psychology
              </span>
            </div>
          </div>
          
          <div className="flex-1 overflow-y-auto p-4 space-y-6">
            <div className="space-y-2">
              <div className="text-[10px] font-bold uppercase text-gray-500">搜索存档库 (Archived)</div>
              <input 
                className="w-full border-2 border-black p-2 text-xs focus:ring-0 uppercase font-bold placeholder-gray-400" 
                placeholder="键入关键词进行搜索..." 
              />
            </div>

            {isAiLoading ? (
              <div className="border-2 border-black p-4 text-center text-[10px] animate-pulse">
                AI 正在思考并提取历史记录...
              </div>
            ) : filteredFragments.length > 0 ? (
              filteredFragments.map(frag => (
                <div key={frag.id} className="border-2 border-black p-0 group mb-4">
                  <div className="bg-black text-white p-1 px-2 text-[10px] font-bold flex justify-between">
                    <span>{frag.title}</span>
                    <span className={frag.matchScore > 80 ? 'text-green-400' : 'text-yellow-400'}>
                      匹配度: {frag.matchScore}%
                    </span>
                  </div>
                  <div className="p-2 text-[10px] leading-tight font-mono bg-gray-50">
                    <div className="text-gray-400 text-[9px] mb-1 italic">来源: {frag.source}</div>
                    <div className="text-gray-600 italic">"{frag.content}"</div>
                  </div>
                  <button className="w-full border-t border-black p-1 text-[10px] font-bold hover:bg-black hover:text-white transition-colors">
                    [ 引用此片段 ]
                  </button>
                </div>
              ))
            ) : (
              <div className="text-center py-10 opacity-40 border-2 border-dashed border-black">
                <span className="material-symbols-outlined block mb-2">find_in_page</span>
                <span className="text-[10px] font-bold">无对应历史记录</span>
              </div>
            )}

            <div className="pt-4 border-t-2 border-black">
              <div className="text-[10px] font-bold uppercase mb-3 flex justify-between">
                <span>关联附件资产</span>
                <span className="material-symbols-outlined text-sm">attachment</span>
              </div>
              <div className="space-y-1">
                {LINKED_ASSETS.map(asset => (
                  <div key={asset.name} className="border border-black p-2 flex items-center justify-between text-[10px] hover:bg-gray-100 cursor-pointer group">
                    <span className="font-bold group-hover:underline">{asset.name}</span>
                    <span className="opacity-50">[{asset.size}]</span>
                  </div>
                ))}
              </div>
            </div>
          </div>

          <div className="p-3 bg-black text-white text-[9px] font-mono border-t-4 border-black flex justify-between">
            <span>终端已连接</span>
            <span className="animate-pulse">●</span>
          </div>
        </aside>
      </div>
    </div>
  );
};

export default App;