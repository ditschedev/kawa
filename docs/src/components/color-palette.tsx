"use client";

import { useState } from "react";
import { cn } from "@/lib/cn";

type ColorValue =
  | { type: "hex"; value: string }
  | { type: "oklch"; L: number; C: number; h: number };

interface ColorEntry {
  shade: string;
  color: ColorValue;
}

interface ColorScale {
  label: string;
  prefix: string;
  colors: ColorEntry[];
}

function oklchCss(L: number, C: number, h: number) {
  return `oklch(${L}% ${C} ${h})`;
}

function hex(value: string): ColorValue {
  return { type: "hex", value };
}
function oklch(L: number, C: number, h: number): ColorValue {
  return { type: "oklch", L, C, h };
}

function toCss(c: ColorValue): string {
  return c.type === "hex" ? c.value : oklchCss(c.L, c.C, c.h);
}

const PALETTE: ColorScale[] = [
  {
    label: "Red",
    prefix: "RED",
    colors: [
      { shade: "50", color: oklch(97.1, 0.013, 17.38) },
      { shade: "100", color: oklch(93.6, 0.032, 17.717) },
      { shade: "200", color: oklch(88.5, 0.062, 18.334) },
      { shade: "300", color: oklch(80.8, 0.114, 19.571) },
      { shade: "400", color: oklch(70.4, 0.191, 22.216) },
      { shade: "500", color: oklch(63.7, 0.237, 25.331) },
      { shade: "600", color: oklch(57.7, 0.245, 27.325) },
      { shade: "700", color: oklch(50.5, 0.213, 27.518) },
      { shade: "800", color: oklch(44.4, 0.177, 26.899) },
      { shade: "900", color: oklch(39.6, 0.141, 25.723) },
      { shade: "950", color: oklch(25.8, 0.092, 26.042) },
    ],
  },
  {
    label: "Orange",
    prefix: "ORANGE",
    colors: [
      { shade: "50", color: hex("#fff7ed") },
      { shade: "100", color: hex("#ffedd5") },
      { shade: "200", color: hex("#fed7aa") },
      { shade: "300", color: hex("#fdba74") },
      { shade: "400", color: hex("#fb923c") },
      { shade: "500", color: hex("#f97316") },
      { shade: "600", color: hex("#ea580c") },
      { shade: "700", color: hex("#c2410c") },
      { shade: "800", color: hex("#9a3412") },
      { shade: "900", color: hex("#7c2d12") },
      { shade: "950", color: hex("#431407") },
    ],
  },
  {
    label: "Amber",
    prefix: "AMBER",
    colors: [
      { shade: "50", color: hex("#fffbeb") },
      { shade: "100", color: hex("#fef3c7") },
      { shade: "200", color: hex("#fde68a") },
      { shade: "300", color: hex("#fcd34d") },
      { shade: "400", color: hex("#fbbf24") },
      { shade: "500", color: hex("#f59e0b") },
      { shade: "600", color: hex("#d97706") },
      { shade: "700", color: hex("#b45309") },
      { shade: "800", color: hex("#92400e") },
      { shade: "900", color: hex("#78350f") },
      { shade: "950", color: hex("#451a03") },
    ],
  },
  {
    label: "Yellow",
    prefix: "YELLOW",
    colors: [
      { shade: "50", color: hex("#fefce8") },
      { shade: "100", color: hex("#fef9c3") },
      { shade: "200", color: hex("#fef08a") },
      { shade: "300", color: hex("#fde047") },
      { shade: "400", color: hex("#facc15") },
      { shade: "500", color: hex("#eab308") },
      { shade: "600", color: hex("#ca8a04") },
      { shade: "700", color: hex("#a16207") },
      { shade: "800", color: hex("#854d0e") },
      { shade: "900", color: hex("#713f12") },
      { shade: "950", color: hex("#422006") },
    ],
  },
  {
    label: "Lime",
    prefix: "LIME",
    colors: [
      { shade: "50", color: hex("#f7fee7") },
      { shade: "100", color: hex("#ecfccb") },
      { shade: "200", color: hex("#d9f99d") },
      { shade: "300", color: hex("#bef264") },
      { shade: "400", color: hex("#a3e635") },
      { shade: "500", color: hex("#84cc16") },
      { shade: "600", color: hex("#65a30d") },
      { shade: "700", color: hex("#4d7c0f") },
      { shade: "800", color: hex("#3f6212") },
      { shade: "900", color: hex("#365314") },
      { shade: "950", color: hex("#1a2e05") },
    ],
  },
  {
    label: "Gray",
    prefix: "GRAY",
    colors: [
      { shade: "50", color: oklch(98.5, 0.002, 247.839) },
      { shade: "100", color: oklch(96.7, 0.003, 264.542) },
      { shade: "200", color: oklch(92.8, 0.006, 264.531) },
      { shade: "300", color: oklch(87.2, 0.01, 258.338) },
      { shade: "400", color: oklch(70.7, 0.022, 261.325) },
      { shade: "500", color: oklch(55.1, 0.027, 264.364) },
      { shade: "600", color: oklch(44.6, 0.03, 256.802) },
      { shade: "700", color: oklch(37.3, 0.034, 259.733) },
      { shade: "800", color: oklch(27.8, 0.033, 256.848) },
      { shade: "900", color: oklch(13, 0.028, 261.692) },
    ],
  },
  {
    label: "Slate",
    prefix: "SLATE",
    colors: [
      { shade: "50", color: oklch(98.4, 0.003, 247.858) },
      { shade: "100", color: oklch(96.8, 0.007, 247.896) },
      { shade: "200", color: oklch(92.9, 0.013, 255.508) },
      { shade: "300", color: oklch(86.9, 0.022, 252.894) },
      { shade: "400", color: oklch(70.4, 0.04, 256.788) },
      { shade: "500", color: oklch(55.4, 0.046, 257.417) },
      { shade: "600", color: oklch(44.6, 0.043, 257.281) },
      { shade: "700", color: oklch(37.2, 0.044, 257.287) },
      { shade: "800", color: oklch(27.9, 0.041, 260.031) },
      { shade: "900", color: oklch(20.8, 0.042, 265.755) },
      { shade: "950", color: oklch(12.9, 0.042, 264.695) },
    ],
  },
  {
    label: "Zinc",
    prefix: "ZINC",
    colors: [
      { shade: "50", color: oklch(98.5, 0, 0) },
      { shade: "100", color: oklch(96.7, 0.001, 286.375) },
      { shade: "200", color: oklch(92, 0.004, 286.32) },
      { shade: "300", color: oklch(87.1, 0.006, 286.286) },
      { shade: "400", color: oklch(70.5, 0.015, 286.067) },
      { shade: "500", color: oklch(55.2, 0.016, 285.938) },
      { shade: "600", color: oklch(44.2, 0.017, 285.786) },
      { shade: "700", color: oklch(37, 0.013, 285.805) },
      { shade: "800", color: oklch(27.4, 0.006, 286.033) },
      { shade: "900", color: oklch(21, 0.006, 285.885) },
      { shade: "950", color: oklch(14.1, 0.005, 285.823) },
    ],
  },
  {
    label: "Blue",
    prefix: "BLUE",
    colors: [
      { shade: "50", color: hex("#EFF6FF") },
      { shade: "100", color: hex("#DBEAFE") },
      { shade: "200", color: hex("#BFDBFE") },
      { shade: "300", color: hex("#93C5FD") },
      { shade: "400", color: hex("#60A5FA") },
      { shade: "500", color: hex("#3B82F6") },
      { shade: "600", color: hex("#2563EB") },
      { shade: "700", color: hex("#1D4ED8") },
      { shade: "800", color: hex("#1E40AF") },
      { shade: "900", color: hex("#1E3A8A") },
    ],
  },
  {
    label: "Green",
    prefix: "GREEN",
    colors: [
      { shade: "50", color: hex("#F0FDF4") },
      { shade: "100", color: hex("#DCFCE7") },
      { shade: "200", color: hex("#BBF7D0") },
      { shade: "300", color: hex("#86EFAC") },
      { shade: "500", color: hex("#22C55E") },
      { shade: "600", color: hex("#16A34A") },
      { shade: "700", color: hex("#15803D") },
      { shade: "900", color: hex("#14532D") },
    ],
  },
  {
    label: "Indigo",
    prefix: "INDIGO",
    colors: [
      { shade: "50", color: hex("#EEF2FF") },
      { shade: "100", color: hex("#E0E7FF") },
      { shade: "500", color: hex("#6366F1") },
      { shade: "600", color: hex("#4F46E5") },
      { shade: "700", color: hex("#4338CA") },
      { shade: "900", color: hex("#312E81") },
    ],
  },
];

