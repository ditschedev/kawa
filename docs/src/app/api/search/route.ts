import { source } from "@/lib/source";
import { createFromSource } from "fumadocs-core/search/server";
import { getPostHogClient } from "@/lib/posthog-server";

const { GET: searchGET } = createFromSource(source, {
  // https://docs.orama.com/docs/orama-js/supported-languages
  language: "english",
});

export async function GET(request: Request) {
  const url = new URL(request.url);
  const query = url.searchParams.get("query");

  if (query) {
    const posthog = getPostHogClient();
    posthog.capture({
      distinctId: "anonymous",
      event: "search_performed",
      properties: { query },
    });
    await posthog.shutdown();
  }

  return searchGET(request);
}
