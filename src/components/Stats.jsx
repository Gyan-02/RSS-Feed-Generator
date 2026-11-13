import { useMemo } from "react";
import { useFeed } from "../context/FeedContext.jsx";

function countWords(text) {
  return text.trim().split(/\s+/).filter(Boolean).length;
}

function Stats() {
  const { items } = useFeed();

  const { itemCount, wordCount } = useMemo(
    () => ({
      itemCount: items.length,
      wordCount: items.reduce(
        (total, item) =>
          total + countWords(item.title) + countWords(item.description),
        0
      ),
    }),
    [items]
  );

  return (
    <div className="stats" aria-label="Feed statistics">
      <div>
        <strong>{itemCount}</strong>
        <span>{itemCount === 1 ? "Item" : "Items"}</span>
      </div>
      <div>
        <strong>{wordCount}</strong>
        <span>{wordCount === 1 ? "Word" : "Words"}</span>
      </div>
    </div>
  );
}

export default Stats;