const NEUTRAL = [
  { label: "WHITE", constant: "Colors.WHITE", color: hex("#FFFFFF") },
  { label: "BLACK", constant: "Colors.BLACK", color: hex("#000000") },
];

function Swatch({
  constant,
  label,
  color,
}: {
  constant: string;
  label: string;
  color: ColorValue;
}) {
  const [flash, setFlash] = useState(false);
  const css = toCss(color);

  function handleClick() {
    navigator.clipboard.writeText(constant).catch(() => {});
    setTimeout(() => setFlash(false), 500);
  }

  return (
    <button
      type="button"
      onClick={handleClick}
      className="group relative flex flex-col items-center gap-1.5 focus:outline-none"
    >
      <div
        className={cn(
          "size-9 rounded-md border border-black/10 transition-all duration-150 dark:border-white/10",
          flash ? "scale-90 opacity-50" : ""
        )}
        style={{ backgroundColor: css }}
      />
      <span className="text-fd-muted-foreground text-xs leading-none tabular-nums">{label}</span>
      <div className="bg-fd-card text-fd-card-foreground pointer-events-none absolute bottom-full left-1/2 z-20 mb-2 -translate-x-1/2 rounded-lg border px-2.5 py-1.5 text-xs whitespace-nowrap opacity-0 shadow-md transition-opacity duration-100 group-hover:opacity-100">
        <p className="font-mono font-semibold">{constant}</p>
        <p className="text-fd-muted-foreground mt-0.5 font-mono">{css}</p>
      </div>
    </button>
  );
}

export function ColorPalette() {
  return (
    <div className="not-prose my-6 flex flex-col gap-4">
      {PALETTE.map((scale) => (
        <div key={scale.prefix} className="flex items-start gap-4">
          <span className="w-14 shrink-0 pt-1.5 text-sm/6 font-medium">{scale.label}</span>
          <div className="flex flex-wrap gap-3">
            {scale.colors.map((c) => (
              <Swatch
                key={c.shade}
                constant={`Colors.${scale.prefix}_${c.shade}`}
                label={c.shade}
                color={c.color}
              />
            ))}
          </div>
        </div>
      ))}
      <div className="flex items-start gap-3">
        <span className="w-14 shrink-0 pt-1.5 text-sm font-medium">Neutral</span>
        <div className="flex flex-wrap gap-1.5">
          {NEUTRAL.map((c) => (
            <Swatch key={c.label} constant={c.constant} label={c.label} color={c.color} />
          ))}
        </div>
      </div>
    </div>
  );
}
