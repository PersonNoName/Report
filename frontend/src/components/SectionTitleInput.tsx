import { useState, useEffect, useRef } from "react";
import { cn } from "@/lib/utils";

interface SectionTitleInputProps {
    value: string;
    onUpdate: (value: string) => void;
    className?: string;
    placeholder?: string;
}

export default function SectionTitleInput({
    value,
    onUpdate,
    className,
    placeholder
}: SectionTitleInputProps) {
    const [localValue, setLocalValue] = useState(value || "");
    const isComposing = useRef(false);

    useEffect(() => {
        setLocalValue(value || "");
    }, [value]);

    const handleBlur = () => {
        if (localValue !== value) {
            onUpdate(localValue);
        }
    };

    const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
        if (e.key === "Enter" && !isComposing.current) {
            e.currentTarget.blur();
        }
    };

    return (
        <input
            type="text"
            value={localValue}
            onChange={(e) => setLocalValue(e.target.value)}
            onBlur={handleBlur}
            onKeyDown={handleKeyDown}
            onCompositionStart={() => { isComposing.current = true; }}
            onCompositionEnd={() => { isComposing.current = false; }}
            className={cn("bg-transparent border-none p-0 focus:ring-0", className)}
            placeholder={placeholder}
        />
    );
}
