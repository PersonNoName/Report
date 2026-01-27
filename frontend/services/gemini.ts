
import { GoogleGenAI } from "@google/genai";

// Create instance right before making an API call to ensure it uses the latest configuration
const getAI = () => new GoogleGenAI({ apiKey: process.env.API_KEY });

export const generateSectionContent = async (title: string, context?: string) => {
  const ai = getAI();
  try {
    const response = await ai.models.generateContent({
      model: 'gemini-3-flash-preview',
      contents: `你是一位专业的报告撰写专家。请为标题为 "${title}" 的报告章节生成专业的摘要内容。${context ? `参考背景信息: ${context}` : ''} 请直接返回 Markdown 格式的内容，使用中文撰写。`,
    });
    // Access the text property directly as per guidelines
    return response.text || '';
  } catch (error) {
    console.error("AI 生成错误:", error);
    return "生成内容时出错，请稍后重试。";
  }
};