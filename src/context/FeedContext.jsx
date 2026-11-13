import { createContext, useContext, useState, useEffect, useMemo } from "react";
import useLocalStorage from "../hooks/useLocalStorage.js";
import { validateFeed } from "../utils/validate.js";
import { createFeed, getFeedRss } from "../services/feedApi.js";

const FeedContext = createContext(null);

const initialFeed = {
  title: "",
  website: "",
  description: "",
  author: "",
  category: "Technology",
};

const sampleFeed = {
  title: "Tech News Daily",
  description: "Latest technology news and updates.",
  author: "Gyan Gaurav",
  category: "Technology",
};

const sampleItems = [
  {
    id: "sample-1",
    title: "Latest Web Development Trends",
    description: "Learn about some of the latest trends in web development.",
    url: "https://developer.mozilla.org/en-US/docs/",
  },
  {
    id: "sample-2",
    title: "Introduction to RSS Feeds",
    description:
      "RSS feeds help users receive updates from websites in an organized format.",
    url: "https://en.wikipedia.org/wiki/RSS",
  },
  {
    id: "sample-3",
    title: "Getting Started with HTML and CSS",
    description:
      "Learn the basic concepts of creating webpages using HTML and CSS.",
    url: "https://developer.mozilla.org/en-US/docs/Learn_web_development",
  },
];

function createId() {
  return Date.now().toString(36) + Math.random().toString(36).slice(2, 8);
}

function cleanItem(item) {
  return {
    title: item.title.trim(),
    url: item.url.trim(),
    description: item.description.trim(),
  };
}

export function FeedProvider({ children }) {
  const [feed, setFeed] = useLocalStorage("rss-feed", initialFeed);
  const [items, setItems] = useLocalStorage("rss-items", []);
  const [rssOutput, setRssOutput] = useState("");
  const [editingId, setEditingId] = useState(null);
  const [feedSubmitted, setFeedSubmitted] = useState(false);
  const [backendFeedId, setBackendFeedId] = useLocalStorage("rss-backend-feed-id", null);
  const [isSaving, setIsSaving] = useState(false);
  const [apiError, setApiError] = useState("");
  const [apiSuccess, setApiSuccess] = useState("");

  useEffect(() => {
    document.title = feed.title
      ? feed.title + " | RSS Feed Generator"
      : "RSS Feed Generator";
  }, [feed.title]);

  // Generated XML goes stale as soon as the feed or its items change.
  useEffect(() => {
    setRssOutput("");
    setApiSuccess("");
  }, [feed, items]);

  const isSample = items.length === 0;

  const visibleItems = useMemo(
    () => (isSample ? sampleItems : items),
    [isSample, items]
  );

  const feedErrors = useMemo(
    () => (feedSubmitted ? validateFeed(feed, items) : {}),
    [feedSubmitted, feed, items]
  );

  function handleFeedChange(event) {
    const { name, value } = event.target;
    setFeed((prev) => ({ ...prev, [name]: value }));
  }

  function addItem(item) {
    setItems((prev) => [...prev, { ...cleanItem(item), id: createId() }]);
  }

  function updateItem(id, item) {
    setItems((prev) =>
      prev.map((existing) =>
        existing.id === id ? { ...existing, ...cleanItem(item) } : existing
      )
    );
    setEditingId(null);
  }

  function removeItem(id) {
    setItems((prev) => prev.filter((item) => item.id !== id));
    setEditingId((current) => (current === id ? null : current));
  }

  function startEdit(id) {
    setEditingId(id);
  }

  function cancelEdit() {
    setEditingId(null);
  }

  // Returns true when the backend saved the feed and returned XML.
  async function generateRss() {
    setFeedSubmitted(true);
    setApiError("");

    if (Object.keys(validateFeed(feed, items)).length > 0) {
      return false;
    }

    setIsSaving(true);

    try {
      const savedFeed = await createFeed({ ...feed, items });
      const xml = await getFeedRss(savedFeed.id);
      setBackendFeedId(savedFeed.id);
      setRssOutput(xml);
      setApiSuccess("Feed saved to Spring Boot and RSS XML generated.");
      return true;
    } catch (error) {
      setApiError(
        error.message ||
          "Could not connect to the Spring Boot backend. Check that it is running."
      );
      return false;
    } finally {
      setIsSaving(false);
    }
  }

  const value = {
    feed,
    sampleFeed,
    items,
    visibleItems,
    isSample,
    rssOutput,
    backendFeedId,
    feedErrors,
    editingId,
    isSaving,
    apiError,
    apiSuccess,
    handleFeedChange,
    addItem,
    updateItem,
    removeItem,
    startEdit,
    cancelEdit,
    generateRss,
  };

  return <FeedContext.Provider value={value}>{children}</FeedContext.Provider>;
}

export function useFeed() {
  return useContext(FeedContext);
}
