const xmlEntities = {
  "&": "&amp;",
  "<": "&lt;",
  ">": "&gt;",
  '"': "&quot;",
  "'": "&apos;",
};

export function escapeXml(text) {
  return String(text).replace(/[&<>"']/g, (char) => xmlEntities[char]);
}

function tag(indent, name, value) {
  return `${indent}<${name}>${escapeXml(value.trim())}</${name}>\n`;
}

export function buildRss(feed, items) {
  let rss = '<?xml version="1.0" encoding="UTF-8"?>\n';
  rss += '<rss version="2.0">\n';
  rss += "    <channel>\n";
  rss += tag("        ", "title", feed.title);
  rss += tag("        ", "link", feed.website);
  rss += tag("        ", "description", feed.description);
  rss += tag("        ", "author", feed.author);
  rss += tag("        ", "category", feed.category);

  for (const item of items) {
    rss += "        <item>\n";
    rss += tag("            ", "title", item.title);
    rss += tag("            ", "link", item.url);
    rss += tag("            ", "description", item.description);
    rss += "        </item>\n";
  }

  rss += "    </channel>\n";
  rss += "</rss>";
  return rss;
}
