
import React from 'react';

interface BrutalButtonProps {
  children: React.ReactNode;
  onClick?: () => void;
  variant?: 'primary' | 'secondary';
  className?: string;
}

export const BrutalButton: React.FC<BrutalButtonProps> = ({ 
  children, 
  onClick, 
  variant = 'primary',
  className = ''
}) => {
  const baseStyles = "px-4 py-1 text-xs font-bold transition-all border-black h-8 flex items-center justify-center";
  const variants = {
    primary: "bg-black text-white hover:bg-white hover:text-black",
    secondary: "bg-white text-black hover:bg-black hover:text-white border",
  };

  return (
    <button onClick={onClick} className={`${baseStyles} ${variants[variant]} ${className}`}>
      {children}
    </button>
  );
};
