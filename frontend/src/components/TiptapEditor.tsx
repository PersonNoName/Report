"use client";

import { useEditor, EditorContent } from "@tiptap/react";
import StarterKit from "@tiptap/starter-kit";
import { useEffect } from "react";

interface TiptapEditorProps {
  content?: string;
  onChange?: (html: string) => void;
  onFocus?: () => void;
  placeholder?: string;
}

const TiptapEditor = ({
  content = "",
  onChange,
  onFocus,
  placeholder = "开始输入内容..."
}: TiptapEditorProps) => {
  const editor = useEditor({
    extensions: [StarterKit],
    content: content,
    editorProps: {
      attributes: {
        class: "prose prose-sm max-w-none focus:outline-none min-h-[150px] p-4",
      },
    },
    onUpdate: ({ editor }) => {
      onChange?.(editor.getHTML());
    },
    onFocus: () => {
      onFocus?.();
    },
  });

  // Update content when prop changes
  useEffect(() => {
    if (editor && content !== editor.getHTML()) {
      editor.commands.setContent(content);
    }
  }, [content, editor]);

  if (!editor) {
    return <div className="p-4 text-gray-400">{placeholder}</div>;
  }

  return (
    <div className="tiptap-wrapper">
      <EditorContent editor={editor} />
    </div>
  );
};

export default TiptapEditor;
