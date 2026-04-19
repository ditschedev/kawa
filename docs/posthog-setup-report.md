<wizard-report>
# PostHog post-wizard report

The wizard has completed a deep integration of PostHog analytics into the Kawa docs site. PostHog is initialized client-side via `instrumentation-client.ts` (the recommended approach for Next.js 15.3+), with a reverse proxy configured in `next.config.mjs` to route ingestion traffic through `/ingest` for better reliability. A server-side PostHog client (`src/lib/posthog-server.ts`) enables event capture from API routes. The home page was converted to a client component to support click tracking on CTAs and navigation links. A lightweight `DocPageTracker` client component captures doc page view events with metadata (title, slug, description). The search API route wraps the fumadocs handler to track search queries server-side.

| Event                 | Description                                                                                      | File                                  |
| --------------------- | ------------------------------------------------------------------------------------------------ | ------------------------------------- |
| `docs_cta_clicked`    | User clicked the primary "Read the docs" CTA button on the home page                             | `src/app/(home)/page.tsx`             |
| `github_link_clicked` | User clicked a "View on GitHub" link (hero or footer), with `location` property                  | `src/app/(home)/page.tsx`             |
| `quick_link_clicked`  | User clicked one of the quick link cards in the "Explore the docs" section                       | `src/app/(home)/page.tsx`             |
| `doc_page_viewed`     | User viewed a documentation page, with `doc_title`, `doc_slug`, and `doc_description` properties | `src/components/doc-page-tracker.tsx` |
| `search_performed`    | User performed a search query via the docs search API (server-side), with `query` property       | `src/app/api/search/route.ts`         |

## Next steps

We've built some insights and a dashboard for you to keep an eye on user behavior, based on the events we just instrumented:

- **Dashboard — Analytics basics**: https://eu.posthog.com/project/161963/dashboard/629933
- **Doc page views over time**: https://eu.posthog.com/project/161963/insights/r98bTApi
- **CTA & GitHub clicks over time**: https://eu.posthog.com/project/161963/insights/efm87Q1W
- **Home → Docs conversion funnel**: https://eu.posthog.com/project/161963/insights/0WV12UZ0
- **Most viewed doc pages**: https://eu.posthog.com/project/161963/insights/h0nVo8si
- **Search volume over time**: https://eu.posthog.com/project/161963/insights/jnVqYYeR

### Agent skill

We've left an agent skill folder in your project. You can use this context for further agent development when using Claude Code. This will help ensure the model provides the most up-to-date approaches for integrating PostHog.

</wizard-report>
