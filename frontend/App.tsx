import React, { useState, useEffect, useMemo, useCallback } from 'react';
import type {
  ReportTemplate,
  TemplateSection,
  ReportInstance,
  ReportContent,
  ReferenceMaterial,
  ReportMetadata
} from './types';
import { BrutalButton } from './components/BrutalButton';
import { EditorSection } from './components/EditorSection';
import { TemplateSelectDialog } from './components/TemplateSelectDialog';
import { SectionFormDialog } from './components/SectionFormDialog';
import * as api from './services/api';

const App: React.FC = () => {
  // State
  const [templates, setTemplates] = useState<ReportTemplate[]>([]);
  const [sections, setSections] = useState<TemplateSection[]>([]);
  const [currentReport, setCurrentReport] = useState<ReportInstance | null>(null);
  const [contents, setContents] = useState<Record<string, ReportContent>>({});
  const [references, setReferences] = useState<ReferenceMaterial[]>([]);
  const [activeSectionKey, setActiveSectionKey] = useState<string>('');
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [searchKeyword, setSearchKeyword] = useState('');

  // Dialog states
  const [showTemplateDialog, setShowTemplateDialog] = useState(false);
  const [showSectionDialog, setShowSectionDialog] = useState(false);
  const [editingSection, setEditingSection] = useState<TemplateSection | null>(null);
  const [selectedTemplateId, setSelectedTemplateId] = useState<number | null>(null);

  const [metadata, setMetadata] = useState<ReportMetadata>({
    title: '新报告',
    version: 'v1.0',
    author: '编辑者',
    date: new Date().toISOString().split('T')[0]
  });

  // Load initial data
  useEffect(() => {
    loadInitialData();
  }, []);

  const loadInitialData = async () => {
    try {
      setIsLoading(true);

      // Load templates
      console.log('正在加载模板列表...');
      const templateList = await api.getTemplates();
      console.log('模板加载结果:', templateList);
      setTemplates(templateList);

      if (templateList.length > 0) {
        setSelectedTemplateId(templateList[0].id);

        // Load sections for first template
        const sectionList = await api.getTemplateSections(templateList[0].id);
        setSections(sectionList);

        if (sectionList.length > 0) {
          setActiveSectionKey(sectionList[0].sectionKey);
        }

        // Check for existing reports or create new one
        const reports = await api.getReports();
        if (reports.length > 0) {
          await loadReport(reports[0].id);
        }
      }
    } catch (error) {
      console.error('加载数据失败:', error);
      alert('加载数据失败，请检查后端服务是否正常运行');
    } finally {
      setIsLoading(false);
    }
  };

  const loadReport = async (reportId: number) => {
    try {
      const detail = await api.getReportDetail(reportId);
      setCurrentReport(detail.report);
      setSections(detail.sections);
      setContents(detail.contents);
      setMetadata({
        title: detail.report.reportName,
        version: 'v1.0',
        author: '编辑者',
        date: detail.report.startDate || new Date().toISOString().split('T')[0]
      });
    } catch (error) {
      console.error('加载报告失败:', error);
    }
  };

  // Load references when active section changes
  useEffect(() => {
    if (activeSectionKey) {
      loadReferences();
    }
  }, [activeSectionKey]);

  const loadReferences = async () => {
    try {
      const refs = await api.searchReferences(activeSectionKey, searchKeyword || undefined);
      setReferences(refs);
    } catch (error) {
      console.error('加载参考资料失败:', error);
    }
  };

  // Create new report - show template selection dialog
  const createNewReport = () => {
    // Allow opening dialog even if no templates, so user can create one
    // if (templates.length === 0) {
    //   alert('暂无可用模板');
    //   return;
    // }
    setShowTemplateDialog(true);
  };

  // Handle template selection and create report
  const handleTemplateSelect = async (templateId: number) => {
    setShowTemplateDialog(false);
    setSelectedTemplateId(templateId);

    try {
      // Load sections for selected template
      const sectionList = await api.getTemplateSections(templateId);
      setSections(sectionList);
      if (sectionList.length > 0) {
        setActiveSectionKey(sectionList[0].sectionKey);
      }

      // Create new report with selected template
      const report = await api.createReport({
        templateId,
        reportName: metadata.title,
        startDate: new Date().toISOString().split('T')[0],
        endDate: new Date().toISOString().split('T')[0]
      });
      await loadReport(report.id);
    } catch (error) {
      console.error('创建报告失败:', error);
    }
  };

  // Context Menu State
  const [contextMenu, setContextMenu] = useState<{ x: number; y: number; node: TreeSection } | null>(null);
  const [parentSectionId, setParentSectionId] = useState<number | null>(null);

  // Close context menu on click elsewhere
  useEffect(() => {
    const handleClick = () => setContextMenu(null);
    document.addEventListener('click', handleClick);
    return () => document.removeEventListener('click', handleClick);
  }, []);

  const handleContextMenu = (e: React.MouseEvent, node: TreeSection) => {
    e.preventDefault(); // Prevent default browser menu
    setContextMenu({ x: e.clientX, y: e.clientY, node });
  };

  // Section management functions
  const openSectionDialog = (section: TemplateSection | null, parentId: number | null = null) => {
    setEditingSection(section);
    setParentSectionId(parentId);
    setShowSectionDialog(true);
  };

  const handleSaveSection = async (data: { title: string; sectionType: string; sectionKey: string }) => {
    const templateId = selectedTemplateId || currentReport?.templateId;
    if (!templateId) {
      alert('请先选择模板或创建报告');
      return;
    }

    try {
      if (editingSection) {
        // Update existing section
        await api.updateSection(editingSection.id, {
          title: data.title,
          sectionType: data.sectionType as 'RICH_TEXT' | 'TABLE' | 'CHART',
        });
      } else {
        // Add new section
        await api.addSection(templateId, {
          title: data.title,
          sectionKey: data.sectionKey,
          sectionType: data.sectionType as 'RICH_TEXT' | 'TABLE' | 'CHART',
          sortOrder: sections.length + 1,
          parentId: parentSectionId // Use state
        });
      }

      // Refresh sections list
      const updatedSections = await api.getTemplateSections(templateId);
      setSections(updatedSections);
      setShowSectionDialog(false);
      setEditingSection(null);
      setParentSectionId(null);
    } catch (error) {
      console.error('保存章节失败:', error);
      alert('保存失败，请重试');
    }
  };

  // (Empty replacement to delete)

  const handleDeleteSection = async (sectionId: number) => {
    if (!confirm('确定删除此章节？删除后无法恢复。')) return;

    const templateId = selectedTemplateId || currentReport?.templateId;
    if (!templateId) return;

    try {
      await api.deleteSection(sectionId);

      // Refresh sections list
      const updatedSections = await api.getTemplateSections(templateId);
      setSections(updatedSections);

      // Reset active section if deleted
      if (sections.find(s => s.id === sectionId)?.sectionKey === activeSectionKey) {
        setActiveSectionKey(updatedSections[0]?.sectionKey || '');
      }
    } catch (error) {
      console.error('删除章节失败:', error);
      alert('删除失败，请重试');
    }
  };

  // Save content
  const handleSaveContent = useCallback(async (sectionKey: string, contentHtml: string) => {
    if (!currentReport) return;

    setIsSaving(true);
    try {
      const saved = await api.saveContent(currentReport.id, sectionKey, contentHtml);
      setContents(prev => ({
        ...prev,
        [sectionKey]: saved
      }));
    } catch (error) {
      console.error('保存失败:', error);
    } finally {
      setIsSaving(false);
    }
  }, [currentReport]);

  // Auto-save with debounce
  const handleContentChange = useCallback((sectionKey: string, content: string) => {
    setContents(prev => ({
      ...prev,
      [sectionKey]: {
        ...prev[sectionKey],
        contentHtml: content
      }
    }));
  }, []);

  // Export Word
  const handleExport = () => {
    if (!currentReport) return;
    window.open(api.getExportUrl(currentReport.id), '_blank');
  };

  // Finalize report
  const handleFinalize = async () => {
    if (!currentReport) return;

    try {
      await api.finalizeReport(currentReport.id);
      alert('报告已归档！');
    } catch (error) {
      console.error('归档失败:', error);
    }
  };

  // Save as standard reference
  const handleSaveAsStandard = async (content: string) => {
    if (!activeSectionKey || !content) return;

    try {
      await api.saveAsStandard(activeSectionKey, content);
      await loadReferences();
      alert('已保存为标准话术！');
    } catch (error) {
      console.error('保存失败:', error);
    }
  };

  // Apply reference to current section
  const handleApplyReference = (contentText: string) => {
    if (!activeSectionKey) return;

    setContents(prev => ({
      ...prev,
      [activeSectionKey]: {
        ...prev[activeSectionKey],
        contentHtml: (prev[activeSectionKey]?.contentHtml || '') + '\n\n' + contentText
      }
    }));
  };

  const getActiveContent = () => {
    return contents[activeSectionKey]?.contentHtml || '';
  };

  const activeSection = useMemo(() =>
    sections.find(s => s.sectionKey === activeSectionKey),
    [sections, activeSectionKey]
  );

  const sectionTree = useMemo(() => buildSectionTree(sections), [sections]);

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-full">
        <div className="text-xl font-bold animate-pulse">加载中...</div>
      </div>
    );
  }

  return (
    <div className="flex flex-col h-full overflow-hidden font-mono">
      {/* Header */}
      <header className="h-12 flex-none border-b-4 border-black px-4 flex items-center justify-between z-20 bg-white">
        <div className="flex items-center gap-4">
          <span className="font-bold text-lg tracking-tighter">
            PROJ_{metadata.title.replace(/\s+/g, '_').toUpperCase()}.MD
          </span>
          <div className="h-4 w-px bg-black"></div>
          <div className="text-[10px] flex gap-4 uppercase font-bold">
            <span className={isSaving ? 'text-yellow-600' : 'text-green-600'}>
              {isSaving ? '保存中...' : '已保存'}
            </span>
            <span>编码: UTF-8</span>
            <span className="text-blue-600">
              状态: {currentReport?.status === 'FINALIZED' ? '已归档' : '草稿'}
            </span>
          </div>
        </div>
        <div className="flex items-center gap-px bg-black border border-black h-8">
          <BrutalButton variant="secondary" className="border-r border-black h-full" onClick={createNewReport}>
            新建报告
          </BrutalButton>
          <BrutalButton variant="secondary" className="border-r border-black h-full" onClick={handleFinalize}>
            归档
          </BrutalButton>
          <BrutalButton variant="primary" className="h-full" onClick={handleExport}>
            导出 Word
          </BrutalButton>
        </div>
      </header>

      <div className="flex flex-1 overflow-hidden">
        {/* Left Sidebar - Section Tree */}
        <aside className="w-64 flex-none border-r-4 border-black flex flex-col bg-white">
          <div className="p-3 border-b-2 border-black bg-black text-white flex justify-between items-center">
            <span className="text-xs font-bold uppercase tracking-widest">目录结构</span>
            <button
              onClick={() => openSectionDialog(null)}
              className="hover:bg-white/20 p-1 rounded transition-colors"
              title="添加章节"
            >
              <span className="material-symbols-outlined text-sm">add</span>
            </button>
          </div>
          <div className="flex-1 overflow-y-auto p-2 text-[10px] font-bold uppercase leading-loose">
            {sections.length === 0 ? (
              <div className="text-center py-8 text-gray-400">
                <span className="material-symbols-outlined text-2xl block mb-2">folder_open</span>
                <p>暂无章节</p>
                <button
                  onClick={() => openSectionDialog(null)}
                  className="mt-2 text-blue-600 hover:underline normal-case"
                >
                  添加第一个章节
                </button>
              </div>
            ) : (
              sectionTree.map(node => (
                <SidebarTreeItem
                  key={node.id}
                  node={node}
                  activeKey={activeSectionKey}
                  onSelect={setActiveSectionKey}
                  onEdit={(s) => openSectionDialog(s, s.parentId)}
                  onDelete={handleDeleteSection}
                  onContextMenu={handleContextMenu}
                />
              ))
            )}
          </div>
          <div className="p-4 border-t-2 border-black">
            <div className="bg-gray-100 p-2 border border-black text-[10px]">
              <div className="font-bold border-b border-black mb-1">统计</div>
              <div>章节数: {sections.length}</div>
              <div>已填写: {Object.keys(contents).filter(k => contents[k]?.contentHtml).length}</div>
            </div>
          </div>
        </aside>

        {/* Main Editor */}
        <main className="flex-1 overflow-y-auto bg-white p-8 space-y-12">
          <div className="max-w-3xl mx-auto space-y-12">
            {/* Metadata Block */}
            <div className="border-2 border-black p-4 bg-gray-50 mb-8">
              <div className="text-[10px] text-gray-500 mb-2 font-bold italic">--- YAML 元数据 ---</div>
              <div className="grid grid-cols-[100px_1fr] gap-2 text-xs">
                <span className="font-bold">标题:</span>
                <input
                  className="bg-transparent border-none p-0 focus:ring-0 font-mono text-xs w-full font-bold"
                  value={metadata.title}
                  onChange={(e) => setMetadata({ ...metadata, title: e.target.value })}
                />
                <span className="font-bold">版本:</span>
                <span>{metadata.version}</span>
                <span className="font-bold">作者:</span>
                <span>{metadata.author}</span>
              </div>
              <div className="text-[10px] text-gray-500 mt-2 font-bold italic">---</div>
            </div>

            {/* Section Editors */}
            <EditorTree
              nodes={sectionTree}
              contents={contents}
              activeSectionKey={activeSectionKey}
              onUpdate={handleContentChange}
              onFocus={setActiveSectionKey}
              onSave={(key) => handleSaveContent(key, contents[key]?.contentHtml || '')}
              EditorSection={EditorSection}
            />
          </div>
        </main>

        {/* Right Sidebar - References */}
        <aside className="w-80 flex-none border-l-4 border-black flex flex-col bg-white overflow-hidden">
          <div className="p-3 border-b-2 border-black bg-white flex justify-between items-center">
            <span className="text-xs font-bold uppercase tracking-widest">参考资料库</span>
            <button
              onClick={() => handleSaveAsStandard(getActiveContent())}
              className="text-[10px] font-bold hover:underline"
              title="保存当前内容为标准话术"
            >
              存为话术
            </button>
          </div>

          <div className="flex-1 overflow-y-auto p-4 space-y-6">
            <div className="space-y-2">
              <div className="text-[10px] font-bold uppercase text-gray-500">搜索存档库</div>
              <input
                className="w-full border-2 border-black p-2 text-xs focus:ring-0 uppercase font-bold placeholder-gray-400"
                placeholder="输入关键词..."
                value={searchKeyword}
                onChange={(e) => setSearchKeyword(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && loadReferences()}
              />
            </div>

            <div className="text-[10px] font-bold text-gray-400 uppercase">
              当前章节: {activeSection?.title || '未选择'}
            </div>

            {references.length > 0 ? (
              references.map(ref => (
                <div key={ref.id} className="border-2 border-black group mb-4">
                  <div className="bg-black text-white p-1 px-2 text-[10px] font-bold flex justify-between">
                    <span>{ref.isStandard ? '★ 标准话术' : '历史记录'}</span>
                    <span className="text-gray-400">{ref.createdAt?.split('T')[0]}</span>
                  </div>
                  <div className="p-2 text-[10px] leading-tight font-mono bg-gray-50">
                    <div className="text-gray-600 italic line-clamp-3">
                      "{ref.contentText?.substring(0, 200)}..."
                    </div>
                  </div>
                  <button
                    className="w-full border-t border-black p-1 text-[10px] font-bold hover:bg-black hover:text-white transition-colors"
                    onClick={() => handleApplyReference(ref.contentText)}
                  >
                    [ 引用此片段 ]
                  </button>
                </div>
              ))
            ) : (
              <div className="text-center py-10 opacity-40 border-2 border-dashed border-black">
                <span className="material-symbols-outlined block mb-2">find_in_page</span>
                <span className="text-[10px] font-bold">暂无参考资料</span>
              </div>
            )}
          </div>

          <div className="p-3 bg-black text-white text-[9px] font-mono border-t-4 border-black flex justify-between">
            <span>API: {currentReport ? '已连接' : '待连接'}</span>
            <span className="animate-pulse">●</span>
          </div>
        </aside>
      </div>

      {/* Dialogs */}
      <TemplateSelectDialog
        templates={templates}
        isOpen={showTemplateDialog}
        onSelect={handleTemplateSelect}
        onCreate={async (name, description, sectionNodes, baseDocxUrl) => {
          try {
            let newTemplate: ReportTemplate;
            const templateData = { name, description, baseDocxUrl };
            if (sectionNodes && sectionNodes.length > 0) {
              newTemplate = await api.createTemplateWithSections(templateData, sectionNodes);
            } else {
              newTemplate = await api.createTemplate(templateData);
            }
            setTemplates(prev => [...prev, newTemplate]);
          } catch (error) {
            console.error('创建模板失败:', error);
            alert('创建模板失败');
          }
        }}
        onClose={() => setShowTemplateDialog(false)}
      />

      <SectionFormDialog
        isOpen={showSectionDialog}
        section={editingSection}
        onSave={handleSaveSection}
        onClose={() => {
          setShowSectionDialog(false);
          setEditingSection(null);
        }}
      />

      {/* Context Menu */}
      {contextMenu && (
        <div
          className="fixed z-50 bg-white border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] py-1 min-w-[150px]"
          style={{ top: contextMenu.y, left: contextMenu.x }}
          onClick={(e) => e.stopPropagation()}
        >
          <button
            className="w-full text-left px-3 py-2 hover:bg-black hover:text-white text-xs font-bold font-mono"
            onClick={() => {
              openSectionDialog(null, contextMenu.node.parentId); // Add Sibling
              setContextMenu(null);
            }}
          >
            + 添加同级章节
          </button>
          <button
            className="w-full text-left px-3 py-2 hover:bg-black hover:text-white text-xs font-bold font-mono"
            onClick={() => {
              openSectionDialog(null, contextMenu.node.id); // Add Child
              setContextMenu(null);
            }}
          >
            ↳ 添加子章节
          </button>
          <div className="h-px bg-gray-200 my-1"></div>
          <button
            className="w-full text-left px-3 py-2 hover:bg-red-600 hover:text-white text-xs font-bold font-mono text-red-600"
            onClick={() => {
              handleDeleteSection(contextMenu.node.id);
              setContextMenu(null);
            }}
          >
            × 删除章节
          </button>
        </div>
      )}
    </div>
  );
};

