
import './globals.css';
import { JetBrains_Mono } from 'next/font/google';

const mono = JetBrains_Mono({ 
  subsets: ['latin'],
  variable: '--font-mono',
});

export const metadata = {
  title: 'REPORT_EDITOR // 原子化报告编辑器',
  description: '基于原子化内容管理的专业报告系统',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="zh-CN" className={`${mono.variable}`}>
      <head>
        <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200" rel="stylesheet" />
      </head>
      <body className="font-mono bg-white text-black antialiased h-screen border-4 border-black overflow-hidden">
        {children}
      </body>
    </html>
  );
}
