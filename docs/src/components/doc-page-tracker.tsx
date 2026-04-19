"use client";

import { useEffect } from "react";
import posthog from "posthog-js";

interface DocPageTrackerProps {
  title: string;
  slug: string;
  description?: string;
}

export function DocPageTracker({ title, slug, description }: DocPageTrackerProps) {
  useEffect(() => {
    posthog.capture("doc_page_viewed", {
      doc_title: title,
      doc_slug: slug,
      doc_description: description,
    });
  }, [title, slug, description]);

  return null;
}
