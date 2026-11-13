function isValidUrl(value) {
  try {
    const url = new URL(value);
    return url.protocol === "http:" || url.protocol === "https:";
  } catch {
    return false;
  }
}

function checkUrl(value, label) {
  const trimmed = value.trim();
  if (trimmed === "") return `${label} is required.`;
  if (!isValidUrl(trimmed)) {
    return `Enter a valid URL starting with http:// or https://.`;
  }
  return null;
}

function checkRequired(value, label) {
  return value.trim() === "" ? `${label} is required.` : null;
}

function collect(checks) {
  const errors = {};
  for (const [field, message] of Object.entries(checks)) {
    if (message) errors[field] = message;
  }
  return errors;
}

export function validateFeed(feed, items) {
  return collect({
    title: checkRequired(feed.title, "Feed title"),
    website: checkUrl(feed.website, "Website URL"),
    description: checkRequired(feed.description, "Feed description"),
    author: checkRequired(feed.author, "Author name"),
    items: items.length === 0 ? "Add at least one feed item first." : null,
  });
}

export function validateItem(item) {
  return collect({
    title: checkRequired(item.title, "Article title"),
    url: checkUrl(item.url, "Article URL"),
    description: checkRequired(item.description, "Article description"),
  });
}
