"use client";

import Link from "next/link";
import posthog from "posthog-js";
import { ArrowRight, BookOpenText, Boxes, Palette, ScanLine, FileStack } from "lucide-react";

const features = [
  {
    title: "Fluent layout primitives",
    description:
      "Compose pages with text, tables, images, rows, columns, and reusable building blocks instead of hand-placing PDF objects.",
    icon: Boxes,
  },
  {
    title: "Automatic pagination",
    description:
      "Long content flows across pages cleanly while headers, footers, spacing, and wrapping stay consistent.",
    icon: FileStack,
  },
  {
    title: "Production-ready output",
    description:
      "Embed fonts, work with units, style documents, add QR codes or barcodes, and ship polished reports from Java.",
    icon: Palette,
  },
];

const quickLinks = [
  {
    href: "/docs/getting-started",
    title: "Getting started",
    description: "Install Kawa, render your first document, and learn the core flow.",
  },
  {
    href: "/docs/document",
    title: "Document API",
    description: "Compose pages, margins, headers, footers, metadata, and pagination.",
  },
  {
    href: "/docs/elements",
    title: "Elements",
    description: "Use text, media, tables, forms, and custom components in one layout tree.",
  },
];

export default function HomePage() {
  return (
    <main>
      <section className="overflow-hidden border border-white/10 bg-neutral-950 text-neutral-100 shadow-[0_30px_120px_-48px_rgba(0,0,0,0.8)]">
        <div className="border-b border-white/10 bg-[radial-gradient(circle_at_top_left,rgba(34,211,238,0.18),transparent_30%),radial-gradient(circle_at_80%_20%,rgba(96,165,250,0.14),transparent_28%),linear-gradient(180deg,rgba(23,23,23,0.92),rgba(10,10,10,1))]">
          <div className="mx-auto max-w-(--fd-layout-width) px-6 py-14 lg:px-10 lg:py-20">
            <div className="grid gap-10 lg:grid-cols-[11fr_9fr] lg:gap-14">
              <div className="space-y-4">
                <p className="font-mono text-sm text-neutral-400">
                  Open source PDF generation for Java
                </p>
                <h1 className="max-w-[16ch] text-5xl font-medium tracking-[-0.04em] text-balance text-white sm:text-6xl">
                  Readable PDF composition for Java.
                </h1>
              </div>

              <div className="flex flex-col justify-end gap-7 lg:pt-16">
                <p className="max-w-[40ch] text-base text-pretty text-neutral-300 sm:text-lg">
                  Kawa gives you composable layout primitives, automatic pagination, styling, and
                  PDF operations in one fluent API, so reports and document workflows stay
                  maintainable as they grow.
                </p>

                <div className="flex flex-col gap-3 sm:flex-row">
                  <Link
                    href="/docs/getting-started"
                    className="inline-flex items-center justify-center gap-2 rounded-full bg-cyan-300 px-4 py-3 text-base font-medium text-neutral-950 transition hover:bg-cyan-200 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-cyan-300 sm:text-sm"
                    onClick={() =>
                      posthog.capture("docs_cta_clicked", { destination: "/docs/getting-started" })
                    }
                  >
                    Read the docs
                    <ArrowRight className="size-4" />
                  </Link>
                  <Link
                    href="https://github.com/ditschedev/kawa"
                    className="inline-flex items-center justify-center rounded-full border border-white/15 bg-white/5 px-4 py-3 text-base text-neutral-200 transition hover:border-white/25 hover:bg-white/8 hover:text-white focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-neutral-500 sm:text-sm"
                    onClick={() => posthog.capture("github_link_clicked", { location: "hero" })}
                  >
                    View on GitHub
                  </Link>
                </div>

                <div className="flex flex-col gap-3 text-base text-neutral-400 sm:flex-row sm:flex-wrap sm:gap-6 sm:text-sm">
                  <p className="font-mono text-neutral-500">Java 17+</p>
                  <p className="font-mono text-neutral-500">Apache PDFBox 3</p>
                  <p className="font-mono text-neutral-500">No templates</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="mx-auto max-w-(--fd-layout-width) px-6 py-14 lg:px-10 lg:py-16">
          <div className="space-y-4">
            <p className="font-mono text-sm text-neutral-500">Why Kawa</p>
            <h2 className="max-w-[23ch] text-4xl font-semibold tracking-tight text-balance text-white sm:text-5xl">
              A layout-first API for documents that need more than a quick export.
            </h2>
            <p className="max-w-[48ch] text-base text-pretty text-neutral-400 sm:text-lg">
              The library is designed for invoices, reports, statements, confirmations, and
              generated PDFs that need structure, reuse, and predictable output.
            </p>
          </div>

          <dl className="mt-10 grid gap-5 lg:grid-cols-3">
            {features.map((feature) => {
              const Icon = feature.icon;

              return (
                <div
                  key={feature.title}
                  className="rounded-[1.75rem] border border-white/10 bg-white/[0.03] p-6"
                >
                  <dt className="flex items-center gap-3 text-xl font-semibold text-white">
                    <span className="flex size-11 items-center justify-center rounded-2xl bg-white/8 text-cyan-300">
                      <Icon className="size-5" />
                    </span>
                    {feature.title}
                  </dt>
                  <dd className="mt-4 text-base text-pretty text-neutral-400 sm:text-sm">
                    {feature.description}
                  </dd>
                </div>
              );
            })}
          </dl>
        </div>

        <div className="border-t border-white/10 bg-white/[0.02]">
          <div className="mx-auto max-w-(--fd-layout-width) px-6 py-14 lg:px-10 lg:py-16">
            <div className="grid gap-10 lg:grid-cols-[9fr_11fr] lg:items-start">
              <div className="space-y-4">
                <p className="font-mono text-sm text-neutral-500">Explore the docs</p>
                <h2 className="max-w-[22ch] text-4xl font-semibold tracking-tight text-balance text-white sm:text-5xl">
                  Start with the core concepts, then branch into elements and styling.
                </h2>
                <p className="max-w-[42ch] text-base text-pretty text-neutral-400 sm:text-lg">
                  The docs are organized so you can get a first document out quickly, then deepen
                  into composition, fonts, colors, units, and PDF workflows.
                </p>
              </div>

              <div className="grid gap-4">
                {quickLinks.map((link) => (
                  <Link
                    key={link.href}
                    href={link.href}
                    className="group rounded-[1.5rem] border border-white/10 bg-neutral-900/80 p-5 transition hover:border-cyan-300/40 hover:bg-neutral-900"
                    onClick={() =>
                      posthog.capture("quick_link_clicked", { title: link.title, href: link.href })
                    }
                  >
                    <div className="flex items-start justify-between gap-4">
                      <div>
                        <p className="text-xl font-semibold text-white">{link.title}</p>
                        <p className="mt-2 max-w-[44ch] text-base text-pretty text-neutral-400 sm:text-sm">
                          {link.description}
                        </p>
                      </div>
                      <span className="mt-1 flex size-10 shrink-0 items-center justify-center rounded-full border border-white/10 bg-white/5 text-neutral-300 transition group-hover:border-cyan-300/40 group-hover:text-cyan-300">
                        <ArrowRight className="size-4" />
                      </span>
                    </div>
                  </Link>
                ))}
              </div>
            </div>
          </div>
        </div>

        <div className="border-t border-white/10">
          <div className="mx-auto flex max-w-(--fd-layout-width) flex-col gap-4 px-6 py-8 text-base text-neutral-400 sm:flex-row sm:items-center sm:justify-between sm:text-sm lg:px-10">
            <div className="flex items-center gap-3">
              <ScanLine className="size-4 text-cyan-300" />
              <span>Composable PDFs, reusable report classes, and layout that scales.</span>
            </div>
            <div className="flex items-center gap-5">
              <Link
                href="/docs"
                className="inline-flex items-center gap-2 text-neutral-200 transition hover:text-white"
              >
                <BookOpenText className="size-4" />
                Browse docs
              </Link>
              <Link
                href="https://github.com/ditschedev/kawa"
                className="text-neutral-200 transition hover:text-white"
                onClick={() => posthog.capture("github_link_clicked", { location: "footer" })}
              >
                GitHub
              </Link>
            </div>
          </div>
        </div>
      </section>
    </main>
  );
}
