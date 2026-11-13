import test from "node:test";
import assert from "node:assert/strict";
import { buildRss, escapeXml } from "./rss.js";
import { validateFeed, validateItem } from "./validate.js";

const validFeed = () => ({
  title: "Technology Weekly",
  website: "https://example.com",
  description: "Updates from the engineering team.",
  author: "Gyan Gaurav",
  category: "Technology",
});

const validItem = () => ({
  title: "Spring Boot update",
  url: "https://example.com/article",
  description: "A short project update.",
});

test("escapeXml escapes each XML special character", () => {
  assert.equal(escapeXml(`A & B < C > D "quoted" 'value'`), "A &amp; B &lt; C &gt; D &quot;quoted&quot; &apos;value&apos;");
});

test("escapeXml converts non-string input to text", () => {
  assert.equal(escapeXml(42), "42");
});

test("validateFeed accepts complete data and an HTTPS website", () => {
  assert.deepEqual(validateFeed(validFeed(), [validItem()]), {});
});

test("validateFeed accepts an HTTP website", () => {
  assert.deepEqual(validateFeed({ ...validFeed(), website: "http://example.com" }, [validItem()]), {});
});

test("validateFeed rejects blank required channel fields", () => {
  const errors = validateFeed({ ...validFeed(), title: " ", description: "", author: "\t" }, [validItem()]);
  assert.deepEqual(errors, {
    title: "Feed title is required.",
    description: "Feed description is required.",
    author: "Author name is required.",
  });
});

for (const website of ["example.com", "ftp://example.com", "javascript:alert(1)", "", "  ", "/relative/path"]) {
  test(`validateFeed rejects unsupported website URL: ${JSON.stringify(website)}`, () => {
    assert.equal(validateFeed({ ...validFeed(), website }, [validItem()]).website,
      website.trim() ? "Enter a valid URL starting with http:// or https://." : "Website URL is required.");
  });
}

test("validateFeed requires at least one item", () => {
  assert.equal(validateFeed(validFeed(), []).items, "Add at least one feed item first.");
});

test("validateItem accepts a complete article with an HTTPS URL", () => {
  assert.deepEqual(validateItem(validItem()), {});
});

test("validateItem accepts an HTTP article URL", () => {
  assert.deepEqual(validateItem({ ...validItem(), url: "http://example.com/article" }), {});
});

test("validateItem reports each missing article field", () => {
  assert.deepEqual(validateItem({ title: " ", url: "", description: "" }), {
    title: "Article title is required.",
    url: "Article URL is required.",
    description: "Article description is required.",
  });
});

test("validateItem rejects non-HTTP URL schemes", () => {
  assert.equal(validateItem({ ...validItem(), url: "file:///tmp/article" }).url,
    "Enter a valid URL starting with http:// or https://.");
});

test("buildRss returns an RSS 2.0 document with channel fields and one item", () => {
  const xml = buildRss(validFeed(), [validItem()]);
  assert.match(xml, /^<\?xml version="1\.0" encoding="UTF-8"\?>\n<rss version="2\.0">/);
  assert.match(xml, /<channel>[\s\S]*<title>Technology Weekly<\/title>/);
  assert.match(xml, /<item>[\s\S]*<title>Spring Boot update<\/title>/);
  assert.match(xml, /<description>A short project update\.<\/description>/);
  assert.match(xml, /<\/channel>\n<\/rss>$/);
});

test("buildRss trims channel and item text before serialization", () => {
  const feed = { ...validFeed(), title: "  Weekly  " };
  const item = { ...validItem(), title: "  Article  " };
  const xml = buildRss(feed, [item]);
  assert.match(xml, /<title>Weekly<\/title>/);
  assert.match(xml, /<title>Article<\/title>/);
});

test("buildRss escapes XML markup in channel and article values", () => {
  const feed = { ...validFeed(), title: "R&D <News>" };
  const item = { ...validItem(), description: `A "quoted" & 'safe' <text>` };
  const xml = buildRss(feed, [item]);
  assert.match(xml, /<title>R&amp;D &lt;News&gt;<\/title>/);
  assert.match(xml, /A &quot;quoted&quot; &amp; &apos;safe&apos; &lt;text&gt;/);
});

test("buildRss serializes multiple items in the supplied order", () => {
  const xml = buildRss(validFeed(), [validItem(), { ...validItem(), title: "Second story" }]);
  assert.ok(xml.indexOf("<title>Spring Boot update</title>") < xml.indexOf("<title>Second story</title>"));
  assert.equal((xml.match(/<item>/g) ?? []).length, 2);
});

test("buildRss produces a channel with no item elements for an empty list", () => {
  const xml = buildRss(validFeed(), []);
  assert.match(xml, /<channel>[\s\S]*<\/channel>/);
  assert.doesNotMatch(xml, /<item>/);
});