// --- Helper Components for Hierarchy ---

interface TreeSection extends TemplateSection {
  children?: TreeSection[];
  depth: number;
}

const buildSectionTree = (sections: TemplateSection[]): TreeSection[] => {
  const map = new Map<number, TreeSection>();
  const roots: TreeSection[] = [];

  // Sort by sortOrder first
  const sorted = [...sections].sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0));

  // Init map
  sorted.forEach(s => {
    map.set(s.id, { ...s, children: [], depth: 0 });
  });

  // Build tree
  sorted.forEach(s => {
    const node = map.get(s.id)!;
    if (s.parentId && map.has(s.parentId)) {
      const parent = map.get(s.parentId)!;
      parent.children!.push(node);
      // We'll fix depth recursively later or just ignore depth here and compute in render
    } else {
      roots.push(node);
    }
  });

  return roots;
};

// Sidebar Tree Item
const SidebarTreeItem: React.FC<{
  node: TreeSection;
  activeKey: string;
  onSelect: (key: string) => void;
  onEdit: (s: TemplateSection) => void;
  onDelete: (id: number) => void;
  onContextMenu: (e: React.MouseEvent, node: TreeSection) => void;
  depth?: number;
}> = ({ node, activeKey, onSelect, onEdit, onDelete, onContextMenu, depth = 0 }) => {
  return (
    <>
      <div
        className={`group flex items-center justify-between px-2 py-1 mb-1 cursor-pointer transition-colors ${activeKey === node.sectionKey
          ? 'bg-black text-white'
          : 'hover:bg-gray-200'
          }`}
        style={{ paddingLeft: `${8 + depth * 12}px` }}
        onContextMenu={(e) => {
          e.preventDefault();
          onContextMenu(e, node);
        }}
      >
        <div
          className="flex items-center gap-2 flex-1 min-w-0"
          onClick={() => onSelect(node.sectionKey)}
        >
          {depth > 0 && <span>└</span>}
          <span className="material-symbols-outlined text-[14px]">
            {node.children && node.children.length > 0 ? 'folder' : 'description'}
          </span>
          <span className="truncate">{node.title}</span>
        </div>
        <div className={`flex gap-1 ${activeKey === node.sectionKey
          ? 'opacity-100'
          : 'opacity-0 group-hover:opacity-100'
          } transition-opacity`}>
          <button
            onClick={(e) => {
              e.stopPropagation();
              onEdit(node);
            }}
            className="hover:bg-white/20 p-0.5 rounded"
            title="编辑章节"
          >
            <span className="material-symbols-outlined text-[12px]">edit</span>
          </button>
          <button
            onClick={(e) => {
              e.stopPropagation();
              onDelete(node.id);
            }}
            className="hover:bg-red-500/20 p-0.5 rounded"
            title="删除章节"
          >
            <span className="material-symbols-outlined text-[12px]">delete</span>
          </button>
        </div>
      </div>
      {node.children && node.children.map(child => (
        <SidebarTreeItem
          key={child.id}
          node={child}
          activeKey={activeKey}
          onSelect={onSelect}
          onEdit={onEdit}
          onDelete={onDelete}
          onContextMenu={onContextMenu}
          depth={depth + 1}
        />
      ))}
    </>
  );
};

