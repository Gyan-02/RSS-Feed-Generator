const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";

async function parseError(response) {
  try {
    const data = await response.json();
    if (Array.isArray(data.messages) && data.messages.length > 0) {
      return data.messages.join(" ");
    }
  } catch {
    // The server may be unreachable or return a non-JSON error page.
  }

  return `Request failed with status ${response.status}.`;
}

export async function createFeed(payload) {
  const response = await fetch(`${API_BASE_URL}/feeds`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(payload),
  });

  if (!response.ok) {
    throw new Error(await parseError(response));
  }

  return response.json();
}

export async function getFeeds() {
  const response = await fetch(`${API_BASE_URL}/feeds`);

  if (!response.ok) {
    throw new Error(await parseError(response));
  }

  return response.json();
}

export function getFeedRssUrl(feedId) {
  return `${API_BASE_URL}/feeds/${feedId}/rss`;
}

export async function getFeedRss(feedId) {
  const response = await fetch(getFeedRssUrl(feedId), {
    headers: {
      Accept: "application/rss+xml",
    },
  });

  if (!response.ok) {
    throw new Error(await parseError(response));
  }

  return response.text();
}
