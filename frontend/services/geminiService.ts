
import { GoogleGenAI } from "@google/genai";

// Initialize with a named parameter using process.env.API_KEY directly as per guidelines
const ai = new GoogleGenAI({ apiKey: process.env.API_KEY });

export const generateSectionContent = async (title: string, context?: string) => {
  try {
    const response = await ai.models.generateContent({
      model: 'gemini-3-flash-preview',
      contents: `你是一位专业的报告撰写专家。请为标题为 "${title}" 的报告章节生成专业的摘要内容。${context ? `参考背景信息: ${context}` : ''} 请直接返回 Markdown 格式的内容，使用中文撰写。`,
    });
    // Access the text property directly instead of calling a text() method
    return response.text || '';
  } catch (error) {
    console.error("AI 生成错误:", error);
    return "生成内容时出错，请稍后重试。";
  }
};

export const analyzeDiff = async (oldContent: string, newContent: string) => {
  try {
    const response = await ai.models.generateContent({
      model: 'gemini-3-flash-preview',
      contents: `对比以下两个报告片段，并提供关键变化的简要总结（使用中文）。
      片段 1: ${oldContent}
      片段 2: ${newContent}`,
    });
    // Access the text property directly instead of calling a text() method
    return response.text || '';
  } catch (error) {
    console.error("AI 分析错误:", error);
    return "无法分析差异。";
  }
};