// Editor Tree
const EditorTree: React.FC<{
  nodes: TreeSection[];
  contents: Record<string, ReportContent>;
  activeSectionKey: string;
  onUpdate: (key: string, content: string) => void;
  onFocus: (key: string) => void;
  onSave: (key: string) => void;
  depth?: number;
  EditorSection: any;
}> = ({ nodes, contents, activeSectionKey, onUpdate, onFocus, onSave, depth = 0, EditorSection }) => {
  return (
    <div className="space-y-6">
      {nodes.map(node => (
        <div key={node.id} className="relative">
          {/* Visual hierarchy indicator */}
          {depth > 0 && (
            <div
              className="absolute left-[-16px] top-0 bottom-0 w-px bg-gray-300"
              style={{ left: -16 }}
            />
          )}

          <div style={{ marginLeft: depth > 0 ? 16 : 0 }}>
            {/* Only render editor if leaf? No, user might want to edit parent content too if it's Mixed content. 
                 But usually folder nodes don't have content? 
                 Let's assume all nodes can have content for now. */}
            <h3 className="font-bold text-sm mb-2 opacity-50 uppercase flex items-center gap-2">
              {depth > 0 && <span>↳</span>} {node.title}
            </h3>

            <EditorSection
              section={{
                id: node.sectionKey,
                title: node.title,
                content: contents[node.sectionKey]?.contentHtml || '',
                type: node.sectionType === 'TABLE' ? 'table' : 'markdown',
                lastModified: contents[node.sectionKey]?.updatedAt || ''
              }}
              isActive={activeSectionKey === node.sectionKey}
              onUpdate={(content: string) => onUpdate(node.sectionKey, content)}
              onDelete={() => { }}
              onFocus={() => onFocus(node.sectionKey)}
              onSave={() => onSave(node.sectionKey)}
            />

            {/* Recursive Children */}
            {node.children && node.children.length > 0 && (
              <div className="mt-6 border-l-2 border-dashed border-gray-200 pl-4 py-2">
                <EditorTree
                  nodes={node.children}
                  contents={contents}
                  activeSectionKey={activeSectionKey}
                  onUpdate={onUpdate}
                  onFocus={onFocus}
                  onSave={onSave}
                  depth={depth + 1}
                  EditorSection={EditorSection}
                />
              </div>
            )}
          </div>
        </div>
      ))}
    </div>
  );
};

export default App;